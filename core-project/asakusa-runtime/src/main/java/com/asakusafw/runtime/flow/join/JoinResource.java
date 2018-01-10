/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.runtime.flow.join;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.JobContext;

import com.asakusafw.runtime.flow.FlowResource;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.stage.resource.StageResourceDriver;
import com.asakusafw.runtime.stage.temporary.TemporaryStorage;

/**
 * An abstract implementation of resource for providing lookup table.
 * @param <L> the left value type (provides lookup table for this type)
 * @param <R> the right value type (looking up using this type)
 */
public abstract class JoinResource<L extends Writable, R> implements FlowResource {

    static final Log LOG = LogFactory.getLog(JoinResource.class);

    private final LookUpKey lookupKeyBuffer = new LookUpKey();

    private LookUpTable<L> table;

    @Override
    public void setup(JobContext context) throws IOException, InterruptedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug(MessageFormat.format(
                    "Building join-table from \"{0}\" on distributed cache", //$NON-NLS-1$
                    getCacheName()));
        }
        try (StageResourceDriver driver = new StageResourceDriver(context)) {
            List<Path> paths = driver.findCache(getCacheName());
            if (paths.isEmpty()) {
                throw new FileNotFoundException(MessageFormat.format(
                        "Missing resource \"{0}\" in distributed cache",
                        getCacheName()));
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Building join table \"{0}\" using \"{1}\"", //$NON-NLS-1$
                        getCacheName(),
                        paths));
            }
            try {
                table = createTable(driver, paths);
            } catch (IOException e) {
                throw new IOException(MessageFormat.format(
                        "Failed to build a join table from \"{0}\"",
                        getCacheName()), e);
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Built join-table from \"{0}\"", //$NON-NLS-1$
                        getCacheName()));
            }
        }
    }
    private LookUpTable<L> createTable(
            StageResourceDriver driver,
            List<Path> paths) throws IOException {
        assert driver != null;
        assert paths != null;
        LookUpTable.Builder<L> builder = createLookUpTable();
        L value = createValueObject();
        for (Path path : paths) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(MessageFormat.format(
                        "Reading local cache fragment \"{1}\" for join table {0}", //$NON-NLS-1$
                        getCacheName(),
                        path));
            }

            try (@SuppressWarnings("unchecked") ModelInput<L> input = (ModelInput<L>) TemporaryStorage.openInput(
                    driver.getConfiguration(),
                    value.getClass(),
                    path)) {
                while (input.readTo(value)) {
                    lookupKeyBuffer.reset();
                    LookUpKey k = buildLeftKey(value, lookupKeyBuffer);
                    builder.add(k, value);
                    value = createValueObject();
                }
            }
        }
        return builder.build();
    }

    /**
     * Returns a builder for building a new lookup table.
     * @return the created builder
     */
    protected LookUpTable.Builder<L> createLookUpTable() {
        return new VolatileLookUpTable.Builder<>();
    }

    /**
     * Returns the name of Hadoop distributed cache which provides contents of the lookup table.
     * @return the name of Hadoop distributed cache
     */
    protected abstract String getCacheName();

    /**
     * Returns a new left value object.
     * @return the created object
     */
    protected abstract L createValueObject();

    /**
     * Returns a lookup key from a left value.
     * @param value the left value
     * @param buffer the key buffer
     * @return the key object
     * @throws IOException if error occurred while building the lookup key
     * @throws IllegalArgumentException if some parameters are {@code null}
     */
    protected abstract LookUpKey buildLeftKey(L value, LookUpKey buffer) throws IOException;

    /**
     * Returns a lookup key from a right value.
     * @param value the right value
     * @param buffer the key buffer
     * @return the key object
     * @throws IOException if error occurred while building the lookup key
     * @throws IllegalArgumentException if some parameters are {@code null}
     */
    protected abstract LookUpKey buildRightKey(R value, LookUpKey buffer) throws IOException;

    /**
     * Looks up left values from the related right value.
     * @param value the right value
     * @return the related left values
     * @throws LookUpException if error was occurred while looking up the values
     */
    public List<L> find(R value) {
        try {
            lookupKeyBuffer.reset();
            LookUpKey k = buildRightKey(value, lookupKeyBuffer);
            List<L> found = table.get(k);
            return found;
        } catch (IOException e) {
            throw new LookUpException(MessageFormat.format(
                    "Failed to lookup join target for \"{0}\"",
                    value), e);
        }
    }
}
