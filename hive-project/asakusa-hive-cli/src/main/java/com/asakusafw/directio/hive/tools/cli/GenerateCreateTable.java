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
package com.asakusafw.directio.hive.tools.cli;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.directio.hive.common.HiveTableInfo;

/**
 * Program entry for generate Hive QL {@code CREATE TABLE} statements.
 * @since 0.7.0
 */
public final class GenerateCreateTable {

    static final Logger LOG = LoggerFactory.getLogger(GenerateCreateTable.class);

    static final Option OPT_LOCATION;
    static final Option OPT_DATABASE;
    static final Option OPT_INCLUDE;
    static final Option OPT_CLASSPATH;
    static final Option OPT_PLUGINPATH;
    static final Option OPT_OUTPUT;
    static final Options OPTIONS;

    static {
        OPT_LOCATION = new Option("l", "location", true, //$NON-NLS-1$ //$NON-NLS-2$
                Messages.getString("GenerateCreateTable.optLocation")); //$NON-NLS-1$
        OPT_LOCATION.setArgName("URI"); //$NON-NLS-1$
        OPT_LOCATION.setRequired(false);

        OPT_DATABASE = new Option("db", "database", true, //$NON-NLS-1$ //$NON-NLS-2$
                Messages.getString("GenerateCreateTable.optDatabase")); //$NON-NLS-1$
        OPT_DATABASE.setArgName("dbname"); //$NON-NLS-1$
        OPT_DATABASE.setRequired(false);

        OPT_INCLUDE = new Option("i", "include", true, //$NON-NLS-1$ //$NON-NLS-2$
                Messages.getString("GenerateCreateTable.optInclude")); //$NON-NLS-1$
        OPT_INCLUDE.setArgName("regex"); //$NON-NLS-1$
        OPT_INCLUDE.setRequired(false);

        OPT_CLASSPATH = new Option("cp", "classpath", true, //$NON-NLS-1$ //$NON-NLS-2$
                Messages.getString("GenerateCreateTable.optClasspath")); //$NON-NLS-1$
        OPT_CLASSPATH.setArgName("/path/to/classes[" + File.pathSeparator + "...]"); //$NON-NLS-1$ //$NON-NLS-2$
        OPT_CLASSPATH.setRequired(true);

        OPT_PLUGINPATH = new Option("pp", "pluginpath", true, //$NON-NLS-1$ //$NON-NLS-2$
                Messages.getString("GenerateCreateTable.optPluginpath")); //$NON-NLS-1$
        OPT_PLUGINPATH.setArgName("/path/to/plugin.jar[" + File.pathSeparator + "...]"); //$NON-NLS-1$ //$NON-NLS-2$
        OPT_PLUGINPATH.setRequired(false);

        OPT_OUTPUT = new Option("o", "output", true, //$NON-NLS-1$ //$NON-NLS-2$
                Messages.getString("GenerateCreateTable.optOutput")); //$NON-NLS-1$
        OPT_OUTPUT.setArgName("/path/to/output-file"); //$NON-NLS-1$
        OPT_OUTPUT.setRequired(true);

        OPTIONS = new Options();
        OPTIONS.addOption(OPT_LOCATION);
        OPTIONS.addOption(OPT_DATABASE);
        OPTIONS.addOption(OPT_INCLUDE);
        OPTIONS.addOption(OPT_CLASSPATH);
        OPTIONS.addOption(OPT_PLUGINPATH);
        OPTIONS.addOption(OPT_OUTPUT);
    }

    private GenerateCreateTable() {
        return;
    }

    /**
     * The program entry.
     * @param args class-paths... output-file
     * @throws IOException if failed by I/O error
     */
    public static void main(String[] args) throws IOException {
        int exit = execute(args);
        if (exit != 0) {
            System.exit(exit);
        }
    }

    static int execute(String... args) {
        GenerateCreateTableTask task = new GenerateCreateTableTask();
        GenerateCreateTableTask.Configuration conf;
        try {
            conf = parseConfiguration(args);
        } catch (Exception e) {
            LOG.error(Messages.getString("GenerateCreateTable.errorInvalidArgument"), e); //$NON-NLS-1$
            HelpFormatter formatter = new HelpFormatter();
            formatter.setWidth(Integer.MAX_VALUE);
            formatter.printHelp(
                    MessageFormat.format(
                            "java -classpath ... {0}", //$NON-NLS-1$
                            GenerateCreateTable.class.getName()),
                    OPTIONS,
                    true);
            return 1;
        }
        try {
            task.perform(conf);
        } catch (Exception e) {
            LOG.error(Messages.getString("GenerateCreateTable.errorExecute"), e); //$NON-NLS-1$
            return 1;
        } finally {
            closeQuiet(conf.classLoader);
        }
        return 0;
    }

