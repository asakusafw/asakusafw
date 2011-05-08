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
import java.net.URI;
import java.text.MessageFormat;
import java.util.Calendar;

import org.apache.poi.ss.usermodel.Cell;
import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelDefinition.Builder;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.DataModelScanner;
import com.asakusafw.testdriver.core.PropertyName;

/**
 * Extracts a model object from a row on Excel sheet.
 * @since 0.2.0
 */
class ExcelDataDriver {

    private final Engine engine;

    /**
     * Creates a new instance.
     * @param definition model definition
     * @param id source ID (nullable)
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public ExcelDataDriver(DataModelDefinition<?> definition, URI id) {
        if (definition == null) {
            throw new IllegalArgumentException("definition must not be null"); //$NON-NLS-1$
        }
        this.engine = new Engine(definition, id);
    }

    /**
     * Processes the target property and the corresponded cell.
     * @param propertyName target property name
     * @param cell target cell
     * @throws IOException if failed to process the target cell
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void process(PropertyName propertyName, Cell cell) throws IOException {
        if (propertyName == null) {
            throw new IllegalArgumentException("propertyName must not be null"); //$NON-NLS-1$
        }
        if (cell == null) {
            throw new IllegalArgumentException("cell must not be null"); //$NON-NLS-1$
        }
        if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
            return;
        }
        engine.scan(engine.definition, propertyName, cell);
    }

    /**
     * Returns the created model.
     * @return the created model object
     */
    public DataModelReflection getReflection() {
        return engine.builder.build();
    }

    private static class Engine extends DataModelScanner<Cell, IOException>{

        final DataModelDefinition<?> definition;

        final URI id;

        final Builder<?> builder;

        Engine(DataModelDefinition<?> definition, URI id) {
            assert definition != null;
            this.definition = definition;
            this.id = id;
            this.builder = definition.newReflection();
        }

        @Override
        public void booleanProperty(PropertyName name, Cell context) throws IOException {
            if (context.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
                builder.add(name, context.getBooleanCellValue());
            } else {
                String string = context.getStringCellValue();
                if (string.equalsIgnoreCase("true")) {
                    builder.add(name, true);
                } else if (string.equalsIgnoreCase("false")) {
                    builder.add(name, false);
                } else {
                    throw exception(name, context, "boolean");
                }
            }
        }

        @Override
        public void byteProperty(PropertyName name, Cell context) throws IOException {
            long value = toLong(name, context, "byte");
            checkRange(name, context, value, Byte.MIN_VALUE, Byte.MAX_VALUE);
            builder.add(name, (byte) value);
        }

        @Override
        public void shortProperty(PropertyName name, Cell context) throws IOException {
            long value = toLong(name, context, "short");
            checkRange(name, context, value, Short.MIN_VALUE, Short.MAX_VALUE);
            builder.add(name, (short) value);
        }

        @Override
        public void intProperty(PropertyName name, Cell context) throws IOException {
            long value = toLong(name, context, "int");
            checkRange(name, context, value, Integer.MIN_VALUE, Integer.MAX_VALUE);
            builder.add(name, (int) value);
        }

        @Override
        public void longProperty(PropertyName name, Cell context) throws IOException {
            long value = toLong(name, context, "long");
            builder.add(name, value);
        }

        private long toLong(PropertyName name, Cell cell, String expected) throws IOException {
            assert name != null;
            assert cell != null;
            assert expected != null;
            if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                return (long) cell.getNumericCellValue();
            } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                try {
                    return Long.parseLong(stripNumber(cell.getStringCellValue()));
                } catch (NumberFormatException e) {
                    // fall through
                }
            }
            throw exception(name, cell, expected);
        }

        private String stripNumber(String string) {
            if (string == null) {
                return null;
            }
            String trimmed = string.trim().replaceAll("[_,]", "");
            if (trimmed.startsWith("+")) {
                return trimmed.substring(1).trim();
            }
            return trimmed;
        }

        private void checkRange(PropertyName name, Cell cell, long value, int min, int max) throws IOException {
            assert name != null;
            assert cell != null;
            if (value < min || max < value) {
                throw new IOException(MessageFormat.format(
                        "{0}は{2}~{3}を指定してください: {1} (cell=({5}, {6}), id={4})",
                        name,
                        value,
                        min,
                        max,
                        id,
                        cell.getRowIndex() + 1,
                        cell.getColumnIndex() + 1));
            }
        }

        @Override
        public void floatProperty(PropertyName name, Cell context) throws IOException {
            double value = toDouble(name, context, "float");
            builder.add(name, (float) value);
        }

        @Override
        public void doubleProperty(PropertyName name, Cell context) throws IOException {
            double value = toDouble(name, context, "double");
            builder.add(name, value);
        }

        private double toDouble(PropertyName name, Cell cell, String expected) throws IOException {
            assert name != null;
            assert cell != null;
            assert expected != null;
            if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                return cell.getNumericCellValue();
            } else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
                try {
                    return Double.parseDouble(stripNumber(cell.getStringCellValue()));
                } catch (NumberFormatException e) {
                    // fall through
                }
            }
            throw exception(name, cell, expected);
        }

        @Override
        public void integerProperty(PropertyName name, Cell context) throws IOException {
            BigDecimal decimal = toDecimal(name, context, "integer");
            builder.add(name, decimal.toBigInteger());
        }

        @Override
        public void decimalProperty(PropertyName name, Cell context) throws IOException {
            BigDecimal decimal = toDecimal(name, context, "decimal");
            builder.add(name, decimal);
        }

        private BigDecimal toDecimal(PropertyName name, Cell context, String expected) throws IOException {
            assert name != null;
            assert context != null;
            assert expected != null;
            if (context.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                return new BigDecimal(context.getNumericCellValue());
            } else if (context.getCellType() == Cell.CELL_TYPE_STRING) {
                try {
                    return new BigDecimal(stripNumber(context.getStringCellValue()));
                } catch (NumberFormatException e) {
                    // fall through
                }
            }
            throw exception(name, context, expected);
        }

        @Override
        public void stringProperty(PropertyName name, Cell context) throws IOException {
            if (context.getCellType() != Cell.CELL_TYPE_STRING) {
                throw new IOException(MessageFormat.format(
                        "({0}, {1}, {2})の形式を判別できませんでした。先頭に ' を付けて文字列を表すようにしてください",
                        id,
                        context.getRowIndex() + 1,
                        context.getColumnIndex() + 1));
            }
            builder.add(name, context.getStringCellValue());
        }

        @Override
        public void calendarProperty(PropertyName name, Cell context) throws IOException {
            if (context.getCellType() != Cell.CELL_TYPE_NUMERIC) {
                throw exception(name, context, "date/datetime");
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(context.getDateCellValue());

            Calendar result = Calendar.getInstance();
            result.clear();
            result.set(
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DATE),
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    calendar.get(Calendar.SECOND));
            builder.add(name, result);
        }

        @Override
        public void anyProperty(PropertyName name, Cell context) throws IOException {
            throw exception(name, context, "(BLANK)");
        }

        private IOException exception(PropertyName name, Cell cell, String expected) {
            return new IOException(MessageFormat.format(
                    "{0}は{1}を指定してください: (cell=({3}, {4}), id={2})",
                    name,
                    expected,
                    id,
                    cell.getRowIndex() + 1,
                    cell.getColumnIndex() + 1));
        }
    }
}
