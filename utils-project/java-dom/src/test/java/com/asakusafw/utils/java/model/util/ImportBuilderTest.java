/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.utils.java.model.util;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import com.asakusafw.utils.java.model.syntax.ImportDeclaration;
import com.asakusafw.utils.java.model.syntax.ImportKind;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.PackageDeclaration;
import com.asakusafw.utils.java.model.syntax.QualifiedType;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.util.ImportBuilder.Strategy;

/**
 * Test for {@link ImportBuilder}.
 */
public class ImportBuilderTest {

    ModelFactory f = Models.getModelFactory();

    /**
     * プリミティブ型。
     */
    @Test
    public void primitive() {
        ImportBuilder importer = new ImportBuilder(f, null, Strategy.TOP_LEVEL);
        assertThat(importer.toType(int.class), is(type(int.class)));
        assertImported(importer);
    }

    /**
     * クラス型。
     */
    @Test
    public void aClass() {
        ImportBuilder importer = new ImportBuilder(f, null, Strategy.TOP_LEVEL);
        assertThat(importer.toType(Date.class), is(type(Date.class.getSimpleName())));
        assertImported(importer, Date.class);
    }

    /**
     * 配列型。
     */
    @Test
    public void array() {
        ImportBuilder importer = new ImportBuilder(f, null, Strategy.TOP_LEVEL);
        assertThat(importer.toType(int[].class), is(type(int[].class)));
        assertThat(importer.toType(Date[].class),
            is((Type) f.newArrayType(type("Date"))));
        assertImported(importer, Date.class);
    }

    /**
     * パラメータ化型。
     */
    @Test
    public void parameterized() {
        ImportBuilder importer = new ImportBuilder(f, null, Strategy.TOP_LEVEL);
        assertThat(
            importer.resolve(type(Map.class, Date.class, Calendar.class)),
            is(type("Map", "Date", "Calendar")));
        assertImported(importer, Map.class, Calendar.class, Date.class);
    }

    /**
     * 限定型。
     */
    @Test
    public void qualified() {
        QualifiedType type = f.newQualifiedType(
            type(List.class, Date.class),
            f.newSimpleName("Inner"));

        QualifiedType imported = f.newQualifiedType(
            type("List", "Date"),
            f.newSimpleName("Inner"));

        ImportBuilder importer = new ImportBuilder(f, null, Strategy.TOP_LEVEL);
        assertThat(importer.resolve(type), is((Type) imported));
        assertImported(importer, List.class, Date.class);
    }

    /**
     * java.lang。
     */
    @Test
    public void javaLang() {
        ImportBuilder importer = new ImportBuilder(f, null, Strategy.TOP_LEVEL);
        assertThat(importer.toType(String.class), is(type(String.class.getSimpleName())));
        assertImported(importer);
    }

    /**
     * 同一パッケージ。
     */
    @Test
    public void current() {
        PackageDeclaration pkg = f.newPackageDeclaration(
            Models.toName(f, "com.example"));
        ImportBuilder importer = new ImportBuilder(f, pkg, Strategy.TOP_LEVEL);
        assertThat(importer.resolve(type("com.example.Test")), is(type("Test")));
        assertImported(importer);
    }

    /**
     * 内部クラス型 (トップレベルのみインポート)。
     */
    @Test
    public void enclosing() {
        ImportBuilder importer = new ImportBuilder(f, null, Strategy.TOP_LEVEL);
        assertThat(importer.toType(Map.Entry.class), is(type("Map.Entry")));
        assertImported(importer, Map.class);
    }

    /**
     * 内部クラス型 (直接インポート)。
     */
    @Test
    public void enclosing_just() {
        ImportBuilder importer = new ImportBuilder(f, null, Strategy.ENCLOSING);
        assertThat(importer.toType(Map.Entry.class), is(type("Entry")));
        assertImported(importer, Map.Entry.class);
    }

    /**
     * デフォルトパッケージ。
     */
    @Test
    public void defaultPackage() {
        ImportBuilder importer = new ImportBuilder(f, null, Strategy.TOP_LEVEL);
        assertThat(
            importer.resolve(type("Example")),
            is(type("Example")));
        assertThat(
            importer.resolve(type("Outer.Inner")),
            is(type("Outer.Inner")));
        assertImportedNames(importer);
    }

    /**
     * 重複。
     */
    @Test
    public void duplicate() {
        ImportBuilder importer = new ImportBuilder(f, null, Strategy.TOP_LEVEL);
        assertThat(importer.resolve(type(Date.class)), is(type("Date")));
        assertThat(importer.resolve(type(Date.class)), is(type("Date")));
        assertThat(importer.resolve(type(Date.class)), is(type("Date")));
        assertImported(importer, Date.class);
    }

    /**
     * 衝突。
     */
    @Test
    public void conflict() {
        ImportBuilder importer = new ImportBuilder(f, null, Strategy.TOP_LEVEL);
        assertThat(importer.resolve(type(Date.class)), is(type("Date")));
        assertThat(importer.resolve(type(java.sql.Date.class)), is(type(java.sql.Date.class)));
        assertImported(importer, Date.class);
    }

    /**
     * 衝突。
     */
    @Test
    public void conflictInDefaultPackage() {
        ImportBuilder importer = new ImportBuilder(f, null, Strategy.TOP_LEVEL);
        assertThat(importer.resolve(type("Example")), is(type("Example")));
        assertThat(importer.resolve(type("com.example.Example")), is(type("com.example.Example")));
        assertImported(importer);
    }

    private Type type(
            java.lang.reflect.Type type,
            java.lang.reflect.Type... arguments) {
        Type result = Models.toType(f, type);
        if (arguments.length != 0) {
            List<Type> args = new ArrayList<Type>();
            for (java.lang.reflect.Type t : arguments) {
                args.add(Models.toType(f, t));
            }
            result = f.newParameterizedType(result, args);
        }
        return result;
    }

    private Type type(
            String name,
            String... arguments) {
        Type result = f.newNamedType(Models.toName(f, name));
        if (arguments.length != 0) {
            List<Type> args = new ArrayList<Type>();
            for (String t : arguments) {
                args.add(f.newNamedType(Models.toName(f, t)));
            }
            result = f.newParameterizedType(result, args);
        }
        return result;
    }

    private void assertImported(ImportBuilder importer, Class<?>... types) {
        String[] expect = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            expect[i] = types[i].getName().replace('$', '.');
        }
        assertImportedNames(importer, expect);
    }

    private void assertImportedNames(ImportBuilder importer, String... types) {
        List<ImportDeclaration> decls = importer.toImportDeclarations();
        Set<String> actual = new TreeSet<String>();
        for (ImportDeclaration d : decls) {
            assertThat(d.getImportKind(), is(ImportKind.SINGLE_TYPE));
            String name = d.getName().toNameString();
            assertThat(actual, not(hasItem(name)));
            actual.add(name);
        }

        Set<String> expect = new TreeSet<String>();
        Collections.addAll(expect, types);

        assertThat(actual, is(expect));
    }
}
