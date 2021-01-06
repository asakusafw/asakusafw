/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.directio.hive.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hive.serde2.typeinfo.CharTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.DecimalTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.ListTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.MapTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.PrimitiveTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.StructTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.UnionTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.VarcharTypeInfo;

import com.asakusafw.info.hive.ArrayType;
import com.asakusafw.info.hive.ColumnInfo;
import com.asakusafw.info.hive.DecimalType;
import com.asakusafw.info.hive.FieldType;
import com.asakusafw.info.hive.MapType;
import com.asakusafw.info.hive.PlainType;
import com.asakusafw.info.hive.SequenceType;
import com.asakusafw.info.hive.StructType;
import com.asakusafw.info.hive.UnionType;

/**
 * Utilities about Hive schema.
 * @since 0.8.1
 */
public final class SchemaUtil {

    private SchemaUtil() {
        return;
    }

    /**
     * Converts a Hive type into an equivalent portable representation.
     * @param info the original information
     * @return an equivalent portable representation
     */
    public static FieldType toPortable(TypeInfo info) {
        switch (info.getCategory()) {
        case PRIMITIVE:
            return toPortable((PrimitiveTypeInfo) info);
        case LIST:
            return toPortable((ListTypeInfo) info);
        case MAP:
            return toPortable((MapTypeInfo) info);
        case STRUCT:
            return toPortable((StructTypeInfo) info);
        case UNION:
            return toPortable((UnionTypeInfo) info);
        default:
            throw new IllegalArgumentException(MessageFormat.format(
                    "unsupported Hive type: {0}",
                    info));
        }
    }

    private static FieldType toPortable(PrimitiveTypeInfo info) {
        switch (info.getPrimitiveCategory()) {
        case BOOLEAN:
            return PlainType.of(FieldType.TypeName.BOOLEAN);
        case BYTE:
            return PlainType.of(FieldType.TypeName.TINYINT);
        case SHORT:
            return PlainType.of(FieldType.TypeName.SMALLINT);
        case INT:
            return PlainType.of(FieldType.TypeName.INT);
        case LONG:
            return PlainType.of(FieldType.TypeName.BIGINT);
        case FLOAT:
            return PlainType.of(FieldType.TypeName.FLOAT);
        case DOUBLE:
            return PlainType.of(FieldType.TypeName.DOUBLE);
        case DATE:
            return PlainType.of(FieldType.TypeName.DATE);
        case TIMESTAMP:
            return PlainType.of(FieldType.TypeName.TIMESTAMP);
        case STRING:
            return PlainType.of(FieldType.TypeName.STRING);
        case CHAR:
            return toPortable((CharTypeInfo) info);
        case VARCHAR:
            return toPortable((VarcharTypeInfo) info);
        case DECIMAL:
            return toPortable((DecimalTypeInfo) info);
        default:
            throw new IllegalArgumentException(MessageFormat.format(
                    "unsupported Hive type: {0}",
                    info));
        }
    }

    private static FieldType toPortable(CharTypeInfo info) {
        return new SequenceType(
                FieldType.TypeName.VARCHAR,
                info.getLength());
    }

    private static FieldType toPortable(VarcharTypeInfo info) {
        return new SequenceType(
                FieldType.TypeName.CHAR,
                info.getLength());
    }

    private static FieldType toPortable(DecimalTypeInfo info) {
        return new DecimalType(
                FieldType.TypeName.DECIMAL,
                info.getPrecision(),
                info.getScale());
    }

    private static FieldType toPortable(ListTypeInfo info) {
        return new ArrayType(toPortable(info.getListElementTypeInfo()));
    }

    private static FieldType toPortable(MapTypeInfo info) {
        return new MapType(
                toPortable(info.getMapKeyTypeInfo()),
                toPortable(info.getMapValueTypeInfo()));
    }

    private static FieldType toPortable(StructTypeInfo info) {
        List<ColumnInfo> members = new ArrayList<>();
        List<String> names = info.getAllStructFieldNames();
        List<TypeInfo> types = info.getAllStructFieldTypeInfos();
        assert names.size() == types.size();
        for (int i = 0, n = Math.min(names.size(), types.size()); i < n; i++) {
            members.add(new ColumnInfo(names.get(i), toPortable(types.get(i))));
        }
        return new StructType(members);
    }

    private static FieldType toPortable(UnionTypeInfo info) {
        List<FieldType> elements = new ArrayList<>();
        for (TypeInfo e : info.getAllUnionObjectTypeInfos()) {
            elements.add(toPortable(e));
        }
        return new UnionType(elements);
    }
}
