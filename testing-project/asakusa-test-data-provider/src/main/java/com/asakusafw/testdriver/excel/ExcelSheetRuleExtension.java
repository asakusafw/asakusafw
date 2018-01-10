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
package com.asakusafw.testdriver.excel;

import com.asakusafw.testdriver.core.PropertyName;
import com.asakusafw.testdriver.core.PropertyType;
import com.asakusafw.testdriver.core.VerifyContext;
import com.asakusafw.testdriver.rule.ValuePredicate;

/**
 * Extension API for verify rules from Excel sheets.
 * @since 0.7.0
 */
public interface ExcelSheetRuleExtension {

    /**
     * Resolves a rule expression and returns its {@link ValuePredicate}.
     * @param context the current verification context
     * @param name the target property name
     * @param type the target property type
     * @param expression the target expression
     * @return a predicate for the expression, or {@code null} if this extension does not support it expression
     * @throws ExcelRuleExtractor.FormatException if the expression is not valid
     */
    ValuePredicate<?> resolve(
            VerifyContext context,
            PropertyName name,
            PropertyType type,
            String expression) throws ExcelRuleExtractor.FormatException;
}
