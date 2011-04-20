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
package com.asakusafw.dmdl.java;

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
import com.asakusafw.dmdl.java.emitter.CompositeDataModelDriver;
import com.asakusafw.dmdl.java.emitter.JavaModelClassGenerator;
import com.asakusafw.dmdl.java.spi.JavaDataModelDriver;
import com.asakusafw.dmdl.model.AstModelDefinition;
import com.asakusafw.dmdl.model.AstScript;
import com.asakusafw.dmdl.parser.DmdlParser;
import com.asakusafw.dmdl.parser.DmdlSyntaxException;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.source.DmdlSourceRepository;
import com.asakusafw.dmdl.source.DmdlSourceRepository.Cursor;
import com.asakusafw.dmdl.spi.AttributeDriver;
import com.asakusafw.dmdl.spi.TypeDriver;

/**
 * Generates Java model classes from input DMDL scripts.
 */
public class GenerateTask {

    static final Logger LOG = LoggerFactory.getLogger(GenerateTask.class);

    private final Configuration conf;

    /**
     * Creates and returns a new instance.
     * @param conf emitter configuration
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public GenerateTask(Configuration conf) {
        if (conf == null) {
            throw new IllegalArgumentException("conf must not be null"); //$NON-NLS-1$
        }
        this.conf = conf;
    }

    /**
     * Generates all models from source repository in the current configuration.
     * @throws IOException if failed to process DMDL scripts
     */
    public void process() throws IOException {
        JavaDataModelDriver driver = new CompositeDataModelDriver(conf.getServiceClassLoader());
        process(driver);
    }

    /**
     * Generates all models from source repository in the current configuration.
     * @param driver the Java program generator driver
     * @throws IOException if failed to process DMDL scripts
     */
    public void process(JavaDataModelDriver driver) throws IOException {
        if (driver == null) {
            throw new IllegalArgumentException("driver must not be null"); //$NON-NLS-1$
        }
        DmdlSemantics semantics = analyze();
        JavaModelClassGenerator generator = new JavaModelClassGenerator(semantics, conf, driver);
        for (ModelDeclaration model : semantics.getDeclaredModels()) {
            generator.emit(model);
        }
    }

    private DmdlSemantics analyze() throws IOException {
        DmdlAnalyzer analyzer = parse();
        try {
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
                }
            }
            throw new IOException("モデル構造の解析中にエラーが発生したため、データモデルクラスの生成を中止します");
        }
    }

    private DmdlAnalyzer parse() throws IOException {
        boolean green = true;
        DmdlParser parser = new DmdlParser();
        DmdlAnalyzer analyzer = new DmdlAnalyzer(
                ServiceLoader.load(TypeDriver.class, conf.getServiceClassLoader()),
                ServiceLoader.load(AttributeDriver.class, conf.getServiceClassLoader()));
        DmdlSourceRepository source = conf.getSource();
        int count = 0;
        Cursor cursor = source.createCursor();
        try {
            while (cursor.next()) {
                count++;
                URI name = cursor.getIdentifier();
                Reader resource = cursor.openResource();
                try {
                    AstScript script = parser.parse(resource, name);
                    for (AstModelDefinition<?> model : script.models) {
                        analyzer.addModel(model);
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
        } finally {
            cursor.close();
        }
        if (green == false) {
            throw new IOException("DMDLスクリプトの解析中にエラーが発生したため、データモデルクラスの生成を中止します");
        }
        if (count == 0) {
            throw new IOException("入力がありません");
        }
        return analyzer;
    }
}
