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
package com.asakusafw.testdriver.json;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelDefinition.Builder;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.DataModelScanner;
import com.asakusafw.testdriver.core.PropertyName;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Converts JSON object into {@link DataModelReflection}.
 * @since 0.2.0
 */
public final class JsonObjectDriver extends DataModelScanner<JsonObject, IOException> {

    private final Builder<?> builder;

    private JsonObjectDriver(Builder<?> builder) {
        assert builder != null;
        this.builder = builder;
    }

    /**
     * Converts JsonElement into a corresponded {@link DataModelReflection}.
     * @param definition structure of {@link DataModelReflection}
     * @param element conversion target
     * @return the converted object
     * @throws IOException if failed to convert
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static DataModelReflection convert(
            DataModelDefinition<?> definition,
            JsonElement element) throws IOException {
        if ((element instanceof JsonObject) == false) {
            throw new IOException(MessageFormat.format(
                    Messages.getString("JsonObjectDriver.errorInvalidJsonValue"), //$NON-NLS-1$
                    element));
        }
        JsonObjectDriver driver = new JsonObjectDriver(definition.newReflection());
        try {
            driver.scan(definition, (JsonObject) element);
        } catch (RuntimeException e) {
            throw new IOException(MessageFormat.format(
                    Messages.getString("JsonObjectDriver.errorFailedToConvert"), //$NON-NLS-1$
                    element), e);
        }
        return driver.builder.build();
    }

    private JsonElement property(JsonObject context, PropertyName name) {
        assert context != null;
        assert name != null;
        String jsName = toJsName(name);
        JsonElement element = context.get(jsName);
        return element;
    }

    String toJsName(PropertyName name) {
        assert name != null;
        StringBuilder buf = new StringBuilder();
        Iterator<String> iterator = name.getWords().iterator();
        assert iterator.hasNext();
        buf.append(iterator.next());
        while (iterator.hasNext()) {
            buf.append('_');
            buf.append(iterator.next());
        }
        return buf.toString();
    }

    @Override
    public void booleanProperty(PropertyName name, JsonObject context) throws IOException {
        JsonElement prop = property(context, name);
        if (prop == null) {
            return;
        }
        builder.add(name, prop.getAsBoolean());
    }

    @Override
    public void byteProperty(PropertyName name, JsonObject context) throws IOException {
        JsonElement prop = property(context, name);
        if (prop == null) {
            return;
        }
        builder.add(name, prop.getAsByte());
    }

    @Override
    public void shortProperty(PropertyName name, JsonObject context) throws IOException {
        JsonElement prop = property(context, name);
        if (prop == null) {
            return;
        }
        builder.add(name, prop.getAsShort());
    }

    @Override
    public void intProperty(PropertyName name, JsonObject context) throws IOException {
        JsonElement prop = property(context, name);
        if (prop == null) {
            return;
        }
        builder.add(name, prop.getAsInt());
    }

    @Override
    public void longProperty(PropertyName name, JsonObject context) throws IOException {
        JsonElement prop = property(context, name);
        if (prop == null) {
            return;
        }
        builder.add(name, prop.getAsLong());
    }

    @Override
    public void integerProperty(PropertyName name, JsonObject context) throws IOException {
        JsonElement prop = property(context, name);
        if (prop == null) {
            return;
        }
        builder.add(name, prop.getAsBigInteger());
    }

    @Override
    public void floatProperty(PropertyName name, JsonObject context) throws IOException {
        JsonElement prop = property(context, name);
        if (prop == null) {
            return;
        }
        builder.add(name, prop.getAsFloat());
    }

    @Override
    public void doubleProperty(PropertyName name, JsonObject context) throws IOException {
        JsonElement prop = property(context, name);
        if (prop == null) {
            return;
        }
        builder.add(name, prop.getAsDouble());
    }

    @Override
    public void decimalProperty(PropertyName name, JsonObject context) throws IOException {
        JsonElement prop = property(context, name);
        if (prop == null) {
            return;
        }
        builder.add(name, prop.getAsBigDecimal());
    }

    @Override
    public void stringProperty(PropertyName name, JsonObject context) throws IOException {
        JsonElement prop = property(context, name);
        if (prop == null) {
            return;
        }
        builder.add(name, prop.getAsString());
    }

    private static final Pattern DATE = Pattern.compile("(\\d{3,})-(\\d{1,2})-(\\d{1,2})"); //$NON-NLS-1$
    @Override
    public void dateProperty(PropertyName name, JsonObject context) throws IOException {
        JsonElement prop = property(context, name);
        if (prop == null) {
            return;
        }
        String string = prop.getAsString();
        Matcher matcher = DATE.matcher(string);
        if (matcher.matches() == false) {
            throw new IOException(MessageFormat.format(
                    Messages.getString("JsonObjectDriver.errorInvalidDateFormat"), //$NON-NLS-1$
                    name,
                    string,
                    "yyyy-mm-dd")); //$NON-NLS-1$
        }
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, Integer.parseInt(matcher.group(1)));
        calendar.set(Calendar.MONTH, Integer.parseInt(matcher.group(2)) - 1);
        calendar.set(Calendar.DATE, Integer.parseInt(matcher.group(3)));
        builder.add(name, calendar);
    }

    private static final Pattern TIME = Pattern.compile("(\\d{1,2}):(\\d{1,2}):(\\d{1,2})"); //$NON-NLS-1$
    @Override
    public void timeProperty(PropertyName name, JsonObject context) throws IOException {
        JsonElement prop = property(context, name);
        if (prop == null) {
            return;
        }
        String string = prop.getAsString();
        Matcher matcher = TIME.matcher(string);
        if (matcher.matches() == false) {
            throw new IOException(MessageFormat.format(
                    Messages.getString("JsonObjectDriver.errorInvalidTimeFormat"), //$NON-NLS-1$
                    name,
                    string,
                    "hh:mm:ss")); //$NON-NLS-1$
        }
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(matcher.group(1)));
        calendar.set(Calendar.MINUTE, Integer.parseInt(matcher.group(2)) - 1);
        calendar.set(Calendar.SECOND, Integer.parseInt(matcher.group(3)));
        builder.add(name, calendar);
    }

    private static final Pattern DATETIME = Pattern.compile(
            "(\\d{3,})-(\\d{1,2})-(\\d{1,2})\\s+(\\d{1,2}):(\\d{1,2}):(\\d{1,2})"); //$NON-NLS-1$
    @Override
    public void datetimeProperty(PropertyName name, JsonObject context) throws IOException {
        JsonElement prop = property(context, name);
        if (prop == null) {
            return;
        }
        String string = prop.getAsString();
        Matcher matcher = DATETIME.matcher(string);
        if (matcher.matches() == false) {
            throw new IOException(MessageFormat.format(
                    Messages.getString("JsonObjectDriver.errorInvalidDateTimeFormat"), //$NON-NLS-1$
                    name,
                    string,
                    "yyyy-mm-dd hh:mm:ss")); //$NON-NLS-1$
        }
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, Integer.parseInt(matcher.group(1)));
        calendar.set(Calendar.MONTH, Integer.parseInt(matcher.group(2)) - 1);
        calendar.set(Calendar.DATE, Integer.parseInt(matcher.group(3)));
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(matcher.group(4)));
        calendar.set(Calendar.MINUTE, Integer.parseInt(matcher.group(5)));
        calendar.set(Calendar.SECOND, Integer.parseInt(matcher.group(6)));
        builder.add(name, calendar);
    }
}
