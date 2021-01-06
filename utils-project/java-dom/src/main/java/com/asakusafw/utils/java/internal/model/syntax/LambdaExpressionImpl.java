/**
 * Copyright 2011-2021 Asakusa Framework Team.
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
package com.asakusafw.utils.java.internal.model.syntax;

import java.util.List;

import com.asakusafw.utils.java.model.syntax.LambdaBody;
import com.asakusafw.utils.java.model.syntax.LambdaExpression;
import com.asakusafw.utils.java.model.syntax.LambdaParameter;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Visitor;

/**
 * An implementation of {@link LambdaExpression}.
 * @since 0.9.0
 */
public class LambdaExpressionImpl extends ModelRoot implements LambdaExpression {

    private List<? extends LambdaParameter> parameters;

    private LambdaBody body;

    @Override
    public ModelKind getModelKind() {
        return ModelKind.LAMBDA_EXPRESSION;
    }

    @Override
    public List<? extends LambdaParameter> getParameters() {
        return parameters;
    }

    /**
     * Sets a parameters.
     * @param parameters the parameters
     */
    public void setParameters(List<? extends LambdaParameter> parameters) {
        Util.notNull(parameters, "parameters"); //$NON-NLS-1$
        this.parameters = Util.freeze(parameters);
    }

    @Override
    public LambdaBody getBody() {
        return body;
    }

    /**
     * Sets a body.
     * @param body the body
     */
    public void setBody(LambdaBody body) {
        Util.notNull(body, "body"); //$NON-NLS-1$
        this.body = body;
    }

    @Override
    public <R, C, E extends Throwable> R accept(Visitor<R, C, E> visitor, C context) throws E {
        Util.notNull(visitor, "visitor"); //$NON-NLS-1$
        return visitor.visitLambdaExpression(this, context);
    }
}
