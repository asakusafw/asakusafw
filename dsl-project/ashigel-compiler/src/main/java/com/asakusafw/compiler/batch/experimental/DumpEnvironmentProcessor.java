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
package com.asakusafw.compiler.batch.experimental;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.text.MessageFormat;
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
import com.asakusafw.compiler.batch.processor.ScriptWorkDescriptionProcessor;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.utils.collections.Lists;

/**
 * 環境変数{@code ASAKUSA_*}、およびシステムプロパティ{@code com.asakusafw.*}の一覧を出力する。
 */
public class DumpEnvironmentProcessor extends AbstractWorkflowProcessor {

    static final Logger LOG = LoggerFactory.getLogger(DumpEnvironmentProcessor.class);

    static final Charset ENCODING = Charset.forName("UTF-8"); //$NON-NLS-1$

    /**
     * 出力先のパス。
     */
    public static final String PATH = "etc/build.log"; //$NON-NLS-1$

    /**
     * 出力する環境変数の接頭辞。
     */
    public static final String PREFIX_ENV = "ASAKUSA_"; //$NON-NLS-1$

    /**
     * 出力するシステムプロパティの接頭辞。
     */
    public static final String PREFIX_SYSPROP = "com.asakusafw."; //$NON-NLS-1$

    /**
     * 実験用のシェルスクリプトの出力先を返す。
     * @param outputDir コンパイル結果の出力先ディレクトリ
     * @return 実験用のシェルスクリプトの出力先
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public static File getScriptOutput(File outputDir) {
        Precondition.checkMustNotBeNull(outputDir, "outputDir"); //$NON-NLS-1$
        return new File(outputDir, PATH);
    }

    @Override
    public Collection<Class<? extends WorkDescriptionProcessor<?>>> getDescriptionProcessors() {
        List<Class<? extends WorkDescriptionProcessor<?>>> results = Lists.create();
        results.add(JobFlowWorkDescriptionProcessor.class);
        results.add(ScriptWorkDescriptionProcessor.class);
        return results;
    }

    @Override
    public void process(Workflow workflow) throws IOException {
        OutputStream output = getEnvironment().openResource(PATH);
        try {
            Context context = new Context(output);
            dumpInternal(context);
            dumpEnv(context);
            dumpSystemProperties(context);
            context.close();
        } finally {
            output.close();
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
        SortedMap<String, T> results = new TreeMap<String, T>();
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

        public Context(OutputStream output) {
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
