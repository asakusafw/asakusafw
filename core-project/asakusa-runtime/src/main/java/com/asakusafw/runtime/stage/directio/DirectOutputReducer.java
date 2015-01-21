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
package com.asakusafw.runtime.stage.directio;

import java.io.IOException;

import org.apache.hadoop.conf.Configurable;

import com.asakusafw.runtime.compatibility.JobCompatibility;
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
import com.asakusafw.runtime.util.VariableTable;

/**
 * Reducer for direct output.
 * @since 0.2.5
 * @version 0.7.0
 */
public final class DirectOutputReducer extends ReducerWithRuntimeResource<
        AbstractDirectOutputKey, AbstractDirectOutputValue,
        Object, Object> {

    private static final String COUNTER_GROUP = "com.asakusafw.directio.output.Statistics"; //$NON-NLS-1$

    private org.apache.hadoop.mapreduce.Counter recordCounter;

    private DirectDataSourceRepository repository;

    private VariableTable variables;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        this.recordCounter = JobCompatibility.getTaskOutputRecordCounter(context);
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
        String path = variables.parse(group.getPath(), false);
        String id = repository.getRelatedId(path);
        OutputAttemptContext outputContext = HadoopDataSourceUtil.createContext(context, id);
        DataDefinition definition = SimpleDataDefinition.newInstance(
                group.getDataType(),
                configure(context, group.getFormat()));
        DirectDataSource datasource = repository.getRelatedDataSource(path);
        String basePath = repository.getComponentPath(path);
        String resourcePath = variables.parse(group.getResourcePath());

        Counter counter = new Counter();
        ModelOutput output = datasource.openOutput(outputContext, definition, basePath, resourcePath, counter);

        long records = 0;
        try {
            for (Union union : values) {
                Object object = union.getObject();
                output.write(object);
                records++;
            }
        } finally {
            output.close();
        }
        recordCounter.increment(records);
        context.getCounter(COUNTER_GROUP, id + ".files").increment(1); //$NON-NLS-1$
        context.getCounter(COUNTER_GROUP, id + ".records").increment(records); //$NON-NLS-1$
        context.getCounter(COUNTER_GROUP, id + ".size").increment(counter.get()); //$NON-NLS-1$
    }

    private <T> T configure(Context context, T object) {
        if (object instanceof Configurable) {
            ((Configurable) object).setConf(context.getConfiguration());
        }
        return object;
    }
}
