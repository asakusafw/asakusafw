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
package com.asakusafw.utils.java.internal.model.syntax;

import org.junit.Test;

/**
 * {@link SimpleNameImpl}のテスト。
 */
public class SimpleNameImplTest {

    /**
     * 通常の名前。
     */
    @Test
    public void camel() {
        SimpleNameImpl name = new SimpleNameImpl();
        name.setToken("simpleNameImpl");
    }

    /**
     * 一文字。
     */
    @Test
    public void singleChar() {
        SimpleNameImpl name = new SimpleNameImpl();
        name.setToken("a");
    }

    /**
     * 後ろに数値。
     */
    @Test
    public void trailingNumbers() {
        SimpleNameImpl name = new SimpleNameImpl();
        name.setToken("a1");
    }

    /**
     * ドル。
     */
    @Test
    public void dollar() {
        SimpleNameImpl name = new SimpleNameImpl();
        name.setToken("$");
    }

    /**
     * 空文字列。
     */
    @Test(expected = IllegalArgumentException.class)
    public void empty() {
        SimpleNameImpl name = new SimpleNameImpl();
        name.setToken("");
    }

    /**
     * 数値。
     */
    @Test(expected = IllegalArgumentException.class)
    public void isNumber() {
        SimpleNameImpl name = new SimpleNameImpl();
        name.setToken("125");
    }

    /**
     * 不正な文字。
     */
    @Test(expected = IllegalArgumentException.class)
    public void hasInvalid() {
        SimpleNameImpl name = new SimpleNameImpl();
        name.setToken("a-");
    }

    /**
     * キーワード。
     */
    @Test(expected = IllegalArgumentException.class)
    public void keyword() {
        SimpleNameImpl name = new SimpleNameImpl();
        name.setToken("void");
    }

    /**
     * リテラル true。
     */
    @Test(expected = IllegalArgumentException.class)
    public void booleanLiteralTrue() {
        SimpleNameImpl name = new SimpleNameImpl();
        name.setToken("true");
    }

    /**
     * リテラル false。
     */
    @Test(expected = IllegalArgumentException.class)
    public void booleanLiteralFalse() {
        SimpleNameImpl name = new SimpleNameImpl();
        name.setToken("false");
    }

    /**
     * リテラル null。
     */
    @Test(expected = IllegalArgumentException.class)
    public void nullLiteral() {
        SimpleNameImpl name = new SimpleNameImpl();
        name.setToken("null");
    }
}
