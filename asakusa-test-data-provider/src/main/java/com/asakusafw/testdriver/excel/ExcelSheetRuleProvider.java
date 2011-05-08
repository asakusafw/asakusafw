/**
 * Copyright 2011 Asakusa Framework Team.
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

import static com.asakusafw.testdriver.rule.Predicates.*;

import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.VerifyContext;
import com.asakusafw.testdriver.core.VerifyRule;
import com.asakusafw.testdriver.core.VerifyRuleProvider;
import com.asakusafw.testdriver.excel.legacy.LegacyExcelRuleExtractor;
import com.asakusafw.testdriver.rule.BothAreNull;
import com.asakusafw.testdriver.rule.DataModelCondition;
import com.asakusafw.testdriver.rule.Predicates;
import com.asakusafw.testdriver.rule.ValuePredicate;
import com.asakusafw.testdriver.rule.VerifyRuleBuilder;
import com.asakusafw.testdriver.rule.VerifyRuleBuilder.Property;

/**
 * Provides {@link VerifyRule} from Excel Sheet.
 * @since 0.2.0
 */
public class ExcelSheetRuleProvider implements VerifyRuleProvider {

    private static final List<ExcelRuleExtractor> EXTRACTORS;
    static {
        List<ExcelRuleExtractor> drivers = new ArrayList<ExcelRuleExtractor>();
        drivers.add(new DefaultExcelRuleExtractor());
        drivers.add(new LegacyExcelRuleExtractor());
        EXTRACTORS = Collections.unmodifiableList(drivers);
    }

    @Override
    public <T> VerifyRule get(
            DataModelDefinition<T> definition, VerifyContext context, URI source) throws IOException {
        Sheet sheet = Util.extract(source);
        if (sheet == null) {
            return null;
        }
        ExcelRuleExtractor extractor = findExtractor(sheet);
        if (extractor == null) {
            return null;
        }
        try {
            return resolve(definition, context, sheet, extractor);
        } catch (ExcelRuleExtractor.FormatException e) {
            throw new IOException(MessageFormat.format(
                    "{0}の形式が正しくありません",
                    source), e);
        }
    }

    private ExcelRuleExtractor findExtractor(Sheet sheet) {
        assert sheet != null;
        for (ExcelRuleExtractor extractor : EXTRACTORS) {
            if (extractor.supports(sheet)) {
                return extractor;
            }
        }
        return null;
    }

    private <T> VerifyRule resolve(
            DataModelDefinition<T> definition,
            VerifyContext context,
            Sheet sheet,
            ExcelRuleExtractor extractor) throws ExcelRuleExtractor.FormatException {
        assert definition != null;
        assert context != null;
        assert sheet != null;
        assert extractor != null;

        VerifyRuleBuilder builder = new VerifyRuleBuilder(definition);
        Set<DataModelCondition> modelPredicates = extractor.extractDataModelCondition(sheet);
        if (modelPredicates.contains(DataModelCondition.IGNORE_ABSENT)) {
            builder.acceptIfAbsent();
        }
        if (modelPredicates.contains(DataModelCondition.IGNORE_UNEXPECTED)) {
            builder.acceptIfUnexpected();
        }
        if (modelPredicates.contains(DataModelCondition.IGNORE_MATCHED) == false) {
            int start = extractor.extractPropertyRowStartIndex(sheet);
            int end = sheet.getLastRowNum() + 1;
            for (int i = start; i < end; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                resolveRow(builder, definition, context, row, extractor);
            }
        }
        return builder.toVerifyRule();
    }

    private <T> void resolveRow(
            VerifyRuleBuilder builder,
            DataModelDefinition<T> definition,
            VerifyContext context,
            Row row,
            ExcelRuleExtractor extractor) throws ExcelRuleExtractor.FormatException {
        assert builder != null;
        assert definition != null;
        assert context != null;
        assert row != null;
        assert extractor != null;
        String name = extractor.extractName(row);
        if (name == null) {
            return;
        }
        VerifyRuleBuilder.Property property;
        try {
            property = builder.property(name);
        } catch (IllegalArgumentException e) {
            throw new ExcelRuleExtractor.FormatException(MessageFormat.format(
                    "プロパティが見つかりません (row={0})",
                    row.getRowNum() + 1), e);
        }
        ValueConditionKind value = extractor.extractValueCondition(row);
        NullityConditionKind nullity = extractor.extractNullityCondition(row);
        if (buildNullity(property, value, nullity) == false) {
            return;
        }
        buildValue(property, context, value);
    }

