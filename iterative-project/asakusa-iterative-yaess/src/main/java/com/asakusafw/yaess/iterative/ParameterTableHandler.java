/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.asakusafw.yaess.iterative;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.iterative.common.IterativeExtensions;
import com.asakusafw.iterative.common.ParameterTable;
import com.asakusafw.yaess.basic.BasicExtension;
import com.asakusafw.yaess.core.Extension;
import com.asakusafw.yaess.core.ExtensionHandler;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

/**
 * Handles {@link ParameterTable}.
 * @since 0.8.0
 */
public class ParameterTableHandler implements ExtensionHandler {

    static final Logger LOG = LoggerFactory.getLogger(ParameterTableHandler.class);

    /**
     * The extension tag name.
     */
    public static final String TAG = "parameter-table";

    @Override
    public Extension handle(String tag, String value) throws IOException {
        if (tag.equals(TAG) == false) {
            return null;
        }
        File file = new File(value);
        if (file.isFile() == false) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        ParameterTable table = parse(file);

        File temporary = File.createTempFile("asakusa-iterative-", ".bin"); //$NON-NLS-1$ //$NON-NLS-2$
        boolean success = false;
        try (OutputStream output = new FileOutputStream(temporary)) {
            LOG.debug("storing parameter table: {}", temporary); //$NON-NLS-1$
            IterativeExtensions.save(output, table);
            success = true;
        } finally {
            if (success == false) {
                if (temporary.delete() == false && temporary.exists()) {
                    LOG.warn(MessageFormat.format(
                            "failed to delete temporary file: {0}", //$NON-NLS-1$
                            temporary));
                }
            }
        }
        return new BasicExtension(IterativeExtensions.EXTENSION_NAME, temporary, true);
    }

    static ParameterTable parse(File file) throws IOException {
        LOG.debug("parsing JSON parameter table: {}", file); //$NON-NLS-1$
        JsonFactory json = new JsonFactory();
        json.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
        json.enable(JsonParser.Feature.ALLOW_COMMENTS);
        json.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        json.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        json.enable(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);
        json.enable(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS);
        try (JsonParser parser = json.createParser(file)) {
            ParameterTable.Builder builder = IterativeExtensions.builder();
            while (true) {
                JsonToken t = parser.nextToken();
                if (t == null) {
                    break;
                } else if (t == JsonToken.START_OBJECT) {
                    builder.next();
                    parseRow(parser, builder);
                } else {
                    throw new IOException(MessageFormat.format(
                            "invalid JSON format (invalid start object): {0}",
                            parser.getCurrentLocation()));
                }
            }
            return builder.build();
        }
    }

    private static void parseRow(JsonParser parser, ParameterTable.Builder builder) throws IOException {
        while (true) {
            JsonToken t0 = parser.nextToken();
            if (t0 == JsonToken.END_OBJECT) {
                return;
            } else if (t0 != JsonToken.FIELD_NAME) {
                throw new IOException(MessageFormat.format(
                        "invalid JSON format (invalid field name): {0}",
                        parser.getCurrentLocation()));
            }
            String name = parser.getCurrentName();

            JsonToken t1 = parser.nextToken();
            if (t1 == null) {
                throw new IOException(MessageFormat.format(
                        "invalid JSON format (unexpected EOF): {0}",
                        parser.getCurrentLocation()));
            }
            switch (t1) {
            case VALUE_STRING:
            case VALUE_NUMBER_FLOAT:
            case VALUE_NUMBER_INT:
            case VALUE_TRUE:
            case VALUE_FALSE:
                // ok
                break;
            default:
                throw new IOException(MessageFormat.format(
                        "invalid JSON format (unsupported value): {0}",
                        parser.getCurrentLocation()));
            }
            String value = parser.getValueAsString();

            builder.put(name, value);
        }
    }
}
