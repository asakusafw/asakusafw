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
package com.asakusafw.bulkloader.cache;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import com.asakusafw.bulkloader.bean.ImportBean;
import com.asakusafw.bulkloader.bean.ImportTargetTableBean;
import com.asakusafw.bulkloader.common.ConfigurationLoader;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.log.Log;
import com.asakusafw.bulkloader.transfer.FileList;
import com.asakusafw.bulkloader.transfer.FileListProvider;
import com.asakusafw.bulkloader.transfer.FileProtocol;
import com.asakusafw.bulkloader.transfer.RemoteFileListProviderFactory;
import com.asakusafw.runtime.core.context.RuntimeContext;
import com.asakusafw.thundergate.runtime.cache.CacheInfo;

/**
 * Retrieves {@link CacheInfo}.
 * @since 0.2.3
 * @see GetCacheInfoRemote
 */
public class GetCacheInfoLocal {

    static final Log LOG = new Log(GetCacheInfoLocal.class);

    /*
     * This field should not be static because of saving system resources on testing.
     */
    private final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactory() {
        final AtomicInteger counter = new AtomicInteger();
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName(String.format("get-cache-info-%d", counter.incrementAndGet()));
            return t;
        }
    });

    /**
     * Retrieves {@link CacheInfo} from already deployed cache directories.
     * This will return the pairs - {@link ImportTargetTableBean#getDfsFilePath()} and corresponded cache information.
     * If a target table does not use cache feature or related cache did not exist, there will be not in the result.
     * @param bean importer information
     * @return the retrieved information
     * @throws BulkLoaderSystemException if failed to obtain cache information by system exception
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public Map<String, CacheInfo> get(ImportBean bean) throws BulkLoaderSystemException {
        if (bean == null) {
            throw new IllegalArgumentException("bean must not be null"); //$NON-NLS-1$
        }
        if (hasCacheUser(bean) == false) {
            return Collections.emptyMap();
        }
        LOG.info("TG-IMPORTER-12001",
                bean.getTargetName(),
                bean.getBatchId(),
                bean.getJobflowId(),
                bean.getExecutionId());
        FileListProvider provider = null;
        try {
            provider = openFileList(
                    bean.getTargetName(), bean.getBatchId(), bean.getJobflowId(), bean.getExecutionId());
            Future<Void> upstream = submitUpstream(bean, provider);
            Future<Map<String, CacheInfo>> downstream = submitDownstream(provider);
            Map<String, CacheInfo> result;
            while (true) {
                try {
                    if (upstream.isDone()) {
                        upstream.get();
                    }
                    result = downstream.get(1, TimeUnit.SECONDS);
                    break;
                } catch (TimeoutException e) {
                    LOG.debugMessage("trying retry"); //$NON-NLS-1$
                } catch (CancellationException e) {
                    upstream.cancel(true);
                    downstream.cancel(true);
                    throw new IOException("Collecting cache information was cancelled", e);
                } catch (ExecutionException e) {
                    upstream.cancel(true);
                    downstream.cancel(true);
                    Throwable cause = e.getCause();
                    if (cause instanceof Error) {
                        throw (Error) cause;
                    } else if (cause instanceof RuntimeException) {
                        throw (RuntimeException) cause;
                    } else if (cause instanceof IOException) {
                        throw (IOException) cause;
                    } else {
                        throw new AssertionError(cause);
                    }
                }
            }
            provider.waitForComplete();
            LOG.info("TG-IMPORTER-12003",
                    bean.getTargetName(),
                    bean.getBatchId(),
                    bean.getJobflowId(),
                    bean.getExecutionId(),
                    result.size());
            return result;
        } catch (IOException e) {
            throw new BulkLoaderSystemException(e, getClass(), "TG-IMPORTER-12004",
                    bean.getTargetName(),
                    bean.getBatchId(),
                    bean.getJobflowId(),
                    bean.getExecutionId());
        } catch (InterruptedException e) {
            throw new BulkLoaderSystemException(e, getClass(), "TG-IMPORTER-12004",
                    bean.getTargetName(),
                    bean.getBatchId(),
                    bean.getJobflowId(),
                    bean.getExecutionId());
        } finally {
            if (provider != null) {
                try {
                    provider.close();
                } catch (IOException ignored) {
                    ignored.printStackTrace();
                }
            }
        }
    }

    private boolean hasCacheUser(ImportBean bean) {
        assert bean != null;
        for (String tableName : bean.getImportTargetTableList()) {
            ImportTargetTableBean table = bean.getTargetTable(tableName);
            if (table.getCacheId() != null) {
                return true;
            }
        }
        return false;
    }

    private Future<Void> submitUpstream(final ImportBean bean, final FileListProvider provider) {
        return executor.submit(new Callable<Void>() {
            @Override
            public Void call() throws IOException {
                FileList.Writer writer = provider.openWriter(false);
                try {
                    for (String tableName : bean.getImportTargetTableList()) {
                        ImportTargetTableBean table = bean.getTargetTable(tableName);
                        if (table.getCacheId() == null || table.getDfsFilePath() == null) {
                            continue;
                        }
                        FileProtocol protocol = new FileProtocol(
                                FileProtocol.Kind.GET_CACHE_INFO,
                                table.getDfsFilePath(),
                                null);

                        // send only header
                        writer.openNext(protocol).close();
                    }
                } finally {
                    writer.close();
                }
                return null;
            }
        });
    }

    private Future<Map<String, CacheInfo>> submitDownstream(final FileListProvider provider) {
        assert provider != null;
        return executor.submit(new Callable<Map<String, CacheInfo>>() {
            @Override
            public Map<String, CacheInfo> call() throws IOException {
                Map<String, CacheInfo> results = new HashMap<String, CacheInfo>();
                FileList.Reader reader = provider.openReader();
                try {
                    while (reader.next()) {
                        FileProtocol protocol = reader.getCurrentProtocol();

                        // receive only header
                        reader.openContent().close();

                        if (protocol.getKind() == FileProtocol.Kind.RESPONSE_CACHE_INFO) {
                            assert protocol.getInfo() != null;
                            results.put(protocol.getLocation(), protocol.getInfo());
                        } else if (protocol.getKind() != FileProtocol.Kind.RESPONSE_NOT_FOUND
                                && protocol.getKind() != FileProtocol.Kind.RESPONSE_ERROR) {
                            throw new IOException(MessageFormat.format(
                                    "Unknown protocol in response: {0}",
                                    protocol));
                        }
                    }
                } finally {
                    reader.close();
                }
                return results;
            }
        });
    }

    /**
     * Opens a new {@link FileListProvider} for get-cache-info.
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
        String scriptPath = ConfigurationLoader.getRemoteScriptPath(Constants.PATH_REMOTE_CACHE_INFO);
        List<String> command = new ArrayList<String>();
        command.add(scriptPath);
        command.add(targetName);
        command.add(batchId);
        command.add(jobflowId);
        command.add(executionId);

        Map<String, String> env = new HashMap<String, String>();
        env.putAll(ConfigurationLoader.getPropSubMap(Constants.PROP_PREFIX_HC_ENV));
        env.putAll(RuntimeContext.get().unapply());

        LOG.info("TG-IMPORTER-12002",
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
