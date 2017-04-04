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
package com.asakusafw.compiler.bootstrap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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
import com.asakusafw.vocabulary.batch.Batch;
import com.asakusafw.vocabulary.batch.BatchDescription;

/**
 * Program entry for compiling all batch classes under a class library.
 * @see BatchCompilerDriver
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
    private static final Option OPT_INCLUDE;
    private static final Option OPT_EXCLUDE;

    private static final Options OPTIONS;
    static {
        OPT_OUTPUT = new Option("output", true, //$NON-NLS-1$
                Messages.getString("AllBatchCompilerDriver.optOutput")); //$NON-NLS-1$
        OPT_OUTPUT.setArgName("/path/to/output"); //$NON-NLS-1$
        OPT_OUTPUT.setValueSeparator(File.pathSeparatorChar);
        OPT_OUTPUT.setRequired(true);

        OPT_PACKAGE = new Option("package", true, //$NON-NLS-1$
                Messages.getString("AllBatchCompilerDriver.optPackage")); //$NON-NLS-1$
        OPT_PACKAGE.setArgName("pkg.name"); //$NON-NLS-1$
        OPT_PACKAGE.setRequired(true);

        OPT_HADOOPWORK = new Option("hadoopwork", true, //$NON-NLS-1$
                Messages.getString("AllBatchCompilerDriver.optHadoopwork")); //$NON-NLS-1$
        OPT_HADOOPWORK.setArgName("batch/working"); //$NON-NLS-1$
        OPT_HADOOPWORK.setRequired(true);

        OPT_COMPILERWORK = new Option("compilerwork", true, //$NON-NLS-1$
                Messages.getString("AllBatchCompilerDriver.optCompilerwork")); //$NON-NLS-1$
        OPT_COMPILERWORK.setArgName("/path/to/temporary"); //$NON-NLS-1$
        OPT_COMPILERWORK.setRequired(false);

        OPT_LINK = new Option("link", true, //$NON-NLS-1$
                Messages.getString("AllBatchCompilerDriver.optLink")); //$NON-NLS-1$
        OPT_LINK.setArgName("classlib.jar" + File.pathSeparatorChar + "/path/to/classes"); //$NON-NLS-1$ //$NON-NLS-2$

        OPT_PLUGIN = new Option("plugin", true, //$NON-NLS-1$
                Messages.getString("AllBatchCompilerDriver.optPlugin")); //$NON-NLS-1$
        OPT_PLUGIN.setArgName("plugin-1.jar" + File.pathSeparatorChar + "plugin-2.jar"); //$NON-NLS-1$ //$NON-NLS-2$
        OPT_PLUGIN.setRequired(false);

        OPT_SKIPERROR = new Option("skiperror", //$NON-NLS-1$
                Messages.getString("AllBatchCompilerDriver.optSkiperror")); //$NON-NLS-1$

        OPT_SCANPATH = new Option("scanpath", true, //$NON-NLS-1$
                Messages.getString("AllBatchCompilerDriver.optScanpath")); //$NON-NLS-1$
        OPT_SCANPATH.setArgName("/path/to/classlib"); //$NON-NLS-1$
        OPT_SCANPATH.setRequired(true);

        OPT_INCLUDE = new Option("include", true, //$NON-NLS-1$
                Messages.getString("AllBatchCompilerDriver.optInclude")); //$NON-NLS-1$
        OPT_INCLUDE.setArgName("class-pattern1[,class-pattern2[,..]]"); //$NON-NLS-1$
        OPT_INCLUDE.setRequired(false);

        OPT_EXCLUDE = new Option("exclude", true, //$NON-NLS-1$
                Messages.getString("AllBatchCompilerDriver.optExclude")); //$NON-NLS-1$
        OPT_EXCLUDE.setArgName("class-pattern1[,class-pattern2[,..]]"); //$NON-NLS-1$
        OPT_EXCLUDE.setRequired(false);

        OPTIONS = new Options();
        OPTIONS.addOption(OPT_OUTPUT);
        OPTIONS.addOption(OPT_PACKAGE);
        OPTIONS.addOption(OPT_HADOOPWORK);
        OPTIONS.addOption(OPT_COMPILERWORK);
        OPTIONS.addOption(OPT_LINK);
        OPTIONS.addOption(OPT_PLUGIN);
        OPTIONS.addOption(OPT_SKIPERROR);
        OPTIONS.addOption(OPT_SCANPATH);
        OPTIONS.addOption(OPT_INCLUDE);
        OPTIONS.addOption(OPT_EXCLUDE);
    }

    /**
     * The program entry.
     * @param args command line arguments
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
        String include = cmd.getOptionValue(OPT_INCLUDE.getOpt());
        String exclude = cmd.getOptionValue(OPT_EXCLUDE.getOpt());

        File outputDirectory = new File(output);
        Location hadoopWorkLocation = Location.fromPath(hadoopWork, '/');
        File compilerWorkDirectory = new File(compilerWork);
        List<File> linkingResources = extractEmbedResources(link);
        List<URL> pluginLocations = extractPluginPath(plugin);
        ClassNamePattern includePattern = ClassNamePattern.parse(include, true);
        ClassNamePattern excludePattern = ClassNamePattern.parse(exclude, false);
        Set<String> errorBatches = new HashSet<>();
        boolean succeeded = true;
        try {
            ResourceRepository scanner = getScanner(new File(scanPath));
            try (Cursor cursor = scanner.createCursor()) {
                while (cursor.next()) {
                    Location location = cursor.getLocation();
                    Class<? extends BatchDescription> batchDescription =
                            getBatchDescription(location, includePattern, excludePattern);
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
            }
        } catch (Exception e) {
            LOG.error(MessageFormat.format(
                    Messages.getString("AllBatchCompilerDriver.errorFailedToSearchForBatchClass"), //$NON-NLS-1$
                    scanPath),
                    e);
        }
        if (succeeded) {
            LOG.info(Messages.getString("AllBatchCompilerDriver.infoCompleteBatchClass")); //$NON-NLS-1$
        } else {
            LOG.error(MessageFormat.format(
                    Messages.getString("AllBatchCompilerDriver.errorFailedToCompileBatchClass"), //$NON-NLS-1$
                    errorBatches));
        }

        return succeeded;
    }

    private static List<File> extractEmbedResources(String path) {
        if (path == null) {
            return Collections.emptyList();
        }
        List<File> results = new ArrayList<>();
        for (String s : path.split(File.pathSeparator)) {
            results.add(new File(s));
        }
        return results;
    }

    private static List<URL> extractPluginPath(String path) {
        if (path == null) {
            return Collections.emptyList();
        }
        List<URL> results = new ArrayList<>();
        for (String s : path.split(File.pathSeparator)) {
            if (s.trim().isEmpty()) {
                continue;
            }
            try {
                File file = new File(s);
                if (file.exists() == false) {
                    throw new FileNotFoundException(file.getAbsolutePath());
                }
                URL url = file.toURI().toURL();
                results.add(url);
            } catch (IOException e) {
                LOG.warn(MessageFormat.format(
                        Messages.getString("AllBatchCompilerDriver.warnFailedToLoadPlugin"), //$NON-NLS-1$
                        s),
                        e);
            }
        }
        return results;
    }

    private static Class<? extends BatchDescription> getBatchDescription(
            Location location,
            ClassNamePattern include, ClassNamePattern exclude) {
        assert location != null;
        assert include != null;
        assert exclude != null;
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
        if (include.accepts(className) == false || exclude.accepts(className)) {
            LOG.info(MessageFormat.format(
                    Messages.getString("AllBatchCompilerDriver.infoSkipBatchClass"), //$NON-NLS-1$
                    className));
            return null;
        }
        LOG.info(MessageFormat.format(
                Messages.getString("AllBatchCompilerDriver.infoStartBatchClass"), //$NON-NLS-1$
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
            Class<?> aClass = Class.forName(className, false, AllBatchCompilerDriver.class.getClassLoader());
            if (BatchDescription.class.isAssignableFrom(aClass) == false) {
                return null;
            }
            if (aClass.isAnnotationPresent(Batch.class) == false) {
                LOG.warn(MessageFormat.format(
                        Messages.getString("AllBatchCompilerDriver.warnMissingAnnotation"), //$NON-NLS-1$
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
                    Messages.getString("AllBatchCompilerDriver.errorMissingScanpath"), //$NON-NLS-1$
                    scanPath));
        }
        if (scanPath.isDirectory()) {
            return new FileRepository(scanPath);
        } else if (scanPath.isFile() && (name.endsWith(".zip") || name.endsWith(".jar"))) { //$NON-NLS-1$ //$NON-NLS-2$
            return new ZipRepository(scanPath);
        } else {
            throw new IOException(MessageFormat.format(
                    Messages.getString("AllBatchCompilerDriver.errorInvalidScanpath"), //$NON-NLS-1$
                    scanPath));
        }
    }

    private AllBatchCompilerDriver() {
        return;
    }
}
