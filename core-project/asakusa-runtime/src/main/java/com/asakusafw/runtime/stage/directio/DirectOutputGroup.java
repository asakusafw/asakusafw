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
package com.asakusafw.runtime.stage.directio;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.asakusafw.runtime.directio.DataFormat;
import com.asakusafw.runtime.io.util.NullWritableRawComparable;
import com.asakusafw.runtime.io.util.WritableRawComparable;

/**
 * Group object in shuffle key in direct output stages.
 * @since 0.2.5
 */
class DirectOutputGroup implements WritableRawComparable {

    /**
     * An empty output.
     * @since 0.4.0
     */
    public static final DirectOutputGroup EMPTY = new DirectOutputGroup();

    private final String path;

    private final Class<?> dataType;

    private final DataFormat<?> format;

    private final StringTemplate nameGenerator;

    private DirectOutputGroup() {
        this.path = "";
        this.dataType = NullWritableRawComparable.class;
        this.format = new DataFormat<NullWritableRawComparable>() {
            @Override
            public Class<NullWritableRawComparable> getSupportedType() {
                return NullWritableRawComparable.class;
            }
        };
        this.nameGenerator = StringTemplate.EMPTY;
    }

    /**
     * Creates a new instance.
     * @param path the path
     * @param dataType the data type
     * @param format format object
     * @param nameGenerator name generator
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public DirectOutputGroup(String path, Class<?> dataType, DataFormat<?> format, StringTemplate nameGenerator) {
        if (path == null) {
            throw new IllegalArgumentException("path must not be null"); //$NON-NLS-1$
        }
        if (dataType == null) {
            throw new IllegalArgumentException("dataType must not be null"); //$NON-NLS-1$
        }
        if (format == null) {
            throw new IllegalArgumentException("format must not be null"); //$NON-NLS-1$
        }
        if (nameGenerator == null) {
            throw new IllegalArgumentException("nameGenerator must not be null"); //$NON-NLS-1$
        }
        this.path = path;
        this.dataType = dataType;
        this.format = format;
        this.nameGenerator = nameGenerator;
    }

    public void set(Object value) {
        nameGenerator.set(value);
    }

    public String getPath() {
        return path;
    }

    public Class<?> getDataType() {
        return dataType;
    }

    public DataFormat<?> getFormat() {
        return format;
    }

    public String getResourcePath() {
        return nameGenerator.apply();
    }

    public final String generateName() {
        return nameGenerator.apply();
    }

    @Override
    public final int getSizeInBytes(byte[] buf, int offset) throws IOException {
        return nameGenerator.getSizeInBytes(buf, offset);
    }

    @Override
    public final int compareInBytes(byte[] b1, int o1, byte[] b2, int o2) throws IOException {
        return nameGenerator.compareInBytes(b1, o1, b2, o2);
    }

    @Override
    public final int compareTo(WritableRawComparable o) {
        return nameGenerator.compareTo(o);
    }

    @Override
    public final void write(DataOutput out) throws IOException {
        nameGenerator.write(out);
    }

    @Override
    public final void readFields(DataInput in) throws IOException {
        nameGenerator.readFields(in);
    }

    @Override
    public int hashCode() {
        return nameGenerator.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DirectOutputGroup other = (DirectOutputGroup) obj;
        if (!nameGenerator.equals(other.nameGenerator)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DirectOutputGroup [path=");
        builder.append(path);
        builder.append(", format=");
        builder.append(format);
        builder.append(", nameGenerator=");
        builder.append(nameGenerator);
        builder.append("]");
        return builder.toString();
    }
}
