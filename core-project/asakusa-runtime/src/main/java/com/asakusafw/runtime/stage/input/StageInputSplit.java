/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.runtime.stage.input;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableUtils;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.util.ReflectionUtils;

/**
 * An implementation of Hadoop {@link InputSplit} for handling multiple Map operations.
 * @since 0.1.0
 * @version 0.2.6
 */
public class StageInputSplit extends InputSplit implements Writable, Configurable {

    private Class<? extends Mapper<?, ?, ?, ?>> mapperClass;

    private List<Source> sources = new ArrayList<>();

    private Configuration configuration;

    private String[] locations;

    /**
     * Creates a new instance for the serialization framework.
     */
    public StageInputSplit() {
        return;
    }

    /**
     * Creates a new instance.
     * @param original the actual {@link InputSplit}
     * @param formatClass the {@link InputFormat} for handling this split
     * @param mapperClass the {@link Mapper} for handling this split
     * @throws IllegalArgumentException if some parameters are {@code null}
     * @deprecated Use {@link #StageInputSplit(Class, List)} instead
     */
    @Deprecated
    public StageInputSplit(
            InputSplit original,
            Class<? extends InputFormat<?, ?>> formatClass,
            Class<? extends Mapper<?, ?, ?, ?>> mapperClass) {
        if (original == null) {
            throw new IllegalArgumentException("original must not be null"); //$NON-NLS-1$
        }
        if (formatClass == null) {
            throw new IllegalArgumentException("formatClass must not be null"); //$NON-NLS-1$
        }
        if (mapperClass == null) {
            throw new IllegalArgumentException("mapperClass must not be null"); //$NON-NLS-1$
        }
        this.sources = Collections.singletonList(new Source(original, formatClass));
        this.mapperClass = mapperClass;
    }

    /**
     * Creates a new instance.
     * @param mapperClass target mapper class
     * @param sources input format and splits
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.2.6
     */
    public StageInputSplit(Class<? extends Mapper<?, ?, ?, ?>> mapperClass, List<Source> sources) {
        this(mapperClass, sources, null);
    }

    /**
     * Creates a new instance.
     * @param mapperClass target mapper class
     * @param sources input format and splits
     * @param locations hint for locations (nullable)
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.2.6
     */
    public StageInputSplit(Class<? extends Mapper<?, ?, ?, ?>> mapperClass, List<Source> sources, String[] locations) {
        if (mapperClass == null) {
            throw new IllegalArgumentException("mapperClass must not be null"); //$NON-NLS-1$
        }
        if (sources == null) {
            throw new IllegalArgumentException("sources must not be null"); //$NON-NLS-1$
        }
        this.mapperClass = mapperClass;
        this.sources = sources;
        this.locations = locations == null ? null : locations.clone();
    }

    @Override
    public long getLength() throws IOException, InterruptedException {
        long results = 0;
        for (Source source : sources) {
            results += source.getSplit().getLength();
        }
        return results;
    }

    @Override
    public String[] getLocations() throws IOException, InterruptedException {
        if (locations != null) {
            return locations.clone();
        }
        List<String> results = new ArrayList<>();
        Set<String> saw = new HashSet<>();
        for (Source source : sources) {
            String[] elements = source.getSplit().getLocations();
            if (elements != null) {
                for (String element : elements) {
                    if (saw.contains(element) == false) {
                        saw.add(element);
                        results.add(element);
                    }
                }
            }
        }
        return results.toArray(new String[results.size()]);
    }

    /**
     * Returns the actual input split.
     * @return the actual input split
     * @deprecated Use {@link #getSources()} instead.
     */
    @Deprecated
    public InputSplit getOriginal() {
        if (sources.size() != 1) {
            throw new UnsupportedOperationException();
        }
        return sources.get(0).getSplit();
    }

    /**
     * Returns the input format for handling this.
     * @return the input format for handling this
     * @deprecated Use {@link #getSources()} instead.
     */
    @Deprecated
    public Class<? extends InputFormat<?, ?>> getFormatClass() {
        if (sources.size() != 1) {
            throw new UnsupportedOperationException();
        }
        return sources.get(0).getFormatClass();
    }

