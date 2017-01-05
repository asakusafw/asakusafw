/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.compiler.flow;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.asakusafw.compiler.common.NameGenerator;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.runtime.core.Result;
import com.asakusafw.runtime.flow.ArrayListBuffer;
import com.asakusafw.runtime.flow.FileMapListBuffer;
import com.asakusafw.runtime.flow.ListBuffer;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.FieldDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.ImportBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;
import com.asakusafw.vocabulary.flow.graph.FlowElementAttribute;
import com.asakusafw.vocabulary.flow.graph.FlowElementAttributeProvider;
import com.asakusafw.vocabulary.flow.graph.FlowElementDescription;
import com.asakusafw.vocabulary.flow.graph.FlowElementPortDescription;
import com.asakusafw.vocabulary.flow.graph.FlowResourceDescription;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.flow.processor.InputBuffer;

/**
 * An abstract super interface for processing flow elements.
 * Developers should not inherit this interface directly, and inherit {@link AbstractFlowElementProcessor} instead.
 */
public interface FlowElementProcessor extends FlowCompilingEnvironment.Initializable {

    /**
     * A method name of {@link Result#add(Object)}.
     */
    String RESULT_METHOD_NAME = "add"; //$NON-NLS-1$

    /**
     * Returns the kind of this processor.
     * @return the processor kind
     */
    FlowElementProcessor.Kind getKind();

    /**
     * Returns the target operator annotation type of this processor.
     * @return the target operator annotation type
     */
    Class<? extends Annotation> getTargetAnnotationType();

    /**
     * The abstract implementation of context objects for {@link FlowElementProcessor}.
     * @since 0.1.0
     * @version 0.9.1
     */
    abstract class AbstractProcessorContext implements FlowElementAttributeProvider {

        /**
         * The current environment.
         */
        protected final FlowCompilingEnvironment environment;

        private final FlowElementAttributeProvider element;

        /**
         * The Java DOM factory.
         */
        protected final ModelFactory factory;

        /**
         * The import declaration builder.
         */
        protected final ImportBuilder importer;

        /**
         * The unique name generator.
         */
        protected final NameGenerator names;

        /**
         * The target operator description.
         */
        protected final OperatorDescription description;

        /**
         * The mapping between external resources and their Java expressions.
         */
        protected final Map<FlowResourceDescription, Expression> resources;

        /**
         * The generated fields.
         */
        protected final List<FieldDeclaration> generatedFields;

        /**
         * Creates a new instance.
         * @param environment the current context
         * @param element the target element
         * @param importer the import declaration builder
         * @param names the unique name generator
         * @param desc the target operator description
         * @param resources the mapping between external resources and their Java expressions
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        public AbstractProcessorContext(
                FlowCompilingEnvironment environment,
                FlowElementAttributeProvider element,
                ImportBuilder importer,
                NameGenerator names,
                OperatorDescription desc,
                Map<FlowResourceDescription, Expression> resources) {
            Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(importer, "importer"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(element, "element"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(names, "names"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(desc, "desc"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(resources, "resources"); //$NON-NLS-1$
            this.environment = environment;
            this.element = element;
            this.factory = environment.getModelFactory();
            this.importer = importer;
            this.names = names;
            this.description = desc;
            this.resources = resources;
            this.generatedFields = new ArrayList<>();
        }

        /**
         * Returns the target operator description.
         * @return the target operator description
         */
        public OperatorDescription getOperatorDescription() {
            return description;
        }

        @Override
        public Set<? extends Class<? extends FlowElementAttribute>> getAttributeTypes() {
            return element.getAttributeTypes();
        }

        @Override
        public <T extends FlowElementAttribute> T getAttribute(Class<T> attributeClass) {
            return element.getAttribute(attributeClass);
        }

        /**
         * Returns the port description of the target operator.
         * @param portNumber the target port number
         * @return the port description
         * @throws IllegalArgumentException the target port is not found
         */
        public FlowElementPortDescription getInputPort(int portNumber) {
            if (portNumber < 0 || portNumber >= description.getInputPorts().size()) {
                throw new IllegalArgumentException("invalid port number"); //$NON-NLS-1$
            }
            return description.getInputPorts().get(portNumber);
        }

