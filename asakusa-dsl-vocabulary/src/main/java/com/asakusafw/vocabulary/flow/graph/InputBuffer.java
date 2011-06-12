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
package com.asakusafw.vocabulary.flow.graph;

/**
 * Listを引数に取る演算子の、入力バッファの性質。
 * @since 0.2.0
 */
public enum InputBuffer implements FlowElementAttribute {

    /**
     * 入力バッファをヒープ上に構築する。
     * <p>
     * 高速なバッファとして利用できる代わりに、
     * ヒープ上に乗る程度のサイズのグループのみを取り扱える。
     * </p>
     */
    HEAP,

    /**
     * 巨大な入力データを取り扱えるようにするが、<em>{@code List}の動作に制約がかかる</em>。
     * <p>
     * このオプションを指定した場合、それぞれの{@code List}からはひとつずつしかオブジェクトを取り出せなくなる。
     * 2つ以上オブジェクトを取り出した場合、最後に取り出したオブジェクト以外は
     * まったく別の内容に変更されている可能性がある。
     * また、リストから取り出したオブジェクトを変更しても、リストの別の要素にアクセスしただけで
     * 変更したオブジェクトの内容が失われる可能性がある。
     * </p>
     * <p>
     * つまり、次のようなプログラムを書いた場合の動作は保証されない。
     * </p>
<pre><code>
&#64;CoGroup(largeInput = true)
public void invalid(List&lt;Hoge&gt; list, Result&lt;Hoge&gt; result) {
    // 二つ取り出すとaの内容が保証されない
    Hoge a = list.get(0);
    Hoge b = list.get(1);

    // 内容を変更しても、別の要素を参照しただけでオブジェクトの内容が変わる場合がある
    b.setValue(100);
    list.get(2);
}
</code></pre>
     * <p>
     * 上記のようなプログラムを書きたい場合には、かならず<em>オブジェクトのコピーを作成する</em>こと。
     * </p>
<pre><code>
Hoge a = new Hoge();
Hoge b = new Hoge();

&#64;CoGroup(largeInput = true)
public void invalid(List&lt;Hoge&gt; list, Result&lt;Hoge&gt; result) {
    a.copyFrom(list.get(0));
    b.copyFrom(list.get(1));
    b.setValue(100);
    list.get(2);
    ...
}
</code></pre>
     * <p>
     * 下記のようにひとつずつ取り出して使う場合にはコピーは必要ない。
     * </p>
<pre><code>
&#64;CoGroup(largeInput = true)
public void invalid(List&lt;Hoge&gt; list, Result&lt;Hoge&gt; result) {
    for (Hoge hoge : list) {
        hoge.setValue(100);
        result.add(hoge);
    }
}
</code></pre>
     * <p>
     * このオプションを指定すると、演算子メソッドの引数に指定したリストは
     * 内部的に「スワップ領域」をもつようになる。
     * Java VMのヒープ上に配置されるオブジェクトは全体の一部で、残りはファイルシステム上などの
     * ヒープを利用しない領域に保存する。
     * </p>
     */
    SWAP,
}
