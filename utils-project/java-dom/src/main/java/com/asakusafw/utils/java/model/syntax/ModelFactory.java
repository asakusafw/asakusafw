/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.utils.java.model.syntax;

import java.util.List;

/**
 * {@link Model}を生成するファクトリのインターフェース。
 */
public interface ModelFactory {

    /**
     * 新しい{@link AlternateConstructorInvocation}を生成して返す。
     * @param arguments
     *     実引数の一覧
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code arguments}に{@code null}が指定された場合
     */
    AlternateConstructorInvocation newAlternateConstructorInvocation(
            Expression... arguments
    );

    /**
     * 新しい{@link AlternateConstructorInvocation}を生成して返す。
     * @param arguments
     *     実引数の一覧
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code arguments}に{@code null}が指定された場合
     */
    AlternateConstructorInvocation newAlternateConstructorInvocation(
            List<? extends Expression> arguments
    );

    /**
     * 新しい{@link AlternateConstructorInvocation}を生成して返す。
     * @param typeArguments
     *     型引数の一覧
     * @param arguments
     *     実引数の一覧
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code typeArguments}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code arguments}に{@code null}が指定された場合
     */
    AlternateConstructorInvocation newAlternateConstructorInvocation(
            List<? extends Type> typeArguments,
            List<? extends Expression> arguments
    );

