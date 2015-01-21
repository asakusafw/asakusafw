/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.runtime.stage;

import static com.asakusafw.runtime.stage.StageConstants.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.asakusafw.runtime.core.context.RuntimeContext;
import com.asakusafw.runtime.util.VariableTable;
import com.asakusafw.runtime.util.VariableTable.RedefineStrategy;

/**
 * Stage client for cleanup phase.
 * @since 0.2.6
 * @version 0.4.0
 */
public abstract class AbstractCleanupStageClient extends BaseStageClient {

    /**
     * Fully qualified class name of implementation of this class.
     */
    public static final String IMPLEMENTATION = "com.asakusafw.runtime.stage.CleanupStageClient"; //$NON-NLS-1$

    /**
     * The method name of {@link #getCleanupPath()}.
     */
    public static final String METHOD_CLEANUP_PATH = "getCleanupPath"; //$NON-NLS-1$

    static final Log LOG = LogFactory.getLog(AbstractCleanupStageClient.class);

    /**
     * Returns the cleanup target path.
     * @return the cleanup target path
     */
    protected abstract String getCleanupPath();

    @Override
    protected int execute(String[] args) throws IOException, InterruptedException {
        Configuration conf = getConf();
        Path path = getPath(conf);
        FileSystem fileSystem = FileSystem.get(path.toUri(), conf);
        String info = MessageFormat.format(
                "batchId={0}, flowId={1}, executionId={2}, operationId={3}, path={4}", //$NON-NLS-1$
                getBatchId(),
                getFlowId(),
                getExecutionId(),
                getOperationId(),
                path);
        try {
            LOG.info(MessageFormat.format(
                    "Searching for cleanup target: {0}",
                    info));
            long start = System.currentTimeMillis();
            if (RuntimeContext.get().isSimulation()) {
                LOG.info(MessageFormat.format(
                        "Skip deleting cleanup target because current execution is in simulation mode: {0}",
                        info));
            } else {
                FileStatus stat = fileSystem.getFileStatus(path);
                if (stat == null) {
                    throw new FileNotFoundException(path.toString());
                }
                LOG.info(MessageFormat.format(
                        "Start deleting cleanup target: {0}",
                        info));
                if (fileSystem.delete(path, true) == false) {
                    throw new IOException("FileSystem.delete() returned false");
                }
            }
            long end = System.currentTimeMillis();
            LOG.info(MessageFormat.format(
                    "Finish deleting cleanup target: {0}, elapsed={1}ms",
                    info,
                    end - start));
            return 0;
        } catch (FileNotFoundException e) {
            LOG.warn(MessageFormat.format(
                    "Cleanup target is missing: {0}",
                    info));
            return 0;
        } catch (IOException e) {
            LOG.warn(MessageFormat.format(
                    "Failed to delete cleanup target: {0}",
                    info), e);
            return 1;
        } finally {
            FileSystem.closeAll();
        }
    }

    private Path getPath(Configuration conf) {
        VariableTable variables = getPathParser(conf);
        String barePath = getCleanupPath();
        String pathString = variables.parse(barePath, false);
        Path path = new Path(pathString);
        return path;
    }

    private VariableTable getPathParser(Configuration configuration) {
        assert configuration != null;
        VariableTable variables = new VariableTable(RedefineStrategy.IGNORE);
        variables.defineVariable(VAR_USER, getUser());
        variables.defineVariable(VAR_DEFINITION_ID, getDefinitionId());
        variables.defineVariable(VAR_STAGE_ID, getStageId());
        variables.defineVariable(VAR_BATCH_ID, getBatchId());
        variables.defineVariable(VAR_FLOW_ID, getFlowId());
        variables.defineVariable(VAR_EXECUTION_ID, getExecutionId());
        String arguments = configuration.get(PROP_ASAKUSA_BATCH_ARGS);
        if (arguments == null) {
            LOG.warn(MessageFormat.format(
                    "A mandatory property \"{0}\" does not defined",
                    PROP_ASAKUSA_BATCH_ARGS));
        } else {
            variables.defineVariables(arguments);
        }
        return variables;
    }
}
