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
import com.asakusafw.thundergate.runtime.cache.CacheInfo;
import com.asakusafw.thundergate.runtime.cache.CacheStorage;

/**
 * Program entry for fetching {@link CacheInfo}.
 * This requires {@link FileList} with
 * {@link com.asakusafw.bulkloader.transfer.FileProtocol.Kind#GET_CACHE_INFO}
 * on standard input.
 * And then this returns {@link FileList} with
 * {@link com.asakusafw.bulkloader.transfer.FileProtocol.Kind#RESPONSE_CACHE_INFO} and/or
 * {@link com.asakusafw.bulkloader.transfer.FileProtocol.Kind#RESPONSE_NOT_FOUND}
 * on standard output.
 * This program requires following arguments:
 * <ol>
 * <li> target name </li>
 * <li> batch ID </li>
 * <li> flow ID </li>
 * <li> execution ID </li>
 * <li> user name </li>
 * </ol>
 * @since 0.2.3
 */
public class GetCacheInfoRemote extends Configured implements Tool {

    static final Log LOG = new Log(GetCacheInfoRemote.class);

    private static final List<String> PROPERTIES = Constants.PROPERTIES_HC;

    String targetName;

    String batchId;

    String flowId;

    String executionId;

    String userName;

    /**
     * Program entry for normal launching (see class documentation).
     * @param args program arguments
     * @throws Exception if failed to execute
     */
    public static void main(String[] args) throws Exception {
        SystemOutManager.changeSystemOutToSystemErr();
        RuntimeContext.set(RuntimeContext.DEFAULT.apply(System.getenv()));
        GetCacheInfoRemote service = new GetCacheInfoRemote();
        service.setConf(new Configuration());
        int exitCode = service.run(args);
        System.exit(exitCode);
    }

    @Override
    public int run(String[] args) throws Exception {
        initialize(args);
        LOG.info("TG-GETCACHE-01001", targetName, batchId, flowId, executionId, userName);
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
        LOG.info("TG-GETCACHE-01002", targetName, batchId, flowId, executionId, userName);
        return Constants.EXIT_CODE_SUCCESS;
    }

    /**
     * Initializes this object.
     * @param args program arguments (see the class document)
     * @see GetCacheInfoRemote
     */
    void initialize(String... args) {
        assert args != null;
        if (args.length != 5) {
            LOG.error("TG-GETCACHE-01003", Arrays.toString(args));
            throw new IllegalArgumentException(MessageFormat.format(
                    "Invalid arguments: {0}",
                    Arrays.toString(args)));
        }
        this.targetName = args[0];
        this.batchId = args[1];
        this.flowId = args[2];
        this.executionId = args[3];
        this.userName = args[4];
        if (BulkLoaderInitializer.initHadoopCluster(flowId, executionId, PROPERTIES) == false) {
            LOG.error("TG-GETCACHE-01004", targetName, batchId, flowId, executionId, userName);
            throw new IllegalStateException(MessageFormat.format(
                    "Failed to initialize GetCacheInfo: {0}",
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
                if (protocol.getKind() != FileProtocol.Kind.GET_CACHE_INFO) {
                    throw new IOException(MessageFormat.format(
                            "Unexpected protocol kind in GetCacheInfo: {0}",
                            protocol.getKind(),
                            protocol.getLocation()));
                }
                LOG.info("TG-GETCACHE-01006", protocol.getLocation());
                CacheInfo info = getCacheInfo(protocol.getLocation());
                FileProtocol result;
                if (info == null) {
                    result = new FileProtocol(FileProtocol.Kind.RESPONSE_NOT_FOUND, protocol.getLocation(), null);
                    LOG.info("TG-GETCACHE-01008", protocol.getLocation());
                } else {
                    result = new FileProtocol(FileProtocol.Kind.RESPONSE_CACHE_INFO, protocol.getLocation(), info);
                    LOG.info("TG-GETCACHE-01007",
                            protocol.getLocation(),
                            info.getId(),
                            info.getTableName(),
                            info.getTimestamp().getTime());
                }
                if (RuntimeContext.get().isSimulation() == false) {
                    output.openNext(result).close();
                }
            }
        } catch (IOException e) {
            throw new BulkLoaderSystemException(e, getClass(), "TG-GETCACHE-01005",
                    targetName, batchId, flowId, executionId, userName);
        }
    }

    private CacheInfo getCacheInfo(String location) throws BulkLoaderSystemException {
        assert location != null;
        URI cacheBaseUri = FileNameUtil.createPath(getConf(), location, executionId, userName).toUri();
        try {
            CacheStorage storage = new CacheStorage(getConf(), cacheBaseUri);
            try {
                if (RuntimeContext.get().canExecute(storage)) {
                    return storage.getHeadCacheInfo();
                } else {
                    return null;
                }
            } finally {
                IOUtils.closeQuietly(storage);
            }
        } catch (IOException e) {
            LOG.warn(e, "TG-GETCACHE-01009", location);
            return null;
        }
    }
}
