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
package com.asakusafw.utils.java.internal.model.util;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.RandomAccess;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.junit.After;
import org.junit.Test;

import com.asakusafw.utils.java.jsr199.testing.VolatileCompiler;
import com.asakusafw.utils.java.model.syntax.Annotation;
import com.asakusafw.utils.java.model.syntax.AnnotationDeclaration;
import com.asakusafw.utils.java.model.syntax.AnnotationElement;
import com.asakusafw.utils.java.model.syntax.Attribute;
import com.asakusafw.utils.java.model.syntax.BasicTypeKind;
import com.asakusafw.utils.java.model.syntax.CatchClause;
import com.asakusafw.utils.java.model.syntax.ClassDeclaration;
import com.asakusafw.utils.java.model.syntax.CompilationUnit;
import com.asakusafw.utils.java.model.syntax.EnumConstantDeclaration;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.ImportDeclaration;
import com.asakusafw.utils.java.model.syntax.ImportKind;
import com.asakusafw.utils.java.model.syntax.InfixOperator;
import com.asakusafw.utils.java.model.syntax.LocalVariableDeclaration;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Modifier;
import com.asakusafw.utils.java.model.syntax.ModifierKind;
import com.asakusafw.utils.java.model.syntax.NamedType;
import com.asakusafw.utils.java.model.syntax.PackageDeclaration;
import com.asakusafw.utils.java.model.syntax.PostfixOperator;
import com.asakusafw.utils.java.model.syntax.ReturnStatement;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.TryResource;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.syntax.TypeDeclaration;
import com.asakusafw.utils.java.model.syntax.TypeParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.UnaryOperator;
import com.asakusafw.utils.java.model.syntax.VariableDeclarator;
import com.asakusafw.utils.java.model.syntax.WildcardBoundKind;
import com.asakusafw.utils.java.model.util.CommentEmitTrait;
import com.asakusafw.utils.java.model.util.Models;

/**
 * Test for {@link ModelEmitter}.
 */
public class ModelEmitterTest {

    private final ModelFactory f = Models.getModelFactory();

    private PackageDeclaration packageDecl = null;

    private final List<ImportDeclaration> importDecls = new ArrayList<>();

    private final VolatileCompiler compiler = new VolatileCompiler();

    /**
     * disposes compiler.
     * @throws Exception if exception was occurred
     */
    @After
    public void tearDown() throws Exception {
        compiler.close();
    }

    /**
     * simple case.
     */
    @Test
    public void simple() {
        assertToString(
            fromExpr("Hello", f.newLiteral("\"Hello, world!\"")),
            "Hello",
            "Hello, world!");
    }

    /**
     * literal - string.
     */
    @Test
    public void Literal_String() {
        assertToString(
            fromExpr("Hello", f.newLiteral("\"Hello, world!\"")),
            "Hello",
            "Hello, world!");
    }

    /**
     * literal - integer.
     */
    @Test
    public void Literal_Int() {
        assertToString(
            fromExpr("Hello", f.newLiteral("150")),
            "Hello",
            "150");
    }

    /**
     * literal - float.
     */
    @Test
    public void Literal_Float() {
        assertToString(
            fromExpr("Hello", f.newLiteral("100.f")),
            "Hello",
            "100.0");
    }

    /**
     * literal - char.
     */
    @Test
    public void Literal_Char() {
        assertToString(
            fromExpr("Hello", f.newLiteral("'!'")),
            "Hello",
            "!");
    }

    /**
     * literal - boolean.
     */
    @Test
    public void Literal_Boolean() {
        assertToString(
            fromExpr("Hello", f.newLiteral("true")),
            "Hello",
            "true");
    }

    /**
     * void type.
     */
    @Test
    public void Type_void() {
        assertType(
            f.newBasicType(BasicTypeKind.VOID),
            void.class);
    }

    /**
     * primitive type.
     */
    @Test
    public void Type_primitive() {
        assertType(
            f.newBasicType(BasicTypeKind.INT),
            int.class);
    }

    /**
     * named type.
     */
    @Test
    public void Type_ClassOrInterface() {
        assertType(
            f.newNamedType(f.newSimpleName("Runnable")),
            Runnable.class);
    }

    /**
     * array type.
     */
    @Test
    public void Type_Array() {
        assertType(
            f.newArrayType(f.newBasicType(BasicTypeKind.INT)),
            int[].class);
    }

    /**
     * type variable.
     */
    @Test
    public void Type_variable() {
        java.lang.reflect.Type type = getType(
            f.newNamedType(f.newSimpleName("T")));

        assertThat(type, instanceOf(TypeVariable.class));
        TypeVariable<?> typeVar = (TypeVariable<?>) type;
        assertThat(typeVar.getName(), is("T"));
    }

    /**
     * parameterized type.
     */
    @Test
    public void Type_parameterized() {
        java.lang.reflect.Type type = getType(
            f.newParameterizedType(
                f.newNamedType(f.newSimpleName("Comparable")),
                Arrays.asList(new Type[] {
                    f.newNamedType(f.newSimpleName("String"))
                })));

        assertThat(type, instanceOf(java.lang.reflect.ParameterizedType.class));
        java.lang.reflect.ParameterizedType p = (java.lang.reflect.ParameterizedType) type;
        assertThat(p.getRawType(), is((java.lang.reflect.Type) Comparable.class));
        assertThat(p.getActualTypeArguments()[0],
            is((java.lang.reflect.Type) String.class));
    }

    /**
     * type wildcard.
     */
    @Test
    public void Type_wildcard() {
        java.lang.reflect.Type type = getType(
            f.newParameterizedType(
                f.newNamedType(f.newSimpleName("Comparable")),
                Arrays.asList(new Type[] {
                    f.newWildcard()
                })));

        assertThat(type, instanceOf(java.lang.reflect.ParameterizedType.class));
        java.lang.reflect.Type p = ((java.lang.reflect.ParameterizedType) type).getActualTypeArguments()[0];

        assertThat(p, instanceOf(WildcardType.class));
        WildcardType w = (WildcardType) p;

        assertThat(w.getLowerBounds().length, is(0));
        assertThat(w.getUpperBounds().length, is(1));
        assertThat(w.getUpperBounds()[0], is((java.lang.reflect.Type) Object.class));
    }

    /**
     * type wildcard w/ upper bound types.
     */
    @Test
    public void Type_wildcard_upperBounded() {
        java.lang.reflect.Type type = getType(
            f.newParameterizedType(
                f.newNamedType(f.newSimpleName("Comparable")),
                Arrays.asList(new Type[] {
                    f.newWildcard(
                        WildcardBoundKind.UPPER_BOUNDED,
                        f.newNamedType(f.newSimpleName("CharSequence")))
                })));

        assertThat(type, instanceOf(java.lang.reflect.ParameterizedType.class));
        java.lang.reflect.Type p = ((java.lang.reflect.ParameterizedType) type).getActualTypeArguments()[0];

        assertThat(p, instanceOf(WildcardType.class));
        WildcardType w = (WildcardType) p;

        assertThat(w.getLowerBounds().length, is(0));
        assertThat(w.getUpperBounds().length, is(1));
        assertThat(w.getUpperBounds()[0], is((java.lang.reflect.Type) CharSequence.class));
    }

    /**
     * type wildcard w/ lower bound types.
     */
    @Test
    public void Type_wildcard_lowerBounded() {
        java.lang.reflect.Type type = getType(
            f.newParameterizedType(
                f.newNamedType(f.newSimpleName("Comparable")),
                Arrays.asList(new Type[] {
                    f.newWildcard(
                        WildcardBoundKind.LOWER_BOUNDED,
                        f.newNamedType(f.newSimpleName("CharSequence")))
                })));

        assertThat(type, instanceOf(java.lang.reflect.ParameterizedType.class));
        java.lang.reflect.Type p = ((java.lang.reflect.ParameterizedType) type).getActualTypeArguments()[0];

        assertThat(p, instanceOf(WildcardType.class));
        WildcardType w = (WildcardType) p;

        assertThat(w.getLowerBounds().length, is(1));
        assertThat(w.getLowerBounds()[0], is((java.lang.reflect.Type) CharSequence.class));
        assertThat(w.getUpperBounds().length, is(1));
        assertThat(w.getUpperBounds()[0], is((java.lang.reflect.Type) Object.class));
    }

    /**
     * unary operator.
     */
    @Test
    public void Unary() {
        assertToString(
            fromExpr("Hello",
                f.newUnaryExpression(UnaryOperator.NOT, Models.toLiteral(f, true))),
            "Hello",
            "false");
    }

    /**
     * cast operator - int.
     */
    @Test
    public void Cast_Basic() {
        assertToString(
            fromExpr("Hello",
                f.newCastExpression(
                    f.newBasicType(BasicTypeKind.INT),
                    Models.toLiteral(f, 'A'))),
            "Hello",
            "65");
    }

    /**
     * cast operator - object.
     */
    @Test
    public void Cast_Reference() {
        assertToString(
            fromExpr("Hello",
                f.newCastExpression(
                    f.newNamedType(f.newSimpleName("Object")),
                    Models.toLiteral(f, 100))),
            "Hello",
            "100");
    }

    /**
     * infix operator.
     */
    @Test
    public void Infix() {
        assertToString(
            fromExpr("Hello",
                f.newInfixExpression(
                    Models.toLiteral(f, 10),
                    InfixOperator.PLUS,
                    Models.toLiteral(f, 20))),
            "Hello",
            "30");
    }

    /**
     * instance-of operator.
     */
    @Test
    public void Instanceof() {
        assertToString(
            fromExpr("Hello",
                f.newInstanceofExpression(
                    f.newCastExpression(
                        f.newNamedType(f.newSimpleName("Object")),
                        Models.toLiteral(f, "Hello")),
                    f.newNamedType(f.newSimpleName("String")))),
            "Hello",
            "true");
    }

    /**
     * conditional operator.
     */
    @Test
    public void Conditional() {
        assertToString(
            fromExpr("Hello",
                f.newConditionalExpression(
                    Models.toLiteral(f, false),
                    Models.toLiteral(f, 100),
                    Models.toLiteral(f, 200))),
            "Hello",
            "200");
    }