        /**
         * Returns the port description of the target operator.
         * @param portNumber the target port number
         * @return the port description
         * @throws IllegalArgumentException the target port is not found
         */
        public FlowElementPortDescription getOutputPort(int portNumber) {
            if (portNumber < 0 || portNumber >= description.getOutputPorts().size()) {
                throw new IllegalArgumentException("invalid port number"); //$NON-NLS-1$
            }
            return description.getOutputPorts().get(portNumber);
        }

        /**
         * Returns the external resource description of the target operator.
         * @param resourceNumber the target resource number
         * @return the resource description
         * @throws IllegalArgumentException the target resource is not found
         */
        public FlowResourceDescription getResourceDescription(int resourceNumber) {
            if (resourceNumber < 0 || resourceNumber >= description.getResources().size()) {
                throw new IllegalArgumentException("invalid resource number"); //$NON-NLS-1$
            }
            FlowResourceDescription resource = description.getResources().get(resourceNumber);
            return resource;
        }

        /**
         * Returns the Java expression to access to the target external resource.
         * @param resource the target resource description
         * @return the expression
         * @throws IllegalArgumentException if the target parameter is {@code null}
         */
        public Expression getResource(FlowResourceDescription resource) {
            Precondition.checkMustNotBeNull(resource, "resource"); //$NON-NLS-1$
            Expression expression = resources.get(resource);
            assert expression != null;
            return expression;
        }

        /**
         * Returns the Java DOM factory.
         * @return the Java DOM factory
         */
        public ModelFactory getModelFactory() {
            return factory;
        }

        private Expression addField(Type type, String name, Expression init) {
            assert type != null;
            assert name != null;
            SimpleName fieldName = createName(name);
            FieldDeclaration field = factory.newFieldDeclaration(
                    null,
                    new AttributeBuilder(factory)
                        .Private()
                        .toAttributes(),
                    type,
                    fieldName,
                    init);
            generatedFields.add(field);
            return factory.newFieldAccessExpression(
                    factory.newThis(),
                    fieldName);
        }

        /**
         * Returns the generated fields.
         * @return the generated fields
         */
        public List<FieldDeclaration> getGeneratedFields() {
            return generatedFields;
        }

        /**
         * Returns a new unique name.
         * @param hint the name hint
         * @return the unique name
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        public SimpleName createName(String hint) {
            Precondition.checkMustNotBeNull(hint, "hint"); //$NON-NLS-1$
            return names.create(hint);
        }

        /**
         * Returns an expression which creates a new instance of the target operator implementation class.
         * @return the generated expression
         */
        public Expression createImplementation() {
            Class<?> implementing = description.getDeclaration().getImplementing();
            Type type = convert(implementing);
            return addField(type, "op", new TypeBuilder(factory, type) //$NON-NLS-1$
                .newObject()
                .toExpression());
        }

        /**
         * Returns an expression which accesses a new field.
         * @param type the target type
         * @param name a name hint for the target field
         * @return an expression to access the created field
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        public Expression createField(java.lang.reflect.Type type, String name) {
            return createField(type, name, null);
        }

        /**
         * Returns an expression which accesses a new field.
         * @param type the target type
         * @param name a name hint for the target field
         * @param init field initialization expression (nullable)
         * @return an expression to access the created field
         * @throws IllegalArgumentException if some parameters were {@code null}
         * @since 0.5.1
         */
        public Expression createField(java.lang.reflect.Type type, String name, Expression init) {
            Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(name, "name"); //$NON-NLS-1$
            return addField(
                    importer.toType(type),
                    name,
                    init);
        }

