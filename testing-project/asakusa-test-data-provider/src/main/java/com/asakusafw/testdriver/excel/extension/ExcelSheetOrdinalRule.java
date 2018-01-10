/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.testdriver.excel.extension;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import com.asakusafw.testdriver.core.PropertyName;
import com.asakusafw.testdriver.core.PropertyType;
import com.asakusafw.testdriver.core.VerifyContext;
import com.asakusafw.testdriver.excel.ExcelRuleExtractor.FormatException;
import com.asakusafw.testdriver.excel.ExcelSheetRuleExtension;
import com.asakusafw.testdriver.rule.CalendarCompare;
import com.asakusafw.testdriver.rule.CompareOperator;
import com.asakusafw.testdriver.rule.DecimalCompare;
import com.asakusafw.testdriver.rule.FloatCompare;
import com.asakusafw.testdriver.rule.IntegerCompare;
import com.asakusafw.testdriver.rule.ValuePredicate;

/**
 * Ordinal comparison extension for testing rules in Excel sheet.
 * @since 0.7.0
 */
public class ExcelSheetOrdinalRule implements ExcelSheetRuleExtension {

    private static final Map<String, CompareOperator> OPERATORS;
    static {
        Map<String, CompareOperator> map = new HashMap<>();
        for (CompareOperator operator : CompareOperator.values()) {
            map.put(operator.getSymbol(), operator);
        }
        OPERATORS = map;
    }

    @Override
    public ValuePredicate<?> resolve(
            VerifyContext context,
            PropertyName name,
            PropertyType type,
            String expression) throws FormatException {
        CompareOperator operator = OPERATORS.get(expression.trim());
        if (operator == null) {
            return null;
        }
        switch (type) {
        case BYTE:
        case SHORT:
        case INT:
        case LONG:
            return new IntegerCompare(operator);
        case FLOAT:
        case DOUBLE:
            return new FloatCompare(operator);
        case DECIMAL:
            return new DecimalCompare(operator);
        case DATE:
        case DATETIME:
            return new CalendarCompare(operator);
        default:
            throw new FormatException(MessageFormat.format(
                    Messages.getString("ExcelSheetOrdinalRule.errorUnsupportedType"), //$NON-NLS-1$
                    operator.getSymbol(),
                    name));
        }
    }
}
