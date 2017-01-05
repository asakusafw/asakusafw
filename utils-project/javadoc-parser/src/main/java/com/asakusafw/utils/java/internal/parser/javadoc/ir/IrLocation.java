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
package com.asakusafw.utils.java.internal.parser.javadoc.ir;

import java.io.Serializable;
import java.text.MessageFormat;

/**
 * Represents locations on {@link IrDocComment}.
 */
public class IrLocation implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int startPosition;

    private final int length;

    /**
     * Creates a new instance.
     * @param startPosition the starting position (0-origin)
     * @param length the character length
     * @throws IllegalArgumentException if some parameters are negative value
     */
    public IrLocation(int startPosition, int length) {
        if (startPosition < 0 || length < 0) {
            throw new IllegalArgumentException();
        }
        this.startPosition = startPosition;
        this.length = length;
    }

    /**
     * Returns the starting position.
     * @return the starting position
     */
    public int getStartPosition() {
        return this.startPosition;
    }

    /**
     * Returns the character length.
     * @return the character length
     */
    public int getLength() {
        return this.length;
    }

    /**
     * Returns a new location object which is moved the base location as the specified offset.
     * @param base the base location (nullable)
     * @param offset the moving offset
     * @return the moved location, or {@code null} if the base location is also {@code null}
     * @throws IllegalArgumentException if the moved location starts with negative position
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
        IrLocation other = (IrLocation) obj;
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
