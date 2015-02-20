==============
開発環境のJava
==============

この文書では、開発環境で利用するJava(JDK)環境やバージョンについて、
また利用するJavaに応じたアプリケーションプロジェクトの設定について説明します。

Asakusa Frameworkが動作検証を行なっているプラットフォーム環境の情報については、
:doc:`../product/target-platform` を参照してください。

開発環境で利用するJDK
=====================
Asakusa Framework バージョン |version| では主に `Oracle JDK`_ で動作検証を行なっています。
また JDKのバージョンは主に JDK7 (JDK 1.7) で動作検証を行なっています。

..  attention::
    :jinrikisha:`Jinrikisha <index.xml>` を使って開発環境を構築する場合、
    試用を目的として `Open JDK`_ を使ったセットアップを行う機能を提供しています。
    `Open JDK`_ では簡単な動作検証のみを行なっているため、
    試用目的以外の用途でAsakusa Frameworkを使用する場合は `Oracle JDK`_ を利用してください。

..  _`Oracle JDK`: http://www.oracle.com/technetwork/jp/java/javase/index.html
..  _`Open JDK`: http://openjdk.java.net/

JDKのインストール
-----------------
Oracle JDKの入手やインストール方法については、 :doc:`../introduction/start-guide` や
OracleのJavaのサイトなどを参照してください。

開発ツールが利用するJDKの設定
=============================
GradleやEclipseなどの開発ツールが利用するためのJDKの設定を行います。

通常、これらは環境変数 ``$PATH`` や ``$JAVA_HOME`` に対して、インストールしたJDKのパスを設定します。
具体的な設定については :doc:`../introduction/start-guide` や各ツールのドキュメントなどを参照してください。

アプリケーションプロジェクトの設定
==================================
Asakusa Frameworkのバージョン |version| では、
アプリケーション開発環境のJavaバージョンの設定は、
デフォルトでJDK7(JDK 1.7)を利用する環境向けに設定されています。

JDK6(JDK 1.6)を使用する場合、必要に応じてこれらの設定を変更します。

..  attention::
    Asakusa Framework バージョン 0.7.0 から
    アプリケーション開発環境向けのデフォルト設定が
    JDK6 から JDK7 に変更になりました。 
    過去バージョンからのマイグレーションに関する注意点などは、
    :doc:`migration-guide` も参照してください。

..  attention::
    一部のHadoopディストリビューションでは、 Java6でのみ動作確認が行われています。
    Asakusa Frameworkが動作検証を行なっているHadoopディストリビューションと
    Javaバージョンの組み合わせについては、 :doc:`../product/target-platform` を参照してください。

..  attention::
    JDK8 上での動作は未確認です。

Javaバージョンに関する設定
--------------------------
:doc:`../introduction/start-guide` や :doc:`gradle-plugin` を利用して作成したアプリケーションプロジェクトは、
Javaバージョンの設定に関して以下の設定を持ちます。

ソースコードのバージョン ( ``source`` )
  javac(Javaコンパイラ)が受け付けるソースコードのバージョンを指定します。
  Asakusa Frameworkのバージョン |version| ではデフォルトでこの値が ``1.7`` に設定されています。

ターゲットのクラスファイルバージョン ( ``target`` )
  javacが生成するクラスファイルのバージョンを指定します。
  Asakusa Frameworkのバージョン |version| ではデフォルトでこの値が ``1.7`` に設定されています。

EclipseのJRE用クラスパスコンテナ
  Eclipseがプロジェクトに対して使用するJDKのバージョンを指定します。
  Asakusa Frameworkのバージョン |version| ではこの値が ``JavaSE-1.7`` に設定されています [#]_ 。

..  [#] Asakusa Gradle Plugin はソースコードのバージョンに対応するEclipseのJRE用クラスパスコンテナを設定します。

.. _using-jdk6:

JDK6向け設定
------------
アプリケーションプロジェクトをJDK6向けの設定に変更する場合、
``build.gradle`` に対して以下の設定を変更します。

Batch Application Pluginの規約プロパティの設定
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Batch Application Pluginに設定しているソースコードのバージョンとターゲットのクラスファイルバージョンを変更します。
``asakusafw`` ブロックに ``javac`` ブロックを追加し、
プロパティ ``sourceCompatibility`` と ``targetCompatibility`` の値に ``'1.6'`` を設定します。

..  code-block:: groovy
    
    asakusafw {
        asakusafwVersion '0.7.3-hadoop1'
    
        modelgen {
            modelgenSourcePackage 'com.example.modelgen'
        }
        compiler {
            compiledSourcePackage 'com.example.batchapp'
        }
        javac {
            sourceCompatibility '1.6'
            targetCompatibility '1.6'
        }
    }

Eclipseプロジェクト情報の再構成
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Batch Application Pluginの設定をEclipseのプロジェクト設定に反映するには、
プロジェクト上で ``cleanEclipse`` タスクと ``eclipse`` タスクを実行します。

..  code-block:: sh
    
    ./gradlew cleanEclipse eclipse

..  attention::
    JDK7のみがインストールされている開発環境でEclipseを使用する場合、
    JDK6向けの設定を行うと Eclipseの ``Problems`` ビューに以下の警告が出力されることがあります。
    
    .. code-block:: none
       
       Build path specifies execution environment JavaSE-1.6. There are no JREs installed in the workspace that are strictly compatible with this environment. 
    
    これは、プロジェクト側の設定では ``JavaSE-1.6`` が指定されているが、Eclipse側で厳密に一致するJavaのバージョンがインストールされていないと認識するためです。開発環境のJavaバージョンを変えずに警告を非表示にする場合は、EclipseのPreferences画面から以下の設定を行います。
    
    * ``[Java]`` -> ``[Compiler]`` -> ``[Building]`` -> ``[Build path problems]`` の ``No strictly compatible JRE for execution environment available:`` を ``Ignore`` に変更

..  attention::
    Eclipse の バージョン ``4.4`` 以降は、 JDK7以降でのみ動作します。

