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
package com.asakusafw.yaess.tools.log.util;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Test for {@link LineSource}.
 */
public class LineSourceTest {

    /**
     * Simple test case.
     */
    @Test
    public void simple() {
        assertThat(consume("Hello, world!"), contains("Hello, world!"));
    }

    /**
     * Multiple lines.
     */
    @Test
    public void multiple() {
        assertThat(consume("Hello, world!\nThis is a test\n"), contains("Hello, world!", "This is a test"));
    }

    private List<String> consume(String lines) {
        try {
            List<String> results = new ArrayList<String>();
            LineSource source = new LineSource(new StringReader(lines));
            while (source.next()) {
                results.add(source.get());
            }
            source.close();
            return results;
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
