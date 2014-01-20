/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.testdriver.json;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;

/**
 * A simple data model.
 * @since 0.2.0
 */
public class Simple {

    /**
     * int.
     */
    public Integer number;

    /**
     * String.
     */
    public String text;

    /**
     * boolean.
     */
    public Boolean booleanValue;

    /**
     * byte.
     */
    public Byte byteValue;

    /**
     * short.
     */
    public Short shortValue;

    /**
     * long.
     */
    public Long longValue;

    /**
     * big int.
     */
    public BigInteger bigIntegerValue;

    /**
     * float.
     */
    public Float floatValue;

    /**
     * double.
     */
    public Double doubleValue;

    /**
     * big decimal.
     */
    public BigDecimal bigDecimalValue;

    /**
     * date.
     */
    public Calendar dateValue;

    /**
     * date.
     */
    public Calendar datetimeValue;
}
