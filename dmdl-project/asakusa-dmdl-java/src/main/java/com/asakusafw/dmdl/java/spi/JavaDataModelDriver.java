/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.dmdl.java.spi;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.utils.java.model.syntax.Annotation;
import com.asakusafw.utils.java.model.syntax.FieldDeclaration;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.Type;

/**
 * Decorates Java data model classes.
 * <p>
 * To enhance Java model class generator, clients can implement this
 * and put the class name in
 * {@code META-INF/services/com.asakusafw.dmdl.java.spi.JavaDataModelDriver}.
 * </p>
 * <p>
 * All default implementation will do nothing.
 * </p>
 */
public abstract class JavaDataModelDriver {

    /**
     * Generates any resources (includes source programs) for the model.
     * This will be invoked once for each model.
     * @param context the attached context
     * @param model target model
     * @throws IOException if failed to generate resources
     * @since 0.2.2
     */
    public void generateResources(EmitContext context, ModelDeclaration model) throws IOException {
        return;
    }

    /**
     * Returns the interface types to implement to the model.
     * @param context the attached context
     * @param model target model
     * @return the list of interface types
     * @throws IOException if failed to create other models
     */
    public List<Type> getInterfaces(EmitContext context, ModelDeclaration model) throws IOException {
        return Collections.emptyList();
    }

    /**
     * Returns the field declarations to mixin to the model.
     * @param context the attached context
     * @param model target model
     * @return the list of field declarations
     * @throws IOException if failed to create other models
     * @since 0.2.3
     */
    public List<FieldDeclaration> getFields(EmitContext context, ModelDeclaration model) throws IOException {
        return Collections.emptyList();
    }

    /**
     * Returns the method declarations to mixin to the model.
     * @param context the attached context
     * @param model target model
     * @return the list of method declarations
     * @throws IOException if failed to create other models
     */
    public List<MethodDeclaration> getMethods(EmitContext context, ModelDeclaration model) throws IOException {
        return Collections.emptyList();
    }

    /**
     * Returns the type annotations to attach to the model.
     * @param context the attached context
     * @param model target model
     * @return the list of annotations
     * @throws IOException if failed to create other models
     */
    public List<Annotation> getTypeAnnotations(EmitContext context, ModelDeclaration model) throws IOException {
        return Collections.emptyList();
    }

    /**
     * Returns the type annotations to attach to the property.
     * @param context the attached context
     * @param property target property
     * @return the list of annotations
     * @throws IOException if failed to create other models
     */
    public List<Annotation> getMemberAnnotations(
            EmitContext context, PropertyDeclaration property) throws IOException {
        return Collections.emptyList();
    }
}
