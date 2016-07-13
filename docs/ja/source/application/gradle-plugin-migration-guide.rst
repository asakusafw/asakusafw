============================================
Asakusa Gradle Plugin マイグレーションガイド
============================================

ここでは、Asakusa Gradle Plugin で構築した開発環境のバージョンアップ手順や、 従来のAsakusa Frameworkが提供するMavenベースのビルドシステムからAsakusa Gradle Pluginを使ったビルドシステムに移行するための手順を説明します。

Asakusa Frameworkの各バージョン固有のマイグレーション情報については :doc:`migration-guide` に説明があるので、こちらも必ず確認してください。

.. _vup-gradle-plugin:

Gradleプロジェクトのマイグレーション
====================================

ここではAsakusa Gradle Pluginを利用したアプリケーションプロジェクトをマイグレーションする手順例を説明します。

..  note::
    以降ではコマンドライン上での手順を説明しますが、Shafuを利用している場合も手順は同様です。
    利用するメニューなどについては、 :jinrikisha:`Shafuのドキュメント <shafu.html>` を参照してください。

マイグレーション前のビルドの確認
--------------------------------

..  hint::
    この手順は必須ではありませんが、マイグレーション作業時に問題が発生した場合に備えて実施しておくことを推奨します。

マイグレーション以前の状態でプロジェクトのフルビルドを行い、ビルドが成功することを確認します。

..  code-block:: sh

    ./gradlew clean build

また、ビルドが成功した状態のプロジェクトをバックアップするなど、マイグレーション作業で問題が発生した場合にすぐに元の状態に戻れるようにしておきます。

Asakusa Gradle Pluginのバージョン変更
-------------------------------------

ビルドスクリプト内の ``buildscript`` ブロック内に定義しているAsakusa Gradle Pluginのバージョンの値を、アップデートするAsakusa Gradle Pluginのバージョンに変更します。

..  code-block:: groovy
    :caption: build.gradle
    :name: build.gradle-gradle-plugin-migration-guide-1
    :emphasize-lines: 6

    buildscript {
        repositories {
            maven { url 'http://asakusafw.s3.amazonaws.com/maven/releases' }
        }
        dependencies {
            classpath group: 'com.asakusafw', name: 'asakusa-gradle-plugins', version: '0.8.1'
        }
    }

:asakusa-on-spark:`Asakusa on Spark <index.html>` を利用している場合は、Asakusa on Spark Gradle Pluginのバージョンを指定します。

..  literalinclude:: gradle-attachment/template-build.gradle
    :language: groovy
    :caption: build.gradle
    :name: build.gradle-gradle-plugin-migration-guide-2
    :lines: 3-10
    :emphasize-lines: 6

..  attention::
    Asakusa on Spark Gradle Pluginのバージョンや、Asakusa on Sparkが利用するAsakusa Frameworkのバージョンについては、:asakusa-on-spark:`Asakusa on Sparkのドキュメント <index.html>` を確認してください。

.. _vup-gradle-wrapper:

バージョン固有のビルドスクリプト設定の変更
------------------------------------------

Asakusa Frameworkの各バージョンでビルドスクリプトに関すバージョン固有のマイグレーション手順が示されている場合、ここでビルドスクリプトの内容を編集します。

バージョン固有のマイグレーション内容ついては、以下のドキュメントを参照してください。

* :doc:`migration-guide` を参照してください。

..  warning::
    バージョン 0.6系, 0.7系のGradleプロジェクトのマイグレーション時には、必ず以下のドキュメントを参照してください。

    * :doc:`gradle-plugin-v08-changes`

Gradleラッパーのアップデート
----------------------------

アプリケーションプロジェクトで利用するGradleラッパーをAsakusa Gradle Pluginが推奨するバージョンにアップデートします。

プロジェクトディレクトリに移動し、 :program:`asakusaUpgrade` タスクを実行します。

..  code-block:: sh

    ./gradlew asakusaUpgrade

