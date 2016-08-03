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

import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asakusafw.operator.CompileEnvironment;
import com.asakusafw.operator.Constants;
import com.asakusafw.operator.description.ClassDescription;
import com.asakusafw.operator.model.JavaName;
import com.asakusafw.operator.model.OperatorClass;
import com.asakusafw.operator.model.OperatorDescription.Node;
import com.asakusafw.operator.model.OperatorElement;
import com.asakusafw.operator.util.DescriptionHelper;
import com.asakusafw.operator.util.ElementHelper;
import com.asakusafw.operator.util.JavadocHelper;
import com.asakusafw.utils.java.jsr269.bridge.Jsr269;
import com.asakusafw.utils.java.model.syntax.ClassDeclaration;
import com.asakusafw.utils.java.model.syntax.ClassLiteral;
import com.asakusafw.utils.java.model.syntax.CompilationUnit;
import com.asakusafw.utils.java.model.syntax.ConstructorDeclaration;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.FieldDeclaration;
import com.asakusafw.utils.java.model.syntax.Javadoc;
import com.asakusafw.utils.java.model.syntax.MethodDeclaration;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Statement;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.TypeBodyDeclaration;
import com.asakusafw.utils.java.model.util.AttributeBuilder;
import com.asakusafw.utils.java.model.util.ImportBuilder;
import com.asakusafw.utils.java.model.util.ImportBuilder.Strategy;
import com.asakusafw.utils.java.model.util.JavadocBuilder;
import com.asakusafw.utils.java.model.util.Models;
import com.asakusafw.utils.java.model.util.TypeBuilder;

/**
 * Emits operator factories for operator methods.
 */
public class OperatorFactoryEmitter {

    static final Logger LOG = LoggerFactory.getLogger(OperatorFactoryEmitter.class);

    private final CompileEnvironment environment;

