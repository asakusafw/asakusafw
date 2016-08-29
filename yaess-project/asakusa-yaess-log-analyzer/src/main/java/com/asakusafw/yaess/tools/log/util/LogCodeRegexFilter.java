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
package com.asakusafw.yaess.tools.log.util;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.asakusafw.yaess.tools.log.YaessLogRecord;

/**
 * Filters {@link YaessLogRecord} by its log code using regular expressions.
 * @since 0.6.2
 */
public class LogCodeRegexFilter implements Predicate<YaessLogRecord> {

    private final Pattern pattern;

    /**
     * Creates a new instance.
     * @param pattern the log code pattern
     */
    public LogCodeRegexFilter(Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public boolean test(YaessLogRecord value) {
        String code = value.getCode();
        if (code == null) {
            return false;
        }
        return pattern.matcher(code).matches();
    }
}