    /**
     * parenthesize.
     */
    @Test
    public void Parenthesize() {
        assertToString(
            fromExpr("Hello",
                f.newInfixExpression(
                    Models.toLiteral(f, 100),
                    InfixOperator.MINUS,
                    f.newParenthesizedExpression(
                        f.newInfixExpression(
                            Models.toLiteral(f, 50),
                            InfixOperator.MINUS,
                            Models.toLiteral(f, 100))))),
            "Hello",
            "150");
    }

    /**
     * expression name.
     */
    @Test
    public void ExpressionName() {
        assertToString(
            fromExpr("Hello",
                Models.toName(f, "Math.PI")),
            "Hello",
            String.valueOf(Math.PI));
    }

    /**
     * field access.
     */
    @Test
    public void FieldAccess() {
        assertToString(
            fromExpr("Hello",
                f.newFieldAccessExpression(
                    f.newClassInstanceCreationExpression(
                        null,
                        Collections.emptyList(),
                        f.newNamedType(Models.toName(f, "java.awt.Point")),
                        Arrays.asList(
                            Models.toLiteral(f, 100),
                            Models.toLiteral(f, 200)),
                        null),
                    f.newSimpleName("y"))),
            "Hello",
            "200");
    }

    /**
     * new array - 1 dimension.
     */
    @Test
    public void ArrayCreation_Single() {
        assertToString(
            fromExpr("Hello",
                f.newMethodInvocationExpression(
                    Models.toName(f, "java.util.Arrays"),
                    Collections.emptyList(),
                    f.newSimpleName("toString"),
                    Arrays.asList(
                        f.newArrayCreationExpression(
                            f.newArrayType(f.newBasicType(BasicTypeKind.INT)),
                            Arrays.asList(Models.toLiteral(f, 3)),
                            null)))),
            "Hello",
            "[0, 0, 0]");
    }

    /**
     * new array - multiple dimensions.
     */
    @Test
    public void ArrayCreation_Multi() {
        assertToString(
            fromExpr("Hello",
                f.newMethodInvocationExpression(
                    Models.toName(f, "java.util.Arrays"),
                    Collections.emptyList(),
                    f.newSimpleName("deepToString"),
                    Arrays.asList(
                        f.newArrayCreationExpression(
                            f.newArrayType(
                                f.newArrayType(f.newBasicType(BasicTypeKind.INT))),
                            Arrays.asList(Models.toLiteral(f, 3), Models.toLiteral(f, 2)),
                            null)))),
            "Hello",
            "[[0, 0], [0, 0], [0, 0]]");
    }

    /**
     * new array - multiple dimensions w/ lacked dimensions.
     */
    @Test
    public void ArrayCreation_MultiPartial() {
        assertToString(
            fromExpr("Hello",
                f.newMethodInvocationExpression(
                    Models.toName(f, "java.util.Arrays"),
                    Collections.emptyList(),
                    f.newSimpleName("deepToString"),
                    Arrays.asList(
                        f.newArrayCreationExpression(
                            f.newArrayType(
                                f.newArrayType(f.newBasicType(BasicTypeKind.INT))),
                            Arrays.asList(Models.toLiteral(f, 3)),
                            null)))),
            "Hello",
            "[null, null, null]");
    }

    /**
     * array initializer.
     */
    @Test
    public void ArrayInitializer() {
        assertToString(
            fromExpr("Hello",
                f.newMethodInvocationExpression(
                    Models.toName(f, "java.util.Arrays"),
                    Collections.emptyList(),
                    f.newSimpleName("toString"),
                    Arrays.asList(
                        f.newArrayCreationExpression(
                            f.newArrayType(f.newBasicType(BasicTypeKind.INT)),
                            Collections.emptyList(),
                            f.newArrayInitializer(Arrays.asList(
                                Models.toLiteral(f, 0),
                                Models.toLiteral(f, 1),
                                Models.toLiteral(f, 2))))))),
            "Hello",
            "[0, 1, 2]");
    }

    /**
     * array access.
     */
    @Test
    public void ArrayAccess() {
        assertToString(
            fromExpr("Hello",
                f.newArrayAccessExpression(
                    f.newParenthesizedExpression(
                        f.newArrayCreationExpression(
                            f.newArrayType(f.newBasicType(BasicTypeKind.INT)),
                            Collections.emptyList(),
                            f.newArrayInitializer(Arrays.asList(
                                Models.toLiteral(f, 100),
                                Models.toLiteral(f, 200),
                                Models.toLiteral(f, 300))))),
                    Models.toLiteral(f, 1))),
            "Hello",
            "200");
    }

    /**
     * type literal.
     */
    @Test
    public void ClassLiteral() {
        assertToString(
            fromExpr("Hello",
                f.newMethodInvocationExpression(
                    f.newClassLiteral(f.newNamedType(f.newSimpleName("Object"))),
                    Collections.emptyList(),
                    f.newSimpleName("getName"),
                    Collections.emptyList())),
            "Hello",
            "java.lang.Object");
    }

    /**
     * method invocation.
     */
    @Test
    public void MethodInvocation() {
        assertToString(
            fromExpr("Hello",
                f.newMethodInvocationExpression(
                        f.newLiteral("\"Hello, world!\""),
                        Collections.emptyList(),
                        f.newSimpleName("toString"),
                        Collections.emptyList())),
            "Hello",
            "Hello, world!");
    }

    /**
     * method invocation whose receiver object has cast.
     */
    @Test
    public void MethodInvocation_cast_object() {
        assertToString(
            fromExpr("Hello",
                f.newMethodInvocationExpression(
                        f.newCastExpression(
                                f.newNamedType(f.newSimpleName("Integer")),
                                f.newLiteral("1")),
                        Collections.emptyList(),
                        f.newSimpleName("toString"),
                        Collections.emptyList())),
            "Hello",
            "1");
    }

    /**
     * constructor reference.
     */
    @Test
    public void ConstructorReference_type() {
        assertToString(
            fromExpr("Hello",
                // ((UnaryOperator<String>) String::new).apply("Hello, world!")
                f.newMethodInvocationExpression(
                        f.newCastExpression(
                                f.newParameterizedType(
                                        Models.toType(f, java.util.function.UnaryOperator.class),
                                        Models.toType(f, String.class)),
                                f.newConstructorReferenceExpression(
                                        Models.toType(f, String.class))),
                        f.newSimpleName("apply"),
                        f.newLiteral("\"Hello, world!\""))),
            "Hello",
            "Hello, world!");
    }

    /**
     * method reference.
     */
    @Test
    public void MethodReference_type() {
        assertToString(
            fromExpr("Hello",
                // ((UnaryOperator<String>) String::toString).apply("Hello, world!")
                f.newMethodInvocationExpression(
                        f.newCastExpression(
                                f.newParameterizedType(
                                        Models.toType(f, java.util.function.UnaryOperator.class),
                                        Models.toType(f, String.class)),
                                f.newMethodReferenceExpression(
                                        Models.toType(f, String.class),
                                        f.newSimpleName("toString"))),
                        f.newSimpleName("apply"),
                        f.newLiteral("\"Hello, world!\""))),
            "Hello",
            "Hello, world!");
    }

    /**
     * method reference.
     */
    @Test
    public void MethodReference_expression() {
        assertToString(
            fromExpr("Hello",
                // ((Supplier<String>) "Hello, world!"::toString).get()
                f.newMethodInvocationExpression(
                        f.newCastExpression(
                                f.newParameterizedType(
                                        Models.toType(f, java.util.function.Supplier.class),
                                        Models.toType(f, String.class)),
                                f.newMethodReferenceExpression(
                                        f.newLiteral("\"Hello, world!\""),
                                        f.newSimpleName("toString"))),
                        f.newSimpleName("get"))),
            "Hello",
            "Hello, world!");
    }

    /**
     * new object.
     */
    @Test
    public void ClassInstanceCreation() {
        assertToString(
            fromExpr("Hello",
                f.newClassInstanceCreationExpression(
                    null,
                    Collections.emptyList(),
                    f.newNamedType(f.newSimpleName("String")),
                    Arrays.asList(Models.toLiteral(f, "Hello, world!")),
                    null)),
            "Hello",
            "Hello, world!");
    }

    /**
     * new object w/ anonymous class block.
     */
    @Test
    public void ClassInstanceCreation_Anonymous() {
        assertToString(
                fromExpr("Hello",
                    f.newClassInstanceCreationExpression(
                        null,
                        Collections.emptyList(),
                        f.newNamedType(f.newSimpleName("Object")),
                        Collections.emptyList(),
                        f.newClassBody(Arrays.asList(new TypeBodyDeclaration[] {
                            toString(f.newReturnStatement(Models.toLiteral(f, "Anon")))
                        })))),
                "Hello",
                "Anon");
    }

    /**
     * lambda expression w/ empty parameters.
     */
    @Test
    public void LambdaExpression() {
        assertToString(
                fromExpr("Hello",
                    f.newMethodInvocationExpression(
                            f.newCastExpression(
                                    f.newParameterizedType(
                                            Models.toType(f, Supplier.class),
                                            Models.toType(f, String.class)),
                                    f.newLambdaExpression(Models.toLiteral(f, "LAMBDA"))),
                            f.newSimpleName("get"),
                            Arrays.asList())),
                "Hello",
                "LAMBDA");
    }

    /**
     * lambda expression w/ block body.
     */
    @Test
    public void LambdaExpression_block() {
        assertToString(
                fromExpr("Hello",
                    f.newMethodInvocationExpression(
                            f.newCastExpression(
                                    f.newParameterizedType(
                                            Models.toType(f, Supplier.class),
                                            Models.toType(f, String.class)),
                                    f.newLambdaExpression(f.newBlock(f.newReturnStatement(
                                            Models.toLiteral(f, "LAMBDA"))))),
                            f.newSimpleName("get"),
                            Arrays.asList())),
                "Hello",
                "LAMBDA");
    }

    /**
     * lambda expression w/ single simple parameter.
     */
    @Test
    public void LambdaExpression_single() {
        assertToString(
                fromExpr("Hello",
                    f.newMethodInvocationExpression(
                            f.newCastExpression(
                                    f.newParameterizedType(
                                            Models.toType(f, Function.class),
                                            Models.toType(f, String.class),
                                            Models.toType(f, String.class)),
                                    f.newLambdaExpression(f.newSimpleName("e"), e -> e)),
                            f.newSimpleName("apply"),
                            Arrays.asList(Models.toLiteral(f, "LAMBDA")))),
                "Hello",
                "LAMBDA");
    }

