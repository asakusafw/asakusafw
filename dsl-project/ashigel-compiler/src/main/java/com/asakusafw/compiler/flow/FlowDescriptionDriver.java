/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.compiler.flow;

import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.asakusafw.compiler.common.Precondition;
import com.asakusafw.utils.collections.Lists;
import com.asakusafw.vocabulary.external.ExporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.Out;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;
import com.asakusafw.vocabulary.flow.graph.FlowIn;
import com.asakusafw.vocabulary.flow.graph.FlowOut;
import com.asakusafw.vocabulary.flow.graph.InputDescription;
import com.asakusafw.vocabulary.flow.graph.OutputDescription;


/**
 * {@link FlowDescription}を結線するドライバ。
 */
public class FlowDescriptionDriver {

    private final List<Object> ports = Lists.create();

    private final Map<String, FlowIn<?>> inputs = new LinkedHashMap<String, FlowIn<?>>();

    private final Map<String, FlowOut<?>> outputs = new LinkedHashMap<String, FlowOut<?>>();

    /**
     * 指定の名前とインポーター記述を持つ入力を作成する。
     * <p>
     * 入力の名前は、同一のフロー記述における入力内で重複してはならない。
     * また、識別子には、下記の形式の名前 (Javaの変数名のうち、ASCIIコード表に収まるもののみ)
     * を利用可能である。
     * </p>
<pre><code>
Name :
    NameStart NamePart*
NameStart: one of
    A-Z
    a-z
    _
NamePart: one of
    NameStart
    0-9
</code></pre>
     * @param <T> 入力データの型
     * @param name 入力の名前
     * @param importer 入力に利用するインポーターの名前
     * @return 作成した入力オブジェクト
     * @throws IllegalStateException すでに指定の名前の入力が存在する場合
     * @throws IllegalArgumentException 引数に不正な名前、または{@code null}が指定された場合
     */
    public <T> In<T> createIn(String name, ImporterDescription importer) {
        Precondition.checkMustNotBeNull(name, "name"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(importer, "importer"); //$NON-NLS-1$
        if (isValidName(name) == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "入力の名前が不正です ({0})",
                    name));
        }
        if (inputs.containsKey(name)) {
            throw new IllegalStateException(MessageFormat.format(
                    "入力の名前が重複しています ({0})",
                    name));
        }
        FlowIn<T> in = new FlowIn<T>(new InputDescription(name, importer));
        inputs.put(name, in);
        ports.add(in);
        return in;
    }

    /**
     * 指定の名前とエクスポーター記述を持つ出力を作成する。
     * <p>
     * 出力の名前は、同一のフロー記述における出力内で重複してはならない。
     * また、識別子には、下記の形式の名前 (Javaの変数名のうち、ASCIIコード表に収まるもののみ)
     * を利用可能である。
     * </p>
<pre><code>
Name :
    NameStart NamePart*
NameStart: one of
    A-Z
    a-z
    _
NamePart: one of
    NameStart
    0-9
</code></pre>
     * @param <T> 出力データの型
     * @param name 出力の名前
     * @param exporter 出力に利用するエクスポーターの名前
     * @return 作成した出力オブジェクト
     * @throws IllegalStateException すでに指定の名前の出力が存在する場合
     * @throws IllegalArgumentException 引数に不正な名前、または{@code null}が指定された場合
     */
    public <T> Out<T> createOut(String name, ExporterDescription exporter) {
        Precondition.checkMustNotBeNull(name, "name"); //$NON-NLS-1$
        Precondition.checkMustNotBeNull(exporter, "exporter"); //$NON-NLS-1$
        if (isValidName(name) == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "出力の名前が不正です ({0})",
                    name));
        }
        if (outputs.containsKey(name)) {
            throw new IllegalStateException(MessageFormat.format(
                    "出力の名前が重複しています ({0})",
                    name));
        }
        FlowOut<T> out = new FlowOut<T>(new OutputDescription(name, exporter));
        outputs.put(name, out);
        ports.add(out);
        return out;
    }

    private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z_][0-9A-Za-z_]*");

    /**
     * 指定の名前が入出力の識別子として正しい場合のみ{@code true}を返す。
     * @param name 名前
     * @return 名前が識別子として正しい場合のみ{@code true}、{@code null}や不正な文字列の場合は{@code false}
     */
    public boolean isValidName(String name) {
        if (name == null) {
            return false;
        }
        return VALID_NAME.matcher(name).matches();
    }

    /**
     * これまでに登録した入出力をその順番に返す。
     * <p>
     * 返されるリストに含まれる要素は、必ず{@link FlowIn}か {@link FlowOut}のいずれかである。
     * </p>
     * @return これまでに登録した入出力
     */
    public List<Object> getPorts() {
        return ports;
    }

    /**
     * 指定のフロー記述オブジェクトを解釈し、ここまでに作成した入出力を起点とする
     * 演算子グラフを構築して返す。
     * @param description 対象のフロー記述オブジェクト
     * @return 生成した演算子グラフ
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public FlowGraph createFlowGraph(FlowDescription description) {
        Precondition.checkMustNotBeNull(description, "description"); //$NON-NLS-1$
        description.start();
        FlowGraph result = new FlowGraph(
                description.getClass(),
                Lists.from(inputs.values()),
                Lists.from(outputs.values()));
        inputs.clear();
        outputs.clear();
        return result;
    }
}
