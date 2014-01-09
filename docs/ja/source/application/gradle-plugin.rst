===============================
Asakusa Gradle Plugin利用ガイド
===============================

この文書では、GradleにAsakusa Framework を使ったアプリケーションの開発やデプロイを行うための機能を追加する Asakusa Gradle Plugin について説明します。

..  attention::
    Asakusa Frameworkの現在のバージョン |version| では、Asakusa Gradle Pluginは試験的機能として提供されています。
    
    このバージョンでは、従来のMavenベースのビルドシステムが標準となっていますが、今後のビルドシステムに対する機能拡張はAsakusa Gradle Pluginを中心に行うこと、また将来的にはGradleをベースとしたビルドシステムを標準とすることを検討しています。

.. contents::
   :local:
   :depth: 2
   :backlinks: none

概要
====

Asakusa Gradle Plugin は、Asakusa Framework用の `Gradle <http://www.gradle.org/>`_ 拡張プラグインです。このプラグインを利用することで、Gradleを利用してAsakusa Framework を使ったアプリケーションの開発やデプロイを行うことができます。

利用環境
--------
Asakusa Gradle Plugin を利用するにはJava(JDK)、およびHadoopがインストールされている必要があります。 これらの導入方法については、 :doc:`../introduction/start-guide` の :ref:`startguide-development-environment` などを参考にしてください。

..  note::
    このドキュメントには Maven や Asakusa Frameworkのインストール手順も含まれますが、Asakusa Gradle Plugin の利用にはこれらの手順の実施は不要です。
    
..  note::
    `Jinrikisha (人力車) - Asakusa Framework Starter Package -`_ を使って構築した環境でも Asakusa Gradle Plugin を利用することができます。現在のバージョンの Jinrikisha に含まれるサンプルアプリケーションは従来のMavenベースのビルド環境を持つプロジェクト構成になっています。
 
..  _`Jinrikisha (人力車) - Asakusa Framework Starter Package -`: http://asakusafw.s3.amazonaws.com/documents/jinrikisha/ja/html/index.html

なお、Gradleのインストールについては、 本書では `Gradleラッパー <http://www.gradle.org/docs/current/userguide/gradle_wrapper.html>`_  と呼ばれるGradleを利用するためのコマンドを使う方法を推奨しています。この方法に沿ってGradleを利用する場合は前もってGradleをインストールする必要はありません。詳しくは後述の `Asakusa Gradle Pluginの導入`_ を参考にしてください。

Gradleについて
--------------

`Gradle <http://www.gradle.org/>`_  は オープンソースプロジェクトとして開発されているJVM環境(Java, Groovy, Scalaなど)を中心としたビルドシステムです。シンプルかつ拡張性の高いビルド定義を行うためのDSLやプラグイン機構を持ち、他のビルドシステムとの連携を含む様々な方式に対応した依存性管理のメカニズムを有しているなど多くの特徴を持っています。

Gradleに関する詳しい情報は、以下のドキュメントなどを参考にしてください。

*  `Gradle Documentation <http://www.gradle.org/documentation>`_  (Gradleの公式ドキュメントサイト)
*  `Gradle 日本語ドキュメント <http://gradle.monochromeroad.com/docs/>`_  (公式ドキュメントの翻訳サイト)


Asakusa Gradle Pluginの導入
===========================

Asakusa Gradle Plugin を利用する方法として、以下のいずれかの方法があります。

#. Asakusa Gradle Plugin 用プロジェクトテンプレートを使用する
#. ビルドスクリプトに個別にプラグイン利用に必要な設定を定義する

1)は、Asakusa Gradle Pluginの利用設定が行われたビルドスクリプト、及び標準的なプロジェクトレイアウトを含むプロジェクトテンプレートを利用する方法です。Asakusa Gradle Pluginを使った標準的なアプリケーション開発環境を導入するにはこのテンプレートを使うと便利です。

このプロジェクトテンプレートには  `Gradleラッパー <http://www.gradle.org/docs/current/userguide/gradle_wrapper.html>`_  と呼ばれるGradleを利用するコマンドが含まれます。このコマンドを利用することで、Gradle自体の導入設定は不要となり、すぐにこのプロジェクト上で開発を始めることができます。

2)は、テンプレートを使用せずフルスクラッチでビルドスクリプトの定義やプロジェクトレイアウトを作成する方法です。この方法はGradleやAsakusa Gradle Pluginの利用に精通している必要がありますが、プロジェクトテンプレートに対して多くのカスタマイズが必要となる場合はこちらの方法を検討してください。

以下では、1)のAsakusa Gradle Plugin 用プロジェクトテンプレートを利用した導入方法を解説します。 2)については、後述の  `Asakusa Gradle Plugin リファレンス`_  を参照してください。

プロジェクトテンプレートのダウンロード
--------------------------------------

プロジェクトテンプレートは、以下リンクからダウンロードします。

基本的なプロジェクトレイアウトのみを持つプロジェクトテンプレートのほか、これにサンプルアプリケーションのソースコード一式を加えたサンプルアプリケーションプロジェクトを公開しています。

Asakusa Gradle Plugin 用プロジェクトテンプレート
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

* `asakusa-project-template-0.6.0.tar.gz <http://www.asakusafw.com/download/gradle-plugin/asakusa-project-template-0.6.0.tar.gz>`_ 

Asakusa Gradle Plugin 用サンプルアプリケーションプロジェクト
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

* `asakusa-example-project-0.6.0.tar.gz <http://www.asakusafw.com/download/gradle-plugin/asakusa-example-project-0.6.0.tar.gz>`_ 

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
    * -  ``src`` 
      - プロジェクトのソースディレクトリ
    * -  ``build`` 
      - プロジェクトのビルドディレクトリ（ビルド時に生成）
    * -  ``gradle`` 
      - Gradleラッパー (Gradle Version: 1.10)
    * -  ``gradlew`` 
      - Gradleラッパーコマンド (Unix)
    * -  ``gradlew.bat`` 
      - Gradleラッパーコマンド (Windows)
    * -  ``build.gradle`` 
      - ビルドスクリプト

アプリケーション開発者は ``src`` ディレクトリ配下を編集することでアプリケーションを開発します。  ``build`` ディレクトリは ``src`` ディレクトリ配下のファイルをビルドすることで生成される成果物が配置されます。

``build`` ディレクトリ配下のファイルはビルドの度に初期化、再作成されるため ``build`` ディレクトリ配下のファイルは直接編集しないようにしてください。

GradleラッパーはGradleを使ったビルドを実行するために使用します。Gradleラッパーに関するディレクトリ及びファイルは、Gradleラッパー自体のマイグレーションを行う場合を除き編集しないようにしてください。

..  note::
    Gradleラッパーを使用せず、開発環境に対して個別にインストールしたGradleを使用することも出来ます。この場合、Asakusa Frameworkで未検証のバージョンのGradleを使用した場合に問題が発生する可能性があることに注意してください。本書ではGradleラッパーを使ってGradleに関する操作を説明しています。

ビルドスクリプト
~~~~~~~~~~~~~~~~

ビルドスクリプト( ``build.gradle`` )はプロジェクトのビルド設定を記述したGradle用のビルドスクリプトで、プロジェクトテンプレートに含まれるビルドスクリプトにはAsakusa Gradle Pluginを利用するための設定が記述されています。

