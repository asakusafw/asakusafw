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
package com.asakusafw.bulkloader.extractor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile.CompressionType;

import com.asakusafw.bulkloader.bean.ImportBean;
import com.asakusafw.bulkloader.bean.ImportTargetTableBean;
import com.asakusafw.bulkloader.common.ConfigurationLoader;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.common.FileNameUtil;
import com.asakusafw.bulkloader.common.MultiThreadedCopier;
import com.asakusafw.bulkloader.common.StreamRedirectThread;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.log.Log;
import com.asakusafw.bulkloader.transfer.FileList;
import com.asakusafw.bulkloader.transfer.FileProtocol;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.TsvIoFactory;
import com.asakusafw.runtime.stage.temporary.TemporaryStorage;
import com.asakusafw.thundergate.runtime.cache.CacheInfo;
import com.asakusafw.thundergate.runtime.cache.CacheStorage;
import com.asakusafw.thundergate.runtime.cache.mapreduce.CacheBuildClient;

/**
 * 標準入力を読み込んで入力のためのデータを書き出すクラス。
 * @author yuta.shirai
 */
public class DfsFileImport {

    static final Log LOG = new Log(DfsFileImport.class);

    private static final int INPUT_BUFFER_BYTES = 128 * 1024;

    private static final int COPY_BUFFER_RECORDS = 1000;

    private final ExecutorService executor;

    private final String cacheBuildCommand;

    /**
     * Creates a new instance.
     */
    public DfsFileImport() {
        File cmd = ConfigurationLoader.getLocalScriptPath(Constants.PATH_LOCAL_CACHE_BUILD);
        this.cacheBuildCommand = cmd.getAbsolutePath();
        int parallel = Integer.parseInt(ConfigurationLoader.getProperty(Constants.PROP_KEY_CACHE_BUILDER_PARALLEL));
        LOG.debugMessage("Building a cache builder with {0} threads", parallel);
        this.executor = Executors.newFixedThreadPool(parallel);
    }

