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
package com.asakusafw.compiler.flow;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.apache.hadoop.io.Writable;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.common.Naming;
import com.asakusafw.compiler.flow.plan.StageBlock;
import com.asakusafw.compiler.flow.plan.StageGraph;
import com.asakusafw.compiler.flow.plan.StagePlanner;
import com.asakusafw.compiler.flow.stage.CompiledShuffle;
import com.asakusafw.compiler.flow.stage.CompiledType;
import com.asakusafw.compiler.flow.stage.MapFragmentEmitter;
import com.asakusafw.compiler.flow.stage.ReduceFragmentEmitter;
import com.asakusafw.compiler.flow.stage.ShuffleAnalyzer;
import com.asakusafw.compiler.flow.stage.ShuffleGroupingComparatorEmitter;
import com.asakusafw.compiler.flow.stage.ShuffleKeyEmitter;
import com.asakusafw.compiler.flow.stage.ShuffleModel;
import com.asakusafw.compiler.flow.stage.ShuffleModel.Segment;
import com.asakusafw.compiler.flow.stage.ShufflePartitionerEmitter;
import com.asakusafw.compiler.flow.stage.ShuffleSortComparatorEmitter;
import com.asakusafw.compiler.flow.stage.ShuffleValueEmitter;
import com.asakusafw.compiler.flow.stage.StageAnalyzer;
import com.asakusafw.compiler.flow.stage.StageCompiler;
import com.asakusafw.compiler.flow.stage.StageModel;
import com.asakusafw.compiler.flow.stage.StageModel.Fragment;
import com.asakusafw.compiler.flow.stage.StageModel.MapUnit;
import com.asakusafw.compiler.flow.stage.StageModel.ReduceUnit;
import com.asakusafw.compiler.repository.SpiDataClassRepository;
import com.asakusafw.compiler.repository.SpiExternalIoDescriptionProcessorRepository;
import com.asakusafw.compiler.repository.SpiFlowElementProcessorRepository;
import com.asakusafw.compiler.repository.SpiFlowGraphRewriterRepository;
import com.asakusafw.runtime.core.Result;
import com.asakusafw.runtime.flow.Rendezvous;
import com.asakusafw.runtime.flow.SegmentedWritable;
import com.asakusafw.utils.java.jsr199.testing.VolatileCompiler;
import com.asakusafw.utils.java.jsr199.testing.VolatileJavaFile;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.graph.FlowGraph;

/**
 * A test root for compiling jobflow compilers.
 */
public class JobflowCompilerTestRoot {

    static final Logger LOG = LoggerFactory.getLogger(JobflowCompilerTestRoot.class);

    private final VolatileCompiler javaCompiler = new VolatileCompiler();

    private VolatilePackager packager = new VolatilePackager();

    /**
     * The environment.
     */
    protected FlowCompilingEnvironment environment;

    /**
     * Initializes the test.
     * @throws Exception if some errors were occurred
     */
    @Before
    public void setUp() throws Exception {
        packager = new VolatilePackager();
        FlowCompilerConfiguration config = new FlowCompilerConfiguration();
        config.setBatchId("batch");
        config.setFlowId("flow");
        config.setFactory(Models.getModelFactory());
        config.setProcessors(new SpiFlowElementProcessorRepository());
        config.setExternals(new SpiExternalIoDescriptionProcessorRepository());
        config.setDataClasses(new SpiDataClassRepository());
        config.setGraphRewriters(new SpiFlowGraphRewriterRepository());
        config.setPackager(packager);
        config.setRootPackageName("com.example");
        config.setRootLocation(Location.fromPath("com/example", '/'));
        config.setServiceClassLoader(getClass().getClassLoader());
        config.setOptions(new FlowCompilerOptions());
        config.setBuildId("testing");
        environment = new FlowCompilingEnvironment(config);
        environment.bless();
    }

    /**
     * Cleans up the test.
     * @throws Exception if some errors were occurred
     */
    @After
    public void tearDown() throws Exception {
        javaCompiler.close();
    }

    /**
     * Compiles a jobflow class and returns the stage models.
     * @param aClass the jobflow class
     * @return the compiled results
     */
    protected List<StageModel> compile(Class<? extends FlowDescription> aClass) {
        assert aClass != null;
        StageGraph graph = jfToStageGraph(aClass);
        return compileStages(graph);
    }

    /**
     * Analyzes a jobflow class and returns the stage graph.
     * @param aClass the jobflow class
     * @return the stage graph
     */
    protected StageGraph jfToStageGraph(Class<? extends FlowDescription> aClass) {
        assert aClass != null;
        JobFlowDriver analyzed = JobFlowDriver.analyze(aClass);
        assertThat(analyzed.getDiagnostics().toString(), analyzed.hasError(), is(false));
        JobFlowClass flow = analyzed.getJobFlowClass();
        FlowGraph flowGraph = flow.getGraph();
        return flowToStageGraph(flowGraph);
    }

