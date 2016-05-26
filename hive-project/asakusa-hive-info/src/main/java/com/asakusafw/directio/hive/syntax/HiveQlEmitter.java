/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.directio.hive.syntax;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import com.asakusafw.directio.hive.info.BuiltinStorageFormatInfo;
import com.asakusafw.directio.hive.info.ColumnInfo;
import com.asakusafw.directio.hive.info.CustomStorageFormatInfo;
import com.asakusafw.directio.hive.info.DelimitedRowFormatInfo;
import com.asakusafw.directio.hive.info.RowFormatInfo;
import com.asakusafw.directio.hive.info.SerdeRowFormatInfo;
import com.asakusafw.directio.hive.info.StorageFormatInfo;
import com.asakusafw.directio.hive.info.TableInfo;

/**
 * Emits Hive QL.
 * @since 0.8.1
 */
public final class HiveQlEmitter {

    private HiveQlEmitter() {
        return;
    }

    /**
     * Emits {@code CREATE TABLE} statement into the target writer.
     * @param statement the source statement
     * @param writer the target writer
     * @throws IOException if failed by I/O error
     */
    public static void emit(HiveCreateTable statement, Appendable writer) throws IOException {
        TableInfo table = statement.getTableInfo();
        Context c = new Context(writer);

        emitCreateTableHead(c, statement);

        // TODO PARTITIONED BY
        // TODO CLUSTERED BY
        // TODO SKEWED BY

        RowFormatInfo rowFormat = table.getRowFormat();
        if (rowFormat != null) {
            c.tokens("ROW", "FORMAT");
            switch (rowFormat.getFormatKind()) {
            case DELIMITED:
                emitRowFormat(c, (DelimitedRowFormatInfo) rowFormat);
                break;
            case SERDE:
                emitRowFormat(c, (SerdeRowFormatInfo) rowFormat);
                break;
            default:
                throw new AssertionError(rowFormat.getFormatKind());
            }
            c.newLine();
        }
        StorageFormatInfo storageFormat = table.getStorageFormat();
        if (storageFormat != null) {
            c.tokens("STORED", "AS"); //$NON-NLS-1$ //$NON-NLS-2$
            switch (storageFormat.getFormatKind().getCategory()) {
            case BUILTIN:
                emitStorageFormat(c, (BuiltinStorageFormatInfo) storageFormat);
                break;
            case CUSTOM:
                emitStorageFormat(c, (CustomStorageFormatInfo) storageFormat);
                break;
            default:
                throw new AssertionError(storageFormat);
            }
            c.newLine();
        }
        // or TODO STORED BY

        if (statement.getLocation() != null) {
            c.token("LOCATION"); //$NON-NLS-1$
            c.string(statement.getLocation());
            c.newLine();
        }
        if (table.getProperties().isEmpty() == false) {
            c.token("TBLPROPERTIES"); //$NON-NLS-1$
            c.token("("); //$NON-NLS-1$
            c.newLine();
            c.indent(+1);
            emitProperties(c, table.getProperties());
            c.indent(-1);
            c.token(")"); //$NON-NLS-1$
            c.newLine();
        }
    }

    private static void emitRowFormat(Context c, DelimitedRowFormatInfo info) throws IOException {
        c.token("DELIMITED");
        c.newLine();
        if (info.getFieldSeparator() != null) {
            c.indent(+1);
            c.tokens("FIELDS", "TERMINATED", "BY");
            c.string(info.getFieldSeparator());
            if (info.getFieldSeparatorEscape() != null) {
                c.tokens("ESCAPED", "BY");
                c.string(info.getFieldSeparatorEscape());
            }
            c.newLine();
            c.indent(-1);
        }
        if (info.getCollectionItemSeparator() != null) {
            c.indent(+1);
            c.tokens("COLLECTION", "ITEMS", "TERMINATED", "BY");
            c.string(info.getCollectionItemSeparator());
            c.newLine();
            c.indent(-1);
        }
        if (info.getMapPairSeparator() != null) {
            c.indent(+1);
            c.tokens("MAP", "KEYS", "TERMINATED", "BY");
            c.string(info.getMapPairSeparator());
            c.newLine();
            c.indent(-1);
        }
        if (info.getLineSeparator() != null) {
            c.indent(+1);
            c.tokens("LINES", "TERMINATED", "BY");
            c.string(info.getLineSeparator());
            c.newLine();
            c.indent(-1);
        }
        if (info.getNullSymbol() != null) {
            c.indent(+1);
            c.tokens("NULL", "DEFINED", "AS");
            c.string(info.getNullSymbol());
            c.newLine();
            c.indent(-1);
        }
    }

