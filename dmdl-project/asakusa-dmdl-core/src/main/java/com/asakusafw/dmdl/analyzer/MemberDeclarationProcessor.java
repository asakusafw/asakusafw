/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.asakusafw.dmdl.model.AstAttributeValue;
import com.asakusafw.dmdl.model.AstAttributeValueArray;
import com.asakusafw.dmdl.model.AstAttributeValueMap;
import com.asakusafw.dmdl.model.AstJoin;
import com.asakusafw.dmdl.model.AstLiteral;
import com.asakusafw.dmdl.model.AstModelDefinition;
import com.asakusafw.dmdl.model.AstModelReference;
import com.asakusafw.dmdl.model.AstName;
import com.asakusafw.dmdl.model.AstNode.AbstractVisitor;
import com.asakusafw.dmdl.model.AstPropertyDefinition;
import com.asakusafw.dmdl.model.AstRecord;
import com.asakusafw.dmdl.model.AstRecordDefinition;
import com.asakusafw.dmdl.model.AstSimpleName;
import com.asakusafw.dmdl.model.AstSummarize;
import com.asakusafw.dmdl.model.AstTerm;
import com.asakusafw.dmdl.model.AstUnionExpression;
import com.asakusafw.dmdl.model.LiteralKind;

final class MemberDeclarationProcessor {

    private final Context context;

    private MemberDeclarationProcessor(Context context) {
        this.context = context;
    }

    static void validate(Context context, AstModelDefinition<?> definition) {
        MemberDeclarationProcessor processor = new MemberDeclarationProcessor(context);
        switch (definition.kind) {
        case PROJECTIVE:
            processor.validateRecord(definition.asProjective());
            break;
        case RECORD:
            processor.validateRecord(definition.asRecord());
            break;
        case JOINED:
        case SUMMARIZED:
            return;
        default:
            throw new AssertionError(definition.kind);
        }
    }

    private void validateRecord(AstModelDefinition<AstRecord> node) {
        List<AstPropertyDefinition> members = MemberCollector.collect(node);
        for (AstPropertyDefinition member : members) {
            switch (member.getPropertyKind()) {
            case NORMAL:
                validateNormalProperty(node, member);
                break;
            case REFERENCE:
                validatePropertyReference(node, member);
                break;
            case INVALID:
                // may not occur in normal case
                throw new IllegalStateException();
            default:
                throw new AssertionError(member.getPropertyKind());
            }
        }
    }


    private void validateNormalProperty(AstModelDefinition<AstRecord> model, AstPropertyDefinition member) {
        if (member.expression != null) {
            // NOTE: may not occur (resolved in AstPropertyDefinition.getPropertyKind())
            context.error(
                    member.expression,
                    Messages.getString("MemberDeclarationProcessor.diagnosticUnexpectedPropertyExpression"), //$NON-NLS-1$
                    model.name,
                    member.name);
        }
    }

    private void validatePropertyReference(AstModelDefinition<AstRecord> model, AstPropertyDefinition member) {
        if (member.attributes.isEmpty() == false) {
            context.error(
                    member.name,
                    Messages.getString("MemberDeclarationProcessor.diagnosticUnexpectedReferenceAttribute"), //$NON-NLS-1$
                    model.name,
                    member.name);
        }
        AstAttributeValue expression = member.expression;
        if (expression instanceof AstName) {
            validatePropertyReferenceName(model, member, (AstName) expression);
        } else if (expression instanceof AstAttributeValueArray) {
            validatePropertyReferenceList(model, member, (AstAttributeValueArray) expression);
        } else if (expression instanceof AstAttributeValueMap) {
            validatePropertyReferenceMap(model, member, (AstAttributeValueMap) expression);
        } else if (expression != null) {
            // NOTE: may not be occur (resolved in parser)
            context.error(
                    expression,
                    Messages.getString("MemberDeclarationProcessor.diagnosticInvalidReferenceExpression")); //$NON-NLS-1$
        }
    }

    private void validatePropertyReferenceName(
            AstModelDefinition<AstRecord> model, AstPropertyDefinition member,
            AstName expression) {
        if ((expression.getQualifier() instanceof AstSimpleName) == false) {
            context.error(
                    expression,
                    Messages.getString("MemberDeclarationProcessor.diagnosticInvalidReferenceSelector"), //$NON-NLS-1$
                    model.name,
                    member.name,
                    expression);
        }
    }

    private void validatePropertyReferenceList(
            AstModelDefinition<AstRecord> model, AstPropertyDefinition member,
            AstAttributeValueArray expression) {
        for (AstAttributeValue element : expression.elements) {
            validateReference(model, member, element);
        }
    }

    private void validatePropertyReferenceMap(
            AstModelDefinition<AstRecord> model, AstPropertyDefinition member,
            AstAttributeValueMap expression) {
        Set<String> sawKeys = new HashSet<>();
        for (AstAttributeValueMap.Entry entry : expression.entries) {
            String key = resolveKey(model, member, entry.key);
            validateReference(model, member, entry.value);
            if (key != null) {
                if (sawKeys.contains(key)) {
                    context.error(
                            entry.key,
                            Messages.getString("MemberDeclarationProcessor.diagnosticDuplicatedReferenceMapKey"), //$NON-NLS-1$
                            model.name,
                            member.name,
                            entry.key.token);
                } else {
                    sawKeys.add(key);
                }
            }

        }
    }

    private String resolveKey(
            AstModelDefinition<AstRecord> model, AstPropertyDefinition member,
            AstLiteral key) {
        if (key.kind != LiteralKind.STRING) {
            context.error(
                    key,
                    Messages.getString("MemberDeclarationProcessor.diagnosticInvalidReferenceMapKey"), //$NON-NLS-1$
                    model.name,
                    member.name,
                    key.token);
            return null;
        }
        return key.toStringValue();
    }

    private void validateReference(
            AstModelDefinition<AstRecord> model, AstPropertyDefinition member,
            AstAttributeValue element) {
        if ((element instanceof AstSimpleName) == false) {
            context.error(
                    element,
                    Messages.getString("MemberDeclarationProcessor.diagnosticInvalidReferenceElement"), //$NON-NLS-1$
                    model.name,
                    member.name);
        }
    }

    private static final class MemberCollector extends AbstractVisitor<Consumer<AstPropertyDefinition>, Void> {

        private static final MemberCollector INSTANCE = new MemberCollector();

        static List<AstPropertyDefinition> collect(AstModelDefinition<AstRecord> model) {
            List<AstPropertyDefinition> results = new ArrayList<>();
            model.expression.accept(results::add, INSTANCE);
            return results;
        }

        @Override
        public <T extends AstTerm<T>> Void visitUnionExpression(
                Consumer<AstPropertyDefinition> context, AstUnionExpression<T> node) {
            node.terms.forEach(it -> it.accept(context, this));
            return null;
        }

        @Override
        public Void visitRecordDefinition(Consumer<AstPropertyDefinition> context, AstRecordDefinition node) {
            node.properties.forEach(context);
            return null;
        }

        @Override
        public Void visitModelReference(Consumer<AstPropertyDefinition> context, AstModelReference node) {
            return null;
        }

        @Override
        public Void visitJoin(Consumer<AstPropertyDefinition> context, AstJoin node) {
            throw new IllegalStateException();
        }

        @Override
        public Void visitSummarize(Consumer<AstPropertyDefinition> context, AstSummarize node) {
            throw new IllegalStateException();
        }
    }
}
