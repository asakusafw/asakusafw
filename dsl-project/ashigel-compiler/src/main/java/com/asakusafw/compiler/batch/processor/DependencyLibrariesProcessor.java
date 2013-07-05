/**
 * Copyright 2013 Asakusa Framework Team.
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
package com.asakusafw.compiler.batch.processor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.batch.AbstractWorkflowProcessor;
import com.asakusafw.compiler.batch.WorkDescriptionProcessor;
import com.asakusafw.compiler.batch.Workflow;
import com.asakusafw.utils.collections.Lists;

/**
 * Copies dependency libraries into the final artifact.
 * @since 0.5.1
 */
public class DependencyLibrariesProcessor extends AbstractWorkflowProcessor {

    static final Logger LOG = LoggerFactory.getLogger(DependencyLibrariesProcessor.class);

    /**
     * The library directory path in the target project.
     */
    public static final String LIBRARY_DIRECTORY_PATH = "src/main/lib";

    /**
     * The output directory path in the final artifact.
     */
    public static final String OUTPUT_DIRECTORY_PATH = "usr/lib";

    @Override
    public Collection<Class<? extends WorkDescriptionProcessor<?>>> getDescriptionProcessors() {
        List<Class<? extends WorkDescriptionProcessor<?>>> results = Lists.create();
        results.add(JobFlowWorkDescriptionProcessor.class);
        return results;
    }

    @Override
    public void process(Workflow workflow) throws IOException {
        File libraryDirectory = getLibraryDirectory();
        LOG.debug("Inspecting library directory: {}", libraryDirectory);

        if (libraryDirectory.isDirectory() == false) {
            LOG.debug("Library directory is not found: {}", libraryDirectory);
            return;
        }

        File outputDirectory = new File(getEnvironment().getConfiguration().getOutputDirectory(), OUTPUT_DIRECTORY_PATH);
        if (outputDirectory.mkdirs() == false && outputDirectory.isDirectory() == false) {
            throw new IOException(MessageFormat.format(
                    "Failed to create library output: {0}",
                    outputDirectory.getAbsolutePath()));
        }

        LOG.info("Copying library files: {}", libraryDirectory);
        for (File file : libraryDirectory.listFiles()) {
            if (file.isDirectory()) {
                LOG.warn(MessageFormat.format(
                        "Ignored a sub-directory in library path: {0}",
                        file.getAbsolutePath()));
            } else {
                File target = new File(outputDirectory, file.getName());
                try {
                    copyFile(file, target);
                } catch (IOException e) {
                    throw new IOException(MessageFormat.format(
                            "Failed to copy a library: {0} -> {1}",
                            file,
                            target));
                }
            }
        }
        LOG.debug("Finished copying library files: {}", libraryDirectory);
    }

    private File getLibraryDirectory() {
        return new File(LIBRARY_DIRECTORY_PATH).getAbsoluteFile();
    }

    private void copyFile(File source, File destination) throws IOException {
        LOG.debug("Copying library file: {} -> {}", source, destination);
        byte[] buf = new byte[1024];
        InputStream in = new FileInputStream(source);
        try {
            OutputStream out = new FileOutputStream(destination);
            try {
                while (true) {
                    int read = in.read(buf);
                    if (read < 0) {
                        break;
                    }
                    out.write(buf, 0, read);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }
}
