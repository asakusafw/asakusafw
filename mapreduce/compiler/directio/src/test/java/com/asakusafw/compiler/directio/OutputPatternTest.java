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
package com.asakusafw.compiler.directio;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.asakusafw.compiler.directio.OutputPattern.CompiledOrder;
import com.asakusafw.compiler.directio.OutputPattern.CompiledResourcePattern;
import com.asakusafw.compiler.directio.OutputPattern.SourceKind;
import com.asakusafw.compiler.flow.DataClass;
import com.asakusafw.runtime.stage.directio.StringTemplate.Format;

/**
 * Test for {@link OutputPattern}.
 */
public class OutputPatternTest {

    final DataClass dataClass = new MockDataClass(MockData.class);

    /**
     * resource pattern with plain.
     */
    @Test
    public void resource_plain() {
        List<CompiledResourcePattern> pattern = OutputPattern.compileResourcePattern(
                "hello", dataClass);
        assertThat(pattern.size(), is(1));
        assertThat(pattern.get(0).getFormat(), is(Format.PLAIN));
        assertThat(pattern.get(0).getArgument(), is("hello"));
    }

    /**
     * resource pattern with placeholder.
     */
    @Test
    public void resource_placeholder() {
        List<CompiledResourcePattern> pattern = OutputPattern.compileResourcePattern(
                "{intValue}", dataClass);
        assertThat(pattern.size(), is(1));
        assertThat(pattern.get(0).getTarget().getName(), is("intValue"));
        assertThat(pattern.get(0).getFormat(), is(Format.NATURAL));
    }

    /**
     * resource pattern with int placeholder.
     */
    @Test
    public void resource_placeholder_numeric() {
        List<CompiledResourcePattern> pattern = OutputPattern.compileResourcePattern(
                "{intValue:#,###}", dataClass);
        assertThat(pattern.size(), is(1));
        assertThat(pattern.get(0).getTarget().getName(), is("intValue"));
        assertThat(pattern.get(0).getFormat(), is(Format.INT));
        assertThat(pattern.get(0).getArgument(), is("#,###"));
    }

    /**
     * resource pattern with date placeholder.
     */
    @Test
    public void resource_placeholder_date() {
        List<CompiledResourcePattern> pattern = OutputPattern.compileResourcePattern(
                "{dateValue:yyyy/MM}", dataClass);
        assertThat(pattern.size(), is(1));
        assertThat(pattern.get(0).getTarget().getName(), is("dateValue"));
        assertThat(pattern.get(0).getFormat(), is(Format.DATE));
        assertThat(pattern.get(0).getArgument(), is("yyyy/MM"));
    }

    /**
     * resource pattern with datetime placeholder.
     */
    @Test
    public void resource_placeholder_datetime() {
        List<CompiledResourcePattern> pattern = OutputPattern.compileResourcePattern(
                "{datetimeValue:yyyy/MM_ss}", dataClass);
        assertThat(pattern.size(), is(1));
        assertThat(pattern.get(0).getTarget().getName(), is("datetimeValue"));
        assertThat(pattern.get(0).getFormat(), is(Format.DATETIME));
        assertThat(pattern.get(0).getArgument(), is("yyyy/MM_ss"));
    }

    /**
     * resource pattern with placeholder.
     */
    @Test
    public void resource_placeholder_mixed() {
        List<CompiledResourcePattern> pattern = OutputPattern.compileResourcePattern(
                "left{intValue}right", dataClass);
        assertThat(pattern.size(), is(3));

        assertThat(pattern.get(0).getFormat(), is(Format.PLAIN));
        assertThat(pattern.get(0).getArgument(), is("left"));

        assertThat(pattern.get(1).getTarget().getName(), is("intValue"));
        assertThat(pattern.get(1).getFormat(), is(Format.NATURAL));

        assertThat(pattern.get(2).getFormat(), is(Format.PLAIN));
        assertThat(pattern.get(2).getArgument(), is("right"));
    }

    /**
     * resource pattern with random.
     */
    @Test
    public void resource_randomNumber() {
        List<CompiledResourcePattern> pattern = OutputPattern.compileResourcePattern(
                "[0..10]", dataClass);
        assertThat(pattern.size(), is(1));
        assertThat(pattern.get(0).getRandomNumber().getLowerBound(), is(0));
        assertThat(pattern.get(0).getRandomNumber().getUpperBound(), is(10));
        assertThat(pattern.get(0).getFormat(), is(Format.NATURAL));
    }

