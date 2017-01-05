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

import java.io.DataInput;
import java.io.DataOutput;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.util.CommentEmitTrait;
import com.asakusafw.utils.java.model.util.Models;

/**
 * An abstract super interface of resolved data types.
 */
public interface DataClass {

    /**
     * Returns the representation of this data type as a Java class.
     * @return the representation of this data type as a Java class
     */
    java.lang.reflect.Type getType();

    /**
     * Returns the all available properties in this data type.
     * @return the all available properties
     */
    Collection<? extends Property> getProperties();

    /**
     * Returns a property in this data type.
     * @param propertyName the target property name
     * @return the target property, or {@code null} if this data type does not contain such a property
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    Property findProperty(String propertyName);

    /**
     * Returns an expression that creates a new instance of this type.
     * @param type a DOM for this data type
     * @return the created expression
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    Expression createNewInstance(Type type);

    /**
     * Returns a statement that assigns a value of this data type.
     * @param target the assign target
     * @param source the assign value
     * @return the created statement
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    Statement assign(Expression target, Expression source);

    /**
     * Returns a statement that resets a value of this data type.
     * @param object the expression of the target value
     * @return the created statement
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    Statement reset(Expression object);

    /**
     * Returns a statement that writes a value of this data type into {@link DataOutput}.
     * @param object the expression of target value
     * @param dataOutput the expression of the target {@link DataOutput}
     * @return the created statement
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    Statement createWriter(Expression object, Expression dataOutput);

    /**
     * Returns a statement that reads a contents of this data type from {@link DataInput}.
     * @param object the expression of the target value
     * @param dataInput the expression of the source {@link DataInput}
     * @return the created statement
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    Statement createReader(Expression object, Expression dataInput);

    /**
     * Represents an unresolved {@link DataClass}.
     */
    class Unresolved implements DataClass {

        private final ModelFactory factory;

        private final java.lang.reflect.Type runtimeType;

        /**
         * Creates a new instance.
         * @param factory the Java DOM factory
         * @param runtimeType the target runtime type
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        public Unresolved(ModelFactory factory, java.lang.reflect.Type runtimeType) {
            Precondition.checkMustNotBeNull(factory, "factory"); //$NON-NLS-1$
            Precondition.checkMustNotBeNull(runtimeType, "runtimeType"); //$NON-NLS-1$
            this.factory = factory;
            this.runtimeType = runtimeType;
        }

        @Override
        public java.lang.reflect.Type getType() {
            return runtimeType;
        }

        @Override
        public Collection<? extends Property> getProperties() {
            return Collections.emptySet();
        }

        @Override
        public Property findProperty(String propertyName) {
            return null;
        }

        @Override
        public Statement reset(Expression object) {
            Statement statement = factory.newEmptyStatement();
            statement.putModelTrait(CommentEmitTrait.class, new CommentEmitTrait(Arrays.asList(
                    MessageFormat.format(
                            "Failed to resolve in \"reset\": {0}", //$NON-NLS-1$
                            runtimeType)
            )));
            return statement;
        }

        @Override
        public Expression createNewInstance(Type type) {
            Expression expression = Models.toNullLiteral(factory);
            expression.putModelTrait(CommentEmitTrait.class, new CommentEmitTrait(Arrays.asList(
                    MessageFormat.format(
                            "Failed to resolve in \"createNewInstance\": {0}", //$NON-NLS-1$
                            runtimeType)
            )));
            return expression;
        }

        @Override
        public Statement assign(Expression target, Expression source) {
            Statement statement = factory.newEmptyStatement();
            statement.putModelTrait(CommentEmitTrait.class, new CommentEmitTrait(Arrays.asList(
                    MessageFormat.format(
                            "Failed to resolve in \"assign\": {0}", //$NON-NLS-1$
                            runtimeType)
            )));
            return statement;
        }

        @Override
        public Statement createWriter(Expression object, Expression dataOutput) {
            Statement statement = factory.newEmptyStatement();
            statement.putModelTrait(CommentEmitTrait.class, new CommentEmitTrait(Arrays.asList(
                    MessageFormat.format(
                            "Failed to resolve in \"createWriter\": {0}", //$NON-NLS-1$
                            runtimeType)
            )));
            return statement;
        }

        @Override
        public Statement createReader(Expression object, Expression dataInput) {
            Statement statement = factory.newEmptyStatement();
            statement.putModelTrait(CommentEmitTrait.class, new CommentEmitTrait(Arrays.asList(
                    MessageFormat.format(
                            "Failed to resolve in \"createReader\": {0}", //$NON-NLS-1$
                            runtimeType)
            )));
            return statement;
        }
    }

    /**
     * Represents a property in {@link DataClass}.
     */
    interface Property {

