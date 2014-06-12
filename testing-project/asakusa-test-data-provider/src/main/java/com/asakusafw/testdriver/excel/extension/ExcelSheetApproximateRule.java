/**
 * Copyright 2014 Asakusa Framework Team.
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

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.asakusafw.testdriver.core.PropertyName;
import com.asakusafw.testdriver.core.PropertyType;
import com.asakusafw.testdriver.core.VerifyContext;
import com.asakusafw.testdriver.excel.ExcelRuleExtractor.FormatException;
import com.asakusafw.testdriver.excel.ExcelSheetRuleExtension;
import com.asakusafw.testdriver.rule.Predicates;
import com.asakusafw.testdriver.rule.ValuePredicate;

/**
 * Approximate comparison extension for testing rules in Excel sheet.
 * @since 0.7.0
 */
public class ExcelSheetApproximateRule implements ExcelSheetRuleExtension {

    private static final Pattern PATTERN = Pattern.compile("\\s*~\\s*([\\+\\-]\\s*)?(.+)");

    private static final String TRIGGER = "~";

    private static final String SIGN_PLUS = "+";

    private static final String SIGN_MINUS = "-";

    @Override
    public ValuePredicate<?> resolve(
            VerifyContext context,
            PropertyName name,
            PropertyType type,
            String expression) throws FormatException {
        if (expression.trim().startsWith(TRIGGER) == false) {
            return null;
        }
        Matcher matcher = PATTERN.matcher(expression);
        if (matcher.matches() == false) {
            throw new FormatException(MessageFormat.format(
                    "Invalid approx(~) expression: {0}",
                    name));
        }
        String sign = matcher.group(1);
        boolean plus = sign == null || sign.equals(SIGN_PLUS);
        boolean minus = sign == null || sign.equals(SIGN_MINUS);
        String magnitude = matcher.group(2).trim();

        switch (type) {
        case BYTE:
        case SHORT:
        case INT:
        case LONG:
            try {
                long value = Long.parseLong(magnitude);
                return Predicates.integerRange(minus ? -value : 0, plus ? +value : 0);
            } catch (NumberFormatException e) {
                throw new FormatException(MessageFormat.format(
                        "Invalid approx(~) error \"{1}\": {0}",
                        name,
                        magnitude), e);
            }
        case FLOAT:
        case DOUBLE:
            try {
                double value = Double.parseDouble(magnitude);
                return Predicates.floatRange(minus ? -value : 0, plus ? +value : 0);
            } catch (NumberFormatException e) {
                throw new FormatException(MessageFormat.format(
                        "Invalid approx(~) value \"{1}\": {0}",
                        name,
                        magnitude), e);
            }
        case DECIMAL:
            try {
                BigDecimal value = new BigDecimal(magnitude);
                return Predicates.decimalRange(
                        minus ? value.negate() : BigDecimal.ZERO,
                        plus ? value : BigDecimal.ZERO);
            } catch (NumberFormatException e) {
                throw new FormatException(MessageFormat.format(
                        "Invalid approx(~) error \"{1}\": {0}",
                        name,
                        magnitude), e);
            }
        default:
            throw new FormatException(MessageFormat.format(
                    "Property does not support approx(~) expression: {0}",
                    name));
        }
    }
}
