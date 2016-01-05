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
package com.asakusafw.modelgen.emitter;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Generated;

import org.apache.hadoop.io.Writable;

import com.asakusafw.modelgen.Constants;
import com.asakusafw.modelgen.model.ModelDescription;
import com.asakusafw.modelgen.model.ModelProperty;
import com.asakusafw.modelgen.model.PropertyType;
import com.asakusafw.utils.java.model.syntax.Annotation;
import com.asakusafw.utils.java.model.syntax.Attribute;
import com.asakusafw.utils.java.model.syntax.Block;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.FieldDeclaration;
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.InfixOperator;
import com.asakusafw.utils.java.model.syntax.Javadoc;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.ModifierKind;
import com.asakusafw.utils.java.model.syntax.PackageDeclaration;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.syntax.TypeDeclaration;
import com.asakusafw.utils.java.model.syntax.TypeParameterDeclaration;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ExpressionBuilder;
import com.asakusafw.utils.java.model.util.JavadocBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;

/**
 * モデルに対するエンティティ情報をファイル上に出力するエミッタ。
 * @param <T> 出力するモデルの種類
 */
public abstract class ModelEntityEmitter<T extends ModelDescription>
        extends BaseEmitter<T> {

    private static final int HASHCODE_PRIME = 31;

    /**
     * インスタンスを生成する。
     * @param factory ソースコードを生成するファクトリ
     * @param output 出力先のベースディレクトリ
     * @param packageName 出力先のパッケージ名
     * @param headerComment ファイルのヘッダコメント、不要の場合は{@code null}
     */
    public ModelEntityEmitter(
            ModelFactory factory,
            File output,
            String packageName,
            List<String> headerComment) {
        super(factory, output, packageName, headerComment);
    }

    /**
     * このエミッタが対象に取れるモデルの種類を返す。
     * @return このエミッタが対象に取れるモデルの種類を表すクラス
     */
    public abstract Class<T> getEmitTargetType();

    @Override
    protected PackageDeclaration createPackageDeclaration(T model) {
        return f.newPackageDeclaration(
                common.getPackageNameOf(
                        model.getReference(),
                        Constants.CATEGORY_MODEL));
    }

    @Override
    protected TypeDeclaration createTypeDeclaration(T model) {
        // クラス名が衝突しないように、強制的にインポート扱いにする
        bless(common.getModelType(model.getReference()));

        List<Annotation> annotations = createAnnotationsForModel(model);
        List<Attribute> modifiers = new ArrayList<Attribute>();
        modifiers.addAll(new AttributeBuilder(f)
            .annotation(
                    bless(Generated.class),
                    Models.toLiteral(f, MessageFormat.format("{0}:{1}",
                            getClass().getSimpleName(),
                            Constants.VERSION)))
            .toAnnotations());
        modifiers.addAll(annotations);
        modifiers.add(f.newModifier(ModifierKind.PUBLIC));
        // modifiers.add(f.newModifier(ModifierKind.FINAL));
        return f.newClassDeclaration(
                createJavadocForModel(model),
                modifiers,
                common.getTypeNameOf(model.getReference()),
                Collections.<TypeParameterDeclaration>emptyList(),
                null,
                createSuperInterfaces(model),
                createMembers(model));
    }

    /**
     * 対象のモデルに対するメンバーの一覧を返す。
     * @param model 対象のモデル
     * @return メンバーの一覧
     */
    protected List<TypeBodyDeclaration> createMembers(T model) {
        List<ModelProperty> properties = model.getProperties();
        List<TypeBodyDeclaration> body = new ArrayList<TypeBodyDeclaration>();
        for (ModelProperty property : properties) {
            TypeBodyDeclaration member = createField(property);
            body.add(member);
        }
        for (ModelProperty property : properties) {
            TypeBodyDeclaration getter = createGetter(property);
            body.add(getter);
            TypeBodyDeclaration setter = createSetter(property);
            body.add(setter);
            TypeBodyDeclaration altGetter = createAltGetter(property);
            if (altGetter != null) {
                body.add(altGetter);
            }
            TypeBodyDeclaration altSetter = createAltSetter(property);
            if (altSetter != null) {
                body.add(altSetter);
            }
            TypeBodyDeclaration optionGetter = createOptionGetter(property);
            body.add(optionGetter);
            TypeBodyDeclaration optionSetter = createOptionSetter(property);
            body.add(optionSetter);
        }
        body.add(createCopier(model));

        body.add(createWritableWrite(model));
        body.add(createWritableReadFields(model));

        body.add(createHashCode(model));
        body.add(createEquals(model));

        body.add(createToString(model));
        return body;
    }

    /**
     * 指定のプロパティに対するフィールドの宣言を、対応するJavaのDOMとして返す。
     * @param property 対象のプロパティ
     * @return 対応するDOM
     */
    protected FieldDeclaration createField(ModelProperty property) {
        List<Annotation> annotations = createAnnotationsForField(property);
        List<Attribute> modifiers = new ArrayList<Attribute>();
        modifiers.addAll(annotations);
        modifiers.add(f.newModifier(ModifierKind.PRIVATE));
        return f.newFieldDeclaration(
                createJavadocForField(property),
                modifiers,
                bless(common.getOptionType(property.getType())),
                common.getFieldNameOf(property.getName(), property.getType()),
                createInitializerForField(property));
    }

    /**
     * 指定のプロパティに対するゲッターメソッドの宣言を、対応するJavaのDOMとして返す。
     * @param property 対象のプロパティ
     * @return 対応するDOM
     */
    protected MethodDeclaration createGetter(ModelProperty property) {
        List<Annotation> annotations = createAnnotationsForGetter(property);
        List<Attribute> modifiers = new ArrayList<Attribute>();
        modifiers.addAll(annotations);
        modifiers.add(f.newModifier(ModifierKind.PUBLIC));
        return f.newMethodDeclaration(
                createJavadocForGetter(property),
                modifiers,
                Collections.<TypeParameterDeclaration>emptyList(),
                bless(common.getRawType(property.getType())),
                common.getGetterNameOf(property.getName(), property.getType()),
                Collections.<FormalParameterDeclaration>emptyList(),
                0,
                Collections.<Type>emptyList(),
                createBodyForGetter(property));
    }

    /**
     * 指定のプロパティに対するセッターメソッドの宣言を、対応するJavaのDOMとして返す。
     * @param property 対象のプロパティ
     * @return 対応するDOM
     */
    protected MethodDeclaration createSetter(ModelProperty property) {
        List<Annotation> annotations = createAnnotationsForSetter(property);
        List<Attribute> modifiers = new ArrayList<Attribute>();
        modifiers.addAll(annotations);
        modifiers.add(f.newModifier(ModifierKind.PUBLIC));
        SimpleName parameterName = createNameForParameter(property);
        return f.newMethodDeclaration(
                createJavadocForSetter(property),
                modifiers,
                Collections.<TypeParameterDeclaration>emptyList(),
                Models.toType(f, void.class),
                common.getSetterNameOf(property.getName(), property.getType()),
                Collections.singletonList(
                        f.newFormalParameterDeclaration(
                                bless(common.getRawType(property.getType())),
                                parameterName)
                ),
                0,
                Collections.<Type>emptyList(),
                createBodyForSetter(property, parameterName));
    }

    /**
     * 指定のプロパティに対する代替ゲッターメソッドの宣言を、対応するJavaのDOMとして返す。
     * @param property 対象のプロパティ
     * @return 対応するDOM
     */
    protected MethodDeclaration createAltGetter(ModelProperty property) {
        SimpleName name =
            common.getAltGetterNameOf(property.getName(), property.getType());
        if (name == null) {
            return null;
        }
        // Javadocは通常のものを代用
        List<Annotation> annotations = createAnnotationsForGetter(property);
        List<Attribute> modifiers = new ArrayList<Attribute>();
        modifiers.addAll(annotations);
        modifiers.add(f.newModifier(ModifierKind.PUBLIC));
        return f.newMethodDeclaration(
                createJavadocForGetter(property),
                modifiers,
                Collections.<TypeParameterDeclaration>emptyList(),
                bless(common.getAltType(property.getType())),
                name,
                Collections.<FormalParameterDeclaration>emptyList(),
                0,
                Collections.<Type>emptyList(),
                createBodyForAltGetter(property));
    }

    /**
     * 指定のプロパティに対するセッターメソッドの宣言を、対応するJavaのDOMとして返す。
     * @param property 対象のプロパティ
     * @return 対応するDOM
     */
    protected MethodDeclaration createAltSetter(ModelProperty property) {
        SimpleName name =
            common.getAltSetterNameOf(property.getName(), property.getType());
        if (name == null) {
            return null;
        }
        // Javadocは通常のものを代用
        List<Annotation> annotations = createAnnotationsForSetter(property);
        List<Attribute> modifiers = new ArrayList<Attribute>();
        modifiers.addAll(annotations);
        modifiers.add(f.newModifier(ModifierKind.PUBLIC));
        SimpleName parameterName = createNameForParameter(property);
        return f.newMethodDeclaration(
                createJavadocForSetter(property),
                modifiers,
                Collections.<TypeParameterDeclaration>emptyList(),
                Models.toType(f, void.class),
                common.getAltSetterNameOf(property.getName(), property.getType()),
                Collections.singletonList(
                        f.newFormalParameterDeclaration(
                                bless(common.getAltType(property.getType())),
                                parameterName)
                ),
                0,
                Collections.<Type>emptyList(),
                createBodyForAltSetter(property, parameterName));
    }

    /**
     * 指定のプロパティに対するオプションオブジェクトのゲッターメソッドの宣言を、対応するJavaのDOMとして返す。
     * @param property 対象のプロパティ
     * @return 対応するDOM
     */
    protected MethodDeclaration createOptionGetter(ModelProperty property) {
        List<Annotation> annotations = createAnnotationsForOptionGetter(property);
        List<Attribute> modifiers = new ArrayList<Attribute>();
        modifiers.addAll(annotations);
        modifiers.add(f.newModifier(ModifierKind.PUBLIC));
        return f.newMethodDeclaration(
                createJavadocForOptionGetter(property),
                modifiers,
                Collections.<TypeParameterDeclaration>emptyList(),
                bless(common.getOptionType(property.getType())),
                common.getOptionGetterNameOf(property.getName(), property.getType()),
                Collections.<FormalParameterDeclaration>emptyList(),
                0,
                Collections.<Type>emptyList(),
                createBodyForOptionGetter(property));
    }

    /**
     * 指定のプロパティに対するオプションオブジェクトのセッターメソッドの宣言を、対応するJavaのDOMとして返す。
     * @param property 対象のプロパティ
     * @return 対応するDOM
     */
    protected MethodDeclaration createOptionSetter(ModelProperty property) {
        List<Annotation> annotations = createAnnotationsForOptionSetter(property);
        List<Attribute> modifiers = new ArrayList<Attribute>();
        modifiers.addAll(annotations);
        modifiers.add(f.newModifier(ModifierKind.PUBLIC));
        SimpleName parameterName = createNameForParameter(property);
        return f.newMethodDeclaration(
                createJavadocForOptionSetter(property),
                modifiers,
                Collections.<TypeParameterDeclaration>emptyList(),
                Models.toType(f, void.class),
                common.getOptionSetterNameOf(property.getName(), property.getType()),
                Collections.singletonList(
                        f.newFormalParameterDeclaration(
                                bless(common.getOptionType(property.getType())),
                                parameterName)
                ),
                0,
                Collections.<Type>emptyList(),
                createBodyForOptionSetter(property, parameterName));
    }

    /**
     * 指定のモデルに対するコピーメソッドの宣言を、対応するJavaのDOMとして返す。
     * @param model 対象のモデル
     * @return 対応するDOM
     */
    protected TypeBodyDeclaration createCopier(T model) {
        SimpleName parameter = common.getVariableNameOf(model, "source");
        List<Statement> statements = new ArrayList<Statement>();
        for (ModelProperty property : model.getProperties()) {
            statements.add(createCopierFor(
                    parameter,
                    property.getName(),
                    property.getName(),
                    property.getType()));
        }
        return f.newMethodDeclaration(
                new JavadocBuilder(f)
                    .text("指定のオブジェクトが持つプロパティの内容を全てこのオブジェクトにコピーする。")
                    .param(parameter)
                        .text("コピー元になるオブジェクト")
                    .toJavadoc(),
                new AttributeBuilder(f)
                    .Public()
                    .toAttributes(),
                Collections.<TypeParameterDeclaration>emptyList(),
                Models.toType(f, void.class),
                common.getCopierName(),
                Collections.singletonList(f.newFormalParameterDeclaration(
                        bless(common.getModelType(model.getReference())),
                        parameter)),
                0,
                Collections.<Type>emptyList(),
                f.newBlock(statements));
    }

    private TypeBodyDeclaration createWritableWrite(T model) {
        SimpleName parameter = common.getVariableNameOf(model, "out");
        List<Statement> statements = new ArrayList<Statement>();
        for (ModelProperty property : model.getProperties()) {
            SimpleName fieldName = common.getFieldNameOf(
                    property.getName(),
                    property.getType());
            statements.add(new ExpressionBuilder(f, fieldName)
                .method("write", parameter)
                .toStatement());
        }
        return f.newMethodDeclaration(
                null,
                new AttributeBuilder(f)
                    .annotation(bless(Override.class))
                    .Public()
                    .toAttributes(),
                Collections.<TypeParameterDeclaration>emptyList(),
                Models.toType(f, void.class),
                f.newSimpleName("write"),
                Collections.singletonList(f.newFormalParameterDeclaration(
                        bless(DataOutput.class),
                        parameter)),
                0,
                Collections.singletonList(bless(IOException.class)),
                f.newBlock(statements));
    }

    private TypeBodyDeclaration createWritableReadFields(T model) {
        SimpleName parameter = common.getVariableNameOf(model, "in");
        List<Statement> statements = new ArrayList<Statement>();
        for (ModelProperty property : model.getProperties()) {
            SimpleName fieldName = common.getFieldNameOf(
                    property.getName(),
                    property.getType());
            statements.add(new ExpressionBuilder(f, fieldName)
                .method("readFields", parameter)
                .toStatement());
        }
        return f.newMethodDeclaration(
                null,
                new AttributeBuilder(f)
                    .annotation(bless(Override.class))
                    .Public()
                    .toAttributes(),
                Collections.<TypeParameterDeclaration>emptyList(),
                Models.toType(f, void.class),
                f.newSimpleName("readFields"),
                Arrays.asList(f.newFormalParameterDeclaration(
                        bless(DataInput.class),
                        parameter)),
                0,
                Collections.singletonList(bless(IOException.class)),
                f.newBlock(statements));
    }

    private TypeBodyDeclaration createHashCode(T model) {
        List<Statement> statements = new ArrayList<Statement>();
        SimpleName prime = common.getVariableNameOf(model, "prime");
        SimpleName result = common.getVariableNameOf(model, "result");
        statements.add(new ExpressionBuilder(f, Models.toLiteral(f, HASHCODE_PRIME))
            .toLocalVariableDeclaration(Models.toType(f, int.class), prime));
        statements.add(new ExpressionBuilder(f, Models.toLiteral(f, 1))
            .toLocalVariableDeclaration(Models.toType(f, int.class), result));
        for (ModelProperty property : model.getProperties()) {
            SimpleName field = common.getFieldNameOf(
                    property.getName(),
                    property.getType());
            statements.add(new ExpressionBuilder(f, result)
                .assignFrom(new ExpressionBuilder(f, prime)
                    .apply(InfixOperator.TIMES, result)
                    .apply(InfixOperator.PLUS, new ExpressionBuilder(f, field)
                        .method("hashCode")
                        .toExpression())
                    .toExpression())
                .toStatement());
        }
        statements.add(f.newReturnStatement(result));

        return f.newMethodDeclaration(
                null,
                new AttributeBuilder(f)
                    .annotation(bless(Override.class))
                    .Public()
                    .toAttributes(),
                Models.toType(f, int.class),
                f.newSimpleName("hashCode"),
                Collections.<FormalParameterDeclaration>emptyList(),
                statements);
    }

    private TypeBodyDeclaration createEquals(T model) {
        List<Statement> statements = new ArrayList<Statement>();
        SimpleName obj = common.getVariableNameOf(model, "obj");
        statements.add(f.newIfStatement(
                new ExpressionBuilder(f, f.newThis())
                    .apply(InfixOperator.EQUALS, obj)
                    .toExpression(),
                f.newBlock(f.newReturnStatement(Models.toLiteral(f, true)))));
        statements.add(f.newIfStatement(
                new ExpressionBuilder(f, obj)
                    .apply(InfixOperator.EQUALS, Models.toNullLiteral(f))
                    .toExpression(),
                f.newBlock(f.newReturnStatement(Models.toLiteral(f, false)))));
        statements.add(f.newIfStatement(
                new ExpressionBuilder(f, f.newThis())
                    .method("getClass")
                    .apply(InfixOperator.NOT_EQUALS, new ExpressionBuilder(f, obj)
                        .method("getClass")
                        .toExpression())
                    .toExpression(),
                f.newBlock(f.newReturnStatement(Models.toLiteral(f, false)))));
        SimpleName other = common.getVariableNameOf(model, "other");
        statements.add(new ExpressionBuilder(f, obj)
            .castTo(bless(common.getModelType(model.getReference())))
            .toLocalVariableDeclaration(
                    bless(common.getModelType(model.getReference())),
                    other));
        for (ModelProperty property : model.getProperties()) {
            SimpleName field = common.getFieldNameOf(property.getName(), property.getType());
            statements.add(f.newIfStatement(
                    new ExpressionBuilder(f, f.newThis())
                        .field(field)
                        .method("equals", new ExpressionBuilder(f, other)
                            .field(field)
                            .toExpression())
                        .apply(InfixOperator.EQUALS, Models.toLiteral(f, false))
                        .toExpression(),
                    f.newBlock(f.newReturnStatement(Models.toLiteral(f, false)))));
        }
        statements.add(f.newReturnStatement(Models.toLiteral(f, true)));

        return f.newMethodDeclaration(
                null,
                new AttributeBuilder(f)
                    .annotation(bless(Override.class))
                    .Public()
                    .toAttributes(),
                Models.toType(f, boolean.class),
                f.newSimpleName("equals"),
                Collections.singletonList(f.newFormalParameterDeclaration(
                        bless(Object.class),
                        obj)),
                statements);
    }

    private TypeBodyDeclaration createToString(T model) {
        List<Statement> statements = new ArrayList<Statement>();
        SimpleName buffer = common.getVariableNameOf(model, "result");
        statements.add(new TypeBuilder(f, bless(StringBuilder.class))
            .newObject()
            .toLocalVariableDeclaration(bless(StringBuilder.class), buffer));
        statements.add(new ExpressionBuilder(f, buffer)
            .method("append", Models.toLiteral(f, "{"))
            .toStatement());
        statements.add(new ExpressionBuilder(f, buffer)
            .method("append", Models.toLiteral(f, "class=" + model.getReference().getSimpleName()))
            .toStatement());
        for (ModelProperty property : model.getProperties()) {
            statements.add(new ExpressionBuilder(f, buffer)
                .method("append", Models.toLiteral(f, MessageFormat.format(
                        ", {0}=",
                        common.getFieldNameOf(property.getName(), property.getType()))))
                .toStatement());
            statements.add(new ExpressionBuilder(f, buffer)
                .method("append", new ExpressionBuilder(f, f.newThis())
                    .field(common.getFieldNameOf(property.getName(), property.getType()))
                    .toExpression())
                .toStatement());
        }
        statements.add(new ExpressionBuilder(f, buffer)
            .method("append", Models.toLiteral(f, "}"))
            .toStatement());
        statements.add(new ExpressionBuilder(f, buffer)
            .method("toString")
            .toReturnStatement());
        return f.newMethodDeclaration(
                null,
                new AttributeBuilder(f)
                    .annotation(bless(Override.class))
                    .Public()
                    .toAttributes(),
                bless(String.class),
                f.newSimpleName("toString"),
                Collections.<FormalParameterDeclaration>emptyList(),
                statements);
    }

    /**
     * 指定のプロパティに対するコピーメソッドの内容を、対応するJavaのDOMとして返す。
     * @param fromObject 入力元のオブジェクト
     * @param fromName 入力元の情報を持つプロパティ
     * @param toName 出力先のプロパティ
     * @param type コピーするプロパティの種類
     * @return 対応するDOM
     */
    protected Statement createCopierFor(
            Expression fromObject,
            String fromName,
            String toName,
            PropertyType type) {
        Expression to = new ExpressionBuilder(f, f.newThis())
            .field(common.getFieldNameOf(toName, type))
            .toExpression();
        Expression from = new ExpressionBuilder(f, fromObject)
            .field(common.getFieldNameOf(fromName, type))
            .toExpression();
        return new ExpressionBuilder(f, to)
            .method(Constants.NAME_OPTION_COPIER, from)
            .toStatement();
    }

    /**
     * 指定のオブジェクトのプロパティを、このオブジェクトに書き出す文のDOMを生成して返す。
     * @param fromObject 入力元のオブジェクト、{@code null}の場合は初期値を利用する
     * @param fromName 入力元の情報を持つプロパティ、{@code null}の場合は初期値を利用する
     * @param toName 出力先のプロパティ
     * @param type コピーするプロパティの種類
     * @return 対応するDOM
     */
    protected Statement createImporterFor(
            Expression fromObject,
            String fromName,
            String toName,
            PropertyType type) {
        Expression to = f.newFieldAccessExpression(
                f.newThis(),
                common.getFieldNameOf(toName, type));
        if (fromObject == null || fromName == null) {
            return new ExpressionBuilder(f, to)
                .method(Constants.NAME_OPTION_ERASER)
                .toStatement();
        } else {
            Expression from = new ExpressionBuilder(f, fromObject)
                .method(common.getOptionGetterNameOf(fromName, type))
                .toExpression();
            return new ExpressionBuilder(f, to)
                .method(Constants.NAME_OPTION_COPIER, from)
                .toStatement();
        }
    }

    /**
     * このオブジェクトのプロパティを、指定のオブジェクトに書き出す文のDOMを生成して返す。
     * @param fromName 入力元の情報を持つプロパティ
     * @param toObject 出力先のオブジェクト
     * @param toName 出力先のプロパティ
     * @param type コピーするプロパティの種類
     * @return 対応するDOM
     */
    protected Statement createExporterFor(
            String fromName,
            Expression toObject,
            String toName,
            PropertyType type) {
        Expression from = new ExpressionBuilder(f, f.newThis())
            .field(common.getFieldNameOf(fromName, type))
            .toExpression();
        Expression to = new ExpressionBuilder(f, toObject)
            .method(common.getOptionGetterNameOf(toName, type))
            .toExpression();
        return new ExpressionBuilder(f, to)
            .method(Constants.NAME_OPTION_COPIER, from)
            .toStatement();
    }

    /**
     * 対象のモデルに対するインターフェースの一覧を返す。
     * <p>
     * この実装では、空のリストを返す。
     * </p>
     * @param model 対象のモデル
     * @return 対応するインターフェース一覧
     */
    protected List<Type> createSuperInterfaces(T model) {
        return Collections.singletonList(bless(Writable.class));
    }

    /**
     * 対象のプロパティに対するパラメータ名を返す。
     * <p>
     * この実装では、プロパティに対するフィールドと同じ名前を返す。
     * </p>
     * @param property 対象のプロパティ
     * @return 対応する名前
     */
    protected final SimpleName createNameForParameter(ModelProperty property) {
        return common.getFieldNameOf(property.getName(), property.getType());
    }

    /**
     * 対象のプロパティに対するフィールドの初期化式を返す。
     * @param property 対象のプロパティ
     * @return 対応する初期化式、不要の場合は{@code null}
     */
    protected Expression createInitializerForField(ModelProperty property) {
        return common.getInitialValue(property.getType(), imports);
    }

    /**
     * 対象のプロパティに対するゲッターの本体を返す。
     * @param property 対象のプロパティ
     * @return 対応するブロック
     */
    protected Block createBodyForGetter(ModelProperty property) {
        List<Statement> statements = new ArrayList<Statement>();
        statements.add(thisFieldFor(property)
            .method(Constants.NAME_OPTION_EXTRACTOR)
            .toReturnStatement());
        return f.newBlock(statements);
    }

    /**
     * 対象のプロパティに対するセッターの本体を返す。
     * @param property 対象のプロパティ
     * @param parameterName 利用するパラメータ名
     * @return 対応するブロック
     */
    protected Block createBodyForSetter(
            ModelProperty property,
            SimpleName parameterName) {
        List<Statement> statements = new ArrayList<Statement>();
        statements.add(thisFieldFor(property)
            .method(Constants.NAME_OPTION_MODIFIER, parameterName)
            .toStatement());
        return f.newBlock(statements);
    }

    /**
     * 対象のプロパティに対する代替ゲッターの本体を返す。
     * @param property 対象のプロパティ
     * @return 対応するブロック
     */
    protected Block createBodyForAltGetter(ModelProperty property) {
        List<Statement> statements = new ArrayList<Statement>();
        statements.add(thisFieldFor(property)
            .method(common.toAltMemberName(Constants.NAME_OPTION_EXTRACTOR, property.getType()))
            .toReturnStatement());
        return f.newBlock(statements);
    }

    /**
     * 対象のプロパティに対するセッターの本体を返す。
     * @param property 対象のプロパティ
     * @param parameterName 利用するパラメータ名
     * @return 対応するブロック
     */
    protected Block createBodyForAltSetter(
            ModelProperty property,
            SimpleName parameterName) {
        return createBodyForSetter(property, parameterName);
    }

    /**
     * 対象のプロパティに対するオプションゲッターの本体を返す。
     * @param property 対象のプロパティ
     * @return 対応するブロック
     */
    protected Block createBodyForOptionGetter(ModelProperty property) {
        Statement result = thisFieldFor(property)
            .toReturnStatement();
        return f.newBlock(Collections.singletonList(result));
    }

    /**
     * 対象のプロパティに対するセッターの本体を返す。
     * @param property 対象のプロパティ
     * @param parameterName 利用するパラメータ名
     * @return 対応するブロック
     */
    protected Block createBodyForOptionSetter(
            ModelProperty property,
            SimpleName parameterName) {
        Statement result = thisFieldFor(property)
            .method(Constants.NAME_OPTION_COPIER, parameterName)
            .toStatement();
        return f.newBlock(Collections.singletonList(result));
    }

    private ExpressionBuilder thisFieldFor(ModelProperty property) {
        assert property != null;
        SimpleName fieldName = common.getFieldNameOf(
                property.getName(),
                property.getType());
        return new ExpressionBuilder(f, f.newThis()).field(fieldName);
    }

    /**
     * 対象のモデルに対する型のJavadocを返す。
     * <p>
     * この実装では常に{@code null}を返す。
     * </p>
     * @param model 対象のモデル
     * @return 対応するJavadoc、存在しない場合は{@code null}
     */
    protected Javadoc createJavadocForModel(T model) {
        return null;
    }

    /**
     * 対象のプロパティに対するフィールドのJavadocを返す。
     * <p>
     * この実装では常に{@code null}を返す。
     * </p>
     * @param property 対象のプロパティ
     * @return 対応するJavadoc、存在しない場合は{@code null}
     */
    protected Javadoc createJavadocForField(ModelProperty property) {
        return null;
    }

    /**
     * 対象のプロパティに対するゲッターのJavadocを返す。
     * <p>
     * この実装では常に{@code null}を返す。
     * </p>
     * @param property 対象のプロパティ
     * @return 対応するJavadoc、存在しない場合は{@code null}
     */
    protected Javadoc createJavadocForGetter(ModelProperty property) {
        return null;
    }

    /**
     * 対象のプロパティに対するセッターのJavadocを返す。
     * <p>
     * この実装では常に{@code null}を返す。
     * </p>
     * @param property 対象のプロパティ
     * @return 対応するJavadoc、存在しない場合は{@code null}
     */
    protected Javadoc createJavadocForSetter(ModelProperty property) {
        return null;
    }

    /**
     * 対象のプロパティに対するオプションオブジェクトゲッターのJavadocを返す。
     * <p>
     * この実装では常に{@code null}を返す。
     * </p>
     * @param property 対象のプロパティ
     * @return 対応するJavadoc、存在しない場合は{@code null}
     */
    protected Javadoc createJavadocForOptionGetter(ModelProperty property) {
        SimpleName getter = common.getGetterNameOf(
                property.getName(),
                property.getType());
        return new JavadocBuilder(f)
            .linkMethod(getter)
            .text("の情報を")
            .code("null")
            .text("も表現可能な形式で返す。")
            .returns()
                .text("オプション形式の")
                .linkMethod(getter)
            .toJavadoc();
    }

    /**
     * 対象のプロパティに対するオプションオブジェクトセッターのJavadocを返す。
     * <p>
     * この実装では常に{@code null}を返す。
     * </p>
     * @param property 対象のプロパティ
     * @return 対応するJavadoc、存在しない場合は{@code null}
     */
    protected Javadoc createJavadocForOptionSetter(ModelProperty property) {
        SimpleName setter = common.getSetterNameOf(
                property.getName(),
                property.getType());
        return new JavadocBuilder(f)
            .linkMethod(setter, bless(common.getRawType(property.getType())))
            .text("を")
            .code("null")
            .text("が指定可能なオプションの形式で設定する。")
            .param(createNameForParameter(property))
                .text("設定する値、消去する場合は")
                .code("null")
            .toJavadoc();
    }

    /**
     * 対象のモデルに対する型の注釈一覧を返す。
     * <p>
     * この実装では常に空のリストを返す。
     * </p>
     * @param model 対象のモデル
     * @return 対応する注釈一覧
     */
    protected List<Annotation> createAnnotationsForModel(T model) {
        return Collections.emptyList();
    }

    /**
     * 対象のプロパティに対するフィールドの注釈一覧を返す。
     * <p>
     * この実装では常に空のリストを返す。
     * </p>
     * @param property 対象のプロパティ
     * @return 対応する注釈一覧
     */
    protected List<Annotation> createAnnotationsForField(ModelProperty property) {
        return Collections.emptyList();
    }

    /**
     * 対象のプロパティに対するゲッターの注釈一覧を返す。
     * <p>
     * この実装では常に空のリストを返す。
     * </p>
     * @param property 対象のプロパティ
     * @return 対応する注釈一覧
     */
    protected List<Annotation> createAnnotationsForGetter(ModelProperty property) {
        return Collections.emptyList();
    }

    /**
     * 対象のプロパティに対するセッターの注釈一覧を返す。
     * <p>
     * この実装では常に空のリストを返す。
     * </p>
     * @param property 対象のプロパティ
     * @return 対応する注釈一覧
     */
    protected List<Annotation> createAnnotationsForSetter(ModelProperty property) {
        return Collections.emptyList();
    }

    /**
     * 対象のプロパティに対するオプションゲッターの注釈一覧を返す。
     * <p>
     * この実装では常に空のリストを返す。
     * </p>
     * @param property 対象のプロパティ
     * @return 対応する注釈一覧
     */
    protected List<Annotation> createAnnotationsForOptionGetter(ModelProperty property) {
        return Collections.emptyList();
    }

    /**
     * 対象のプロパティに対するオプションセッターの注釈一覧を返す。
     * <p>
     * この実装では常に空のリストを返す。
     * </p>
     * @param property 対象のプロパティ
     * @return 対応する注釈一覧
     */
    protected List<Annotation> createAnnotationsForOptionSetter(ModelProperty property) {
        return Collections.emptyList();
    }
}
