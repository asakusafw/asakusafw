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
package com.asakusafw.testdriver.core;

import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 * Provides test data tools for Asakusa Framework.
 * @since 0.6.0
 */
public interface TestDataToolProvider {

    /**
     * Converts data model class into its definition.
     * @param <T> data model type
     * @param dataModelClass target class
     * @return the related definition
     * @throws IOException if failed to convert class
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    <T> DataModelDefinition<T> toDataModelDefinition(Class<T> dataModelClass) throws IOException;

    /**
     * Returns a {@link DataModelSourceFactory} for the URI.
     * @param uri target URI
     * @return related factory
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    DataModelSourceFactory getDataModelSourceFactory(URI uri);

    /**
     * Returns a {@link DataModelSinkFactory} for the URI.
     * @param uri target URI
     * @return related factory
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    DataModelSinkFactory getDataModelSinkFactory(URI uri);

    /**
     * Returns a {@link DifferenceSinkFactory} for the URI.
     * @param uri target URI
     * @return related factory
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    DifferenceSinkFactory getDifferenceSinkFactory(URI uri);

    /**
     * Returns a {@link VerifyRuleFactory} for the URI.
     * @param ruleUri the URI which describes verification rule
     * @param extraRules extra rules
     * @return the created factory
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    VerifyRuleFactory getVerifyRuleFactory(URI ruleUri, List<? extends TestRule> extraRules);

    /**
     * Converts model verifier into a corresponding rule.
     * @param <T> verification target type
     * @param dataModelClass target class
     * @param verifier target verifier object
     * @return the related rule
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    <T> VerifyRuleFactory toVerifyRuleFactory(DataModelDefinition<T> dataModelClass, ModelVerifier<? super T> verifier);

    /**
     * Converts model tester into a corresponding rule.
     * @param <T> test target type
     * @param dataModelClass target class
     * @param tester target tester object
     * @return the related rule
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    <T> TestRule toVerifyRuleFragment(DataModelDefinition<T> dataModelClass, ModelTester<? super T> tester);

    /**
     * Returns a {@link VerifierFactory} for expected data set and a verification rule.
     * @param expectedFactory the data model source factory which provides expected data set
     * @param ruleFactory verification rule factory
     * @return related factory
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    VerifierFactory toVerifierFactory(DataModelSourceFactory expectedFactory, VerifyRuleFactory ruleFactory);
}