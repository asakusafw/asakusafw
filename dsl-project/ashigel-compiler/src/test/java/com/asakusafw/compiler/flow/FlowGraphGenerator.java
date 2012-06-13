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
package com.asakusafw.compiler.flow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.asakusafw.compiler.flow.plan.FlowPath;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.graph.FlowBoundary;
import com.asakusafw.vocabulary.flow.graph.FlowElement;
import com.asakusafw.vocabulary.flow.graph.FlowElementAttribute;
import com.asakusafw.vocabulary.flow.graph.FlowElementDescription;
import com.asakusafw.vocabulary.flow.graph.FlowElementInput;
import com.asakusafw.vocabulary.flow.graph.FlowElementOutput;
import com.asakusafw.vocabulary.flow.graph.FlowElementPortDescription;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;
import com.asakusafw.vocabulary.flow.graph.FlowIn;
import com.asakusafw.vocabulary.flow.graph.FlowOut;
import com.asakusafw.vocabulary.flow.graph.FlowPartDescription;
import com.asakusafw.vocabulary.flow.graph.FlowResourceDescription;
import com.asakusafw.vocabulary.flow.graph.InputDescription;
import com.asakusafw.vocabulary.flow.graph.OperatorDescription;
import com.asakusafw.vocabulary.flow.graph.OutputDescription;
import com.asakusafw.vocabulary.flow.graph.PortConnection;
import com.asakusafw.vocabulary.flow.graph.PortDirection;
import com.asakusafw.vocabulary.flow.util.PseudElementDescription;
import com.asakusafw.vocabulary.operator.Identity;


/**
 * テストで利用するフローグラフ生成器。
 */
public class FlowGraphGenerator {

    private static final Class<String> TYPE = String.class;

    private List<FlowIn<?>> flowInputs = new ArrayList<FlowIn<?>>();

    private List<FlowOut<?>> flowOutputs = new ArrayList<FlowOut<?>>();

    private Map<String, FlowElement> elements =
        new HashMap<String, FlowElement>();

    /**
     * 入力要素を追加する。
     * @param name 追加する要素の名前
     * @return 追加した要素
     */
    public FlowElement defineInput(String name) {
        InputDescription desc = new InputDescription(name, TYPE);
        FlowIn<?> node = new FlowIn<Object>(desc);
        flowInputs.add(node);
        return register(name, node.getFlowElement());
    }

    /**
     * 出力要素を追加する。
     * @param name 追加する要素の名前
     * @return 追加した要素
     */
    public FlowElement defineOutput(String name) {
        OutputDescription desc = new OutputDescription(name, TYPE);
        FlowOut<?> node = new FlowOut<Object>(desc);
        flowOutputs.add(node);
        return register(name, node.getFlowElement());
    }

    /**
     * 演算子を追加する。
     * @param name 追加する要素の名前
     * @param inputList スペース区切りの入力ポート名の一覧
     * @param outputList スペース区切りの出力ポート名の一覧
     * @param attributes 属性の一覧
     * @return 追加した要素
     */
    public FlowElement defineOperator(
            String name,
            String inputList,
            String outputList,
            FlowElementAttribute... attributes) {
        List<FlowElementPortDescription> inputs = parsePorts(PortDirection.INPUT, inputList);
        List<FlowElementPortDescription> outputs = parsePorts(PortDirection.OUTPUT, outputList);
        FlowElementDescription desc = new OperatorDescription(
                new OperatorDescription.Declaration(
                        Identity.class,
                        TYPE,
                        TYPE,
                        name,
                        Collections.<Class<?>>emptyList()),
                inputs,
                outputs,
                Collections.<FlowResourceDescription>emptyList(),
                Collections.<OperatorDescription.Parameter>emptyList(),
                Arrays.asList(attributes));
        return register(name, desc);
    }

