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
package com.asakusafw.directio.hive.serde;

import java.math.BigDecimal;

import org.apache.hadoop.hive.common.type.HiveDecimal;
import org.apache.hadoop.hive.serde2.io.HiveDecimalWritable;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.HiveDecimalObjectInspector;
import org.apache.hadoop.hive.serde2.typeinfo.DecimalTypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.HiveDecimalUtils;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoFactory;

import com.asakusafw.runtime.value.DecimalOption;
import com.asakusafw.runtime.value.ValueOption;

/**
 * Inspects {@link DecimalOption} object.
 * @since 0.7.0
 */
public class DecimalOptionInspector extends AbstractValueInspector implements HiveDecimalObjectInspector {

    /**
     * Creates a new instance.
     */
    public DecimalOptionInspector() {
        super(TypeInfoFactory.decimalTypeInfo);
    }

    /**
     * Creates a new instance.
     * @param precision the decimal precision
     * @param scale the decimal scale
     */
    public DecimalOptionInspector(int precision, int scale) {
        super(TypeInfoFactory.getDecimalTypeInfo(precision, scale));
    }

    @Override
    protected ValueOption<?> newObject() {
        return new DecimalOption();
    }

    @Override
    public HiveDecimal getPrimitiveJavaObject(Object o) {
        DecimalOption object = (DecimalOption) o;
        if (object == null || object.isNull()) {
            return null;
        }
        return convert(object.get());
    }

    @Override
    public HiveDecimalWritable getPrimitiveWritableObject(Object o) {
        HiveDecimal value = getPrimitiveJavaObject(o);
        if (value == null) {
            return null;
        }
        return new HiveDecimalWritable(value);
    }

    private HiveDecimal convert(BigDecimal value) {
        return HiveDecimalUtils.enforcePrecisionScale(HiveDecimal.create(value), (DecimalTypeInfo) typeInfo);
    }
}
