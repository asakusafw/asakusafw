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
package test.modelgen.table.model;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import javax.annotation.Generated;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.vocabulary.model.DataModel;
import com.asakusafw.vocabulary.model.Property;
import com.asakusafw.vocabulary.model.TableModel;
/**
 * テーブル<code>export_temp_test_02</code>を表すモデルクラス。
 */
@Generated("TableModelEntityEmitter:0.0.1")@DataModel@TableModel(name = "export_temp_test_02", columns = {"TEXTDATA1",
            "INTDATA1"}, primary = {"TEXTDATA1"})@SuppressWarnings("deprecation") public class ExportTempTest02
        implements Writable {
    /**
     * カラム<code>TEXTDATA1</code>を表すフィールド。
     */
    @Property(name = "TEXTDATA1") private StringOption textdata1 = new StringOption();
    /**
     * カラム<code>INTDATA1</code>を表すフィールド。
     */
    @Property(name = "INTDATA1") private IntOption intdata1 = new IntOption();
    /**
     * カラム<code>TEXTDATA1</code>の値を返す。
     * @return カラム<code>TEXTDATA1</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Text getTextdata1() {
        return this.textdata1.get();
    }
    /**
     * カラム<code>TEXTDATA1</code>の値を変更する。
     * @param textdata1 設定する値
     */
    public void setTextdata1(Text textdata1) {
        this.textdata1.modify(textdata1);
    }
    /**
     * カラム<code>TEXTDATA1</code>の値を返す。
     * @return カラム<code>TEXTDATA1</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public String getTextdata1AsString() {
        return this.textdata1.getAsString();
    }
    /**
     * カラム<code>TEXTDATA1</code>の値を変更する。
     * @param textdata1 設定する値
     */
    public void setTextdata1AsString(String textdata1) {
        this.textdata1.modify(textdata1);
    }
    /**
     * {@link #getTextdata1()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getTextdata1()}
     */
    public StringOption getTextdata1Option() {
        return this.textdata1;
    }
    /**
     * {@link #setTextdata1(Text)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param textdata1 設定する値、消去する場合は{@code null}
     */
    public void setTextdata1Option(StringOption textdata1) {
        this.textdata1.copyFrom(textdata1);
    }
    /**
     * カラム<code>INTDATA1</code>の値を返す。
     * @return カラム<code>INTDATA1</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public int getIntdata1() {
        return this.intdata1.get();
    }
    /**
     * カラム<code>INTDATA1</code>の値を変更する。
     * @param intdata1 設定する値
     */
    public void setIntdata1(int intdata1) {
        this.intdata1.modify(intdata1);
    }
    /**
     * {@link #getIntdata1()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getIntdata1()}
     */
    public IntOption getIntdata1Option() {
        return this.intdata1;
    }
    /**
     * {@link #setIntdata1(int)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param intdata1 設定する値、消去する場合は{@code null}
     */
    public void setIntdata1Option(IntOption intdata1) {
        this.intdata1.copyFrom(intdata1);
    }
    /**
     * 指定のオブジェクトが持つプロパティの内容を全てこのオブジェクトにコピーする。
     * @param source コピー元になるオブジェクト
     */
    public void copyFrom(ExportTempTest02 source) {
        this.textdata1.copyFrom(source.textdata1);
        this.intdata1.copyFrom(source.intdata1);
    }
    @Override public void write(DataOutput out) throws IOException {
        textdata1.write(out);
        intdata1.write(out);
    }
    @Override public void readFields(DataInput in) throws IOException {
        textdata1.readFields(in);
        intdata1.readFields(in);
    }
    @Override public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + textdata1.hashCode();
        result = prime * result + intdata1.hashCode();
        return result;
    }
    @Override public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }
        if(obj == null) {
            return false;
        }
        if(this.getClass()!= obj.getClass()) {
            return false;
        }
        ExportTempTest02 other = (ExportTempTest02) obj;
        if(this.textdata1.equals(other.textdata1)== false) {
            return false;
        }
        if(this.intdata1.equals(other.intdata1)== false) {
            return false;
        }
        return true;
    }
    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{");
        result.append("class=export_temp_test_02");
        result.append(", textdata1=");
        result.append(this.textdata1);
        result.append(", intdata1=");
        result.append(this.intdata1);
        result.append("}");
        return result.toString();
    }
}