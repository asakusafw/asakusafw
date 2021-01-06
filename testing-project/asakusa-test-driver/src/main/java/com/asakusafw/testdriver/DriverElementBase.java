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
package com.asakusafw.testdriver;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import org.apache.hadoop.conf.Configuration;

import com.asakusafw.runtime.directio.DataFormat;
import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.DataModelSinkFactory;
import com.asakusafw.testdriver.core.DataModelSource;
import com.asakusafw.testdriver.core.DataModelSourceFactory;
import com.asakusafw.testdriver.core.DataModelSourceFilter;
import com.asakusafw.testdriver.core.Difference;
import com.asakusafw.testdriver.core.IteratorDataModelSource;
import com.asakusafw.testdriver.core.SourceDataModelSource;
import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.testdriver.core.TestDataToolProvider;
import com.asakusafw.testdriver.core.Verifier;
import com.asakusafw.testdriver.core.VerifierFactory;
import com.asakusafw.testdriver.core.VerifyContext;
import com.asakusafw.testdriver.core.VerifyRuleFactory;
import com.asakusafw.testdriver.hadoop.ConfigurationFactory;
import com.asakusafw.utils.io.Provider;
import com.asakusafw.utils.io.Source;

/**
 * An abstract implementation of test driver elements.
 * @since 0.7.3
 * @version 0.9.1
 */
public abstract class DriverElementBase {

    /**
     * Returns the caller class.
     * @return the caller class
     */
    protected abstract Class<?> getCallerClass();

    /**
     * Returns the test tools.
     * @return the test tools
     */
    protected abstract TestDataToolProvider getTestTools();

