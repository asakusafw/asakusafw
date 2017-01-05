/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.compiler.util.tester;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Writable;
import org.junit.Assume;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.JavaName;
import com.asakusafw.compiler.flow.FlowCompilerOptions;
import com.asakusafw.compiler.flow.FlowDescriptionDriver;
import com.asakusafw.compiler.flow.JobFlowDriver;
import com.asakusafw.compiler.flow.Location;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Export;
import com.asakusafw.compiler.flow.jobflow.JobflowModel.Import;
import com.asakusafw.compiler.testing.BatchInfo;
import com.asakusafw.compiler.testing.DirectBatchCompiler;
import com.asakusafw.compiler.testing.DirectExporterDescription;
import com.asakusafw.compiler.testing.DirectFlowCompiler;
import com.asakusafw.compiler.testing.DirectImporterDescription;
import com.asakusafw.compiler.testing.JobflowInfo;
import com.asakusafw.compiler.testing.StageInfo;
import com.asakusafw.runtime.configuration.FrameworkDeployer;
import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.stage.AbstractCleanupStageClient;
import com.asakusafw.runtime.stage.StageConstants;
import com.asakusafw.runtime.util.VariableTable;
import com.asakusafw.runtime.util.VariableTable.RedefineStrategy;
import com.asakusafw.runtime.util.hadoop.ConfigurationProvider;
import com.asakusafw.vocabulary.batch.BatchDescription;
import com.asakusafw.vocabulary.external.ExporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription;
import com.asakusafw.vocabulary.external.ImporterDescription.DataSize;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.Out;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;

/**
 * Testing utilities for compiler.
 * @since 0.1.0
 * @version 0.6.1
 */
public class CompilerTester implements TestRule {

    static final Logger LOG = LoggerFactory.getLogger(CompilerTester.class);

    /**
     * Hadoop driver.
     */
    protected final HadoopDriver hadoopDriver;

    /**
     * Deploys framework.
     */
    protected final FrameworkDeployer frameworkDeployer;

    final FlowDescriptionDriver flow;

    Class<?> testClass;

    String testName;

    private final VariableTable variables;

    private final FlowCompilerOptions options;

    private final List<File> libraries;

    /**
     * Creates a new instance.
     */
    public CompilerTester() {
        this(true);
    }

    /**
     * Creates a new instance.
     * @param configurations the Hadoop configuration provider (nullable)
     * @since 0.6.1
     */
    public CompilerTester(ConfigurationProvider configurations) {
        this(configurations, true);
    }

    /**
     * Creates a new instance.
     * @param createFramework creates framework structure from src/.../dist.
     */
    public CompilerTester(boolean createFramework) {
        this(null, createFramework);
    }

    /**
     * Creates a new instance.
     * @param configurations the Hadoop configuration provider (nullable)
     * @param createFramework creates framework structure from src/.../dist.
     * @since 0.6.1
     */
    public CompilerTester(ConfigurationProvider configurations, boolean createFramework) {
        this.hadoopDriver = configurations == null
                ? HadoopDriver.createInstance()
                : HadoopDriver.createInstance(configurations);
        this.frameworkDeployer = new FrameworkDeployer(createFramework);
        this.flow = new FlowDescriptionDriver();
        this.testClass = getClass();
        this.testName = "unknown";
        this.variables = new VariableTable(RedefineStrategy.ERROR);
        this.options = new FlowCompilerOptions();
        this.libraries = new ArrayList<>();
    }

