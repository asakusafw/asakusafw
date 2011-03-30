/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.compiler.batch;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.asakusafw.compiler.flow.processor.operator.UpdateFlowFactory;
import com.asakusafw.compiler.flow.processor.operator.UpdateFlowFactory.WithParameter;
import com.asakusafw.compiler.flow.testing.model.Ex1;
import com.asakusafw.vocabulary.external.FileExporterDescription;
import com.asakusafw.vocabulary.external.FileImporterDescription;
import com.asakusafw.vocabulary.flow.Export;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.Import;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.JobFlow;
import com.asakusafw.vocabulary.flow.Out;


/**
 * 入力に100を足す。
 */
@JobFlow(name = "join")
public class JoinJobFlow extends FlowDescription {

    private In<Ex1> in;

    private Out<Ex1> out;

    /**
     * インスタンスを生成する。
     * @param in 入力
     * @param out 出力
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public JoinJobFlow(
            @Import(name = "join", description = Importer.class)
            In<Ex1> in,
            @Export(name = "join", description = Exporter.class)
            Out<Ex1> out) {
        this.in = in;
        this.out = out;
    }

    @Override
    protected void describe() {
        UpdateFlowFactory f = new UpdateFlowFactory();
        WithParameter op = f.withParameter(in, 10);
        out.add(op.out);
    }

    /**
     * インポーター。
     */
    public static class Importer extends FileImporterDescription {

        @Override
        public Class<?> getModelType() {
            return Ex1.class;
        }

        @Override
        public Set<String> getPaths() {
            return new HashSet<String>(Arrays.asList(
                    "target/testing/sequencefile/second/out-*",
                    "target/testing/sequencefile/side/out-*"));
        }
    }

    /**
     * エクスポーター。
     */
    public static class Exporter extends FileExporterDescription {

        @Override
        public Class<?> getModelType() {
            return Ex1.class;
        }

        @Override
        public String getPathPrefix() {
            return "target/testing/sequencefile/join/out-*";
        }
    }
}
