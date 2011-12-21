/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.testdriver.core;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Scans properties in {@link DataModelDefinition}.
 * @param <C> type of context object
 * @param <E> type of exception object
 * @since 0.2.0
 */
public abstract class DataModelScanner<C, E extends Throwable> {

    /**
     * Starts scan and visits each property method.
     * @param definition the model definition
     * @param context context objects (nullable)
     * @throws E if failed
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void scan(DataModelDefinition<?> definition, C context) throws E {
        if (definition == null) {
            throw new IllegalArgumentException("definition must not be null"); //$NON-NLS-1$
        }
        for (PropertyName name : definition.getProperties()) {
            scan(definition, name, context);
        }
    }

    /**
     * Scans about single property.
     * @param definition the model definition
     * @param name target property name
     * @param context context object (nullable)
     * @throws E if failed
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    public void scan(DataModelDefinition<?> definition, PropertyName name, C context) throws E {
        if (definition == null) {
            throw new IllegalArgumentException("definition must not be null"); //$NON-NLS-1$
        }
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        PropertyType type = definition.getType(name);
        if (type == null) {
            anyProperty(name, context);
        } else {
            switch (type) {
            case BOOLEAN:
                booleanProperty(name, context);
                break;
            case BYTE:
                byteProperty(name, context);
                break;
            case DATE:
                dateProperty(name, context);
                break;
            case DATETIME:
                datetimeProperty(name, context);
                break;
            case DECIMAL:
                decimalProperty(name, context);
                break;
            case DOUBLE:
                doubleProperty(name, context);
                break;
            case FLOAT:
                floatProperty(name, context);
                break;
            case INT:
                intProperty(name, context);
                break;
            case INTEGER:
                integerProperty(name, context);
                break;
            case LONG:
                longProperty(name, context);
                break;
            case OBJECT:
                objectProperty(name, context);
                break;
            case SEQUENCE:
                sequenceProperty(name, context);
                break;
            case SHORT:
                shortProperty(name, context);
                break;
            case STRING:
                stringProperty(name, context);
                break;
            case TIME:
                timeProperty(name, context);
                break;
            default:
                anyProperty(name, context);
                break;
            }
        }
    }

    /**
     * Invoked each {@link Boolean} property.
     * @param name property name
     * @param context context object (specified in {@link #scan(DataModelDefinition, Object)})
     * @throws E if failed
     */
    public void booleanProperty(PropertyName name, C context) throws E {
        anyProperty(name, context);
    }

    /**
     * Invoked each {@link Byte} property.
     * @param name property name
     * @param context context object (specified in {@link #scan(DataModelDefinition, Object)})
     * @throws E if failed
     */
    public void byteProperty(PropertyName name, C context) throws E {
        anyProperty(name, context);
    }

    /**
     * Invoked each {@link Short} property.
     * @param name property name
     * @param context context object (specified in {@link #scan(DataModelDefinition, Object)})
     * @throws E if failed
     */
    public void shortProperty(PropertyName name, C context) throws E {
        anyProperty(name, context);
    }

    /**
     * Invoked each {@link Integer} property.
     * @param name property name
     * @param context context object (specified in {@link #scan(DataModelDefinition, Object)})
     * @throws E if failed
     */
    public void intProperty(PropertyName name, C context) throws E {
        anyProperty(name, context);
    }

    /**
     * Invoked each {@link Long} property.
     * @param name property name
     * @param context context object (specified in {@link #scan(DataModelDefinition, Object)})
     * @throws E if failed
     */
    public void longProperty(PropertyName name, C context) throws E {
        anyProperty(name, context);
    }

    /**
     * Invoked each {@link BigInteger} property.
     * @param name property name
     * @param context context object (specified in {@link #scan(DataModelDefinition, Object)})
     * @throws E if failed
     */
    public void integerProperty(PropertyName name, C context) throws E {
        anyProperty(name, context);
    }

    /**
     * Invoked each {@link Float} property.
     * @param name property name
     * @param context context object (specified in {@link #scan(DataModelDefinition, Object)})
     * @throws E if failed
     */
    public void floatProperty(PropertyName name, C context) throws E {
        anyProperty(name, context);
    }

    /**
     * Invoked each {@link Double} property.
     * @param name property name
     * @param context context object (specified in {@link #scan(DataModelDefinition, Object)})
     * @throws E if failed
     */
    public void doubleProperty(PropertyName name, C context) throws E {
        anyProperty(name, context);
    }

    /**
     * Invoked each {@link BigDecimal} property.
     * @param name property name
     * @param context context object (specified in {@link #scan(DataModelDefinition, Object)})
     * @throws E if failed
     */
    public void decimalProperty(PropertyName name, C context) throws E {
        anyProperty(name, context);
    }

    /**
     * Invoked each {@link String} property.
     * @param name property name
     * @param context context object (specified in {@link #scan(DataModelDefinition, Object)})
     * @throws E if failed
     */
    public void stringProperty(PropertyName name, C context) throws E {
        anyProperty(name, context);
    }

    /**
     * Invoked each {@link PropertyType#DATE} property.
     * @param name property name
     * @param context context object (specified in {@link #scan(DataModelDefinition, Object)})
     * @throws E if failed
     */
    public void dateProperty(PropertyName name, C context) throws E {
        anyProperty(name, context);
    }

    /**
     * Invoked each {@link PropertyType#TIME} property.
     * @param name property name
     * @param context context object (specified in {@link #scan(DataModelDefinition, Object)})
     * @throws E if failed
     */
    public void timeProperty(PropertyName name, C context) throws E {
        anyProperty(name, context);
    }

    /**
     * Invoked each {@link PropertyType#DATETIME} property.
     * @param name property name
     * @param context context object (specified in {@link #scan(DataModelDefinition, Object)})
     * @throws E if failed
     */
    public void datetimeProperty(PropertyName name, C context) throws E {
        anyProperty(name, context);
    }

    /**
     * Invoked each {@link Sequence} property.
     * @param name property name
     * @param context context object (specified in {@link #scan(DataModelDefinition, Object)})
     * @throws E if failed
     */
    public void sequenceProperty(PropertyName name, C context) throws E {
        anyProperty(name, context);
    }

    /**
     * Invoked each {@link DataModelReflection} property.
     * @param name property name
     * @param context context object (specified in {@link #scan(DataModelDefinition, Object)})
     * @throws E if failed
     */
    public void objectProperty(PropertyName name, C context) throws E {
        anyProperty(name, context);
    }

    /**
     * Invoked each property.
     * @param name property name
     * @param context context object (specified in {@link #scan(DataModelDefinition, Object)})
     * @throws E if failed
     */
    public void anyProperty(PropertyName name, C context) throws E {
        return;
    }
}
