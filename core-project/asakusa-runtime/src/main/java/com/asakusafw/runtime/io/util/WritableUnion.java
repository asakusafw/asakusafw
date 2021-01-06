/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.runtime.io.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.text.MessageFormat;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableUtils;
import org.apache.hadoop.util.ReflectionUtils;

/**
 * An abstract implementation of union object.
 * @since 0.2.5
 */
public class WritableUnion implements Union, Writable {

    private final Writable[] objects;

    private int position;

    /**
     * Creates a new instance.
     * @param classes element classes
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    protected WritableUnion(Class<?>... classes) {
        if (classes == null) {
            throw new IllegalArgumentException("classes must not be null"); //$NON-NLS-1$
        }
        this.objects = new Writable[classes.length];
        for (int i = 0; i < classes.length; i++) {
            this.objects[i] = (Writable) ReflectionUtils.newInstance(classes[i], null);
        }
    }

    /**
     * Creates a new instance.
     * @param objects elements
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    protected WritableUnion(Writable... objects) {
        if (objects == null) {
            throw new IllegalArgumentException("objects must not be null"); //$NON-NLS-1$
        }
        this.objects = objects.clone();
    }

    @Override
    public final int getPosition() {
        return position;
    }

    @Override
    public Object switchObject(int newPosition) {
        this.position = newPosition;
        return getObject();
    }

    @Override
    public final Object getObject() {
        return objects[position];
    }

    @Override
    public void write(DataOutput out) throws IOException {
        WritableUtils.writeVInt(out, position);
        objects[position].write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.position = WritableUtils.readVInt(in);
        objects[position].readFields(in);
    }

    @Override
    public String toString() {
        try {
            return MessageFormat.format(
                    "Union (position={0}, object={1})", //$NON-NLS-1$
                    position,
                    getObject());
        } catch (RuntimeException e) {
            return MessageFormat.format(
                    "Union (position={0}, object=(invalid))", //$NON-NLS-1$
                    position);
        }
    }
}
