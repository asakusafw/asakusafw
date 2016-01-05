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

import java.math.BigDecimal;
import java.util.Calendar;

import com.asakusafw.testdriver.model.SimpleDataModelDefinition;
import com.asakusafw.vocabulary.bulkloader.ColumnOrder;
import com.asakusafw.vocabulary.bulkloader.OriginalName;
import com.asakusafw.vocabulary.bulkloader.PrimaryKey;

/**
 * A simple data model.
 * @since 0.2.0
 */
@PrimaryKey("number")
@OriginalName("SIMPLE")
@ColumnOrder({
    "NUMBER",
    "TEXT",
    "C_BOOL",
    "C_BYTE",
    "C_SHORT",
    "C_LONG",
    "C_FLOAT",
    "C_DOUBLE",
    "C_DECIMAL",
    "C_DATE",
    "C_TIME",
    "C_DATETIME"
})
public class Simple {

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
     * byte.
     */
    @OriginalName("C_BYTE")
    public Byte byteValue;

    /**
     * short.
     */
    @OriginalName("C_SHORT")
    public Short shortValue;

    /**
     * long.
     */
    @OriginalName("C_LONG")
    public Long longValue;

    /**
     * float.
     */
    @OriginalName("C_FLOAT")
    public Float floatValue;

    /**
     * double.
     */
    @OriginalName("C_DOUBLE")
    public Double doubleValue;

    /**
     * big decimal.
     */
    @OriginalName("C_DECIMAL")
    public BigDecimal bigDecimalValue;

    /**
     * date.
     */
    @OriginalName("C_DATE")
    public Calendar dateValue;

    /**
     * date.
     */
    @OriginalName("C_TIME")
    public Calendar timeValue;

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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bigDecimalValue == null) ? 0 : bigDecimalValue.hashCode());
        result = prime * result + ((booleanValue == null) ? 0 : booleanValue.hashCode());
        result = prime * result + ((byteValue == null) ? 0 : byteValue.hashCode());
        result = prime * result + ((dateValue == null) ? 0 : dateValue.hashCode());
        result = prime * result + ((datetimeValue == null) ? 0 : datetimeValue.hashCode());
        result = prime * result + ((doubleValue == null) ? 0 : doubleValue.hashCode());
        result = prime * result + ((floatValue == null) ? 0 : floatValue.hashCode());
        result = prime * result + ((inferOriginalName == null) ? 0 : inferOriginalName.hashCode());
        result = prime * result + ((longValue == null) ? 0 : longValue.hashCode());
        result = prime * result + ((number == null) ? 0 : number.hashCode());
        result = prime * result + ((shortValue == null) ? 0 : shortValue.hashCode());
        result = prime * result + ((text == null) ? 0 : text.hashCode());
        result = prime * result + ((timeValue == null) ? 0 : timeValue.hashCode());
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
        Simple other = (Simple) obj;
        if (bigDecimalValue == null) {
            if (other.bigDecimalValue != null) {
                return false;
            }
        } else if (!bigDecimalValue.equals(other.bigDecimalValue)) {
            return false;
        }
        if (booleanValue == null) {
            if (other.booleanValue != null) {
                return false;
            }
        } else if (!booleanValue.equals(other.booleanValue)) {
            return false;
        }
        if (byteValue == null) {
            if (other.byteValue != null) {
                return false;
            }
        } else if (!byteValue.equals(other.byteValue)) {
            return false;
        }
        if (dateValue == null) {
            if (other.dateValue != null) {
                return false;
            }
        } else if (!dateValue.equals(other.dateValue)) {
            return false;
        }
        if (datetimeValue == null) {
            if (other.datetimeValue != null) {
                return false;
            }
        } else if (!datetimeValue.equals(other.datetimeValue)) {
            return false;
        }
        if (doubleValue == null) {
            if (other.doubleValue != null) {
                return false;
            }
        } else if (!doubleValue.equals(other.doubleValue)) {
            return false;
        }
        if (floatValue == null) {
            if (other.floatValue != null) {
                return false;
            }
        } else if (!floatValue.equals(other.floatValue)) {
            return false;
        }
        if (inferOriginalName == null) {
            if (other.inferOriginalName != null) {
                return false;
            }
        } else if (!inferOriginalName.equals(other.inferOriginalName)) {
            return false;
        }
        if (longValue == null) {
            if (other.longValue != null) {
                return false;
            }
        } else if (!longValue.equals(other.longValue)) {
            return false;
        }
        if (number == null) {
            if (other.number != null) {
                return false;
            }
        } else if (!number.equals(other.number)) {
            return false;
        }
        if (shortValue == null) {
            if (other.shortValue != null) {
                return false;
            }
        } else if (!shortValue.equals(other.shortValue)) {
            return false;
        }
        if (text == null) {
            if (other.text != null) {
                return false;
            }
        } else if (!text.equals(other.text)) {
            return false;
        }
        if (timeValue == null) {
            if (other.timeValue != null) {
                return false;
            }
        } else if (!timeValue.equals(other.timeValue)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return new SimpleDataModelDefinition<Simple>(Simple.class).toReflection(this).toString();
    }
}
