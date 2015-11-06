/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
import com.asakusafw.runtime.model.DataModel;
import com.asakusafw.runtime.model.DataModelKind;
import com.asakusafw.runtime.model.PropertyOrder;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.StringOption;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
/**
 * A data model class that represents part2.
 */
@DataModelKind("DMDL")@PropertyOrder({"sid", "string"}) public class Part2 implements DataModel<Part2>, Writable {
    private final LongOption sid = new LongOption();
    private final StringOption string = new StringOption();
    @Override@SuppressWarnings("deprecation") public void reset() {
        this.sid.setNull();
        this.string.setNull();
    }
    @Override@SuppressWarnings("deprecation") public void copyFrom(Part2 other) {
        this.sid.copyFrom(other.sid);
        this.string.copyFrom(other.string);
    }
    /**
     * Returns sid.
     * @return sid
     * @throws NullPointerException if sid is <code>null</code>
     */
    public long getSid() {
        return this.sid.get();
    }
    /**
     * Sets sid.
     * @param value the value
     */
    @SuppressWarnings("deprecation") public void setSid(long value) {
        this.sid.modify(value);
    }
    /**
     * Returns sid which may be represent <code>null</code>.
     * @return sid
     */
    public LongOption getSidOption() {
        return this.sid;
    }
    /**
     * Sets sid.
     * @param option the value, or <code>null</code> to set this property to <code>null</code>
     */
    @SuppressWarnings("deprecation") public void setSidOption(LongOption option) {
        this.sid.copyFrom(option);
    }
    /**
     * Returns string.
     * @return string
     * @throws NullPointerException if string is <code>null</code>
     */
    public Text getString() {
        return this.string.get();
    }
    /**
     * Sets string.
     * @param value the value
     */
    @SuppressWarnings("deprecation") public void setString(Text value) {
        this.string.modify(value);
    }
    /**
     * Returns string which may be represent <code>null</code>.
     * @return string
     */
    public StringOption getStringOption() {
        return this.string;
    }
    /**
     * Sets string.
     * @param option the value, or <code>null</code> to set this property to <code>null</code>
     */
    @SuppressWarnings("deprecation") public void setStringOption(StringOption option) {
        this.string.copyFrom(option);
    }
    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{");
        result.append("class=part2");
        result.append(", sid=");
        result.append(this.sid);
        result.append(", string=");
        result.append(this.string);
        result.append("}");
        return result.toString();
    }
    @Override public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + sid.hashCode();
        result = prime * result + string.hashCode();
        return result;
    }
    @Override public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }
        if(obj == null) {
            return false;
        }
        if(this.getClass() != obj.getClass()) {
            return false;
        }
        Part2 other = (Part2) obj;
        if(this.sid.equals(other.sid) == false) {
            return false;
        }
        if(this.string.equals(other.string) == false) {
            return false;
        }
        return true;
    }
    /**
     * Returns string.
     * @return string
     * @throws NullPointerException if string is <code>null</code>
     */
    public String getStringAsString() {
        return this.string.getAsString();
    }
    /**
     * Returns string.
     * @param string0 the value
     */
    @SuppressWarnings("deprecation") public void setStringAsString(String string0) {
        this.string.modify(string0);
    }
    @Override public void write(DataOutput out) throws IOException {
        sid.write(out);
        string.write(out);
    }
    @Override public void readFields(DataInput in) throws IOException {
        sid.readFields(in);
        string.readFields(in);
    }
}