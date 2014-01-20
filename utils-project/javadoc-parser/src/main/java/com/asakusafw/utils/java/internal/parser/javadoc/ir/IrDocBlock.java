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
package com.asakusafw.utils.java.internal.parser.javadoc.ir;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ブロック要素。
 */
public class IrDocBlock extends AbstractIrDocElement implements IrDocFragment {

    private static final long serialVersionUID = 1L;

    private String tag;
    private List<? extends IrDocFragment> fragments;

    /**
     * Creates a new instance.
     */
    public IrDocBlock() {
        super();
        this.tag = null;
        this.fragments = Collections.emptyList();
    }

    @Override
    public IrDocElementKind getKind() {
        return IrDocElementKind.BLOCK;
    }

    /**
     * このブロックのタグを返す。
     * このタグが概要ブロックを表現する場合、タグは存在せずこの呼び出しは{@code null}を返す。
     * そうでない場合、返される文字列の先頭は必ず<code>&#64;</code>であることが保証されている。
     * @return <code>&#64;</code>から始まるタグ名、タグが存在しない場合は{@code null}
     */
    public String getTag() {
        return this.tag;
    }

    /**
     * このブロックのタグを設定する。
     * 引数に{@code null}が指定された場合、このブロックはタグが存在しない概要ブロックを表現する。
     * そうでなく、先頭文字が<code>&#64;</code>でない場合、自動的に付与される。
     * @param tag 設定するタグ文字列、または{@code null}
     */
    public void setTag(String tag) {
        if (tag == null) {
            this.tag = null;
        } else {
            if (tag.length() == 0 || tag.charAt(0) != '@') {
                this.tag = ('@' + tag).intern();
            } else {
                this.tag = tag;
            }
        }
    }

    /**
     * このブロックのタグ以降の要素を返す。
     * @return このブロックのタグ以外の要素
     */
    public List<? extends IrDocFragment> getFragments() {
        return this.fragments;
    }

    /**
     * このブロックのタグ以降の要素を設定する。
     * @param fragments 設定する要素
     * @throws IllegalArgumentException 引数に{@code null}が含まれていた場合
     */
    public void setFragments(List<? extends IrDocFragment> fragments) {
        if (fragments == null) {
            throw new IllegalArgumentException("fragments"); //$NON-NLS-1$
        }
        this.fragments = Collections.unmodifiableList(
            new ArrayList<IrDocFragment>(fragments));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + fragments.hashCode();
        result = prime * result + ((tag == null) ? 0 : tag.hashCode());
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
        final IrDocBlock other = (IrDocBlock) obj;
        if (!fragments.equals(other.fragments)) {
            return false;
        }
        if (tag == null) {
            if (other.tag != null) {
                return false;
            }
        } else if (!tag.equals(other.tag)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        if (getTag() != null) {
            buf.append(getTag());
            buf.append(" "); //$NON-NLS-1$
        }
        for (IrDocFragment f: getFragments()) {
            buf.append(f);
            buf.append(" "); //$NON-NLS-1$
        }
        return buf.toString();
    }

    @Override
    public <R, P> R accept(IrDocElementVisitor<R, P> visitor, P context) {
        if (visitor == null) {
            throw new IllegalArgumentException("visitor"); //$NON-NLS-1$
        }
        return visitor.visitBlock(this, context);
    }
}
