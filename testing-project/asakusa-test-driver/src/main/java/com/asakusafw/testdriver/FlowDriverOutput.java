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
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.runtime.directio.DataFormat;
import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelSinkFactory;
import com.asakusafw.testdriver.core.DataModelSource;
import com.asakusafw.testdriver.core.DataModelSourceFactory;
import com.asakusafw.testdriver.core.DataModelSourceFilter;
import com.asakusafw.testdriver.core.DifferenceSinkFactory;
import com.asakusafw.testdriver.core.ModelTester;
import com.asakusafw.testdriver.core.ModelTransformer;
import com.asakusafw.testdriver.core.ModelVerifier;
import com.asakusafw.testdriver.core.TestDataToolProvider;
import com.asakusafw.testdriver.core.VerifierFactory;
import com.asakusafw.utils.io.Provider;
import com.asakusafw.utils.io.Source;

/**
 * An abstract super class which represents an output port of data flow on testing.
 * Clients should not inherit this class directly.
 * @param <T> the output data model type
 * @param <S> the implementation class type
 * @since 0.6.0
 * @version 0.9.1
 */
public abstract class FlowDriverOutput<T, S extends FlowDriverOutput<T, S>> extends DriverOutputBase<T> {

    private static final Logger LOG = LoggerFactory.getLogger(FlowDriverOutput.class);

    /**
     * Creates a new instance.
     * @param callerClass the current context class
     * @param testTools the test data tools
     * @param name the original input name
     * @param modelType the data model type
     * @since 0.6.0
     */
    public FlowDriverOutput(Class<?> callerClass, TestDataToolProvider testTools, String name, Class<T> modelType) {
        super(callerClass, testTools, name, modelType);
    }

    /**
     * Returns this object.
     * @return this
     * @since 0.6.0
     */
    protected abstract S getThis();

    /**
     * Sets the initial data set for this output.
     * @param factory factory which provides test data set
     * @return this
     * @since 0.6.0
     */
    public S prepare(DataModelSourceFactory factory) {
        return prepare(factory, null);
    }

