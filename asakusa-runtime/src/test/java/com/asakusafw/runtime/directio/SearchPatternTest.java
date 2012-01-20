/**
 * Copyright 2012 Asakusa Framework Team.
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
package com.asakusafw.runtime.directio;

import static com.asakusafw.runtime.directio.SearchPattern.PatternElementKind.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;

import com.asakusafw.runtime.directio.SearchPattern;
import com.asakusafw.runtime.directio.SearchPattern.PatternElement;
import com.asakusafw.runtime.directio.SearchPattern.PatternElementKind;
import com.asakusafw.runtime.directio.SearchPattern.Segment;

/**
 * Test for {@link SearchPattern}.
 */
public class SearchPatternTest {

    /**
     * A traverse pattern.
     */
    @Test
    public void traverse() {
        SearchPattern compiled = SearchPattern.compile("**");
        List<Segment> segments = compiled.getSegments();
        assertThat(segments.size(), is(1));

        assertThat(segments.get(0).isTraverse(), is(true));
        assertThat(segments.get(0).getElements(), is(kind()));
    }

    /**
     * A token.
     */
    @Test
    public void token() {
        SearchPattern compiled = SearchPattern.compile("a");
        assertThat(compiled.containsVariables(), is(false));
        List<Segment> segments = compiled.getSegments();
        assertThat(segments.size(), is(1));

        assertThat(segments.get(0).isTraverse(), is(false));
        assertThat(segments.get(0).getElements(), is(kind(TOKEN)));
        assertThat(segments.get(0).getElements(), is(token("a")));
    }

    /**
     * A wildcard pattern.
     */
    @Test
    public void wildcard() {
        SearchPattern compiled = SearchPattern.compile("*");
        assertThat(compiled.containsVariables(), is(false));
        List<Segment> segments = compiled.getSegments();
        assertThat(segments.size(), is(1));

        assertThat(segments.get(0).isTraverse(), is(false));
        assertThat(segments.get(0).getElements(), is(kind(WILDCARD)));
        assertThat(segments.get(0).getElements(), is(token("*")));
    }

    /**
     * A variable pattern.
     */
    @Test
    public void variable() {
        SearchPattern compiled = SearchPattern.compile("${v}");
        assertThat(compiled.containsVariables(), is(true));
        List<Segment> segments = compiled.getSegments();
        assertThat(segments.size(), is(1));

        assertThat(segments.get(0).isTraverse(), is(false));
        assertThat(segments.get(0).getElements(), is(kind(VARIABLE)));
        assertThat(segments.get(0).getElements(), is(token("${v}")));
    }

    /**
     * An empty variable pattern.
     */
    @Test
    public void variable_empty() {
        SearchPattern compiled = SearchPattern.compile("${}");
        List<Segment> segments = compiled.getSegments();
        assertThat(segments.size(), is(1));

        assertThat(segments.get(0).isTraverse(), is(false));
        assertThat(segments.get(0).getElements(), is(kind(VARIABLE)));
        assertThat(segments.get(0).getElements(), is(token("${}")));
    }

    /**
     * A selection pattern.
     */
    @Test
    public void selection() {
        SearchPattern compiled = SearchPattern.compile("{alpha|beta|gamma}");
        assertThat(compiled.containsVariables(), is(false));
        List<Segment> segments = compiled.getSegments();
        assertThat(segments.size(), is(1));

        assertThat(segments.get(0).isTraverse(), is(false));
        assertThat(segments.get(0).getElements(), is(kind(SELECTION)));
        assertThat(segments.get(0).getElements(), is(token("{alpha|beta|gamma}")));
    }

    /**
     * A contains wildcard pattern.
     */
    @Test
    public void containsWildcard() {
        SearchPattern compiled = SearchPattern.compile("data-*.csv");
        List<Segment> segments = compiled.getSegments();
        assertThat(segments.size(), is(1));

        assertThat(segments.get(0).isTraverse(), is(false));
        assertThat(segments.get(0).getElements(), is(kind(TOKEN, WILDCARD, TOKEN)));
        assertThat(segments.get(0).getElements(), is(token("data-", "*", ".csv")));
    }

    /**
     * A selection pattern.
     */
    @Test
    public void selection_containsEmpty() {
        SearchPattern compiled = SearchPattern.compile("{alpha||gamma}");
        assertThat(compiled.containsVariables(), is(false));
        List<Segment> segments = compiled.getSegments();
        assertThat(segments.size(), is(1));

        assertThat(segments.get(0).isTraverse(), is(false));
        assertThat(segments.get(0).getElements(), is(kind(SELECTION)));
        assertThat(segments.get(0).getElements(), is(token("{alpha||gamma}")));
    }

    /**
     * A selection pattern.
     */
    @Test
    public void selection_empty() {
        SearchPattern compiled = SearchPattern.compile("{}");
        assertThat(compiled.containsVariables(), is(false));
        List<Segment> segments = compiled.getSegments();
        assertThat(segments.size(), is(1));

        assertThat(segments.get(0).isTraverse(), is(false));
        assertThat(segments.get(0).getElements(), is(kind(SELECTION)));
        assertThat(segments.get(0).getElements(), is(token("{}")));
    }

