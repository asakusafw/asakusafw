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

import java.io.File;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelSinkFactory;
import com.asakusafw.testdriver.core.DataModelSourceFactory;
import com.asakusafw.testdriver.core.DataModelSourceFilter;
import com.asakusafw.testdriver.core.DifferenceSinkFactory;
import com.asakusafw.testdriver.core.ModelTester;
import com.asakusafw.testdriver.core.ModelTransformer;
import com.asakusafw.testdriver.core.ModelVerifier;
import com.asakusafw.testdriver.core.TestDataToolProvider;
import com.asakusafw.testdriver.core.TestRule;
import com.asakusafw.testdriver.core.VerifierFactory;
import com.asakusafw.testdriver.core.VerifyRuleFactory;

/**
 * テストドライバのテスト出力データの親クラス。
 * @since 0.2.0
 * @version 0.7.0
 * @param <T> モデルクラス
 */
public class DriverOutputBase<T> extends DriverInputBase<T> {

    private static final Logger LOG = LoggerFactory.getLogger(DriverOutputBase.class);

    private VerifierFactory verifier;

    private DataModelSinkFactory resultSink;

    private DifferenceSinkFactory differenceSink;

    private DataModelSourceFilter resultFilter;

    /**
     * Creates a new instance.
     * @param callerClass the current context class
     * @param testTools the test data tools
     * @param name the original input name
     * @param modelType the data model type
     * @since 0.6.0
     */
    public DriverOutputBase(Class<?> callerClass, TestDataToolProvider testTools, String name, Class<T> modelType) {
        super(callerClass, testTools, name, modelType);
    }

    /**
     * Returns the verifier.
     * @return the verifier, or {@code null} if not defined
     * @since 0.2.3
     */
    public VerifierFactory getVerifier() {
        if (verifier == null) {
            return null;
        } else if (resultFilter == null) {
            return verifier;
        } else {
            return toVerifierFactory(verifier, resultFilter);
        }
    }

