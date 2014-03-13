========================================
JDK7を利用してアプリケーションを開発する
========================================

この文書では、アプリケーションの開発にJDK7(JDK1.7)を利用する場合の注意点や、JDK7から追加になった言語機能やAPIを使ってJavaソースコードを記述する場合の設定方法について説明します。

..  attention::
    Asakusa Frameworkのバージョン |version| では基本的には特別な設定を行うことなく、JDK7を利用してアプリケーションを開発し実行することができます [#]_ 。

    アプリケーションのソースコードをJDK7から追加になった言語機能やAPIを使って記述する場合に限り、本書で後述するアプリケーションプロジェクトに対する設定の変更を行う必要があります。

..  [#] Asakusa Frameworkが動作検証を行なっているプラットフォーム環境の情報については、 :doc:`../product/target-platform` を参照してください。

開発環境で利用するJDK
=====================
Asakusa FrameworkはJDK7の動作検証はOracle JDK7でのみ行なっています。開発環境で利用するJDKはOracle JDKを推奨します。

JDKの入手やインストール方法については、OracleのJavaのサイトなどを参照してください。

開発ツールが利用するJDKの設定
=============================
GradleやMaven、Eclipseなどの開発ツールが利用するためのJDKの設定を行います。通常、これらは環境変数 ``$PATH`` や ``$JAVA_HOME`` に対して、インストールしたJDKのパスを設定します。

具体的な設定については各ツールのドキュメントなどを参照してください。

アプリケーションプロジェクトの設定
==================================
Asakusa Frameworkのバージョン |version| では、Asakusa Frameworkのアプリケーション開発環境の設定はデフォルトでJava6(Java 1.6)向けの設定が行われています。JDK7を使用する場合、必要に応じてこれらの設定を変更します。

アプリケーションプロジェクトのデフォルト設定
--------------------------------------------
:doc:`../introduction/start-guide` や :doc:`gradle-plugin` 、 :doc:`maven-archetype` を利用して作成したアプリケーションプロジェクトは、Javaの設定に関してデフォルトで以下の設定を持ちます。

ソースコードのバージョン ( ``source`` )
  javac(Javaコンパイラ)が受け付けるソースコードのバージョンを指定します。Asakusa Frameworkのバージョン |version| ではデフォルトでこの値が ``1.6`` に設定されています。

ターゲットのクラスファイルバージョン ( ``target`` )
  javacが生成するクラスファイルのバージョンを指定します。Asakusa Frameworkのバージョン |version| ではデフォルトでこの値が ``1.6`` に設定されています。

EclipseのJRE用クラスパスコンテナ
  Eclipseがプロジェクトに対して使用するJDKのバージョンを指定します。Asakusa Frameworkのバージョン |version| ではこの値が ``JavaSE-1.6`` に設定されています [#]_ 。

..  attention::
    JDK7のみがインストールされている開発環境でEclipseを使用する場合などにおいて、Eclipseの ``Problems`` ビューに以下の警告が出力されることがあります。
    
    .. code-block:: none
       
       Build path specifies execution environment JavaSE-1.6. There are no JREs installed in the workspace that are strictly compatible with this environment. 
    
    これは、プロジェクト側の設定では ``JavaSE-1.6`` が指定されているが、Eclipse側で厳密に一致するJavaのバージョンがインストールされていないと認識するためです。後述するJDK7向け設定を行うとこの警告は消えますが、設定を変えずに警告を非表示にする場合は、EclipseのPreferences画面から以下の設定を行います。
    
    * ``[Java]`` -> ``[Compiler]`` -> ``[Building]`` -> ``[Build path problems]`` の ``No strictly compatible JRE for execution environment available:`` を ``Ignore`` に変更

..  [#] Asakusa Gradle Pluginではソースコードのバージョンに対応するEclipseのJRE用クラスパスコンテナの設定が生成されます。

デフォルト設定でJDK7を利用する場合の注意点
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
上記で説明したデフォルト設定のままでもJDK7を利用することはできますが、JDK7から追加になった言語機能やAPIは利用できません。デフォルト設定のままこれらの機能を利用してビルドした場合、アプリケーションのソースコードはビルドツールやIDE上ではコンパイルエラーとなります。

JDK7から追加になった言語機能やAPIを利用する場合、以下に示す手順でアプリケーションプロジェクトの設定を変更します。

GradleプロジェクトのJDK7向け設定
--------------------------------
:doc:`../introduction/start-guide` や :doc:`gradle-plugin` を利用したアプリケーションプロジェクトについては、プロジェクトの ``build.gradle`` に対して以下の設定を変更します。

Batch Application Pluginの規約プロパティの設定
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Batch Application Pluginに設定しているソースコードのバージョンとターゲットのクラスファイルバージョンを変更します。
``asakusafw`` ブロックに ``javac/sourceCompatibility`` と ``javac/targetCompatibility`` を追加し、これらの値に ``1.7`` を設定します。

..  code-block:: groovy
    
    asakusafw {
        asakusafwVersion '0.6.1'
    
        modelgen {
            modelgenSourcePackage 'com.example.modelgen'
        }
        compiler {
            compiledSourcePackage 'com.example.batchapp'
        }
        javac {
            sourceCompatibility '1.7'
            targetCompatibility '1.7'
        }
    }

Batch Application Pluginの設定をEclipseのプロジェクト設定に反映するには、プロジェクト上で ``cleanEclipse`` タスクと ``eclipse`` タスクを実行します。

..  code-block:: sh

    ./gradlew cleanEclipse eclipse


MavenプロジェクトのJDK7向け設定
-------------------------------
:doc:`maven-archetype` の手順に従って作成したアプリケーションプロジェクトについては、プロジェクトの ``pom.xml`` に対して以下の設定を変更します。

Maven Compiler Pluginの設定
~~~~~~~~~~~~~~~~~~~~~~~~~~~
Maven Compiler Pluginに設定しているソースコードのバージョンとターゲットのクラスファイルバージョンを変更します。
``maven-compiler-plugin`` の ``configuration`` に含まれる ``source`` と ``target`` の値を ``1.7`` に変更します。

..  code-block:: xml
    
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>${plugin.compiler.version}</version>
        <configuration>
            <fork>true</fork>
            <encoding>${project.build.sourceEncoding}</encoding>
            <source>1.7</source>
            <target>1.7</target>
            <compilerArgument>-Xmaxerrs" "10000" "-XprintRounds</compilerArgument>
            <useIncrementalCompilation>false</useIncrementalCompilation>
        </configuration>
    </plugin>

Maven Eclipse Pluginの設定
~~~~~~~~~~~~~~~~~~~~~~~~~~
Maven Eclipse Pluginに設定しているJRE用クラスパスコンテナを変更します。
``maven-eclipse-plugin`` の ``configuration`` に含まれる ``classpathContainer`` の値を ``...StandardVMType/JavaSE-1.7`` に変更します。

..  code-block:: xml
    
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-eclipse-plugin</artifactId>
        <version>${plugin.eclipse.version}</version>
        <configuration>
            <downloadSources>true</downloadSources>
            <downloadJavadocs>false</downloadJavadocs>
            <classpathContainers>
                <classpathContainer>org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.7</classpathContainer>
            </classpathContainers>
            <additionalConfig>
                <file>
                    <name>.settings/org.eclipse.jdt.core.prefs</name>

Maven Eclipse Pluginの設定をEclipseのプロジェクト設定に反映するには、プロジェクト上で ``mvn eclipse:eclipse`` を実行します。

..  code-block:: sh

    mvn eclipse:eclipse

