/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.workflow.model.basic;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;

import com.asakusafw.workflow.model.CommandTaskInfo.ConfigurationResolver;
import com.asakusafw.workflow.model.CommandToken;
import com.asakusafw.workflow.model.InfoSerDe;

/**
 * Test for {@link BasicConfigurationResolver}.
 * @since 0.10.0
 */
public class BasicConfigurationResolverTest {

    /**
     * simple case.
     */
    @Test
    public void simple() {
        Map<String, String> pairs = new LinkedHashMap<>();
        pairs.put("a", "A");
        pairs.put("b", "B");
        pairs.put("c", "C");

        ConfigurationResolver resolver = new BasicConfigurationResolver("${key}:${value}");
        List<String> resolved = resolver.apply(pairs).stream()
            .map(CommandToken::getImage)
            .collect(Collectors.toList());

        assertThat(resolved, contains("a:A", "b:B", "c:C"));
    }

    /**
     * template has multiple tokens.
     */
    @Test
    public void multiple_tokens() {
        Map<String, String> pairs = new LinkedHashMap<>();
        pairs.put("a", "A");
        pairs.put("b", "B");
        pairs.put("c", "C");

        ConfigurationResolver resolver = new BasicConfigurationResolver("-P", "${key}", "${value}");
        List<String> resolved = resolver.apply(pairs).stream()
                .map(CommandToken::getImage)
                .collect(Collectors.toList());

        assertThat(resolved, contains("-P", "a", "A", "-P", "b", "B", "-P", "c", "C"));
    }

    /**
     * resillient through ser/de.
     */
    @Test
    public void serde() {
        Map<String, String> pairs = new LinkedHashMap<>();
        pairs.put("a", "A");
        pairs.put("b", "B");
        pairs.put("c", "C");

        ConfigurationResolver resolver = new BasicConfigurationResolver("${key}:${value}");
        ConfigurationResolver restored = InfoSerDe.restore(ConfigurationResolver.class, resolver);

        assertThat(restored.apply(pairs), is(resolver.apply(pairs)));
    }
}