        /**
         * Returns a new data model object mirror.
         * @param type the data model type
         * @return the generated expression
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        public DataObjectMirror createModelCache(java.lang.reflect.Type type) {
            Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
            DataClass data = environment.getDataClasses().load(type);
            if (data == null) {
                environment.error(
                        Messages.getString("FlowElementProcessor.errorMissingDataClass"), //$NON-NLS-1$
                        type);
                data = new DataClass.Unresolved(factory, type);
            }
            Type domType = importer.toType(type);
            Expression cache = addField(
                    domType,
                    "cache", //$NON-NLS-1$
                    data.createNewInstance(domType));
            return new DataObjectMirror(factory, cache, data);
        }

        /**
         * Returns a new {@link ListBuffer} object mirror.
         * @param type the element type of the {@link ListBuffer}
         * @param bufferKind the input buffer kind
         * @return the generated expression
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        public ListBufferMirror createListBuffer(java.lang.reflect.Type type, InputBuffer bufferKind) {
            Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(bufferKind, "bufferKind"); //$NON-NLS-1$
            Type elementType = importer.toType(type);
            Class<?> bufferType = inputBufferTypeFromKind(bufferKind);
            Type listType = importer.resolve(factory.newParameterizedType(
                    Models.toType(factory, bufferType),
                    Collections.singletonList(elementType)));
            Expression list = addField(
                    listType,
                    "list", //$NON-NLS-1$
                    new TypeBuilder(factory, listType)
                        .newObject()
                        .toExpression());
            DataClass component = environment.getDataClasses().load(type);
            if (component == null) {
                environment.error(
                        Messages.getString("FlowElementProcessor.errorMissingDataClass"), //$NON-NLS-1$
                        type);
                component = new DataClass.Unresolved(factory, type);
            }
            return new ListBufferMirror(factory, list, component, elementType);
        }

        private Class<?> inputBufferTypeFromKind(InputBuffer kind) {
            assert kind != null;
            switch (kind) {
            case EXPAND:
                return ArrayListBuffer.class;
            case ESCAPE:
                return FileMapListBuffer.class;
            default:
                throw new AssertionError(kind);
            }
        }

        /**
         * Returns an imported type for the specified one.
         * @param type the target type
         * @return the imported type
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        public Type convert(java.lang.reflect.Type type) {
            Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
            return importer.toType(type);
        }

        /**
         * Returns an imported type for the specified one.
         * @param type the target type
         * @return the imported type
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        public Type simplify(Type type) {
            Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
            return importer.resolve(type);
        }
    }

    /**
     * A mirror of data model objects.
     */
    class DataObjectMirror {

        private final Expression object;

        private final DataClass dataClass;

        /**
         * Creates a new instance.
         * @param factory the Java DOM factory
         * @param object an expression of the target data model object
         * @param dataClass the data model class
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        public DataObjectMirror(ModelFactory factory, Expression object, DataClass dataClass) {
            Precondition.checkMustNotBeNull(factory, "factory"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(object, "object"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(dataClass, "dataClass"); //$NON-NLS-1$
            this.object = object;
            this.dataClass = dataClass;
        }

        /**
         * Returns an expression of the target data model object.
         * @return an expression of the target data model object
         */
        public Expression get() {
            return object;
        }

        /**
         * Returns a statement which copies the contents of the specified object into this mirror.
         * @param value an expression which accesses the target object
         * @return the generated statement
         * @throws IllegalArgumentException the parameter is {@code null}
         */
        public Statement createSet(Expression value) {
            Precondition.checkMustNotBeNull(value, "value"); //$NON-NLS-1$
            return dataClass.assign(object, value);
        }

        /**
         * Returns a statement which resets the contents of this mirror.
         * @return the generated statement
         */
        public Statement createReset() {
            return dataClass.reset(object);
        }
    }

    /**
     * A mirror of a {@link Result} object.
     */
    class ResultMirror {

        private final ModelFactory factory;

        private final Expression object;

        /**
         * Creates a new instance.
         * @param factory the Java DOM factory
         * @param object an expression of the target {@link Result} object
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        public ResultMirror(ModelFactory factory, Expression object) {
            Precondition.checkMustNotBeNull(factory, "factory"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(object, "object"); //$NON-NLS-1$
            this.factory = factory;
            this.object = object;
        }

        /**
         * Returns an expression of the target {@link Result} object.
         * @return an expression of the target object
         */
        public Expression get() {
            return object;
        }

        /**
         * Returns a statement which adds a value into this.
         * @param value an expression of the target value
         * @return the generated statement
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        public Statement createAdd(Expression value) {
            Precondition.checkMustNotBeNull(value, "value"); //$NON-NLS-1$
            return new ExpressionBuilder(factory, object)
                .method(RESULT_METHOD_NAME, value)
                .toStatement();
        }
    }

    /**
     * A mirror of {@link ListBuffer}.
     */
    class ListBufferMirror {

        private static final String BEGIN = "begin"; //$NON-NLS-1$

        private static final String ADVANCE = "advance"; //$NON-NLS-1$

        private static final String END = "end"; //$NON-NLS-1$

        private static final String EXPAND = "expand"; //$NON-NLS-1$

        private static final String IS_EXPAND_REQUIRED = "isExpandRequired"; //$NON-NLS-1$

