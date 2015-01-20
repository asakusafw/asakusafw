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
package com.asakusafw.testdriver.core;

import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.List;

import com.asakusafw.vocabulary.external.ExporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;

/**
 * Load tools via SPI.
 * @since 0.2.3
 * @version 0.6.0
 */
public class TestToolRepository extends AbstractTestDataToolProvider {

    final DataModelAdapter dataModelAdapter;

    final ImporterPreparator<ImporterDescription> importerPreparator;

    final ExporterRetriever<ExporterDescription> exporterRetriever;

    final DataModelSourceProvider dataModelSourceProvider;

    final DataModelSinkProvider dataModelSinkProvider;

    final DifferenceSinkProvider differenceSinkProvider;

    final VerifyRuleProvider verifyRuleProvider;

    /**
     * Creates a new instance.
     * @param classLoader the service class loader
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public TestToolRepository(ClassLoader classLoader) {
        if (classLoader == null) {
            throw new IllegalArgumentException("classLoader must not be null"); //$NON-NLS-1$
        }
        this.dataModelAdapter = new SpiDataModelAdapter(classLoader);
        this.importerPreparator = new SpiImporterPreparator(classLoader);
        this.exporterRetriever = new SpiExporterRetriever(classLoader);
        this.dataModelSourceProvider = new SpiDataModelSourceProvider(classLoader);
        this.dataModelSinkProvider = new SpiDataModelSinkProvider(classLoader);
        this.differenceSinkProvider = new SpiDifferenceSinkProvider(classLoader);
        this.verifyRuleProvider = new SpiVerifyRuleProvider(classLoader);
    }

    @Override
    public <T> DataModelDefinition<T> toDataModelDefinition(Class<T> dataModelClass) throws IOException {
        if (dataModelClass == null) {
            throw new IllegalArgumentException("dataModelClass must not be null"); //$NON-NLS-1$
        }
        DataModelDefinition<T> def = dataModelAdapter.get(dataModelClass);
        if (def == null) {
            throw new IOException(MessageFormat.format(
                    "Failed to inspect a data model class (provider not found): {0}",
                    dataModelClass.getName()));
        }
        return def;
    }

    /**
     * Returns an {@link ImporterPreparator} for the description.
     * @param <T> description type
     * @param description target description
     * @return related preparator
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public <T extends ImporterDescription> ImporterPreparator<? super T> getImporterPreparator(T description) {
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        return importerPreparator;
    }

    /**
     * Returns an {@link ExporterRetriever} for the description.
     * @param <T> description type
     * @param description target description
     * @return related retriever
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public <T extends ExporterDescription> ExporterRetriever<? super T> getExporterRetriever(T description) {
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        return exporterRetriever;
    }

    @Override
    public DataModelSourceFactory getDataModelSourceFactory(final URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("uri must not be null"); //$NON-NLS-1$
        }
        return new DataModelSourceFactory() {
            @Override
            public <T> DataModelSource createSource(
                    DataModelDefinition<T> definition,
                    TestContext context) throws IOException {
                DataModelSource source = dataModelSourceProvider.open(definition, uri, context);
                if (source == null) {
                    throw new IOException(MessageFormat.format(
                            "Failed to open a data model sink (provider not found): {0}",
                            uri));
                }
                return source;
            }
            @Override
            public String toString() {
                return MessageFormat.format(
                        "DataModelSource({0})",
                        uri);
            }
        };
    }

    @Override
    public DataModelSinkFactory getDataModelSinkFactory(final URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("uri must not be null"); //$NON-NLS-1$
        }
        return new DataModelSinkFactory() {
            @Override
            public <T> DataModelSink createSink(
                    DataModelDefinition<T> definition,
                    TestContext context) throws IOException {
                DataModelSink sink = dataModelSinkProvider.create(definition, uri, context);
                if (sink == null) {
                    throw new IOException(MessageFormat.format(
                            "Failed to create a data model sink (provider not found): {0}",
                            uri));
                }
                return sink;
            }
            @Override
            public String toString() {
                return MessageFormat.format(
                        "DataModelSink({0})",
                        uri);
            }
        };
    }

    @Override
    public DifferenceSinkFactory getDifferenceSinkFactory(final URI uri) {
        if (uri == null) {
            throw new IllegalArgumentException("uri must not be null"); //$NON-NLS-1$
        }
        return new DifferenceSinkFactory() {
            @Override
            public <T> DifferenceSink createSink(
                    DataModelDefinition<T> definition,
                    TestContext context) throws IOException {
                DifferenceSink sink = differenceSinkProvider.create(definition, uri, context);
                if (sink == null) {
                    throw new IOException(MessageFormat.format(
                            "Failed to create a difference sink (provider not found): {0}",
                            uri));
                }
                return sink;
            }
            @Override
            public String toString() {
                return MessageFormat.format(
                        "DifferenceSink({0})",
                        uri);
            }
        };
    }

    @Override
    public VerifyRuleFactory getVerifyRuleFactory(final URI ruleUri, final List<? extends TestRule> extraRules) {
        assert ruleUri != null;
        assert extraRules != null;
        return new VerifyRuleFactory() {
            @Override
            public <T> VerifyRule createRule(
                    DataModelDefinition<T> definition,
                    VerifyContext context) throws IOException {
                VerifyRule verifyRule = verifyRuleProvider.get(definition, context, ruleUri);
                if (verifyRule == null) {
                    throw new IOException(MessageFormat.format(
                            "Failed to create a verify rule (provider not found): {0}",
                            ruleUri));
                }
                if (extraRules.isEmpty() == false) {
                    verifyRule = new CompositeVerifyRule(verifyRule, extraRules);
                }
                return verifyRule;
            }
            @Override
            public String toString() {
                return MessageFormat.format(
                        "VerifyRule({0})",
                        ruleUri.toString());
            }
        };
    }
}
