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

import com.asakusafw.bulkloader.common.ConfigurationLoader;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.log.Log;
import com.asakusafw.bulkloader.transfer.FileList;
import com.asakusafw.bulkloader.transfer.FileListProvider;
import com.asakusafw.bulkloader.transfer.FileProtocol;
import com.asakusafw.bulkloader.transfer.RemoteFileListProviderFactory;
import com.asakusafw.runtime.core.context.RuntimeContext;

/**
 * Deletes cache storages.
 * @since 0.2.3
 * @see DeleteCacheStorageRemote
 */
public class DeleteCacheStorageLocal {

    static final Log LOG = new Log(DeleteCacheStorageLocal.class);

    /*
     * This field should not be static because of saving system resources on testing.
     */
    private final ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactory() {
        final AtomicInteger counter = new AtomicInteger();
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName(String.format("delete-cache-storage-%d", counter.incrementAndGet()));
            return t;
        }
    });

    /**
     * Deletes storages for the related caches in the list.
     * This will return the pairs - the cache storage path and the its result.
     * @param list the target list
     * @param targetName the current target name
     * @return the deleted results
     * @throws BulkLoaderSystemException if failed to obtain cache information by system exception
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public Map<String, FileProtocol.Kind> delete(
            List<LocalCacheInfo> list,
            String targetName) throws BulkLoaderSystemException {
        if (list == null) {
            throw new IllegalArgumentException("list must not be null"); //$NON-NLS-1$
        }
        if (targetName == null) {
            throw new IllegalArgumentException("targetName must not be null"); //$NON-NLS-1$
        }
        if (list.isEmpty()) {
            return Collections.emptyMap();
        }
        LOG.info("TG-GCCACHE-02001",
                targetName,
                list.size());
        FileListProvider provider = null;
        try {
            provider = openFileList(targetName);
            Future<Void> upstream = submitUpstream(list, provider);
            Future<Map<String, FileProtocol.Kind>> downstream = submitDownstream(provider);
            Map<String, FileProtocol.Kind> results;
            while (true) {
                try {
                    if (upstream.isDone()) {
                        upstream.get();
                    }
                    results = downstream.get(1, TimeUnit.SECONDS);
                    break;
                } catch (TimeoutException e) {
                    LOG.debugMessage("trying retry"); //$NON-NLS-1$
                } catch (CancellationException e) {
                    upstream.cancel(true);
                    downstream.cancel(true);
                    throw new IOException("Deleting cache storages was cancelled", e);
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
            reportResults(targetName, results);
            return results;
        } catch (IOException e) {
            throw new BulkLoaderSystemException(e, getClass(), "TG-GCCACHE-02004",
                    targetName);
        } catch (InterruptedException e) {
            throw new BulkLoaderSystemException(e, getClass(), "TG-GCCACHE-02004",
                    targetName);
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

    private void reportResults(String targetName, Map<String, FileProtocol.Kind> results) {
        assert targetName != null;
        assert results != null;
        int succeed = 0;
        int missing = 0;
        int error = 0;
        for (Map.Entry<String, FileProtocol.Kind> entry : results.entrySet()) {
            switch (entry.getValue()) {
            case RESPONSE_DELETED:
                succeed++;
                break;
            case RESPONSE_NOT_FOUND:
                missing++;
                break;
            case RESPONSE_ERROR:
                error++;
                break;
            default:
                throw new AssertionError(entry);
            }
        }
        LOG.info("TG-GCCACHE-02003",
                targetName,
                succeed,
                missing,
                error);
    }

    private Future<Void> submitUpstream(final List<LocalCacheInfo> list, final FileListProvider provider) {
        assert list != null;
        assert provider != null;
        return executor.submit(new Callable<Void>() {
            @Override
            public Void call() throws IOException {
                FileList.Writer writer = provider.openWriter(false);
                try {
                    for (LocalCacheInfo info : list) {
                        FileProtocol protocol = new FileProtocol(
                                FileProtocol.Kind.DELETE_CACHE,
                                info.getPath(),
                                null);

                        writer.openNext(protocol).close();
                    }
                } finally {
                    writer.close();
                }
                return null;
            }
        });
    }

    private Future<Map<String, FileProtocol.Kind>> submitDownstream(final FileListProvider provider) {
        assert provider != null;
        return executor.submit(new Callable<Map<String, FileProtocol.Kind>>() {
            @Override
            public Map<String, FileProtocol.Kind> call() throws IOException {
                Map<String, FileProtocol.Kind> results = new HashMap<String, FileProtocol.Kind>();
                FileList.Reader reader = provider.openReader();
                try {
                    while (reader.next()) {
                        FileProtocol protocol = reader.getCurrentProtocol();

                        // receive only header
                        reader.openContent().close();

                        switch (protocol.getKind()) {
                        case RESPONSE_DELETED:
                        case RESPONSE_NOT_FOUND:
                        case RESPONSE_ERROR:
                            results.put(protocol.getLocation(), protocol.getKind());
                            break;
                        default:
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
     * Opens a new {@link FileListProvider} for delete-cache-storage.
     * @param targetName current target name
     * @return the created provider
     * @throws IOException if failed to open the file list
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    protected FileListProvider openFileList(String targetName) throws IOException {
        if (targetName == null) {
            throw new IllegalArgumentException("targetName must not be null"); //$NON-NLS-1$
        }
        String sshPath = ConfigurationLoader.getProperty(Constants.PROP_KEY_SSH_PATH);
        String hostName = ConfigurationLoader.getProperty(Constants.PROP_KEY_NAMENODE_HOST);
        String userName = ConfigurationLoader.getProperty(Constants.PROP_KEY_NAMENODE_USER);
        String scriptPath = ConfigurationLoader.getRemoteScriptPath(Constants.PATH_REMOTE_CACHE_DELETE);
        List<String> command = new ArrayList<String>();
        command.add(scriptPath);
        command.add(targetName);

        Map<String, String> env = new HashMap<String, String>();
        env.putAll(ConfigurationLoader.getPropSubMap(Constants.PROP_PREFIX_HC_ENV));
        env.putAll(RuntimeContext.get().unapply());

        LOG.info("TG-GCCACHE-02002",
                sshPath,
                hostName,
                userName,
                scriptPath,
                targetName);

        return new RemoteFileListProviderFactory(sshPath, hostName, userName).newInstance(command, env);
    }
}