..  code-block:: groovy

      1 buildscript {
      2     repositories {
      3         maven { url 'http://asakusafw.s3.amazonaws.com/maven/releases' }
      4     }
      5     dependencies {
      6         classpath group: 'com.asakusafw', name: 'asakusa-gradle-plugins', version: '0.6.0'
      7     }
      8 }
      9 
     10 apply plugin: 'asakusafw'
     11 apply plugin: 'asakusafw-organizer'
     12 apply plugin: 'eclipse'
     13 
     14 asakusafw {
     15     asakusafwVersion = '0.6.0'
     16 
     17     modelgen {
     18         modelgenSourcePackage 'com.example.modelgen'
     19     }
     20     compiler {
     21         compiledSourcePackage 'com.example.batchapp'
     22     }
     23 }
     24 
     25 asakusafwOrganizer {
     26     asakusafwVersion = "${asakusafw.asakusafwVersion}"
     27 }
     28 
     29 dependencies {
     30     compile group: 'com.asakusafw.sdk', name: 'asakusa-sdk-core', version: "${asakusafw.asakusafwVersion}"
     31     compile group: 'com.asakusafw.sdk', name: 'asakusa-sdk-directio', version: "${asakusafw.asakusafwVersion}"
     32     compile group: 'com.asakusafw.sdk', name: 'asakusa-sdk-windgate', version: "${asakusafw.asakusafwVersion}"
     33 
     34     provided (group: 'org.apache.hadoop', name: 'hadoop-client', version: '1.2.1') {
     35         exclude(module: 'junit')
     36         exclude(module: 'mockito-all')
     37         exclude(module: 'slf4j-log4j12')
     38     }
     39 }
    
    

..  note::
    プロジェクトテンプレートに含まれるビルドスクリプトには、Asakusa Frameworkの外部連携機能としてWindGateとDirect I/Oを利用するための構成が定義されています。

ソースディレクトリ
~~~~~~~~~~~~~~~~~~

プロジェクトのソースディレクトリは大きくアプリケーション本体のコードを配置する ``src/main`` ディレクトリと、アプリケーションのテスト用のコードを配置する ``src/test``  ディレクトリに分かれます。

それぞれのディレクトリ/ファイルの構成を以下に示します。

..  list-table:: プロジエクトレイアウト - src/main
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

