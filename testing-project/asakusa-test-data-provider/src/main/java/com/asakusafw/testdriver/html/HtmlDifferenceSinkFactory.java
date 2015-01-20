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
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DifferenceSink;
import com.asakusafw.testdriver.core.DifferenceSinkFactory;
import com.asakusafw.testdriver.core.TestContext;

/**
 * An implementation of {@link DifferenceSinkFactory} to create an HTML file.
 * @since 0.2.3
 */
public class HtmlDifferenceSinkFactory extends DifferenceSinkFactory {

    static final Logger LOG = LoggerFactory.getLogger(HtmlDifferenceSinkFactory.class);

    final File output;

    /**
     * Creates a new instance.
     * @param output output target file
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public HtmlDifferenceSinkFactory(File output) {
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
    public HtmlDifferenceSinkFactory(String output) {
        if (output == null) {
            throw new IllegalArgumentException("output must not be null"); //$NON-NLS-1$
        }
        this.output = new File(output);
    }

    @Override
    public <T> DifferenceSink createSink(DataModelDefinition<T> definition, TestContext context) throws IOException {
        if (definition == null) {
            throw new IllegalArgumentException("definition must not be null"); //$NON-NLS-1$
        }
        if (context == null) {
            throw new IllegalArgumentException("context must not be null"); //$NON-NLS-1$
        }
        File parent = output.getParentFile();
        if (parent != null && parent.isDirectory() == false && parent.mkdirs() == false) {
            throw new IOException(MessageFormat.format(
                    "Failed to create an output directory for {0}",
                    output));
        }
        LOG.info("Generating difference information into {}", output.getAbsoluteFile());
        return new HtmlDifferenceSink(output, definition);
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "{0}({1})",
                HtmlDifferenceSink.class.getSimpleName(),
                output);
    }
}