        /**
         * Returns the name of this property.
         * @return the property name
         */
        String getName();

        /**
         * Returns the type of this property as a Java class.
         * @return the representation of this property type as a Java class
         */
        java.lang.reflect.Type getType();

        /**
         * Returns whether this property is nullable or not.
         * @return {@code true} if this property is nullable, otherwise {@code false}
         */
        boolean canNull();

        /**
         * Returns an expression that creates a new value object for this property.
         * @param target the target data type
         * @return the created expression
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        Expression createNewInstance(Type target);

        /**
         * Returns an expression that returns whether this property is {@code null} or not.
         * @param object the target object
         * @return the created expression
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        Expression createIsNull(Expression object);

        /**
         * Returns an expression that returns this property from the target object.
         * @param object the target object
         * @return the created expression
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        Expression createGetter(Expression object);

        /**
         * Returns a statement that assigns a value into this property.
         * @param target the target property ({@link #createGetter(Expression)})
         * @param source the value to be assigned
         * @return the created statement
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        Statement assign(Expression target, Expression source);

        /**
         * Returns a statement that copies a value of this property into the other value object.
         * @param object the property owner
         * @param target the target value object
         * @return the created statement
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        Statement createGetter(Expression object, Expression target);

        /**
         * Returns a statement that assigns a value into the property of the target object.
         * @param object the property owner
         * @param value the value to be assigned
         * @return the created statement
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        Statement createSetter(Expression object, Expression value);

        /**
         * Returns a statement that writes the property of the target object into {@link DataOutput}.
         * @param object the property owner
         * @param dataOutput the target {@code java.io.DataOutput}
         * @return the created statement
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        Statement createWriter(Expression object, Expression dataOutput);

        /**
         * Returns a statement that reads the property of the target object from {@link DataInput}.
         * @param object the property owner
         * @param dataInput the source {@code java.io.DataInput}
         * @return the created statement
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        Statement createReader(Expression object, Expression dataInput);

        /**
         * Returns an expression that returns the hash code of the property.
         * @param object the property owner
         * @return the created expression
         * @throws IllegalArgumentException if the parameter is {@code null}
         */
        Expression createHashCode(Expression object);

        /**
         * Returns an expression that returns the serialized length of the target value.
         * @param bytes expression of the serialized bytes
         * @param start expression of the start offset index in the serialized bytes
         * @param length expression of the limit length in the serialized bytes
         * @return the created expression
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        Expression createBytesSize(Expression bytes, Expression start, Expression length);

        /**
         * Returns an expression that compares two serialized bytes.
         * @param bytes1 expression of the first serialized bytes
         * @param start1 expression of the start offset in {@code bytes1}
         * @param length1 expression of the limit length in {@code bytes1}
         * @param bytes2 expression of the second serialized bytes
         * @param start2 expression of the start offset in {@code bytes2}
         * @param length2 expression of the limit length in {@code bytes2}
         * @return the created expression
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        Expression createBytesDiff(
                Expression bytes1, Expression start1, Expression length1,
                Expression bytes2, Expression start2, Expression length2);

        /**
         * Returns an expression that compares two values.
         * @param value1 expression of the first value
         * @param value2 expression of the second value
         * @return the created expression
         * @throws IllegalArgumentException if the parameters are {@code null}
         */
        Expression createValueDiff(Expression value1, Expression value2);
    }
}
