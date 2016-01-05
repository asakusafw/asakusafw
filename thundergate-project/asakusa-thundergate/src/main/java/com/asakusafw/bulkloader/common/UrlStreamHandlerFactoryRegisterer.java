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
package com.asakusafw.bulkloader.common;

import java.net.URL;
import java.net.URLStreamHandlerFactory;

import org.apache.hadoop.fs.FsUrlStreamHandlerFactory;

/**
 * {@link URLStreamHandlerFactory}を一度だけ登録する。
 */
public final class UrlStreamHandlerFactoryRegisterer {

    private static volatile boolean registered;

    /**
     * {@link URLStreamHandlerFactory}を一度だけ登録する。
     */
    public static synchronized void register() {
        if (registered) {
            return;
        }
        FsUrlStreamHandlerFactory factory = new FsUrlStreamHandlerFactory();
        try {
            URL.setURLStreamHandlerFactory(factory);
        } catch (Error e) {
            if (e.getClass().equals(Error.class)) {
                // ok.
                return;
            }
            throw e;
        }
        registered = true;
    }

    private UrlStreamHandlerFactoryRegisterer() {
        return;
    }
}
