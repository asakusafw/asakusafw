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
package com.asakusafw.testdriver;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.testdriver.core.DataModelSinkFactory;
import com.asakusafw.testdriver.core.DifferenceSinkFactory;
import com.asakusafw.testdriver.core.ModelTester;
import com.asakusafw.testdriver.core.ModelVerifier;
import com.asakusafw.testdriver.core.TestRule;
import com.asakusafw.testdriver.core.VerifierFactory;
import com.asakusafw.testdriver.core.VerifyRule;
import com.asakusafw.vocabulary.external.ExporterDescription;

/**
 * テストドライバのテスト出力データの親クラス。
 * @since 0.2.0
 *
 * @param <T> モデルクラス
 */
public class DriverOutputBase<T> extends DriverInputBase<T> {

    private static final Logger LOG = LoggerFactory.getLogger(DriverOutputBase.class);

    /**
     * エクスポータ記述。
     */
    protected ExporterDescription exporterDescription;

    /**
     * 検証エンジン (nullable)。
     * @since 0.2.3
     */
    protected VerifierFactory verifier;

    /**
     * 結果の出力先 (nullable)。
     * @since 0.2.3
     */
    protected DataModelSinkFactory resultSink;

    /**
     * 差異の出力先 (nullable)。
     * @since 0.2.3
     */
    protected DifferenceSinkFactory differenceSink;

    /**
     * Returns the exporter description for this output.
     * @return the description
     */
    protected ExporterDescription getExporterDescription() {
        return exporterDescription;
    }

    /**
     * Sets the exporter description for this output.
     * @param exporterDescription the description
     */
    protected void setExporterDescription(ExporterDescription exporterDescription) {
        this.exporterDescription = exporterDescription;
    }

    /**
     * Returns the verifier.
     * @return the verifier, or {@code null} if not defined
     * @since 0.2.3
     */
    protected VerifierFactory getVerifier() {
        return verifier;
    }

    /**
     * Sets the verify rule for this output.
     * @param verifier the verifier to set, or {@code null} to clear verifier
     * @since 0.2.3
     */
    protected void setVerifier(VerifierFactory verifier) {
        this.verifier = verifier;
    }

    /**
     * Sets the verifier for this output.
     * @param expectedUri the URI which represents the expected data set
     * @param ruleUri the URI which represents the verification rule description
     * @param extraRules the extra verification rules
     * @throws IOException if failed to create verifier
     * @since 0.2.3
     */
    protected void setVerifier(
            URI expectedUri,
            URI ruleUri,
            List<? extends ModelTester<? super T>> extraRules) throws IOException {
        List<TestRule> ruleFragments = new ArrayList<TestRule>();
        for (ModelTester<? super T> tester : extraRules) {
            TestRule fragment = driverContext.getRepository().toVerifyRuleFragment(modelType, tester);
            ruleFragments.add(fragment);
        }
        VerifierFactory factory = driverContext.getRepository().getVerifierFactory(expectedUri, ruleUri, ruleFragments);
        setVerifier(factory);
    }

    /**
     * Sets the verifier for this output.
     * @param expectedUri the URI which represents the expected data set
     * @param modelVerifier the model verifier
     * @throws IOException if failed to create verifier
     * @since 0.2.3
     */
    protected void setVerifier(URI expectedUri, ModelVerifier<? super T> modelVerifier) throws IOException {
        LOG.info("expected: {}", expectedUri);
        VerifyRule rule = driverContext.getRepository().toVerifyRule(modelType, modelVerifier);
        VerifierFactory factory = driverContext.getRepository().getVerifierFactory(expectedUri, rule);
        setVerifier(factory);
    }

    /**
     * Sets the verifier for this output.
     * @param expectedPath the path which represents the expected data set
     * @param rulePath the path which represents the verification rule description
     * @param extraRules the extra verification rules
     * @throws IOException if failed to create verifier
     * @since 0.2.3
     */
    protected void setVerifier(
            String expectedPath,
            String rulePath,
            List<? extends ModelTester<? super T>> extraRules) throws IOException {
        URI expectedUri;
        try {
            expectedUri = toUri(expectedPath);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("invalid expected URI:" + expectedPath, e);
        }
        URI ruleUri;
        try {
            ruleUri = toUri(rulePath);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("invalid rule URI: " + rulePath, e);
        }
        setVerifier(expectedUri, ruleUri, extraRules);
    }

    /**
     * Sets the verify rule for this output.
     * @param expectedPath the path which represents the expected data set
     * @param modelVerifier the model verifier
     * @throws IOException if failed to create verifier
     * @since 0.2.3
     */
    protected void setVerifier(String expectedPath, ModelVerifier<? super T> modelVerifier) throws IOException {
        URI expectedUri;
        try {
            expectedUri = toUri(expectedPath);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("invalid expected URI:" + expectedPath, e);
        }
        setVerifier(expectedUri, modelVerifier);
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
     * @param resultSink the result sink to set, {@code null} to cleare the sink
     * @since 0.2.3
     */
    public void setResultSink(DataModelSinkFactory resultSink) {
        this.resultSink = resultSink;
    }

    /**
     * Sets the actual data sink for this output.
     * The specified object will save the actual result of this.
     * @param path the output path
     * @since 0.2.3
     */
    public void setResultSinkUri(String path) {
        URI uri = toOutputUri(path);
        setResultSinkUri(uri);
    }

    /**
     * Sets the actual data sink for this output.
     * The specified object will save the actual result of this.
     * @param uri the target URI
     * @since 0.2.3
     */
    public void setResultSinkUri(URI uri) {
        LOG.info("result sink: {}", uri);
        DataModelSinkFactory sink = driverContext.getRepository().getDataModelSinkFactory(uri);
        setResultSink(sink);
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
     * @param differenceSink the difference sink to set, {@code null} to cleare the sink
     * @since 0.2.3
     */
    public void setDifferenceSink(DifferenceSinkFactory differenceSink) {
        this.differenceSink = differenceSink;
    }

    /**
     * Sets the difference information sink for this output.
     * The specified object will save the difference from expected result of this.
     * @param path the target path
     * @since 0.2.3
     */
    protected void setDifferenceSinkUri(String path) {
        setDifferenceSinkUri(toOutputUri(path));
    }

    /**
     * Sets the difference information sink for this output.
     * The specified object will save the difference from expected result of this.
     * @param uri the target URI
     * @since 0.2.3
     */
    protected void setDifferenceSinkUri(URI uri) {
        LOG.info("difference sink: {}", uri);
        DifferenceSinkFactory sink = driverContext.getRepository().getDifferenceSinkFactory(uri);
        setDifferenceSink(sink);
    }

    /**
     * Converts the path into the related URI.
     * @param path the path
     * @return the resulting URI
     * @since 0.2.3
     */
    protected URI toOutputUri(String path) {
        URI uri = URI.create(path);
        if (uri.getScheme() != null) {
            return uri;
        }
        return new File(path).toURI();
    }
}