    /**
     * フロー部品を追加する。
     * @param name 追加する要素の名前
     * @param graph フロー部品グラフ
     * @return 追加した要素
     */
    public FlowElement defineFlowPart(
            String name,
            FlowGraph graph) {
        FlowElementDescription desc = new FlowPartDescription(graph);
        return register(name, desc);
    }

    /**
     * 空要素を追加する。
     * @param name 追加する要素の名前
     * @return 追加した要素
     */
    public FlowElement defineEmpty(String name) {
        return register(name, new PseudElementDescription(
                name,
                TYPE,
                false,
                true,
                FlowBoundary.STAGE));
    }

    /**
     * 停止要素を追加する。
     * @param name 追加する要素の名前
     * @return 追加した要素
     */
    public FlowElement defineStop(String name) {
        return register(name, new PseudElementDescription(
                name,
                TYPE,
                true,
                false,
                FlowBoundary.STAGE));
    }

    /**
     * 疑似要素を追加する。
     * @param name 追加する要素の名前
     * @param attributes 属性の一覧
     * @return 追加した要素
     */
    public FlowElement definePseud(
            String name,
            FlowElementAttribute... attributes) {
        return register(name, new PseudElementDescription(
                name,
                TYPE,
                true,
                true,
                attributes));
    }

    /**
     * 指定のポート間を接続する。
     * ポートは "element-name.port-name"の形式で指定する。
     * ただし、対象の要素がポートを一つしか有さない場合、"element-name"の形式でも指定できる
     * @param upstream 上流
     * @param downstream 下流
     * @return 自身のオブジェクト
     */
    public FlowGraphGenerator connect(String upstream, String downstream) {
        FlowElementOutput output = findOutput(upstream);
        FlowElementInput input = findInput(downstream);
        PortConnection.connect(output, input);
        return this;
    }

    /**
     * 指定の名前を有する要素を返す。
     * @param name 対象の名前
     * @return 発見した要素
     */
    public FlowElement get(String name) {
        FlowElement found = elements.get(name);
        if (found == null) {
            throw new AssertionError(name + elements.keySet());
        }
        return found;
    }

    /**
     * 指定の名前を有する要素を返す。
     * @param name 対象の名前
     * @return 発見した要素
     */
    public FlowElementDescription desc(String name) {
        FlowElement found = elements.get(name);
        if (found == null) {
            throw new AssertionError(name + elements.keySet());
        }
        return found.getDescription();
    }

    /**
     * 指定の名前を有する入力を返す。
     * @param input 入力名
     * @return 発見したポート
     */
    public FlowElementInput input(String input) {
        return findInput(input);
    }

    /**
     * 指定の名前を有する入力を返す。
     * @param inputs 入力名
     * @return 発見したポート
     */
    public Set<FlowElementInput> inputs(String... inputs) {
        Set<FlowElementInput> results = new HashSet<FlowElementInput>();
        for (String input : inputs) {
            results.add(input(input));
        }
        return results;
    }

    /**
     * 指定の名前を有する出力を返す。
     * @param output 出力名
     * @return 発見したポート
     */
    public FlowElementOutput output(String output) {
        return findOutput(output);
    }

    /**
     * 指定の名前を有する出力を返す。
     * @param outputs 出力名
     * @return 発見したポート
     */
    public Set<FlowElementOutput> outputs(String... outputs) {
        Set<FlowElementOutput> results = new HashSet<FlowElementOutput>();
        for (String output : outputs) {
            results.add(output(output));
        }
        return results;
    }

    /**
     * 指定の名前を持つ要素の一覧を返す。
     * @param names 名前の一覧
     * @return 要素の一覧
     */
    public Set<FlowElement> getAsSet(String... names) {
        Set<FlowElement> results = new HashSet<FlowElement>();
        for (String name : names) {
            results.add(get(name));
        }
        return results;
    }

    /**
     * ここまでに作成した要素の一覧を返す。
     * @return ここまでに作成した要素の一覧
     */
    public Set<FlowElement> all() {
        return new HashSet<FlowElement>(elements.values());
    }

