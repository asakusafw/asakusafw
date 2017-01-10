/**
 * Copyright 2011-2017 Asakusa Framework Team.
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

import com.asakusafw.runtime.io.text.FieldWriter;
import com.asakusafw.runtime.io.text.TextOutput;
import com.asakusafw.runtime.io.text.TextUtil;
import com.asakusafw.runtime.io.text.UnmappableOutput;
import com.asakusafw.runtime.io.text.UnmappableOutputException;

final class OutputDriver<T> implements TextOutput<T> {

    static final Log LOG = LogFactory.getLog(InputDriver.class);

    private static final String NOT_AVAILABLE = "N/A";

    private final FieldWriter writer;

    private final String path;

    private final Class<?> dataType;

    private final FieldDriver<T, ?>[] fields;

    private final ErrorAction onDefaultUnmappableOutput;

    private final BasicFieldOutput fieldOutput = new BasicFieldOutput();

    private boolean requireGenerateHeader;

    @SuppressWarnings("unchecked")
    OutputDriver(
            FieldWriter writer, String path,
            Class<?> dataType, List<FieldDriver<T, ?>> fields,
            HeaderType.Output header,
            ErrorAction onDefaultUnmappableOutput) {
        this.writer = writer;
        this.path = path;
        this.dataType = dataType;
        this.fields = (FieldDriver<T, ?>[]) fields.toArray(new FieldDriver<?, ?>[fields.size()]);
        this.onDefaultUnmappableOutput = onDefaultUnmappableOutput;
        this.requireGenerateHeader = header == HeaderType.Output.ALWAYS;
    }

    @Override
    public void write(T model) throws IOException {
        if (requireGenerateHeader) {
            requireGenerateHeader = false;
            writeHeader();
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace(String.format(
                    "writing record: path=%s, object=%s",
                    path,
                    model));
        }
        for (FieldDriver<T, ?> field : fields) {
            writer.putField(fill(model, field));
        }
        try {
            writer.putEndOfRecord();
        } catch (UnmappableOutputException e) {
            handleUnmappable(model, e.getEntries());
        }
    }

    private void writeHeader() throws IOException {
        BasicFieldOutput output = fieldOutput;
        for (FieldDriver<?, ?> field : fields) {
            writer.putField(output.set(field.name));
        }
        try {
            writer.putEndOfRecord();
        } catch (UnmappableOutputException e) {
            handleUnmappable(null, e.getEntries());
        }
    }

    private <P> BasicFieldOutput fill(T model, FieldDriver<T, P> field) {
        BasicFieldOutput output = fieldOutput;
        P property = field.extractor.apply(model);
        field.adapter.emit(property, output.reset());
        return output;
    }

    private void handleUnmappable(T model, List<UnmappableOutput> entries) throws IOException {
        FieldDriver<T, ?>[] fs = fields;
        int errorCount = 0;
        for (UnmappableOutput entry : entries) {
            int index = entry.getFieldIndex();
            String name = NOT_AVAILABLE;
            String content = NOT_AVAILABLE;
            ErrorAction action = onDefaultUnmappableOutput;
            if (index >= 0 && index < fs.length) {
                FieldDriver<T, ?> field = fs[index];
                if (model != null) {
                    // header always uses default settings
                    action = field.onUnmappedOutput;
                }
                if (action != ErrorAction.IGNORE) {
                    name = field.name;
                    content = extractString(model, field);
                }
            }
            if (action == ErrorAction.IGNORE) {
                continue;
            }
            String message = MessageFormat.format(
                    "output was unmappable: path={0}, type={1}, column={2}, content={3}, code={4}, reason=''{5}''",
                    path != null ? path : NOT_AVAILABLE,
                    dataType.getSimpleName(),
                    TextUtil.quote(name),
                    content,
                    entry.getErrorCode().name(),
                    entry.getReason());
            switch (action) {
            case REPORT:
                LOG.warn(message);
                break;
            case ERROR:
                LOG.error(message);
                errorCount++;
                break;
            default:
                throw new AssertionError(action);
            }
        }
        if (errorCount > 0) {
            throw new IOException(MessageFormat.format(
                    "output contains {0} unmappable sequences (see logs): path={1}, type={2}",
                    errorCount,
                    path != null ? path : NOT_AVAILABLE,
                    dataType.getSimpleName()));
        }
    }

    private <P> String extractString(T model, FieldDriver<T, P> field) {
        if (model == null) {
            return field.name;
        }
        CharSequence content = fill(model, field).get();
        return content == null ? "null" : TextUtil.quote(content); //$NON-NLS-1$
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

    static class FieldDriver<TRecord, TProperty> {

        final String name;

        final Function<? super TRecord, ? extends TProperty> extractor;

        final FieldAdapter<TProperty> adapter;

        final ErrorAction onUnmappedOutput;

        FieldDriver(
                String name,
                Function<? super TRecord, ? extends TProperty> extractor,
                FieldAdapter<TProperty> adapter,
                ErrorAction onUnmappedOutput) {
            this.name = name;
            this.extractor = extractor;
            this.adapter = adapter;
            this.onUnmappedOutput = onUnmappedOutput;
        }
    }
}
