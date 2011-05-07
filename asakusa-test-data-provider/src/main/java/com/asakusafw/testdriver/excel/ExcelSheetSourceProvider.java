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
import org.apache.poi.ss.usermodel.Sheet;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelSource;
import com.asakusafw.testdriver.core.SourceProvider;

/**
 * Provides {@link DataModelSource} from Excel Sheet.
 * This accepts URI:
 * <ul>
 * <li> which is also a valid URL to obtain an Excel workbook, </li>
 * <li> whose "path" segment ends with ".xls", or </li>
 * <li> whose "fragment" is "#:" + 0-origin sheet number or "#" + sheet name </li>
 * </ul>
 * @since 0.2.0
 */
public class ExcelSheetSourceProvider implements SourceProvider {

    @Override
    public <T> DataModelSource open(DataModelDefinition<T> definition, URI source) throws IOException {
        Sheet sheet = Util.extract(source);
        if (sheet == null) {
            return null;
        }
        return new ExcelSheetDataModelSource(definition, source, sheet);
    }
}
