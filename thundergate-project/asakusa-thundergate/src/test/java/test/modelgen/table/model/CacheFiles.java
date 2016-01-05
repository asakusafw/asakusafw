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

import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.runtime.value.DateTimeOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.vocabulary.model.DataModel;
import com.asakusafw.vocabulary.model.Property;
import com.asakusafw.vocabulary.model.TableModel;
/**
 * テーブル<code>cache_files</code>を表すモデルクラス。
 */
@Generated("TableModelEntityEmitter:0.0.1")@DataModel@TableModel(name = "cache_files", columns = {"CACHE_FILE_SID",
            "FILE_PATH", "EXPIRATION_DATETIME"}, primary = {"CACHE_FILE_SID"})@SuppressWarnings("deprecation") public
        class CacheFiles implements Writable {
    /**
     * カラム<code>CACHE_FILE_SID</code>を表すフィールド。
     */
    @Property(name = "CACHE_FILE_SID") private LongOption cacheFileSid = new LongOption();
    /**
     * カラム<code>FILE_PATH</code>を表すフィールド。
     */
    @Property(name = "FILE_PATH") private StringOption filePath = new StringOption();
    /**
     * カラム<code>EXPIRATION_DATETIME</code>を表すフィールド。
     */
    @Property(name = "EXPIRATION_DATETIME") private DateTimeOption expirationDatetime = new DateTimeOption();
    /**
     * カラム<code>CACHE_FILE_SID</code>の値を返す。
     * @return カラム<code>CACHE_FILE_SID</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public long getCacheFileSid() {
        return this.cacheFileSid.get();
    }
    /**
     * カラム<code>CACHE_FILE_SID</code>の値を変更する。
     * @param cacheFileSid 設定する値
     */
    public void setCacheFileSid(long cacheFileSid) {
        this.cacheFileSid.modify(cacheFileSid);
    }
    /**
     * {@link #getCacheFileSid()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getCacheFileSid()}
     */
    public LongOption getCacheFileSidOption() {
        return this.cacheFileSid;
    }
    /**
     * {@link #setCacheFileSid(long)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param cacheFileSid 設定する値、消去する場合は{@code null}
     */
    public void setCacheFileSidOption(LongOption cacheFileSid) {
        this.cacheFileSid.copyFrom(cacheFileSid);
    }
    /**
     * カラム<code>FILE_PATH</code>の値を返す。
     * @return カラム<code>FILE_PATH</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public Text getFilePath() {
        return this.filePath.get();
    }
    /**
     * カラム<code>FILE_PATH</code>の値を変更する。
     * @param filePath 設定する値
     */
    public void setFilePath(Text filePath) {
        this.filePath.modify(filePath);
    }
    /**
     * カラム<code>FILE_PATH</code>の値を返す。
     * @return カラム<code>FILE_PATH</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public String getFilePathAsString() {
        return this.filePath.getAsString();
    }
    /**
     * カラム<code>FILE_PATH</code>の値を変更する。
     * @param filePath 設定する値
     */
    public void setFilePathAsString(String filePath) {
        this.filePath.modify(filePath);
    }
    /**
     * {@link #getFilePath()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getFilePath()}
     */
    public StringOption getFilePathOption() {
        return this.filePath;
    }
    /**
     * {@link #setFilePath(Text)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param filePath 設定する値、消去する場合は{@code null}
     */
    public void setFilePathOption(StringOption filePath) {
        this.filePath.copyFrom(filePath);
    }
    /**
     * カラム<code>EXPIRATION_DATETIME</code>の値を返す。
     * @return カラム<code>EXPIRATION_DATETIME</code>の値
     * @throws NullPointerException 値に{@code null}が格納されていた場合
     */
    public DateTime getExpirationDatetime() {
        return this.expirationDatetime.get();
    }
    /**
     * カラム<code>EXPIRATION_DATETIME</code>の値を変更する。
     * @param expirationDatetime 設定する値
     */
    public void setExpirationDatetime(DateTime expirationDatetime) {
        this.expirationDatetime.modify(expirationDatetime);
    }
    /**
     * {@link #getExpirationDatetime()}の情報を{@code null}も表現可能な形式で返す。
     * @return オプション形式の{@link #getExpirationDatetime()}
     */
    public DateTimeOption getExpirationDatetimeOption() {
        return this.expirationDatetime;
    }
    /**
     * {@link #setExpirationDatetime(DateTime)}を{@code null}が指定可能なオプションの形式で設定する。
     * @param expirationDatetime 設定する値、消去する場合は{@code null}
     */
    public void setExpirationDatetimeOption(DateTimeOption expirationDatetime) {
        this.expirationDatetime.copyFrom(expirationDatetime);
    }
    /**
     * 指定のオブジェクトが持つプロパティの内容を全てこのオブジェクトにコピーする。
     * @param source コピー元になるオブジェクト
     */
    public void copyFrom(CacheFiles source) {
        this.cacheFileSid.copyFrom(source.cacheFileSid);
        this.filePath.copyFrom(source.filePath);
        this.expirationDatetime.copyFrom(source.expirationDatetime);
    }
    @Override public void write(DataOutput out) throws IOException {
        cacheFileSid.write(out);
        filePath.write(out);
        expirationDatetime.write(out);
    }
    @Override public void readFields(DataInput in) throws IOException {
        cacheFileSid.readFields(in);
        filePath.readFields(in);
        expirationDatetime.readFields(in);
    }
    @Override public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + cacheFileSid.hashCode();
        result = prime * result + filePath.hashCode();
        result = prime * result + expirationDatetime.hashCode();
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
        CacheFiles other = (CacheFiles) obj;
        if(this.cacheFileSid.equals(other.cacheFileSid)== false) {
            return false;
        }
        if(this.filePath.equals(other.filePath)== false) {
            return false;
        }
        if(this.expirationDatetime.equals(other.expirationDatetime)== false) {
            return false;
        }
        return true;
    }
    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{");
        result.append("class=cache_files");
        result.append(", cacheFileSid=");
        result.append(this.cacheFileSid);
        result.append(", filePath=");
        result.append(this.filePath);
        result.append(", expirationDatetime=");
        result.append(this.expirationDatetime);
        result.append("}");
        return result.toString();
    }
}