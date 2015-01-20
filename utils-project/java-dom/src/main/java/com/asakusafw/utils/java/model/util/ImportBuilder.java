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
package com.asakusafw.utils.java.model.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.asakusafw.utils.java.model.syntax.ArrayType;
import com.asakusafw.utils.java.model.syntax.BasicType;
import com.asakusafw.utils.java.model.syntax.ImportDeclaration;
import com.asakusafw.utils.java.model.syntax.ImportKind;
import com.asakusafw.utils.java.model.syntax.ModelFactory;
import com.asakusafw.utils.java.model.syntax.ModelKind;
import com.asakusafw.utils.java.model.syntax.Name;
import com.asakusafw.utils.java.model.syntax.NamedType;
import com.asakusafw.utils.java.model.syntax.PackageDeclaration;
import com.asakusafw.utils.java.model.syntax.ParameterizedType;
import com.asakusafw.utils.java.model.syntax.QualifiedName;
import com.asakusafw.utils.java.model.syntax.QualifiedType;
import com.asakusafw.utils.java.model.syntax.SimpleName;
import com.asakusafw.utils.java.model.syntax.StrictVisitor;
import com.asakusafw.utils.java.model.syntax.Type;
import com.asakusafw.utils.java.model.syntax.Wildcard;
import com.asakusafw.utils.java.model.syntax.WildcardBoundKind;

/**
 * {@link ImportDeclaration}を構築するビルダー。
 */
public class ImportBuilder {

    private final PackageDeclaration packageDecl;

    private final Resolver resolver;

    /**
     * インスタンスを生成する。
     * @param factory モデルを生成するファクトリ
     * @param packageDecl 現在のパッケージ宣言
     * @param strategy インポートを行う戦略
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public ImportBuilder(
            ModelFactory factory,
            PackageDeclaration packageDecl,
            Strategy strategy) {
        if (factory == null) {
            throw new IllegalArgumentException("factory must not be null"); //$NON-NLS-1$
        }
        if (strategy == null) {
            throw new IllegalArgumentException("strategy must not be null"); //$NON-NLS-1$
        }
        this.resolver = new Resolver(factory, strategy, packageDecl);
        this.packageDecl = packageDecl;
    }

    /**
     * 指定の名前をパッケージメンバーの型名としてインポートし、インポート後の型表現を返す。
     * @param name 対象の型名
     * @return インポート後の型
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public Type resolvePackageMember(Name name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        Type type;
        if (name.getModelKind() == ModelKind.SIMPLE_NAME) {
            type = reservePackageMember((SimpleName) name);
        } else {
            type = reservePackageMember((QualifiedName) name);
        }
        return resolve(type);
    }

    private Type reservePackageMember(SimpleName name) {
        assert name != null;
        if (packageDecl == null) {
            return resolver.factory.newNamedType(name);
        } else {
            Name qualified = Models.append(
                    resolver.factory,
                    packageDecl.getName(),
                    name);
            return resolver.factory.newNamedType(qualified);
        }
    }

    private Type reservePackageMember(QualifiedName name) {
        assert name != null;
        List<SimpleName> list = name.toNameList();
        Name current;
        Iterator<SimpleName> iter = list.iterator();
        assert iter.hasNext();
        SimpleName first = iter.next();
        if (packageDecl == null) {
            current = first;
        } else {
            current = resolver.factory.newQualifiedName(
                    packageDecl.getName(),
                    first);
        }
        resolver.reserved.put(first, current);

        while (iter.hasNext()) {
            SimpleName next = iter.next();
            current = resolver.factory.newQualifiedName(current, next);
            resolver.reserved.put(next, current);
        }
        return resolver.factory.newNamedType(current);
    }

    /**
     * 指定の型を可能であればインポートし、インポート後の型表現を返す。
     * @param type 対象の型
     * @return インポート後の型
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public Type resolve(Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        return type.accept(resolver, null);
    }

    /**
     * 指定の名前からなる型を可能であればインポートし、インポート後の型表現を返す。
     * @param name 対象の型名
     * @return インポート後の型
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public Type toType(Name name) {
        if (name == null) {
            throw new IllegalArgumentException("name must not be null"); //$NON-NLS-1$
        }
        return resolve(resolver.factory.newNamedType(name));
    }

    /**
     * 指定の型を可能であればインポートし、インポート後の型表現を返す。
     * @param type 対象の型
     * @return インポート後の型
     * @throws IllegalArgumentException 引数に{@code null}が含まれる場合
     */
    public Type toType(java.lang.reflect.Type type) {
        if (type == null) {
            throw new IllegalArgumentException("type must not be null"); //$NON-NLS-1$
        }
        return resolve(Models.toType(resolver.factory, type));
    }

