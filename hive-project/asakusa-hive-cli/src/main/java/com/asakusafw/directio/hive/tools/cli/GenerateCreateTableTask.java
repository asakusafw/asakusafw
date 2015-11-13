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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.directio.hive.common.HiveTableInfo;
import com.asakusafw.directio.hive.ql.HiveCreateTable;
import com.asakusafw.directio.hive.ql.HiveQlEmitter;
import com.asakusafw.directio.hive.tools.cli.ClassCollector.Selector;

/**
 * Generate HiveQL for {@link HiveCreateTable}.
 * @since 0.7.0
 */
public class GenerateCreateTableTask {

    static final Charset ENCODING = Charset.forName("UTF-8"); //$NON-NLS-1$

    static final Logger LOG = LoggerFactory.getLogger(GenerateCreateTableTask.class);

    private static final String STATEMENT_SEPARATOR = ";\n"; //$NON-NLS-1$

    /**
     * Performs this task.
     * @param configuration the task configuration
     * @throws IOException if failed by I/O error
     */
    public void perform(Configuration configuration) throws IOException {
        ClassCollector collector = collect(configuration);
        for (File file : configuration.sources) {
            LOG.info(MessageFormat.format(
                    Messages.getString("GenerateCreateTableTask.infoInspectClassPath"), //$NON-NLS-1$
                    file));
            collector.inspect(file);
        }
        int count = 0;
        Writer writer = open(configuration);
        try {
            for (Class<?> aClass : collector.getClasses()) {
                HiveTableInfo table = newTableInfo(aClass);
                if (table == null) {
                    continue;
                }
                LOG.info(MessageFormat.format(
                        Messages.getString("GenerateCreateTableTask.infoStartGenerateDdl"), //$NON-NLS-1$
                        table.getTableName()));
                HiveQlEmitter.emit(new Ql(table, configuration), writer);
                writer.write(STATEMENT_SEPARATOR);
                count++;
            }
        } finally {
            writer.close();
        }
        LOG.info(MessageFormat.format(
                Messages.getString("GenerateCreateTableTask.infoFinishGenerateDdl"), //$NON-NLS-1$
                count,
                configuration.output));
    }

    private Writer open(Configuration configuration) throws IOException {
        File file = configuration.output;
        File parent = file.getParentFile();
        if (parent.mkdirs() == false && parent.isDirectory() == false) {
            throw new IOException(MessageFormat.format(
                    Messages.getString("GenerateCreateTableTask.errorFailedToCreateOutputDirectory"), //$NON-NLS-1$
                    file));
        }
        return new OutputStreamWriter(new FileOutputStream(file), ENCODING);
    }

    private ClassCollector collect(Configuration configuration) {
        final Pattern pattern = configuration.acceptTableNames;
        ClassCollector collector = new ClassCollector(configuration.classLoader, new Selector() {
            @Override
            public boolean accept(Class<?> aClass) {
                if (HiveTableInfo.class.isAssignableFrom(aClass) == false) {
                    return false;
                }
                if (Modifier.isAbstract(aClass.getModifiers())) {
                    return false;
                }
                if (pattern != null) {
                    HiveTableInfo info = newTableInfo(aClass);
                    if (info == null) {
                        return false;
                    }
                    if (pattern.matcher(info.getTableName()).matches() == false) {
                        LOG.debug("Filtered table: {}", info.getTableName()); //$NON-NLS-1$
                        return false;
                    }
                }
                return true;
            }
        });
        return collector;
    }

    static HiveTableInfo newTableInfo(Class<?> aClass) {
        try {
            return aClass.asSubclass(HiveTableInfo.class).newInstance();
        } catch (Exception e) {
            LOG.warn(MessageFormat.format(
                    Messages.getString("GenerateCreateTableTask.warnFailedToInstantiate"), //$NON-NLS-1$
                    aClass.getName()), e);
            return null;
        }
    }

    /**
     * Configuration for {@link GenerateCreateTableTask}.
     * @since 0.7.0
     */
    public static final class Configuration {

        final ClassLoader classLoader;

        final List<File> sources;

        final Pattern acceptTableNames;

        final Stringnizer locationProvider;

        final String databaseName;

        final File output;

        /**
         * Creates a new instance.
         * @param classLoader the target class loader
         * @param sources the class path (nullable)
         * @param acceptTableNames target table name pattern (nullable)
         * @param locationProvider provides table location (nullable)
         * @param databaseName the database name (nullable)
         * @param output the output path
         */
        public Configuration(
                ClassLoader classLoader,
                List<File> sources,
                Pattern acceptTableNames,
                Stringnizer locationProvider,
                String databaseName,
                File output) {
            this.classLoader = classLoader;
            this.sources = sources;
            this.acceptTableNames = acceptTableNames;
            this.locationProvider = locationProvider;
            this.databaseName = databaseName;
            this.output = output;
        }
    }

    private static final class Ql implements HiveCreateTable {

        private final HiveTableInfo table;

        private final Configuration configuration;

        public Ql(HiveTableInfo table, Configuration configuration) {
            this.table = table;
            this.configuration = configuration;
        }

        @Override
        public HiveTableInfo getTableInfo() {
            return table;
        }

        @Override
        public boolean isExternal() {
            return true;
        }

        @Override
        public boolean isSkipPresentTable() {
            return true;
        }

        @Override
        public String getDatabaseName() {
            return configuration.databaseName;
        }

        @Override
        public String getLocation() {
            if (configuration.locationProvider == null) {
                return null;
            }
            return configuration.locationProvider.toString(table);
        }
    }
}
