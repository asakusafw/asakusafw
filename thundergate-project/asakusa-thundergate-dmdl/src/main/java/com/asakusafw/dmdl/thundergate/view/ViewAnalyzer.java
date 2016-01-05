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
package com.asakusafw.dmdl.thundergate.view;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.dmdl.thundergate.model.Aggregator;
import com.asakusafw.dmdl.thundergate.model.JoinedModelDescription;
import com.asakusafw.dmdl.thundergate.model.ModelDescription;
import com.asakusafw.dmdl.thundergate.model.ModelRepository;
import com.asakusafw.dmdl.thundergate.util.JoinedModelBuilder;
import com.asakusafw.dmdl.thundergate.util.SummarizedModelBuilder;
import com.asakusafw.dmdl.thundergate.view.model.CreateView;
import com.asakusafw.dmdl.thundergate.view.model.CreateView.Kind;
import com.asakusafw.dmdl.thundergate.view.model.Name;
import com.asakusafw.dmdl.thundergate.view.model.On;
import com.asakusafw.dmdl.thundergate.view.model.Select;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.utils.collections.Maps;
import com.asakusafw.utils.graph.Graph;
import com.asakusafw.utils.graph.Graphs;

/**
 * ビューの構造を解析する。
 */
public class ViewAnalyzer {

    static final Logger LOG = LoggerFactory.getLogger(ViewAnalyzer.class);

    private final List<CreateView> added = Lists.create();

    /**
     * この解析器に指定のビューを追加する。
     * @param view 追加するビューの構造
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public void add(CreateView view) {
        if (view == null) {
            throw new IllegalArgumentException("view must not be null"); //$NON-NLS-1$
        }
        added.add(view);
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
        List<CreateView> sorted = sort();

        Map<Name, ModelDescription> context = Maps.create();
        List<ModelDescription> analyzed = Lists.create();
        for (CreateView view : sorted) {
            LOG.info("ビューの構造を解析しています: {}", view.name);
            ModelDescription model = transform(view, repository, context);
            context.put(view.name, model);
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
            CreateView target,
            ModelRepository repository,
            Map<Name, ModelDescription> context) {
        assert target != null;
        assert repository != null;
        assert context != null;

        // 先に外部依存関係を解決してcontextに登録する
        if (resolveDependency(target, repository, context) == false) {
            return null;
        }

        Kind kind = target.getKind();
        if (kind == Kind.JOINED) {
            return transformJoined(target, context);
        } else if (kind == Kind.SUMMARIZED) {
            return transformSummarized(target, context);
        } else {
            LOG.error("{}は処理できない種類のビューです: {}",
                    target.name,
                    target);
            return null;
        }
    }

    private JoinedModelDescription transformJoined(
            CreateView target,
            Map<Name, ModelDescription> context) {
        assert target != null;
        assert target.getKind() == CreateView.Kind.JOINED;
        assert context != null;
        assert context.get(target.from.table) != null;
        assert context.get(target.from.join.table) != null;

        JoinedModelBuilder builder = new JoinedModelBuilder(
                target.name.token,
                context.get(target.from.table),
                target.from.alias,
                context.get(target.from.join.table),
                target.from.join.alias);

        // 結合条件
        for (On on : target.from.join.condition) {
            builder.on(on.left.token, on.right.token);
        }

        // カラムの追加
        for (Select select : target.selectList) {
            assert select.aggregator == Aggregator.IDENT;
            builder.add(select.alias.token, select.name.token);
        }

        return builder.toDescription();
    }

    private ModelDescription transformSummarized(
            CreateView target,
            Map<Name, ModelDescription> context) {
        assert target != null;
        assert target.getKind() == CreateView.Kind.SUMMARIZED;

        assert context.get(target.from.table) != null;

        SummarizedModelBuilder builder = new SummarizedModelBuilder(
                target.name.token,
                context.get(target.from.table),
                target.from.alias);

        // グループ化
        for (Name name : target.groupBy) {
            builder.groupBy(name.token);
        }

        // カラムの追加
        for (Select select : target.selectList) {
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

    private List<CreateView> sort() {
        Map<Name, CreateView> map = Maps.create();
        Graph<Name> dependencies = Graphs.newInstance();
        for (CreateView view : added) {
            Name name = view.name;
            map.put(name, view);
            for (Name dependTo : view.getDependencies()) {
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

        List<CreateView> results = Lists.create();
        for (Name name : sorted) {
            // 外部参照でないものについてのみ結果に残す
            CreateView view = map.get(name);
            if (view != null) {
                results.add(view);
            }
        }
        return results;
    }
}
