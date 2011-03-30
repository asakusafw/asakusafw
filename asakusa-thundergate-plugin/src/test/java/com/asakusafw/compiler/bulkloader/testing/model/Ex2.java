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
package com.asakusafw.compiler.bulkloader.testing.model;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import javax.annotation.Generated;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.runtime.value.DateTimeOption;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.StringOption;
import com.asakusafw.vocabulary.model.DataModel;
import com.asakusafw.vocabulary.model.Property;
import com.asakusafw.vocabulary.model.TableModel;

/**
 * テーブル<code>EX2</code>を表すモデルクラス。
 */
@Generated("TableModelEntityEmitter:0.0.1")
@DataModel
@TableModel(name = "EX2", columns = { "SID", "VALUE", "STRING",
        "LAST_UPDATE_TIME", "JOBFLOW_SID", "CACHE_FILE_SID" }, primary = { "SID" })
@SuppressWarnings("deprecation")
public class Ex2 implements Writable {
    /**
     * カラム<code>SID</code>を表すフィールド。
     */
    @Property(name = "SID")
    private LongOption sid = new LongOption();
    /**
     * カラム<code>VALUE</code>を表すフィールド。
     */
    @Property(name = "VALUE")
    private IntOption value = new IntOption();
    /**
     * カラム<code>STRING</code>を表すフィールド。
     */
    @Property(name = "STRING")
    private StringOption string = new StringOption();
    /**
     * カラム<code>LAST_UPDATE_TIME</code>を表すフィールド。
     */
    @Property(name = "LAST_UPDATE_TIME")
    private DateTimeOption lastUpdateTime = new DateTimeOption();
    /**
     * カラム<code>JOBFLOW_SID</code>を表すフィールド。
     */
    @Property(name = "JOBFLOW_SID")
    private IntOption jobflowSid = new IntOption();
    /**
     * カラム<code>CACHE_FILE_SID</code>を表すフィールド。
     */
    @Property(name = "CACHE_FILE_SID")
    private StringOption cacheFileSid = new StringOption();

    /**
     * カラム<code>SID</code>の値を返す。
     *
     * @return カラム<code>SID</code>の値
     * @throw NullPointerException 値に{@code null}が格納されていた場合
     */
    public long getSid() {
        return this.sid.get();
    }

    /**
     * カラム<code>SID</code>の値を変更する。
     *
     * @param sid
     *            設定する値
     */
    public void setSid(long sid) {
        this.sid.modify(sid);
    }

    /**
     * {@link #getSid()}の情報を{@code null}も表現可能な形式で返す。
     *
     * @return オプション形式の{@link #getSid()}
     */
    public LongOption getSidOption() {
        return this.sid;
    }

    /**
     * {@link #setSid(long)}を{@code null}が指定可能なオプションの形式で設定する。
     *
     * @param sid
     *            設定する値、消去する場合は{@code null}
     */
    public void setSidOption(LongOption sid) {
        this.sid.copyFrom(sid);
    }

    /**
     * カラム<code>VALUE</code>の値を返す。
     *
     * @return カラム<code>VALUE</code>の値
     * @throw NullPointerException 値に{@code null}が格納されていた場合
     */
    public int getValue() {
        return this.value.get();
    }

    /**
     * カラム<code>VALUE</code>の値を変更する。
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
     * カラム<code>STRING</code>の値を返す。
     *
     * @return カラム<code>STRING</code>の値
     * @throw NullPointerException 値に{@code null}が格納されていた場合
     */
    public Text getString() {
        return this.string.get();
    }

    /**
     * カラム<code>STRING</code>の値を変更する。
     *
     * @param string
     *            設定する値
     */
    public void setString(Text string) {
        this.string.modify(string);
    }

    /**
     * カラム<code>STRING</code>の値を返す。
     *
     * @return カラム<code>STRING</code>の値
     * @throw NullPointerException 値に{@code null}が格納されていた場合
     */
    public String getStringAsString() {
        return this.string.getAsString();
    }

    /**
     * カラム<code>STRING</code>の値を変更する。
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
     * カラム<code>LAST_UPDATE_TIME</code>の値を返す。
     *
     * @return カラム<code>LAST_UPDATE_TIME</code>の値
     * @throw NullPointerException 値に{@code null}が格納されていた場合
     */
    public DateTime getLastUpdateTime() {
        return this.lastUpdateTime.get();
    }

    /**
     * カラム<code>LAST_UPDATE_TIME</code>の値を変更する。
     *
     * @param lastUpdateTime
     *            設定する値
     */
    public void setLastUpdateTime(DateTime lastUpdateTime) {
        this.lastUpdateTime.modify(lastUpdateTime);
    }

    /**
     * {@link #getLastUpdateTime()}の情報を{@code null}も表現可能な形式で返す。
     *
     * @return オプション形式の{@link #getLastUpdateTime()}
     */
    public DateTimeOption getLastUpdateTimeOption() {
        return this.lastUpdateTime;
    }

