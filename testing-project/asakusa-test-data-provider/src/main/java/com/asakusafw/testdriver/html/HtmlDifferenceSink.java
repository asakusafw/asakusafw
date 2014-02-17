/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.testdriver.html;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.Difference;
import com.asakusafw.testdriver.core.DifferenceSink;
import com.asakusafw.testdriver.core.PropertyName;
import com.asakusafw.testdriver.core.PropertyType;

/**
 * An implementation of {@link DifferenceSink} as a HTML file.
 * @since 0.2.3
 */
public class HtmlDifferenceSink implements DifferenceSink {

    static final Logger LOG = LoggerFactory.getLogger(HtmlDifferenceSink.class);

    private static final String CSS_FILE_NAME = "difference.css";

    static final Charset CHARSET = Charset.forName("UTF-8");

    static final List<String> CSS;

    static {
        List<String> lines = new ArrayList<String>();
        InputStream in = HtmlDifferenceSink.class.getResourceAsStream(CSS_FILE_NAME);
        if (in != null) {
            try {
                Scanner scanner = new Scanner(in, CHARSET.name());
                while (scanner.hasNextLine()) {
                    IOException exception = scanner.ioException();
                    if (exception != null) {
                        scanner.close();
                        throw exception;
                    }
                    lines.add(scanner.nextLine());
                }
                scanner.close();
            } catch (IOException e) {
                LOG.warn("Failed to initialize HtmlDifferenceSink", e);
                lines.clear();
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    // ignored
                }
            }
        }
        CSS = Collections.unmodifiableList(lines);
    }

    private final Context context;

    private boolean closed;

    /**
     * Creates a new instance.
     * @param output target file
     * @param definition definition
     * @throws IOException if failed to initialize the target file
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public HtmlDifferenceSink(File output, DataModelDefinition<?> definition) throws IOException {
        if (output == null) {
            throw new IllegalArgumentException("output must not be null"); //$NON-NLS-1$
        }
        if (definition == null) {
            throw new IllegalArgumentException("definition must not be null"); //$NON-NLS-1$
        }
        boolean succeed = false;
        OutputStream os = new FileOutputStream(output);
        try {
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(os, CHARSET), true);
            this.context = new Context(writer, definition);
            succeed = true;
        } finally {
            if (succeed == false) {
                try {
                    os.close();
                } catch (IOException e) {
                    LOG.warn(MessageFormat.format(
                            "Failed to open file: {0}",
                            output), e);
                }
            }
        }
        context.writeHeader();
    }

    @Override
    public void put(Difference difference) throws IOException {
        context.writeDifference(difference);
    }

    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }
        try {
            context.writeFooter();
        } finally {
            context.close();
        }
        closed = true;
    }

    private static class Context implements Closeable {

        private static final char[] ASCII_SPECIAL_ESCAPE = new char[0x80];
        static {
            ASCII_SPECIAL_ESCAPE['"'] = '"';
            ASCII_SPECIAL_ESCAPE['\b'] = 'b';
            ASCII_SPECIAL_ESCAPE['\t'] = 't';
            ASCII_SPECIAL_ESCAPE['\n'] = 'n';
            ASCII_SPECIAL_ESCAPE['\f'] = 'f';
            ASCII_SPECIAL_ESCAPE['\r'] = 'r';
            ASCII_SPECIAL_ESCAPE['\\'] = '\\';
        }

        private final DataModelDefinition<?> definition;

        private final PrintWriter writer;

        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

        private final SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        Context(PrintWriter writer, DataModelDefinition<?> definition) {
            assert writer != null;
            assert definition != null;
            this.writer = writer;
            this.definition = definition;
        }

        public void writeDifference(Difference difference) {
            assert difference != null;
            writer.println("<div class=\"difference\">");

            writer.println("<p class=\"diagnostic-label\">");
            writer.println("Diagnostic Message:");
            writer.println("</p>");
            writer.println("<div class=\"diagnostic\">");
            writer.println("<span class=\"diagnostic-message\">");
            writer.println(toHtml(difference.getDiagnostic()));
            writer.println("</span>");
            writer.println("</div>");

            writer.println("<p class=\"compare-label\">");
            writer.println("Inspection:");
            writer.println("</p>");
            writer.println("<div class=\"compare\">");
            writer.println("<table class=\"object\">");

            writer.println("<tr>");

            writer.println("<th class=\"property\">");
            writer.println("Property Name");
            writer.println("</th>");

            writer.println("<th class=\"expected\">");
            writer.println("Expected Value");
            writer.println("</th>");

            writer.println("<th class=\"actual\">");
            writer.println("Actual Value");
            writer.println("</th>");

            writer.println("</tr>");

            DataModelReflection expected = difference.getExpected();
            DataModelReflection actual = difference.getActual();
            for (PropertyName property : definition.getProperties()) {
                writer.println("<tr>");

                writer.println("<td class=\"property\">");
                writer.println(toHtml(property));
                writer.println("</td>");

                writer.println("<td class=\"expected\">");
                writer.println(toHtml(describeProperty(expected, property)));
                writer.println("</td>");

                writer.println("<td class=\"actual\">");
                writer.println(toHtml(describeProperty(actual, property)));
                writer.println("</td>");

                writer.println("</tr>");
            }

            writer.println("</table>");
            writer.println("</div>");
            writer.println("</div>");
        }

        private Object describeProperty(DataModelReflection object, PropertyName property) {
            assert property != null;
            if (object == null) {
                return null;
            }
            Object value = object.getValue(property);
            if (value == null) {
                return null;
            }
            PropertyType type = definition.getType(property);
            switch (type) {
            case DATE:
                return dateFormat.format(((Calendar) value).getTime());
            case TIME:
                return timeFormat.format(((Calendar) value).getTime());
            case DATETIME:
                return datetimeFormat.format(((Calendar) value).getTime());
            case DECIMAL:
                return String.format(
                        "%s(scale=%d)",
                        ((BigDecimal) value).toPlainString(),
                        ((BigDecimal) value).scale());
            case STRING:
                return toStringLiteral((String) value);
            default:
                return value;
            }
        }

        private Object toStringLiteral(String value) {
            assert value != null;
            StringBuilder buf = new StringBuilder();
            buf.append('"');
            for (char c : value.toCharArray()) {
                if (c <= 0x7f && ASCII_SPECIAL_ESCAPE[c] != 0) {
                    buf.append('\\');
                    buf.append(ASCII_SPECIAL_ESCAPE[c]);
                } else if (Character.isISOControl(c) || !Character.isDefined(c)) {
                    buf.append(String.format("\\u%04x", (int) c)); //$NON-NLS-1$
                } else {
                    buf.append(c);
                }
            }
            buf.append('"');
            return buf.toString();
        }

        public void writeHeader() {
            writer.println("<html>");

            writer.println("<head>");
            writer.println("<meta http-equiv=\"Content-type\" content=\"text/html; charset=UTF-8\">");
            writer.println("<title>Differences</title>");
            writer.println("<style type=\"text/css\">");
            writer.println("<!--");
            for (String line : CSS) {
                writer.println(line);
            }
            writer.println("-->");
            writer.println("</style>");
            writer.println("</head>");

            writer.println("<body>");
            writer.println("<p class=\"header-label\">");
            writer.println("Differences");
            writer.println("</p>");
        }

        public void writeFooter() {
            writer.println("<p class=\"footer-label\">");
            writer.printf("Generated: %s%n", datetimeFormat.format(new Date()));
            writer.println("</p>");
            writer.println("</body>");
            writer.println("</html>");
        }

        @Override
        public void close() throws IOException {
            writer.close();
        }

        private String toHtml(Object message) {
            String text = String.valueOf(message);
            StringBuilder buf = new StringBuilder();
            for (char c : text.toCharArray()) {
                if (c == '<') {
                    buf.append("&lt;");
                } else if (c == '>') {
                    buf.append("&gt;");
                } else if (c == '"') {
                    buf.append("&quot;");
                } else if (c == '&') {
                    buf.append("&amp;");
                } else {
                    buf.append(c);
                }
            }
            return buf.toString();
        }
    }
}
