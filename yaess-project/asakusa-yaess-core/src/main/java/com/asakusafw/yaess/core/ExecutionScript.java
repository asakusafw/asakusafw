/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.yaess.core;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An base interface of execution script which represents each atomic execution in YAESS.
 * @since 0.2.3
 * @see ExecutionScriptHandler
 */
public interface ExecutionScript {

    /**
     * An environment variable name of Asakusa installation home.
     */
    String ENV_ASAKUSA_HOME = "ASAKUSA_HOME";

    /**
     * A placeholder name of the Asakusa installed location.
     */
    String PLACEHOLDER_HOME = "{{{PLACEHOLDER/ASAKUSA_HOME}}}";

    /**
     * A placeholder name of the execution ID.
     */
    String PLACEHOLDER_EXECUTION_ID = "{{{PLACEHOLDER/EXECUTION_ID}}}";

    /**
     * A placeholder name of the execution arguments.
     */
    String PLACEHOLDER_ARGUMENTS = "{{{PLACEHOLDER/ARGUMENTS}}}";

    /**
     * Returns the kind of this script.
     * @return the kind of this script
     */
    Kind getKind();

    /**
     * Returns the ID of this script execution.
     * @return the ID of this script execution
     */
    String getId();

    /**
     * Returns the IDs representing blockers of this script execution.
     * @return blockers' ID
     */
    Set<String> getBlockerIds();

    /**
     * Returns desired environment variables used in this execution.
     * @return desired environment variables
     */
    Map<String, String> getEnvironmentVariables();

    /**
     * Returns the supported extension names.
     * @return the supported extension names
     * @since 0.8.0
     */
    Set<String> getSupportedExtensions();

    /**
     * Returns whether this script is resolved.
     * @return {@code true} for resolved instance, otherwise {@code false}
     * @see #resolve(ExecutionContext, ExecutionScriptHandler)
     */
    boolean isResolved();

    /**
     * Resolves placeholders defined in this script.
     * This method does not change this object, but returns a resolved object.
     * @param context current context
     * @param handler target handler
     * @return resolved object
     * @throws InterruptedException if interrupted while resolving this object
     * @throws IOException if failed to resolve some entry
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @see #isResolved()
     */
    ExecutionScript resolve(
            ExecutionContext context,
            ExecutionScriptHandler<?> handler) throws InterruptedException, IOException;

    /**
     * Type of each {@link ExecutionScript}.
     * @since 0.2.3
     */
    public enum Kind {

        /**
         * Generic command script.
         */
        COMMAND,

        /**
         * Script corresponding to Hadoop.
         */
        HADOOP,
        ;

        /**
         * Returns the symbol of this phase.
         * This symbol is used in {@link ExecutionScript}s.
         * @return the symbol of this phase
         */
        public String getSymbol() {
            return name().toLowerCase();
        }

        /**
         * Returns an {@link Kind} corresponded to the symbol.
         * @param symbol target symbol
         * @return the corresponding phase, or {@code null} if not found
         * @throws IllegalArgumentException if some parameters were {@code null}
         */
        public static Kind findFromSymbol(String symbol) {
            if (symbol == null) {
                throw new IllegalArgumentException("symbol must not be null"); //$NON-NLS-1$
            }
            return Lazy.SYMBOLS.get(symbol);
        }

        private static final class Lazy {

            static final Map<String, Kind> SYMBOLS;
            static {
                Map<String, Kind> map = new HashMap<>();
                for (Kind phase : values()) {
                    map.put(phase.getSymbol(), phase);
                }
                SYMBOLS = Collections.unmodifiableMap(map);
            }

            private Lazy() {
                return;
            }
        }
    }

    /**
     * Resolves placeholders in script.
     * @since 0.2.3
     */
    public class PlaceholderResolver {

        static final Logger LOG = LoggerFactory.getLogger(ExecutionScript.class);

        private static final Pattern PLACEHOLDERS = Pattern.compile(
                Pattern.quote(PLACEHOLDER_HOME) + '|'
                + Pattern.quote(PLACEHOLDER_EXECUTION_ID) + '|'
                + Pattern.quote(PLACEHOLDER_ARGUMENTS));

        private final Map<String, String> replacements;

        /**
         * Creates a new instance.
         * @param script target script
         * @param context current execution context
         * @param handler handler which will attempt to resolve placeholders
         * @throws InterruptedException if interrupted to prepare this resolver
         * @throws IOException if failed to prepare replacements
         * @throws IllegalArgumentException if some parameters were {@code null}
         */
        public PlaceholderResolver(
                ExecutionScript script,
                ExecutionContext context,
                ExecutionScriptHandler<?> handler) throws InterruptedException, IOException {
            if (script == null) {
                throw new IllegalArgumentException("script must not be null"); //$NON-NLS-1$
            }
            if (context == null) {
                throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
            }
            if (handler == null) {
                throw new IllegalArgumentException("handler must not be null"); //$NON-NLS-1$
            }
            replacements = new HashMap<>();
            replacements.put(PLACEHOLDER_HOME, getAsakusaHomePath(context, script, handler));
            replacements.put(PLACEHOLDER_EXECUTION_ID, context.getExecutionId());
            replacements.put(PLACEHOLDER_ARGUMENTS, context.getArgumentsAsString());
        }

        private String getAsakusaHomePath(
                ExecutionContext context,
                ExecutionScript script,
                ExecutionScriptHandler<?> handler) throws IOException, InterruptedException {
            assert context != null;
            assert script != null;
            assert handler != null;
            String inScript = script.getEnvironmentVariables().get(ENV_ASAKUSA_HOME);
            if (inScript != null && inScript.equals(PLACEHOLDER_HOME) == false) {
                LOG.debug("Asakusa location is found in script: {} -> {}",
                        script.getId(),
                        inScript);
                return inScript;
            }
            Map<String, String> environmentVariables = handler.getEnvironmentVariables(context, script);
            String inHandler = environmentVariables.get(ENV_ASAKUSA_HOME);
            if (inHandler != null) {
                LOG.debug("Asakusa location is found in handler: {} -> {}",
                        script.getId(),
                        inHandler);
                return inHandler;
            }
            throw new IOException(MessageFormat.format(
                    "{0} is not defined for \"{1}\"",
                    ENV_ASAKUSA_HOME,
                    handler.getHandlerId()));
        }

        /**
         * Resolves placeholder expressions in the target string.
         * @param target target string
         * @return the resolved string
         * @throws IllegalArgumentException if some parameters were {@code null}
         */
        public String resolve(String target) {
            if (target == null) {
                throw new IllegalArgumentException("target must not be null"); //$NON-NLS-1$
            }
            Matcher matcher = PLACEHOLDERS.matcher(target);
            StringBuilder buf = new StringBuilder();
            int start = 0;
            while (matcher.find(start)) {
                buf.append(target.substring(start, matcher.start()));
                String replacement = replacements.get(matcher.group());
                assert replacement != null;
                buf.append(replacement);
                start = matcher.end();
            }
            buf.append(target.substring(start));
            return buf.toString();
        }
    }
}
