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
package com.asakusafw.compiler.flow.packager;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.batch.ResourceRepository;
import com.asakusafw.compiler.flow.JobflowCompilerTestRoot;
import com.asakusafw.compiler.flow.Packager;
import com.asakusafw.compiler.util.TemporaryFolder;
import com.ashigeru.lang.java.model.syntax.Comment;
import com.ashigeru.lang.java.model.syntax.CompilationUnit;
import com.ashigeru.lang.java.model.syntax.ImportDeclaration;
import com.ashigeru.lang.java.model.syntax.ModelFactory;
import com.ashigeru.lang.java.model.syntax.Type;
import com.ashigeru.lang.java.model.syntax.TypeBodyDeclaration;
import com.ashigeru.lang.java.model.syntax.TypeParameterDeclaration;
import com.ashigeru.lang.java.model.util.AttributeBuilder;
import com.ashigeru.lang.java.model.util.Models;

/**
 * Test for {@link FilePackager}.
 */
public class FilePackagerTest extends JobflowCompilerTestRoot {

    static final Logger LOG = LoggerFactory.getLogger(FilePackagerTest.class);

    /**
     * テンポラリフォルダ。
     */
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * Javaのファイルを一つだけビルドする。
     * @throws Exception テストに失敗した場合
     */
    @Test
    public void build_java() throws Exception {
        Set<String> entries = new HashSet<String>();

        FilePackager packager = new FilePackager(
                folder.newFolder(),
                Arrays.<ResourceRepository>asList());
        packager.initialize(environment);

        emit(packager, java("Hello"));
        build(entries, packager);
        assertThat(entries, hasItem("com/example/Hello.class"));
    }

    /**
     * リソースファイルを一つだけビルドする。
     * @throws Exception テストに失敗した場合
     */
    @Test
    public void build_resource() throws Exception {
        Set<String> entries = new HashSet<String>();

        FilePackager packager = new FilePackager(
                folder.newFolder(),
                Arrays.<ResourceRepository>asList());
        packager.initialize(environment);

        write(packager, "com.example", "messages.properties", "key=value");
        build(entries, packager);
        assertThat(entries, hasItem("com/example/messages.properties"));
    }

    /**
     * 複数のファイルをビルドする。
     * @throws Exception テストに失敗した場合
     */
    @Test
    public void build_mixed() throws Exception {
        Set<String> entries = new HashSet<String>();

        FilePackager packager = new FilePackager(
                folder.newFolder(),
                Arrays.<ResourceRepository>asList());
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
     * エラーが出るプログラムをビルドする。
     * @throws Exception テストに失敗した場合
     */
    @Test
    public void build_error() throws Exception {
        Set<String> entries = new HashSet<String>();

        FilePackager packager = new FilePackager(
                folder.newFolder(),
                Arrays.<ResourceRepository>asList());
        packager.initialize(environment);

        ModelFactory f = Models.getModelFactory();
        CompilationUnit cu = f.newCompilationUnit(
                f.newPackageDeclaration(Models.toName(f, "com.example")),
                Collections.<ImportDeclaration>emptyList(),
                Collections.singletonList(f.newClassDeclaration(
                        null,
                        new AttributeBuilder(f)
                            .Public()
                            .Private()
                            .toAttributes(),
                        f.newSimpleName("Hello"),
                        Collections.<TypeParameterDeclaration>emptyList(),
                        null,
                        Collections.<Type>emptyList(),
                        Collections.<TypeBodyDeclaration>emptyList())),
                Collections.<Comment>emptyList());

        emit(packager, cu);
        try {
            build(entries, packager);
            fail();
        } catch (IOException e) {
            assertThat(environment.hasError(), is(true));
        }
    }

    private void build(Set<String> entries, FilePackager packager)
            throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        packager.build(output);
        output.close();

        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
        JarInputStream jar = new JarInputStream(input);
        try {
            while (true) {
                JarEntry entry = jar.getNextJarEntry();
                if (entry == null) {
                    break;
                }
                entries.add(entry.getName());
            }
        } finally {
            jar.close();
        }
    }

    private void emit(Packager packager, CompilationUnit java) throws IOException {
        PrintWriter writer = packager.openWriter(java);
        try {
            Models.emit(java, writer);
        } finally {
            writer.close();
        }
    }

    private void write(Packager packager, String pkg, String rel, String value) throws IOException {
        ModelFactory f = Models.getModelFactory();
        OutputStream output = packager.openStream(
                pkg == null ? null : Models.toName(f, pkg),
                rel);
        try {
            output.write(value.getBytes("UTF-8"));
        } finally {
            output.close();
        }
    }

    private CompilationUnit java(String name) {
        ModelFactory f = Models.getModelFactory();
        return f.newCompilationUnit(
                f.newPackageDeclaration(Models.toName(f, "com.example")),
                Collections.<ImportDeclaration>emptyList(),
                Collections.singletonList(f.newClassDeclaration(
                        null,
                        new AttributeBuilder(f)
                            .Public()
                            .toAttributes(),
                        f.newSimpleName(name),
                        Collections.<TypeParameterDeclaration>emptyList(),
                        null,
                        Collections.<Type>emptyList(),
                        Collections.<TypeBodyDeclaration>emptyList())),
                Collections.<Comment>emptyList());
    }
}
