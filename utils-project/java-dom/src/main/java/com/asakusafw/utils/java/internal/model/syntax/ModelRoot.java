/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
package com.asakusafw.utils.java.internal.model.syntax;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.WeakHashMap;

import com.asakusafw.utils.java.internal.model.util.ModelDigester;
import com.asakusafw.utils.java.internal.model.util.ModelEmitter;
import com.asakusafw.utils.java.internal.model.util.ModelMatcher;
import com.asakusafw.utils.java.internal.model.util.PrintEmitContext;
import com.asakusafw.utils.java.model.syntax.Model;

/**
 * A skeletal implementation of all {@link Model}.
 */
abstract class ModelRoot implements Model {

    private Map<Class<?>, Object> traits;

    @Override
    public <T> T findModelTrait(Class<T> traitClass) {
        if (traitClass == null) {
            throw new IllegalArgumentException("traitClass must not be null"); //$NON-NLS-1$
        }
        if (traits == null) {
            return null;
        }
        Object adapter = traits.get(traitClass);
        if (adapter == null) {
            return null;
        }
        return traitClass.cast(adapter);
    }

    @Override
    public <T> void putModelTrait(Class<T> traitClass, T traitObject) {
        if (traitClass == null) {
            throw new IllegalArgumentException("traitClass must not be null"); //$NON-NLS-1$
        }
        if (traitObject == null) {
            if (traits != null) {
                traits.remove(traitClass);
            }
        } else {
            assert traitClass.isInstance(traitObject);
            if (traits == null) {
                traits = new WeakHashMap<>(4, 0.75f);
            }
            traits.put(traitClass, traitObject);
        }
    }

    @Override
    public int hashCode() {
        return ModelDigester.compute(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if ((obj instanceof Model) == false) {
            return false;
        }
        return accept(ModelMatcher.INSTANCE, (Model) obj) ;
    }

    @Override
    public String toString() {
        StringWriter buffer = new StringWriter();
        PrintWriter output = new PrintWriter(buffer);
        try {
            ModelEmitter.emit(this, new PrintEmitContext(output));
        } catch (RuntimeException e) {
            e.printStackTrace(output);
        }
        output.flush();
        return buffer.toString();
    }
}
