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
package com.asakusafw.testdriver;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.DataModelSource;
import com.asakusafw.testdriver.core.DataModelSourceFactory;
import com.asakusafw.testdriver.core.IteratorDataModelSource;
import com.asakusafw.testdriver.core.SourceDataModelSource;
import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.testdriver.core.TestDataToolProvider;
import com.asakusafw.utils.io.Provider;
import com.asakusafw.utils.io.Source;

/**
 * テストドライバのテスト入力データの親クラス。
 * @since 0.2.0
 * @version 0.6.0
 * @param <T> モデルクラス
 */
public abstract class DriverInputBase<T> {

    private static final Logger LOG = LoggerFactory.getLogger(DriverInputBase.class);

    private final Class<?> callerClass;

    private final TestDataToolProvider testTools;

    private final String name;

    private final Class<T> modelType;

    private DataModelSourceFactory source;

    /**
     * Creates a new instance.
     * @param callerClass the current context class
     * @param testTools the test data tools
     * @param name the original input name
     * @param modelType the data model type
     * @since 0.6.0
     */
    protected DriverInputBase(Class<?> callerClass, TestDataToolProvider testTools, String name, Class<T> modelType) {
        if (callerClass == null) {
            throw new IllegalArgumentException("callerClass must not be null"); //$NON-NLS-1$
        }
        if (testTools == null) {
            throw new IllegalArgumentException("testTools must not be null"); //$NON-NLS-1$
        }
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (modelType == null) {
            throw new IllegalArgumentException("modelType must not be null"); //$NON-NLS-1$
        }
        this.callerClass = callerClass;
        this.testTools = testTools;
        this.name = name;
        this.modelType = modelType;
    }

    /**
     * Returns the caller class.
     * @return the caller class
     */
    protected final Class<?> getCallerClass() {
        return callerClass;
    }

    /**
     * Returns the test tools.
     * @return the test tools
     */
    protected final TestDataToolProvider getTestTools() {
        return testTools;
    }

    /**
     * Returns the name of this port.
     * @return the name
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns the data type of this port.
     * @return the data type
     */
    public final Class<T> getModelType() {
        return modelType;
    }

    /**
     * Returns the source for jobflow input.
     * @return the source, or {@code null} if not defined
     * @since 0.2.3
     */
    public DataModelSourceFactory getSource() {
        return source;
    }

    /**
     * Sets the source for jobflow input.
     * @param source the source, or {@code null} to reset it
     * @since 0.6.0
     */
    protected final void setSource(DataModelSourceFactory source) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Prepare: name={}, model={}, source={}", new Object[] { //$NON-NLS-1$
                    getName(),
                    getModelType().getName(),
                    source,
            });
        }
        this.source = source;
    }

    /**
     * Converts a source path into {@link DataModelSourceFactory} which provides data models.
     * This implementation lazily converts source contents into equivalent {@link DataModelReflection}s.
     * @param sourcePath the source path
     * @return the {@link DataModelSourceFactory}
     * @throws IllegalArgumentException if the target resource is not found
     * @since 0.6.0
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
                    "Invalid URI: {0}",
                    sourcePath), e);
        }
        return getTestTools().getDataModelSourceFactory(sourceUri);
    }

    /**
     * Converts an data model object collection into {@link DataModelSourceFactory} which provides data models.
     * This implementation immediately converts data model objects into equivalent {@link DataModelReflection}s.
     * @param sourceObjects the original data model objects
     * @return the {@link DataModelSourceFactory}
     * @since 0.6.0
     */
    protected final DataModelSourceFactory toDataModelSourceFactory(Iterable<? extends T> sourceObjects) {
        if (sourceObjects == null) {
            throw new IllegalArgumentException("sourceObjects must not be null"); //$NON-NLS-1$
        }
        final DataModelDefinition<T> definition = getDataModelDefinition();
        final ArrayList<DataModelReflection> results = new ArrayList<DataModelReflection>();
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
     * @param sourceProvider the original data model objects
     * @return the {@link DataModelSourceFactory}
     * @since 0.6.0
     */
    protected final DataModelSourceFactory toDataModelSourceFactory(
            final Provider<? extends Source<? extends T>> sourceProvider) {
        return new DataModelSourceFactory() {
            @SuppressWarnings({ "rawtypes", "unchecked" })
            @Override
            public DataModelSource createSource(
                    DataModelDefinition definition,
                    TestContext context) throws IOException {
                try {
                    return new SourceDataModelSource<T>(definition, sourceProvider.open());
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
     * Returns the data model definition for this port.
     * @return the data model definition
     */
    public final DataModelDefinition<T> getDataModelDefinition() {
        try {
            TestDataToolProvider tools = getTestTools();
            return tools.toDataModelDefinition(modelType);
        } catch (IOException e) {
            throw new IllegalStateException(MessageFormat.format(
                    "Invalid data model type in \"{0}\": {1}",
                    name,
                    modelType.getName()), e);
        }
    }

    /**
     * パス文字列からURIを生成する。
     *
     * @param path パス文字列
     * @return ワーキングのリソース位置
     * @throws URISyntaxException 引数の値がURIとして不正な値であった場合
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
                    "指定されたリソースが見つかりません: {0} (検索クラス: {1})",
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
}