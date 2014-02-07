/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
import java.text.MessageFormat;

/**
 * An abstract implementation of {@link TestDataToolProvider}.
 * @since 0.6.0
 */
public abstract class AbstractTestDataToolProvider implements TestDataToolProvider {

    @Override
    public <T> VerifyRuleFactory toVerifyRuleFactory(
            DataModelDefinition<T> dataModel, ModelVerifier<? super T> verifier) {
        if (dataModel == null) {
            throw new IllegalArgumentException("dataModel must not be null"); //$NON-NLS-1$
        }
        if (verifier == null) {
            throw new IllegalArgumentException("verifier must not be null"); //$NON-NLS-1$
        }
        final ModelVerifierDriver<T> rule = new ModelVerifierDriver<T>(verifier, dataModel);
        return new VerifyRuleFactory() {
            @Override
            public <S> VerifyRule createRule(
                    DataModelDefinition<S> definition, VerifyContext context) throws IOException {
                return rule;
            }
            @Override
            public String toString() {
                return rule.toString();
            }
        };
    }

    @Override
    public <T> TestRule toVerifyRuleFragment(
            DataModelDefinition<T> dataModelClass, ModelTester<? super T> tester) {
        if (dataModelClass == null) {
            throw new IllegalArgumentException("dataModelClass must not be null"); //$NON-NLS-1$
        }
        if (tester == null) {
            throw new IllegalArgumentException("tester must not be null"); //$NON-NLS-1$
        }
        return new TesterDriver<T>(tester, dataModelClass);
    }

    @Override
    public VerifierFactory toVerifierFactory(
            final DataModelSourceFactory expectedFactory,
            final VerifyRuleFactory ruleFactory) {
        assert expectedFactory != null;
        assert ruleFactory != null;
        return new VerifierFactory() {
            @Override
            public <T> Verifier createVerifier(
                    DataModelDefinition<T> definition,
                    VerifyContext context) throws IOException {
                DataModelSource expected = expectedFactory.createSource(definition, context.getTestContext());
                VerifyRule verifyRule = ruleFactory.createRule(definition, context);
                boolean succeed = false;
                try {
                    Verifier verifier = new VerifyRuleVerifier(expected, verifyRule);
                    succeed = true;
                    return verifier;
                } finally {
                    if (succeed == false) {
                        expected.close();
                    }
                }
            }
            @Override
            public String toString() {
                return MessageFormat.format(
                        "Verifier(expected={0}, rule={1})",
                        expectedFactory,
                        ruleFactory);
            }
        };
    }

}