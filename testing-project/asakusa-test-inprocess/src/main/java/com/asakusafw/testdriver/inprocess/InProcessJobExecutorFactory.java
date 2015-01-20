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
package com.asakusafw.testdriver.inprocess;

import com.asakusafw.testdriver.JobExecutor;
import com.asakusafw.testdriver.JobExecutorFactory;
import com.asakusafw.testdriver.TestDriverContext;

/**
 * An implementation of {@link JobExecutorFactory} which provides {@link InProcessJobExecutor}.
 * @since 0.6.0
 */
public class InProcessJobExecutorFactory extends JobExecutorFactory {

    @Override
    public JobExecutor newInstance(TestDriverContext context) {
        return new InProcessJobExecutor(context);
    }
}
