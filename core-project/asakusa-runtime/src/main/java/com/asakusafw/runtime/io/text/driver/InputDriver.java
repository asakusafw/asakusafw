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
package com.asakusafw.runtime.io.text.driver;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.asakusafw.runtime.io.text.FieldReader;
import com.asakusafw.runtime.io.text.TextFormatException;
import com.asakusafw.runtime.io.text.TextInput;
import com.asakusafw.runtime.io.text.TextUtil;

final class InputDriver<T> implements TextInput<T> {

    static final Log LOG = LogFactory.getLog(InputDriver.class);

    private static final String NOT_AVAILABLE = "N/A";

    private final FieldReader reader;

    private final String path;

    private final Class<?> dataType;

    private final FieldDriver<T, ?>[] fields;

    private final HeaderType.Input header;

    private final boolean trimExtraInput;

    private final boolean skipExtraEmptyInput;

    private final ErrorAction onLessInput;

    private final ErrorAction onMoreInput;

    private final boolean fromTextHead;

    private boolean requireConsumeHeader;

    private final TrimBuffer trimmer = new TrimBuffer();

    @SuppressWarnings("unchecked")
    InputDriver(
            FieldReader reader, String path,
            Class<? extends T> dataType, List<FieldDriver<T, ?>> fields,
            HeaderType.Input header, boolean trimExtraInput, boolean skipExtraEmptyInput,
            ErrorAction onLessInput, ErrorAction onMoreInput,
            boolean fromTextHead) {
        this.reader = reader;
        this.path = path;
        this.dataType = dataType;
        this.fields = (FieldDriver<T, ?>[]) fields.toArray(new FieldDriver<?, ?>[fields.size()]);
        this.header = header;
        this.trimExtraInput = trimExtraInput;
        this.skipExtraEmptyInput = skipExtraEmptyInput;
        this.onLessInput = onLessInput;
        this.onMoreInput = onMoreInput;
        this.fromTextHead = fromTextHead;
        this.requireConsumeHeader = fromTextHead && header != HeaderType.Input.NEVER;
    }

    @Override
    public long getLineNumber() {
        return fromTextHead ? reader.getRecordLineNumber() : -1L;
    }

    @Override
    public long getRecordIndex() {
        return fromTextHead ? reader.getRecordIndex() : -1L;
    }

    @Override
    public boolean readTo(T model) throws IOException {
        try {
            if (reader.nextRecord() == false) {
                return false;
            }
            if (LOG.isTraceEnabled()) {
                LOG.trace(String.format(
                        "reading record: path=%s, line=%,d, fields=%s",
                        path,
                        getLineNumberMessage(),
                        collectFields()));
                reader.rewindFields();
            }
            if (requireConsumeHeader) {
                requireConsumeHeader = false;
                if (doHeaderCheck() == false) {
                    return false;
                }
            }
            process(model);
            return true;
        } catch (TextFormatException e) {
            throw new IOException(MessageFormat.format(
                    "text format is not valid: path={0}, line={1}, row={2}",
                    path != null ? path : NOT_AVAILABLE,
                    getLineNumberMessage(),
                    getRecordIndexMessage()), e);
        }
    }

    private void process(T model) throws IOException {
        int lessCount = 0;
        for (FieldDriver<T, ?> field : fields) {
            boolean success = processField(model, field);
            if (success == false) {
                lessCount++;
            }
        }
        if (lessCount == 0) {
            checkRest();
        } else {
            handleLess(lessCount);
        }
    }