    /**
     * これまでにこのビルダーでインポートしたクラスに対するインポート宣言の一覧を返す。
     * @return インポートしたクラスに対するインポート宣言の一覧
     */
    public List<ImportDeclaration> toImportDeclarations() {
        ModelFactory f = resolver.factory;
        Map<QualifiedName, SimpleName> imported = resolver.imported;
        Set<Name> implicit = createImplicit();

        List<ImportDeclaration> results = new ArrayList<ImportDeclaration>();
        for (QualifiedName name : imported.keySet()) {
            if (implicit.contains(name.getQualifier())) {
                continue;
            }
            results.add(f.newImportDeclaration(ImportKind.SINGLE_TYPE, name));
        }
        Collections.sort(results, ImportComparator.INSTANCE);
        return results;
    }

    /**
     * このビルダーが対象としているパッケージの宣言を返す。
     * @return このビルダーが対象としているパッケージの宣言
     */
    public PackageDeclaration getPackageDeclaration() {
        return this.packageDecl;
    }

    private Set<Name> createImplicit() {
        Set<Name> implicit = new HashSet<Name>();
        implicit.add(Models.toName(resolver.factory, "java.lang"));
        if (packageDecl != null) {
            implicit.add(packageDecl.getName());
        }
        return implicit;
    }

    private enum ImportComparator implements Comparator<ImportDeclaration> {

        /**
         * 唯一のインスタンス。
         */
        INSTANCE,

        ;
        @Override
        public int compare(ImportDeclaration o1, ImportDeclaration o2) {
            if (o1.getImportKind() != o2.getImportKind()) {
                return o1.getImportKind().compareTo(o2.getImportKind());
            }
            return o1.getName().toNameString().compareTo(o2.getName().toNameString());
        }
    }

    private static class Resolver extends StrictVisitor<Type, Void, NoThrow> {

        final Strategy strategy;

        final Map<QualifiedName, SimpleName> imported;

        final Map<SimpleName, Name> reserved;

        private Set<Name> knownPackageNames = new HashSet<Name>();

        final ModelFactory factory;

        Resolver(
                ModelFactory factory,
                Strategy strategy,
                PackageDeclaration packageDecl) {
            this.factory = factory;
            this.strategy = strategy;
            this.knownPackageNames = new HashSet<Name>();
            if (packageDecl != null) {
                Name current = packageDecl.getName();
                while (current instanceof QualifiedName) {
                    this.knownPackageNames.add(current);
                    current = ((QualifiedName) current).getQualifier();
                }
                this.knownPackageNames.add(current);
            }
            this.imported = new HashMap<QualifiedName, SimpleName>();
            this.reserved = new HashMap<SimpleName, Name>();
        }

        @Override
        public Type visitArrayType(ArrayType elem, Void context) {
            Type component = elem.getComponentType().accept(this, null);
            if (elem.getComponentType().equals(component)) {
                return elem;
            }
            return factory.newArrayType(component);
        }

        @Override
        public Type visitBasicType(BasicType elem, Void context) {
            return elem;
        }

