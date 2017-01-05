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
package com.asakusafw.operator.flowpart;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import com.asakusafw.operator.CompileEnvironment;
import com.asakusafw.operator.Constants;
import com.asakusafw.operator.model.DataModelMirror;
import com.asakusafw.operator.model.ExternMirror;
import com.asakusafw.operator.model.OperatorClass;
import com.asakusafw.operator.model.OperatorDescription;
import com.asakusafw.operator.model.OperatorDescription.Document;
import com.asakusafw.operator.model.OperatorDescription.Node;
import com.asakusafw.operator.model.OperatorDescription.Node.Kind;
import com.asakusafw.operator.model.OperatorDescription.ParameterReference;
import com.asakusafw.operator.model.OperatorDescription.Reference;
import com.asakusafw.operator.model.OperatorElement;
import com.asakusafw.operator.util.AnnotationHelper;
import com.asakusafw.operator.util.ElementHelper;
import com.asakusafw.operator.util.TypeHelper;

/**
 * Analyzes flow-part classes.
 */
public class FlowPartAnalyzer {

    private final CompileEnvironment environment;

    private final TypeElement annotationDecl;

    private final Map<TypeElement, AnnotationMirror> flowpartClasses;

    /**
     * Creates a new instance.
     * @param environment current compiling environment
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public FlowPartAnalyzer(CompileEnvironment environment) {
        this.environment = Objects.requireNonNull(environment, "environment must not be null"); //$NON-NLS-1$
        this.annotationDecl = environment.findTypeElement(Constants.TYPE_FLOW_PART);
        this.flowpartClasses = new HashMap<>();
    }

    /**
     * Registers a flow-part class declaration to this analyzer.
     * @param typeDecl flow-part type declaration
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void register(TypeElement typeDecl) {
        Objects.requireNonNull(typeDecl, "typeDecl must not be null"); //$NON-NLS-1$
        AnnotationMirror annotation = AnnotationHelper.findAnnotation(environment, annotationDecl, typeDecl);
        if (annotation == null) {
            environment.getProcessingEnvironment().getMessager().printMessage(Diagnostic.Kind.ERROR,
                    MessageFormat.format(
                            Messages.getString("FlowPartAnalyzer.errorFailExtractAnnotation"), //$NON-NLS-1$
                            typeDecl.getSimpleName(),
                            annotationDecl.getSimpleName()),
                            typeDecl);
            return;
        }
        this.flowpartClasses.put(typeDecl, annotation);
    }

    /**
     * Resolves previously {@link #register(TypeElement) registered} flow-part classes.
     * @return resolved operator classes, or {@code null} if no valid flow-part classes are registered
     */
    public Collection<OperatorClass> resolve() {
        List<OperatorClass> results = new ArrayList<>();
        for (Map.Entry<TypeElement, AnnotationMirror> entry : flowpartClasses.entrySet()) {
            OperatorClass resolved = resolve(entry.getValue(), entry.getKey());
            if (resolved != null) {
                results.add(resolved);
            }
        }
        return results;
    }

    private OperatorClass resolve(AnnotationMirror annotation, TypeElement classDecl) {
        assert annotation != null;
        assert classDecl != null;
        if (validateClass(classDecl) == false) {
            return null;
        }
        List<ExecutableElement> validConstructors = selectValidConstructors(classDecl);
        List<OperatorElement> elements = new ArrayList<>();
        for (ExecutableElement constructor : validConstructors) {
            OperatorDescription description = analyze(annotation, constructor);
            if (description != null && ElementHelper.validate(environment, constructor, description) == false) {
                description = null;
            }
            if (elements.isEmpty()) {
                elements.add(new OperatorElement(annotation, constructor, description));
            }
        }
        return new OperatorClass(classDecl, elements);
    }

    private boolean validateClass(TypeElement type) {
        assert type != null;
        boolean valid = true;
        DeclaredType superType = environment.findDeclaredType(Constants.TYPE_FLOW_DESCRIPTION);
        if (environment.getProcessingEnvironment().getTypeUtils().isSubtype(type.asType(), superType) == false) {
            error(type, Messages.getString("FlowPartAnalyzer.errorClassNotSubtype"), //$NON-NLS-1$
                    type.getSimpleName(),
                    Constants.TYPE_FLOW_DESCRIPTION);
            valid = false;
        }
        if (type.getEnclosingElement().getKind() != ElementKind.PACKAGE) {
            error(type, Messages.getString("FlowPartAnalyzer.errorClassNotTopLevel"), //$NON-NLS-1$
                    type.getSimpleName());
            valid = false;
        }
        if (type.getModifiers().contains(Modifier.PUBLIC) == false) {
            error(type, Messages.getString("FlowPartAnalyzer.errorClassNotPublic"), //$NON-NLS-1$
                    type.getSimpleName());
            valid = false;
        }
        if (type.getModifiers().contains(Modifier.ABSTRACT)) {
            error(type, Messages.getString("FlowPartAnalyzer.errorClassAbstract"), //$NON-NLS-1$
                    type.getSimpleName());
            valid = false;
        }
        return valid;
    }

