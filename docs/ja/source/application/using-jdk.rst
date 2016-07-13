==============
開発環境のJava
==============

この文書では、開発環境で利用するJava(JDK)環境やバージョンについて、また利用するJavaに応じたアプリケーションプロジェクトの設定について説明します。

..  seealso::
    Asakusa Frameworkが動作検証を行なっているプラットフォーム環境の情報については、 :doc:`../product/target-platform` を参照してください。

開発環境で利用するJDK
=====================

Asakusa Framework バージョン |version| では主に `Oracle JDK`_ で動作検証を行なっています。

..  attention::
    :jinrikisha:`Jinrikisha <index.xml>` を使って開発環境を構築する場合、試用を目的として `Open JDK`_ を使ったセットアップを行う機能を提供していますが、 `Open JDK`_ では簡単な動作検証のみを行なっているため、試用目的以外の用途でAsakusa Frameworkを使用する場合は `Oracle JDK`_ を利用してください。

..  _`Oracle JDK`: http://www.oracle.com/technetwork/jp/java/javase/index.html
..  _`Open JDK`: http://openjdk.java.net/

JDKのインストール
-----------------

Oracle JDKの入手やインストール方法については、OracleのJavaのサイトなど [#]_ を参照してください。

..  [#] https://docs.oracle.com/javase/jp/8/docs/technotes/guides/install/install_overview.html

開発ツールが利用するJDKの設定
=============================

GradleやEclipseなどの開発ツールが利用するためのJDKの設定を行います。

通常、これらは環境変数 ``$PATH`` や ``$JAVA_HOME`` に対して、インストールしたJDKのパスを設定します。
詳しくは各ツールのドキュメントなどを参照してください。

アプリケーションプロジェクトの設定
==================================

Asakusa Frameworkのバージョン |version| では、アプリケーション開発環境のJavaバージョンの設定は、デフォルトでJDK 7(JDK 1.7)を利用する環境向けに設定されています。

その他のバージョンを使用する場合、必要に応じてこれらの設定を変更します。

..  attention::
    Asakusa Framework バージョン 0.8.0 からJDK 6の利用は非対応となりました。

..  attention::
    Asakusa Framework バージョン 0.7.0 からアプリケーション開発環境向けのデフォルト設定が JDK 6からJDK 7に変更になりました。
    過去バージョンからのマイグレーションに関する注意点などは、 :doc:`migration-guide` も参照してください。

Javaバージョンに関する設定
--------------------------

:doc:`../introduction/start-guide` や :doc:`gradle-plugin` を利用して作成したアプリケーションプロジェクトは、Javaバージョンの設定に関して以下の設定を持ちます。

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

Javaバージョンの変更
--------------------

アプリケーションプロジェクトのJavaバージョンに関する設定に変更する場合、 :file:`build.gradle` に対して以下の設定を変更します。

build.gradleの設定
~~~~~~~~~~~~~~~~~~

:file:`build.gradle` に設定しているソースコードのバージョンとターゲットのクラスファイルバージョンを変更します。
``asakusafw`` ブロックに ``javac`` ブロックを追加し、プロパティ ``sourceCompatibility`` と ``targetCompatibility`` の値に 利用するJavaバージョンを設定します。

以下は、Javaバージョンに ``1.8`` (JDK 8)を利用するための設定例です。

..  code-block:: groovy
    :caption: build.gradle
    :name: build.gradle-using-jdk-1

    asakusafw {
        javac {
            sourceCompatibility '1.8'
            targetCompatibility '1.8'
        }
    }

Eclipseプロジェクト情報の再構成
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

:file:`build.gradle` の設定をEclipseのプロジェクト設定に反映するには、プロジェクト上で :program:`cleanEclipse` タスクと :program:`eclipse` タスクを実行します。

..  code-block:: sh

    ./gradlew cleanEclipse eclipse
