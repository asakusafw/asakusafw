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
package com.asakusafw.dmdl.thundergate.util;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.asakusafw.dmdl.thundergate.model.Aggregator;
import com.asakusafw.dmdl.thundergate.model.Attribute;
import com.asakusafw.dmdl.thundergate.model.ModelDescription;
import com.asakusafw.dmdl.thundergate.model.ModelProperty;
import com.asakusafw.dmdl.thundergate.model.Source;
import com.asakusafw.dmdl.thundergate.model.SummarizedModelDescription;
import com.asakusafw.utils.collections.Lists;


/**
 * {@link SummarizedModelBuilder}を構築するビルダー。
 */
public class SummarizedModelBuilder extends ModelBuilder<SummarizedModelBuilder> {

    private String alias;

    private Map<String, Source> sources;

    private List<Source> groupProperties;

    private List<Column> columns;

    /**
     * インスタンスを生成する。
     * @param tableName 構築するモデルに対応するテーブルの名前
     * @param model 集計元のモデル
     * @param alias 集計元のエイリアス (省略可)
     */
    public SummarizedModelBuilder(
            String tableName,
            ModelDescription model,
            String alias) {
        super(tableName);
        this.alias = (alias == null) ? model.getReference().getSimpleName() : alias;
        this.sources = new TreeMap<String, Source>();
        this.groupProperties = Lists.create();
        this.columns = Lists.create();
        for (Source s : model.getPropertiesAsSources()) {
            sources.put(s.getName(), s);
        }
    }

    /**
     * グループ化プロパティを指定する。
     * @param sourceProperties グループ化プロパティの一覧
     * @return このオブジェクト (メソッドチェイン用)
     */
    public SummarizedModelBuilder groupBy(String... sourceProperties) {
        if (sourceProperties == null) {
            throw new IllegalArgumentException("sourceProperties must not be null"); //$NON-NLS-1$
        }
        for (String s : sourceProperties) {
            groupProperties.add(find(s));
        }
        return this;
    }

    private Source find(String sourceProperty) {
        assert sourceProperty != null;
        int qualified = sourceProperty.indexOf('.');
        String simpleName;
        if (qualified < 0) {
            simpleName = sourceProperty;
        } else {
            String qualifier = sourceProperty.substring(0, qualified);
            if (alias.equals(qualifier) == false) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "\"{0}\" が見つかりません ({1})",
                        sourceProperty,
                        getReference()));
            }
            simpleName = sourceProperty.substring(qualified + 1);
        }
        Source source = sources.get(simpleName);
        if (source == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "\"{0}\" が見つかりません ({1})",
                    simpleName,
                    getReference()));
        }
        return source;
    }

    /**
     * カラムの情報を追加する。
     * @param columnName 追加するカラムの名前
     * @param aggregator 集約
     * @param sourceProperty 元となるプロパティ
     * @return このオブジェクト (メソッドチェイン用)
     */
    public SummarizedModelBuilder add(
            String columnName,
            Aggregator aggregator,
            String sourceProperty) {
        if (columnName == null) {
            throw new IllegalArgumentException("columnName must not be null"); //$NON-NLS-1$
        }
        if (aggregator == null) {
            throw new IllegalArgumentException("aggregator must not be null"); //$NON-NLS-1$
        }
        if (sourceProperty == null) {
            throw new IllegalArgumentException("sourceProperty must not be null"); //$NON-NLS-1$
        }
        Source source = find(sourceProperty);
        Column column = new Column(columnName, aggregator, source);
        columns.add(column);
        return this;
    }

    /**
     * ここまでの情報を元に、{@link SummarizedModelDescription}を構築して返す。
     * @return 構築したモデル
     * @throws IllegalStateException 構築に必要な情報が揃っていない場合
     */
    @Override
    public SummarizedModelDescription toDescription() {
        if (columns.isEmpty()) {
            throw new IllegalStateException(MessageFormat.format(
                    "プロパティが追加されていません ({0})",
                    getReference()));
        }
        List<ModelProperty> properties = Lists.create();
        for (Column column : columns) {
            Aggregator aggregator = column.aggregator;
            Source source = column.source;
            if (aggregator == Aggregator.IDENT
                    && groupProperties.contains(source) == false) {
                throw new IllegalStateException(MessageFormat.format(
                        "プロパティ \"{0}.{1}\" は集約関数かグループ化のキーとして指定する必要があります ({2})",
                        source.getDeclaring(),
                        source.getName(),
                        getReference()));
            }
            ModelProperty property = toProperty(column);
            properties.add(property);
        }
        validate();
        return new SummarizedModelDescription(
                getReference(),
                properties,
                groupProperties);
    }

    private void validate() {
        if (groupProperties.isEmpty()) {
            return;
        }
        Set<String> rest = new LinkedHashSet<String>();
        for (Source source : groupProperties) {
            rest.add(source.getName());
        }
        for (Column column : columns) {
            if (column.aggregator == Aggregator.IDENT) {
                rest.remove(column.source.getName());
            }
        }
        if (rest.isEmpty() == false) {
            throw new IllegalStateException(MessageFormat.format(
                    "グループ化キーとして指定されたプロパティ\"{0}.{1}\"は集約モデルにも含める必要があります ({2})",
                    groupProperties.get(0).getDeclaring(),
                    rest,
                    getReference()));
        }
    }

    private ModelProperty toProperty(Column column) {
        assert column != null;
        Source source = new Source(
                column.aggregator,
                column.source.getDeclaring(),
                column.source.getName(),
                column.source.getType(),
                Collections.<Attribute>emptySet());
        return new ModelProperty(column.name, source);
    }

    private static class Column {

        String name;

        Aggregator aggregator;

        Source source;

        Column(String name, Aggregator aggregator, Source source) {
            assert name != null;
            assert aggregator != null;
            assert source != null;
            this.name = name;
            this.aggregator = aggregator;
            this.source = source;
        }
    }
}
