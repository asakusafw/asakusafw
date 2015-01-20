/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.testdriver.html;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DifferenceSink;
import com.asakusafw.testdriver.core.DifferenceSinkProvider;
import com.asakusafw.testdriver.core.TestContext;

/**
 * Provides {@link DifferenceSink} into HTML file.
 * This accepts URI:
 * <ul>
 * <li> which is also a valid URL with file scheme, and </li>
 * <li> which "path" segment ends with ".html". </li>
 * </ul>
 * @since 0.2.3
 */
public class HtmlDifferenceSinkProvider implements DifferenceSinkProvider {

    static final Logger LOG = LoggerFactory.getLogger(HtmlDifferenceSinkProvider.class);

    @Override
    public <T> DifferenceSink create(
            DataModelDefinition<T> definition,
            URI sink,
            TestContext context) throws IOException {
        String scheme = sink.getScheme();
        if (scheme == null || scheme.endsWith("file") == false) {
            return null;
        }
        File file = new File(sink);
        if (file.getName().endsWith(".html") == false) {
            return null;
        }
        LOG.info("HTMLファイルを差分シンクに利用します: {}", sink);
        return new HtmlDifferenceSinkFactory(file).createSink(definition, context);
    }
}
