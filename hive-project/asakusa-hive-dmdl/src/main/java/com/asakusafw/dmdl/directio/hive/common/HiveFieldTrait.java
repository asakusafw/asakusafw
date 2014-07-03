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

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;

import com.asakusafw.directio.hive.serde.StringValueSerdeFactory;
import com.asakusafw.directio.hive.serde.TimestampValueSerdeFactory;
import com.asakusafw.directio.hive.serde.ValueSerdeFactory;
import com.asakusafw.dmdl.java.emitter.EmitContext;
import com.asakusafw.dmdl.model.BasicTypeKind;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;

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
     * Sets the timestamp type info.
     */
    public void setTimestampTypeInfo() {
        this.typeKind = TypeKind.TIMESTAMP;
    }

    /**
     * Sets the string type info.
     */
    public void setStringTypeInfo() {
        this.typeKind = TypeKind.STRING;
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
            return getNaturalTypeInfo(property);
        case TIMESTAMP:
            return TimestampValueSerdeFactory.getCommonTypeInfo();
        case STRING:
            return StringValueSerdeFactory.getCommonTypeInfo();
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
     * Returns the natural type information for the property.
     * @param property the target property
     * @return the natural type information
     */
    public static TypeInfo getNaturalTypeInfo(PropertyDeclaration property) {
        Class<?> valueClass = EmitContext.getFieldTypeAsClass(property);
        ValueSerdeFactory serde = ValueSerdeFactory.fromClass(valueClass);
        return serde.getTypeInfo();
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
        NATURAL(new Acceptor() {
            @Override
            protected boolean accepts(Class<?> valueClass) {
                return ValueSerdeFactory.fromClass(valueClass) != null;
            }
        }),

        /**
         * timestamp type.
         */
        TIMESTAMP(new Acceptor() {
            @Override
            protected boolean accepts(Class<?> valueClass) {
                return TimestampValueSerdeFactory.fromClass(valueClass) != null;
            }
        }),

        /**
         * string type.
         */
        STRING(new Acceptor() {
            @Override
            protected boolean accepts(Class<?> valueClass) {
                return StringValueSerdeFactory.fromClass(valueClass) != null;
            }
        }),

        /**
         * decimal type.
         */
        DECIMAL(BasicTypeKind.DECIMAL),

        /**
         * char type.
         */
        CHAR(BasicTypeKind.TEXT),

        /**
         * varchar type.
         */
        VARCHAR(BasicTypeKind.TEXT),
        ;

        private final Set<BasicTypeKind> supportedKinds;

        private TypeKind(Callable<Set<BasicTypeKind>> lazy) {
            try {
                this.supportedKinds = Collections.unmodifiableSet(lazy.call());
            } catch (Exception e) {
                throw new IllegalArgumentException(e);
            }
        }

        private TypeKind(BasicTypeKind... kinds) {
            EnumSet<BasicTypeKind> set = EnumSet.noneOf(BasicTypeKind.class);
            Collections.addAll(set, kinds);
            this.supportedKinds = Collections.unmodifiableSet(set);
        }

        /**
         * Returns the supported kinds.
         * @return the supported kinds
         */
        public Set<BasicTypeKind> getSupportedKinds() {
            return supportedKinds;
        }

        private static abstract class Acceptor implements Callable<Set<BasicTypeKind>> {

            Acceptor() {
                return;
            }

            @Override
            public Set<BasicTypeKind> call() {
                EnumSet<BasicTypeKind> results = EnumSet.noneOf(BasicTypeKind.class);
                for (BasicTypeKind kind : BasicTypeKind.values()) {
                    Class<?> valueClass = EmitContext.getFieldTypeAsClass(kind);
                    if (accepts(valueClass)) {
                        results.add(kind);
                    }
                }
                return results;
            }

            protected abstract boolean accepts(Class<?> valueClass);
        }
    }
}