    /**
     * Returns the sources.
     * @return the sources
     * @since 0.2.6
     */
    public List<Source> getSources() {
        return sources;
    }

    /**
     * Returns the mapper class for handling this.
     * @return the mapper class for handling this
     */
    public Class<? extends Mapper<?, ?, ?, ?>> getMapperClass() {
        return mapperClass;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        writeClassByName(out, mapperClass);
        WritableUtils.writeVInt(out, sources.size());
        for (Source source : sources) {
            Class<? extends InputSplit> splitClass = source.getSplit().getClass();
            writeClassByName(out, source.getFormatClass());
            writeClassByName(out, splitClass);
            ((Writable) source.getSplit()).write(out);
        }
        if (locations == null) {
            WritableUtils.writeVInt(out, -1);
        } else {
            WritableUtils.writeVInt(out, locations.length);
            for (String string : locations) {
                WritableUtils.writeString(out, string);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readFields(DataInput in) throws IOException {
        this.mapperClass = (Class<? extends Mapper<?, ?, ?, ?>>) readClassByName(Mapper.class, in);
        int sourceCount = WritableUtils.readVInt(in);
        List<Source> newSources = new ArrayList<>();
        for (int i = 0; i < sourceCount; i++) {
            Class<? extends InputFormat<?, ?>> formatClass =
                (Class<? extends InputFormat<?, ?>>) readClassByName(InputFormat.class, in);
            Class<? extends InputSplit> splitClass = readClassByName(InputSplit.class, in);
            InputSplit inputSplit = ReflectionUtils.newInstance(splitClass, getConf());
            ((Writable) inputSplit).readFields(in);
            newSources.add(new Source(inputSplit, formatClass));
        }
        this.sources = newSources;
        int locationCount = WritableUtils.readVInt(in);
        if (locationCount < 0) {
            this.locations = null;
        } else {
            String[] array = new String[locationCount];
            for (int i = 0; i < array.length; i++) {
                array[i] = WritableUtils.readString(in);
            }
            this.locations = array;
        }
    }

    private void writeClassByName(DataOutput out, Class<?> aClass) throws IOException {
        assert out != null;
        assert aClass != null;
        out.writeUTF(aClass.getName());
    }

    private <T> Class<? extends T> readClassByName(
            Class<T> baseClass,
            DataInput in) throws IOException {
        assert baseClass != null;
        assert in != null;
        String className = in.readUTF();
        try {
            Class<?> loaded = getConf().getClassByName(className);
            return loaded.asSubclass(baseClass);
        } catch (Exception e) {
            throw new IOException(MessageFormat.format(
                    "Failed to resolve a class name: {0}",
                    className), e);
        }
    }

    @Override
    public void setConf(Configuration conf) {
        this.configuration = conf;
    }

    @Override
    public Configuration getConf() {
        return configuration;
    }

    /**
     * Represents a pair of {@link InputSplit} and its {@link InputFormat} class.
     * @since 0.2.6
     */
    public static final class Source {

        private final InputSplit split;

        private final Class<? extends InputFormat<?, ?>> formatClass;

        /**
         * Creates a new instance.
         * @param split an {@link InputSplit}
         * @param formatClass a class of {@link InputFormat}
         * @throws IllegalArgumentException if some parameters were {@code null}
         */
        public Source(InputSplit split, Class<? extends InputFormat<?, ?>> formatClass) {
            if (split == null) {
                throw new IllegalArgumentException("split must not be null"); //$NON-NLS-1$
            }
            if (formatClass == null) {
                throw new IllegalArgumentException("formatClass must not be null"); //$NON-NLS-1$
            }
            this.split = split;
            this.formatClass = formatClass;
        }

        /**
         * Returns the {@link InputSplit}.
         * @return the split
         */
        public InputSplit getSplit() {
            return split;
        }

        /**
         * Returns the {@link InputFormat} class.
         * @return the format class
         */
        public Class<? extends InputFormat<?, ?>> getFormatClass() {
            return formatClass;
        }
    }
}
