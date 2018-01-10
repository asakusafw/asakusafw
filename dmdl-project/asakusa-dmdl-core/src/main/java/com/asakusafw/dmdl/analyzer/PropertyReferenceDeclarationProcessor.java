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
package com.asakusafw.dmdl.analyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.asakusafw.dmdl.model.AstAttributeValue;
import com.asakusafw.dmdl.model.AstAttributeValueArray;
import com.asakusafw.dmdl.model.AstAttributeValueMap;
import com.asakusafw.dmdl.model.AstDescription;
import com.asakusafw.dmdl.model.AstExpression;
import com.asakusafw.dmdl.model.AstModelReference;
import com.asakusafw.dmdl.model.AstNode.AbstractVisitor;
import com.asakusafw.dmdl.model.AstPropertyDefinition;
import com.asakusafw.dmdl.model.AstQualifiedName;
import com.asakusafw.dmdl.model.AstRecord;
import com.asakusafw.dmdl.model.AstRecordDefinition;
import com.asakusafw.dmdl.model.AstSimpleName;
import com.asakusafw.dmdl.model.AstTerm;
import com.asakusafw.dmdl.model.AstUnionExpression;
import com.asakusafw.dmdl.semantics.MemberDeclaration;
import com.asakusafw.dmdl.semantics.ModelDeclaration;
import com.asakusafw.dmdl.semantics.ModelSymbol;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.semantics.PropertyReferenceDeclaration;
import com.asakusafw.dmdl.semantics.PropertySymbol;
import com.asakusafw.dmdl.semantics.Type;
import com.asakusafw.dmdl.semantics.type.BasicType;
import com.asakusafw.dmdl.semantics.type.CollectionType;

final class PropertyReferenceDeclarationProcessor extends AbstractVisitor<ModelDeclaration, Void> {

    private final Context context;

    private PropertyReferenceDeclarationProcessor(Context context) {
        this.context = context;
    }

    static void resolve(Context context, ModelDeclaration model, AstExpression<AstRecord> node) {
        PropertyReferenceDeclarationProcessor resolver = new PropertyReferenceDeclarationProcessor(context);
        node.accept(model, resolver);
    }

    @Override
    public <T extends AstTerm<T>> Void visitUnionExpression(ModelDeclaration model, AstUnionExpression<T> node) {
        List<AstRecord> records = node.terms.stream()
                .map(it -> (AstRecord) it)
                .collect(Collectors.toList());
        resolveTerms(model, records);
        return null;
    }

    @Override
    public Void visitRecordDefinition(ModelDeclaration model, AstRecordDefinition node) {
        resolveTerms(model, Collections.singletonList(node));
        return null;
    }

    @Override
    public Void visitModelReference(ModelDeclaration model, AstModelReference node) {
        resolveTerms(model, Collections.singletonList(node));
        return null;
    }