        private static final String SHRINK = "shrink"; //$NON-NLS-1$

        private final ModelFactory factory;

        private final Expression object;

        private final DataClass dataClass;

        private final Type elementType;

        /**
         * Creates a new instance.
         * @param factory the Java DOM factory
         * @param object an expression of the target {@link ListBuffer} object
         * @param dataClass the element type
         * @param elementType the element type
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        public ListBufferMirror(ModelFactory factory, Expression object, DataClass dataClass, Type elementType) {
            Precondition.checkMustNotBeNull(factory, "factory"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(object, "object"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(dataClass, "dataClass"); //$NON-NLS-1$
            this.factory = factory;
            this.object = object;
            this.dataClass = dataClass;
            this.elementType = elementType;
        }

        /**
         * Returns an expression of the target {@link ListBuffer} object.
         * @return the expression of the target object
         */
        public Expression get() {
            return object;
        }

        /**
         * Returns a statement which initializes this object.
         * @return the generated statement
         * @see ListBuffer#begin()
         */
        public Statement createBegin() {
            return new ExpressionBuilder(factory, object)
                .method(BEGIN)
                .toStatement();
        }

        /**
         * Returns a statement which adds a copy of data model object into this object.
         * @param value an expression of the target data model object
         * @return the generated statement
         * @throws IllegalArgumentException if the parameter is {@code null}
         * @see ListBuffer#advance()
         * @see ListBuffer#expand(Object)
         * @see ListBuffer#isExpandRequired()
         */
        public Statement createAdvance(Expression value) {
            Precondition.checkMustNotBeNull(value, "value"); //$NON-NLS-1$
            List<Statement> thenBlock = Arrays.asList(new Statement[] {
                    new ExpressionBuilder(factory, object)
                        .method(EXPAND, dataClass.createNewInstance(elementType))
                        .toStatement(),
                    dataClass.assign(
                            new ExpressionBuilder(factory, object)
                                .method(ADVANCE)
                                .toExpression(),
                            value),
            });
            List<Statement> elseBlock = Arrays.asList(new Statement[] {
                    dataClass.assign(
                            new ExpressionBuilder(factory, object)
                                .method(ADVANCE)
                                .toExpression(),
                            value),
            });
            return factory.newIfStatement(
                    new ExpressionBuilder(factory, object)
                        .method(IS_EXPAND_REQUIRED)
                        .toExpression(),
                    factory.newBlock(thenBlock),
                    factory.newBlock(elseBlock));
        }

        /**
         * Returns a statement which finalizes this object.
         * @return the generated statement
         * @see ListBuffer#end()
         */
        public Statement createEnd() {
            return new ExpressionBuilder(factory, object)
                .method(END)
                .toStatement();
        }

        /**
         * Returns a statement which disposes this object.
         * @return the generated statement
         * @see ListBuffer#shrink()
         */
        public Statement createShrink() {
            return new ExpressionBuilder(factory, object)
                .method(SHRINK)
                .toStatement();
        }
    }

    /**
     * An abstract super interface of repository of {@link FlowElementProcessor}.
     */
    interface Repository extends FlowCompilingEnvironment.Initializable {

        /**
         * Returns a processor for the empty operators.
         * @return the processor
         */
        LinePartProcessor getEmptyProcessor();

        /**
         * Returns a {@link FlowElementProcessor} for processing the target operator.
         * @param description the target element description
         * @return the corresponded processor, or {@code null} if there is no available processors
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        FlowElementProcessor findProcessor(FlowElementDescription description);

        /**
         * Returns a {@link FlowElementProcessor} for processing the target extract-like operator.
         * @param description the target element description
         * @return the corresponded processor, or {@code null} if there is no available processors
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        LineProcessor findLineProcessor(FlowElementDescription description);

        /**
         * Returns a {@link FlowElementProcessor} for processing the target co-group like operator.
         * @param description the target element description
         * @return the corresponded processor, or {@code null} if there is no available processors
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        RendezvousProcessor findRendezvousProcessor(FlowElementDescription description);
    }

    /**
     * Represents a kind of {@link FlowElementProcessor}.
     */
    enum Kind {

        /**
         * {@link LinePartProcessor}.
         */
        LINE_PART,

        /**
         * {@link LineEndProcessor}.
         */
        LINE_END,

        /**
         * {@link RendezvousProcessor}.
         */
        RENDEZVOUS,
    }

}