    /**
     * resource pattern with wildcard.
     */
    @Test
    public void resource_wildcard() {
        List<CompiledResourcePattern> pattern = OutputPattern.compileResourcePattern(
                "*", dataClass);
        assertThat(pattern.size(), is(1));
        assertThat(pattern.get(0).getKind(), is(SourceKind.ENVIRONMENT));
    }

    /**
     * resource pattern with variable.
     */
    @Test
    public void resource_variable() {
        List<CompiledResourcePattern> pattern = OutputPattern.compileResourcePattern(
                "${date}", dataClass);
        assertThat(pattern.size(), is(1));
        assertThat(pattern.get(0).getFormat(), is(Format.PLAIN));
        assertThat(pattern.get(0).getArgument(), is("${date}"));
    }

    /**
     * resource pattern with variable.
     */
    @Test
    public void resource_variable_mixed() {
        List<CompiledResourcePattern> pattern = OutputPattern.compileResourcePattern(
                "left${date}right", dataClass);
        assertThat(pattern.size(), is(1));
        assertThat(pattern.get(0).getFormat(), is(Format.PLAIN));
        assertThat(pattern.get(0).getArgument(), is("left${date}right"));
    }

    /**
     * complex resource pattern.
     */
    @Test
    public void resource_complex() {
        List<CompiledResourcePattern> pattern = OutputPattern.compileResourcePattern(
                "{stringValue}/${category}-{dateValue:yyyy-MM-dd}-[10..99]-*.csv", dataClass);
        assertThat(pattern.size(), is(8));

        assertThat(pattern.get(0).getTarget().getName(), is("stringValue"));
        assertThat(pattern.get(0).getFormat(), is(Format.NATURAL));

        assertThat(pattern.get(1).getKind(), is(SourceKind.NOTHING));
        assertThat(pattern.get(1).getArgument(), is("/${category}-"));

        assertThat(pattern.get(2).getTarget().getName(), is("dateValue"));
        assertThat(pattern.get(2).getFormat(), is(Format.DATE));
        assertThat(pattern.get(2).getArgument(), is("yyyy-MM-dd"));

        assertThat(pattern.get(3).getKind(), is(SourceKind.NOTHING));
        assertThat(pattern.get(3).getArgument(), is("-"));

        assertThat(pattern.get(4).getRandomNumber().getLowerBound(), is(10));
        assertThat(pattern.get(4).getRandomNumber().getUpperBound(), is(99));

        assertThat(pattern.get(5).getFormat(), is(Format.PLAIN));
        assertThat(pattern.get(5).getArgument(), is("-"));

        assertThat(pattern.get(6).getKind(), is(SourceKind.ENVIRONMENT));

        assertThat(pattern.get(7).getKind(), is(SourceKind.NOTHING));
        assertThat(pattern.get(7).getArgument(), is(".csv"));
    }

    /**
     * resource pattern with unknown property.
     */
    @Test(expected = IllegalArgumentException.class)
    public void resource_unknown_property() {
        OutputPattern.compileResourcePattern("{UNKNWON}", dataClass);
    }

    /**
     * resource pattern with unknwon format.
     */
    @Test(expected = IllegalArgumentException.class)
    public void resource_unknown_format() {
        OutputPattern.compileResourcePattern("{stringValue:?}", dataClass);
    }

    /**
     * resource pattern with invalid format.
     */
    @Test(expected = IllegalArgumentException.class)
    public void resource_invalid_format() {
        OutputPattern.compileResourcePattern("{dateValue:yyyy-MM-ddTHH:mm:ssz}", dataClass);
    }

    /**
     * resource pattern with invalid character.
     */
    @Test(expected = IllegalArgumentException.class)
    public void resource_invalid_character() {
        OutputPattern.compileResourcePattern("?", dataClass);
    }

    /**
     * resource pattern with invalid character.
     */
    @Test(expected = IllegalArgumentException.class)
    public void resource_placeholder_eof() {
        OutputPattern.compileResourcePattern("hoge{foo", dataClass);
    }

