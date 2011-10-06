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
import com.asakusafw.testdriver.rule.VerifyRuleBuilder;
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

    private final TestContext context;

    /**
     * Creates a new instance which uses registerd services.
     * @param serviceClassLoader class loader to load services
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public TestResultInspector(ClassLoader serviceClassLoader) {
        this(new TestContext.Empty(), serviceClassLoader);
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
        this(new TestContext.Empty(), adapter, sources, rules, retrievers);
    }

    /**
     * Creates a new instance which uses registerd services.
     * @param context the current context
     * @param serviceClassLoader class loader to load services
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.2.2
     */
    public TestResultInspector(TestContext context, ClassLoader serviceClassLoader) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        if (serviceClassLoader == null) {
            throw new IllegalArgumentException("serviceClassLoader must not be null"); //$NON-NLS-1$
        }
        this.context = context;
        this.adapter = new SpiDataModelAdapter(serviceClassLoader);
        this.sources = new SpiSourceProvider(serviceClassLoader);
        this.rules = new SpiVerifyRuleProvider(serviceClassLoader);
        this.targets = new SpiExporterRetriever(serviceClassLoader);
    }

    /**
     * Creates a new instance which uses the specified services.
     * @param context the current context
     * @param adapter data model driver
     * @param sources test data provider
     * @param rules verification rule provider
     * @param retrievers test result retrievers
     * @throws IllegalArgumentException if some parameters were {@code null}
     * @since 0.2.2
     */
    public TestResultInspector(
            TestContext context,
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
        this.context = context;
        this.adapter = adapter;
        this.sources = sources;
        this.rules = rules;
        this.targets = retrievers;
    }

    /**
     * Inspects the target exporter's output using specified expected data and rule.
     * @param modelClass class of data model
     * @param description target exporter
     * @param verifyContext current verification context
     * @param expected the expected data
     * @param rule the verification rule between expected and actual result
     * @return detected invalid differences
     * @throws IOException if failed to inspect the result
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public List<Difference> inspect(
            Class<?> modelClass,
            ExporterDescription description,
            VerifyContext verifyContext,
            URI expected,
            URI rule) throws IOException {
        return inspect(modelClass, description, verifyContext, expected, rule, null);
    }

    /**
     * Inspects the target exporter's output using specified expected data and rule.
     * @param modelClass class of data model
     * @param description target exporter
     * @param verifyContext current verification context
     * @param expected the expected data
     * @param rule the verification rule between expected and actual result
     * @param resultDataSink the actual result sink (nullable)
     * @return detected invalid differences
     * @throws IOException if failed to inspect the result
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public List<Difference> inspect(
            Class<?> modelClass,
            ExporterDescription description,
            VerifyContext verifyContext,
            URI expected,
            URI rule,
            DataModelSinkFactory resultDataSink) throws IOException {
        if (modelClass == null) {
            throw new IllegalArgumentException("modelClass must not be null"); //$NON-NLS-1$
        }
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        if (verifyContext == null) {
            throw new IllegalArgumentException("verifyContext must not be null"); //$NON-NLS-1$
        }
        if (expected == null) {
            throw new IllegalArgumentException("expected must not be null"); //$NON-NLS-1$
        }
        if (rule == null) {
            throw new IllegalArgumentException("rule must not be null"); //$NON-NLS-1$
        }
        DataModelDefinition<?> definition = findDefinition(modelClass);
        VerifyRule ruleDesc = findRule(definition, verifyContext, rule);
        return inspect(modelClass, description, expected, ruleDesc, resultDataSink);
    }

    /**
     * Creates a {@link VerifyRuleBuilder} for the target model class.
     * @param modelClass target model class
     * @return created {@link VerifyRuleBuilder}
     * @throws IOException if failed to create
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public VerifyRuleBuilder rule(Class<?> modelClass) throws IOException {
        if (modelClass == null) {
            throw new IllegalArgumentException("modelClass must not be null"); //$NON-NLS-1$
        }
        DataModelDefinition<?> definition = findDefinition(modelClass);
        return new VerifyRuleBuilder(definition);
    }

    /**
     * Converts {@link ModelVerifier} into {@link VerifyRule}.
     * @param <T> type of model
     * @param modelClass class of model
     * @param verifier target verifier
     * @return converted rule
     * @throws IOException if failed to convert
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public <T> VerifyRule rule(Class<? extends T> modelClass, ModelVerifier<T> verifier) throws IOException {
        if (modelClass == null) {
            throw new IllegalArgumentException("modelClass must not be null"); //$NON-NLS-1$
        }
        if (verifier == null) {
            throw new IllegalArgumentException("verifier must not be null"); //$NON-NLS-1$
        }
        DataModelDefinition<? extends T> definition = findDefinition(modelClass);
        return new ModelVerifierDriver<T>(verifier, definition);
    }

    /**
     * Inspects the target exporter's output using specified expected data and rule.
     * @param modelClass class of data model
     * @param description target exporter
     * @param expected the expected data
     * @param rule the verification rule between expected and actual result
     * @return detected invalid differences
     * @throws IOException if failed to inspect the result
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public List<Difference> inspect(
            Class<?> modelClass,
            ExporterDescription description,
            URI expected,
            VerifyRule rule) throws IOException {
        return inspect(modelClass, description, expected, rule, null);
    }

    /**
     * Inspects the target exporter's output using specified expected data and rule.
     * @param modelClass class of data model
     * @param description target exporter
     * @param expected the expected data
     * @param rule the verification rule between expected and actual result
     * @param resultDataSink the actual result sink (nullable)
     * @return detected invalid differences
     * @throws IOException if failed to inspect the result
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public List<Difference> inspect(
            Class<?> modelClass,
            ExporterDescription description,
            URI expected,
            VerifyRule rule,
            DataModelSinkFactory resultDataSink) throws IOException {
        if (modelClass == null) {
            throw new IllegalArgumentException("modelClass must not be null"); //$NON-NLS-1$
        }
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        if (expected == null) {
            throw new IllegalArgumentException("expected must not be null"); //$NON-NLS-1$
        }
        if (rule == null) {
            throw new IllegalArgumentException("rule must not be null"); //$NON-NLS-1$
        }
        DataModelDefinition<?> definition = findDefinition(modelClass);
        DataModelSource expectedDesc = findSource(definition, expected);
        VerifyEngine engine = buildVerifier(definition, rule, expectedDesc);
        List<Difference> results = inspect(definition, description, engine, resultDataSink);
        return results;
    }

    private <T> DataModelDefinition<T> findDefinition(Class<T> modelClass) throws IOException {
        assert modelClass != null;
        DataModelDefinition<T> definition = adapter.get(modelClass);
        if (definition == null) {
            throw new IOException(MessageFormat.format(
                    "Failed to adapt {0}: (adaptor not found)",
                    modelClass.getName()));
        }
        return definition;
    }

    private <T> List<Difference> inspect(
            DataModelDefinition<T> definition,
            ExporterDescription description,
            VerifyEngine engine,
            DataModelSinkFactory sinkOrNull) throws IOException {
        assert definition != null;
        assert description != null;
        assert engine != null;
        DataModelSource target = targets.createSource(definition, description, context);
        try {
            if (sinkOrNull != null) {
                target = new TeeDataModelSource(target, sinkOrNull.createSink(definition, context));
            }
            List<Difference> results = new ArrayList<Difference>();
            results.addAll(engine.inspectInput(target));
            results.addAll(engine.inspectRest());
            return results;
        } finally {
            target.close();
        }
    }

    private VerifyEngine buildVerifier(
            DataModelDefinition<?> definition,
            VerifyRule rule,
            DataModelSource expected) throws IOException {
        assert definition != null;
        assert rule != null;
        VerifyEngine engine = new VerifyEngine(rule);
        engine.addExpected(expected);
        return engine;
    }

    private DataModelSource findSource(DataModelDefinition<?> definition, URI uri) throws IOException {
        assert definition != null;
        assert uri != null;
        DataModelSource expected = sources.open(definition, uri, context);
        if (expected == null) {
            throw new IOException(MessageFormat.format(
                    "Failed to load an expected data set: {0}",
                    uri));
        }
        return expected;
    }

    private VerifyRule findRule(
            DataModelDefinition<?> definition,
            VerifyContext verifyContext,
            URI ruleUri) throws IOException {
        assert definition != null;
        assert verifyContext != null;
        assert ruleUri != null;
        VerifyRule rule = rules.get(definition, verifyContext, ruleUri);
        if (rule == null) {
            throw new IOException(MessageFormat.format(
                    "Failed to load a verify rule: {0}",
                    ruleUri));
        }
        return rule;
    }
}