    static GenerateCreateTableTask.Configuration parseConfiguration(String... args) throws ParseException {
        LOG.debug("Analyzing arguments: {}", Arrays.toString(args)); //$NON-NLS-1$

        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(OPTIONS, args);
        String classpath = getOption(cmd, OPT_CLASSPATH);
        String pluginpath = getOption(cmd, OPT_PLUGINPATH);
        String output = getOption(cmd, OPT_OUTPUT);
        String include = getOption(cmd, OPT_INCLUDE);
        String location = getOption(cmd, OPT_LOCATION);
        String database = getOption(cmd, OPT_DATABASE);

        List<File> sources = parseFileList(classpath);
        List<File> plugins = parseFileList(pluginpath);
        List<File> classpathAdditions = new ArrayList<>();
        classpathAdditions.addAll(sources);
        classpathAdditions.addAll(plugins);

        Pattern acceptTableNames = null;
        if (include != null) {
            try {
                acceptTableNames = Pattern.compile(include);
            } catch (PatternSyntaxException e) {
                throw new IllegalArgumentException(MessageFormat.format(
                        Messages.getString("GenerateCreateTable.errorInvalidInclude"), //$NON-NLS-1$
                        OPT_INCLUDE.getLongOpt(),
                        include));
            }
        }

        Stringnizer locationProvider = null;
        if (location != null) {
            final String prefix = location.endsWith("/") ? location : location + '/'; //$NON-NLS-1$
            locationProvider = new Stringnizer() {
                @Override
                public String toString(HiveTableInfo table) {
                    return prefix + table.getTableName();
                }
            };
        }

        if (database != null) {
            database = database.trim();
            if (database.isEmpty()) {
                database = null;
            }
        }
        File outputFile = new File(output);
        URLClassLoader classLoader = buildPluginLoader(
                GenerateCreateTable.class.getClassLoader(),
                classpathAdditions);
        return new GenerateCreateTableTask.Configuration(
                classLoader,
                sources,
                acceptTableNames,
                locationProvider,
                database,
                outputFile);
    }

    private static String getOption(CommandLine cmd, Option option) {
        String value = cmd.getOptionValue(option.getOpt());
        LOG.debug("Option: {}={}", option.getLongOpt(), value); //$NON-NLS-1$
        return value;
    }

    /**
     * Parses a string of file list separated by the platform dependent path separator.
     * @param fileListOrNull target string, or {@code null}
     * @return the represented file list, or an empty list if not specified
     */
    private static List<File> parseFileList(String fileListOrNull) {
        if (fileListOrNull == null || fileListOrNull.isEmpty()) {
            return Collections.emptyList();
        }
        List<File> results = new ArrayList<>();
        int start = 0;
        while (true) {
            int index = fileListOrNull.indexOf(File.pathSeparatorChar, start);
            if (index < 0) {
                break;
            }
            if (start != index) {
                results.add(new File(fileListOrNull.substring(start, index).trim()));
            }
            start = index + 1;
        }
        results.add(new File(fileListOrNull.substring(start).trim()));
        return results;
    }

    /**
     * Creates a class loader for loading plug-ins.
     * @param parent parent class loader, or {@code null} to use the system class loader
     * @param files plug-in class paths (*.jar file or class path directory)
     * @return the created class loader
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    private static URLClassLoader buildPluginLoader(final ClassLoader parent, List<File> files) {
        if (files == null) {
            throw new IllegalArgumentException("files must not be null"); //$NON-NLS-1$
        }
        final List<URL> locations = new ArrayList<>();
        for (File file : files) {
            try {
                if (file.exists() == false) {
                    throw new FileNotFoundException(MessageFormat.format(
                            Messages.getString("GenerateCreateTable.errorMissingPluginFile"), //$NON-NLS-1$
                            file.getAbsolutePath()));
                }
                URL url = file.toURI().toURL();
                locations.add(url);
            } catch (IOException e) {
                LOG.warn(MessageFormat.format(
                        Messages.getString("GenerateCreateTable.warnInvalidPluginFile"), //$NON-NLS-1$
                        file.getAbsolutePath()), e);
            }
        }
        return AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {
            @Override
            public URLClassLoader run() {
                return new URLClassLoader(
                        locations.toArray(new URL[locations.size()]),
                        parent);
            }
        });
    }

    private static void closeQuiet(Object object) {
        if (object instanceof Closeable) {
            try {
                ((Closeable) object).close();
            } catch (IOException e) {
                LOG.warn(MessageFormat.format(
                        Messages.getString("GenerateCreateTable.errorFailedToClose"), //$NON-NLS-1$
                        object), e);
            }
        }
    }
}
