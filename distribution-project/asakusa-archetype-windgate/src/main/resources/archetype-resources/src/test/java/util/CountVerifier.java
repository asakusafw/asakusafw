/**
 * Copyright 2011-2013 Asakusa Framework Team.
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
package ${package}.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.asakusafw.testdriver.core.DataModelDefinition;
import com.asakusafw.testdriver.core.DataModelReflection;
import com.asakusafw.testdriver.core.DataModelSource;
import com.asakusafw.testdriver.core.Difference;
import com.asakusafw.testdriver.core.PropertyName;
import com.asakusafw.testdriver.core.Verifier;
import com.asakusafw.testdriver.core.VerifierFactory;
import com.asakusafw.testdriver.core.VerifyContext;

/**
 * Verifies record count.
 */
public class CountVerifier implements Verifier {

    private final long expected;

    /**
     * Creates a new instance.
     * @param expected the expected record count
     */
    public CountVerifier(long expected) {
        this.expected = expected;
    }

    /**
     * Creates a new factory of this class.
     * @param expected the expected record count
     * @return the created factory
     */
    public static VerifierFactory factory(final long expected) {
        return new VerifierFactory() {
            @Override
            public <T> Verifier createVerifier(DataModelDefinition<T> definition, VerifyContext context) {
                return new CountVerifier(expected);
            }
        };
    }

    @Override
    public List<Difference> verify(DataModelSource results) throws IOException {
        long actual = 0;
        while (results.next() != null) {
            actual++;
        }
        List<Difference> result = new ArrayList<Difference>();
        if (expected != actual) {
            result.add(createDifference(actual));
        }
        return result;
    }

    private Difference createDifference(long actual) {
        PropertyName name = PropertyName.newInstance("count");
        return new Difference(
                new DataModelReflection(Collections.singletonMap(name, expected)),
                new DataModelReflection(Collections.singletonMap(name, actual)),
                "count verification was failed");
    }

    @Override
    public void close() throws IOException {
        return;
    }
}
