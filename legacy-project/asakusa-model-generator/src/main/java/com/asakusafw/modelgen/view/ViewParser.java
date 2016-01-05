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
package com.asakusafw.modelgen.view;

import java.io.IOException;
import java.io.StringReader;
import java.text.MessageFormat;

import com.asakusafw.modelgen.view.model.CreateView;


/**
 * ビューの定義を解析するパーサ。
 */
public final class ViewParser {

    /**
     * 指定のビュー定義の内容を解析し、ビューとして返す。
     * @param definition 対象のビュー定義
     * @return 解析結果
     * @throws IOException 解析に失敗した場合
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
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
        } catch (TokenMgrError e) {
            throw new IOException(MessageFormat.format(
                    "ビュー{0}の解析に失敗しました ({1})",
                    definition.name,
                    definition.statement),
                    e);
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