    /**
     * lambda expression w/ formal parameters.
     */
    @Test
    public void LambdaExpression_formal() {
        assertToString(
                fromExpr("Hello",
                    f.newMethodInvocationExpression(
                            f.newCastExpression(
                                    f.newParameterizedType(
                                            Models.toType(f, BiFunction.class),
                                            Models.toType(f, String.class),
                                            Models.toType(f, Integer.class),
                                            Models.toType(f, String.class)),
                                    f.newLambdaExpression(
                                            Arrays.asList(new FormalParameterDeclaration[] {
                                                    f.newFormalParameterDeclaration(
                                                            Models.toType(f, String.class), f.newSimpleName("a")),
                                                    f.newFormalParameterDeclaration(
                                                            Models.toType(f, Integer.class), f.newSimpleName("b")),
                                            }),
                                            ps -> f.newInfixExpression(ps.get(0), InfixOperator.PLUS, ps.get(1)))),
                            f.newSimpleName("apply"),
                            Arrays.asList(Models.toLiteral(f, "LAMBDA"), Models.toLiteral(f, 100)))),
                "Hello",
                "LAMBDA100");
    }

    /**
     * assert statement.
     */
    @Test
    public void Assert() {
        assertRaise(
            fromStmt("Hello",
                f.newAssertStatement(Models.toLiteral(f, false), null),
                returnAsString(Models.toLiteral(f, "ERROR"))),
            "Hello",
            AssertionError.class);
    }

    /**
     * assert statement w/ message.
     */
    @Test
    public void Assert_WithMessage() {
        assertRaise(
            fromStmt("Hello",
                f.newAssertStatement(
                    Models.toLiteral(f, false),
                    Models.toLiteral(f, "OK")),
                returnAsString(Models.toLiteral(f, "ERROR"))),
            "Hello",
            AssertionError.class);
    }

    /**
     * local variable declaration.
     */
    @Test
    public void LocalVariable() {
        assertToString(
            fromStmt("Hello",
                f.newLocalVariableDeclaration(
                    Collections.emptyList(),
                    f.newBasicType(BasicTypeKind.INT),
                    Arrays.asList(new VariableDeclarator[] {
                        f.newVariableDeclarator(
                            f.newSimpleName("a"),
                            0,
                            Models.toLiteral(f, 100))
                    })),
                returnAsString(f.newSimpleName("a"))),
            "Hello",
            "100");
    }

    /**
     * local variable declaration w/ multiple declarators.
     */
    @Test
    public void LocalVariable_Multiple() {
        assertToString(
            fromStmt("Hello",
                f.newLocalVariableDeclaration(
                    Collections.emptyList(),
                    f.newBasicType(BasicTypeKind.INT),
                    Arrays.asList(new VariableDeclarator[] {
                        f.newVariableDeclarator(
                            f.newSimpleName("a"),
                            0,
                            Models.toLiteral(f, 100)),
                        f.newVariableDeclarator(
                            f.newSimpleName("b"),
                            0,
                            Models.toLiteral(f, 20)),
                        f.newVariableDeclarator(
                            f.newSimpleName("c"),
                            0,
                            Models.toLiteral(f, 30))
                    })),
                returnAsString(
                    f.newInfixExpression(
                        f.newSimpleName("a"),
                        InfixOperator.PLUS,
                        f.newInfixExpression(
                            f.newSimpleName("b"),
                            InfixOperator.TIMES,
                            f.newSimpleName("c"))))),
            "Hello",
            "700");
    }

    /**
     * assignment expression.
     */
    @Test
    public void Assignment() {
        assertToString(
            fromStmt("Hello",
                f.newLocalVariableDeclaration(
                    Collections.emptyList(),
                    f.newBasicType(BasicTypeKind.INT),
                    Arrays.asList(new VariableDeclarator[] {
                        f.newVariableDeclarator(
                            f.newSimpleName("a"),
                            0,
                            null)
                    })),
                f.newExpressionStatement(
                    f.newAssignmentExpression(
                        f.newSimpleName("a"),
                        InfixOperator.ASSIGN,
                        Models.toLiteral(f, 200))),
                returnAsString(f.newSimpleName("a"))),
            "Hello",
            "200");
    }

    /**
     * empty statement.
     */
    @Test
    public void Empty() {
        assertToString(
            fromStmt("Hello",
                f.newEmptyStatement(),
                returnAsString(Models.toLiteral(f, 100))),
            "Hello",
            "100");
    }

    /**
     * if statement - true.
     */
    @Test
    public void If_true() {
        assertToString(
            fromStmt("Hello",
                f.newIfStatement(
                    Models.toLiteral(f, true),
                    returnAsString(Models.toLiteral(f, 100)),
                    null),
                returnAsString(Models.toLiteral(f, 200))),
            "Hello",
            "100");
    }

    /**
     * if statement - false.
     */
    @Test
    public void If_false() {
        assertToString(
            fromStmt("Hello",
                f.newIfStatement(
                    Models.toLiteral(f, false),
                    returnAsString(Models.toLiteral(f, 100)),
                    null),
                returnAsString(Models.toLiteral(f, 200))),
            "Hello",
            "200");
    }

    /**
     * if-else statement - false.
     */
    @Test
    public void If_else() {
        assertToString(
            fromStmt("Hello",
                f.newIfStatement(
                    Models.toLiteral(f, false),
                    returnAsString(Models.toLiteral(f, 100)),
                    returnAsString(Models.toLiteral(f, 200)))),
            "Hello",
            "200");
    }

    /**
     * if-else statement - false.
     */
    @Test
    public void If_elseIf() {
        assertToString(
            fromStmt("Hello",
                f.newIfStatement(
                    Models.toLiteral(f, false),
                    returnAsString(Models.toLiteral(f, 100)),
                    f.newIfStatement(
                        Models.toLiteral(f, false),
                        returnAsString(Models.toLiteral(f, 200)),
                        returnAsString(Models.toLiteral(f, 300))))),
            "Hello",
            "300");
    }

    /**
     * while statement.
     */
    @Test
    public void While() {
        assertToString(
            fromStmt("Hello",
                newStringBuilder("buf"),
                var(f.newBasicType(BasicTypeKind.INT), "a", Models.toLiteral(f, 5)),
                f.newWhileStatement(
                    f.newInfixExpression(
                        f.newSimpleName("a"),
                        InfixOperator.GREATER_EQUALS,
                        Models.toLiteral(f, 0)),
                    f.newBlock(Arrays.asList(new Statement[] {
                        append("buf",
                            f.newPostfixExpression(
                                f.newSimpleName("a"),
                                PostfixOperator.DECREMENT)),
                    }))),
                returnAsString(f.newSimpleName("buf"))),
            "Hello",
            "543210");
    }

    /**
     * for statement w/ variable declaration.
     */
    @Test
    public void For() {
        assertToString(
            fromStmt("Hello",
                newStringBuilder("buf"),
                f.newForStatement(
                    var(f.newBasicType(BasicTypeKind.INT), "i", Models.toLiteral(f, 0)),
                    f.newInfixExpression(
                        f.newSimpleName("i"),
                        InfixOperator.LESS,
                        Models.toLiteral(f, 5)),
                    f.newStatementExpressionList(Arrays.asList(
                        f.newPostfixExpression(
                            f.newSimpleName("i"),
                            PostfixOperator.INCREMENT))),
                    append("buf", f.newSimpleName("i"))),
                returnAsString(f.newSimpleName("buf"))),
            "Hello",
            "01234");
    }

    /**
     * for statement w/o variable declarations.
     */
    @Test
    public void For_noVar() {
        assertToString(
            fromStmt("Hello",
                newStringBuilder("buf"),
                var(f.newBasicType(BasicTypeKind.INT), "i", null),
                f.newForStatement(
                    f.newStatementExpressionList(Arrays.asList(
                        f.newAssignmentExpression(
                            f.newSimpleName("i"),
                            InfixOperator.ASSIGN,
                            Models.toLiteral(f, 5)))),
                    f.newInfixExpression(
                        f.newSimpleName("i"),
                        InfixOperator.GREATER,
                        Models.toLiteral(f, 0)),
                    f.newStatementExpressionList(Arrays.asList(
                        f.newPostfixExpression(
                            f.newSimpleName("i"),
                            PostfixOperator.DECREMENT))),
                    append("buf", f.newSimpleName("i"))),
                returnAsString(f.newSimpleName("buf"))),
            "Hello",
            "54321");
    }

    /**
     * enhanced for statement.
     */
    @Test
    public void ForEach() {
        assertToString(
            fromStmt("Hello",
                newStringBuilder("buf"),
                f.newEnhancedForStatement(
                    f.newFormalParameterDeclaration(
                        Collections.emptyList(),
                        f.newBasicType(BasicTypeKind.INT),
                        false,
                        f.newSimpleName("elem"),
                        0),
                    f.newArrayCreationExpression(
                        f.newArrayType(f.newBasicType(BasicTypeKind.INT)),
                        Collections.emptyList(),
                        f.newArrayInitializer(Arrays.asList(new Expression[] {
                            Models.toLiteral(f, 10),
                            Models.toLiteral(f, 20),
                            Models.toLiteral(f, 30),
                        }))),
                    append("buf", f.newSimpleName("elem"))),
                returnAsString(f.newSimpleName("buf"))),
            "Hello",
            "102030");
    }

    /**
     * do-while statement.
     */
    @Test
    public void DoWhile() {
        assertToString(
            fromStmt("Hello",
                newStringBuilder("buf"),
                var(f.newBasicType(BasicTypeKind.INT), "a", Models.toLiteral(f, 5)),
                f.newDoStatement(
                    f.newBlock(Arrays.asList(new Statement[] {
                        append("buf", f.newSimpleName("a")),
                    })),
                    f.newInfixExpression(
                        f.newPostfixExpression(
                            f.newSimpleName("a"),
                            PostfixOperator.DECREMENT),
                        InfixOperator.GREATER_EQUALS,
                        Models.toLiteral(f, 0))),
                returnAsString(f.newSimpleName("buf"))),
            "Hello",
            "543210-1");
    }

