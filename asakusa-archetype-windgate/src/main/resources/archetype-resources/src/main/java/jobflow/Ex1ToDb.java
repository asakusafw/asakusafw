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

import java.util.Arrays;
import java.util.List;

import ${package}.modelgen.table.model.Ex1;
import ${package}.modelgen.table.jdbc.Ex1JdbcSupport;

import com.asakusafw.windgate.core.vocabulary.DataModelJdbcSupport;

/**
 * Export EX2 from WindGate.
 */
public class Ex1ToDb extends DefaultDbExporterDescription {

    @Override
    public Class<?> getModelType() {
        return Ex1.class;
    }
    
    @Override
    public String getTableName() {
        return "EX2";
    }

    @Override
    public List<String> getColumnNames() {
        return Arrays.asList("SID", "VALUE", "STRING");
    }
    
    @Override
    public Class<? extends DataModelJdbcSupport<?>> getJdbcSupport() {
        return Ex1JdbcSupport.class;
    }

}
