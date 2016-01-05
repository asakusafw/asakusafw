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
package com.asakusafw.utils.java.model.util;

import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.asakusafw.utils.java.internal.model.syntax.ModelFactoryImpl;
import com.asakusafw.utils.java.internal.model.util.LiteralAnalyzer;
import com.asakusafw.utils.java.internal.model.util.ModelEmitter;
import com.asakusafw.utils.java.internal.model.util.ReflectionTypeMapper;
import com.asakusafw.utils.java.model.syntax.ArrayInitializer;
import com.asakusafw.utils.java.model.syntax.BasicTypeKind;
import com.asakusafw.utils.java.model.syntax.ClassLiteral;
import com.asakusafw.utils.java.model.syntax.Expression;
import com.asakusafw.utils.java.model.syntax.Literal;
import com.asakusafw.utils.java.model.syntax.Model;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.syntax.QualifiedName;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.Type;

/**
 * Utilities for {@link Model}.
 */
public final class Models {

    private static final Map<Class<?>, BasicTypeKind> WRAPPER_TYPE_KINDS;
    static {
        Map<Class<?>, BasicTypeKind> map = new HashMap<>();
        map.put(Byte.class, BasicTypeKind.BYTE);
        map.put(Short.class, BasicTypeKind.SHORT);
        map.put(Integer.class, BasicTypeKind.INT);
        map.put(Long.class, BasicTypeKind.LONG);
        map.put(Float.class, BasicTypeKind.FLOAT);
        map.put(Double.class, BasicTypeKind.DOUBLE);
        map.put(Character.class, BasicTypeKind.CHAR);
        map.put(Boolean.class, BasicTypeKind.BOOLEAN);
        WRAPPER_TYPE_KINDS = map;
    }

    /**
     * Returns a basic Java DOM factory.
     * @return a Java DOM factory
     */
    public static ModelFactory getModelFactory() {
        return new ModelFactoryImpl();
    }

