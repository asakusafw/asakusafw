/**
 * Copyright 2011-2017 Asakusa Framework Team.
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
package com.asakusafw.info.value;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Represents a value.
 * @since 0.9.2
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = Constants.ID_KIND,
        visible = false
)
@JsonSubTypes({
    @Type(value = UnknownInfo.class, name = UnknownInfo.KIND),
    @Type(value = NullInfo.class, name = NullInfo.KIND),
    @Type(value = BooleanInfo.class, name = BooleanInfo.KIND),
    @Type(value = ByteInfo.class, name = ByteInfo.KIND),
    @Type(value = ShortInfo.class, name = ShortInfo.KIND),
    @Type(value = IntInfo.class, name = IntInfo.KIND),
    @Type(value = LongInfo.class, name = LongInfo.KIND),
    @Type(value = FloatInfo.class, name = FloatInfo.KIND),
    @Type(value = DoubleInfo.class, name = DoubleInfo.KIND),
    @Type(value = CharInfo.class, name = CharInfo.KIND),
    @Type(value = StringInfo.class, name = StringInfo.KIND),
    @Type(value = ClassInfo.class, name = ClassInfo.KIND),
    @Type(value = ListInfo.class, name = ListInfo.KIND),
    @Type(value = EnumInfo.class, name = EnumInfo.KIND),
    @Type(value = AnnotationInfo.class, name = AnnotationInfo.KIND),
})
public interface ValueInfo {

    /**
     * Returns the type of this value.
     * @return the type
     */
    @JsonProperty(Constants.ID_KIND)
    Kind getKind();

    /**
     * Returns the value as {@link Object}.
     * @return the value, or its string representation
     */
    @JsonIgnore
    Object getObject();

    /**
     * Returns {@link ValueInfo} for the given value.
     * @param value the target value
     * @return the corresponded {@link ValueInfo}
     */
    static ValueInfo of(Object value) {
        return Util.convert(value);
    }

    /**
     * Represents a type of {@link ValueInfo}.
     * @since 0.9.2
     */
    enum Kind {

        /**
         * unknown kind.
         */
        @JsonProperty(UnknownInfo.KIND) UNKNOWN,

        /**
         * null kind.
         */
        @JsonProperty(NullInfo.KIND) NULL,

        /**
         * boolean kind.
         */
        @JsonProperty(BooleanInfo.KIND) BOOLEAN,

        /**
         * byte kind.
         */
        @JsonProperty(ByteInfo.KIND) BYTE,

        /**
         * short kind.
         */
        @JsonProperty(ShortInfo.KIND) SHORT,

        /**
         * int kind.
         */
        @JsonProperty(IntInfo.KIND) INT,

        /**
         * long kind.
         */
        @JsonProperty(LongInfo.KIND) LONG,

        /**
         * float kind.
         */
        @JsonProperty(FloatInfo.KIND) FLOAT,

        /**
         * double kind.
         */
        @JsonProperty(DoubleInfo.KIND) DOUBLE,

        /**
         * char kind.
         */
        @JsonProperty(CharInfo.KIND) CHAR,

        /**
         * string kind.
         */
        @JsonProperty(StringInfo.KIND) STRING,

        /**
         * class kind.
         */
        @JsonProperty(ClassInfo.KIND) CLASS,

        /**
         * value list kind.
         */
        @JsonProperty(ListInfo.KIND) LIST,

        /**
         * enum kind.
         */
        @JsonProperty(EnumInfo.KIND) ENUM,

        /**
         * annotation kind.
         */
        @JsonProperty(AnnotationInfo.KIND) ANNOTATION,
    }
}
