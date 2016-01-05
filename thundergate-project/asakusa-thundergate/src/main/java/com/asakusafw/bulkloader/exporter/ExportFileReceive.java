/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.asakusafw.bulkloader.bean.ExporterBean;
import com.asakusafw.bulkloader.common.ConfigurationLoader;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.common.FileNameUtil;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.log.Log;
import com.asakusafw.bulkloader.transfer.FileList;
import com.asakusafw.bulkloader.transfer.FileListProvider;
import com.asakusafw.bulkloader.transfer.FileProtocol;
import com.asakusafw.bulkloader.transfer.RemoteFileListProviderFactory;
import com.asakusafw.runtime.core.context.RuntimeContext;


/**
 * Exportファイルを取得するクラス。
 *
 * @author yuta.shirai
 */
public class ExportFileReceive {

    static final Log LOG = new Log(ExportFileReceive.class);

    // CHECKSTYLE:OFF MethodLengthCheck - FIXME refactoring
    /**
     * HDFSのNameノードのCollectorを呼出し、Exportファイルを受信してローカルにファイルを書き出す。
     * @param bean パラメータを保持するBean
     * @return Exportファイル取得結果（true:成功、false:失敗）
     */
    public boolean receiveFile(ExporterBean bean) {
        // Exportファイルを置くディレクトリ名を作成
        File fileDirectry = new File(ConfigurationLoader.getProperty(Constants.PROP_KEY_EXP_FILE_DIR));
        if (!FileNameUtil.prepareTemporaryDirectory(fileDirectry)) {
            // ディレクトリが存在しない場合は異常終了する。
            LOG.error("TG-EXPORTER-02001", fileDirectry.getAbsolutePath());
            return false;
        }

        FileListProvider provider = null;
        FileList.Reader reader = null;
        long totalStartTime = System.currentTimeMillis();
        try {
            provider = openFileList(
                    bean.getTargetName(),
                    bean.getBatchId(),
                    bean.getJobflowId(),
                    bean.getExecutionId());
            provider.discardWriter();
            reader = provider.openReader();

            // FileListの終端まで繰り返す
            int fileSeq = 0;

            // プロファイル用のテーブル
            Map<String, TableTransferProfile> profiles = new TreeMap<String, TableTransferProfile>();

            while (reader.next()) {
                FileProtocol protocol = reader.getCurrentProtocol();
                assert protocol.getKind() == FileProtocol.Kind.CONTENT;

                // ファイル名を取得
                String fileName = protocol.getLocation();

                // テーブル名を取得
                String tableName = FileNameUtil.getExportTableName(fileName);
                if (tableName == null) {
                    LOG.error("TG-EXPORTER-02003", fileName, "(Unknown)");
                    return false;
                } else if (bean.getExportTargetTable(tableName) == null) {
                    LOG.error("TG-EXPORTER-02003", fileName, tableName);
                    return false;
                }

                // プロファイル情報の引当
                TableTransferProfile profile = profiles.get(tableName);
                if (profile == null) {
                    profile = new TableTransferProfile(tableName);
                    profiles.put(tableName, profile);
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
                LOG.info("TG-EXPORTER-02008", tableName, file.getAbsolutePath());

                long dumpStartTime = System.currentTimeMillis();
                long dumpFileSize = 0;

                int byteSize = Integer.parseInt(
                        ConfigurationLoader.getProperty(Constants.PROP_KEY_EXP_FILE_COMP_BUFSIZE));
                byte[] b = new byte[byteSize];

                InputStream content = reader.openContent();
                OutputStream fos = null;
                try {
                    fos = createFos(file);
                    while (true) {
                        int read;
                        try {
                            read = content.read(b);
                        } catch (IOException e) {
                            throw new BulkLoaderSystemException(e, getClass(), "TG-EXPORTER-02002",
                                    "Exportファイルの読み込みに失敗。エントリ名：" + protocol.getLocation());
                        }
                        // 入力ファイルの終端を察知する
                        if (read < 0) {
                            break;
                        }
                        dumpFileSize += read;
                        // ファイルを書き出す
                        try {
                            fos.write(b, 0, read);
                        } catch (IOException e) {
                            throw new BulkLoaderSystemException(e, getClass(), "TG-EXPORTER-02002",
                                    "Exportファイルの書き出しに失敗。ファイル名：" +  file.getName());
                        }
                    }
                    // ファイル名を設定する
                    bean.getExportTargetTable(tableName).addExportFile(file);

                    // プロファイル情報を加算する
                    profile.elapsedTime += System.currentTimeMillis() - dumpStartTime;
                    profile.fileSize += dumpFileSize;

                    LOG.info("TG-EXPORTER-02009",
                            tableName, file.getAbsolutePath());

                } finally {
                    try {
                        content.close();
                    } catch (IOException e) {
                        // ここで例外が発生した場合は握りつぶす
                        e.printStackTrace();
                    }
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
            for (TableTransferProfile profile : profiles.values()) {
                LOG.info("TG-PROFILE-02004",
                        bean.getTargetName(),
                        bean.getBatchId(),
                        bean.getJobflowId(),
                        bean.getExecutionId(),
                        profile.tableName,
                        profile.fileSize,
                        profile.elapsedTime);
            }
            reader.close();
            provider.waitForComplete();
            LOG.info("TG-PROFILE-02002",
                    bean.getTargetName(),
                    bean.getBatchId(),
                    bean.getJobflowId(),
                    bean.getExecutionId(),
                    reader.getByteCount(),
                    System.currentTimeMillis() - totalStartTime);
        } catch (BulkLoaderSystemException e) {
            LOG.log(e);
            return false;
        } catch (Exception e) {
            LOG.error(e, "TG-EXPORTER-02002",
                    "Exportファイルの読み込みに失敗。");
            return false;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                    ignored.printStackTrace();
                }
            }
            if (provider != null) {
                try {
                    provider.close();
                } catch (IOException ignored) {
                    ignored.printStackTrace();
                }
            }
        }
        return true;
    }
    // CHECKSTYLE:ON MethodLengthCheck

    /**
     * ファイルに対するアウトプットストリームを作成する。
     * 既にファイルが存在する場合は削除する。
     * @param file 出力先のファイル
     * @return 指定のファイルに出力するためのストリーム
     * @throws BulkLoaderSystemException IO例外が発生した場合
     */
    private OutputStream createFos(File file) throws BulkLoaderSystemException {
        if (file.exists()) {
            if (!file.delete()) {
                // ファイルの削除に失敗した場合は異常終了する
                throw new BulkLoaderSystemException(getClass(), "TG-EXPORTER-02004",
                        file.getName());
            }
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new BulkLoaderSystemException(e, getClass(), "TG-EXPORTER-02002",
                    "Exportファイルの指定が不正。ファイル名：" +  file.getName());
        }
        return fos;
    }

    /**
     * Opens a new {@link FileListProvider}.
     * @param targetName current target name
     * @param batchId current batch ID
     * @param jobflowId current jobflow ID
     * @param executionId current execution ID
     * @return the created provider
     * @throws IOException if failed to open the file list
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    protected FileListProvider openFileList(
            String targetName,
            String batchId,
            String jobflowId,
            String executionId) throws IOException {
        if (targetName == null) {
            throw new IllegalArgumentException("targetName must not be null"); //$NON-NLS-1$
        }
        if (batchId == null) {
            throw new IllegalArgumentException("batchId must not be null"); //$NON-NLS-1$
        }
        if (jobflowId == null) {
            throw new IllegalArgumentException("jobflowId must not be null"); //$NON-NLS-1$
        }
        if (executionId == null) {
            throw new IllegalArgumentException("executionId must not be null"); //$NON-NLS-1$
        }
        String sshPath = ConfigurationLoader.getProperty(Constants.PROP_KEY_SSH_PATH);
        String hostName = ConfigurationLoader.getProperty(Constants.PROP_KEY_NAMENODE_HOST);
        String userName = ConfigurationLoader.getProperty(Constants.PROP_KEY_NAMENODE_USER);
        String scriptPath = ConfigurationLoader.getRemoteScriptPath(Constants.PATH_REMOTE_COLLECTOR);
        String variableTable = Constants.createVariableTable().toSerialString();
        List<String> command = new ArrayList<String>();
        command.add(scriptPath);
        command.add(targetName);
        command.add(batchId);
        command.add(jobflowId);
        command.add(executionId);
        command.add(variableTable);

        Map<String, String> env = new HashMap<String, String>();
        env.putAll(ConfigurationLoader.getPropSubMap(Constants.PROP_PREFIX_HC_ENV));
        env.putAll(RuntimeContext.get().unapply());

        LOG.info("TG-EXPORTER-02007",
                sshPath,
                hostName,
                userName,
                scriptPath,
                targetName,
                batchId,
                jobflowId,
                executionId);

        return new RemoteFileListProviderFactory(sshPath, hostName, userName).newInstance(command, env);
    }

    private static final class TableTransferProfile {

        TableTransferProfile(String tableName) {
            assert tableName != null;
            this.tableName = tableName;
        }

        String tableName;

        long fileSize;

        long elapsedTime;
    }
}
