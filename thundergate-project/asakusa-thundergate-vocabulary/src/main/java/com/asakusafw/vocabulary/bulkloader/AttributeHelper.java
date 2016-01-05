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
package com.asakusafw.vocabulary.bulkloader;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.asakusafw.vocabulary.model.TableModel;

/**
 * Fetches table metadata from attributes.
 */
@SuppressWarnings("deprecation")
final class AttributeHelper {

    static String getTableName(Class<?> modelType) {
        OriginalName original = modelType.getAnnotation(OriginalName.class);
        if (original != null) {
            return original.value();
        }
        TableModel meta = modelType.getAnnotation(TableModel.class);
        if (meta != null) {
            return meta.name();
        }

        StackTraceElement caller = getCaller();
        throw new UnsupportedOperationException(MessageFormat.format(
                "クラス{0}には@{1}の指定がないため、テーブル名を自動的に判別できませんでした。{2}()をオーバーライドして明示的に指定して下さい",
                caller.getClassName(),
                OriginalName.class.getSimpleName(),
                caller.getMethodName()));
    }

    static List<String> getColumnNames(Class<?> modelType) {
        ColumnOrder original = modelType.getAnnotation(ColumnOrder.class);
        if (original != null) {
            return new ArrayList<String>(Arrays.asList(original.value()));
        }
        TableModel meta = modelType.getAnnotation(TableModel.class);
        if (meta != null) {
            return Arrays.asList(meta.columns());
        }

        StackTraceElement caller = getCaller();
        throw new UnsupportedOperationException(MessageFormat.format(
                "クラス{0}には@{1}の指定がないため、カラム名を自動的に判別できませんでした。{2}()をオーバーライドして明示的に指定して下さい",
                caller.getClassName(),
                ColumnOrder.class.getSimpleName(),
                caller.getMethodName()));
    }

    static List<String> getPrimaryKeyNames(Class<?> modelType) {
        PrimaryKey original = modelType.getAnnotation(PrimaryKey.class);
        if (original != null) {
            return new ArrayList<String>(Arrays.asList(original.value()));
        }
        TableModel meta = modelType.getAnnotation(TableModel.class);
        if (meta != null) {
            return Arrays.asList(meta.primary());
        }

        StackTraceElement caller = getCaller();
        throw new UnsupportedOperationException(MessageFormat.format(
                "クラス{0}には@{1}の指定がないため、主キー名を自動的に判別できませんでした。{2}()をオーバーライドして明示的に指定して下さい",
                caller.getClassName(),
                PrimaryKey.class.getSimpleName(),
                caller.getMethodName()));
    }

    private static StackTraceElement getCaller() {
        StackTraceElement[] trace = new Throwable().getStackTrace();
        if (trace == null || trace.length < 3) {
            return new StackTraceElement("?", "?", "?", -1);
        }
        return trace[2];
    }

    private AttributeHelper() {
        return;
    }
}
