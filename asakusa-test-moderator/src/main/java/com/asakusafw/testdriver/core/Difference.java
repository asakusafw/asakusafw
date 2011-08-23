/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.testdriver.core;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateTime;

/**
 * Describes about a pair of the expected data and the actual data.
 * @since 0.2.0
 */
public class Difference {

    private final DataModelReflection expected;

    private final DataModelReflection actual;

    private final Object diagnostic;

    /**
     * Creates a new instance.
     * @param expected expected data (nullable)
     * @param actual actual data (nullable)
     * @param diagnostic diagnostic message (nullable)
     */
    public Difference(DataModelReflection expected, DataModelReflection actual, Object diagnostic) {
        this.expected = expected;
        this.actual = actual;
        this.diagnostic = diagnostic;
    }

    /**
     * Returns the expected data.
     * @return the expected data, or {@code null} if none
     */
    public DataModelReflection getExpected() {
        return expected;
    }

    /**
     * Returns the actual data.
     * @return the actual data, or {@code null} if none
     */
    public DataModelReflection getActual() {
        return actual;
    }

    /**
     * Returns a diagnostic message.
     * @return the diagnostic, or {@code null} if none
     */
    public Object getDiagnostic() {
        return diagnostic;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{2}: expected <{0}>, but was <{1}>",
                formatMap(getExpected()),
                formatMap(getActual()),
                getDiagnostic());
    }

    private static String format(Object value) {
        if (value instanceof Calendar) {
            Calendar c = (Calendar) value;
            if (c.isSet(Calendar.HOUR_OF_DAY)) {
                return new SimpleDateFormat(DateTime.FORMAT).format(c.getTime());
            } else {
                return new SimpleDateFormat(Date.FORMAT).format(c.getTime());
            }
        }
        return String.valueOf(value);
    }

    private static Map<Object, String> formatMap(DataModelReflection reflection) {
        if (reflection == null) {
            return null;
        } else {
            Map<?, ?> map = reflection.properties;
            Map<Object, String> results = new LinkedHashMap<Object, String>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                results.put(entry.getKey(), format(entry.getValue()));
            }
            return results;
        }
    }
}
