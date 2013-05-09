/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package com.asakusafw.runtime.stage.directio;

import static com.asakusafw.runtime.stage.directio.StringTemplate.Format.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.io.DataInputBuffer;
import org.apache.hadoop.io.DataOutputBuffer;
import org.apache.hadoop.io.Writable;
import org.junit.Test;

import com.asakusafw.runtime.io.util.WritableRawComparable;
import com.asakusafw.runtime.stage.directio.StringTemplate.Format;
import com.asakusafw.runtime.stage.directio.StringTemplate.FormatSpec;
import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateOption;
import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.runtime.value.DateTimeOption;

/**
 * Test for {@link StringTemplate}.
 */
public class StringTemplateTest {

    /**
     * plain template.
     * @throws Exception if failed
     */
    @Test
    public void plain() throws Exception {
        Mock mock = new Mock(plain("Hello, world!"));
        assertThat(mock.apply(), is("Hello, world!"));
    }

    /**
     * placeholder.
     * @throws Exception if failed
     */
    @Test
    public void placeholder() throws Exception {
        Mock mock = new Mock(spec(NATURAL));
        mock.setMock("Hello, world!");
        assertThat(mock.apply(), is("Hello, world!"));
    }

    /**
     * date placeholder.
     * @throws Exception if failed
     */
    @Test
    public void placeholder_date() throws Exception {
        Mock mock = new Mock(spec(DATE, "yyyy/MM/dd"));
        mock.setMock(new DateOption(new Date(2012, 2, 3)));
        assertThat(mock.apply(), is("2012/02/03"));
    }

    /**
     * date placeholder.
     * @throws Exception if failed
     */
    @Test
    public void placeholder_date_null() throws Exception {
        Mock mock = new Mock(spec(DATE, "yyyy/MM/dd"));
        mock.setMock(new DateOption());
        mock.apply(); // no error
    }

    /**
     * datetime placeholder.
     * @throws Exception if failed
     */
    @Test
    public void placeholder_datetime() throws Exception {
        Mock mock = new Mock(spec(DATETIME, "yyyy-MM-dd/HH"));
        mock.setMock(new DateTimeOption(new DateTime(2012, 2, 3, 4, 5, 6)));
        assertThat(mock.apply(), is("2012-02-03/04"));
    }

    /**
     * datetime placeholder.
     * @throws Exception if failed
     */
    @Test
    public void placeholder_datetime_null() throws Exception {
        Mock mock = new Mock(spec(DATETIME, "yyyy-MM-dd/HH"));
        mock.setMock(new DateTimeOption());
        mock.apply(); // no error
    }

    /**
     * mixed.
     * @throws Exception if failed
     */
    @Test
    public void mixed() throws Exception {
        Mock mock = new Mock(plain("left"), spec(NATURAL), plain("right"));
        mock.setMock("", "replaced", "");
        assertThat(mock.apply(), is("leftreplacedright"));
    }

    /**
     * serialization testing.
     * @throws Exception if failed
     */
    @Test
    public void serialize() throws Exception {
        Mock mock = new Mock(plain("left"), spec(NATURAL), plain("right"), spec(DATE, "yyyy/MM/dd"));

        mock.setMock("", "0", "", new DateOption(new Date(2012, 1, 2)));
        byte[] s0 = ser(mock);
        Mock r0 = new Mock(plain("left"), spec(NATURAL), plain("right"), spec(DATE, "yyyy/MM/dd"));
        des(r0, s0);

        assertThat(r0.apply(), is("left0right2012/01/02"));

        mock.setMock("LLLLLLLLLLLLLL", "0", "RRRRRRRRRRRRRR");
        byte[] s2 = ser(mock);
        Mock r2 = new Mock(plain("left"), spec(NATURAL), plain("right"), spec(DATE, "yyyy/MM/dd"));
        des(r2, s2);

        assertThat(s0, is(s2));
    }

    /**
     * comparison testing.
     * @throws Exception if failed
     */
    @Test
    public void compare() throws Exception {
        Mock a = new Mock(plain("left"), spec(NATURAL), plain("right"), spec(DATE, "yyyy"));
        a.setMock("", "0", "", new DateOption(new Date(2012, 1, 2)));

        Mock b = new Mock(plain("left"), spec(NATURAL), plain("right"), spec(DATE, "yyyy"));
        b.setMock("", "1", "", new DateOption(new Date(2012, 1, 2)));

        Mock c = new Mock(plain("left"), spec(NATURAL), plain("right"), spec(DATE, "yyyy"));
        c.setMock("", "1", "", new DateOption(new Date(2013, 1, 2)));

        Mock d = new Mock(plain("left"), spec(NATURAL), plain("right"), spec(DATE, "yyyy"));
        d.setMock("LLLLLLLLLLLLLL", "0", "RRRRRRRRRRRRRR", new DateOption(new Date(2012, 2, 3)));

        assertThat(cmp(a, b), is(not(0)));
        assertThat(cmp(b, c), is(not(0)));
        assertThat(cmp(a, d), is(equalTo(0)));
    }

    private FormatSpec plain(String string) {
        return spec(Format.PLAIN, string);
    }

    private FormatSpec spec(Format format) {
        return spec(format, null);
    }

    private FormatSpec spec(Format format, String string) {
        return new FormatSpec(format, string);
    }

    static byte[] ser(WritableRawComparable writable) throws IOException {
        DataOutputBuffer out = new DataOutputBuffer();
        writable.write(out);
        assertThat(writable.getSizeInBytes(out.getData(), 0), is(out.getLength()));
        byte[] results = Arrays.copyOfRange(out.getData(), 0, out.getLength());
        return results;
    }

    static <T extends Writable> T des(T writable, byte[] serialized) throws IOException {
        DataInputBuffer buf = new DataInputBuffer();
        buf.reset(serialized, serialized.length);
        writable.readFields(buf);
        return writable;
    }

    static int cmp(WritableRawComparable a, WritableRawComparable b) throws IOException {
        int cmp = a.compareTo(b);
        assertThat(a.equals(b), is(cmp == 0));
        if (cmp == 0) {
            assertThat(a.hashCode(), is(b.hashCode()));
        }
        byte[] serA = ser(a);
        byte[] serB = ser(b);
        int serCmp = a.compareInBytes(serA, 0, serB, 0);
        assertThat(serCmp, is(cmp));
        return cmp;
    }

    private static class Mock extends StringTemplate {

        Mock(FormatSpec... specs) {
            super(specs);
        }

        void setMock(Object... values) {
            set(values);
        }

        @Override
        public void set(Object object) {
            Object[] values = (Object[]) object;
            for (int i = 0; i < values.length; i++) {
                setProperty(i, values[i]);
            }
        }
    }
}
