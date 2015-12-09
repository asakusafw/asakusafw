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
package com.asakusafw.yaess.core;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A script that describes process execution using Hadoop.
 * @since 0.2.3
 * @version 0.8.0
 */
public class HadoopScript implements ExecutionScript {

    static final Logger LOG = LoggerFactory.getLogger(HadoopScript.class);

    private final String id;

    private final Set<String> blockerIds;

    private final String className;

    private final Map<String, String> hadoopProperties;

    private final Map<String, String> environmentVariables;

    private final Set<String> supportedExtensions;

    private final boolean resolved;

    /**
     * Creates a new instance.
     * Note that this creates an <em>UNRESOLVED</em> instance.
     * To create a resolved instance, please use {@link #resolve(ExecutionContext, ExecutionScriptHandler)}.
     * @param id the script ID
     * @param blockerIds other script IDs blocking this script execution
     * @param className fully qualified execution target class name
     * @param hadoopProperties the extra Hadoop properties
     * @param environmentVariables the extra environment variables
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public HadoopScript(
            String id, Set<String> blockerIds,
            String className,
            Map<String, String> hadoopProperties,
            Map<String, String> environmentVariables) {
        this(id, blockerIds,
                className, hadoopProperties, environmentVariables,
                Collections.<String>emptySet(),
                false);
    }

    /**
     * Creates a new instance.
     * Note that this creates an <em>UNRESOLVED</em> instance.
     * To create a resolved instance, please use {@link #resolve(ExecutionContext, ExecutionScriptHandler)}.
     * @param id the script ID
     * @param blockerIds other script IDs blocking this script execution
     * @param className fully qualified execution target class name
     * @param hadoopProperties the extra Hadoop properties
     * @param environmentVariables the extra environment variables
     * @param supportedExtensions the supported extension names
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.8.0
     */
    public HadoopScript(
            String id, Set<String> blockerIds,
            String className, Map<String, String> hadoopProperties, Map<String, String> environmentVariables,
            Collection<String> supportedExtensions) {
        this(id, blockerIds, className, hadoopProperties, environmentVariables, supportedExtensions, false);
    }

    private HadoopScript(
            String id, Set<String> blockerIds,
            String className, Map<String, String> hadoopProperties, Map<String, String> environmentVariables,
            Collection<String> supportedExtensions,
            boolean resolved) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null"); //$NON-NLS-1$
        }
        if (blockerIds == null) {
            throw new IllegalArgumentException("blockerIds must not be null"); //$NON-NLS-1$
        }
        if (className == null) {
            throw new IllegalArgumentException("className must not be null"); //$NON-NLS-1$
        }
        if (hadoopProperties == null) {
            throw new IllegalArgumentException("hadoopProperties must not be null"); //$NON-NLS-1$
        }
        if (environmentVariables == null) {
            throw new IllegalArgumentException("environmentVariables must not be null"); //$NON-NLS-1$
        }
        if (supportedExtensions == null) {
            throw new IllegalArgumentException("supportedExtensions must not be null"); //$NON-NLS-1$
        }
        this.id = id;
        this.blockerIds = Collections.unmodifiableSet(new TreeSet<>(blockerIds));
        this.className = className;
        this.hadoopProperties = Collections.unmodifiableMap(new LinkedHashMap<>(hadoopProperties));
        this.environmentVariables = Collections.unmodifiableMap(new LinkedHashMap<>(environmentVariables));
        this.supportedExtensions = Collections.unmodifiableSet(new LinkedHashSet<>(supportedExtensions));
        this.resolved = resolved;
    }

    @Override
    public Kind getKind() {
        return Kind.HADOOP;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Set<String> getBlockerIds() {
        return blockerIds;
    }

    /**
     * Returns the target class name.
     * @return the target class name
     */
    public String getClassName() {
        return className;
    }

    /**
     * Returns the extra hadoop properties.
     * @return the extra hadoop properties
     */
    public Map<String, String> getHadoopProperties() {
        return hadoopProperties;
    }

    @Override
    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    @Override
    public Set<String> getSupportedExtensions() {
        return supportedExtensions;
    }

    @Override
    public boolean isResolved() {
        return resolved;
    }

    @Override
    public HadoopScript resolve(
            ExecutionContext context,
            ExecutionScriptHandler<?> handler) throws InterruptedException, IOException {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler must not be null"); //$NON-NLS-1$
        }
        if (isResolved()) {
            return this;
        }
        LOG.debug("Resolving {}", this);

        PlaceholderResolver resolver = new PlaceholderResolver(this, context, handler);
        Map<String, String> resolvedProperties = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : getHadoopProperties().entrySet()) {
            resolvedProperties.put(entry.getKey(), resolver.resolve(entry.getValue()));
        }
        LOG.debug("Resolved Hadoop properties: {}", resolvedProperties);

        Map<String, String> resolvedEnvironments = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : getEnvironmentVariables().entrySet()) {
            resolvedEnvironments.put(entry.getKey(), resolver.resolve(entry.getValue()));
        }
        LOG.debug("Resolved environment variables: {}", resolvedEnvironments);

        return new HadoopScript(
                getId(), getBlockerIds(),
                className,
                resolvedProperties,
                resolvedEnvironments,
                supportedExtensions,
                true);
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "Hadoop'{'id={0}, blockers={1}, class={2}, properties={3}, environment={4}'}'",
                getId(),
                getBlockerIds(),
                getClassName(),
                getHadoopProperties(),
                getEnvironmentVariables());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id.hashCode();
        result = prime * result + blockerIds.hashCode();
        result = prime * result + className.hashCode();
        result = prime * result + hadoopProperties.hashCode();
        result = prime * result + environmentVariables.hashCode();
        result = prime * result + supportedExtensions.hashCode();
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
        HadoopScript other = (HadoopScript) obj;
        if (!id.equals(other.id)) {
            return false;
        }
        if (!blockerIds.equals(other.blockerIds)) {
            return false;
        }
        if (!className.equals(other.className)) {
            return false;
        }
        if (!hadoopProperties.equals(other.hadoopProperties)) {
            return false;
        }
        if (!environmentVariables.equals(other.environmentVariables)) {
            return false;
        }
        if (!supportedExtensions.equals(other.supportedExtensions)) {
            return false;
        }
        return true;
    }
}
