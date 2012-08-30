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
package com.asakusafw.utils.java.parser.javadoc;

import java.util.ArrayList;
import java.util.List;

import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrBasicTypeKind;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocArrayType;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocBasicType;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocBlock;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocComment;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocElementVisitor;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocField;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocFragment;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocMethod;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocMethodParameter;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocNamedType;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocQualifiedName;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocSimpleName;
import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrDocText;
import com.asakusafw.utils.java.model.syntax.BasicTypeKind;
import com.asakusafw.utils.java.model.syntax.DocBlock;
import com.asakusafw.utils.java.model.syntax.DocElement;
import com.asakusafw.utils.java.model.syntax.DocMethodParameter;
import com.asakusafw.utils.java.model.syntax.Javadoc;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.syntax.NamedType;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Type;

/**
 * ドキュメンテーションコメントをIrenkaのモデルに変換する。
 */
public class JavadocConverter {

    private final ModelFactory factory;

    private final JavadocParser parser;

    /**
     * インスタンスを生成する。
     * @param factory モデルを構築するファクトリ
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public JavadocConverter(ModelFactory factory) {
        super();
        this.factory = factory;
        JavadocParserBuilder builder = new JavadocParserBuilder();
        builder.addSpecialStandAloneBlockParser(new FollowsNamedTypeBlockParser(
            "exception", //$NON-NLS-1$
            "throws" //$NON-NLS-1$
        ));
        builder.addSpecialStandAloneBlockParser(new FollowsReferenceBlockParser(
            "see" //$NON-NLS-1$
        ));
        builder.addSpecialStandAloneBlockParser(new ParamBlockParser(
            "param" //$NON-NLS-1$
        ));
        builder.addSpecialStandAloneBlockParser(new SerialFieldBlockParser(
            "serialField" //$NON-NLS-1$
        ));

        builder.addSpecialInlineBlockParser(new FollowsReferenceBlockParser(
            "link", //$NON-NLS-1$
            "linkplain" //$NON-NLS-1$
        ));
        this.parser = builder.build();
    }

    /**
     * 指定の文字列で構成されたドキュメンテーションコメントを生成して返す。
     * @param content 内容文字列
     * @param offset 開始オフセット
     * @return 解析結果
     * @throws JavadocParseException ドキュメンテーションコメントの形式が不正である場合
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public Javadoc convert(String content, int offset) throws JavadocParseException {
        if (content == null) {
            throw new IllegalArgumentException("content must not be null"); //$NON-NLS-1$
        }
        JavadocScanner scanner = DefaultJavadocScanner.newInstance(content);
        IrDocComment ir = parser.parse(scanner);
        return convert(ir, offset);
    }

    private Javadoc convert(IrDocComment comment, int offset) {
        assert comment != null;
        Mapper mapper = new Mapper(factory, offset);

        List<DocBlock> blocks = new ArrayList<DocBlock>();
        for (IrDocBlock block : comment.getBlocks()) {
            blocks.add((DocBlock) block.accept(mapper, null));
        }
        return factory.newJavadoc(blocks);
    }

    private static class Mapper
            extends IrDocElementVisitor<DocElement, Void> {

        final ModelFactory factory;

        private final TypeMapper types;

        Mapper(ModelFactory factory, int offset) {
            assert factory != null;
            this.factory = factory;
            this.types = new TypeMapper();
        }

        @Override
        public DocElement visitBlock(IrDocBlock elem, Void _) {
            String tag = elem.getTag();
            List<DocElement> elements = new ArrayList<DocElement>();
            for (IrDocFragment f : elem.getFragments()) {
                elements.add(f.accept(this, null));
            }
            return factory.newDocBlock(
                tag == null ? "" : tag,
                elements);
        }

        @Override
        public DocElement visitText(IrDocText elem, Void _) {
            return factory.newDocText(elem.getContent());
        }

        @Override
        public DocElement visitSimpleName(IrDocSimpleName elem, Void _) {
            return factory.newSimpleName(elem.getIdentifier());
        }

        @Override
        public DocElement visitQualifiedName(IrDocQualifiedName elem, Void _) {
            Name qualifier = (Name) elem.getQualifier().accept(this, null);
            SimpleName simple = (SimpleName) elem.getName().accept(this, null);
            return factory.newQualifiedName(qualifier, simple);
        }

        @Override
        public DocElement visitField(IrDocField elem, Void _) {
            Type type = declaring(elem.getDeclaringType());
            SimpleName name = (SimpleName) elem.getName().accept(this, null);
            return factory.newDocField(type, name);
        }

        @Override
        public DocElement visitMethod(IrDocMethod elem, Void _) {
            Type type = declaring(elem.getDeclaringType());
            SimpleName name = (SimpleName) elem.getName().accept(this, null);
            List<DocMethodParameter> params = new ArrayList<DocMethodParameter>();
            for (IrDocMethodParameter p : elem.getParameters()) {
                params.add(convert(p));
            }
            return factory.newDocMethod(type, name, params);
        }

        private DocMethodParameter convert(IrDocMethodParameter elem) {
            Type type = elem.getType().accept(types, this);
            SimpleName name;
            if (elem.getName() != null) {
                name = (SimpleName) elem.getName().accept(this, null);
            } else {
                name = null;
            }
            return factory.newDocMethodParameter(
                type,
                name,
                elem.isVariableArity());
        }

        @Override
        public DocElement visitNamedType(IrDocNamedType elem, Void _) {
            Name name = (Name) elem.getName().accept(this, null);
            return factory.newNamedType(name);
        }

        private NamedType declaring(IrDocNamedType declaringType) {
            if (declaringType == null) {
                return null;
            }
            return (NamedType) visitNamedType(declaringType, null);
        }
    }

    private static class TypeMapper extends IrDocElementVisitor<Type, Mapper> {

        TypeMapper() {
            return;
        }

        @Override
        public Type visitArrayType(IrDocArrayType elem, Mapper context) {
            Type component = elem.getComponentType().accept(this, context);
            return context.factory.newArrayType(component);
        }

        @Override
        public Type visitBasicType(IrDocBasicType elem, Mapper context) {
            BasicTypeKind kind = convert(elem.getTypeKind());
            return context.factory.newBasicType(kind);
        }

        @Override
        public Type visitNamedType(IrDocNamedType elem, Mapper context) {
            Name name = (Name) elem.getName().accept(context, null);
            return context.factory.newNamedType(name);
        }

        private static BasicTypeKind convert(IrBasicTypeKind kind) {
            switch (kind) {
            case BOOLEAN:
                return BasicTypeKind.BOOLEAN;
            case BYTE:
                return BasicTypeKind.BYTE;
            case CHAR:
                return BasicTypeKind.CHAR;
            case DOUBLE:
                return BasicTypeKind.DOUBLE;
            case FLOAT:
                return BasicTypeKind.FLOAT;
            case INT:
                return BasicTypeKind.INT;
            case LONG:
                return BasicTypeKind.LONG;
            case SHORT:
                return BasicTypeKind.SHORT;
            case VOID:
                return BasicTypeKind.VOID;
            default:
                throw new AssertionError(kind);
            }
        }
    }
}
