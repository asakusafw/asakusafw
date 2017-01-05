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
package com.asakusafw.compiler.repository;

import java.text.MessageFormat;

import com.asakusafw.compiler.common.JavaName;
import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.DataClass;
import com.asakusafw.runtime.value.ValueOption;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;

/**
 * An implementation of {@code DataClass.Property} which property type is {@link ValueOption}.
 */
public class ValueOptionProperty implements DataClass.Property {

    private final ModelFactory factory;

    private final String name;

    private final Class<? extends ValueOption<?>> optionClass;

    /**
     * Creates a new instance.
     * @param factory the Java DOM factory
     * @param name the target property name
     * @param optionClass the target property type
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public ValueOptionProperty(
            ModelFactory factory,
            String name,
            Class<? extends ValueOption<?>> optionClass) {
        Precondition.checkMustNotBeNull(factory, "factory"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(name, "name"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(optionClass, "optionClass"); //$NON-NLS-1$
        this.factory = factory;
        this.name = name;
        this.optionClass = optionClass;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public java.lang.reflect.Type getType() {
        return optionClass;
    }

    @Override
    public Expression createNewInstance(Type target) {
        return new TypeBuilder(factory, target)
            .newObject()
            .toExpression();
    }

    @Override
    public boolean canNull() {
        return true;
    }

    @Override
    public Expression createIsNull(Expression object) {
        JavaName javaName = JavaName.of(name);
        javaName.addFirst("get"); //$NON-NLS-1$
        javaName.addLast("option"); //$NON-NLS-1$
        return new ExpressionBuilder(factory, object)
            .method(javaName.toMemberName())
            .method("isNull") //$NON-NLS-1$
            .toExpression();
    }

    @Override
    public Expression createGetter(Expression object) {
        JavaName javaName = JavaName.of(name);
        javaName.addFirst("get"); //$NON-NLS-1$
        javaName.addLast("option"); //$NON-NLS-1$
        return new ExpressionBuilder(factory, object)
            .method(javaName.toMemberName())
            .toExpression();
    }

    @Override
    public Statement assign(Expression target, Expression source) {
        return new ExpressionBuilder(factory, target)
            .method("copyFrom", source) //$NON-NLS-1$
            .toStatement();
    }

    @Override
    public Statement createGetter(Expression object, Expression target) {
        return assign(target, createGetter(object));
    }

    @Override
    public Statement createSetter(Expression object, Expression value) {
        JavaName javaName = JavaName.of(name);
        javaName.addFirst("set"); //$NON-NLS-1$
        javaName.addLast("option"); //$NON-NLS-1$
        return new ExpressionBuilder(factory, object)
            .method(javaName.toMemberName(), value)
            .toStatement();
    }

    @Override
    public Statement createWriter(Expression object, Expression dataOutput) {
        Precondition.checkMustNotBeNull(object, "object"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(dataOutput, "dataOutput"); //$NON-NLS-1$
        return new ExpressionBuilder(factory, object)
            .method("write", dataOutput) //$NON-NLS-1$
            .toStatement();
    }

    @Override
    public Statement createReader(Expression object, Expression dataInput) {
        Precondition.checkMustNotBeNull(object, "object"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(dataInput, "dataInput"); //$NON-NLS-1$
        return new ExpressionBuilder(factory, object)
            .method("readFields", dataInput) //$NON-NLS-1$
            .toStatement();
    }

    @Override
    public Expression createHashCode(Expression source) {
        Precondition.checkMustNotBeNull(source, "source"); //$NON-NLS-1$
        return new ExpressionBuilder(factory, source)
            .method("hashCode") //$NON-NLS-1$
            .toExpression();
    }

    @Override
    public Expression createBytesSize(
            Expression bytes,
            Expression start,
            Expression length) {
        Precondition.checkMustNotBeNull(bytes, "bytes"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(start, "start"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(length, "length"); //$NON-NLS-1$
        Type type = factory.newNamedType(Models.toName(factory, optionClass.getName()));
        return new TypeBuilder(factory, type)
            .method("getBytesLength", bytes, start, length) //$NON-NLS-1$
            .toExpression();
    }

    @Override
    public Expression createBytesDiff(
            Expression bytes1, Expression start1, Expression length1,
            Expression bytes2, Expression start2, Expression length2) {
        Precondition.checkMustNotBeNull(bytes1, "bytes1"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(start1, "start1"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(length1, "length1"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(bytes2, "bytes2"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(start2, "start2"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(length2, "length2"); //$NON-NLS-1$
        Type type = factory.newNamedType(Models.toName(factory, optionClass.getName()));
        return new TypeBuilder(factory, type)
            .method("compareBytes", //$NON-NLS-1$
                    bytes1, start1, length1,
                    bytes2, start2, length2)
            .toExpression();
    }

    @Override
    public Expression createValueDiff(Expression value1, Expression value2) {
        Precondition.checkMustNotBeNull(value1, "value1"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(value2, "value2"); //$NON-NLS-1$
        return new ExpressionBuilder(factory, value1)
            .method("compareTo", value2) //$NON-NLS-1$
            .toExpression();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + optionClass.hashCode();
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
        ValueOptionProperty other = (ValueOptionProperty) obj;
        if (name.equals(other.name) == false) {
            return false;
        }
        if (optionClass.equals(other.optionClass) == false) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}({1}:{2})", //$NON-NLS-1$
                getClass().getSimpleName(),
                getName(),
                getType());
    }
}