    /**
     * Sets the test data set for this output.
     * @param factory factory which provides test data set
     * @param transformer the input source transformer
     * @return this
     * @since 0.10.2
     */
    public S prepare(DataModelSourceFactory factory, Consumer<? super T> transformer) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        LOG.debug("prepare - ModelType: {}", getModelType()); //$NON-NLS-1$
        if (transformer == null) {
            setSource(factory);
        } else {
            DataModelSourceFilter filter = toDataModelSourceFilter(transformer);
            setSource(filter.apply(factory));
        }
        return getThis();
    }

    /**
     * Sets how verify the execution results for this output.
     * @param factory factory which provides result verifier
     * @return this
     * @since 0.2.3
     */
    public S verify(VerifierFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        setVerifier(factory);
        return getThis();
    }

    /**
     * Sets the result data set filter for this output.
     * This can select or transform the results before verifying them.
     * @param filter the data set filter
     * @return this
     * @since 0.7.0
     */
    public S filter(UnaryOperator<DataModelSource> filter) {
        if (filter == null) {
            throw new IllegalArgumentException("filter must not be null"); //$NON-NLS-1$
        }
        setResultFilter(filter);
        return getThis();
    }

    /**
     * Sets the result data sink for this output.
     * @param factory factory which provides result data sink
     * @return this
     * @since 0.2.3
     */
    public S dumpActual(DataModelSinkFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        setResultSink(factory);
        return getThis();
    }

    /**
     * Sets the result differences sink for this output.
     * @param factory factory which provides result differences sink
     * @return this
     * @since 0.2.3
     */
    public S dumpDifference(DifferenceSinkFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        setDifferenceSink(factory);
        return getThis();
    }

    /**
     * Sets the initial data set for this output.
     * @param sourcePath path to the initial data set file
     * @return this
     * @throws IllegalArgumentException if the source was not found on the path
     * @since 0.2.0
     */
    public S prepare(String sourcePath) {
        if (sourcePath == null) {
            throw new IllegalArgumentException("sourcePath must not be null"); //$NON-NLS-1$
        }
        return prepare(toDataModelSourceFactory(sourcePath));
    }

    /**
     * Sets the initial data set for this output.
     * @param objects the initial data objects
     * @return this
     * @since 0.6.0
     */
    public S prepare(Iterable<? extends T> objects) {
        if (objects == null) {
            throw new IllegalArgumentException("objects must not be null"); //$NON-NLS-1$
        }
        return prepare(toDataModelSourceFactory(objects));
    }

    /**
     * Sets the initial data set for this output.
     * @param provider the expected data set provider
     * @return this
     * @since 0.6.0
     */
    public S prepare(Provider<? extends Source<? extends T>> provider) {
        if (provider == null) {
            throw new IllegalArgumentException("provider must not be null"); //$NON-NLS-1$
        }
        return prepare(toDataModelSourceFactory(provider));
    }

    /**
     * Sets the test data set for this input.
     * Note that, the original source path may be changed if tracking source file name.
     * To keep the source file path information, please use {@link #prepare(Class, File)} instead.
     * @param formatClass the data format class
     * @param sourcePath the input file path on the class path
     * @return this
     * @throws IllegalArgumentException if the source is not valid for the given data format
     * @since 0.9.1
     */
    public S prepare(Class<? extends DataFormat<? super T>> formatClass, String sourcePath) {
        DataModelDefinition<T> definition = getDataModelDefinition();
        return prepare(toDataModelSourceFactory(definition, formatClass, sourcePath));
    }

    /**
     * Sets the test data set for this input.
     * @param formatClass the data format class
     * @param sourceFile the input file
     * @return this
     * @throws IllegalArgumentException if the source is not valid for the given data format
     * @since 0.9.1
     */
    public S prepare(Class<? extends DataFormat<? super T>> formatClass, File sourceFile) {
        DataModelDefinition<T> definition = getDataModelDefinition();
        return prepare(toDataModelSourceFactory(definition, formatClass, sourceFile));
    }

    /**
     * Enables to verify the results of this output.
     * @param expectedFactory factory which provides the expected data set
     * @param verifyRulePath the path to verification rule file
     * @return this
     * @throws IllegalArgumentException if the verification rule file was not found
     * @since 0.6.0
     */
    public S verify(DataModelSourceFactory expectedFactory, String verifyRulePath) {
        if (expectedFactory == null) {
            throw new IllegalArgumentException("expectedFactory must not be null"); //$NON-NLS-1$
        }
        if (verifyRulePath == null) {
            throw new IllegalArgumentException("verifyRulePath must not be null"); //$NON-NLS-1$
        }
        return verify(expectedFactory, verifyRulePath, null);
    }

    /**
     * Enables to verify the results of this output.
     * @param expectedPath the path to the expected data set file
     * @param verifyRulePath the path to verification rule file
     * @return this
     * @throws IllegalArgumentException if either the expected data set or verification rule file was not found
     * @since 0.2.0
     */
    public S verify(String expectedPath, String verifyRulePath) {
        if (expectedPath == null) {
            throw new IllegalArgumentException("expectedPath must not be null"); //$NON-NLS-1$
        }
        if (verifyRulePath == null) {
            throw new IllegalArgumentException("verifyRulePath must not be null"); //$NON-NLS-1$
        }
        return verify(toDataModelSourceFactory(expectedPath), verifyRulePath, null);
    }

    /**
     * Enables to verify the results of this output.
     * @param expectedObjects the expected data objects
     * @param verifyRulePath the path to verification rule file
     * @return this
     * @throws IllegalArgumentException if the verification rule file was not found
     * @since 0.6.0
     */
    public S verify(Iterable<? extends T> expectedObjects, String verifyRulePath) {
        if (expectedObjects == null) {
            throw new IllegalArgumentException("expectedObjects must not be null"); //$NON-NLS-1$
        }
        if (verifyRulePath == null) {
            throw new IllegalArgumentException("verifyRulePath must not be null"); //$NON-NLS-1$
        }
        return verify(toDataModelSourceFactory(expectedObjects), verifyRulePath, null);
    }

    /**
     * Enables to verify the results of this output.
     * @param expectedProvider the expected data set provider
     * @param verifyRulePath the path to verification rule file
     * @return this
     * @throws IllegalArgumentException if the verification rule file was not found
     * @since 0.6.0
     */
    public S verify(Provider<? extends Source<? extends T>> expectedProvider, String verifyRulePath) {
        if (expectedProvider == null) {
            throw new IllegalArgumentException("expectedProvider must not be null"); //$NON-NLS-1$
        }
        if (verifyRulePath == null) {
            throw new IllegalArgumentException("verifyRulePath must not be null"); //$NON-NLS-1$
        }
        return verify(toDataModelSourceFactory(expectedProvider), verifyRulePath, null);
    }

    /**
     * Enables to verify the results of this output.
     * Note that, the original source path may be changed if tracking source file name.
     * To keep the source file path information, please use {@link #verify(Class, File, String)} instead.
     * @param formatClass the data format class
     * @param expectedPath the path to the expected data set file on the class path
     * @param verifyRulePath the path to verification rule file
     * @return this
     * @throws IllegalArgumentException if the source is not valid for the given data format
     * @since 0.9.1
     */
    public S verify(Class<? extends DataFormat<? super T>> formatClass, String expectedPath, String verifyRulePath) {
        DataModelDefinition<T> definition = getDataModelDefinition();
        return verify(toDataModelSourceFactory(definition, formatClass, expectedPath), verifyRulePath, null);
    }

    /**
     * Enables to verify the results of this output.
     * @param formatClass the data format class
     * @param expectedFile the expected data set file
     * @param verifyRulePath the path to verification rule file
     * @return this
     * @throws IllegalArgumentException if the source is not valid for the given data format
     * @since 0.9.1
     */
    public S verify(Class<? extends DataFormat<? super T>> formatClass, File expectedFile, String verifyRulePath) {
        DataModelDefinition<T> definition = getDataModelDefinition();
        return verify(toDataModelSourceFactory(definition, formatClass, expectedFile), verifyRulePath, null);
    }

    /**
     * Enables to verify the results of this output.
     * @param expectedFactory factory which provides the expected data set
     * @param verifyRulePath the path to verification rule file
     * @param tester the extra verification rule for each data model object (nullable)
     * @return this
     * @throws IllegalArgumentException if the verification rule file was not found
     * @since 0.6.0
     */
    public S verify(DataModelSourceFactory expectedFactory, String verifyRulePath, ModelTester<? super T> tester) {
        if (expectedFactory == null) {
            throw new IllegalArgumentException("expectedFactory must not be null"); //$NON-NLS-1$
        }
        if (verifyRulePath == null) {
            throw new IllegalArgumentException("verifyRulePath must not be null"); //$NON-NLS-1$
        }
        List<? extends ModelTester<? super T>> extraRules;
        if (tester == null) {
            extraRules = Collections.emptyList();
        } else {
            extraRules = Collections.singletonList(tester);
        }
        setVerifier(toVerifierFactory(expectedFactory, toVerifyRuleFactory(verifyRulePath, extraRules)));
        return getThis();
    }

    /**
     * Enables to verify the results of this output.
     * @param expectedPath the path to the expected data set file
     * @param verifyRulePath the path to verification rule file
     * @param tester the extra verification rule for each data model object (nullable)
     * @return this
     * @throws IllegalArgumentException if either the expected data set or verification rule file was not found
     * @since 0.2.3
     */
    public S verify(String expectedPath, String verifyRulePath, ModelTester<? super T> tester) {
        if (expectedPath == null) {
            throw new IllegalArgumentException("expectedPath must not be null"); //$NON-NLS-1$
        }
        if (verifyRulePath == null) {
            throw new IllegalArgumentException("verifyRulePath must not be null"); //$NON-NLS-1$
        }
        return verify(toDataModelSourceFactory(expectedPath), verifyRulePath, tester);
    }

    /**
     * Enables to verify the results of this output.
     * @param expectedObjects the expected data objects
     * @param verifyRulePath the path to verification rule file
     * @param tester the extra verification rule for each data model object (nullable)
     * @return this
     * @throws IllegalArgumentException if the verification rule file was not found
     * @since 0.6.0
     */
    public S verify(Iterable<? extends T> expectedObjects, String verifyRulePath, ModelTester<? super T> tester) {
        if (expectedObjects == null) {
            throw new IllegalArgumentException("expectedObjects must not be null"); //$NON-NLS-1$
        }
        if (verifyRulePath == null) {
            throw new IllegalArgumentException("verifyRulePath must not be null"); //$NON-NLS-1$
        }
        return verify(toDataModelSourceFactory(expectedObjects), verifyRulePath, tester);
    }

    /**
     * Enables to verify the results of this output.
     * @param expectedProvider the expected data set provider
     * @param verifyRulePath the path to verification rule file
     * @param tester the extra verification rule for each data model object (nullable)
     * @return this
     * @throws IllegalArgumentException if the verification rule file was not found
     * @since 0.6.0
     */
    public S verify(
            Provider<? extends Source<? extends T>> expectedProvider,
            String verifyRulePath, ModelTester<? super T> tester) {
        if (expectedProvider == null) {
            throw new IllegalArgumentException("expectedProvider must not be null"); //$NON-NLS-1$
        }
        if (verifyRulePath == null) {
            throw new IllegalArgumentException("verifyRulePath must not be null"); //$NON-NLS-1$
        }
        return verify(toDataModelSourceFactory(expectedProvider), verifyRulePath, tester);
    }

    /**
     * Enables to verify the results of this output.
     * Note that, the original source path may be changed if tracking source file name.
     * To keep the source file path information, please use {@link #verify(Class, String, String, ModelTester)}
     * instead.
     * @param formatClass the data format class
     * @param expectedPath the path to the expected data set file on the class path
     * @param verifyRulePath the path to verification rule file
     * @param tester the extra verification rule for each data model object (nullable)
     * @return this
     * @throws IllegalArgumentException if the source is not valid for the given data format
     * @since 0.9.1
     */
    public S verify(Class<? extends DataFormat<? super T>> formatClass, String expectedPath,
            String verifyRulePath, ModelTester<? super T> tester) {
        DataModelDefinition<T> definition = getDataModelDefinition();
        return verify(toDataModelSourceFactory(definition, formatClass, expectedPath), verifyRulePath, tester);
    }

    /**
     * Enables to verify the results of this output.
     * @param formatClass the data format class
     * @param expectedFile the expected data set file
     * @param verifyRulePath the path to verification rule file
     * @param tester the extra verification rule for each data model object (nullable)
     * @return this
     * @throws IllegalArgumentException if the source is not valid for the given data format
     * @since 0.9.1
     */
    public S verify(Class<? extends DataFormat<? super T>> formatClass, File expectedFile,
            String verifyRulePath, ModelTester<? super T> tester) {
        DataModelDefinition<T> definition = getDataModelDefinition();
        return verify(toDataModelSourceFactory(definition, formatClass, expectedFile), verifyRulePath, tester);
    }

    /**
     * Enables to verify the results of this output.
     * @param expectedFactory factory which provides the expected data set
     * @param modelVerifier the verification rule
     * @return this
     * @since 0.6.0
     */
    public S verify(DataModelSourceFactory expectedFactory, ModelVerifier<? super T> modelVerifier) {
        if (expectedFactory == null) {
            throw new IllegalArgumentException("expectedFactory must not be null"); //$NON-NLS-1$
        }
        if (modelVerifier == null) {
            throw new IllegalArgumentException("modelVerifier must not be null"); //$NON-NLS-1$
        }
        setVerifier(toVerifierFactory(expectedFactory, toVerifyRuleFactory(modelVerifier)));
        return getThis();
    }

    /**
     * Enables to verify the results of this output.
     * @param expectedPath the path to the expected data set file
     * @param modelVerifier the verification rule
     * @return this
     * @throws IllegalArgumentException if either the expected data set or verification rule file was not found
     * @since 0.2.0
     */
    public S verify(String expectedPath, ModelVerifier<? super T> modelVerifier) {
        if (expectedPath == null) {
            throw new IllegalArgumentException("expectedPath must not be null"); //$NON-NLS-1$
        }
        if (modelVerifier == null) {
            throw new IllegalArgumentException("modelVerifier must not be null"); //$NON-NLS-1$
        }
        return verify(toDataModelSourceFactory(expectedPath), modelVerifier);
    }

    /**
     * Enables to verify the results of this output.
     * @param expectedObjects the expected data objects
     * @param modelVerifier the verification rule
     * @return this
     * @since 0.6.0
     */
    public S verify(Iterable<? extends T> expectedObjects, ModelVerifier<? super T> modelVerifier) {
        if (expectedObjects == null) {
            throw new IllegalArgumentException("expectedObjects must not be null"); //$NON-NLS-1$
        }
        if (modelVerifier == null) {
            throw new IllegalArgumentException("modelVerifier must not be null"); //$NON-NLS-1$
        }
        return verify(toDataModelSourceFactory(expectedObjects), modelVerifier);
    }

    /**
     * Enables to verify the results of this output.
     * @param expectedProvider the expected data set provider
     * @param modelVerifier the verification rule
     * @return this
     * @since 0.6.0
     */
    public S verify(Provider<? extends Source<? extends T>> expectedProvider, ModelVerifier<? super T> modelVerifier) {
        if (expectedProvider == null) {
            throw new IllegalArgumentException("expectedProvider must not be null"); //$NON-NLS-1$
        }
        if (modelVerifier == null) {
            throw new IllegalArgumentException("modelVerifier must not be null"); //$NON-NLS-1$
        }
        return verify(toDataModelSourceFactory(expectedProvider), modelVerifier);
    }

    /**
     * Enables to verify the results of this output.
     * Note that, the original source path may be changed if tracking source file name.
     * To keep the source file path information, please use {@link #verify(Class, File, ModelVerifier)} instead.
     * @param formatClass the data format class
     * @param expectedPath the path to the expected data set file on the class path
     * @param modelVerifier the verification rule
     * @return this
     * @throws IllegalArgumentException if the source is not valid for the given data format
     * @since 0.9.1
     */
    public S verify(Class<? extends DataFormat<? super T>> formatClass, String expectedPath,
            ModelVerifier<? super T> modelVerifier) {
        DataModelDefinition<T> definition = getDataModelDefinition();
        return verify(toDataModelSourceFactory(definition, formatClass, expectedPath), modelVerifier);
    }

    /**
     * Enables to verify the results of this output.
     * @param formatClass the data format class
     * @param expectedFile the expected data set file
     * @param modelVerifier the verification rule
     * @return this
     * @throws IllegalArgumentException if the source is not valid for the given data format
     * @since 0.9.1
     */
    public S verify(Class<? extends DataFormat<? super T>> formatClass, File expectedFile,
            ModelVerifier<? super T> modelVerifier) {
        DataModelDefinition<T> definition = getDataModelDefinition();
        return verify(toDataModelSourceFactory(definition, formatClass, expectedFile), modelVerifier);
    }

    /**
     * Enables to transform the result data before verifying the results of this output.
     * @param transformer the data model object transformer
     * @return this
     * @since 0.10.2
     */
    public S transform(Consumer<? super T> transformer) {
        if (transformer == null) {
            throw new IllegalArgumentException("transformer must not be null"); //$NON-NLS-1$
        }
        return filter(toDataModelSourceFilter(transformer));
    }

    /**
     * Enables to transform the result data before verifying the results of this output.
     * @param transformer the data model object transformer
     * @return this
     * @since 0.7.0
     */
    public S transform(ModelTransformer<? super T> transformer) {
        return transform((Consumer<? super T>) transformer);
    }

    /**
     * Enables to store the result data set of this output.
     * @param outputPath the output path
     * @return this
     * @since 0.2.3
     */
    public S dumpActual(String outputPath) {
        if (outputPath == null) {
            throw new IllegalArgumentException("outputPath must not be null"); //$NON-NLS-1$
        }
        return dumpActual(toDataModelSinkFactory(outputPath));
    }

    /**
     * Enables to store the result data set of this output.
     * @param outputPath the output path
     * @return this
     * @since 0.2.3
     */
    public S dumpActual(File outputPath) {
        if (outputPath == null) {
            throw new IllegalArgumentException("outputPath must not be null"); //$NON-NLS-1$
        }
        return dumpActual(toDataModelSinkFactory(outputPath));
    }

    /**
     * Enables to store the result data set of this output.
     * @param formatClass the data format class
     * @param outputPath the output path
     * @return this
     * @since 0.9.1
     */
    public S dumpActual(Class<? extends DataFormat<? super T>> formatClass, File outputPath) {
        DataModelDefinition<T> definition = getDataModelDefinition();
        return dumpActual(toDataModelSinkFactory(definition, formatClass, outputPath));
    }

    /**
     * Enables to store the result differences of this output.
     * @param outputPath the output path
     * @return this
     * @since 0.2.3
     */
    public S dumpDifference(String outputPath) {
        if (outputPath == null) {
            throw new IllegalArgumentException("outputPath must not be null"); //$NON-NLS-1$
        }
        return dumpDifference(toDifferenceSinkFactory(outputPath));
    }

    /**
     * Enables to store the result differences of this output.
     * @param outputPath the output path
     * @return this
     * @since 0.2.3
     */
    public S dumpDifference(File outputPath) {
        if (outputPath == null) {
            throw new IllegalArgumentException("outputPath must not be null"); //$NON-NLS-1$
        }
        return dumpDifference(toDifferenceSinkFactory(outputPath));
    }

    /**
     * Configures this object.
     * @param configurator the configurator
     * @return this
     * @since 0.10.2
     */
    public S with(Consumer<? super S> configurator) {
        if (configurator != null) {
            configurator.accept(getThis());
        }
        return getThis();
    }
}
