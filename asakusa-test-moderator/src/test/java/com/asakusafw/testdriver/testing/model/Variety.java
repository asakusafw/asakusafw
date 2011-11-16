package com.asakusafw.testdriver.testing.model;
import com.asakusafw.runtime.model.DataModel;
import com.asakusafw.runtime.model.DataModelKind;
import com.asakusafw.runtime.model.ModelInputLocation;
import com.asakusafw.runtime.model.ModelOutputLocation;
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
import com.asakusafw.testdriver.testing.io.VarietyInput;
import com.asakusafw.testdriver.testing.io.VarietyOutput;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.math.BigDecimal;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
/**
 * varietyを表すデータモデルクラス。
 */
@DataModelKind("DMDL")@ModelInputLocation(VarietyInput.class)@ModelOutputLocation(VarietyOutput.class)@PropertyOrder({
            "p_int", "p_long", "p_byte", "p_short", "p_decimal", "p_float", "p_double", "p_text", "p_boolean", "p_date", 
            "p_datetime"}) public class Variety implements DataModel<Variety>, Writable {
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
     * p_intを返す。
     * @return p_int
     * @throws NullPointerException p_intの値が<code>null</code>である場合
     */
    public int getPInt() {
        return this.pInt.get();
    }
    /**
     * p_intを設定する。
     * @param value 設定する値
     */
    @SuppressWarnings("deprecation") public void setPInt(int value) {
        this.pInt.modify(value);
    }
    /**
     * <code>null</code>を許すp_intを返す。
     * @return p_int
     */
    public IntOption getPIntOption() {
        return this.pInt;
    }
    /**
     * p_intを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    @SuppressWarnings("deprecation") public void setPIntOption(IntOption option) {
        this.pInt.copyFrom(option);
    }
    /**
     * p_longを返す。
     * @return p_long
     * @throws NullPointerException p_longの値が<code>null</code>である場合
     */
    public long getPLong() {
        return this.pLong.get();
    }
    /**
     * p_longを設定する。
     * @param value 設定する値
     */
    @SuppressWarnings("deprecation") public void setPLong(long value) {
        this.pLong.modify(value);
    }
    /**
     * <code>null</code>を許すp_longを返す。
     * @return p_long
     */
    public LongOption getPLongOption() {
        return this.pLong;
    }
    /**
     * p_longを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    @SuppressWarnings("deprecation") public void setPLongOption(LongOption option) {
        this.pLong.copyFrom(option);
    }
    /**
     * p_byteを返す。
     * @return p_byte
     * @throws NullPointerException p_byteの値が<code>null</code>である場合
     */
    public byte getPByte() {
        return this.pByte.get();
    }
    /**
     * p_byteを設定する。
     * @param value 設定する値
     */
    @SuppressWarnings("deprecation") public void setPByte(byte value) {
        this.pByte.modify(value);
    }
    /**
     * <code>null</code>を許すp_byteを返す。
     * @return p_byte
     */
    public ByteOption getPByteOption() {
        return this.pByte;
    }
    /**
     * p_byteを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    @SuppressWarnings("deprecation") public void setPByteOption(ByteOption option) {
        this.pByte.copyFrom(option);
    }
    /**
     * p_shortを返す。
     * @return p_short
     * @throws NullPointerException p_shortの値が<code>null</code>である場合
     */
    public short getPShort() {
        return this.pShort.get();
    }
    /**
     * p_shortを設定する。
     * @param value 設定する値
     */
    @SuppressWarnings("deprecation") public void setPShort(short value) {
        this.pShort.modify(value);
    }
    /**
     * <code>null</code>を許すp_shortを返す。
     * @return p_short
     */
    public ShortOption getPShortOption() {
        return this.pShort;
    }
    /**
     * p_shortを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    @SuppressWarnings("deprecation") public void setPShortOption(ShortOption option) {
        this.pShort.copyFrom(option);
    }
    /**
     * p_decimalを返す。
     * @return p_decimal
     * @throws NullPointerException p_decimalの値が<code>null</code>である場合
     */
    public BigDecimal getPDecimal() {
        return this.pDecimal.get();
    }
    /**
     * p_decimalを設定する。
     * @param value 設定する値
     */
    @SuppressWarnings("deprecation") public void setPDecimal(BigDecimal value) {
        this.pDecimal.modify(value);
    }
    /**
     * <code>null</code>を許すp_decimalを返す。
     * @return p_decimal
     */
    public DecimalOption getPDecimalOption() {
        return this.pDecimal;
    }
    /**
     * p_decimalを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    @SuppressWarnings("deprecation") public void setPDecimalOption(DecimalOption option) {
        this.pDecimal.copyFrom(option);
    }
    /**
     * p_floatを返す。
     * @return p_float
     * @throws NullPointerException p_floatの値が<code>null</code>である場合
     */
    public float getPFloat() {
        return this.pFloat.get();
    }
    /**
     * p_floatを設定する。
     * @param value 設定する値
     */
    @SuppressWarnings("deprecation") public void setPFloat(float value) {
        this.pFloat.modify(value);
    }
    /**
     * <code>null</code>を許すp_floatを返す。
     * @return p_float
     */
    public FloatOption getPFloatOption() {
        return this.pFloat;
    }
    /**
     * p_floatを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    @SuppressWarnings("deprecation") public void setPFloatOption(FloatOption option) {
        this.pFloat.copyFrom(option);
    }
    /**
     * p_doubleを返す。
     * @return p_double
     * @throws NullPointerException p_doubleの値が<code>null</code>である場合
     */
    public double getPDouble() {
        return this.pDouble.get();
    }
    /**
     * p_doubleを設定する。
     * @param value 設定する値
     */
    @SuppressWarnings("deprecation") public void setPDouble(double value) {
        this.pDouble.modify(value);
    }
    /**
     * <code>null</code>を許すp_doubleを返す。
     * @return p_double
     */
    public DoubleOption getPDoubleOption() {
        return this.pDouble;
    }
    /**
     * p_doubleを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    @SuppressWarnings("deprecation") public void setPDoubleOption(DoubleOption option) {
        this.pDouble.copyFrom(option);
    }
    /**
     * p_textを返す。
     * @return p_text
     * @throws NullPointerException p_textの値が<code>null</code>である場合
     */
    public Text getPText() {
        return this.pText.get();
    }
    /**
     * p_textを設定する。
     * @param value 設定する値
     */
    @SuppressWarnings("deprecation") public void setPText(Text value) {
        this.pText.modify(value);
    }
    /**
     * <code>null</code>を許すp_textを返す。
     * @return p_text
     */
    public StringOption getPTextOption() {
        return this.pText;
    }
    /**
     * p_textを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    @SuppressWarnings("deprecation") public void setPTextOption(StringOption option) {
        this.pText.copyFrom(option);
    }
    /**
     * p_booleanを返す。
     * @return p_boolean
     * @throws NullPointerException p_booleanの値が<code>null</code>である場合
     */
    public boolean isPBoolean() {
        return this.pBoolean.get();
    }
    /**
     * p_booleanを設定する。
     * @param value 設定する値
     */
    @SuppressWarnings("deprecation") public void setPBoolean(boolean value) {
        this.pBoolean.modify(value);
    }
    /**
     * <code>null</code>を許すp_booleanを返す。
     * @return p_boolean
     */
    public BooleanOption getPBooleanOption() {
        return this.pBoolean;
    }
    /**
     * p_booleanを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    @SuppressWarnings("deprecation") public void setPBooleanOption(BooleanOption option) {
        this.pBoolean.copyFrom(option);
    }
    /**
     * p_dateを返す。
     * @return p_date
     * @throws NullPointerException p_dateの値が<code>null</code>である場合
     */
    public Date getPDate() {
        return this.pDate.get();
    }
    /**
     * p_dateを設定する。
     * @param value 設定する値
     */
    @SuppressWarnings("deprecation") public void setPDate(Date value) {
        this.pDate.modify(value);
    }
    /**
     * <code>null</code>を許すp_dateを返す。
     * @return p_date
     */
    public DateOption getPDateOption() {
        return this.pDate;
    }
    /**
     * p_dateを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    @SuppressWarnings("deprecation") public void setPDateOption(DateOption option) {
        this.pDate.copyFrom(option);
    }
    /**
     * p_datetimeを返す。
     * @return p_datetime
     * @throws NullPointerException p_datetimeの値が<code>null</code>である場合
     */
    public DateTime getPDatetime() {
        return this.pDatetime.get();
    }
    /**
     * p_datetimeを設定する。
     * @param value 設定する値
     */
    @SuppressWarnings("deprecation") public void setPDatetime(DateTime value) {
        this.pDatetime.modify(value);
    }
    /**
     * <code>null</code>を許すp_datetimeを返す。
     * @return p_datetime
     */
    public DateTimeOption getPDatetimeOption() {
        return this.pDatetime;
    }
    /**
     * p_datetimeを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
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
        if(this.getClass()!= obj.getClass()) {
            return false;
        }
        Variety other = (Variety) obj;
        if(this.pInt.equals(other.pInt)== false) {
            return false;
        }
        if(this.pLong.equals(other.pLong)== false) {
            return false;
        }
        if(this.pByte.equals(other.pByte)== false) {
            return false;
        }
        if(this.pShort.equals(other.pShort)== false) {
            return false;
        }
        if(this.pDecimal.equals(other.pDecimal)== false) {
            return false;
        }
        if(this.pFloat.equals(other.pFloat)== false) {
            return false;
        }
        if(this.pDouble.equals(other.pDouble)== false) {
            return false;
        }
        if(this.pText.equals(other.pText)== false) {
            return false;
        }
        if(this.pBoolean.equals(other.pBoolean)== false) {
            return false;
        }
        if(this.pDate.equals(other.pDate)== false) {
            return false;
        }
        if(this.pDatetime.equals(other.pDatetime)== false) {
            return false;
        }
        return true;
    }
    /**
     * p_textを返す。
     * @return p_text
     * @throws NullPointerException p_textの値が<code>null</code>である場合
     */
    public String getPTextAsString() {
        return this.pText.getAsString();
    }
    /**
     * p_textを設定する。
     * @param pText0 設定する値
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