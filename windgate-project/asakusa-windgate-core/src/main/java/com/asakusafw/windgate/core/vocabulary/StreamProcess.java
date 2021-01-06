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
package com.asakusafw.windgate.core.vocabulary;

/**
 * WindGate stream resource configuration.
 * @since 0.2.2
 */
public enum StreamProcess implements ConfigurationItem {

    /**
     * The script key of {@link DataModelStreamSupport} class.
     */
    STREAM_SUPPORT(
            "support", //$NON-NLS-1$
            "DataModelStreamSupport class name"
    ),
    ;

    private final String key;

    private final String description;

    StreamProcess(String key, String description) {
        assert key != null;
        assert description != null;
        this.key = key;
        this.description = description;
    }

    @Override
    public final String key() {
        return key;
    }

    @Override
    public String description() {
        return description;
    }
}
