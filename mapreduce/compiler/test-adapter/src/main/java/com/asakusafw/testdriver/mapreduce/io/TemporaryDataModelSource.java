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
package com.asakusafw.testdriver.mapreduce.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.stage.input.TemporaryInputFormat;
import com.asakusafw.runtime.stage.temporary.TemporaryStorage;
import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.DataModelSource;

/**
 * {@link DataModelSource} using {@link TemporaryInputFormat}.
 * @since 0.2.5
 */
public class TemporaryDataModelSource implements DataModelSource {

    private final Configuration conf;

    private final DataModelDefinition<Object> definition;

    private final Object object;

    private final FileSystem fs;

    private final Iterator<Path> rest;

    private volatile ModelInput<Object> current;

    /**
     * Creates a new instance.
     * @param conf current configuration
     * @param definition data type
     * @param pathExpression the source path (can include wildcard)
     * @throws IOException if failed to create instance
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    @SuppressWarnings("unchecked")
    public TemporaryDataModelSource(
            Configuration conf,
            DataModelDefinition<?> definition,
            String pathExpression) throws IOException {
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        if (definition == null) {
            throw new IllegalArgumentException("definition must not be null"); //$NON-NLS-1$
        }
        if (pathExpression == null) {
            throw new IllegalArgumentException("pathExpression must not be null"); //$NON-NLS-1$
        }
        this.conf = conf;
        this.definition = (DataModelDefinition<Object>) definition;
        this.object = definition.toObject(definition.newReflection().build());
        Path path = new Path(pathExpression);
        this.fs = path.getFileSystem(conf);
        FileStatus[] list = fs.globStatus(path);
        List<Path> paths = new ArrayList<>();
        for (int i = 0; i < list.length; i++) {
            paths.add(list[i].getPath());
        }
        this.rest = paths.iterator();
    }

    @Override
    public DataModelReflection next() throws IOException {
        while (true) {
            if (current == null) {
                if (rest.hasNext() == false) {
                    return null;
                }
                current = TemporaryStorage.openInput(conf, definition.getModelClass(), rest.next());
            }
            if (current.readTo(object)) {
                break;
            } else {
                current.close();
                current = null;
            }
        }
        return definition.toReflection(object);
    }

    @Override
    public void close() throws IOException {
        if (current != null) {
            current.close();
            current = null;
        }
    }
}