    /**
     * 新しい{@link AnnotationDeclaration}を生成して返す。
     * @param javadoc
     *     ドキュメンテーションコメント、
     *     ただしドキュメンテーションコメントが存在しない場合は{@code null}
     * @param modifiers
     *     修飾子および注釈の一覧
     * @param name
     *     型の単純名
     * @param bodyDeclarations
     *     メンバの一覧
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code modifiers}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code bodyDeclarations}に{@code null}が指定された場合
     */
    AnnotationDeclaration newAnnotationDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            SimpleName name,
            List<? extends TypeBodyDeclaration> bodyDeclarations
    );

    /**
     * 新しい{@link AnnotationElement}を生成して返す。
     * @param name
     *     注釈要素の名前
     * @param expression
     *     注釈要素値の式
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code expression}に{@code null}が指定された場合
     */
    AnnotationElement newAnnotationElement(
            SimpleName name,
            Expression expression
    );

    /**
     * 新しい{@link AnnotationElementDeclaration}を生成して返す。
     * @param javadoc
     *     ドキュメンテーションコメント、
     *     ただしドキュメンテーションコメントが存在しない場合は{@code null}
     * @param modifiers
     *     修飾子および注釈の一覧
     * @param type
     *     注釈要素の型
     * @param name
     *     注釈要素の名前
     * @param defaultExpression
     *     注釈要素の規定値、
     *     ただし規定値が存在しない場合は{@code null}
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code modifiers}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code type}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     */
    AnnotationElementDeclaration newAnnotationElementDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            Type type,
            SimpleName name,
            Expression defaultExpression
    );

    /**
     * 新しい{@link ArrayAccessExpression}を生成して返す。
     * @param array
     *     配列式
     * @param index
     *     添え字式
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code array}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code index}に{@code null}が指定された場合
     */
    ArrayAccessExpression newArrayAccessExpression(
            Expression array,
            Expression index
    );

    /**
     * 新しい{@link ArrayCreationExpression}を生成して返す。
     * @param type
     *     生成する配列の型
     * @param arrayInitializer
     *     配列初期化子、
     *     ただし配列初期化子が指定されない場合は{@code null}
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code type}に{@code null}が指定された場合
     */
    ArrayCreationExpression newArrayCreationExpression(
            ArrayType type,
            ArrayInitializer arrayInitializer
    );

    /**
     * 新しい{@link ArrayCreationExpression}を生成して返す。
     * @param type
     *     生成する配列の型
     * @param dimensionExpressions
     *     要素数指定式
     * @param arrayInitializer
     *     配列初期化子、
     *     ただし配列初期化子が指定されない場合は{@code null}
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code type}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code dimensionExpressions}に{@code null}が指定された場合
     */
    ArrayCreationExpression newArrayCreationExpression(
            ArrayType type,
            List<? extends Expression> dimensionExpressions,
            ArrayInitializer arrayInitializer
    );

    /**
     * 新しい{@link ArrayInitializer}を生成して返す。
     * @param elements
     *     要素の一覧
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code elements}に{@code null}が指定された場合
     */
    ArrayInitializer newArrayInitializer(
            Expression... elements
    );

    /**
     * 新しい{@link ArrayInitializer}を生成して返す。
     * @param elements
     *     要素の一覧
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code elements}に{@code null}が指定された場合
     */
    ArrayInitializer newArrayInitializer(
            List<? extends Expression> elements
    );

    /**
     * 新しい{@link ArrayType}を生成して返す。
     * @param componentType
     *     要素型
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code componentType}に{@code null}が指定された場合
     */
    ArrayType newArrayType(
            Type componentType
    );

    /**
     * 新しい{@link AssertStatement}を生成して返す。
     * @param expression
     *     表明式
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code expression}に{@code null}が指定された場合
     */
    AssertStatement newAssertStatement(
            Expression expression
    );

    /**
     * 新しい{@link AssertStatement}を生成して返す。
     * @param expression
     *     表明式
     * @param message
     *     メッセージ式、
     *     ただしメッセージ式が省略された場合は{@code null}
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code expression}に{@code null}が指定された場合
     */
    AssertStatement newAssertStatement(
            Expression expression,
            Expression message
    );

    /**
     * 新しい{@link AssignmentExpression}を生成して返す。
     * @param leftHandSide
     *     左辺式
     * @param rightHandSide
     *     右辺式
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code leftHandSide}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code rightHandSide}に{@code null}が指定された場合
     */
    AssignmentExpression newAssignmentExpression(
            Expression leftHandSide,
            Expression rightHandSide
    );

    /**
     * 新しい{@link AssignmentExpression}を生成して返す。
     * @param leftHandSide
     *     左辺式
     * @param operator
     *     単純代入演算子、または複合する演算子
     * @param rightHandSide
     *     右辺式
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code leftHandSide}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code operator}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code rightHandSide}に{@code null}が指定された場合
     */
    AssignmentExpression newAssignmentExpression(
            Expression leftHandSide,
            InfixOperator operator,
            Expression rightHandSide
    );

    /**
     * 新しい{@link BasicType}を生成して返す。
     * @param typeKind
     *     基本型の種類
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code typeKind}に{@code null}が指定された場合
     */
    BasicType newBasicType(
            BasicTypeKind typeKind
    );

    /**
     * 新しい{@link Block}を生成して返す。
     * @param statements
     *     文の一覧
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code statements}に{@code null}が指定された場合
     */
    Block newBlock(
            Statement... statements
    );

    /**
     * 新しい{@link Block}を生成して返す。
     * @param statements
     *     文の一覧
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code statements}に{@code null}が指定された場合
     */
    Block newBlock(
            List<? extends Statement> statements
    );

    /**
     * 新しい{@link BlockComment}を生成して返す。
     * @param string
     *     コメント文字列
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code string}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code string}に空が指定された場合
     */
    BlockComment newBlockComment(
            String string
    );

    /**
     * 新しい{@link BreakStatement}を生成して返す。
     * @return 生成した要素
     */
    BreakStatement newBreakStatement(
    );

    /**
     * 新しい{@link BreakStatement}を生成して返す。
     * @param target
     *     分岐先ラベル、
     *     ただし分岐先ラベルが指定されない場合は{@code null}
     * @return 生成した要素
     */
    BreakStatement newBreakStatement(
            SimpleName target
    );

    /**
     * 新しい{@link CastExpression}を生成して返す。
     * @param type
     *     キャスト対象の型
     * @param expression
     *     演算項
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code type}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code expression}に{@code null}が指定された場合
     */
    CastExpression newCastExpression(
            Type type,
            Expression expression
    );

    /**
     * 新しい{@link CatchClause}を生成して返す。
     * @param parameter
     *     例外仮引数
     * @param body
     *     {@code catch}ブロック
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code parameter}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code body}に{@code null}が指定された場合
     */
    CatchClause newCatchClause(
            FormalParameterDeclaration parameter,
            Block body
    );

    /**
     * 新しい{@link ClassBody}を生成して返す。
     * @param bodyDeclarations
     *     メンバの一覧
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code bodyDeclarations}に{@code null}が指定された場合
     */
    ClassBody newClassBody(
            List<? extends TypeBodyDeclaration> bodyDeclarations
    );

    /**
     * 新しい{@link ClassDeclaration}を生成して返す。
     * @param javadoc
     *     ドキュメンテーションコメント、
     *     ただしドキュメンテーションコメントが存在しない場合は{@code null}
     * @param modifiers
     *     修飾子および注釈の一覧
     * @param name
     *     型の単純名
     * @param superClass
     *     親クラス、
     *     ただし親クラスが明示されない場合は{@code null}
     * @param superInterfaceTypes
     *     親インターフェースの一覧
     * @param bodyDeclarations
     *     メンバの一覧
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code modifiers}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code superInterfaceTypes}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code bodyDeclarations}に{@code null}が指定された場合
     */
    ClassDeclaration newClassDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            SimpleName name,
            Type superClass,
            List<? extends Type> superInterfaceTypes,
            List<? extends TypeBodyDeclaration> bodyDeclarations
    );

    /**
     * 新しい{@link ClassDeclaration}を生成して返す。
     * @param javadoc
     *     ドキュメンテーションコメント、
     *     ただしドキュメンテーションコメントが存在しない場合は{@code null}
     * @param modifiers
     *     修飾子および注釈の一覧
     * @param name
     *     型の単純名
     * @param typeParameters
     *     仮型引数宣言の一覧
     * @param superClass
     *     親クラス、
     *     ただし親クラスが明示されない場合は{@code null}
     * @param superInterfaceTypes
     *     親インターフェースの一覧
     * @param bodyDeclarations
     *     メンバの一覧
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code modifiers}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code typeParameters}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code superInterfaceTypes}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code bodyDeclarations}に{@code null}が指定された場合
     */
    ClassDeclaration newClassDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            SimpleName name,
            List<? extends TypeParameterDeclaration> typeParameters,
            Type superClass,
            List<? extends Type> superInterfaceTypes,
            List<? extends TypeBodyDeclaration> bodyDeclarations
    );

    /**
     * 新しい{@link ClassInstanceCreationExpression}を生成して返す。
     * @param type
     *     インスタンスを生成する型
     * @param arguments
     *     実引数の一覧
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code type}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code arguments}に{@code null}が指定された場合
     */
    ClassInstanceCreationExpression newClassInstanceCreationExpression(
            Type type,
            Expression... arguments
    );

    /**
     * 新しい{@link ClassInstanceCreationExpression}を生成して返す。
     * @param type
     *     インスタンスを生成する型
     * @param arguments
     *     実引数の一覧
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code type}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code arguments}に{@code null}が指定された場合
     */
    ClassInstanceCreationExpression newClassInstanceCreationExpression(
            Type type,
            List<? extends Expression> arguments
    );

    /**
     * 新しい{@link ClassInstanceCreationExpression}を生成して返す。
     * @param qualifier
     *     限定式、
     *     ただし限定式が指定されない場合は{@code null}
     * @param typeArguments
     *     型引数の一覧
     * @param type
     *     インスタンスを生成する型
     * @param arguments
     *     実引数の一覧
     * @param body
     *     匿名クラス本体、
     *     ただし匿名クラス本体が指定されない場合は{@code null}
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code typeArguments}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code type}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code arguments}に{@code null}が指定された場合
     */
    ClassInstanceCreationExpression newClassInstanceCreationExpression(
            Expression qualifier,
            List<? extends Type> typeArguments,
            Type type,
            List<? extends Expression> arguments,
            ClassBody body
    );

    /**
     * 新しい{@link ClassLiteral}を生成して返す。
     * @param type
     *     対象の型
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code type}に{@code null}が指定された場合
     */
    ClassLiteral newClassLiteral(
            Type type
    );

    /**
     * 新しい{@link CompilationUnit}を生成して返す。
     * @param packageDeclaration
     *     パッケージ宣言、
     *     ただし無名パッケージ上に存在するコンパイル単位を表現する場合は{@code null}
     * @param importDeclarations
     *     このコンパイル単位で宣言されるインポート宣言の一覧
     * @param typeDeclarations
     *     このコンパイル単位で宣言される型の一覧
     * @param comments
     *     このコンパイル単位に記述されたコメントの一覧
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code importDeclarations}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code typeDeclarations}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code comments}に{@code null}が指定された場合
     */
    CompilationUnit newCompilationUnit(
            PackageDeclaration packageDeclaration,
            List<? extends ImportDeclaration> importDeclarations,
            List<? extends TypeDeclaration> typeDeclarations,
            List<? extends Comment> comments
    );

    /**
     * 新しい{@link ConditionalExpression}を生成して返す。
     * @param condition
     *     条件式
     * @param thenExpression
     *     条件成立時に評価される式
     * @param elseExpression
     *     条件不成立時に評価される式
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code condition}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code thenExpression}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code elseExpression}に{@code null}が指定された場合
     */
    ConditionalExpression newConditionalExpression(
            Expression condition,
            Expression thenExpression,
            Expression elseExpression
    );

    /**
     * 新しい{@link ConstructorDeclaration}を生成して返す。
     * @param javadoc
     *     ドキュメンテーションコメント、
     *     ただしドキュメンテーションコメントが存在しない場合は{@code null}
     * @param modifiers
     *     修飾子および注釈の一覧
     * @param name
     *     メソッドまたはコンストラクタの名前
     * @param formalParameters
     *     仮引数宣言の一覧
     * @param statements
     *     コンストラクタ本体
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code modifiers}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code formalParameters}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code statements}に{@code null}が指定された場合
     */
    ConstructorDeclaration newConstructorDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            SimpleName name,
            List<? extends FormalParameterDeclaration> formalParameters,
            List<? extends Statement> statements
    );

    /**
     * 新しい{@link ConstructorDeclaration}を生成して返す。
     * @param javadoc
     *     ドキュメンテーションコメント、
     *     ただしドキュメンテーションコメントが存在しない場合は{@code null}
     * @param modifiers
     *     修飾子および注釈の一覧
     * @param typeParameters
     *     型引数宣言の一覧
     * @param name
     *     メソッドまたはコンストラクタの名前
     * @param formalParameters
     *     仮引数宣言の一覧
     * @param exceptionTypes
     *     例外型宣言の一覧
     * @param body
     *     コンストラクタ本体
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code modifiers}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code typeParameters}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code formalParameters}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code exceptionTypes}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code body}に{@code null}が指定された場合
     */
    ConstructorDeclaration newConstructorDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            List<? extends TypeParameterDeclaration> typeParameters,
            SimpleName name,
            List<? extends FormalParameterDeclaration> formalParameters,
            List<? extends Type> exceptionTypes,
            Block body
    );

    /**
     * 新しい{@link ContinueStatement}を生成して返す。
     * @return 生成した要素
     */
    ContinueStatement newContinueStatement();

    /**
     * 新しい{@link ContinueStatement}を生成して返す。
     * @param target
     *     分岐先ラベル、
     *     ただし分岐先ラベルが指定されない場合は{@code null}
     * @return 生成した要素
     */
    ContinueStatement newContinueStatement(
            SimpleName target
    );

    /**
     * 新しい{@link DoStatement}を生成して返す。
     * @param body
     *     ループ本体
     * @param condition
     *     条件式
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code body}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code condition}に{@code null}が指定された場合
     */
    DoStatement newDoStatement(
            Statement body,
            Expression condition
    );

    /**
     * 新しい{@link DocBlock}を生成して返す。
     * @param tag
     *     タグ文字列
     * @param elements
     *     インライン要素の一覧
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code tag}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code elements}に{@code null}が指定された場合
     */
    DocBlock newDocBlock(
            String tag,
            List<? extends DocElement> elements
    );

    /**
     * 新しい{@link DocField}を生成して返す。
     * @param type
     *     フィールドを宣言した型、
     *     ただし宣言型が指定されない場合は{@code null}
     * @param name
     *     フィールドの名称
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     */
    DocField newDocField(
            Type type,
            SimpleName name
    );

    /**
     * 新しい{@link DocMethod}を生成して返す。
     * @param type
     *     メソッドまたはコンストラクタの宣言型、
     *     ただし宣言型が指定されない場合は{@code null}
     * @param name
     *     メソッドまたはコンストラクタの名前
     * @param formalParameters
     *     メソッドまたはコンストラクタの仮引数宣言の一覧
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code formalParameters}に{@code null}が指定された場合
     */
    DocMethod newDocMethod(
            Type type,
            SimpleName name,
            List<? extends DocMethodParameter> formalParameters
    );

    /**
     * 新しい{@link DocMethodParameter}を生成して返す。
     * @param type
     *     仮引数の型
     * @param name
     *     仮引数の名前、
     *     ただし仮引数の名前が省略される場合は{@code null}
     * @param variableArity
     *     可変長引数
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code type}に{@code null}が指定された場合
     */
    DocMethodParameter newDocMethodParameter(
            Type type,
            SimpleName name,
            boolean variableArity
    );

    /**
     * 新しい{@link DocText}を生成して返す。
     * @param string
     *     テキストを構成する文字列
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code string}に{@code null}が指定された場合
     */
    DocText newDocText(
            String string
    );

    /**
     * 新しい{@link EmptyStatement}を生成して返す。
     * @return 生成した要素
     */
    EmptyStatement newEmptyStatement(

    );

    /**
     * 新しい{@link EnhancedForStatement}を生成して返す。
     * @param parameter
     *     ループ変数
     * @param expression
     *     ループ対象式
     * @param body
     *     ループ本体
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code parameter}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code expression}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code body}に{@code null}が指定された場合
     */
    EnhancedForStatement newEnhancedForStatement(
            FormalParameterDeclaration parameter,
            Expression expression,
            Statement body
    );

    /**
     * 新しい{@link EnumConstantDeclaration}を生成して返す。
     * @param javadoc
     *     ドキュメンテーションコメント、
     *     ただしドキュメンテーションコメントが存在しない場合は{@code null}
     * @param name
     *     列挙定数の名前
     * @param arguments
     *     コンストラクタ引数の一覧
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code arguments}に{@code null}が指定された場合
     */
    EnumConstantDeclaration newEnumConstantDeclaration(
            Javadoc javadoc,
            SimpleName name,
            Expression... arguments
    );

    /**
     * 新しい{@link EnumConstantDeclaration}を生成して返す。
     * @param javadoc
     *     ドキュメンテーションコメント、
     *     ただしドキュメンテーションコメントが存在しない場合は{@code null}
     * @param modifiers
     *     修飾子および注釈の一覧
     * @param name
     *     列挙定数の名前
     * @param arguments
     *     コンストラクタ引数の一覧
     * @param body
     *     クラス本体の宣言、
     *     ただしクラスの本体が宣言されない場合は{@code null}
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code modifiers}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code arguments}に{@code null}が指定された場合
     */
    EnumConstantDeclaration newEnumConstantDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            SimpleName name,
            List<? extends Expression> arguments,
            ClassBody body
    );

    /**
     * 新しい{@link EnumDeclaration}を生成して返す。
     * @param javadoc
     *     ドキュメンテーションコメント、
     *     ただしドキュメンテーションコメントが存在しない場合は{@code null}
     * @param modifiers
     *     修飾子および注釈の一覧
     * @param name
     *     型の単純名
     * @param constantDeclarations
     *     列挙定数の一覧
     * @param bodyDeclarations
     *     メンバの一覧
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code modifiers}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code constantDeclarations}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code bodyDeclarations}に{@code null}が指定された場合
     */
    EnumDeclaration newEnumDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            SimpleName name,
            List<? extends EnumConstantDeclaration> constantDeclarations,
            TypeBodyDeclaration... bodyDeclarations
    );

    /**
     * 新しい{@link EnumDeclaration}を生成して返す。
     * @param javadoc
     *     ドキュメンテーションコメント、
     *     ただしドキュメンテーションコメントが存在しない場合は{@code null}
     * @param modifiers
     *     修飾子および注釈の一覧
     * @param name
     *     型の単純名
     * @param superInterfaceTypes
     *     親インターフェースの一覧
     * @param constantDeclarations
     *     列挙定数の一覧
     * @param bodyDeclarations
     *     メンバの一覧
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code modifiers}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code superInterfaceTypes}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code constantDeclarations}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code bodyDeclarations}に{@code null}が指定された場合
     */
    EnumDeclaration newEnumDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            SimpleName name,
            List<? extends Type> superInterfaceTypes,
            List<? extends EnumConstantDeclaration> constantDeclarations,
            List<? extends TypeBodyDeclaration> bodyDeclarations
    );

    /**
     * 新しい{@link ExpressionStatement}を生成して返す。
     * @param expression
     *     内包する式
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code expression}に{@code null}が指定された場合
     */
    ExpressionStatement newExpressionStatement(
            Expression expression
    );

    /**
     * 新しい{@link FieldAccessExpression}を生成して返す。
     * @param qualifier
     *     限定式
     * @param name
     *     フィールドの名前
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code qualifier}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     */
    FieldAccessExpression newFieldAccessExpression(
            Expression qualifier,
            SimpleName name
    );

    /**
     * 新しい{@link FieldDeclaration}を生成して返す。
     * @param javadoc
     *     ドキュメンテーションコメント、
     *     ただしドキュメンテーションコメントが存在しない場合は{@code null}
     * @param modifiers
     *     修飾子および注釈の一覧
     * @param type
     *     フィールドの型
     * @param name
     *     変数の名前
     * @param initializer
     *     初期化式、
     *     ただし初期化式が指定されない場合は{@code null}
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code modifiers}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code type}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     */
    FieldDeclaration newFieldDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            Type type,
            SimpleName name,
            Expression initializer
    );

    /**
     * 新しい{@link FieldDeclaration}を生成して返す。
     * @param javadoc
     *     ドキュメンテーションコメント、
     *     ただしドキュメンテーションコメントが存在しない場合は{@code null}
     * @param modifiers
     *     修飾子および注釈の一覧
     * @param type
     *     フィールドの型
     * @param variableDeclarators
     *     宣言するフィールドの一覧
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code modifiers}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code type}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code variableDeclarators}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code variableDeclarators}に空が指定された場合
     */
    FieldDeclaration newFieldDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            Type type,
            List<? extends VariableDeclarator> variableDeclarators
    );

    /**
     * 新しい{@link ForStatement}を生成して返す。
     * @param initialization
     *     ループ初期化部、
     *     ただしループ初期化部が指定されない場合は{@code null}
     * @param condition
     *     ループ条件式、
     *     ただしループ条件が指定されない場合は{@code null}
     * @param update
     *     ループ更新部、
     *     ただしループ更新部が指定されない場合は{@code null}
     * @param body
     *     ループ本体
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code body}に{@code null}が指定された場合
     */
    ForStatement newForStatement(
            ForInitializer initialization,
            Expression condition,
            StatementExpressionList update,
            Statement body
    );

    /**
     * 新しい{@link FormalParameterDeclaration}を生成して返す。
     * @param type
     *     宣言する変数の型
     * @param name
     *     仮引数の名前
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code type}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     */
    FormalParameterDeclaration newFormalParameterDeclaration(
            Type type,
            SimpleName name
    );

    /**
     * 新しい{@link FormalParameterDeclaration}を生成して返す。
     * @param modifiers
     *     修飾子および注釈の一覧
     * @param type
     *     宣言する変数の型
     * @param variableArity
     *     可変長引数
     * @param name
     *     仮引数の名前
     * @param extraDimensions
     *     追加次元数の宣言
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code modifiers}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code type}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code extraDimensions}に負の値が指定された場合
     */
    FormalParameterDeclaration newFormalParameterDeclaration(
            List<? extends Attribute> modifiers,
            Type type,
            boolean variableArity,
            SimpleName name,
            int extraDimensions
    );

    /**
     * 新しい{@link IfStatement}を生成して返す。
     * @param condition
     *     条件式
     * @param thenStatement
     *     条件成立時に実行される文
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code condition}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code thenStatement}に{@code null}が指定された場合
     */
    IfStatement newIfStatement(
            Expression condition,
            Statement thenStatement
    );

    /**
     * 新しい{@link IfStatement}を生成して返す。
     * @param condition
     *     条件式
     * @param thenStatement
     *     条件成立時に実行される文
     * @param elseStatement
     *     条件不成立時に実行される文、
     *     ただしこの文が{@code if-then}文である場合は{@code null}
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code condition}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code thenStatement}に{@code null}が指定された場合
     */
    IfStatement newIfStatement(
            Expression condition,
            Statement thenStatement,
            Statement elseStatement
    );

    /**
     * 新しい{@link ImportDeclaration}を生成して返す。
     * @param importKind
     *     インポートの種類
     * @param name
     *     インポートする型およびメンバの名前
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code importKind}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     */
    ImportDeclaration newImportDeclaration(
            ImportKind importKind,
            Name name
    );

    /**
     * 新しい{@link InfixExpression}を生成して返す。
     * @param leftOperand
     *     第一演算項
     * @param operator
     *     二項演算子
     * @param rightOperand
     *     第二演算項
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code leftOperand}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code operator}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code rightOperand}に{@code null}が指定された場合
     */
    InfixExpression newInfixExpression(
            Expression leftOperand,
            InfixOperator operator,
            Expression rightOperand
    );

    /**
     * 新しい{@link InitializerDeclaration}を生成して返す。
     * @param body
     *     初期化子の本体
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code body}に{@code null}が指定された場合
     */
    InitializerDeclaration newInitializerDeclaration(
            List<? extends Statement> body
    );

    /**
     * 新しい{@link InitializerDeclaration}を生成して返す。
     * @param javadoc
     *     ドキュメンテーションコメント、
     *     ただしドキュメンテーションコメントが存在しない場合は{@code null}
     * @param modifiers
     *     修飾子および注釈の一覧
     * @param body
     *     初期化子の本体
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code modifiers}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code body}に{@code null}が指定された場合
     */
    InitializerDeclaration newInitializerDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            Block body
    );

    /**
     * 新しい{@link InstanceofExpression}を生成して返す。
     * @param expression
     *     被演算項
     * @param type
     *     比較対象型
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code expression}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code type}に{@code null}が指定された場合
     */
    InstanceofExpression newInstanceofExpression(
            Expression expression,
            Type type
    );

    /**
     * 新しい{@link InterfaceDeclaration}を生成して返す。
     * @param javadoc
     *     ドキュメンテーションコメント、
     *     ただしドキュメンテーションコメントが存在しない場合は{@code null}
     * @param modifiers
     *     修飾子および注釈の一覧
     * @param name
     *     型の単純名
     * @param superInterfaceTypes
     *     親インターフェースの一覧
     * @param bodyDeclarations
     *     メンバの一覧
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code modifiers}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code superInterfaceTypes}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code bodyDeclarations}に{@code null}が指定された場合
     */
    InterfaceDeclaration newInterfaceDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            SimpleName name,
            List<? extends Type> superInterfaceTypes,
            List<? extends TypeBodyDeclaration> bodyDeclarations
    );

    /**
     * 新しい{@link InterfaceDeclaration}を生成して返す。
     * @param javadoc
     *     ドキュメンテーションコメント、
     *     ただしドキュメンテーションコメントが存在しない場合は{@code null}
     * @param modifiers
     *     修飾子および注釈の一覧
     * @param name
     *     型の単純名
     * @param typeParameters
     *     仮型引数宣言の一覧
     * @param superInterfaceTypes
     *     親インターフェースの一覧
     * @param bodyDeclarations
     *     メンバの一覧
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code modifiers}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code typeParameters}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code superInterfaceTypes}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code bodyDeclarations}に{@code null}が指定された場合
     */
    InterfaceDeclaration newInterfaceDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            SimpleName name,
            List<? extends TypeParameterDeclaration> typeParameters,
            List<? extends Type> superInterfaceTypes,
            List<? extends TypeBodyDeclaration> bodyDeclarations
    );

    /**
     * 新しい{@link Javadoc}を生成して返す。
     * @param blocks
     *     ブロックの一覧
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code blocks}に{@code null}が指定された場合
     */
    Javadoc newJavadoc(
            List<? extends DocBlock> blocks
    );

    /**
     * 新しい{@link LabeledStatement}を生成して返す。
     * @param label
     *     ラベルの名前
     * @param body
     *     対象の文
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code label}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code body}に{@code null}が指定された場合
     */
    LabeledStatement newLabeledStatement(
            SimpleName label,
            Statement body
    );

    /**
     * 新しい{@link LineComment}を生成して返す。
     * @param string
     *     コメント文字列
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code string}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code string}に空が指定された場合
     */
    LineComment newLineComment(
            String string
    );

    /**
     * 新しい{@link Literal}を生成して返す。
     * @param token
     *     このリテラルを構成する字句
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code token}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code token}に空が指定された場合
     */
    Literal newLiteral(
            String token
    );

    /**
     * 新しい{@link LocalClassDeclaration}を生成して返す。
     * @param declaration
     *     宣言するクラス
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code declaration}に{@code null}が指定された場合
     */
    LocalClassDeclaration newLocalClassDeclaration(
            ClassDeclaration declaration
    );

    /**
     * 新しい{@link LocalVariableDeclaration}を生成して返す。
     * @param type
     *     宣言する変数の型
     * @param name
     *     変数の名前
     * @param initializer
     *     初期化式、
     *     ただし初期化式が指定されない場合は{@code null}
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code type}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     */
    LocalVariableDeclaration newLocalVariableDeclaration(
            Type type,
            SimpleName name,
            Expression initializer
    );

    /**
     * 新しい{@link LocalVariableDeclaration}を生成して返す。
     * @param modifiers
     *     修飾子および注釈の一覧
     * @param type
     *     宣言する変数の型
     * @param variableDeclarators
     *     宣言する変数の一覧
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code modifiers}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code type}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code variableDeclarators}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code variableDeclarators}に空が指定された場合
     */
    LocalVariableDeclaration newLocalVariableDeclaration(
            List<? extends Attribute> modifiers,
            Type type,
            List<? extends VariableDeclarator> variableDeclarators
    );

    /**
     * 新しい{@link MarkerAnnotation}を生成して返す。
     * @param type
     *     注釈の型
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code type}に{@code null}が指定された場合
     */
    MarkerAnnotation newMarkerAnnotation(
            NamedType type
    );

    /**
     * 新しい{@link MethodDeclaration}を生成して返す。
     * @param javadoc
     *     ドキュメンテーションコメント、
     *     ただしドキュメンテーションコメントが存在しない場合は{@code null}
     * @param modifiers
     *     修飾子および注釈の一覧
     * @param returnType
     *     戻り値の型
     * @param name
     *     メソッドまたはコンストラクタの名前
     * @param formalParameters
     *     仮引数宣言の一覧
     * @param statements
     *     メソッドまたはコンストラクタ本体、
     *     ただしこのメソッドが本体を提供されない抽象メソッドやインターフェースメソッドである場合は{@code null}
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code modifiers}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code returnType}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code formalParameters}に{@code null}が指定された場合
     */
    MethodDeclaration newMethodDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            Type returnType,
            SimpleName name,
            List<? extends FormalParameterDeclaration> formalParameters,
            List<? extends Statement> statements
    );

    /**
     * 新しい{@link MethodDeclaration}を生成して返す。
     * @param javadoc
     *     ドキュメンテーションコメント、
     *     ただしドキュメンテーションコメントが存在しない場合は{@code null}
     * @param modifiers
     *     修飾子および注釈の一覧
     * @param typeParameters
     *     型引数宣言の一覧
     * @param returnType
     *     戻り値の型
     * @param name
     *     メソッドまたはコンストラクタの名前
     * @param formalParameters
     *     仮引数宣言の一覧
     * @param extraDimensions
     *     戻り値の次元数
     * @param exceptionTypes
     *     例外型宣言の一覧
     * @param body
     *     メソッドまたはコンストラクタ本体、
     *     ただしこのメソッドが本体を提供されない抽象メソッドやインターフェースメソッドである場合は{@code null}
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code modifiers}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code typeParameters}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code returnType}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code formalParameters}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code extraDimensions}に負の値が指定された場合
     * @throws IllegalArgumentException
     *     {@code exceptionTypes}に{@code null}が指定された場合
     */
    MethodDeclaration newMethodDeclaration(
            Javadoc javadoc,
            List<? extends Attribute> modifiers,
            List<? extends TypeParameterDeclaration> typeParameters,
            Type returnType,
            SimpleName name,
            List<? extends FormalParameterDeclaration> formalParameters,
            int extraDimensions,
            List<? extends Type> exceptionTypes,
            Block body
    );

    /**
     * 新しい{@link MethodInvocationExpression}を生成して返す。
     * @param qualifier
     *     限定式、または型限定子、
     *     ただし限定式が指定されない場合(単純メソッド起動)は{@code null}
     * @param name
     *     メソッドの名前
     * @param arguments
     *     実引数の一覧
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code arguments}に{@code null}が指定された場合
     */
    MethodInvocationExpression newMethodInvocationExpression(
            Expression qualifier,
            SimpleName name,
            Expression... arguments
    );

    /**
     * 新しい{@link MethodInvocationExpression}を生成して返す。
     * @param qualifier
     *     限定式、または型限定子、
     *     ただし限定式が指定されない場合(単純メソッド起動)は{@code null}
     * @param name
     *     メソッドの名前
     * @param arguments
     *     実引数の一覧
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code arguments}に{@code null}が指定された場合
     */
    MethodInvocationExpression newMethodInvocationExpression(
            Expression qualifier,
            SimpleName name,
            List<? extends Expression> arguments
    );

    /**
     * 新しい{@link MethodInvocationExpression}を生成して返す。
     * @param qualifier
     *     限定式、または型限定子、
     *     ただし限定式が指定されない場合(単純メソッド起動)は{@code null}
     * @param typeArguments
     *     型引数の一覧
     * @param name
     *     メソッドの名前
     * @param arguments
     *     実引数の一覧
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code typeArguments}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code arguments}に{@code null}が指定された場合
     */
    MethodInvocationExpression newMethodInvocationExpression(
            Expression qualifier,
            List<? extends Type> typeArguments,
            SimpleName name,
            List<? extends Expression> arguments
    );

    /**
     * 新しい{@link Modifier}を生成して返す。
     * @param modifierKind
     *     修飾子の種類
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code modifierKind}に{@code null}が指定された場合
     */
    Modifier newModifier(
            ModifierKind modifierKind
    );

    /**
     * 新しい{@link NamedType}を生成して返す。
     * @param name
     *     型の名前
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     */
    NamedType newNamedType(
            Name name
    );

    /**
     * 新しい{@link NormalAnnotation}を生成して返す。
     * @param type
     *     注釈の型
     * @param elements
     *     注釈要素の一覧
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code type}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code elements}に{@code null}が指定された場合
     */
    NormalAnnotation newNormalAnnotation(
            NamedType type,
            List<? extends AnnotationElement> elements
    );

    /**
     * 新しい{@link PackageDeclaration}を生成して返す。
     * @param name
     *     宣言するパッケージの名称
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     */
    PackageDeclaration newPackageDeclaration(
            Name name
    );

    /**
     * 新しい{@link PackageDeclaration}を生成して返す。
     * @param javadoc
     *     ドキュメンテーションコメント、
     *     ただしドキュメンテーションコメントが存在しない場合は{@code null}
     * @param annotations
     *     注釈の一覧
     * @param name
     *     宣言するパッケージの名称
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code annotations}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     */
    PackageDeclaration newPackageDeclaration(
            Javadoc javadoc,
            List<? extends Annotation> annotations,
            Name name
    );

    /**
     * 新しい{@link ParameterizedType}を生成して返す。
     * @param type
     *     パラメータ化されていない型
     * @param typeArguments
     *     型引数の一覧
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code type}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code typeArguments}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code typeArguments}に空が指定された場合
     */
    ParameterizedType newParameterizedType(
            Type type,
            Type... typeArguments
    );

    /**
     * 新しい{@link ParameterizedType}を生成して返す。
     * @param type
     *     パラメータ化されていない型
     * @param typeArguments
     *     型引数の一覧
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code type}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code typeArguments}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code typeArguments}に空が指定された場合
     */
    ParameterizedType newParameterizedType(
            Type type,
            List<? extends Type> typeArguments
    );

    /**
     * 新しい{@link ParenthesizedExpression}を生成して返す。
     * @param expression
     *     内包する式
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code expression}に{@code null}が指定された場合
     */
    ParenthesizedExpression newParenthesizedExpression(
            Expression expression
    );

    /**
     * 新しい{@link PostfixExpression}を生成して返す。
     * @param operand
     *     後置演算項
     * @param operator
     *     演算子
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code operand}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code operator}に{@code null}が指定された場合
     */
    PostfixExpression newPostfixExpression(
            Expression operand,
            PostfixOperator operator
    );

    /**
     * 新しい{@link QualifiedName}を生成して返す。
     * @param qualifier
     *     限定子
     * @param simpleName
     *     この限定名の末尾にある単純名
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code qualifier}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code simpleName}に{@code null}が指定された場合
     */
    QualifiedName newQualifiedName(
            Name qualifier,
            SimpleName simpleName
    );

    /**
     * 新しい{@link QualifiedType}を生成して返す。
     * @param qualifier
     *     型限定子
     * @param simpleName
     *     型の単純名
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code qualifier}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code simpleName}に{@code null}が指定された場合
     */
    QualifiedType newQualifiedType(
            Type qualifier,
            SimpleName simpleName
    );

    /**
     * 新しい{@link ReturnStatement}を生成して返す。
     * @return 生成した要素
     */
    ReturnStatement newReturnStatement(
    );

    /**
     * 新しい{@link ReturnStatement}を生成して返す。
     * @param expression
     *     返戻値、
     *     ただし返戻値が指定されない場合は{@code null}
     * @return 生成した要素
     */
    ReturnStatement newReturnStatement(
            Expression expression
    );

    /**
     * 新しい{@link SimpleName}を生成して返す。
     * @param string
     *     この単純名を表現する文字列
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code string}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code string}に空が指定された場合
     */
    SimpleName newSimpleName(
            String string
    );

    /**
     * 新しい{@link SingleElementAnnotation}を生成して返す。
     * @param type
     *     注釈の型
     * @param expression
     *     {@code value}要素値の式
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code type}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code expression}に{@code null}が指定された場合
     */
    SingleElementAnnotation newSingleElementAnnotation(
            NamedType type,
            Expression expression
    );

    /**
     * 新しい{@link StatementExpressionList}を生成して返す。
     * @param expressions
     *     式の一覧
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code expressions}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code expressions}に空が指定された場合
     */
    StatementExpressionList newStatementExpressionList(
            Expression... expressions
    );

    /**
     * 新しい{@link StatementExpressionList}を生成して返す。
     * @param expressions
     *     式の一覧
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code expressions}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code expressions}に空が指定された場合
     */
    StatementExpressionList newStatementExpressionList(
            List<? extends Expression> expressions
    );

    /**
     * 新しい{@link Super}を生成して返す。
     * @return 生成した要素
     */
    Super newSuper(
    );

    /**
     * 新しい{@link Super}を生成して返す。
     * @param qualifier
     *     型限定子、
     *     ただし限定子が指定されない場合は{@code null}
     * @return 生成した要素
     */
    Super newSuper(
            NamedType qualifier
    );

    /**
     * 新しい{@link SuperConstructorInvocation}を生成して返す。
     * @param arguments
     *     実引数の一覧
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code arguments}に{@code null}が指定された場合
     */
    SuperConstructorInvocation newSuperConstructorInvocation(
            Expression... arguments
    );

    /**
     * 新しい{@link SuperConstructorInvocation}を生成して返す。
     * @param arguments
     *     実引数の一覧
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code arguments}に{@code null}が指定された場合
     */
    SuperConstructorInvocation newSuperConstructorInvocation(
            List<? extends Expression> arguments
    );

    /**
     * 新しい{@link SuperConstructorInvocation}を生成して返す。
     * @param qualifier
     *     限定式、
     *     ただし限定式が指定されない場合は{@code null}
     * @param typeArguments
     *     型引数の一覧
     * @param arguments
     *     実引数の一覧
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code typeArguments}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code arguments}に{@code null}が指定された場合
     */
    SuperConstructorInvocation newSuperConstructorInvocation(
            Expression qualifier,
            List<? extends Type> typeArguments,
            List<? extends Expression> arguments
    );

    /**
     * 新しい{@link SwitchCaseLabel}を生成して返す。
     * @param expression
     *     {@code case}ラベルの値
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code expression}に{@code null}が指定された場合
     */
    SwitchCaseLabel newSwitchCaseLabel(
            Expression expression
    );

    /**
     * 新しい{@link SwitchDefaultLabel}を生成して返す。
     * @return 生成した要素
     */
    SwitchDefaultLabel newSwitchDefaultLabel(

    );

    /**
     * 新しい{@link SwitchStatement}を生成して返す。
     * @param expression
     *     セレクタ式
     * @param statements
     *     {@code switch}文の本体
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code expression}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code statements}に{@code null}が指定された場合
     */
    SwitchStatement newSwitchStatement(
            Expression expression,
            List<? extends Statement> statements
    );

    /**
     * 新しい{@link SynchronizedStatement}を生成して返す。
     * @param expression
     *     同期オブジェクト
     * @param body
     *     本体ブロック
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code expression}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code body}に{@code null}が指定された場合
     */
    SynchronizedStatement newSynchronizedStatement(
            Expression expression,
            Block body
    );

    /**
     * 新しい{@link This}を生成して返す。
     * @return 生成した要素
     */
    This newThis(
    );

    /**
     * 新しい{@link This}を生成して返す。
     * @param qualifier
     *     型限定子、
     *     ただし限定子が指定されない場合は{@code null}
     * @return 生成した要素
     */
    This newThis(
            NamedType qualifier
    );

    /**
     * 新しい{@link ThrowStatement}を生成して返す。
     * @param expression
     *     例外オブジェクト
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code expression}に{@code null}が指定された場合
     */
    ThrowStatement newThrowStatement(
            Expression expression
    );

    /**
     * 新しい{@link TryStatement}を生成して返す。
     * @param tryBlock
     *     {@code try}節
     * @param catchClauses
     *     {@code catch}節の一覧
     * @param finallyBlock
     *     {@code finally}節、
     *     ただし{@code finally}節が指定されない場合は{@code null}
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code tryBlock}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code catchClauses}に{@code null}が指定された場合
     */
    TryStatement newTryStatement(
            Block tryBlock,
            List<? extends CatchClause> catchClauses,
            Block finallyBlock
    );

    /**
     * 新しい{@link TypeParameterDeclaration}を生成して返す。
     * @param name
     *     型引数の名前
     * @param typeBounds
     *     境界型の一覧
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code typeBounds}に{@code null}が指定された場合
     */
    TypeParameterDeclaration newTypeParameterDeclaration(
            SimpleName name,
            Type... typeBounds
    );

    /**
     * 新しい{@link TypeParameterDeclaration}を生成して返す。
     * @param name
     *     型引数の名前
     * @param typeBounds
     *     境界型の一覧
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code typeBounds}に{@code null}が指定された場合
     */
    TypeParameterDeclaration newTypeParameterDeclaration(
            SimpleName name,
            List<? extends Type> typeBounds
    );

    /**
     * 新しい{@link UnaryExpression}を生成して返す。
     * @param operator
     *     単項演算子
     * @param operand
     *     演算項
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code operator}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code operand}に{@code null}が指定された場合
     */
    UnaryExpression newUnaryExpression(
            UnaryOperator operator,
            Expression operand
    );

    /**
     * 新しい{@link VariableDeclarator}を生成して返す。
     * @param name
     *     変数の名前
     * @param initializer
     *     初期化式、
     *     ただし初期化式が指定されない場合は{@code null}
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     */
    VariableDeclarator newVariableDeclarator(
            SimpleName name,
            Expression initializer
    );

    /**
     * 新しい{@link VariableDeclarator}を生成して返す。
     * @param name
     *     変数の名前
     * @param extraDimensions
     *     追加次元数の宣言
     * @param initializer
     *     初期化式、
     *     ただし初期化式が指定されない場合は{@code null}
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code name}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code extraDimensions}に負の値が指定された場合
     */
    VariableDeclarator newVariableDeclarator(
            SimpleName name,
            int extraDimensions,
            Expression initializer
    );

    /**
     * 新しい{@link WhileStatement}を生成して返す。
     * @param condition
     *     条件式
     * @param body
     *     ループ文
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code condition}に{@code null}が指定された場合
     * @throws IllegalArgumentException
     *     {@code body}に{@code null}が指定された場合
     */
    WhileStatement newWhileStatement(
            Expression condition,
            Statement body
    );

    /**
     * 新しい{@link Wildcard}を生成して返す。
     * @return 生成した要素
     */
    Wildcard newWildcard(
    );

    /**
     * 新しい{@link Wildcard}を生成して返す。
     * @param boundKind
     *     型境界の種類
     * @param typeBound
     *     境界型、
     *     ただし境界型が指定されない場合は{@code null}
     * @return 生成した要素
     * @throws IllegalArgumentException
     *     {@code boundKind}に{@code null}が指定された場合
     */
    Wildcard newWildcard(
            WildcardBoundKind boundKind,
            Type typeBound
    );
}
