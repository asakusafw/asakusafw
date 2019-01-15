/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.runtime.stage.input;

import java.io.IOException;
import java.text.MessageFormat;

import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.ReflectionUtils;

/**
 * An implementation of Hadoop {@link Mapper} for dispatching multiple Map operations.
 * @since 0.1.0
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class StageInputMapper extends Mapper {

    private Mapper mapper;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        assert context.getInputSplit() instanceof StageInputSplit;
        StageInputSplit split = (StageInputSplit) context.getInputSplit();
        try {
            this.mapper = ReflectionUtils.newInstance(split.getMapperClass(), context.getConfiguration());
        } catch (Exception e) {
            throw new IOException(MessageFormat.format(
                    "failed to instantiate {0}",
                    split.getMapperClass().getName()), e);
        }
    }

    @Override
    public void run(Context context) throws IOException, InterruptedException {
        setup(context);
        mapper.run(context);
        cleanup(context);
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        this.mapper = null;
    }
}
