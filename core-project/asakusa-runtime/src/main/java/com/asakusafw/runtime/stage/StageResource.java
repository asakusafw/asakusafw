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
package com.asakusafw.runtime.stage;

/**
 * ステージで利用するリソース。
 */
public class StageResource {

    private String location;

    private String name;

    /**
     * インスタンスを生成する。
     * @param location リソースのDFS上での位置
     * @param name ステージで利用する際の名前
     * @throws IllegalArgumentException 引数に{@code null}が指定された場合
     */
    public StageResource(String location, String name) {
        if (location == null) {
            throw new IllegalArgumentException("location must not be null"); //$NON-NLS-1$
        }
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        this.location = location;
        this.name = name;
    }

    /**
     * リソースのDFS上での位置を返す。
     * @return リソースのDFS上での位置
     */
    public String getLocation() {
        return location;
    }

    /**
     * ステージで利用する際の名前を返す。
     * @return ステージで利用する際の名前
     */
    public String getName() {
        return name;
    }
}