    /**
     * {@link #setLastUpdateTime(DateTime)}を{@code null}が指定可能なオプションの形式で設定する。
     *
     * @param lastUpdateTime
     *            設定する値、消去する場合は{@code null}
     */
    public void setLastUpdateTimeOption(DateTimeOption lastUpdateTime) {
        this.lastUpdateTime.copyFrom(lastUpdateTime);
    }

    /**
     * カラム<code>JOBFLOW_SID</code>の値を返す。
     *
     * @return カラム<code>JOBFLOW_SID</code>の値
     * @throw NullPointerException 値に{@code null}が格納されていた場合
     */
    public int getJobflowSid() {
        return this.jobflowSid.get();
    }

    /**
     * カラム<code>JOBFLOW_SID</code>の値を変更する。
     *
     * @param jobflowSid
     *            設定する値
     */
    public void setJobflowSid(int jobflowSid) {
        this.jobflowSid.modify(jobflowSid);
    }

    /**
     * {@link #getJobflowSid()}の情報を{@code null}も表現可能な形式で返す。
     *
     * @return オプション形式の{@link #getJobflowSid()}
     */
    public IntOption getJobflowSidOption() {
        return this.jobflowSid;
    }

    /**
     * {@link #setJobflowSid(int)}を{@code null}が指定可能なオプションの形式で設定する。
     *
     * @param jobflowSid
     *            設定する値、消去する場合は{@code null}
     */
    public void setJobflowSidOption(IntOption jobflowSid) {
        this.jobflowSid.copyFrom(jobflowSid);
    }

    /**
     * カラム<code>CACHE_FILE_SID</code>の値を返す。
     *
     * @return カラム<code>CACHE_FILE_SID</code>の値
     * @throw NullPointerException 値に{@code null}が格納されていた場合
     */
    public Text getCacheFileSid() {
        return this.cacheFileSid.get();
    }

    /**
     * カラム<code>CACHE_FILE_SID</code>の値を変更する。
     *
     * @param cacheFileSid
     *            設定する値
     */
    public void setCacheFileSid(Text cacheFileSid) {
        this.cacheFileSid.modify(cacheFileSid);
    }

    /**
     * カラム<code>CACHE_FILE_SID</code>の値を返す。
     *
     * @return カラム<code>CACHE_FILE_SID</code>の値
     * @throw NullPointerException 値に{@code null}が格納されていた場合
     */
    public String getCacheFileSidAsString() {
        return this.cacheFileSid.getAsString();
    }

    /**
     * カラム<code>CACHE_FILE_SID</code>の値を変更する。
     *
     * @param cacheFileSid
     *            設定する値
     */
    public void setCacheFileSidAsString(String cacheFileSid) {
        this.cacheFileSid.modify(cacheFileSid);
    }

    /**
     * {@link #getCacheFileSid()}の情報を{@code null}も表現可能な形式で返す。
     *
     * @return オプション形式の{@link #getCacheFileSid()}
     */
    public StringOption getCacheFileSidOption() {
        return this.cacheFileSid;
    }

    /**
     * {@link #setCacheFileSid(Text)}を{@code null}が指定可能なオプションの形式で設定する。
     *
     * @param cacheFileSid
     *            設定する値、消去する場合は{@code null}
     */
    public void setCacheFileSidOption(StringOption cacheFileSid) {
        this.cacheFileSid.copyFrom(cacheFileSid);
    }

    /**
     * 指定のオブジェクトが持つプロパティの内容を全てこのオブジェクトにコピーする。
     *
     * @param source
     *            コピー元になるオブジェクト
     */
    public void copyFrom(Ex2 source) {
        this.sid.copyFrom(source.sid);
        this.value.copyFrom(source.value);
        this.string.copyFrom(source.string);
        this.lastUpdateTime.copyFrom(source.lastUpdateTime);
        this.jobflowSid.copyFrom(source.jobflowSid);
        this.cacheFileSid.copyFrom(source.cacheFileSid);
    }

    @Override
    public void write(DataOutput out) throws IOException {
        sid.write(out);
        value.write(out);
        string.write(out);
        lastUpdateTime.write(out);
        jobflowSid.write(out);
        cacheFileSid.write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        sid.readFields(in);
        value.readFields(in);
        string.readFields(in);
        lastUpdateTime.readFields(in);
        jobflowSid.readFields(in);
        cacheFileSid.readFields(in);
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result += prime * result + sid.hashCode();
        result += prime * result + value.hashCode();
        result += prime * result + string.hashCode();
        result += prime * result + lastUpdateTime.hashCode();
        result += prime * result + jobflowSid.hashCode();
        result += prime * result + cacheFileSid.hashCode();
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
        Ex2 other = (Ex2) obj;
        if (this.sid.equals(other.sid) == false) {
            return false;
        }
        if (this.value.equals(other.value) == false) {
            return false;
        }
        if (this.string.equals(other.string) == false) {
            return false;
        }
        if (this.lastUpdateTime.equals(other.lastUpdateTime) == false) {
            return false;
        }
        if (this.jobflowSid.equals(other.jobflowSid) == false) {
            return false;
        }
        if (this.cacheFileSid.equals(other.cacheFileSid) == false) {
            return false;
        }
        return true;
    }
}