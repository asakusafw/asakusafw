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
package com.asakusafw.yaess.core;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import com.asakusafw.yaess.core.util.PropertiesUtil;

/**
 * Profile services for YAESS.
 * @since 0.2.3
 */
public class YaessProfile {

    /**
     * The qualifier delimiter for each property keys.
     */
    public static final char QUALIFIER = '.';

    /**
     * The key prefix of core profile.
     * This profile must describes {@link CoreProfile} service.
     */
    public static final String PREFIX_CORE = "core";

    /**
     * The key prefix of monitor profile.
     * This profile must describes {@link ExecutionMonitorProvider} service.
     */
    public static final String PREFIX_MONITOR = "monitor";

    /**
     * The key prefix of lock provider profile.
     * This profile must describes {@link ExecutionLockProvider} service.
     */
    public static final String PREFIX_LOCK = "lock";

    /**
     * The key prefix of job scheduler profile.
     * This profile must describes {@link JobScheduler} service.
     */
    public static final String PREFIX_SCHEDULER = "scheduler";

    /**
     * The key prefix of Hadoop script handler profile.
     * This profile must describes {@link HadoopScriptHandler} service.
     */
    public static final String PREFIX_HADOOP = "hadoop";

    /**
     * The key prefix of command script handlers profile.
     * Each command script handler must be use {@code "command.<profile-name>"} as its prefix.
     * This profile must describes {@link CommandScriptHandler} service.
     */
    public static final String PREFIX_COMMAND = "command";

    private static final String GROUP_PREFIX_COMMAND = PREFIX_COMMAND + QUALIFIER;

    private final ServiceProfile<CoreProfile> core;

    private final ServiceProfile<ExecutionMonitorProvider> monitors;

    private final ServiceProfile<ExecutionLockProvider> locks;

    private final ServiceProfile<JobScheduler> scheduler;

    private final ServiceProfile<HadoopScriptHandler> hadoopHandler;

    private final Map<String, ServiceProfile<CommandScriptHandler>> commandHandlers;

