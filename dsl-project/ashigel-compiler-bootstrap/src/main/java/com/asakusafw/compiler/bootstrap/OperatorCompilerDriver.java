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
package com.asakusafw.compiler.bootstrap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.utils.collections.Lists;

/**
 * 演算子クラスを直接コンパイルする。
 */
public final class OperatorCompilerDriver {

    static final Logger LOG = LoggerFactory.getLogger(OperatorCompilerDriver.class);

    private static final Option OPT_SOURCEPATH;
    private static final Option OPT_OUTPUT;
    private static final Option OPT_ENCODING;
    private static final Option OPT_CLASSES;

    private static final Options OPTIONS;
    static {
        OPT_SOURCEPATH = new Option("sourcepath", true, "コンパイル対象のソースコードを含むソースパス"); //$NON-NLS-1$
        OPT_SOURCEPATH.setArgName("/path/to/sourceroot"); //$NON-NLS-1$
        OPT_SOURCEPATH.setRequired(true);

        OPT_ENCODING = new Option("encoding", true, "コンパイル対象の文字エンコーディング"); //$NON-NLS-1$
        OPT_ENCODING.setArgName("charset-name"); //$NON-NLS-1$

        OPT_OUTPUT = new Option("output", true, "コンパイル結果を出力する先のディレクトリ"); //$NON-NLS-1$
        OPT_OUTPUT.setArgName("/path/to/output"); //$NON-NLS-1$
        OPT_OUTPUT.setValueSeparator(File.pathSeparatorChar);
        OPT_OUTPUT.setRequired(true);

        OPT_CLASSES = new Option("class", true, "コンパイル対象のクラス名一覧"); //$NON-NLS-1$
        OPT_CLASSES.setArgName("class-names"); //$NON-NLS-1$
        OPT_CLASSES.setArgs(Option.UNLIMITED_VALUES);
        OPT_CLASSES.setRequired(true);

        OPTIONS = new Options();
        OPTIONS.addOption(OPT_SOURCEPATH);
        OPTIONS.addOption(OPT_ENCODING);
        OPTIONS.addOption(OPT_OUTPUT);
        OPTIONS.addOption(OPT_CLASSES);
    }

