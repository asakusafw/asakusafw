/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.vocabulary.flow.graph;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;

/**
 * フロー内の任意の要素が持つ出力ポートを表現する。
 * <p>
 * DSL利用者はこのクラスのオブジェクトを直接操作すべきでない。
 * </p>
 */
public final class FlowElementOutput extends FlowElementPort {

    /**
     * インスタンスを生成する。
     * @param description このポートの定義記述
     * @param owner このポートを有するフロー要素
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public FlowElementOutput(
            FlowElementPortDescription description,
            FlowElement owner) {
        super(description, owner);
    }

    /**
     * このポートに接続されている全ての入力ポートを返す。
     * @return このポートに接続されている全ての入力ポート
     */
    public Collection<FlowElementInput> getOpposites() {
        Collection<FlowElementInput> results = new ArrayList<FlowElementInput>();
        for (PortConnection conn : getConnected()) {
            results.add(conn.getDownstream());
        }
        return results;
    }

    /**
     * このポートに接続されている全ての入力ポートに対し、その接続を解除する。
     * @return 解除した出力ポートの一覧
     */
    @Override
    public Collection<FlowElementInput> disconnectAll() {
        Collection<FlowElementInput> results = new ArrayList<FlowElementInput>();
        for (PortConnection conn : new ArrayList<PortConnection>(getConnected())) {
            results.add(conn.getDownstream());
            conn.disconnect();
        }
        return results;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}<-{1}",
                getDescription().getName(),
                getOwner());
    }
}
