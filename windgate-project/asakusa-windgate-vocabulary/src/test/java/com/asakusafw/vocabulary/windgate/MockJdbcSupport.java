/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.vocabulary.windgate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.asakusafw.windgate.core.vocabulary.DataModelJdbcSupport;

/**
 * Mock {@link DataModelJdbcSupport}.
 * @param <T> data type
 */
public abstract class MockJdbcSupport<T> implements DataModelJdbcSupport<T> {

    @Override
    public boolean isSupported(List<String> columnNames) {
        return true;
    }

    @Override
    public Map<String, String> getColumnMap() {
        return Collections.emptyMap();
    }

    @Override
    public DataModelResultSet<T> createResultSetSupport(ResultSet resultSet, List<String> columnNames) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DataModelPreparedStatement<T> createPreparedStatementSupport(PreparedStatement statement, List<String> columnNames) {
        throw new UnsupportedOperationException();
    }
}