        @Override
        public Type visitNamedType(NamedType elem, Void context) {
            Name name = elem.getName();

            if (name.getModelKind() == ModelKind.SIMPLE_NAME) {
                reserved.put((SimpleName) name, elem.getName());
                return elem;
            }

            LinkedList<SimpleName> segments = new LinkedList<SimpleName>();
            name = normalize(name, segments);
            if (name.getModelKind() == ModelKind.SIMPLE_NAME) {
                reserved.put((SimpleName) name, elem.getName());
                return elem;
            }

            QualifiedName qname = (QualifiedName) name;
            SimpleName renamed = imported.get(qname);
            if (renamed == null) {
                if (reserved.containsKey(qname.getSimpleName())
                        && reserved.get(qname.getSimpleName()).equals(qname) == false) {
                    return elem;
                }
                imported.put(qname, qname.getSimpleName());
                reserved.put(qname.getSimpleName(), qname);
            }
            return factory.newNamedType(Models.append(
                factory,
                segments.toArray(new Name[segments.size()])));
        }

        private Name normalize(
                Name name,
                LinkedList<SimpleName> segments) {
            Name current = name;
            if (strategy == Strategy.TOP_LEVEL) {
                while (isLikeEnclosingType(current)) {
                    QualifiedName qname = (QualifiedName) current;
                    segments.addFirst(qname.getSimpleName());
                    current = qname.getQualifier();
                }
            }
            if (current.getModelKind() == ModelKind.QUALIFIED_NAME) {
                segments.addFirst(((QualifiedName) current).getSimpleName());
            } else {
                segments.addFirst((SimpleName) current);
            }
            return current;
        }

        private boolean isLikeEnclosingType(Name name) {
            assert name != null;
            if (name.getModelKind() != ModelKind.QUALIFIED_NAME) {
                return false;
            }
            Name qualifier = ((QualifiedName) name).getQualifier();

            // 限定子がパッケージなら、その名前は定義上トップレベル
            if (knownPackageNames.contains(qualifier)) {
                return false;
            }

            // 親の単純名がクラス名の形式であれば、この型は内部クラスとみなす
            SimpleName parent;
            if (qualifier.getModelKind() == ModelKind.QUALIFIED_NAME) {
                parent = ((QualifiedName) qualifier).getSimpleName();
            } else {
                parent = (SimpleName) qualifier;
            }
            return isClassName(parent);
        }

        private boolean isClassName(SimpleName name) {
            assert name != null;
            char first = name.getToken().charAt(0);
            return Character.isUpperCase(first);
        }

        @Override
        public Type visitParameterizedType(ParameterizedType elem, Void context) {
            Type nonparameterized = elem.getType().accept(this, null);
            List<Type> arguments = new ArrayList<Type>();
            for (Type t : elem.getTypeArguments()) {
                arguments.add(t.accept(this, null));
            }
            if (nonparameterized.equals(elem.getType())
                    && arguments.equals(elem.getTypeArguments())) {
                return elem;
            }
            return factory.newParameterizedType(nonparameterized, arguments);
        }

        @Override
        public Type visitQualifiedType(QualifiedType elem, Void context) {
            Type qualifier = elem.getQualifier().accept(this, null);
            if (qualifier.equals(elem.getQualifier())) {
                return elem;
            }
            return factory.newQualifiedType(qualifier, elem.getSimpleName());
        }

        @Override
        public Type visitWildcard(Wildcard elem, Void context) {
            if (elem.getBoundKind() == WildcardBoundKind.UNBOUNDED) {
                return elem;
            }
            Type bound = elem.getTypeBound().accept(this, null);
            if (bound.equals(elem.getTypeBound())) {
                return elem;
            }
            return factory.newWildcard(elem.getBoundKind(), bound);
        }
    }

    /**
     * インポートの戦略。
     */
    public enum Strategy {

        /**
         * トップレベル型のみをインポートする。
         */
        TOP_LEVEL,

        /**
         * 末端の単純名を利用するようにインポートする。
         */
        ENCLOSING,
    }
}
