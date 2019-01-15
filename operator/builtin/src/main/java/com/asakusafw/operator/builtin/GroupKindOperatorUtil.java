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
package com.asakusafw.operator.builtin;

import com.asakusafw.operator.builtin.DslBuilder.AnnotationRef;
import com.asakusafw.operator.builtin.DslBuilder.ElementRef;
import com.asakusafw.operator.builtin.DslBuilder.TypeRef;
import com.asakusafw.operator.description.ClassDescription;
import com.asakusafw.operator.description.EnumConstantDescription;

final class GroupKindOperatorUtil {

    private static final String INPUT_BUFFER = "inputBuffer"; //$NON-NLS-1$

    static final ClassDescription TYPE_ONCE = new ClassDescription("com.asakusafw.vocabulary.model.Once"); //$NON-NLS-1$

    static final ClassDescription TYPE_SPILL = new ClassDescription("com.asakusafw.vocabulary.model.Spill"); //$NON-NLS-1$

    private static final ClassDescription TYPE_INPUT_BUFFER =
            new ClassDescription("com.asakusafw.vocabulary.flow.processor.InputBuffer"); //$NON-NLS-1$

    private static final ClassDescription TYPE_BUFFER_TYPE =
            new ClassDescription("com.asakusafw.vocabulary.attribute.BufferType"); //$NON-NLS-1$

    private static final EnumConstantDescription INPUT_BUFFER_ESCAPE =
            new EnumConstantDescription(TYPE_INPUT_BUFFER, "ESCAPE"); //$NON-NLS-1$

    private static final EnumConstantDescription BUFFER_TYPE_HEAP =
            new EnumConstantDescription(TYPE_BUFFER_TYPE, "HEAP"); //$NON-NLS-1$

    private static final EnumConstantDescription BUFFER_TYPE_SPILL =
            new EnumConstantDescription(TYPE_BUFFER_TYPE, "SPILL"); //$NON-NLS-1$

    private static final EnumConstantDescription BUFFER_TYPE_VOLATILE =
            new EnumConstantDescription(TYPE_BUFFER_TYPE, "VOLATILE"); //$NON-NLS-1$

    private GroupKindOperatorUtil() {
        return;
    }

    static EnumConstantDescription getInputBuffer(DslBuilder dsl) {
        return dsl.annotation().constant(INPUT_BUFFER);
    }

    static EnumConstantDescription getBufferType(ElementRef parameter, EnumConstantDescription parent) {
        /*
         * - inputBuffer: EXPAND (default)
         *   -            () : HEAP
         *   -        (Once) : VOLATILE
         *   -       (Spill) : SPILL
         *   - (Once, Spill) : VOLATILE
         * - inputBuffer = ESCAPE
         *   -            () : SPILL
         *   -        (Once) : VOLATILE
         *   -       (Spill) : SPILL
         *   - (Once, Spill) : VOLATILE
         */
        TypeRef type = parameter.type();
        AnnotationRef once = parameter.annotation(TYPE_ONCE);
        if (once != null) {
            if (type.isIterable() == false) {
                once.error(Messages.getString("GroupKindOperatorUtil.errorOnceNotIterable")); //$NON-NLS-1$
                return BUFFER_TYPE_HEAP;
            } else {
                return BUFFER_TYPE_VOLATILE;
            }
        }
        AnnotationRef spill = parameter.annotation(TYPE_SPILL);
        if (spill != null || parent.equals(INPUT_BUFFER_ESCAPE)) {
            return BUFFER_TYPE_SPILL;
        }
        // default buffer type
        return BUFFER_TYPE_HEAP;
    }
}
