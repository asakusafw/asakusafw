/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.testdriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.directio.BinaryStreamFormat;
import com.asakusafw.runtime.directio.Counter;
import com.asakusafw.runtime.directio.DataFormat;
import com.asakusafw.runtime.directio.hadoop.HadoopDataSourceUtil;
import com.asakusafw.runtime.directio.hadoop.HadoopFileFormat;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.DataModelSource;
import com.asakusafw.testdriver.core.DataModelSourceFactory;
import com.asakusafw.testdriver.core.IteratorDataModelSource;
import com.asakusafw.testdriver.core.TestContext;

final class DirectIoUtil {

    static final Logger LOG = LoggerFactory.getLogger(DirectIoUtil.class);

    private DirectIoUtil() {
        return;
    }

    static <T> DataModelSourceFactory load(
            Configuration configuration,
            DataModelDefinition<T> definition,
            Class<? extends DataFormat<?>> formatClass,
            URL source) throws IOException, InterruptedException {
        DataFormat<? super T> format = newDataFormat(configuration, formatClass);
        if (format.getSupportedType().isAssignableFrom(definition.getModelClass()) == false) {
            throw new IllegalArgumentException();
        }
        return load(configuration, definition, format, source);
    }

    static <T> DataModelSourceFactory load(
            Configuration configuration,
            DataModelDefinition<T> definition,
            Class<? extends DataFormat<?>> formatClass,
            File source) throws IOException, InterruptedException {
        DataFormat<? super T> format = newDataFormat(configuration, formatClass);
        if (format.getSupportedType().isAssignableFrom(definition.getModelClass()) == false) {
            throw new IllegalArgumentException();
        }
        return load(configuration, definition, format, source);
    }

    private static <T> DataModelSourceFactory load(
            Configuration configuration,
            DataModelDefinition<T> definition,
            DataFormat<? super T> format,
            File source) throws IOException, InterruptedException {
        if (format instanceof BinaryStreamFormat<?>) {
            return load0(definition, (BinaryStreamFormat<? super T>) format, source);
        }
        HadoopFileFormat<? super T> hFormat = HadoopDataSourceUtil.toHadoopFileFormat(configuration, format);
        return load0(definition, hFormat, source);
    }

    private static <T> DataModelSourceFactory load(
            Configuration configuration,
            DataModelDefinition<T> definition,
            DataFormat<? super T> format,
            URL source) throws IOException, InterruptedException {
        if (source.getProtocol().equals("file")) { //$NON-NLS-1$
            File file = null;
            try {
                file = new File(source.toURI());
            } catch (URISyntaxException e) {
                LOG.debug("failed to convert URL into local file path: {}", source, e); //$NON-NLS-1$
            }
            if (file != null) {
                return load(configuration, definition, format, file);
            }
        }
        if (format instanceof BinaryStreamFormat<?>) {
            return load0(definition, (BinaryStreamFormat<? super T>) format, source);
        }
        HadoopFileFormat<? super T> hFormat = HadoopDataSourceUtil.toHadoopFileFormat(configuration, format);
        return load0(definition, hFormat, source);
    }

    private static <T> DataFormat<T> newDataFormat(
            Configuration configuration,
            Class<? extends DataFormat<?>> formatClass) {
        try {
            @SuppressWarnings("unchecked")
            DataFormat<T> format = (DataFormat<T>) formatClass.newInstance();
            if (format instanceof Configurable) {
                ((Configurable) format).setConf(configuration);
            }
            return format;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static <T> DataModelSourceFactory load0(
            DataModelDefinition<T> definition,
            BinaryStreamFormat<? super T> format,
            File source) throws IOException, InterruptedException {
        String path = source.toURI().toString();
        try (InputStream stream = new FileInputStream(source);
                ModelInput<? super T> input = format.createInput(definition.getModelClass(), path, stream)) {
            return collect(definition, input);
        }
    }

    private static <T> DataModelSourceFactory load0(
            DataModelDefinition<T> definition,
            HadoopFileFormat<? super T> format,
            File source) throws IOException, InterruptedException {
        try (ModelInput<? super T> input = format.createInput(
                definition.getModelClass(),
                org.apache.hadoop.fs.FileSystem.getLocal(format.getConf()),
                new org.apache.hadoop.fs.Path(source.toURI()),
                new Counter())) {
            return collect(definition, input);
        }
    }

    private static <T> DataModelSourceFactory load0(
            DataModelDefinition<T> definition,
            BinaryStreamFormat<? super T> format,
            URL source) throws IOException, InterruptedException {
        String path = source.toString();
        try (InputStream stream = source.openStream();
                ModelInput<? super T> input = format.createInput(definition.getModelClass(), path, stream)) {
            return collect(definition, input);
        }
    }

    private static <T> DataModelSourceFactory load0(
            DataModelDefinition<T> definition,
            HadoopFileFormat<? super T> format,
            URL source) throws IOException, InterruptedException {
        List<String> segments = Arrays.stream(source.getPath().split("/")) //$NON-NLS-1$
                .map(String::trim)
                .filter(s -> s.isEmpty() == false)
                .collect(Collectors.toList());
        String name;
        if (segments.isEmpty()) {
            name = "testing.file"; //$NON-NLS-1$
        } else {
            name = segments.get(segments.size() - 1);
        }
        Path tmpdir = Files.createTempDirectory("asakusa-"); //$NON-NLS-1$
        try (InputStream in = source.openStream()) {
            Path target = tmpdir.resolve(name);
            Files.copy(in, target);
            return load0(definition, format, target.toFile());
        } finally {
            File dir = tmpdir.toFile();
            if (FileUtils.deleteQuietly(dir) == false && dir.exists()) {
                LOG.warn(MessageFormat.format(
                        "failed to delete a temporary file: {0}",
                        tmpdir));
            }
        }
    }

    private static <T> DataModelSourceFactory collect(
            DataModelDefinition<T> definition,
            ModelInput<? super T> input) throws IOException {
        List<DataModelReflection> loaded = new ArrayList<>();
        T object = newDataObject(definition);
        while (input.readTo(object)) {
            DataModelReflection ref = definition.toReflection(object);
            loaded.add(ref);
        }
        return new DataModelSourceFactory() {
            @Override
            public <U> DataModelSource createSource(DataModelDefinition<U> def, TestContext context) {
                return new IteratorDataModelSource(loaded.iterator());
            }
        };
    }

    private static <T> T newDataObject(DataModelDefinition<T> definition) {
        try {
            return definition.getModelClass().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
