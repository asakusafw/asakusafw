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
package com.asakusafw.dmdl.parser;

import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstAttributeElement;
import com.asakusafw.dmdl.model.AstAttributeValue;
import com.asakusafw.dmdl.model.AstAttributeValueArray;
import com.asakusafw.dmdl.model.AstAttributeValueMap;
import com.asakusafw.dmdl.model.AstBasicType;
import com.asakusafw.dmdl.model.AstCollectionType;
import com.asakusafw.dmdl.model.AstDescription;
import com.asakusafw.dmdl.model.AstGrouping;
import com.asakusafw.dmdl.model.AstJoin;
import com.asakusafw.dmdl.model.AstLiteral;
import com.asakusafw.dmdl.model.AstModelDefinition;
import com.asakusafw.dmdl.model.AstModelFolding;
import com.asakusafw.dmdl.model.AstModelMapping;
import com.asakusafw.dmdl.model.AstModelReference;
import com.asakusafw.dmdl.model.AstName;
import com.asakusafw.dmdl.model.AstNode;
import com.asakusafw.dmdl.model.AstPropertyDefinition;
import com.asakusafw.dmdl.model.AstPropertyFolding;
import com.asakusafw.dmdl.model.AstPropertyMapping;
import com.asakusafw.dmdl.model.AstQualifiedName;
import com.asakusafw.dmdl.model.AstRecordDefinition;
import com.asakusafw.dmdl.model.AstReferenceType;
import com.asakusafw.dmdl.model.AstScript;
import com.asakusafw.dmdl.model.AstSequenceType;
import com.asakusafw.dmdl.model.AstSimpleName;
import com.asakusafw.dmdl.model.AstSummarize;
import com.asakusafw.dmdl.model.AstTerm;
import com.asakusafw.dmdl.model.AstUnionExpression;

/**
 * Emits {@link AstNode} as DMDL syntax.
 */
public final class DmdlEmitter {

