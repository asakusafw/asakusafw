/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.utils.java.model.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.asakusafw.utils.java.model.syntax.ArrayInitializer;
import com.asakusafw.utils.java.model.syntax.ArrayType;
import com.asakusafw.utils.java.model.syntax.ClassBody;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.syntax.NamedType;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Type;

/**
 * A builder for building types or their related elements.
 */
public class TypeBuilder {

    private final ModelFactory f;

    private Type context;

    /**
     * Creates a new instance.
     * @param factory the Java DOM factory
     * @param context the context type
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public TypeBuilder(ModelFactory factory, Type context) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        this.f = factory;
        this.context = context;
    }

    /**
     * Returns a copy of this builder.
     * @return the copy
     */
    public TypeBuilder copy() {
        return new TypeBuilder(f, context);
    }

    /**
     * Returns the built type.
     * @return the built type
     */
    public Type toType() {
        return context;
    }

    /**
     * Returns the built type as a named type.
     * @return the built type
     * @throws IllegalStateException the building type is not a named type
     */
    public NamedType toNamedType() {
        if (context.getModelKind() != ModelKind.NAMED_TYPE) {
            throw new IllegalStateException("context type must be a named type"); //$NON-NLS-1$
        }
        return (NamedType) context;
    }

    /**
     * Returns the built type as an array type.
     * @return the built type
     * @throws IllegalStateException the building type is not an array type
     */
    public ArrayType toArrayType() {
        if (context.getModelKind() != ModelKind.ARRAY_TYPE) {
            throw new IllegalStateException("context type must be an array type"); //$NON-NLS-1$
        }
        return (ArrayType) context;
    }

    /**
     * Returns the parameterized type.
     * @param typeArguments the type arguments
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public TypeBuilder parameterize(Type... typeArguments) {
        if (typeArguments == null) {
            throw new IllegalArgumentException("typeArguments must not be null"); //$NON-NLS-1$
        }
        return parameterize(Arrays.asList(typeArguments));
    }

    /**
     * Returns the parameterized type.
     * @param typeArguments the type arguments
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public TypeBuilder parameterize(List<? extends Type> typeArguments) {
        if (typeArguments == null) {
            throw new IllegalArgumentException("typeArguments must not be null"); //$NON-NLS-1$
        }
        if (typeArguments.isEmpty()) {
            throw new IllegalArgumentException("typeArguments must have one or more elements"); //$NON-NLS-1$
        }
        return chain(f.newParameterizedType(context, typeArguments));
    }

    /**
     * Returns the parameterized type.
     * @param typeArguments the type arguments
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public TypeBuilder parameterize(java.lang.reflect.Type... typeArguments) {
        if (typeArguments == null) {
            throw new IllegalArgumentException("typeArguments must not be null"); //$NON-NLS-1$
        }
        List<Type> args = new ArrayList<>();
        for (java.lang.reflect.Type type : typeArguments) {
            args.add(Models.toType(f, type));
        }
        return parameterize(args);
    }

    /**
     * Returns the qualified type.
     * @param name the enclosing type name
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public TypeBuilder enclose(Name name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (context.getModelKind() == ModelKind.NAMED_TYPE) {
            Name enclosed = Models.append(f, toNamedType().getName(), name);
            return chain(f.newNamedType(enclosed));
        } else {
            Type current = context;
            for (SimpleName segment : Models.toList(name)) {
                current = f.newQualifiedType(current, segment);
            }
            return chain(current);
        }
    }

    /**
     * Returns the qualified type.
     * @param name the enclosing type name
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public TypeBuilder enclose(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        return enclose(Models.toName(f, name));
    }

    /**
     * Returns the array type.
     * @param dimensions the number of dimensions
     * @return this
     * @throws IllegalArgumentException if the parameter is a negative value
     */
    public TypeBuilder array(int dimensions) {
        if (dimensions < 0) {
            throw new IllegalArgumentException("dimensions must be greater than or equal to 0"); //$NON-NLS-1$
        }
        Type current = context;
        for (int i = 0; i < dimensions; i++) {
            current = f.newArrayType(current);
        }
        return chain(current);
    }

