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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.net.URI;

import org.junit.Test;

/**
 * Test for {@link SpiVerifyRuleProvider}.
 * @since 0.2.0
 */
public class SpiVerifyRuleProviderTest extends SpiTestRoot {

    /**
     * Test method for {@link SpiVerifyRuleProvider#get(DataModelDefinition, VerifyContext, URI)}.
     * @throws Exception if failed
     */
    @Test
    public void open() throws Exception {
        ClassLoader cl = register(VerifyRuleProvider.class, MockVerifyRuleProvider.class);
        SpiVerifyRuleProvider target = new SpiVerifyRuleProvider(cl);
        VerifyContext context = new VerifyContext(new TestContext.Empty());
        context.testFinished();
        VerifyRule rule = target.get(ValueDefinition.of(String.class), context, new URI("default:rule"));
        assertThat(rule, not(nullValue()));

        DataModelReflection ref = ValueDefinition.of(String.class).toReflection("Hello, world!");
        assertThat(rule.getKey(ref), is((Object) ref));
    }

    /**
     * not found.
     * @throws Exception if failed
     */
    @Test
    public void open_notfound() throws Exception {
        ClassLoader cl = register(VerifyRuleProvider.class, MockVerifyRuleProvider.class);
        SpiVerifyRuleProvider target = new SpiVerifyRuleProvider(cl);
        VerifyContext context = new VerifyContext(new TestContext.Empty());
        context.testFinished();
        VerifyRule rule = target.get(ValueDefinition.of(String.class), context, new URI("missing:rule"));
        assertThat(rule, is(nullValue()));
    }
}