:program:`asakusaUpgrade` タスクを実行後、 :program:`help` タスクを実行するとGradleラッパーのバージョンを確認できます。

..  code-block:: sh

    ./gradlew help

..  code-block:: none

    :help

    Welcome to Gradle 2.12.
    ...

..  attention::
    Shafuを利用している場合、ShafuはプロジェクトのGradleラッパーを使用せず、Shafu側で設定されているGradleを使用します。
    Shafuが利用するGradleのバージョンをアップデートするにはShafu側の設定を変更する必要があります。

    Shafuの設定については :jinrikisha:`Shafuのドキュメント <shafu.html>` を参照してください。

Asakusa Frameworkの再インストール
---------------------------------

開発環境のAsakusa Frameworkを再インストールします。

..  code-block:: sh

    ./gradlew installAsakusafw

マイグレーションしたビルド設定の確認
------------------------------------

マイグレーション後の状態でプロジェクトのフルビルドを行い、ビルドが成功することを確認します。

..  code-block:: sh

    ./gradlew clean build

Eclipse定義ファイルの更新
-------------------------

Eclipseを利用している場合は、Eclipse用定義ファイルを更新します。

..  code-block:: sh

    ./gradlew cleanEclipse eclipse

.. _migrate-from-maven-to-gradle:

Mavenプロジェクトのマイグレーション
===================================

ここでは、 :doc:`../application/maven-archetype` や Asakusa Framework バージョン ``0.5.3`` 以前の :doc:`../introduction/start-guide` 及び :jinrikisha:`Jinrikisha (人力車) - Asakusa Framework Starter Package - <index.html>` で記載されている手順に従って構築した開発環境やMavenベースのアプリケーションプロジェクト(以下「Mavenプロジェクト」と表記)をAsakusa Gradle Pluginを使った環境にマイグレーションする手順を説明します。

..  attention::
    プロジェクトのソースディレクトリに含まれるアプリケーションのソースコード(Asakusa DSL, DMDL, テストコードなど)についてのマイグレーション作業は不要で、そのまま利用することが出来ます。

.. _apply-gradle-project-template:

マイグレーション前のビルドの確認
--------------------------------

..  hint::
    この手順は必須ではありませんが、マイグレーション作業時に問題が発生した場合に備えて実施しておくことを推奨します。

マイグレーション以前の状態でプロジェクトのフルビルドを行い、ビルドが成功することを確認します。

..  code-block:: sh

    mvn clean package

また、ビルドが成功した状態のプロジェクトをバックアップするなど、マイグレーション作業で問題が発生した場合にすぐに元の状態に戻れるようにしておきます。

プロジェクトテンプレートの適用
------------------------------

Asakusa Gradle Pluginのプロジェクトテンプレートに含まれるファイル一式をMavenプロジェクトに適用します。

以下は、ダウンロードしたプロジェクトテンプレートを ``$HOME/workspace/migrate-app`` に適用する例です。

..  code-block:: sh

    cd ~/Downloads
    tar xf asakusa-*-template-*.tar.gz
    cd asakusa-*-template
    cp -a build.gradle gradlew gradlew.bat .buildtools ~/workspace/migrate-app

プロジェクト初期設定ファイルの適用
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

MavenプロジェクトとAsakusa Gradle Pluginのプロジェクトテンプレートの両方に含まれるプロジェクトの初期設定ファイルに対しては、以下のファイル内容を確認し、必要に応じてMavenプロジェクトに適用します。

MavenプロジェクトとAsakusa Gradle Pluginのプロジェクトテンプレートの両方に含まれるファイルの一覧を以下に示します。

..  list-table::
    :widths: 234 218
    :header-rows: 1

    * - ファイル
      - 説明
    * - :file:`src/test/resources/logback-test.xml`
      - ビルド/テスト実行時に使用されるログ定義ファイル

..  tip::
    Mavenプロジェクトで上記の設定ファイルをデフォルト設定のまま利用している場合は、Asakusa Gradle Pluginのプロジェクトテンプレートの内容で上書きすることを推奨します。

