package com.asakusafw.testdriver.testing.model;
import com.asakusafw.runtime.model.DataModel;
import com.asakusafw.runtime.model.DataModelKind;
import com.asakusafw.runtime.model.ModelInputLocation;
import com.asakusafw.runtime.model.ModelOutputLocation;
import com.asakusafw.runtime.model.PropertyOrder;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.runtime.value.LongOption;
import com.asakusafw.testdriver.testing.io.NamingInput;
import com.asakusafw.testdriver.testing.io.NamingOutput;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.Writable;
/**
 * namingを表すデータモデルクラス。
 */
@DataModelKind("DMDL")@ModelInputLocation(NamingInput.class)@ModelOutputLocation(NamingOutput.class)@PropertyOrder({"a", 
            "very_very_very_long_name"}) public class Naming implements DataModel<Naming>, Writable {
    private final IntOption a = new IntOption();
    private final LongOption veryVeryVeryLongName = new LongOption();
    @Override@SuppressWarnings("deprecation") public void reset() {
        this.a.setNull();
        this.veryVeryVeryLongName.setNull();
    }
    @Override@SuppressWarnings("deprecation") public void copyFrom(Naming other) {
        this.a.copyFrom(other.a);
        this.veryVeryVeryLongName.copyFrom(other.veryVeryVeryLongName);
    }
    /**
     * aを返す。
     * @return a
     * @throws NullPointerException aの値が<code>null</code>である場合
     */
    public int getA() {
        return this.a.get();
    }
    /**
     * aを設定する。
     * @param value 設定する値
     */
    @SuppressWarnings("deprecation") public void setA(int value) {
        this.a.modify(value);
    }
    /**
     * <code>null</code>を許すaを返す。
     * @return a
     */
    public IntOption getAOption() {
        return this.a;
    }
    /**
     * aを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    @SuppressWarnings("deprecation") public void setAOption(IntOption option) {
        this.a.copyFrom(option);
    }
    /**
     * very_very_very_long_nameを返す。
     * @return very_very_very_long_name
     * @throws NullPointerException very_very_very_long_nameの値が<code>null</code>である場合
     */
    public long getVeryVeryVeryLongName() {
        return this.veryVeryVeryLongName.get();
    }
    /**
     * very_very_very_long_nameを設定する。
     * @param value 設定する値
     */
    @SuppressWarnings("deprecation") public void setVeryVeryVeryLongName(long value) {
        this.veryVeryVeryLongName.modify(value);
    }
    /**
     * <code>null</code>を許すvery_very_very_long_nameを返す。
     * @return very_very_very_long_name
     */
    public LongOption getVeryVeryVeryLongNameOption() {
        return this.veryVeryVeryLongName;
    }
    /**
     * very_very_very_long_nameを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    @SuppressWarnings("deprecation") public void setVeryVeryVeryLongNameOption(LongOption option) {
        this.veryVeryVeryLongName.copyFrom(option);
    }
    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{");
        result.append("class=naming");
        result.append(", a=");
        result.append(this.a);
        result.append(", veryVeryVeryLongName=");
        result.append(this.veryVeryVeryLongName);
        result.append("}");
        return result.toString();
    }
    @Override public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + a.hashCode();
        result = prime * result + veryVeryVeryLongName.hashCode();
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
        Naming other = (Naming) obj;
        if(this.a.equals(other.a)== false) {
            return false;
        }
        if(this.veryVeryVeryLongName.equals(other.veryVeryVeryLongName)== false) {
            return false;
        }
        return true;
    }
    @Override public void write(DataOutput out) throws IOException {
        a.write(out);
        veryVeryVeryLongName.write(out);
    }
    @Override public void readFields(DataInput in) throws IOException {
        a.readFields(in);
        veryVeryVeryLongName.readFields(in);
    }
}