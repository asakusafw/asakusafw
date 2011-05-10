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
package com.asakusafw.testdriver.api;

import java.util.LinkedList;
import java.util.List;

import com.asakusafw.compiler.flow.FlowDescriptionDriver;
import com.asakusafw.vocabulary.flow.FlowDescription;

/**
 * フロー部品用のテストドライバクラス。
 */
public class FlowPartTestDriver {

    private List<FlowPartDriverInput<?>> inputs = new LinkedList<FlowPartDriverInput<?>>();
    private List<FlowPartDriverOutput<?>> outputs = new LinkedList<FlowPartDriverOutput<?>>();
    
    private FlowDescriptionDriver flowDescriptionDriver = new FlowDescriptionDriver();
    
    /**
     * テスト入力データを指定する。
     * 
     * @param <T> ModelType。
     * @param name 入力データ名。テストドライバに指定する入力データ間で一意の名前を指定する。
     * @param modelType ModelType。
     * @return テスト入力データオブジェクト。
     */
    public <T> FlowPartDriverInput<T> input(String name, Class<T> modelType) {
        FlowPartDriverInput<T> input = new FlowPartDriverInput<T>(flowDescriptionDriver, name, modelType);
        inputs.add(input);
        return input;
    }
    
    /**
     * テスト結果の出力データ（期待値データ）を指定する。
     * 
     * @param <T> ModelType。
     * @param name 出力データ名。テストドライバに指定する出力データ間で一意の名前を指定する。
     * @param modelType ModelType。
     * @return テスト入力データオブジェクト。
     */
    public <T> FlowPartDriverOutput<T> output(String name, Class<T> modelType) {
        FlowPartDriverOutput<T> output = new FlowPartDriverOutput<T>(flowDescriptionDriver, name, modelType);
        outputs.add(output);
        return output;
    }
    
    /**
     * フロー部品のテストを実行し、テスト結果を検証する。
     *
     * @param flowDescription フロー部品クラスのインスタンス
     */
    public void runTest(FlowDescription flowDescription) {
        // TODO impl
    }

}
