/**
 * Copyright 2011-2015 Asakusa Framework Team.
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
package com.asakusafw.utils.java.parser.javadoc;

import com.asakusafw.utils.java.internal.parser.javadoc.ir.IrLocation;

/**
 * ドキュメンテーションコメントの形式が不正であることを表す例外。
 */
public class IllegalDocCommentFormatException extends JavadocParseException {

    private static final long serialVersionUID = 1L;

    private boolean head;

    /**
     * Creates a new instance.
     * @param head {@code true}ならばコメントの先頭がない、{@code false}ならば末尾がない
     * @param location 位置
     * @param cause この原因の元となった例外
     */
    public IllegalDocCommentFormatException(boolean head, IrLocation location, Throwable cause) {
        super(buildMessage(head), location, cause);
        this.head = head;
    }

    /**
     * コメントの先頭が存在しない場合のみ{@code true}を返す。
     * @return コメントの先頭が存在しない場合のみ{@code true}
     */
    public boolean isMissingHead() {
        return head;
    }

    /**
     * コメントの末尾が存在しない場合のみ{@code true}を返す。
     * @return コメントの末尾が存在しない場合のみ{@code true}
     */
    public boolean isMissingTail() {
        return !isMissingHead();
    }

    private static String buildMessage(boolean head) {
        if (head) {
            return Messages.getString("IllegalDocCommentFormatException.messageMissingHeader"); //$NON-NLS-1$
        } else {
            return Messages.getString("IllegalDocCommentFormatException.messageMissingFooter"); //$NON-NLS-1$
        }
    }
}
