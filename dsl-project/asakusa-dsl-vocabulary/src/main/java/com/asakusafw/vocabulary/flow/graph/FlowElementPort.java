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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * フロー内の任意の要素が持つ入出力ポートを表現する。
 * <p>
 * DSL利用者はこのクラスのオブジェクトを直接操作すべきでない。
 * </p>
 */
public abstract class FlowElementPort {

    private FlowElement owner;

    private FlowElementPortDescription description;

    private Set<PortConnection> connected;

    /**
     * インスタンスを生成する。
     * @param description このポートの定義記述
     * @param owner このポートを有するフロー要素
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public FlowElementPort(
            FlowElementPortDescription description,
            FlowElement owner) {
        if (owner == null) {
            throw new IllegalArgumentException("owner must not be null"); //$NON-NLS-1$
        }
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        this.owner = owner;
        this.description = description;
        this.connected = new HashSet<PortConnection>();
    }

    /**
     * この入出力ポートをもつ要素を返す。
     * @return この入出力ポートをもつ要素
     */
    public FlowElement getOwner() {
        return owner;
    }

    /**
     * この入出力ポートの定義記述を返す。
     * @return この入出力ポートの定義記述
     */
    public FlowElementPortDescription getDescription() {
        return description;
    }

    /**
     * このポートへの接続を返す。
     * @return このポートへの接続一覧
     */
    public Set<PortConnection> getConnected() {
        return Collections.unmodifiableSet(connected);
    }

    /**
     * このポートに指定の接続を追加する。
     * @param connection 追加する接続
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    void register(PortConnection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("connection must not be null"); //$NON-NLS-1$
        }
        connected.add(connection);
    }

    /**
     * このポートから指定の接続を除去する。
     * @param connection 追加する接続
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    void unregister(PortConnection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("connection must not be null"); //$NON-NLS-1$
        }
        boolean removed = connected.remove(connection);
        if (removed == false) {
            throw new IllegalStateException();
        }
    }
}