    /**
     * Returns a simple name list of the target name.
     * @param name the target name
     * @return the simple name list
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static List<SimpleName> toList(Name name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        ModelKind kind = name.getModelKind();
        if (kind == ModelKind.SIMPLE_NAME) {
            return Collections.singletonList((SimpleName) name);
        } else {
            LinkedList<SimpleName> result = new LinkedList<>();
            Name current = name;
            do {
                QualifiedName qname = (QualifiedName) current;
                result.addFirst(qname.getSimpleName());
                current = qname.getQualifier();
            } while (current.getModelKind() == ModelKind.QUALIFIED_NAME);

            assert current.getModelKind() == ModelKind.SIMPLE_NAME;
            result.addFirst((SimpleName) current);

            return result;
        }
    }

    /**
     * Returns a new name which is concatenated the prefix name with the suffix name string.
     * @param factory the Java DOM factory
     * @param prefix the prefix name
     * @param rest the suffix name string (may be a qualified name string)
     * @return the concatenated name
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public static Name append(ModelFactory factory, Name prefix, String rest) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (prefix == null) {
            throw new IllegalArgumentException("prefix must not be null"); //$NON-NLS-1$
        }
        if (rest == null) {
            throw new IllegalArgumentException("rest must not be null"); //$NON-NLS-1$
        }
        Name name = Models.toName(factory, rest);
        return append(factory, prefix, name);
    }

    /**
     * Concatenates each name and returns it.
     * @param factory the Java DOM factory
     * @param names the names to be concatenated
     * @return the concatenated name
     * @throws IllegalArgumentException if the names are empty, or the parameters are {@code null}
     */
    public static Name append(ModelFactory factory, Name... names) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (names == null) {
            throw new IllegalArgumentException("names must not be null"); //$NON-NLS-1$
        }
        if (names.length == 0) {
            throw new IllegalArgumentException("names must have elements"); //$NON-NLS-1$
        }
        if (names.length == 1) {
            return names[0];
        }
        Name current = names[0];
        for (int i = 1; i < names.length; i++) {
            for (SimpleName segment : toList(names[i])) {
                current = factory.newQualifiedName(current, segment);
            }
        }
        return current;
    }

    /**
     * Emits Java DOM object into the target writer.
     * If there are elements which have {@link CommentEmitTrait},
     * their comments will be also emitted into the writer.
     * @param model the target DOM object
     * @param writer the target writer
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public static void emit(Model model, PrintWriter writer) {
        if (model == null) {
            throw new IllegalArgumentException("model must not be null"); //$NON-NLS-1$
        }
        if (writer == null) {
            throw new IllegalArgumentException("writer must not be null"); //$NON-NLS-1$
        }
        ModelEmitter emitter = new ModelEmitter(writer);
        emitter.emit(model);
    }

    /**
     * Returns a Java DOM object from the Java reflective object.
     * @param factory the Java DOM factory
     * @param type the target reflective object
     * @return the corresponded Java DOM object
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public static Type toType(ModelFactory factory, java.lang.reflect.Type type) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        return new ReflectionTypeMapper().dispatch(type, factory);
    }

    /**
     * Returns a name from the name string.
     * @param factory the Java DOM factory
     * @param nameString the target name string
     * @return the corresponded name
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public static Name toName(ModelFactory factory, String nameString) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (nameString == null) {
            throw new IllegalArgumentException("nameString must not be null"); //$NON-NLS-1$
        }
        String[] segments = nameString.trim().split("\\s*\\.\\s*"); //$NON-NLS-1$
        if (segments.length == 0 || segments[0].length() == 0) {
            throw new IllegalArgumentException("nameString is empty"); //$NON-NLS-1$
        }
        Name left = factory.newSimpleName(segments[0]);
        for (int i = 1; i < segments.length; i++) {
            SimpleName right = factory.newSimpleName(segments[i]);
            left = factory.newQualifiedName(left, right);
        }
        return left;
    }

    /**
     * Returns the fully qualified name of the target enum constant.
     * @param factory the Java DOM factory
     * @param constant the target enum constant
     * @return the corresponded name
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public static Name toName(ModelFactory factory, Enum<?> constant) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (constant == null) {
            throw new IllegalArgumentException("constant must not be null"); //$NON-NLS-1$
        }
        Name typeName = toName(factory, constant.getDeclaringClass().getName());
        return factory.newQualifiedName(typeName, factory.newSimpleName(constant.name()));
    }

    /**
     * Returns a Java literal (with cast operation) of the target value.
     * @param factory the Java DOM factory
     * @param value the target value
     * @return the corresponded Java literal
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public static Expression toLiteral(ModelFactory factory, byte value) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        String token = LiteralAnalyzer.intLiteralOf(value);
        return factory.newCastExpression(factory.newBasicType(BasicTypeKind.BYTE), factory.newLiteral(token));
    }

    /**
     * Returns a Java literal (with cast operation) of the target value.
     * @param factory the Java DOM factory
     * @param value the target value
     * @return the corresponded Java literal
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static Expression toLiteral(ModelFactory factory, short value) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        String token = LiteralAnalyzer.intLiteralOf(value);
        return factory.newCastExpression(factory.newBasicType(BasicTypeKind.SHORT), factory.newLiteral(token));
    }

    /**
     * Returns a Java literal of the target value.
     * @param factory the Java DOM factory
     * @param value the target value
     * @return the corresponded Java literal
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static Literal toLiteral(ModelFactory factory, int value) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        String token = LiteralAnalyzer.intLiteralOf(value);
        return factory.newLiteral(token);
    }

    /**
     * Returns a Java literal of the target value.
     * @param factory the Java DOM factory
     * @param value the target value
     * @return the corresponded Java literal
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static Literal toLiteral(ModelFactory factory, long value) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        String token = LiteralAnalyzer.longLiteralOf(value);
        return factory.newLiteral(token);
    }

    /**
     * Returns a Java literal of the target value.
     * @param factory the Java DOM factory
     * @param value the target value
     * @return the corresponded Java literal
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static Literal toLiteral(ModelFactory factory, float value) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        String token = LiteralAnalyzer.floatLiteralOf(value);
        return factory.newLiteral(token);
    }

    /**
     * Returns a Java literal of the target value.
     * @param factory the Java DOM factory
     * @param value the target value
     * @return the corresponded Java literal
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static Literal toLiteral(ModelFactory factory, double value) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        String token = LiteralAnalyzer.doubleLiteralOf(value);
        return factory.newLiteral(token);
    }

    /**
     * Returns a Java literal of the target value.
     * @param factory the Java DOM factory
     * @param value the target value
     * @return the corresponded Java literal
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static Literal toLiteral(ModelFactory factory, boolean value) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        String token = LiteralAnalyzer.booleanLiteralOf(value);
        return factory.newLiteral(token);
    }

    /**
     * Returns a Java literal of the target value.
     * @param factory the Java DOM factory
     * @param value the target value
     * @return the corresponded Java literal
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static Literal toLiteral(ModelFactory factory, char value) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        String token = LiteralAnalyzer.charLiteralOf(value);
        return factory.newLiteral(token);
    }

    /**
     * Returns a Java literal of the target value.
     * @param factory the Java DOM factory
     * @param value the target value
     * @return the corresponded Java literal
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public static Literal toLiteral(ModelFactory factory, String value) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (value == null) {
            throw new IllegalArgumentException("value must not be null"); //$NON-NLS-1$
        }
        String token = LiteralAnalyzer.stringLiteralOf(value);
        return factory.newLiteral(token);
    }

    /**
     * Returns a Java literal of the target value.
     * The value must be one of the following value:
     * <ul>
     * <li> the primitive wrapper object </li>
     * <li> {@code java.lang.String} </li>
     * <li> {@code java.lang.reflect.Type} </li>
     * <li> {@code null} </li>
     * </ul>
     * @param factory the Java DOM factory
     * @param value the target value
     * @return the corresponded Java literal
     * @throws IllegalArgumentException if the value is something wrong, or the parameters are {@code null}
     */
    public static Expression toLiteral(ModelFactory factory, Object value) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (value == null) {
            return toNullLiteral(factory);
        }
        Class<? extends Object> valueClass = value.getClass();
        BasicTypeKind kind = WRAPPER_TYPE_KINDS.get(valueClass);
        if (kind != null) {
            switch (kind) {
            case BYTE:
                return toLiteral(factory, (byte) (Byte) value);
            case SHORT:
                return toLiteral(factory, (short) (Short) value);
            case INT:
                return toLiteral(factory, (int) (Integer) value);
            case LONG:
                return toLiteral(factory, (long) (Long) value);
            case FLOAT:
                return toLiteral(factory, (float) (Float) value);
            case DOUBLE:
                return toLiteral(factory, (double) (Double) value);
            case CHAR:
                return toLiteral(factory, (char) (Character) value);
            case BOOLEAN:
                return toLiteral(factory, (boolean) (Boolean) value);
            default:
                throw new AssertionError(kind);
            }
        } else if (valueClass == String.class) {
            return toLiteral(factory, (String) value);
        } else if (value instanceof java.lang.reflect.Type) {
            return toClassLiteral(factory, (java.lang.reflect.Type) value);
        }
        throw new IllegalArgumentException(MessageFormat.format(
                "Cannot convert {0} to literal ({1})",
                value,
                valueClass));
    }

    /**
     * Returns a type literal of the target type.
     * @param factory the Java DOM factory
     * @param type the target Java type
     * @return the corresponded Java literal
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public static ClassLiteral toClassLiteral(ModelFactory factory, java.lang.reflect.Type type) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        return factory.newClassLiteral(Models.toType(factory, type));
    }

    /**
     * Returns a {@code null} literal.
     * @param factory the Java DOM factory
     * @return the literal
     * @throws IllegalArgumentException if the parameter is {@code null}
     */
    public static Literal toNullLiteral(ModelFactory factory) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        String token = LiteralAnalyzer.nullLiteral();
        return factory.newLiteral(token);
    }

    /**
     * Returns an array initializer which contains the original elements as related Java expression.
     * @param factory the Java DOM factory
     * @param array the target array
     * @return the corresponded array initializer
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public static ArrayInitializer toArrayInitializer(ModelFactory factory, int[] array) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (array == null) {
            throw new IllegalArgumentException("array must not be null"); //$NON-NLS-1$
        }
        List<Expression> literals = new ArrayList<>();
        for (int value : array) {
            literals.add(Models.toLiteral(factory, value));
        }
        return factory.newArrayInitializer(literals);
    }

    /**
     * Returns an array initializer which contains the original elements as related Java expression.
     * @param factory the Java DOM factory
     * @param array the target array
     * @return the corresponded array initializer
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public static ArrayInitializer toArrayInitializer(ModelFactory factory, float[] array) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (array == null) {
            throw new IllegalArgumentException("array must not be null"); //$NON-NLS-1$
        }
        List<Expression> literals = new ArrayList<>();
        for (float value : array) {
            literals.add(Models.toLiteral(factory, value));
        }
        return factory.newArrayInitializer(literals);
    }

    /**
     * Returns an array initializer which contains the original elements as related Java expression.
     * @param factory the Java DOM factory
     * @param array the target array
     * @return the corresponded array initializer
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public static ArrayInitializer toArrayInitializer(ModelFactory factory, long[] array) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (array == null) {
            throw new IllegalArgumentException("array must not be null"); //$NON-NLS-1$
        }
        List<Expression> literals = new ArrayList<>();
        for (long value : array) {
            literals.add(Models.toLiteral(factory, value));
        }
        return factory.newArrayInitializer(literals);
    }

    /**
     * Returns an array initializer which contains the original elements as related Java expression.
     * @param factory the Java DOM factory
     * @param array the target array
     * @return the corresponded array initializer
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public static ArrayInitializer toArrayInitializer(ModelFactory factory, double[] array) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (array == null) {
            throw new IllegalArgumentException("array must not be null"); //$NON-NLS-1$
        }
        List<Expression> literals = new ArrayList<>();
        for (double value : array) {
            literals.add(Models.toLiteral(factory, value));
        }
        return factory.newArrayInitializer(literals);
    }

    /**
     * Returns an array initializer which contains the original elements as related Java expression.
     * @param factory the Java DOM factory
     * @param array the target array
     * @return the corresponded array initializer
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public static ArrayInitializer toArrayInitializer(ModelFactory factory, char[] array) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (array == null) {
            throw new IllegalArgumentException("array must not be null"); //$NON-NLS-1$
        }
        List<Expression> literals = new ArrayList<>();
        for (char value : array) {
            literals.add(Models.toLiteral(factory, value));
        }
        return factory.newArrayInitializer(literals);
    }

    /**
     * Returns an array initializer which contains the original elements as related Java expression.
     * @param factory the Java DOM factory
     * @param array the target array
     * @return the corresponded array initializer
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public static ArrayInitializer toArrayInitializer(ModelFactory factory, boolean[] array) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (array == null) {
            throw new IllegalArgumentException("array must not be null"); //$NON-NLS-1$
        }
        List<Expression> literals = new ArrayList<>();
        for (boolean value : array) {
            literals.add(Models.toLiteral(factory, value));
        }
        return factory.newArrayInitializer(literals);
    }

    /**
     * Returns an array initializer which contains the original elements as related Java expression.
     * @param factory the Java DOM factory
     * @param array the target array
     * @return the corresponded array initializer
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public static ArrayInitializer toArrayInitializer(ModelFactory factory, byte[] array) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (array == null) {
            throw new IllegalArgumentException("array must not be null"); //$NON-NLS-1$
        }
        List<Expression> literals = new ArrayList<>();
        for (byte value : array) {
            literals.add(Models.toLiteral(factory, value));
        }
        return factory.newArrayInitializer(literals);
    }

    /**
     * Returns an array initializer which contains the original elements as related Java expression.
     * @param factory the Java DOM factory
     * @param array the target array
     * @return the corresponded array initializer
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public static ArrayInitializer toArrayInitializer(ModelFactory factory, short[] array) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (array == null) {
            throw new IllegalArgumentException("array must not be null"); //$NON-NLS-1$
        }
        List<Expression> literals = new ArrayList<>();
        for (short value : array) {
            literals.add(Models.toLiteral(factory, value));
        }
        return factory.newArrayInitializer(literals);
    }

    /**
     * Returns an array initializer which contains the original elements as related Java expression.
     * @param factory the Java DOM factory
     * @param array the target array
     * @return the corresponded array initializer
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public static ArrayInitializer toArrayInitializer(ModelFactory factory, String[] array) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (array == null) {
            throw new IllegalArgumentException("array must not be null"); //$NON-NLS-1$
        }
        List<Expression> literals = new ArrayList<>();
        for (String value : array) {
            if (value == null) {
                literals.add(Models.toNullLiteral(factory));
            } else {
                literals.add(Models.toLiteral(factory, value));
            }
        }
        return factory.newArrayInitializer(literals);
    }

    /**
     * Returns an array initializer which contains the original elements as related Java expression.
     * @param factory the Java DOM factory
     * @param array the target array
     * @return the corresponded array initializer
     * @throws IllegalArgumentException if the parameters are {@code null}
     */
    public static ArrayInitializer toArrayInitializer(ModelFactory factory, java.lang.reflect.Type[] array) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (array == null) {
            throw new IllegalArgumentException("array must not be null"); //$NON-NLS-1$
        }
        List<Expression> literals = new ArrayList<>();
        for (java.lang.reflect.Type value : array) {
            if (value == null) {
                literals.add(Models.toNullLiteral(factory));
            } else {
                literals.add(Models.toClassLiteral(factory, value));
            }
        }
        return factory.newArrayInitializer(literals);
    }

    private Models() {
        throw new AssertionError();
    }
}