    private <P> boolean processField(T model, FieldDriver<T, P> field) throws IOException {
        P property = field.extractor.apply(model);
        FieldAdapter<? super P> adapter = field.adapter;
        while (reader.nextField()) {
            CharSequence value = reader.getContent();
            if (value != null) {
                if (field.trimInput) {
                    value = trimmer.wrap(value);
                }
                if (value.length() == 0 && field.skipEmptyInput) {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace(String.format(
                                "skip empty field: path=%s, line=%,d, row=%,d, column=%,d",
                                path,
                                getLineNumberMessage(),
                                getRecordIndexMessage(),
                                getFieldIndexMessage()));
                    }
                    continue;
                }
            }
            try {
                adapter.parse(value, property);
            } catch (MalformedFieldException e) {
                adapter.clear(property);
                handleMalformed(field, value, e);
            }
            return true;
        }
        adapter.clear(property);
        return false;
    }

    private void checkRest() throws IOException {
        if (onMoreInput == ErrorAction.IGNORE) {
            return;
        }
        int count = countRest();
        if (count == 0) {
            return;
        }
        handle(onMoreInput, null, MessageFormat.format(
                "record has {0} (of {1}) fields: path={2}, line={3}, row={4}, fields={5}",
                count + fields.length,
                fields.length,
                path != null ? path : NOT_AVAILABLE,
                getLineNumberMessage(),
                getRecordIndexMessage(),
                collectFields()));
    }

    private void handleLess(int lessCount) throws IOException {
        if (onLessInput == ErrorAction.IGNORE) {
            return;
        }
        handle(onLessInput, null, MessageFormat.format(
                "record has {0} (of {1}) fields: path={2}, line={3}, row={4}, fields={5}",
                fields.length - lessCount,
                fields.length,
                path != null ? path : NOT_AVAILABLE,
                getLineNumberMessage(),
                getRecordIndexMessage(),
                collectFields()));
    }

    private void handleMalformed(
            FieldDriver<?, ?> field, CharSequence value,
            MalformedFieldException cause) throws IOException {
        if (field.onMalformedInput == ErrorAction.IGNORE) {
            return;
        }
        handle(field.onMalformedInput, cause, MessageFormat.format(
                "field \"{0}\" (in {1}) is malformed: path={2}, line={3}, row={4}, column={5}, content={6}",
                field.name,
                dataType.getSimpleName(),
                path != null ? path : NOT_AVAILABLE,
                getLineNumberMessage(),
                getRecordIndexMessage(),
                getFieldIndexMessage(),
                value == null ? "null" : TextUtil.quote(value))); //$NON-NLS-1$
    }

    private void handle(ErrorAction action, Exception cause, String message) throws IOException {
        switch (action) {
        case REPORT:
            LOG.warn(message, cause);
            break;
        case ERROR:
            throw new IOException(message, cause);
        default:
            throw new AssertionError(action);
        }
    }

    private boolean doHeaderCheck() throws IOException {
        // if the first line is filtered out, we never consume headers
        if (reader.getRecordLineNumber() != 0L) {
            return true;
        }
        if (testConsumeHeader()) {
            return reader.nextRecord();
        } else {
            reader.rewindFields();
            return true;
        }
    }

    private boolean testConsumeHeader() throws IOException {
        switch (header) {
        case ALWAYS:
            return true;
        case OPTIONAL:
            return compareHeader();
        default:
            throw new AssertionError(header);
        }
    }

    private boolean compareHeader() throws IOException {
        int matched = 0;
        for (FieldDriver<?, ?> field : fields) {
            while (reader.nextField()) {
                CharSequence value = reader.getContent();
                String label = field.name;
                if (value != null) {
                    if (trimExtraInput) {
                        value = trimmer.wrap(value);
                        label = label.trim();
                    }
                    if (value.length() == 0 && field.skipEmptyInput) {
                        if (LOG.isTraceEnabled()) {
                            LOG.trace(String.format(
                                    "skip empty header field: path=%s, column=%,d",
                                    path,
                                    reader.getFieldIndex()));
                        }
                        continue;
                    }
                }
                if (value == null || label.contentEquals(value) == false) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(String.format(
                                "header mismatch: path=%s, column=%,d, expected=%s, appeared=%s", //$NON-NLS-1$
                                path,
                                reader.getFieldIndex(),
                                TextUtil.quote(field.name),
                                value == null ? "null" : TextUtil.quote(value))); //$NON-NLS-1$
                    }
                    return false;
                }
                matched++;
                break;
            }
        }
        if (checkHeaderFieldCount(matched, onLessInput) == false) {
            return false;
        }
        if (checkHeaderFieldCount(fields.length + countRest(), onMoreInput) == false) {
            return false;
        }
        return true;
    }

    private boolean checkHeaderFieldCount(int count, ErrorAction action) {
        if (count == fields.length || action == ErrorAction.IGNORE) {
            return true;
        }
        String message = MessageFormat.format(
                "header has {0} (of {1}) fields: path={2}",
                count,
                fields.length,
                path != null ? path : NOT_AVAILABLE);
        switch (action) {
        case REPORT:
            LOG.warn(message);
            return true;
        case ERROR:
            LOG.debug(message);
            return false;
        default:
            throw new AssertionError(action);
        }
    }

    private String collectFields() {
        try {
            reader.rewindFields();
            StringBuilder buffer = new StringBuilder();
            buffer.append('{');
            while (reader.nextField()) {
                if (buffer.length() > 1) {
                    buffer.append(", "); //$NON-NLS-1$
                }
                CharSequence content = reader.getContent();
                if (content == null) {
                    buffer.append((Object) null);
                } else {
                    TextUtil.quoteTo(content, buffer);
                }
            }
            buffer.append('}');
            return buffer.toString();
        } catch (IOException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("error occurred while peeking the current line", e); //$NON-NLS-1$
            }
            return NOT_AVAILABLE;
        }
    }

    private int countRest() throws IOException {
        int count = 0;
        while (reader.nextField()) {
            if (skipExtraEmptyInput) {
                CharSequence cs = reader.getContent();
                if (cs != null) {
                    if (trimExtraInput) {
                        cs = trimmer.wrap(cs);
                    }
                    if (cs.length() == 0) {
                        continue;
                    }
                }
            }
            count++;
        }
        return count;
    }

    private Object getLineNumberMessage() {
        return getIndexMessage(getLineNumber());
    }

    private Object getRecordIndexMessage() {
        return getIndexMessage(getRecordIndex());
    }

    private Object getFieldIndexMessage() {
        return getIndexMessage(reader.getFieldIndex());
    }

    private Object getIndexMessage(long index) {
        return index < 0 ? NOT_AVAILABLE : index + 1;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

    @Override
    public String toString() {
        return String.format("InputDriver(path=%s, reader=%s)", path, reader); //$NON-NLS-1$
    }

    static class FieldDriver<TRecord, TProperty> {

        final String name;

        final Function<? super TRecord, ? extends TProperty> extractor;

        final FieldAdapter<? super TProperty> adapter;

        final boolean trimInput;

        final boolean skipEmptyInput;

        final ErrorAction onMalformedInput;

        FieldDriver(
                String name,
                Function<? super TRecord, ? extends TProperty> extractor,
                FieldAdapter<? super TProperty> adapter,
                boolean trimInput, boolean skipEmptyInput,
                ErrorAction onMalformedInput) {
            this.name = name;
            this.extractor = extractor;
            this.adapter = adapter;
            this.trimInput = trimInput;
            this.skipEmptyInput = skipEmptyInput;
            this.onMalformedInput = onMalformedInput;
        }
    }

    private static final class TrimBuffer implements CharSequence {

        private CharSequence parent;

        private int offset;

        private int length;

        TrimBuffer() {
            this.parent = ""; //$NON-NLS-1$
            this.offset = 0;
            this.length = 0;
        }

        CharSequence wrap(CharSequence cs) {
            int newLength = cs.length();
            int newOffset = TextUtil.countLeadingWhitespaces(cs, 0, newLength);
            newLength -= newOffset;
            newLength -= TextUtil.countTrailingWhitespaces(cs, newOffset, newLength);
            if (newLength == 0) {
                return ""; //$NON-NLS-1$
            } else if (newOffset == 0 && newLength == cs.length()) {
                return cs;
            } else {
                parent = cs;
                offset = newOffset;
                length = newLength;
                return this;
            }
        }

        @Override
        public int length() {
            return length;
        }

        @Override
        public char charAt(int index) {
            if (index < 0 || index >= length) {
                throw new IndexOutOfBoundsException();
            }
            return parent.charAt(index + offset);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            if (start < 0 || start > end || end > length) {
                throw new IndexOutOfBoundsException();
            }
            return parent.subSequence(start + offset, end + offset);
        }

        @Override
        public String toString() {
            return parent.subSequence(offset, offset + length).toString();
        }
    }
}