    /**
     * 標準入力を読み込んでDFSにファイルを書き出す。
     * {@link FileList}形式で受け取ったTSV形式のファイルをModelオブジェクトに変換して、テンポラリ入力データとして配置する。
     * 出力先はプロトコルの形式によって異なる。
     * 利用可能なプロトコルは以下のとおり。
     * <ul>
     * <li> {@link com.asakusafw.bulkloader.transfer.FileProtocol.Kind#CONTENT} </li>
     * <li> {@link com.asakusafw.bulkloader.transfer.FileProtocol.Kind#CREATE_CACHE} </li>
     * <li> {@link com.asakusafw.bulkloader.transfer.FileProtocol.Kind#UPDATE_CACHE} </li>
     * </ul>
     * @param bean パラメータを保持するBean
     * @param user OSのユーザー名
     * @return 出力結果（true：正常終了、false：異常終了）
     */
    public boolean importFile(ImportBean bean, String user) {
        // 標準入力を取得
        FileList.Reader reader;
        try {
            reader = FileList.createReader(getInputStream());
        } catch (IOException e) {
            LOG.error(e, "TG-EXTRACTOR-02001",
                    "標準入力からFileListの取得に失敗");
            return false;
        }
        try {
            // FileListの終端まで繰り返す
            List<Future<?>> running = new ArrayList<Future<?>>();
            while (reader.next()) {
                FileProtocol protocol = reader.getCurrentProtocol();
                InputStream content = reader.openContent();
                try {
                    switch (protocol.getKind()) {
                    case CONTENT:
                        importContent(protocol, content, bean, user);
                        break;

                    case CREATE_CACHE:
                    case UPDATE_CACHE:
                        long recordCount = putCachePatch(protocol, content, bean, user);
                        Callable<?> builder = createCacheBuilder(protocol, bean, user, recordCount);
                        if (builder != null) {
                            LOG.debugMessage("Submitting cache builder: {0} {1}",
                                    protocol.getKind(),
                                    protocol.getInfo().getTableName());
                            running.add(executor.submit(builder));
                        }
                        break;

                    default:
                        throw new AssertionError(protocol.getKind());
                    }
                } finally {
                    content.close();
                }
            }

            waitForCompleteTasks(bean, running);
            // 正常終了
            return true;

        } catch (BulkLoaderSystemException e) {
            LOG.log(e);
        } catch (IOException e) {
            // FileListの展開に失敗
            LOG.error(e, "TG-EXTRACTOR-02001",
                    "標準入力からFileListの取得に失敗");
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                // ここで例外が発生した場合は握りつぶす
                e.printStackTrace();
            }
        }
        return false;
    }

    private void importContent(
            FileProtocol protocol,
            InputStream content,
            ImportBean bean,
            String user) throws BulkLoaderSystemException {
        assert protocol != null;
        assert content != null;
        assert bean != null;
        assert user != null;
        String tableName = FileNameUtil.getImportTableName(protocol.getLocation());

        ImportTargetTableBean targetTableBean = bean.getTargetTable(tableName);
        if (targetTableBean == null) {
            // 対応するテーブルの定義がDSL存在しない場合異常終了する。
            throw new BulkLoaderSystemException(getClass(), "TG-EXTRACTOR-02001",
                    MessageFormat.format(
                            "エントリに対応するテーブルの定義がDSL存在しない。テーブル名：{0}",
                            tableName));
        }

        URI dfsFilePath = resolveLocation(bean, user, targetTableBean.getDfsFilePath());
        Class<?> targetTableModel = targetTableBean.getImportTargetType();

        LOG.info("TG-EXTRACTOR-02002",
                tableName, dfsFilePath.toString(), targetTableModel.toString());

        // ファイルをジョブ入力データ領域に書き出す
        long recordCount = write(targetTableModel, dfsFilePath, content);

        LOG.info("TG-EXTRACTOR-02003",
                tableName, dfsFilePath.toString(), targetTableModel.toString());
        LOG.info("TG-PROFILE-01002",
                bean.getTargetName(),
                bean.getBatchId(),
                bean.getJobflowId(),
                bean.getExecutionId(),
                tableName,
                recordCount);
    }

    private long putCachePatch(
            FileProtocol protocol,
            InputStream content,
            ImportBean bean,
            String user) throws BulkLoaderSystemException {
        assert protocol != null;
        assert content != null;
        assert bean != null;
        assert user != null;
        assert protocol.getKind() == FileProtocol.Kind.CREATE_CACHE
            || protocol.getKind() == FileProtocol.Kind.UPDATE_CACHE;

        CacheInfo info = protocol.getInfo();
        assert info != null;

        ImportTargetTableBean targetTableBean = bean.getTargetTable(info.getTableName());
        if (targetTableBean == null) {
            // 対応するテーブルの定義がDSL存在しない場合異常終了する。
            throw new BulkLoaderSystemException(getClass(), "TG-EXTRACTOR-02001",
                    MessageFormat.format(
                            "エントリに対応するテーブルの定義がDSL存在しない。テーブル名：{0}",
                            info.getTableName()));
        }

        URI dfsFilePath = resolveLocation(bean, user, protocol.getLocation());
        try {
            CacheStorage storage = new CacheStorage(new Configuration(), dfsFilePath);
            try {
                LOG.info("TG-EXTRACTOR-11001", info.getId(), info.getTableName(), storage.getPatchProperties());
                storage.putPatchCacheInfo(info);
                LOG.info("TG-EXTRACTOR-11002", info.getId(), info.getTableName(), storage.getPatchProperties());

                Class<?> targetTableModel = targetTableBean.getImportTargetType();
                Path targetUri = storage.getPatchContents("0");
                LOG.info("TG-EXTRACTOR-11003", info.getId(), info.getTableName(), targetUri);
                long recordCount = write(targetTableModel, targetUri.toUri(), content);
                LOG.info("TG-EXTRACTOR-11004", info.getId(), info.getTableName(), targetUri, recordCount);
                LOG.info("TG-PROFILE-01002",
                        bean.getTargetName(),
                        bean.getBatchId(),
                        bean.getJobflowId(),
                        bean.getExecutionId(),
                        info.getTableName(),
                        recordCount);
                return recordCount;
            } finally {
                storage.close();
            }
        } catch (IOException e) {
            throw new BulkLoaderSystemException(e, getClass(), "TG-EXTRACTOR-11005",
                    info.getId(), info.getTableName(), dfsFilePath);
        }
    }

    private Callable<?> createCacheBuilder(
            FileProtocol protocol,
            ImportBean bean,
            String user,
            long recordCount) throws BulkLoaderSystemException {
        assert protocol != null;
        assert bean != null;
        assert user != null;
        CacheInfo info = protocol.getInfo();
        URI location = resolveLocation(bean, user, protocol.getLocation());
        assert info != null;
        try {
            switch (protocol.getKind()) {
            case CREATE_CACHE:
                return createCacheBuilder(CacheBuildClient.SUBCOMMAND_CREATE, bean, location, info);
            case UPDATE_CACHE:
                if (recordCount > 0) {
                    return createCacheBuilder(CacheBuildClient.SUBCOMMAND_UPDATE, bean, location, info);
                } else {
                    return null;
                }
            default:
                throw new AssertionError(protocol);
            }
        } catch (IOException e) {
            throw new BulkLoaderSystemException(e, getClass(), "TG-EXTRACTOR-12002",
                    protocol.getKind(),
                    info.getId(),
                    info.getTableName(),
                    bean.getTargetName(),
                    bean.getBatchId(),
                    bean.getJobflowId(),
                    bean.getExecutionId());
        }
    }

    /**
     * Creates a cache builder for the specified cache (of candidate).
     * @param subcommand subcommand name
     * @param bean current importer script
     * @param location cache location
     * @param info cache information
     * @return the future object of the execution, or {@code null} if nothing to do
     * @throws IOException if failed to start execution
     */
    protected Callable<?> createCacheBuilder(
            final String subcommand,
            ImportBean bean,
            final URI location,
            final CacheInfo info) throws IOException {
        assert subcommand != null;
        assert bean != null;
        assert location != null;
        assert info != null;

        List<String> command = new ArrayList<String>();
        command.add(cacheBuildCommand);
        command.add(subcommand);
        command.add(bean.getBatchId());
        command.add(bean.getJobflowId());
        command.add(bean.getExecutionId());
        command.add(location.toString());
        command.add(info.getModelClassName());

        LOG.info("TG-EXTRACTOR-12001",
                subcommand,
                info.getId(),
                info.getTableName(),
                bean.getTargetName(),
                bean.getBatchId(),
                bean.getJobflowId(),
                bean.getExecutionId(),
                command);

        final ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(new File(System.getProperty("user.home", ".")));
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                LOG.info("TG-EXTRACTOR-12003", subcommand, info.getId(), info.getTableName());
                Process process = builder.start();
                try {
                    Thread stdout = new StreamRedirectThread(process.getInputStream(), System.out);
                    stdout.setDaemon(true);
                    stdout.start();
                    Thread stderr = new StreamRedirectThread(process.getErrorStream(), System.err);
                    stderr.setDaemon(true);
                    stderr.start();
                    stdout.join();
                    stderr.join();
                    int exitCode = process.waitFor();
                    if (exitCode != 0) {
                        throw new IOException(MessageFormat.format(
                                "Cache builder returns unexpected exit code: {0}",
                                exitCode));
                    }
                    LOG.info("TG-EXTRACTOR-12004", subcommand, info.getId(), info.getTableName());
                } catch (Exception e) {
                    throw new BulkLoaderSystemException(e, DfsFileImport.class, "TG-EXTRACTOR-12005",
                            subcommand,
                            info.getId(),
                            info.getTableName());
                } finally {
                    process.destroy();
                }
                return null;
            }
        };
    }

    /**
     * Resolves target location.
     * @param bean importer bean
     * @param user current user name
     * @param location target location
     * @return the resolved location
     * @throws BulkLoaderSystemException if failed to resolve
     */
    protected URI resolveLocation(ImportBean bean, String user, String location) throws BulkLoaderSystemException {
        Configuration conf = new Configuration();
        URI dfsFilePath = FileNameUtil.createPath(conf, location, bean.getExecutionId(), user).toUri();
        return dfsFilePath;
    }

    private void waitForCompleteTasks(ImportBean bean, List<Future<?>> running) throws BulkLoaderSystemException {
        assert bean != null;
        assert running != null;
        if (running.isEmpty()) {
            return;
        }
        LOG.info("TG-EXTRACTOR-12006",
                bean.getTargetName(),
                bean.getBatchId(),
                bean.getJobflowId(),
                bean.getExecutionId());

        boolean sawError = false;
        LinkedList<Future<?>> rest = new LinkedList<Future<?>>(running);
        while (rest.isEmpty() == false) {
            Future<?> future = rest.removeFirst();
            try {
                future.get(1, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                // continue...
                rest.addLast(future);
            } catch (InterruptedException e) {
                cancel(rest);
                throw new BulkLoaderSystemException(e, getClass(), "TG-EXTRACTOR-12007",
                        bean.getTargetName(),
                        bean.getBatchId(),
                        bean.getJobflowId(),
                        bean.getExecutionId());
            } catch (ExecutionException e) {
                cancel(rest);
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                } else if (cause instanceof Error) {
                    throw (Error) cause;
                } else if (cause instanceof BulkLoaderSystemException) {
                    LOG.log((BulkLoaderSystemException) cause);
                    sawError = true;
                } else {
                    LOG.error(e, "TG-EXTRACTOR-12008",
                            bean.getTargetName(),
                            bean.getBatchId(),
                            bean.getJobflowId(),
                            bean.getExecutionId());
                    sawError = true;
                }
            }
        }
        if (sawError) {
            throw new BulkLoaderSystemException(getClass(), "TG-EXTRACTOR-12008",
                    bean.getTargetName(),
                    bean.getBatchId(),
                    bean.getJobflowId(),
                    bean.getExecutionId());
        } else {
            LOG.info("TG-EXTRACTOR-12009",
                    bean.getTargetName(),
                    bean.getBatchId(),
                    bean.getJobflowId(),
                    bean.getExecutionId());
        }
    }

    private void cancel(List<Future<?>> futures) {
        assert futures != null;
        for (Future<?> future : futures) {
            future.cancel(true);
        }
    }

    /**
     * ストリームからTSVファイルを読み出し、ジョブの入力データとして書き出す。
     * @param <T> Import対象テーブルに対応するModelのクラス型
     * @param targetTableModel Import対象テーブルに対応するModelのクラス
     * @param dfsFilePath HFSF上のファイル名
     * @param inputStream FileList
     * @return 書きだした件数
     * @throws BulkLoaderSystemException 読み出しや出力に失敗した場合
     */
    protected <T> long write(
            Class<T> targetTableModel,
            URI dfsFilePath,
            InputStream inputStream) throws BulkLoaderSystemException {
        ModelInput<T> input = null;
        ModelOutput<T> output = null;
        try {
            // TSVファイルをBeanに変換するオブジェクトを生成する
            TsvIoFactory<T> factory = new TsvIoFactory<T>(targetTableModel);
            input = factory.createModelInput(inputStream);

            // ジョブ入力テンポラリ領域に出力するオブジェクトを生成する
            Configuration conf = new Configuration();

            // コピー用のバッファを作成する
            Collection<T> working = new ArrayList<T>(COPY_BUFFER_RECORDS);
            for (int i = 0; i < COPY_BUFFER_RECORDS; i++) {
                working.add(factory.createModelObject());
            }

            // 圧縮に関する情報を取得
            String strCompType = ConfigurationLoader.getProperty(Constants.PROP_KEY_IMP_SEQ_FILE_COMP_TYPE);
            CompressionType compType = getCompType(strCompType);

            // Writerを経由して別スレッドで書き出す
            if (compType == CompressionType.NONE) {
                output = TemporaryStorage.openOutput(conf, targetTableModel, new Path(dfsFilePath), null);
            } else {
                output = TemporaryStorage.openOutput(conf, targetTableModel, new Path(dfsFilePath));
            }
            return MultiThreadedCopier.copy(input, output, working);
        } catch (IOException e) {
            throw new BulkLoaderSystemException(e, getClass(), "TG-EXTRACTOR-02001",
                    "DFSにファイルを書き出す処理に失敗。URI：" + dfsFilePath);
        } catch (InterruptedException e) {
            throw new BulkLoaderSystemException(e, getClass(), "TG-EXTRACTOR-02001",
                    "DFSにファイルを書き出す処理に失敗。URI：" + dfsFilePath);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * 圧縮の種類を取得する。
     * @param strCompType CompressionTypeの文字列
     * @return CompressionType
     */
    protected CompressionType getCompType(String strCompType) {
        CompressionType compType = null;
        try {
            compType = CompressionType.valueOf(strCompType);
        } catch (Exception e) {
            compType = CompressionType.NONE;
            LOG.warn("TG-EXTRACTOR-02004", strCompType);
        }
        return compType;
    }
    /**
     * InputStreamを生成して返す。
     * @return InputStream
     * @throws IOException if failed to open stream
     */
    protected InputStream getInputStream() throws IOException {
        return new BufferedInputStream(System.in, INPUT_BUFFER_BYTES);
    }
}
