/**
 * Copyright 2011-2019 Asakusa Framework Team.
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
package com.example;

import com.asakusafw.vocabulary.flow.Export;
import com.asakusafw.vocabulary.flow.FlowDescription;
import com.asakusafw.vocabulary.flow.Import;
import com.asakusafw.vocabulary.flow.In;
import com.asakusafw.vocabulary.flow.JobFlow;
import com.asakusafw.vocabulary.flow.Out;
import com.example.modelgen.dmdl.model.Ksv;

/**
 * Partitioned sort using {@link Ksv}.
 */
@JobFlow(name = "sort")
public class WgKsvSortJob extends FlowDescription {

    final In<Ksv> in;

    final Out<Ksv> out;

    /**
     * Creates a new instance.
     * @param in the input
     * @param out the output
     */
    public WgKsvSortJob(
            @Import(name = "ksv", description = KsvImporterDescription.class) In<Ksv> in,
            @Export(name = "ksv", description = KsvExporterDescription.class) Out<Ksv> out) {
        this.in = in;
        this.out = out;
    }

    @Override
    protected void describe() {
        out.add(in);
    }
}
