/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.runtime.io.text.value;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

import com.asakusafw.runtime.io.text.TextUtil;
import com.asakusafw.runtime.io.text.driver.FieldAdapter;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.runtime.value.StringOptionUtil;

/**
 * An implementation of {@link FieldAdapter} which accepts {@link StringOption}.
 * @since 0.9.1
 * @version 0.10.3
 */
public final class StringOptionFieldAdapter extends ValueOptionFieldAdapter<StringOption> {

    private final CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT);

    private final ByteBuffer encodeBuffer = ByteBuffer.allocate(256);

    StringOptionFieldAdapter(String nullFormat) {
        super(nullFormat);
    }

    /**
     * Returns a new builder.
     * @return the created builder
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected void doParse(CharSequence contents, StringOption property) {
        property.reset();
        if (contents.length() == 0) {
            return;
        }
        try {
            StringOptionUtil.append(CharBuffer.wrap(contents), property, encoder, encodeBuffer);
        } catch (CharacterCodingException e) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "cannot map input string to UTF-8: {0}",
                    TextUtil.quote(contents)), e);
        }
    }

    @Override
    protected void doEmit(StringOption property, StringBuilder output) {
        property.appendTo(output);
    }

    /**
     * A builder of {@link StringOptionFieldAdapter}.
     * @since 0.9.1
     */
    public static class Builder extends BuilderBase<Builder, StringOptionFieldAdapter> {
        @Override
        public StringOptionFieldAdapter build() {
            return new StringOptionFieldAdapter(getNullFormat());
        }
    }
}
