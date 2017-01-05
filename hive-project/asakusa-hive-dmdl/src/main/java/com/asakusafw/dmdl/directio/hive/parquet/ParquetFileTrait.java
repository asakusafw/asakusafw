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
package com.asakusafw.dmdl.directio.hive.parquet;

import com.asakusafw.directio.hive.parquet.ParquetFormatConfiguration;
import com.asakusafw.dmdl.directio.hive.common.BaseTrait;
import com.asakusafw.dmdl.semantics.ModelDeclaration;

/**
 * Attributes for parquet file format.
 * @since 0.7.0
 */
public class ParquetFileTrait extends BaseTrait<ParquetFileTrait> {

    private String tableName;

    private final ParquetFormatConfiguration configuration = new ParquetFormatConfiguration().clear();

    /**
     * Returns the explicit table name.
     * @return the explicit table name
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Sets the explicit table name.
     * @param name the explicit table name
     */
    public void setTableName(String name) {
        this.tableName = name;
    }

    /**
     * Returns the explicit/inferred table name.
     * @param model the target data model
     * @return the explicit/inferred table name
     */
    public static String getTableName(ModelDeclaration model) {
        ParquetFileTrait trait = model.getTrait(ParquetFileTrait.class);
        if (trait == null || trait.getTableName() == null) {
            return model.getName().identifier;
        }
        return trait.getTableName();
    }

    /**
     * Returns the view of {@link ParquetFormatConfiguration}.
     * @return the configuration
     */
    public ParquetFormatConfiguration configuration() {
        return configuration;
    }
}
