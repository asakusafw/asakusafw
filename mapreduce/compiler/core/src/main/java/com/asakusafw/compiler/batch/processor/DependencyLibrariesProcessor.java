/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.compiler.batch.AbstractWorkflowProcessor;
import com.asakusafw.compiler.batch.WorkDescriptionProcessor;
import com.asakusafw.compiler.batch.Workflow;
import com.asakusafw.compiler.flow.FlowCompilerOptions;
import com.asakusafw.compiler.flow.FlowCompilerOptions.GenericOptionValue;

/**
 * Copies dependency libraries into the final artifact.
 * @since 0.5.1
 * @version 0.8.0
 */
public class DependencyLibrariesProcessor extends AbstractWorkflowProcessor {

    static final Logger LOG = LoggerFactory.getLogger(DependencyLibrariesProcessor.class);

    /**
     * The compiler option whether this feature is enabled or not.
     * @since 0.8.0
     */
    public static final String KEY_ENABLE = DependencyLibrariesProcessor.class.getName() + ".enabled"; //$NON-NLS-1$

    /**
     * The library directory path in the target project.
     */
    public static final String LIBRARY_DIRECTORY_PATH = "src/main/libs"; //$NON-NLS-1$

    /**
     * The output directory path in the final artifact.
     */
    public static final String OUTPUT_DIRECTORY_PATH = "usr/lib"; //$NON-NLS-1$

    @Override
    public Collection<Class<? extends WorkDescriptionProcessor<?>>> getDescriptionProcessors() {
        List<Class<? extends WorkDescriptionProcessor<?>>> results = new ArrayList<>();
        results.add(JobFlowWorkDescriptionProcessor.class);
        return results;
    }

    @Override
    public void process(Workflow workflow) throws IOException {
        if (isEnabled() == false) {
            LOG.debug("Attaching dependency libraries is not enabled"); //$NON-NLS-1$
            return;
        }
        File libraryDirectory = getLibraryDirectory();
        LOG.debug("Inspecting library directory: {}", libraryDirectory); //$NON-NLS-1$

        if (libraryDirectory.isDirectory() == false) {
            LOG.debug("Library directory is not found: {}", libraryDirectory); //$NON-NLS-1$
            return;
        }

        File outputDirectory = new File(
                getEnvironment().getConfiguration().getOutputDirectory(),
                OUTPUT_DIRECTORY_PATH);
        if (outputDirectory.mkdirs() == false && outputDirectory.isDirectory() == false) {
            throw new IOException(MessageFormat.format(
                    Messages.getString("DependencyLibrariesProcessor.errorFailedToCreateOutputDierctory"), //$NON-NLS-1$
                    outputDirectory.getAbsolutePath()));
        }

        LOG.debug("Copying library files: {}", libraryDirectory); //$NON-NLS-1$
        for (File file : list(libraryDirectory)) {
            if (file.isDirectory()) {
                LOG.warn(MessageFormat.format(
                        Messages.getString("DependencyLibrariesProcessor.warnIgnoreNestedInputDirectory"), //$NON-NLS-1$
                        file.getAbsolutePath()));
            } else {
                File target = new File(outputDirectory, file.getName());
                try {
                    copyFile(file, target);
                } catch (IOException e) {
                    throw new IOException(MessageFormat.format(
                            Messages.getString("DependencyLibrariesProcessor.errorFailedToCopyLibrary"), //$NON-NLS-1$
                            file,
                            target));
                }
            }
        }
        LOG.debug("Finished copying library files: {}", libraryDirectory); //$NON-NLS-1$
    }

    private static List<File> list(File file) {
        return Optional.ofNullable(file.listFiles())
                .map(Arrays::asList)
                .orElse(Collections.emptyList());
    }

    private boolean isEnabled() {
        FlowCompilerOptions options = getEnvironment().getConfiguration().getFlowCompilerOptions();
        GenericOptionValue value = options.getGenericExtraAttribute(KEY_ENABLE, GenericOptionValue.AUTO);
        return value != GenericOptionValue.DISABLED;
    }

    private File getLibraryDirectory() {
        return new File(LIBRARY_DIRECTORY_PATH).getAbsoluteFile();
    }

    private void copyFile(File source, File destination) throws IOException {
        LOG.debug("Copying library file: {} -> {}", source, destination); //$NON-NLS-1$
        byte[] buf = new byte[1024];
        try (InputStream in = new FileInputStream(source);
                OutputStream out = new FileOutputStream(destination)) {
            while (true) {
                int read = in.read(buf);
                if (read < 0) {
                    break;
                }
                out.write(buf, 0, read);
            }
        }
    }
}
