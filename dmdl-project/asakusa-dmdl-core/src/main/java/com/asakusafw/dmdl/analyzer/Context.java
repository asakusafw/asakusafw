/**
 * Copyright 2011-2018 Asakusa Framework Team.
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

import com.asakusafw.dmdl.Diagnostic;
import com.asakusafw.dmdl.Diagnostic.Level;
import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstNode;
import com.asakusafw.dmdl.model.AstType;
import com.asakusafw.dmdl.semantics.Declaration;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.Type;
import com.asakusafw.dmdl.spi.AttributeDriver;
import com.asakusafw.dmdl.spi.TypeDriver;
import com.asakusafw.utils.collections.Maps;

/**
 * A context of semantic analyzer.
 * @since 0.2.0
 * @version 0.9.2
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
     * Reports an error.
     * @param node the related AST node
     * @param format the message format
     * @param arguments the message arguments
     * @since 0.9.2
     */
    public void error(AstNode node, String format, Object... arguments) {
        world.report(new Diagnostic(Level.ERROR, node, format, arguments));
    }

    /**
     * Returns whether or not there are any errors in this context.
     * @return {@code true} if there are some errors, otherwise {@code false}
     */
    public boolean hasError() {
        return world.hasError();
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
        TypeDriver.Context local = new TypeDriver.Context() {
            @Override
            public DmdlSemantics getEnvironment() {
                return Context.this.getWorld();
            }
            @Override
            public Type resolve(AstType node) {
                return Context.this.resolveType(node);
            }
        };
        for (TypeDriver driver : typeDrivers) {
            Type resolved = driver.resolve(local, type);
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
        List<TypeDriver> results = new ArrayList<>();
        for (TypeDriver driver : drivers) {
            LOG.debug("Activating type driver: {}", driver.getClass().getName()); //$NON-NLS-1$
            results.add(driver);
        }
        return results;
    }

    private Map<String, AttributeDriver> buildAttributeDriverMap(Iterable<? extends AttributeDriver> flatDrivers) {
        assert flatDrivers != null;
        Map<String, List<AttributeDriver>> group = new HashMap<>();
        for (AttributeDriver driver : flatDrivers) {
            LOG.debug("Activating attribute driver: {}", driver.getClass().getName()); //$NON-NLS-1$
            String target = driver.getTargetName();
            Maps.addToList(group, target, driver);
        }

        Map<String, AttributeDriver> results = new HashMap<>();
        for (Map.Entry<String, List<AttributeDriver>> entry : group.entrySet()) {
            String target = entry.getKey();
            LOG.debug("Enabling attribute: {}", target); //$NON-NLS-1$
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

    private static class CompositeAttributeDriver implements AttributeDriver {

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
        public void process(Context context, Declaration declaration, AstAttribute attribute) {
            for (AttributeDriver driver : drivers) {
                driver.process(context, declaration, attribute);
            }
        }

        @Override
        public void verify(Context context, Declaration declaration, AstAttribute attribute) {
            for (AttributeDriver driver : drivers) {
                driver.verify(context, declaration, attribute);
            }
        }
    }
}
