/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.runtime.flow;

import org.apache.hadoop.io.Writable;

/**
 * 合流地点に配置される演算子を処理する要素。
 * @param <V> 処理するデータの種類
 */
public abstract class Rendezvous<V extends Writable> {

    /**
     * {@link #begin()}メソッドの名前。
     */
    public static final String BEGIN = "begin";

    /**
     * {@link #process(Writable)}メソッドの名前。
     */
    public static final String PROCESS = "process";

    /**
     * {@link #end()}メソッドの名前。
     */
    public static final String END = "end";

    /**
     * 特定グループの処理を開始する際に起動される。
     */
    public abstract void begin();

    /**
     * 演算子への個々の入力に対する処理を実行する。
     * @param value 処理する値
     */
    public abstract void process(V value);

    /**
     * 演算子に対するすべての入力が完了した際に起動される。
     */
    public abstract void end();
}
