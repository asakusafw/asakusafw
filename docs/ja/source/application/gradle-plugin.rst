===============================
Asakusa Gradle Plugin利用ガイド
===============================

この文書では、GradleにAsakusa Framework を使ったアプリケーションの開発やデプロイを行うための機能を追加する Asakusa Gradle Plugin について説明します。

.. contents::
   :local:
   :depth: 2
   :backlinks: none

概要
====

Asakusa Gradle Plugin は、Asakusa Framework用の `Gradle <http://www.gradle.org/>`_ 拡張プラグインです。このプラグインを利用することで、Gradleを利用してAsakusa Framework を使ったアプリケーションの開発やデプロイを行うことができます。

利用環境
--------
Asakusa Gradle Plugin を利用するにはJava(JDK)がインストールされている必要があります。 これらの導入方法については、 :doc:`../introduction/start-guide` の :ref:`startguide-development-environment` などを参考にしてください。

なお、Gradleのインストールについては、 本書では `Gradleラッパー <http://www.gradle.org/docs/current/userguide/gradle_wrapper.html>`_  と呼ばれるGradleを利用するためのコマンドを使う方法を推奨しています。この方法に沿ってGradleを利用する場合は前もってGradleをインストールする必要はありません。詳しくは後述の `Asakusa Gradle Pluginの導入`_ を参考にしてください。

Gradleについて
--------------

`Gradle <http://www.gradle.org/>`_  は オープンソースプロジェクトとして開発されている様々なプラットフォームに対応したビルドシステムです。シンプルかつ拡張性の高いビルド定義を行うためのDSLやプラグイン機構を持ち、他のビルドシステムとの連携を含む様々な方式に対応した依存性管理のメカニズムを有しているなど多くの特徴を持っています。

Gradleに関する詳しい情報は、以下のドキュメントなどを参考にしてください。

*  `Gradle Documentation <http://www.gradle.org/documentation>`_  (Gradleの公式ドキュメントサイト)
*  `Gradle 日本語ドキュメント <http://gradle.monochromeroad.com/docs/>`_  (公式ドキュメントの翻訳サイト)

関連プロダクト
==============

Shafu
-----
Shafu (車夫) は、 Asakusa Framework のバッチアプリケーション開発をサポートするEclipseプラグインです。

* :jinrikisha:`Shafu - Asakusa Gradle Plug-in Helper for Eclipse - <shafu.html>`

Shafu は バッチアプリケーション開発に Asakusa Gradle Plugin を利用する際に、Eclipseから透過的にビルドツール上の操作を行えます。Shafuを使うことで、コマンドライン上でのビルドツールの操作が不要となり、Eclipse上でアプリケーション開発に必要なほとんどの作業を行うことができるようになります。

本書ではGradleを使った操作はコマンドライン上での利用手順として説明していますが、
Eclipseを使った開発を行う場合は Shafu を利用してEclipse上からGradleの操作を行うことも可能です。

Asakusa Gradle Pluginの導入
===========================

Asakusa Gradle Plugin を利用する方法として、以下のいずれかの方法があります。

#. Asakusa Gradle Plugin 用プロジェクトテンプレートを使用する
#. ビルドスクリプトに個別にプラグイン利用に必要な設定を定義する

1)は、Asakusa Gradle Pluginの利用設定が行われたビルドスクリプト、及び標準的なプロジェクトレイアウトを含むプロジェクトテンプレートを利用する方法です。Asakusa Gradle Pluginを使った標準的なアプリケーション開発環境を導入するにはこのテンプレートを使うと便利です。

このプロジェクトテンプレートには  `Gradleラッパー <http://www.gradle.org/docs/current/userguide/gradle_wrapper.html>`_  と呼ばれるGradleを利用するコマンドが含まれます。このコマンドを利用することで、Gradle自体の導入設定は不要となり、すぐにこのプロジェクト上で開発を始めることができます。

2)は、テンプレートを使用せずフルスクラッチでビルドスクリプトの定義やプロジェクトレイアウトを作成する方法です。この方法はGradleやAsakusa Gradle Pluginの利用に精通している必要がありますが、既存プロジェクトのマイグレーションなどで個別に設定を行う必要がある場合などでは、こちらの方法を検討してください。

以下では、1)のAsakusa Gradle Plugin 用プロジェクトテンプレートを利用した導入方法を解説します。 2)については、後述の  `Asakusa Gradle Plugin リファレンス`_  を参照してください。

プロジェクトテンプレートのダウンロード
--------------------------------------

プロジェクトテンプレートは、以下リンクからダウンロードします。

基本的なプロジェクトレイアウトのみを持つプロジェクトテンプレートのほか、これにサンプルアプリケーションのソースコード一式を加えたサンプルアプリケーションプロジェクトを公開しています。

Asakusa Gradle Plugin 用プロジェクトテンプレート
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

* `asakusa-project-template-0.7.2.tar.gz <http://www.asakusafw.com/download/gradle-plugin/asakusa-project-template-0.7.2.tar.gz>`_ 

Asakusa Gradle Plugin 用サンプルアプリケーションプロジェクト
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

* `asakusa-example-project-0.7.2.tar.gz <http://www.asakusafw.com/download/gradle-plugin/asakusa-example-project-0.7.2.tar.gz>`_ 

..  note::
    サンプルアプリケーションの内容や利用方法については、
    :doc:`../introduction/start-guide` の :ref:`startguide-running-example`  を参照してください。

プロジェクトの配置
------------------

ダウンロードしたアーカイブファイルを展開すると、プロジェクトテンプレート名をディレクトリ名に持つプロジェクトファイル一式が作成されます。このディレクトリ名は開発するアプリケーションを示す名前に変更して作業用ディレクトリに配置してください。

以降本書では、ビルドの流れを解説するためにサンプルアプリケーションプロジェクトを使って説明します。ここでは、ダウンロードしたサンプルアプリケーションプロジェクトを ``example-app`` というプロジェクト名で ``$HOME/workspace`` に配置したものとします。

..  code-block:: sh

    cd ~/Downloads
    tar xf asakusa-example-project-*.tar.gz
    mv asakusa-example-project ~/workspace/example-app

プロジェクトレイアウト
----------------------

プロジェクトテンプレートを使ったアプリケーションプロジェクトのディレクトリ構成とテンプレートに含まれるファイルについて説明します。

..  tip::
    以降に示すプロジェクトレイアウトのディレクトリパスやファイル名は、Gradleの各プラグインやAsakusa Gradle Pluginの設定により変更可能です。詳しくはGradleのドキュメントや  `Asakusa Gradle Plugin リファレンス`_  を参照してください。

プロジェクトルートディレクトリ
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

テンプレートから作成したプロジェクトディレクトリの直下には、以下のディレクトリ/ファイルが含まれます。

..  list-table:: プロジェクトレイアウト - プロジェクトルートディレクトリ
    :widths: 4 6
    :header-rows: 1

    * - ファイル/ディレクトリ
      - 説明
    * -  ``build.gradle`` 
      - Gradleビルドスクリプト
    * -  ``src`` 
      - プロジェクトのソースディレクトリ
    * -  ``build`` 
      - プロジェクトのビルドディレクトリ（ビルド時に生成）
    * -  ``gradlew`` 
      - Gradleラッパーコマンド (Unix)
    * -  ``gradlew.bat`` 
      - Gradleラッパーコマンド (Windows)
    * -  ``.buildtools``
      - Gradleラッパーライブラリ (Gradle Version: 2.0)

アプリケーション開発者は ``src`` ディレクトリ配下を編集することでアプリケーションを開発します。  ``build`` ディレクトリは ``src`` ディレクトリ配下のファイルをビルドすることで生成される成果物が配置されます。

``build`` ディレクトリ配下のファイルはビルドの度に初期化、再作成されるため ``build`` ディレクトリ配下のファイルは直接編集しないようにしてください。

GradleラッパーはGradleを使ったビルドを実行するために使用します。Gradleラッパーに関するディレクトリ及びファイルは、Gradleラッパー自体のマイグレーションを行う場合を除き編集しないようにしてください。

..  note::
    Gradleラッパーを使用せず、開発環境に対して個別にインストールしたGradleを使用することも出来ます。この場合、Asakusa Frameworkで未検証のバージョンのGradleを使用した場合に問題が発生する可能性があることに注意してください。本書ではGradleラッパーを使ってGradleに関する操作を説明しています。

ビルドスクリプト
~~~~~~~~~~~~~~~~

ビルドスクリプト( ``build.gradle`` )はプロジェクトのビルド設定を記述したGradle用のビルドスクリプトで、プロジェクトテンプレートに含まれるビルドスクリプトにはAsakusa Gradle Pluginを利用するための設定が記述されています。

:download:`build.gradle <gradle-attachment/build.gradle>`

..  literalinclude:: gradle-attachment/build.gradle
    :language: groovy

..  note::
    プロジェクトテンプレートに含まれるビルドスクリプトには、Asakusa Frameworkの外部連携機能としてDirect I/OとWindGateを利用するための構成が定義されています。

ソースディレクトリ
~~~~~~~~~~~~~~~~~~

プロジェクトのソースディレクトリは大きくアプリケーション本体のコードを配置する ``src/main`` ディレクトリと、アプリケーションのテスト用のコードを配置する ``src/test``  ディレクトリに分かれます。

それぞれのディレクトリ/ファイルの構成を以下に示します。