    /**
     * break statement.
     */
    @Test
    public void Break() {
        assertToString(
            fromStmt("Hello",
                newStringBuilder("buf"),
                var(f.newBasicType(BasicTypeKind.INT), "a", Models.toLiteral(f, 5)),
                f.newWhileStatement(
                    f.newInfixExpression(
                        f.newSimpleName("a"),
                        InfixOperator.GREATER_EQUALS,
                        Models.toLiteral(f, 0)),
                    f.newBlock(Arrays.asList(new Statement[] {
                        append("buf",
                            f.newPostfixExpression(
                                f.newSimpleName("a"),
                                PostfixOperator.DECREMENT)),
                        f.newBreakStatement(null),
                    }))),
                returnAsString(f.newSimpleName("buf"))),
            "Hello",
            "5");
    }

    /**
     * break statement w/ label.
     */
    @Test
    public void Break_Labeled() {
        assertToString(
            fromStmt("Hello",
                newStringBuilder("buf"),
                var(f.newBasicType(BasicTypeKind.INT), "a", Models.toLiteral(f, 5)),
                f.newLabeledStatement(f.newSimpleName("LABEL"), f.newWhileStatement(
                    Models.toLiteral(f, true),
                    f.newWhileStatement(
                        f.newInfixExpression(
                            f.newSimpleName("a"),
                            InfixOperator.GREATER_EQUALS,
                            Models.toLiteral(f, 0)),
                        f.newBlock(Arrays.asList(new Statement[] {
                            append("buf",
                                f.newPostfixExpression(
                                    f.newSimpleName("a"),
                                    PostfixOperator.DECREMENT)),
                            f.newBreakStatement(f.newSimpleName("LABEL")),
                        }))))),
                returnAsString(f.newSimpleName("buf"))),
            "Hello",
            "5");
    }

    /**
     * continue statement.
     */
    @Test
    public void Continue() {
        assertToString(
            fromStmt("Hello",
                newStringBuilder("buf"),
                f.newForStatement(
                    var(f.newBasicType(BasicTypeKind.INT), "i", Models.toLiteral(f, 0)),
                    f.newInfixExpression(
                        f.newSimpleName("i"),
                        InfixOperator.LESS,
                        Models.toLiteral(f, 5)),
                    f.newStatementExpressionList(Arrays.asList(
                        f.newPostfixExpression(
                            f.newSimpleName("i"),
                            PostfixOperator.INCREMENT))),
                    f.newBlock(Arrays.asList(new Statement[] {
                        f.newIfStatement(
                            f.newInfixExpression(
                                f.newSimpleName("i"),
                                InfixOperator.GREATER,
                                Models.toLiteral(f, 3)),
                            f.newContinueStatement(null),
                            null),
                        append("buf", f.newSimpleName("i"))
                    }))),
                returnAsString(f.newSimpleName("buf"))),
            "Hello",
            "0123");
    }

    /**
     * continue statement.
     */
    @Test
    public void Continue_Labeled() {
        assertToString(
            fromStmt("Hello",
                newStringBuilder("buf"),
                f.newLabeledStatement(f.newSimpleName("OUT"), f.newForStatement(
                    var(f.newBasicType(BasicTypeKind.INT), "o", Models.toLiteral(f, 0)),
                    f.newInfixExpression(
                        f.newSimpleName("o"),
                        InfixOperator.LESS,
                        Models.toLiteral(f, 3)),
                    f.newStatementExpressionList(Arrays.asList(
                        f.newPostfixExpression(
                            f.newSimpleName("o"),
                            PostfixOperator.INCREMENT))),
                    f.newForStatement(
                        var(f.newBasicType(BasicTypeKind.INT), "i", Models.toLiteral(f, 0)),
                        f.newInfixExpression(
                            f.newSimpleName("i"),
                            InfixOperator.LESS,
                            Models.toLiteral(f, 5)),
                        f.newStatementExpressionList(Arrays.asList(
                            f.newPostfixExpression(
                                f.newSimpleName("i"),
                                PostfixOperator.INCREMENT))),
                        f.newBlock(Arrays.asList(new Statement[] {
                            f.newIfStatement(
                                f.newInfixExpression(
                                    f.newSimpleName("i"),
                                    InfixOperator.GREATER,
                                    Models.toLiteral(f, 3)),
                                f.newContinueStatement(f.newSimpleName("OUT")),
                                null),
                            append("buf", f.newSimpleName("o")),
                            append("buf", f.newSimpleName("i")),
                        }))))),
                returnAsString(f.newSimpleName("buf"))),
            "Hello",
            "000102031011121320212223");
    }

    /**
     * switch-case statement.
     */
    @Test
    public void Switch_Case() {
        assertToString(
            fromStmt("Hello",
                f.newSwitchStatement(
                    Models.toLiteral(f, 2),
                    Arrays.asList(new Statement[] {
                        f.newSwitchCaseLabel(Models.toLiteral(f, 1)),
                        returnAsString(Models.toLiteral(f, "a")),
                        f.newSwitchCaseLabel(Models.toLiteral(f, 2)),
                        returnAsString(Models.toLiteral(f, "b")),
                        f.newSwitchDefaultLabel(),
                        returnAsString(Models.toLiteral(f, "c")),
                    }))),
            "Hello",
            "b");
    }

    /**
     * switch-case statement.
     */
    @Test
    public void Switch_Default() {
        assertToString(
            fromStmt("Hello",
                f.newSwitchStatement(
                    Models.toLiteral(f, 4),
                    Arrays.asList(new Statement[] {
                        f.newSwitchCaseLabel(Models.toLiteral(f, 1)),
                        returnAsString(Models.toLiteral(f, "a")),
                        f.newSwitchCaseLabel(Models.toLiteral(f, 2)),
                        returnAsString(Models.toLiteral(f, "b")),
                        f.newSwitchDefaultLabel(),
                        returnAsString(Models.toLiteral(f, "c")),
                    }))),
            "Hello",
            "c");
    }

    /**
     * switch-case statement.
     */
    @Test
    public void Throw() {
        assertRaise(
            fromStmt("Hello",
                f.newThrowStatement(
                    f.newClassInstanceCreationExpression(
                        null,
                        Collections.emptyList(),
                        f.newNamedType(Models.toName(f, "UnsupportedOperationException")),
                        Collections.emptyList(),
                        null))),
            "Hello",
            UnsupportedOperationException.class);
    }

    /**
     * try-catch statement.
     */
    @Test
    public void Try_Catch() {
        assertToString(
            fromStmt("Hello",
                newStringBuilder("buf"),
                f.newTryStatement(
                    f.newBlock(Arrays.asList(new Statement[] {
                        f.newThrowStatement(
                            f.newClassInstanceCreationExpression(
                                null,
                                Collections.emptyList(),
                                f.newNamedType(Models.toName(f, "UnsupportedOperationException")),
                                Arrays.asList(new Expression[] {
                                    Models.toLiteral(f, "OK")
                                }),
                                null))
                    })),
                    Arrays.asList(new CatchClause[] {
                        f.newCatchClause(
                            f.newFormalParameterDeclaration(
                                Collections.emptyList(),
                                f.newNamedType(f.newSimpleName("Error")),
                                false,
                                f.newSimpleName("e"),
                                0),
                            f.newBlock(Arrays.asList(new Statement[] {
                            }))),
                        f.newCatchClause(
                            f.newFormalParameterDeclaration(
                                Collections.emptyList(),
                                f.newNamedType(f.newSimpleName("UnsupportedOperationException")),
                                false,
                                f.newSimpleName("e"),
                                0),
                            f.newBlock(Arrays.asList(new Statement[] {
                                append(
                                "buf",
                                f.newMethodInvocationExpression(
                                    f.newSimpleName("e"),
                                    Collections.emptyList(),
                                    f.newSimpleName("getMessage"),
                                    Collections.emptyList()))
                            })))
                    })),
                returnAsString(f.newSimpleName("buf"))),
            "Hello",
            "OK");
    }

    /**
     * try with multi-catch.
     */
    @Test
    public void Try_Catch_multi() {
        assertToString(
            fromStmt("Hello",
                newStringBuilder("buf"),
                f.newTryStatement(
                    f.newBlock(Arrays.asList(new Statement[] {
                        f.newThrowStatement(
                            f.newClassInstanceCreationExpression(
                                null,
                                Collections.emptyList(),
                                f.newNamedType(Models.toName(f, "UnsupportedOperationException")),
                                Arrays.asList(new Expression[] {
                                    Models.toLiteral(f, "OK")
                                }),
                                null))
                    })),
                    Arrays.asList(new CatchClause[] {
                        f.newCatchClause(
                            f.newFormalParameterDeclaration(
                                Collections.emptyList(),
                                f.newUnionType(new Type[] {
                                        f.newNamedType(f.newSimpleName("IllegalArgumentException")),
                                        f.newNamedType(f.newSimpleName("UnsupportedOperationException")),
                                }),
                                false,
                                f.newSimpleName("e"),
                                0),
                            f.newBlock(Arrays.asList(new Statement[] {
                                append(
                                "buf",
                                f.newMethodInvocationExpression(
                                    f.newSimpleName("e"),
                                    Collections.emptyList(),
                                    f.newSimpleName("getMessage"),
                                    Collections.emptyList()))
                            })))
                    })),
                returnAsString(f.newSimpleName("buf"))),
            "Hello",
            "OK");
    }

