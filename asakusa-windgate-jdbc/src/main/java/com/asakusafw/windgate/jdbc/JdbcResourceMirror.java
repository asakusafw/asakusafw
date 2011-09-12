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
package com.asakusafw.windgate.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.windgate.core.DriverScript;
import com.asakusafw.windgate.core.GateScript;
import com.asakusafw.windgate.core.ParameterList;
import com.asakusafw.windgate.core.ProcessScript;
import com.asakusafw.windgate.core.WindGateLogger;
import com.asakusafw.windgate.core.resource.DrainDriver;
import com.asakusafw.windgate.core.resource.ResourceMirror;
import com.asakusafw.windgate.core.resource.SourceDriver;
import com.asakusafw.windgate.core.util.ProcessUtil;

/**
 * An implementation of {@link ResourceMirror} using JDBC.
 * @since 0.2.2
 */
public class JdbcResourceMirror extends ResourceMirror {

    static final WindGateLogger WGLOG = new JdbcLogger(JdbcResourceMirror.class);

    static final Logger LOG = LoggerFactory.getLogger(JdbcResourceMirror.class);

    private final JdbcProfile profile;

    private final ParameterList arguments;

    /**
     * Creates a new instance.
     * @param profile the profile of this resource
     * @param arguments the runtime arguments
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public JdbcResourceMirror(JdbcProfile profile, ParameterList arguments) {
        if (profile == null) {
            throw new IllegalArgumentException("profile must not be null"); //$NON-NLS-1$
        }
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
        }
        this.profile = profile;
        this.arguments = arguments;
    }

    @Override
    public String getName() {
        return profile.getResourceName();
    }

    @Override
    public void prepare(GateScript script) throws IOException {
        if (script == null) {
            throw new IllegalArgumentException("script must not be null"); //$NON-NLS-1$
        }
        LOG.debug("Preparing JDBC resource: {}",
                getName());
        for (ProcessScript<?> process : script.getProcesses()) {
            if (process.getSourceScript().getResourceName().equals(getName())) {
                JdbcResourceUtil.convert(profile, process, arguments, DriverScript.Kind.SOURCE);
                ProcessUtil.newDataModel(profile.getResourceName(), process);
            }
            if (process.getDrainScript().getResourceName().equals(getName())) {
                JdbcResourceUtil.convert(profile, process, arguments, DriverScript.Kind.DRAIN);
            }
        }
    }

    @Override
    public <T> SourceDriver<T> createSource(ProcessScript<T> script) throws IOException {
        if (script == null) {
            throw new IllegalArgumentException("script must not be null"); //$NON-NLS-1$
        }
        LOG.debug("Creating source driver for resource \"{}\" in process \"{}\"",
                getName(),
                script.getName());
        JdbcScript<T> jdbcScript = JdbcResourceUtil.convert(profile, script, arguments, DriverScript.Kind.SOURCE);
        T object = ProcessUtil.newDataModel(profile.getResourceName(), script);
        WGLOG.info("I02001",
                getName(),
                script.getName());
        Connection connection = profile.openConnection();
        boolean succeed = false;
        try {
            JdbcSourceDriver<T> driver = new JdbcSourceDriver<T>(profile, jdbcScript, connection, object);
            succeed = true;
            return driver;
        } finally {
            if (succeed == false) {
                try {
                    LOG.debug("Disposing source driver for resource \"{}\" in process \"{}\"",
                            getName(),
                            script.getName());
                    connection.close();
                } catch (SQLException e) {
                    WGLOG.warn(e, "W02001",
                            getName(),
                            script.getName());
                }
            }
        }
    }

    @Override
    public <T> DrainDriver<T> createDrain(ProcessScript<T> script) throws IOException {
        if (script == null) {
            throw new IllegalArgumentException("script must not be null"); //$NON-NLS-1$
        }
        LOG.debug("Creating drain driver for resource \"{}\" in process \"{}\"",
                getName(),
                script.getName());
        JdbcScript<T> jdbcScript = JdbcResourceUtil.convert(profile, script, arguments, DriverScript.Kind.DRAIN);
        WGLOG.info("I02001",
                getName(),
                script.getName());
        Connection connection = profile.openConnection();
        boolean succeed = false;
        try {
            JdbcDrainDriver<T> driver = new JdbcDrainDriver<T>(profile, jdbcScript, connection, true);
            succeed = true;
            return driver;
        } finally {
            if (succeed == false) {
                try {
                    LOG.debug("Disposing drain driver for resource \"{}\" in process \"{}\"",
                            getName(),
                            script.getName());
                    connection.close();
                } catch (SQLException e) {
                    WGLOG.warn(e, "W02001",
                            getName(),
                            script.getName());
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        LOG.debug("Closing JDBC resource: {}",
                getName());
    }
}
