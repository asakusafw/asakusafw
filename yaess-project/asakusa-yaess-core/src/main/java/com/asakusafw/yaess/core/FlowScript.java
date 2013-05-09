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
package com.asakusafw.yaess.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.yaess.core.util.PropertiesUtil;

/**
 * A script describes each jobflow structure.
 * @since 0.2.3
 */
public final class FlowScript {

    static final Logger LOG = LoggerFactory.getLogger(FlowScript.class);

    /**
     * A configuration key prefix of each flow.
     */
    public static final String KEY_FLOW_PREFIX = "flow.";

    /**
     * A configuration key name of IDs.
     */
    public static final String KEY_ID = "id";

    /**
     * A configuration key name of blockers' ID.
     */
    public static final String KEY_BLOCKERS = "blockerIds";

    /**
     * A configuration key name of {@link ExecutionScript#getKind() script kind}.
     */
    public static final String KEY_KIND = "kind";

    /**
     * A configuration key name of {@link HadoopScript#getClassName() class name}.
     */
    public static final String KEY_CLASS_NAME = "class";

    /**
     * A configuration key name of {@link CommandScript#getProfileName() profile name}.
     */
    public static final String KEY_PROFILE = "profile";

    /**
     * A configuration key name of {@link CommandScript#getModuleName() module name}.
     */
    public static final String KEY_MODULE = "module";

    /**
     * The configuration key prefix of {@link ExecutionScript#getEnvironmentVariables() environment variables}.
     */
    public static final String KEY_ENV_PREFIX = "env.";

    /**
     * The configuration key prefix of {@link CommandScript#getCommandLineTokens() command line tokens}.
     * This must follows each command token with lexicographic order.
     */
    private static final String KEY_COMMAND_PREFIX = "command.";

    /**
     * The configuration key prefix of {@link HadoopScript#getHadoopProperties() extra Hadoop properties}.
     */
    private static final String KEY_PROP_PREFIX = "prop.";

    private static final Comparator<ExecutionScript> SCRIPT_COMPARATOR = new Comparator<ExecutionScript>() {
        @Override
        public int compare(ExecutionScript o1, ExecutionScript o2) {
            return o1.getId().compareTo(o2.getId());
        }
    };

    private final String id;

    private final Set<String> blockerIds;

    private final Map<ExecutionPhase, Set<ExecutionScript>> scripts;

    /**
     * Creates a new instance.
     * @param id the flow ID
     * @param blockerIds the predecessors' flow ID
     * @param scripts the execution scripts for each {@link ExecutionPhase}
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public FlowScript(
            String id,
            Set<String> blockerIds,
            Map<ExecutionPhase, ? extends Collection<? extends ExecutionScript>> scripts) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null"); //$NON-NLS-1$
        }
        if (id.indexOf('.') >= 0) {
            throw new IllegalArgumentException("id must not contain dot"); //$NON-NLS-1$
        }
        if (blockerIds == null) {
            throw new IllegalArgumentException("blockerIds must not be null"); //$NON-NLS-1$
        }
        if (scripts == null) {
            throw new IllegalArgumentException("scripts must not be null"); //$NON-NLS-1$
        }
        this.id = id;
        this.blockerIds = Collections.unmodifiableSet(new LinkedHashSet<String>(blockerIds));

        EnumMap<ExecutionPhase, Set<ExecutionScript>> map =
            new EnumMap<ExecutionPhase, Set<ExecutionScript>>(ExecutionPhase.class);
        for (ExecutionPhase phase : ExecutionPhase.values()) {
            if (scripts.containsKey(phase)) {
                TreeSet<ExecutionScript> set = new TreeSet<ExecutionScript>(SCRIPT_COMPARATOR);
                set.addAll(scripts.get(phase));
                if (set.size() != scripts.get(phase).size()) {
                    throw new IllegalArgumentException(MessageFormat.format(
                            "{0}@{1} contains duplicated IDs in scripts",
                            id,
                            phase));
                }
                map.put(phase, Collections.unmodifiableSet(set));
            } else {
                map.put(phase, Collections.<ExecutionScript>emptySet());
            }
        }
        this.scripts = Collections.unmodifiableMap(map);
    }

    /**
     * Returns the ID of this flow.
     * @return the flow ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the ID of this flow execution.
     * @return the ID of this flow execution
     */
    public Set<String> getBlockerIds() {
        return blockerIds;
    }

