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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.ExpressionStatement;
import com.asakusafw.utils.java.model.syntax.InfixOperator;
import com.asakusafw.utils.java.model.syntax.LocalVariableDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.PostfixOperator;
import com.asakusafw.utils.java.model.syntax.ReturnStatement;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.ThrowStatement;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.UnaryOperator;

/**
 * A builder for building Java expressions.
 */
public class ExpressionBuilder {

    private final ModelFactory f;

    private Expression context;

    /**
     * Creates a new instance.
     * @param factory the Java DOM factory
     * @param context the context expression
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public ExpressionBuilder(ModelFactory factory, Expression context) {
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
    public ExpressionBuilder copy() {
        return new ExpressionBuilder(f, context);
    }

    /**
     * Returns the built expression.
     * @return the built expression
     */
    public Expression toExpression() {
        return context;
    }

    /**
     * Returns the built expression as expression statement.
     * @return the built expression as expression statement
     */
    public ExpressionStatement toStatement() {
        return f.newExpressionStatement(toExpression());
    }

    /**
     * Returns a {@code throw} statement which throws the built expression.
     * @return a {@code throw} statement which throws the built expression
     */
    public ThrowStatement toThrowStatement() {
        return f.newThrowStatement(toExpression());
    }

    /**
     * Returns a {@code return} statement which passes the built expression.
     * @return a {@code return} statement which passes the built expression
     */
    public ReturnStatement toReturnStatement() {
        return f.newReturnStatement(toExpression());
    }

    /**
     * Returns a local variable declaration which is initialized by the built expression.
     * @param type the variable type
     * @param name the variable name
     * @return a local variable declaration which is initialized by the built expression
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public LocalVariableDeclaration toLocalVariableDeclaration(Type type, String name) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        return toLocalVariableDeclaration(type, f.newSimpleName(name));
    }

    /**
     * Returns a local variable declaration which is initialized by the built expression.
     * @param type the variable type
     * @param name the variable name
     * @return a local variable declaration which is initialized by the built expression
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public LocalVariableDeclaration toLocalVariableDeclaration(Type type, SimpleName name) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        return f.newLocalVariableDeclaration(type, name, context);
    }

    /**
     * Returns a binary expression which contains the building expression on the left term.
     * @param operator the infix operator
     * @param right the right term
     * @return this
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public ExpressionBuilder apply(InfixOperator operator, Expression right) {
        if (operator == null) {
            throw new IllegalArgumentException("operator must not be null"); //$NON-NLS-1$
        }
        if (right == null) {
            throw new IllegalArgumentException("right must not be null"); //$NON-NLS-1$
        }
        return chain(f.newInfixExpression(context, operator, right));
    }

    /**
     * Returns a unary expression.
     * @param operator the unary operator
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ExpressionBuilder apply(UnaryOperator operator) {
        if (operator == null) {
            throw new IllegalArgumentException("operator must not be null"); //$NON-NLS-1$
        }
        return chain(f.newUnaryExpression(operator, context));
    }

    /**
     * Returns a postfix expression.
     * @param operator the postfix operator
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ExpressionBuilder apply(PostfixOperator operator) {
        if (operator == null) {
            throw new IllegalArgumentException("operator must not be null"); //$NON-NLS-1$
        }
        return chain(f.newPostfixExpression(context, operator));
    }

    /**
     * Returns an assignment expression which contains the building expression on the left hand side.
     * @param rightHandSide the right hand side expression
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ExpressionBuilder assignFrom(Expression rightHandSide) {
        if (rightHandSide == null) {
            throw new IllegalArgumentException("rightHandSide must not be null"); //$NON-NLS-1$
        }
        return assignFrom(InfixOperator.ASSIGN, rightHandSide);
    }

    /**
     * Returns an assignment expression which contains the building expression on the left hand side.
     * @param operator infix operator for compound assignment
     * @param rightHandSide the right hand side expression
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ExpressionBuilder assignFrom(
            InfixOperator operator,
            Expression rightHandSide) {
        if (operator == null) {
            throw new IllegalArgumentException("operator must not be null"); //$NON-NLS-1$
        }
        if (rightHandSide == null) {
            throw new IllegalArgumentException("rightHandSide must not be null"); //$NON-NLS-1$
        }
        return chain(f.newAssignmentExpression(context, operator, rightHandSide));
    }

    /**
     * Returns a cast expression.
     * @param type the target type
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ExpressionBuilder castTo(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        return chain(f.newCastExpression(type, context));
    }

    /**
     * Returns a cast expression.
     * @param type the target type
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ExpressionBuilder castTo(java.lang.reflect.Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        return castTo(Models.toType(f, type));
    }

    /**
     * Returns an {@code instanceof} expression.
     * @param type the target type
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ExpressionBuilder instanceOf(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        return chain(f.newInstanceofExpression(context, type));
    }

    /**
     * Returns an {@code instanceof} expression.
     * @param type the target type
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ExpressionBuilder instanceOf(java.lang.reflect.Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        return instanceOf(Models.toType(f, type));
    }

    /**
     * Returns a field access expression which take the building expression as its owner object.
     * @param name the target field name
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ExpressionBuilder field(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        return field(f.newSimpleName(name));
    }

    /**
     * Returns a field access expression which take the building expression as its owner object.
     * @param name the target field name
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ExpressionBuilder field(SimpleName name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        return chain(f.newFieldAccessExpression(context, name));
    }

    /**
     * Returns an array access expression which contains the building expression as the target array.
     * @param index the index
     * @return this
     */
    public ExpressionBuilder array(int index) {
        return array(Models.toLiteral(f, index));
    }

