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
package com.asakusafw.testdriver.json;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.text.MessageFormat;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.DataModelSource;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonStreamParser;

/**
 * {@link DataModelSource} from JSON object stream.
 * @since 0.2.0
 * @version 0.6.0
 */
public class JsonDataModelSource implements DataModelSource {

    private final DataModelDefinition<?> definition;

    private final URI id;

    private final Reader reader;

    private final JsonStreamParser parser;

    private boolean first = true;

    /**
     * Creates a new instance.
     * @param id source ID (nullable)
     * @param definition the data model definition
     * @param reader source character stream
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public JsonDataModelSource(URI id, DataModelDefinition<?> definition, Reader reader) {
        if (definition == null) {
            throw new IllegalArgumentException("definition must not be null"); //$NON-NLS-1$
        }
        if (reader == null) {
            throw new IllegalArgumentException("reader must not be null"); //$NON-NLS-1$
        }
        this.id = id;
        this.definition = definition;
        this.reader = reader;
        this.parser = new JsonStreamParser(reader);
    }

    @Override
    public DataModelReflection next() throws IOException {
        if (first) {
            try {
                parser.hasNext();
            } catch (JsonParseException e) {
                if (e.getCause() instanceof EOFException) {
                    return null;
                }
            }
            first = false;
        }
        try {
            if (parser.hasNext() == false) {
                return null;
            }
            JsonElement element = parser.next();
            return JsonObjectDriver.convert(definition, element);
        } catch (JsonParseException e) {
            throw new IOException(MessageFormat.format(
                    "Malformed JSON object (id={0})",
                    id), e);
        }
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
