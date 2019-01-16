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
package com.asakusafw.runtime.io.json;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;

class InputDriver<T> implements JsonInput<T> {

    static final Logger LOG = LoggerFactory.getLogger(InputDriver.class);

    private final String path;

    private final JsonParser parser;

    private final ErrorAction onUnknownInput;

    private final boolean enableSourcePosition;

    private final boolean enableRecordIndex;

    private final Map<String, Property<T, ?>> propertyMap;

    private final Property<T, ?>[] properties;

    private final Predicate<? super String> excludes;

    private final BitSet presentSet;

    private final PropertyReader adapter;

    private JsonLocation lastSourcePosition;

    private long lastRecordIndex = 0;

    InputDriver(
            String path,
            JsonParser parser,
            Collection<? extends PropertyInfo<T, ?>> properties,
            Predicate<? super String> excludes,
            ErrorAction onUnknownInput,
            boolean enableSourcePosition,
            boolean enableRecordIndex) {
        this.path = path;
        this.parser = parser;
        this.properties = convert(properties);
        this.excludes = excludes;
        this.onUnknownInput = onUnknownInput;
        this.enableSourcePosition = enableSourcePosition;
        this.enableRecordIndex = enableRecordIndex;

        this.propertyMap = new HashMap<>();
        for (Property<T, ?> ref : this.properties) {
            String name = ref.definition.getName();
            if (this.propertyMap.containsKey(name)) {
                throw new IllegalArgumentException(MessageFormat.format(
                        "duplicate property: {0}", //$NON-NLS-1$
                        name));
            }
            this.propertyMap.put(name, ref);
        }
        this.presentSet = new BitSet(properties.size());


        this.adapter = new PropertyReader(parser);
    }

    private static <T> Property<T, ?>[] convert(Collection<? extends PropertyInfo<T, ?>> properties) {
        @SuppressWarnings("unchecked")
        Property<T, ?>[] results = (Property<T, ?>[]) new Property<?, ?>[properties.size()];
        int index = 0;
        for (PropertyInfo<T, ?> property : properties) {
            Property<T, ?> ref = new Property<>(property, index);
            results[index] = ref;
            index++;
        }
        return results;
    }

    @Override
    public boolean readTo(T model) throws IOException {
        JsonToken token = nextToken();
        if (token == null) {
            lastSourcePosition = null;
            lastRecordIndex = -1L;
            return false; // EOF
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace(String.format(
                    "reading object: path=%s, location=%s%s",
                    path,
                    parser.getCurrentLocation(),
                    enableSourcePosition ? "" : " (may be splitted)"));
        }
        if (token == JsonToken.START_OBJECT) {
            presentSet.clear();
            if (enableSourcePosition) {
                lastSourcePosition = parser.getTokenLocation();
            }
            ++lastRecordIndex;
            try {
                parseObject(model);
            } catch (JsonProcessingException e) {
                throw new JsonFormatException(buildErrorMessage("invalid JSON object format"), e);
            }
            fillAbsents(model);
            return true;
        }
        throw new JsonFormatException(buildErrorMessage(MessageFormat.format(
                "invalid data format (unexpected \"{0}\")",
                parser.getText())));
    }

    private void parseObject(T model) throws IOException {
        while (true) {
            // NOTE: current context is beginning of objects, or ending of properties
            JsonToken token = nextToken();
            if (token == JsonToken.END_OBJECT) {
                return;
            }
            if (token == null) {
                throw new IllegalStateException(buildErrorMessage("unexpected EOF"));
            }

            // NOTE: if JSON object is malformed syntactically, the nextToken() will raise an error
            assert token == JsonToken.FIELD_NAME : token;

            // NOTE: current-name is already copied as (interned) string object
            String name = parser.getCurrentName();
            Property<T, ?> property = propertyMap.get(name);
            if (property == null) {
                if (excludes.test(name) == false) {
                    handle(onUnknownInput, null, MessageFormat.format(
                            "unknown property \"{0}\"",
                            name));
                }
                // skip next value
                nextToken();
                discardCurrentValue();
                continue;
            }
            // mark the property is present
            presentSet.set(property.index);

            JsonToken value = nextToken();
            if (value == null) {
                // may not occur
                throw new IllegalStateException(buildErrorMessage(property, "unexpected EOF"));
            }

            switch (value) {
            case START_ARRAY:
            case START_OBJECT:
                handleMalformedInput(null, property, "unsupported JSON value");
                // skip current value
                discardCurrentValue();
                break;

            case VALUE_NUMBER_FLOAT:
            case VALUE_NUMBER_INT:
            case VALUE_STRING:
            case VALUE_TRUE:
            case VALUE_FALSE:
            case VALUE_NULL:
                try {
                    property.apply(model, adapter);
                } catch (JsonProcessingException | RuntimeException e) {
                    handleMalformedInput(e, property, parser.getValueAsString());
                    property.absent(model);
                    // skip current value (trivially does nothing)
                    discardCurrentValue();
                }
                break;

            default:
                throw new AssertionError(value);
            }
        }
    }

