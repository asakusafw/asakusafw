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


import org.apache.hadoop.io.Writable;

import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.vocabulary.model.DataModel;
import com.asakusafw.vocabulary.model.JoinedModel;
import com.asakusafw.vocabulary.model.Key;
import com.asakusafw.vocabulary.model.ModelRef;
import com.asakusafw.vocabulary.model.Property;

/**
 * テーブル<code>EX1, EX2</code>を結合した結果のモデルクラス。
 * <p>
 * 以下のように結合されている:
 * </p>
 * <ul>
 * <li><code>EX1.VALUE == EX2.VALUE</code></li>
 * </ul>
 */
@Generated("JoinedModelEntityEmitter:0.0.1")
@DataModel
@JoinedModel(from = @ModelRef(type = Ex1.class, key = @Key(group = { "value" })), join = @ModelRef(type = Ex2.class, key = @Key(group = { "value" })))
@SuppressWarnings("deprecation")
public class ExJoined implements Writable {
    /**
     * カラム<code>EX1.SID</code>の内容のフィールド。
     */
    @Property(from = @Property.Source(declaring = Ex1.class, name = "sid"))
    private LongOption sid1 = new LongOption();
    /**
     * カラム<code>EX2.SID</code>の内容のフィールド。
     */
    @Property(join = @Property.Source(declaring = Ex2.class, name = "sid"))
    private LongOption sid2 = new LongOption();
    /**
     * カラム<code>EX1.VALUE</code>および<code>EX2.VALUE</code>の内容のフィールド。
     */
    @Property(from = @Property.Source(declaring = Ex1.class, name = "value"), join = @Property.Source(declaring = Ex2.class, name = "value"))
    private IntOption value = new IntOption();

    /**
     * カラム<code>EX1.SID</code>の内容を返す。
     *
     * @return カラム<code>EX1.SID</code>の内容
     * @throw NullPointerException 値に{@code null}が格納されていた場合
     */
    public long getSid1() {
        return this.sid1.get();
    }

    /**
     * カラム<code>EX1.SID</code>の内容を変更する。
     *
     * @param sid1
     *            設定する値
     */
    public void setSid1(long sid1) {
        this.sid1.modify(sid1);
    }

    /**
     * {@link #getSid1()}の情報を{@code null}も表現可能な形式で返す。
     *
     * @return オプション形式の{@link #getSid1()}
     */
    public LongOption getSid1Option() {
        return this.sid1;
    }

    /**
     * {@link #setSid1(long)}を{@code null}が指定可能なオプションの形式で設定する。
     *
     * @param sid1
     *            設定する値、消去する場合は{@code null}
     */
    public void setSid1Option(LongOption sid1) {
        this.sid1.copyFrom(sid1);
    }

    /**
     * カラム<code>EX2.SID</code>の内容を返す。
     *
     * @return カラム<code>EX2.SID</code>の内容
     * @throw NullPointerException 値に{@code null}が格納されていた場合
     */
    public long getSid2() {
        return this.sid2.get();
    }

    /**
     * カラム<code>EX2.SID</code>の内容を変更する。
     *
     * @param sid2
     *            設定する値
     */
    public void setSid2(long sid2) {
        this.sid2.modify(sid2);
    }

    /**
     * {@link #getSid2()}の情報を{@code null}も表現可能な形式で返す。
     *
     * @return オプション形式の{@link #getSid2()}
     */
    public LongOption getSid2Option() {
        return this.sid2;
    }

    /**
     * {@link #setSid2(long)}を{@code null}が指定可能なオプションの形式で設定する。
     *
     * @param sid2
     *            設定する値、消去する場合は{@code null}
     */
    public void setSid2Option(LongOption sid2) {
        this.sid2.copyFrom(sid2);
    }

    /**
     * カラム<code>EX1.VALUE</code>および<code>EX2.VALUE</code>の内容を返す。
     *
     * @return カラム<code>EX1.VALUE</code>および<code>EX2.VALUE</code>の内容
     * @throw NullPointerException 値に{@code null}が格納されていた場合
     */
    public int getValue() {
        return this.value.get();
    }

    /**
     * カラム<code>EX1.VALUE</code>および<code>EX2.VALUE</code>の内容を変更する。
     *
     * @param value
     *            設定する値
     */
    public void setValue(int value) {
        this.value.modify(value);
    }

    /**
     * {@link #getValue()}の情報を{@code null}も表現可能な形式で返す。
     *
     * @return オプション形式の{@link #getValue()}
     */
    public IntOption getValueOption() {
        return this.value;
    }

    /**
     * {@link #setValue(int)}を{@code null}が指定可能なオプションの形式で設定する。
     *
     * @param value
     *            設定する値、消去する場合は{@code null}
     */
    public void setValueOption(IntOption value) {
        this.value.copyFrom(value);
    }

    /**
     * 指定のオブジェクトが持つプロパティの内容を全てこのオブジェクトにコピーする。
     *
     * @param source
     *            コピー元になるオブジェクト
     */
    public void copyFrom(ExJoined source) {
        this.sid1.copyFrom(source.sid1);
        this.sid2.copyFrom(source.sid2);
        this.value.copyFrom(source.value);
    }

    @Override
    public void write(DataOutput out) throws IOException {
        sid1.write(out);
        sid2.write(out);
        value.write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        sid1.readFields(in);
        sid2.readFields(in);
        value.readFields(in);
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result += prime * result + sid1.hashCode();
        result += prime * result + sid2.hashCode();
        result += prime * result + value.hashCode();
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
        ExJoined other = (ExJoined) obj;
        if (this.sid1.equals(other.sid1) == false) {
            return false;
        }
        if (this.sid2.equals(other.sid2) == false) {
            return false;
        }
        if (this.value.equals(other.value) == false) {
            return false;
        }
        return true;
    }

    /**
     * 2つのモデルオブジェクトを結合した結果を、このオブジェクトに設定する。
     *
     * @param left
     *            結合されるモデルのオブジェクト
     * @param right
     *            結合するモデルのオブジェクト
     */
    public void joinFrom(Ex1 left, Ex2 right) {
        this.sid1.copyFrom(left.getSidOption());
        this.sid2.copyFrom(right.getSidOption());
        this.value.copyFrom(left.getValueOption());
    }

    /**
     * この結合されたモデルを、もとの2つのモデルに分解して書き出す。
     *
     * @param left
     *            結合されるモデルのオブジェクト
     * @param right
     *            結合するモデルのオブジェクト
     */
    public void splitInto(Ex1 left, Ex2 right) {
        left.getSidOption().copyFrom(this.sid1);
        right.getSidOption().copyFrom(this.sid2);
        left.getValueOption().copyFrom(this.value);
        right.getValueOption().copyFrom(this.value);
    }
}