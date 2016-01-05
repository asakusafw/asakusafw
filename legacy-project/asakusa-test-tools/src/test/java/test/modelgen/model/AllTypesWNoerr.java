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
package test.modelgen.model;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.math.BigDecimal;

import javax.annotation.Generated;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import com.asakusafw.runtime.value.ByteOption;
import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateOption;
import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.runtime.value.DateTimeOption;
import com.asakusafw.runtime.value.DecimalOption;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.ShortOption;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.vocabulary.model.Property;
import com.asakusafw.vocabulary.model.TableModel;
/**
 * テーブル<code>all_types_w_noerr</code>を表すモデルクラス。
 */
@TableModel(name = "all_types_w_noerr", primary = {})@Generated("TableModelEntityEmitter")@SuppressWarnings(
        "deprecation") public class AllTypesWNoerr implements Writable {
    /**
     * カラム<code>C_TAG</code>を表すフィールド。
     */
    @Property(name = "C_TAG") private StringOption cTag = new StringOption();
    /**
     * カラム<code>C_COMMENT</code>を表すフィールド。
     */
    @Property(name = "C_COMMENT") private StringOption cComment = new StringOption();
    /**
     * カラム<code>C_BIGINT</code>を表すフィールド。
     */
    @Property(name = "C_BIGINT") private LongOption cBigint = new LongOption();
    /**
     * カラム<code>C_INT</code>を表すフィールド。
     */
    @Property(name = "C_INT") private IntOption cInt = new IntOption();
    /**
     * カラム<code>C_SMALLINT</code>を表すフィールド。
     */
    @Property(name = "C_SMALLINT") private ShortOption cSmallint = new ShortOption();
    /**
     * カラム<code>C_TINYINT</code>を表すフィールド。
     */
    @Property(name = "C_TINYINT") private ByteOption cTinyint = new ByteOption();
    /**
     * カラム<code>C_CHAR</code>を表すフィールド。
     */
    @Property(name = "C_CHAR") private StringOption cChar = new StringOption();
    /**
     * カラム<code>C_DATETIME</code>を表すフィールド。
     */
    @Property(name = "C_DATETIME") private DateTimeOption cDatetime = new DateTimeOption();
    /**
     * カラム<code>C_DATE</code>を表すフィールド。
     */
    @Property(name = "C_DATE") private DateOption cDate = new DateOption();
    /**
     * カラム<code>C_DECIMAL20_0</code>を表すフィールド。
     */
    @Property(name = "C_DECIMAL20_0") private DecimalOption cDecimal200 = new DecimalOption();
    /**
     * カラム<code>C_DECIMAL25_5</code>を表すフィールド。
     */
    @Property(name = "C_DECIMAL25_5") private DecimalOption cDecimal255 = new DecimalOption();
    /**
     * カラム<code>C_VCHAR</code>を表すフィールド。
     */
    @Property(name = "C_VCHAR") private StringOption cVchar = new StringOption();
    /**
     * カラム<code>C_TAG</code>の値を返す。
     * @return カラム<code>C_TAG</code>の値
     */
    public Text getCTag() {
        return this.cTag.get();
    }
    /**
     * カラム<code>C_TAG</code>の値を変更する。
     * @param cTag 設定する値
     */
    public void setCTag(Text cTag) {
        this.cTag.modify(cTag);
    }
    /**
     * カラム<code>C_TAG</code>の値を返す。
     * @return カラム<code>C_TAG</code>の値
     */
    public String getCTagAsString() {
        return this.cTag.getAsString();
    }
    /**
     * カラム<code>C_TAG</code>の値を変更する。
     * @param cTag 設定する値
     */
    public void setCTagAsString(String cTag) {
        this.cTag.modify(cTag);
    }
    /**
     * {@link#getCTag()}の情報を{@code null}も表現可能な形式で返す
     * @return オプション形式の{@link#getCTag()}
     */
    public StringOption getCTagOption() {
        return this.cTag;
    }
    /**
     * {@link#setCTag(Text)}を{@code null}が指定可能なオプションの形式で設定する
     * @param cTag 設定する値、消去する場合は{@code null}
     */
    public void setCTagOption(StringOption cTag) {
        this.cTag.copyFrom(cTag);
    }
    /**
     * カラム<code>C_COMMENT</code>の値を返す。
     * @return カラム<code>C_COMMENT</code>の値
     */
    public Text getCComment() {
        return this.cComment.get();
    }
    /**
     * カラム<code>C_COMMENT</code>の値を変更する。
     * @param cComment 設定する値
     */
    public void setCComment(Text cComment) {
        this.cComment.modify(cComment);
    }
    /**
     * カラム<code>C_COMMENT</code>の値を返す。
     * @return カラム<code>C_COMMENT</code>の値
     */
    public String getCCommentAsString() {
        return this.cComment.getAsString();
    }
    /**
     * カラム<code>C_COMMENT</code>の値を変更する。
     * @param cComment 設定する値
     */
    public void setCCommentAsString(String cComment) {
        this.cComment.modify(cComment);
    }
    /**
     * {@link#getCComment()}の情報を{@code null}も表現可能な形式で返す
     * @return オプション形式の{@link#getCComment()}
     */
    public StringOption getCCommentOption() {
        return this.cComment;
    }
    /**
     * {@link#setCComment(Text)}を{@code null}が指定可能なオプションの形式で設定する
     * @param cComment 設定する値、消去する場合は{@code null}
     */
    public void setCCommentOption(StringOption cComment) {
        this.cComment.copyFrom(cComment);
    }
    /**
     * カラム<code>C_BIGINT</code>の値を返す。
     * @return カラム<code>C_BIGINT</code>の値
     */
    public long getCBigint() {
        return this.cBigint.get();
    }
    /**
     * カラム<code>C_BIGINT</code>の値を変更する。
     * @param cBigint 設定する値
     */
    public void setCBigint(long cBigint) {
        this.cBigint.modify(cBigint);
    }
    /**
     * {@link#getCBigint()}の情報を{@code null}も表現可能な形式で返す
     * @return オプション形式の{@link#getCBigint()}
     */
    public LongOption getCBigintOption() {
        return this.cBigint;
    }
    /**
     * {@link#setCBigint(long)}を{@code null}が指定可能なオプションの形式で設定する
     * @param cBigint 設定する値、消去する場合は{@code null}
     */
    public void setCBigintOption(LongOption cBigint) {
        this.cBigint.copyFrom(cBigint);
    }
    /**
     * カラム<code>C_INT</code>の値を返す。
     * @return カラム<code>C_INT</code>の値
     */
    public int getCInt() {
        return this.cInt.get();
    }
    /**
     * カラム<code>C_INT</code>の値を変更する。
     * @param cInt 設定する値
     */
    public void setCInt(int cInt) {
        this.cInt.modify(cInt);
    }
    /**
     * {@link#getCInt()}の情報を{@code null}も表現可能な形式で返す
     * @return オプション形式の{@link#getCInt()}
     */
    public IntOption getCIntOption() {
        return this.cInt;
    }
    /**
     * {@link#setCInt(int)}を{@code null}が指定可能なオプションの形式で設定する
     * @param cInt 設定する値、消去する場合は{@code null}
     */
    public void setCIntOption(IntOption cInt) {
        this.cInt.copyFrom(cInt);
    }
    /**
     * カラム<code>C_SMALLINT</code>の値を返す。
     * @return カラム<code>C_SMALLINT</code>の値
     */
    public short getCSmallint() {
        return this.cSmallint.get();
    }
    /**
     * カラム<code>C_SMALLINT</code>の値を変更する。
     * @param cSmallint 設定する値
     */
    public void setCSmallint(short cSmallint) {
        this.cSmallint.modify(cSmallint);
    }
    /**
     * {@link#getCSmallint()}の情報を{@code null}も表現可能な形式で返す
     * @return オプション形式の{@link#getCSmallint()}
     */
    public ShortOption getCSmallintOption() {
        return this.cSmallint;
    }
    /**
     * {@link#setCSmallint(short)}を{@code null}が指定可能なオプションの形式で設定する
     * @param cSmallint 設定する値、消去する場合は{@code null}
     */
    public void setCSmallintOption(ShortOption cSmallint) {
        this.cSmallint.copyFrom(cSmallint);
    }
    /**
     * カラム<code>C_TINYINT</code>の値を返す。
     * @return カラム<code>C_TINYINT</code>の値
     */
    public byte getCTinyint() {
        return this.cTinyint.get();
    }
    /**
     * カラム<code>C_TINYINT</code>の値を変更する。
     * @param cTinyint 設定する値
     */
    public void setCTinyint(byte cTinyint) {
        this.cTinyint.modify(cTinyint);
    }
    /**
     * {@link#getCTinyint()}の情報を{@code null}も表現可能な形式で返す
     * @return オプション形式の{@link#getCTinyint()}
     */
    public ByteOption getCTinyintOption() {
        return this.cTinyint;
    }
    /**
     * {@link#setCTinyint(byte)}を{@code null}が指定可能なオプションの形式で設定する
     * @param cTinyint 設定する値、消去する場合は{@code null}
     */
    public void setCTinyintOption(ByteOption cTinyint) {
        this.cTinyint.copyFrom(cTinyint);
    }
    /**
     * カラム<code>C_CHAR</code>の値を返す。
     * @return カラム<code>C_CHAR</code>の値
     */
    public Text getCChar() {
        return this.cChar.get();
    }
    /**
     * カラム<code>C_CHAR</code>の値を変更する。
     * @param cChar 設定する値
     */
    public void setCChar(Text cChar) {
        this.cChar.modify(cChar);
    }
    /**
     * カラム<code>C_CHAR</code>の値を返す。
     * @return カラム<code>C_CHAR</code>の値
     */
    public String getCCharAsString() {
        return this.cChar.getAsString();
    }
    /**
     * カラム<code>C_CHAR</code>の値を変更する。
     * @param cChar 設定する値
     */
    public void setCCharAsString(String cChar) {
        this.cChar.modify(cChar);
    }
    /**
     * {@link#getCChar()}の情報を{@code null}も表現可能な形式で返す
     * @return オプション形式の{@link#getCChar()}
     */
    public StringOption getCCharOption() {
        return this.cChar;
    }
    /**
     * {@link#setCChar(Text)}を{@code null}が指定可能なオプションの形式で設定する
     * @param cChar 設定する値、消去する場合は{@code null}
     */
    public void setCCharOption(StringOption cChar) {
        this.cChar.copyFrom(cChar);
    }
    /**
     * カラム<code>C_DATETIME</code>の値を返す。
     * @return カラム<code>C_DATETIME</code>の値
     */
    public DateTime getCDatetime() {
        return this.cDatetime.get();
    }
    /**
     * カラム<code>C_DATETIME</code>の値を変更する。
     * @param cDatetime 設定する値
     */
    public void setCDatetime(DateTime cDatetime) {
        this.cDatetime.modify(cDatetime);
    }
    /**
     * {@link#getCDatetime()}の情報を{@code null}も表現可能な形式で返す
     * @return オプション形式の{@link#getCDatetime()}
     */
    public DateTimeOption getCDatetimeOption() {
        return this.cDatetime;
    }
    /**
     * {@link#setCDatetime(DateTime)}を{@code null}が指定可能なオプションの形式で設定する
     * @param cDatetime 設定する値、消去する場合は{@code null}
     */
    public void setCDatetimeOption(DateTimeOption cDatetime) {
        this.cDatetime.copyFrom(cDatetime);
    }
    /**
     * カラム<code>C_DATE</code>の値を返す。
     * @return カラム<code>C_DATE</code>の値
     */
    public Date getCDate() {
        return this.cDate.get();
    }
    /**
     * カラム<code>C_DATE</code>の値を変更する。
     * @param cDate 設定する値
     */
    public void setCDate(Date cDate) {
        this.cDate.modify(cDate);
    }
    /**
     * {@link#getCDate()}の情報を{@code null}も表現可能な形式で返す
     * @return オプション形式の{@link#getCDate()}
     */
    public DateOption getCDateOption() {
        return this.cDate;
    }
    /**
     * {@link#setCDate(Date)}を{@code null}が指定可能なオプションの形式で設定する
     * @param cDate 設定する値、消去する場合は{@code null}
     */
    public void setCDateOption(DateOption cDate) {
        this.cDate.copyFrom(cDate);
    }
    /**
     * カラム<code>C_DECIMAL20_0</code>の値を返す。
     * @return カラム<code>C_DECIMAL20_0</code>の値
     */
    public BigDecimal getCDecimal200() {
        return this.cDecimal200.get();
    }
    /**
     * カラム<code>C_DECIMAL20_0</code>の値を変更する。
     * @param cDecimal200 設定する値
     */
    public void setCDecimal200(BigDecimal cDecimal200) {
        this.cDecimal200.modify(cDecimal200);
    }
    /**
     * {@link#getCDecimal200()}の情報を{@code null}も表現可能な形式で返す
     * @return オプション形式の{@link#getCDecimal200()}
     */
    public DecimalOption getCDecimal200Option() {
        return this.cDecimal200;
    }
    /**
     * {@link#setCDecimal200(BigDecimal)}を{@code null}が指定可能なオプションの形式で設定する
     * @param cDecimal200 設定する値、消去する場合は{@code null}
     */
    public void setCDecimal200Option(DecimalOption cDecimal200) {
        this.cDecimal200.copyFrom(cDecimal200);
    }
    /**
     * カラム<code>C_DECIMAL25_5</code>の値を返す。
     * @return カラム<code>C_DECIMAL25_5</code>の値
     */
    public BigDecimal getCDecimal255() {
        return this.cDecimal255.get();
    }
    /**
     * カラム<code>C_DECIMAL25_5</code>の値を変更する。
     * @param cDecimal255 設定する値
     */
    public void setCDecimal255(BigDecimal cDecimal255) {
        this.cDecimal255.modify(cDecimal255);
    }
    /**
     * {@link#getCDecimal255()}の情報を{@code null}も表現可能な形式で返す
     * @return オプション形式の{@link#getCDecimal255()}
     */
    public DecimalOption getCDecimal255Option() {
        return this.cDecimal255;
    }
    /**
     * {@link#setCDecimal255(BigDecimal)}を{@code null}が指定可能なオプションの形式で設定する
     * @param cDecimal255 設定する値、消去する場合は{@code null}
     */
    public void setCDecimal255Option(DecimalOption cDecimal255) {
        this.cDecimal255.copyFrom(cDecimal255);
    }
    /**
     * カラム<code>C_VCHAR</code>の値を返す。
     * @return カラム<code>C_VCHAR</code>の値
     */
    public Text getCVchar() {
        return this.cVchar.get();
    }
    /**
     * カラム<code>C_VCHAR</code>の値を変更する。
     * @param cVchar 設定する値
     */
    public void setCVchar(Text cVchar) {
        this.cVchar.modify(cVchar);
    }
    /**
     * カラム<code>C_VCHAR</code>の値を返す。
     * @return カラム<code>C_VCHAR</code>の値
     */
    public String getCVcharAsString() {
        return this.cVchar.getAsString();
    }
    /**
     * カラム<code>C_VCHAR</code>の値を変更する。
     * @param cVchar 設定する値
     */
    public void setCVcharAsString(String cVchar) {
        this.cVchar.modify(cVchar);
    }
    /**
     * {@link#getCVchar()}の情報を{@code null}も表現可能な形式で返す
     * @return オプション形式の{@link#getCVchar()}
     */
    public StringOption getCVcharOption() {
        return this.cVchar;
    }
    /**
     * {@link#setCVchar(Text)}を{@code null}が指定可能なオプションの形式で設定する
     * @param cVchar 設定する値、消去する場合は{@code null}
     */
    public void setCVcharOption(StringOption cVchar) {
        this.cVchar.copyFrom(cVchar);
    }
    /**
     * 指定のオブジェクトが持つプロパティの内容を全てこのオブジェクトにコピーする。
     * @param source コピー元になるオブジェクト
     */
    public void copyFrom(AllTypesWNoerr source) {
        this.cTag.copyFrom(source.cTag);
        this.cComment.copyFrom(source.cComment);
        this.cBigint.copyFrom(source.cBigint);
        this.cInt.copyFrom(source.cInt);
        this.cSmallint.copyFrom(source.cSmallint);
        this.cTinyint.copyFrom(source.cTinyint);
        this.cChar.copyFrom(source.cChar);
        this.cDatetime.copyFrom(source.cDatetime);
        this.cDate.copyFrom(source.cDate);
        this.cDecimal200.copyFrom(source.cDecimal200);
        this.cDecimal255.copyFrom(source.cDecimal255);
        this.cVchar.copyFrom(source.cVchar);
    }
    @Override public void write(DataOutput out) throws IOException {
        cTag.write(out);
        cComment.write(out);
        cBigint.write(out);
        cInt.write(out);
        cSmallint.write(out);
        cTinyint.write(out);
        cChar.write(out);
        cDatetime.write(out);
        cDate.write(out);
        cDecimal200.write(out);
        cDecimal255.write(out);
        cVchar.write(out);
    }
    @Override public void readFields(DataInput in) throws IOException {
        cTag.readFields(in);
        cComment.readFields(in);
        cBigint.readFields(in);
        cInt.readFields(in);
        cSmallint.readFields(in);
        cTinyint.readFields(in);
        cChar.readFields(in);
        cDatetime.readFields(in);
        cDate.readFields(in);
        cDecimal200.readFields(in);
        cDecimal255.readFields(in);
        cVchar.readFields(in);
    }
    @Override public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + cTag.hashCode();
        result = prime * result + cComment.hashCode();
        result = prime * result + cBigint.hashCode();
        result = prime * result + cInt.hashCode();
        result = prime * result + cSmallint.hashCode();
        result = prime * result + cTinyint.hashCode();
        result = prime * result + cChar.hashCode();
        result = prime * result + cDatetime.hashCode();
        result = prime * result + cDate.hashCode();
        result = prime * result + cDecimal200.hashCode();
        result = prime * result + cDecimal255.hashCode();
        result = prime * result + cVchar.hashCode();
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
        AllTypesWNoerr other = (AllTypesWNoerr) obj;
        if(this.cTag.equals(other.cTag)== false) {
            return false;
        }
        if(this.cComment.equals(other.cComment)== false) {
            return false;
        }
        if(this.cBigint.equals(other.cBigint)== false) {
            return false;
        }
        if(this.cInt.equals(other.cInt)== false) {
            return false;
        }
        if(this.cSmallint.equals(other.cSmallint)== false) {
            return false;
        }
        if(this.cTinyint.equals(other.cTinyint)== false) {
            return false;
        }
        if(this.cChar.equals(other.cChar)== false) {
            return false;
        }
        if(this.cDatetime.equals(other.cDatetime)== false) {
            return false;
        }
        if(this.cDate.equals(other.cDate)== false) {
            return false;
        }
        if(this.cDecimal200.equals(other.cDecimal200)== false) {
            return false;
        }
        if(this.cDecimal255.equals(other.cDecimal255)== false) {
            return false;
        }
        if(this.cVchar.equals(other.cVchar)== false) {
            return false;
        }
        return true;
    }
}