    @Override
    public Statement apply(Statement base, Description description) {
        Statement stmt = new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Assume.assumeNotNull(hadoopDriver);
                try {
                    testClass = description.getTestClass();
                    testName = MessageFormat.format(
                            "{0}_{1}",
                            description.getTestClass().getSimpleName(),
                            description.getMethodName().replaceAll("\\W", "_"));
                    hadoopDriver.setLogger(LoggerFactory.getLogger(testClass));
                    hadoopDriver.clean();
                    configure(description);
                    base.evaluate();
                } finally {
                    hadoopDriver.close();
                }
            }
        };
        return frameworkDeployer.apply(stmt, description);
    }

    /**
     * Configures this object.
     * @param description test description
     * @throws Exception if failed to configure
     */
    protected void configure(Description description) throws Exception {
        return;
    }

    /**
     * Returns the variable table for batch arguments.
     * Clients can modify returned object.
     * @return the variables
     */
    public VariableTable variables() {
        return variables;
    }

    /**
     * Returns the compiler options.
     * Clients can modify returned object.
     * @return compiler options
     */
    public FlowCompilerOptions options() {
        return options;
    }

    /**
     * Returns hadoop configuration.
     * Clients can modify returned object.
     * @return hadoop configuration
     */
    public Configuration configuration() {
        return hadoopDriver.getConfiguration();
    }

    /**
     * Returns runtime libraries.
     * Clients can modify returned list.
     * @return framework classes
     */
    public List<File> libraries() {
        return libraries;
    }

    /**
     * Returns current framework deployer.
     * @return framework deployer.
     */
    public FrameworkDeployer framework() {
        return frameworkDeployer;
    }

    /**
     * Compiles the target flow description and executes it.
     * @param description the target flow description
     * @return {@code true} if compile and execution were successfully finished
     * @throws IOException if compile or execution were failed
     */
    public boolean runFlow(FlowDescription description) throws IOException {
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        return run(compileFlow(description));
    }

    /**
     * Analyzes the target flow description.
     * @param description the target flow description
     * @return the analyzed flow graph
     */
    public FlowGraph analyzeFlow(FlowDescription description) {
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        return flow.createFlowGraph(description);
    }

    /**
     * Compiles the target flow description.
     * @param description the target flow description
     * @return the compilation results
     * @throws IOException if compile was failed
     */
    public JobflowInfo compileFlow(FlowDescription description) throws IOException {
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        FlowGraph graph = flow.createFlowGraph(description);
        List<File> classPath = buildClassPath(description.getClass());
        return DirectFlowCompiler.compile(
                graph,
                "testing",
                description.getClass().getName(),
                "com.example",
                hadoopDriver.toPath(path("runtime", "stages")),
                new File("target/localwork", testName),
                classPath,
                getClass().getClassLoader(),
                options);
    }

    /**
     * Compiles the target jobflow class.
     * @param description the target flow description
     * @return the compilation results
     * @throws IOException if compile was failed
     */
    public JobflowInfo compileJobflow(Class<? extends FlowDescription> description) throws IOException {
        JobFlowDriver driver = JobFlowDriver.analyze(description);
        assertThat(driver.getDiagnostics().toString(), driver.hasError(), is(false));
        List<File> classPath = buildClassPath(description);
        JobflowInfo info = DirectFlowCompiler.compile(
                driver.getJobFlowClass().getGraph(),
                "testing",
                driver.getJobFlowClass().getConfig().name(),
                "com.example",
                hadoopDriver.toPath(path("runtime", "jobflow")),
                new File("target/localwork", testName),
                classPath,
                getClass().getClassLoader(),
                options);
        return info;
    }

    /**
     * Compiles the target jobflow class and executes it.
     * @param description the target flow description
     * @return {@code true} if successfully executed, otherwise {@code false}
     * @throws IOException if compile or execution were failed
     */
    public boolean runJobflow(Class<? extends FlowDescription> description) throws IOException {
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        JobflowInfo info = compileJobflow(description);
        return run(info);
    }

    /**
     * Compiles the target batch class.
     * @param description the target batch description
     * @return {@code true} if successfully executed, otherwise {@code false}
     * @throws IOException if compile was failed
     */
    public BatchInfo compileBatch(Class<? extends BatchDescription> description) throws IOException {
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        List<File> classPath = buildClassPath(description);
        BatchInfo info = DirectBatchCompiler.compile(
                description,
                "com.example",
                hadoopDriver.toPath(path("runtime", "batch")),
                new File("target/CompilerTester/" + testName + "/output"),
                new File("target/CompilerTester/" + testName + "/build"),
                classPath,
                getClass().getClassLoader(),
                options);
        return info;
    }

    /**
     * Compiles the target batch class and executes it.
     * @param description the target batch description
     * @return {@code true} if successfully executed, otherwise {@code false}
     * @throws IOException if compile or execution were failed
     */
    public boolean runBatch(Class<? extends BatchDescription> description) throws IOException {
        if (description == null) {
            throw new IllegalArgumentException("description must not be null"); //$NON-NLS-1$
        }
        return run(compileBatch(description));
    }

    /**
     * Executes the target batch.
     * @param info the target batch information
     * @return {@code true} if successfully executed, otherwise {@code false}
     * @throws IOException if failed to execute
     */
    public boolean run(BatchInfo info) throws IOException {
        if (info == null) {
            throw new IllegalArgumentException("info must not be null"); //$NON-NLS-1$
        }
        for (JobflowInfo jobflow : info.getJobflows()) {
            boolean succeed = run(jobflow);
            if (succeed == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * Runs the specified jobflow.
     * @param info target jobflow information
     * @return {@code true} iff execution was succeeded
     * @throws IOException if failed to execute job
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public boolean run(JobflowInfo info) throws IOException {
        return run(info, true, true);
    }

    /**
     * Runs the specified jobflow except cleanup.
     * @param info target jobflow information
     * @return {@code true} iff execution was succeeded
     * @throws IOException if failed to execute job
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public boolean runStages(JobflowInfo info) throws IOException {
        return run(info, true, false);
    }

    private boolean run(JobflowInfo info, boolean stages, boolean cleanup) throws IOException {
        if (info == null) {
            throw new IllegalArgumentException("info must not be null"); //$NON-NLS-1$
        }
        File confFile = frameworkDeployer.getCoreConfigurationFile();
        if (confFile != null && confFile.exists() == false) {
            LOG.warn("execute hadoop with no configuration file (missing: {})", confFile.getAbsolutePath());
        }

        Map<String, String> definitions = new HashMap<>();
        definitions.put(StageConstants.PROP_USER, System.getProperty("user.name"));
        definitions.put(StageConstants.PROP_EXECUTION_ID, UUID.randomUUID().toString());
        definitions.put(StageConstants.PROP_ASAKUSA_BATCH_ARGS, variables.toSerialString());

        List<File> libjars = new ArrayList<>();
        libjars.add(info.getPackageFile());
        libjars.add(frameworkDeployer.getCoreRuntimeLibrary());
        libjars.addAll(frameworkDeployer.getRuntimeLibraries());
        libjars.addAll(libraries);

        if (stages) {
            if (executeStage(info, confFile, definitions, libjars) == false) {
                return false;
            }
        }
        if (cleanup) {
            if (executeCleanup(info, confFile, definitions, libjars) == false) {
                return false;
            }
        }
        return true;
    }

    private boolean executeStage(
            JobflowInfo info,
            File confFile,
            Map<String, String> definitions,
            List<File> libjars) throws IOException {
        assert info != null;
        assert definitions != null;
        assert libjars != null;
        for (StageInfo stage : info.getStages()) {
            boolean succeed = hadoopDriver.runJob(
                    frameworkDeployer.getCoreRuntimeLibrary(),
                    libjars,
                    stage.getClassName(),
                    confFile,
                    definitions);
            if (succeed == false) {
                return false;
            }
        }
        return true;
    }

    private boolean executeCleanup(
            JobflowInfo info,
            File confFile,
            Map<String, String> definitions,
            List<File> libjars) throws IOException {
        assert info != null;
        assert definitions != null;
        assert libjars != null;
        if (info.getStages().isEmpty() == false) {
            boolean succeed = hadoopDriver.runJob(
                    frameworkDeployer.getCoreRuntimeLibrary(),
                    libjars,
                    AbstractCleanupStageClient.IMPLEMENTATION,
                    confFile,
                    definitions);
            if (succeed == false) {
                return false;
            }
        }
        return true;
    }

    private List<File> buildClassPath(Class<?>... libraryClasses) {
        List<File> classPath = new ArrayList<>();
        classPath.add(findClassPathFromClass(testClass));
        for (Class<?> libraryClass : libraryClasses) {
            classPath.add(findClassPathFromClass(libraryClass));
        }
        return classPath;
    }

    private File findClassPathFromClass(Class<?> aClass) {
        assert aClass != null;
        File path = DirectFlowCompiler.toLibraryPath(aClass);
        assertThat(aClass.getName(), path, not(nullValue()));
        return path;
    }

    /**
     * Returns a utility object for handling flow input.
     * @param <T> the input data type
     * @param type the input data type
     * @param name the input name
     * @return the created object
     * @throws IOException if the operation was failed by I/O error
     */
    public <T extends Writable> TestInput<T> input(
            Class<T> type,
            String name) throws IOException {
        return input(type, name, DataSize.UNKNOWN);
    }

    /**
     * Returns a utility object for handling flow input.
     * @param <T> the input data type
     * @param type the input data type
     * @param name the input name
     * @param dataSize the data size hint
     * @return the created object
     * @throws IOException if the operation was failed by I/O error
     */
    public <T extends Writable> TestInput<T> input(
            Class<T> type,
            String name,
            DataSize dataSize) throws IOException {
        Location path = hadoopDriver.toPath(path("input", JavaName.of(name).toMemberName()));
        return new TestInput<>(type, name, path, dataSize);
    }

    /**
     * Returns a utility object for handling flow input.
     * @param <T> the input data type
     * @param importer the importer description
     * @param name the input name
     * @return the created object
     */
    public <T> In<T> input(
            String name,
            ImporterDescription importer) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (importer == null) {
            throw new IllegalArgumentException("importer must not be null"); //$NON-NLS-1$
        }
        return flow.createIn(name, importer);
    }

    /**
     * Returns a utility object for handling flow input.
     * @param <T> the input data type
     * @param exporter the exporter description
     * @param name the input name
     * @return the created object
     */
    public <T> Out<T> output(
            String name,
            ExporterDescription exporter) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        if (exporter == null) {
            throw new IllegalArgumentException("exporter must not be null"); //$NON-NLS-1$
        }
        return flow.createOut(name, exporter);
    }

    /**
     * Returns a utility object for handling flow output.
     * @param <T> the output data type
     * @param type the output data type
     * @param name the output name
     * @return the created object
     * @throws IOException if error occurred while output
     */
    public <T extends Writable> TestOutput<T> output(
            Class<T> type,
            String name) throws IOException {
        Location path = hadoopDriver.toPath(testName, "output", name).asPrefix();
        return new TestOutput<>(type, name, path);
    }

    /**
     * Creates a data model output for writing data into the target location.
     * @param <T> the data model type
     * @param type the data model type
     * @param location the target location
     * @return the data model output
     * @throws IOException if error occurred while initializing output
     */
    public <T extends Writable> ModelOutput<T> openOutput(
            Class<T> type,
            Location location) throws IOException {
        return hadoopDriver.openOutput(type, location);
    }

    /**
     * Creates a data model output for writing data into the target importer source.
     * @param <T> the data model type
     * @param type the data model type
     * @param importer the target importer
     * @return the data model output
     * @throws IOException if error occurred while initializing output
     */
    public <T extends Writable> ModelOutput<T> openOutput(
            Class<T> type,
            Import importer) throws IOException {
        Iterator<Location> iter = importer.getInputInfo().getLocations().iterator();
        assert iter.hasNext();
        Location location = iter.next();
        return hadoopDriver.openOutput(type, location);
    }

    /**
     * Creates a data model input for reading data on the target location.
     * @param <T> the data model type
     * @param type the data model type
     * @param location the target location
     * @return the data model input
     * @throws IOException if the operation was failed by I/O error
     */
    public <T extends Writable> ModelInput<T> openInput(
            Class<T> type,
            Location location) throws IOException {
        return hadoopDriver.openInput(type, location);
    }

    /**
     * Returns the importer description in the target batch.
     * @param info the target batch
     * @param name the output name
     * @return the importer description
     */
    public Import getImporter(BatchInfo info, String name) {
        for (JobflowInfo jf : info.getJobflows()) {
            for (Import in : jf.getJobflow().getImports()) {
                if (in.getDescription().getName().equals(name)) {
                    return in;
                }
            }
        }
        throw new AssertionError(name);
    }

    /**
     * Returns the exporter description in the target batch.
     * @param info the target batch
     * @param name the output name
     * @return the exporter description
     */
    public Export getExporter(BatchInfo info, String name) {
        for (JobflowInfo jf : info.getJobflows()) {
            for (Export out : jf.getJobflow().getExports()) {
                if (out.getDescription().getName().equals(name)) {
                    return out;
                }
            }
        }
        throw new AssertionError(name);
    }

    /**
     * Returns the importer description in the target jobflow.
     * @param info the compiled jobflow information
     * @param name the output name
     * @return the importer description
     */
    public Import getImporter(JobflowInfo info, String name) {
        for (Import in : info.getJobflow().getImports()) {
            if (in.getDescription().getName().equals(name)) {
                return in;
            }
        }
        throw new AssertionError(name);
    }

    /**
     * Returns the exporter description in the target jobflow.
     * @param info the compiled jobflow information
     * @param name the output name
     * @return the exporter description
     */
    public Export getExporter(JobflowInfo info, String name) {
        for (Export out : info.getJobflow().getExports()) {
            if (out.getDescription().getName().equals(name)) {
                return out;
            }
        }
        throw new AssertionError(name);
    }

    /**
     * Collects data model objects from the target file.
     * @param <T> the data model type
     * @param type the data model type
     * @param location the target location
     * @return the data model input
     * @throws IOException if the operation was failed by I/O error
     */
    public <T extends Writable> List<T> getList(
            Class<T> type,
            Location location) throws IOException {
        try (ModelInput<T> input = hadoopDriver.openInput(type, location)) {
            List<T> results = new ArrayList<>();
            while (true) {
                T target = type.newInstance();
                if (input.readTo(target) == false) {
                    break;
                }
                results.add(target);
            }
            return results;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * Collects data model objects from the target file.
     * @param <T> the data model type
     * @param type the data model type
     * @param location the target location
     * @param comparator the data model comparator
     * @return the data model input
     * @throws IOException if the operation was failed by I/O error
     */
    public <T extends Writable> List<T> getList(
            Class<T> type,
            Location location,
            Comparator<? super T> comparator) throws IOException {
        List<T> list = getList(type, location);
        Collections.sort(list, comparator);
        return list;
    }

    private String path(String prefix, String name) {
        if (testName == null) {
            return prefix + "/" + name;
        } else {
            return testName + "/" + prefix + "/" + name;
        }
    }

    /**
     * A testing utility for flow inputs.
     * @param <T> the input data type
     */
    public class TestInput<T extends Writable> implements Closeable {

        private final Class<T> type;

        private final ModelOutput<T> output;

        private final String name;

        private final Location path;

        private final DataSize dataSize;

        TestInput(Class<T> type, String name, Location path, DataSize dataSize) throws IOException {
            assert type != null;
            assert name != null;
            assert path != null;
            this.type = type;
            this.name = name;
            this.path = path;
            this.output = hadoopDriver.openOutput(type, path);
            this.dataSize = dataSize;
        }

        /**
         * Adds an input data.
         * @param model the data model object
         * @throws IOException if the operation was failed by I/O error
         */
        public void add(T model) throws IOException {
            output.write(model);
        }

        /**
         * Deploys previously added input data and returns a flow in object to read them.
         * @return the flow in object
         * @throws IOException if the operation was failed by I/O error
         */
        public In<T> flow() throws IOException {
            close();
            DirectImporterDescription description = new DirectImporterDescription(type, path.toPath('/'));
            description.setDataSize(dataSize);
            return flow.createIn(name, description);
        }

        @Override
        public void close() throws IOException {
            output.close();
        }
    }

    /**
     * A testing utility for flow outputs.
     * @param <T> the output data type
     */
    public class TestOutput<T extends Writable> {

        private final Class<T> type;

        private final String name;

        private final Location pathPrefix;

        TestOutput(Class<T> type, String name, Location pathPrefix) {
            assert type != null;
            assert name != null;
            assert pathPrefix != null;
            this.type = type;
            this.name = name;
            this.pathPrefix = pathPrefix;
        }

        /**
         * Creates a new flow out.
         * @return the flow out
         * @throws IOException if the operation was failed by I/O error
         */
        public Out<T> flow() throws IOException {
            return flow.createOut(name, new DirectExporterDescription(
                    type,
                    pathPrefix.getParent().append(pathPrefix.getName()).asPrefix().toPath('/')));
        }

        /**
         * Returns the output data list.
         * @return the output data list
         * @throws IOException if the operation was failed by I/O error
         */
        public List<T> toList() throws IOException {
            try (ModelInput<T> input = hadoopDriver.openInput(type, pathPrefix)) {
                List<T> results = new ArrayList<>();
                while (true) {
                    T target = type.newInstance();
                    if (input.readTo(target) == false) {
                        break;
                    }
                    results.add(target);
                }
                return results;
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                throw new IOException(e);
            }
        }

        /**
         * Returns the output data list.
         * @param cmp the comparator
         * @return the output data list
         * @throws IOException if the operation was failed by I/O error
         */
        public List<T> toList(Comparator<? super T> cmp) throws IOException {
            List<T> results = toList();
            Collections.sort(results, cmp);
            return results;
        }
    }
}
