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
package com.asakusafw.testdriver.bulkloader;

import java.math.BigDecimal;
import java.util.Calendar;

import com.asakusafw.vocabulary.bulkloader.OriginalName;
import com.asakusafw.vocabulary.bulkloader.PrimaryKey;

/**
 * A simple data model.
 * @since 0.2.0
 */
@PrimaryKey("number")
@OriginalName("SIMPLE")
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
    @OriginalName("C_BOOLEAN")
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
    @OriginalName("C_TIMESTAMP")
    public Calendar datetimeValue;
}
