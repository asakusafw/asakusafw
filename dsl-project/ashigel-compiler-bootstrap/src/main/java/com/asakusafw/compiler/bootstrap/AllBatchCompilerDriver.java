/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
import java.text.MessageFormat;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.batch.ResourceRepository;
import com.asakusafw.compiler.batch.ResourceRepository.Cursor;
import com.asakusafw.compiler.common.FileRepository;
import com.asakusafw.compiler.common.ZipRepository;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.collections.Sets;
import com.asakusafw.vocabulary.batch.Batch;
import com.asakusafw.vocabulary.batch.BatchDescription;

/**
 * バッチDSLをまとめてコンパイルするコンパイラのプログラムエントリ。
 */
public final class AllBatchCompilerDriver {

    static final Logger LOG = LoggerFactory.getLogger(AllBatchCompilerDriver.class);

    private static final Option OPT_OUTPUT;
    private static final Option OPT_PACKAGE;
    private static final Option OPT_HADOOPWORK;
    private static final Option OPT_COMPILERWORK;
    private static final Option OPT_LINK;
    private static final Option OPT_PLUGIN;
    private static final Option OPT_SKIPERROR;
    private static final Option OPT_SCANPATH;

    private static final Options OPTIONS;
    static {
        OPT_OUTPUT = new Option("output", true, "コンパイル結果を出力する先のディレクトリ"); //$NON-NLS-1$
        OPT_OUTPUT.setArgName("/path/to/output"); //$NON-NLS-1$
        OPT_OUTPUT.setValueSeparator(File.pathSeparatorChar);
        OPT_OUTPUT.setRequired(true);

        OPT_PACKAGE = new Option("package", true, "コンパイル結果のベースパッケージ"); //$NON-NLS-1$
        OPT_PACKAGE.setArgName("pkg.name"); //$NON-NLS-1$
        OPT_PACKAGE.setRequired(true);

        OPT_HADOOPWORK = new Option("hadoopwork", true, "Hadoop上でのワーキングディレクトリ (ホームディレクトリからの相対パス)"); //$NON-NLS-1$
        OPT_HADOOPWORK.setArgName("batch/working"); //$NON-NLS-1$
        OPT_HADOOPWORK.setRequired(true);

        OPT_COMPILERWORK = new Option("compilerwork", true, "コンパイラのワーキングディレクトリ"); //$NON-NLS-1$
        OPT_COMPILERWORK.setArgName("/path/to/temporary"); //$NON-NLS-1$
        OPT_COMPILERWORK.setRequired(false);

        OPT_LINK = new Option("link", true, "リンクするクラスライブラリの一覧"); //$NON-NLS-1$
        OPT_LINK.setArgName("classlib.jar" + File.pathSeparatorChar + "/path/to/classes"); //$NON-NLS-1$ //$NON-NLS-2$

        OPT_PLUGIN = new Option("plugin", true, "利用するコンパイラプラグインの一覧"); //$NON-NLS-1$
        OPT_PLUGIN.setArgName("plugin-1.jar" + File.pathSeparatorChar + "plugin-2.jar"); //$NON-NLS-1$ //$NON-NLS-2$
        OPT_PLUGIN.setRequired(false);

        OPT_SKIPERROR = new Option("skiperror", "コンパイルエラーが発生しても続けて次のバッチをコンパイルする"); //$NON-NLS-1$

        OPT_SCANPATH = new Option("scanpath", true, "コンパイル対象のバッチを含むクラスライブラリ"); //$NON-NLS-1$
        OPT_SCANPATH.setArgName("/path/to/classlib"); //$NON-NLS-1$
        OPT_SCANPATH.setRequired(true);

        OPTIONS = new Options();
        OPTIONS.addOption(OPT_OUTPUT);
        OPTIONS.addOption(OPT_PACKAGE);
        OPTIONS.addOption(OPT_HADOOPWORK);
        OPTIONS.addOption(OPT_COMPILERWORK);
        OPTIONS.addOption(OPT_LINK);
        OPTIONS.addOption(OPT_PLUGIN);
        OPTIONS.addOption(OPT_SKIPERROR);
        OPTIONS.addOption(OPT_SCANPATH);
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
                            "java -classpath ... {0}", //$NON-NLS-1$
                            AllBatchCompilerDriver.class.getName()),
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
        String scanPath = cmd.getOptionValue(OPT_SCANPATH.getOpt());
        String packageName = cmd.getOptionValue(OPT_PACKAGE.getOpt());
        String hadoopWork = cmd.getOptionValue(OPT_HADOOPWORK.getOpt());
        String compilerWork = cmd.getOptionValue(OPT_COMPILERWORK.getOpt());
        String link = cmd.getOptionValue(OPT_LINK.getOpt());
        String plugin = cmd.getOptionValue(OPT_PLUGIN.getOpt());
        boolean skipError = cmd.hasOption(OPT_SKIPERROR.getOpt());

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

