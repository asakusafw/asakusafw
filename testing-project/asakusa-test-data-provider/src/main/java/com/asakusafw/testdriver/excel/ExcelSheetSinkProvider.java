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
package com.asakusafw.testdriver.excel;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelSink;
import com.asakusafw.testdriver.core.DataModelSinkProvider;
import com.asakusafw.testdriver.core.TestContext;

/**
 * Provides {@link DataModelSink} into Excel Sheet.
 * This accepts URI:
 * <ul>
 * <li> which is also a valid URL with file scheme, and </li>
 * <li> which "path" segment ends with ".xls". </li>
 * </ul>
 * @since 0.2.3
 */
public class ExcelSheetSinkProvider implements DataModelSinkProvider {

    static final Logger LOG = LoggerFactory.getLogger(ExcelSheetSinkProvider.class);

    @Override
    public <T> DataModelSink create(
            DataModelDefinition<T> definition,
            URI sink,
            TestContext context) throws IOException {
        String scheme = sink.getScheme();
        if (scheme == null || scheme.endsWith("file") == false) {
            return null;
        }
        File file = new File(sink);
        if (file.getName().endsWith(".xls") == false) {
            return null;
        }
        LOG.info("Excelシートをデータシンクに利用します: {}", sink);
        return new ExcelSheetSinkFactory(file).createSink(definition, context);
    }
}
