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
package com.asakusafw.bulkloader.exporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.asakusafw.bulkloader.bean.ExporterBean;
import com.asakusafw.bulkloader.common.ConfigurationLoader;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.common.FileNameUtil;
import com.asakusafw.bulkloader.common.MessageIdConst;
import com.asakusafw.bulkloader.common.StreamRedirectThread;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.log.Log;


/**
 * Exportファイルを取得するクラス。
 *
 * @author yuta.shirai
 */
public class ExportFileReceive {

    /**
     * HDFSのNameノードのCollectorを呼出し、Exportファイルを受信してローカルにファイルを書き出す。
     * @param bean パラメータを保持するBean
     * @return Exportファイル取得結果（true:成功、false:失敗）
     */
    public boolean receiveFile(ExporterBean bean) {
        Process process = null;
        InputStream is = null;
        ZipInputStream zIs = null;
        int exitCode = 0;

        // SSH起動の為の情報を取得
        String sshPath = ConfigurationLoader.getProperty(Constants.PROP_KEY_SSH_PATH);
        String nameNodeIp = ConfigurationLoader.getProperty(Constants.PROP_KEY_NAMENODE_HOST);
        String nameNodeUser = ConfigurationLoader.getProperty(Constants.PROP_KEY_NAMENODE_USER);
        String collectorShellName = ConfigurationLoader.getProperty(Constants.PROP_KEY_COL_SHELL_NAME);
        String variableTable = Constants.createVariableTable().toSerialString();

        // Exportファイルを置くディレクトリ名を作成
        File fileDirectry = new File(ConfigurationLoader.getProperty(Constants.PROP_KEY_EXP_FILE_DIR));
        if (!fileDirectry.exists()) {
            // ディレクトリが存在しない場合は異常終了する。
            Log.log(this.getClass(), MessageIdConst.EXP_DIR_NOT_EXISTS_ERROR, fileDirectry.getAbsolutePath());
            return false;
        }

        try {
            // SSHのプロセスを起動し、標準入力のストリームを取得する
            Log.log(this.getClass(), MessageIdConst.EXP_START_SUB_PROCESS,
                    sshPath,
                    nameNodeIp,
                    nameNodeUser,
                    collectorShellName,
                    bean.getTargetName(),
                    bean.getBatchId(),
                    bean.getJobflowId(),
                    bean.getExecutionId());

            process = createProcess(
                    sshPath,
                    nameNodeIp,
                    nameNodeUser,
                    collectorShellName,
                    bean.getTargetName(),
                    bean.getBatchId(),
                    bean.getJobflowId(),
                    bean.getExecutionId(),
                    variableTable);

            // 標準エラー入力を別スレッドで読み込んでログ出力する
            StreamRedirectThread thread = new StreamRedirectThread(process.getErrorStream(), System.err);
            thread.start();

            // インプットストリームを生成
            is = process.getInputStream();
            zIs = new ZipInputStream(is);

            // ZIPファイルの終端まで繰り返す
            ZipEntry zipEntry = null;

            int fileSeq = 0;

            while ((zipEntry = zIs.getNextEntry()) != null) {
                // ファイル名を取得
                String fileName = zipEntry.getName().replace(File.separatorChar, '/');
                // ファイル名がダミーファイルの場合は無視する
                if (Constants.EXPORT_FILE_NOT_FOUND.equals(fileName)) {
                    continue;
                }
                // テーブル名を取得
                String tableName = FileNameUtil.getExportTableName(fileName);
                if (tableName == null) {
                    Log.log(this.getClass(), MessageIdConst.EXP_DSL_NOTFOUND, fileName, "(Unknown)");
                    return false;
                } else if (bean.getExportTargetTable(tableName) == null) {
                    Log.log(this.getClass(), MessageIdConst.EXP_DSL_NOTFOUND, fileName, tableName);
                    return false;
                }

                // ファイル名を作成
                File file = FileNameUtil.createExportFilePath(
                        fileDirectry,
                        bean.getTargetName(),
                        bean.getJobflowId(),
                        bean.getExecutionId(),
                        tableName,
                        fileSeq++);

                // ファイルを読み込んでローカルファイルに書き込む
                Log.log(this.getClass(), MessageIdConst.EXP_FILERECEIV, tableName, file.getAbsolutePath());

                FileOutputStream fos = null;
                try {
                    fos = createFos(file);
                    while (true) {
                        // ファイルを読み込む
                        int byteSize = Integer.parseInt(
                                ConfigurationLoader.getProperty(Constants.PROP_KEY_EXP_FILE_COMP_BUFSIZE));
                        byte[] b = new byte[byteSize];
                        int read;
                        try {
                            read = zIs.read(b);
                        } catch (IOException e) {
                            throw new BulkLoaderSystemException(
                                    e,
                                    this.getClass(),
                                    MessageIdConst.EXP_FILERECEIV_EXCEPTION,
                                    "Exportファイルの読み込みに失敗。エントリ名：" + zipEntry.getName());
                        }
                        // 入力ファイルの終端を察知する
                        if (read == -1) {
                            break;
                        }
                        // ファイルを書き出す
                        try {
                            fos.write(b, 0, read);
                        } catch (IOException e) {
                            throw new BulkLoaderSystemException(
                                    e,
                                    this.getClass(),
                                    MessageIdConst.EXP_FILERECEIV_EXCEPTION,
                                    "Exportファイルの書き出しに失敗。ファイル名：" +  file.getName());
                        }
                    }
                    // ファイル名を設定する
                    bean.getExportTargetTable(tableName).addExportFile(file);
                    Log.log(
                            this.getClass(),
                            MessageIdConst.EXP_FILERECEIV_SUCCESS,
                            tableName, file.getAbsolutePath());

                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            // ここで例外が発生した場合は握りつぶす
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (BulkLoaderSystemException e) {
            Log.log(e.getCause(), e.getClazz(), e.getMessageId(), e.getMessageArgs());
            return false;
        } catch (IOException e) {
            Log.log(
                    e,
                    this.getClass(),
                    MessageIdConst.EXP_FILERECEIV_EXCEPTION,
                    "Exportファイルの読み込みに失敗。");
            return false;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // ここで例外が発生した場合は握りつぶす
                    e.printStackTrace();
                }
            }
            if (zIs != null) {
                try {
                    zIs.close();
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
                    exitCode = -1;
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
            Log.log(this.getClass(), MessageIdConst.EXP_COLLECTOR_ERROR, exitCode);
            return false;
        }

    }

    /**
     * ファイルに対するアウトプットストリームを作成する。
     * 既にファイルが存在する場合は削除する。
     * @param file 出力先のファイル
     * @return 指定のファイルに出力するためのストリーム
     * @throws BulkLoaderSystemException IO例外が発生した場合
     */
    private FileOutputStream createFos(File file) throws BulkLoaderSystemException {
        if (file.exists()) {
            if (!file.delete()) {
                // ファイルの削除に失敗した場合は異常終了する
                throw new BulkLoaderSystemException(
                        this.getClass(),
                        MessageIdConst.EXP_DELETEFILE_FAILED,
                        file.getName());
            }
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new BulkLoaderSystemException(
                    e,
                    this.getClass(),
                    MessageIdConst.EXP_FILERECEIV_EXCEPTION,
                    "Exportファイルの指定が不正。ファイル名：" +  file.getName());
        }
        return fos;
    }

    /**
     * サブプロセスを生成して返す。
     * @param sshPath SSHコマンドへのパス文字列
     * @param collectorHost Collectorを起動するノードのホスト名またはIPアドレス
     * @param collectorUser Collectorを起動するノードの実行ユーザー名
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
            String collectorHost,
            String collectorUser,
            String shellName,
            String targetName,
            String batchId,
            String jobflowId,
            String executionId,
            String bulkloaderArgs) throws BulkLoaderSystemException {
        ProcessBuilder builder = new ProcessBuilder(sshPath,
                "-l", collectorUser, collectorHost, shellName,
                targetName, batchId, jobflowId, executionId, bulkloaderArgs);
        Process process = null;
        try {
            process = builder.start();
        } catch (IOException e) {
            throw new BulkLoaderSystemException(
                    e,
                    this.getClass(),
                    MessageIdConst.EXP_FILERECEIV_EXCEPTION,
                    // TODO MessageFormat.formatの検討
                    "サブプロセスの開始に失敗。SSHのパス：" +  sshPath
                    + " マスターノードのホスト名：" + collectorHost
                    + " マスターノードのユーザー名：" + collectorUser
                    + " Collectorのシェルスクリプトのパス：" + shellName
                    + " ターゲット名" + targetName
                    + " バッチID：" + batchId
                    + " ジョブフローID：" + jobflowId
                    + " 実行時ID：" + executionId
                    + " 変数表：" + bulkloaderArgs);
        }
        return process;
    }
}
