/**
 * Copyright 2011-2015 Asakusa Framework Team.
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

/**
 * {@link FlowElementPort}間の結線を表現する。
 */
public class PortConnection {

    private final FlowElementOutput upstream;

    private final FlowElementInput downstream;

    private boolean connected;

    /**
     * インスタンスを生成する。
     * @param upstream 上流ポート
     * @param downstream 下流ポート
     * @throws IllegalArgumentException 入出力の型に不一致がある場合、
     *     または引数に{@code null}が指定された場合
     */
    PortConnection(FlowElementOutput upstream, FlowElementInput downstream) {
        if (upstream == null) {
            throw new IllegalArgumentException("upstream must not be null"); //$NON-NLS-1$
        }
        if (downstream == null) {
            throw new IllegalArgumentException("downstream must not be null"); //$NON-NLS-1$
        }
        FlowElementPortDescription up = upstream.getDescription();
        FlowElementPortDescription down = downstream.getDescription();
        if (down.getDataType().equals(up.getDataType()) == false) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "Invalid data type {0} ({1}) -> {2} ({3})",
                    up.getName(),
                    up.getDataType(),
                    down.getName(),
                    down.getDataType()));
        }
        this.upstream = upstream;
        this.downstream = downstream;
    }

    /**
     * 指定のポート間を接続する。
     * @param upstream 上流ポート
     * @param downstream 下流ポート
     * @throws IllegalArgumentException {@code upstream}に出力以外が指定された場合、
     *     または{@code downstream}に入力以外が指定された場合、
     *     または入出力の型に不一致がある場合、
     *     または引数に{@code null}が指定された場合
     */
    public static void connect(FlowElementOutput upstream, FlowElementInput downstream) {
        if (upstream == null) {
            throw new IllegalArgumentException("upstream must not be null"); //$NON-NLS-1$
        }
        if (downstream == null) {
            throw new IllegalArgumentException("downstream must not be null"); //$NON-NLS-1$
        }
        if (isConnected(upstream, downstream)) {
            return;
        }
        connect0(upstream, downstream);
    }

    private static boolean isConnected(FlowElementOutput upstream, FlowElementInput downstream) {
        assert upstream != null;
        assert downstream != null;
        if (upstream.getConnected().size() > downstream.getConnected().size()) {
            for (PortConnection c : downstream.getConnected()) {
                if (c.getUpstream() == upstream) {
                    return true;
                }
            }
        } else {
            for (PortConnection c : upstream.getConnected()) {
                if (c.getDownstream() == downstream) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void connect0(FlowElementOutput upstream, FlowElementInput downstream) {
        assert upstream != null;
        assert downstream != null;
        PortConnection connection = new PortConnection(upstream, downstream);
        upstream.register(connection);
        downstream.register(connection);
        connection.connected = true;
    }

    /**
     * この接続を解除する。
     * <p>
     * 接続が既に解除されている場合、このメソッドはなにも行わない。
     * </p>
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public void disconnect() {
        if (isValid() == false) {
            return;
        }
        upstream.unregister(this);
        downstream.unregister(this);
    }

    /**
     * この接続が解除されておらず、有効である場合に{@code true}を返す。
     * @return この接続が有効である場合に{@code true}
     */
    public boolean isValid() {
        return connected;
    }

    /**
     * この結線の上流ポートを返す。
     * @return この結線の上流ポート
     */
    public FlowElementOutput getUpstream() {
        return upstream;
    }

    /**
     * この結線の下流ポートを返す。
     * @return この結線の下流ポート
     */
    public FlowElementInput getDownstream() {
        return downstream;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0} => {1}",
                getUpstream(),
                getDownstream());
    }
}