    /**
     * 演算子クラスをコンパイルする。
     * @param sourcePath ソースパス
     * @param outputPath 出力先のパス
     * @param encoding エンコーディング
     * @param operatorClasses コンパイル対象の一覧
     * @throws IOException 書き出しに失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public static void compile(
            File sourcePath,
            File outputPath,
            Charset encoding,
            List<Class<?>> operatorClasses) throws IOException {
        if (sourcePath == null) {
            throw new IllegalArgumentException("sourcePath must not be null"); //$NON-NLS-1$
        }
        if (outputPath == null) {
            throw new IllegalArgumentException("outputPath must not be null"); //$NON-NLS-1$
        }
        if (encoding == null) {
            throw new IllegalArgumentException("encoding must not be null"); //$NON-NLS-1$
        }
        if (operatorClasses == null) {
            throw new IllegalArgumentException("operatorClasses must not be null"); //$NON-NLS-1$
        }

        // 内部的にはJSR-199を使ってコンパイラを起動するだけ
        List<File> sourceFiles = toSources(sourcePath, operatorClasses);
        LOG.info(MessageFormat.format(
                "Compiling {0}",
                sourceFiles));
        List<String> arguments = toArguments(sourcePath, outputPath, encoding);
        LOG.debug("Compiler arguments {}", arguments); //$NON-NLS-1$
        if (outputPath.isDirectory() == false && outputPath.mkdirs() == false) {
            throw new IOException(MessageFormat.format(
                    "Failed to create {0}",
                    outputPath));
        }
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IOException("Failed to create a compiler");
        }
        StandardJavaFileManager files = compiler.getStandardFileManager(null, null, encoding);
        try {
            CompilationTask task = compiler.getTask(
                    null,
                    files,
                    null,
                    arguments,
                    Collections.<String>emptyList(),
                    files.getJavaFileObjectsFromFiles(sourceFiles));
            if (task.call() == false) {
                LOG.error("Compilation Failed");
            }
        } finally {
            files.close();
        }
        LOG.info("Completed");
    }

    private static List<String> toArguments(
            File sourcePath,
            File outputPath,
            Charset encoding) {
        assert sourcePath != null;
        assert outputPath != null;
        assert encoding != null;
        List<String> results = Lists.create();
        Collections.addAll(results, "-proc:only"); //$NON-NLS-1$
        Collections.addAll(results, "-source", "1.6"); //$NON-NLS-1$ //$NON-NLS-2$
        Collections.addAll(results, "-target", "1.6"); //$NON-NLS-1$ //$NON-NLS-2$
        Collections.addAll(results, "-encoding", encoding.displayName()); //$NON-NLS-1$
        Collections.addAll(results, "-sourcepath", sourcePath.getAbsolutePath()); //$NON-NLS-1$
        Collections.addAll(results, "-s", outputPath.getAbsolutePath()); //$NON-NLS-1$
        return results;
    }

    private static List<File> toSources(
            File sourcePath,
            List<Class<?>> operatorClasses) throws IOException {
        assert sourcePath != null;
        assert operatorClasses != null;
        Set<File> results = new LinkedHashSet<File>();
        for (Class<?> aClass : operatorClasses) {
            File source = findSource(sourcePath, aClass);
            if (results.contains(source) == false) {
                results.add(source);
            }
        }
        return Lists.from(results);
    }

    private static File findSource(File sourcePath, Class<?> aClass) throws IOException {
        assert sourcePath != null;
        assert aClass != null;
        String[] segments = aClass.getName().split("\\."); //$NON-NLS-1$
        File current = sourcePath;
        for (int i = 0; i < segments.length - 1; i++) {
            current = new File(current, segments[i]);
            if (current.isDirectory() == false) {
                throw new FileNotFoundException(MessageFormat.format(
                        "{0} (for {1})",
                        current,
                        aClass.getName()));
            }
        }
        String name = segments[segments.length - 1];
        int enclosing = name.indexOf('$');
        if (enclosing >= 0) {
            name = name.substring(0, enclosing);
        }
        File file = new File(current, name + ".java"); //$NON-NLS-1$
        if (file.isFile() == false) {
            if (current.isDirectory() == false) {
                throw new FileNotFoundException(MessageFormat.format(
                        "{0} (for {1})",
                        file,
                        aClass.getName()));
            }
        }
        return file.getCanonicalFile();
    }

    /**
     * プログラムエントリ。
     * @param args コマンドライン引数
     */
    public static void main(String... args) {
        try {
            start(args);
        } catch (Exception e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.setWidth(Integer.MAX_VALUE);
            formatter.printHelp(
                    MessageFormat.format(
                            "java -classpath ... {0}", //$NON-NLS-1$
                            OperatorCompilerDriver.class.getName()),
                    OPTIONS,
                    true);
            e.printStackTrace(System.out);
            System.exit(1);
        }
    }

    private static void start(String[] args) throws Exception {
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(OPTIONS, args);
        String sourcePath = cmd.getOptionValue(OPT_SOURCEPATH.getOpt());
        String output = cmd.getOptionValue(OPT_OUTPUT.getOpt());
        String encoding = cmd.getOptionValue(OPT_ENCODING.getOpt(), "UTF-8"); //$NON-NLS-1$
        String[] classes = cmd.getOptionValues(OPT_CLASSES.getOpt());

        List<Class<?>> operatorClasses = Lists.create();
        for (String className : classes) {
            Class<?> oc = Class.forName(className);
            operatorClasses.add(oc);
        }
        compile(
                new File(sourcePath),
                new File(output),
                Charset.forName(encoding),
                operatorClasses);
    }

    private OperatorCompilerDriver() {
        return;
    }
}