        Set<String> errorBatches = Sets.create();
        boolean succeeded = true;
        try {
            ResourceRepository scanner = getScanner(new File(scanPath));
            Cursor cursor = scanner.createCursor();
            try {
                while (cursor.next()) {
                    Location location = cursor.getLocation();
                    Class<? extends BatchDescription> batchDescription = getBatchDescription(location);
                    if (batchDescription == null) {
                        continue;
                    }
                    boolean singleSucceeded = BatchCompilerDriver.compile(
                            outputDirectory,
                            batchDescription,
                            packageName,
                            hadoopWorkLocation,
                            compilerWorkDirectory,
                            linkingResources,
                            pluginLocations);
                    succeeded &= singleSucceeded;
                    if (singleSucceeded == false) {
                        errorBatches.add(toClassName(location));
                        if (skipError == false) {
                            break;
                        }
                    }
                }
            } finally {
                cursor.close();
            }
        } catch (Exception e) {
            LOG.error(MessageFormat.format(
                    "バッチクラスの検索に失敗しました ({0})",
                    scanPath),
                    e);
        }
        if (succeeded) {
            LOG.info("バッチアプリケーションの生成が完了しました");
        } else {
            LOG.error(MessageFormat.format(
                    "バッチをコンパイルする際にエラーが発生しました: {0}",
                    errorBatches));
        }

        return succeeded;
    }

    private static Class<? extends BatchDescription> getBatchDescription(Location location) {
        assert location != null;
        if (isValidClassFileName(location) == false) {
            LOG.debug("invalid batch class name: {}", location); //$NON-NLS-1$
            return null;
        }
        String className = toClassName(location);
        Class<? extends BatchDescription> batchClass = loadIfBatchClass(className);
        if (batchClass == null) {
            LOG.debug("not a batch class: {}", className); //$NON-NLS-1$
            return null;
        }
        LOG.info(MessageFormat.format(
                "バッチクラスをコンパイルします: {0}",
                className));
        return batchClass;
    }

    private static String toClassName(Location location) {
        assert location != null;
        String className = location.toPath('.');
        className = className.substring(0, className.length() - ".class".length()); //$NON-NLS-1$
        return className;
    }

    private static boolean isValidClassFileName(Location location) {
        assert location != null;
        String simpleName = location.getName();
        if (simpleName.endsWith(".class") == false) { //$NON-NLS-1$
            return false;
        }
        for (Location current = location.getParent();
                current != null;
                current = current.getParent()) {
            if (current.getName().indexOf('.') >= 0) {
                return false;
            }
        }
        if (simpleName.indexOf('$') >= 0) {
            return false;
        }
        return true;
    }

    private static Class<? extends BatchDescription> loadIfBatchClass(String className) {
        try {
            Class<?> aClass = Class.forName(className);
            if (BatchDescription.class.isAssignableFrom(aClass) == false) {
                return null;
            }
            if (aClass.isAnnotationPresent(Batch.class) == false) {
                LOG.warn(MessageFormat.format(
                        "{0}には@Batchの指定がありません",
                        aClass.getName()));
                return null;
            }
            return aClass.asSubclass(BatchDescription.class);
        } catch (ClassNotFoundException e) {
            LOG.debug("failed to load batch class", e); //$NON-NLS-1$
            return null;
        }
    }

    private static ResourceRepository getScanner(File scanPath) throws IOException {
        assert scanPath != null;
        String name = scanPath.getName();
        if (scanPath.exists() == false) {
            throw new FileNotFoundException(MessageFormat.format(
                    "{0}が見つかりません",
                    scanPath));
        }
        if (scanPath.isDirectory()) {
            return new FileRepository(scanPath);
        } else if (scanPath.isFile() && (name.endsWith(".zip") || name.endsWith(".jar"))) { //$NON-NLS-1$ //$NON-NLS-2$
            return new ZipRepository(scanPath);
        } else {
            throw new IOException(MessageFormat.format(
                    "{0}を開けません",
                    scanPath));
        }
    }

    private AllBatchCompilerDriver() {
        return;
    }
}