    /**
     * Returns the execution scripts for each {@link ExecutionPhase}.
     * If some phase has no scripts, then the related entry contains an empty list.
     * @return the execution scripts
     */
    public Map<ExecutionPhase, Set<ExecutionScript>> getScripts() {
        return scripts;
    }

    private static String getPrefix(String flowId) {
        assert flowId != null;
        return KEY_FLOW_PREFIX + flowId + '.';
    }

    private static String getPrefix(String flowId, ExecutionPhase phase) {
        assert flowId != null;
        assert phase != null;
        return getPrefix(flowId) + phase.getSymbol() + '.';
    }

    private static String getPrefix(String flowId, ExecutionPhase phase, String nodeId) {
        assert flowId != null;
        assert phase != null;
        assert nodeId != null;
        return getPrefix(flowId, phase) + nodeId + '.';
    }

    /**
     * Loads a {@link FlowScript} with the specified ID.
     * @param properties source properties
     * @param flowId the target flow ID
     * @return the loaded script
     * @throws IllegalArgumentException if script is invalid, or some parameters were {@code null}
     * @see #extractFlowIds(Properties)
     */
    public static FlowScript load(Properties properties, String flowId) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        if (flowId == null) {
            throw new IllegalArgumentException("flowId must not be null"); //$NON-NLS-1$
        }

        String prefix = getPrefix(flowId);
        LOG.debug("Loading execution scripts: {}*", prefix);

