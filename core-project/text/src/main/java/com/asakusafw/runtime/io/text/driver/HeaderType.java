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
package com.asakusafw.runtime.io.text.driver;

/**
 * Represents header type of formatted text.
 * @since 0.9.1
 */
public enum HeaderType {

    /**
     * No headers.
     */
    NOTHING(Input.NEVER, Output.NEVER),

    /**
     * Consumes and generates header.
     */
    FORCE(Input.ALWAYS, Output.ALWAYS),

    /**
     * Consumes header only if exists.
     */
    SKIP(Input.OPTIONAL, Output.NEVER),

    /**
     * Ignore header only if exists, and generates header.
     */
    AUTO(Input.OPTIONAL, Output.ALWAYS),
    ;

    private final Input input;

    private final Output output;

    HeaderType(Input input, Output output) {
        this.input = input;
        this.output = output;
    }

    /**
     * Returns the input.
     * @return the input
     */
    public Input getInput() {
        return input;
    }

    /**
     * Returns the output.
     * @return the output
     */
    public Output getOutput() {
        return output;
    }

    /**
     * Input header type.
     * @since 0.9.1
     */
    public enum Input {

        /**
         * Never consumes header.
         */
        NEVER,

        /**
         * Always consumes header.
         */
        ALWAYS,

        /**
         * Consumes only if matched.
         */
        OPTIONAL,
    }

    /**
     * Output header type.
     * @since 0.9.1
     */
    public enum Output {

        /**
         * Never generates header.
         */
        NEVER,

        /**
         * Always generates header.
         */
        ALWAYS,
    }
}
