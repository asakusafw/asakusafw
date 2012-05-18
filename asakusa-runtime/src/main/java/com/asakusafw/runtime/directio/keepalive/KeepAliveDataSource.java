/**
 * Copyright 2012 Asakusa Framework Team.
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
package com.asakusafw.runtime.directio.keepalive;

import java.io.IOException;
import java.util.List;

import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.directio.DataFormat;
import com.asakusafw.runtime.directio.DirectDataSource;
import com.asakusafw.runtime.directio.DirectInputFragment;
import com.asakusafw.runtime.directio.OutputAttemptContext;
import com.asakusafw.runtime.directio.OutputTransactionContext;
import com.asakusafw.runtime.directio.ResourcePattern;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;

/**
 * A wrapper of {@link DirectDataSource} that sends heartbeat continuously.
 * @since 0.2.6
 */
public class KeepAliveDataSource implements DirectDataSource {

    private final DirectDataSource entity;

    final HeartbeatKeeper heartbeat;

    /**
     * Creates a new instance.
     * @param entity component {@link DirectDataSource}
     * @param interval heartbeat interval
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public KeepAliveDataSource(DirectDataSource entity, long interval) {
        if (entity == null) {
            throw new IllegalArgumentException("entity must not be null"); //$NON-NLS-1$
        }
        this.entity = entity;
        this.heartbeat = new HeartbeatKeeper(interval);
    }

    @Override
    public <T> List<DirectInputFragment> findInputFragments(
            Class<? extends T> dataType,
            DataFormat<T> format,
            String basePath,
            ResourcePattern resourcePattern) throws IOException, InterruptedException {
        return entity.findInputFragments(dataType, format, basePath, resourcePattern);
    }

    @Override
    public <T> ModelInput<T> openInput(
            Class<? extends T> dataType,
            DataFormat<T> format,
            DirectInputFragment fragment,
            Counter counter) throws IOException, InterruptedException {
        ModelInput<T> input = entity.openInput(dataType, format, fragment, counter);
        return new WrappedModelInput<T>(input, heartbeat, counter);
    }

    @Override
    public <T> ModelOutput<T> openOutput(
            OutputAttemptContext context,
            Class<? extends T> dataType,
            DataFormat<T> format,
            String basePath,
            String resourcePath,
            Counter counter) throws IOException, InterruptedException {
        ModelOutput<T> output = entity.openOutput(context, dataType, format, basePath, resourcePath, counter);
        return new WrappedModelOutput<T>(output, heartbeat, counter);
    }

    @Override
    public boolean delete(String basePath, ResourcePattern resourcePattern) throws IOException, InterruptedException {
        return entity.delete(basePath, resourcePattern);
    }

    @Override
    public void setupAttemptOutput(OutputAttemptContext context) throws IOException, InterruptedException {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        Counter counter = context.getCounter();
        heartbeat.register(counter);
        try {
            entity.setupAttemptOutput(context);
        } finally {
            heartbeat.unregister(counter);
        }
    }

    @Override
    public void commitAttemptOutput(OutputAttemptContext context) throws IOException, InterruptedException {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        Counter counter = context.getCounter();
        heartbeat.register(counter);
        try {
            entity.commitAttemptOutput(context);
        } finally {
            heartbeat.unregister(counter);
        }
    }

    @Override
    public void cleanupAttemptOutput(OutputAttemptContext context) throws IOException, InterruptedException {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        Counter counter = context.getCounter();
        heartbeat.register(counter);
        try {
            entity.cleanupAttemptOutput(context);
        } finally {
            heartbeat.unregister(counter);
        }
    }

    @Override
    public void setupTransactionOutput(OutputTransactionContext context) throws IOException, InterruptedException {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        Counter counter = context.getCounter();
        heartbeat.register(counter);
        try {
            entity.setupTransactionOutput(context);
        } finally {
            heartbeat.unregister(counter);
        }
    }

    @Override
    public void commitTransactionOutput(OutputTransactionContext context) throws IOException, InterruptedException {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        Counter counter = context.getCounter();
        heartbeat.register(counter);
        try {
            entity.commitTransactionOutput(context);
        } finally {
            heartbeat.unregister(counter);
        }
    }

    @Override
    public void cleanupTransactionOutput(OutputTransactionContext context) throws IOException,  InterruptedException {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        Counter counter = context.getCounter();
        heartbeat.register(counter);
        try {
            entity.cleanupTransactionOutput(context);
        } finally {
            heartbeat.unregister(counter);
        }
    }

    private static final class WrappedModelInput<T> implements ModelInput<T> {

        private final ModelInput<T> component;

        private final HeartbeatKeeper keeper;

        private final Counter counter;

        private boolean closed = false;

        WrappedModelInput(ModelInput<T> component, HeartbeatKeeper keeper, Counter counter) {
            assert component != null;
            assert keeper != null;
            assert counter != null;
            this.component = component;
            this.keeper = keeper;
            this.counter = counter;
            keeper.register(counter);
        }

        @Override
        public boolean readTo(T model) throws IOException {
            return component.readTo(model);
        }

        @Override
        public void close() throws IOException {
            if (closed) {
                return;
            }
            try {
                component.close();
            } finally {
                keeper.unregister(counter);
                closed = true;
            }
        }
    }

    private static final class WrappedModelOutput<T> implements ModelOutput<T> {

        private final ModelOutput<T> component;

        private final HeartbeatKeeper keeper;

        private final Counter counter;

        private boolean closed = false;

        WrappedModelOutput(ModelOutput<T> component, HeartbeatKeeper keeper, Counter counter) {
            assert component != null;
            assert keeper != null;
            assert counter != null;
            this.component = component;
            this.keeper = keeper;
            this.counter = counter;
            keeper.register(counter);
        }

        @Override
        public void write(T model) throws IOException {
            component.write(model);
        }

        @Override
        public void close() throws IOException {
            if (closed) {
                return;
            }
            try {
                component.close();
            } finally {
                keeper.unregister(counter);
                closed = true;
            }
        }
    }
}
