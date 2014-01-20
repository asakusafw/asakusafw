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
package com.asakusafw.vocabulary.batch;


/**
 * 連結リスト。
 */
public class ListBatch extends BatchDescription {

    @Override
    protected void describe() {
        Work jf1 = run(JobFlow1.class).soon();
        Work jf2 = run(JobFlow2.class).after(jf1);
        run(JobFlow3.class).after(jf2);
    }
}
