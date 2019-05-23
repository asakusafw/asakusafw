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
package com.asakusafw.runtime.io.text.value;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import com.asakusafw.runtime.io.text.TextUtil;
import com.asakusafw.runtime.io.text.driver.FieldAdapter;
import com.asakusafw.runtime.value.LongOption;

/**
 * An implementation of {@link FieldAdapter} which accepts {@link LongOption}.
 * @since 0.9.1
 */
public final class LongOptionFieldAdapter extends NumericOptionFieldAdapter<LongOption> {

    LongOptionFieldAdapter(String nullFormat, DecimalFormat decimalFormat) {
        super(nullFormat, decimalFormat);
    }

    /**
     * Returns a new builder.
     * @return the created builder
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected Number get(LongOption property) {
        return property.get();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void set(BigDecimal value, LongOption property) {
        property.modify(value.longValueExact());
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void doParseDefault(CharSequence contents, LongOption property) {
        property.modify(TextUtil.parseLong(contents, 0, contents.length()));
    }

    @Override
    protected void doEmitDefault(LongOption property, StringBuilder output) {
        output.append(property.get());
    }

    /**
     * A builder of {@link LongOptionFieldAdapter}.
     * @since 0.9.1
     */
    public static class Builder extends NumericBuilderBase<Builder, LongOptionFieldAdapter> {
        @Override
        public LongOptionFieldAdapter build() {
            return new LongOptionFieldAdapter(getNullFormat(), getDecimalFormat());
        }
    }
}
