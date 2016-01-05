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
import java.net.URI;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;

import com.asakusafw.bulkloader.collector.SystemOutManager;
import com.asakusafw.bulkloader.common.BulkLoaderInitializer;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.common.FileNameUtil;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.log.Log;
import com.asakusafw.bulkloader.transfer.FileList;
import com.asakusafw.bulkloader.transfer.FileProtocol;
import com.asakusafw.runtime.core.context.RuntimeContext;
import com.asakusafw.thundergate.runtime.cache.CacheStorage;

/**
 * Program entry for deleting cache storage.
 * This requires {@link FileList} with
 * {@link com.asakusafw.bulkloader.transfer.FileProtocol.Kind#DELETE_CACHE}
 * on standard input.
 * And then this returns {@link FileList} with
 * {@link com.asakusafw.bulkloader.transfer.FileProtocol.Kind#RESPONSE_DELETED} and/or
 * {@link com.asakusafw.bulkloader.transfer.FileProtocol.Kind#RESPONSE_NOT_FOUND}
 * on standard output.
 * This program requires following arguments:
 * <ol>
 * <li> target name </li>
 * <li> user name </li>
 * </ol>
 * @since 0.2.3
 */
public class DeleteCacheStorageRemote extends Configured implements Tool {

    private static final String SURROGATE_EXECUTION_ID = "gc";

    static final Log LOG = new Log(DeleteCacheStorageRemote.class);

    private static final List<String> PROPERTIES = Constants.PROPERTIES_HC;

    String targetName;

    String userName;

    /**
     * Program entry for normal launching (see class documentation).
     * @param args program arguments
     * @throws Exception if failed to execute
     */
    public static void main(String[] args) throws Exception {
        SystemOutManager.changeSystemOutToSystemErr();
        RuntimeContext.set(RuntimeContext.DEFAULT.apply(System.getenv()));
        DeleteCacheStorageRemote service = new DeleteCacheStorageRemote();
        service.setConf(new Configuration());
        int exitCode = service.run(args);
        System.exit(exitCode);
    }

    @Override
    public int run(String[] args) throws Exception {
        initialize(args);
        LOG.info("TG-GCCACHE-03001", targetName, userName);
        FileList.Reader in = null;
        FileList.Writer out = null;
        try {
            in = FileList.createReader(System.in);
            out = FileList.createWriter(SystemOutManager.getOut(), false);
            execute(in, out);
            out.close();
        } catch (BulkLoaderSystemException e) {
            LOG.log(e);
            return Constants.EXIT_CODE_ERROR;
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
        LOG.info("TG-GCCACHE-03002", targetName, userName);
        return Constants.EXIT_CODE_SUCCESS;
    }

    /**
     * Initializes this object.
     * @param args program arguments (see the class document)
     * @see DeleteCacheStorageRemote
     */
    void initialize(String... args) {
        assert args != null;
        if (args.length != 2) {
            LOG.error("TG-GCCACHE-03003", Arrays.toString(args));
            throw new IllegalArgumentException(MessageFormat.format(
                    "Invalid arguments: {0}",
                    Arrays.toString(args)));
        }
        this.targetName = args[0];
        this.userName = args[1];
        if (BulkLoaderInitializer.initHadoopCluster("cache", SURROGATE_EXECUTION_ID, PROPERTIES) == false) {
            LOG.error("TG-GCCACHE-03004", targetName, userName);
            throw new IllegalStateException(MessageFormat.format(
                    "Failed to initialize DeleteCacheStorage: {0}",
                    Arrays.toString(args)));
        }
    }

    /**
     * Executes the program with the specified input/output.
     * @param input input stream
     * @param output output stream
     * @throws BulkLoaderSystemException if failed to execute
     */
    void execute(
            FileList.Reader input,
            FileList.Writer output) throws BulkLoaderSystemException {
        assert input != null;
        assert output != null;
        try {
            while (input.next()) {
                FileProtocol protocol = input.getCurrentProtocol();
                if (protocol.getKind() != FileProtocol.Kind.DELETE_CACHE) {
                    throw new IOException(MessageFormat.format(
                            "Unexpected protocol kind in DeleteCacheStorage: {0}",
                            protocol.getKind(),
                            protocol.getLocation()));
                }
                LOG.info("TG-GCCACHE-03006", protocol.getLocation());
                FileProtocol.Kind result = deleteCacheData(protocol.getLocation());
                LOG.info("TG-GCCACHE-03007", protocol.getLocation(), result);
                FileProtocol response = new FileProtocol(result, protocol.getLocation(), null);
                output.openNext(response).close();
            }
        } catch (IOException e) {
            throw new BulkLoaderSystemException(e, getClass(), "TG-GCCACHE-03005",
                    targetName, userName);
        }
    }

    private FileProtocol.Kind deleteCacheData(String location) throws BulkLoaderSystemException {
        assert location != null;
        URI cacheBaseUri = FileNameUtil.createPath(getConf(), location, SURROGATE_EXECUTION_ID, userName).toUri();
        try {
            CacheStorage storage = new CacheStorage(getConf(), cacheBaseUri);
            try {
                boolean succeed;
                if (RuntimeContext.get().canExecute(storage)) {
                    succeed = storage.deleteAll();
                } else {
                    succeed = true;
                }
                return succeed ? FileProtocol.Kind.RESPONSE_DELETED : FileProtocol.Kind.RESPONSE_NOT_FOUND;
            } finally {
                IOUtils.closeQuietly(storage);
            }
        } catch (IOException e) {
            LOG.warn(e, "TG-GCCACHE-03008", location);
            return FileProtocol.Kind.RESPONSE_ERROR;
        }
    }
}
