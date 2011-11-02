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
package com.asakusafw.testdriver.excel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelSink;
import com.asakusafw.testdriver.core.DataModelSinkFactory;
import com.asakusafw.testdriver.core.TestContext;

/**
 * An implementation of {@link DataModelSinkFactory} to create an Excel sheet.
 * @since 0.2.3
 */
public class ExcelSheetSinkFactory extends DataModelSinkFactory {

    /**
     *
     */
    private static final int MAX_COLUMN_SIZE = 255;

    static final Logger LOG = LoggerFactory.getLogger(ExcelSheetSinkFactory.class);

    final File output;

    /**
     * Creates a new instance.
     * @param output output target file
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public ExcelSheetSinkFactory(File output) {
        if (output == null) {
            throw new IllegalArgumentException("output must not be null"); //$NON-NLS-1$
        }
        this.output = output;
    }

    /**
     * Creates a new instance.
     * @param output output target file
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public ExcelSheetSinkFactory(String output) {
        if (output == null) {
            throw new IllegalArgumentException("output must not be null"); //$NON-NLS-1$
        }
        this.output = new File(output);
    }

    @Override
    public <T> DataModelSink createSink(DataModelDefinition<T> definition, TestContext context) throws IOException {
        if (definition == null) {
            throw new IllegalArgumentException("definition must not be null"); //$NON-NLS-1$
        }
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        if (definition.getProperties().size() > MAX_COLUMN_SIZE) {
            LOG.warn("The data model \"{}\" has > {} properties, so several properties will be omitted to generate {}.",
                    new Object[] {
                        definition.getModelClass().getName(),
                        MAX_COLUMN_SIZE,
                        output,
                    }
            );
        }
        File parent = output.getParentFile();
        if (parent != null && parent.isDirectory() == false && parent.mkdirs() == false) {
            throw new IOException(MessageFormat.format(
                    "Failed to create an output directory for {0}",
                    output));
        }
        final Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("results");
        return new ExcelSheetSink(definition, sheet, MAX_COLUMN_SIZE) {
            private boolean closed = false;
            @Override
            public void close() throws IOException {
                if (closed) {
                    return;
                }
                closed = true;
                LOG.info("Generating job result into {}", output);
                OutputStream stream = new FileOutputStream(output);
                try {
                    workbook.write(stream);
                } finally {
                    stream.close();
                }
            }
        };
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}({1})",
                ExcelSheetSink.class.getSimpleName(),
                output);
    }
}
