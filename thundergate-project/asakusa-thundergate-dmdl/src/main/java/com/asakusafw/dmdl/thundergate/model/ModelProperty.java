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
package com.asakusafw.dmdl.thundergate.model;

import java.text.MessageFormat;

/**
 * 各モデルのプロパティを表現する。
 */
public class ModelProperty {

    private String name;

    private Source from;

    private Source joined;

    /**
     * インスタンスを生成する。
     * @param name プロパティの名称
     * @param from モデルの元となったプロパティの情報
     */
    public ModelProperty(String name, Source from) {
        this.name = name;
        this.from = from;
    }

    /**
     * インスタンスを生成する。
     * @param name プロパティの名称
     * @param from モデルの元となったプロパティの情報(主)、存在しない場合は{@code null}
     * @param joined モデルの元となったプロパティの情報(結合)、存在しない場合は{@code null}
     * @throws IllegalArgumentException 引数{@code from, joined}がいずれも指令されない場合
     */
    public ModelProperty(String name, Source from, Source joined) {
        this.name = name;
        if (from == null && joined == null) {
            throw new IllegalArgumentException(MessageFormat.format(
                    "プロパティのソースが指定されていません ({0})",
                    name));
        }
        this.from = from;
        this.joined = joined;
    }

    /**
     * このプロパティの名前を返す。
     * @return プロパティの名前
     */
    public String getName() {
        return name;
    }

    /**
     * このプロパティの型を返す。
     * @return このプロパティの型
     */
    public PropertyType getType() {
        Source source = getSource();
        return source.getAggregator().inferType(source.getType());
    }

    /**
     * このプロパティの元になったプロパティを返す。
     * <p>
     * 元になったプロパティが複数存在する場合、それらのうちいずれか一つを返す。
     * </p>
     * @return このプロパティの元になったプロパティ
     */
    public Source getSource() {
        if (from != null) {
            return from;
        }
        assert joined != null;
        return joined;
    }

    /**
     * このプロパティの元になったプロパティのうち、結合によるものではないプロパティの情報を返す。
     * <p>
     * 元になったプロパティが結合によってのみ得られる場合、この呼び出しは{@code null}を返す。
     * </p>
     * @return 元になったプロパティの情報、結合によってのみ得られる場合は{@code null}
     */
    public Source getFrom() {
        return from;
    }

    /**
     * このプロパティの元になったプロパティのうち、結合によるプロパティの情報を返す
     * <p>
     * 元になったプロパティが結合するモデルに含まれない場合、この呼び出しは{@code null}を返す。
     * </p>
     * @return 元になったプロパティの情報、結合によって得られるプロパティが存在しない場合は{@code null}
     */
    public Source getJoined() {
        return joined;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((from == null) ? 0 : from.hashCode());
        result = prime * result + ((joined == null) ? 0 : joined.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        ModelProperty other = (ModelProperty) obj;
        if (name.equals(other.name) == false) {
            return false;
        }
        if (from == null) {
            if (other.from != null) {
                return false;
            }
        } else if (from.equals(other.from) == false) {
            return false;
        }
        if (joined == null) {
            if (other.joined != null) {
                return false;
            }
        } else if (joined.equals(other.joined) == false) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ModelProperty [name=");
        builder.append(name);
        builder.append(", from=");
        builder.append(from);
        builder.append(", joined=");
        builder.append(joined);
        builder.append("]");
        return builder.toString();
    }
}
