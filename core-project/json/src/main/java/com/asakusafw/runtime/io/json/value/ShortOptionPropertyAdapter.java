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
package com.asakusafw.runtime.io.json.value;

import java.io.IOException;

import com.asakusafw.runtime.io.json.PropertyAdapter;
import com.asakusafw.runtime.io.json.ValueReader;
import com.asakusafw.runtime.io.json.ValueWriter;
import com.asakusafw.runtime.value.ShortOption;

/**
 * An implementation of {@link PropertyAdapter} for {@link ShortOption}.
 * @since 0.10.3
 */
public class ShortOptionPropertyAdapter extends ValueOptionPropertyAdapter<ShortOption> {

    /**
     * Creates a new instance.
     * @param builder the source builder
     */
    ShortOptionPropertyAdapter(Builder builder) {
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
    protected void doRead(ValueReader reader, ShortOption property) throws IOException {
        int v = reader.readInt();
        short value = (short) v;
        if (v != value) {
            throw new ArithmeticException(String.format("short overflow: %d", v));
        }
        property.modify(value);
    }

    @Override
    protected void doWrite(ShortOption property, ValueWriter writer) throws IOException {
        writer.writeInt(property.get());
    }

    /**
     * A builder for {@link ShortOptionPropertyAdapter}.
     * @since 0.10.3
     */
    public static class Builder extends BuilderBase<Builder, ShortOptionPropertyAdapter> {

        @Override
        public ShortOptionPropertyAdapter build() {
            return new ShortOptionPropertyAdapter(this);
        }
    }
}
