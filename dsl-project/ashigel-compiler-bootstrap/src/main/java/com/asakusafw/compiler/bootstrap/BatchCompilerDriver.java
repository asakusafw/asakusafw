/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.batch.BatchDriver;
import com.asakusafw.compiler.flow.FlowCompilerOptions;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.testing.DirectBatchCompiler;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.vocabulary.batch.BatchDescription;

/**
 * バッチDSLをコンパイルするコンパイラのプログラムエントリ。
 */
public final class BatchCompilerDriver {

    static final Logger LOG = LoggerFactory.getLogger(BatchCompilerDriver.class);

    private static final Option OPT_OUTPUT;
    private static final Option OPT_PACKAGE;
    private static final Option OPT_HADOOPWORK;
    private static final Option OPT_COMPILERWORK;
    private static final Option OPT_LINK;
    private static final Option OPT_PLUGIN;
    private static final Option OPT_CLASS;

    private static final Options OPTIONS;
    static {
        OPT_OUTPUT = new Option("output", true, "コンパイル結果を出力する先のディレクトリ");
        OPT_OUTPUT.setArgName("/path/to/output");
        OPT_OUTPUT.setValueSeparator(File.pathSeparatorChar);
        OPT_OUTPUT.setRequired(true);

        OPT_PACKAGE = new Option("package", true, "コンパイル結果のベースパッケージ");
        OPT_PACKAGE.setArgName("pkg.name");
        OPT_PACKAGE.setRequired(true);

        OPT_HADOOPWORK = new Option("hadoopwork", true, "Hadoop上でのワーキングディレクトリ (ホームディレクトリからの相対パス)");
        OPT_HADOOPWORK.setArgName("batch/working");
        OPT_HADOOPWORK.setRequired(true);

        OPT_COMPILERWORK = new Option("compilerwork", true, "コンパイラのワーキングディレクトリ");
        OPT_COMPILERWORK.setArgName("/path/to/temporary");
        OPT_COMPILERWORK.setRequired(false);

        OPT_LINK = new Option("link", true, "リンクするクラスライブラリの一覧");
        OPT_LINK.setArgName("classlib.jar" + File.pathSeparatorChar + "/path/to/classes");

        OPT_PLUGIN = new Option("plugin", true, "利用するコンパイラプラグインの一覧");
        OPT_PLUGIN.setArgName("plugin-1.jar" + File.pathSeparatorChar + "plugin-2.jar");
        OPT_PLUGIN.setRequired(false);

        OPT_CLASS = new Option("class", true, "コンパイル対象のバッチクラス名");
        OPT_CLASS.setArgName("class-name");
        OPT_CLASS.setRequired(true);

        OPTIONS = new Options();
        OPTIONS.addOption(OPT_OUTPUT);
        OPTIONS.addOption(OPT_PACKAGE);
        OPTIONS.addOption(OPT_HADOOPWORK);
        OPTIONS.addOption(OPT_COMPILERWORK);
        OPTIONS.addOption(OPT_LINK);
        OPTIONS.addOption(OPT_PLUGIN);
        OPTIONS.addOption(OPT_CLASS);
    }

    /**
     * プログラムエントリ。
     * @param args コマンドライン引数
     */
    public static void main(String... args) {
        try {
            if (start(args) == false) {
                System.exit(1);
            }
        } catch (Exception e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.setWidth(Integer.MAX_VALUE);
            formatter.printHelp(
                    MessageFormat.format(
                            "java -classpath ... {0}",
                            BatchCompilerDriver.class.getName()),
                    OPTIONS,
                    true);
            e.printStackTrace(System.out);
            System.exit(1);
        }
    }

    private static boolean start(String[] args) throws Exception {
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(OPTIONS, args);
        String output = cmd.getOptionValue(OPT_OUTPUT.getOpt());
        String className = cmd.getOptionValue(OPT_CLASS.getOpt());
        String packageName = cmd.getOptionValue(OPT_PACKAGE.getOpt());
        String hadoopWork = cmd.getOptionValue(OPT_HADOOPWORK.getOpt());
        String compilerWork = cmd.getOptionValue(OPT_COMPILERWORK.getOpt());
        String link = cmd.getOptionValue(OPT_LINK.getOpt());
        String plugin = cmd.getOptionValue(OPT_PLUGIN.getOpt());

        File outputDirectory = new File(output);
        Location hadoopWorkLocation = Location.fromPath(hadoopWork, '/');
        File compilerWorkDirectory = new File(compilerWork);
        List<File> linkingResources = Lists.create();
        if (link != null) {
            for (String s : link.split(File.pathSeparator)) {
                linkingResources.add(new File(s));
            }
        }
        List<URL> pluginLocations = Lists.create();
        if (plugin != null) {
            for (String s : plugin.split(File.pathSeparator)) {
                if (s.trim().isEmpty()) {
                    continue;
                }
                try {
                    File file = new File(s);
                    if (file.exists() == false) {
                        throw new FileNotFoundException(file.getAbsolutePath());
                    }
                    URL url = file.toURI().toURL();
                    pluginLocations.add(url);
                } catch (IOException e) {
                    LOG.warn(MessageFormat.format(
                            "プラグイン{0}をロードできませんでした",
                            s),
                            e);
                }
            }
        }

        Class<? extends BatchDescription> batchDescription =
            Class.forName(className).asSubclass(BatchDescription.class);
        boolean succeeded = compile(
                outputDirectory,
                batchDescription,
                packageName,
                hadoopWorkLocation,
                compilerWorkDirectory,
                linkingResources,
                pluginLocations);

        if (succeeded) {
            LOG.info("{}をコンパイルしました", batchDescription.getName());
        } else {
            LOG.error(MessageFormat.format(
                    "コンパイルはエラーにより中断しました ({0})",
                    className));
        }
        return succeeded;
    }

    static boolean compile(
            File outputDirectory,
            Class<? extends BatchDescription> batchDescription,
            String packageName,
            Location hadoopWorkLocation,
            File compilerWorkDirectory,
            List<File> linkingResources,
            final List<URL> pluginLibraries) {
        assert outputDirectory != null;
        assert batchDescription != null;
        assert packageName != null;
        assert hadoopWorkLocation != null;
        assert compilerWorkDirectory != null;
        assert linkingResources != null;
        try {
            BatchDriver analyzed = BatchDriver.analyze(batchDescription);
            if (analyzed.hasError()) {
                for (String diagnostic : analyzed.getDiagnostics()) {
                    LOG.error(diagnostic);
                }
                return false;
            }

            String batchId = analyzed.getBatchClass().getConfig().name();
            ClassLoader serviceLoader = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                @Override
                public ClassLoader run() {
                    URLClassLoader loader = new URLClassLoader(
                            pluginLibraries.toArray(new URL[pluginLibraries.size()]),
                            BatchCompilerDriver.class.getClassLoader());
                    return loader;
                }
            });
            DirectBatchCompiler.compile(
                    analyzed.getDescription(),
                    packageName,
                    hadoopWorkLocation,
                    new File(outputDirectory, batchId),
                    new File(compilerWorkDirectory, batchId),
                    linkingResources,
                    serviceLoader,
                    FlowCompilerOptions.load(System.getProperties()));
            return true;
        } catch (Exception e) {
            LOG.error(
                    MessageFormat.format(
                            "コンパイルはエラーにより中断しました ({0})",
                            batchDescription.getName()),
                    e);
            return false;
        }
    }

    private BatchCompilerDriver() {
        return;
    }
}
