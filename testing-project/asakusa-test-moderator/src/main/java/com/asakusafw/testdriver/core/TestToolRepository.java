/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
 */
public class TestToolRepository {

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

    /**
     * Converts data model class into its definition.
     * @param <T> data model type
     * @param dataModelClass target class
     * @return the related definition
     * @throws IOException if failed to convert class
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
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
     * Converts model verifier into a corresponding rule.
     * @param <T> verification target type
     * @param dataModelClass target class
     * @param verifier target verifier object
     * @return the related rule
     * @throws IOException if failed to convert verifier
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public <T> VerifyRule toVerifyRule(Class<T> dataModelClass, ModelVerifier<? super T> verifier) throws IOException {
        if (dataModelClass == null) {
            throw new IllegalArgumentException("dataModelClass must not be null"); //$NON-NLS-1$
        }
        if (verifier == null) {
            throw new IllegalArgumentException("verifier must not be null"); //$NON-NLS-1$
        }
        DataModelDefinition<T> def = toDataModelDefinition(dataModelClass);
        return new ModelVerifierDriver<T>(verifier, def);
    }

    /**
     * Converts model tester into a correspoding rule.
     * @param <T> test target type
     * @param dataModelClass target class
     * @param tester target tester object
     * @return the related rule
     * @throws IOException if failed to convert tester
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public <T> TestRule toVerifyRuleFragment(
            Class<T> dataModelClass,
            ModelTester<? super T> tester) throws IOException {
        if (dataModelClass == null) {
            throw new IllegalArgumentException("dataModelClass must not be null"); //$NON-NLS-1$
        }
        if (tester == null) {
            throw new IllegalArgumentException("tester must not be null"); //$NON-NLS-1$
        }
        DataModelDefinition<T> def = toDataModelDefinition(dataModelClass);
        return new TesterDriver<T>(tester, def);
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

    /**
     * Returns a {@link DataModelSourceFactory} for the URI.
     * @param uri target URI
     * @return related factory
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
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

    /**
     * Returns a {@link DataModelSinkFactory} for the URI.
     * @param uri target URI
     * @return related factory
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
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

    /**
     * Returns a {@link DifferenceSinkFactory} for the URI.
     * @param uri target URI
     * @return related factory
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
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

    /**
     * Returns a {@link VerifierFactory} for the URIs.
     * @param expectedUri the URI which describes expected data set
     * @param ruleUri the URI which describes verification rule
     * @param extraRules extra rules
     * @return related factory
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public VerifierFactory getVerifierFactory(
            final URI expectedUri,
            final URI ruleUri,
            final List<TestRule> extraRules) {
        if (expectedUri == null) {
            throw new IllegalArgumentException("expectedUri must not be null"); //$NON-NLS-1$
        }
        if (ruleUri == null) {
            throw new IllegalArgumentException("ruleUri must not be null"); //$NON-NLS-1$
        }
        if (extraRules == null) {
            throw new IllegalArgumentException("extraRules must not be null"); //$NON-NLS-1$
        }
        final DataModelSourceFactory expectedFactory = getDataModelSourceFactory(expectedUri);
        return new VerifierFactory() {
            @Override
            public <T> Verifier createVerifier(
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
                DataModelSource expected = expectedFactory.createSource(definition, context.getTestContext());
                boolean succeed = false;
                try {
                    Verifier verifier = new VerifyRuleVerifier(expected, verifyRule);
                    succeed = true;
                    return verifier;
                } finally {
                    if (succeed == false) {
                        expected.close();
                    }
                }
            }
            @Override
            public String toString() {
                return MessageFormat.format(
                        "Verifier(expected={0}, rule={1})",
                        expectedUri,
                        ruleUri);
            }
        };
    }

    /**
     * Returns a {@link VerifierFactory} for the URIs.
     * @param expectedUri the URI which describes expected data set
     * @param verifyRule  verification rule
     * @return related factory
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public VerifierFactory getVerifierFactory(final URI expectedUri, final VerifyRule verifyRule) {
        if (expectedUri == null) {
            throw new IllegalArgumentException("expectedUri must not be null"); //$NON-NLS-1$
        }
        if (verifyRule == null) {
            throw new IllegalArgumentException("verifyRule must not be null"); //$NON-NLS-1$
        }
        final DataModelSourceFactory expectedFactory = getDataModelSourceFactory(expectedUri);
        return new VerifierFactory() {
            @Override
            public <T> Verifier createVerifier(
                    DataModelDefinition<T> definition,
                    VerifyContext context) throws IOException {
                DataModelSource expected = expectedFactory.createSource(definition, context.getTestContext());
                boolean succeed = false;
                try {
                    Verifier verifier = new VerifyRuleVerifier(expected, verifyRule);
                    succeed = true;
                    return verifier;
                } finally {
                    if (succeed == false) {
                        expected.close();
                    }
                }
            }
            @Override
            public String toString() {
                return MessageFormat.format(
                        "Verifier(expected={0}, rule={1})",
                        expectedUri,
                        verifyRule);
            }
        };
    }
}
