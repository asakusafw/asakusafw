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
package com.asakusafw.operator.method;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import com.asakusafw.operator.CompileEnvironment;
import com.asakusafw.operator.Constants;
import com.asakusafw.operator.description.ClassDescription;
import com.asakusafw.operator.model.OperatorClass;
import com.asakusafw.operator.util.DescriptionHelper;
import com.asakusafw.operator.util.Logger;
import com.asakusafw.utils.java.jsr269.bridge.Jsr269;
import com.asakusafw.utils.java.model.syntax.ArrayType;
import com.asakusafw.utils.java.model.syntax.ClassDeclaration;
import com.asakusafw.utils.java.model.syntax.CompilationUnit;
import com.asakusafw.utils.java.model.syntax.FormalParameterDeclaration;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.syntax.TypeParameterDeclaration;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ImportBuilder;
import com.asakusafw.utils.java.model.util.ImportBuilder.Strategy;
import com.asakusafw.utils.java.model.util.JavadocBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;

/**
 * Emits operator implementation classes for operator methods.
 */
public class OperatorImplementationEmitter {

    static final Logger LOG = Logger.get(OperatorImplementationEmitter.class);

    private final CompileEnvironment environment;

    /**
     * Creates a new instance.
     * @param environment current compiling environment
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public OperatorImplementationEmitter(CompileEnvironment environment) {
        this.environment = Objects.requireNonNull(environment, "environment must not be null"); //$NON-NLS-1$
    }

    /**
     * Emits an operator implementation class.
     * @param operatorClass target class description
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void emit(OperatorClass operatorClass) {
        Objects.requireNonNull(operatorClass, "operatorClass must not be null"); //$NON-NLS-1$
        ClassDescription key = Constants.getImplementationClass(operatorClass.getDeclaration().getQualifiedName());
        if (environment.isResourceGenerated(key)) {
            LOG.debug("class is already generated: {}", key.getClassName()); //$NON-NLS-1$
            return;
        }
        CompilationUnit unit = Generator.generate(environment, operatorClass);
        try {
            environment.emit(unit, operatorClass.getDeclaration());
            environment.setResourceGenerated(key);
        } catch (IOException e) {
            environment.getProcessingEnvironment().getMessager().printMessage(Diagnostic.Kind.ERROR,
                    MessageFormat.format(
                            Messages.getString("OperatorImplementationEmitter.errorFailEmit"), //$NON-NLS-1$
                            e.toString()),
                    operatorClass.getDeclaration());
            LOG.error(MessageFormat.format(
                    Messages.getString("OperatorImplementationEmitter.logFailEmit"), //$NON-NLS-1$
                    operatorClass.getDeclaration().getQualifiedName()), e);
        }
    }

    private static final class Generator {

        private final CompileEnvironment environment;

        private final ModelFactory f;

        private final TypeElement operatorClass;

        private final Jsr269 converter;

        private final ImportBuilder imports;

        private Generator(CompileEnvironment environment, TypeElement operatorClass) {
            assert environment != null;
            assert operatorClass != null;
            this.environment = environment;
            this.f = Models.getModelFactory();
            this.converter = new Jsr269(f);
            this.operatorClass = operatorClass;
            this.imports = new ImportBuilder(
                    f,
                    converter.convert((PackageElement) operatorClass.getEnclosingElement()),
                    Strategy.TOP_LEVEL);
        }

        static CompilationUnit generate(CompileEnvironment environment, OperatorClass operatorClass) {
            Generator generator = new Generator(environment, operatorClass.getDeclaration());
            return generator.generate();
        }

        CompilationUnit generate() {
            ClassDeclaration typeDecl = generateClass();
            return f.newCompilationUnit(
                    imports.getPackageDeclaration(),
                    imports.toImportDeclarations(),
                    Collections.singletonList(typeDecl));
        }

        private ClassDeclaration generateClass() {
            Types types = environment.getProcessingEnvironment().getTypeUtils();
            DeclaredType superClass = types.getDeclaredType(operatorClass);
            SimpleName className = generateClassName();
            imports.resolvePackageMember(className);
            List<TypeBodyDeclaration> members = new ArrayList<>();
            members.add(generateConstructor());
            members.addAll(generateMembers());
            return f.newClassDeclaration(
                    new JavadocBuilder(f)
                        .inline(Messages.getString("OperatorImplementationEmitter.javadocClassSynopsis"), //$NON-NLS-1$
                                d -> d.linkType(imports.resolve(converter.convert(superClass))))
                        .toJavadoc(),
                    new AttributeBuilder(f)
                        .annotation(DescriptionHelper.resolveAnnotation(imports, Constants.getGenetedAnnotation()))
                        .Public()
                        .toAttributes(),
                    className,
                    imports.resolve(converter.convert(superClass)),
                    Collections.emptyList(),
                    members);
        }

        private TypeBodyDeclaration generateConstructor() {
            return f.newConstructorDeclaration(
                    new JavadocBuilder(f)
                        .text(Messages.getString("OperatorImplementationEmitter.javadocConstructorSynopsis")) //$NON-NLS-1$
                        .toJavadoc(),
                    new AttributeBuilder(f)
                        .Public()
                        .toAttributes(),
                    generateClassName(),
                    Collections.emptyList(),
                    Collections.singletonList(f.newReturnStatement()));
        }

        private SimpleName generateClassName() {
            ClassDescription aClass = Constants.getImplementationClass(operatorClass.getQualifiedName());
            return f.newSimpleName(aClass.getSimpleName());
        }

        private List<TypeBodyDeclaration> generateMembers() {
            List<TypeBodyDeclaration> results = new ArrayList<>();
            for (ExecutableElement method : ElementFilter.methodsIn(operatorClass.getEnclosedElements())) {
                if (method.getModifiers().contains(Modifier.ABSTRACT)) {
                    results.add(generateImplementor(method));
                }
            }
            return results;
        }

        private MethodDeclaration generateImplementor(ExecutableElement method) {
            return f.newMethodDeclaration(
                    null,
                    new AttributeBuilder(f)
                        .annotation(imports.toType(Override.class))
                        .Public()
                        .toAttributes(),
                    toTypeParameters(method),
                    imports.resolve(converter.convert(method.getReturnType())),
                    f.newSimpleName(method.getSimpleName().toString()),
                    toParameters(method),
                    0,
                    Collections.emptyList(),
                    f.newBlock(new TypeBuilder(f, imports.toType(UnsupportedOperationException.class))
                        .newObject()
                        .toThrowStatement()));
        }

        private List<FormalParameterDeclaration> toParameters(ExecutableElement method) {
            assert method != null;
            List<? extends VariableElement> parameters = method.getParameters();
            List<FormalParameterDeclaration> results = new ArrayList<>();
            for (int i = 0, n = parameters.size(); i < n; i++) {
                VariableElement var = parameters.get(i);
                boolean varArgs = (i == n - 1) && method.isVarArgs();
                Type type = converter.convert(var.asType());
                if (varArgs && type.getModelKind() == ModelKind.ARRAY_TYPE) {
                    type = ((ArrayType) type).getComponentType();
                }
                results.add(f.newFormalParameterDeclaration(
                        Collections.emptyList(),
                        imports.resolve(type),
                        varArgs,
                        f.newSimpleName(var.getSimpleName().toString()),
                        0));
            }
            return results;
        }

        private List<TypeParameterDeclaration> toTypeParameters(ExecutableElement method) {
            assert method != null;
            List<? extends TypeParameterElement> typeParameters = method.getTypeParameters();
            if (typeParameters.isEmpty()) {
                return Collections.emptyList();
            }
            List<TypeParameterDeclaration> results = new ArrayList<>();
            for (TypeParameterElement typeParameter : typeParameters) {
                SimpleName name = f.newSimpleName(typeParameter.getSimpleName().toString());
                List<Type> typeBounds = new ArrayList<>();
                for (TypeMirror typeBound : typeParameter.getBounds()) {
                    typeBounds.add(imports.resolve(converter.convert(typeBound)));
                }
                results.add(f.newTypeParameterDeclaration(name, typeBounds));
            }
            return results;
        }
    }
}