    private void resolveTerms(ModelDeclaration model, List<? extends AstRecord> terms) {
        // collect model refs
        Set<ModelSymbol> modelRefs = terms.stream()
            .filter(t -> t instanceof AstModelReference)
            .map(t -> context.getWorld().findModelDeclaration(((AstModelReference) t).name.identifier))
            .peek(Objects::requireNonNull)
            .map(d -> d.getSymbol())
            .collect(Collectors.toSet());

        // resolve explicit property refs
        for (AstTerm<?> term : terms) {
            if (term instanceof AstRecordDefinition) {
                resolvePropertyReferences(model, (AstRecordDefinition) term, modelRefs);
            }
        }

        // resolve implicit property refs
        Map<String, PropertyReferenceDeclaration> implicits = new LinkedHashMap<>();
        for (AstTerm<?> term : terms) {
            if (term instanceof AstModelReference) {
                AstSimpleName name = ((AstModelReference) term).name;
                ModelDeclaration decl = context.getWorld().findModelDeclaration(name.identifier);
                assert decl != null;
                for (PropertyReferenceDeclaration ref : decl.getDeclaredPropertyReferences()) {
                    MemberDeclaration member = model.findMemberDeclaration(ref.getName().identifier);
                    if (member != null) {
                        if ((member instanceof PropertyReferenceDeclaration) == false) {
                            context.error(
                                    name,
                                    Messages.getString("DmdlAnalyzer.diagnosticDuplicatedProperty"), //$NON-NLS-1$
                                    ref.getName().identifier);
                        }
                        // also skip explicitly defined reference
                        continue;
                    }
                    PropertyReferenceDeclaration conflict = implicits.get(ref.getName().identifier);
                    if (conflict != null) {
                        if (ref.getReference().getKind() != conflict.getReference().getKind()
                                || ref.getType().isSame(conflict.getType()) == false) {
                            context.error(
                                    name,
                                    Messages.getString("DmdlAnalyzer.diagnosticInconsistentTypeProperty"), //$NON-NLS-1$
                                    ref.getName(),
                                    model.getName());
                            continue;
                        }
                        if (ref.getReference().isStub() == false
                                && conflict.getReference().isStub() == false
                                && isEquivalent(ref.getReference(), conflict.getReference()) == false) {
                            context.error(
                                    name,
                                    Messages.getString("PropertyReferenceDeclarationProcessor.diagnosticAmbiguousReferenceParent"), //$NON-NLS-1$
                                    name.identifier,
                                    conflict.getOwner().getName().identifier,
                                    ref.getName().identifier);
                            continue;
                        }
                    }
                    implicits.merge(ref.getName().identifier, ref, (a, b) -> a.getReference().isStub() ? b : a);
                }
            }
        }
        for (PropertyReferenceDeclaration ref : implicits.values()) {
            inheritPropertyReference(model, ref);
        }
    }

    private static boolean isEquivalent(
            PropertyReferenceDeclaration.ReferenceContainer<?> a,
            PropertyReferenceDeclaration.ReferenceContainer<?> b) {
        if (a.getKind() != b.getKind()) {
            return false;
        }
        if (a.isStub() || b.isStub()) {
            return false;
        }
        switch (a.getKind()) {
        case LIST:
            return isEquivalent(a.asList(), b.asList());
        case MAP:
            return isEquivalent(a.asMap(), b.asMap());
        default:
            throw new AssertionError(a.getKind());
        }
    }

