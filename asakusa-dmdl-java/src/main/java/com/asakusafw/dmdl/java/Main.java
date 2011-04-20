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
package com.asakusafw.dmdl.java;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.dmdl.source.CompositeSourceRepository;
import com.asakusafw.dmdl.source.DmdlSourceDirectory;
import com.asakusafw.dmdl.source.DmdlSourceFile;
import com.asakusafw.dmdl.source.DmdlSourceRepository;
import com.ashigeru.lang.java.model.syntax.ModelFactory;
import com.ashigeru.lang.java.model.util.Filer;
import com.ashigeru.lang.java.model.util.Models;

/**
 * Asakusa DMDL Compiler Command Line Interface.
 */
public class Main {

    static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private static final Option OPT_OUTPUT;
    private static final Option OPT_PACKAGE;
    private static final Option OPT_SOURCE_ENCODING;
    private static final Option OPT_TARGET_ENCODING;
    private static final Option OPT_SOURCE_PATH;
    private static final Option OPT_PLUGIN;

    private static final Options OPTIONS;
    static {
        OPT_OUTPUT = new Option("output", true, "output directory location");
        OPT_OUTPUT.setArgName("/path/to/output");
        OPT_OUTPUT.setRequired(true);

        OPT_SOURCE_PATH = new Option("source", true, "source file or source directory paths");
        OPT_SOURCE_PATH.setArgName("source-file.dmdl" + File.pathSeparatorChar + "/path/to/source");
        OPT_SOURCE_PATH.setValueSeparator(File.pathSeparatorChar);
        OPT_SOURCE_PATH.setRequired(true);

        OPT_PACKAGE = new Option("package", true, "package name of output Java files");
        OPT_PACKAGE.setArgName("pkg.name");
        OPT_PACKAGE.setRequired(true);

        OPT_SOURCE_ENCODING = new Option("sourceencoding", true, "input DMDL charset encoding");
        OPT_SOURCE_ENCODING.setArgName("source-encoding");
        OPT_SOURCE_ENCODING.setRequired(false);

        OPT_TARGET_ENCODING = new Option("targetencoding", true, "output Java charset encoding");
        OPT_TARGET_ENCODING.setArgName("target-encoding");
        OPT_TARGET_ENCODING.setRequired(false);

        OPT_PLUGIN = new Option("plugin", true, "DMDL processor plug-ins");
        OPT_PLUGIN.setArgName("plugin-1.jar" + File.pathSeparatorChar + "plugin-2.jar");
        OPT_PLUGIN.setValueSeparator(File.pathSeparatorChar);
        OPT_PLUGIN.setRequired(false);

        OPTIONS = new Options();
        OPTIONS.addOption(OPT_OUTPUT);
        OPTIONS.addOption(OPT_PACKAGE);
        OPTIONS.addOption(OPT_SOURCE_ENCODING);
        OPTIONS.addOption(OPT_TARGET_ENCODING);
        OPTIONS.addOption(OPT_SOURCE_PATH);
        OPTIONS.addOption(OPT_PLUGIN);
    }

    /**
     * Program entry.
     * @param args program arguments
     */
    public static void main(String... args) {
        try {
            Configuration conf = configure(args);
            GenerateTask task = new GenerateTask(conf);
            task.process();
        } catch (Exception e) {
            e.printStackTrace(System.out);
            HelpFormatter formatter = new HelpFormatter();
            formatter.setWidth(Integer.MAX_VALUE);
            formatter.printHelp(
                    MessageFormat.format(
                            "java -classpath ... {0}",
                            Main.class.getName()),
                    OPTIONS,
                    true);
            System.exit(1);
        }
    }

    private static Configuration configure(String[] args) throws ParseException {
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(OPTIONS, args);
        String output = cmd.getOptionValue(OPT_OUTPUT.getOpt());
        String packageName = cmd.getOptionValue(OPT_PACKAGE.getOpt());
        Charset sourceEnc = consumeCharset(cmd, OPT_SOURCE_ENCODING);
        Charset targetEnc = consumeCharset(cmd, OPT_TARGET_ENCODING);
        String[] sourcePaths = cmd.getOptionValues(OPT_SOURCE_PATH.getOpt());
        String[] plugin = cmd.getOptionValues(OPT_PLUGIN.getOpt());

        File outputDirectory = new File(output);
        List<DmdlSourceRepository> sources = new ArrayList<DmdlSourceRepository>();
        if (sourcePaths != null) {
            for (String s : sourcePaths) {
                File file = new File(s);
                if (file.isFile()) {
                    sources.add(new DmdlSourceFile(Collections.singletonList(file), sourceEnc));
                } else if (file.isDirectory()) {
                    sources.add(new DmdlSourceDirectory(
                            file,
                            sourceEnc,
                            Pattern.compile(".*\\.dmdl"),
                            Pattern.compile("^\\..*")));
                } else {
                    LOG.warn("{}がありません", file);
                }
            }
        }
        ClassLoader serviceLoader = loadPlugins(plugin);

        ModelFactory factory = Models.getModelFactory();
        return new Configuration(
                factory,
                new CompositeSourceRepository(sources),
                Models.toName(factory, packageName),
                new Filer(outputDirectory, targetEnc),
                serviceLoader,
                Locale.getDefault());
    }

    private static Charset consumeCharset(CommandLine cmd, Option option) {
        assert cmd != null;
        assert option != null;
        String charset = cmd.getOptionValue(option.getOpt());
        if (charset == null) {
            return Charset.defaultCharset();
        }
        return Charset.forName(charset);
    }

    private static ClassLoader loadPlugins(String[] plugin) {
        final List<URL> pluginLocations = new ArrayList<URL>();
        if (plugin != null) {
            for (String s : plugin) {
                try {
                    File file = new File(s);
                    if (file.exists() == false) {
                        throw new FileNotFoundException(file.getAbsolutePath());
                    }
                    URL url = file.toURI().toURL();
                    pluginLocations.add(url);
                } catch (IOException e) {
                    LOG.warn(MessageFormat.format(
                            "Failed to load plugin \"{0}\"",
                            s),
                            e);
                }
            }
        }
        ClassLoader serviceLoader = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            @Override
            public ClassLoader run() {
                URLClassLoader loader = new URLClassLoader(
                        pluginLocations.toArray(new URL[pluginLocations.size()]),
                        Main.class.getClassLoader());
                return loader;
            }
        });
        return serviceLoader;
    }
}