    private void fillAbsents(T model) throws IOException {
        BitSet presents = presentSet;
        for (int i = presents.nextClearBit(0); i >= 0 && i < properties.length; i = presents.nextClearBit(i + 1)) {
            Property<T, ?> property = properties[i];
            handleMissingInput(property);
            property.absent(model);
        }
    }

    private JsonToken nextToken() throws IOException {
        JsonToken token = parser.nextToken();
        if (LOG.isTraceEnabled()) {
            LOG.trace("read token: {} ({})", token, parser.getValueAsString("?"));  //$NON-NLS-1$ //$NON-NLS-2$
        }
        return token;
    }

    private void discardCurrentValue() throws IOException {
        // advance parser cursor to the end of the *CURRENT* value
        JsonToken token = parser.currentToken();
        if (token == null) {
            throw new IllegalStateException(buildErrorMessage("invalid data format (unexpected EOF)"));
        }
        switch (token) {
        case VALUE_FALSE:
        case VALUE_NULL:
        case VALUE_NUMBER_FLOAT:
        case VALUE_NUMBER_INT:
        case VALUE_STRING:
        case VALUE_TRUE:
        case FIELD_NAME:
            // already end of value
            break;

        case START_ARRAY:
            nextToken();
            discardUntil(JsonToken.END_ARRAY);
            break;

        case START_OBJECT:
            nextToken();
            discardUntil(JsonToken.END_OBJECT);
            break;

        default:
            throw new AssertionError(token);
        }
    }

    private void discardUntil(JsonToken end) throws IOException {
        while (true) {
            JsonToken current = parser.currentToken();
            if (current == end) {
                // NOTE: if current token is null, the succeeding discardCurrentValue() will raise error
                return;
            }
            discardCurrentValue();
            nextToken();
        }

    }

    private void handleMalformedInput(
            Exception exception, Property<T, ?> property, String message) throws IOException {
        ErrorAction action = property.definition.getOnMalformedInput();
        if (action == ErrorAction.IGNORE) {
            return;
        }
        String text = buildErrorMessage(property, message);
        handle(action, exception, text);
    }

    private void handleMissingInput(Property<T, ?> property) throws IOException {
        ErrorAction action = property.definition.getOnMissingInput();
        if (action == ErrorAction.IGNORE) {
            return;
        }
        String text = buildErrorMessage(property, "property is not present");
        handle(action, null, text);
    }

    private static void handle(ErrorAction action, Exception exception, String message) throws IOException {
        switch (action) {
        case REPORT:
            LOG.warn(message, exception);
            break;
        case ERROR:
            throw new JsonFormatException(message, exception);
        default:
            break;
        }
    }

    private String buildErrorMessage(String message) {
        if (enableSourcePosition) {
            JsonLocation location = parser.getCurrentLocation();
            return MessageFormat.format(
                    "{0} [at {1}:{2}:{3}]",
                    message,
                    path,
                    location.getLineNr(),
                    location.getColumnNr());
        } else {
            return MessageFormat.format(
                    "{0} [at {1}:<unknown-source-potision>]",
                    message,
                    path);
        }
    }

    private String buildErrorMessage(Property<T, ?> property, String message) {
        return buildErrorMessage(MessageFormat.format(
                "{0} [on property \"{1}\"]",
                message,
                property.definition.getName()));
    }

    @Override
    public long getLineNumber() {
        JsonLocation loc = lastSourcePosition;
        return loc != null ? loc.getLineNr() - 1 : -1L;
    }

