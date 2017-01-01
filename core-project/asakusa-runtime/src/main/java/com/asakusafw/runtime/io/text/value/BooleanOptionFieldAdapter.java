/**
 * Copyright 2011-2017 Asakusa Framework Team.
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

import java.util.function.Predicate;

import com.asakusafw.runtime.io.text.driver.FieldAdapter;
import com.asakusafw.runtime.value.BooleanOption;

/**
 * An implementation of {@link FieldAdapter} which accepts {@link BooleanOption}.
 * @since 0.9.1
 */
public final class BooleanOptionFieldAdapter extends ValueOptionFieldAdapter<BooleanOption> {

    /**
     * The default value of {@code true} format.
     */
    public static final String DEFAULT_TRUE_FORMAT = "true";

    /**
     * The default value of {@code false} format.
     */
    public static final String DEFAULT_FALSE_FORMAT = "false";

    private final Predicate<? super CharSequence> trueTester;

    private final Predicate<? super CharSequence> falseTester;

    private final String trueFormat;

    private final String falseFormat;

    BooleanOptionFieldAdapter(
            String nullFormat,
            Predicate<? super CharSequence> trueTester, Predicate<? super CharSequence> falseTester,
            String trueFormat, String falseFormat) {
        super(nullFormat);
        this.trueTester = trueTester;
        this.falseTester = falseTester;
        this.trueFormat = trueFormat;
        this.falseFormat = falseFormat;
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
    protected void doParse(CharSequence contents, BooleanOption property) {
        if (trueTester.test(contents)) {
            property.modify(true);
        } else if (falseTester.test(contents)) {
            property.modify(false);
        } else {
            throw new IllegalArgumentException(contents.toString());
        }
    }

    @Override
    protected void doEmit(BooleanOption property, StringBuilder output) {
        output.append(property.get() ? trueFormat : falseFormat);
    }

    /**
     * A builder of {@link BooleanOptionFieldAdapter}.
     * @since 0.9.1
     */
    public static class Builder extends BuilderBase<Builder, BooleanOptionFieldAdapter> {

        private String trueFormat = DEFAULT_TRUE_FORMAT;

        private String falseFormat = DEFAULT_FALSE_FORMAT;

        private Predicate<? super CharSequence> trueTester;

        private Predicate<? super CharSequence> falseTester;

        /**
         * Creates a new instance.
         */
        public Builder() {
            this.trueTester = trueFormat::contentEquals;
            this.falseTester = falseFormat::contentEquals;
        }

        /**
         * Sets the format of {@code true}.
         * @param newValue the output sequence of {@code true}
         * @return this
         */
        public Builder withTrueFormat(String newValue) {
            return withTrueFormat(newValue, newValue::contentEquals);
        }

        /**
         * Sets the format of {@code false}.
         * @param newValue the output sequence of {@code false}
         * @return this
         */
        public Builder withFalseFormat(String newValue) {
            return withFalseFormat(newValue, newValue::contentEquals);
        }

        /**
         * Sets the format of {@code true}.
         * @param newValue the output sequence of {@code true}
         * @param newTester the input tester of {@code true}
         * @return this
         */
        public Builder withTrueFormat(String newValue, Predicate<? super CharSequence> newTester) {
            this.trueFormat = newValue;
            this.trueTester = newTester;
            return this;
        }

        /**
         * Sets the format of {@code false}.
         * @param newValue the output sequence of {@code false}
         * @param newTester the input tester of {@code false}
         * @return this
         */
        public Builder withFalseFormat(String newValue, Predicate<? super CharSequence> newTester) {
            this.falseFormat = newValue;
            this.falseTester = newTester;
            return this;
        }

        @Override
        public BooleanOptionFieldAdapter build() {
            return new BooleanOptionFieldAdapter(
                    getNullFormat(),
                    trueTester, falseTester,
                    trueFormat, falseFormat);
        }
    }
}
