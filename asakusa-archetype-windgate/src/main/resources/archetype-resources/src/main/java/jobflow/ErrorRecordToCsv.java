#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
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
package ${package}.jobflow;

import ${package}.modelgen.dmdl.csv.AbstractErrorRecordCsvExporterDescription;

/**
 * エラー情報をWindGate/CSVでエクスポートする。
 * エクスポート対象ファイルは {@code result/category-<date:日付>.csv}。
 */
public class ErrorRecordToCsv extends AbstractErrorRecordCsvExporterDescription {

    @Override
    public String getProfileName() {
        return "asakusa";
    }

    @Override
    public String getPath() {
        return "result/error-${symbol_dollar}{date}.csv";
    }
}