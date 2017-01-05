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
package com.asakusafw.compiler.batch.experimental;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.batch.AbstractWorkflowProcessor;
import com.asakusafw.compiler.batch.WorkDescriptionProcessor;
import com.asakusafw.compiler.batch.Workflow;
import com.asakusafw.compiler.batch.processor.JobFlowWorkDescriptionProcessor;

/**
 * Dumps environment variables: {@code ASAKUSA_*} and system properties: {@code com.asakusafw.*}.
 */
public class DumpEnvironmentProcessor extends AbstractWorkflowProcessor {

    static final Logger LOG = LoggerFactory.getLogger(DumpEnvironmentProcessor.class);

    static final Charset ENCODING = StandardCharsets.UTF_8;

    /**
     * The output path.
     */
    public static final String PATH = "etc/build.log"; //$NON-NLS-1$

    /**
     * The name prefix of target environment variables.
     */
    public static final String PREFIX_ENV = "ASAKUSA_"; //$NON-NLS-1$

    /**
     * The key prefix of target system properties.
     */
    public static final String PREFIX_SYSPROP = "com.asakusafw."; //$NON-NLS-1$

    @Override
    public Collection<Class<? extends WorkDescriptionProcessor<?>>> getDescriptionProcessors() {
        List<Class<? extends WorkDescriptionProcessor<?>>> results = new ArrayList<>();
        results.add(JobFlowWorkDescriptionProcessor.class);
        return results;
    }

    @Override
    public void process(Workflow workflow) throws IOException {
        try (Context context = new Context(getEnvironment().openResource(PATH))) {
            dumpInternal(context);
            dumpEnv(context);
            dumpSystemProperties(context);
        }
    }

    private void dumpInternal(Context context) {
        context.put("core.batchId = {0}", getEnvironment().getConfiguration().getBatchId()); //$NON-NLS-1$
        context.put("core.buildId = {0}", getEnvironment().getBuildId()); //$NON-NLS-1$
    }

    private void dumpEnv(Context context) {
        try {
            SortedMap<String, String> map = sortFilter(System.getenv(), PREFIX_ENV);
            for (Map.Entry<String, ?> entry : map.entrySet()) {
                context.put("{0} = {1}", entry.getKey(), entry.getValue()); //$NON-NLS-1$
            }
        } catch (SecurityException e) {
            LOG.warn(Messages.getString(
                    "DumpEnvironmentProcessor.warnFailedToObtainEnvironmentVariable"), e); //$NON-NLS-1$
        }
    }

    private void dumpSystemProperties(Context context) {
        try {
            SortedMap<String, Object> map = sortFilter(System.getProperties(), PREFIX_SYSPROP);
            for (Map.Entry<String, ?> entry : map.entrySet()) {
                context.put("{0} = {1}", entry.getKey(), entry.getValue()); //$NON-NLS-1$
            }
        } catch (SecurityException e) {
            LOG.warn(Messages.getString(
                    "DumpEnvironmentProcessor.warnFailedToObtainSystemProperty"), e); //$NON-NLS-1$
        }
    }

    private <T> SortedMap<String, T> sortFilter(Map<?, T> map, String prefix) {
        assert map != null;
        assert prefix != null;
        SortedMap<String, T> results = new TreeMap<>();
        for (Map.Entry<?, T> entry : map.entrySet()) {
            String key = String.valueOf(entry.getKey());
            if (key.startsWith(prefix)) {
                results.put(key, entry.getValue());
            }
        }
        return results;
    }

    private static class Context implements Closeable {

        private final PrintWriter writer;

        Context(OutputStream output) {
            assert output != null;
            writer = new PrintWriter(new OutputStreamWriter(output, ENCODING));
        }

        public void put(String pattern, Object... arguments) {
            assert pattern != null;
            assert arguments != null;
            String text;
            if (arguments.length == 0) {
                text = pattern;
            } else {
                text = MessageFormat.format(pattern, arguments);
            }
            writer.println(text);
            LOG.debug(text);
        }

        @Override
        public void close() throws IOException {
            writer.close();
        }
    }
}