    /**
     * try-finally statement.
     */
    @Test
    public void Try_Finally() {
        assertToString(
            fromStmt("Hello",
                newStringBuilder("buf"),
                f.newTryStatement(
                    f.newBlock(Arrays.asList(new Statement[] {
                        f.newThrowStatement(
                            f.newClassInstanceCreationExpression(
                                null,
                                Collections.emptyList(),
                                f.newNamedType(Models.toName(f, "UnsupportedOperationException")),
                                Arrays.asList(new Expression[] {
                                    Models.toLiteral(f, "OK")
                                }),
                                null))
                    })),
                    Arrays.asList(new CatchClause[] {
                        f.newCatchClause(
                            f.newFormalParameterDeclaration(
                                Collections.emptyList(),
                                f.newNamedType(f.newSimpleName("UnsupportedOperationException")),
                                false,
                                f.newSimpleName("e"),
                                0),
                            f.newBlock(Arrays.asList(new Statement[] {
                                append(
                                "buf",
                                f.newMethodInvocationExpression(
                                    f.newSimpleName("e"),
                                    Collections.emptyList(),
                                    f.newSimpleName("getMessage"),
                                    Collections.emptyList()))
                            })))
                    }),
                    f.newBlock(Arrays.asList(new Statement[] {
                        append("buf", Models.toLiteral(f, "fin"))
                    }))),
                returnAsString(f.newSimpleName("buf"))),
            "Hello",
            "OKfin");
    }

    /**
     * try with resources.
     */
    @Test
    public void Try_with_resources() {
        assertToString(
            fromStmt("Hello",
                f.newTryStatement(
                    Arrays.asList(f.newTryResource(
                        Models.toType(f, ByteArrayOutputStream.class),
                        f.newSimpleName("out"),
                        f.newClassInstanceCreationExpression(Models.toType(f, ByteArrayOutputStream.class)))),
                    f.newBlock(f.newReturnStatement(Models.toLiteral(f, "OK"))),
                    Arrays.asList(f.newCatchClause(
                            Models.toType(f, IOException.class),
                            f.newSimpleName("e"),
                            e -> f.newBlock(f.newReturnStatement(Models.toLiteral(f, "NG"))))))),
            "Hello",
            "OK");
    }

    /**
     * try with resources.
     */
    @Test
    public void Try_with_resources_multiple() {
        assertToString(
                fromStmt("Hello",
                    f.newTryStatement(
                        Arrays.asList(new TryResource[] {
                            f.newTryResource(
                                Models.toType(f, ByteArrayOutputStream.class),
                                f.newSimpleName("a"),
                                f.newClassInstanceCreationExpression(Models.toType(f, ByteArrayOutputStream.class))),
                            f.newTryResource(
                                Models.toType(f, ByteArrayOutputStream.class),
                                f.newSimpleName("b"),
                                f.newClassInstanceCreationExpression(Models.toType(f, ByteArrayOutputStream.class))),
                            f.newTryResource(
                                Models.toType(f, ByteArrayOutputStream.class),
                                f.newSimpleName("c"),
                                f.newClassInstanceCreationExpression(Models.toType(f, ByteArrayOutputStream.class))),
                        }),
                        f.newBlock(f.newReturnStatement(Models.toLiteral(f, "OK"))),
                        Arrays.asList(f.newCatchClause(
                            Models.toType(f, IOException.class),
                            f.newSimpleName("e"),
                            e -> f.newBlock(f.newReturnStatement(Models.toLiteral(f, "NG"))))))),
                "Hello",
                "OK");
    }

    /**
     * synchronized statement.
     */
    @Test
    public void Synchronized() {
        assertToString(
            fromStmt("Hello",
                newStringBuilder("buf"),
                f.newSynchronizedStatement(
                    f.newThis(),
                    f.newBlock(Arrays.asList(new Statement[] {
                        f.newExpressionStatement(f.newMethodInvocationExpression(
                            f.newThis(),
                            Collections.emptyList(),
                            f.newSimpleName("notify"),
                            Collections.emptyList())),
                    }))),
                append("buf", Models.toLiteral(f, "OK")),
                returnAsString(f.newSimpleName("buf"))),
            "Hello",
            "OK");
    }

    /**
     * class declaration.
     */
    @Test
    public void ClassDeclaration() {
        Class<?> klass = getTypeDeclaration(
            f.newClassDeclaration(
                null,
                Collections.emptyList(),
                f.newSimpleName("Testing"),
                Collections.emptyList(),
                null,
                Collections.emptyList(),
                Collections.emptyList()));
        assertThat(klass.getAnnotations().length, is(0));
        assertThat(klass.getName(), equalTo("Testing"));
        assertThat(klass.getTypeParameters().length, is(0));
        assertThat(klass.getSuperclass(), is((Object) Object.class));
        assertThat(klass.getInterfaces().length, is(0));
    }

    /**
     * class declaration w/ modifiers.
     */
    @Test
    public void ClassDeclaration_modifiers() {
        Class<?> klass = getTypeDeclaration(
            f.newClassDeclaration(
                null,
                Arrays.asList(new Attribute[] {
                    f.newModifier(ModifierKind.PUBLIC),
                    f.newModifier(ModifierKind.ABSTRACT),
                }),
                f.newSimpleName("Testing"),
                Collections.emptyList(),
                null,
                Collections.emptyList(),
                Collections.emptyList()));

        assertThat(java.lang.reflect.Modifier.isPublic(klass.getModifiers()),
                is(true));
        assertThat(java.lang.reflect.Modifier.isAbstract(klass.getModifiers()),
                is(true));
    }

    /**
     * class declaration w/ marker annotation.
     */
    @Test
    public void ClassDeclaration_markerAnnotation() {
        Class<?> klass = getTypeDeclaration(
            f.newClassDeclaration(
                null,
                Arrays.asList(new Attribute[] {
                    f.newMarkerAnnotation(
                        (NamedType) Models.toType(f, Deprecated.class))
                }),
                f.newSimpleName("Testing"),
                Collections.emptyList(),
                null,
                Collections.emptyList(),
                Collections.emptyList()));

        assertThat(klass.getAnnotation(Deprecated.class),
                not(nullValue()));
    }

    /**
     * class declaration - single element annotation.
     */
    @Test
    public void ClassDeclaration_singleElementAnnotation() {
        Class<?> klass = getTypeDeclaration(
            f.newClassDeclaration(
                null,
                Arrays.asList(new Attribute[] {
                    f.newSingleElementAnnotation(
                        f.newNamedType(f.newSimpleName("An")),
                        f.newArrayInitializer(Arrays.asList(new Expression[] {
                            Models.toLiteral(f, "a"),
                            Models.toLiteral(f, "b"),
                        })))
                }),
                f.newSimpleName("Testing"),
                Collections.emptyList(),
                null,
                Collections.emptyList(),
                Collections.emptyList()),
            createAnnotation("An"));

        String[] value = (String[]) getAnnotationValue(klass, "An", "value");
        assertThat(value.length, is(2));
        assertThat(value[0], is("a"));
        assertThat(value[1], is("b"));

        Integer option = (Integer) getAnnotationValue(klass, "An", "option");
        assertThat(option, is(100));
    }

    /**
     * class declaration w/ normal annotation.
     */
    @Test
    public void ClassDeclaration_normalAnnotation() {
        Class<?> klass = getTypeDeclaration(
            f.newClassDeclaration(
                null,
                Arrays.asList(new Attribute[] {
                    f.newNormalAnnotation(
                        f.newNamedType(f.newSimpleName("An")),
                        Arrays.asList(new AnnotationElement[] {
                            f.newAnnotationElement(
                                f.newSimpleName("value"),
                                Models.toLiteral(f, "a")),
                            f.newAnnotationElement(
                                f.newSimpleName("option"),
                                Models.toLiteral(f, 500)),
                        }))
                }),
                f.newSimpleName("Testing"),
                Collections.emptyList(),
                null,
                Collections.emptyList(),
                Collections.emptyList()),
            createAnnotation("An"));

        String[] value = (String[]) getAnnotationValue(klass, "An", "value");
        assertThat(value.length, is(1));
        assertThat(value[0], is("a"));

        Integer option = (Integer) getAnnotationValue(klass, "An", "option");
        assertThat(option, is(500));
    }

    /**
     * class declaration w/ type parameters.
     */
    @Test
    public void ClassDeclaration_typeParameter() {
        Class<?> klass = getTypeDeclaration(
            f.newClassDeclaration(
                null,
                Collections.emptyList(),
                f.newSimpleName("Testing"),
                Arrays.asList(new TypeParameterDeclaration[] {
                    f.newTypeParameterDeclaration(
                        f.newSimpleName("T"),
                        Collections.emptyList())
                }),
                null,
                Collections.emptyList(),
                Collections.emptyList()));
        TypeVariable<?>[] tps = klass.getTypeParameters();
        assertThat(tps.length, is(1));
        assertThat(tps[0].getName(), is("T"));
        java.lang.reflect.Type[] bounds = tps[0].getBounds();
        if (bounds.length == 1) {
            assertThat(bounds[0], is((Object) Object.class));
        }
    }

    /**
     * class declaration w/ type parameters.
     */
    @Test
    public void ClassDeclaration_boundedTypeParameter() {
        Class<?> klass = getTypeDeclaration(
            f.newClassDeclaration(
                null,
                Collections.emptyList(),
                f.newSimpleName("Testing"),
                Arrays.asList(new TypeParameterDeclaration[] {
                    f.newTypeParameterDeclaration(
                        f.newSimpleName("T"),
                        Arrays.asList(new Type[] {
                            Models.toType(f, CharSequence.class)
                        }))
                }),
                null,
                Collections.emptyList(),
                Collections.emptyList()));
        TypeVariable<?>[] tps = klass.getTypeParameters();
        assertThat(tps.length, is(1));
        assertThat(tps[0].getName(), is("T"));
        java.lang.reflect.Type[] bounds = tps[0].getBounds();
        assertThat(bounds.length, is(1));
        assertThat(bounds[0], is((Object) CharSequence.class));
    }

    /**
     * class declaration w/ super class.
     */
    @Test
    public void ClassDeclaration_superClass() {
        Class<?> klass = getTypeDeclaration(
            f.newClassDeclaration(
                null,
                Collections.emptyList(),
                f.newSimpleName("Testing"),
                Collections.emptyList(),
                Models.toType(f, Date.class),
                Collections.emptyList(),
                Collections.emptyList()));
        assertThat(klass.getSuperclass(), is((Object) Date.class));
    }

