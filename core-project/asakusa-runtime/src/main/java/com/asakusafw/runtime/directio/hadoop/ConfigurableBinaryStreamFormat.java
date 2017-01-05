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
package com.asakusafw.runtime.directio.hadoop;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;

import com.asakusafw.runtime.directio.BinaryStreamFormat;

/**
 * {@link BinaryStreamFormat} with {@code Configurable}.
 * @param <T> the type of target data model
 * @since 0.7.0
 */
public abstract class ConfigurableBinaryStreamFormat<T> extends BinaryStreamFormat<T> implements Configurable {

    private volatile Configuration conf;

    @Override
    public Configuration getConf() {
        if (conf == null) {
            this.conf = new Configuration();
        }
        return conf;
    }

    @Override
    public void setConf(Configuration conf) {
        this.conf = conf;
    }
}