    /**
     * multiple segments.
     */
    @Test
    public void segments() {
        SearchPattern compiled = SearchPattern.compile("alpha/beta/gamma");
        List<Segment> segments = compiled.getSegments();
        assertThat(segments.size(), is(3));

        assertThat(segments.get(0).isTraverse(), is(false));
        assertThat(segments.get(0).getElements(), is(kind(TOKEN)));
        assertThat(segments.get(0).getElements(), is(token("alpha")));

        assertThat(segments.get(1).isTraverse(), is(false));
        assertThat(segments.get(1).getElements(), is(kind(TOKEN)));
        assertThat(segments.get(1).getElements(), is(token("beta")));

        assertThat(segments.get(2).isTraverse(), is(false));
        assertThat(segments.get(2).getElements(), is(kind(TOKEN)));
        assertThat(segments.get(2).getElements(), is(token("gamma")));
    }

    /**
     * A complex pattern (all csv).
     */
    @Test
    public void all_csv() {
        SearchPattern compiled = SearchPattern.compile("**/*.csv");
        List<Segment> segments = compiled.getSegments();
        assertThat(segments.size(), is(2));

        assertThat(segments.get(0).isTraverse(), is(true));

        assertThat(segments.get(1).isTraverse(), is(false));
        assertThat(segments.get(1).getElements(), is(kind(WILDCARD, TOKEN)));
        assertThat(segments.get(1).getElements(), is(token("*", ".csv")));
    }

    /**
     * A complex pattern.
     */
    @Test
    public void complex() {
        SearchPattern compiled = SearchPattern.compile("alpha/**/{beta|gamma}/${date}-*.csv");
        assertThat(compiled.containsVariables(), is(true));

        List<Segment> segments = compiled.getSegments();
        assertThat(segments.size(), is(4));

        assertThat(segments.get(0).isTraverse(), is(false));
        assertThat(segments.get(0).getElements(), is(kind(TOKEN)));
        assertThat(segments.get(0).getElements(), is(token("alpha")));

        assertThat(segments.get(1).isTraverse(), is(true));

        assertThat(segments.get(2).isTraverse(), is(false));
        assertThat(segments.get(2).getElements(), is(kind(SELECTION)));
        assertThat(segments.get(2).getElements(), is(token("{beta|gamma}")));

        assertThat(segments.get(3).isTraverse(), is(false));
        assertThat(segments.get(3).getElements(), is(kind(VARIABLE, TOKEN, WILDCARD, TOKEN)));
        assertThat(segments.get(3).getElements(), is(token("${date}", "-", "*", ".csv")));
    }

    /**
     * An invalid wildcard.
     */
    @Test(expected = IllegalArgumentException.class)
    public void consecutive_wildcard() {
        SearchPattern.compile("**.csv");
    }

    /**
     * A doller.
     */
    @Test(expected = IllegalArgumentException.class)
    public void doller() {
        SearchPattern.compile("$.csv");
    }

    /**
     * An unclosed variable.
     */
    @Test(expected = IllegalArgumentException.class)
    public void variable_unclosed() {
        SearchPattern.compile("${csv");
    }

    /**
     * An unclosed selection.
     */
    @Test(expected = IllegalArgumentException.class)
    public void selection_unclosed() {
        SearchPattern.compile("{data");
    }

    /**
     * An invalid character in selection.
     */
    @Test(expected = IllegalArgumentException.class)
    public void selection_invalid_character() {
        SearchPattern.compile("{*}");
    }

    /**
     * An invalid character.
     */
    @Test(expected = IllegalArgumentException.class)
    public void invalid_character() {
        SearchPattern.compile("\\data.csv");
    }

    private Matcher<List<PatternElement>> kind(final PatternElementKind... kinds) {
        return new BaseMatcher<List<PatternElement>>() {

            @Override
            public boolean matches(Object obj) {
                @SuppressWarnings("unchecked")
                List<PatternElement> elements = (List<PatternElement>) obj;
                if (elements.size() != kinds.length) {
                    return false;
                }
                for (int i = 0; i < kinds.length; i++) {
                    if (elements.get(i).getKind() != kinds[i]) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void describeTo(Description desc) {
                desc.appendText(Arrays.toString(kinds));
            }
        };
    }

    private Matcher<List<PatternElement>> token(final String... tokens) {
        return new BaseMatcher<List<PatternElement>>() {

            @Override
            public boolean matches(Object obj) {
                @SuppressWarnings("unchecked")
                List<PatternElement> elements = (List<PatternElement>) obj;
                if (elements.size() != tokens.length) {
                    return false;
                }
                for (int i = 0; i < tokens.length; i++) {
                    if (elements.get(i).getToken().equals(tokens[i]) == false) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void describeTo(Description desc) {
                desc.appendText(Arrays.toString(tokens));
            }
        };
    }
}
