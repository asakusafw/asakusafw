/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.directio.hive.tools.cli;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.hadoop.hive.ql.parse.ASTNode;
import org.apache.hadoop.hive.ql.parse.ParseDriver;
import org.apache.hadoop.hive.ql.parse.ParseException;
import org.junit.Rule;
import org.junit.rules.ExternalResource;
import org.junit.rules.TemporaryFolder;

import com.asakusafw.info.hive.TableInfo;

/**
 * Base class for {@link GenerateCreateTableTask} related classes.
 */
public class GenerateCeateTableTestRoot {

    /**
     * temporary folder for testing.
     */
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    /**
     * Resets test delegate objects.
     */
    @Rule
    public final ExternalResource delegateCleaner = new ExternalResource() {
            @Override
            protected void before() {
                clear();
            }
            @Override
            protected void after() {
                clear();
            }
            private void clear() {
                Info1.schema = null;
                Info2.schema = null;
            }
        };

    /**
     * Collect output statements.
     * @param target the target file
     * @return the statements
     * @throws IOException if failed
     */
    protected List<String> collectStatements(File target) throws IOException {
        StringBuilder buf = new StringBuilder();
        try (Scanner scanner = new Scanner(target, "UTF-8")) {
            while (scanner.hasNextLine()) {
                buf.append(scanner.nextLine());
                buf.append('\n');
            }
        }
        List<String> results = new ArrayList<>();
        for (String ql : buf.toString().split(";")) {
            String s = ql.trim();
            if (s.isEmpty() == false) {
                results.add(s);
            }
        }
        return results;
    }

    /**
     * Verifies HiveQL.
     * @param ql the statement
     * @return the AST
     * @throws IOException if failed
     * @throws ParseException if failed
     */
    protected ASTNode verify(String ql) throws IOException, ParseException {
        ASTNode node = new ParseDriver().parse(ql);
        return node;
    }

    /**
     * Mock {@link TableInfo} provider for testing.
     */
    public static class Info1 implements TableInfo.Provider {

        /**
         * Table schema.
         */
        protected static TableInfo schema;

        @Override
        public TableInfo getSchema() {
            return schema;
        }
    }

    /**
     * Mock {@link TableInfo} provider for testing.
     */
    public static class Info2 implements TableInfo.Provider {

        /**
         * Table schema.
         */
        protected static TableInfo schema;

        @Override
        public TableInfo getSchema() {
            return schema;
        }
    }

    /**
     * Invalid {@link TableInfo} provider for testing.
     */
    public static class InfoInvalid implements TableInfo.Provider {

        @Override
        public TableInfo getSchema() {
            return null;
        }
    }
}