    /**
     * class declaration - super interfaces.
     */
    @Test
    public void ClassDeclaration_superInterfaces() {
        Class<?> klass = getTypeDeclaration(
            f.newClassDeclaration(
                null,
                Arrays.asList(new Attribute[] {
                    f.newModifier(ModifierKind.ABSTRACT)
                }),
                f.newSimpleName("Testing"),
                Collections.emptyList(),
                null,
                Arrays.asList(new Type[] {
                    Models.toType(f, Serializable.class),
                    Models.toType(f, RandomAccess.class),
                }),
                Collections.emptyList()));

        Class<?>[] interfaces = klass.getInterfaces();
        assertThat(interfaces.length, is(2));
        assertThat(interfaces, hasItemInArray((Object) Serializable.class));
        assertThat(interfaces, hasItemInArray((Object) RandomAccess.class));
    }

    /**
     * interface declaration.
     */
    @Test
    public void InterfaceDeclaration() {
        Class<?> klass = getTypeDeclaration(
            f.newInterfaceDeclaration(
                null,
                Arrays.asList(new Attribute[] {
                    f.newModifier(ModifierKind.PUBLIC)
                }),
                f.newSimpleName("Testing"),
                Arrays.asList(new TypeParameterDeclaration[] {
                    f.newTypeParameterDeclaration(
                        f.newSimpleName("A"),
                        Collections.emptyList())
                }),
                Arrays.asList(new Type[] {
                    Models.toType(f, Serializable.class),
                    Models.toType(f, RandomAccess.class),
                }),
                Collections.emptyList()));

        assertThat(klass.isInterface(), is(true));
        assertThat(klass.getName(), equalTo("Testing"));

        TypeVariable<?>[] tps = klass.getTypeParameters();
        assertThat(tps.length, is(1));
        assertThat(tps[0].getName(), is("A"));

        Class<?>[] interfaces = klass.getInterfaces();
        assertThat(interfaces.length, is(2));
        assertThat(interfaces, hasItemInArray((Object) Serializable.class));
        assertThat(interfaces, hasItemInArray((Object) RandomAccess.class));
    }

    /**
     * interface declaration w/ default method.
     * @throws Exception if failed
     */
    @Test
    public void InterfaceDeclaration_default() throws Exception {
        Class<?> klass = getTypeDeclaration(
                f.newInterfaceDeclaration(
                        null,
                        Arrays.asList(f.newModifier(ModifierKind.PUBLIC)),
                        f.newSimpleName("Testing"),
                        Collections.emptyList(),
                        Arrays.asList(f.newMethodDeclaration(
                                null,
                                Arrays.asList(f.newModifier(ModifierKind.DEFAULT)),
                                Models.toType(f, String.class),
                                f.newSimpleName("f"),
                                Collections.emptyList(),
                                Arrays.asList(f.newReturnStatement(Models.toLiteral(f, "OK")))))));

        assertThat(klass.isInterface(), is(true));
        assertThat(klass.getName(), equalTo("Testing"));

        assertThat(klass.getMethod("f").isDefault(), is(true));
    }

    /**
     * enum declaration.
     * @throws Exception if error occurred
     */
    @Test
    public void EnumDeclaration() throws Exception {
        Class<?> klass = getTypeDeclaration(
            f.newEnumDeclaration(
                null,
                Arrays.asList(new Attribute[] {
                    f.newModifier(ModifierKind.PUBLIC)
                }),
                f.newSimpleName("Testing"),
                Arrays.asList(new Type[] {
                    Models.toType(f, RandomAccess.class),
                }),
                Arrays.asList(new EnumConstantDeclaration[] {
                    f.newEnumConstantDeclaration(
                        null,
                        Collections.emptyList(),
                        f.newSimpleName("A"),
                        Collections.emptyList(),
                        null),
                    f.newEnumConstantDeclaration(
                        null,
                        Collections.emptyList(),
                        f.newSimpleName("B"),
                        Collections.emptyList(),
                        null),
                }),
                Arrays.asList(new TypeBodyDeclaration[] {
                    f.newMethodDeclaration(
                        null,
                        Arrays.asList(new Attribute[] {
                            f.newModifier(ModifierKind.PUBLIC)
                        }),
                        Collections.emptyList(),
                        Models.toType(f, void.class),
                        f.newSimpleName("example"),
                        Collections.emptyList(),
                        0,
                        Collections.emptyList(),
                        f.newBlock(Collections.emptyList()))
                })));

        assertThat(klass.isEnum(), is(true));
        assertThat(klass.getName(), equalTo("Testing"));

        Object[] constants = klass.getEnumConstants();
        assertThat(constants.length, is(2));
        assertThat(((Enum<?>) constants[0]).name(), is("A"));
        assertThat(((Enum<?>) constants[1]).name(), is("B"));

        klass.getDeclaredMethod("example");

        Class<?>[] interfaces = klass.getInterfaces();
        assertThat(interfaces.length, is(1));
        assertThat(interfaces, hasItemInArray((Object) RandomAccess.class));
    }

    /**
     * field declaration.
     * @throws Exception if error occurred
     */
    @Test
    public void FieldDeclaration() throws Exception {
        Class<?> klass = getTypeDeclaration(klass("Testing",
            f.newFieldDeclaration(
                null,
                Arrays.asList(new Attribute[] {
                    f.newModifier(ModifierKind.PUBLIC)
                }),
                Models.toType(f, int.class),
                Arrays.asList(new VariableDeclarator[] {
                    f.newVariableDeclarator(
                        f.newSimpleName("a"),
                        0,
                        null),
                    f.newVariableDeclarator(
                        f.newSimpleName("b"),
                        1,
                        f.newArrayInitializer(Arrays.asList(new Expression[] {
                            Models.toLiteral(f, 100)
                        }))),
                }))));
        Object obj = create(klass);

        Field a = klass.getDeclaredField("a");
        assertThat(a.get(obj), is((Object) 0));

        Field b = klass.getDeclaredField("b");
        assertThat(b.get(obj), is((Object) new int[] { 100 }));
    }

    /**
     * initializer declaration.
     * @throws Exception if error occurred
     */
    @Test
    public void InitializerDeclaration() throws Exception {
        Class<?> klass = getTypeDeclaration(klass("Testing",
            f.newFieldDeclaration(
                null,
                Arrays.asList(new Attribute[] {
                    f.newModifier(ModifierKind.PUBLIC),
                    f.newModifier(ModifierKind.STATIC),
                }),
                Models.toType(f, int.class),
                Arrays.asList(new VariableDeclarator[] {
                    f.newVariableDeclarator(
                        f.newSimpleName("a"),
                        0,
                        null),
                })),
            f.newInitializerDeclaration(
                null,
                Arrays.asList(new Modifier[] {
                    f.newModifier(ModifierKind.STATIC)
                }),
                f.newBlock(Arrays.asList(new Statement[] {
                    f.newExpressionStatement(
                        f.newAssignmentExpression(
                            f.newSimpleName("a"),
                            InfixOperator.ASSIGN,
                            Models.toLiteral(f, 100)))
                })))));

        Class.forName(klass.getName(), true, klass.getClassLoader());
        Field a = klass.getDeclaredField("a");
        assertThat(a.get(null), is((Object) 100));
    }

    /**
     * constructor declaration.
     * @throws Exception if error occurred
     */
    @Test
    public void ConstructorDeclaration() throws Exception {
        Class<?> klass = getTypeDeclaration(klass("Testing",
            f.newFieldDeclaration(
                null,
                Arrays.asList(new Attribute[] {
                    f.newModifier(ModifierKind.PUBLIC),
                }),
                Models.toType(f, int.class),
                Arrays.asList(new VariableDeclarator[] {
                    f.newVariableDeclarator(
                        f.newSimpleName("a"),
                        0,
                        null),
                })),
            f.newConstructorDeclaration(
                null,
                Arrays.asList(new Attribute[] {
                    f.newModifier(ModifierKind.PUBLIC)
                }),
                Collections.emptyList(),
                f.newSimpleName("Testing"),
                Collections.emptyList(),
                Collections.emptyList(),
                f.newBlock(Arrays.asList(new Statement[] {
                    f.newExpressionStatement(
                        f.newAssignmentExpression(
                            f.newSimpleName("a"),
                            InfixOperator.ASSIGN,
                            Models.toLiteral(f, 500)))
                })))));

        Object obj = create(klass);
        Field a = klass.getDeclaredField("a");
        assertThat(a.get(obj), is((Object) 500));
    }

    /**
     * constructor declaration w/ delegate constructor invocation.
     * @throws Exception if error occurred
     */
    @Test
    public void ConstructorDeclaration_delegate() throws Exception {
        Class<?> klass = getTypeDeclaration(klass("Testing",
            f.newFieldDeclaration(
                null,
                Arrays.asList(new Attribute[] {
                    f.newModifier(ModifierKind.PUBLIC),
                }),
                Models.toType(f, int.class),
                Arrays.asList(new VariableDeclarator[] {
                    f.newVariableDeclarator(
                        f.newSimpleName("a"),
                        0,
                        null),
                })),
            f.newConstructorDeclaration(
                null,
                Arrays.asList(new Attribute[] {
                    f.newModifier(ModifierKind.PUBLIC)
                }),
                Collections.emptyList(),
                f.newSimpleName("Testing"),
                Collections.emptyList(),
                Collections.emptyList(),
                f.newBlock(Arrays.asList(new Statement[] {
                    f.newAlternateConstructorInvocation(
                        Collections.emptyList(),
                        Arrays.asList(new Expression[] {
                            Models.toLiteral(f, 100),
                        })),
                    f.newExpressionStatement(
                        f.newAssignmentExpression(
                            f.newSimpleName("a"),
                            InfixOperator.PLUS,
                            Models.toLiteral(f, 30)))
                }))),
            f.newConstructorDeclaration(
                null,
                Arrays.asList(new Attribute[] {
                    f.newModifier(ModifierKind.PRIVATE)
                }),
                Collections.emptyList(),
                f.newSimpleName("Testing"),
                Arrays.asList(new FormalParameterDeclaration[] {
                    f.newFormalParameterDeclaration(
                        Collections.emptyList(),
                        Models.toType(f, int.class),
                        false,
                        f.newSimpleName("p"),
                        0)
                }),
                Collections.emptyList(),
                f.newBlock(Arrays.asList(new Statement[] {
                    f.newSuperConstructorInvocation(
                        null,
                        Collections.emptyList(),
                        Collections.emptyList()),
                    f.newExpressionStatement(
                        f.newAssignmentExpression(
                            f.newSimpleName("a"),
                            InfixOperator.ASSIGN,
                            f.newSimpleName("p")))
                })))));

        Object obj = create(klass);
        Field a = klass.getDeclaredField("a");
        assertThat(a.get(obj), is((Object) 130));
    }

