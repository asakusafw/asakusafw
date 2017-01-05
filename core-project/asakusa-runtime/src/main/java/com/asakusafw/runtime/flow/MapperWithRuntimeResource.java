/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.runtime.flow;

import java.io.IOException;
import java.text.MessageFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.mapreduce.Mapper;

import com.asakusafw.runtime.core.legacy.RuntimeResource;

/**
 * An abstract super class of a mapper with {@link RuntimeResource}s.
 * @param <KEYIN> input key type
 * @param <VALUEIN> input value type
 * @param <KEYOUT> output key type
 * @param <VALUEOUT> output value type
 * @since 0.5.1
 */
public abstract class MapperWithRuntimeResource<KEYIN, VALUEIN, KEYOUT, VALUEOUT>
        extends Mapper<KEYIN, VALUEIN, KEYOUT, VALUEOUT> {

    private static final Log LOG = LogFactory.getLog(MapperWithRuntimeResource.class);

    @SuppressWarnings("unused")
    private byte[] oombuf = new byte[4096];

    private RuntimeResourceManager resources;

    /**
     * Invokes {@code Mapper#run(Context)} internally.
     * Clients can override this method and implement customized {@code run} method.
     * @param context current context
     * @throws IOException if task is failed by I/O error
     * @throws InterruptedException if task execution is interrupted
     */
    protected void runInternal(Context context) throws IOException, InterruptedException {
        super.run(context);
    }

    @Override
    public final void run(Context context) throws IOException, InterruptedException {
        this.resources = new RuntimeResourceManager(context.getConfiguration());
        resources.setup();
        try {
            runInternal(context);
        } catch (Throwable t) {
            oombuf = null;
            LOG.error(MessageFormat.format(
                    "error occurred while executing mapper: {0}",
                    getClass().getName()), t);
            if (t instanceof Error) {
                throw (Error) t;
            } else if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else if (t instanceof IOException) {
                throw (IOException) t;
            } else if (t instanceof InterruptedException) {
                throw (InterruptedException) t;
            } else {
                throw new AssertionError(t);
            }
        } finally {
            this.resources.cleanup();
        }
    }
}
