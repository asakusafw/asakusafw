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
package com.asakusafw.dmdl.directio.hive.common;

import java.util.ArrayList;
import java.util.List;

import com.asakusafw.dmdl.semantics.ModelDeclaration;

/**
 * Attributes for Hive compatible data models.
 * @since 0.7.0
 */
public class HiveDataModelTrait extends BaseTrait<HiveDataModelTrait> {

    private final List<Namer> dataFormatNamer = new ArrayList<>();

    /**
     * Returns the {@link HiveDataModelTrait} for the target data model declaration.
     * @param declaration the target declaration
     * @return the related trait
     */
    public static HiveDataModelTrait get(ModelDeclaration declaration) {
        HiveDataModelTrait trait = declaration.getTrait(HiveDataModelTrait.class);
        if (trait == null) {
            trait = new HiveDataModelTrait();
            declaration.putTrait(HiveDataModelTrait.class, trait);
        }
        return trait;
    }

    /**
     * Add a data format namer.
     * @param namer the namer
     */
    public void addDataFormatNamer(Namer namer) {
        this.dataFormatNamer.add(namer);
    }

    /**
     * Returns the data format namers.
     * @return the namers
     */
    public List<Namer> getDataFormatNamers() {
        return dataFormatNamer;
    }
}
