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
package com.asakusafw.modelgen.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.asakusafw.modelgen.model.Aggregator;
import com.asakusafw.modelgen.model.Attribute;
import com.asakusafw.modelgen.model.BasicType;
import com.asakusafw.modelgen.model.ModelProperty;
import com.asakusafw.modelgen.model.PropertyType;
import com.asakusafw.modelgen.model.PropertyTypeKind;
import com.asakusafw.modelgen.model.Source;
import com.asakusafw.modelgen.model.TableModelDescription;


/**
 * {@link TableModelDescription}を構築するビルダー。
 */
public class TableModelBuilder extends ModelBuilder<TableModelBuilder> {

    private List<Column> columns;

    /**
     * インスタンスを生成する。
     * @param tableName 構築するモデルに対応するテーブルの名前
     */
    public TableModelBuilder(String tableName) {
        super(tableName);
        this.columns = new ArrayList<Column>();
    }

    /**
     * カラムの情報を追加する。
     * @param comment コメント文字列 (省略可)
     * @param columnName 追加するカラムの名前
     * @param basicTypeKind 追加するカラムの種類
     * @param attributes 追加するカラムの属性一覧
     * @return このオブジェクト (メソッドチェイン用)
     */
    public TableModelBuilder add(
            String comment,
            String columnName,
            PropertyTypeKind basicTypeKind,
            Attribute... attributes) {
        if (columnName == null) {
            throw new IllegalArgumentException("columnName must not be null"); //$NON-NLS-1$
        }
        if (basicTypeKind == null) {
            throw new IllegalArgumentException("columnTypeKind must not be null"); //$NON-NLS-1$
        }
        if (attributes == null) {
            throw new IllegalArgumentException("attributes must not be null"); //$NON-NLS-1$
        }
        Column column = new Column(columnName, new BasicType(basicTypeKind), attributes);
        columns.add(column);
        return this;
    }

    /**
     * カラムの情報を追加する。
     * @param comment コメント文字列 (省略可)
     * @param columnName 追加するカラムの名前
     * @param columnType 追加するカラムの種類
     * @param attributes 追加するカラムの属性一覧
     * @return このオブジェクト (メソッドチェイン用)
     */
    public TableModelBuilder add(
            String comment,
            String columnName,
            PropertyType columnType,
            Attribute... attributes) {
        if (columnName == null) {
            throw new IllegalArgumentException("columnName must not be null"); //$NON-NLS-1$
        }
        if (columnType == null) {
            throw new IllegalArgumentException("columnType must not be null"); //$NON-NLS-1$
        }
        if (attributes == null) {
            throw new IllegalArgumentException("attributes must not be null"); //$NON-NLS-1$
        }
        Column column = new Column(columnName, columnType, attributes);
        columns.add(column);
        return this;
    }

    /**
     * ここまでの情報を元に、{@link TableModelDescription}を構築して返す。
     * @return 構築したモデル
     */
    @Override
    public TableModelDescription toDescription() {
        if (columns.isEmpty()) {
            throw new IllegalStateException(MessageFormat.format(
                    "プロパティが追加されていません ({0})",
                    getReference()));
        }
        List<ModelProperty> properties = new ArrayList<ModelProperty>();
        for (Column column : columns) {
            ModelProperty property = toProperty(column);
            properties.add(property);
        }
        return new TableModelDescription(getReference(), properties);
    }

    private ModelProperty toProperty(Column column) {
        assert column != null;
        Source source = new Source(
                Aggregator.IDENT,
                getReference(),
                column.name,
                column.type,
                column.attributes);
        return new ModelProperty(column.name, source);
    }

    private static class Column {

        String name;

        PropertyType type;

        Set<Attribute> attributes;

        Column(String name, PropertyType type, Attribute[] attributes) {
            assert name != null;
            assert type != null;
            assert attributes != null;
            this.name = name;
            this.type = type;
            this.attributes = EnumSet.noneOf(Attribute.class);
            for (Attribute attr : attributes) {
                this.attributes.add(attr);
            }
        }
    }
}
