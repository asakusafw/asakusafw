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
 * Represents a connection between {@link FlowElementPort}s.
 */
public final class PortConnection {

    private final FlowElementOutput upstream;

    private final FlowElementInput downstream;

    private boolean connected;

    private PortConnection(FlowElementOutput upstream, FlowElementInput downstream) {
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
                    "Invalid data type {0} ({1}) -> {2} ({3})", //$NON-NLS-1$
                    up.getName(),
                    up.getDataType(),
                    down.getName(),
                    down.getDataType()));
        }
        this.upstream = upstream;
        this.downstream = downstream;
    }

    /**
     * Connects between the two ports.
     * @param upstream the upstream port
     * @param downstream the downstream port
     * @throws IllegalArgumentException if the data type is not consistent, or some parameters are {@code null}
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
     * Disposes this connection.
     * If this has been already disposed, this method will have no effects.
     */
    public void disconnect() {
        if (isValid() == false) {
            return;
        }
        upstream.unregister(this);
        downstream.unregister(this);
    }

    /**
     * Returns whether this connection is valid or not.
     * @return {@code true} if this connection is valid, or {@code false} if this is {@link #disconnect() disposed}
     */
    public boolean isValid() {
        return connected;
    }

    /**
     * Returns the upstream port.
     * @return the upstream port
     */
    public FlowElementOutput getUpstream() {
        return upstream;
    }

    /**
     * Returns the downstream port.
     * @return the downstream port
     */
    public FlowElementInput getDownstream() {
        return downstream;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0} => {1}", //$NON-NLS-1$
                getUpstream(),
                getDownstream());
    }
}