    private static boolean isEquivalent(List<PropertySymbol> a, List<PropertySymbol> b) {
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0, n = a.size(); i < n; i++) {
            PropertySymbol aSymbol = a.get(i);
            PropertySymbol bSymbol = b.get(i);
            if (Objects.equals(aSymbol.getName(), bSymbol.getName()) == false) {
                return false;
            }
        }
        return true;
    }

    private static boolean isEquivalent(Map<String, PropertySymbol> a, Map<String, PropertySymbol> b) {
        if (a.size() != b.size()) {
            return false;
        }
        for (Map.Entry<String, PropertySymbol> entry : a.entrySet()) {
            PropertySymbol aSymbol = entry.getValue();
            PropertySymbol bSymbol = b.get(entry.getKey());
            if (bSymbol == null || Objects.equals(aSymbol.getName(), bSymbol.getName()) == false) {
                return false;
            }
        }
        return true;
    }

    private static PropertyReferenceDeclaration inheritPropertyReference(
            ModelDeclaration model, PropertyReferenceDeclaration ref) {
        PropertyReferenceDeclaration.ReferenceContainer<?> targets = ref.getReference();
        Map<PropertySymbol, PropertySymbol> mapping = new LinkedHashMap<>();
        for (PropertySymbol source : targets.getAllReferences()) {
            PropertyDeclaration destination = model.findPropertyDeclaration(source.getName().identifier);
            if (destination == null) {
                throw new IllegalStateException();
            }
            mapping.put(source, destination.getSymbol());
        }
        return model.declarePropertyReference(
                null,
                ref.getName(),
                ref.getType(),
                ref.getReference().remap(mapping::get),
                ref.getDescription(),
                ref.getAttributes());
    }

    private void resolvePropertyReferences(
            ModelDeclaration model, AstRecordDefinition node, Set<ModelSymbol> modelRefs) {
        for (AstPropertyDefinition property : node.properties) {
            // processes only reference properties
            if (property.getPropertyKind() != AstPropertyDefinition.PropertyKind.REFERENCE) {
                continue;
            }
            definePropertyReference(model, property, modelRefs);
        }
    }

    private void definePropertyReference(
            ModelDeclaration model, AstPropertyDefinition node, Set<ModelSymbol> modelRefs) {
        MemberDeclaration other = model.findMemberDeclaration(node.name.identifier);
        if (other != null) {
            context.error(
                    node.name,
                    Messages.getString("DmdlAnalyzer.diagnosticDuplicatedProperty"), //$NON-NLS-1$
                    node.name.identifier);
            return;
        }
        CollectionType explicitType;
        if (node.type == null) {
            explicitType = findInheritedType(modelRefs, node.name.identifier);
        } else {
            Type type = context.resolveType(node.type);
            if (type == null) {
                context.error(
                        node.type,
                        Messages.getString("DmdlAnalyzer.diagnosticUnknownTypeProperty"), //$NON-NLS-1$
                        node.type);
                return;
            }
            if (type instanceof CollectionType
                    && ((CollectionType) type).getElementType() instanceof BasicType) {
                explicitType = (CollectionType) type;
            } else {
                context.error(
                        node.type,
                        Messages.getString("PropertyReferenceDeclarationProcessor.diagnosticInvalidReferenceContainerType"), //$NON-NLS-1$
                        node.name.identifier,
                        type);
                return;
            }
        }
        if (node.expression == null) {
            if (explicitType == null) {
                // may not occur (unsupported syntax)
                context.error(
                        node.name,
                        Messages.getString("PropertyReferenceDeclarationProcessor.diagnosticMissingReferenceType"), //$NON-NLS-1$
                        node.name.identifier);
                return;
            }
            model.declarePropertyReference(
                    node,
                    node.name,
                    explicitType.getElementType(),
                    stubFor(explicitType.getKind()),
                    Optional.ofNullable(node.description)
                        .orElseGet(() -> findInheritedDescription(modelRefs, node.name.identifier)),
                    node.attributes);
        } else {
            ExpressionResolver resolver = new ExpressionResolver(context, model, node, modelRefs);
            node.expression.accept(explicitType, resolver);
        }
    }

    private static CollectionType findInheritedType(Set<ModelSymbol> modelRefs, String name) {
        List<CollectionType> candidates = modelRefs.stream()
                .map(ModelSymbol::findDeclaration)
                .filter(it -> it != null)
                .map(it -> it.findPropertyReferenceDeclaration(name))
                .filter(it -> it != null)
                .map(PropertyReferenceDeclarationProcessor::getType)
                .collect(Collectors.toList());
        CollectionType result = null;
        for (CollectionType candidate : candidates) {
            if (result == null) {
                result = candidate;
            } else if (result.isSame(candidate) == false) {
                return null;
            }
        }
        return result;
    }

    static AstDescription findInheritedDescription(Set<ModelSymbol> modelRefs, String name) {
        List<PropertyReferenceDeclaration> inherited = modelRefs.stream()
            .map(ModelSymbol::findDeclaration)
            .filter(it -> it != null)
            .map(it -> it.findPropertyReferenceDeclaration(name))
            .filter(it -> it != null)
            .collect(Collectors.toList());
        if (inherited.isEmpty()
                || inherited.stream().anyMatch(it -> it.getDescription() == null)
                || inherited.stream()
                    .map(it -> it.getDescription().getText())
                    .distinct()
                    .count() != 1) {
            return null;
        }
        return inherited.get(0).getDescription();
    }

    private static CollectionType getType(PropertyReferenceDeclaration decl) {
        Type elementType = decl.getType();
        switch (decl.getReference().getKind()) {
        case LIST:
            return new CollectionType(null, CollectionType.CollectionKind.LIST, elementType);
        case MAP:
            return new CollectionType(null, CollectionType.CollectionKind.MAP, elementType);
        default:
            throw new AssertionError(decl.getReference().getKind());
        }
    }

    private static PropertyReferenceDeclaration.ReferenceContainer<?> stubFor(CollectionType.CollectionKind kind) {
        switch (kind) {
        case LIST:
            return PropertyReferenceDeclaration.stub(PropertyReferenceDeclaration.ReferenceKind.LIST);
        case MAP:
            return PropertyReferenceDeclaration.stub(PropertyReferenceDeclaration.ReferenceKind.MAP);
        default:
            throw new AssertionError(kind);
        }
    }

    private static class ExpressionResolver extends AbstractVisitor<CollectionType, Void> {

        private final Context context;

        private final ModelDeclaration model;

        private final AstPropertyDefinition property;

        private final Set<ModelSymbol> modelRefs;

        ExpressionResolver(
                Context context,
                ModelDeclaration model, AstPropertyDefinition property, Set<ModelSymbol> modelRefs) {
            this.context = context;
            this.model = model;
            this.property = property;
            this.modelRefs = modelRefs;
        }

        @Override
        public Void visitQualifiedName(CollectionType explicitType, AstQualifiedName name) {
            assert name.getQualifier() instanceof AstSimpleName;
            AstSimpleName ownerRef = (AstSimpleName) name.getQualifier();
            ModelDeclaration owner = context.getWorld().findModelDeclaration(ownerRef.identifier);
            if (owner == null || modelRefs.contains(owner.getSymbol()) == false) {
                context.error(
                        name,
                        Messages.getString("PropertyReferenceDeclarationProcessor.diagnosticIrrelevantReferenceSelectorTarget"), //$NON-NLS-1$
                        name,
                        modelRefs);
                return null;
            }
            PropertyReferenceDeclaration ref = owner.findPropertyReferenceDeclaration(name.simpleName.identifier);
            if (ref == null) {
                if (owner.findMemberDeclaration(name.simpleName.identifier) == null) {
                    context.error(
                            name,
                            Messages.getString("PropertyReferenceDeclarationProcessor.diagnosticMissingReferenceSelectorTarget"), //$NON-NLS-1$
                            ownerRef.identifier,
                            name.simpleName.identifier);
                } else {
                    context.error(
                            name,
                            Messages.getString("PropertyReferenceDeclarationProcessor.diagnosticInvalidReferenceSelectorTarget"), //$NON-NLS-1$
                            ownerRef.identifier,
                            name.simpleName.identifier);
                }
                return null;
            }
            if (checkType(explicitType, ref.getReference().getKind(), ref.getType()) == false) {
                return null;
            }
            if (ref.getReference().isStub()) {
                context.error(
                        name,
                        Messages.getString("PropertyReferenceDeclarationProcessor.diagnosticStubReferenceSelectorTarget"), //$NON-NLS-1$
                        name);
                return null;
            }
            PropertyReferenceDeclaration.ReferenceContainer<?> targets = ref.getReference();
            Map<PropertySymbol, PropertySymbol> mapping = new LinkedHashMap<>();
            for (PropertySymbol source : targets.getAllReferences()) {
                PropertyDeclaration destination = model.findPropertyDeclaration(source.getName().identifier);
                assert destination != null;
                mapping.put(source, destination.getSymbol());
            }
            model.declarePropertyReference(
                    null,
                    property.name,
                    ref.getType(),
                    ref.getReference().remap(mapping::get),
                    property.description != null ? property.description : ref.getDescription(),
                    ref.getAttributes());
            return null;
        }

        @Override
        public Void visitAttributeValueArray(CollectionType explicitType, AstAttributeValueArray elements) {
            if (elements.elements.isEmpty() && explicitType == null) {
                requireExplicitType();
                return null;
            }
            Type refType = explicitType == null ? null : explicitType.getElementType();
            List<PropertySymbol> references = new ArrayList<>();
            for (AstAttributeValue ref : elements.elements) {
                PropertyDeclaration decl = findProperty(refType, ref);
                if (decl == null) {
                    return null;
                }
                refType = decl.getType();
                references.add(decl.getSymbol());
            }
            assert refType != null;
            PropertyReferenceDeclaration.ReferenceContainer<?> container =
                    PropertyReferenceDeclaration.container(references);
            if (checkType(explicitType, container.getKind(), refType) == false) {
                return null;
            }
            declarePropertyReference(refType, container);
            return null;
        }

        private void requireExplicitType() {
            context.error(
                    property.name,
                    Messages.getString("PropertyReferenceDeclarationProcessor.diagnosticInconsistentReferenceSelectorTarget"), //$NON-NLS-1$
                    property.name.identifier);
        }

        @Override
        public Void visitAttributeValueMap(CollectionType explicitType, AstAttributeValueMap elements) {
            if (elements.entries.isEmpty() && explicitType == null) {
                requireExplicitType();
                return null;
            }
            Type refType = explicitType == null ? null : explicitType.getElementType();
            Map<String, PropertySymbol> references = new LinkedHashMap<>();
            for (AstAttributeValueMap.Entry entry : elements.entries) {
                PropertyDeclaration decl = findProperty(refType, entry.value);
                if (decl == null) {
                    return null;
                }
                refType = decl.getType();
                references.put(entry.key.toStringValue(), decl.getSymbol());
            }
            assert refType != null;
            PropertyReferenceDeclaration.ReferenceContainer<?> container =
                    PropertyReferenceDeclaration.container(references);
            if (checkType(explicitType, container.getKind(), refType) == false) {
                return null;
            }
            declarePropertyReference(refType, container);
            return null;
        }

        private PropertyDeclaration findProperty(Type expectedType, AstAttributeValue reference) {
            assert reference instanceof AstSimpleName;
            AstSimpleName name = (AstSimpleName) reference;
            PropertyDeclaration decl = model.findPropertyDeclaration(name.identifier);
            if (decl == null) {
                context.error(
                        reference,
                        Messages.getString("PropertyReferenceDeclarationProcessor.diagnosticMissingReferenceElement"), //$NON-NLS-1$
                        model.getName().identifier,
                        property.name.identifier);
                return null;
            }
            if (expectedType != null && expectedType.isSame(decl.getType()) == false) {
                context.error(
                        reference,
                        Messages.getString("PropertyReferenceDeclarationProcessor.diagnosticUnexpectedReferenceElementType"), //$NON-NLS-1$
                        decl.getName().identifier,
                        expectedType,
                        decl.getType());
                return null;
            }
            return decl;
        }

        private boolean checkType(
                CollectionType type,
                PropertyReferenceDeclaration.ReferenceKind kind, Type element) {
            if (type == null) {
                return true; // don't care
            }
            boolean match = true;
            match &= type.getElementType().isSame(element);
            switch (type.getKind()) {
            case LIST:
                match &= kind == PropertyReferenceDeclaration.ReferenceKind.LIST;
                break;
            case MAP:
                match &= kind == PropertyReferenceDeclaration.ReferenceKind.MAP;
                break;
            default:
                throw new AssertionError(type);
            }
            if (match == false) {
                context.error(
                        property.name,
                        Messages.getString("PropertyReferenceDeclarationProcessor.diagnosticInconsistentReferenceType"), //$NON-NLS-1$
                        property.name.identifier,
                        type);
                return false;
            }
            return true;
        }

        private PropertyReferenceDeclaration declarePropertyReference(
                Type elementType,
                PropertyReferenceDeclaration.ReferenceContainer<?> reference) {
            return model.declarePropertyReference(
                    property,
                    property.name,
                    elementType,
                    reference,
                    Optional.ofNullable(property.description)
                        .orElseGet(() -> findInheritedDescription(modelRefs, property.name.identifier)),
                    property.attributes);
        }
    }
}