    /**
     * Emits the node as DMDL.
     * @param node the node to emit
     * @param output emit target
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static void emit(AstNode node, PrintWriter output) {
        if (node == null) {
            throw new IllegalArgumentException("node must not be null"); //$NON-NLS-1$
        }
        if (output == null) {
            throw new IllegalArgumentException("output must not be null"); //$NON-NLS-1$
        }
        Context context = new Context(output);
        node.accept(context, Engine.INSTANCE);
    }

    private DmdlEmitter() {
        return;
    }

    private static class Engine implements AstNode.Visitor<Context, Void> {

        static final Engine INSTANCE = new Engine();

        @Override
        public Void visitAttribute(Context context, AstAttribute node) {
            context.print("@"); //$NON-NLS-1$
            node.name.accept(context, this);
            if (node.elements.isEmpty() == false) {
                context.println("("); //$NON-NLS-1$
                context.enter();
                for (int i = 0, n = node.elements.size(); i < n; i++) {
                    node.elements.get(i).accept(context, this);
                    if (i != n - 1) {
                        context.println(","); //$NON-NLS-1$
                    } else {
                        context.println();
                    }
                }
                context.exit();
                context.print(")"); //$NON-NLS-1$
            }
            return null;
        }

        @Override
        public Void visitAttributeElement(Context context, AstAttributeElement node) {
            node.name.accept(context, this);
            context.print(" = "); //$NON-NLS-1$
            node.value.accept(context, this);
            return null;
        }

        @Override
        public Void visitAttributeValueArray(Context context, AstAttributeValueArray node) {
            List<AstAttributeValue> elements = node.elements;
            if (elements.isEmpty()) {
                context.print("'{}'"); //$NON-NLS-1$
            } else {
                context.println("'{'"); //$NON-NLS-1$
                context.enter();
                for (int i = 0, n = elements.size(); i < n; i++) {
                    elements.get(i).accept(context, this);
                    if (i != n - 1) {
                        context.println(","); //$NON-NLS-1$
                    } else {
                        context.println();
                    }
                }
                context.exit();
                context.print("'}'"); //$NON-NLS-1$
            }
            return null;
        }

        @Override
        public Void visitAttributeValueMap(Context context, AstAttributeValueMap node) {
            List<AstAttributeValueMap.Entry> entries = node.entries;
            if (entries.isEmpty()) {
                context.print("'{:}'"); //$NON-NLS-1$
            } else {
                context.println("'{'"); //$NON-NLS-1$
                context.enter();
                for (int i = 0, n = entries.size(); i < n; i++) {
                    AstAttributeValueMap.Entry entry = entries.get(i);
                    entry.key.accept(context, this);
                    context.print(" : "); //$NON-NLS-1$
                    entry.value.accept(context, this);
                    if (i != n - 1) {
                        context.println(","); //$NON-NLS-1$
                    } else {
                        context.println();
                    }
                }
                context.exit();
                context.print("'}'"); //$NON-NLS-1$
            }
            return null;
        }

        @Override
        public Void visitBasicType(Context context, AstBasicType node) {
            context.print("{0}", node.kind.name()); //$NON-NLS-1$
            return null;
        }

        @Override
        public Void visitCollectionType(Context context, AstCollectionType node) {
            switch (node.kind) {
            case LIST:
                context.print("'{'"); //$NON-NLS-1$
                node.elementType.accept(context, this);
                context.print("'}'"); //$NON-NLS-1$
                break;
            case MAP:
                context.print("'{'"); //$NON-NLS-1$
                context.print(":"); //$NON-NLS-1$
                node.elementType.accept(context, this);
                context.print("'}'"); //$NON-NLS-1$
                break;
            default:
                throw new AssertionError(node.kind);
            }
            return null;
        }

        @Override
        public Void visitDescription(Context context, AstDescription node) {
            context.print("{0}", node.token); //$NON-NLS-1$
            return null;
        }

        @Override
        public Void visitGrouping(Context context, AstGrouping node) {
            context.print("% "); //$NON-NLS-1$
            Iterator<AstSimpleName> iter = node.properties.iterator();
            if (iter.hasNext()) {
                iter.next().accept(context, this);
                while (iter.hasNext()) {
                    context.print(", "); //$NON-NLS-1$
                    iter.next().accept(context, this);
                }
            }
            return null;
        }

        @Override
        public Void visitJoin(Context context, AstJoin node) {
            node.reference.accept(context, this);
            if (node.mapping != null) {
                context.print(" "); //$NON-NLS-1$
                node.mapping.accept(context, this);
            }
            if (node.grouping != null) {
                context.print(" "); //$NON-NLS-1$
                node.grouping.accept(context, this);
            }
            return null;
        }

        @Override
        public Void visitLiteral(Context context, AstLiteral node) {
            context.print("{0}", node.token); //$NON-NLS-1$
            return null;
        }

        @Override
        public <T extends AstTerm<T>> Void visitModelDefinition(Context context, AstModelDefinition<T> node) {
            if (node.description != null) {
                node.description.accept(context, this);
                context.println();
            }
            for (AstAttribute attribute : node.attributes) {
                attribute.accept(context, this);
                context.println();
            }
            switch (node.kind) {
            case JOINED:
                context.print("joined "); //$NON-NLS-1$
                break;
            case PROJECTIVE:
                context.print("projective "); //$NON-NLS-1$
                break;
            case SUMMARIZED:
                context.print("summarized "); //$NON-NLS-1$
                break;
            default:
                break;
            }
            node.name.accept(context, this);
            context.print(" = "); //$NON-NLS-1$
            node.expression.accept(context, this);
            context.print(";"); //$NON-NLS-1$
            return null;
        }

        @Override
        public Void visitModelFolding(Context context, AstModelFolding node) {
            context.println("{0}", "=> {"); //$NON-NLS-1$ //$NON-NLS-2$
            context.enter();
            for (AstPropertyFolding property : node.properties) {
                property.accept(context, this);
                context.println();
            }
            context.exit();
            context.print("{0}", "}"); //$NON-NLS-1$ //$NON-NLS-2$
            return null;
        }

        @Override
        public Void visitModelMapping(Context context, AstModelMapping node) {
            context.println("{0}", "-> {"); //$NON-NLS-1$ //$NON-NLS-2$
            context.enter();
            for (AstPropertyMapping property : node.properties) {
                property.accept(context, this);
                context.println();
            }
            context.exit();
            context.print("{0}", "}"); //$NON-NLS-1$ //$NON-NLS-2$
            return null;
        }

        @Override
        public Void visitModelReference(Context context, AstModelReference node) {
            node.name.accept(context, this);
            return null;
        }

        @Override
        public Void visitPropertyDefinition(Context context, AstPropertyDefinition node) {
            if (node.description != null) {
                node.description.accept(context, this);
                context.println();
            }
            for (AstAttribute attribute : node.attributes) {
                attribute.accept(context, this);
                context.println();
            }
            node.name.accept(context, this);
            if (node.type != null) {
                context.print(" : "); //$NON-NLS-1$
                node.type.accept(context, this);
            }
            if (node.expression != null) {
                context.print(" = "); //$NON-NLS-1$
                node.expression.accept(context, this);
            }
            context.print(";"); //$NON-NLS-1$
            return null;
        }

        @Override
        public Void visitPropertyFolding(Context context, AstPropertyFolding node) {
            if (node.description != null) {
                node.description.accept(context, this);
                context.println();
            }
            for (AstAttribute attribute : node.attributes) {
                attribute.accept(context, this);
                context.println();
            }
            node.aggregator.accept(context, this);
            context.print(" "); //$NON-NLS-1$
            node.source.accept(context, this);
            context.print(" -> "); //$NON-NLS-1$
            node.target.accept(context, this);
            context.print(";"); //$NON-NLS-1$
            return null;
        }

        @Override
        public Void visitPropertyMapping(Context context, AstPropertyMapping node) {
            if (node.description != null) {
                node.description.accept(context, this);
                context.println();
            }
            for (AstAttribute attribute : node.attributes) {
                attribute.accept(context, this);
                context.println();
            }
            node.source.accept(context, this);
            context.print(" -> "); //$NON-NLS-1$
            node.target.accept(context, this);
            context.print(";"); //$NON-NLS-1$
            return null;
        }

        @Override
        public Void visitRecordDefinition(Context context, AstRecordDefinition node) {
            context.println("'{'"); //$NON-NLS-1$
            context.enter();
            for (AstPropertyDefinition property : node.properties) {
                property.accept(context, this);
                context.println();
            }
            context.exit();
            context.print("'}'"); //$NON-NLS-1$
            return null;
        }

        @Override
        public Void visitReferenceType(Context context, AstReferenceType node) {
            node.name.accept(context, this);
            return null;
        }

        @Override
        public Void visitSequenceType(Context context, AstSequenceType node) {
            node.elementType.accept(context, this);
            context.print("*"); //$NON-NLS-1$
            return null;
        }

        @Override
        public Void visitScript(Context context, AstScript node) {
            Iterator<AstModelDefinition<?>> iter = node.models.iterator();
            if (iter.hasNext()) {
                iter.next().accept(context, this);
                while (iter.hasNext()) {
                    context.println();
                    context.println();
                    iter.next().accept(context, this);
                }
            }
            return null;
        }

        @Override
        public Void visitSummarize(Context context, AstSummarize node) {
            node.reference.accept(context, this);
            context.print(" "); //$NON-NLS-1$
            node.folding.accept(context, this);
            if (node.grouping != null) {
                context.print(" "); //$NON-NLS-1$
                node.grouping.accept(context, this);
            }
            return null;
        }

        @Override
        public <T extends AstTerm<T>> Void visitUnionExpression(Context context, AstUnionExpression<T> node) {
            Iterator<T> iter = node.terms.iterator();
            assert iter.hasNext();
            iter.next().accept(context, this);
            while (iter.hasNext()) {
                context.print(" + "); //$NON-NLS-1$
                iter.next().accept(context, this);
            }
            return null;
        }

        @Override
        public Void visitSimpleName(Context context, AstSimpleName node) {
            context.print("{0}", node.identifier); //$NON-NLS-1$
            return null;
        }

        @Override
        public Void visitQualifiedName(Context context, AstQualifiedName node) {
            LinkedList<AstSimpleName> names = new LinkedList<>();
            AstName current = node;
            while (current.getQualifier() != null) {
                names.addFirst(current.getSimpleName());
                current = current.getQualifier();
            }
            assert current instanceof AstSimpleName;
            current.accept(context, this);
            for (AstSimpleName segment : names) {
                context.print("."); //$NON-NLS-1$
                segment.accept(context, this);
            }
            return null;
        }
    }

    private static class Context {

        private final PrintWriter writer;

        private int indent;

        private boolean headOfLine;

        Context(PrintWriter writer) {
            assert writer != null;
            this.writer = writer;
        }

        public void print(String pattern, String... arguments) {
            put(MessageFormat.format(pattern, (Object[]) arguments));
        }

        private void put(String string) {
            if (string.isEmpty()) {
                return;
            }
            if (headOfLine) {
                for (int i = 0; i < indent; i++) {
                    writer.print("    "); //$NON-NLS-1$
                }
                headOfLine = false;
            }
            writer.print(string);
        }

        public void println(String pattern, String... arguments) {
            print(pattern, arguments);
            println();
        }

        public void println() {
            headOfLine = true;
            writer.println();
        }

        public void enter() {
            indent++;
        }

        public void exit() {
            indent--;
        }
    }
}