..  list-table:: プロジェクトレイアウト - src/main
    :widths: 4 6
    :header-rows: 1

    * - ファイル/ディレクトリ
      - 説明
    * -  ``src/main/java`` 
      - Asakusa DSLのソースディレクトリ [#]_
    * -  ``src/main/resources`` 
      - プロジェクトのリソースディレクトリ
    * -  ``src/main/dmdl`` 
      - DMDLスクリプトディレクトリ
    * -  ``src/main/libs`` 
      - プロジェクトの依存ライブラリディレクトリ [#]_

..  [#] ソースディレクトリ配下に配置するAsakusa DSLの推奨パッケージ名について、 :doc:`../dsl/start-guide` に記載されています。

..  [#] このディレクトリ内に **直接** 配置したライブラリファイル ( \*.jar ) のみ、バッチアプリケーション内でも利用可能です（サブディレクトリに配置したライブラリファイルは無視されます）。詳しくは、後述の  `アプリケーション用依存ライブラリの追加`_  を参照してください。

..  list-table:: プロジェクトレイアウト - src/test
    :widths: 4 6
    :header-rows: 1

    * - ファイル/ディレクトリ
      - 説明
    * -  ``src/test/java`` 
      - Asakusa DSLのテスト用ソースディレクトリ
    * -  ``src/test/resources`` 
      - プロジェクトのテスト用リソースディレクトリ
    * -  ``src/test/resources/logback-test.xml`` 
      - ビルド/テスト実行時に使用されるログ定義ファイル

ビルドディレクトリ
~~~~~~~~~~~~~~~~~~

プロジェクトのビルドディレクトリはGradleの各プラグインが提供するタスクの実行に対応したビルド成果物が作成されます。デフォルト設定のビルドディレクトリは ``build`` です。

ビルドディレクトリの主なディレクトリ/ファイルの構成を以下に示します [#]_

..  list-table:: プロジェクトレイアウト - build
    :widths: 113 113 113 113
    :header-rows: 1

    * - ファイル/ディレクトリ
      - プラグイン
      - タスク
      - 説明
    * -  ``generated-sources/modelgen`` 
      -  ``asakusafw`` 
      -  ``compileDMDL`` 
      - DMDLコンパイラによって生成されるデータモデルクラス用ソースディレクトリ
    * -  ``generated-sources/annotations`` 
      -  ``java`` 
      -  ``classes`` 
      - Operator DSLコンパイラによって生成される演算子実装クラス/演算子ファクトリクラス用ソースディレクトリ
    * -  ``excel`` 
      -  ``asakusafw`` 
      -  ``generateTestbook`` 
      - テストデータ定義シートを生成するディレクトリ
    * -  ``reports/tests`` 
      -  ``java`` 
      -  ``check`` 
      - テストレポートファイルを生成するディレクトリ
    * -  ``batchc`` 
      -  ``asakusafw`` 
      -  ``compileBatchapp`` 
      - バッチアプリケーション生成ディレクトリ
    * -  ``*-batchapp-*.jar`` 
      -  ``asakusafw`` 
      -  ``jarBatchapp`` 
      - バッチアプリケーションアーカイブファイル
    * -  ``asakusafw-*.tar.gz`` 
      -  ``asakusafw-organizer`` 
      -  ``assemble`` 
      - デプロイメントアーカイブファイル

..  [#] 各タスクが処理過程で生成するワークディレクトリについては割愛しています。また、ここで示すディレクトリ以外にも、実行するGradleのタスクによって様々なディレクトリが生成されます。これらの詳細についてはGradleの各プラグインのドキュメントなどを参照してください。

基本的なプラグインの使用方法
============================

ここでは、Asakusa Frameworkの開発の流れに沿ってAsakusa Gradle Plugin の基本的な使い方を紹介します。

以降の説明では、コマンドライン上のカレントディレクトリがサンプルアプリケーションを配置したディレクトリに設定されていることを前提とします。

..  code-block:: sh

    cd ~/workspace/example-app


開発用のAsakusa Frameworkインストール
-------------------------------------

Asakusa Frameworkを開発環境にインストールします。

Asakusa Frameworkを開発環境にインストールするには、インストールディレクトリパスを環境変数 ``ASAKUSA_HOME`` に定義した上で ``installAsakusafw`` タスクを実行します。

..  code-block:: sh

    ./gradlew installAsakusafw

このタスクは ``ASAKUSA_HOME`` のパス上に開発環境用の構成 [#]_ を持つAsakusa Frameworkをインストールします。

..  note::
    開発環境では、Asakusa DSLを使ってアプリケーションを記述するだけであればAsakusa Frameworkのインストールは不要ですが、テストドライバを使ってFlow DSL、Batch DSLのテストを行う場合や、YAESSを使ってローカル環境でバッチアプリケーションを実行する場合など、Hadoopを実際に動作させる機能については、Asakusa Frameworkをインストールする必要があります。

..  [#] 開発環境用の構成に関する定義は ``dev`` プロファイルにて定義されます。プロファイルについての詳細は後述の `プロファイルの管理`_ を参照してください。

データモデルクラスの生成
------------------------

DMDLスクリプトから演算子の実装で使用するデータモデルクラスを生成します。DMDLスクリプトの記述や配置方法については :doc:`../dmdl/index` を参照してください。

データモデルクラスを生成するには、 ``compileDMDL`` タスクを実行します。

..  code-block:: sh
    
    ./gradlew compileDMDL

このタスクはDMDLコンパイラを実行し、DMDLスクリプトディレクトリ( ``src/main/dmdl`` )配下のDMDLスクリプトからデータモデルクラスを データモデルクラス用ソースディレクトリ( ``build/generated-sources/modelgen`` )配下に生成します。

データモデルクラスに使われるJavaパッケージ名は、ビルドスクリプト( ``build.gradle`` )のプロパティ ``asakusafw.modelgen.modelgenSourcePackage`` [#]_ で指定します。プロジェクトテンプレートに含まれるビルドスクリプトの初期値は ``com.example.modelgen`` となっているので、アプリケーションが使用する適切なパッケージ名に変更してください。

**build.gradle**

..  literalinclude:: gradle-attachment/build.gradle
    :language: groovy
    :lines: 19-28

..  attention::
    DMDLスクリプトでモデル名を変更した後に ``compileDMDL`` タスクを実行した場合、モデル名を変更する前のデータモデルクラスが出力ディレクトリに残ります。 データモデルクラス用のソースディレクトリを初期化する場合、 ``cleanCompileDMDL`` タスクを合わせて実行します。

..  [#] ビルドスクリプト上のビルド設定方法について詳しくは、後述の `プラグイン規約プロパティ`_ を参照してください。

.. _batch-compile-gradle-plugin:

バッチコンパイルとデプロイメントアーカイブの生成
------------------------------------------------

Asakusa DSLで記述したバッチアプリケーションをアプリケーション運用環境（Hadoopクラスタなど）で実行するには、Asakusa DSLコンパイラ [#]_ を実行してバッチアプリケーション実行ファイルを作成します。そして生成した実行ファイルをAsakusa Framework本体の実行モジュールとあわせて運用環境にデプロイします。

Gradle Pluginでは運用環境にデプロイする実行モジュールを全て含む「デプロイメントアーカイブ」と呼ばれるパッケージファイルを生成することができます。

デプロイメントアーカイブの作成には、
Gradleの ``assemble`` タスクを実行します。

..  code-block:: sh
    
    ./gradlew assemble

``assemble`` タスクを実行すると、 ``build`` ディレクトリ配下にデプロイメントアーカイブをはじめ、
デプロイに関するいくつかのアーカイブファイルやディレクトリが生成されます。

``assemble`` タスクの主な生成物は以下の通りです。

..  list-table:: ``build`` ディレクトリに生成される ``assemble`` タスクの主な生成物
    :widths: 4 6
    :header-rows: 1

    * - ファイル/ディレクトリ [#]_
      - 説明
    * -  ``asakusafw-${asakusafwVersion}.tar.gz`` 
      - デプロイメントアーカイブファイル
    * -  ``${baseName}-batchapp-${version}.jar`` 
      - バッチアプリケーションアーカイブファイル
    * -  ``batchc`` 
      - バッチアプリケーション生成ディレクトリ

デプロイメントアーカイブは運用環境上の ``$ASAKUSA_HOME`` 配下に展開してデプロイします。
運用環境へのデプロイメントや ``assemble`` タスクの具体的な使用例については、
:doc:`../administration/deployment-guide` を参照してください。

..  attention::
    デプロイメントアーカイブ生成について、
    Asakusa Framework バージョン 0.6.x 以前から
    推奨となる機能と設定方法が変更になっています。
   
    0.6.x からのマイグレーションを検討する場合、
    :doc:`gradle-plugin-deprecated` も参照してください。 

バッチアプリケーションアーカイブファイルは
運用環境の ``$ASAKUSA_HOME/batchapps`` 配下にjarファイルを展開してデプロイします。
バッチアプリケーションの再デプロイのみを行うといった場合に使用します。

バッチアプリケーション生成ディレクトリにはバッチIDをディレクトリ名とした
バッチアプリケーション実行ファイル一式が生成されます。 
開発環境でバッチアプリケーション生成ディレクトリを ``$ASAKUSA_HOME/batchapp`` 配下に配置すれば
YAESS経由でアプリケーションの実行が可能となります [#]_ 。

..  [#] DSLコンパイラについての詳しい情報は :doc:`../dsl/user-guide` を参照してください。

..  [#] これらのファイル名やディレクトリの一部は設定により変更可能です。詳しくは `Asakusa Gradle Plugin リファレンス` を参照してください。

..  [#] サンプルアプリケーションの実行方法については、 :doc:`../directio/start-guide` などを参照してください。


アプリケーションのテスト
------------------------

Asakusa DSLとして記述したバッチアプリケーションに対して、テストロジックを実行してテストを行います。Asakusa DSLのテスト手法については、 :doc:`../testing/index` などを参照してください。

Asakusa DSLのテストを実行するには、 ``check`` タスクを実行します。

..  code-block:: sh
    
    ./gradlew check

``check`` タスクを実行すると、Asakusa DSLのテスト用ソースディレクトリ( ``src/test/java`` )に含まれる、JUnitかTestNGを使って記述されたテストクラスを自動的に検出し、これを実行します。

テストの実行結果は、 ``build/reports/tests`` 配下にHTML形式のテストレポートが生成されます。また、 ``build/test-results`` にはXML形式のテスト結果ファイルが生成されます。このXMLはCIサーバなどのツールと連携して使用することができます。

テストドライバの :doc:`../testing/using-excel` を使用したテストを記述する場合、  ``generateTestbook`` タスクを実行することでテストデータ定義シート（テストデータテンプレート）を生成することができます。

..  code-block:: sh

    ./gradlew generateTestbook

``generateTestbook`` タスクを実行すると、 ``build/excel`` 配下にDMDLで記述したデータモデルに対応するテストデータ定義シートが作成されます。

フルビルド
----------

``build`` タスクはプロジェクトのフルビルドを実行します。 実際には上記 ``assemble`` と  ``check``  タスクを実行します。CIサーバなどでリリースビルドを行うような場合に、このタスクを利用するとよいでしょう。

..  code-block:: sh

    ./gradlew clean build

..  hint::
    ``clean`` タスクはプロジェクトのビルドディレクトリ ( ``build`` )を初期化します。
    リリースビルドを行うような場合は合わせて実行するとよいでしょう。

Eclipse定義ファイルの作成
-------------------------

アプリケーション開発用の統合開発環境(IDE)にEclipseを使用する場合、開発環境にEclipseをインストールした上で、プロジェクトに対してEclipseプロジェクト用の定義ファイルを追加します。

Eclipseプロジェクト用の定義ファイルを作成するには、 ``eclipse`` タスクを実行します。

..  code-block:: sh

    ./gradlew eclipse

このコマンドを実行することによって、プロジェクトディレクトリに対してEclipseプロジェクト用の定義ファイルやEclipse上のクラスパスに対応したソースディレクトリなどが追加されます。これにより、Eclipseからプロジェクトをインポートすることが可能になります。

..  tip::
    Eclipseからプロジェクトをインポートするには、Eclipseのメニューから  ``[File]``  ->  ``[Import]``  ->  ``[General]``  ->  ``[Existing Projects into Workspace]``  を選択し、プロジェクトディレクトリを指定します。

.. _gradle-plugin-customize:

ビルド設定のカスタマイズ
========================

ビルド設定のカスタマイズは、基本的にはGradleのビルドスクリプトである  ``build.gradle`` を編集します。

以下は、いくつかの基本的なカスタマイズをおこなったビルドスクリプトの例です。

**build.gradle**

..  literalinclude:: gradle-attachment/custom-build.gradle
    :language: groovy

標準プロジェクトプロパティ
--------------------------

標準的なプロジェクト情報は、以下のように ビルドスクリプト のトップレベルの階層に定義します。

**build.gradle**

..  literalinclude:: gradle-attachment/custom-build.gradle
    :language: groovy
    :lines: 1-4

このうち、 ``version`` プロパティはアーカイブファイル名に付加されたり、バッチアプリケーションのコンパイル時のビルド情報ファイルに含まれたりするなど、様々な箇所で使用されます。

指定可能なプロパティ一覧についてはGradleのドキュメントを参照してください。

プラグイン規約プロパティ
------------------------

Asakusa Gradle Plugin固有の設定情報は、ビルドスクリプトの ``asakusafw`` ブロック内に指定します。

``asakusafw`` のブロックで定義できるプロパティをプラグイン規約プロパティと呼びます。 ``asakusafw`` ブロックは設定のカテゴリ別に階層化されています。

以下の例では、トップレベルの階層に プロジェクトで使用するAsakusa Frameworkのバージョンを示す ``asakusafwVersion`` が指定され、続いてデータモデルクラスの生成に関する ``modelgen`` ブロック、DSLコンパイルの設定に関する ``compiler`` ブロックが指定されています。ブロック内には複数のプロパティを指定することができます。

以下の例では、プロジェクトテンプレートのデフォルト設定に対して、モデルクラス名のパッケージ名の変更、DSLコンパイルオプションを指定するプロパティの追加を行っています。

**build.gradle**

..  literalinclude:: gradle-attachment/custom-build.gradle
    :language: groovy
    :lines: 24-35

依存ライブラリの管理
--------------------

アプリケーションのビルドで使用するライブラリの依存関係に関する設定は、ビルドスクリプトの ``dependencies`` ブロックに指定します。

**build.gradle**

..  literalinclude:: gradle-attachment/custom-build.gradle
    :language: groovy
    :lines: 43-56

上記の例では、Asakusa Framework のDirect I/O に TSVフォーマットのファイルを扱うための拡張機能である :doc:`../sandbox/directio-tsv` を利用するための設定を追加しています。

Direct I/O TSVはDMDLコンパイラの拡張のみを行う機能であるため、運用環境に対するランタイムライブラリの配置は不要です。アプリケーションの演算子で利用するライブラリを追加する場合は、運用環境にもこのライブラリを配置する必要があります。これについては、次項の  `アプリケーション用依存ライブラリの追加`_  で説明します。

.. _dependency-library-gradle-plugin:

アプリケーション用依存ライブラリの追加
--------------------------------------

バッチアプリケーションの演算子からHadoopが提供するライブラリ以外の共通ライブラリを使用する場合は、以下に示すいずれかの方法でアプリケーション用依存ライブラリを追加します。

プロジェクトの依存ライブラリディレクトリへjarファイルを配置
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

プロジェクトディレクトリの「依存ライブラリディレクトリ」( ``src/main/libs`` ) 配下にjarファイルを配置 [#]_ すると、Javaソースファイルのコンパイル時にこのライブラリが依存関係に追加され、さらにDSLコンパイルの結果バッチアプリケーションの実行ファイルに自動的に含まれるようになります。

..  tip::
    通常はこの方法でライブラリを追加することを推奨します。

..  [#]  ``src/main/libs`` ディレクトリの直下に配置したjarファイルのみ有効です。サブディレクトリを作成してその中にjarファイルを配置してもそのファイルは無視されます。

Asakusaの拡張ライブラリディレクトリへjarファイルを配置
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

バッチアプリケーションの実行時に依存ライブラリを利用するもう一つの方法は、Asakusa Framework全体の「拡張ライブラリディレクトリ」( ``$ASAKUSA_HOME/ext/lib`` )に対象のjarファイルを直接配置してしまうことです。 拡張ライブラリディレクトリに追加したjarファイルは、実行時に全てのバッチアプリケーションから参照できます。

この場合、Javaソースのコンパイル時にはこのライブラリは参照されないため、ビルドスクリプトの ``dependencies`` ブロックにも依存関係の追加を行う必要があることに注意してください。

Eclipse設定の更新
~~~~~~~~~~~~~~~~~

Eclipseを使用している場合は、上記の方法で依存ライブラリを追加した後に、Eclipseプロジェクト上のクラスパス設定を更新する必要があります。以下は更新の実行例です。

..  code-block:: sh

    ./gradlew cleanEclipse eclipse

.. _asakusa-gradle-plugin-reference:

Asakusa Gradle Plugin リファレンス
==================================

Asakusa Gradle Pluginが提供する機能とインターフェースについて個々に解説します。

Asakusa Gradle Plugin 一覧
--------------------------

Asakusa Gradle Pluginはいくつかのプラグインから構成されています。以下にその一覧を示します。

..  list-table:: Asakusa Gradle Plugin 一覧
    :widths: 110 87 76 86 92
    :header-rows: 1

    * - プラグインID
      - プラグイン名
      - 自動適用
      - 協調して動作
      - 説明
    * -  ``asakusafw`` 
      -  Batch Application Plugin
      -  ``java`` 
      -  ``eclipse`` , ``idea``
      - Asakusa Framework の バッチアプリケーションを開発を行うための支援機能をプロジェクトに追加する。
    * -  ``asakusafw-organizer`` 
      -  Framework Organizer Plugin
      -  ``-`` 
      -  ``asakusafw`` 
      - Asakusa Framework を 利用した開発環境の構築や、運用環境へのデプロイを行うための援機能を提供する。

Batch Application Plugin
------------------------

Batch Application Plugin は、Asakusa Framework の バッチアプリケーション開発を行うための支援機能を提供します。

Batch Application Plugin はAsakusa Framework の バッチアプリケーションプロジェクトに対して、以下のような機能を提供します。

* DMDLスクリプト から モデルクラスを生成するタスクの提供
* Gradle標準のJavaコンパイルタスクに対して、Operator DSLコンパイラによる演算子実装クラス、演算子ファクトリクラスの生成を行うための設定を追加
* Asakusa DSLとして記述したJavaソースファイル一式に対して、Batch DSLコンパイラによるバッチアプリケーション実行モジュールの生成を行うタスクの提供
* テストドライバを利用したテストケースを作成するためのテストデータ定義シートのテンプレートファイルを生成するタスクの提供
* Gradle標準のEclipseのタスクに対して、Asakusa Framework用の設定を追加

..  tip::
    このプラグインはGradleが提供するJavaプラグインやEclipseプラグインを拡張して作成されています。

使用方法
~~~~~~~~

Batch Application Plugin [#]_ を使うためには、ビルドスクリプトに下記を含めます：

..  code-block:: groovy

    apply plugin: 'asakusafw'

..  [#] :gradledoc:`com.asakusafw.gradle.plugins.AsakusafwPlugin`

タスク
~~~~~~

Batch Application Plugin は、以下のタスクをプロジェクトに追加します。

..  list-table:: Batch Application Plugin - タスク
    :widths: 113 63 113 163
    :header-rows: 1

    * - タスク名
      - 依存先
      - 型
      - 説明
    * -  ``compileDMDL``
      -  ``-`` [#]_
      - ``CompileDmdlTask`` [#]_
      - DMDLコンパイラを使ってモデルクラスを生成する
    * -  ``compileBatchapp`` 
      -  ``compileJava, processResources`` 
      - ``CompileBatchappTask`` [#]_
      - DSLコンパイラを使ってバッチアプリケーションを生成する
    * -  ``jarBatchapp`` 
      -  ``compileBatchapp`` 
      - ``Jar``
      - バッチアプリケーションアーカイブを生成する
    * -  ``generateTestbook`` 
      -  ``-`` 
      - ``GenerateTestbookTask`` [#]_
      - テストデータ定義シートを生成する
    * -  ``generateThunderGateDataModel`` 
      -  ``-`` 
      - ``GenerateThunderGateDataModelTask`` [#]_
      - ThunderGate用のMySQLメタデータからDMDLスクリプトを生成する
    * -  ``testRunBatchapp`` 
      -  ``-`` 
      - ``RunBatchappTask`` [#]_
      - バッチテストランナーを実行する
    * -  ``summarizeYaessJob`` 
      -  ``-`` 
      - ``AnalyzeYaessLogTask`` [#]_
      - YAESS Log Analyzerを実行する [#]_
    * -  ``generateHiveDDL`` 
      -  ``-`` 
      - ``GenerateHiveDdlTask`` [#]_
      - DMDLからHive用のDDLファイルを生成する

..  [#] ThunderGateの設定を有効にした場合、 ``generateThunderGateDataModel`` タスクが依存先に追加されます
..  [#] :gradledoc:`com.asakusafw.gradle.tasks.CompileDmdlTask`
..  [#] :gradledoc:`com.asakusafw.gradle.tasks.CompileBatchappTask`
..  [#] :gradledoc:`com.asakusafw.gradle.tasks.GenerateTestbookTask`
..  [#] :gradledoc:`com.asakusafw.gradle.tasks.GenerateThunderGateDataModelTask`
..  [#] :gradledoc:`com.asakusafw.gradle.tasks.RunBatchappTask`
..  [#] :gradledoc:`com.asakusafw.gradle.tasks.AnalyzeYaessLogTask`
..  [#] YAESS Log Analyzerやその使い方については、 :doc:`yaess-log-visualization` を参照してください。
..  [#] :gradledoc:`com.asakusafw.gradle.tasks.GenerateHiveDdlTask`

またBatch Application Plugin は、自動適用される以下のタスクに対してタスク依存関係を追加します。

..  list-table:: Batch Application Plugin - タスク依存関係
    :widths: 113 113 113 113
    :header-rows: 1

    * - タスク名
      - 依存先
      - 型
      - 説明
    * -  ``compileJava`` 
      -  ``compileDMDL`` 
      - ``JavaCompile``
      - Javaソースファイルをコンパイルする
    * -  ``assemble`` 
      -  ``jarBatchapp`` 
      - ``Task``
      - プロジェクトのすべてのアーカイブを構築する

依存関係の管理
~~~~~~~~~~~~~~

Batch Application Plugin は、以下の依存関係設定をプロジェクトに追加します。

..  list-table:: Batch Application Plugin - 依存関係設定
    :widths: 110 341
    :header-rows: 1

    * - 名前
      - 説明
    * - ``provided``
      - アプリケーションのビルド時に依存するが、アプリケーションの実行時には実行環境に配置されている実体(jarファイルなど)を使用する依存関係設定
    * - ``embedded``
      - プロジェクトディレクトリ配下に実体(jarファイルなど)を配置する依存関係設定

リポジトリ
~~~~~~~~~~

Batch Application Plugin は、以下のリポジトリをプロジェクトに追加します。

..  list-table:: Batch Application Plugin - リポジトリ
    :widths: 286 166
    :header-rows: 1

    * - 名前/URL
      - 説明
    * - http://repo1.maven.org/maven2/
      - Mavenのセントラルリポジトリ
    * - http://asakusafw.s3.amazonaws.com/maven/releases
      - Asakusa Frameworkのリリース用Mavenリポジトリ
    * - http://asakusafw.s3.amazonaws.com/maven/snapshots
      - Asakusa Frameworkのスナップショット用Mavenリポジトリ

..  tip::
    プロジェクトに固有のリポジトリを追加する場合、
    ビルドスクリプトのプラグイン定義 ( ``apply plugin: 'asakusafw'`` ) 位置の前に
    リポジトリ定義を追加すると、
    プラグインが標準で設定するリポジトリよりも優先して使用されます。
    
    開発環境でインハウスリポジトリを優先して利用したい場合などは、
    プラグイン定義の前にリポジトリ定義を追加するとよいでしょう。

規約プロパティ
~~~~~~~~~~~~~~

Batch Application Plugin の規約プロパティはビルドスクリプトから 参照名 ``asakusafw`` でアクセスできます [#]_ 。この規約オブジェクトは以下のプロパティを持ちます。

..  list-table:: Batch Application Plugin - 規約プロパティ ( ``asakusafw`` ブロック )
    :widths: 2 1 2 5
    :header-rows: 1

    * - プロパティ名
      - 型
      - デフォルト値
      - 説明
    * -  ``asakusafwVersion`` 
      - String
      -  ``未定義`` 
      - プロジェクトが使用するAsakusa Frameworkのバージョン
    * -  ``maxHeapSize`` 
      - String
      -  ``1024m`` 
      - プラグインが実行するJavaプロセスの最大ヒープサイズ
    * -  ``logbackConf`` 
      - String
      -  ``src/${project.sourceSets.test.name}/resources/logback-test.xml`` 
      - プロジェクトのLogback設定ファイル [#]_
    * -  ``basePackage`` 
      - String
      -  ``${project.group}`` 
      - プラグインの各タスクでJavaソースコードの生成時に指定する基底Javaパッケージ

..  [#] これらのプロパティは規約オブジェクト :gradledoc:`com.asakusafw.gradle.plugins.AsakusafwPluginConvention` が提供します。
..  [#] Logback設定ファイルの詳細は次のドキュメントを参照してください: http://logback.qos.ch/manual/configuration.html

DMDLプロパティ
^^^^^^^^^^^^^^

DMDLに関する規約プロパティは、 ``asakusafw`` ブロック内の参照名 ``dmdl`` でアクセスできます [#]_ 。この規約オブジェクトは以下のプロパティを持ちます。

..  list-table:: Batch Application Plugin - DMDLプロパティ ( ``dmdl`` ブロック )
    :widths: 2 1 2 5
    :header-rows: 1

    * - プロパティ名
      - 型
      - デフォルト値
      - 説明
    * -  ``dmdlEncoding`` 
      - String
      -  ``UTF-8`` 
      - DMDLスクリプトのエンコーディング
    * -  ``dmdlSourceDirectory`` 
      - String
      -  ``src/${project.sourceSets.main.name}/dmdl`` 
      - DMDLスクリプトのソースディレクトリ

..  [#] これらのプロパティは規約オブジェクト :gradledoc:`com.asakusafw.gradle.plugins.AsakusafwPluginConvention.DmdlConfiguration` が提供します。

データモデル生成プロパティ
^^^^^^^^^^^^^^^^^^^^^^^^^^

データモデル生成に関する規約プロパティは、 ``asakusafw`` ブロック内の参照名 ``modelgen`` でアクセスできます [#]_ 。この規約オブジェクトは以下のプロパティを持ちます。

..  list-table:: Batch Application Plugin - データモデル生成プロパティ ( ``modelgen`` ブロック )
    :widths: 2 1 2 5
    :header-rows: 1

    * - プロパティ名
      - 型
      - デフォルト値
      - 説明
    * -  ``modelgenSourcePackage`` 
      - String
      -  ``${asakusafw.basePackage}.modelgen`` 
      - データモデルクラスに使用されるパッケージ名
    * -  ``modelgenSourceDirectory`` 
      - String
      -  ``${project.buildDir}/generated-sources/modelgen`` 
      - データモデルクラスのソースディレクトリ

..  [#] これらのプロパティは規約オブジェクト :gradledoc:`com.asakusafw.gradle.plugins.AsakusafwPluginConvention.ModelgenConfiguration` が提供します。

Javaコンパイラプロパティ
^^^^^^^^^^^^^^^^^^^^^^^^

Javaコンパイラ関する規約プロパティは、 ``asakusafw`` ブロック内の参照名 ``javac`` でアクセスできます [#]_ 。この規約オブジェクトは以下のプロパティを持ちます。

..  list-table:: Batch Application Plugin - Javaコンパイラプロパティ ( ``javac`` ブロック )
    :widths: 2 1 2 5
    :header-rows: 1

    * - プロパティ名
      - 型
      - デフォルト値
      - 説明
    * -  ``annotationSourceDirectory`` 
      - String
      -  ``${project.buildDir}/generated-sources/annotations`` 
      - アノテーションプロセッサが生成するJavaソースの出力先
    * -  ``sourceEncoding`` 
      - String
      -  ``UTF-8`` 
      - プロジェクトのソースファイルのエンコーディング
    * -  ``sourceCompatibility`` 
      - JavaVersion。Stringも利用可能。 例： ``'1.7'`` [#]_
      -  ``1.7`` 
      - Javaソースのコンパイル時に使用するJavaバージョン互換性
    * -  ``targetCompatibility`` 
      - JavaVersion。Stringも利用可能。例： ``'1.7'``
      -  ``1.7`` 
      - クラス生成のターゲットJavaバージョン

..  [#] これらのプロパティは規約オブジェクト :gradledoc:`com.asakusafw.gradle.plugins.AsakusafwPluginConvention.JavacConfiguration` が提供します。
..  [#] JDK6Iを利用するなどの場合に変更します。 詳しくは :doc:`using-jdk` を参照してください。

DSLコンパイラプロパティ
^^^^^^^^^^^^^^^^^^^^^^^

DSLコンパイラ関する規約プロパティは、 ``asakusafw`` ブロック内の参照名 ``compiler`` でアクセスできます [#]_ 。この規約オブジェクトは以下のプロパティを持ちます。

..  list-table:: Batch Application Plugin - DSLコンパイラプロパティ ( ``compiler`` ブロック )
    :widths: 2 1 2 5
    :header-rows: 1

    * - プロパティ名
      - 型
      - デフォルト値
      - 説明
    * -  ``compiledSourcePackage`` 
      - String
      -  ``${asakusafw.basePackage}.batchapp`` 
      - DSLコンパイラが生成する各クラスに使用されるパッケージ名
    * -  ``compiledSourceDirectory`` 
      - String
      -  ``${project.buildDir}/batchc`` 
      - DSLコンパイラが生成する成果物の出力先
    * -  ``compilerOptions`` 
      - String
      -  ``未指定`` 
      - DSLコンパイラオプション
    * -  ``compilerWorkDirectory`` 
      - String
      -  ``未指定`` 
      - DSLコンパイラのワーキングディレクトリ
    * -  ``hadoopWorkDirectory`` 
      - String
      -  ``target/hadoopwork/${execution_id}`` 
      - DSLコンパイラが生成するアプリケーション(Hadoopジョブ)が使用するHadoop上のワーキングディレクトリ

..  [#] これらのプロパティは規約オブジェクト :gradledoc:`com.asakusafw.gradle.plugins.AsakusafwPluginConvention.CompilerConfiguration` が提供します。

テストツールプロパティ
^^^^^^^^^^^^^^^^^^^^^^

テストツールに関する規約プロパティは、 ``asakusafw`` ブロック内の参照名 ``testtools`` でアクセスできます [#]_ 。この規約オブジェクトは以下のプロパティを持ちます。

..  list-table:: Batch Application Plugin - テストツールプロパティ ( ``testtools`` ブロック )
    :widths: 2 1 2 5
    :header-rows: 1

    * - プロパティ名
      - 型
      - デフォルト値
      - 説明
    * -  ``testDataSheetFormat`` 
      - String
      -  ``ALL`` 
      - テストデータ定義シートのフォーマット [#]_
    * -  ``testDataSheetDirectory`` 
      - String
      -  ``${project.buildDir}/excel`` 
      - テストデータ定義シートの出力先

..  [#] これらのプロパティは規約オブジェクト :gradledoc:`com.asakusafw.gradle.plugins.AsakusafwPluginConvention.TestToolsConfiguration` が提供します。
..  [#] テストデータ定義シートのフォーマット指定値は、 :doc:`../testing/using-excel` の :ref:`testdata-generator-excel-format` を参照してください。

ThunderGateプロパティ
^^^^^^^^^^^^^^^^^^^^^

ThunderGateに関する規約プロパティは、 ``asakusafw`` ブロック内の参照名 ``thundergate`` でアクセスできます [#]_ 。この規約オブジェクトは以下のプロパティを持ちます。

..  list-table:: Batch Application Plugin - ThunderGateプロパティ ( ``thundergate`` ブロック ) 
    :widths: 2 1 2 5
    :header-rows: 1

    * - プロパティ名
      - 型
      - デフォルト値
      - 説明
    * -  ``target`` 
      - String
      -  ``未指定`` 
      - ThunderGateのターゲット。この値をセットすることでThunderGate用のビルド設定が有効になる [#]_
    * -  ``jdbcFile`` 
      - String
      -  ``未指定`` 
      - ``generateThunderGateDataModel`` タスクの実行時に使用するJDBC接続設定ファイルのパス。この値をセットすることでThunderGate用のビルド設定が有効になる [#]_
    * -  ``ddlEncoding`` 
      - String
      -  ``未指定`` 
      - MySQLメタデータ登録用DDLファイルのエンコーディング
    * -  ``ddlSourceDirectory`` 
      - String
      -  ``src/${project.sourceSets.main.name}/sql/modelgen`` 
      - MySQLメタデータ登録用DDLファイルのソースディレクトリ
    * -  ``includes`` 
      - String
      -  ``未指定`` 
      - モデルジェネレータ、およびテストデータテンプレート生成ツールが生成対象とするモデル名を正規表現の書式で指定
    * -  ``excludes`` 
      - String
      -  ``未指定`` 
      - モデルジェネレータ、およびテストデータテンプレート生成ツールが生成対象外とするモデル名を正規表現の書式で指定
    * -  ``dmdlOutputDirectory`` 
      - String
      -  ``${project.buildDir}/thundergate/dmdl`` 
      - MySQLメタデータから生成されるDMDLスクリプトの出力先
    * -  ``ddlOutputDirectory`` 
      - String
      -  ``${project.buildDir}/thundergate/sql`` 
      - ThunderGate管理テーブル用DDLスクリプトの出力先
    * -  ``sidColumn`` 
      - String
      -  ``SID`` 
      - ThunderGateが入出力を行う業務テーブルのシステムIDカラム名
    * -  ``timestampColumn`` 
      - String
      -  ``UPDT_DATETIME`` 
      - ThunderGateが入出力を行う業務テーブルの更新日時カラム名
    * -  ``deleteColumn`` 
      - String
      -  ``DELETE_FLAG`` 
      - ThunderGateが入出力を行う論理削除フラグカラム名
    * -  ``deleteValue`` 
      - String
      -  ``'1'`` 
      - ThunderGateが入出力を行う業務テーブルの論理削除フラグが削除されたことを示す値

..  [#] これらのプロパティは規約オブジェクト :gradledoc:`com.asakusafw.gradle.plugins.AsakusafwPluginConvention.ThunderGateConfiguration` が提供します。

..  [#] この設定を利用する場合、タスク実行時にAsakusa Frameworkがインストール済みとなっている必要があります。または ``jdbcFile`` をプロパティを設定することで、インストールを行わない状態でタスクが実行できるようになります。

..  [#] ``target`` プロパティを同時に有効にした場合、 ``jdbcFile`` プロパティが優先されます。

Eclipse Pluginの拡張
~~~~~~~~~~~~~~~~~~~~
Batch Application Plugin は Gradleが提供するEclipse Pluginが提供するタスクに対して、以下のようなEclipseプロジェクトの追加設定を行います。

* OperatorDSLコンパイラを実行するためのAnnotation Processorの設定
* Javaのバージョンやエンコーディングに関する設定

また、Batch Application Pluginが設定する規約プロパティの情報を ``.settings/com.asakusafw.asakusafw.prefs`` に出力します。

Idea Pluginの拡張
~~~~~~~~~~~~~~~~~

..  attention::
    Asakusa Framework バージョン |version| では、 Idea Pluginの拡張は試験的機能として提供しています。

Batch Application Plugin は Gradleが提供するIdea Pluginが提供するタスクに対して、以下のようなEclipseプロジェクトの追加設定を行います。

* OperatorDSLコンパイラを実行するためのAnnotation Processorの設定
* Javaのバージョンやエンコーディングに関する設定

バッチテストランナーの実行
~~~~~~~~~~~~~~~~~~~~~~~~~~
``testRunBatchapp`` タスクはインテグレーションテスト用のテストAPIであるバッチテストランナー [#]_ をGradleタスクとして実行することができます。

..  attention::
    Asakusa Frameworkのバージョン |version| では、 ``testRunBatchapp`` タスクは試験的機能として提供しています。

``testRunBatchapp`` タスクは ``gradlew`` コマンド実行時に以下のコマンドライン引数を指定します。

``--id batch-id``
  実行するバッチアプリケーションのバッチID
  
``--arguments key1=value1 [,key2=value2]``
  バッチ引数を ``key=value`` 形式で指定。複数のバッチ引数がある場合はカンマ区切りで指定 ( ``key1=value1,key2=value2`` )。

``testRunBatchapp`` タスクの実行例は以下の通りです。

..  code-block:: sh
    
    ./gradlew testRunBatchapp --id example.summarizeSales --arguments date=2011-04-01

..  [#] バッチテストランナーの詳細は :doc:`../testing/user-guide` の :ref:`testing-userguide-integration-test` を参照してください。
    
.. _gradle-plugin-task-hiveddl:

Hive用DDLファイルの生成
~~~~~~~~~~~~~~~~~~~~~~~
``generateHiveDDL`` は Hive連携用の拡張属性 [#]_ を持つDMDLスクリプトからをHive用のDDLファイルを生成します。
 
``generateHiveDDL`` タスクを実行すると、プロジェクトの ``build/hive-ddl``
ディレクトリ配下にHiveのテーブル作成用の ``CREATE TABLE`` 文を含むSQLファイルが生成されます。

``generateHiveDDL`` タスクは ``gradlew`` コマンド実行時に以下のコマンドラインオプションを指定することができます。

``--location /path/to/base-location``
  生成する ``CREATE TABLE`` 文に ``LOCATION`` (テーブルに対応するファイルを配置するHDFS上のパス) を追加する。
  このオプションを指定すると、 ``LOCATION`` の値に ``'<指定したパス>/<table-name>'`` が設定される。

  指定がない場合は ``LOCATION`` 句は未指定
  
``--database-name database-name``
  生成する ``CRATE TABLE`` 文のテーブル名の前にデータベース名を付与する

  指定がない場合は データベース名は未指定

``--include regex-table-name-pattern``
  指定した正規表現にマッチするテーブルに対してのみDDLを生成

  指定がない場合はすべてのテーブルに対してDDLを生成する

``--output /path/to/ddloutput``
  指定した出力先のパスにDDLファイルを生成する

  指定がない場合のファイルパスは ``${project.buildDir}/hive-ddl/${project.name}.sql``

``generateHiveDDL`` タスクの実行例は以下の通りです。

..  code-block:: sh
    
    ./gradlew generateHiveDDL --location /home/hadoop/target/testing/directio/tables --include item


..  [#] Hiveとの連携については、 :doc:`../directio/using-hive` を参照してください。

Framework Organizer Plugin
--------------------------

Framework Organizer Plugin は、Asakusa Framework を 利用した開発環境の構築や、
運用環境に対するデプロイモジュールの構成管理機能を提供します。

Framework Organizer Plugin が提供する機能には次のようなものがあります。

* Aasakusa Frameworkのデプロイメントモジュールの構成を定義し、デプロイメントアーカイブを生成するタスクの提供。
* Asakusa Frameworkを開発環境へインストールするタスクの提供。

使用方法
~~~~~~~~

Framework Organizer Plugin [#]_ を使うためには、ビルドスクリプトに下記を含めます：

..  code-block:: groovy

    apply plugin: 'asakusafw-organizer'

..  [#] :gradledoc:`com.asakusafw.gradle.plugins.AsakusafwOrganizerPlugin`

タスク
~~~~~~

Framework Organizer Plugin は、以下のタスクを定義します。

..  list-table:: Framework Organizer Plugin - タスク
    :widths: 152 121 48 131
    :header-rows: 1

    * - タスク名
      - 依存先
      - 型
      - 説明
    * -  ``assembleAsakusafw`` 
      -  ``-``
      - ``Task``
      - 運用環境向けのデプロイメント構成を持つデプロイメントアーカイブを生成する
    * -  ``installAsakusafw`` 
      -  ``-`` 
      - ``Task``
      - 開発環境向けのデプロイメント構成をローカル環境にインストールする [#]_

..  attention::
    Framework Organizer Pluginが提供するタスクにおいて
    Asakusa Framework バージョン 0.6.x から
    非推奨となったタスク、削除されたタスクがあります。
    
    0.6.x からのマイグレーションを検討する場合、
    :doc:`gradle-plugin-deprecated` も参照してください。 

..  attention::
    Framework Organizer Pluginは上記のタスク一覧の他に、
    プラグイン内部で ``attach`` から始まるタスクを生成し利用します。

..  [#]  ``installAsakusafw`` タスクを利用するには環境変数 ``ASAKUSA_HOME`` が設定されている必要があります。

リポジトリ
~~~~~~~~~~

Framework Organizer Plugin は、
`Batch Application Plugin`_ のリポジトリ定義と
共通の設定を使用します。

..  tip::
    `Batch Application Plugin`_ と同様に、
    プロジェクトに固有のリポジトリを追加する場合、
    ビルドスクリプトのプラグイン定義 ( ``apply plugin: 'asakusafw'`` ) 位置の前に
    リポジトリ定義を追加すると、
    プラグインが標準で設定するリポジトリよりも優先して使用されます。

規約プロパティ
~~~~~~~~~~~~~~

Framework Organizer Plugin の規約プロパティはビルドスクリプトから 参照名  ``asakusafwOrganizer`` でアクセスできます [#]_ 。この規約オブジェクトは以下のプロパティを持ちます。

..  list-table:: Framework Organizer Plugin - 規約プロパティ
    :widths: 135 102 101 113
    :header-rows: 1

    * - プロパティ名
      - 型
      - デフォルト値
      - 説明
    * -  ``asakusafwVersion`` 
      - String
      -  ``未定義`` 
      - デプロイメント構成に含むAsakusa Frameworkのバージョン
    * -  ``assembleDir`` 
      - String
      -  ``${project.buildDir}/asakusafw-assembly`` 
      - デプロイメント構成の構築時に利用するワーキングディレクトリのプレフィックス

..  [#] これらのプロパティは規約オブジェクト :gradledoc:`com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention` が提供します。

バッチアプリケーションプロパティ
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

バッチアプリケーションの構成に関する規約プロパティは、 ``asakusafwOrganizer`` ブロック内の参照名 ``batchapps`` でアクセスできます [#]_ 。この規約オブジェクトは以下のプロパティを持ちます。

..  list-table:: Framework Organizer Plugin - バッチアプリケーションプロパティ ( ``batchapps`` ブロック )
    :widths: 2 1 2 5
    :header-rows: 1

    * - プロパティ名
      - 型
      - デフォルト値
      - 説明
    * -  ``enabled``
      - boolean
      - true
      - この値をtrueにするとデプロイメントアーカイブにプロジェクトのバッチアプリケーションを含める

..  [#] これらのプロパティは規約オブジェクト :gradledoc:`com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.BatchappsConfiguration` が提供します。

Direct I/Oプロパティ
^^^^^^^^^^^^^^^^^^^^

Direct I/Oの構成に関する規約プロパティは、 ``asakusafwOrganizer`` ブロック内の参照名 ``directio`` でアクセスできます [#]_ 。この規約オブジェクトは以下のプロパティを持ちます。

..  list-table:: Framework Organizer Plugin - Direct I/Oプロパティ ( ``directio`` ブロック )
    :widths: 2 1 2 5
    :header-rows: 1

    * - プロパティ名
      - 型
      - デフォルト値
      - 説明
    * -  ``enabled``
      - boolean
      - true
      - この値をtrueにするとDirect I/O用の構成を行う

..  [#] これらのプロパティは規約オブジェクト :gradledoc:`com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.DirectIoConfiguration` が提供します。

.. _gradle-plugin-oraganizer-hive:

Hiveプロパティ
^^^^^^^^^^^^^^

Direct I/O Hiveの構成に関する規約プロパティは、 ``asakusafwOrganizer`` ブロック内の参照名 ``hive`` でアクセスできます [#]_ 。この規約オブジェクトは以下のプロパティを持ちます。

..  list-table:: Framework Organizer Plugin - Hiveプロパティ ( ``hive`` ブロック )
    :widths: 2 1 2 5
    :header-rows: 1

    * - プロパティ名
      - 型
      - デフォルト値
      - 説明
    * -  ``enabled``
      - boolean
      - false
      - この値をtrueにすると Direct I/O Hive連携モジュール用の構成を行う
    * -  ``libraries``
      - java.util.List
      - ``org.apache.hive:hive-exec:0.13.1``
      - Directi I/O Hiveが実行時に使用するHiveライブラリ

..  [#] これらのプロパティは規約オブジェクト :gradledoc:`com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.HiveConfiguration` が提供します。

テストドライバプロパティ
^^^^^^^^^^^^^^^^^^^^^^^^

テストモジュール用の構成に関する規約プロパティは、 ``asakusafwOrganizer`` ブロック内の参照名 ``testing`` でアクセスできます [#]_ 。この規約オブジェクトは以下のプロパティを持ちます。

..  list-table:: Framework Organizer Plugin - テストモジュールプロパティ ( ``testing`` ブロック )
    :widths: 2 1 2 5
    :header-rows: 1

    * - プロパティ名
      - 型
      - デフォルト値
      - 説明
    * -  ``enabled``
      - boolean
      - false
      - この値をtrueにするとテストモジュール用の構成を行う

..  [#] これらのプロパティは規約オブジェクト :gradledoc:`com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.TestingConfiguration` が提供します。

ThunderGateプロパティ
^^^^^^^^^^^^^^^^^^^^^

ThunderGateの構成に関する規約プロパティは、 ``asakusafwOrganizer`` ブロック内の参照名 ``thundergate`` でアクセスできます [#]_ 。この規約オブジェクトは以下のプロパティを持ちます。

..  list-table:: Framework Organizer Plugin - ThunderGateプロパティ ( ``thundergate`` ブロック )
    :widths: 2 1 2 5
    :header-rows: 1

    * - プロパティ名
      - 型
      - デフォルト値
      - 説明
    * -  ``enabled``
      - boolean
      - false
      - この値をtrueにするとThunderGate用の構成を行う
    * -  ``target`` 
      - String
      -  ``未指定`` 
      - デプロイメントアーカイブに含める既定のThunderGateのターゲット名。

..  [#] これらのプロパティは規約オブジェクト :gradledoc:`com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.ThunderGateConfiguration` が提供します。

WindGateプロパティ
^^^^^^^^^^^^^^^^^^

WindGateの構成に関する規約プロパティは、 ``asakusafwOrganizer`` ブロック内の参照名 ``windgate`` でアクセスできます [#]_ 。この規約オブジェクトは以下のプロパティを持ちます。

..  list-table:: Framework Organizer Plugin - WindGateプロパティ ( ``windgate`` ブロック )
    :widths: 2 1 2 5
    :header-rows: 1

    * - プロパティ名
      - 型
      - デフォルト値
      - 説明
    * -  ``enabled``
      - boolean
      - true
      - この値をtrueにするとWindGate用の構成を行う
    * -  ``retryableEnabled`` 
      - boolean
      - false
      - この値をtrueにするとWindGateプラグイン ``asakusa-windgate-retryable`` を追加する [#]_
    * -  ``sshEnabled`` 
      - boolean
      - true
      - この値をtrueにするとHadoopブリッジ ( ``windgate-ssh`` ) を追加する [#]_

..  [#] これらのプロパティは規約オブジェクト :gradledoc:`com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.WindGateConfiguration` が提供します。

..  [#] 詳しくは :doc:`../windgate/user-guide` の :ref:`windgate-userguide-retryable-plugin` を参照してください。

..  [#] 詳しくは :doc:`../windgate/user-guide` の :ref:`windgate-userguide-ssh-hadoop` を参照してください。

YAESSプロパティ
^^^^^^^^^^^^^^^

YAESSの構成に関する規約プロパティは、 ``asakusafwOrganizer`` ブロック内の参照名 ``yaess`` でアクセスできます [#]_ 。この規約オブジェクトは以下のプロパティを持ちます。

..  list-table:: Framework Organizer Plugin - YAESSプロパティ ( ``yaess`` ブロック )
    :widths: 2 1 2 5
    :header-rows: 1

    * - プロパティ名
      - 型
      - デフォルト値
      - 説明
    * -  ``enabled``
      - boolean
      - true
      - この値をtrueにするとYAESS用の構成を行う
    * -  ``hadoopEnabled`` 
      - boolean
      - true
      - この値をtrueにするとHadoopブリッジ ( ``yaess-hadoop`` ) を追加する [#]_
    * -  ``jobqueueEnabled`` 
      - boolean
      - false
      - この値をtrueにするとYAESSプラグイン ``asakusa-yaess-jobqueue`` を追加する [#]_
    * -  ``toolsEnabled`` 
      - boolean
      - true
      - この値をtrueにするとYAESS拡張ツールを追加する

..  [#] これらのプロパティは規約オブジェクト :gradledoc:`com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.YaessConfiguration` が提供します。

..  [#] 詳しくは :doc:`../yaess/user-guide` の :ref:`yaess-profile-hadoop-section-ssh` を参照してください。

..  [#] 詳しくは :doc:`../yaess/jobqueue` の :ref:`yaess-plugin-jobqueue-client` を参照してください。

フレームワーク拡張プロパティ
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Asakusa Frameworkの拡張構成に関する規約プロパティは、 ``asakusafwOrganizer`` ブロック内の参照名 ``extension`` でアクセスできます [#]_ 。この規約オブジェクトは以下のプロパティを持ちます。

..  list-table:: Framework Organizer Plugin - フレームワーク拡張プロパティ ( ``extension`` ブロック )
    :widths: 2 1 2 5
    :header-rows: 1

    * - プロパティ名
      - 型
      - デフォルト値
      - 説明
    * -  ``libraries``
      - java.util.List
      - ``[]``
      - ``$ASAKUSA_HOME/ext/lib`` 配下に配置するライブラリ [#]_

..  [#] これらのプロパティは規約オブジェクト :gradledoc:`com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.ExtensionConfiguration` が提供します。

..  [#] 明示的に指定されたライブラリのみを配置し、明示的でない依存ライブラリ等は自動的に配置しません。

デプロイメントアーカイブの編集
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
デプロイメントアーカイブの構成方法として、
コンポーネントの規約プロパティによってデプロイ構成を編集する機能の他に、
デプロイメントアーカイブに任意のファイルを追加する機能を利用できます。

この機能は、 ``asakusafwOrganizer`` ブロック内の参照名 ``assembly`` でアクセスできます [#]_ 。

以下は、 ``assembly`` の利用例です [#]_ 。

**build.gradle**

..  code-block:: groovy
   
    asakusafwOrganizer {
        profiles.prod {
            asakusafwVersion asakusafw.asakusafwVersion
            assembly.into('.') {
                put 'src/dist/prod'
                replace 'asakusa-resources.xml', inputCombineMax: '24'
            }
        }
    }

``assembly.into`` は引数に指定したパス上に、ブロック配下の定義で対象とするファイルを追加します。

コンポーネントの規約プロパティによる構成で追加されるファイルと同名のファイルが含まれる場合は、
ここで追加するファイルで上書きされるため、特定環境向けに構成した設定ファイルなどを含めることができます。

``assembly.into`` ブロック内では以下のような指定が可能です [#]_ 。

``put``
  デプロイメントアーカイブ追加するディレクトリやファイルのパスを指定します。
  相対パスで指定した場合はプロジェクトディレクトリが起点となります。 

``replace``
  ``put`` の指定で追加の対象となるファイルに対して置換を行います。
  第1引数は置換の対象となるファイル名を指定します。ここで指定したパスは後方一致で評価されます。
  置換の対象となるファイル内の ``@key@`` のように ``@`` 文字で囲まれた文字列が置換対象となります。

  後の引数に、置換対象文字列をMap形式 ( ``key``:``value`` )で指定します。

..  [#] これらの機能は :gradledoc:`com.asakusafw.gradle.assembly.AsakusafwAssembly` が提供します。

..  [#] ``assembly`` の利用例は、 :doc:`../administration/deployment-guide` も参照してください。

..  [#] これらの機能は :gradledoc:`com.asakusafw.gradle.assembly.AssemblyHandler` が提供します。

.. _gradle-plugin-oraganizer-profile:

プロファイルの管理
~~~~~~~~~~~~~~~~~~
Framework Organizer Pluginでは、
特定の環境向けに個別にデプロイメントアーカイブの構成を設定するために
「プロファイル」を定義することができます。

プロファイルの指定は、 ``asakusafwOrganizer`` ブロック内の参照名 ``profiles`` で定義します [#]_ 。

Framework Organizer Pluginはプロファイルごとの設定に従って
それぞれのプロファイルに対応するデプロイメントアーカイブを生成します。

標準では、以下のプロファイルが設定されています。

..  list-table:: Framework Organizer Plugin - 標準プロファイル
    :widths: 2 8
    :header-rows: 1

    * - プロファイル名
      - 説明
    * -  ``dev``
      - 開発環境向けのデプロイ構成を定義するプロファイル [#]_
    * -  ``prod`` 
      - 運用環境向けのデプロイ構成を定義するプロファイル

標準で設定されているプロファイルに加えて、
``asakusafwOrganizer`` ブロック配下に ``profiles.<profile-name>`` という形式で
任意のプロファイルを追加することができます。

以下は、ステージング環境用のデプロイ構成を持つ
プロファイル ``stage`` を定義する例です [#]_ 。

**build.gradle**

..  code-block:: groovy
     
    asakusafwOrganizer {
        hive.enabled true
        profiles.prod {
            asakusafwVersion asakusafw.asakusafwVersion
            windgate.retryableEnabled true
            assembly.into('.') {
                put 'src/dist/prod'
            }
        }
        profiles.stage {
            asakusafwVersion asakusafw.asakusafwVersion
            assembly.into('.') {
                put 'src/dist/stage'
            }
        }
    }

デプロイメントアーカイブの生成を行うと、
``build`` ディレクトリ配下に
``asakusafw-${asakusafwVersion}-<profile-name>.tar.gz`` というファイル名 [#]_ で 
プロファイルに対応したデプロイメントアーカイブが生成されます。

プロファイル内では上記で説明したコンポーネントごとの規約プロパティや
``assembly`` プロパティを使ったデプロイメントアーカイブの編集機能を使うことができます。

``asakusafwOrganizer`` 直下に設定されている設定は、すべてのプロファイルに反映されます。
同じプロパティが ``asakusafwOrganizer`` 直下とプロファイルの両方に
指定されていた場合は、プロファイル側の設定が利用されます。

例えば上の例では、 ``asakusafwOrganizer`` 直下の ``hive.enabled true`` の設定は
プロファイル ``prod`` と ``stage`` 、及び標準のプロフィルである ``dev`` に反映されます。


..  [#] これらの機能は :gradledoc:`com.asakusafw.gradle.plugins.AsakusafwOrganizerProfile` が提供します。

..  [#] ``dev`` プロファイルは主に ``installAsakusafw`` タスクで開発環境にデプロイする構成として使用します。
        ``dev`` プロファイルはテストドライバ用の構成が有効になるなど、開発環境向けの既定値が設定されています。

..  [#] プロファイルの設定例は、 :doc:`../administration/deployment-guide` も参照してください。

..  [#] 標準の設定では、プロファイル ``prod`` のデプロイメントアーカイブは
        ``asakusafw-${asakusafwVersion}.tar.gz`` というファイル名(プロファイル名が接尾辞につかない)
        で生成されます。
        またプロファイル内にプロパティ ``archiveName`` を設定することで任意のファイル名を指定することもできます。

.. _include-hadoop-gradle-plugin:

デプロイメント構成に含むAsakusa Frameworkのバージョン
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

プロファイルを利用することで、
開発環境で利用するHadoopと運用環境で利用するHadoopのバージョン系を
個別に設定することができます。

例えば、開発環境では :jinrikisha:`Jinrikisha <index.xml>` で構築したHadoop1系のAsakusa Frameworkを利用し、
運用環境ではHadoop2系のAsakusa Frameworkを利用する、といった設定が可能です。

Hadoopのバージョン系や対応するAsakusa Frameworkバージョンについての説明や、
プロファイルの設定例については、 :doc:`../administration/deployment-guide` を参照してください。

.. _standalone-organizer-gradle-plugin:

Framework Organizer Pluginを単体で利用する
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

プロジェクトテンプレートに含まれる標準のビルドスクリプトでは、Framework Organizer Plugin プロジェクトのビルドスクリプトに組み込まれています。この利用方法には、Batch Application Pluginと連動し、プロジェクト上のバッチアプリケーション生成やデプロイと連動することができるといったメリットがあります。

このような利用方法のほかに、Framework Organizer Pluginをあるプロジェクトとは独立して利用することができます。この利用方法には、複数のプロジェクトにわたって共通のデプロイメント構成を管理したい、といった場合に有効です。

..  attention::
    Asakusa Framework バージョン 0.6.x 以前では 開発環境と運用環境で異なるHadoopバージョンを
    個別に設定する手法として、Framework Organizer Pluginを単体で利用することを推奨していましたが、
    バージョン 0.7.0 からは `デプロイメント構成に含むAsakusa Frameworkのバージョン`_ で
    説明するようにプロファイルを利用して設定することを推奨します。

制約事項
--------

Asakusa Framework の現在バージョン |version| におけるAsakusa Gradle Pluginの制約事項を以下に挙げます。

* レガシーモジュール [#]_ には未対応です。

..  [#] :doc:`../application/legacy-module-guide`

Asakusa Gradle Plugin マイグレーションガイド
============================================

ここでは、Asakusa Gradle Plugin で構築した開発環境のバージョンアップ手順や、 従来のAsakusa Frameworkが提供するMavenベースのビルドシステムからAsakusa Gradle Pluginを使ったビルドシステムに移行するための手順を説明します。

.. _vup-gradle-plugin:

Asakusa Gradle Pluginで構築した開発環境のバージョンアップ
---------------------------------------------------------

Asakusa Gradle Plugin で構築したAsakusa Framework開発環境をバージョンアップする手順例を説明します。Asakusa Frameworkの各バージョン固有のマイグレーション情報については :doc:`migration-guide` に説明があるので、こちらも必ず確認してください。

Asakusa Gradle Plugin をバージョンアップするには、ビルドスクリプト内のAsakusa Gradle Plugin のバージョン指定と、Asakusa Frameworkのバージョン指定をそれぞれ変更したのち、開発環境の再セットアップを行います。

マイグレーション前のビルドの確認
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

マイグレーション以前の状態でプロジェクトのフルビルドを行い、
ビルドが成功することを確認します。

..  code-block:: sh

    ./gradlew clean build

..  hint::
    この手順は必須ではありませんが、
    マイグレーション後のビルドが正常に動作しない場合に
    その原因がマイグレーション作業によるものであることを
    確実にするために実施すべき手順です。

Asakusa Gradle Pluginのバージョン指定
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

ビルドスクリプト内の ``buildscript`` ブロック内に定義しているAsakusa Gradle Pluginのクラスパス定義 (``classpath group: 'com.asakusafw', name: 'asakusa-gradle-plugins``) の バージョン指定 ``version`` の値をアップデートするAsakusa Gradle Pluginのバージョンに変更します。

**build.gradle**

..  literalinclude:: gradle-attachment/build.gradle
    :language: groovy
    :lines: 1-8
    :emphasize-lines: 6

..  attention::
    ここで指定するバージョン番号は、 Asakusa Gradle Pluginのバージョン番号です。
    例えば Asakusa Framework バージョン ``0.7.2`` では ``0.7.2`` のような値となります。
    
    次の手順の `Asakusa Frameworkのバージョン指定`_ とは異なり、バージョン番号に
    ``-hadoop1`` や ``-hadoop2`` といった接尾辞は付かないことに注意してください。

Asakusa Frameworkのバージョン指定
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

ビルドスクリプト内の ``asakusafw`` ブロック内に定義しているAsakusa Frameworkのバージョン ``asakusafwVersion`` の値を、使用するAsakusa Frameworkのバージョンに変更します。

**build.gradle**

..  literalinclude:: gradle-attachment/build.gradle
    :language: groovy
    :lines: 19-20

また、 ``asakusafwOrganizer`` ブロック内に定義しているAsakusa Frameworkのバージョン ``asakusafwVersion`` の値を、
使用するAsakusa Frameworkのバージョンに変更します。

なおデフォルト設定のまま使用している場合は以下のように
``asakusafw`` ブロックの ``asakusafwVersion`` の値を参照しているため変更は不要ですが、
Hadoop2系向けのバージョン ( ``-hadoop2`` ) を指定している場合やプロファイル固有の設定を追加している場合などで
特定バージョンの値を直接設定している場合は、これらの値を忘れずに変更してください。

**build.gradle**

..  literalinclude:: gradle-attachment/build.gradle
    :language: groovy
    :lines: 30-33

..  attention::
    ここで指定するバージョン番号は、 Asakusa Framework本体のバージョン番号です。
    例えば Asakusa Framework バージョン ``0.7.2`` では ``0.7.2-hadoop1`` のような値となります。
    バージョン番号に ``-hadoop1`` や ``-hadoop2`` といった接尾辞が必要となることに注意してください
    
    バージョン ``0.6.x`` からのマイグレーションを検討する場合は、
    :ref:`versioning-sysytem-changing` ( :doc:`migration-guide` ) の内容を必ず確認してください。

Asakusa Frameworkの再インストール
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

開発環境のAsakusa Frameworkを再インストールします。

..  code-block:: sh

    ./gradlew installAsakusafw

マイグレーションしたビルド設定の確認
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

プロジェクトのフルビルドを行い、ビルドが成功することを確認してください。

..  code-block:: sh

    ./gradlew clean build

Eclipse定義ファイルの更新
~~~~~~~~~~~~~~~~~~~~~~~~~

Eclipseを利用している場合は、Eclipse用定義ファイルを更新します。

..  code-block:: sh

    ./gradlew cleanEclipse eclipse


.. _vup-gradle-wrapper:

プロジェクトで利用するGradleのバージョンアップ
----------------------------------------------

アプリケーションプロジェクトで利用するGradle (Gradle Wrapper) をバージョンアップする手順例を説明します。Asakusa Frameworkの各バージョン固有のマイグレーション情報については :doc:`migration-guide` に説明があるので、こちらも必ず確認してください。

Gradleのバージョン指定
~~~~~~~~~~~~~~~~~~~~~~

ビルドスクリプト内の ``task wrapper`` ブロック内に定義しているGradle Wrapperのディストリビューション
``distributionUrl`` の値を、使用するGradle Wrapperのバージョンに応じて変更します。

**build.gradle**

..  literalinclude:: gradle-attachment/build.gradle
    :language: groovy
    :lines: 10-13

..  attention::
    Asakusa Framework バージョン ``0.6.2`` 以前では、
    ``task wrapper`` ブロック内にはGradle Wrapperのバージョン指定に
    ``distributionUrl`` ではなく ``gradleVersion`` という値を使用していました。
    
    バージョン ``0.6.2`` 以前からのマイグレーションを行う場合は、
    ``gradleVersion`` を削除して ``distributionUrl`` を指定してください。

Gradle Wrapperの再生成
~~~~~~~~~~~~~~~~~~~~~~

プロジェクトのGradle Wrapperを再生成します。

..  code-block:: sh

    ./gradlew wrapper

.. _migrate-from-maven-to-gradle:

Mavenプロジェクトのマイグレーション
-----------------------------------

ここでは、 :doc:`../application/maven-archetype` や Asakusa Framework バージョン ``0.5.3`` 以前の :doc:`../introduction/start-guide` 及び :jinrikisha:`Jinrikisha (人力車) - Asakusa Framework Starter Package - <index.html>` で記載されている手順に従って構築した開発環境やMavenベースのアプリケーションプロジェクト(以下「Mavenプロジェクト」と表記)をAsakusa Gradle Pluginを使った環境にマイグレーションする手順を説明します。

..  note::
    プロジェクトのソースディレクトリに含まれるアプリケーションのソースコード(Asakusa DSL, DMDL, テストコードなど)についてのマイグレーション作業は不要で、そのまま利用することが出来ます。

..  attention::
    プロジェクトのマイグレーション作業前に、プロジェクトのバックアップとリストアの確認など、マイグレーション作業にトラブルが発生した場合に元に戻せる状態となっていることを確認してください。

.. _apply-gradle-project-template:

マイグレーション前のビルドの確認
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

マイグレーション以前の状態でプロジェクトのフルビルドを行い、
ビルドが成功することを確認します。

..  code-block:: sh

    mvn clean package

..  hint::
    この手順は必須ではありませんが、
    マイグレーション後のビルドが正常に動作しない場合に
    その原因がマイグレーション作業によるものであることを
    確実にするために実施すべき手順です。

プロジェクトテンプレートの適用
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

`Asakusa Gradle Pluginの導入`_  で説明したAsakusa Gradle Pluginのプロジェクトテンプレートに含まれるファイル一式をMavenプロジェクトに適用します。

以下は、ダウンロードしたプロジェクトテンプレートを ``$HOME/workspace/migrate-app`` に適用する例です。

..  code-block:: sh

    cd ~/Downloads
    tar xf asakusa-project-template-*.tar.gz
    cd asakusa-project-template
    cp -a build.gradle gradlew gradlew.bat .buildtools ~/workspace/migrate-app

プロジェクト初期設定ファイルの適用
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

MavenプロジェクトとAsakusa Gradle Pluginのプロジェクトテンプレートの両方に含まれるプロジェクトの初期設定ファイルに対しては、以下のファイル内容を確認し、必要に応じてMavenプロジェクトに適用します。

MavenプロジェクトとAsakusa Gradle Pluginのプロジェクトテンプレートの両方に含まれるファイルの一覧を以下に示します。

..  list-table:: 
    :widths: 234 218
    :header-rows: 1

    * - ファイル
      - 説明
    * -  ``src/test/resources/logback-test.xml`` 
      - ビルド/テスト実行時に使用されるログ定義ファイル

..  tip::
    Mavenプロジェクトで上記の設定ファイルをデフォルト設定のまま利用している場合は、Asakusa Gradle Pluginのプロジェクトテンプレートの内容で上書きすることを推奨します。

プロジェクト定義のマイグレーション
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Mavenプロジェクトのプロジェクト定義( ``pom.xml`` )の内容をGradleのビルドスクリプト( ``build.gradle`` )に反映します。

``pom.xml`` の代表的なカスタマイズ内容として、アプリケーションで利用するライブラリ追加による依存関係の設定があります。これは ``pom.xml`` 上では ``dependencies`` 配下に定義していました。

Gradle、およびAsakusa Gradle Pluginでは従来のMavenベースの依存関係の管理から一部機能が変更になっているため、  `ビルド設定のカスタマイズ`_  の内容をよく確認した上でアプリケーションに対して適切な設定を行ってください。

その他に確認すべき点は、  `標準プロジェクトプロパティ`_  の内容です。これに相当する内容はMavenアーキタイプからプロジェクトを作成する際に入力した内容が ``pom.xml`` のトップレベルの階層に定義されています。以下、この箇所に該当する ``pom.xml`` の設定例です。

..  code-block:: xml
         
        <name>Example Application</name>
        <groupId>com.example</groupId>
        <artifactId>migrate-app</artifactId>
        <version>1.0-SNAPSHOT</version>

Gradleではこれらのプロパティについてビルドスクリプト上の定義は必須ではありませんが、必要に応じて ``pom.xml`` の設定を反映するとよいでしょう。

ビルド定義ファイルのマイグレーション
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

従来のMavenのビルド定義ファイル( ``build.properties`` )の内容をGradleのビルドスクリプト( ``build.gradle`` )に反映します。

ビルド定義ファイルの内容は、移行後の ``build.gradle`` では  `Batch Application Plugin`_  上の規約プロパティとして定義します。

ここで必ず確認すべき項目は、Mavenアーキタイプでプロジェクトを作成した内容が反映される以下のプロパティです。

..  list-table::
    :widths: 113 113 113
    :header-rows: 1

    * - プロパティ
      - 対応するbuild.gradle上の設定項目
      - 説明
    * -  ``asakusa.package.default`` 
      -  ``asakusafw.compiler.compiledSourcePackage`` 
      - DSLコンパイラが生成する各クラスに使用されるパッケージ名
    * -  ``asakusa.modelgen.package`` 
      -  ``asakusafw.modelgen.modelgenSourcePackage`` 
      - モデルクラスに使用されるパッケージ名

その他の項目については、 ``build.properties`` をデフォルト値のまま利用している場合は移行作業は不要です。変更しているものがある場合はBatch Application Plugin上の規約プロパティを確認し、設定を反映してください。

Asakusa Frameworkの再インストール
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

開発環境のAsakusa Frameworkを再インストールします。

..  code-block:: sh

    ./gradlew installAsakusafw

マイグレーションしたビルド設定の確認
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

プロジェクトのフルビルドを行い、ビルドが成功することを確認してください。

..  code-block:: sh

    ./gradlew clean build

Eclipse定義ファイルの更新
~~~~~~~~~~~~~~~~~~~~~~~~~

Eclipseを利用している場合は、Eclipse用定義ファイルを更新します。

..  code-block:: sh

    ./gradlew cleanEclipse eclipse

Mavenビルド用ファイルの削除
~~~~~~~~~~~~~~~~~~~~~~~~~~~

Mavenプロジェクトのビルドで利用していた以下のファイル、ディレクトリを削除します。

*  ``pom.xml`` 
*  ``build.properties`` 
*  ``target`` 

Maven Framework Organizerのマイグレーション
-------------------------------------------

従来の Maven Framework Organizer [#]_ で提供していた機能は、 `Framework Organizer Plugin`_  によって提供されます。詳しくは Framework Organizer Plugin のドキュメントを参照してください。

..  [#] :doc:`../administration/framework-organizer`
