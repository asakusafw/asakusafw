/**
 * Copyright 2011-2018 Asakusa Framework Team.
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

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.mapreduce.TaskCounter;

import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.directio.DataDefinition;
import com.asakusafw.runtime.directio.DirectDataSource;
import com.asakusafw.runtime.directio.DirectDataSourceRepository;
import com.asakusafw.runtime.directio.OutputAttemptContext;
import com.asakusafw.runtime.directio.SimpleDataDefinition;
import com.asakusafw.runtime.directio.hadoop.HadoopDataSourceUtil;
import com.asakusafw.runtime.flow.ReducerWithRuntimeResource;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.util.Union;
import com.asakusafw.runtime.stage.StageConstants;
import com.asakusafw.runtime.stage.output.BridgeOutputFormat;
import com.asakusafw.runtime.util.VariableTable;

/**
 * Reducer for direct output.
 * @since 0.2.5
 * @version 0.8.1
 */
public final class DirectOutputReducer extends ReducerWithRuntimeResource<
        AbstractDirectOutputKey, AbstractDirectOutputValue,
        Object, Object> {

    private org.apache.hadoop.mapreduce.Counter recordCounter;

    private DirectDataSourceRepository repository;

    private VariableTable variables;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        this.recordCounter = context.getCounter(TaskCounter.REDUCE_OUTPUT_RECORDS);
        this.repository = HadoopDataSourceUtil.loadRepository(context.getConfiguration());
        String arguments = context.getConfiguration().get(StageConstants.PROP_ASAKUSA_BATCH_ARGS, ""); //$NON-NLS-1$
        this.variables = new VariableTable(VariableTable.RedefineStrategy.IGNORE);
        variables.defineVariables(arguments);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    protected void reduce(
            AbstractDirectOutputKey key,
            Iterable<AbstractDirectOutputValue> values,
            Context context) throws IOException , InterruptedException {
        DirectOutputGroup group = (DirectOutputGroup) key.getGroupObject().getObject();
        String portId = group.getOutputId();
        String path = variables.parse(group.getPath(), false);
        String sourceId = repository.getRelatedId(path);
        OutputAttemptContext outputContext = BridgeOutputFormat.createContext(context, sourceId);
        DataDefinition definition = SimpleDataDefinition.newInstance(
                group.getDataType(),
                configure(context, group.getFormat()));
        DirectDataSource datasource = repository.getRelatedDataSource(path);
        String basePath = repository.getComponentPath(path);
        String resourcePath = variables.parse(group.getResourcePath());

        Counter counter = new Counter();
        long records = 0;
        try (ModelOutput output = datasource.openOutput(outputContext, definition, basePath, resourcePath, counter)) {
            for (Union union : values) {
                Object object = union.getObject();
                output.write(object);
                records++;
            }
        }
        recordCounter.increment(records);
        Constants.putCounts(context, sourceId, portId, 1, records, counter.get());
    }

    private <T> T configure(Context context, T object) {
        if (object instanceof Configurable) {
            ((Configurable) object).setConf(context.getConfiguration());
        }
        return object;
    }
}
