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
import java.net.URI;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.DataModelSource;
import com.asakusafw.testdriver.core.PropertyName;

/**
 * {@link DataModelSource} from Excel Sheet.
 * @since 0.2.0
 */
public class ExcelSheetDataModelSource implements DataModelSource {

    private final DataModelDefinition<?> definition;

    private final URI id;

    private final Sheet sheet;

    private final Map<PropertyName, Integer> names;

    private int nextRowNumber;

    /**
     * Creates a new instance.
     * @param definition the model definition
     * @param id sheet ID (nullable)
     * @param sheet target cheet
     * @throws IOException if failed to extract property info from the sheet
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public ExcelSheetDataModelSource(DataModelDefinition<?> definition, URI id, Sheet sheet) throws IOException {
        if (definition == null) {
            throw new IllegalArgumentException("definition must not be null"); //$NON-NLS-1$
        }
        if (sheet == null) {
            throw new IllegalArgumentException("sheet must not be null"); //$NON-NLS-1$
        }
        this.definition = definition;
        this.id = id;
        this.sheet = sheet;
        this.names = extractProperties();
    }

    private Map<PropertyName, Integer> extractProperties() throws IOException {
        // first row must be property names
        Row row = sheet.getRow(0);
        if (row == null) {
            throw new IOException(MessageFormat.format(
                    "最初の行はプロパティ名の一覧でなければなりません: (id={0})",
                    id));
        }
        nextRowNumber = 1;
        Map<PropertyName, Integer> results = new LinkedHashMap<PropertyName, Integer>();
        for (Iterator<Cell> iter = row.cellIterator(); iter.hasNext(); ) {
            Cell cell = iter.next();
            int type = cell.getCellType();
            if (type == Cell.CELL_TYPE_BLANK) {
                continue;
            }
            if (type != Cell.CELL_TYPE_STRING || cell.getStringCellValue().isEmpty()) {
                throw new IOException(MessageFormat.format(
                        "最初の行はプロパティ名を文字列で指定してください: (id={0}, column={1})",
                        id,
                        cell.getColumnIndex() + 1));
            }
            String name = cell.getStringCellValue();
            PropertyName property = toPropertyName(cell, name);
            if (definition.getType(property) == null) {
                throw new IOException(MessageFormat.format(
                        "{0}にプロパティ\"{1}\"は定義されていません: (id={2}, column={3})",
                        definition.getModelClass().getName(),
                        property,
                        id,
                        cell.getColumnIndex() + 1));
            }
            names.put(property, cell.getColumnIndex());
        }
        return results;
    }

    private PropertyName toPropertyName(Cell cell, String name) {
        assert cell != null;
        assert name != null;
        String[] words = name.split("(_|-)+");
        return PropertyName.newInstance(words);
    }

    @Override
    public DataModelReflection next() throws IOException {
        Row row = sheet.getRow(nextRowNumber);
        if (row == null) {
            return null;
        }
        ExcelCellDriver driver = new ExcelCellDriver(definition, id);
        for (Map.Entry<PropertyName, Integer> entry : names.entrySet()) {
            Cell cell = row.getCell(entry.getValue(), Row.CREATE_NULL_AS_BLANK);
            int type = cell.getCellType();
            if (type == Cell.CELL_TYPE_FORMULA || type == Cell.CELL_TYPE_ERROR) {
                throw new IOException(MessageFormat.format(
                        "セルに数式を利用できません: (pos=({1}, {2}), id={0})",
                        id,
                        row.getRowNum() + 1,
                        cell.getColumnIndex() + 1));
            }
            driver.process(entry.getKey(), cell);
        }
        nextRowNumber++;
        return driver.getReflection();
    }

    @Override
    public void close() throws IOException {
        return;
    }
}