    /**
     * ここまでに構築した内容を元にしたグラフを返す。
     * @return 構築した内容を元にしたグラフ
     */
    public FlowGraph toGraph() {
        return new FlowGraph(Testing.class, flowInputs, flowOutputs);
    }

    /**
     * ここまでに構築した内容を元にしたパスを返す。
     * <p>
     * 返されるパスはグラフの入力から出力までを表現する。
     * </p>
     * @param direction パスの向き、逆方向を指定すると入出力が反転する
     * @return 構築したパス
     */
    public FlowPath toPath(FlowPath.Direction direction) {
        Set<FlowElement> inputs = new HashSet<FlowElement>();
        Set<FlowElement> passings = new HashSet<FlowElement>();
        Set<FlowElement> outputs = new HashSet<FlowElement>();

        for (FlowIn<?> node : flowInputs) {
            inputs.add(node.getFlowElement());
        }
        for (FlowOut<?> node : flowOutputs) {
            outputs.add(node.getFlowElement());
        }
        passings.removeAll(inputs);
        passings.removeAll(outputs);
        return new FlowPath(
                direction,
                direction == FlowPath.Direction.FORWARD ? inputs : outputs,
                passings,
                direction == FlowPath.Direction.FORWARD ? outputs : inputs);
    }

    private static final Pattern PORT = Pattern.compile("(.+?)(\\.(.+?))?");

    private FlowElementInput findInput(String spec) {
        Matcher matcher = PORT.matcher(spec);
        if (matcher.matches() == false) {
            throw new AssertionError(spec);
        }
        String elementName = matcher.group(1);
        FlowElement element = elements.get(elementName);
        if (element == null) {
            throw new AssertionError(elementName + elements.keySet());
        }

        String portName = matcher.group(3);
        if (portName == null) {
            if (element.getInputPorts().size() != 1) {
                throw new AssertionError(element.getInputPorts());
            }
            return element.getInputPorts().get(0);
        }

        FlowElementInput port = null;
        for (FlowElementInput finding : element.getInputPorts()) {
            if (portName.equals(finding.getDescription().getName())) {
                port = finding;
                break;
            }
        }
        if (port == null) {
            throw new AssertionError(elementName + "." + portName + elements.keySet());

        }
        return port;
    }

    private FlowElementOutput findOutput(String spec) {
        Matcher matcher = PORT.matcher(spec);
        if (matcher.matches() == false) {
            throw new AssertionError(spec);
        }
        String elementName = matcher.group(1);
        FlowElement element = elements.get(elementName);
        if (element == null) {
            throw new AssertionError(elementName + elements.keySet());
        }

        String portName = matcher.group(3);
        if (portName == null) {
            if (element.getOutputPorts().size() != 1) {
                throw new AssertionError(element.getOutputPorts());
            }
            return element.getOutputPorts().get(0);
        }

        FlowElementOutput port = null;
        for (FlowElementOutput finding : element.getOutputPorts()) {
            if (portName.equals(finding.getDescription().getName())) {
                port = finding;
                break;
            }
        }
        if (port == null) {
            throw new AssertionError(elementName + "." + portName + elements.keySet());

        }
        return port;
    }

    private FlowElement register(String name, FlowElementDescription desc) {
        FlowElement element = new FlowElement(desc);
        return register(name, element);
    }

    private FlowElement register(String name, FlowElement element) {
        if (elements.containsKey(name)) {
            throw new AssertionError(name + elements.keySet());
        }
        elements.put(name, element);
        return element;
    }

    private List<FlowElementPortDescription> parsePorts(
            PortDirection direction,
            String nameList) {
        String[] names = nameList.trim().split("\\s+");
        List<FlowElementPortDescription> results = new ArrayList<FlowElementPortDescription>();
        for (String name : names) {
            results.add(new FlowElementPortDescription(name, TYPE, direction));
        }
        return results;
    }

    private static class Testing extends FlowDescription {
        @Override
        protected void describe() {
            return;
        }
    }
}
