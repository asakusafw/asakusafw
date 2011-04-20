package com.asakusafw.compiler.operator.model;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

import com.asakusafw.runtime.model.DataModel;
import com.asakusafw.runtime.value.IntOption;
import com.asakusafw.vocabulary.model.Joined;
import com.asakusafw.vocabulary.model.Key;

/**
 * mock_joinedを表すデータモデルクラス。
 */
@Joined(terms = {
        @Joined.Term(source = MockHoge.class, mappings = { @Joined.Mapping(source = "value", destination = "hogeValue") }, shuffle = @Key(group = { "value" })),
        @Joined.Term(source = MockFoo.class, mappings = { @Joined.Mapping(source = "value", destination = "fooValue") }, shuffle = @Key(group = { "value" })) })
public class MockJoined implements DataModel<MockJoined>, Writable {
    private final IntOption hogeValue = new IntOption();
    private final IntOption fooValue = new IntOption();

    @Override
    @SuppressWarnings("deprecation")
    public void reset() {
        this.hogeValue.setNull();
        this.fooValue.setNull();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void copyFrom(MockJoined other) {
        this.hogeValue.copyFrom(other.hogeValue);
        this.fooValue.copyFrom(other.fooValue);
    }

    /**
     * hoge_valueを返す。
     *
     * @return hoge_value
     * @throws NullPointerException
     *             hoge_valueの値が<code>null</code>である場合
     */
    public int getHogeValue() {
        return this.hogeValue.get();
    }

    /**
     * hoge_valueを設定する。
     *
     * @param value
     *            設定する値
     */
    @SuppressWarnings("deprecation")
    public void setHogeValue(int value) {
        this.hogeValue.modify(value);
    }

    /**
     * <code>null</code>を許すhoge_valueを返す。
     *
     * @return hoge_value
     */
    public IntOption getHogeValueOption() {
        return this.hogeValue;
    }

    /**
     * hoge_valueを設定する。
     *
     * @param option
     *            設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    @SuppressWarnings("deprecation")
    public void setHogeValueOption(IntOption option) {
        this.hogeValue.copyFrom(option);
    }

    /**
     * foo_valueを返す。
     *
     * @return foo_value
     * @throws NullPointerException
     *             foo_valueの値が<code>null</code>である場合
     */
    public int getFooValue() {
        return this.fooValue.get();
    }

    /**
     * foo_valueを設定する。
     *
     * @param value
     *            設定する値
     */
    @SuppressWarnings("deprecation")
    public void setFooValue(int value) {
        this.fooValue.modify(value);
    }

    /**
     * <code>null</code>を許すfoo_valueを返す。
     *
     * @return foo_value
     */
    public IntOption getFooValueOption() {
        return this.fooValue;
    }

    /**
     * foo_valueを設定する。
     *
     * @param option
     *            設定する値、<code>null</code>の場合にはこのプロパティが<code>null</code>を表すようになる
     */
    @SuppressWarnings("deprecation")
    public void setFooValueOption(IntOption option) {
        this.fooValue.copyFrom(option);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("{");
        result.append("class=mock_joined");
        result.append(", hogeValue=");
        result.append(this.hogeValue);
        result.append(", fooValue=");
        result.append(this.fooValue);
        result.append("}");
        return result.toString();
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + hogeValue.hashCode();
        result = prime * result + fooValue.hashCode();
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
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        MockJoined other = (MockJoined) obj;
        if (this.hogeValue.equals(other.hogeValue) == false) {
            return false;
        }
        if (this.fooValue.equals(other.fooValue) == false) {
            return false;
        }
        return true;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        hogeValue.write(out);
        fooValue.write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        hogeValue.readFields(in);
        fooValue.readFields(in);
    }
}