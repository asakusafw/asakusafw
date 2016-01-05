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
package com.asakusafw.bulkloader.cache;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.VLongWritable;
import org.apache.hadoop.io.Writable;

import com.asakusafw.thundergate.runtime.cache.ThunderGateCacheSupport;

/**
 * Cache testing data model.
 */
public class TestDataModel implements Writable, ThunderGateCacheSupport, Comparable<TestDataModel> {

    /**
     * system ID.
     */
    public final VLongWritable systemId = new VLongWritable();

    /**
     * content.
     */
    public final Text value = new Text();

    /**
     * deleted.
     */
    public final BooleanWritable deleted = new BooleanWritable();

    /**
     * Creates a copy of this.
     * @return the copy
     */
    public TestDataModel copy() {
        TestDataModel copy = new TestDataModel();
        copy.systemId.set(systemId.get());
        copy.value.set(value);
        copy.deleted.set(deleted.get());
        return copy;
    }

    @Override
    public long __tgc__DataModelVersion() {
        return 1L;
    }

    @Override
    public String __tgc__TimestampColumn() {
        return "DUMMY";
    }

    @Override
    public long __tgc__SystemId() {
        return systemId.get();
    }

    @Override
    public boolean __tgc__Deleted() {
        return deleted.get();
    }

    @Override
    public void write(DataOutput out) throws IOException {
        systemId.write(out);
        value.write(out);
        deleted.write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        systemId.readFields(in);
        value.readFields(in);
        deleted.readFields(in);
    }

    @Override
    public int compareTo(TestDataModel o) {
        return systemId.compareTo(o.systemId);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("TestDataModel [systemId=");
        builder.append(systemId);
        builder.append(", value=");
        builder.append(value);
        builder.append(", deleted=");
        builder.append(deleted);
        builder.append("]");
        return builder.toString();
    }
}
