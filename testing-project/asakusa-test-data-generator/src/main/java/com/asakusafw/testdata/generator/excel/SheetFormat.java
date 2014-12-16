/**
 * Copyright 2011-2014 Asakusa Framework Team.
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
package com.asakusafw.testdata.generator.excel;

/**
 * Generating format of each Excel sheet.
 * @since 0.2.0
 */
public final class SheetFormat {

    private final Kind kind;

    private final String name;

    private SheetFormat(Kind kind, String name) {
        assert kind != null;
        assert name != null;
        this.kind = kind;
        this.name = name;
    }

    /**
     * Returns data sheet format representation.
     * @param name the sheet name
     * @return the created representation
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static SheetFormat data(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        return new SheetFormat(Kind.DATA, name);
    }

    /**
     * Returns rule sheet format representation.
     * @param name the sheet name
     * @return the created representation
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static SheetFormat rule(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        return new SheetFormat(Kind.RULE, name);
    }

    /**
     * Returns the kind of this format.
     * @return the kind
     */
    public SheetFormat.Kind getKind() {
        return kind;
    }

    /**
     * Returns the name of this sheet.
     * @return the name
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SheetFormat [kind="); //$NON-NLS-1$
        builder.append(kind);
        builder.append(", name="); //$NON-NLS-1$
        builder.append(name);
        builder.append("]"); //$NON-NLS-1$
        return builder.toString();
    }

    /**
     * Kind of sheet style.
     * @since 0.2.0
     */
    public enum Kind {

        /**
         * Data sheets.
         */
        DATA,

        /**
         * Rule sheets.
         */
        RULE,
    }
}
