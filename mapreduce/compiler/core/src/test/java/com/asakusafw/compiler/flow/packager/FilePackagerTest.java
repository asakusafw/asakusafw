/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.compiler.flow.packager;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import javax.lang.model.SourceVersion;

import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.flow.JobflowCompilerTestRoot;
import com.asakusafw.compiler.flow.Packager;
import com.asakusafw.utils.java.model.syntax.CompilationUnit;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.Models;

/**
 * Test for {@link FilePackager}.
 */
public class FilePackagerTest extends JobflowCompilerTestRoot {

    static final Logger LOG = LoggerFactory.getLogger(FilePackagerTest.class);

    /**
     * A temporary folder for testing.
     */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * Builds a Java source file.
     * @throws Exception if exception was occurred
     */
    @Test
    public void build_java() throws Exception {
        Set<String> entries = new HashSet<>();

        FilePackager packager = new FilePackager(
                folder.newFolder(),
                Collections.emptyList());
        packager.initialize(environment);

        emit(packager, java("Hello"));
        build(entries, packager);
        assertThat(entries, hasItem("com/example/Hello.class"));
    }

    /**
     * Builds a resource file.
     * @throws Exception if exception was occurred
     */
    @Test
    public void build_resource() throws Exception {
        Set<String> entries = new HashSet<>();

        FilePackager packager = new FilePackager(
                folder.newFolder(),
                Collections.emptyList());
        packager.initialize(environment);

        write(packager, "com.example", "messages.properties", "key=value");
        build(entries, packager);
        assertThat(entries, hasItem("com/example/messages.properties"));
    }

    /**
     * Builds Java and resource files.
     * @throws Exception if exception was occurred
     */
    @Test
    public void build_mixed() throws Exception {
        Set<String> entries = new HashSet<>();

        FilePackager packager = new FilePackager(
                folder.newFolder(),
                Collections.emptyList());
        packager.initialize(environment);

        emit(packager, java("Hello"));
        emit(packager, java("World"));
        write(packager, "com.example", "messages.properties", "key=value");
        write(packager, null, "META-INF/services/Example", "");
        build(entries, packager);
        assertThat(entries, hasItem("com/example/Hello.class"));
        assertThat(entries, hasItem("com/example/World.class"));
        assertThat(entries, hasItem("META-INF/services/Example"));
        assertThat(entries, hasItem("com/example/messages.properties"));
    }

    /**
     * Builds a malformed Java files.
     * @throws Exception if exception was occurred
     */
    @Test
    public void build_error() throws Exception {
        Set<String> entries = new HashSet<>();

        FilePackager packager = new FilePackager(
                folder.newFolder(),
                Collections.emptyList());
        packager.initialize(environment);
        CompilationUnit cu = getErroneousSource();
        emit(packager, cu);
        try {
            build(entries, packager);
            fail();
        } catch (IOException e) {
            assertThat(environment.hasError(), is(true));
        }
    }

    /**
     * Skipping packaging.
     * @throws Exception if failed
     */
    @Test
    public void skip_packaging() throws Exception {
        environment.getOptions().putExtraAttribute(FilePackager.KEY_OPTION_PACKAGING, "false");
        Set<String> entries = new HashSet<>();

        FilePackager packager = new FilePackager(
                folder.newFolder(),
                Collections.emptyList());
        packager.initialize(environment);

        CompilationUnit cu = getErroneousSource();

        emit(packager, cu);
        build(entries, packager);
        assertThat(environment.hasError(), is(false));
    }

    /**
     * Builds a java source file w/ {@code JDK 7}.
     * @throws Exception if failed
     */
    @Test
    public void build_java_jdk7() throws Exception {
        Assume.assumeThat(SourceVersion.latest(), is(greaterThan(SourceVersion.RELEASE_8)));
        environment.getOptions().putExtraAttribute(FilePackager.KEY_JAVA_VERSION, FilePackager.DEFAULT_JAVA_VERSION);
        Set<String> entries = new HashSet<>();
        FilePackager packager = new FilePackager(
                folder.newFolder(),
                Collections.emptyList());
        packager.initialize(environment);
        emit(packager, java("Hello"));
        build(entries, packager);
        assertThat(entries, hasItem("com/example/Hello.class"));
    }

    private CompilationUnit getErroneousSource() {
        ModelFactory f = Models.getModelFactory();
        CompilationUnit cu = f.newCompilationUnit(
                f.newPackageDeclaration(Models.toName(f, "com.example")),
                Collections.emptyList(),
                Collections.singletonList(f.newClassDeclaration(
                        null,
                        new AttributeBuilder(f)
                            .Public()
                            .Private()
                            .toAttributes(),
                        f.newSimpleName("Hello"),
                        null,
                        Collections.emptyList(),
                        Collections.emptyList())));
        return cu;
    }

    private void build(Set<String> entries, FilePackager packager)
            throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        packager.build(output);
        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
        try (JarInputStream jar = new JarInputStream(input)) {
            while (true) {
                JarEntry entry = jar.getNextJarEntry();
                if (entry == null) {
                    break;
                }
                entries.add(entry.getName());
            }
        }
    }

    private void emit(Packager packager, CompilationUnit java) throws IOException {
        try (PrintWriter writer = packager.openWriter(java)) {
            Models.emit(java, writer);
        }
    }

    private void write(Packager packager, String pkg, String rel, String value) throws IOException {
        ModelFactory f = Models.getModelFactory();
        try (OutputStream output = packager.openStream(
                pkg == null ? null : Models.toName(f, pkg),
                rel)) {
            output.write(value.getBytes("UTF-8"));
        }
    }

    private CompilationUnit java(String name) {
        ModelFactory f = Models.getModelFactory();
        return f.newCompilationUnit(
                f.newPackageDeclaration(Models.toName(f, "com.example")),
                Collections.emptyList(),
                Collections.singletonList(f.newClassDeclaration(
                        null,
                        new AttributeBuilder(f)
                            .Public()
                            .toAttributes(),
                        f.newSimpleName(name),
                        null,
                        Collections.emptyList(),
                        Collections.emptyList())));
    }
}
