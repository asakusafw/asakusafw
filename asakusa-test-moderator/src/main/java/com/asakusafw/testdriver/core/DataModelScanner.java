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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Scans properties in {@link DataModelDefinition}.
 * @param <C> type of context object
 * @param <E> type of exception object
 * @since 0.2.0
 */
public abstract class DataModelScanner<C, E extends Throwable> {

    private static final Map<Class<?>, Callback> CALLBACKS;
    static {
        Map<Class<?>, Callback> map = new HashMap<Class<?>, Callback>();
        map.put(Boolean.class, new Callback() {
            @Override
            <C, E extends Throwable> void apply(DataModelScanner<C, E> s, PropertyName n, C c) throws E {
                s.booleanProperty(n, c);
            }
        });
        map.put(Byte.class, new Callback() {
            @Override
            <C, E extends Throwable> void apply(DataModelScanner<C, E> s, PropertyName n, C c) throws E {
                s.byteProperty(n, c);
            }
        });
        map.put(Short.class, new Callback() {
            @Override
            <C, E extends Throwable> void apply(DataModelScanner<C, E> s, PropertyName n, C c) throws E {
                s.shortProperty(n, c);
            }
        });
        map.put(Integer.class, new Callback() {
            @Override
            <C, E extends Throwable> void apply(DataModelScanner<C, E> s, PropertyName n, C c) throws E {
                s.intProperty(n, c);
            }
        });
        map.put(Long.class, new Callback() {
            @Override
            <C, E extends Throwable> void apply(DataModelScanner<C, E> s, PropertyName n, C c) throws E {
                s.longProperty(n, c);
            }
        });
        map.put(Float.class, new Callback() {
            @Override
            <C, E extends Throwable> void apply(DataModelScanner<C, E> s, PropertyName n, C c) throws E {
                s.floatProperty(n, c);
            }
        });
        map.put(Double.class, new Callback() {
            @Override
            <C, E extends Throwable> void apply(DataModelScanner<C, E> s, PropertyName n, C c) throws E {
                s.doubleProperty(n, c);
            }
        });
        map.put(BigInteger.class, new Callback() {
            @Override
            <C, E extends Throwable> void apply(DataModelScanner<C, E> s, PropertyName n, C c) throws E {
                s.integerProperty(n, c);
            }
        });
        map.put(BigDecimal.class, new Callback() {
            @Override
            <C, E extends Throwable> void apply(DataModelScanner<C, E> s, PropertyName n, C c) throws E {
                s.decimalProperty(n, c);
            }
        });
        map.put(String.class, new Callback() {
            @Override
            <C, E extends Throwable> void apply(DataModelScanner<C, E> s, PropertyName n, C c) throws E {
                s.stringProperty(n, c);
            }
        });
        map.put(Calendar.class, new Callback() {
            @Override
            <C, E extends Throwable> void apply(DataModelScanner<C, E> s, PropertyName n, C c) throws E {
                s.calendarProperty(n, c);
            }
        });
        CALLBACKS = map;
    }

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
        Class<?> type = definition.getType(name);
        Callback callback = CALLBACKS.get(type);
        if (callback == null) {
            anyProperty(name, context);
        } else {
            callback.apply(this, name, context);
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
     * Invoked each {@link Calendar} property.
     * @param name property name
     * @param context context object (specified in {@link #scan(DataModelDefinition, Object)})
     * @throws E if failed
     */
    public void calendarProperty(PropertyName name, C context) throws E {
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

    /**
     * Dummy throwable class without checked.
     * @since 0.2.0
     */
    public static final class _ extends Error {

        private static final long serialVersionUID = 1L;

        private _() {
            return;
        }
    }

    private abstract static class Callback {

        Callback() {
            return;
        }

        abstract <C, E extends Throwable> void apply(
                DataModelScanner<C, E> scanner, PropertyName name, C context) throws E;
    }
}