    /**
     * Converts a source path into {@link DataModelSourceFactory} which provides data models.
     * This implementation lazily converts source contents into equivalent {@link DataModelReflection}s.
     * @param sourcePath the source path
     * @return the {@link DataModelSourceFactory}
     * @throws IllegalArgumentException if the target resource is not found
     */
    protected final DataModelSourceFactory toDataModelSourceFactory(String sourcePath) {
        if (sourcePath == null) {
            throw new IllegalArgumentException("sourcePath must not be null"); //$NON-NLS-1$
        }
        URI sourceUri;
        try {
            sourceUri = toUri(sourcePath);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    Messages.getString("DriverElementBase.errorInvalidUri"), //$NON-NLS-1$
                    sourcePath), e);
        }
        return getTestTools().getDataModelSourceFactory(sourceUri);
    }

    /**
     * Converts an data model object collection into {@link DataModelSourceFactory} which provides data models.
     * This implementation immediately converts data model objects into equivalent {@link DataModelReflection}s.
     * @param <T> the data model type
     * @param definition the data model definition
     * @param sourceObjects the original data model objects
     * @return the {@link DataModelSourceFactory}
     */
    protected final <T> DataModelSourceFactory toDataModelSourceFactory(
            DataModelDefinition<T> definition,
            Iterable<? extends T> sourceObjects) {
        if (sourceObjects == null) {
            throw new IllegalArgumentException("sourceObjects must not be null"); //$NON-NLS-1$
        }
        ArrayList<DataModelReflection> results = new ArrayList<>();
        for (T dataModel : sourceObjects) {
            results.add(definition.toReflection(dataModel));
        }
        results.trimToSize();
        return new DataModelSourceFactory() {
            @Override
            public <S> DataModelSource createSource(
                    DataModelDefinition<S> inner, TestContext context) throws IOException {
                if (inner.getModelClass() != definition.getModelClass()) {
                    throw new IllegalStateException();
                }
                return new IteratorDataModelSource(results.iterator());
            }
            @Override
            public String toString() {
                return "DataModelSource(Iterable)"; //$NON-NLS-1$
            }
        };
    }

    /**
     * Converts an data model object collection into {@link DataModelSourceFactory} which provides data models.
     * This implementation lazily converts data model objects into equivalent {@link DataModelReflection}s.
     * @param <T> the data model type
     * @param sourceProvider the original data model objects
     * @return the {@link DataModelSourceFactory}
     */
    protected final <T> DataModelSourceFactory toDataModelSourceFactory(
            Provider<? extends Source<? extends T>> sourceProvider) {
        return new DataModelSourceFactory() {
            @SuppressWarnings({ "rawtypes", "unchecked" })
            @Override
            public DataModelSource createSource(
                    DataModelDefinition definition,
                    TestContext context) throws IOException {
                try {
                    return new SourceDataModelSource<>(definition, sourceProvider.open());
                } catch (InterruptedException e) {
                    throw (InterruptedIOException) new InterruptedIOException().initCause(e);
                }
            }
            @Override
            public String toString() {
                return String.format("DataModelSource(%s)", sourceProvider); //$NON-NLS-1$
            }
        };
    }

    /**
     * Loads a source file  {@link DataModelSourceFactory} which provides data models.
     * This implementation immediately converts loaded into {@link DataModelReflection}s.
     * @param <T> the data type
     * @param definition the data model definition
     * @param formatClass the data format class
     * @param sourcePath the input file path on the class path
     * @return the loaded {@link DataModelSourceFactory}
     * @since 0.9.1
     */
    protected final <T> DataModelSourceFactory toDataModelSourceFactory(
            DataModelDefinition<T> definition,
            Class<? extends DataFormat<? super T>> formatClass,
            String sourcePath) {
        if (definition == null) {
            throw new IllegalArgumentException("definition must not be null"); //$NON-NLS-1$
        }
        if (formatClass == null) {
            throw new IllegalArgumentException("formatClass must not be null"); //$NON-NLS-1$
        }
        if (sourcePath == null) {
            throw new IllegalArgumentException("sourcePath must not be null"); //$NON-NLS-1$
        }
        try {
            Configuration conf = ConfigurationFactory.getDefault().newInstance();
            URL url = toUri(sourcePath).toURL();
            return DirectIoUtil.load(conf, definition, formatClass, url);
        } catch (IOException | InterruptedException | URISyntaxException e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "error occurred while loading Direct I/O input: {0}",
                    sourcePath), e);
        }
    }

    /**
     * Loads a source file  {@link DataModelSourceFactory} which provides data models.
     * This implementation immediately converts loaded into {@link DataModelReflection}s.
     * @param <T> the data type
     * @param definition the data model definition
     * @param formatClass the data format class
     * @param sourceFile the input file
     * @return the loaded {@link DataModelSourceFactory}
     * @since 0.9.1
     */
    protected final <T> DataModelSourceFactory toDataModelSourceFactory(
            DataModelDefinition<T> definition,
            Class<? extends DataFormat<? super T>> formatClass,
            File sourceFile) {
        if (definition == null) {
            throw new IllegalArgumentException("definition must not be null"); //$NON-NLS-1$
        }
        if (formatClass == null) {
            throw new IllegalArgumentException("formatClass must not be null"); //$NON-NLS-1$
        }
        if (sourceFile == null) {
            throw new IllegalArgumentException("sourceFile must not be null"); //$NON-NLS-1$
        }
        try {
            Configuration conf = ConfigurationFactory.getDefault().newInstance();
            return DirectIoUtil.load(conf, definition, formatClass, sourceFile);
        } catch (IOException | InterruptedException e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "error occurred while loading Direct I/O input: {0}",
                    sourceFile), e);
        }
    }

    /**
     * Converts an model transformer into {@link DataModelSourceFilter}.
     * @param definition the target data model definition
     * @param transformer the data model transformer
     * @return the filter which transforms each data model objects using the transformer
     * @param <T> the data model type
     * @since 0.7.3
     */
    protected final <T> DataModelSourceFilter toDataModelSourceFilter(
            DataModelDefinition<T> definition,
            Consumer<? super T> transformer) {
        return new DataModelSourceFilter() {
            @Override
            public DataModelSource apply(DataModelSource source) {
                return new DataModelSource() {
                    @Override
                    public DataModelReflection next() throws IOException {
                        DataModelReflection next = source.next();
                        if (next == null) {
                            return null;
                        }
                        T object = definition.toObject(next);
                        transformer.accept(object);
                        return definition.toReflection(object);
                    }
                    @Override
                    public void close() throws IOException {
                        source.close();
                    }
                };
            }
            @Override
            public String toString() {
                return MessageFormat.format(
                        "Filter(transformer={0})", //$NON-NLS-1$
                        transformer);
            }
        };
    }

    /**
     * Converts an output path to {@link DataModelSinkFactory} to write to the path.
     * @param <T> the data type
     * @param definition the data model definition
     * @param formatClass the data format class
     * @param destinationFile the output path
     * @return the target sink factory
     * @since 0.9.1
     */
    protected final <T> DataModelSinkFactory toDataModelSinkFactory(
            DataModelDefinition<T> definition,
            Class<? extends DataFormat<? super T>> formatClass,
            File destinationFile) {
        if (definition == null) {
            throw new IllegalArgumentException("definition must not be null"); //$NON-NLS-1$
        }
        if (formatClass == null) {
            throw new IllegalArgumentException("formatClass must not be null"); //$NON-NLS-1$
        }
        if (destinationFile == null) {
            throw new IllegalArgumentException("sourceFile must not be null"); //$NON-NLS-1$
        }
        try {
            Configuration conf = ConfigurationFactory.getDefault().newInstance();
            return DirectIoUtil.dump(conf, definition, formatClass, destinationFile);
        } catch (IOException e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "error occurred while preparing Direct I/O output: {0}",
                    destinationFile), e);
        }
    }

    /**
     * Converts a pair of expected data set factory and verify rule factory into {@link VerifyRuleFactory}.
     * @param verifierFactory the original verifier factory
     * @param sourceFilter the filter for verifier input
     * @return the {@link VerifierFactory} which provides a verifier using the filtered input
     * @since 0.7.0
     */
    protected final VerifierFactory toVerifierFactory(
            VerifierFactory verifierFactory,
            UnaryOperator<DataModelSource> sourceFilter) {
        return new VerifierFactory() {
            @Override
            public <M> Verifier createVerifier(
                    DataModelDefinition<M> definition,
                    VerifyContext context) throws IOException {
                final Verifier delegate = verifierFactory.createVerifier(definition, context);
                return new Verifier() {
                    @Override
                    public List<Difference> verify(DataModelSource results) throws IOException {
                        DataModelSource filtered = sourceFilter.apply(results);
                        return delegate.verify(filtered);
                    }
                    @Override
                    public void close() throws IOException {
                        delegate.close();
                    }
                };
            }
            @Override
            public String toString() {
                return MessageFormat.format(
                        "Verifier(verifier={0}, filter={1})", //$NON-NLS-1$
                        verifierFactory,
                        sourceFilter);
            }
        };
    }

    /**
     * Returns URI for the existing resource.
     * @param path the path string of the target resource
     * @return the URI for the target resource
     * @throws URISyntaxException if the URI is not valid
     */
    public URI toUri(String path) throws URISyntaxException {
        if (path == null) {
            throw new IllegalArgumentException("path must not be null"); //$NON-NLS-1$
        }
        // FIXME for invalid characters
        URI uri = URI.create(path.replace('\\', '/'));
        if (uri.getScheme() != null) {
            return uri;
        }

        URL url = getCallerClass().getResource(uri.getPath());
        if (url == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    Messages.getString("DriverElementBase.errorMissingResource"), //$NON-NLS-1$
                    path,
                    getCallerClass().getName()));
        }
        URI resourceUri = url.toURI();
        if (uri.getFragment() == null) {
            return resourceUri;
        } else {
            URI resolvedUri = URI.create(resourceUri.toString() + '#' + uri.getFragment());
            return resolvedUri;
        }
    }

    /**
     * Returns a URI for the local resource.
     * @param path the local path
     * @return the resulting URI
     */
    protected URI toOutputUri(String path) {
        URI uri = URI.create(path.replace('\\', '/'));
        if (uri.getScheme() != null) {
            return uri;
        }
        return new File(path).toURI();
    }
}
