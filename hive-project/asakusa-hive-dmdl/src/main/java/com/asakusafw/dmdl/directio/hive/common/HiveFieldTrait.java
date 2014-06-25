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
package com.asakusafw.dmdl.directio.hive.common;

import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;

import com.asakusafw.directio.hive.serde.ValueSerdeFactory;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.semantics.type.BasicType;

/**
 * Attributes for Hive column field.
 * @since 0.7.0
 */
public class HiveFieldTrait extends BaseTrait<HiveFieldTrait> {

    private boolean columnPresent = true;

    private String columnName;

    private TypeKind typeKind = TypeKind.NATURAL;

    private int decimalPrecision;

    private int decimalScale;

    private int stringLength;

    /**
     * Returns whether this field is a table column or not.
     * @return {@code true} if this field is a column
     */
    public boolean isColumnPresent() {
        return columnPresent;
    }

    /**
     * Sets whether this field is a table column or not.
     * @param present {@code true} to make this field be column, {@code false} otherwise
     */
    public void setColumnPresent(boolean present) {
        this.columnPresent = present;
    }

    /**
     * Returns the explicit column name.
     * @return the explicit column name
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * Sets the explicit column name.
     * @param name the explicit column name
     */
    public void setColumnName(String name) {
        this.columnName = name;
    }

    /**
     * Returns the explicit/inferred column name.
     * @param property the target property
     * @return the explicit/inferred column name
     */
    public static String getColumnName(PropertyDeclaration property) {
        HiveFieldTrait trait = property.getTrait(HiveFieldTrait.class);
        if (trait == null || trait.getColumnName() == null) {
            return property.getName().identifier;
        }
        return trait.getColumnName();
    }

    /**
     * Returns the type kind.
     * @return the type kind
     */
    public TypeKind getTypeKind() {
        return typeKind;
    }

    /**
     * Returns the explicit decimal precision.
     * @return the explicit decimal precision
     */
    public int getDecimalPrecision() {
        return decimalPrecision;
    }

    /**
     * Returns the explicit decimal scale.
     * @return the explicit decimal scale
     */
    public int getDecimalScale() {
        return decimalScale;
    }

    /**
     * Returns the explicit char/varchar length.
     * @return the explicit char/varchar length
     */
    public int getStringLength() {
        return stringLength;
    }

    /**
     * Sets the decimal type info.
     * @param precision the precision
     * @param scale the scale
     */
    public void setDecimalTypeInfo(int precision, int scale) {
        this.typeKind = TypeKind.DECIMAL;
        this.decimalPrecision = precision;
        this.decimalScale = scale;
    }

    /**
     * Sets the char type info
     * @param length the character string length
     */
    public void setCharTypeInfo(int length) {
        this.typeKind = TypeKind.CHAR;
        this.stringLength = length;
    }

    /**
     * Sets the varchar type info
     * @param length the character string length
     */
    public void setVarcharTypeInfo(int length) {
        this.typeKind = TypeKind.VARCHAR;
        this.stringLength = length;
    }

    /**
     * Returns the type information for the property.
     * @param property the target property
     * @return the type information
     */
    public static TypeInfo getTypeInfo(PropertyDeclaration property) {
        HiveFieldTrait field = HiveFieldTrait.get(property);
        switch (field.getTypeKind()) {
        case NATURAL:
            switch (((BasicType) property.getType()).getKind()) {
            case BOOLEAN:
                return ValueSerdeFactory.BOOLEAN.getTypeInfo();
            case BYTE:
                return ValueSerdeFactory.BYTE.getTypeInfo();
            case DATE:
                return ValueSerdeFactory.DATE.getTypeInfo();
            case DECIMAL:
                return ValueSerdeFactory.DECIMAL.getTypeInfo();
            case DOUBLE:
                return ValueSerdeFactory.DOUBLE.getTypeInfo();
            case FLOAT:
                return ValueSerdeFactory.FLOAT.getTypeInfo();
            case INT:
                return ValueSerdeFactory.INT.getTypeInfo();
            case LONG:
                return ValueSerdeFactory.LONG.getTypeInfo();
            case SHORT:
                return ValueSerdeFactory.SHORT.getTypeInfo();
            case DATETIME:
                return ValueSerdeFactory.DATE_TIME.getTypeInfo();
            case TEXT:
                return ValueSerdeFactory.STRING.getTypeInfo();
            default:
                throw new AssertionError(property.getType());
            }
        case CHAR:
            return ValueSerdeFactory.getChar(field.getStringLength()).getTypeInfo();
        case VARCHAR:
            return ValueSerdeFactory.getVarchar(field.getStringLength()).getTypeInfo();
        case DECIMAL:
            return ValueSerdeFactory.getDecimal(field.getDecimalPrecision(), field.getDecimalScale()).getTypeInfo();
        default:
            throw new AssertionError(field.getTypeKind());
        }
    }

    /**
     * Returns the {@link HiveFieldTrait} for the target property declaration.
     * @param declaration the target declaration
     * @return the related trait
     */
    public static HiveFieldTrait get(PropertyDeclaration declaration) {
        HiveFieldTrait trait = declaration.getTrait(HiveFieldTrait.class);
        if (trait == null) {
            trait = new HiveFieldTrait();
            declaration.putTrait(HiveFieldTrait.class, trait);
        }
        return trait;
    }

    /**
     * The special type kind.
     */
    public enum TypeKind {

        /**
         * Use property natural type.
         */
        NATURAL,

        /**
         * decimal type.
         */
        DECIMAL,

        /**
         * char type.
         */
        CHAR,

        /**
         * varchar type.
         */
        VARCHAR,
    }
}