    /**
     * constructor declaration w/ exception types.
     * @throws Exception if error occurred
     */
    @Test
    public void ConstructorDeclaration_throws() throws Exception {
        Class<?> klass = getTypeDeclaration(klass("Testing",
            f.newConstructorDeclaration(
                null,
                Arrays.asList(new Attribute[] {
                    f.newModifier(ModifierKind.PUBLIC)
                }),
                Collections.emptyList(),
                f.newSimpleName("Testing"),
                Collections.emptyList(),
                Arrays.asList(new Type[] {
                    Models.toType(f, Exception.class)
                }),
                f.newBlock(Arrays.asList(new Statement[] {
                    f.newThrowStatement(
                        f.newClassInstanceCreationExpression(
                            null,
                            Collections.emptyList(),
                            Models.toType(f, IOException.class),
                            Collections.emptyList(),
                            null))
                })))));

        Constructor<?> ctor = klass.getDeclaredConstructor();
        assertThat(ctor.getExceptionTypes(), is((Object) new Class<?>[] {
            Exception.class
        }));
        try {
            klass.newInstance();
        } catch (Exception e) {
            assertThat(e, instanceOf(IOException.class));
        }
    }

    /**
     * method declaration.
     * @throws Exception if error occurred
     */
    @Test
    public void MethodDeclaration() throws Exception {
        Class<?> klass = getTypeDeclaration(klass("Testing",
            f.newMethodDeclaration(
                null,
                Arrays.asList(new Attribute[] {
                    f.newModifier(ModifierKind.PUBLIC)
                }),
                Collections.emptyList(),
                Models.toType(f, String.class),
                f.newSimpleName("method"),
                Collections.emptyList(),
                0,
                Arrays.asList(new Type[] {
                    Models.toType(f, Exception.class)
                }),
                f.newBlock(Arrays.asList(new Statement[] {
                    f.newReturnStatement(Models.toLiteral(f, "hello"))
                })))));

        Object obj = create(klass);
        Method method = klass.getDeclaredMethod("method");
        assertThat(method.invoke(obj), is((Object) "hello"));
    }

    /**
     * method declaration w/ parameters.
     * @throws Exception if error occurred
     */
    @Test
    public void MethodDeclaration_parameter() throws Exception {
        Class<?> klass = getTypeDeclaration(klass("Testing",
            f.newMethodDeclaration(
                null,
                Arrays.asList(new Attribute[] {
                    f.newModifier(ModifierKind.PUBLIC)
                }),
                Collections.emptyList(),
                Models.toType(f, String.class),
                f.newSimpleName("method"),
                Arrays.asList(new FormalParameterDeclaration[] {
                    f.newFormalParameterDeclaration(
                        Collections.emptyList(),
                        Models.toType(f, String.class),
                        false,
                        f.newSimpleName("a"),
                        0)
                }),
                0,
                Collections.emptyList(),
                f.newBlock(Arrays.asList(new Statement[] {
                    f.newReturnStatement(f.newSimpleName("a"))
                })))));

        Object obj = create(klass);
        Method method = klass.getDeclaredMethod("method", String.class);
        assertThat(method.invoke(obj, "self"), is((Object) "self"));
    }

    /**
     * method declaration w/ varargs.
     * @throws Exception if error occurred
     */
    @Test
    public void MethodDeclaration_varargs() throws Exception {
        Class<?> klass = getTypeDeclaration(klass("Testing",
            f.newMethodDeclaration(
                null,
                Arrays.asList(new Attribute[] {
                    f.newModifier(ModifierKind.PUBLIC)
                }),
                Collections.emptyList(),
                Models.toType(f, String[].class),
                f.newSimpleName("method"),
                Arrays.asList(new FormalParameterDeclaration[] {
                    f.newFormalParameterDeclaration(
                        Collections.emptyList(),
                        Models.toType(f, String.class),
                        true,
                        f.newSimpleName("a"),
                        0)
                }),
                0,
                Collections.emptyList(),
                f.newBlock(Arrays.asList(new Statement[] {
                    f.newReturnStatement(f.newSimpleName("a"))
                })))));

        Object obj = create(klass);
        Method method = klass.getDeclaredMethod("method", String[].class);
        assertThat(method.isVarArgs(), is(true));
        assertThat(
            method.invoke(obj, (Object) new String[] {"a", "b", "c"}),
            is((Object) new String[] {"a", "b", "c"}));
    }

    /**
     * method declaration w/ extra dimensions.
     * @throws Exception if error occurred
     */
    @Test
    public void MethodDeclaration_extraDims() throws Exception {
        Class<?> klass = getTypeDeclaration(klass("Testing",
            f.newMethodDeclaration(
                null,
                Arrays.asList(new Attribute[] {
                    f.newModifier(ModifierKind.PUBLIC)
                }),
                Collections.emptyList(),
                Models.toType(f, String.class),
                f.newSimpleName("method"),
                Arrays.asList(new FormalParameterDeclaration[] {
                    f.newFormalParameterDeclaration(
                        Collections.emptyList(),
                        Models.toType(f, String.class),
                        false,
                        f.newSimpleName("a"),
                        1)
                }),
                1,
                Collections.emptyList(),
                f.newBlock(Arrays.asList(new Statement[] {
                    f.newReturnStatement(f.newSimpleName("a"))
                })))));

        Object obj = create(klass);
        Method method = klass.getDeclaredMethod("method", String[].class);
        assertThat(
            method.invoke(obj, (Object) new String[] {"a", "b", "c"}),
            is((Object) new String[] {"a", "b", "c"}));
    }

    /**
     * enum constant declaration w/ constructor arguments.
     * @throws Exception if error occurred
     */
    @Test
    public void EnumConstantDeclaration_arguments() throws Exception {
        Class<?> klass = getTypeDeclaration(
            f.newEnumDeclaration(
                null,
                Arrays.asList(new Attribute[] {
                    f.newModifier(ModifierKind.PUBLIC)
                }),
                f.newSimpleName("Testing"),
                Collections.emptyList(),
                Arrays.asList(new EnumConstantDeclaration[] {
                    f.newEnumConstantDeclaration(
                        null,
                        Collections.emptyList(),
                        f.newSimpleName("A"),
                        Arrays.asList(new Expression[] {
                            Models.toLiteral(f, 1),
                            Models.toLiteral(f, "a"),
                        }),
                        null),
                }),
                Arrays.asList(new TypeBodyDeclaration[] {
                    f.newFieldDeclaration(
                        null,
                        Arrays.asList(new Attribute[] {
                            f.newModifier(ModifierKind.PUBLIC),
                        }),
                        Models.toType(f, Object[].class),
                        Arrays.asList(new VariableDeclarator[] {
                            f.newVariableDeclarator(
                                f.newSimpleName("arguments"),
                                0,
                                null),
                        })),
                    f.newConstructorDeclaration(
                        null,
                        Arrays.asList(new Attribute[] {
                            f.newModifier(ModifierKind.PRIVATE)
                        }),
                        Collections.emptyList(),
                        f.newSimpleName("Testing"),
                        Arrays.asList(new FormalParameterDeclaration[] {
                            f.newFormalParameterDeclaration(
                                Collections.emptyList(),
                                Models.toType(f, Object.class),
                                true,
                                f.newSimpleName("args"),
                                0)
                        }),
                        Collections.emptyList(),
                        f.newBlock(Arrays.asList(new Statement[] {
                            f.newExpressionStatement(
                                f.newAssignmentExpression(
                                    f.newSimpleName("arguments"),
                                    InfixOperator.ASSIGN,
                                    f.newSimpleName("args"))),
                        })))
                })));

        Enum<?> constant = Enum.valueOf(klass.asSubclass(Enum.class), "A");
        Field field = klass.getDeclaredField("arguments");
        assertThat(field.get(constant), is((Object) new Object[] { 1, "a" }));
    }

    /**
     * enum constant declaration w/ class block.
     * @throws Exception if error occurred
     */
    @Test
    public void EnumConstantDeclaration_body() throws Exception {
        Class<?> klass = getTypeDeclaration(
            f.newEnumDeclaration(
                null,
                Arrays.asList(new Attribute[] {
                    f.newModifier(ModifierKind.PUBLIC)
                }),
                f.newSimpleName("Testing"),
                Collections.emptyList(),
                Arrays.asList(new EnumConstantDeclaration[] {
                    f.newEnumConstantDeclaration(
                        null,
                        Collections.emptyList(),
                        f.newSimpleName("A"),
                        Collections.emptyList(),
                        f.newClassBody(Arrays.asList(new TypeBodyDeclaration[] {
                            toString(returnAsString(Models.toLiteral(f, "hello")))
                        }))),
                }),
                Collections.emptyList()
            ));

        Enum<?> constant = Enum.valueOf(klass.asSubclass(Enum.class), "A");
        assertThat(constant.toString(), is("hello"));
    }

    /**
     * super method invocation.
     * @throws Exception if error occurred
     */
    @Test
    public void Super() throws Exception {
        Class<?> klass = getTypeDeclaration(
            f.newEnumDeclaration(
                null,
                Arrays.asList(new Attribute[] {
                    f.newModifier(ModifierKind.PUBLIC)
                }),
                f.newSimpleName("Testing"),
                Collections.emptyList(),
                Arrays.asList(new EnumConstantDeclaration[] {
                    f.newEnumConstantDeclaration(
                        null,
                        Collections.emptyList(),
                        f.newSimpleName("A"),
                        Collections.emptyList(),
                        f.newClassBody(Arrays.asList(new TypeBodyDeclaration[] {
                            toString(returnAsString(f.newMethodInvocationExpression(
                                f.newSuper(null),
                                Collections.emptyList(),
                                f.newSimpleName("name"),
                                Collections.emptyList())))
                        }))),
                }),
                Collections.emptyList()
            ));

        Enum<?> constant = Enum.valueOf(klass.asSubclass(Enum.class), "A");
        assertThat(constant.toString(), is("A"));
    }

