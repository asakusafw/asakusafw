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

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.asakusafw.bulkloader.bean.ImportBean;
import com.asakusafw.bulkloader.bean.ImportTargetTableBean;
import com.asakusafw.bulkloader.common.BulkLoaderInitializer;
import com.asakusafw.bulkloader.common.ConfigurationLoader;
import com.asakusafw.bulkloader.common.Constants;
import com.asakusafw.bulkloader.common.JobFlowParamLoader;
import com.asakusafw.bulkloader.exception.BulkLoaderSystemException;
import com.asakusafw.bulkloader.importer.Importer;
import com.asakusafw.bulkloader.log.Log;
import com.asakusafw.runtime.core.context.RuntimeContext;

/**
 * Builds each cache for table.
 * @since 0.2.6
 */
public final class BuildCache {

    static final Log LOG = new Log(BuildCache.class);

    private static final List<String> PROPERTIES = Constants.PROPERTIES_DB;

    private BuildCache() {
        return;
    }

    /**
     * Program entry.
     * @param args target-name, batch-id, flow-id, table-name
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static void main(String[] args) {
        RuntimeContext.set(RuntimeContext.DEFAULT.apply(System.getenv()));
        RuntimeContext.get().verifyApplication(BuildCache.class.getClassLoader());
        if (args.length != 4 && args.length != 5) {
            LOG.error("TG-BUILDCACHE-01003", Arrays.toString(args));
            System.exit(Constants.EXIT_CODE_ERROR);
            return;
        }
        String targetName = args[0];
        String batchId = args[1];
        String flowId = args[2];
        String tableName = args[3];
        String executionId = args.length == 5 ? args[4] : UUID.randomUUID().toString();

        int initExit = initialize(targetName, flowId, executionId);
        if (initExit != Constants.EXIT_CODE_SUCCESS) {
            System.exit(initExit);
        }
        LOG.info("TG-BUILDCACHE-01001", targetName, batchId, flowId, executionId, tableName);
        int exitCode = new BuildCache().execute(targetName, batchId, flowId, tableName, executionId);
        LOG.info("TG-BUILDCACHE-01002", targetName, batchId, flowId, executionId, tableName, exitCode);

        System.exit(exitCode);
    }

    private static int initialize(String targetName, String flowId, String executionId) {
        if (!BulkLoaderInitializer.initDBServer(flowId, executionId, PROPERTIES, targetName)) {
            LOG.error("TG-BUILDCACHE-01004", targetName, flowId, executionId);
            return Constants.EXIT_CODE_ERROR;
        }
        return Constants.EXIT_CODE_SUCCESS;
    }

    private int execute(String targetName, String batchId, String flowId, String tableName, String executionId) {
        assert targetName != null;
        assert batchId != null;
        assert flowId != null;
        assert tableName != null;
        assert executionId != null;
        try {
            ImportBean bean = createBean(targetName, batchId, flowId, executionId, tableName);
            if (bean == null) {
                return Constants.EXIT_CODE_ERROR;
            }
            if (RuntimeContext.get().isSimulation()) {
                return Constants.EXIT_CODE_SUCCESS;
            }
            Importer importer = new Importer();
            int exitCode = importer.importTables(bean);
            if (exitCode == Constants.EXIT_CODE_SUCCESS) {
                LOG.info("TG-BUILDCACHE-01007", targetName, batchId, flowId, executionId, tableName);
                int releaseExit = new ReleaseCacheLock().execute(targetName, executionId);
                if (releaseExit != Constants.EXIT_CODE_SUCCESS) {
                    LOG.error("TG-BUILDCACHE-01008", targetName, batchId, flowId, executionId, tableName);
                    exitCode = Constants.EXIT_CODE_WARNING;
                }
            } else {
                LOG.info("TG-BUILDCACHE-01009", targetName, batchId, flowId, executionId, tableName);
            }
            return exitCode;
        } catch (BulkLoaderSystemException e) {
            LOG.log(e);
            return Constants.EXIT_CODE_ERROR;
        } catch (Exception e) {
            try {
                LOG.error(e, "TG-BUILDCACHE-01010", targetName, batchId, flowId, executionId, tableName);
                return Constants.EXIT_CODE_ERROR;
            } catch (Exception e1) {
                System.err.print("build-cacheで不明なエラーが発生しました。");
                e1.printStackTrace();
                return Constants.EXIT_CODE_ERROR;
            }
        }
    }

    private ImportBean createBean(
            String targetName,
            String batchId,
            String jobflowId,
            String executionId,
            String tableName) {
        assert targetName != null;
        assert batchId != null;
        assert jobflowId != null;
        assert executionId != null;
        assert tableName != null;

        ImportBean bean = new ImportBean();
        bean.setPrimary(false);
        bean.setTargetName(targetName);
        bean.setBatchId(batchId);
        bean.setJobflowId(jobflowId);
        bean.setExecutionId(executionId);
        bean.setJobnetEndTime(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(10)));
        bean.setRetryCount(Integer.parseInt(
                ConfigurationLoader.getProperty(Constants.PROP_KEY_IMP_RETRY_COUNT)));
        bean.setRetryInterval(Integer.parseInt(
                ConfigurationLoader.getProperty(Constants.PROP_KEY_IMP_RETRY_INTERVAL)));

        JobFlowParamLoader loader = new JobFlowParamLoader();
        if (loader.loadCacheBuildParam(targetName, batchId, jobflowId) == false) {
            return null;
        }
        ImportTargetTableBean table = null;
        for (Map.Entry<String, ImportTargetTableBean> entry : loader.getImportTargetTables().entrySet()) {
            if (entry.getKey().equals(tableName)) {
                table = entry.getValue();
                break;
            }
        }
        if (table == null) {
            LOG.error("TG-BUILDCACHE-01005", targetName, batchId, jobflowId, executionId, tableName);
            return null;
        }
        if (table.getCacheId() == null) {
            LOG.error("TG-BUILDCACHE-01006", targetName, batchId, jobflowId, executionId, tableName);
            return null;
        }
        bean.setTargetTable(Collections.singletonMap(tableName, table));
        return bean;
    }
}
