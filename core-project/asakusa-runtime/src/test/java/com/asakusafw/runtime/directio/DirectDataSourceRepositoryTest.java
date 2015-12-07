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
package com.asakusafw.runtime.directio;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

/**
 * Test for {@link DirectDataSourceRepository}.
 */
public class DirectDataSourceRepositoryTest {

    /**
     * simple testing.
     * @throws Exception if failed
     */
    @Test
    public void simple() throws Exception {
        DirectDataSourceRepository repo = repo("id:hoge");

        assertThat(repo.getRelatedId("hoge"), is("id"));
        assertThat(repo.getRelatedId("hoge/foo"), is("id"));
        assertThat(repo.getRelatedId("hoge/foo/bar"), is("id"));

        assertThat(repo.getContainerPath("hoge"), is("hoge"));
        assertThat(repo.getContainerPath("hoge/foo"), is("hoge"));
        assertThat(repo.getContainerPath("hoge/foo/bar"), is("hoge"));

        assertThat(repo.getComponentPath("hoge"), is(""));
        assertThat(repo.getComponentPath("hoge/foo"), is("foo"));
        assertThat(repo.getComponentPath("hoge/foo/bar"), is("foo/bar"));
    }

    /**
     * root datasource.
     * @throws Exception if failed
     */
    @Test
    public void root() throws Exception {
        DirectDataSourceRepository repo = repo("id:");

        assertThat(repo.getRelatedId("hoge"), is("id"));
        assertThat(repo.getRelatedId("hoge/foo"), is("id"));
        assertThat(repo.getRelatedId("hoge/foo/bar"), is("id"));

        assertThat(repo.getContainerPath("hoge"), is(""));
        assertThat(repo.getContainerPath("hoge/foo"), is(""));
        assertThat(repo.getContainerPath("hoge/foo/bar"), is(""));

        assertThat(repo.getComponentPath("hoge"), is("hoge"));
        assertThat(repo.getComponentPath("hoge/foo"), is("hoge/foo"));
        assertThat(repo.getComponentPath("hoge/foo/bar"), is("hoge/foo/bar"));
    }

    private DirectDataSourceRepository repo(String... specs) {
        List<MockProvider> providers = new ArrayList<>();
        for (String spec : specs) {
            String[] fields = spec.split(":", 2);
            providers.add(new MockProvider(fields[0], fields[1]));
        }
        return new DirectDataSourceRepository(providers);
    }

    private static final class MockProvider implements DirectDataSourceProvider {

        private final String id;

        private final String path;

        MockProvider(String id, String path) {
            this.id = id;
            this.path = path;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public DirectDataSource newInstance() throws IOException, InterruptedException {
            MockDirectDataSource ds = new MockDirectDataSource();
            ds.configure(new DirectDataSourceProfile(
                    id,
                    MockDirectDataSource.class,
                    path,
                    Collections.<String, String>emptyMap()));
            return ds;
        }
    }
}
