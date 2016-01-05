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
package com.asakusafw.bulkloader.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.asakusafw.bulkloader.bean.ImportBean;
import com.asakusafw.bulkloader.bean.ImportTargetTableBean;
import com.asakusafw.bulkloader.common.ConfigurationLoader;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.common.FileCompType;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.log.Log;
import com.asakusafw.bulkloader.transfer.FileList;
import com.asakusafw.bulkloader.transfer.FileListProvider;
import com.asakusafw.bulkloader.transfer.RemoteFileListProviderFactory;
import com.asakusafw.bulkloader.transfer.FileProtocol;
import com.asakusafw.runtime.core.context.RuntimeContext;

/**
 * Import対象ファイルをDBサーバからHDFSのNameノードへ送信するクラス。
 * @author yuta.shirai
 */
public class ImportFileSend {

    static final Log LOG = new Log(ImportFileSend.class);

    /**
     * Import対象ファイルをHDFSのNameノードへ送信する。
     * TSV形式のImport対象ファイルを読み込み、
     * ZIP形式に圧縮してNameノードの標準入力へsshで送信する。
     * @param bean パラメータを保持するBean
     * @return Import対象ファイル送信結果（true:成功、false:失敗）
     */
    public boolean sendImportFile(ImportBean bean) {
        // ZIP圧縮に関する情報を取得
        String strCompType = ConfigurationLoader.getProperty(Constants.PROP_KEY_IMP_FILE_COMP_TYPE);
        FileCompType compType = FileCompType.find(strCompType);

        FileListProvider provider = null;
        FileList.Writer writer = null;

        long totalStartTime = System.currentTimeMillis();
        try {
            provider = openFileList(
                    bean.getTargetName(),
                    bean.getBatchId(),
                    bean.getJobflowId(),
                    bean.getExecutionId());
            provider.discardReader();
            writer = provider.openWriter(compType == FileCompType.DEFLATED);

            // Import対象テーブル毎にファイルの読み込み・書き出しの処理を行う
            List<String> list = arrangeSendOrder(bean);
            for (String tableName : list) {
                long tableStartTime = System.currentTimeMillis();
                ImportTargetTableBean targetTable = bean.getTargetTable(tableName);
                LOG.info("TG-IMPORTER-04004",
                        tableName,
                        targetTable.getImportFile().getAbsolutePath(),
                        compType.getSymbol());
                long dumpFileSize = sendTableFile(writer, tableName, targetTable);
                LOG.info("TG-PROFILE-02003",
                        bean.getTargetName(),
                        bean.getBatchId(),
                        bean.getJobflowId(),
                        bean.getExecutionId(),
                        tableName,
                        dumpFileSize,
                        System.currentTimeMillis() - tableStartTime);
                LOG.info("TG-IMPORTER-04005",
                        tableName,
                        targetTable.getImportFile().getAbsolutePath(),
                        compType.getSymbol());
            }
            writer.close();
            provider.waitForComplete();
            LOG.info("TG-PROFILE-02001",
                    bean.getTargetName(),
                    bean.getBatchId(),
                    bean.getJobflowId(),
                    bean.getExecutionId(),
                    writer.getByteCount(),
                    System.currentTimeMillis() - totalStartTime);
        } catch (BulkLoaderSystemException e) {
            LOG.log(e);
            return false;
        } catch (Exception e) {
            LOG.error(e, "TG-IMPORTER-04002");
            return false;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
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

    private List<String> arrangeSendOrder(ImportBean bean) {
        assert bean != null;
        final Map<String, ImportTargetTableBean> tables = new HashMap<String, ImportTargetTableBean>();
        final Map<String, Long> sizes = new HashMap<String, Long>();
        List<String> tableNames = new ArrayList<String>(bean.getImportTargetTableList());
        for (String tableName : tableNames) {
            ImportTargetTableBean tableBean = bean.getTargetTable(tableName);
            tables.put(tableName, tableBean);
            sizes.put(tableName, tableBean.getImportFile().length());
        }
        Collections.sort(tableNames, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                ImportTargetTableBean t1 = tables.get(o1);
                ImportTargetTableBean t2 = tables.get(o2);

                // put cached table on top
                if (t1.getCacheId() != null && t2.getCacheId() == null) {
                    return -1;
                } else if (t1.getCacheId() == null && t2.getCacheId() != null) {
                    return +1;
                }

                // put large file on top
                long s1 = sizes.get(o1);
                long s2 = sizes.get(o2);
                if (s1 > s2) {
                    return -1;
                } else if (s1 < s2) {
                    return +1;
                }

                // sort by its table name
                return o1.compareTo(o2);
            }
        });
        return tableNames;
    }

    private long sendTableFile(
            FileList.Writer writer,
            String tableName,
            ImportTargetTableBean targetTable) throws BulkLoaderSystemException {
        assert writer != null;
        assert tableName != null;
        assert targetTable != null;
        File localFile = targetTable.getImportFile();
        int buffSize = Integer.parseInt(ConfigurationLoader.getProperty(Constants.PROP_KEY_IMP_FILE_COMP_BUFSIZE));
        byte[] buf = new byte[buffSize];
        long dumpFileSize = 0;
        try {
            InputStream input = new FileInputStream(localFile);
            try {
                FileProtocol protocol = targetTable.getImportProtocol();
                assert protocol != null;
                OutputStream output = writer.openNext(protocol);
                try {
                    while (true) {
                        int read = input.read(buf);
                        if (read < 0) {
                            break;
                        }
                        dumpFileSize += read;
                        output.write(buf, 0, read);
                    }
                } finally {
                    output.close();
                }
            } finally {
                input.close();
            }
        } catch (IOException e) {
            throw new BulkLoaderSystemException(e, getClass(), "TG-IMPORTER-04001",
                    MessageFormat.format(
                            "Importファイルの転送に失敗。テーブル名：{0}, Importファイル名： {1}",
                            tableName,
                            localFile.getPath()));
        }
        return dumpFileSize;
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
        String scriptPath = ConfigurationLoader.getRemoteScriptPath(Constants.PATH_REMOTE_EXTRACTOR);
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

        LOG.info("TG-IMPORTER-04003",
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
}