プロジェクト定義のマイグレーション
----------------------------------

Mavenプロジェクトのプロジェクト定義( :file:`pom.xml` )の内容をGradleのビルドスクリプト( :file:`build.gradle` )に反映します。

:file:`pom.xml` の代表的なカスタマイズ内容として、アプリケーションで利用するライブラリ追加による依存関係の設定があります。これは :file:`pom.xml` 上では ``dependencies`` 配下に定義していました。

Gradle、およびAsakusa Gradle Pluginでは従来のMavenベースの依存関係の管理から一部機能が変更になっているため、 :doc:`gradle-plugin` - :ref:`gradle-plugin-dependency-management` や :doc:`gradle-plugin-reference` などの内容を確認してアプリケーションに対して適切な設定を行ってください。

その他に確認すべき点は、標準プロジェクトプロパティの内容です。
これに相当する内容はMavenアーキタイプからプロジェクトを作成する際に入力した内容が :file:`pom.xml` のトップレベルの階層に定義されています。

以下、この箇所に該当する :file:`pom.xml` の設定例です。

..  code-block:: xml
    :caption: pom.xml
    :name: pom.xml-gradle-plugin-migration-guide-1

        <name>Example Application</name>
        <groupId>com.example</groupId>
        <artifactId>migrate-app</artifactId>
        <version>1.0-SNAPSHOT</version>

Gradleではこれらのプロパティについてビルドスクリプト上の定義は必須ではありませんが、必要に応じて :file:`pom.xml` の設定を反映するとよいでしょう。

ビルド定義ファイルのマイグレーション
------------------------------------

従来のMavenのビルド定義ファイル( :file:`build.properties` )の内容をGradleのビルドスクリプト( :file:`build.gradle` )に反映します。
ビルド定義ファイルの内容は、移行後の :file:`build.gradle` では Batch Application Plugin 上の規約プロパティとして定義します。

ここで必ず確認すべき項目は、Mavenアーキタイプでプロジェクトを作成した内容が反映される以下のプロパティです。

..  list-table::
    :widths: 113 113 113
    :header-rows: 1

    * - プロパティ
      - 対応するbuild.gradle上の設定項目
      - 説明
    * - ``asakusa.package.default``
      - ``asakusafw.compiler.compiledSourcePackage``
      - DSLコンパイラが生成する各クラスに使用されるパッケージ名
    * - ``asakusa.modelgen.package``
      - ``asakusafw.modelgen.modelgenSourcePackage``
      - データモデルクラスに使用されるパッケージ名

その他の項目については、 :file:`build.properties` をデフォルト値のまま利用している場合は移行作業は不要です。
変更しているものがある場合は :doc:`gradle-plugin` - :ref:`gradle-plugin-customize` や :doc:`gradle-plugin-reference` 上の規約プロパティを確認し、設定を反映してください。

Asakusa Frameworkの再インストール
---------------------------------

開発環境のAsakusa Frameworkを再インストールします。

..  code-block:: sh

    ./gradlew installAsakusafw

マイグレーションしたビルド設定の確認
------------------------------------

マイグレーション後の状態でプロジェクトのフルビルドを行い、ビルドが成功することを確認します。

..  code-block:: sh

    ./gradlew clean build

Eclipse定義ファイルの更新
-------------------------

Eclipseを利用している場合は、Eclipse用定義ファイルを更新します。

..  code-block:: sh

    ./gradlew cleanEclipse eclipse

Mavenビルド用ファイルの削除
---------------------------

Mavenプロジェクトのビルドで利用していた以下のファイル、ディレクトリを削除します。

*  :file:`pom.xml`
*  :file:`build.properties`
*  :file:`target`

Maven Framework Organizerのマイグレーション
===========================================

従来の Maven Framework Organizer [#]_ で提供していた機能は、Asakusa Gradle Plugin によって提供されます。
詳しくは :doc:`gradle-plugin` や :doc:`gradle-plugin-reference` の内容を確認してください。

..  [#] :doc:`../administration/framework-organizer`
