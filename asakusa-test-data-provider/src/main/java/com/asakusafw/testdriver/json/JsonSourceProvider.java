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
package com.asakusafw.testdriver.json;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelSource;
import com.asakusafw.testdriver.core.DataModelSourceProvider;
import com.asakusafw.testdriver.core.TestContext;

/**
 * Provides {@link DataModelSource} from JSON object enumeration.
 * This accepts URI which is compatible as a valid URL and ends with ".json".
 * @since 0.2.0
 */
public class JsonSourceProvider implements DataModelSourceProvider {

    static final Logger LOG = LoggerFactory.getLogger(JsonSourceProvider.class);

    private static final String EXTENSION = ".json";

    private static final Charset ENCONDING = Charset.forName("UTF-8");

    @Override
    public <T> DataModelSource open(
            DataModelDefinition<T> definition,
            URI source,
            TestContext context) throws IOException {
        String path = source.getSchemeSpecificPart();
        if (path == null || path.endsWith(EXTENSION) == false) {
            return null;
        }
        LOG.info("JSONファイルをデータソースに利用します: {}", source);
        URL url = source.toURL();
        InputStream input = url.openStream();
        boolean established = false;
        try {
            InputStream bin = new BufferedInputStream(input);
            Reader reader = new InputStreamReader(bin, ENCONDING);
            DataModelSource dms = new JsonDataModelSource(source, definition, reader);
            established = true;
            return dms;
        } finally {
            if (established == false) {
                input.close();
            }
        }
    }
}