    private boolean buildNullity(
            VerifyRuleBuilder.Property property,
            ValueConditionKind value,
            NullityConditionKind nullity) {
        assert property != null;
        assert value != null;
        assert nullity != null;
        switch (nullity) {
        case NORMAL:
            if (value == ValueConditionKind.EQUAL) {
                property.accept(new BothAreNull());
            }
            break;
        case ACCEPT_ABSENT:
            property.accept(isNull());
            break;
        case ACCEPT_PRESENT:
            property.accept(not(isNull()));
            break;
        case DENY_ABSENT:
            // must be denied if value was checked
            if (value == ValueConditionKind.ANY || value == ValueConditionKind.KEY) {
                property.accept(not(isNull()));
            }
            break;
        case DENY_PRESENT:
            // must be denied if value was checked
            property.accept(isNull());
            return false;
        }
        return true;
    }

    private void buildValue(
            VerifyRuleBuilder.Property property,
            VerifyContext context,
            ValueConditionKind value) throws ExcelRuleExtractor.FormatException {
        assert property != null;
        assert context != null;
        assert value != null;
        switch (value) {
        case ANY:
            break;
        case KEY:
            property.asKey();
            break;
        case EQUAL:
            property.accept(Predicates.equals());
            break;
        case CONTAIN:
            if (property.getType() != String.class) {
                throw typeError(property, ValueConditionKind.CONTAIN);
            }
            property.accept(containsString());
            break;
        case TODAY:
            if (property.getType() != Calendar.class) {
                throw typeError(property, ValueConditionKind.TODAY);
            }
            property.accept(createTodayPredicate(context));
            break;
        case NOW:
            if (property.getType() != Calendar.class) {
                throw typeError(property, ValueConditionKind.NOW);
            }
            property.accept(createNowPredicate(context));
            break;
        }
    }

    private ValuePredicate<Calendar> createTodayPredicate(VerifyContext context) {
        assert context != null;
        Calendar begin = toDate(context.getTestStarted());
        Calendar end = toDate(context.getTestFinished());
        end.add(Calendar.DATE, 1);
        end.add(Calendar.MILLISECOND, -1);
        return Predicates.period(begin, end);
    }

    private Calendar toDate(Date date) {
        assert date != null;
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        int y = instance.get(Calendar.YEAR);
        int m = instance.get(Calendar.MONTH);
        int d = instance.get(Calendar.DATE);
        instance.clear();
        instance.set(y, m, d);
        return instance;
    }

    private ValuePredicate<Calendar> createNowPredicate(VerifyContext context) {
        assert context != null;
        Calendar begin = toDatetime(context.getTestStarted());
        Calendar end = toDatetime(context.getTestFinished());
        end.add(Calendar.SECOND, 1);
        end.add(Calendar.MILLISECOND, -1);
        return Predicates.period(begin, end);
    }

    private Calendar toDatetime(Date date) {
        assert date != null;
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        int y = instance.get(Calendar.YEAR);
        int m = instance.get(Calendar.MONTH);
        int d = instance.get(Calendar.DATE);
        int ho = instance.get(Calendar.HOUR_OF_DAY);
        int mi = instance.get(Calendar.MINUTE);
        int se = instance.get(Calendar.SECOND);
        instance.clear();
        instance.set(y, m, d, ho, mi, se);
        return instance;
    }

    private ExcelRuleExtractor.FormatException typeError(Property property, ValueConditionKind kind) {
        assert property != null;
        assert kind != null;
        return new ExcelRuleExtractor.FormatException(MessageFormat.format(
                "{1}をプロパティ\"{0}\"に利用できません: {2}のみに利用可能です",
                property.getName(),
                kind.getTitle(),
                kind.getExpectedType()));
    }
}