    /**
     * Creates a new instance.
     * @param core core profile
     * @param monitors monitor profile
     * @param locks lock profile
     * @param scheduler scheduler profile
     * @param hadoopHandler hadoop handler profile
     * @param commandHandlers command handler profiles
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public YaessProfile(
            ServiceProfile<CoreProfile> core,
            ServiceProfile<ExecutionMonitorProvider> monitors,
            ServiceProfile<ExecutionLockProvider> locks,
            ServiceProfile<JobScheduler> scheduler,
            ServiceProfile<HadoopScriptHandler> hadoopHandler,
            Collection<ServiceProfile<CommandScriptHandler>> commandHandlers) {
        if (core == null) {
            throw new IllegalArgumentException("core must not be null"); //$NON-NLS-1$
        }
        if (monitors == null) {
            throw new IllegalArgumentException("monitors must not be null"); //$NON-NLS-1$
        }
        if (locks == null) {
            throw new IllegalArgumentException("locks must not be null"); //$NON-NLS-1$
        }
        if (scheduler == null) {
            throw new IllegalArgumentException("scheduler must not be null"); //$NON-NLS-1$
        }
        if (hadoopHandler == null) {
            throw new IllegalArgumentException("hadoopHandler must not be null"); //$NON-NLS-1$
        }
        if (commandHandlers == null) {
            throw new IllegalArgumentException("commandHandlers must not be null"); //$NON-NLS-1$
        }
        checkPrefix(core, PREFIX_CORE);
        checkPrefix(monitors, PREFIX_MONITOR);
        checkPrefix(locks, PREFIX_LOCK);
        checkPrefix(scheduler, PREFIX_SCHEDULER);
        checkPrefix(hadoopHandler, PREFIX_HADOOP);
        this.core = core;
        this.monitors = monitors;
        this.locks = locks;
        this.scheduler = scheduler;
        this.hadoopHandler = hadoopHandler;
        Map<String, ServiceProfile<CommandScriptHandler>> map =
            new TreeMap<String, ServiceProfile<CommandScriptHandler>>();
        for (ServiceProfile<CommandScriptHandler> profile : commandHandlers) {
            if (profile.getPrefix().startsWith(GROUP_PREFIX_COMMAND) == false) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "Profile \"{1}\" is invalid, this must be \"{0}<profile-name>\" ({2})",
                        GROUP_PREFIX_COMMAND,
                        profile.getPrefix(),
                        profile.getServiceClass().getName()));
            }
            String profileName = profile.getPrefix().substring(GROUP_PREFIX_COMMAND.length());
            if (map.containsKey(profileName)) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "Profile \"{0}\" is duplicated ({1} <=> {2})",
                        profileName,
                        profile.getServiceClass().getName(),
                        map.get(profileName).getServiceClass().getName()));
            }
            map.put(profileName, profile);
        }
        this.commandHandlers = Collections.unmodifiableMap(map);
    }

    private void checkPrefix(ServiceProfile<?> profile, String prefix) {
        assert profile != null;
        assert prefix != null;
        if (profile.getPrefix().equals(prefix) == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Profile \"{1}\" is invalid, this must be \"{0}\" ({2})",
                    prefix,
                    profile.getPrefix(),
                    profile.getServiceClass().getName()));
        }
    }

    /**
     * Loads a YAESS profile from the specified properties.
     * @param properties source properties
     * @param classLoader the class loader to load the service class
     * @return the loaded profile
     * @throws IllegalArgumentException if the target profile is invalid, or parameters contain {@code null}
     * @deprecated use {@link #load(Properties, ProfileContext)} instead
     */
    @Deprecated
    public static YaessProfile load(Properties properties, ClassLoader classLoader) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        if (classLoader == null) {
            throw new IllegalArgumentException("classLoader must not be null"); //$NON-NLS-1$
        }
        return load(properties, ProfileContext.system(classLoader));
    }

    /**
     * Loads a YAESS profile from the specified properties.
     * @param properties source properties
     * @param context the current profile context
     * @return the loaded profile
     * @throws IllegalArgumentException if the target profile is invalid, or parameters contain {@code null}
     * @since 0.2.4
     */
    public static YaessProfile load(Properties properties, ProfileContext context) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must not be null"); //$NON-NLS-1$
        }
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        ServiceProfile<CoreProfile> core =
            ServiceProfile.load(properties, PREFIX_CORE, CoreProfile.class, context);
        ServiceProfile<ExecutionMonitorProvider> monitors =
            ServiceProfile.load(properties, PREFIX_MONITOR, ExecutionMonitorProvider.class, context);
        ServiceProfile<ExecutionLockProvider> locks =
            ServiceProfile.load(properties, PREFIX_LOCK, ExecutionLockProvider.class, context);
        ServiceProfile<JobScheduler> scheduler =
            ServiceProfile.load(properties, PREFIX_SCHEDULER, JobScheduler.class, context);
        ServiceProfile<HadoopScriptHandler> hadoopHandler =
            ServiceProfile.load(properties, PREFIX_HADOOP, HadoopScriptHandler.class, context);
        List<ServiceProfile<CommandScriptHandler>> commandHandlers =
            new ArrayList<ServiceProfile<CommandScriptHandler>>();
        for (String commandHandlerPrefix : PropertiesUtil.getChildKeys(properties, GROUP_PREFIX_COMMAND, ".")) {
            ServiceProfile<CommandScriptHandler> profile =
                ServiceProfile.load(properties, commandHandlerPrefix, CommandScriptHandler.class, context);
            commandHandlers.add(profile);
        }
        return new YaessProfile(core, monitors, locks, scheduler, hadoopHandler, commandHandlers);
    }

    /**
     * Returns a core profile.
     * @return core profile
     */
    public ServiceProfile<CoreProfile> getCore() {
        return core;
    }

    /**
     * Returns a monitor provider.
     * @return monitor provider
     */
    public ServiceProfile<ExecutionMonitorProvider> getMonitors() {
        return monitors;
    }

    /**
     * Returns a lock provider.
     * @return lock provider
     */
    public ServiceProfile<ExecutionLockProvider> getLocks() {
        return locks;
    }

    /**
     * Returns a job scheduler.
     * @return the scheduler
     */
    public ServiceProfile<JobScheduler> getScheduler() {
        return scheduler;
    }

    /**
     * Returns a Hadoop script handler.
     * @return Hadoop script handler
     */
    public ServiceProfile<HadoopScriptHandler> getHadoopHandler() {
        return hadoopHandler;
    }

    /**
     * Returns command script handlers for each target profile name.
     * @return the command script handlers
     */
    public Map<String, ServiceProfile<CommandScriptHandler>> getCommandHandlers() {
        return commandHandlers;
    }
}