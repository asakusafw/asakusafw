/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package test.modelgen.io;
import java.io.IOException;

import test.modelgen.model.AllTypesWNoerr;

import com.asakusafw.runtime.io.ModelInput;
import com.asakusafw.runtime.io.RecordParser;
/**
 * TSVファイルなどのレコードを表すファイルを入力として{@link AllTypesWNoerr}を読み出す。
 */
public final class AllTypesWNoerrModelInput implements ModelInput<AllTypesWNoerr> {
    /**
     * 内部で利用するパーサー
     */
    private final RecordParser parser;
    /**
     * インスタンスを生成する
     * @param parser 利用するパーサー
     * @throw IllegalArgumentException 引数にnullが指定された場合
     */
    public AllTypesWNoerrModelInput(RecordParser parser) {
        if(parser == null) {
            throw new IllegalArgumentException();
        }
        this.parser = parser;
    }
    @Override public boolean readTo(AllTypesWNoerr model) throws IOException {
        if(parser.next()== false) {
            return false;
        }
        parser.fill(model.getCTagOption());
        parser.fill(model.getCCommentOption());
        parser.fill(model.getCBigintOption());
        parser.fill(model.getCIntOption());
        parser.fill(model.getCSmallintOption());
        parser.fill(model.getCTinyintOption());
        parser.fill(model.getCCharOption());
        parser.fill(model.getCDatetimeOption());
        parser.fill(model.getCDateOption());
        parser.fill(model.getCDecimal200Option());
        parser.fill(model.getCDecimal255Option());
        parser.fill(model.getCVcharOption());
        return true;
    }
    @Override public void close() throws IOException {
        parser.close();
    }
}