    private List<ExecutableElement> selectValidConstructors(TypeElement type) {
        assert type != null;
        List<ExecutableElement> results = new LinkedList<>();
        for (ExecutableElement element : ElementFilter.constructorsIn(type.getEnclosedElements())) {
            if (element.getModifiers().contains(Modifier.PUBLIC)) {
                results.add(element);
            }
        }
        if (results.isEmpty()) {
            error(type, Messages.getString("FlowPartAnalyzer.errorConstructorMissing"), type.getSimpleName()); //$NON-NLS-1$
        }
        if (results.size() >= 2) {
            for (ExecutableElement element : results) {
                error(element, Messages.getString("FlowPartAnalyzer.errorConstructorAmbiguous"), type.getSimpleName()); //$NON-NLS-1$
            }
        }
        for (Iterator<ExecutableElement> iter = results.iterator(); iter.hasNext();) {
            ExecutableElement ctor = iter.next();
            if (validateConstructor(ctor) == false) {
                iter.remove();
            }
        }
        return results;
    }

    private boolean validateConstructor(ExecutableElement ctor) {
        boolean valid = true;
        if (ctor.getThrownTypes().isEmpty() == false) {
            error(ctor, Messages.getString("FlowPartAnalyzer.errorConstructorException")); //$NON-NLS-1$
            valid = false;
        }
        if (ctor.getTypeParameters().isEmpty() == false) {
            error(ctor, Messages.getString("FlowPartAnalyzer.errorConstructorGeneric")); //$NON-NLS-1$
            valid = false;
        }
        boolean sawIn = false;
        boolean sawOut = false;
        boolean sawFlowIn = false;
        boolean sawFlowOut = false;
        TypeElement importType = environment.findTypeElement(Constants.TYPE_IMPORT);
        TypeElement exportType = environment.findTypeElement(Constants.TYPE_EXPORT);
        for (VariableElement param : ctor.getParameters()) {
            TypeMirror type = param.asType();
            AnnotationMirror importer = AnnotationHelper.findAnnotation(environment, importType, param);
            AnnotationMirror exporter = AnnotationHelper.findAnnotation(environment, exportType, param);
            if (environment.isFlowpartExternalIo() == false) {
                if (importer != null) {
                    error(param, Messages.getString("FlowPartAnalyzer.errorConstructorImportAnnoattion")); //$NON-NLS-1$
                    valid = false;
                    continue;
                }
                if (exporter != null) {
                    error(param, Messages.getString("FlowPartAnalyzer.errorConstructorExportAnnotation")); //$NON-NLS-1$
                    valid = false;
                    continue;
                }
            }
            boolean in = TypeHelper.isIn(environment, type);
            boolean out = TypeHelper.isOut(environment, type);
            if (in) {
                sawIn = true;
                TypeMirror component = TypeHelper.getInType(environment, type);
                if (component == null) {
                    error(param, Messages.getString("FlowPartAnalyzer.errorInputRawInType")); //$NON-NLS-1$
                    valid = false;
                    continue;
                }
                DataModelMirror model = environment.findDataModel(component);
                if (model == null) {
                    error(param, Messages.getString("FlowPartAnalyzer.errorInputNotDataModelType"), component); //$NON-NLS-1$
                    valid = false;
                    continue;
                }
                sawFlowIn |= (importer == null);
                valid &= validateImport(param, component, importer);
            } else {
                if (importer != null) {
                    error(param, Messages.getString("FlowPartAnalyzer.errorImportNotIn"), type); //$NON-NLS-1$
                    valid = false;
                    continue;
                }
            }
            if (out) {
                sawOut = true;
                TypeMirror component = TypeHelper.getOutType(environment, type);
                if (component == null) {
                    error(param, Messages.getString("FlowPartAnalyzer.errorOutputRawOutType")); //$NON-NLS-1$
                    valid = false;
                    continue;
                }
                DataModelMirror model = environment.findDataModel(component);
                if (model == null) {
                    error(param, Messages.getString("FlowPartAnalyzer.errorOutputNotDataModelType"), component); //$NON-NLS-1$
                    valid = false;
                    continue;
                }
                sawFlowOut |= (exporter == null);
                valid &= validateExport(param, component, exporter);
            } else {
                if (exporter != null) {
                    error(param, Messages.getString("FlowPartAnalyzer.errorExportNotOut"), type); //$NON-NLS-1$
                    valid = false;
                    continue;
                }
            }
        }
        if (sawIn == false) {
            error(ctor, Messages.getString("FlowPartAnalyzer.errorInMissing")); //$NON-NLS-1$
            valid = false;
        } else if (sawFlowIn == false) {
            error(ctor, Messages.getString("FlowPartAnalyzer.errorInWithoutExternMissing")); //$NON-NLS-1$
            valid = false;
        }
        if (sawOut == false) {
            error(ctor, Messages.getString("FlowPartAnalyzer.errorOutMissing")); //$NON-NLS-1$
            valid = false;
        } else if (sawFlowOut == false) {
            error(ctor, Messages.getString("FlowPartAnalyzer.errorOutWithoutExternMissing")); //$NON-NLS-1$
            valid = false;
        }
        return valid;
    }

