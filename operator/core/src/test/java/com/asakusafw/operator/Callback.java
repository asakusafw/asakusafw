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
package com.asakusafw.operator;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOError;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import com.asakusafw.operator.description.ClassDescription;
import com.asakusafw.operator.description.EnumConstantDescription;
import com.asakusafw.operator.model.OperatorElement;

/**
 * Callback from {@link DelegateProcessor}.
 */
public abstract class Callback {

    private RuntimeException runtimeException;

    private Error error;

    /**
     * The current compiling environment.
     */
    public CompileEnvironment env;

    /**
     * The type utilities.
     */
    public Types types;

    /**
     * The element utilities.
     */
    public Elements elements;

    /**
     * The current rounding environment.
     */
    public RoundEnvironment round;

    /**
     * Target annotations.
     */
    public Set<? extends TypeElement> annotatios;

    private final boolean oneshot;

    private int count = 0;

    /**
     * Creates a new instance.
     */
    public Callback() {
        this(true);
    }

    /**
     * Creates a new instance.
     * @param oneshot one shot execution
     */
    public Callback(boolean oneshot) {
        this.oneshot = oneshot;
    }

    /**
     * Runs {@link #test()} method.
     * @param pEnv processing environment
     * @param rEnv rounding environment
     * @param annotations target annotations
     */
    public void run(ProcessingEnvironment pEnv, RoundEnvironment rEnv, Set<? extends TypeElement> annotations) {
        this.env = createCompileEnvironment(pEnv);
        this.round = rEnv;
        this.types = pEnv.getTypeUtils();
        this.elements = pEnv.getElementUtils();
        this.annotatios = annotations;
        try {
            if (count++ == 0 || oneshot == false) {
                test();
            }
        } catch (IOException e) {
            this.error = new IOError(e);
        } catch (RuntimeException e) {
            this.runtimeException = e;
        } catch (Error e) {
            this.error = e;
        }
    }

    /**
     * Creates a compile environment for this processing (for testing).
     * @param processingEnv current processing environment
     * @return created environment
     */
    protected CompileEnvironment createCompileEnvironment(ProcessingEnvironment processingEnv) {
        return CompileEnvironment.newInstance(
                processingEnv,
                CompileEnvironment.Support.DATA_MODEL_REPOSITORY,
                CompileEnvironment.Support.OPERATOR_DRIVER);
    }

    /**
     * Throws exceptions/errors which are thrown in {@link #test()}.
     */
    public void rethrow() {
        if (runtimeException != null) {
            throw runtimeException;
        } else if (error != null) {
            throw error;
        }
    }

    /**
     * Performs the test.
     * @throws IOException if compilation was failed
     */
    protected abstract void test() throws IOException;

    /**
     * Returns the declared type.
     * @param aClass target raw type
     * @param arguments type arguments
     * @return the declared type
     */
    public DeclaredType getType(Class<?> aClass, TypeMirror...arguments) {
        String typeName = aClass.getName();
        return getType(typeName, arguments);
    }

    /**
     * Returns the declared type.
     * @param typeName target type name
     * @param arguments type arguments
     * @return the declared type
     */
    public DeclaredType getType(String typeName, TypeMirror... arguments) {
        TypeElement type = elements.getTypeElement(typeName);
        assertThat(typeName, type, not(nullValue()));
        if (arguments.length == 0) {
            return (DeclaredType) types.erasure(type.asType());
        } else {
            return types.getDeclaredType(type, arguments);
        }
    }

    /**
     * Returns the type parameter.
     * @param typeName target type name
     * @param name parameter name
     * @return the type variable
     */
    public TypeVariable getTypeVariable(String typeName, String name) {
        return getTypeVariable(elements.getTypeElement(typeName), name);
    }

    /**
     * Returns the type parameter.
     * @param element type parameter owner
     * @param name parameter name
     * @return the type variable
     */
    public TypeVariable getTypeVariable(TypeElement element, String name) {
        return getTypeParameters(name, element.getTypeParameters());
    }

    /**
     * Returns the type parameter.
     * @param element type parameter owner
     * @param name parameter name
     * @return the type variable
     */
    public TypeVariable getTypeVariable(ExecutableElement element, String name) {
        return getTypeParameters(name, element.getTypeParameters());
    }

    private TypeVariable getTypeParameters(String name, List<? extends TypeParameterElement> typeParameters) {
        for (TypeParameterElement param : typeParameters) {
            if (param.getSimpleName().contentEquals(name)) {
                return (TypeVariable) param.asType();
            }
        }
        throw new AssertionError(name);
    }

    /**
     * Returns the matcher that compares to the specified type.
     * @param aClass target raw type
     * @param arguments type arguments
     * @return the matcher
     */
    public Matcher<TypeMirror> sameType(Class<?> aClass, TypeMirror...arguments) {
        return sameType(getType(aClass, arguments));
    }

    /**
     * Returns the matcher that compares to the specified type.
     * @param typeName target type name
     * @param arguments type arguments
     * @return the matcher
     */
    public Matcher<TypeMirror> sameType(String typeName, TypeMirror...arguments) {
        return sameType(getType(typeName, arguments));
    }

    /**
     * Returns the matcher that compares to the specified type.
     * @param type target type
     * @return the matcher
     */
    public Matcher<TypeMirror> sameType(TypeMirror type) {
        return new BaseMatcher<TypeMirror>() {
            @Override
            public boolean matches(Object arg) {
                return env.getProcessingEnvironment().getTypeUtils().isSameType((TypeMirror) arg, type);
            }
            @Override
            public void describeTo(Description desc) {
                desc.appendValue(type);
            }
        };
    }

    /**
     * Returns the matcher that compares to the specified type kind.
     * @param kind target type kind
     * @return the matcher
     */
    public Matcher<TypeMirror> kindOf(TypeKind kind) {
        return new BaseMatcher<TypeMirror>() {
            @Override
            public boolean matches(Object arg) {
                return ((TypeMirror) arg).getKind() == kind;
            }
            @Override
            public void describeTo(Description desc) {
                desc.appendValue(kind);
            }
        };
    }

    /**
     * Returns a matcher that tests whether or not the operator has the specified attribute.
     * @param attributeType the attribute type
     * @return the matcher
     */
    public Matcher<OperatorElement> hasAttribute(Class<? extends Enum<?>> attributeType) {
        ClassDescription type = ClassDescription.of(attributeType);
        return new BaseMatcher<OperatorElement>() {
            @Override
            public boolean matches(Object item) {
                return ((OperatorElement) item).getDescription().getAttributes().stream()
                        .filter(EnumConstantDescription.class::isInstance)
                        .map(EnumConstantDescription.class::cast)
                        .map(EnumConstantDescription::getDeclaringClass)
                        .anyMatch(Predicate.isEqual(type));
            }
            @Override
            public void describeTo(Description description) {
                description.appendText("has attribute ").appendValue(type.getClassName());
            }
        };
    }

    /**
     * Returns a matcher that tests whether or not the operator has the specified attribute.
     * @param attribute the attribute
     * @return the matcher
     */
    public Matcher<OperatorElement> hasAttribute(Enum<?> attribute) {
        EnumConstantDescription desc = EnumConstantDescription.of(attribute);
        return new BaseMatcher<OperatorElement>() {
            @Override
            public boolean matches(Object item) {
                return ((OperatorElement) item).getDescription().getAttributes().contains(desc);
            }
            @Override
            public void describeTo(Description description) {
                description.appendText("has attribute ").appendValue(desc);
            }
        };
    }
}
