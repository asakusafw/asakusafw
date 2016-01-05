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
package com.asakusafw.dmdl.thundergate.emitter;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;

/**
 * Generates ThunderGate record lock DDL file.
 * @since 0.6.1
 */
public class RecordLockDdlEmitter {

    private static final Charset ENCODING = Charset.forName("UTF-8"); //$NON-NLS-1$

    private static final String TEMPLATE_FILE = "record-lock-ddl.sql.template"; //$NON-NLS-1$

    private static final String TEMPLATE_TABLE_NAME_PLACEHOLDER = "@TABLE_NAME@"; //$NON-NLS-1$

    private static final String TEMPLATE_CONTENTS;
    static {
        InputStream in = RecordLockDdlEmitter.class.getResourceAsStream(TEMPLATE_FILE);
        try {
            try {
                TEMPLATE_CONTENTS = IOUtils.toString(in, ENCODING);
            } finally {
                in.close();
            }
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    private final Set<String> tableNames = new TreeSet<String>();

    /**
     * Adds a table to this generator.
     * @param tableName the target table name
     */
    public void addTable(String tableName) {
        this.tableNames.add(tableName);
    }

    /**
     * Appends DDL contents into the target writer.
     * @param appendable the target writer
     * @throws IOException if failed to append to the writer by I/O error
     */
    public void appendTo(Appendable appendable) throws IOException {
        if (appendable == null) {
            throw new IllegalArgumentException("appendable must not be null"); //$NON-NLS-1$
        }
        for (String tableName : tableNames) {
            String ddl = TEMPLATE_CONTENTS.replaceAll(TEMPLATE_TABLE_NAME_PLACEHOLDER, tableName);
            appendable.append(ddl);
        }
    }

    /**
     * Appends DDL contents into the target stream.
     * @param stream the target stream
     * @throws IOException if failed to append to the stream by I/O error
     */
    public void appendTo(OutputStream stream) throws IOException {
        Writer writer = new OutputStreamWriter(stream, RecordLockDdlEmitter.ENCODING);
        appendTo(writer);
        writer.flush();
    }
}
