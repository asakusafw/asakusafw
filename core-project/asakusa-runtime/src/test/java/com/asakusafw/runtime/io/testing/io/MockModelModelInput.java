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
package com.asakusafw.runtime.io.testing.io;

import java.io.IOException;

import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.RecordParser;
import com.asakusafw.runtime.io.testing.model.MockModel;


/**
 * {@link MockModel}のレコード入力。
 */
public class MockModelModelInput implements ModelInput<MockModel> {

    private RecordParser parser;

    /**
     * インスタンスを生成する。
     * @param parser 利用するパーサー
     */
    public MockModelModelInput(RecordParser parser) {
        this.parser = parser;
    }

    @Override
    public boolean readTo(MockModel model) throws IOException {
        if (parser.next() == false) {
            return false;
        }
        parser.fill(model.value);
        return true;
    }

    @Override
    public void close() throws IOException {
        parser.close();
    }
}
