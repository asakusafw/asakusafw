/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.compiler.operator.model;

import com.asakusafw.vocabulary.model.DataModel;
import com.asakusafw.vocabulary.model.JoinedModel;
import com.asakusafw.vocabulary.model.Key;
import com.asakusafw.vocabulary.model.ModelRef;

/**
 * ダミーのテーブルモデル。
 */
@DataModel
@JoinedModel(
    from = @ModelRef(type = MockHoge.class, key = @Key(group = "value")),
    join = @ModelRef(type = MockFoo.class, key = @Key(group = "value"))
)
public class MockJoined implements JoinedModel.Interface<MockJoined, MockHoge, MockFoo> {

    /**
     * Hogeの唯一のプロパティ
     */
    public int hogeValue;

    /**
     * Fooの唯一のプロパティ
     */
    public int fooValue;

    @Override
    public void copyFrom(MockJoined source) {
        return;
    }

    @Override
    public void joinFrom(MockHoge left, MockFoo right) {
        hogeValue = left.value;
        fooValue = right.value;
    }

    @Override
    public void splitInto(MockHoge left, MockFoo right) {
        left.value = hogeValue;
        right.value = fooValue;
    }
}
