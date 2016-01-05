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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.asakusafw.dmdl.thundergate.model.JoinedModelDescription;
import com.asakusafw.dmdl.thundergate.model.ModelDescription;
import com.asakusafw.dmdl.thundergate.model.ModelProperty;
import com.asakusafw.dmdl.thundergate.model.Source;
import com.asakusafw.utils.collections.Lists;

/**
 * {@link JoinedModelDescription}を構築するビルダー。
 */
public class JoinedModelBuilder extends ModelBuilder<JoinedModelBuilder> {

    private final List<String> columns;

    private final Side left;

    private final Side right;

    /**
     * インスタンスを生成する。
     * @param name 生成するモデルの名前
     * @param left 結合されるモデル
     * @param leftAlias 結合されるモデルのエイリアス (省略可)
     * @param right 結合するモデル
     * @param rightAlias 結合するモデルのエイリアス (省略可)
     */
    public JoinedModelBuilder(
            String name,
            ModelDescription left,
            String leftAlias,
            ModelDescription right,
            String rightAlias) {
        super(name);
        if (left == null) {
            throw new IllegalArgumentException("left must not be null"); //$NON-NLS-1$
        }
        if (right == null) {
            throw new IllegalArgumentException("right must not be null"); //$NON-NLS-1$
        }
        this.columns = Lists.create();
        this.left = new Side(left);
        this.right = new Side(right);
        if (leftAlias != null) {
            this.left.alias = leftAlias;
        }
        if (rightAlias != null) {
            this.right.alias = rightAlias;
        }
        if (this.left.alias.equals(this.right.alias)) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "エイリアス名が衝突しています ({0})",
                    this.left.alias));
        }
    }

    /**
     * 結合条件を追加する。
     * @param aProperty 等価結合条件の1つ目の項
     * @param bProperty 等価結合条件の2つ目の項
     * @return このオブジェクト (メソッドチェイン用)
     */
    public JoinedModelBuilder on(String aProperty, String bProperty) {
        if (aProperty == null) {
            throw new IllegalArgumentException("aProperty must not be null"); //$NON-NLS-1$
        }
        if (bProperty == null) {
            throw new IllegalArgumentException("bProperty must not be null"); //$NON-NLS-1$
        }
        Ref a = resolve(aProperty);
        Ref b = resolve(bProperty);
        if (a.side == b.side) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "結合条件は左右で異なるモデルを参照して下さい ({0} = {1})",
                    aProperty,
                    bProperty));
        }
        a.side.condition.add(a.side.find(a.name));
        b.side.condition.add(b.side.find(b.name));
        return this;
    }

    /**
     * カラムの情報を追加する。
     * @param columnName 追加するカラムの名前
     * @param sourceProperty マッピング元のプロパティの指定
     * @return このオブジェクト (メソッドチェイン用)
     */
    public JoinedModelBuilder add(
            String columnName,
            String sourceProperty) {
        if (columnName == null) {
            throw new IllegalArgumentException("columnName must not be null"); //$NON-NLS-1$
        }
        if (sourceProperty == null) {
            throw new IllegalArgumentException("sourceProperty must not be null"); //$NON-NLS-1$
        }
        Ref source = resolve(sourceProperty);
        columns.add(columnName);
        source.side.mapping.put(source.name, columnName);
        return this;
    }

    /**
     * ここまでの情報を元に、{@link JoinedModelDescription}を構築して返す。
     * @return 構築したモデル
     */
    @Override
    public JoinedModelDescription toDescription() {
        if (left.condition.isEmpty()) {
            throw new IllegalStateException(MessageFormat.format(
                    "結合条件が指定されていません ({0})",
                    getReference()));
        }
        if (columns.isEmpty()) {
            throw new IllegalStateException(MessageFormat.format(
                    "プロパティが追加されていません ({0})",
                    getReference()));
        }

        pairingTrivialSource(left, right);
        pairingTrivialSource(right, left);

        return new JoinedModelDescription(
                getReference(),
                buildProperties(),
                left.condition,
                right.condition);
    }

    private void pairingTrivialSource(Side a, Side b) {
        assert a != null;
        assert b != null;
        // 結合条件になったフィールドが結果に含まれる場合、それもソースとして登録する
        for (int i = 0, n = a.condition.size(); i < n; i++) {
            Source as = a.condition.get(i);
            Source bs = b.condition.get(i);
            if (a.mapping.get(as.getName()) != null && b.mapping.get(bs.getName()) == null) {
                String column = a.mapping.get(as.getName());
                b.mapping.put(bs.getName(), column);
            }
        }
    }

    private List<ModelProperty> buildProperties() {
        // ペア用のコンテナを作成
        Map<String, SourcePair> pairs = new TreeMap<String, SourcePair>();
        for (String mapTo : columns) {
            pairs.put(mapTo, new SourcePair());
        }

        // 左の情報をペアのコンテナに格納
        for (Map.Entry<String, String> entry : left.mapping.entrySet()) {
            String mapTo = entry.getValue();
            assert mapTo == null || pairs.containsKey(mapTo);
            if (mapTo == null) {
                continue;
            }
            Source source = left.sources.get(entry.getKey());
            pairs.get(mapTo).left = source;
        }

        // 右の情報をペアのコンテナに格納
        for (Map.Entry<String, String> entry : right.mapping.entrySet()) {
            String mapTo = entry.getValue();
            assert mapTo == null || pairs.containsKey(mapTo);
            if (mapTo == null) {
                continue;
            }
            Source source = right.sources.get(entry.getKey());
            pairs.get(mapTo).right = source;
        }

        // ペアの情報を元にプロパティを構築
        List<ModelProperty> properties = Lists.create();
        for (String mapTo : columns) {
            SourcePair sources = pairs.get(mapTo);
            assert sources != null;
            assert sources.left != null || sources.right != null;
            ModelProperty property = new ModelProperty(
                    mapTo,
                    sources.left,
                    sources.right);
            properties.add(property);
        }
        return properties;
    }

    private Ref resolve(String source) {
        assert source != null;
        int qualified = source.indexOf('.');
        if (qualified < 0) {
            boolean leftHit = left.mapping.containsKey(source);
            boolean rightHit = right.mapping.containsKey(source);
            if (leftHit && rightHit) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "{0}はあいまいです ({1})",
                        source,
                        getReference()));
            }
            if (leftHit == false && rightHit == false) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "{0}が見つかりません ({1})",
                        source,
                        getReference()));
            }
            if (leftHit) {
                return new Ref(left, source);
            } else {
                return new Ref(right, source);
            }
        } else {
            String qualifier = source.substring(0, qualified);
            String column = source.substring(qualified + 1);
            if (left.alias.equals(qualifier)) {
                return new Ref(left, column);
            } else if (right.alias.equals(qualifier)) {
                return new Ref(right, column);
            } else {
                throw new IllegalArgumentException(MessageFormat.format(
                        "{0}が見つかりません ({1})",
                        qualifier,
                        source));
            }
        }
    }

    private static class Side {

        ModelDescription model;

        Map<String, Source> sources;

        String alias;

        List<Source> condition;

        Map<String, String> mapping;

        Side(ModelDescription model) {
            assert model != null;
            this.model = model;
            this.sources = new TreeMap<String, Source>();
            this.alias = model.getReference().getSimpleName();
            this.condition = Lists.create();
            this.mapping = new TreeMap<String, String>();
            for (Source s : model.getPropertiesAsSources()) {
                sources.put(s.getName(), s);
                mapping.put(s.getName(), null);
            }
        }

        Source find(String columnName) {
            assert columnName != null;
            if (sources.containsKey(columnName)) {
                return sources.get(columnName);
            }
            throw new IllegalArgumentException(MessageFormat.format(
                    "結合条件のカラムが見つかりません({0}.{1})",
                    model.getReference(),
                    columnName));
        }
    }

    private static class Ref {

        Side side;

        String name;

        public Ref(Side side, String name) {
            assert side != null;
            assert name != null;
            this.side = side;
            this.name = name;
        }
    }

    private static class SourcePair {

        Source left;

        Source right;

        SourcePair() {
            return;
        }

        @Override
        public String toString() {
            return MessageFormat.format(
                    "({0}, {1})",
                    left == null ? "-" : left.getName(),
                    right == null ? "-" : right.getName());
        }
    }
}
