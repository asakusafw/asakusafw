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
package com.asakusafw.runtime.stage.directio;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.util.ReflectionUtils;

import com.asakusafw.runtime.compatibility.JobCompatibility;
import com.asakusafw.runtime.directio.DataDefinition;
import com.asakusafw.runtime.directio.DataFormat;
import com.asakusafw.runtime.directio.DirectDataSource;
import com.asakusafw.runtime.directio.DirectDataSourceRepository;
import com.asakusafw.runtime.directio.OutputAttemptContext;
import com.asakusafw.runtime.directio.SimpleDataDefinition;
import com.asakusafw.runtime.directio.hadoop.HadoopDataSourceUtil;
import com.asakusafw.runtime.flow.MapperWithRuntimeResource;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.stage.StageConstants;
import com.asakusafw.runtime.util.VariableTable;

/**
 * Mapper which directly creates file for direct output.
 * @param <T> target data type
 * @since 0.4.0
 * @version 0.7.0
 */
public abstract class AbstractNoReduceDirectOutputMapper<T> extends MapperWithRuntimeResource<
        Object, T,
        Object, Object> {

    private static final String COUNTER_GROUP = "com.asakusafw.directio.output.Statistics"; //$NON-NLS-1$

    private final Log log;

    private final Class<? extends T> dataType;

    private final String rawBasePath;

    private final String rawResourcePath;

    private final Class<? extends DataFormat<? super T>> dataFormatClass;

    /**
     * Creates a new instance.
     * @param dataType target data type
     * @param rawBasePath target base path
     * @param rawResourcePath target resource path
     * @param dataFormatClass output data format
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public AbstractNoReduceDirectOutputMapper(
            Class<? extends T> dataType,
            String rawBasePath,
            String rawResourcePath,
            Class<? extends DataFormat<? super T>> dataFormatClass) {
        if (dataType == null) {
            throw new IllegalArgumentException("dataType must not be null"); //$NON-NLS-1$
        }
        if (rawBasePath == null) {
            throw new IllegalArgumentException("rawBasePath must not be null"); //$NON-NLS-1$
        }
        if (rawResourcePath == null) {
            throw new IllegalArgumentException("rawResourcePath must not be null"); //$NON-NLS-1$
        }
        if (dataFormatClass == null) {
            throw new IllegalArgumentException("dataFormatClass must not be null"); //$NON-NLS-1$
        }
        this.log = LogFactory.getLog(getClass());
        this.dataType = dataType;
        this.rawBasePath = rawBasePath;
        this.rawResourcePath = rawResourcePath;
        this.dataFormatClass = dataFormatClass;
    }

    @Override
    protected void runInternal(Context context) throws IOException, InterruptedException {
        if (context.nextKeyValue() == false) {
            if (log.isDebugEnabled()) {
                log.debug(MessageFormat.format(
                        "There are not input for directly output Mapper {0}@{1}", //$NON-NLS-1$
                        getClass().getName(),
                        context.getTaskAttemptID()));
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug(MessageFormat.format(
                        "Start setup directly output Mapper {0}@{1}", //$NON-NLS-1$
                        getClass().getName(),
                        context.getTaskAttemptID()));
            }
            DirectDataSourceRepository repository = HadoopDataSourceUtil.loadRepository(context.getConfiguration());
            String arguments = context.getConfiguration().get(StageConstants.PROP_ASAKUSA_BATCH_ARGS, ""); //$NON-NLS-1$
            VariableTable variables = new VariableTable(VariableTable.RedefineStrategy.IGNORE);
            variables.defineVariables(arguments);

            String path = variables.parse(rawBasePath, false);
            String id = repository.getRelatedId(path);
            OutputAttemptContext outputContext = HadoopDataSourceUtil.createContext(context, id);
            DataFormat<? super T> format = ReflectionUtils.newInstance(dataFormatClass, context.getConfiguration());
            DirectDataSource datasource = repository.getRelatedDataSource(path);
            String basePath = repository.getComponentPath(path);
            String unresolvedResourcePath = rawResourcePath.replaceAll(
                    Pattern.quote("*"), //$NON-NLS-1$
                    String.format("%04d", context.getTaskAttemptID().getTaskID().getId())); //$NON-NLS-1$
            String resourcePath = variables.parse(unresolvedResourcePath);
            DataDefinition<? super T> definition = SimpleDataDefinition.newInstance(dataType, format);

            if (log.isDebugEnabled()) {
                log.debug(MessageFormat.format(
                        "Open mapper output (id={0}, basePath={1}, resourcePath={2})", //$NON-NLS-1$
                        id,
                        basePath,
                        resourcePath));
            }

            int records = 0;
            try (ModelOutput<? super T> output = datasource.openOutput(
                    outputContext,
                    definition,
                    basePath,
                    resourcePath,
                    outputContext.getCounter())) {
                do {
                    output.write(context.getCurrentValue());
                    records++;
                } while (context.nextKeyValue());
            } finally {
                if (log.isDebugEnabled()) {
                    log.debug(MessageFormat.format(
                            "Start cleanup directly output Mapper {0}@{1}", //$NON-NLS-1$
                            getClass().getName(),
                            context.getTaskAttemptID()));
                }
            }
            org.apache.hadoop.mapreduce.Counter recordCounter = JobCompatibility.getTaskOutputRecordCounter(context);
            recordCounter.increment(records);
            context.getCounter(COUNTER_GROUP, id + ".files").increment(1); //$NON-NLS-1$
            context.getCounter(COUNTER_GROUP, id + ".records").increment(records); //$NON-NLS-1$
            context.getCounter(COUNTER_GROUP, id + ".size").increment(outputContext.getCounter().get()); //$NON-NLS-1$
        }
    }
}
