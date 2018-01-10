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
package com.asakusafw.info.value;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Represents {@code null}.
 * @since 0.9.2
 */
public final class NullInfo implements ValueInfo {

    static final String KIND = "null"; //$NON-NLS-1$

    private static final NullInfo INSTANCE = new NullInfo();

    private NullInfo() {
        return;
    }

    /**
     * Returns the instance.
     * @return the instance
     */
    @JsonCreator
    public static NullInfo get() {
        return INSTANCE;
    }

    @Override
    public Kind getKind() {
        return Kind.NULL;
    }

    @Override
    public Object getObject() {
        return null;
    }

    @Override
    public String toString() {
        return KIND;
    }
}