    /**
     * Creates a new instance.
     * @param environment current compiling environment
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public OperatorFactoryEmitter(CompileEnvironment environment) {
        this.environment = Objects.requireNonNull(environment, "environment must not be null"); //$NON-NLS-1$
    }

    /**
     * Emits an operator factory class.
     * @param operatorClass target class description
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void emit(OperatorClass operatorClass) {
        Objects.requireNonNull(operatorClass, "operatorClass must not be null"); //$NON-NLS-1$
        ClassDescription key = Constants.getFactoryClass(operatorClass.getDeclaration().getQualifiedName());
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
                            Messages.getString("OperatorFactoryEmitter.errorFailEmit"), //$NON-NLS-1$
                            e.toString()),
                    operatorClass.getDeclaration());
            LOG.error(MessageFormat.format(
                    Messages.getString("OperatorFactoryEmitter.logFailEmit"), //$NON-NLS-1$
                    operatorClass.getDeclaration().getQualifiedName()), e);
        }
    }

    private static final class Generator {

        private final CompileEnvironment environment;

        private final ModelFactory f;

        private final OperatorClass operatorClass;

        private final Jsr269 converter;

        private final ImportBuilder imports;

        private Generator(CompileEnvironment environment, OperatorClass operatorClass) {
            assert environment != null;
            assert operatorClass != null;
            this.environment = environment;
            this.f = Models.getModelFactory();
            this.converter = new Jsr269(f);
            this.operatorClass = operatorClass;
            this.imports = new ImportBuilder(
                    f,
                    converter.convert((PackageElement) operatorClass.getDeclaration().getEnclosingElement()),
                    Strategy.TOP_LEVEL);
        }

        static CompilationUnit generate(CompileEnvironment environment, OperatorClass operatorClass) {
            Generator generator = new Generator(environment, operatorClass);
            return generator.generate();
        }

        CompilationUnit generate() {
            reserveNameSpace();
            ClassDeclaration typeDecl = generateClass();
            return f.newCompilationUnit(
                    imports.getPackageDeclaration(),
                    imports.toImportDeclarations(),
                    Collections.singletonList(typeDecl),
                    Collections.emptyList());
        }

        private ClassDeclaration generateClass() {
            Types types = environment.getProcessingEnvironment().getTypeUtils();
            DeclaredType originalClass = types.getDeclaredType(operatorClass.getDeclaration());
            SimpleName className = generateClassName();
            List<TypeBodyDeclaration> members = new ArrayList<>();
            members.add(generateConstructor());
            members.addAll(generateMembers());
            return f.newClassDeclaration(
                    new JavadocBuilder(f)
                        .inline(Messages.getString("OperatorFactoryEmitter.javadocClassSynopsis"), //$NON-NLS-1$
                                d -> d.linkType(imports.resolve(converter.convert(originalClass))))
                        .toJavadoc(),
                    new AttributeBuilder(f)
                        .annotation(DescriptionHelper.resolveAnnotation(imports, Constants.getGenetedAnnotation()))
                        .annotation(ElementHelper.toOperatorFactoryAnnotation(environment, operatorClass, imports))
                        .Public()
                        .Final()
                        .toAttributes(),
                    className,
                    null,
                    Collections.emptyList(),
                    members);
        }

        private void reserveNameSpace() {
            SimpleName className = generateClassName();
            imports.resolvePackageMember(className);
            for (OperatorElement element : operatorClass.getElements()) {
                if (element.getDescription() == null) {
                    continue;
                }
                imports.resolvePackageMember(f.newQualifiedName(className, generateNodeClassName(element)));
            }
        }

        private List<TypeBodyDeclaration> generateMembers() {
            List<TypeBodyDeclaration> results = new ArrayList<>();
            for (OperatorElement element : operatorClass.getElements()) {
                if (element.getDescription() == null) {
                    continue;
                }
                ClassDeclaration node = generateNodeClass(element);
                Type type = imports.resolvePackageMember(f.newQualifiedName(generateClassName(), node.getName()));
                MethodDeclaration factory = generateFactoryMethod(element, type);
                results.add(node);
                results.add(factory);
            }
            return results;
        }

        private ClassDeclaration generateNodeClass(OperatorElement element) {
            List<TypeBodyDeclaration> members = new ArrayList<>();
            members.addAll(generateOutputFields(element));
            members.add(generateNodeConstructor(element));
            return f.newClassDeclaration(
                    generateNodeClassComment(element),
                    new AttributeBuilder(f)
                        .Public()
                        .Static()
                        .Final()
                        .toAttributes(),
                    generateNodeClassName(element),
                    ElementHelper.toTypeParameters(environment, element.getDeclaration().getTypeParameters(), imports),
                    null,
                    Collections.emptyList(),
                    members);
        }

        private List<FieldDeclaration> generateOutputFields(OperatorElement element) {
            List<FieldDeclaration> results = new ArrayList<>();
            for (Node node : element.getDescription().getOutputs()) {
                Type type = new TypeBuilder(f, DescriptionHelper.resolve(imports, Constants.TYPE_SOURCE))
                    .parameterize(imports.resolve(converter.convert(node.getType())))
                    .toType();
                results.add(f.newFieldDeclaration(
                        generateOutputFieldComment(element, node),
                        new AttributeBuilder(f)
                            .Public()
                            .Final()
                            .toAttributes(),
                        type,
                        f.newSimpleName(node.getName()),
                        null));
            }
            return results;
        }

        private ConstructorDeclaration generateNodeConstructor(OperatorElement element) {
            Type builderType = DescriptionHelper.resolve(imports, Constants.TYPE_ELEMENT_BUILDER);
            List<Statement> statements = new ArrayList<>();
            SimpleName builderVar = f.newSimpleName("$builder$"); //$NON-NLS-1$
            statements.add(new TypeBuilder(f, builderType)
                .method("createOperator", toOperatorDeclaration(element)) //$NON-NLS-1$
                .toLocalVariableDeclaration(builderType, builderVar));
            statements.addAll(ElementHelper.toNodeConstructorStatements(environment, element, builderVar, imports));
            return f.newConstructorDeclaration(
                    null,
                    new AttributeBuilder(f).toAttributes(),
                    generateNodeClassName(element),
                    ElementHelper.toParameters(environment, element, imports),
                    statements);
        }

        private List<Expression> toOperatorDeclaration(OperatorElement element) {
            assert element != null;
            List<Expression> results = new ArrayList<>();
            results.add(toLiteral(element.getAnnotation().getAnnotationType()));
            results.add(toLiteral(operatorClass.getDeclaration().asType()));
            if (operatorClass.getDeclaration().getModifiers().contains(Modifier.ABSTRACT)) {
                ClassDescription aClass = Constants.getImplementationClass(
                        operatorClass.getDeclaration().getQualifiedName());
                Type implementationClass = DescriptionHelper.resolve(imports, aClass);
                results.add(f.newClassLiteral(implementationClass));
            }
            results.add(Models.toLiteral(f, element.getDeclaration().getSimpleName().toString()));
            for (VariableElement param : element.getDeclaration().getParameters()) {
                results.add(toLiteral(param.asType()));
            }
            return results;
        }

        private ClassLiteral toLiteral(TypeMirror type) {
            return f.newClassLiteral(imports.resolve(converter.convert(environment.getErasure(type))));
        }

        private SimpleName generateNodeClassName(OperatorElement element) {
            assert element != null;
            String name = JavaName.of(element.getDeclaration().getSimpleName().toString()).toTypeName();
            return f.newSimpleName(name);
        }

        private MethodDeclaration generateFactoryMethod(OperatorElement element, Type rawNodeType) {
            Type nodeType = ElementHelper.toParameterizedType(
                    environment, element.getDeclaration().getTypeParameters(), rawNodeType, imports);
            return f.newMethodDeclaration(
                    generateFactoryMethodComment(element),
                    new AttributeBuilder(f)
                        .annotation(ElementHelper.toOperatorInfoAnnotation(environment, element, imports))
                        .Public()
                        .toAttributes(),
                    ElementHelper.toTypeParameters(environment, element.getDeclaration().getTypeParameters(), imports),
                    nodeType,
                    f.newSimpleName(element.getDeclaration().getSimpleName().toString()),
                    ElementHelper.toParameters(environment, element, imports),
                    0,
                    Collections.emptyList(),
                    f.newBlock(new TypeBuilder(f, nodeType)
                        .newObject(ElementHelper.toArguments(environment, element, imports))
                        .toReturnStatement()));
        }

        private TypeBodyDeclaration generateConstructor() {
            return f.newConstructorDeclaration(
                    new JavadocBuilder(f)
                        .text(Messages.getString("OperatorFactoryEmitter.javadocConstructorSynopsis")) //$NON-NLS-1$
                        .toJavadoc(),
                    new AttributeBuilder(f)
                        .Public()
                        .toAttributes(),
                    generateClassName(),
                    Collections.emptyList(),
                    Collections.singletonList(f.newReturnStatement()));
        }

        private SimpleName generateClassName() {
            ClassDescription aClass = Constants.getFactoryClass(operatorClass.getDeclaration().getQualifiedName());
            return f.newSimpleName(aClass.getSimpleName());
        }

        private Javadoc generateFactoryMethodComment(OperatorElement element) {
            assert element != null;
            JavadocHelper source = new JavadocHelper(environment);
            source.put(element.getDeclaration());
            JavadocBuilder javadoc = new JavadocBuilder(f);
            javadoc.inline(source.get(element.getDescription().getDocument()));
            appendTypeParameterDocs(element, javadoc, source);
            for (Node node : element.getDescription().getParameters()) {
                javadoc.param(node.getName());
                javadoc.inline(source.get(node.getDocument()));
            }
            javadoc.returns().text(Messages.getString("OperatorFactoryEmitter.javadocFactoryMethodReturn")); //$NON-NLS-1$
            return javadoc.toJavadoc();
        }

        private Javadoc generateOutputFieldComment(OperatorElement element, Node output) {
            assert element != null;
            assert output != null;
            JavadocHelper source = new JavadocHelper(environment);
            source.put(element.getDeclaration());
            JavadocBuilder javadoc = new JavadocBuilder(f);
            javadoc.inline(source.get(output.getDocument()));
            return javadoc.toJavadoc();
        }

        private Javadoc generateNodeClassComment(OperatorElement element) {
            assert element != null;
            JavadocHelper source = new JavadocHelper(environment);
            source.put(element.getDeclaration());
            JavadocBuilder javadoc = new JavadocBuilder(f);
            javadoc.inline(source.get(element.getDescription().getDocument()));
            appendTypeParameterDocs(element, javadoc, source);
            return javadoc.toJavadoc();
        }

        private void appendTypeParameterDocs(OperatorElement element, JavadocBuilder javadoc, JavadocHelper source) {
            assert element != null;
            assert javadoc != null;
            assert source != null;
            for (TypeParameterElement param : element.getDeclaration().getTypeParameters()) {
                javadoc.typeParam(param.getSimpleName().toString());
                javadoc.inline(source.getTypeParameter(param.getSimpleName().toString()));
            }
        }
    }
}
