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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.asakusafw.modelgen.model.Attribute;
import com.asakusafw.modelgen.model.ModelProperty;
import com.asakusafw.modelgen.model.TableModelDescription;
import com.asakusafw.utils.java.model.syntax.Annotation;
import com.asakusafw.utils.java.model.syntax.Javadoc;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.JavadocBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.vocabulary.model.DataModel;
import com.asakusafw.vocabulary.model.Property;
import com.asakusafw.vocabulary.model.TableModel;

/**
 * {@link TableModelDescription}に対するエンティティ情報をファイル上に出力するエミッタ。
 */
public class TableModelEntityEmitter
        extends ModelEntityEmitter<TableModelDescription> {

    /**
     * インスタンスを生成する。
     * @param factory ソースコードを生成するファクトリ
     * @param output 出力先のベースディレクトリ
     * @param packageName 出力先のパッケージ名
     * @param headerComment ファイルのヘッダコメント、不要の場合は{@code null}
     */
    public TableModelEntityEmitter(
            ModelFactory factory,
            File output,
            String packageName,
            List<String> headerComment) {
        super(factory, output, packageName, headerComment);
    }

    @Override
    public Class<TableModelDescription> getEmitTargetType() {
        return TableModelDescription.class;
    }

    @Override
    protected List<Annotation> createAnnotationsForModel(
            TableModelDescription model) {
        String name = model.getReference().getSimpleName();
        List<String> columnNames = new ArrayList<String>();
        List<String> primaryKeys = new ArrayList<String>();
        for (ModelProperty property : model.getProperties()) {
            columnNames.add(property.getName());
            if (property.getSource().getAttributes().contains(Attribute.PRIMARY_KEY)) {
                primaryKeys.add(property.getName());
            }
        }
        String[] primary = primaryKeys.toArray(new String[primaryKeys.size()]);
        String[] columns = columnNames.toArray(new String[columnNames.size()]);
        return new AttributeBuilder(f)
            .annotation(bless(DataModel.class))
            .annotation(bless(TableModel.class),
                    "name", Models.toLiteral(f, name),
                    "columns", Models.toArrayInitializer(f, columns),
                    "primary", Models.toArrayInitializer(f, primary))
            .annotation(bless(SuppressWarnings.class), Models.toLiteral(f, "deprecation"))
            .toAnnotations();
    }

    @Override
    protected List<Annotation> createAnnotationsForField(ModelProperty property) {
        return new AttributeBuilder(f)
            .annotation(bless(Property.class),
                    "name", Models.toLiteral(f, property.getName()))
            .toAnnotations();
    }

    @Override
    protected Javadoc createJavadocForModel(TableModelDescription model) {
        return new JavadocBuilder(f)
            .text("テーブル<code>{0}</code>を表すモデルクラス。",
                    model.getReference().getSimpleName())
            .toJavadoc();
    }

    @Override
    protected Javadoc createJavadocForField(ModelProperty property) {
        return new JavadocBuilder(f)
            .text("カラム<code>{0}</code>を表すフィールド。",
                    property.getName())
            .toJavadoc();
    }

    @Override
    protected Javadoc createJavadocForGetter(ModelProperty property) {
        return new JavadocBuilder(f)
            .text("カラム<code>{0}</code>の値を返す。", property.getName())
            .returns()
                .text("カラム<code>{0}</code>の値", property.getName())
            .exception(bless(NullPointerException.class))
                .text("値に")
                .code("null")
                .text("が格納されていた場合")
            .toJavadoc();
    }

    @Override
    protected Javadoc createJavadocForSetter(ModelProperty property) {
        return new JavadocBuilder(f)
            .text("カラム<code>{0}</code>の値を変更する。", property.getName())
            .param(createNameForParameter(property))
                .text("設定する値")
            .toJavadoc();
    }
}
