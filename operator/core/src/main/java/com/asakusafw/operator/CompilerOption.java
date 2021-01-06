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
package com.asakusafw.operator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import com.asakusafw.operator.util.Logger;

/**
 * Compiler options.
 * @since 0.9.0
 */
public final class CompilerOption {

    static final Logger LOG = Logger.get(CompilerOption.class);

    private static final String KEY_PREFIX = "com.asakusafw.operator."; //$NON-NLS-1$

    /**
     * The processor option name of warning action type.
     * @see WarningAction
     */
    public static final String KEY_WARNING_ACTION = KEY_PREFIX + "warning"; //$NON-NLS-1$

    private CompilerOption() {
        return;
    }

    static void install(CompileEnvironment environment) {
        Map<String, String> options = new TreeMap<>(environment.getProcessingEnvironment().getOptions());
        installWarningAction(environment, options);
        installFeatures(environment, options);
    }

    private static void installWarningAction(CompileEnvironment environment, Map<String, String> options) {
        Optional.ofNullable(options.remove(KEY_WARNING_ACTION))
            .flatMap(WarningAction::fromSymbol)
            .ifPresent(environment::withWarningAction);
    }

    private static void installFeatures(CompileEnvironment environment, Map<String, String> options) {
        for (CompileEnvironment.Support feature : CompileEnvironment.Support.values()) {
            Optional.ofNullable(options.remove(keyOf(feature)))
                    .map(String::trim)
                    .map(v -> v.isEmpty() || Boolean.parseBoolean(v))
                    .ifPresent(v -> feature.set(environment, v));
        }
    }

    /**
     * Returns the available option names.
     * @param available the available features
     * @return the available option names
     */
    public static Set<String> getOptionNames(Collection<? extends CompileEnvironment.Support> available) {
        Set<String> results = new HashSet<>();
        results.add(KEY_WARNING_ACTION);
        available.stream()
            .map(CompilerOption::keyOf)
            .forEach(results::add);
        return results;
    }

    private static String keyOf(CompileEnvironment.Support item) {
        return KEY_PREFIX + item.name();
    }
}