    @Override
    public long getRecordIndex() {
        return enableRecordIndex ? lastRecordIndex - 1 : -1L;
    }

    @Override
    public void close() throws IOException {
        parser.close();
    }

    private static final class Property<T, P> {

        final Function<? super T, ? extends P> extractor;

        final PropertyDefinition<? super P> definition;

        final PropertyAdapter<? super P> adapter;

        final int index;

        Property(PropertyInfo<T, P> info, int index) {
            this.extractor = info.extractor;
            this.definition = info.definition;
            this.adapter = info.definition.getAdapter();
            this.index = index;
        }

        void apply(T model, ValueReader reader) throws IOException {
            P property = extractor.apply(model);
            adapter.read(reader, property);
        }

        void absent(T model) {
            P property = extractor.apply(model);
            adapter.absent(property);
        }
    }

    private static final class PropertyReader implements ValueReader {

        final JsonParser parser;

        PropertyReader(JsonParser parser) {
            this.parser = parser;
        }

        @Override
        public boolean isNull() throws IOException {
            return parser.currentToken() == JsonToken.VALUE_NULL;
        }

        @Override
        public void readString(StringBuilder buffer) throws IOException {
            // TODO: measure performance between creating String object and using StringBuffer via StringWriter
            buffer.append(readString());
        }

        @Override
        public String readString() throws IOException {
            if (parser.currentToken() == JsonToken.VALUE_STRING) {
                return parser.getText();
            }
            checkNull();
            return parser.getValueAsString();
        }

        @Override
        public BigDecimal readDecimal() throws IOException {
            if (parser.currentToken() == JsonToken.VALUE_STRING) {
                try {
                    return new BigDecimal(readString());
                } catch (NumberFormatException e) {
                    LOG.debug("parse error: {}", readString(), e); //$NON-NLS-1$
                    // fall through ...
                }
            }
            checkNull();
            return parser.getDecimalValue();
        }

        @Override
        public int readInt() throws IOException {
            if (parser.currentToken() == JsonToken.VALUE_STRING) {
                try {
                    return Integer.parseInt(readString());
                } catch (NumberFormatException e) {
                    LOG.debug("parse error: {}", readString(), e); //$NON-NLS-1$
                    // fall through ...
                }
            }
            checkNull();
            return parser.getIntValue();
        }

        @Override
        public long readLong() throws IOException {
            if (parser.currentToken() == JsonToken.VALUE_STRING) {
                try {
                    return Long.parseLong(readString());
                } catch (NumberFormatException e) {
                    LOG.debug("parse error: {}", readString(), e); //$NON-NLS-1$
                    // fall through ...
                }
            }
            checkNull();
            return parser.getLongValue();
        }

        @Override
        public float readFloat() throws IOException {
            if (parser.currentToken() == JsonToken.VALUE_STRING) {
                try {
                    return Float.parseFloat(readString());
                } catch (NumberFormatException e) {
                    LOG.debug("parse error: {}", readString(), e); //$NON-NLS-1$
                    // fall through ...
                }
            }
            checkNull();
            return parser.getFloatValue();
        }

        @Override
        public double readDouble() throws IOException {
            if (parser.currentToken() == JsonToken.VALUE_STRING) {
                try {
                    return Double.parseDouble(readString());
                } catch (NumberFormatException e) {
                    LOG.debug("parse error: {}", readString(), e); //$NON-NLS-1$
                    // fall through ...
                }
            }
            checkNull();
            return parser.getDoubleValue();
        }

        @Override
        public boolean readBoolean() throws IOException {
            if (parser.currentToken() == JsonToken.VALUE_STRING) {
                // NOTE: don't use Boolean.parseBoolean
                // because it returns false for non-"true" values
                String value = readString();
                if (value.equals("true")) {
                    return true;
                }
                if (value.equals("false")) {
                    return false;
                }
                LOG.debug("parse error (boolean): {}", readString()); //$NON-NLS-1$
                // fall through...
            }
            checkNull();
            return parser.getBooleanValue();
        }

        private void checkNull() throws IOException {
            if (isNull()) {
                throw new JsonParseException(parser, "unexpected null value");
            }
        }
    }
}
