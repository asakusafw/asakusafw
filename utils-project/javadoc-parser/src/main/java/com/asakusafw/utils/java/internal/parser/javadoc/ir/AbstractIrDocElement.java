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
package com.asakusafw.utils.java.internal.parser.javadoc.ir;

import java.io.Serializable;

/**
 * A skeletal implementation of {@link IrDocElement}.
 */
public abstract class AbstractIrDocElement implements IrDocElement, Serializable {

    private static final long serialVersionUID = 6223179902104926164L;

    private IrLocation location;

    @Override
    public IrLocation getLocation() {
        return this.location;
    }

    @Override
    public void setLocation(IrLocation location) {
        this.location = location;
    }
}
