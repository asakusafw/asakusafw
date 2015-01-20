/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.runtime.io.testing.model;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import com.asakusafw.runtime.value.StringOption;

/**
 * 入出力確認用のモックモデル。
 */
public class MockModel {

    /**
     * 入力したデータを保持するフィールド。
     */
    public StringOption value = new StringOption();

    /**
     * 現在の値を確認する。
     * @param expect 期待値
     */
    public void assertValueIs(String expect) {
        assertThat(value.getAsString(), is(expect));
    }
}
