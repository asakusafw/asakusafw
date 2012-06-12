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
package com.asakusafw.compiler.operator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import com.asakusafw.compiler.common.NameGenerator;
import com.asakusafw.compiler.common.Precondition;
import com.ashigeru.lang.java.jsr269.bridge.Jsr269;
import com.ashigeru.lang.java.model.syntax.Attribute;
import com.ashigeru.lang.java.model.syntax.Expression;
import com.ashigeru.lang.java.model.syntax.FieldAccessExpression;
import com.ashigeru.lang.java.model.syntax.FieldDeclaration;
import com.ashigeru.lang.java.model.syntax.FormalParameterDeclaration;
import com.ashigeru.lang.java.model.syntax.MethodDeclaration;
import com.ashigeru.lang.java.model.syntax.ModelFactory;
import com.ashigeru.lang.java.model.syntax.SimpleName;
import com.ashigeru.lang.java.model.syntax.Statement;
import com.ashigeru.lang.java.model.syntax.Type;
import com.ashigeru.lang.java.model.syntax.TypeBodyDeclaration;
import com.ashigeru.lang.java.model.syntax.TypeParameterDeclaration;
import com.ashigeru.lang.java.model.util.AttributeBuilder;
import com.ashigeru.lang.java.model.util.ExpressionBuilder;
import com.ashigeru.lang.java.model.util.ImportBuilder;
import com.ashigeru.lang.java.model.util.TypeBuilder;

/**
 * 演算子メソッドの実装を構築するビルダー。
 */
public class ImplementationBuilder {

    private ExecutableElement element;

    private ModelFactory factory;

    private ImportBuilder importer;

    private NameGenerator names;

    private Jsr269 converter;

    private List<Statement> statements;

    private List<FieldDeclaration> fields;

    /**
     * インスタンスを生成する。
     * @param context コンテキストオブジェクト
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public ImplementationBuilder(OperatorProcessor.Context context) {
        Precondition.checkMustNotBeNull(context, "context"); //$NON-NLS-1$
        this.element = context.element;
        this.factory = context.environment.getFactory();
        this.importer = context.importer;
        this.names = context.names;
        this.converter = new Jsr269(factory);
        this.statements = new ArrayList<Statement>();
        this.fields = new ArrayList<FieldDeclaration>();
    }

    /**
     * 引数の名前を返す。
     * @param index 引数の番号 (0起算)
     * @return 引数の名前
     */
    public SimpleName getParameterName(int index) {
        VariableElement parameter = element.getParameters().get(index);
        return factory.newSimpleName(parameter.getSimpleName().toString());
    }

    /**
     * 引数の型を返す。
     * @param index 引数の番号 (0起算)
     * @return 引数の型
     */
    public Type getParameterType(int index) {
        VariableElement parameter = element.getParameters().get(index);
        return importer.resolve(converter.convert(parameter.asType()));
    }

    /**
     * 実装のメソッドに文を追加する。
     * @param statement 対象の文
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public void addStatement(Statement statement) {
        Precondition.checkMustNotBeNull(statement, "statement"); //$NON-NLS-1$
        this.statements.add(statement);
    }

    /**
     * 実装のメソッドにモデルのコピー文を追加する。
     * @param from コピー元のモデルが含まれる式
     * @param to コピー先のモデルが含まれる式
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public void addCopyStatement(Expression from, Expression to) {
        Precondition.checkMustNotBeNull(from, "from"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(to, "to"); //$NON-NLS-1$
        this.statements.add(new ExpressionBuilder(factory, to)
            .method("copyFrom", from)
            .toStatement());
    }

    /**
     * モデルオブジェクトを保持するフィールドを新たに生成する。
     * @param type フィールドの型
     * @param name フィールドの名前
     * @return 生成したフィールドにアクセスするための式
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public FieldAccessExpression addModelObjectField(TypeMirror type, String name) {
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(name, "name"); //$NON-NLS-1$
        return addModelObjectField(converter.convert(type), name);
    }

    /**
     * モデルオブジェクトを保持するフィールドを新たに生成する。
     * @param type フィールドの型
     * @param name フィールドの名前
     * @return 生成したフィールドにアクセスするための式
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public FieldAccessExpression addModelObjectField(Type type, String name) {
        Precondition.checkMustNotBeNull(type, "type"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(name, "name"); //$NON-NLS-1$
        SimpleName fieldName = names.create(name);
        Type fieldType = importer.resolve(type);
        fields.add(factory.newFieldDeclaration(
                null,
                new AttributeBuilder(factory)
                    .Private()
                    .toAttributes(),
                fieldType,
                fieldName,
                new TypeBuilder(factory, fieldType)
                    .newObject()
                    .toExpression()));
        return factory.newFieldAccessExpression(factory.newThis(), fieldName);
    }

    /**
     * これまでに生成した情報を元に実装を返す。
     * @return 生成した実装
     */
    public List<TypeBodyDeclaration> toImplementation() {
        List<TypeBodyDeclaration> results = new ArrayList<TypeBodyDeclaration>();
        results.addAll(fields);
        results.add(toMethodDeclaration());
        return results;
    }

    private MethodDeclaration toMethodDeclaration() {
        return factory.newMethodDeclaration(
                null,
                new AttributeBuilder(factory)
                    .annotation(importer.toType(Override.class))
                    .Public()
                    .toAttributes(),
                toTypeParameters(),
                importer.resolve(converter.convert(element.getReturnType())),
                factory.newSimpleName(element.getSimpleName().toString()),
                toParameters(),
                0,
                Collections.<Type>emptyList(),
                factory.newBlock(statements));
    }

    private List<FormalParameterDeclaration> toParameters() {
        List<? extends VariableElement> parameters = element.getParameters();
        List<FormalParameterDeclaration> results = new ArrayList<FormalParameterDeclaration>();
        for (int i = 0, n = parameters.size(); i < n; i++) {
            VariableElement var = parameters.get(i);
            results.add(factory.newFormalParameterDeclaration(
                    Collections.<Attribute>emptyList(),
                    importer.resolve(converter.convert(var.asType())),
                    (i == n - 1) && element.isVarArgs(),
                    factory.newSimpleName(var.getSimpleName().toString()),
                    0));
        }
        return results;
    }

    private List<TypeParameterDeclaration> toTypeParameters() {
        List<? extends TypeParameterElement> typeParameters = element.getTypeParameters();
        if (typeParameters.isEmpty()) {
            return Collections.emptyList();
        }
        List<TypeParameterDeclaration> results = new ArrayList<TypeParameterDeclaration>();
        for (TypeParameterElement typeParameter : typeParameters) {
            SimpleName name = factory.newSimpleName(typeParameter.getSimpleName().toString());
            List<Type> typeBounds = new ArrayList<Type>();
            for (TypeMirror typeBound : typeParameter.getBounds()) {
                typeBounds.add(importer.resolve(converter.convert(typeBound)));
            }
            results.add(factory.newTypeParameterDeclaration(name, typeBounds));
        }
        return results;
    }
}
