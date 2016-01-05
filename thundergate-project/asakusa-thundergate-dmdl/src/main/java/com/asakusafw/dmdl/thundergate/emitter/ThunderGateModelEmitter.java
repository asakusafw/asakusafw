/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.dmdl.thundergate.emitter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstLiteral;
import com.asakusafw.dmdl.model.AstModelDefinition;
import com.asakusafw.dmdl.model.AstScript;
import com.asakusafw.dmdl.model.LiteralKind;
import com.asakusafw.dmdl.parser.DmdlEmitter;
import com.asakusafw.dmdl.thundergate.Configuration;
import com.asakusafw.dmdl.thundergate.Constants;
import com.asakusafw.dmdl.thundergate.model.JoinedModelDescription;
import com.asakusafw.dmdl.thundergate.model.ModelDescription;
import com.asakusafw.dmdl.thundergate.model.ModelProperty;
import com.asakusafw.dmdl.thundergate.model.PropertyType;
import com.asakusafw.dmdl.thundergate.model.PropertyTypeKind;
import com.asakusafw.dmdl.thundergate.model.SummarizedModelDescription;
import com.asakusafw.dmdl.thundergate.model.TableModelDescription;

/**
 * Emits ThunderGate data models as DMDL.
 * @since 0.2.0
 * @version 0.2.3
 */
public class ThunderGateModelEmitter {

    static final Logger LOG = LoggerFactory.getLogger(ThunderGateModelEmitter.class);

    private final Configuration config;

    /**
     * Creates and returns a new instance.
     * @param config the configuration
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public ThunderGateModelEmitter(Configuration config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null"); //$NON-NLS-1$
        }
        this.config = config;
    }

    /**
     * Convert the ThunderGete model description into DMDL model and emit DMDL script.
     * @param model target model
     * @throws IOException if failed to emit DMDL script
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void emit(ModelDescription model) throws IOException {
        if (model == null) {
            throw new IllegalArgumentException("model must not be null"); //$NON-NLS-1$
        }
        AstScript script = convert(model);
        String name = model.getReference().getSimpleName();
        emit(name, script);
    }

    private AstScript convert(ModelDescription model) {
        assert model != null;
        AstModelDefinition<?> def;
        if (model instanceof TableModelDescription) {
            TableModelDescription tableModel = (TableModelDescription) model;
            AstAttribute cacheSupport = generateCacheSupport(tableModel);
            if (cacheSupport == null) {
                def = RecordModelGenerator.generate(tableModel);
            } else {
                def = RecordModelGenerator.generate(tableModel, cacheSupport);
            }
        } else if (model instanceof JoinedModelDescription) {
            def = JoinedModelGenerator.generate((JoinedModelDescription) model);
        } else if (model instanceof SummarizedModelDescription) {
            def = SummarizedModelGenerator.generate((SummarizedModelDescription) model);
        } else {
            throw new AssertionError(model);
        }
        return new AstScript(null, Collections.singletonList(def));
    }

    private AstAttribute generateCacheSupport(TableModelDescription model) {
        assert model != null;
        if (config.getSidColumn() == null) {
            return null;
        }

        Map<String, ModelProperty> properties = new TreeMap<String, ModelProperty>(String.CASE_INSENSITIVE_ORDER);
        for (ModelProperty property : model.getProperties()) {
            properties.put(property.getSource().getName(), property);
        }

        ModelProperty sid = properties.get(config.getSidColumn());
        if (sid == null) {
            LOG.warn("テーブル{}にはカラム{}が定義されていないため、キャッシュはサポートされません",
                    model.getReference().getSimpleName(),
                    config.getSidColumn());
            return null;
        }
        if (sid.getType().getKind() != PropertyTypeKind.LONG) {
            LOG.warn("テーブル{}のカラム{}がBIGINTでないため、キャッシュはサポートされません",
                    model.getReference().getSimpleName(),
                    config.getSidColumn());
            return null;
        }

        ModelProperty timestamp = properties.get(config.getTimestampColumn());
        if (timestamp == null) {
            LOG.warn("テーブル{}にはカラム{}が定義されていないため、キャッシュはサポートされません",
                    model.getReference().getSimpleName(),
                    config.getTimestampColumn());
            return null;
        }
        if (timestamp.getType().getKind() != PropertyTypeKind.DATETIME) {
            LOG.warn("テーブル{}のカラム{}がDATETIMEでないため、キャッシュはサポートされません",
                    model.getReference().getSimpleName(),
                    config.getTimestampColumn());
            return null;
        }

        if (config.getDeleteFlagColumn() != null) {
            ModelProperty deleteFlag = properties.get(config.getDeleteFlagColumn());
            if (deleteFlag == null) {
                LOG.info("テーブル{}のカラム{}が定義されていないため、このテーブルに対する論理削除機能は無効化されます",
                        model.getReference().getSimpleName(),
                        config.getDeleteFlagColumn());
            } else if (acceptsLiteral(deleteFlag.getType(), config.getDeleteFlagValue()) == false) {
                LOG.warn("テーブル{}のカラム{}は指定した論理削除の値({})を利用できないため、キャッシュはサポートされません", new Object[] {
                        model.getReference().getSimpleName(),
                        config.getDeleteFlagColumn(),
                        config.getDeleteFlagValue(),
                });
                return null;
            } else {
                return AstBuilder.getCacheSupport(sid, timestamp, deleteFlag, config.getDeleteFlagValue());
            }
        }
        return AstBuilder.getCacheSupport(sid, timestamp);
    }

    private boolean acceptsLiteral(PropertyType type, AstLiteral value) {
        assert type != null;
        assert value != null;
        LiteralKind literalKind = value.getKind();
        switch (type.getKind()) {
        case BOOLEAN:
            return literalKind == LiteralKind.BOOLEAN;
        case BYTE:
        case SHORT:
        case INT:
        case LONG:
            return literalKind == LiteralKind.INTEGER;
        case STRING:
            return literalKind == LiteralKind.STRING;
        default:
            return false;
        }
    }

    private void emit(String name, AstScript script) throws IOException {
        assert name != null;
        assert script != null;
        PrintWriter output = open(name);
        try {
            DmdlEmitter.emit(script, output);
            if (output.checkError()) {
                throw new IOException(MessageFormat.format(
                        "Cannot output DMDL {0}",
                        name));
            }
        } finally {
            output.close();
        }
    }

    private PrintWriter open(String name) throws IOException {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        File file = new File(config.getOutput(), name + Constants.DMDL_LIKE_EXTENSION);
        File directory = file.getParentFile();
        if (directory.exists() == false && directory.mkdirs() == false) {
            throw new IOException(MessageFormat.format(
                    "Failed to create output directory: {0}",
                    directory));
        }
        PrintWriter writer = new PrintWriter(file, config.getEncoding().name());
        return writer;
    }
}