    private static void emitRowFormat(Context c, SerdeRowFormatInfo info) throws IOException {
        c.token("SERDE");
        c.string(info.getName());
        if (info.getProperties().isEmpty() == false) {
            c.tokens("WITH", "SERDEPROPERTIES");
            c.token("("); //$NON-NLS-1$
            c.newLine();
            c.indent(+1);
            emitProperties(c, info.getProperties());
            c.indent(-1);
            c.token(")"); //$NON-NLS-1$
        }
    }

    private static void emitStorageFormat(Context c, BuiltinStorageFormatInfo info) throws IOException {
        c.token(info.getFormatKind().name());
    }

    private static void emitStorageFormat(Context c, CustomStorageFormatInfo info) throws IOException {
        c.token("INPUTFORMAT"); //$NON-NLS-1$
        c.string(info.getInputFormatClass());
        c.token("OUTPUTFORMAT"); //$NON-NLS-1$
        c.string(info.getOutputFormatClass());
    }

    private static void emitCreateTableHead(Context c, HiveCreateTable statement) throws IOException {
        c.token("CREATE"); //$NON-NLS-1$
        if (statement.isExternal()) {
            c.token("EXTERNAL"); //$NON-NLS-1$
        }
        c.token("TABLE"); //$NON-NLS-1$
        if (statement.isSkipPresentTable()) {
            c.tokens("IF", "NOT", "EXISTS"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        TableInfo table = statement.getTableInfo();
        if (statement.getDatabaseName() == null) {
            c.name(table.getName());
        } else {
            c.token(String.format(
                    "%s.%s", //$NON-NLS-1$
                    HiveSyntax.quoteIdentifier(statement.getDatabaseName()),
                    HiveSyntax.quoteIdentifier(table.getName())));
        }

        c.token("("); //$NON-NLS-1$
        c.newLine();
        c.indent(+1);
        ColumnInfo[] columns = table.getColumns().toArray(new ColumnInfo[table.getColumns().size()]);
        for (int i = 0; i < columns.length; i++) {
            ColumnInfo column = columns[i];
            c.name(column.getName());
            c.token(column.getType().getQualifiedName());
            if (column.getComment() != null) {
                c.token("COMMENT"); //$NON-NLS-1$
                c.string(column.getComment());
            }
            if (i != columns.length - 1) {
                c.token(","); //$NON-NLS-1$
            }
            c.newLine();
        }
        c.indent(-1);
        c.token(")"); //$NON-NLS-1$
        c.newLine();

        if (table.getComment() != null) {
            c.token("COMMENT"); //$NON-NLS-1$
            c.string(table.getComment());
            c.newLine();
        }
    }

    private static void emitProperties(Context c, Map<String, String> properties) throws IOException {
        if (properties.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<String, String>> iter = properties.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, String> entry = iter.next();
            c.string(entry.getKey());
            c.token("="); //$NON-NLS-1$
            c.string(entry.getValue());
            if (iter.hasNext()) {
                c.token(","); //$NON-NLS-1$
            }
            c.newLine();
        }
    }

    private static final class Context {

        private static final int INDENT_WIDTH = 4;

        private final Appendable writer;

        private int indent = 0;

        private boolean headOfLine = true;

        private boolean newLine = false;

        /**
         * Creates a new instance.
         * @param writer the target writer
         */
        Context(Appendable writer) {
            this.writer = writer;
        }

        public void name(String name) throws IOException {
            pad();
            writer.append(HiveSyntax.quoteIdentifier(name));
        }

        public void token(String token) throws IOException {
            pad();
            writer.append(token);
        }

        public void tokens(String... tokens) throws IOException {
            for (String string : tokens) {
                token(string);
            }
        }

        public void string(String text) throws IOException {
            pad();
            writer.append(HiveSyntax.quoteLiteral('\'', text));
        }

        public void newLine() {
            newLine = true;
        }

        public void indent(int delta) {
            assert newLine || headOfLine;
            this.indent += delta;
        }

        private void pad() throws IOException {
            if (newLine) {
                newLine = false;
                writer.append('\n');
                headOfLine = true;
            }
            if (headOfLine) {
                for (int i = 0, n = indent * INDENT_WIDTH; i < n; i++) {
                    writer.append(' ');
                }
                headOfLine = false;
            } else {
                writer.append(' ');
            }
        }
    }
}
