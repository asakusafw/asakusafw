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
package com.asakusafw.operator;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

import com.asakusafw.operator.CompileEnvironment;
import com.asakusafw.operator.DataModelMirrorRepository;
import com.asakusafw.operator.description.ClassDescription;
import com.asakusafw.operator.description.Descriptions;
import com.asakusafw.operator.model.ConcreteDataModelMirror;
import com.asakusafw.operator.model.DataModelMirror;
import com.asakusafw.operator.model.PartialDataModelMirror;
import com.asakusafw.operator.model.PropertyMirror;
import com.asakusafw.operator.model.PropertyMirrorCollector;

/**
 * Mock {@link DataModelMirrorRepository}.
 */
public class StringDataModelMirrorRepository implements DataModelMirrorRepository, PropertyMirrorCollector {

    @Override
    public DataModelMirror load(CompileEnvironment environment, TypeMirror type) {
        if (isTargetType(environment, type) == false) {
            return null;
        }
        if (type.getKind() == TypeKind.DECLARED) {
            return new ConcreteDataModelMirror(environment, (DeclaredType) type, this);
        } else if (type.getKind() == TypeKind.TYPEVAR) {
            return new PartialDataModelMirror(environment, (TypeVariable) type, this);
        }
        return null;
    }

    private boolean isTargetType(CompileEnvironment environment, TypeMirror type) {
        TypeMirror target = environment.getErasure(type);
        if (target.getKind() != TypeKind.DECLARED) {
            return false;
        }
        DeclaredType decl = (DeclaredType) target;
        String name = ((TypeElement) decl.asElement()).getQualifiedName().toString();
        if (name.equals(String.class.getName()) && type.getKind() == TypeKind.DECLARED) {
            return true;
        } else if (name.equals(CharSequence.class.getName()) && type.getKind() == TypeKind.TYPEVAR) {
            return true;
        }
        return false;
    }

    @Override
    public Set<PropertyMirror> collect(
            CompileEnvironment environment,
            TypeMirror targetType,
            List<TypeElement> upperBounds) {
        ClassDescription aClass = Descriptions.classOf(String.class);
        return Collections.singleton(new PropertyMirror("value", environment.findDeclaredType(aClass)));
    }
}
