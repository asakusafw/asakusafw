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
package com.asakusafw.windgate.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;

import com.asakusafw.windgate.core.vocabulary.DataModelJdbcSupport;

/**
 * Can not create a new instance.
 */
public class SupportWithPrivateConstructor implements DataModelJdbcSupport<Object> {

    private SupportWithPrivateConstructor() {
        return;
    }

    @Override
    public Class<Object> getSupportedType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSupported(List<String> columnNames) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, String> getColumnMap() {
        throw new UnsupportedOperationException();
    }

    @Override
    public DataModelResultSet<Object> createResultSetSupport(ResultSet resultSet, List<String> columnNames) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DataModelPreparedStatement<Object> createPreparedStatementSupport(
            PreparedStatement statement, List<String> columnNames) {
        throw new UnsupportedOperationException();
    }
}
