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
package com.asakusafw.utils.java.jsr199.testing;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Set;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link VolatileCompiler}.
 */
public class VolatileCompilerTest {

    private VolatileCompiler compiler;

    /**
     * テストを初期化する。
     * @throws Exception if occur
     */
    @Before
    public void setUp() throws Exception {
        compiler = new VolatileCompiler();
    }

    /**
     * テストの情報を破棄する。
     * @throws Exception 例外が発生した場合
     */
    @After
    public void tearDown() throws Exception {
        if (compiler != null) {
            compiler.close();
        }
    }

    /**
     * 注釈つき。
     */
    @Test
    public void annotated() {
        compiler.addSource(load("Example.java"));
        compiler.addProcessor(new MockProcessor());
        ClassLoader compiled = compile();
        Set<String> elements = MockProcessor.load(compiled);
        assertThat(elements.size(), is(1));
        assertThat(elements, hasItem("Example"));
    }

    /**
     * 注釈なし。
     */
    @Test
    public void noAnnotated() {
        compiler.addSource(load("Empty.java"));
        compiler.addProcessor(new MockProcessor());
        ClassLoader compiled = compile();
        Set<String> elements = MockProcessor.load(compiled);
        assertThat(elements.size(), is(0));
    }

    /**
     * 複数注釈。
     */
    @Test
    public void multiAnnotated() {
        compiler.addSource(load("multi/A.java"));
        compiler.addSource(load("multi/B.java"));
        compiler.addSource(load("multi/C.java"));
        compiler.addProcessor(new MockProcessor());
        ClassLoader compiled = compile();
        Set<String> elements = MockProcessor.load(compiled);
        assertThat(elements.size(), is(3));
        assertThat(elements, hasItem("A"));
        assertThat(elements, hasItem("B"));
        assertThat(elements, hasItem("C"));
    }

    private ClassLoader compile() {
        List<Diagnostic<? extends JavaFileObject>> diagnostics = compiler.doCompile();
        for (Diagnostic<?> d : diagnostics) {
            if (d.getKind() == Diagnostic.Kind.ERROR) {
                throw new AssertionError(diagnostics);
            }
        }
        for (Diagnostic<?> d : diagnostics) {
            System.err.println(d);
        }
        return compiler.getClassLoader();
    }

    private JavaFileObject load(String path) {
        Class<?> klass = getClass();
        String name = klass.getSimpleName() + ".img/" + path + ".txt";
        InputStream in = klass.getResourceAsStream(name);
        assertThat(name, in, not(nullValue()));
        StringBuilder content = new StringBuilder();
        try {
            char[] buf = new char[1024];
            Reader r = new InputStreamReader(in, "UTF-8");
            try {
                while (true) {
                    int read = r.read(buf);
                    if (read < 0) {
                        break;
                    }
                    content.append(buf, 0, read);
                }
            } finally {
                in.close();
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return new VolatileJavaFile(path, content.toString());
    }
}
