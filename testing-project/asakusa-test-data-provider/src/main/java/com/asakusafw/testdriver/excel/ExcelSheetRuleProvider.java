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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.PropertyType;
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
 * This accepts URI:
 * <ul>
 * <li> which is also a valid URL to obtain an Excel workbook, </li>
 * <li> whose "path" segment ends with ".xls", or </li>
 * <li>
 *     whose "fragment" is "#:" + 0-origin sheet number, "#" + sheet name,
 *     or null (which means the first sheet)
 * </li>
 * </ul>
 * @since 0.2.0
 * @version 0.9.0
 */
public class ExcelSheetRuleProvider implements VerifyRuleProvider {

    static final Logger LOG = LoggerFactory.getLogger(ExcelSheetRuleProvider.class);

    private static final List<ExcelRuleExtractor> EXTRACTORS;
    static {
        List<ExcelRuleExtractor> drivers = new ArrayList<>();
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
        LOG.debug("Finding Excel sheet extractor: {}", source); //$NON-NLS-1$
        ExcelRuleExtractor extractor = findExtractor(sheet);
        if (extractor == null) {
            LOG.debug("Valid Excel sheet extractor is not found: {}", source); //$NON-NLS-1$
            return null;
        }
        LOG.info(MessageFormat.format(
                Messages.getString("ExcelSheetRuleProvider.infoApply"), //$NON-NLS-1$
                source));
        try {
            return resolve(definition, context, sheet, extractor);
        } catch (ExcelRuleExtractor.FormatException e) {
            throw new IOException(MessageFormat.format(
                    Messages.getString("ExcelSheetRuleProvider.errorInvalidFormat"), //$NON-NLS-1$
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

        Set<DataModelCondition> modelPredicates = extractor.extractDataModelCondition(sheet);
        // if ignores anything, this returns special rule that accepts anything
        if (modelPredicates.contains(DataModelCondition.IGNORE_MATCHED)
                && modelPredicates.contains(DataModelCondition.IGNORE_ABSENT)
                && modelPredicates.contains(DataModelCondition.IGNORE_UNEXPECTED)) {
            return VerifyRule.NULL;
        }

        VerifyRuleBuilder builder = new VerifyRuleBuilder(definition);
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
                    Messages.getString("ExcelSheetRuleProvider.errorMissingProperty"), //$NON-NLS-1$
                    row.getRowNum() + 1), e);
        }
        ValueConditionKind value = extractor.extractValueCondition(row);
        NullityConditionKind nullity = extractor.extractNullityCondition(row);
        if (buildNullity(property, value, nullity) == false) {
            return;
        }
        String extraOptions = extractor.extractOptions(row);
        buildValue(property, context, value, extraOptions);
    }

    private boolean buildNullity(
            VerifyRuleBuilder.Property property,
            ValueConditionKind value,
            NullityConditionKind nullity) throws ExcelRuleExtractor.FormatException {
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
        default:
            throw new ExcelRuleExtractor.FormatException(MessageFormat.format(
                    Messages.getString("ExcelSheetRuleProvider.errorUnknownNullityConstraint"), //$NON-NLS-1$
                    property,
                    nullity));
        }
        return true;
    }

    private void buildValue(
            VerifyRuleBuilder.Property property,
            VerifyContext context,
            ValueConditionKind value,
            String extraOptions) throws ExcelRuleExtractor.FormatException {
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
            if (property.getType() == PropertyType.STRING) {
                property.accept(containsString());
            } else {
                throw typeError(property, ValueConditionKind.CONTAIN);
            }
            break;
        case TODAY:
            if (property.getType() == PropertyType.DATE
                    || property.getType() == PropertyType.DATETIME) {
                property.accept(createTodayPredicate(context));
            } else {
                throw typeError(property, ValueConditionKind.TODAY);
            }
            break;
        case NOW:
            if (property.getType() == PropertyType.DATE
                    || property.getType() == PropertyType.DATETIME) {
                property.accept(createNowPredicate(context));
            } else {
                throw typeError(property, ValueConditionKind.NOW);
            }
            break;
        case EXPRESSION:
            buildExpression(property, context, extraOptions);
            break;
        default:
            throw new ExcelRuleExtractor.FormatException(MessageFormat.format(
                    Messages.getString("ExcelSheetRuleProvider.errorUnknownValueConstraint"), //$NON-NLS-1$
                    property,
                    value));
        }
    }

    private void buildExpression(
            VerifyRuleBuilder.Property property,
            VerifyContext context,
            String expression) throws ExcelRuleExtractor.FormatException {
        ExcelSheetRuleExtensionRepository repo = ExcelSheetRuleExtensionRepository.get(context);
        ValuePredicate<?> resolved = repo.resolve(context, property.getName(), property.getType(), expression);
        if (resolved != null) {
            property.accept(resolved);
        } else {
            throw new ExcelRuleExtractor.FormatException(MessageFormat.format(
                    Messages.getString("ExcelSheetRuleProvider.errorUnsupportedOptionalExpression"), //$NON-NLS-1$
                    property.getName(),
                    expression));
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
                Messages.getString("ExcelSheetRuleProvider.errorInconsistentPropertyType"), //$NON-NLS-1$
                property.getName(),
                kind.getTitle(),
                kind.getExpectedType()));
    }
}