    private boolean validateImport(VariableElement param, TypeMirror component, AnnotationMirror extern) {
        assert param != null;
        assert component != null;
        boolean valid = true;
        if (extern != null) {
            if (component.getKind() == TypeKind.TYPEVAR) {
                error(param, Messages.getString("FlowPartAnalyzer.errorImportTypeVariable"), component); //$NON-NLS-1$
                valid = false;
            }
            AnnotationValue value = AnnotationHelper.getValue(environment, extern, "description"); //$NON-NLS-1$
            if (value.getValue() instanceof TypeMirror) {
                TypeMirror desc = (TypeMirror) value.getValue();
                Types types = environment.getProcessingEnvironment().getTypeUtils();
                if (types.isSubtype(desc, environment.findDeclaredType(Constants.TYPE_IMPORTER_DESC)) == false) {
                    error(param, Messages.getString("FlowPartAnalyzer.errorImportInvalidDescriptionType"), desc); //$NON-NLS-1$
                    valid = false;
                }
            }
        }
        return valid;
    }

    private boolean validateExport(VariableElement param, TypeMirror component, AnnotationMirror extern) {
        assert param != null;
        assert component != null;
        boolean valid = true;
        if (extern != null) {
            if (component.getKind() == TypeKind.TYPEVAR) {
                error(param, Messages.getString("FlowPartAnalyzer.errorExportTypeVariable"), component); //$NON-NLS-1$
                valid = false;
            }
            AnnotationValue value = AnnotationHelper.getValue(environment, extern, "description"); //$NON-NLS-1$
            if (value.getValue() instanceof TypeMirror) {
                TypeMirror desc = (TypeMirror) value.getValue();
                Types types = environment.getProcessingEnvironment().getTypeUtils();
                if (types.isSubtype(desc, environment.findDeclaredType(Constants.TYPE_EXPORTER_DESC)) == false) {
                    error(param, Messages.getString("FlowPartAnalyzer.errorExportInvalidDescriptionType"), desc); //$NON-NLS-1$
                    valid = false;
                }
            }
        }
        return valid;
    }

    private OperatorDescription analyze(AnnotationMirror annotation, ExecutableElement constructor) {
        assert annotation != null;
        assert constructor != null;
        List<Node> parameters = new ArrayList<>();
        List<Node> outputs = new ArrayList<>();
        TypeElement importType = environment.findTypeElement(Constants.TYPE_IMPORT);
        TypeElement exportType = environment.findTypeElement(Constants.TYPE_EXPORT);
        int index = -1;
        for (VariableElement param : constructor.getParameters()) {
            index++;
            ParameterReference reference = Reference.parameter(index);
            String name = param.getSimpleName().toString();
            TypeMirror type = param.asType();
            if (TypeHelper.isIn(environment, type)) {
                AnnotationMirror importer = AnnotationHelper.findAnnotation(environment, importType, param);
                ExternMirror extern = importer == null ? null : ExternMirror.parse(environment, importer, param);
                TypeMirror component = TypeHelper.getInType(environment, type);
                parameters.add(new Node(Kind.INPUT, name, Document.reference(reference), component, reference)
                        .withExtern(extern));
            } else if (TypeHelper.isOut(environment, type)) {
                AnnotationMirror exporter = AnnotationHelper.findAnnotation(environment, exportType, param);
                ExternMirror extern = exporter == null ? null : ExternMirror.parse(environment, exporter, param);
                TypeMirror component = TypeHelper.getOutType(environment, type);
                outputs.add(new Node(Kind.OUTPUT, name, Document.reference(reference), component, reference)
                        .withExtern(extern));
            } else {
                parameters.add(new Node(Kind.DATA, name, Document.reference(reference), type, reference));
            }
        }
        OperatorDescription description = new OperatorDescription(
                Document.reference(Reference.method()), parameters, outputs);
        return description;
    }

    private void error(Element element, String pattern, Object... arguments) {
        assert element != null;
        assert pattern != null;
        assert arguments != null;
        String message = arguments.length == 0 ? pattern : MessageFormat.format(pattern, arguments);
        environment.getProcessingEnvironment().getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
    }
}
