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
package com.asakusafw.yaess.basic;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.yaess.core.Extension;

/**
 * A basic implementation of {@link Extension}.
 * @since 0.8.0
 */
public class BasicExtension implements Extension {

    static final Logger LOG = LoggerFactory.getLogger(BasicExtension.class);

    private final String name;

    private final FileBlob file;

    private final boolean deleteOnClose;

    /**
     * Creates a new instance.
     * @param name the extension name
     * @param file the extension data file
     * @param deleteOnClose {@code true} if delete the data file on close this extension, otherwise {@code false}
     */
    public BasicExtension(String name, File file, boolean deleteOnClose) {
        this.name = name;
        this.file = new FileBlob(file);
        this.deleteOnClose = deleteOnClose;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public FileBlob getData() {
        return file;
    }

    @Override
    public void close() throws IOException {
        if (deleteOnClose) {
            File f = file.getFile();
            if (f.delete() == false && f.exists()) {
                LOG.debug("failed to delete an extension file: name={}", name, f); //$NON-NLS-1$
            }
        }
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "Extension(name={0}, file={1})", //$NON-NLS-1$
                name,
                file.getFile());
    }
}
