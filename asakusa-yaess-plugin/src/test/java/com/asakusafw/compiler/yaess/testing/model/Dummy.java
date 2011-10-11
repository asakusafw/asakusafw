package com.asakusafw.compiler.yaess.testing.model;
import com.asakusafw.compiler.yaess.testing.io.DummyInput;
import com.asakusafw.compiler.yaess.testing.io.DummyOutput;
import com.asakusafw.runtime.model.DataModel;
import com.asakusafw.runtime.model.DataModelKind;
import com.asakusafw.runtime.model.ModelInputLocation;
import com.asakusafw.runtime.model.ModelOutputLocation;
import com.asakusafw.runtime.value.IntOption;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.Writable;
/**
 * dummyを表すデータモデルクラス。
 */
@DataModelKind("DMDL")@ModelInputLocation(DummyInput.class)@ModelOutputLocation(DummyOutput.class) public class Dummy 
        implements DataModel<Dummy>, Writable {
    private final IntOption value = new IntOption();
    @Override@SuppressWarnings("deprecation") public void reset() {
        this.value.setNull();
    }
    @Override@SuppressWarnings("deprecation") public void copyFrom(Dummy other) {
        this.value.copyFrom(other.value);
    }
    /**
     * valueを返す。
     * @return value
     * @throws NullPointerException valueの値が<code>null</code>である場合
     */
    public int getValue() {
        return this.value.get();
    }
    /**
     * valueを設定する。
     * @param value0 設定する値
     */
    @SuppressWarnings("deprecation") public void setValue(int value0) {
        this.value.modify(value0);
    }
    /**
     * <code>null</code>を許すvalueを返す。
     * @return value
     */
    public IntOption getValueOption() {
        return this.value;
    }
    /**
     * valueを設定する。
     * @param option 設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    @SuppressWarnings("deprecation") public void setValueOption(IntOption option) {
        this.value.copyFrom(option);
    }
    @Override public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{");
        result.append("class=dummy");
        result.append(", value=");
        result.append(this.value);
        result.append("}");
        return result.toString();
    }
    @Override public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + value.hashCode();
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
        Dummy other = (Dummy) obj;
        if(this.value.equals(other.value)== false) {
            return false;
        }
        return true;
    }
    @Override public void write(DataOutput out) throws IOException {
        value.write(out);
    }
    @Override public void readFields(DataInput in) throws IOException {
        value.readFields(in);
    }
}