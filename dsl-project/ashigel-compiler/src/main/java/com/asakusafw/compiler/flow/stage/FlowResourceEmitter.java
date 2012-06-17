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
package com.asakusafw.compiler.flow.stage;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.compiler.flow.FlowCompilingEnvironment;
import com.asakusafw.compiler.flow.FlowGraphRewriter;
import com.asakusafw.compiler.flow.FlowGraphRewriter.RewriteException;
import com.asakusafw.runtime.flow.FlowResource;
import com.asakusafw.utils.collections.Maps;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.vocabulary.flow.graph.FlowResourceDescription;

/**
 * {@link FlowResourceDescription}をコンパイルして{@link FlowResource}を生成する。
 */
public class FlowResourceEmitter {

    static final Logger LOG = LoggerFactory.getLogger(FlowResourceEmitter.class);

    private FlowCompilingEnvironment environment;

    private List<FlowGraphRewriter> rewriters;

    /**
     * インスタンスを生成する。
     * @param environment 環境オブジェクト
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public FlowResourceEmitter(FlowCompilingEnvironment environment) {
        Precondition.checkMustNotBeNull(environment, "environment"); //$NON-NLS-1$
        this.environment = environment;
        this.rewriters = environment.getGraphRewriters().getRewriters();
    }

    /**
     * リソースの一覧をコンパイルして返す。
     * @param resources コンパイル対象の一覧
     * @return リソースとコンパイル結果の対応表
     * @throws IOException ファイルの出力に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public Map<FlowResourceDescription, CompiledType> emit(Set<FlowResourceDescription> resources) throws IOException {
        Precondition.checkMustNotBeNull(resources, "resources"); //$NON-NLS-1$
        Map<FlowResourceDescription, CompiledType> results = Maps.create();
        for (FlowResourceDescription resource : resources) {
            Name compiled = compile(resource);
            if (compiled != null) {
                results.put(resource, new CompiledType(compiled));
            }
        }
        return results;
    }

    private Name compile(FlowResourceDescription resource) {
        assert resource != null;
        for (FlowGraphRewriter rewriter : rewriters) {
            try {
                Name compiled = rewriter.resolve(resource);
                if (compiled != null) {
                    LOG.debug("{}は{}でコンパイルしました", resource, compiled);
                    return compiled;
                }
            } catch (RewriteException e) {
                environment.error(
                        "リソース{0}のコンパイルに失敗しました: {1}",
                        resource,
                        e.getMessage());
                LOG.error(MessageFormat.format(
                        "リソース{0}をコンパイルできませんでした",
                        resource),
                        e);
            }
        }
        environment.error(
                "リソース{0}をコンパイルするエンジンが見つかりませんでした",
                resource);
        return null;
    }
}
