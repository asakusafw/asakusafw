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
package com.asakusafw.testdriver.tools.runner;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.testdriver.DriverElementBase;
import com.asakusafw.testdriver.TestDriverContext;
import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelSourceFactory;
import com.asakusafw.testdriver.core.DataModelSourceFilter;
import com.asakusafw.testdriver.core.Difference;
import com.asakusafw.testdriver.core.ModelTester;
import com.asakusafw.testdriver.core.ModelTransformer;
import com.asakusafw.testdriver.core.TestContext;
import com.asakusafw.testdriver.core.TestModerator;
import com.asakusafw.testdriver.core.TestRule;
import com.asakusafw.testdriver.core.TestToolRepository;
import com.asakusafw.testdriver.core.VerifierFactory;
import com.asakusafw.testdriver.core.VerifyContext;
import com.asakusafw.testdriver.core.VerifyRuleFactory;
import com.asakusafw.vocabulary.external.ExporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;

/**
 * Testing tools for Asakusa batch applications.
 * @see BatchTestRunner
 * @since 0.7.3
 */
public class BatchTestTool extends DriverElementBase implements TestContext {

    private static final Logger LOG = LoggerFactory.getLogger(BatchTestTool.class);

    private final ClassLoader classLoader;

    private final Map<String, String> batchArguments;

    private final Map<String, String> environmentVariables;

    private final Class<?> callerClass;

    private final TestToolRepository tools;

    /**
     * Creates a new instance.
     * @param callerClass the caller class (for detecting testing resources)
     */
    public BatchTestTool(Class<?> callerClass) {
        this.callerClass = callerClass;
        this.classLoader = callerClass.getClassLoader();
        this.batchArguments = new LinkedHashMap<String, String>();
        this.environmentVariables = new LinkedHashMap<String, String>(System.getenv());
        this.tools = new TestToolRepository(classLoader);
    }

    /**
     * Creates a new instance.
     * @param callerClass the caller class (for detecting testing resources)
     * @param context the current testing context
     */
    public BatchTestTool(Class<?> callerClass, TestContext context) {
        this(callerClass, context, new TestToolRepository(context.getClassLoader()));
    }

    /**
     * Creates a new instance.
     * @param context the current context
     */
    public BatchTestTool(TestDriverContext context) {
        this(context.getCallerClass(), context, context.getRepository());
    }

    /**
     * Creates a new instance.
     * @param callerClass the caller class (for detecting testing resources)
     * @param context the current testing context
     * @param tools the testing tools
     */
    protected BatchTestTool(Class<?> callerClass, TestContext context, TestToolRepository tools) {
        this.classLoader = context.getClassLoader();
        this.batchArguments = new LinkedHashMap<String, String>(context.getArguments());
        this.environmentVariables = new LinkedHashMap<String, String>(context.getEnvironmentVariables());
        this.callerClass = callerClass;
        this.tools = new TestToolRepository(classLoader);
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public Map<String, String> getArguments() {
        return batchArguments;
    }

    @Override
    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    @Override
    protected final Class<?> getCallerClass() {
        return callerClass;
    }

    @Override
    protected final TestToolRepository getTestTools() {
        return tools;
    }

    /**
     * Truncates jobflow input.
     * @param description the target importer description
     * @throws IOException if failed to truncate the input
     */
    public void truncate(ImporterDescription description) throws IOException {
        LOG.info(MessageFormat.format(
                "cleaning input: {0}",
                description.getClass().getName()));
        TestModerator moderator = new TestModerator(getTestTools(), this);
        moderator.truncate(description);
    }

    /**
     * Truncates jobflow output.
     * @param description the target exporter description
     * @throws IOException if failed to truncate the output
     */
    public void truncate(ExporterDescription description) throws IOException {
        LOG.info(MessageFormat.format(
                "cleaning output: {0}",
                description.getClass().getName()));
        TestModerator moderator = new TestModerator(getTestTools(), this);
        moderator.truncate(description);
    }

    /**
     * Prepares jobflow input.
     * @param description the target importer description
     * @param dataPath the data URI
     * @throws IOException if failed to prepare the output
     */
    public void prepare(ImporterDescription description, String dataPath) throws IOException {
        try {
            DataModelSourceFactory source = getTestTools().getDataModelSourceFactory(toUri(dataPath));
            prepare(description, source);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(MessageFormat.format(
                    "failed to prepare input: {0}",
                    description.getClass().getName()), e);
        }
    }

    /**
     * Prepares jobflow input.
     * @param description the importer description
     * @param source the data model source
     * @throws IOException if failed to prepare the input
     */
    public void prepare(ImporterDescription description, DataModelSourceFactory source) throws IOException {
        TestModerator moderator = new TestModerator(getTestTools(), this);
        moderator.prepare(description.getModelType(), description, source);
    }

    /**
     * Verifies jobflow output.
     * @param description the target exporter description
     * @param expectedPath the expected data URI
     * @param verifyRulePath the verification rule URI
     * @param extraRules extra verification rules
     * @return verify differences
     * @throws IOException if failed to verify the output
     */
    public List<Difference> verify(
            ExporterDescription description,
            String expectedPath,
            String verifyRulePath,
            ModelTester<?>... extraRules) throws IOException {
        return verify(description, expectedPath, verifyRulePath, null, extraRules);
    }

    /**
     * Verifies jobflow output.
     * @param description the target exporter description
     * @param expectedPath the expected data URI
     * @param verifyRulePath the verification rule URI
     * @param transformer the data model transformer
     * @param extraRules extra verification rules
     * @return verify differences
     * @throws IOException if failed to verify the output
     */
    public List<Difference> verify(
            ExporterDescription description,
            String expectedPath,
            String verifyRulePath,
            ModelTransformer<?> transformer,
            ModelTester<?>... extraRules) throws IOException {
        try {
            VerifierFactory verifier = toVerifierFactory(
                    description.getModelType(),
                    expectedPath, verifyRulePath,
                    transformer, extraRules);
            return verify(description, verifier);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(MessageFormat.format(
                    "failed to prepare verify output: {0}",
                    description.getClass().getName()), e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> VerifierFactory toVerifierFactory(
            Class<T> dataType,
            String expectedPath,
            String verifyRulePath,
            ModelTransformer<?> transformer,
            ModelTester<?>... extraRules) throws URISyntaxException, IOException {
        DataModelDefinition<T> definition = getTestTools().toDataModelDefinition(dataType);
        DataModelSourceFactory source = toDataModelSourceFactory(expectedPath);
        List<TestRule> fragments = new ArrayList<TestRule>();
        for (ModelTester<?> tester : extraRules) {
            TestRule rule = getTestTools().toVerifyRuleFragment(definition, (ModelTester<? super T>) tester);
            fragments.add(rule);
        }
        VerifyRuleFactory rules = getTestTools().getVerifyRuleFactory(toUri(verifyRulePath), fragments);
        VerifierFactory result = getTestTools().toVerifierFactory(source, rules);
        if (transformer != null) {
            DataModelSourceFilter filter =
                    toDataModelSourceFilter(definition, (ModelTransformer<? super T>) transformer);
            result = toVerifierFactory(result, filter);
        }
        return result;
    }

    /**
     * Verifies jobflow output.
     * @param description the exporter description
     * @param verifier the verifier
     * @return verify differences
     * @throws IOException if failed to verify the output
     */
    public List<Difference> verify(ExporterDescription description, VerifierFactory verifier) throws IOException {
        TestModerator moderator = new TestModerator(getTestTools(), this);
        return moderator.inspect(
                description.getModelType(),
                description,
                new VerifyContext(this),
                verifier);
    }
}
