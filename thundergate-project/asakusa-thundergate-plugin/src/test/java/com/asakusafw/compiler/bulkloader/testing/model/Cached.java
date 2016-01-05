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
package com.asakusafw.compiler.bulkloader.testing.model;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

import com.asakusafw.compiler.bulkloader.testing.io.CachedInput;
import com.asakusafw.compiler.bulkloader.testing.io.CachedOutput;
import com.asakusafw.runtime.model.DataModel;
import com.asakusafw.runtime.model.DataModelKind;
import com.asakusafw.runtime.model.ModelInputLocation;
import com.asakusafw.runtime.model.ModelOutputLocation;
import com.asakusafw.runtime.model.PropertyOrder;
import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.runtime.value.DateTimeOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.thundergate.runtime.cache.ThunderGateCacheSupport;
import com.asakusafw.vocabulary.bulkloader.ColumnOrder;
import com.asakusafw.vocabulary.bulkloader.OriginalName;
import com.asakusafw.vocabulary.bulkloader.PrimaryKey;
/**
 * cachedを表すデータモデルクラス。
 */
@ColumnOrder(value = {"SID", "TIMESTAMP"})@DataModelKind("DMDL")@ModelInputLocation(CachedInput.class)@
        ModelOutputLocation(CachedOutput.class)@OriginalName(value = "CACHED")@PrimaryKey(value = {"sid"})@PropertyOrder
        ({"sid", "timestamp"}) public class Cached implements DataModel<Cached>, ThunderGateCacheSupport, Writable {
    private final LongOption sid = new LongOption();
    private final DateTimeOption timestamp = new DateTimeOption();
    @Override@SuppressWarnings("deprecation") public void reset() {
        this.sid.setNull();
        this.timestamp.setNull();
    }
    @Override@SuppressWarnings("deprecation") public void copyFrom(Cached other) {
        this.sid.copyFrom(other.sid);
        this.timestamp.copyFrom(other.timestamp);
    }
    /**
     * sidを返す。
     * @return sid
     * @throws NullPointerException sidの値が<code>null</code>である場合
     */
    public long getSid() {
        return this.sid.get();
    }
    /**
     * sidを設定する。
     * @param value 設定する値
     */
    @SuppressWarnings("deprecation") public void setSid(long value) {
        this.sid.modify(value);
    }
    /**
     * <code>null</code>を許すsidを返す。
     * @return sid
     */
    public LongOption getSidOption() {
        return this.sid;
    }
    /**
     * sidを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    @SuppressWarnings("deprecation") public void setSidOption(LongOption option) {
        this.sid.copyFrom(option);
    }
    /**
     * timestampを返す。
     * @return timestamp
     * @throws NullPointerException timestampの値が<code>null</code>である場合
     */
    public DateTime getTimestamp() {
        return this.timestamp.get();
    }
    /**
     * timestampを設定する。
     * @param value 設定する値
     */
    @SuppressWarnings("deprecation") public void setTimestamp(DateTime value) {
        this.timestamp.modify(value);
    }
    /**
     * <code>null</code>を許すtimestampを返す。
     * @return timestamp
     */
    public DateTimeOption getTimestampOption() {
        return this.timestamp;
    }
    /**
     * timestampを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    @SuppressWarnings("deprecation") public void setTimestampOption(DateTimeOption option) {
        this.timestamp.copyFrom(option);
    }
    @Override public long __tgc__DataModelVersion() {
        return -1214092834169263204L;
    }
    @Override public String __tgc__TimestampColumn() {
        return "TIMESTAMP";
    }
    @Override public long __tgc__SystemId() {
        return this.getSid();
    }
    @Override public boolean __tgc__Deleted() {
        return false;
    }
    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{");
        result.append("class=cached");
        result.append(", sid=");
        result.append(this.sid);
        result.append(", timestamp=");
        result.append(this.timestamp);
        result.append("}");
        return result.toString();
    }
    @Override public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + sid.hashCode();
        result = prime * result + timestamp.hashCode();
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
        Cached other = (Cached) obj;
        if(this.sid.equals(other.sid)== false) {
            return false;
        }
        if(this.timestamp.equals(other.timestamp)== false) {
            return false;
        }
        return true;
    }
    @Override public void write(DataOutput out) throws IOException {
        sid.write(out);
        timestamp.write(out);
    }
    @Override public void readFields(DataInput in) throws IOException {
        sid.readFields(in);
        timestamp.readFields(in);
    }
}