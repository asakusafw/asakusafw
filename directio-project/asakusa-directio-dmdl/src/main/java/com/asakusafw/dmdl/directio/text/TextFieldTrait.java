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
package com.asakusafw.dmdl.directio.text;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;

import com.asakusafw.dmdl.Diagnostic;
import com.asakusafw.dmdl.model.AstAttribute;
import com.asakusafw.dmdl.model.AstNode;
import com.asakusafw.dmdl.semantics.DmdlSemantics;
import com.asakusafw.dmdl.semantics.PropertyDeclaration;
import com.asakusafw.dmdl.semantics.Trait;

/**
 * Attributes of formatted text fields.
 * @since 0.9.1
 */
public class TextFieldTrait implements Trait<TextFieldTrait> {

    private static final TextFieldSettings EMPTY_SETTINGS = new TextFieldSettings();

    private final AstAttribute attribute;

    private final Kind kind;

    private final String name;

    private final TextFieldSettings settings;

    /**
     * Creates a new instance.
     * @param attribute the original attribute
     * @param kind the field kind
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public TextFieldTrait(AstAttribute attribute, Kind kind) {
        this.attribute = attribute;
        this.kind = kind;
        this.name = null;
        this.settings = new TextFieldSettings();
    }

    /**
     * Creates a new instance.
     * @param attribute the original attribute
     * @param name the explicit field name (nullable)
     * @param settings the field settings
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public TextFieldTrait(AstAttribute attribute, String name, TextFieldSettings settings) {
        this.attribute = attribute;
        this.kind = Kind.VALUE;
        this.name = name;
        this.settings = settings;
    }

    @Override
    public AstNode getOriginalAst() {
        return attribute;
    }

    static void register(
            DmdlSemantics environment, PropertyDeclaration property, AstAttribute attribute, TextFieldTrait trait) {
        TextFieldTrait other = property.getTrait(TextFieldTrait.class);
        if (other == null) {
            property.putTrait(TextFieldTrait.class, trait);
        } else {
            environment.report(new Diagnostic(
                    Diagnostic.Level.ERROR, attribute.name,
                    Messages.getString("TextFieldTrait.diagnosticDuplicateAttribute"), //$NON-NLS-1$
                    property.getOwner().getName(), property.getName(),
                    attribute.name, other.attribute.name));
        }
    }

    /**
     * Returns the trait for the property.
     * @param property the property
     * @return the trait, or empty if it is not defined
     */
    public static Optional<TextFieldTrait> find(PropertyDeclaration property) {
        return Optional.ofNullable(property.getTrait(TextFieldTrait.class));
    }

    /**
     * Returns the trait for the property.
     * @param property the property
     * @return the trait
     */
    public static TextFieldTrait get(PropertyDeclaration property) {
        return find(property).orElseThrow(NoSuchElementException::new);
    }

    /**
     * Returns the field kind of the property.
     * If the field kind is not declared explicitly in the property, this returns the default kind.
     * @param property target property
     * @return the field kind
     */
    public static Kind getKind(PropertyDeclaration property) {
        return get(property, trait -> trait.kind).orElse(Kind.VALUE);
    }

    /**
     * Returns the field name of the property.
     * If the field name is not declared explicitly in the property, this returns the property name.
     * @param property target property
     * @return the field name
     */
    public static String getName(PropertyDeclaration property) {
        return get(property, trait -> trait.name).orElse(property.getName().identifier);
    }

    /**
     * Returns the field settings of the property.
     * If the field settings is not declared explicitly in the property, this returns the property settings.
     * @param property target property
     * @return the field settings
     */
    public static TextFieldSettings getSettings(PropertyDeclaration property) {
        return get(property, trait -> trait.settings).orElse(EMPTY_SETTINGS);
    }

    private static <T> Optional<T> get(PropertyDeclaration property, Function<TextFieldTrait, T> getter) {
        return find(property).flatMap(t -> Optional.ofNullable(getter.apply(t)));
    }

    boolean verify(DmdlSemantics environment, PropertyDeclaration declaration) {
        return settings.verify(environment, attribute);
    }

    /**
     * Returns the field kind.
     * @return the kind
     */
    public Kind getKind() {
        return kind;
    }

    /**
     * Returns the field name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the settings.
     * @return the settings
     */
    public TextFieldSettings getSettings() {
        return settings;
    }

    /**
     * The field kind.
     * @since 0.9.1
     */
    public enum Kind {

        /**
         * normal fields.
         */
        VALUE,

        /**
         * fields which keep file name.
         */
        FILE_NAME,

        /**
         * fields which keep line number.
         */
        LINE_NUMBER,

        /**
         * fields which keep record number.
         */
        RECORD_NUMBER,

        /**
         * ignored fields.
         */
        IGNORE,
    }
}
