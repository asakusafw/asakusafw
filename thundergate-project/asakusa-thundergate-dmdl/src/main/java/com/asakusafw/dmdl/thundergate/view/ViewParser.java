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
package com.asakusafw.dmdl.thundergate.view;

import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;

import com.asakusafw.dmdl.thundergate.view.model.CreateView;

/**
 * Parses VIEW structure and builds ASTs.
 */
public final class ViewParser {

    /**
     * Parses VIEW structure and returns its abstract syntax tree.
     * @param definition target view definition
     * @return parsing results
     * @throws IOException if failed to parse the view
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public static CreateView parse(ViewDefinition definition) throws IOException {
        if (definition == null) {
            throw new IllegalArgumentException("reader must not be null"); //$NON-NLS-1$
        }
        StringReader stream = new StringReader(definition.statement);
        JjViewParser parser = new JjViewParser(stream);
        try {
            CreateView parsed = parser.parse(definition.name);
            return parsed;
        } catch (ParseException e) {
            throw new IOException(MessageFormat.format(
                    "ビュー{0}の解析に失敗しました ({1})",
                    definition.name,
                    definition.statement),
                    e);
        }
    }

    private ViewParser() {
        return;
    }
}
