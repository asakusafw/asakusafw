/**
 * Copyright 2011 Asakusa Framework Team.
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
import java.util.ArrayList;
import java.util.List;

import com.asakusafw.vocabulary.external.ExporterDescription;

/**
 * Inspects each test result.
 * @since 0.2.0
 */
public class TestResultInspector {

    private final DataModelAdapter adapter;

    private final SourceProvider sources;

    private final VerifyRuleProvider rules;

    private final ExporterRetriever<ExporterDescription> targets;

    /**
     * Creates a new instance which uses registerd services.
     * @param serviceClassLoader class loader to load services
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public TestResultInspector(ClassLoader serviceClassLoader) {
        if (serviceClassLoader == null) {
            throw new IllegalArgumentException("serviceClassLoader must not be null"); //$NON-NLS-1$
        }
        this.adapter = new SpiDataModelAdapter(serviceClassLoader);
        this.sources = new SpiSourceProvider(serviceClassLoader);
        this.rules = new SpiVerifyRuleProvider(serviceClassLoader);
        this.targets = new SpiExporterRetriever(serviceClassLoader);
    }

    /**
     * Creates a new instance which uses the specified services.
     * @param adapter data model driver
     * @param sources test data provider
     * @param rules verification rule provider
     * @param retrievers test result retrievers
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public TestResultInspector(
            DataModelAdapter adapter,
            SourceProvider sources,
            VerifyRuleProvider rules,
            ExporterRetriever<ExporterDescription> retrievers) {
        if (adapter == null) {
            throw new IllegalArgumentException("adapter must not be null"); //$NON-NLS-1$
        }
        if (sources == null) {
            throw new IllegalArgumentException("sources must not be null"); //$NON-NLS-1$
        }
        if (rules == null) {
            throw new IllegalArgumentException("rules must not be null"); //$NON-NLS-1$
        }
        if (retrievers == null) {
            throw new IllegalArgumentException("retrievers must not be null"); //$NON-NLS-1$
        }
        this.adapter = adapter;
        this.sources = sources;
        this.rules = rules;
        this.targets = retrievers;
    }

    /**
     * Inspects the target exporter's output using specified expected data and rule.
     * @param description target exporter
     * @param expected the expected data
     * @param rule the verification rule between expected and actual result
     * @return detected invalid differences
     * @throws IOException if failed to inspect the result
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public List<Difference> inspect(ExporterDescription description, URI expected, URI rule) throws IOException {
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        if (expected == null) {
            throw new IllegalArgumentException("expected must not be null"); //$NON-NLS-1$
        }
        if (rule == null) {
            throw new IllegalArgumentException("rule must not be null"); //$NON-NLS-1$
        }
        DataModelDefinition<?> definition = adapter.get(description.getModelType());
        if (definition == null) {
            throw new IOException(MessageFormat.format(
                    "Failed to adapt {0}: (adaptor not found)",
                    description.getModelType().getName()));
        }
        List<Difference> results = inspect(definition, description, expected, rule);
        return results;
    }

    private <T> List<Difference> inspect(
            DataModelDefinition<T> definition,
            ExporterDescription description,
            URI expected,
            URI rule) throws IOException {
        assert definition != null;
        assert description != null;
        assert expected != null;
        assert rule != null;
        VerifyEngine verifier = buildVerifier(definition, expected, rule);
        DataModelSource target = targets.open(definition, description);
        try {
            List<Difference> results = new ArrayList<Difference>();
            results.addAll(verifier.inspectInput(target));
            results.addAll(verifier.inspectRest());
            return results;
        } finally {
            target.close();
        }
    }

    private VerifyEngine buildVerifier(
            DataModelDefinition<?> definition,
            URI expectedUri,
            URI ruleUri) throws IOException {
        assert definition != null;
        assert expectedUri != null;
        assert ruleUri != null;
        VerifyRule rule = rules.get(ruleUri);
        if (rule == null) {
            throw new IOException(MessageFormat.format(
                    "Failed to load a verify rule: {0}",
                    ruleUri));
        }
        VerifyEngine engine = new VerifyEngine(rule);
        DataModelSource expected = sources.open(definition, expectedUri);
        if (expected == null) {
            throw new IOException(MessageFormat.format(
                    "Failed to load an expected data set: {0}",
                    expectedUri));
        }
        engine.addExpected(expected);
        return engine;
    }
}
