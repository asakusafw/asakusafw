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
import java.math.BigDecimal;

import com.asakusafw.runtime.io.json.PropertyAdapter;
import com.asakusafw.runtime.io.json.ValueReader;
import com.asakusafw.runtime.io.json.ValueWriter;
import com.asakusafw.runtime.value.DecimalOption;

/**
 * An implementation of {@link PropertyAdapter} for {@link DecimalOption}.
 * @since 0.10.3
 */
public class DecimalOptionPropertyAdapter extends ValueOptionPropertyAdapter<DecimalOption> {

    /**
     * Creates a new instance.
     * @param builder the source builder
     */
    protected DecimalOptionPropertyAdapter(Builder builder) {
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
    protected void doRead(ValueReader reader, DecimalOption property) throws IOException {
        BigDecimal value = reader.readDecimal();
        property.modify(value);
    }

    @Override
    protected void doWrite(DecimalOption property, ValueWriter writer) throws IOException {
        writer.writeDecimal(property.get());
    }

    /**
     * A builder for {@link DecimalOptionPropertyAdapter}.
     * @since 0.10.3
     */
    public static class Builder extends BuilderBase<Builder, DecimalOptionPropertyAdapter> {

        @Override
        public DecimalOptionPropertyAdapter build() {
            return new DecimalOptionPropertyAdapter(this);
        }
    }
}