    /**
     * resource pattern with invalid character.
     */
    @Test(expected = IllegalArgumentException.class)
    public void resource_format_eof() {
        OutputPattern.compileResourcePattern("hoge{foo:bar", dataClass);
    }

    /**
     * resource pattern with invalid character.
     */
    @Test(expected = IllegalArgumentException.class)
    public void resource_random_eof() {
        OutputPattern.compileResourcePattern("hoge[100..", dataClass);
    }

    /**
     * resource pattern with invalid character.
     */
    @Test(expected = IllegalArgumentException.class)
    public void resource_variable_eof() {
        OutputPattern.compileResourcePattern("hoge${foo", dataClass);
    }

    /**
     * resource pattern with invalid random number range.
     */
    @Test(expected = IllegalArgumentException.class)
    public void resource_random_invalid_range() {
        OutputPattern.compileResourcePattern("[1..1]", dataClass);
    }

    /**
     * simple order pattern.
     */
    @Test
    public void order() {
        List<CompiledOrder> pattern = OutputPattern.compileOrder(list("intValue"), dataClass);
        assertThat(pattern.size(), is(1));
        assertThat(pattern.get(0).getTarget().getName(), is("intValue"));
        assertThat(pattern.get(0).isAscend(), is(true));
    }

    /**
     * ascendant order.
     */
    @Test
    public void order_asc() {
        List<CompiledOrder> pattern = OutputPattern.compileOrder(list("+intValue"), dataClass);
        assertThat(pattern.size(), is(1));
        assertThat(pattern.get(0).getTarget().getName(), is("intValue"));
        assertThat(pattern.get(0).isAscend(), is(true));
    }

    /**
     * descendant order.
     */
    @Test
    public void order_desc() {
        List<CompiledOrder> pattern = OutputPattern.compileOrder(list("-intValue"), dataClass);
        assertThat(pattern.size(), is(1));
        assertThat(pattern.get(0).getTarget().getName(), is("intValue"));
        assertThat(pattern.get(0).isAscend(), is(false));
    }

    /**
     * legacy ascendant order.
     */
    @Test
    public void order_asc_legacy() {
        List<CompiledOrder> pattern = OutputPattern.compileOrder(list("intValue ASC"), dataClass);
        assertThat(pattern.size(), is(1));
        assertThat(pattern.get(0).getTarget().getName(), is("intValue"));
        assertThat(pattern.get(0).isAscend(), is(true));
    }

    /**
     * legacy descendant order.
     */
    @Test
    public void order_desc_legacy() {
        List<CompiledOrder> pattern = OutputPattern.compileOrder(list("intValue DESC"), dataClass);
        assertThat(pattern.size(), is(1));
        assertThat(pattern.get(0).getTarget().getName(), is("intValue"));
        assertThat(pattern.get(0).isAscend(), is(false));
    }

    /**
     * multiple order.
     */
    @Test
    public void order_multiple() {
        List<CompiledOrder> pattern = OutputPattern.compileOrder(
                list("+intValue", "- stringValue", "dateValue"), dataClass);
        assertThat(pattern.size(), is(3));

        assertThat(pattern.get(0).getTarget().getName(), is("intValue"));
        assertThat(pattern.get(0).isAscend(), is(true));

        assertThat(pattern.get(1).getTarget().getName(), is("stringValue"));
        assertThat(pattern.get(1).isAscend(), is(false));

        assertThat(pattern.get(2).getTarget().getName(), is("dateValue"));
        assertThat(pattern.get(2).isAscend(), is(true));
    }

    /**
     * order with unknown property.
     */
    @Test(expected = IllegalArgumentException.class)
    public void order_invalid_format() {
        OutputPattern.compileOrder(list("Hello, world!"), dataClass);
    }

    /**
     * order with unknown property.
     */
    @Test(expected = IllegalArgumentException.class)
    public void order_unknown_property() {
        OutputPattern.compileOrder(list("UNKNOWN"), dataClass);
    }

    /**
     * order duplicate property.
     */
    @Test(expected = IllegalArgumentException.class)
    public void order_duplicate() {
        OutputPattern.compileOrder(list("+intValue", "- stringValue", "-intValue"), dataClass);
    }

    private List<String> list(String... values) {
        return Arrays.asList(values);
    }
}
