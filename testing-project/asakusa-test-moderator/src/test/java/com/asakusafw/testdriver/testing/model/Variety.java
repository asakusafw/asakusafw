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
package com.asakusafw.testdriver.testing.model;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.math.BigDecimal;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

import com.asakusafw.runtime.model.DataModel;
import com.asakusafw.runtime.model.DataModelKind;
import com.asakusafw.runtime.model.PropertyOrder;
import com.asakusafw.runtime.value.BooleanOption;
import com.asakusafw.runtime.value.ByteOption;
import com.asakusafw.runtime.value.Date;
import com.asakusafw.runtime.value.DateOption;
import com.asakusafw.runtime.value.DateTime;
import com.asakusafw.runtime.value.DateTimeOption;
import com.asakusafw.runtime.value.DecimalOption;
import com.asakusafw.runtime.value.DoubleOption;
import com.asakusafw.runtime.value.FloatOption;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.runtime.value.ShortOption;
import com.asakusafw.runtime.value.StringOption;
/**
 * A data model class that represents variety.
 */
@DataModelKind("DMDL")@PropertyOrder({"p_int", "p_long", "p_byte", "p_short", "p_decimal", "p_float", "p_double",
            "p_text", "p_boolean", "p_date", "p_datetime"}) public class Variety implements DataModel<Variety>, Writable
        {
    private final IntOption pInt = new IntOption();
    private final LongOption pLong = new LongOption();
    private final ByteOption pByte = new ByteOption();
    private final ShortOption pShort = new ShortOption();
    private final DecimalOption pDecimal = new DecimalOption();
    private final FloatOption pFloat = new FloatOption();
    private final DoubleOption pDouble = new DoubleOption();
    private final StringOption pText = new StringOption();
    private final BooleanOption pBoolean = new BooleanOption();
    private final DateOption pDate = new DateOption();
    private final DateTimeOption pDatetime = new DateTimeOption();
    @Override@SuppressWarnings("deprecation") public void reset() {
        this.pInt.setNull();
        this.pLong.setNull();
        this.pByte.setNull();
        this.pShort.setNull();
        this.pDecimal.setNull();
        this.pFloat.setNull();
        this.pDouble.setNull();
        this.pText.setNull();
        this.pBoolean.setNull();
        this.pDate.setNull();
        this.pDatetime.setNull();
    }
    @Override@SuppressWarnings("deprecation") public void copyFrom(Variety other) {
        this.pInt.copyFrom(other.pInt);
        this.pLong.copyFrom(other.pLong);
        this.pByte.copyFrom(other.pByte);
        this.pShort.copyFrom(other.pShort);
        this.pDecimal.copyFrom(other.pDecimal);
        this.pFloat.copyFrom(other.pFloat);
        this.pDouble.copyFrom(other.pDouble);
        this.pText.copyFrom(other.pText);
        this.pBoolean.copyFrom(other.pBoolean);
        this.pDate.copyFrom(other.pDate);
        this.pDatetime.copyFrom(other.pDatetime);
    }
    /**
     * Returns p_int.
     * @return p_int
     * @throws NullPointerException if p_int is <code>null</code>
     */
    public int getPInt() {
        return this.pInt.get();
    }
    /**
     * Sets p_int.
     * @param value the value
     */
    @SuppressWarnings("deprecation") public void setPInt(int value) {
        this.pInt.modify(value);
    }
    /**
     * Returns p_int which may be represent <code>null</code>.
     * @return p_int
     */
    public IntOption getPIntOption() {
        return this.pInt;
    }
    /**
     * Sets p_int.
     * @param option the value, or <code>null</code> to set this property to <code>null</code>
     */
    @SuppressWarnings("deprecation") public void setPIntOption(IntOption option) {
        this.pInt.copyFrom(option);
    }
    /**
     * Returns p_long.
     * @return p_long
     * @throws NullPointerException if p_long is <code>null</code>
     */
    public long getPLong() {
        return this.pLong.get();
    }
    /**
     * Sets p_long.
     * @param value the value
     */
    @SuppressWarnings("deprecation") public void setPLong(long value) {
        this.pLong.modify(value);
    }
    /**
     * Returns p_long which may be represent <code>null</code>.
     * @return p_long
     */
    public LongOption getPLongOption() {
        return this.pLong;
    }
    /**
     * Sets p_long.
     * @param option the value, or <code>null</code> to set this property to <code>null</code>
     */
    @SuppressWarnings("deprecation") public void setPLongOption(LongOption option) {
        this.pLong.copyFrom(option);
    }
    /**
     * Returns p_byte.
     * @return p_byte
     * @throws NullPointerException if p_byte is <code>null</code>
     */
    public byte getPByte() {
        return this.pByte.get();
    }
    /**
     * Sets p_byte.
     * @param value the value
     */
    @SuppressWarnings("deprecation") public void setPByte(byte value) {
        this.pByte.modify(value);
    }
    /**
     * Returns p_byte which may be represent <code>null</code>.
     * @return p_byte
     */
    public ByteOption getPByteOption() {
        return this.pByte;
    }
    /**
     * Sets p_byte.
     * @param option the value, or <code>null</code> to set this property to <code>null</code>
     */
    @SuppressWarnings("deprecation") public void setPByteOption(ByteOption option) {
        this.pByte.copyFrom(option);
    }
    /**
     * Returns p_short.
     * @return p_short
     * @throws NullPointerException if p_short is <code>null</code>
     */
    public short getPShort() {
        return this.pShort.get();
    }
    /**
     * Sets p_short.
     * @param value the value
     */
    @SuppressWarnings("deprecation") public void setPShort(short value) {
        this.pShort.modify(value);
    }
    /**
     * Returns p_short which may be represent <code>null</code>.
     * @return p_short
     */
    public ShortOption getPShortOption() {
        return this.pShort;
    }
    /**
     * Sets p_short.
     * @param option the value, or <code>null</code> to set this property to <code>null</code>
     */
    @SuppressWarnings("deprecation") public void setPShortOption(ShortOption option) {
        this.pShort.copyFrom(option);
    }
    /**
     * Returns p_decimal.
     * @return p_decimal
     * @throws NullPointerException if p_decimal is <code>null</code>
     */
    public BigDecimal getPDecimal() {
        return this.pDecimal.get();
    }
    /**
     * Sets p_decimal.
     * @param value the value
     */
    @SuppressWarnings("deprecation") public void setPDecimal(BigDecimal value) {
        this.pDecimal.modify(value);
    }
    /**
     * Returns p_decimal which may be represent <code>null</code>.
     * @return p_decimal
     */
    public DecimalOption getPDecimalOption() {
        return this.pDecimal;
    }
    /**
     * Sets p_decimal.
     * @param option the value, or <code>null</code> to set this property to <code>null</code>
     */
    @SuppressWarnings("deprecation") public void setPDecimalOption(DecimalOption option) {
        this.pDecimal.copyFrom(option);
    }
    /**
     * Returns p_float.
     * @return p_float
     * @throws NullPointerException if p_float is <code>null</code>
     */
    public float getPFloat() {
        return this.pFloat.get();
    }
    /**
     * Sets p_float.
     * @param value the value
     */
    @SuppressWarnings("deprecation") public void setPFloat(float value) {
        this.pFloat.modify(value);
    }
    /**
     * Returns p_float which may be represent <code>null</code>.
     * @return p_float
     */
    public FloatOption getPFloatOption() {
        return this.pFloat;
    }
    /**
     * Sets p_float.
     * @param option the value, or <code>null</code> to set this property to <code>null</code>
     */
    @SuppressWarnings("deprecation") public void setPFloatOption(FloatOption option) {
        this.pFloat.copyFrom(option);
    }
    /**
     * Returns p_double.
     * @return p_double
     * @throws NullPointerException if p_double is <code>null</code>
     */
    public double getPDouble() {
        return this.pDouble.get();
    }
    /**
     * Sets p_double.
     * @param value the value
     */
    @SuppressWarnings("deprecation") public void setPDouble(double value) {
        this.pDouble.modify(value);
    }
    /**
     * Returns p_double which may be represent <code>null</code>.
     * @return p_double
     */
    public DoubleOption getPDoubleOption() {
        return this.pDouble;
    }
    /**
     * Sets p_double.
     * @param option the value, or <code>null</code> to set this property to <code>null</code>
     */
    @SuppressWarnings("deprecation") public void setPDoubleOption(DoubleOption option) {
        this.pDouble.copyFrom(option);
    }
    /**
     * Returns p_text.
     * @return p_text
     * @throws NullPointerException if p_text is <code>null</code>
     */
    public Text getPText() {
        return this.pText.get();
    }
    /**
     * Sets p_text.
     * @param value the value
     */
    @SuppressWarnings("deprecation") public void setPText(Text value) {
        this.pText.modify(value);
    }
    /**
     * Returns p_text which may be represent <code>null</code>.
     * @return p_text
     */
    public StringOption getPTextOption() {
        return this.pText;
    }
    /**
     * Sets p_text.
     * @param option the value, or <code>null</code> to set this property to <code>null</code>
     */
    @SuppressWarnings("deprecation") public void setPTextOption(StringOption option) {
        this.pText.copyFrom(option);
    }
    /**
     * Returns p_boolean.
     * @return p_boolean
     * @throws NullPointerException if p_boolean is <code>null</code>
     */
    public boolean isPBoolean() {
        return this.pBoolean.get();
    }
    /**
     * Sets p_boolean.
     * @param value the value
     */
    @SuppressWarnings("deprecation") public void setPBoolean(boolean value) {
        this.pBoolean.modify(value);
    }
    /**
     * Returns p_boolean which may be represent <code>null</code>.
     * @return p_boolean
     */
    public BooleanOption getPBooleanOption() {
        return this.pBoolean;
    }
    /**
     * Sets p_boolean.
     * @param option the value, or <code>null</code> to set this property to <code>null</code>
     */
    @SuppressWarnings("deprecation") public void setPBooleanOption(BooleanOption option) {
        this.pBoolean.copyFrom(option);
    }
    /**
     * Returns p_date.
     * @return p_date
     * @throws NullPointerException if p_date is <code>null</code>
     */
    public Date getPDate() {
        return this.pDate.get();
    }
    /**
     * Sets p_date.
     * @param value the value
     */
    @SuppressWarnings("deprecation") public void setPDate(Date value) {
        this.pDate.modify(value);
    }
    /**
     * Returns p_date which may be represent <code>null</code>.
     * @return p_date
     */
    public DateOption getPDateOption() {
        return this.pDate;
    }
    /**
     * Sets p_date.
     * @param option the value, or <code>null</code> to set this property to <code>null</code>
     */
    @SuppressWarnings("deprecation") public void setPDateOption(DateOption option) {
        this.pDate.copyFrom(option);
    }
    /**
     * Returns p_datetime.
     * @return p_datetime
     * @throws NullPointerException if p_datetime is <code>null</code>
     */
    public DateTime getPDatetime() {
        return this.pDatetime.get();
    }
    /**
     * Sets p_datetime.
     * @param value the value
     */
    @SuppressWarnings("deprecation") public void setPDatetime(DateTime value) {
        this.pDatetime.modify(value);
    }
    /**
     * Returns p_datetime which may be represent <code>null</code>.
     * @return p_datetime
     */
    public DateTimeOption getPDatetimeOption() {
        return this.pDatetime;
    }
    /**
     * Sets p_datetime.
     * @param option the value, or <code>null</code> to set this property to <code>null</code>
     */
    @SuppressWarnings("deprecation") public void setPDatetimeOption(DateTimeOption option) {
        this.pDatetime.copyFrom(option);
    }
    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{");
        result.append("class=variety");
        result.append(", pInt=");
        result.append(this.pInt);
        result.append(", pLong=");
        result.append(this.pLong);
        result.append(", pByte=");
        result.append(this.pByte);
        result.append(", pShort=");
        result.append(this.pShort);
        result.append(", pDecimal=");
        result.append(this.pDecimal);
        result.append(", pFloat=");
        result.append(this.pFloat);
        result.append(", pDouble=");
        result.append(this.pDouble);
        result.append(", pText=");
        result.append(this.pText);
        result.append(", pBoolean=");
        result.append(this.pBoolean);
        result.append(", pDate=");
        result.append(this.pDate);
        result.append(", pDatetime=");
        result.append(this.pDatetime);
        result.append("}");
        return result.toString();
    }
    @Override public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + pInt.hashCode();
        result = prime * result + pLong.hashCode();
        result = prime * result + pByte.hashCode();
        result = prime * result + pShort.hashCode();
        result = prime * result + pDecimal.hashCode();
        result = prime * result + pFloat.hashCode();
        result = prime * result + pDouble.hashCode();
        result = prime * result + pText.hashCode();
        result = prime * result + pBoolean.hashCode();
        result = prime * result + pDate.hashCode();
        result = prime * result + pDatetime.hashCode();
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
        Variety other = (Variety) obj;
        if(this.pInt.equals(other.pInt) == false) {
            return false;
        }
        if(this.pLong.equals(other.pLong) == false) {
            return false;
        }
        if(this.pByte.equals(other.pByte) == false) {
            return false;
        }
        if(this.pShort.equals(other.pShort) == false) {
            return false;
        }
        if(this.pDecimal.equals(other.pDecimal) == false) {
            return false;
        }
        if(this.pFloat.equals(other.pFloat) == false) {
            return false;
        }
        if(this.pDouble.equals(other.pDouble) == false) {
            return false;
        }
        if(this.pText.equals(other.pText) == false) {
            return false;
        }
        if(this.pBoolean.equals(other.pBoolean) == false) {
            return false;
        }
        if(this.pDate.equals(other.pDate) == false) {
            return false;
        }
        if(this.pDatetime.equals(other.pDatetime) == false) {
            return false;
        }
        return true;
    }
    /**
     * Returns p_text.
     * @return p_text
     * @throws NullPointerException if p_text is <code>null</code>
     */
    public String getPTextAsString() {
        return this.pText.getAsString();
    }
    /**
     * Returns p_text.
     * @param pText0 the value
     */
    @SuppressWarnings("deprecation") public void setPTextAsString(String pText0) {
        this.pText.modify(pText0);
    }
    @Override public void write(DataOutput out) throws IOException {
        pInt.write(out);
        pLong.write(out);
        pByte.write(out);
        pShort.write(out);
        pDecimal.write(out);
        pFloat.write(out);
        pDouble.write(out);
        pText.write(out);
        pBoolean.write(out);
        pDate.write(out);
        pDatetime.write(out);
    }
    @Override public void readFields(DataInput in) throws IOException {
        pInt.readFields(in);
        pLong.readFields(in);
        pByte.readFields(in);
        pShort.readFields(in);
        pDecimal.readFields(in);
        pFloat.readFields(in);
        pDouble.readFields(in);
        pText.readFields(in);
        pBoolean.readFields(in);
        pDate.readFields(in);
        pDatetime.readFields(in);
    }
}