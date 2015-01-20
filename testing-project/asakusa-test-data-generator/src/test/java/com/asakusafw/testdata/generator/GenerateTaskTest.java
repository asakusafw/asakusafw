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
package com.asakusafw.testdata.generator;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.source.DmdlSourceRepository;
import com.asakusafw.dmdl.source.DmdlSourceResource;

/**
 * Test for {@link GenerateTask}.
 */
public class GenerateTaskTest {

    /**
     * Test method for {@link com.asakusafw.testdata.generator.GenerateTask#process()}.
     * @throws Exception if occur
     */
    @Test
    public void process() throws Exception {
        Mock mock = new Mock();
        DmdlSourceRepository repo = repo("simple.dmdl");
        GenerateTask task = new GenerateTask(mock, repo, getClass().getClassLoader());
        task.process();
        assertThat(mock.saw.size(), is(4));
        assertThat(mock.saw, hasItem("p"));
        assertThat(mock.saw, hasItem("a"));
        assertThat(mock.saw, hasItem("b"));
        assertThat(mock.saw, hasItem("c"));
    }

    private DmdlSourceRepository repo(String...files) {
        List<URL> resources = new ArrayList<URL>();
        for (String s : files) {
            URL r = getClass().getResource(s);
            assertThat(s, r, not(nullValue()));
            resources.add(r);
        }
        return new DmdlSourceResource(resources, Charset.forName("UTF-8"));
    }

    static class Mock implements TemplateGenerator {

        final Set<String> saw = new HashSet<String>();

        @Override
        public void generate(ModelDeclaration model) throws IOException {
            saw.add(model.getName().identifier);
        }

        @Override
        public String getTitle() {
            return getClass().getName();
        }
    }
}
