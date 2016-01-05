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
package com.asakusafw.testdriver.bulkloader;

import java.util.Calendar;

import com.asakusafw.testdriver.model.SimpleDataModelDefinition;
import com.asakusafw.thundergate.runtime.cache.ThunderGateCacheSupport;
import com.asakusafw.vocabulary.bulkloader.ColumnOrder;
import com.asakusafw.vocabulary.bulkloader.OriginalName;
import com.asakusafw.vocabulary.bulkloader.PrimaryKey;

/**
 * A simple data model with cache support.
 * @since 0.2.0
 */
@PrimaryKey("number")
@OriginalName("SIMPLE")
@ColumnOrder({
    "NUMBER",
    "TEXT",
    "C_BOOL",
    "C_DATETIME"
})
public class CacheSupport implements ThunderGateCacheSupport {

    /**
     * int.
     */
    @OriginalName("NUMBER")
    public Integer number;

    /**
     * String.
     */
    @OriginalName("TEXT")
    public String text;

    /**
     * boolean.
     */
    @OriginalName("C_BOOL")
    public Boolean booleanValue;

    /**
     * date.
     */
    @OriginalName("C_DATETIME")
    public Calendar datetimeValue;

    /**
     * original name is not specified.
     */
    public Integer inferOriginalName;

    @Override
    public long __tgc__DataModelVersion() {
        return 0;
    }

    @Override
    public boolean __tgc__Deleted() {
        return Boolean.TRUE.equals(booleanValue);
    }

    @Override
    public long __tgc__SystemId() {
        return number;
    }

    @Override
    public String __tgc__TimestampColumn() {
        return "C_DATETIME";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((booleanValue == null) ? 0 : booleanValue.hashCode());
        result = prime * result + ((datetimeValue == null) ? 0 : datetimeValue.hashCode());
        result = prime * result + ((number == null) ? 0 : number.hashCode());
        result = prime * result + ((text == null) ? 0 : text.hashCode());
        result = prime * result + ((inferOriginalName == null) ? 0 : inferOriginalName.hashCode());
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        CacheSupport other = (CacheSupport) obj;
        if (booleanValue == null) {
            if (other.booleanValue != null) {
                return false;
            }
        } else if (!booleanValue.equals(other.booleanValue)) {
            return false;
        }
        if (datetimeValue == null) {
            if (other.datetimeValue != null) {
                return false;
            }
        } else if (!datetimeValue.equals(other.datetimeValue)) {
            return false;
        }
        if (inferOriginalName == null) {
            if (other.inferOriginalName != null) {
                return false;
            }
        } else if (!inferOriginalName.equals(other.inferOriginalName)) {
            return false;
        }
        if (number == null) {
            if (other.number != null) {
                return false;
            }
        } else if (!number.equals(other.number)) {
            return false;
        }
        if (text == null) {
            if (other.text != null) {
                return false;
            }
        } else if (!text.equals(other.text)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return new SimpleDataModelDefinition<CacheSupport>(CacheSupport.class).toReflection(this).toString();
    }
}