    /**
     * package declaration.
     */
    @Test
    public void PackageDeclaration() {
        packageDecl = f.newPackageDeclaration(
            null,
            Collections.emptyList(),
            Models.toName(f, "com.example"));
        assertToString(
            fromExpr("Hello", Models.toLiteral(f, "Hello, world!")),
            "com.example.Hello",
            "Hello, world!");
    }

    /**
     * import declaration.
     */
    @Test
    public void ImportDeclaration_type() {
        importDecls.add(f.newImportDeclaration(ImportKind.SINGLE_TYPE,
            Models.toName(f, "java.util.Arrays")));
        assertToString(
            fromExpr("Hello",
                f.newMethodInvocationExpression(
                    Models.toName(f, "Arrays"),
                    Collections.emptyList(),
                    f.newSimpleName("asList"),
                    Arrays.asList(Models.toLiteral(f, "Hello, world!")))),
            "Hello",
            "[Hello, world!]");
    }

    /**
     * import declaration - on-demand import.
     */
    @Test
    public void ImportDeclaration_onDemand() {
        importDecls.add(f.newImportDeclaration(ImportKind.TYPE_ON_DEMAND,
            Models.toName(f, "java.util")));
        assertToString(
            fromExpr("Hello",
                f.newMethodInvocationExpression(
                    Models.toName(f, "Arrays"),
                    Collections.emptyList(),
                    f.newSimpleName("asList"),
                    Arrays.asList(Models.toLiteral(f, "Hello, world!")))),
            "Hello",
            "[Hello, world!]");
    }

    /**
     * import declaration - static.
     */
    @Test
    public void ImportDeclaration_singleStatic() {
        importDecls.add(f.newImportDeclaration(ImportKind.SINGLE_STATIC,
            Models.toName(f, "java.util.Arrays.asList")));
        assertToString(
            fromExpr("Hello",
                f.newMethodInvocationExpression(
                    null,
                    Collections.emptyList(),
                    f.newSimpleName("asList"),
                    Arrays.asList(Models.toLiteral(f, "Hello, world!")))),
            "Hello",
            "[Hello, world!]");
    }

    /**
     * import declaration - static on-demand import.
     */
    @Test
    public void ImportDeclaration_staticOnDemand() {
        importDecls.add(f.newImportDeclaration(ImportKind.STATIC_ON_DEMAND,
            Models.toName(f, "java.util.Arrays")));
        assertToString(
            fromExpr("Hello",
                f.newMethodInvocationExpression(
                    null,
                    Collections.emptyList(),
                    f.newSimpleName("asList"),
                    Arrays.asList(Models.toLiteral(f, "Hello, world!")))),
            "Hello",
            "[Hello, world!]");
    }

    /**
     * auto parenthesize - left term of infix operator.
     */
    @Test
    public void autoParenthesize_infixLeft() {
        assertToString(
            fromExpr("Hello", f.newInfixExpression(
                f.newInfixExpression(
                    Models.toLiteral(f, 10),
                    InfixOperator.MINUS,
                    Models.toLiteral(f, 5)),
                InfixOperator.TIMES,
                Models.toLiteral(f, 2))),
            "Hello",
            "10");
    }

    /**
     * auto parenthesize - right term of infix operator.
     */
    @Test
    public void autoParenthesize_infixRight() {
        assertToString(
            fromExpr("Hello", f.newInfixExpression(
                Models.toLiteral(f, 100),
                InfixOperator.MINUS,
                f.newInfixExpression(
                    Models.toLiteral(f, 100),
                    InfixOperator.MINUS,
                    Models.toLiteral(f, 100)))),
            "Hello",
            "100");
    }

    private LocalVariableDeclaration var(Type type, String name, Expression init) {
        return f.newLocalVariableDeclaration(
            Collections.emptyList(),
            type,
            Arrays.asList(new VariableDeclarator[] {
                f.newVariableDeclarator(f.newSimpleName(name), 0, init)
            }));
    }

    private Statement newStringBuilder(String name) {
        return var(
            f.newNamedType(f.newSimpleName("StringBuilder")),
            name,
            f.newClassInstanceCreationExpression(
                null,
                Collections.emptyList(),
                f.newNamedType(f.newSimpleName("StringBuilder")),
                Collections.emptyList(),
                null));
    }

    private Statement append(String name, Expression value) {
        return f.newExpressionStatement(
            f.newMethodInvocationExpression(
                f.newSimpleName(name),
                Collections.emptyList(),
                f.newSimpleName("append"),
                Arrays.asList(value)));
    }

    private CompilationUnit unit(TypeDeclaration...types) {
        return f.newCompilationUnit(packageDecl, importDecls, Arrays.asList(types));
    }

    private ClassDeclaration klass(String name, TypeBodyDeclaration...elems) {
        return f.newClassDeclaration(
            null,
            Arrays.asList(new Attribute[] {
                f.newModifier(ModifierKind.PUBLIC)
            }),
            f.newSimpleName(name),
            Collections.emptyList(),
            null,
            Arrays.asList(),
            Arrays.asList(elems));
    }

    private MethodDeclaration toString(Statement...statements) {
        return f.newMethodDeclaration(
            null,
            Arrays.asList(new Attribute[] {
                f.newModifier(ModifierKind.PUBLIC)
            }),
            Collections.emptyList(),
            f.newNamedType(Models.toName(f, "String")),
            f.newSimpleName("toString"),
            Collections.emptyList(),
            0,
            Collections.emptyList(),
            f.newBlock(Arrays.asList(statements)));
    }



    private CompilationUnit fromExpr(String name, Expression expr) {
        return unit(klass(name, toString(returnAsString(expr))));
    }

    private ReturnStatement returnAsString(Expression expr) {
        return f.newReturnStatement(
            f.newMethodInvocationExpression(
                Models.toName(f, "String"),
                Collections.emptyList(),
                f.newSimpleName("valueOf"),
                Arrays.asList(expr))
        );
    }

    private CompilationUnit fromStmt(String name, Statement...stmts) {
        return unit(klass(name, toString(stmts)));
    }

    private void assertToString(CompilationUnit unit, String name, String actual) {
        Class<?> klass = compile(unit, name);
        Object object = create(klass);
        assertThat(object.toString(), is(actual));
    }

    private void assertRaise(CompilationUnit unit, String name, Class<?> exception) {
        Class<?> klass = compile(unit, name);
        Object object = create(klass);
        try {
            object.toString();
        } catch (Throwable t) {
            assertThat(t, instanceOf(exception));
        }
    }

    private AnnotationDeclaration createAnnotation(String name) {
        return f.newAnnotationDeclaration(
                null,
                Arrays.asList(new Annotation[] {
                    f.newSingleElementAnnotation(
                        (NamedType) Models.toType(f, Retention.class),
                        Models.toName(f, RetentionPolicy.RUNTIME))
                }),
                f.newSimpleName(name),
                Arrays.asList(new TypeBodyDeclaration[] {
                    f.newAnnotationElementDeclaration(
                        null,
                        Collections.emptyList(),
                        Models.toType(f, String[].class),
                        f.newSimpleName("value"),
                        null),
                    f.newAnnotationElementDeclaration(
                            null,
                            Collections.emptyList(),
                            Models.toType(f, int.class),
                            f.newSimpleName("option"),
                            Models.toLiteral(f, 100)),
                }));

    }

    private Object getAnnotationValue(
            Class<?> klass,
            String annotationTypeName,
            String annotationElementName) {
        for (java.lang.annotation.Annotation a : klass.getDeclaredAnnotations()) {
            Class<?> annotationType = a.annotationType();
            if (annotationType.getName().equals(annotationTypeName)) {
                try {
                    Method element = annotationType.getDeclaredMethod(annotationElementName);
                    element.setAccessible(true);
                    return element.invoke(a);
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            }
        }
        throw new AssertionError(klass);
    }

    private void assertType(Type type, java.lang.reflect.Type expect) {
        assertThat(getType(type), is(expect));
    }

    private Class<?> getTypeDeclaration(TypeDeclaration... decls) {
        CompilationUnit unit = unit(decls);
        Class<?> klass = compile(unit, decls[0].getName().getToken());
        return klass;
    }

    private java.lang.reflect.Type getType(Type type) {
        CompilationUnit unit = unit(
            klass("Testing",
                f.newMethodDeclaration(
                    null,
                    Arrays.asList(new Attribute[] {
                        f.newModifier(ModifierKind.PUBLIC),
                        f.newModifier(ModifierKind.NATIVE),
                    }),
                    Arrays.asList(new TypeParameterDeclaration[] {
                        f.newTypeParameterDeclaration(
                            f.newSimpleName("T"),
                            Collections.emptyList())
                    }),
                    type,
                    f.newSimpleName("testing"),
                    Collections.emptyList(),
                    0,
                    Collections.emptyList(),
                    null)));
        Class<?> klass = compile(unit, "Testing");
        try {
            Method method = klass.getDeclaredMethod("testing");
            java.lang.reflect.Type result = method.getGenericReturnType();
            if (result instanceof GenericArrayType) {
                // special case for array (like int[])
                GenericArrayType array = (GenericArrayType) result;
                if (array.getGenericComponentType() instanceof Class<?>) {
                    return method.getReturnType();
                }
                return method.getReturnType();
            }
            return result;
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private Class<?> compile(CompilationUnit unit, String name) {
        List<String> lines = new ArrayList<>();
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        for (int i = 1; i < elements.length; i++) {
            if (getClass().getName().equals(elements[i].getClassName()) == false) {
                break;
            }
            lines.add(elements[i].toString());
        }
        unit.putModelTrait(CommentEmitTrait.class, new CommentEmitTrait(lines));
        compiler.addSource(new CompilationUnitJavaFile(unit));
        List<Diagnostic<? extends JavaFileObject>> diagnostics = compiler.doCompile();
        if (diagnostics.isEmpty() == false) {
            throw new AssertionError(diagnostics + ":::" + unit);
        }
        try {
            return compiler.getClassLoader().loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T create(Class<?> target, Object...args) {
        Class<?>[] argTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            argTypes[i] = args[i].getClass();
        }
        try {
            return (T) target.getConstructor(argTypes).newInstance(args);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
