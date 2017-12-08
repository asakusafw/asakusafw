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
package com.asakusafw.info.cli.generate.ddl;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.info.cli.common.BatchInfoParameter;
import com.asakusafw.info.hive.HiveIoAttribute;
import com.asakusafw.info.hive.HivePortInfo;
import com.asakusafw.info.hive.LocationInfo;
import com.asakusafw.info.hive.TableInfo;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.ParametersDelegate;

/**
 * Provides Direct I/O Hive inputs/outputs information.
 * @since 0.10.0
 */
@Parameters(resourceBundle = "com.asakusafw.info.cli.jcommander")
public class HiveIoParameter {

    static final Logger LOG = LoggerFactory.getLogger(HiveIoParameter.class);

    /**
     * The batch information.
     */
    @ParametersDelegate
    public final BatchInfoParameter batchInfoParameter = new BatchInfoParameter();

    private List<TableLocationInfo> inputs;

    private List<TableLocationInfo> outputs;

    /**
     * Returns the available Direct I/O Hive tables.
     * @return the table information
     */
    public List<TableLocationInfo> getInputs() {
        if (inputs == null) {
            inputs = collect(HiveIoAttribute::getInputs);
        }
        return inputs;
    }

    /**
     * Returns the available Direct I/O Hive tables.
     * @return the table information
     */
    public List<TableLocationInfo> getOutputs() {
        if (outputs == null) {
            outputs = collect(HiveIoAttribute::getOutputs);
        }
        return outputs;
    }

    private List<TableLocationInfo> collect(Function<HiveIoAttribute, List<? extends HivePortInfo>> f) {
        return batchInfoParameter.load().getJobflows().stream()
                .flatMap(jobflow -> jobflow.getAttributes().stream())
                .filter(it -> it instanceof HiveIoAttribute)
                .map(it -> (HiveIoAttribute) it)
                .map(f)
                .flatMap(List::stream)
                .map(it -> new TableLocationInfo(it.getSchema(), it.getLocation()))
                .sorted(Comparator.comparing((TableLocationInfo it) -> it.getTable().getName())
                        .thenComparing(it -> it.getLocation().getBasePath())
                        .thenComparing(it -> it.getLocation().getResourcePattern()))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Represents table schema and location.
     * @since 0.10.0
     */
    public static class TableLocationInfo {

        private final TableInfo table;

        private final LocationInfo location;

        /**
         * Creates a new instance.
         * @param table table information
         * @param location location information
         */
        public TableLocationInfo(TableInfo table, LocationInfo location) {
            this.table = table;
            this.location = location;
        }

        /**
         * Returns the table.
         * @return the table
         */
        public TableInfo getTable() {
            return table;
        }

        /**
         * Returns the location.
         * @return the location
         */
        public LocationInfo getLocation() {
            return location;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Objects.hashCode(table);
            result = prime * result + Objects.hashCode(location);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            TableLocationInfo other = (TableLocationInfo) obj;
            return Objects.equals(table, other.table) && Objects.equals(location, other.location);
        }

        @Override
        public String toString() {
            return String.format("%s(%s)", table.getName(), location);
        }
    }
}