        NavigableMap<String, String> flowMap = PropertiesUtil.createPrefixMap(properties, prefix);
        String blockersString = extract(flowMap, prefix, KEY_BLOCKERS);
        Set<String> blockerIds = parseTokens(blockersString);
        EnumMap<ExecutionPhase, List<ExecutionScript>> scripts =
            new EnumMap<ExecutionPhase, List<ExecutionScript>>(ExecutionPhase.class);
        for (ExecutionPhase phase : ExecutionPhase.values()) {
            scripts.put(phase, Collections.<ExecutionScript>emptyList());
        }
        int count = 0;
        Map<String, NavigableMap<String, String>> phaseMap = partitioning(flowMap);
        for (Map.Entry<String, NavigableMap<String, String>> entry : phaseMap.entrySet()) {
            String phaseSymbol = entry.getKey();
            NavigableMap<String, String> phaseContents = entry.getValue();
            ExecutionPhase phase = ExecutionPhase.findFromSymbol(phaseSymbol);
            if (phase == null) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "Unknown phase in \"{0}\": {1}",
                        flowId,
                        phaseSymbol));
            }
            List<ExecutionScript> scriptsInPhase = loadScripts(flowId, phase, phaseContents);
            scripts.put(phase, scriptsInPhase);
            count += scriptsInPhase.size();
        }
        FlowScript script = new FlowScript(flowId, blockerIds, scripts);
        LOG.debug("Loaded {} execution scripts: {}*", count, prefix);
        LOG.trace("Loaded {}*: {}", prefix, script);
        return script;
    }

    /**
     * Loads a {@link ExecutionScript}s in the specified flow and phase.
     * If the target phase is empty in the specified flow, this returns an empty list.
     * Note that this method will raise an exception if the specified flow does not exist.
     * @param properties source properties
     * @param flowId the target flow ID
     * @param phase the target phase
     * @return the loaded execution scripts
     * @throws IllegalArgumentException if script is invalid, or some parameters were {@code null}
     * @see #extractFlowIds(Properties)
     */
    public static Set<ExecutionScript> load(Properties properties, String flowId, ExecutionPhase phase) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        if (flowId == null) {
            throw new IllegalArgumentException("flowId must not be null"); //$NON-NLS-1$
        }
        if (phase == null) {
            throw new IllegalArgumentException("phase must not be null"); //$NON-NLS-1$
        }
        String prefix = getPrefix(flowId, phase);
        LOG.debug("Loading execution scripts: {}*", prefix);
        Set<String> availableFlowIds = extractFlowIds(properties);
        if (availableFlowIds.contains(flowId) == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Flow \"{0}\" does not exist",
                    flowId));
        }
        NavigableMap<String, String> contents = PropertiesUtil.createPrefixMap(properties, prefix);
        List<ExecutionScript> scripts = loadScripts(flowId, phase, contents);
        LOG.debug("Loaded {} execution scripts: {}*", scripts.size(), prefix);
        LOG.trace("Loaded {}*: {}", prefix, scripts);
        TreeSet<ExecutionScript> results = new TreeSet<ExecutionScript>(SCRIPT_COMPARATOR);
        results.addAll(scripts);
        return results;
    }

    private static List<ExecutionScript> loadScripts(
            String flowId,
            ExecutionPhase phase,
            NavigableMap<String, String> contents) {
        assert flowId != null;
        assert phase != null;
        assert contents != null;
        if (contents.isEmpty()) {
            return Collections.emptyList();
        }
        List<ExecutionScript> results = new ArrayList<ExecutionScript>();
        Map<String, NavigableMap<String, String>> scripts = partitioning(contents);
        for (Map.Entry<String, NavigableMap<String, String>> entry : scripts.entrySet()) {
            String scriptId = entry.getKey();
            NavigableMap<String, String> scriptContents = entry.getValue();
            ExecutionScript script = loadScript(flowId, phase, scriptId, scriptContents);
            results.add(script);
        }
        checkBlockers(flowId, phase, results);
        return results;
    }

    private static void checkBlockers(
            String flowId,
            ExecutionPhase phase,
            List<ExecutionScript> scripts) {
        assert flowId != null;
        assert phase != null;
        assert scripts != null;
        // TODO check dependencies
    }

    private static ExecutionScript loadScript(
            String flowId,
            ExecutionPhase phase,
            String nodeId,
            Map<String, String> contents) {
        assert flowId != null;
        assert phase != null;
        assert nodeId != null;
        assert contents != null;
        String prefix = getPrefix(flowId, phase, nodeId);
        String scriptId = extract(contents, prefix, KEY_ID);
        String kindSymbol = extract(contents, prefix, KEY_KIND);
        ExecutionScript.Kind kind = ExecutionScript.Kind.findFromSymbol(kindSymbol);
        String blockersString = extract(contents, prefix, KEY_BLOCKERS);
        Set<String> blockers = parseTokens(blockersString);
        Map<String, String> environmentVariables = PropertiesUtil.createPrefixMap(contents, KEY_ENV_PREFIX);
        ExecutionScript script;
        if (kind == ExecutionScript.Kind.COMMAND) {
            String profileName = extract(contents, prefix, KEY_PROFILE);
            String moduleName = extract(contents, prefix, KEY_MODULE);
            NavigableMap<String, String> commandMap = PropertiesUtil.createPrefixMap(contents, KEY_COMMAND_PREFIX);
            if (commandMap.isEmpty()) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "\"{0}*\" is not defined",
                        prefix + KEY_COMMAND_PREFIX));
            }
            List<String> command = new ArrayList<String>(commandMap.values());
            script = new CommandScript(
                    scriptId,
                    blockers,
                    profileName,
                    moduleName,
                    command,
                    environmentVariables);
        } else if (kind == ExecutionScript.Kind.HADOOP) {
            String className = extract(contents, prefix, KEY_CLASS_NAME);
            Map<String, String> properties = PropertiesUtil.createPrefixMap(contents, KEY_PROP_PREFIX);
            script = new HadoopScript(scriptId, blockers, className, properties, environmentVariables);
        } else {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Unsupported kind in \"{0}\": {1}",
                    prefix + KEY_KIND,
                    kindSymbol));
        }
        LOG.trace("Loaded script {}* -> {}", script);
        return script;
    }

    private static String extract(Map<String, String> contents, String prefix, String key) {
        assert contents != null;
        assert prefix != null;
        assert key != null;
        String kindSymbol = contents.remove(key);
        if (kindSymbol == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "\"{0}\" is not defined",
                    prefix + key));
        }
        return kindSymbol;
    }

    private static Map<String, NavigableMap<String, String>> partitioning(NavigableMap<String, String> map) {
        assert map != null;
        Map<String, NavigableMap<String, String>> results = new TreeMap<String, NavigableMap<String, String>>();
        while (map.isEmpty() == false) {
            String name = map.firstKey();
            int index = name.indexOf('.');
            if (index >= 0) {
                name = name.substring(0, index);
            }
            String first = name + '.';
            String last = name + (char) ('.' + 1);
            NavigableMap<String, String> partition = new TreeMap<String, String>();
            for (Map.Entry<String, String> entry : map.subMap(first, last).entrySet()) {
                String key = entry.getKey();
                partition.put(key.substring(name.length() + 1), entry.getValue());
            }
            results.put(name, partition);
            map.remove(name);
            map.subMap(first, last).clear();
        }
        return results;
    }

    /**
     * Returns all flow IDs defined in the properties.
     * @param properties target properties
     * @return all flow IDs
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static Set<String> extractFlowIds(Properties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        LOG.debug("Extracting Flow IDs");
        Set<String> childKeys = PropertiesUtil.getChildKeys(properties, KEY_FLOW_PREFIX, String.valueOf('.'));
        int prefixLength = KEY_FLOW_PREFIX.length();
        Set<String> results = new TreeSet<String>();
        for (String childKey : childKeys) {
            assert childKey.startsWith(KEY_FLOW_PREFIX);
            results.add(childKey.substring(prefixLength));
        }
        LOG.debug("Extracted Flow IDs: {}", results);
        return results;
    }

    /**
     * Stores this script into the specified object.
     * @param properties target properties
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void storeTo(Properties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        properties.setProperty(getPrefix(getId()) + KEY_BLOCKERS, join(getBlockerIds()));
        for (Map.Entry<ExecutionPhase, Set<ExecutionScript>> phase : getScripts().entrySet()) {
            int index = 0;
            for (ExecutionScript script : phase.getValue()) {
                String scriptPrefix = getPrefix(getId(), phase.getKey(), String.format("%04d", index++));
                properties.setProperty(scriptPrefix + KEY_ID, script.getId());
                properties.setProperty(scriptPrefix + KEY_KIND, script.getKind().getSymbol());
                properties.setProperty(scriptPrefix + KEY_BLOCKERS, join(script.getBlockerIds()));
                String envPrefix = scriptPrefix + KEY_ENV_PREFIX;
                for (Map.Entry<String, String> entry : script.getEnvironmentVariables().entrySet()) {
                    properties.setProperty(envPrefix + entry.getKey(), entry.getValue());
                }
                switch (script.getKind()) {
                case COMMAND: {
                    CommandScript s = (CommandScript) script;
                    properties.setProperty(scriptPrefix + KEY_PROFILE, s.getProfileName());
                    properties.setProperty(scriptPrefix + KEY_MODULE, s.getModuleName());
                    List<String> command = s.getCommandLineTokens();
                    assert command.size() <= 9999;
                    String commandPrefix = scriptPrefix + KEY_COMMAND_PREFIX;
                    for (int i = 0, n = command.size(); i < n; i++) {
                        properties.setProperty(String.format("%s%04d", commandPrefix, i), command.get(i));
                    }
                    break;
                }
                case HADOOP: {
                    HadoopScript s = (HadoopScript) script;
                    properties.setProperty(scriptPrefix + KEY_CLASS_NAME, s.getClassName());
                    String propPrefix = scriptPrefix + KEY_PROP_PREFIX;
                    for (Map.Entry<String, String> entry : s.getHadoopProperties().entrySet()) {
                        properties.setProperty(propPrefix + entry.getKey(), entry.getValue());
                    }
                    break;
                }
                default:
                    throw new AssertionError(script.getKind());
                }
            }
        }
    }

    private static Set<String> parseTokens(String tokens) {
        assert tokens != null;
        String trimmed = tokens.trim();
        if (trimmed.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> results = new LinkedHashSet<String>();
        for (String token : trimmed.split("\\s*,\\s*")) {
            if (token.isEmpty()) {
                continue;
            }
            results.add(token);
        }
        return results;
    }

    private String join(Set<String> tokens) {
        assert tokens != null;
        if (tokens.isEmpty()) {
            return "";
        }
        StringBuilder buf = new StringBuilder();
        Iterator<String> iter = tokens.iterator();
        assert iter.hasNext();
        buf.append(iter.next());
        while (iter.hasNext()) {
            buf.append(',');
            buf.append(iter.next());
        }
        return buf.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((blockerIds == null) ? 0 : blockerIds.hashCode());
        result = prime * result + ((scripts == null) ? 0 : scripts.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FlowScript other = (FlowScript) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (blockerIds == null) {
            if (other.blockerIds != null) {
                return false;
            }
        } else if (!blockerIds.equals(other.blockerIds)) {
            return false;
        }
        if (scripts == null) {
            if (other.scripts != null) {
                return false;
            }
        } else if (!scripts.equals(other.scripts)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "Flow'{'id={0}, blockers={1}, scripts={2}'}'",
                getId(),
                getBlockerIds(),
                getScripts());
    }
}