    /**
     * Returns an array access expression which contains the building expression as the target array.
     * @param index the index variable name
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ExpressionBuilder array(String index) {
        if (index == null) {
            throw new IllegalArgumentException("index must not be null"); //$NON-NLS-1$
        }
        return array(Models.toName(f, index));
    }

    /**
     * Returns an array access expression which contains the building expression as the target array.
     * @param index the index expression
     * @return this
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public ExpressionBuilder array(Expression index) {
        if (index == null) {
            throw new IllegalArgumentException("index must not be null"); //$NON-NLS-1$
        }
        return chain(f.newArrayAccessExpression(context, index));
    }

    /**
     * Returns the method invocation which take the building expression as its receiver object.
     * @param name the target method name
     * @param arguments the method arguments
     * @return this
     * @throws IllegalArgumentException the parameters are {@code null}
     */
    public ExpressionBuilder method(
            String name,
            Expression... arguments) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
        }
        return method(Collections.<Type> emptyList(), name, Arrays.asList(arguments));
    }

    /**
     * Returns the method invocation which take the building expression as its receiver object.
     * @param typeArguments the type arguments
     * @param name the target method name
     * @param arguments the method arguments
     * @return this
     * @throws IllegalArgumentException the parameters are {@code null}
     */
    public ExpressionBuilder method(
            List<? extends Type> typeArguments,
            String name,
            Expression... arguments) {
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
     * Returns the method invocation which take the building expression as its receiver object.
     * @param name the target method name
     * @param arguments the method arguments
     * @return this
     * @throws IllegalArgumentException the parameters are {@code null}
     */
    public ExpressionBuilder method(
            String name,
            List<? extends Expression> arguments) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
        }
        return method(Collections.<Type> emptyList(), name, arguments);
    }

    /**
     * Returns the method invocation which take the building expression as its receiver object.
     * @param typeArguments the type arguments
     * @param name the target method name
     * @param arguments the method arguments
     * @return this
     * @throws IllegalArgumentException the parameters are {@code null}
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
     * Returns the method invocation which take the building expression as its receiver object.
     * @param name the target method name
     * @param arguments the method arguments
     * @return this
     * @throws IllegalArgumentException the parameters are {@code null}
     */
    public ExpressionBuilder method(
            SimpleName name,
            Expression... arguments) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
        }
        return method(Collections.<Type> emptyList(), name, Arrays.asList(arguments));
    }

    /**
     * Returns the method invocation which take the building expression as its receiver object.
     * @param typeArguments the type arguments
     * @param name the target method name
     * @param arguments the method arguments
     * @return this
     * @throws IllegalArgumentException the parameters are {@code null}
     */
    public ExpressionBuilder method(
            List<? extends Type> typeArguments,
            SimpleName name,
            Expression... arguments) {
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
     * Returns the method invocation which take the building expression as its receiver object.
     * @param name the target method name
     * @param arguments the method arguments
     * @return this
     * @throws IllegalArgumentException the parameters are {@code null}
     */
    public ExpressionBuilder method(
            SimpleName name,
            List<? extends Expression> arguments) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (arguments == null) {
            throw new IllegalArgumentException("arguments must not be null"); //$NON-NLS-1$
        }
        return method(Collections.<Type> emptyList(), name, arguments);
    }

    /**
     * Returns the method invocation which take the building expression as its receiver object.
     * @param typeArguments the type arguments
     * @param name the target method name
     * @param arguments the method arguments
     * @return this
     * @throws IllegalArgumentException the parameters are {@code null}
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
        return chain(f.newMethodInvocationExpression(context, typeArguments, name, arguments));
    }

    private ExpressionBuilder chain(Expression expression) {
        assert expression != null;
        context = expression;
        return this;
    }
}
