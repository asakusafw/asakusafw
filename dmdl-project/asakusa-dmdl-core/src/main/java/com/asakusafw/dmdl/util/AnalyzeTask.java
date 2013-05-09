/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.dmdl.util;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.dmdl.Diagnostic;
import com.asakusafw.dmdl.analyzer.DmdlAnalyzer;
import com.asakusafw.dmdl.analyzer.DmdlSemanticException;
import com.asakusafw.dmdl.model.AstModelDefinition;
import com.asakusafw.dmdl.model.AstScript;
import com.asakusafw.dmdl.parser.DmdlParser;
import com.asakusafw.dmdl.parser.DmdlSyntaxException;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.source.DmdlSourceRepository;
import com.asakusafw.dmdl.source.DmdlSourceRepository.Cursor;
import com.asakusafw.dmdl.spi.AttributeDriver;
import com.asakusafw.dmdl.spi.TypeDriver;

/**
 * Analyzes DMDL models from input DMDL scripts.
 */
public class AnalyzeTask {

    static final Logger LOG = LoggerFactory.getLogger(AnalyzeTask.class);

    private final String processName;

    private final ClassLoader serviceClassLoader;

    /**
     * Creates and returns a new instance.
     * @param processName the parent process name
     * @param serviceClassLoader class loader to load the plug-ins
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public AnalyzeTask(String processName, ClassLoader serviceClassLoader) {
        if (processName == null) {
            throw new IllegalArgumentException("processName must not be null"); //$NON-NLS-1$
        }
        if (serviceClassLoader == null) {
            throw new IllegalArgumentException("serviceClassLoader must not be null"); //$NON-NLS-1$
        }
        this.processName = processName;
        this.serviceClassLoader = serviceClassLoader;
    }

    /**
     * Analyzes all models from source repository in the current configuration.
     * @param repository the source repository
     * @return the analyzed model
     * @throws IOException if failed to process DMDL scripts
     */
    public DmdlSemantics process(DmdlSourceRepository repository) throws IOException {
        if (repository == null) {
            throw new IllegalArgumentException("repository must not be null"); //$NON-NLS-1$
        }
        DmdlAnalyzer analyzer = parse(repository);
        try {
            LOG.info("モデルの内容を解析します");
            return analyzer.resolve();
        } catch (DmdlSemanticException e) {
            LOG.error("モデルの解析に失敗しました", e);
            for (Diagnostic diagnostic : e.getDiagnostics()) {
                switch (diagnostic.level) {
                case INFO:
                    LOG.info("{} ({})", diagnostic.message, diagnostic.region);
                    break;
                case WARN:
                    LOG.warn("{} ({})", diagnostic.message, diagnostic.region);
                    break;
                case ERROR:
                    LOG.error("{} ({})", diagnostic.message, diagnostic.region);
                    break;
                default:
                    LOG.warn("[INTERNAL ERROR] Unknown Diagnostic Kind: {}", diagnostic);
                    break;
                }
            }
            throw new IOException(MessageFormat.format(
                    "モデル構造の解析中にエラーが発生したため、{0}を中止します",
                    processName));
        }
    }

    private DmdlAnalyzer parse(DmdlSourceRepository source) throws IOException {
        assert source != null;
        boolean green = true;
        DmdlParser parser = new DmdlParser();
        DmdlAnalyzer analyzer = new DmdlAnalyzer(
                ServiceLoader.load(TypeDriver.class, serviceClassLoader),
                ServiceLoader.load(AttributeDriver.class, serviceClassLoader));
        int count = 0;
        Cursor cursor = source.createCursor();
        try {
            while (cursor.next()) {
                URI name = cursor.getIdentifier();
                LOG.info("DMDLスクリプトを解析します: {}", name);
                Reader resource = cursor.openResource();
                try {
                    AstScript script = parser.parse(resource, name);
                    for (AstModelDefinition<?> model : script.models) {
                        LOG.info("モデルを追加します: {}", model.name);
                        analyzer.addModel(model);
                        count++;
                    }
                } catch (DmdlSyntaxException e) {
                    LOG.error(MessageFormat.format(
                            "{0}の解析に失敗しました",
                            name), e);
                    green = false;
                } finally {
                    resource.close();
                }
            }
            LOG.info("{}個のモデルが定義されています", count);
        } finally {
            cursor.close();
        }
        if (green == false) {
            throw new IOException(MessageFormat.format(
                    "DMDLスクリプトの解析中にエラーが発生したため、{0}を中止します",
                    processName));
        }
        if (count == 0) {
            throw new IOException("入力がありません");
        }
        return analyzer;
    }
}
