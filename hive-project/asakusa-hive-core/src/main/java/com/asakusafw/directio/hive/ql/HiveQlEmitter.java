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
package com.asakusafw.directio.hive.ql;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import com.asakusafw.directio.hive.common.DelimitedRowFormatInfo;
import com.asakusafw.directio.hive.common.HiveFieldInfo;
import com.asakusafw.directio.hive.common.HiveTableInfo;
import com.asakusafw.directio.hive.common.RowFormatInfo;

/**
 * Emits Hive QL.
 * @since 0.7.0
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
        HiveTableInfo table = statement.getTableInfo();
        Context c = new Context(writer);

        emitCreateTableHead(c, statement);

        // TODO PARTITIONED BY
        // TODO CLUSTERED BY
        // TODO SKEWED BY

        RowFormatInfo rowFormat = table.getRowFormat();
        if (rowFormat != null) {
            switch (rowFormat.getKind()) {
            case DELIMITED:
                emitDelimitedRowFormat(c, (DelimitedRowFormatInfo) table.getRowFormat());
                break;
            case SERDE:
                throw new UnsupportedOperationException("ROW FORMAT SERDE"); //$NON-NLS-1$
            default:
                throw new AssertionError(rowFormat.getKind());
            }
        }
        if (table.getFormatName() != null) {
            c.tokens("STORED", "AS"); //$NON-NLS-1$ //$NON-NLS-2$
            c.token(table.getFormatName());
            c.newLine();
        }
        // or TODO STORED BY

        if (statement.getLocation() != null) {
            c.token("LOCATION"); //$NON-NLS-1$
            c.string(statement.getLocation());
            c.newLine();
        }
        if (table.getTableProperties().isEmpty() == false) {
            c.token("TBLPROPERTIES"); //$NON-NLS-1$
            c.token("("); //$NON-NLS-1$
            c.newLine();
            c.indent(+1);
            emitProperties(c, table.getTableProperties());
            c.indent(-1);
            c.token(")"); //$NON-NLS-1$
            c.newLine();
        }
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

        HiveTableInfo table = statement.getTableInfo();
        if (statement.getDatabaseName() == null) {
            c.name(table.getTableName());
        } else {
            c.name(String.format("%s.%s", statement.getDatabaseName(), table.getTableName())); //$NON-NLS-1$
        }

        c.token("("); //$NON-NLS-1$
        c.newLine();
        c.indent(+1);
        HiveFieldInfo[] fields = table.getFields().toArray(new HiveFieldInfo[table.getFields().size()]);
        for (int i = 0; i < fields.length; i++) {
            HiveFieldInfo field = fields[i];
            c.name(field.getFieldName());
            c.token(field.getFieldTypeInfo().getQualifiedName());
            if (field.getFieldComment() != null) {
                c.token("COMMENT"); //$NON-NLS-1$
                c.string(field.getFieldComment());
            }
            if (i != fields.length - 1) {
                c.token(","); //$NON-NLS-1$
            }
            c.newLine();
        }
        c.indent(-1);
        c.token(")"); //$NON-NLS-1$
        c.newLine();

        if (table.getTableComment() != null) {
            c.token("COMMENT"); //$NON-NLS-1$
            c.string(table.getTableComment());
            c.newLine();
        }
    }

    private static void emitDelimitedRowFormat(Context c, DelimitedRowFormatInfo info) throws IOException {
        c.tokens("ROW", "FORMAT", "DELIMITED"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        c.newLine();
        c.indent(+1);
        if (info.getFieldsTerminatedBy() != null) {
            c.tokens("FIELDS", "TERMINATED", "BY"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            c.string(info.getFieldsTerminatedBy());
            c.newLine();
            if (info.getEscapedBy() != null) {
                c.tokens("ESCAPED", "BY"); //$NON-NLS-1$ //$NON-NLS-2$
                c.string(info.getEscapedBy());
                c.newLine();
            }
        }
        if (info.getCollectionItemsTerminatedBy() != null) {
            c.tokens("COLLECTION", "ITEMS", "TERMINATED", "BY"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            c.string(info.getCollectionItemsTerminatedBy());
            c.newLine();
        }
        if (info.getMapKeysTerminatedBy() != null) {
            c.tokens("MAP", "KEYS", "TERMINATED", "BY"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            c.string(info.getMapKeysTerminatedBy());
            c.newLine();
        }
        if (info.getLinesTerminatedBy() != null) {
            c.tokens("LINES", "TERMINATED", "BY"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            c.string(info.getLinesTerminatedBy());
            c.newLine();
        }
        if (info.getNullDefinedAs() != null) {
            c.tokens("NULL", "DEFINED", "AS"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            c.string(info.getNullDefinedAs());
            c.newLine();
        }
        c.indent(-1);
        c.newLine();
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
        public Context(Appendable writer) {
            this.writer = writer;
        }

        public void name(String name) throws IOException {
            pad();
            // TODO escape identifier
            writer.append(name);
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
            StringBuilder buf = new StringBuilder();
            buf.append('\'');
            for (int i = 0, n = text.length(); i < n; i++) {
                char c = text.charAt(i);
                if (c == '\\' || c == '\'') {
                    buf.append('\\');
                    buf.append(c);
                } else if (Character.isISOControl(c)) {
                    buf.append(String.format("\\%03o", (int) c)); //$NON-NLS-1$
                } else {
                    buf.append(c);
                }
            }
            buf.append('\'');
            writer.append(buf);
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
