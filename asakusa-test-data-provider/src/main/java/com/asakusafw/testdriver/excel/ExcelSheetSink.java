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

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.DataModelScanner;
import com.asakusafw.testdriver.core.DataModelSink;
import com.asakusafw.testdriver.core.PropertyName;

/**
 * An implementation of {@link DataModelSink}.
 * @since 0.2.3
 */
public class ExcelSheetSink implements DataModelSink {

    static final Logger LOG = LoggerFactory.getLogger(ExcelSheetSink.class);

    private final Sheet sheet;

    private final WorkbookInfo info;

    private final Engine engine;

    private int rowIndex;

    /**
     * Creates a new instance.
     * @param definition the data model definition
     * @param sheet target sheet
     * @param maxColumns the count of max columns
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public ExcelSheetSink(DataModelDefinition<?> definition, Sheet sheet, int maxColumns) {
        if (definition == null) {
            throw new IllegalArgumentException("definition must not be null"); //$NON-NLS-1$
        }
        if (sheet == null) {
            throw new IllegalArgumentException("sheet must not be null"); //$NON-NLS-1$
        }
        this.sheet = sheet;
        this.info = new WorkbookInfo(sheet.getWorkbook());
        this.engine = new Engine(definition, info, maxColumns);
        engine.createHeaderRow(sheet.createRow(0));
        this.rowIndex = 1;
    }

    @Override
    public void put(DataModelReflection model) {
        if (model == null) {
            throw new IllegalArgumentException("model must not be null"); //$NON-NLS-1$
        }
        Row row = sheet.createRow(rowIndex++);
        engine.process(model, row);
    }

    @Override
    public void close() throws IOException {
        return;
    }

    private static class Engine extends DataModelScanner<Context, RuntimeException> {

        private final DataModelDefinition<?> definition;

        private final WorkbookInfo info;

        private final List<PropertyName> properties;

        Engine(DataModelDefinition<?> definition, WorkbookInfo info, int maxColumns) {
            assert definition != null;
            assert info != null;
            this.definition = definition;
            this.info = info;
            List<PropertyName> props = new ArrayList<PropertyName>(definition.getProperties());
            if (props.size() > maxColumns) {
                props = props.subList(0, maxColumns);
            }
            this.properties = props;
        }

        void createHeaderRow(Row row) {
            assert row != null;
            int columnIndex = 0;
            for (PropertyName name : properties) {
                Cell cell = row.createCell(columnIndex++);
                cell.setCellStyle(info.titleStyle);
                cell.setCellValue(name.toString());
            }
        }

        void process(DataModelReflection model, Row row) {
            assert model != null;
            assert row != null;
            Context context = new Context(model, row, info);
            for (PropertyName name : properties) {
                scan(definition, name, context);
            }
        }

        @Override
        public void booleanProperty(PropertyName name, Context context) {
            Cell cell = context.nextCell();
            Boolean value = (Boolean) context.getValue(name);
            if (value != null) {
                cell.setCellValue(value);
            }
        }

        @Override
        public void byteProperty(PropertyName name, Context context) {
            Cell cell = context.nextCell();
            Byte value = (Byte) context.getValue(name);
            if (value != null) {
                cell.setCellValue(value);
            }
        }

        @Override
        public void shortProperty(PropertyName name, Context context) {
            Cell cell = context.nextCell();
            Short value = (Short) context.getValue(name);
            if (value != null) {
                cell.setCellValue(value);
            }
        }

        @Override
        public void intProperty(PropertyName name, Context context) {
            Cell cell = context.nextCell();
            Integer value = (Integer) context.getValue(name);
            if (value != null) {
                cell.setCellValue(value);
            }
        }

        @Override
        public void longProperty(PropertyName name, Context context) {
            Cell cell = context.nextCell();
            Long value = (Long) context.getValue(name);
            if (value != null) {
                cell.setCellValue(value);
            }
        }

        @Override
        public void integerProperty(PropertyName name, Context context) {
            Cell cell = context.nextCell();
            BigInteger value = (BigInteger) context.getValue(name);
            if (value != null) {
                cell.setCellValue(value.toString());
            }
        }

        @Override
        public void floatProperty(PropertyName name, Context context) {
            Cell cell = context.nextCell();
            Float value = (Float) context.getValue(name);
            if (value != null) {
                cell.setCellValue(value);
            }
        }

        @Override
        public void doubleProperty(PropertyName name, Context context) {
            Cell cell = context.nextCell();
            Double value = (Double) context.getValue(name);
            if (value != null) {
                cell.setCellValue(value);
            }
        }

        @Override
        public void decimalProperty(PropertyName name, Context context) {
            Cell cell = context.nextCell();
            BigDecimal value = (BigDecimal) context.getValue(name);
            if (value != null) {
                cell.setCellValue(value.toPlainString());
            }
        }

        @Override
        public void stringProperty(PropertyName name, Context context) {
            Cell cell = context.nextCell();
            String value = (String) context.getValue(name);
            if (value != null) {
                cell.setCellValue(value);
            }
        }

        @Override
        public void dateProperty(PropertyName name, Context context) {
            Cell cell = context.nextCell();
            cell.setCellStyle(info.dataStyle);
            Calendar value = (Calendar) context.getValue(name);
            if (value != null) {
                cell.setCellValue(value);
            }
        }

        @Override
        public void timeProperty(PropertyName name, Context context) {
            Cell cell = context.nextCell();
            cell.setCellStyle(info.timeDataStyle);
            Calendar value = (Calendar) context.getValue(name);
            if (value != null) {
                cell.setCellValue(value);
            }
        }

        @Override
        public void datetimeProperty(PropertyName name, Context context) {
            Cell cell = context.nextCell();
            cell.setCellStyle(info.datetimeDataStyle);
            Calendar value = (Calendar) context.getValue(name);
            if (value != null) {
                cell.setCellValue(value);
            }
        }

        @Override
        public void anyProperty(PropertyName name, Context context) {
            Cell cell = context.nextCell();
            Object value = context.getValue(name);
            if (value != null) {
                cell.setCellValue(value.toString());
            }
        }
    }

    private static class Context {

        private final DataModelReflection model;

        private final Row row;

        private final WorkbookInfo info;

        private int column;

        Context(DataModelReflection model, Row row, WorkbookInfo info) {
            assert model != null;
            assert row != null;
            assert info != null;
            this.model = model;
            this.row = row;
            this.info = info;
            this.column = 0;
        }

        public Cell nextCell() {
            Cell cell = row.createCell(column++);
            cell.setCellStyle(info.dataStyle);
            return cell;
        }

        public Object getValue(PropertyName name) {
            assert name != null;
            return model.getValue(name);
        }
    }
}
