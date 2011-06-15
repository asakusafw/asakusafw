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
package com.asakusafw.dmdl.analyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstType;
import com.asakusafw.dmdl.semantics.Declaration;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.Type;
import com.asakusafw.dmdl.spi.AttributeDriver;
import com.asakusafw.dmdl.spi.TypeDriver;

/**
 * A context of semantic analyzer.
 */
public class Context {

    static final Logger LOG = LoggerFactory.getLogger(Context.class);

    private final DmdlSemantics world;

    private final List<TypeDriver> typeDrivers;

    private final Map<String, AttributeDriver> attributeDrivers;

    /**
     * Creates and returns a new instance.
     * @param world the world object
     * @param typeDrivers type resolvers
     * @param attributeDrivers attributed analyzers
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public Context(
            DmdlSemantics world,
            Iterable<? extends TypeDriver> typeDrivers,
            Iterable<? extends AttributeDriver> attributeDrivers) {
        if (world == null) {
            throw new IllegalArgumentException("world must not be null"); //$NON-NLS-1$
        }
        if (typeDrivers == null) {
            throw new IllegalArgumentException("typeDrivers must not be null"); //$NON-NLS-1$
        }
        if (attributeDrivers == null) {
            throw new IllegalArgumentException("attributeDrivers must not be null"); //$NON-NLS-1$
        }
        this.world = world;
        this.typeDrivers = buildTypeDrivers(typeDrivers);
        this.attributeDrivers = buildAttributeDriverMap(attributeDrivers);
    }

    /**
     * Returns the root semantics model of current context.
     * @return the root model
     */
    public DmdlSemantics getWorld() {
        return world;
    }

    /**
     * Returns the corresponded type of the type syntax.
     * @param type the type syntax
     * @return the corresponded type, or {@code null} if cannot resolve
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public Type resolveType(AstType type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        for (TypeDriver driver : typeDrivers) {
            Type resolved = driver.resolve(world, type);
            if (resolved != null) {
                return resolved;
            }
        }
        return null;
    }

    /**
     * Returns an attribute driver corresponding to the attribute.
     * @param attribute the attribute to find a corresponded driver
     * @return the corresponded driver, or {@code null} if does not exist
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public AttributeDriver findAttributeDriver(AstAttribute attribute) {
        if (attribute == null) {
            throw new IllegalArgumentException("attribute must not be null"); //$NON-NLS-1$
        }
        String name = attribute.name.toString();
        return attributeDrivers.get(name);
    }

    private List<TypeDriver> buildTypeDrivers(Iterable<? extends TypeDriver> drivers) {
        assert drivers != null;
        List<TypeDriver> results = new ArrayList<TypeDriver>();
        for (TypeDriver driver : drivers) {
            LOG.debug("Activating type driver: {}", driver.getClass().getName());
            results.add(driver);
        }
        return results;
    }

    private Map<String, AttributeDriver> buildAttributeDriverMap(
            Iterable<? extends AttributeDriver> flatDrivers) {
        assert flatDrivers != null;
        Map<String, List<AttributeDriver>> group = new HashMap<String, List<AttributeDriver>>();
        for (AttributeDriver driver : flatDrivers) {
            LOG.debug("Activating attribute driver: {}", driver.getClass().getName());
            String target = driver.getTargetName();
            List<AttributeDriver> groupDrivers = group.get(target);
            if (groupDrivers == null) {
                groupDrivers = new ArrayList<AttributeDriver>();
                group.put(target, groupDrivers);
            }
            groupDrivers.add(driver);
        }

        Map<String, AttributeDriver> results = new HashMap<String, AttributeDriver>();
        for (Map.Entry<String, List<AttributeDriver>> entry : group.entrySet()) {
            String target = entry.getKey();
            LOG.debug("Enabling attribute: {}", target);
            List<AttributeDriver> targetDrivers = entry.getValue();
            AttributeDriver singular;
            if (targetDrivers.size() == 1) {
                singular = targetDrivers.get(0);
            } else {
                assert targetDrivers.isEmpty() == false;
                singular = new CompositeAttributeDriver(targetDrivers);
            }
            results.put(target, singular);
        }
        return results;
    }

    private static class CompositeAttributeDriver extends AttributeDriver {

        final List<AttributeDriver> drivers;

        private final String targetName;

        CompositeAttributeDriver(List<AttributeDriver> drivers) {
            assert drivers != null;
            assert drivers.isEmpty() == false;
            this.targetName = drivers.get(0).getTargetName();
            this.drivers = drivers;
        }

        @Override
        public String getTargetName() {
            return targetName;
        }

        @Override
        public void process(
                DmdlSemantics environment,
                Declaration declaration,
                AstAttribute attribute) {
            for (AttributeDriver driver : drivers) {
                driver.process(environment, declaration, attribute);
            }
        }
    }
}
