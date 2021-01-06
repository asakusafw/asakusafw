/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.runtime.io.json.value;

import java.io.IOException;

import com.asakusafw.runtime.io.json.PropertyAdapter;
import com.asakusafw.runtime.io.json.ValueReader;
import com.asakusafw.runtime.io.json.ValueWriter;
import com.asakusafw.runtime.value.DoubleOption;

/**
 * An implementation of {@link PropertyAdapter} for {@link DoubleOption}.
 * @since 0.10.3
 */
public class DoubleOptionPropertyAdapter extends ValueOptionPropertyAdapter<DoubleOption> {

    /**
     * Creates a new instance.
     * @param builder the source builder
     */
    protected DoubleOptionPropertyAdapter(Builder builder) {
        super(builder);
    }

    /**
     * Returns a new builder.
     * @return the created builder
     */
    public static Builder builder() {
        return new Builder();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void doRead(ValueReader reader, DoubleOption property) throws IOException {
        double value = reader.readDouble();
        property.modify(value);
    }

    @Override
    protected void doWrite(DoubleOption property, ValueWriter writer) throws IOException {
        writer.writeDouble(property.get());
    }

    /**
     * A builder for {@link DoubleOptionPropertyAdapter}.
     * @since 0.10.3
     */
    public static class Builder extends BuilderBase<Builder, DoubleOptionPropertyAdapter> {

        @Override
        public DoubleOptionPropertyAdapter build() {
            return new DoubleOptionPropertyAdapter(this);
        }
    }
}
