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
package com.asakusafw.modelgen.view;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.modelgen.model.Aggregator;
import com.asakusafw.modelgen.model.JoinedModelDescription;
import com.asakusafw.modelgen.model.ModelDescription;
import com.asakusafw.modelgen.model.ModelRepository;
import com.asakusafw.modelgen.util.JoinedModelBuilder;
import com.asakusafw.modelgen.util.SummarizedModelBuilder;
import com.asakusafw.modelgen.view.model.CreateView;
import com.asakusafw.modelgen.view.model.CreateView.Kind;
import com.asakusafw.modelgen.view.model.Name;
import com.asakusafw.modelgen.view.model.On;
import com.asakusafw.modelgen.view.model.Select;
import com.asakusafw.utils.graph.Graph;
import com.asakusafw.utils.graph.Graphs;

/**
 * ビューの構造を解析する。
 */
public class ViewAnalyzer {

    static final Logger LOG = LoggerFactory.getLogger(ViewAnalyzer.class);

    private List<View> added = new ArrayList<View>();

    /**
     * この解析器に指定のビューを追加する。
     * @param namespace ビューの名前空間
     * @param view 追加するビューの構造
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public void add(List<String> namespace, CreateView view) {
        if (namespace == null) {
            throw new IllegalArgumentException("namespace must not be null"); //$NON-NLS-1$
        }
        if (view == null) {
            throw new IllegalArgumentException("view must not be null"); //$NON-NLS-1$
        }
        added.add(new View(namespace, view));
    }

    /**
     * これまでに追加されたビューの情報を解析して、{@link ModelDescription}の形式に変換する。
     * @param repository 利用するリポジトリ
     * @return 変換後のモデル
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public List<ModelDescription> analyze(ModelRepository repository) {
        if (repository == null) {
            throw new IllegalArgumentException("repository must not be null"); //$NON-NLS-1$
        }

        LOG.info("ビューの関係を解析しています ({}個のビュー)", added.size());
        List<View> sorted = sort();

        Map<Name, ModelDescription> context =
            new HashMap<Name, ModelDescription>();
        List<ModelDescription> analyzed = new ArrayList<ModelDescription>();
        for (View view : sorted) {
            LOG.info("ビューの構造を解析しています: {}", view.ast.name);
            ModelDescription model = transform(view, repository, context);
            context.put(view.ast.name, model);
            if (model != null) {
                analyzed.add(model);
            }
        }

        if (analyzed.size() != added.size()) {
            throw new IllegalStateException(MessageFormat.format(
                    "解析中にエラーが発生したため、ビューの処理を中断します (処理したビューの個数: {0}/{1})",
                    analyzed.size(),
                    added.size()));
        }
        return analyzed;
    }

    private ModelDescription transform(
            View target,
            ModelRepository repository,
            Map<Name, ModelDescription> context) {
        assert target != null;
        assert repository != null;
        assert context != null;

        CreateView ast = target.ast;

        // 先に外部依存関係を解決してcontextに登録する
        if (resolveDependency(ast, repository, context) == false) {
            return null;
        }

        Kind kind = ast.getKind();
        if (kind == Kind.JOINED) {
            return transformJoined(target, context);
        } else if (kind == Kind.SUMMARIZED) {
            return transformSummarized(target, context);
        } else {
            LOG.error("{}は処理できない種類のビューです: {}",
                    ast.name,
                    ast);
            return null;
        }
    }

    private JoinedModelDescription transformJoined(
            View target,
            Map<Name, ModelDescription> context) {
        assert target != null;
        assert target.ast.getKind() == CreateView.Kind.JOINED;
        assert context != null;
        CreateView ast = target.ast;
        assert context.get(ast.from.table) != null;
        assert context.get(ast.from.join.table) != null;

        JoinedModelBuilder builder = new JoinedModelBuilder(
                ast.name.token,
                context.get(ast.from.table),
                ast.from.alias,
                context.get(ast.from.join.table),
                ast.from.join.alias);
        builder.namespace(target.namespace);

        // 結合条件
        for (On on : ast.from.join.condition) {
            builder.on(on.left.token, on.right.token);
        }

        // カラムの追加
        for (Select select : ast.selectList) {
            assert select.aggregator == Aggregator.IDENT;
            builder.add(select.alias.token, select.name.token);
        }

        return builder.toDescription();
    }

    private ModelDescription transformSummarized(
            View target,
            Map<Name, ModelDescription> context) {
        assert target != null;
        assert target.ast.getKind() == CreateView.Kind.SUMMARIZED;

        CreateView ast = target.ast;
        assert context.get(ast.from.table) != null;

        SummarizedModelBuilder builder = new SummarizedModelBuilder(
                ast.name.token,
                context.get(ast.from.table),
                ast.from.alias);
        builder.namespace(target.namespace);

        // グループ化
        for (Name name : ast.groupBy) {
            builder.groupBy(name.token);
        }

        // カラムの追加
        for (Select select : ast.selectList) {
            builder.add(
                    select.alias.token,
                    select.aggregator,
                    select.name.token);
        }

        return builder.toDescription();
    }

    private boolean resolveDependency(
            CreateView target,
            ModelRepository repository,
            Map<Name, ModelDescription> context) {
        boolean success = true;
        for (Name dependency : target.getDependencies()) {
            LOG.debug("{}の依存先として{}を解決しています",
                    target.name,
                    dependency);
            if (context.containsKey(dependency)) {
                if (context.get(dependency) == null) {
                    LOG.warn("{}は依存先の{}が失敗しているため、変換をスキップします",
                            target.name,
                            dependency);
                    return false;
                }
            } else {
                ModelDescription resolved = repository.find(dependency.token);
                if (resolved == null) {
                    LOG.error("{}は依存先の{}が見つからないため、変換に失敗しました",
                            target.name,
                            dependency);
                    success = false;
                } else {
                    context.put(dependency, resolved);
                }
            }
        }
        return success;
    }

    private List<View> sort() {
        Map<Name, View> map = new HashMap<Name, View>();
        Graph<Name> dependencies = Graphs.newInstance();
        for (View view : added) {
            Name name = view.ast.name;
            map.put(name, view);
            for (Name dependTo : view.ast.getDependencies()) {
                dependencies.addEdge(name, dependTo);
            }
        }

        // 依存関係の循環を指摘
        Set<Set<Name>> circuit = Graphs.findCircuit(dependencies);
        if (circuit.isEmpty() == false) {
            throw new IllegalStateException(MessageFormat.format(
                    "ビューの参照関係に循環が存在します: {0}",
                    circuit));
        }

        // 依存関係の逆順に整列
        List<Name> sorted = Graphs.sortPostOrder(dependencies);

        List<View> results = new ArrayList<View>();
        for (Name name : sorted) {
            // 外部参照でないものについてのみ結果に残す
            View view = map.get(name);
            if (view != null) {
                results.add(view);
            }
        }
        return results;
    }

    private static class View {

        String[] namespace;

        CreateView ast;

        View(List<String> namespace, CreateView ast) {
            assert namespace != null;
            assert ast != null;
            this.namespace = namespace.toArray(new String[namespace.size()]);
            this.ast = ast;
        }
    }
}
