/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.utils.jcommander.common;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Objects;

import com.asakusafw.utils.jcommander.CommandConfigurationException;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * Provides output target.
 * @since 0.10.0
 */
@Parameters(resourceBundle = "com.asakusafw.utils.jcommander.common.jcommander")
public class OutputParameter {

    /**
     * The default output name (standard output).
     */
    public static final String DEFAULT_OUTPUT = "-"; //$NON-NLS-1$

    /**
     * The output target file, or {@link #DEFAULT_OUTPUT}.
     */
    @Parameter(
            names = { "-o", "--output" },
            descriptionKey = "parameter.output",
            arity = 1,
            required = false)
    public String output = DEFAULT_OUTPUT;

    /**
     * The output charset encoding.
     */
    @Parameter(
            names = { "-e", "--encoding" },
            descriptionKey = "parameter.encoding",
            arity = 1,
            required = false)
    public String encoding = Charset.defaultCharset().name();

    /**
     * Opens the command output.
     * @return the command output
     */
    public PrintWriter open() {
        try {
            if (Objects.equals(output, DEFAULT_OUTPUT)) {
                return new PrintWriter(new OutputStreamWriter(System.out, encoding), true) {
                    @Override
                    public void close() {
                        // NOTE: never close stdout
                        flush();
                    }
                };
            } else {
                Path file = LocalPath.of(output);
                Path parent = file.getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                return new PrintWriter(Files.newBufferedWriter(file, Charset.forName(encoding)), true);
            }
        } catch (IOException e) {
            throw new CommandConfigurationException(MessageFormat.format(
                    "error occurred while configuring the command output: output={0}, encoding={1}",
                    output, encoding), e);
        }
    }
}