    private StageGraph flowToStageGraph(FlowGraph flowGraph) {
        assert flowGraph != null;
        StagePlanner planner = new StagePlanner(
                environment.getGraphRewriters().getRewriters(),
                environment.getOptions());
        StageGraph planned = planner.plan(flowGraph);
        assertThat(planner.getDiagnostics().toString(),
                planner.getDiagnostics().isEmpty(),
                is(true));
        return planned;
    }

    /**
     * Compiles a stage graph and returns the stage models.
     * @param graph the target stage graph
     * @return the compile results
     */
    protected List<StageModel> compileStages(StageGraph graph) {
        try {
            return new StageCompiler(environment).compile(graph);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Compiles a stage block and returns a stage model.
     * @param block the stage block
     * @return the stage model
     * @throws IOException if failed to output
     */
    protected StageModel compileFragments(StageBlock block) throws IOException {
        ShuffleModel shuffle = compileShuffle(block);
        StageModel stage = new StageAnalyzer(environment).analyze(block, shuffle);
        for (MapUnit unit : stage.getMapUnits()) {
            for (Fragment fragment : unit.getFragments()) {
                compile(fragment, stage);
            }
        }
        for (ReduceUnit unit : stage.getReduceUnits()) {
            for (Fragment fragment : unit.getFragments()) {
                compile(fragment, stage);
            }
        }
        return stage;
    }

    private void compile(Fragment fragment, StageModel stage) throws IOException {
        if (fragment.isRendezvous()) {
            CompiledType compiled = new ReduceFragmentEmitter(environment).emit(
                    fragment,
                    stage.getShuffleModel(),
                    stage.getStageBlock());
            fragment.setCompiled(compiled);
        } else {
            CompiledType compiled = new MapFragmentEmitter(environment).emit(
                    fragment,
                    stage.getStageBlock());
            fragment.setCompiled(compiled);
        }
    }

    /**
     * Compiles a stage block and returns its shuffle operation.
     * @param block the stage block
     * @return the shuffle model, or {@code null} if the target has no shuffle operations
     * @throws IOException if failed to output
     */
    protected ShuffleModel compileShuffle(StageBlock block) throws IOException {
        ShuffleModel shuffle = new ShuffleAnalyzer(environment).analyze(block);
        assertThat(environment.hasError(), is(false));
        if (shuffle == null) {
            return null;
        }
        Name keyTypeName = new ShuffleKeyEmitter(environment).emit(shuffle);
        Name valueTypeName = new ShuffleValueEmitter(environment).emit(shuffle);
        Name groupComparatorTypeName = new ShuffleGroupingComparatorEmitter(environment).emit(shuffle, keyTypeName);
        Name sortComparatorTypeName = new ShuffleSortComparatorEmitter(environment).emit(shuffle, keyTypeName);
        Name partitionerTypeName = new ShufflePartitionerEmitter(environment).emit(shuffle, keyTypeName, valueTypeName);
        CompiledShuffle compiled = new CompiledShuffle(
                keyTypeName,
                valueTypeName,
                groupComparatorTypeName,
                sortComparatorTypeName,
                partitionerTypeName);
        shuffle.setCompiled(compiled);
        return shuffle;
    }

    /**
     * Loads a class and returns its object.
     * @param loader the class loader
     * @param name the class name
     * @param arguments the arguments
     * @return the created instance
     */
    protected Object create(
            ClassLoader loader,
            Name name,
            Object...arguments) {
        try {
            Class<?> loaded = loader.loadClass(name.toNameString());
            for (Constructor<?> ctor : loaded.getConstructors()) {
                if (ctor.getParameterTypes().length == arguments.length) {
                    return ctor.newInstance(arguments);
                }
            }
            throw new AssertionError();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Loads a class and returns its object.
     * @param <T> the data type
     * @param loader the class loader
     * @param name the class name
     * @param arguments the arguments
     * @return the created instance
     */
    @SuppressWarnings("unchecked")
    protected <T> Result<T> createResult(
            ClassLoader loader,
            Name name,
            Object...arguments) {
        return (Result<T>) create(loader, name, arguments);
    }

    /**
     * Loads a class and returns its object.
     * @param <K> the key type
     * @param <V> the value type
     * @param loader the class loader
     * @param name the class name
     * @param arguments the arguments
     * @return the created instance
     */
    @SuppressWarnings("unchecked")
    protected <K extends Writable, V extends Writable> Rendezvous<V> createRendezvous(
            ClassLoader loader,
            Name name,
            Object...arguments) {
        return (Rendezvous<V>) create(loader, name, arguments);
    }

    /**
     * Invokes the target method.
     * @param object the target object
     * @param name the target method name
     * @param arguments the arguments
     * @return the method result
     */
    protected Object invoke(Object object, String name, Object... arguments) {
        try {
            for (Method method : object.getClass().getMethods()) {
                if (method.getName().equals(name)) {
                    return method.invoke(object, arguments);
                }
            }
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        throw new AssertionError(name);
    }

    /**
     * Returns the field value.
     * @param object the target object
     * @param name the field name
     * @return the field value
     */
    protected Object access(Object object, String name) {
        try {
            for (Field field : object.getClass().getFields()) {
                if (field.getName().equals(name)) {
                    return field.get(object);
                }
            }
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        throw new AssertionError(name);
    }

    /**
     * Returns the shuffle key of the target stage.
     * @param loader the class loader
     * @param stage the target stage
     * @return the target shuffle key
     */
    protected SegmentedWritable createShuffleKey(
            ClassLoader loader,
            StageModel stage) {
        assertThat(stage.getShuffleModel(), not(nullValue()));
        Name name = stage.getShuffleModel().getCompiled().getKeyTypeName();
        return (SegmentedWritable) create(loader, name);
    }

    /**
     * Returns the shuffle value of the target stage.
     * @param loader the class loader
     * @param stage the target stage
     * @return the target shuffle value
     */
    protected SegmentedWritable createShuffleValue(
            ClassLoader loader,
            StageModel stage) {
        assertThat(stage.getShuffleModel(), not(nullValue()));
        Name name = stage.getShuffleModel().getCompiled().getValueTypeName();
        return (SegmentedWritable) create(loader, name);
    }

    /**
     * Sets a content into the target shuffle key.
     * @param segment the target segment
     * @param key the target object
     * @param toSet the source object
     */
    protected void setShuffleKey(
            Segment segment,
            SegmentedWritable key,
            Object toSet) {
        String name = Naming.getShuffleKeySetter(segment.getPortId());
        try {
            Method method = key.getClass().getMethod(name, toSet.getClass());
            method.invoke(key, toSet);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Sets a content into the target shuffle value.
     * @param segment the target segment
     * @param value the target object
     * @param toSet the source object
     */
    protected void setShuffleValue(
            Segment segment,
            SegmentedWritable value,
            Object toSet) {
        String name = Naming.getShuffleValueSetter(segment.getPortId());
        try {
            Method method = value.getClass().getMethod(name, toSet.getClass());
            method.invoke(value, toSet);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Sets a content into the target shuffle key and value.
     * @param segment the target segment
     * @param key the target key object
     * @param value the target value object
     * @param toSet the source object
     */
    protected void setShuffleKeyValue(
            Segment segment,
            SegmentedWritable key,
            SegmentedWritable value,
            Object toSet) {
        setShuffleKey(segment, key, toSet);
        setShuffleValue(segment, value, toSet);
    }

    /**
     * Restores a source object from the shuffle value.
     * @param segment the target segment
     * @param value the target shuffle value
     * @return the source object for the target segment
     */
    protected Object getShuffleValue(
            Segment segment,
            SegmentedWritable value) {
        String name = Naming.getShuffleValueGetter(segment.getPortId());
        try {
            Method method = value.getClass().getMethod(name);
            return method.invoke(value);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Compiles previously added sources.
     * @return the class loader for loading results
     */
    protected ClassLoader start() {
        List<Diagnostic<? extends JavaFileObject>> diagnostics = doCompile();
        boolean wrong = false;
        for (Diagnostic<?> d : diagnostics) {
            if (d.getKind() != Diagnostic.Kind.NOTE) {
                wrong = true;
                break;
            }
        }
        if (wrong) {
            for (JavaFileObject java : javaCompiler.getSources()) {
                try {
                    System.out.println("====" + java.getName());
                    System.out.println(java.getCharContent(true));
                } catch (IOException e) {
                    // ignore.
                }
            }
            for (Diagnostic<? extends JavaFileObject> d : diagnostics) {
                System.out.println("====");
                System.out.println(d);
            }
            throw new AssertionError(diagnostics);
        }
        return javaCompiler.getClassLoader();
    }

    private List<Diagnostic<? extends JavaFileObject>> doCompile() {
        List<VolatileJavaFile> sources = packager.getEmitter().getEmitted();
        if (LOG.isDebugEnabled()) {
            for (JavaFileObject java : sources) {
                try {
                    LOG.debug("==== {}", java.getName());
                    LOG.debug("{}", java.getCharContent(true));
                } catch (IOException e) {
                    // ignore.
                }
            }
        }

        javaCompiler.addArguments("-Xlint:unchecked");
        for (JavaFileObject java : sources) {
            javaCompiler.addSource(java);
        }
        if (sources.isEmpty()) {
            javaCompiler.addSource(new VolatileJavaFile("A", "public class A {}"));
        }
        List<Diagnostic<? extends JavaFileObject>> diagnostics = javaCompiler.doCompile();
        if (LOG.isDebugEnabled()) {
            for (JavaFileObject java : javaCompiler.getSources()) {
                try {
                    LOG.debug("==== {}", java.getName());
                    LOG.debug("{}", java.getCharContent(true));
                } catch (IOException e) {
                    // ignore.
                }
            }
            for (Diagnostic<? extends JavaFileObject> d : diagnostics) {
                LOG.debug("====");
                LOG.debug("{}", d);
            }
        }
        return diagnostics;
    }
}
