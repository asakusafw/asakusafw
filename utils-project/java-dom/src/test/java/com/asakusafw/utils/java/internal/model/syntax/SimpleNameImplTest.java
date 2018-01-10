/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.utils.java.internal.model.syntax;

import org.junit.Test;

/**
 * test for {@link SimpleNameImpl}.
 */
public class SimpleNameImplTest {

    /**
     * camel case.
     */
    @Test
    public void camel() {
        SimpleNameImpl name = new SimpleNameImpl();
        name.setToken("simpleNameImpl");
    }

    /**
     * single character.
     */
    @Test
    public void singleChar() {
        SimpleNameImpl name = new SimpleNameImpl();
        name.setToken("a");
    }

    /**
     * digits after a character.
     */
    @Test
    public void trailingNumbers() {
        SimpleNameImpl name = new SimpleNameImpl();
        name.setToken("a1");
    }

    /**
     * $.
     */
    @Test
    public void dollar() {
        SimpleNameImpl name = new SimpleNameImpl();
        name.setToken("$");
    }

    /**
     * empty string.
     */
    @Test(expected = IllegalArgumentException.class)
    public void empty() {
        SimpleNameImpl name = new SimpleNameImpl();
        name.setToken("");
    }

    /**
     * numeric string.
     */
    @Test(expected = IllegalArgumentException.class)
    public void isNumber() {
        SimpleNameImpl name = new SimpleNameImpl();
        name.setToken("125");
    }

    /**
     * w/ invalid character.
     */
    @Test(expected = IllegalArgumentException.class)
    public void hasInvalid() {
        SimpleNameImpl name = new SimpleNameImpl();
        name.setToken("a-");
    }

    /**
     * keyword.
     */
    @Test(expected = IllegalArgumentException.class)
    public void keyword() {
        SimpleNameImpl name = new SimpleNameImpl();
        name.setToken("void");
    }

    /**
     * literal - true.
     */
    @Test(expected = IllegalArgumentException.class)
    public void booleanLiteralTrue() {
        SimpleNameImpl name = new SimpleNameImpl();
        name.setToken("true");
    }

    /**
     * literal - false.
     */
    @Test(expected = IllegalArgumentException.class)
    public void booleanLiteralFalse() {
        SimpleNameImpl name = new SimpleNameImpl();
        name.setToken("false");
    }

    /**
     * literal - null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void nullLiteral() {
        SimpleNameImpl name = new SimpleNameImpl();
        name.setToken("null");
    }
}
