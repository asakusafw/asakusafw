/**
 * Copyright 2011 Asakusa Framework Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.asakusafw.bulkloader.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.asakusafw.bulkloader.bean.ImportBean;
import com.asakusafw.bulkloader.bean.ImportTargetTableBean;
import com.asakusafw.bulkloader.common.ConfigurationLoader;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.common.FileCompType;
import com.asakusafw.bulkloader.common.FileNameUtil;
import com.asakusafw.bulkloader.common.MessageIdConst;
import com.asakusafw.bulkloader.common.StreamRedirectThread;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.log.Log;


/**
 * Import対象ファイルをDBサーバからHDFSのNameノードへ送信するクラス。
 * @author yuta.shirai
 */
public class ImportFileSend {

    /**
     * Import対象ファイルをHDFSのNameノードへ送信する。
     * TSV形式のImport対象ファイルを読み込み、
     * ZIP形式に圧縮してNameノードの標準入力へsshで送信する。
     * @param bean パラメータを保持するBean
     * @return Import対象ファイル送信結果（true:成功、false:失敗）
     */
    public boolean sendImportFile(ImportBean bean) {
        Process process = null;
        OutputStream os = null;
        ZipOutputStream zos = null;
        InputStream fis = null;
        int exitCode = 0;

        // SSH起動の為の情報を取得
        String sshPath = ConfigurationLoader.getProperty(Constants.PROP_KEY_SSH_PATH);
        String nameNodeIp = ConfigurationLoader.getProperty(Constants.PROP_KEY_NAMENODE_HOST);
        String nameNodeUser = ConfigurationLoader.getProperty(Constants.PROP_KEY_NAMENODE_USER);
        String extractorShellName = ConfigurationLoader.getProperty(Constants.PROP_KEY_EXT_SHELL_NAME);
        String variableTable = Constants.createVariableTable().toSerialString();
        // ZIP圧縮に関する情報を取得
        String strCompType = ConfigurationLoader.getProperty(Constants.PROP_KEY_IMP_FILE_COMP_TYPE);
        FileCompType compType = FileCompType.find(strCompType);

        Log.log(this.getClass(), MessageIdConst.IMP_START_SUB_PROCESS,
                sshPath,
                nameNodeIp,
                nameNodeUser,
                extractorShellName,
                bean.getTargetName(),
                bean.getBatchId(),
                bean.getJobflowId(),
                bean.getExecutionId());

        try {
            // SSHのプロセスを起動し、標準出力のストリームを取得する
            process = createProcess(sshPath,
                    nameNodeIp,
                    nameNodeUser,
                    extractorShellName,
                    bean.getTargetName(),
                    bean.getBatchId(),
                    bean.getJobflowId(),
                    bean.getExecutionId(),
                    variableTable);
            os = process.getOutputStream();

            // 標準入力を別スレッドで読み込んでログ出力する
            StreamRedirectThread outThread = new StreamRedirectThread(process.getInputStream(), System.out);
            outThread.start();
            StreamRedirectThread errThread = new StreamRedirectThread(process.getErrorStream(), System.err);
            errThread.start();

            // SSHプロセスの標準出力ストリームをZipOutputStreamでラップする
            zos = new ZipOutputStream(os);

            // Import対象テーブル毎にファイルの読み込み・書き出しの処理を行う
            List<String> list = bean.getImportTargetTableList();
            for (String tableName : list) {
                ImportTargetTableBean targetTable = bean.getTargetTable(tableName);
                File file = targetTable.getImportFile();
                String fileName = FileNameUtil.createSendImportFileName(tableName);

                Log.log(
                        this.getClass(),
                        MessageIdConst.IMP_FILE_SEND,
                        tableName, file.getAbsolutePath(), fileName, compType.getCompType());

                // ZIPファイルエントリを追加
                ZipEntry ze = new ZipEntry(fileName);
                try {
                    zos.putNextEntry(ze);
                } catch (IOException e) {
                    throw new BulkLoaderSystemException(
                            e,
                            this.getClass(),
                            MessageIdConst.IMP_SENDFILE_EXCEPTION,
                            // TODO MessageFormat.formatの検討
                            "ZIPファイルエントリの追加に失敗。テーブル名："
                            + tableName
                            + " ZIPエントリに追加するファイル名："
                            + fileName);
                }
                if (FileCompType.STORED.equals(compType)) {
                    zos.setLevel(0);
//                    ze.setMethod(ZipOutputStream.STORED);
//                    ze.setCrc(0);
//                    ze.setCompressedSize(file.length());
                }

                // ファイルを読み込んで書き出す
                try {
                    fis = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    throw new BulkLoaderSystemException(
                            e,
                            this.getClass(),
                            MessageIdConst.IMP_SENDFILE_EXCEPTION,
                            // TODO MessageFormat.formatの検討
                            "Importファイルが存在しない。テーブル名："
                            + tableName
                            + " Importファイル名："
                            + file.getPath());
                }

                int buffSize = Integer.parseInt(
                        ConfigurationLoader.getProperty(Constants.PROP_KEY_IMP_FILE_COMP_BUFSIZE));

                byte[] b = new byte[buffSize];
                while (true) {
                    // ファイルを読み込む
                    int read;
                    try {
                        read = fis.read(b);
                        if (read == -1) {
                            break;
                        }
                    } catch (IOException e) {
                        throw new BulkLoaderSystemException(
                                e,
                                this.getClass(),
                                MessageIdConst.IMP_SENDFILE_EXCEPTION,
                                // TODO MessageFormat.formatの検討
                                "Importファイルの読み込みに失敗。テーブル名："
                                + tableName
                                + " Importファイル名："
                                + file.getPath());
                    }
                    // ファイルを書き出す
                    try {
                        zos.write(b, 0, read);
                    } catch (IOException e) {
                        throw new BulkLoaderSystemException(
                                e,
                                this.getClass(),
                                MessageIdConst.IMP_SENDFILE_EXCEPTION,
                                // TODO MessageFormat.formatの検討
                                "Importファイルの書き出しに失敗。テーブル名："
                                + tableName
                                + " Importファイル名："
                                + file.getPath());
                    }
                }
                Log.log(
                        this.getClass(),
                        MessageIdConst.IMP_FILE_SEND_END,
                        tableName, file.getAbsolutePath(), fileName, compType.getCompType());
            }
        } catch (BulkLoaderSystemException e) {
            Log.log(e.getCause(), e.getClazz(), e.getMessageId(), e.getMessageArgs());
            return false;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    // ここで例外が発生した場合は握りつぶす
                    e.printStackTrace();
                }
            }
            if (zos != null) {
                try {
                    zos.close();
                } catch (IOException e) {
                    // ここで例外が発生した場合は握りつぶす
                    e.printStackTrace();
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    // ここで例外が発生した場合は握りつぶす
                    e.printStackTrace();
                }
            }
            // SSHプロセスの実行結果を取得する
            if (process != null) {
                try {
                    exitCode = process.waitFor();
                } catch (InterruptedException e) {
                    // ここで例外が発生した場合は握りつぶす
                    e.printStackTrace();
                    process.destroy();
                }
            } else {
                exitCode = -1;
            }
        }
        // サブプロセスの終了コードを判定して返す
        if (exitCode == 0) {
            return true;
        } else {
            Log.log(this.getClass(), MessageIdConst.IMP_EXTRACTOR_ERROR, exitCode);
            return false;
        }
    }
    /**
     * サブプロセスを生成して返す。
     * @param sshPath SSHコマンドへのパス文字列
     * @param extractorHost Collectorを起動するノードのホスト名またはIPアドレス
     * @param extractorUser Collectorを起動するノードの実行ユーザー名
     * @param shellName Collectorを起動するノードで実行するシェルの名前
     * @param targetName 処理中のターゲット名
     * @param batchId 処理中のバッチID
     * @param jobflowId 処理中のジョブフローID
     * @param executionId 処理中の実行ID
     * @param bulkloaderArgs 変数表の文字列表現
     * @return Collectorとの通信を行うプロセス
     * @throws BulkLoaderSystemException プロセスの生成に失敗した場合
     */
    protected Process createProcess(
            String sshPath,
            String extractorHost,
            String extractorUser,
            String shellName,
            String targetName,
            String batchId,
            String jobflowId,
            String executionId,
            String bulkloaderArgs) throws BulkLoaderSystemException {
        ProcessBuilder builder = new ProcessBuilder(
                sshPath, "-l", extractorUser, extractorHost,
                shellName, targetName, batchId, jobflowId, executionId, bulkloaderArgs);
        try {
            return builder.start();
        } catch (IOException e) {
            throw new BulkLoaderSystemException(
                    e,
                    this.getClass(),
                    MessageIdConst.IMP_SENDFILE_EXCEPTION,
                    "サブプロセスの生成に失敗");
        }
    }
}
