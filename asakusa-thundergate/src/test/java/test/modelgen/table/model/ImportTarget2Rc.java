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
package test.modelgen.table.model;
import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.runtime.value.DateTimeOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.vocabulary.model.DataModel;
import com.asakusafw.vocabulary.model.Property;
import com.asakusafw.vocabulary.model.TableModel;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import javax.annotation.Generated;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
/**
 * テーブル<code>import_target2_rc</code>を表すモデルクラス。
 */
@Generated("TableModelEntityEmitter:0.0.1")@DataModel@TableModel(name = "import_target2_rc", columns = {"SID",
            "CACHE_FILE_SID", "CREATE_DATETIME"}, primary = {"SID"})@SuppressWarnings("deprecation") public class
        ImportTarget2Rc implements Writable {
    /**
     * カラム<code>SID</code>を表すフィールド。
     */
    @Property(name = "SID") private LongOption sid = new LongOption();
    /**
     * カラム<code>CACHE_FILE_SID</code>を表すフィールド。
     */
    @Property(name = "CACHE_FILE_SID") private StringOption cacheFileSid = new StringOption();
    /**
     * カラム<code>CREATE_DATETIME</code>を表すフィールド。
     */
    @Property(name = "CREATE_DATETIME") private DateTimeOption createDatetime = new DateTimeOption();
    /**
     * カラム<code>SID</code>の値を返す。
     * @return カラム<code>SID</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public long getSid() {
        return this.sid.get();
    }
    /**
     * カラム<code>SID</code>の値を変更する。
     * @param sid 設定する値
     */
    public void setSid(long sid) {
        this.sid.modify(sid);
    }
    /**
     * {@link #getSid()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getSid()}
     */
    public LongOption getSidOption() {
        return this.sid;
    }
    /**
     * {@link #setSid(long)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param sid 設定する値、消去する場合は{@code null}
     */
    public void setSidOption(LongOption sid) {
        this.sid.copyFrom(sid);
    }
    /**
     * カラム<code>CACHE_FILE_SID</code>の値を返す。
     * @return カラム<code>CACHE_FILE_SID</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Text getCacheFileSid() {
        return this.cacheFileSid.get();
    }
    /**
     * カラム<code>CACHE_FILE_SID</code>の値を変更する。
     * @param cacheFileSid 設定する値
     */
    public void setCacheFileSid(Text cacheFileSid) {
        this.cacheFileSid.modify(cacheFileSid);
    }
    /**
     * カラム<code>CACHE_FILE_SID</code>の値を返す。
     * @return カラム<code>CACHE_FILE_SID</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public String getCacheFileSidAsString() {
        return this.cacheFileSid.getAsString();
    }
    /**
     * カラム<code>CACHE_FILE_SID</code>の値を変更する。
     * @param cacheFileSid 設定する値
     */
    public void setCacheFileSidAsString(String cacheFileSid) {
        this.cacheFileSid.modify(cacheFileSid);
    }
    /**
     * {@link #getCacheFileSid()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getCacheFileSid()}
     */
    public StringOption getCacheFileSidOption() {
        return this.cacheFileSid;
    }
    /**
     * {@link #setCacheFileSid(Text)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param cacheFileSid 設定する値、消去する場合は{@code null}
     */
    public void setCacheFileSidOption(StringOption cacheFileSid) {
        this.cacheFileSid.copyFrom(cacheFileSid);
    }
    /**
     * カラム<code>CREATE_DATETIME</code>の値を返す。
     * @return カラム<code>CREATE_DATETIME</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public DateTime getCreateDatetime() {
        return this.createDatetime.get();
    }
    /**
     * カラム<code>CREATE_DATETIME</code>の値を変更する。
     * @param createDatetime 設定する値
     */
    public void setCreateDatetime(DateTime createDatetime) {
        this.createDatetime.modify(createDatetime);
    }
    /**
     * {@link #getCreateDatetime()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getCreateDatetime()}
     */
    public DateTimeOption getCreateDatetimeOption() {
        return this.createDatetime;
    }
    /**
     * {@link #setCreateDatetime(DateTime)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param createDatetime 設定する値、消去する場合は{@code null}
     */
    public void setCreateDatetimeOption(DateTimeOption createDatetime) {
        this.createDatetime.copyFrom(createDatetime);
    }
    /**
     * 指定のオブジェクトが持つプロパティの内容を全てこのオブジェクトにコピーする。
     * @param source コピー元になるオブジェクト
     */
    public void copyFrom(ImportTarget2Rc source) {
        this.sid.copyFrom(source.sid);
        this.cacheFileSid.copyFrom(source.cacheFileSid);
        this.createDatetime.copyFrom(source.createDatetime);
    }
    @Override public void write(DataOutput out) throws IOException {
        sid.write(out);
        cacheFileSid.write(out);
        createDatetime.write(out);
    }
    @Override public void readFields(DataInput in) throws IOException {
        sid.readFields(in);
        cacheFileSid.readFields(in);
        createDatetime.readFields(in);
    }
    @Override public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + sid.hashCode();
        result = prime * result + cacheFileSid.hashCode();
        result = prime * result + createDatetime.hashCode();
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
        ImportTarget2Rc other = (ImportTarget2Rc) obj;
        if(this.sid.equals(other.sid)== false) {
            return false;
        }
        if(this.cacheFileSid.equals(other.cacheFileSid)== false) {
            return false;
        }
        if(this.createDatetime.equals(other.createDatetime)== false) {
            return false;
        }
        return true;
    }
    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{");
        result.append("class=import_target2_rc");
        result.append(", sid=");
        result.append(this.sid);
        result.append(", cacheFileSid=");
        result.append(this.cacheFileSid);
        result.append(", createDatetime=");
        result.append(this.createDatetime);
        result.append("}");
        return result.toString();
    }
}