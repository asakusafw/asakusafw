/**
 * Copyright 2011-2016 Asakusa Framework Team.
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
package com.asakusafw.dmdl.thundergate.source;

import java.util.Map;

import com.asakusafw.dmdl.thundergate.model.PropertyTypeKind;
import com.asakusafw.utils.collections.Maps;

/**
 *  MySQLのデータ型に関する情報を保持する列挙型。
 */
public enum MySqlDataType {

    /**
     * 1バイト整数。
     */
    TINY_INT("tinyint", PropertyTypeKind.BYTE),

    /**
     * 2バイト整数。
     */
    SMALL_INT("smallint", PropertyTypeKind.SHORT),

    /**
     * 4バイト整数。
     */
    INT("int", PropertyTypeKind.INT),

    /**
     * 8バイト整数。
     */
    LONG("bigint", PropertyTypeKind.LONG),

    /**
     * 4バイト整数。
     */
    FLOAT("float", PropertyTypeKind.FLOAT),

    /**
     * 8バイト整数。
     */
    DOUBLE("double", PropertyTypeKind.DOUBLE),

    /**
     * 10進数。
     */
    DECIMAL("decimal", PropertyTypeKind.BIG_DECIMAL),

    /**
     * 日付。
     */
    DATE("date", PropertyTypeKind.DATE),

    /**
     * 時刻。
     */
    DATETIME("datetime", PropertyTypeKind.DATETIME),

    /**
     * タイムスタンプ。
     */
    TIMESTAMP("timestamp", PropertyTypeKind.DATETIME),

    /**
     * 固定長文字列。
     */
    CHAR("char", PropertyTypeKind.STRING),

    /**
     * 可変長文字列。
     */
    VARCHAR("varchar", PropertyTypeKind.STRING),

    /**
     * Character large objects (~2^8-1bytes).
     */
    TINYTEXT("tinytext", PropertyTypeKind.STRING),

    /**
     * Character large objects (~2^16-1bytes).
     */
    TEXT("text", PropertyTypeKind.STRING),

    /**
     * Character large objects (~2^24-1bytes).
     */
    MEDIUMTEXT("mediumtext", PropertyTypeKind.STRING),

    /**
     * Character large objects (~2^32-1bytes).
     */
    LONGTEXT("longtext", PropertyTypeKind.STRING),
    ;

    /**
     * MySQLで使用するデータ型を表す文字列。
     */
    private String dataTypeString;

    /**
     * ModelGeneratorが利用するプロパティ型の種類。
     */
    private PropertyTypeKind propertyTypeKind;

    private MySqlDataType(String str, PropertyTypeKind type) {
        assert str != null;
        assert type != null;
        this.dataTypeString = str;
        this.propertyTypeKind = type;
    }

    /**
     * NFORMACTION_SCHEMA.COLUMNSのDATA_TYPEカラムの文字列と、
     * MySQLのデータ型との対応を保持するMap。
     */
    private static Map<String, MySqlDataType> dataTypeMap = Maps.create();
    static {
        for (MySqlDataType type : MySqlDataType.values()) {
            String mySqlStr = type.getDataTypeString();
            if (dataTypeMap.containsKey(mySqlStr)) {
                throw new RuntimeException("MySQLのデータ型を表す文字列に重複があります");
            }
            dataTypeMap.put(mySqlStr, type);
        }
    }

    /**
     * NFORMACTION_SCHEMA.COLUMNSのDATA_TYPEカラムの文字列と、
     * プロパティの型との対応を保持するMap。
     */
    private static Map<String, PropertyTypeKind> propertyMap = Maps.create();
    static {
        for (MySqlDataType type : MySqlDataType.values()) {
            String mySqlStr = type.getDataTypeString();
            if (propertyMap.containsKey(mySqlStr)) {
                throw new RuntimeException("MySQLのデータ型を表す文字列に重複があります");
            }
            propertyMap.put(mySqlStr, type.getPropertyType());
        }
    }

    /**
     * INFORMACTION_SCHEMA.COLUMNSのDATA_TYPEカラムに格納されている文字列から、MySQLのデータ型を取得する。
     * @param str INFORMACTION_SCHEMAの文字列
     * @return MySQLのデータ型、対応する種類が存在しない場合は{@code null}
     */
    public static MySqlDataType getDataTypeByString(String str) {
        return dataTypeMap.get(str);
    }

    /**
     * INFORMACTION_SCHEMA.COLUMNSのDATA_TYPEカラムに格納されている文字列から、プロパティ型の種類を取得する。
     * @param str INFORMACTION_SCHEMAの文字列
     * @return プロパティ型の種類、対応する種類が存在しない場合は{@code null}
     */
    public static PropertyTypeKind getPropertyTypeByString(String str) {
        return propertyMap.get(str);
    }

    /**
     * データ型の文字列表現を返す。
     * @return データ型の文字列表現
     */
    public String getDataTypeString() {
        return dataTypeString;
    }

    /**
     * プロパティ型の種類を返す。
     * @return プロパティ型の種類
     */
    public PropertyTypeKind getPropertyType() {
        return propertyTypeKind;
    }

}
