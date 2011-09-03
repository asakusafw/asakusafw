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
package com.asakusafw.windgate.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.windgate.core.util.PropertiesUtil;

/**
 * A gate script.
 * @since 0.2.3
 */
public class GateScript {

    static final WindGateLogger WGLOG = new WindGateCoreLogger(GateScript.class);

    static final Logger LOG = LoggerFactory.getLogger(GateScript.class);

    static final char QUALIFIER = '.';

    private final String name;

    private final List<ProcessScript<?>> processes;

    /**
     * Creates a new instance.
     * @param name the name of this script (for hint)
     * @param processes the member processes
     * @throws IllegalArgumentException if any parameter is {@code null}
     */
    public GateScript(String name, List<? extends ProcessScript<?>> processes) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (processes == null) {
            throw new IllegalArgumentException("processes must not be null"); //$NON-NLS-1$
        }
        this.name = name;
        this.processes = Collections.unmodifiableList(new ArrayList<ProcessScript<?>>(processes));
    }

    /**
     * Return the name of this script (for hint).
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the member processes.
     * @return the processes
     */
    public List<ProcessScript<?>> getProcesses() {
        return processes;
    }

    /**
     * Loads a gate script from the properties.
     * @param name the name of script (for hint)
     * @param properties source properties
     * @param loader class loader to load the data model classes
     * @return the loaded script
     * @throws IllegalArgumentException if properties are invalid, or if any parameter is {@code null}
     */
    public static GateScript loadFrom(String name, Properties properties, ClassLoader loader) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        if (loader == null) {
            throw new IllegalArgumentException("loader must not be null"); //$NON-NLS-1$
        }
        LOG.debug("Restoring WindGate script");
        Map<String, Map<String, String>> partitions = partitioning(properties);
        List<ProcessScript<?>> processes = new ArrayList<ProcessScript<?>>();
        for (Map.Entry<String, Map<String, String>> entry : partitions.entrySet()) {
            LOG.debug("Restoring WindGate process: {}", entry.getKey());
            ProcessScript<?> process = loadProcess(entry.getKey(), entry.getValue(), loader);
            processes.add(process);
        }
        return new GateScript(name, processes);
    }

    private static Map<String, Map<String, String>> partitioning(Properties properties) {
        assert properties != null;
        Map<String, Map<String, String>> results = new HashMap<String, Map<String, String>>();
        for (Map.Entry<?, ?> entry : properties.entrySet()) {
            if ((entry.getKey() instanceof String) == false || (entry.getValue() instanceof String) == false) {
                continue;
            }
            String key = (String) entry.getKey();
            int index = key.indexOf(QUALIFIER);
            if (index < 0) {
                WGLOG.error("E03001",
                        key,
                        entry.getValue());
                throw new IllegalArgumentException(MessageFormat.format(
                        "Gate script includes invalid property key: \"{0}\"",
                        key));
            }
            String name = key.substring(0, index);
            Map<String, String> partition = results.get(name);
            if (partition == null) {
                partition = new TreeMap<String, String>();
                results.put(name, partition);
            }
            partition.put(key.substring(index + 1), (String) entry.getValue());
        }
        return results;
    }

    private static ProcessScript<?> loadProcess(String name, Map<String, String> conf, ClassLoader loader) {
        assert name != null;
        assert conf != null;
        assert loader != null;
        String processType = consume(conf, name, ProcessScript.KEY_PROCESS_TYPE);
        String dataClassName = consume(conf, name, ProcessScript.KEY_DATA_CLASS);
        DriverScript sourceScript = loadDriver(name, DriverScript.Kind.SOURCE, conf);
        DriverScript drainScript = loadDriver(name, DriverScript.Kind.DRAIN, conf);

        LOG.debug("Loading data model class: {}",
                dataClassName);
        Class<?> dataClass;
        try {
            dataClass = loader.loadClass(dataClassName);
        } catch (ClassNotFoundException e) {
            WGLOG.error(e, "E03002",
                    name,
                    dataClassName);
            throw new IllegalArgumentException(MessageFormat.format(
                    "Failed to load \"{0}\" ({1}.{2})",
                    dataClassName,
                    name,
                    ProcessScript.KEY_DATA_CLASS));
        }

        if (conf.isEmpty() == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Unknown key(s) in gate script ({0}.{1})",
                    name,
                    conf.keySet()));
        }
        return createProcess(name, processType, dataClass, sourceScript, drainScript);
    }

    private static String consume(Map<String, String> proccessConf, String name, String key) {
        assert proccessConf != null;
        assert name != null;
        assert key != null;
        String value = proccessConf.remove(key);
        if (value == null) {
            WGLOG.error("E03001",
                    name + QUALIFIER + key,
                    "(empty)");
            throw new IllegalArgumentException(MessageFormat.format(
                    "\"{0}.{1}\" is not specified",
                    name,
                    key));
        }
        return value;
    }

    private static DriverScript loadDriver(String name, DriverScript.Kind kind, Map<String, String> conf) {
        assert name != null;
        assert kind != null;
        assert conf != null;
        String resourceName = consume(conf, name, kind.prefix);
        Map<String, String> driverConf = new HashMap<String, String>();
        String prefix = kind.prefix + QUALIFIER;
        for (Iterator<Map.Entry<String, String>> iter = conf.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<String, String> entry = iter.next();
            String key = entry.getKey();
            if (key.startsWith(prefix)) {
                driverConf.put(key.substring(prefix.length()), entry.getValue());
                iter.remove();
            }
        }
        return new DriverScript(resourceName, driverConf);
    }

    private static <T> ProcessScript<T> createProcess(
            String name,
            String processType,
            Class<T> dataClass,
            DriverScript sourceScript,
            DriverScript drainScript) {
        return new ProcessScript<T>(name, processType, dataClass, sourceScript, drainScript);
    }

    /**
     * Stores this script into the specified properties.
     * @param properties target properties object
     * @throws IllegalArgumentException if target properties already contains keys about this script,
     *     or if any parameter is {@code null}
     */
    public void storeTo(Properties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        LOG.debug("Saving WindGate script");
        for (ProcessScript<?> process : processes) {
            PropertiesUtil.checkAbsentKeyPrefix(properties, process.getName() + QUALIFIER);
        }
        for (ProcessScript<?> process : processes) {
            storeProcessTo(properties, process);
        }
    }

    private void storeProcessTo(Properties properties, ProcessScript<?> process) {
        assert properties != null;
        assert process != null;
        String prefix = process.getName() + QUALIFIER;
        properties.setProperty(prefix + ProcessScript.KEY_DATA_CLASS, process.getDataClass().getName());
        properties.setProperty(prefix + ProcessScript.KEY_PROCESS_TYPE, process.getProcessType());
        storeDriverTo(
                properties,
                process,
                DriverScript.Kind.SOURCE,
                process.getSourceScript());
        storeDriverTo(
                properties,
                process,
                DriverScript.Kind.DRAIN,
                process.getDrainScript());
    }

    private void storeDriverTo(
            Properties properties,
            ProcessScript<?> process,
            DriverScript.Kind kind,
            DriverScript driver) {
        assert properties != null;
        assert process != null;
        assert kind != null;
        assert driver != null;
        properties.setProperty(process.getName() + QUALIFIER + kind.prefix, driver.getResourceName());
        String prefix = process.getName() + QUALIFIER + kind.prefix + QUALIFIER;
        for (Map.Entry<String, String> entry : driver.getConfiguration().entrySet()) {
            properties.put(prefix + entry.getKey(), entry.getValue());
        }
    }
}