    /**
     * Sets the verify rule for this output.
     * @param verifier the verifier to set, or {@code null} to clear verifier
     * @since 0.2.3
     */
    protected final void setVerifier(VerifierFactory verifier) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Verifier: name={}, model={}, verifier={}", new Object[] { //$NON-NLS-1$
                    getName(),
                    getModelType().getName(),
                    verifier,
            });
        }
        this.verifier = verifier;
    }

    /**
     * Returns the actual data sink for this output.
     * @return the actual data sink, or {@code null} if not defined
     * @since 0.2.3
     */
    public DataModelSinkFactory getResultSink() {
        return resultSink;
    }

    /**
     * Sets the actual data sink for this output.
     * The specified object will save the actual result of this.
     * @param resultSink the result sink to set, {@code null} to clear the sink
     * @since 0.2.3
     */
    protected final void setResultSink(DataModelSinkFactory resultSink) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("ResultSink: name={}, model={}, sink={}", new Object[] { //$NON-NLS-1$
                    getName(),
                    getModelType().getName(),
                    resultSink,
            });
        }
        this.resultSink = resultSink;
    }

    /**
     * Returns the difference information sink for this output.
     * @return the difference information sink, or {@code null} if not defined
     * @since 0.2.3
     */
    public DifferenceSinkFactory getDifferenceSink() {
        return differenceSink;
    }

    /**
     * Sets the difference information sink for this output.
     * The specified object will save the difference from expected result of this.
     * @param differenceSink the difference sink to set, {@code null} to clear the sink
     * @since 0.2.3
     */
    protected final void setDifferenceSink(DifferenceSinkFactory differenceSink) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("DifferenceSink: name={}, model={}, sink={}", new Object[] { //$NON-NLS-1$
                    getName(),
                    getModelType().getName(),
                    differenceSink,
            });
        }
        this.differenceSink = differenceSink;
    }

    /**
     * Sets the data model source filter for actual results of this output.
     * @param filter the source filter
     * @since 0.7.0
     */
    protected final void setResultFilter(DataModelSourceFilter filter) {
        this.resultFilter = filter;
    }

    /**
     * Converts an output path to {@link DataModelSinkFactory} to write to the path.
     * @param path the output path
     * @return the target sink factory
     * @since 0.6.0
     */
    protected final DataModelSinkFactory toDataModelSinkFactory(String path) {
        return getTestTools().getDataModelSinkFactory(toOutputUri(path));
    }

    /**
     * Converts an output path to {@link DataModelSinkFactory} to write to the path.
     * @param path the output path
     * @return the target sink factory
     * @since 0.6.0
     */
    protected final DataModelSinkFactory toDataModelSinkFactory(File path) {
        return getTestTools().getDataModelSinkFactory(path.toURI());
    }

    /**
     * Converts an output path to {@link DifferenceSinkFactory} to write to the path.
     * @param path the output path
     * @return the target sink factory
     * @since 0.6.0
     */
    protected final DifferenceSinkFactory toDifferenceSinkFactory(String path) {
        return getTestTools().getDifferenceSinkFactory(toOutputUri(path));
    }

    /**
     * Converts an output path to {@link DifferenceSinkFactory} to write to the path.
     * @param path the output path
     * @return the target sink factory
     * @since 0.6.0
     */
    protected final DifferenceSinkFactory toDifferenceSinkFactory(File path) {
        return getTestTools().getDifferenceSinkFactory(path.toURI());
    }

    /**
     * Converts an model transformer into {@link DataModelSourceFilter}.
     * @param transformer the data model transformer
     * @return the filter which transforms each data model objects using the transformer
     * @since 0.7.0
     */
    protected final DataModelSourceFilter toDataModelSourceFilter(final ModelTransformer<? super T> transformer) {
        final DataModelDefinition<T> definition = getDataModelDefinition();
        return toDataModelSourceFilter(definition, transformer);
    }

    /**
     * Converts {@link ModelVerifier} into {@link VerifyRuleFactory}.
     * @param rulePath the path which represents the verification rule description
     * @param extraRules the extra verification rules
     * @return the equivalent {@link VerifyRuleFactory}
     * @since 0.6.0
     */
    protected final VerifyRuleFactory toVerifyRuleFactory(
            String rulePath,
            List<? extends ModelTester<? super T>> extraRules) {
        try {
            TestDataToolProvider tools = getTestTools();
            List<TestRule> fragments = new ArrayList<TestRule>();
            for (ModelTester<? super T> tester : extraRules) {
                fragments.add(tools.toVerifyRuleFragment(getDataModelDefinition(), tester));
            }
            return tools.getVerifyRuleFactory(toUri(rulePath), fragments);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(MessageFormat.format(
                    "Invalid rule path: {0}", //$NON-NLS-1$
                    rulePath), e);
        }
    }

    /**
     * Converts {@link ModelVerifier} into {@link VerifyRuleFactory}.
     * @param modelVerifier the original verifier
     * @return the equivalent {@link VerifyRuleFactory}
     * @since 0.6.0
     */
    protected final VerifyRuleFactory toVerifyRuleFactory(ModelVerifier<? super T> modelVerifier) {
        return getTestTools().toVerifyRuleFactory(getDataModelDefinition(), modelVerifier);
    }

    /**
     * Converts a pair of expected data set factory and verify rule factory into {@link VerifyRuleFactory}.
     * @param expectedFactory the expected data set factory
     * @param ruleFactory the verification rule factory
     * @return the {@link VerifierFactory} which provides a verifier using the expected data set and verification rule
     * @since 0.6.0
     */
    protected final VerifierFactory toVerifierFactory(
            DataModelSourceFactory expectedFactory, VerifyRuleFactory ruleFactory) {
        return getTestTools().toVerifierFactory(expectedFactory, ruleFactory);
    }
}
