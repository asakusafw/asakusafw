/**
 * Copyright 2011-2012 Asakusa Framework Team.
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
package com.asakusafw.utils.java.internal.parser.javadoc.ir;

import java.io.Serializable;
import java.text.MessageFormat;

/**
 * 位置を表すオブジェクト。
 */
public class IrLocation implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int startPosition;
    private final int length;

    /**
     * インスタンスを生成する。
     * @param startPosition 開始位置
     * @param length 長さ
     * @throws IllegalArgumentException 引数が負であった場合
     */
    public IrLocation(int startPosition, int length) {
        super();
        if (startPosition < 0 || length < 0) {
            throw new IllegalArgumentException();
        }
        this.startPosition = startPosition;
        this.length = length;
    }

    /**
     * 開始位置を返す。
     * @return 開始位置
     */
    public int getStartPosition() {
        return this.startPosition;
    }

    /**
     * 長さを返す。
     * @return 長さ
     */
    public int getLength() {
        return this.length;
    }

    /**
     * 指定の位置オブジェクトに指定のオフセットを足した新しい位置オブジェクトを返す。
     * 元となる位置オブジェクトに{@code null}が指定された場合、この呼び出しは{@code null}を返す。
     * @param base 元となる位置オブジェクト、または{@code null}
     * @param offset オフセット
     * @return 移動した位置オブジェクト、または{@code null}
     * @throws IllegalArgumentException 移動の結果、開始位置が負になる場合
     */
    public static IrLocation move(IrLocation base, int offset) {
        if (base == null) {
            return null;
        }
        int fixed = base.getStartPosition() + offset;
        return new IrLocation(fixed, base.length);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + length;
        result = prime * result + startPosition;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IrLocation other = (IrLocation) obj;
        if (length != other.length) {
            return false;
        }
        if (startPosition != other.startPosition) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        int s = getStartPosition();
        return MessageFormat.format("[{0}-{1})", s, s + getLength()); //$NON-NLS-1$
    }
}
