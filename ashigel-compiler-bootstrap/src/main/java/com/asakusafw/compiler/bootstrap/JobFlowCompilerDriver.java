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
package com.asakusafw.compiler.bootstrap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.flow.FlowCompilerOptions;
import com.asakusafw.compiler.flow.JobFlowClass;
import com.asakusafw.compiler.flow.JobFlowDriver;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.testing.DirectFlowCompiler;
import com.asakusafw.compiler.testing.JobflowInfo;
import com.asakusafw.compiler.testing.StageInfo;
import com.asakusafw.vocabulary.flow.FlowDescription;

/**
 * ジョブフローDSLをコンパイルするコンパイラのプログラムエントリ。
 */
public final class JobFlowCompilerDriver {

    static final Logger LOG = LoggerFactory.getLogger(JobFlowCompilerDriver.class);

    private static final Option OPT_OUTPUT;
    private static final Option OPT_PACKAGE;
    private static final Option OPT_HADOOPWORK;
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
        OPT_HADOOPWORK.setArgName("jobflow/working");
        OPT_HADOOPWORK.setRequired(true);

        OPT_LINK = new Option("link", true, "リンクするクラスライブラリの一覧");
        OPT_LINK.setArgName("classlib.jar" + File.pathSeparatorChar + "/path/to/classes");

        OPT_PLUGIN = new Option("plugin", true, "利用するコンパイラプラグインの一覧");
        OPT_PLUGIN.setArgName("plugin-1.jar" + File.pathSeparatorChar + "plugin-2.jar");
        OPT_PLUGIN.setRequired(false);

        OPT_CLASS = new Option("class", true, "コンパイル対象のジョブフロークラス名");
        OPT_CLASS.setArgName("class-name");
        OPT_CLASS.setRequired(true);

        OPTIONS = new Options();
        OPTIONS.addOption(OPT_OUTPUT);
        OPTIONS.addOption(OPT_PACKAGE);
        OPTIONS.addOption(OPT_HADOOPWORK);
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
                            JobFlowCompilerDriver.class.getName()),
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
        String link = cmd.getOptionValue(OPT_LINK.getOpt());
        String plugin = cmd.getOptionValue(OPT_PLUGIN.getOpt());

        Class<? extends FlowDescription> flowDescription =
            Class.forName(className).asSubclass(FlowDescription.class);
        Location hadoopWorkLocation = Location.fromPath(hadoopWork, '/');
        File outputDirectory = new File(output);
        List<File> linkingResources = new ArrayList<File>();
        if (link != null) {
            for (String s : link.split(File.pathSeparator)) {
                linkingResources.add(new File(s));
            }
        }
        final List<URL> pluginLocations = new ArrayList<URL>();
        if (plugin != null) {
            for (String s : plugin.split(File.pathSeparator)) {
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

        try {
            JobFlowDriver analyzed = JobFlowDriver.analyze(flowDescription);
            JobFlowClass flow = analyzed.getJobFlowClass();

            ClassLoader serviceLoader = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                @Override
                public ClassLoader run() {
                    URLClassLoader loader = new URLClassLoader(
                            pluginLocations.toArray(new URL[pluginLocations.size()]),
                            BatchCompilerDriver.class.getClassLoader());
                    return loader;
                }
            });
            JobflowInfo compiled = DirectFlowCompiler.compile(
                    flow.getGraph(),
                    "jobflow",
                    flow.getConfig().name(),
                    packageName,
                    hadoopWorkLocation,
                    outputDirectory,
                    linkingResources,
                    serviceLoader,
                    FlowCompilerOptions.load(System.getProperties()));

            LOG.info("=== compiled");
            LOG.info(compiled.getPackageFile().getAbsolutePath());
            LOG.info("=== sources");
            LOG.info(compiled.getSourceArchive().getAbsolutePath());
            LOG.info("=== stages");
            for (StageInfo stage : compiled.getStages()) {
                LOG.info(stage.getClassName());
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private JobFlowCompilerDriver() {
        return;
    }
}