    /**
     * Returns a class literal.
     * @return the chained expression builder
     */
    public ExpressionBuilder dotClass() {
        return expr(f.newClassLiteral(context));
    }

    /**
     * Returns the array instance creation expression.
     * @param dimensions the number of array elements for each dimension
     * @return the chained expression builder
     * @throws IllegalStateException if the building type is not an array type
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ExpressionBuilder newArray(int... dimensions) {
        if (dimensions == null) {
            throw new IllegalArgumentException("dimensions must not be null"); //$NON-NLS-1$
        }
        List<Expression> exprs = new ArrayList<>();
        for (int dim : dimensions) {
            exprs.add(Models.toLiteral(f, dim));
        }
        return newArray(exprs);
    }

    /**
     * Returns the array instance creation expression.
     * @param dimensions the number of array elements for each dimension
     * @return the chained expression builder
     * @throws IllegalStateException if the building type is not an array type
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ExpressionBuilder newArray(Expression... dimensions) {
        if (dimensions == null) {
            throw new IllegalArgumentException("dimensions must not be null"); //$NON-NLS-1$
        }
        return newArray(Arrays.asList(dimensions));
    }

    /**
     * Returns the array instance creation expression.
     * @param dimensions the number of array elements for each dimension
     * @return the chained expression builder
     * @throws IllegalStateException if the building type is not an array type
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ExpressionBuilder newArray(List<? extends Expression> dimensions) {
        if (dimensions == null) {
            throw new IllegalArgumentException("dimensions must not be null"); //$NON-NLS-1$
        }
        return expr(f.newArrayCreationExpression(toArrayType(), dimensions, null));
    }

    /**
     * Returns the array instance creation expression.
     * @param initializer the array initializer
     * @return the chained expression builder
     * @throws IllegalStateException if the building type is not an array type
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ExpressionBuilder newArray(ArrayInitializer initializer) {
        if (initializer == null) {
            throw new IllegalArgumentException("initializer must not be null"); //$NON-NLS-1$
        }
        return expr(f.newArrayCreationExpression(toArrayType(), Collections.emptyList(), initializer));
    }

    /**
     * Returns the class instance creation expression.
     * @param arguments the constructor arguments
     * @return the chained expression builder
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ExpressionBuilder newObject(Expression... arguments) {
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
        }
        return newObject(Arrays.asList(arguments), null);
    }

    /**
     * Returns the class instance creation expression.
     * @param arguments the constructor arguments
     * @return the chained expression builder
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ExpressionBuilder newObject(List<? extends Expression> arguments) {
        return newObject(arguments, null);
    }

    /**
     * Returns the class instance creation expression.
     * @param arguments the constructor arguments
     * @param anonymousClassBlock the anonymous class block (nullable)
     * @return the chained expression builder
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ExpressionBuilder newObject(List<? extends Expression> arguments, ClassBody anonymousClassBlock) {
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
        }
        return expr(f.newClassInstanceCreationExpression(
                null,
                Collections.emptyList(),
                context,
                arguments,
                anonymousClassBlock));
    }

    /**
     * Returns the field access expression.
     * @param name the target field name
     * @return the chained expression builder
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ExpressionBuilder field(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        return field(f.newSimpleName(name));
    }

    /**
     * Returns the field access expression.
     * @param name the target field name
     * @return the chained expression builder
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ExpressionBuilder field(SimpleName name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        return expr(f.newQualifiedName(toNamedType().getName(), name));
    }

    /**
     * Returns the method invocation using the building type as its qualifier.
     * @param name the target method name
     * @param arguments the method arguments
     * @return the chained expression builder
     * @throws IllegalStateException the building type is not a named type
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public ExpressionBuilder method(String name, Expression... arguments) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
        }
        return method(Collections.emptyList(), name, Arrays.asList(arguments));
    }

    /**
     * Returns the method invocation using the building type as its qualifier.
     * @param typeArguments the type arguments
     * @param name the target method name
     * @param arguments the method arguments
     * @return the chained expression builder
     * @throws IllegalStateException the building type is not a named type
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public ExpressionBuilder method(List<? extends Type> typeArguments, String name, Expression... arguments) {
        if (typeArguments == null) {
            throw new IllegalArgumentException("typeArguments must not be null"); //$NON-NLS-1$
        }
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
        }
        return method(typeArguments, name, Arrays.asList(arguments));
    }

    /**
     * Returns the method invocation using the building type as its qualifier.
     * @param name the target method name
     * @param arguments the method arguments
     * @return the chained expression builder
     * @throws IllegalStateException the building type is not a named type
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public ExpressionBuilder method(String name, List<? extends Expression> arguments) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
        }
        return method(Collections.emptyList(), name, arguments);
    }

    /**
     * Returns the method invocation using the building type as its qualifier.
     * @param typeArguments the type arguments
     * @param name the target method name
     * @param arguments the method arguments
     * @return the chained expression builder
     * @throws IllegalStateException the building type is not a named type
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public ExpressionBuilder method(
            List<? extends Type> typeArguments,
            String name,
            List<? extends Expression> arguments) {
        if (typeArguments == null) {
            throw new IllegalArgumentException("typeArguments must not be null"); //$NON-NLS-1$
        }
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
        }
        return method(typeArguments, f.newSimpleName(name), arguments);
    }

    /**
     * Returns the method invocation using the building type as its qualifier.
     * @param name the target method name
     * @param arguments the method arguments
     * @return the chained expression builder
     * @throws IllegalStateException the building type is not a named type
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public ExpressionBuilder method(SimpleName name, Expression... arguments) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
        }
        return method(Collections.emptyList(), name, Arrays.asList(arguments));
    }

    /**
     * Returns the method invocation using the building type as its qualifier.
     * @param typeArguments the type arguments
     * @param name the target method name
     * @param arguments the method arguments
     * @return the chained expression builder
     * @throws IllegalStateException the building type is not a named type
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public ExpressionBuilder method(List<? extends Type> typeArguments, SimpleName name, Expression... arguments) {
        if (typeArguments == null) {
            throw new IllegalArgumentException("typeArguments must not be null"); //$NON-NLS-1$
        }
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
        }
        return method(typeArguments, name, Arrays.asList(arguments));
    }

    /**
     * Returns the method invocation using the building type as its qualifier.
     * @param name the target method name
     * @param arguments the method arguments
     * @return the chained expression builder
     * @throws IllegalStateException the building type is not a named type
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public ExpressionBuilder method(SimpleName name, List<? extends Expression> arguments) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
        }
        return method(Collections.emptyList(), name, arguments);
    }

    /**
     * Returns the method invocation using the building type as its qualifier.
     * @param typeArguments the type arguments
     * @param name the target method name
     * @param arguments the method arguments
     * @return the chained expression builder
     * @throws IllegalStateException the building type is not a named type
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public ExpressionBuilder method(
            List<? extends Type> typeArguments,
            SimpleName name,
            List<? extends Expression> arguments) {
        if (typeArguments == null) {
            throw new IllegalArgumentException("typeArguments must not be null"); //$NON-NLS-1$
        }
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
        }
        return expr(f.newMethodInvocationExpression(toNamedType().getName(), typeArguments, name, arguments));
    }

    private TypeBuilder chain(Type type) {
        assert type != null;
        this.context = type;
        return this;
    }

    private ExpressionBuilder expr(Expression expression) {
        assert expression != null;
        return new ExpressionBuilder(f, expression);
    }
}
