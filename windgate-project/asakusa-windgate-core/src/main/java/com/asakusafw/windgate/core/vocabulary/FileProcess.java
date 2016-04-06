/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
 * WindGate file resource configuration.
 * @since 0.2.2
 */
public enum FileProcess implements ConfigurationItem {

    /**
     * The script key of target path(s).
     * The parameters in value will be replaced.
     */
    FILE(
            "file", //$NON-NLS-1$
            "Target file path(s)"
    ),
    ;

    private final String key;

    private final String description;

    FileProcess(String key, String description) {
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
