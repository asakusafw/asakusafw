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
package com.asakusafw.trace.io;

import java.lang.reflect.Type;
import java.util.Collection;

import com.asakusafw.trace.model.TraceSetting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * Serializes trace settings.
 * @since 0.5.1
 */
public final class TraceSettingSerializer {

    private static final Gson GSON;
    static {
        GSON = new GsonBuilder().create();
    }

    private static final Type TYPE = (new TypeToken<Collection<TraceSetting>>() {
        // no members;
    }).getType();

    private TraceSettingSerializer() {
        return;
    }

    /**
     * Serializes trace settings.
     * @param settings target trace settings
     * @return the serialized form
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static String serialize(Collection<? extends TraceSetting> settings) {
        if (settings == null) {
            throw new IllegalArgumentException("settings must not be null"); //$NON-NLS-1$
        }
        return GSON.toJson(settings, TYPE);
    }

    /**
     * Deserializes trace settings.
     * @param serializedSettings serialized representation of trace settings
     * @return the deserialized trace settings
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static Collection<? extends TraceSetting> deserialize(String serializedSettings) {
        if (serializedSettings == null) {
            throw new IllegalArgumentException("serializedSettings must not be null"); //$NON-NLS-1$
        }
        return GSON.fromJson(serializedSettings, TYPE);
    }
}
