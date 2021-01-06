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
package com.asakusafw.runtime.io.text.value;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import com.asakusafw.runtime.io.text.TextUtil;
import com.asakusafw.runtime.io.text.driver.FieldAdapter;
import com.asakusafw.runtime.value.FloatOption;

/**
 * An implementation of {@link FieldAdapter} which accepts {@link FloatOption}.
 * @since 0.9.1
 */
public final class FloatOptionFieldAdapter extends NumericOptionFieldAdapter<FloatOption> {

    FloatOptionFieldAdapter(String nullFormat, DecimalFormat decimalFormat) {
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
    protected Number get(FloatOption property) {
        return property.get();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void set(BigDecimal value, FloatOption property) {
        property.modify(value.floatValue());
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void setSpecial(CharSequence contents, Number value, FloatOption property) {
        property.modify(value.floatValue());
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void doParseDefault(CharSequence contents, FloatOption property) {
        property.modify(TextUtil.parseFloat(contents, 0, contents.length()));
    }

    @Override
    protected void doEmitDefault(FloatOption property, StringBuilder output) {
        output.append(property.get());
    }

    /**
     * A builder of {@link FloatOptionFieldAdapter}.
     * @since 0.9.1
     */
    public static class Builder extends NumericBuilderBase<Builder, FloatOptionFieldAdapter> {
        @Override
        public FloatOptionFieldAdapter build() {
            return new FloatOptionFieldAdapter(getNullFormat(), getDecimalFormat());
        }
    }
}