..  [#] このディレクトリ内に 直接 配置したライブラリファイル ( \*.jar ) のみ、バッチアプリケーション内でも利用可能です（サブディレクトリに配置したライブラリファイルは無視されます）。詳しくは、後述の  `アプリケーション用依存ライブラリの追加`_  を参照してください。

..  list-table:: プロジエクトレイアウト - src/test
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
    * -  ``src/test/resources/asakusa-resources.xml`` 
      - 演算子のテスト実行時に使用される実行時プラグイン設定ファイル

ビルドディレクトリ
~~~~~~~~~~~~~~~~~~

プロジェクトのビルドディレクトリはGradleの各プラグインが提供するタスクの実行に対応したビルド成果物が作成されます。デフォルト設定のビルドディレクトリは ``build`` です。

ビルドディレクトリの主なディレクトリ/ファイルの構成を以下に示します [#]_

..  list-table:: プロジエクトレイアウト - build
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
      -  ``compileJava`` 
      - Operator DSLコンパイラによって生成される演算子実装クラス/演算子ファクトリクラス用ソースディレクトリ
    * -  ``classes`` 
      -  ``java`` 
      -  ``compileJava, compileTestJava`` 
      - Javaクラスを生成するディレクトリ
    * -  ``resources`` 
      -  ``java`` 
      -  ``processResources, processTestResources`` 
      - リソースを生成するディレクトリ
    * -  ``libs`` 
      -  ``java`` 
      -  ``jar`` 
      - ライブラリを生成するディレクトリ [#]_
    * -  ``excel`` 
      -  ``asakusafw`` 
      -  ``generateTestbook`` 
      - テストデータ定義シートを生成するディレクトリ
    * -  ``test-results`` 
      -  ``java`` 
      -  ``test`` 
      - テスト結果の.xmlファイルを生成するディレクトリ
    * -  ``reports/tests`` 
      -  ``java`` 
      -  ``test`` 
      - テストレポートファイルを生成するディレクトリ
    * -  ``batchc`` 
      -  ``asakusafw`` 
      -  ``compileBatchapp`` 
      - DSLコンパイラによって生成されるバッチアプリケーション用ディレクトリ
    * -  ``*-batchapp-*.jar`` 
      -  ``asakusafw`` 
      -  ``jarBatchapp`` 
      - batchcディレクトリ配下をアーカイブしたバッチアプリケーションアーカイブファイル  [#]_
    * -  ``asakusafw-*.tar.gz`` 
      -  ``asakusafw-organizer`` 
      -  ``assembleAsakusafw`` 
      - Asakusa Frameworkのデプロイメントアーカイブファイル [#]_

..  [#] 各タスクが処理過程で生成するワークディレクトリについては割愛しています。また、ここで示すディレクトリ以外にも、実行するGradleのタスクによって様々なディレクトリが生成されます。これらの詳細についてはGradleの各プラグインのドキュメントなどを参照してください。

..  [#] Asakusa Frameworkで作成したアプリケーション実行では利用しません。詳しくは後述の  `タスク依存関係とライフサイクルタスク`_  を参照してください。

..  [#] バッチアプリケーションアーカイブファイルについては、後述の  `バッチコンパイルとバッチアプリケーションアーカイブの生成`_  を参照してください。

..  [#] Asakusa Frameworkのデプロイメントアーカイブファイルについては、後述の  `Asakusa Frameworkのデプロイメントアーカイブ生成`_  を参照してください。

基本的なプラグインの使用方法
============================

ここでは、Asakusa Frameworkの開発の流れに沿ってAsakusa Gradle Plugin の基本的な使い方を紹介します。

以降の説明では、ターミナル上のカレントディレクトリがサンプルアプリケーションを配置したディレクトリに設定されていることを前提とします。

..  code-block:: sh

    cd ~/workspace/example-app


開発用のAsakusa Frameworkインストール
-------------------------------------

Asakusa Frameworkを開発環境にインストールします。

Asakusa Frameworkを開発環境にインストールするには、インストールディレクトリパスを環境変数 ``ASAKUSA_HOME`` に定義した上で ``installAsakusafw`` タスクを実行します。

..  code-block:: sh

    ./gradlew installAsakusafw

このタスクは ``ASAKUSA_HOME`` のパス上に開発環境用の構成を持つAsakusa Frameworkをインストールします。

..  note::
    開発環境では、Asakusa DSLを使ってアプリケーションを記述するだけであればAsakusa Frameworkのインストールは不要ですが、テストドライバを使ってFlow DSL、Batch DSLのテストを行う場合や、YAESSを使ってローカル環境でバッチアプリケーションを実行する場合など、Hadoopを実際に動作させる機能については、Asakusa Frameworkをインストールする必要があります。

データモデルクラスの生成
------------------------

DMDLスクリプトから演算子の実装で使用するデータモデルクラスを生成します。DMDLスクリプトの記述や配置方法については :doc:`../dmdl/index` を参照してください。

データモデルクラスを生成するには、 ``compileDMDL`` タスクを実行します。

..  code-block:: sh

    ./gradlew clean compileDMDL

このタスクはDMDLコンパイラを実行し、DMDLスクリプトディレクトリ( ``src/main/dmdl`` )配下のDMDLスクリプトからデータモデルクラスを データモデルクラス用ソースディレクトリ( ``build/generated-sources/modelgen`` )配下に生成します。

データモデルクラスに使われるJavaパッケージ名は、ビルドスクリプト( ``build.gradle`` )のプロパティ ``asakusafw/modelgen/modelgenSourcePackage`` [#]_ で指定します。プロジェクトテンプレートに含まれるビルドスクリプトの初期値は ``com.example.modelgen`` となっているので、アプリケーションが使用する適切なパッケージ名に変更してください。

..  code-block:: groovy

     14 asakusafw {
     15     asakusafwVersion = '0.6.0'
     16 
     17     modelgen {
     18         modelgenSourcePackage 'com.example.modelgen'
     19     }

上記のタスク実行例では ``clean`` タスクを合わせて実行しています ``。clean`` タスクはビルドディレクトリを初期化(削除)します。DMDLスクリプトでモデルの名称を変えたとき時などに使わなくなったデータモデルクラスが残らないようにするには、上記のように ``clean`` タスクを合わせて実行するとよいでしょう。

以降の説明では、同様の理由によりいくつかのタスクの実行例について ``clean`` タスクを合わせて実行しています。

..  [#] ここでのビルド設定方法について詳しくは、後述の `プラグイン規約プロパティ`_ を参照してください。

Javaソースファイルのコンパイル
------------------------------

Asakusa DSLとして記述したJavaソースファイルをコンパイルします [#]_ 。Asakusa DSLの記述や配置方法については、 :doc:`../dsl/index` を参照してください。

Javaソースファイルをコンパイルするには、 ``compileJava`` タスクを実行します [#]_ 。

..  code-block:: sh

    ./gradlew clean compileJava
    
``compileJava`` タスクを実行すると、Asakusa DSLのソースディレクトリ( ``src/main/java`` )に含まれるソースファイルをコンパイルして、 ``build/classes/main`` 配下にJavaクラスを生成します。

また、演算子クラスやフロー部品クラスに対しては、アノテーションプロセッサを利用したOperator DSLコンパイラが実行され、 ``build/generated-sources/annotations`` 配下に演算子実装クラス/演算子ファクトリクラスのJavaソースファイルが生成され、さらにこのJavaソースファイルをコンパイルした結果のクラスファイルが生成されます。

..  [#] EclipseなどのIDE上で作業する際に、IDEの自動ビルド機能を有効にしている場合は、ここで示すJavaソースファイルのコンパイルはソースファイルの編集や保存などのタイミングで自動的に行われます。

..  [#] Gradleには ``compileJava`` タスクの他にも、より細かい単位でソースファイルをコンパイルするためのタスクがいくつか提供されています。詳しくは Gradle のドキュメントを参照してください。

バッチコンパイルとバッチアプリケーションアーカイブの生成
--------------------------------------------------------

Asakusa DSLで記述したバッチアプリケーションをアプリケーション運用環境（Hadoopクラスタなど）にデプロイするには、Asakusa DSLコンパイラを実行してバッチアプリケーション実行ファイルを作成します。DSLコンパイラについての詳しい情報は :doc:`../dsl/user-guide` を参照してください。

Asakusa DSLコンパイラを実行するには、 ``compileBatchapp`` タスクを実行します。

..  code-block:: sh

    ./gradlew clean compileBatchapp

``compileBatchapp`` タスクを実行すると、 ``build/batchc`` 配下にバッチIDをディレクトリ名としたバッチアプリケーション実行ファイル一式が生成されます。 ``build/batchc`` 配下のディレクトリを  ``$ASAKUSA_HOME/batchapp``  配下に配置すればYAESS経由でアプリケーションの実行が可能となります [#]_ 。

また、 ``jarBatchapp`` タスクは ``build/batchc`` 配下のファイルを配布用のjarファイルにパッケージします。

..  code-block:: sh

    ./gradlew clean jarBatchapp
    
``jarBatchapp`` タスクを実行すると、 ``build`` 配下に  ``${baseName}-batchapp-${version}.jar`` という名前 [#]_ で ``build/batchc`` 配下をアーカイブしたjarアーカイブファイルを作成します。本書ではこれをバッチアプリケーションアーカイブファイルと呼びます。

バッチアプリケーションアーカイブファイルは運用環境上の ``$ASAKUSA_HOME/batchapps`` 配下にこのjarファイルを展開してデプロイします。より詳しくは、 :doc:`../administration/index` のデプロイメントガイドなどを参照してください。

..  [#] サンプルアプリケーションの実行方法については、 :doc:`../directio/start-guide` などを参照してください。

..  [#] より正確には、このファイルはGradleのアーカイブタスクのネーミングルールに従います。詳しくはGradleのドキュメントを参照してください。

アプリケーションのテスト
------------------------

Asakusa DSLとして記述したバッチアプリケーションに対して、テストロジックを実行してテストを行います。Asakusa DSLのテスト手法については、 :doc:`../testing/index` などを参照してください。

Asakusa DSLのテストを実行するには、 ``test`` タスクを実行します。

..  code-block:: sh

    ./gradlew clean test

``test`` タスクを実行すると、Asakusa DSLのテスト用ソースディレクトリ( ``src/test/java`` )に含まれる、JUnitかTestNGを使って記述されたテストクラスを自動的に検出し、これを実行します。

テストの実行結果は、 ``build/reports/tests`` 配下にHTML形式のテストレポートが生成されます。また、 ``build/test-results`` にはXML形式のテスト結果ファイルが生成されます。このXMLはCIサーバなどのツールと連携して使用することができます。

テストドライバの :doc:`../testing/using-excel` を使用したテストを記述する場合、  ``generateTestbook`` タスクを実行することでテストデータ定義シート（テストデータテンプレート）を生成することができます。

..  code-block:: sh

    ./gradlew generateTestbook

``generateTestbook`` タスクを実行すると、 ``build/excel`` 配下にDMDLで記述したデータモデルに対応するテストデータ定義シートが作成されます。

タスク依存関係とライフサイクルタスク
------------------------------------

タスク依存関係
~~~~~~~~~~~~~~

Gradleのタスクには適切な依存関係が設定されており、あるタスクを実行する上で前提条件となる処理を行うタスクの実行は自動的に実行されるようになっています。

例えば、 ``jarBatchapp`` タスクを実行するには ``compileBatchapp`` タスクをあらかじめ実行しておく必要があり、さらに ``compileBatchapp`` タスクは ``classes`` タスクを事前に実行しておく必要がある、といったタスク間の依存関係がありますが、Gradleコマンドからは単に ``jarBatchapp`` タスクのみを指定して実行すると、事前に実行する必要があるタスクを適切な順番で自動的に実行するようになっています。

Asakusa Gradle Plugin が提供するタスク依存関係の詳細は、 `Asakusa Gradle Plugin リファレンス`_  を参照してください。

ライフサイクルタスク
~~~~~~~~~~~~~~~~~~~~

Asakusa Gradle Pluginにはプロジェクトに対するビルドを目的別に実行するための「ライフサイクルタスク」が定義されています [#]_ 。

ライフサイクルタスクを利用することで、複雑なタスク依存関係を隠蔽し、ビルドの目的に応じて必要なタスクをシンプルに実行することができます。ここでは主なライフサイクルタスクを紹介します。

assemble
^^^^^^^^

``assemble`` タスクはプロジェクトのすべてのアーカイブを構築します。Asakusa Gradle Pluginを利用する上では、バッチアプリケーションアーカイブの生成を行う目的で使用できます。

..  code-block:: sh

    ./gradlew clean assemble

check
^^^^^

``check`` タスクはプロジェクトのすべての検証タスクを実行します。Asakusa Gradle Pluginを利用する上では、アプリケーションに対するテストを実行する目的で使用できます。

..  code-block:: sh

    ./gradlew clean check

build
^^^^^

``build`` タスクはプロジェクトのフルビルドを実行します。 実際には上記 ``assemble`` と  ``check``  タスクを実行します。CIサーバなどでリリースビルドを行うような場合に、このタスクを利用するとよいでしょう。

..  code-block:: sh

    ./gradlew clean build

..  [#] 正確には、ライフサイクルタスクはGradleが標準で提供する Java プラグインによって定義されています。詳しくは Gradle のドキュメントを参照してください。

Eclipse定義ファイルの作成
-------------------------

アプリケーション開発用の統合開発環境(IDE)にEclipseを使用する場合、開発環境にEclipseをインストールした上で、プロジェクトに対してEclipseプロジェクト用の定義ファイルを追加します。

Eclipseプロジェクト用の定義ファイルを作成するには、 ``eclipse`` タスクを実行します。

..  code-block:: sh

    ./gradlew compileJava eclipse

EclipseにJavaソースディレクトリを正しく認識させるためには、 ``eclipse`` タスクを実行する時点で、プロジェクトで使用するすべてのJavaソースディレクトリが存在している必要があります。 Asakusa Gradle Plugin では `Javaソースファイルのコンパイル`_ で説明した ``compileJava`` タスクを実行することで必要なソースディレクトリが生成されるので、上記の例では ``compileJava`` タスクを事前に実行しています。

このコマンドを実行することによってプロジェクトディレクトリに対して ``.project`` , ``.classpath`` , ``.factorypath`` などのいくつかのEclipse用の定義ファイルが追加されます。これにより、Eclipseからプロジェクトをインポートすることが可能になります。

..  tip::
    Eclipseからプロジェクトをインポートするには、Eclipseのメニューから  ``[File]``  ->  ``[Import]``  ->  ``[General]``  ->  ``[Existing Projects into Workspace]``  を選択し、プロジェクトディレクトリを指定します。

Asakusa Frameworkのデプロイメントアーカイブ生成
-----------------------------------------------

Asakusa Frameworkを運用環境にデプロイするためのデプロイメントアーカイブを生成します。

運用環境向けの標準的な構成を持つデプロイメントアーカイブを生成するには、 ``assembleAsakusafw`` タスクを実行します。

..  code-block:: sh

    ./gradlew assembleAsakusafw
    
``assembleAsakusafw`` タスクを実行すると、 ``build`` 配下に  ``asakusafw-${asakusafwVersion}.tar.gz`` という名前でデプロイメントアーカイブが作成されます。このアーカイブには WindGate, Direct I/Oを含むAsakusa Framework実行環境一式が含まれます。

このデプロイメントアーカイブは運用環境上の$ASAKUSA_HOME配下に展開してデプロイします。より詳しくは、 :doc:`../administration/index` のデプロイメントガイドなどを参照してください。

バッチアプリケーションの同梱
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

前述の  `バッチコンパイルとバッチアプリケーションアーカイブの生成`_  で説明した、コンパイル済のバッチアプリケーションをデプロイメントアーカイブに含めることができます。

``attachBatchapps`` タスクは、 ``build/batchc`` 配下に存在するバッチアプリケーションをデプロイメントアーカイブに含めます。以下は、バッチアプリケーションの生成してこれを含むデプロイメントアーカイブを生成する例です。

..  code-block:: sh

    ./gradlew clean compileBatchapp attachBatchapps assembleAsakusafw

このようにタスクを実行すると、バッチコンパイルを実行後に ``build`` 配下に  ``asakusafw-${asakusafwVersion}.tar.gz`` が生成され、このアーカイブの   ``batchapps`` 配下には ``compileBatchapp`` タスクによって ``build/batchc`` 配下に生成されたバッチアプリケーションの実行ファイル一式が含まれます。

設定ファイル/アプリケーションライブラリの同梱
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

デプロイメントアーカイブに、特定の運用環境向けの設定ファイルやアプリケーション用の追加ライブラリを含めることもできます。

この機能を使用するには、まずプロジェクトディレクトリの ``src/dist`` 配下に特定環境を示す名前（以下この名前を「ディストリビューション名」と呼びます）を持つディレクトリを生成します。このディレクトリは英数小文字のみ使用できます。

ディストリビューション名のディレクトリ配下に、 ``$ASAKUSA_HOME`` のディレクトリ構造と同じ形式で追加したい設定ファイルやライブラリファイルを配置します。このディレクトリ構成がデプロイメントアーカイブにそのまま追加されます。

以下は、 ``src/dist`` 配下に ``myenv`` というディストリビューション名を持つのディレクトリを作成し、これに アプリケーション向けの追加ライブラリとYAESS 向けの設定ファイル を配置した例です。

..  code-block:: sh

    src/dist
    └── myenv
        ├── ext
        │   └── lib
        │       └── joda-time-2.2.jar
        └── yaess
            └── conf
                └── yaess.properties

``src/dist`` 配下のディレクトリ構成をデプロイメントアーカイブに含めるには、  ``attachConf<ディストリビューション名>`` というタスクを実行します。タスク名の ``<ディストビューション名>`` 部分は ``src/dist`` 配下のディストリビューション名に対応し、これにマッチしたディレクトリ配下のファイル [#]_ をデプロイメントアーカイブに含めます。

タスク名の  ``<ディストリビューション名>``  部分は大文字/小文字の違いを無視します。例えば ``src/dist/myenv`` に対応するタスクは ``attachConfMyEnv`` にも対応します。

以下は、 ``src/dist/myenv`` 配下のファイルを含むデプロイメントアーカイブを生成する例です。

..  code-block:: sh

    ./gradlew attachConfMyEnv assembleAsakusafw
    

このようにタスクを実行すると、バッチコンパイルを実行後に ``build`` 配下に   ``asakusafw-${asakusafwVersion}.tar.gz`` が生成され、このアーカイブには  ``src/dist/myenv`` 以下のディレクトリ構造を含むファイル一式が含まれます。

..  [#]  ``.``  (ドット)から始まる名前を持つファイルやディレクトリは無視され、アーカイブに含まれません。

拡張モジュールの同梱
~~~~~~~~~~~~~~~~~~~~

Asakusa Frameworkでは、標準のデプロイメントアーカイブに含まれない追加機能を拡張モジュール [#]_ として提供しています。

拡張モジュールは Asakusa Framworkの標準的なデプロイ構成にプラグインライブラリを追加することで利用することができます。Asakusa Gradle Plugin ではデプロイメントアーカイブの生成時に拡張モジュール取得用のタスク [#]_ を合わせて実行することで、デプロイメントアーカイブに拡張モジュールを含めることができます。

以下は、拡張モジュール ``asakusa-windgate-retryable`` をデプロイメントアーカイブに含める例です。

..  code-block:: sh

    ./gradlew attachExtensionWindGateRetryable assembleAsakusafw

このようにタスクを実行すると、 ``build`` 配下に  ``asakusafw-${asakusafwVersion}.tar.gz`` が生成され、このアーカイブには拡張モジュールが含まれた状態となります。今回の例では、アーカイブ内の  ``windgate/plugin`` 配下に ``asakusa-windgate-retryable`` 用のjarファイルが追加されています。

..  [#] 拡張モジュールについて、詳しくは  :doc:`../administration/deployment-extension-module` を参照してください。

..  [#] 拡張モジュール取得用のタスク一覧については、 `Asakusa Gradle Plugin リファレンス`_ を参照してください。

組み合わせの例
~~~~~~~~~~~~~~

これまで説明した内容を組み合わせて利用すると、特定環境向けのリリース用デプロイメントアーカイブをビルド時に作成することができます。

以下は、リリースビルドを想定したデプロイメントアーカイブ生成の実行例です。

..  code-block:: sh

    ./gradlew clean build attachBatchapps attachConfMyEnv attachExtensionWindGateRetryable assembleAsakusafw
    
このようにタスクを実行すると、テスト済のバッチアプリケーションと設定ファイル、追加ライブラリ、拡張モジュールを含むデプロイメントアーカイブを生成します。

ビルド設定のカスタマイズ
========================

ビルド設定のカスタマイズは、基本的にはGradleのビルドスクリプトである  ``build.gradle`` を編集します。

以下は、いくつかの基本的なカスタマイズをおこなったビルドスクリプトの例です。

..  code-block:: groovy

      1 // Standard project properties
      2 group   'com.example'
      3 version '0.1.2'
      4 description 'Example application'
      5 
      6 buildscript {
      7     repositories {
      8         maven { url "http://asakusafw.s3.amazonaws.com/maven/releases" }
      9     }
     10 
     11     dependencies {
     12         classpath group: 'com.asakusafw', name: 'asakusa-gradle-plugins', version: '0.6.0'
     13     }
     14 }
     15 
     16 apply plugin: 'asakusafw'
     17 apply plugin: 'asakusafw-organizer'
     18 
     19 asakusafw {
     20     asakusafwVersion = '0.6.0'
     21 
     22     modelgen {
     23         modelgenSourcePackage "com.example.modelgen"
     24     }
     25     compiler {
     26         compiledSourcePackage "com.example.batchapp"
     27         // DSL compile options
     28         compilerOptions "+enableCombiner,-compressFlowPart"
     29     }
     30 }
     31 
     32 asakusafwOrganizer {
     33     asakusafwVersion = "${asakusafw.asakusafwVersion}"
     34 }
     35 
     36 dependencies {
     37     compile group: 'com.asakusafw.sdk', name: 'asakusa-sdk-core', version: "${asakusafw.asakusafwVersion}"
     38     compile group: 'com.asakusafw.sdk', name: 'asakusa-sdk-directio', version: "${asakusafw.asakusafwVersion}"
     39     compile group: 'com.asakusafw.sdk', name: 'asakusa-sdk-windgate', version: "${asakusafw.asakusafwVersion}"
     40 
     41     provided (group: 'org.apache.hadoop', name: 'hadoop-client', version: '1.2.1') {
     42         exclude(module: 'junit')
     43         exclude(module: 'mockito-all')
     44         exclude(module: 'slf4j-log4j12')
     45     }
     46 
     47     // Additional dependencies
     48     compile group: 'com.asakusafw.sandbox', name: 'asakusa-directio-dmdl-ext', version: "${asakusafw.asakusafwVersion}"
     49 }
    

標準プロジェクトプロパティ
--------------------------

標準的なプロジェクト情報は、以下のように ビルドスクリプト のトップレベルの階層に定義します。

..  code-block:: groovy

      1 // Standard project properties
      2 group   'com.example'
      3 version '0.1.2'
      4 description 'Example application'

このうち、 ``version`` プロパティはアーカイブファイル名に付加されたり、バッチアプリケーションのコンパイル時のビルド情報ファイルに含まれたりするなど、様々な箇所で使用されます。

指定可能なプロパティ一覧についてはGradleのドキュメントを参照してください。

プラグイン規約プロパティ
------------------------

Asakusa Gradle Plugin固有の設定情報は、ビルドスクリプトの ``asakusafw`` ブロック内に指定します。

``asakusafw`` のブロックで定義できるプロパティをプラグイン規約プロパティと呼びます。 ``asakusafw`` ブロックは設定のカテゴリ別に階層化されています。

以下の例では、トップレベルの階層に プロジェクトで使用するAsakusa Frameworkのバージョンを示す ``asakusafwVersion`` が指定され、続いてデータモデルクラスの生成に関する ``modelgen`` ブロック、DSLコンパイルの設定に関する ``compiler`` ブロックが指定されています。ブロック内には複数のプロパティを指定することができます。

以下の例では、プロジェクトテンプレートのデフォルト設定に対して、モデルクラス名のパッケージ名の変更、DSLコンパイルオプションを指定するプロパティの追加を行っています。

..  code-block:: groovy

     19 asakusafw {
     20     asakusafwVersion = '0.6.0'
     21 
     22     modelgen {
     23         modelgenSourcePackage "com.example.modelgen"
     24     }
     25     compiler {
     26         compiledSourcePackage "com.example.batchapp"
     27         // DSL compile options
     28         compilerOptions "+enableCombiner,-compressFlowPart"
     29     }
     30 }

プラグイン規約プロパティには多くの指定可能なプロパティが存在しますが、ほとんどのプロパティについては適切なデフォルト値が設定されており、多くの用途では個別に指定する必要がないようになっています。プラグイン規約プロパティの一覧については、 `Asakusa Gradle Plugin リファレンス`_  を参照してください。

依存関係の管理
--------------

アプリケーションのビルドで使用するライブラリの依存関係に関する設定は、ビルドスクリプトの ``dependencies`` ブロックに指定します。

..  code-block:: groovy

     36 dependencies {
     37     compile group: 'com.asakusafw.sdk', name: 'asakusa-sdk-core', version: "${asakusafw.asakusafwVersion}"
     38     compile group: 'com.asakusafw.sdk', name: 'asakusa-sdk-directio', version: "${asakusafw.asakusafwVersion}"
     39     compile group: 'com.asakusafw.sdk', name: 'asakusa-sdk-windgate', version: "${asakusafw.asakusafwVersion}"
     40 
     41     provided (group: 'org.apache.hadoop', name: 'hadoop-client', version: '1.2.1') {
     42         exclude(module: 'junit')
     43         exclude(module: 'mockito-all')
     44         exclude(module: 'slf4j-log4j12')
     45     }
     46 
     47     // Additional dependencies
     48     compile group: 'com.asakusafw.sandbox', name: 'asakusa-directio-dmdl-ext', version: "${asakusafw.asakusafwVersion}"
     49 }

上記の例では、Asakusa Framework のDirect I/O に TSVフォーマットのファイルを扱うための拡張機能である  `Direct I/O TSV <http://asakusafw.s3.amazonaws.com/documents/sandbox/ja/html/directio/tsv.html>`_  を利用するための設定を追加しています。

Direct I/O TSVはDMDLコンパイラの拡張のみを行う機能であるため、運用環境に対するランタイムライブラリの配置が不要ですが、アプリケーションの演算子で利用するライブラリを追加する場合は、運用環境にもこのライブラリを配置する必要があります。これについては、次項の  `アプリケーション用依存ライブラリの追加`_  で説明します。

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
      -  ``Batch Application Plugin`` 
      -  ``java`` 
      -  ``eclipse`` 
      - Asakusa Framework の バッチアプリケーションを開発を行うための支援機能をプロジェクトに追加する。
    * -  ``asakusafw-organizer`` 
      -  ``Framework Organizer Plugin`` 
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

Batch Application Pluginを使うためには、ビルドスクリプトに下記を含めます：

..  code-block:: groovy

    apply plugin: 'asakusafw'

タスク
~~~~~~

Batch Application Plugin は、以下のタスクをプロジェクトに追加します。

..  list-table:: Batch Application Plugin - タスク
    :widths: 113 113 113 113
    :header-rows: 1

    * - タスク名
      - 依存先
      - 型
      - 説明
    * -  ``compileDMDL`` 
      -  ``-`` 
      - SourceTask
      - DMDLコンパイラを使ってモデルクラスを生成する
    * -  ``compileBatchapp`` 
      -  ``classes`` 
      - Task
      - DSLコンパイラを使ってバッチアプリケーションを生成する
    * -  ``jarBatchapp`` 
      -  ``compileBatchapp`` 
      - Jar
      - バッチアプリケーションアーカイブを生成する
    * -  ``generateTestbook`` 
      -  ``-`` 
      - SourceTask
      - テストデータ定義シートを生成する

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
      - JavaCompile
      - Javaソースファイルをコンパイルする
    * -  ``assemble`` 
      -  ``jarBatchapp`` 
      - Task
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
      - ``compile`` と同様に振る舞うが各ディストリビューション用アーカイブには追加されない。

リポジトリ
~~~~~~~~~~

Batch Application Plugin は、以下のリポジトリをプロジェクトに追加します。

..  list-table:: Batch Application Plugin - リポジトリ
    :widths: 286 166
    :header-rows: 1

    * - 名前/URL
      - 説明
    * - mavenCentral
      - Mavenのセントラルリポジトリ
    * - http://asakusafw.s3.amazonaws.com/maven/releases
      - Asakusa Frameworkのリリース用Mavenリポジトリ
    * - http://asakusafw.s3.amazonaws.com/maven/snapshots
      - Asakusa Frameworkのスナップショット用Mavenリポジトリ

規約プロパティ
~~~~~~~~~~~~~~

Batch Application Plugin の規約プロパティはビルドスクリプトから 参照名 ``asakusafw`` でアクセスできます [#]_ 。この規約オブジェクトは以下のプロパティと従属する規約オブジェクトを持ちます。

..  list-table:: Batch Application Plugin - 規約プロパティ
    :widths: 135 102 101 113
    :header-rows: 1

    * - プロパティ名
      - 型
      - デフォルト値
      - 説明
    * -  ``asakusafwVersion`` 
      - String
      -  ``なし`` 
      - プロジェクトが使用するAsakusa Frameworkのバージョン
    * -  ``maxHeapSize`` 
      - String
      -  ``1024m`` 
      - プラグインが実行するJavaプロセスの最大ヒープサイズ
    * -  ``logbackConf`` 
      - String
      -  ``src/${project.sourceSets.test.name}/resources/logback-test.xml`` 
      - プロジェクトのlogback設定ファイル
    * -  ``dmdl`` 
      - DmdlConfiguration
      -  ``-`` 
      - DMDLに関する規約オブジェクト
    * -  ``dmdl.dmdlEncoding`` 
      - String
      -  ``UTF-8`` 
      - DMDLスクリプトのエンコーディング
    * -  ``dmdl.dmdlSourceDirectory`` 
      - String
      -  ``src/${project.sourceSets.main.name}/dmdl`` 
      - DMDLスクリプトのソースディレクトリ
    * -  ``modelgen`` 
      - ModelgenConfiguration
      -  ``-`` 
      - モデル生成に関する規約オブジェクト
    * -  ``modelgen.modelgenSourcePackage`` 
      - String
      -  ``${project.group}.modelgen`` 
      - モデルクラスに使用されるパッケージ名
    * -  ``modelgen.modelgenSourceDirectory`` 
      - String
      -  ``${project.buildDir}/generated-sources/modelgen`` 
      - モデルクラスのソースディレクトリ
    * -  ``javac`` 
      - JavacConfiguration
      -  ``-`` 
      - javacに関する規約オブジェクト
    * -  ``javac.annotationSourceDirectory`` 
      - String
      -  ``${project.buildDir}/generated-sources/annotations`` 
      - アノテーションプロセッサが生成するJavaソースの出力先
    * -  ``javac.sourceEncoding`` 
      - String
      -  ``UTF-8`` 
      - プロジェクトのソースファイルのエンコーディング
    * -  ``javac.sourceCompatibility`` 
      - JavaVersion。StringやNumberで設定することも可能。例： '1.6' や 1.6 [#]_
      -  ``1.6`` 
      - Javaソースのコンパイル時に使用するJavaバージョン互換性
    * -  ``javac.targetCompatibility`` 
      - JavaVersion。StringやNumberで設定することも可能。例： '1.6' や 1.6
      -  ``1.6`` 
      - クラス生成のターゲットJavaバージョン
    * -  ``compiler`` 
      - CompilerConfiguration
      -  ``-`` 
      - DSLコンパイラに関す規約オブジェクト
    * -  ``compiler.compiledSourcePackage`` 
      - String
      -  ``${project.group}.batchapp`` 
      - DSLコンパイラが生成する各クラスに使用されるパッケージ名
    * -  ``compiler.compiledSourceDirectory`` 
      - String
      -  ``${project.buildDir}/batchc`` 
      - DSLコンパイラが生成する成果物の出力先
    * -  ``compiler.compilerOptions`` 
      - String
      -  ``未指定`` 
      - DSLコンパイラオプション
    * -  ``compiler.compilerWorkDirectory`` 
      - String
      -  ``${project.buildDir}/batchcwork`` 
      - DSLコンパイラのワーキングディレクトリ
    * -  ``compiler.hadoopWorkDirectory`` 
      - String
      -  ``${project.buildDir}/hadoopwork/${execution_id}`` 
      - DSLコンパイラが生成するアプリケーション(Hadoopジョブ)が使用するHadoop上のワーキングディレクトリ
    * -  ``testtools`` 
      - TestToolsConfiguration
      -  ``-`` 
      - テストツールに関する規約オブジェクト
    * -  ``testtools.testDataSheetFormat`` 
      - String
      -  ``ALL`` 
      - テストデータ定義シートのフォーマット [#]_
    * -  ``testtools.testDataSheetDirectory`` 
      - String
      -  ``${project.buildDir}/excel`` 
      - テストデータ定義シートの出力先

..  [#] これらのプロパティは規約オブジェクト ``AsakusafwConvention`` が提供します。
..  [#] JDK 7で追加になった言語機能やAPIを利用するなどの場合に変更します。 詳しくは :doc:`develop-with-jdk7` を参照してください。
..  [#] テストデータ定義シートのフォーマット指定値は、 :doc:`../testing/using-excel` の :ref:`testdata-generator-excel-format` を参照してください。

Eclipse Pluginの拡張
~~~~~~~~~~~~~~~~~~~~
Batch Application Plugin は Gradleが提供するEclipse Pluginが提供するタスクに対して、以下のようなEclipseプロジェクトの追加設定を行います。

* OperatorDSLコンパイラを実行するためのAnnotation Processorの設定
* Javaのバージョンやエンコーディングに関する設定

また、Batch Application Pluginが設定する規約プロパティの情報を ``.settings/com.asakusafw.asakusafw.prefs`` に出力します。

Framework Organizer Plugin
--------------------------

Framework Organizer Plugin は、Asakusa Framework を 利用した開発環境の構築や、運用環境へのデプロイを行うための支援機能を提供します。

Framework Organizer Plugin が提供する機能には次のようなものがあります。

* Aasakusa Framework本体のデプロイメントモジュールを生成するタスクの提供。数種類の代表的なデプロイメント構成から選択可能。
* 上記機能のオプションとして、Asakusa Frameworkの構成要素（Asakusa Framework本体、Asakusa Frameworkプラグイン、設定ファイル、バッチアプリケーション、バッチアプリケーション用追加ライブラリなど）を統合してパッケージングする機能を提供。
* Asakusa Frameworkを開発環境へインストールするタスクの提供。

使用方法
~~~~~~~~

Framework Organizer Pluginを使うためには、ビルドスクリプトに下記を含めます：

..  code-block:: groovy

    apply plugin: 'asakusafw-organizer'

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
    * -  ``cleanAssembleAsakusafw`` 
      -  ``-`` 
      - Task
      - デプロイメント構成の構築時に利用するワーキングディレクトリを初期化する [#]_
    * -  ``attachBatchapps`` 
      -  ``-`` 
      - Task
      - デプロイメント構成にバッチアプリケーションを追加する [#]_
    * -  ``attachComponentCore`` 
      -  ``-`` 
      - Task
      - デプロイメント構成にランタイムコアモジュールを追加する
    * -  ``attachComponentDirectIo`` 
      -  ``-`` 
      - Task
      - デプロイメント構成にDirect I/Oを追加する
    * -  ``attachComponentYaess`` 
      -  ``-`` 
      - Task
      - デプロイメント構成にYAESSを追加する
    * -  ``attachComponentWindGate`` 
      -  ``-`` 
      - Task
      - デプロイメント構成にWindGateを追加する
    * -  ``attachComponentDevelopment`` 
      -  ``-`` 
      - Task
      - デプロイメント構成に開発ツールを追加する
    * -  ``attachComponentOperation`` 
      -  ``-`` 
      - Task
      - デプロイメント構成に運用ツールを追加する
    * -  ``attachExtensionYaessJobQueue`` 
      -  ``-`` 
      - Task
      - デプロイメント構成にYAESS JobQueue Pluginを追加する
    * -  ``attachExtensionWindGateRetryable`` 
      -  ``-`` 
      - Task
      - デプロイメント構成にWindGate Retryable Pluginを追加する
    * -  ``attachConf<``  ``DistributionName``  ``>`` 
      -  ``-`` 
      - Task
      - デプロイメント構成にディストリビューション名に対応するディレクトリを追加する [#]_
    * -  ``attachAssembleDev`` 
      -  ``attachBatchapps,`` 
        
         ``attachComponentCore,`` 
        
         ``attachComponentDirectIo,`` 
        
         ``attachComponentYaess,`` 
        
         ``attachComponentWindGate,`` 
        
         ``attachComponentDevelopment,`` 
        
         ``attachComponentOperation`` 
      - Task
      - 開発環境向けのデプロイメント構成を構築する
    * -  ``attachAssemble`` 
      -  ``attachComponentCore,`` 
        
         ``attachComponentDirectIo,`` 
        
         ``attachComponentYaess,`` 
        
         ``attachComponentWindGate,`` 
        
         ``attachComponentOperation`` 
      - Task
      - 運用環境向けのデプロイメント構成を構築する
    * -  ``assembleCustomAsakusafw`` 
      -  ``-`` 
      - Task
      - 任意のデプロイメント構成を持つデプロイメントアーカイブを生成する
    * -  ``assembleDevAsakusafw`` 
      -  ``attachAssembleDev`` 
      - Task
      - 開発環境向けのデプロイメント構成を持つデプロイメントアーカイブを生成する
    * -  ``assembleAsakusafw`` 
      -  ``attachAssemble`` 
      - Task
      - 運用環境向けのデプロイメント構成を持つデプロイメントアーカイブを生成する
    * -  ``installAsakusafw`` 
      -  ``attachAssembleDev`` 
      - Task
      - 開発環境向けのデプロイメント構成をローカル環境にインストールする [#]_

..  [#]  ``cleanAssembleAsakusafw`` タスクは ``attach`` をプレフィックスに持つタスクが呼ばれるタスクグラフ構成が構築された場合に、 ``attach`` を持つタスク群が実行される前に一度だけ自動的に実行されます。

..  [#]  ``attachBatchapps`` タスクを利用するには本プラグインをアプリケーションプロジェクト上で利用する必要があります。

..  [#]  ``attachConf<DistributionName>`` タスクを利用するには本プラグインをアプリケーションプロジェクト上で利用する必要があります。

..  [#]  ``installAsakusafw`` タスクを利用するには環境変数 ``ASAKUSA_HOME`` が設定されている必要があります。

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
      -  ``なし`` 
      - デプロイメント構成に含むAsakusa Frameworkのバージョン
    * -  ``assembleDir`` 
      - String
      -  ``${project.buildDir}/asakusafw-assembly`` 
      - デプロイメント構成の構築時に利用するワーキングディレクトリ

..  [#] これらのプロパティは規約オブジェクト ``AsakusafwOrganizerConvention`` が提供します。

デプロイメント構成に含むAsakusa Frameworkのバージョン
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

このプラグインの規約プロパティである ``asakusafwVersion`` はデプロイメント構成に含むAsakusa Frameworkです。これは、アプリケーションプロジェクトで利用するAsakusa Frameworkとは個別に設定が可能です。

以下は、デプロイメントアーカイブに含めるAsakusa FrameworkのバージョンをHadoop2系向けバージョンに変更するビルドスクリプトの例です。

..  code-block:: groovy

    asakusafw {
        asakusafwVersion = '0.6.0'
        ....
    }
    
    asakusafwOrganizer {
        asakusafwVersion = "${asakusafw.asakusafwVersion}-hadoop2"
    }

Framework Organizer Pluginを単体で利用する
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

プロジェクトテンプレートに含まれるビルドスクリプトのように、Framework Organizer Pluginはプロジェクトのビルドスクリプトに適用して利用することができます。この利用方法には、Batch Application Pluginと連携してプロジェクトのバッチアプリケーションやプロジェクトに定義したディストリビューションを含めることができるといったメリットがあります。

このような利用方法のほかに、Framework Organizer Pluginをあるプロジェクトとは独立して利用することができます。この利用方法には、複数のプロジェクトにわたって共通のデプロイメント構成を管理したい場合や、プロジェクトで利用するAsakusa Framework のバージョンと運用環境で利用するAsakusa Frameworkのバージョンが異なり、これを独立して管理したい、といった場合に有効です。

制約事項
--------

Asakusa Framework の現在バージョン |version| におけるAsakusa Gradle Pluginの制約事項を以下に挙げます。

* ThunderGate [#]_ には未対応です。
* レガシーモジュール [#]_ には未対応です。

..  [#] :doc:`../thundergate/index`

..  [#] :doc:`../application/legacy-module-guide`

Asakusa Gradle Plugin マイグレーションガイド
============================================

ここでは、Asakusa Gradle Plugin のバージョンアップ手順や、 従来のAsakusa Frameworkが提供するMavenベースのビルドシステムからAsakusa Gradle Pluginを使ったビルドシステムに移行するための手順を説明します。

なお、Asakusa Frameworkのマイグレーション情報については、 :doc:`migration-guide` も参考にしてください。

Asakusa Gradle Pluginのバージョンアップ
---------------------------------------

ここではプロジェクトテンプレートで提供されるビルドスクリプトを例に、Asakusa Gradle Plugin をバージョンアップする基本的な手順を説明します。

Asakusa Gradle Plugin をバージョンアップするには、ビルドスクリプト内のAsakusa Gradle Plugin のバージョン指定と、Asakusa Frameworkのバージョン指定をそれぞれ変更したのち、開発環境の再セットアップを行います。

Asakusa Gradle Pluginのバージョン指定
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

ビルドスクリプト内のAsakusa Gradle Pluginのバージョン ``buildscript/dependencies/classpath@version`` を変更します。プロジェクトテンプレートで提供されるビルドスクリプトでは、6行目の ``version`` の値を変更します。

..  code-block:: groovy

      1 buildscript {
      2     repositories {
      3         maven { url 'http://asakusafw.s3.amazonaws.com/maven/releases' }
      4     }
      5     dependencies {
      6         classpath group: 'com.asakusafw', name: 'asakusa-gradle-plugins', version: '0.6.0'
      7     }
      8 }

Asakusa Frameworkのバージョン指定
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

ビルドスクリプト内のAsakusa Frameworkのバージョン ``asakusafw/asakusafwVersion`` を変更します。プロジェクトテンプレートで提供されるビルドスクリプトでは、15行目の ``asakusafwVersion`` の値を変更します。

..  code-block:: groovy

     14 asakusafw {
     15     asakusafwVersion = '0.6.0'
     16 
     17     modelgen {
     18         modelgenSourcePackage 'com.example.modelgen'

Asakusa Frameworkの再インストール
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

開発環境のAsakusa Frameworkを再インストールします。

..  code-block:: sh

    ./gradlew installAsakusafw

マイグレーションしたビルド設定の確認
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

プロジェクトのフルビルドを行い、ビルドが成功することを確認してください。

Eclipseを利用している場合はクラスパス設定が変更になっているため必ずEclipseの作業前にGradleの ``eclipse`` タスクを実行してください。

..  code-block:: sh

    ./gradlew cleanEclipse clean build eclipse


Mavenプロジェクトのマイグレーション
-----------------------------------

:doc:`../introduction/start-guide` や :doc:`../application/maven-archetype` で記載されている手順に従って構築したMavenベースのビルド定義を持つアプリケーションプロジェクトでAsakusa Gradle Pluginを使うには、まずこのプロジェクトにAsakusa Gradle Plugin用のビルドスクリプトを配置します。

アプリケーションプロジェクトのプロジェクト定義( ``pom.xml`` )やビルド定義ファイル( ``build.properties`` )を編集していた場合は、この設定をAsakusa Gradle Plugin用のビルドスクリプトに反映します。

プロジェクトのソースディレクトリについてはマイグレーション作業は不要で、そのまま利用することが出来ます。

以降では、Mavenアーキタイプで作成したプロジェクトに対してAsakusa Gradle Pluginのプロジェクトテンプレートを適用するマイグレーション手順を紹介します。

..  attention::
    プロジェクトのマイグレーション作業前に、プロジェクトのバックアップとリストアの確認など、マイグレーション作業にトラブルが発生した場合に元に戻せる状態となっていることを確認してください。

プロジェクトテンプレートの適用
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

`Asakusa Gradle Pluginの導入`_  で説明したAsakusa Gradle Pluginのプロジェクトテンプレートに含まれるファイル一式をアプリケーションプロジェクトに適用します。

事前に確認すべき点として、MavenアーキタイプとAsakusa Gradle Pluginのプロジェクトテンプレートの両方に含まれるファイルに対しては、従来の設定をそのまま利用したい場合にはテンプレート適用前にファイルを退避して、テンプレート適用後にこれを反映する必要があります。

MavenアーキタイプとAsakusa Gradle Pluginのプロジェクトテンプレートの両方に含まれるファイルの一覧を以下に示します [#]_ 。

..  list-table:: 
    :widths: 234 218
    :header-rows: 1

    * - ファイル
      - 説明
    * -  ``src/test/resources/asakusa-resources.xml`` 
      - 演算子のテスト実行時に使用される実行時プラグイン設定ファイル
    * -  ``src/test/resources/logback-test.xml`` 
      - ビルド/テスト実行時に使用されるログ定義ファイル

..  [#] これらのファイルをデフォルト設定のまま利用している場合は、プロジェクトテンプレートの内容で上書きしてください。

以下は、ダウンロードしたプロジェクトテンプレートを ``$HOME/workspace/migrate-app`` に適用する例です。

..  code-block:: sh

    cd ~/Downloads
    tar xf asakusa-project-template-*.tar.gz
    cp -r asakusa-project-template/* ~/workspace/migrate-app

プロジェクト定義のマイグレーション
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

従来のMavenのプロジェクト定義( ``pom.xml`` )の内容をGradleのビルドスクリプト( ``build.gradle`` )に反映します。

``pom.xml`` の代表的なカスタマイズ内容として、アプリケーションで利用するライブラリ追加による依存関係の設定があります。これは ``pom.xml`` 上では ``dependencies`` 配下に定義していました。

Gradle、およびAsakusa Gradle Pluginでは従来のMavenベースの依存関係の管理から一部機能が変更になっているため、  `ビルド設定のカスタマイズ`_  の内容をよく確認した上でアプリケーションに対して適切な設定を行ってください。

その他に確認すべき点は、  `標準プロジェクトプロパティ`_  の内容です。これに相当する内容はMavenアーキタイプでプロジェクトを作成する際に入力した内容が ``pom.xml`` のトップレベルの階層に定義されています。以下、この箇所に該当する ``pom.xml`` の設定例です。

..  code-block:: xml

      4     <name>Example Application</name>
      5     <groupId>com.example</groupId>
      6     <artifactId>migrate-app</artifactId>
      7     <version>1.0-SNAPSHOT</version>

Gradleではこれらのプロパティについてビルドスクリプト上の定義は必須ではありませんが、必要に応じて ``pom.xml`` の設定を反映するとよいでしょう。

ビルド定義ファイルのマイグレーション
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

従来のMavenのビルド定義ファイル( ``build.properties`` )の内容をGradleのビルドスクリプト( ``build.gradle`` )に反映します。

ビルド定義ファイルの内容は、移行後の ``build.gradle`` では  `Batch Application Plugin`_  上の規約プロパティとして定義します。

ここで必ず確認すべき項目は、Mavenアーキタイプでプロジェクトを作成した内容が反映される以下のプロパティです。

..  list-table::
    :widths: 113 113 113 113
    :header-rows: 1

    * - プロパティ
      - デフォルト値
      - 対応するbuild.gradle上の設定項目
      - 説明
    * -  ``asakusa.package.default`` 
      -  ``${groupId}/batchapp`` 
      -  ``compiler.compiledSourcePackage`` 
      - DSLコンパイラが生成する各クラスに使用されるパッケージ名
    * -  ``asakusa.modelgen.package`` 
      -  ``${groupId}/modelgen`` 
      -  ``modelgen.modelgenSourcePackage`` 
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

Eclipseを利用している場合はクラスパス設定が変更になっているため必ずEclipseの作業前にGradleの ``eclipse`` タスクを実行してください。

..  code-block:: sh

    ./gradlew cleanEclipse clean build eclipse
    

Mavenビルド用ファイルの削除
~~~~~~~~~~~~~~~~~~~~~~~~~~~

従来のMavenベースのビルドシステムで利用していたファイルは、そのままプロジェクト上に残しても問題ありません。MavenベースのビルドシステムとGradleのビルドシステムを併用することもできます。

..  note::
    MavenベースのビルドシステムとGradleのビルドシステムの併用は設定を多重で持つことや、IDEとの連携時に注意が必要であるなどデメリットも多いことに注意してください。

Mavenベースのビルドシステムで利用していたファイルを削除したい場合は、プロジェクト配下の以下のファイル、ディレクトリを削除してください。

*  ``pom.xml`` 
*  ``build.properties`` 
*  ``target`` 

Framework Organizerのマイグレーション
-------------------------------------

従来の Framework Organizer [#]_ で提供していた機能は、 `Framework Organizer Plugin`_  によって提供されます。詳しくは Framework Organizer Plugin のドキュメントを参照してください。

..  [#] :doc:`../administration/framework-organizer`
