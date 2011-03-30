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
package com.asakusafw.compiler.flow.testing.model;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import javax.annotation.Generated;


import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.vocabulary.model.DataModel;
import com.asakusafw.vocabulary.model.Key;
import com.asakusafw.vocabulary.model.ModelRef;
import com.asakusafw.vocabulary.model.Property;
import com.asakusafw.vocabulary.model.SummarizedModel;

/**
 * テーブル<code>EX_SUMMARIZED</code>を集計した結果のモデルクラス。
 * <p>
 * グループ化はそれぞれ <code>[STRING]</code> で行っている。
 * </p>
 */
@Generated("SummarizeModelEntityEmitter:0.0.1")
@DataModel
@SummarizedModel(from = @ModelRef(type = Ex1.class, key = @Key(group = { "string" })))
@SuppressWarnings("deprecation")
public class ExSummarized implements Writable {
    /**
     * グループ化したカラム<code>STRING</code>の内容のフィールド。
     */
    @Property(from = @Property.Source(declaring = Ex1.class, name = "string"), aggregator = Property.Aggregator.IDENT)
    private StringOption string = new StringOption();
    /**
     * カラム<code>VALUE</code>を<code>SUM()</code>で集約した結果のフィールド。
     */
    @Property(from = @Property.Source(declaring = Ex1.class, name = "value"), aggregator = Property.Aggregator.SUM)
    private LongOption value = new LongOption();
    /**
     * カラム<code>SID</code>を<code>COUNT()</code>で集約した結果のフィールド。
     */
    @Property(from = @Property.Source(declaring = Ex1.class, name = "sid"), aggregator = Property.Aggregator.COUNT)
    private LongOption count = new LongOption();

    /**
     * グループ化したカラム<code>STRING</code>の内容を返す。
     *
     * @return グループ化したカラム<code>STRING</code>の内容
     * @throw NullPointerException 値に{@code null}が格納されていた場合
     */
    public Text getString() {
        return this.string.get();
    }

    /**
     * グループ化したカラム<code>STRING</code>の内容を変更する。
     *
     * @param string
     *            設定する値
     */
    public void setString(Text string) {
        this.string.modify(string);
    }

    /**
     * グループ化したカラム<code>STRING</code>の内容を返す。
     *
     * @return グループ化したカラム<code>STRING</code>の内容
     * @throw NullPointerException 値に{@code null}が格納されていた場合
     */
    public String getStringAsString() {
        return this.string.getAsString();
    }

    /**
     * グループ化したカラム<code>STRING</code>の内容を変更する。
     *
     * @param string
     *            設定する値
     */
    public void setStringAsString(String string) {
        this.string.modify(string);
    }

    /**
     * {@link #getString()}の情報を{@code null}も表現可能な形式で返す。
     *
     * @return オプション形式の{@link #getString()}
     */
    public StringOption getStringOption() {
        return this.string;
    }

    /**
     * {@link #setString(Text)}を{@code null}が指定可能なオプションの形式で設定する。
     *
     * @param string
     *            設定する値、消去する場合は{@code null}
     */
    public void setStringOption(StringOption string) {
        this.string.copyFrom(string);
    }

    /**
     * カラム<code>VALUE</code>を<code>SUM()</code>で集約した結果を返す。
     *
     * @return カラム<code>VALUE</code>を<code>SUM()</code>で集約した結果
     * @throw NullPointerException 値に{@code null}が格納されていた場合
     */
    public long getValue() {
        return this.value.get();
    }

    /**
     * カラム<code>VALUE</code>を<code>SUM()</code>で集約した結果を変更する。
     *
     * @param value
     *            設定する値
     */
    public void setValue(long value) {
        this.value.modify(value);
    }

    /**
     * {@link #getValue()}の情報を{@code null}も表現可能な形式で返す。
     *
     * @return オプション形式の{@link #getValue()}
     */
    public LongOption getValueOption() {
        return this.value;
    }

    /**
     * {@link #setValue(long)}を{@code null}が指定可能なオプションの形式で設定する。
     *
     * @param value
     *            設定する値、消去する場合は{@code null}
     */
    public void setValueOption(LongOption value) {
        this.value.copyFrom(value);
    }

    /**
     * カラム<code>SID</code>を<code>COUNT()</code>で集約した結果を返す。
     *
     * @return カラム<code>SID</code>を<code>COUNT()</code>で集約した結果
     * @throw NullPointerException 値に{@code null}が格納されていた場合
     */
    public long getCount() {
        return this.count.get();
    }

    /**
     * カラム<code>SID</code>を<code>COUNT()</code>で集約した結果を変更する。
     *
     * @param count
     *            設定する値
     */
    public void setCount(long count) {
        this.count.modify(count);
    }

    /**
     * {@link #getCount()}の情報を{@code null}も表現可能な形式で返す。
     *
     * @return オプション形式の{@link #getCount()}
     */
    public LongOption getCountOption() {
        return this.count;
    }

    /**
     * {@link #setCount(long)}を{@code null}が指定可能なオプションの形式で設定する。
     *
     * @param count
     *            設定する値、消去する場合は{@code null}
     */
    public void setCountOption(LongOption count) {
        this.count.copyFrom(count);
    }

    /**
     * 指定のオブジェクトが持つプロパティの内容を全てこのオブジェクトにコピーする。
     *
     * @param source
     *            コピー元になるオブジェクト
     */
    public void copyFrom(ExSummarized source) {
        this.string.copyFrom(source.string);
        this.value.copyFrom(source.value);
        this.count.copyFrom(source.count);
    }

    @Override
    public void write(DataOutput out) throws IOException {
        string.write(out);
        value.write(out);
        count.write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        string.readFields(in);
        value.readFields(in);
        count.readFields(in);
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result += prime * result + string.hashCode();
        result += prime * result + value.hashCode();
        result += prime * result + count.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        ExSummarized other = (ExSummarized) obj;
        if (this.string.equals(other.string) == false) {
            return false;
        }
        if (this.value.equals(other.value) == false) {
            return false;
        }
        if (this.count.equals(other.count) == false) {
            return false;
        }
        return true;
    }

    /**
     * 指定のモデルを最初の要素として、このモデルの集計結果を初期化する。
     *
     * @param original
     *            最初の要素となるモデル
     */
    public void startSummarization(Ex1 original) {
        this.string.modify(original.getString());
        this.value.modify(original.getValue());
        this.count.modify(1);
    }

    /**
     * このモデルに、指定のモデルの集計結果を合成する。
     *
     * @param original
     *            合成するモデル
     */
    public void combineSummarization(ExSummarized original) {
        this.value.add(original.value);
        this.count.add(original.count);
    }
}