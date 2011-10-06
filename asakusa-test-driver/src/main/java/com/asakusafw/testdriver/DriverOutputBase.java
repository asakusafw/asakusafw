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
package com.asakusafw.testdriver;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.testdriver.core.DataModelSinkFactory;
import com.asakusafw.testdriver.core.ModelVerifier;
import com.asakusafw.vocabulary.external.ExporterDescription;

/**
 * テストドライバのテスト出力データの親クラス。
 * @since 0.2.0
 *
 * @param <T> モデルクラス
 */
public class DriverOutputBase<T> extends DriverInputBase<T> {

    private static final Logger LOG = LoggerFactory.getLogger(DriverOutputBase.class);

    /** 期待値URI */
    protected URI expectedUri;
    /** 検証ルール */
    protected VerifyRuleHolder<T> verifyRule;
    /** エクスポータ記述 */
    protected ExporterDescription exporterDescription;

    /**
     * 結果の出力先 (nullable)。
     */
    protected DataModelSinkHolder resultSink;

    /**
     * @return the expectedUri
     */
    protected URI getExpectedUri() {
        return expectedUri;
    }

    /**
     * @param expectedUri the expectedUri to set
     */
    protected void setExpectedUri(URI expectedUri) {
        this.expectedUri = expectedUri;
    }

    /**
     * Returns the verify rule for this output.
     * @return the verify rule if exists, {@code null} otherwise
     */
    public VerifyRuleHolder<T> getVerifyRule() {
        return verifyRule;
    }

    /**
     * Sets the verify rule for this output.
     * @param verifyRule the rule to set, {@code null} to clear verify rules
     */
    public void setVerifyRule(VerifyRuleHolder<T> verifyRule) {
        this.verifyRule = verifyRule;
    }

    /**
     * Returns the actual data sink for this output.
     * @return the actual data sink, or {@code null} if not defined
     */
    public DataModelSinkHolder getResultSink() {
        return resultSink;
    }

    /**
     * Sets the actual data sink for this output.
     * The specified object will save the actual result of this object.
     * @param resultSink the result sink to set, {@code null} to cleare the sink
     */
    public void setResultSink(DataModelSinkHolder resultSink) {
        this.resultSink = resultSink;
    }

    /**
     * @return the exporterDescription
     */
    protected ExporterDescription getExporterDescription() {
        return exporterDescription;
    }

    /**
     * @param exporterDescription the exporterDescription to set
     */
    protected void setExporterDescription(ExporterDescription exporterDescription) {
        this.exporterDescription = exporterDescription;
    }

    /**
     * set expected URI from expected path.
     *
     * @param expectedPath expected path.
     */
    protected void setExpectedUri(String expectedPath) {
        try {
            expectedUri = toUri(expectedPath);
            LOG.info("Expected URI:" + expectedUri);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("invalid expected URI. expectedPath:" + expectedPath, e);
        }
    }

    /**
     * Sets a {@link ModelVerifier} as verify rule for this output.
     * @param verifier the verifier
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    protected void setModelVerifier(ModelVerifier<? super T> verifier) {
        if (verifier == null) {
            throw new IllegalArgumentException("verifier must not be null"); //$NON-NLS-1$
        }
        this.verifyRule = new VerifyRuleHolder<T>(verifier);
        LOG.info("Model Verifier: {}", verifier);
    }

    /**
     * set verifyRule URI from verifyRule path.
     *
     * @param verifyRulePath expected path.
     */
    protected void setVerifyRuleUri(String verifyRulePath) {
        try {
            URI verifyRuleUri = toUri(verifyRulePath);
            LOG.info("Verify Rule URI:" + verifyRuleUri);
            setVerifyRule(new VerifyRuleHolder<T>(verifyRuleUri));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("invalid verifyRule URI. verifyRulePath:" + verifyRulePath, e);
        }
    }

    /**
     * Holds data model sink as {@link DataModelSinkFactory}.
     * @since 0.2.3
     */
    public static class DataModelSinkHolder {

        private final DataModelSinkFactory factory;

        /**
         * Creates a new instance.
         * @param factory the factory to be held
         * @throws IllegalArgumentException if some parameters were {@code null}
         */
        public DataModelSinkHolder(DataModelSinkFactory factory) {
            if (factory == null) {
                throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
            }
            this.factory = factory;
        }

        /**
         * Returns whether this holder has {@link DataModelSinkFactory}.
         * @return {@code true} iff this holder has {@link DataModelSinkFactory}
         */
        public boolean hasFactory() {
            return factory != null;
        }

        /**
         * Returns the defined factory.
         * @return the factory if defined, otherwise {@code false}
         */
        public DataModelSinkFactory getFactory() {
            return factory;
        }
    }

    /**
     * Holds verifier as {@link URI} or {@link ModelVerifier}.
     * @param <T> output model object type
     * @since 0.2.0
     */
    public static class VerifyRuleHolder<T> {

        private final URI uri;

        private final ModelVerifier<? super T> verifier;

        /**
         * Creates a new instance from {@link URI}.
         * @param uri verifier URI
         * @throws IllegalArgumentException if some parameters were {@code null}
         */
        public VerifyRuleHolder(URI uri) {
            if (uri == null) {
                throw new IllegalArgumentException("uri must not be null"); //$NON-NLS-1$
            }
            this.uri = uri;
            this.verifier = null;
        }

        /**
         * Creates a new instance from {@link ModelVerifier}.
         * @param verifier verifier object
         * @throws IllegalArgumentException if some parameters were {@code null}
         */
        public VerifyRuleHolder(ModelVerifier<? super T> verifier) {
            if (verifier == null) {
                throw new IllegalArgumentException("verifier must not be null"); //$NON-NLS-1$
            }
            this.uri = null;
            this.verifier = verifier;
        }

        /**
         * Returns {@code true} iff this holder has a verifier as URI.
         * @return {@code true} iff this holder has a verifier as URI
         */
        public boolean hasUri() {
            return uri != null;
        }

        /**
         * Returns the verifier URI.
         * @return the verifier URI if defined, {@code null} otherwise
         */
        public URI getUri() {
            return uri;
        }

        /**
         * Returns the verifier.
         * @return the verifier if defined, {@code null} otherwise
         */
        public ModelVerifier<? super T> getVerifier() {
            return verifier;
        }
    }
}
