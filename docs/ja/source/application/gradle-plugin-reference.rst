==================================
Asakusa Gradle Plugin リファレンス
==================================

この文書では、Asakusa Gradle Pluginが提供する機能とインターフェースについて個々に解説します。

リポジトリ
==========

Asakusa Gradle PluginはAsakusa FrameworkのMavenリポジトリに以下のMavenアーティファクトとして登録されています。

..  list-table:: Asakusa Gradle PluginのMavenアーティファクト
    :widths: 2 2 6
    :header-rows: 1

    * - グループID
      - アーティファクトID
      - 説明
    * - ``com.asakusafw``
      - ``asakusa-gradle-plugins``
      - Asakusa Gradle Pluginの標準機能を提供する
    * - ``com.asakusafw.spark``
      - ``asakusa-spark-gradle``
      - Asakusa Gradle Pluginの標準機能に加えて、 :asakusa-on-spark:`Asakusa on Spark <index.html>` 向けの機能を提供する。
    * - ``com.asakusafw.m3bp``
      - ``asakusa-m3bp-gradle``
      - Asakusa Gradle Pluginの標準機能に加えて、 :asakusa-on-m3bp:`Asakusa on M3BP <index.html>` 向けの機能を提供する。

アプリケーションプロジェクトからAsakusa Gradle Pluginを利用する場合、ビルドスクリプトに下記を含めます。

..  code-block:: groovy
    :caption: build.gradle
    :name: build.gradle-gradle-plugin-reference-1

    buildscript {
        repositories {
            maven { url 'http://asakusafw.s3.amazonaws.com/maven/releases' }
        }
        dependencies {
            classpath group: 'com.asakusafw', name: 'asakusa-gradle-plugins', version: '0.8.1'
        }
    }

..  seealso::
    Asakusa on Spark , |ASAKUSA_ON_M3BP| を利用する場合の設定例については、それぞれ以下のドキュメントを確認してください。

    * :asakusa-on-spark:`Asakusa on Spark ユーザガイド <user-guide.html>`
    * :asakusa-on-m3bp:`Asakusa on M3BP ユーザガイド <user-guide.html>`

Asakusa Gradle Plugin 一覧
==========================

Asakusa Gradle Pluginはいくつかのプラグインから構成されています。以下にその一覧を示します。

..  list-table:: Asakusa Gradle Plugin 一覧
    :widths: 2 2 1 5
    :header-rows: 1

    * - プラグインID
      - プラグイン名
      - 自動適用
      - 説明
    * - ``asakusafw-sdk``
      - `Batch Application Plugin`_
      - ``java``
      - Asakusa Frameworkのバッチアプリケーションを開発を行うための支援機能をプロジェクトに追加する。
    * - ``asakusafw-organizer``
      - `Framework Organizer Plugin`_
      - ``-``
      - Asakusa Frameworkを利用した開発環境の構築や、運用環境へのデプロイを行うための援機能を提供する。

また、以下のプラグインは上記のプラグインに対して特定プラットフォームやコンポーネントを利用するための機能を追加します。

..  list-table:: Asakusa Gradle Plugin 拡張プラグイン一覧
    :widths: 2 2 1 5
    :header-rows: 1

    * - プラグインID
      - プラグイン名
      - 自動適用
      - 説明
    * - ``asakusafw-mapreduce``
      - Asakusa on MapReduce Plugin
      - ``-``
      - MapReduce向けのバッチアプリケーションを生成、実行するための機能を導入する。
    * - ``asakusafw-spark``
      - Asakusa on Spark Plugin
      - ``-``
      - Spark向けのバッチアプリケーションを生成、実行するための機能を導入する。
    * - ``asakusafw-m3bp``
      - |ASAKUSA_ON_M3BP| Plugin
      - ``-``
      - |M3BP_ENGINE| 向けのバッチアプリケーションを生成、実行するための機能を導入する。
    * - ``asakusafw-legacy``
      - Legacy Modules Plugin
      - ``-``
      - ThunderGateなどのレガシーモジュールを利用するための機能を導入する

使用方法
--------

Asakusa Gradle Pluginの各プラグインを使うためには、ビルドスクリプトに ``apply plugin: <プラグインID>`` という設定を含めます。
以下、設定例です。

..  code-block:: groovy
    :caption: build.gradle
    :name: build.gradle-gradle-plugin-reference-2

    apply plugin: 'asakusafw-sdk'
    apply plugin: 'asakusafw-organizer'
    apply plugin: 'asakusafw-mapreduce'
    apply plugin: 'asakusafw-spark'

.. _batch-application-plugin-reference:

Batch Application Plugin
========================

Batch Application Plugin は、Asakusa Framework の バッチアプリケーション開発を行うための支援機能を提供します。

Batch Application Plugin はAsakusa Framework の バッチアプリケーションプロジェクトに対して、以下のような機能を提供します。

* DMDLスクリプトからデータモデルクラスを生成するタスクの提供
* Gradle標準のJavaコンパイルタスクに対して、Operator DSLコンパイラによる演算子実装クラス、演算子ファクトリクラスの生成を行うための設定を追加
* Asakusa DSLとして記述したJavaソースファイル一式に対して、Batch DSLコンパイラによるバッチアプリケーション実行モジュールの生成を行うタスクの提供
* テストドライバーを利用したテストケースを作成するためのテストデータ定義シートのテンプレートファイルを生成するタスクの提供
* Gradle標準のEclipseのタスクに対して、Asakusa Framework用の設定を追加

..  note::
    このプラグインはGradleが提供するJavaプラグインやEclipseプラグインを拡張して作成されています。

タスク
------

Batch Application Plugin は、以下のタスクをプロジェクトに追加します。

..  list-table:: Batch Application Plugin - タスク
    :widths: 2 2 2 4
    :header-rows: 1

    * - タスク名
      - プラグイン
      - 型
      - 説明
    * - :program:`asakusaVersion`
      - ``asakusafw-sdk``
      - ``-``
      - プロジェクトが利用する各コンポーネントのバージョンを表示する
    * - :program:`asakusaUpgrade`
      - ``asakusafw-sdk``
      - ``-``
      - プロジェクトが利用するGradleコンポーネントをアップデートする
    * - :program:`compileDMDL`
      - ``asakusafw-sdk``
      - ``CompileDmdlTask`` [#]_
      - DMDLコンパイラを使ってデータモデルクラスを生成する
    * - :program:`compileBatchapp`
      - ``asakusafw-sdk``
      - ``CompileBatchappTask`` [#]_
      - プロジェクトのビルド構成に基づくBatch DSLコンパイラを使ってバッチアプリケーションを生成する
    * - :program:`mapreduceCompileBatchapps`
      - ``asakusafw-mapreduce``
      - ``-``
      - MapReduce DSLコンパイラを使ってバッチアプリケーションを生成する
    * - :program:`sparkCompileBatchapps`
      - ``asakusafw-spark``
      - ``-``
      - Spark DSLコンパイラを使ってバッチアプリケーションを生成する
    * - :program:`m3bpCompileBatchapps`
      - ``asakusafw-m3bp``
      - ``-``
      - M\ :sup:`3`\ BP DSLコンパイラを使ってバッチアプリケーションを生成する
    * - :program:`jarBatchapp`
      - ``asakusafw-sdk``
      - ``Jar``
      - バッチアプリケーションアーカイブを生成する
    * - :program:`generateTestbook`
      - ``asakusafw-sdk``
      - ``GenerateTestbookTask`` [#]_
      - テストデータ定義シートを生成する
    * - :program:`testRunBatchapp`
      - ``asakusafw-mapreduce``
      - ``RunBatchappTask`` [#]_
      - バッチテストランナーを実行する
    * - :program:`summarizeYaessJob`
      - ``asakusafw-sdk``
      - ``AnalyzeYaessLogTask`` [#]_
      - YAESS Log Analyzerを実行する [#]_
    * - :program:`generateHiveDDL`
      - ``asakusafw-sdk``
      - ``GenerateHiveDdlTask`` [#]_
      - DMDLからHive用のDDLファイルを生成する

..  [#] :gradledoc:`com.asakusafw.gradle.tasks.CompileDmdlTask`
..  [#] :gradledoc:`com.asakusafw.gradle.tasks.CompileBatchappTask`
..  [#] :gradledoc:`com.asakusafw.gradle.tasks.GenerateTestbookTask`
..  [#] :gradledoc:`com.asakusafw.gradle.tasks.RunBatchappTask`
..  [#] :gradledoc:`com.asakusafw.gradle.tasks.AnalyzeYaessLogTask`
..  [#] YAESS Log Analyzerやその使い方については、 :doc:`yaess-log-visualization` を参照してください。
..  [#] :gradledoc:`com.asakusafw.gradle.tasks.GenerateHiveDdlTask`

また、Batch Application Pluginは以下のタスクに対してタスク依存関係を追加します。

..  list-table:: Batch Application Plugin - タスク依存関係
    :widths: 3 7
    :header-rows: 1

    * - タスク名
      - 依存先
    * - :program:`compileJava`
      - :program:`compileDMDL`
    * - :program:`compileBatchapp`
      - :program:`classes`, :program:`mapreduceCompileBatchapps`, :program:`sparkCompileBatchapps`, :program:`m3bpCompileBatchapps`
    * - :program:`jarBatchapp`
      - :program:`compileBatchapp`
    * - :program:`assemble`
      - :program:`compileBatchapp`

依存関係の管理
--------------

Batch Application Plugin は、以下の依存関係設定をプロジェクトに追加します。

..  list-table:: Batch Application Plugin - 依存関係設定
    :widths: 1 9
    :header-rows: 1

    * - 名前
      - 説明
    * - ``provided``
      - アプリケーションのビルド時に依存するが、アプリケーションの実行時には実行環境に配置されている実体(jarファイルなど)を使用する依存関係設定
    * - ``embedded``
      - プロジェクトディレクトリ配下に実体(jarファイルなど)を配置する依存関係設定

リポジトリ
----------

Batch Application Plugin は、以下のリポジトリをプロジェクトに追加します。

..  list-table:: Batch Application Plugin - リポジトリ
    :widths: 2 2
    :header-rows: 1

    * - 名前/URL
      - 説明
    * - ``http://repo1.maven.org/maven2/``
      - Mavenのセントラルリポジトリ
    * - ``http://asakusafw.s3.amazonaws.com/maven/releases``
      - Asakusa Frameworkのリリース用Mavenリポジトリ
    * - ``http://asakusafw.s3.amazonaws.com/maven/snapshots``
      - Asakusa Frameworkのスナップショット用Mavenリポジトリ

..  tip::
    プロジェクトに固有のリポジトリを追加する場合、ビルドスクリプトのプラグイン定義 ( ``apply plugin: xxx`` ) 位置の前にリポジトリ定義を追加すると、プラグインが標準で設定するリポジトリよりも優先して使用されます。
    開発環境でインハウスリポジトリを優先して利用したい場合などは、プラグイン定義の前にリポジトリ定義を追加するとよいでしょう。

規約プロパティ
--------------

Batch Application Pluginの規約プロパティはビルドスクリプトから 参照名 ``asakusafw`` でアクセスできます [#]_ 。
この規約オブジェクトは以下のプロパティを持ちます。

..  list-table:: Batch Application Plugin - 規約プロパティ ( ``asakusafw`` ブロック )
    :widths: 2 1 2 5
    :header-rows: 1

    * - プロパティ名
      - 型
      - デフォルト値
      - 説明
    * - ``maxHeapSize``
      - String
      - ``1024m``
      - プラグインが実行するJavaプロセスの最大ヒープサイズ
    * - ``logbackConf``
      - String
      - ``src/${project.sourceSets.test.name}/resources/logback-test.xml``
      - プロジェクトのLogback設定ファイル [#]_
    * - ``basePackage``
      - String
      - ``${project.group}``
      - プラグインの各タスクでJavaソースコードの生成時に指定する基底Javaパッケージ

..  [#] これらのプロパティは規約オブジェクト :gradledoc:`com.asakusafw.gradle.plugins.AsakusafwPluginConvention` が提供します。
..  [#] Logback設定ファイルの詳細は次のドキュメントを参照してください: http://logback.qos.ch/manual/configuration.html

DMDLプロパティ
~~~~~~~~~~~~~~

DMDLに関する規約プロパティは、 ``asakusafw`` ブロック内の参照名 ``dmdl`` でアクセスできます [#]_ 。
この規約オブジェクトは以下のプロパティを持ちます。

..  list-table:: Batch Application Plugin - DMDLプロパティ ( ``dmdl`` ブロック )
    :widths: 2 1 2 5
    :header-rows: 1

    * - プロパティ名
      - 型
      - デフォルト値
      - 説明
    * - ``dmdlEncoding``
      - String
      - ``UTF-8``
      - DMDLスクリプトのエンコーディング
    * - ``dmdlSourceDirectory``
      - String
      - ``src/${project.sourceSets.main.name}/dmdl``
      - DMDLスクリプトのソースディレクトリ

..  [#] これらのプロパティは規約オブジェクト :gradledoc:`com.asakusafw.gradle.plugins.AsakusafwPluginConvention.DmdlConfiguration` が提供します。

データモデル生成プロパティ
~~~~~~~~~~~~~~~~~~~~~~~~~~

データモデル生成に関する規約プロパティは、 ``asakusafw`` ブロック内の参照名 ``modelgen`` でアクセスできます [#]_ 。
この規約オブジェクトは以下のプロパティを持ちます。

..  list-table:: Batch Application Plugin - データモデル生成プロパティ ( ``modelgen`` ブロック )
    :widths: 2 1 2 5
    :header-rows: 1

    * - プロパティ名
      - 型
      - デフォルト値
      - 説明
    * - ``modelgenSourcePackage``
      - String
      - ``${asakusafw.basePackage}.modelgen``
      - データモデルクラスに使用されるパッケージ名
    * - ``modelgenSourceDirectory``
      - String
      - ``${project.buildDir}/generated-sources/modelgen``
      - データモデルクラスのソースディレクトリ

..  [#] これらのプロパティは規約オブジェクト :gradledoc:`com.asakusafw.gradle.plugins.AsakusafwPluginConvention.ModelgenConfiguration` が提供します。

Javaコンパイラプロパティ
~~~~~~~~~~~~~~~~~~~~~~~~

Javaコンパイラ関する規約プロパティは、 ``asakusafw`` ブロック内の参照名 ``javac`` でアクセスできます [#]_ 。
この規約オブジェクトは以下のプロパティを持ちます。

..  list-table:: Batch Application Plugin - Javaコンパイラプロパティ ( ``javac`` ブロック )
    :widths: 2 1 2 5
    :header-rows: 1

    * - プロパティ名
      - 型
      - デフォルト値
      - 説明
    * - ``annotationSourceDirectory``
      - String
      - ``${project.buildDir}/generated-sources/annotations``
      - アノテーションプロセッサが生成するJavaソースの出力先
    * - ``sourceEncoding``
      - String
      - ``UTF-8``
      - プロジェクトのソースファイルのエンコーディング
    * - ``sourceCompatibility``
      - JavaVersion。Stringも利用可能。 例： ``'1.7'`` [#]_
      - ``1.7``
      - Javaソースのコンパイル時に使用するJavaバージョン互換性
    * - ``targetCompatibility``
      - JavaVersion。Stringも利用可能。例： ``'1.7'``
      - ``1.7``
      - クラス生成のターゲットJavaバージョン

..  [#] これらのプロパティは規約オブジェクト :gradledoc:`com.asakusafw.gradle.plugins.AsakusafwPluginConvention.JavacConfiguration` が提供します。
..  [#] JDK6を利用するなどの場合に変更します。 詳しくは :doc:`using-jdk` を参照してください。

DSLコンパイラプロパティ
~~~~~~~~~~~~~~~~~~~~~~~

..  attention::
    バージョン 0.8.0 より、 ``compiler`` ブロックの指定は非推奨となりました。
    MapReduceコンパイラに対する設定は、後述の MapReduceプロパティ を使用してください。

..  attention::
    バージョン |version| 時点では、 ``compiler`` ブロックではMapReduce DSLコンパイラに対する設定がおこなわれます。

DSLコンパイラ関する規約プロパティは、 ``asakusafw`` ブロック内の参照名 ``compiler`` でアクセスできます [#]_ 。

この規約オブジェクトは以下のプロパティを持ちます。

..  list-table:: Batch Application Plugin - DSLコンパイラプロパティ ( ``compiler`` ブロック )
    :widths: 2 1 2 5
    :header-rows: 1

    * - プロパティ名
      - 型
      - デフォルト値
      - 説明
    * - ``compiledSourcePackage``
      - String
      - ``${asakusafw.basePackage}.batchapp``
      - DSLコンパイラが生成する各クラスに使用されるパッケージ名
    * - ``compiledSourceDirectory``
      - String
      - ``${project.buildDir}/batchc``
      - DSLコンパイラが生成する成果物の出力先
    * - ``compilerOptions``
      - String
      - ``XjavaVersion=${targetCompatibility}`` [#]_
      - DSLコンパイラオプション [#]_
    * - ``compilerWorkDirectory``
      - String
      - ``未指定``
      - DSLコンパイラのワーキングディレクトリ
    * - ``hadoopWorkDirectory``
      - String
      - ``target/hadoopwork/${execution_id}``
      - DSLコンパイラが生成するアプリケーション(Hadoopジョブ)が使用するHadoop上のワーキングディレクトリ

..  [#] これらのプロパティは規約オブジェクト :gradledoc:`com.asakusafw.gradle.plugins.AsakusafwPluginConvention.CompilerConfiguration` が提供します。
..  [#] `Javaコンパイラプロパティ`_ の ``targetCompatibility`` の値が設定されます。
..  [#] DSLコンパイラオプションについては、 :doc:`../dsl/mapreduce-compiler` - :ref:`mapreduce-compile-options` を参照してください。

MapReduceプロパティ
~~~~~~~~~~~~~~~~~~~

MapReduce DSLコンパイラ関する規約プロパティは、 ``asakusafw`` ブロック内の参照名 ``mapreduce`` でアクセスできます [#]_ 。
この規約オブジェクトは以下のプロパティを持ちます。

..  list-table:: Batch Application Plugin - MapReduceコンパイラプロパティ ( ``mapreduce`` ブロック )
    :widths: 2 1 2 5
    :header-rows: 1

    * - プロパティ名
      - 型
      - デフォルト値
      - 説明
    * - ``outputDirectory``
      - String
      - ``$buildDir/batchc``
      - コンパイラの出力先を指定する。文字列や java.io.File などで指定し、相対パスが指定された場合にはプロジェクトからの相対パスとして取り扱う。
    * - ``include``
      - String, List<String>
      - ``null`` (すべて)
      - コンパイルの対象に含めるバッチクラス名のパターンを指定する。バッチクラス名には ``*`` でワイルドカードを含めることが可能。また、バッチクラス名のリストを指定した場合、それらのパターンのいずれかにマッチしたバッチクラスのみをコンパイルの対象に含める。 [#]_
    * - ``exclude``
      - String, List<String>
      - ``null`` (除外しない)
      - コンパイルの対象に含めるバッチクラス名のパターンを指定する。バッチクラス名には ``*`` でワイルドカードを含めることが可能。また、バッチクラス名のリストを指定した場合、それらのパターンのいずれかにマッチしたバッチクラスのみをコンパイルの対象に含める。
    * - ``runtimeWorkingDirectory``
      - String
      - ``null``  (コンパイラの標準設定を利用する)
      - 実行時のテンポラリワーキングディレクトリのパスを指定する。パスにはURIやカレントワーキングディレクトリからの相対パスを指定可能。未指定の場合、コンパイラの標準設定である「 ``target/hadoopwork`` 」を利用する。
    * - ``compilerProperties``
      - Map<String, String>
      - ``null``
      - MapReduceコンパイラのコンパイラオプション [#]_ を追加する。この値はマップ型 ( ``java.util.Map`` ) であるため、プロパティのキーと値をマップのキーと値として追加可能。
    * - ``failOnError``
      - boolean
      - ``true`` (即座にコンパイルを停止する)
      - コンパイルエラーが発生したら即座にコンパイルを停止するかどうかを選択する。コンパイルエラーが発生した際に、 true を指定した場合にはコンパイルをすぐに停止し、 false を指定した場合には最後までコンパイルを実施する。

..  [#] これらのプロパティは規約オブジェクト :gradledoc:`com.asakusafw.gradle.plugins.AsakusafwCompilerExtension` が提供します。
..  [#] ``include``, ``exclude`` プロパティの利用方法については、 :doc:`gradle-plugin` - :ref:`gradle-plugin-dslcompile-filter` を参照してください。
..  [#] DSLコンパイラオプションについては、 :doc:`../dsl/mapreduce-compiler` - :ref:`mapreduce-compile-options` を参照してください。

Sparkプロパティ
~~~~~~~~~~~~~~~

Spark DSLコンパイラに関する規約プロパティは、 ``asakusafw`` ブロック内の参照名 ``spark`` でアクセスできます。

この規約オブジェクトについては、Asakusa on Sparkの以下のドキュメントを参照してください。

* :asakusa-on-spark:`Asakusa on Spark リファレンス - Batch Application Plguin (asakusafw) への拡張 <reference.html#batch-application-plugin-asakusafw>`

M3BPプロパティ
~~~~~~~~~~~~~~

M\ :sup:`3`\ BP DSLコンパイラに関する規約プロパティは、 ``asakusafw`` ブロック内の参照名 ``m3bp`` でアクセスできます。

この規約オブジェクトについては、 |ASAKUSA_ON_M3BP| の以下のドキュメントを参照してください。

* :asakusa-on-m3bp:`Asakusa on M3BP リファレンス - Batch Application Plguin (asakusafw) への拡張 <reference.html#batch-application-plugin-asakusafw>`

テストツールプロパティ
~~~~~~~~~~~~~~~~~~~~~~

テストツールに関する規約プロパティは、 ``asakusafw`` ブロック内の参照名 ``testtools`` でアクセスできます [#]_ 。
この規約オブジェクトは以下のプロパティを持ちます。

..  list-table:: Batch Application Plugin - テストツールプロパティ ( ``testtools`` ブロック )
    :widths: 2 1 2 5
    :header-rows: 1

    * - プロパティ名
      - 型
      - デフォルト値
      - 説明
    * - ``testDataSheetFormat``
      - String
      - ``ALL``
      - テストデータ定義シートのフォーマット [#]_
    * - ``testDataSheetDirectory``
      - String
      - ``${project.buildDir}/excel``
      - テストデータ定義シートの出力先

..  [#] これらのプロパティは規約オブジェクト :gradledoc:`com.asakusafw.gradle.plugins.AsakusafwPluginConvention.TestToolsConfiguration` が提供します。
..  [#] テストデータ定義シートのフォーマット指定値は、 :doc:`../testing/using-excel` - :ref:`testdata-generator-excel-format` を参照してください。

Eclipse Pluginの拡張
--------------------

Batch Application Plugin は Gradleが提供するEclipse Pluginのタスクに対して、以下のようなEclipseプロジェクトの追加設定を行います。

* OperatorDSLコンパイラを実行するためのAnnotation Processorの設定
* Javaのバージョンやエンコーディングに関する設定

また、Batch Application Pluginが設定する規約プロパティの情報を :file:`.settings/com.asakusafw.asakusafw.prefs` に出力します。

GradleからEclipseプロジェクト用の定義ファイルを生成する方法については、 :ref:`gradle-plugin-using-eclipse` を参照してください。

.. _gradle-plugin-using-idea:

IDEA Pluginの拡張
-----------------

..  attention::
    Asakusa Framework バージョン |version| では、 IDEA Pluginの拡張は試験的機能として提供しています。

Batch Application Plugin は Gradleが提供するIDEA Pluginのタスクに対して、以下のようなIntelliJ IDEAプロジェクトの追加設定を行います。

* プロジェクトに含むモジュールの構成(ソースディレクトリに関する設定など)
* OperatorDSLコンパイラを実行するためのAnnotation Processorの設定
* Javaのバージョンやコンパイラに関する設定

アプリケーション開発用の統合開発環境(IDE)にIntelliJ IDEAを使用する場合、開発環境にIntelliJ IDEAをインストールした上で、プロジェクトに対してIntelliJ IDEAプロジェクト用の定義ファイルを追加します。

IntelliJ IDEAプロジェクト用の定義ファイルを作成するには、:program:`idea` タスクを実行します。

..  code-block:: sh

    ./gradlew idea

このコマンドを実行することによって、プロジェクトディレクトリに対してIntelliJ IDEA用の定義ファイルやクラスパスに対応したソースディレクトリなどが追加されます。
これにより、IntelliJ IDEAからプロジェクトをインポートすることが可能になります。

..  tip::
    IntelliJ IDEAからプロジェクトをインポートするには、Welcome Screen(プロジェクトを開いていない時に表示されるダイアログ)から :guilabel:`Import` を選択するか、メニューから :menuselection:`File --> Import Project...` を選択し、プロジェクトディレクトリを選択します。
    インポートウィザードが開始されるので、以下の例を参考にしてプロジェクトのインポートを行います。

    * インポートウィザードの最初の画面では、:guilabel:`Import project from external model` を選択し、 :guilabel:`Gradle` を選択して :guilabel:`Next` を押下します。
    * インポートウィザードの次の画面の :guilabel:`Project format:` は :guilabel:`ipr (file based)` を選択してください。
      デフォルトの :guilabel:`.idea (directory based)` ではGradleの :program:`idea` タスクが生成した設定ファイルが使用されません。

.. _framework-organizer-plugin-reference:

Framework Organizer Plugin
==========================

Framework Organizer Plugin は、Asakusa Framework を 利用した開発環境の構築や、運用環境に対するデプロイモジュールの構成管理機能を提供します。

Framework Organizer Plugin が提供する機能には次のようなものがあります。

* Asakusa Frameworkのデプロイメントモジュールの構成を定義し、デプロイメントアーカイブを生成するタスクの提供
* Asakusa Frameworkが提供する各コンポーネントの設定や拡張モジュールの利用などを環境ごとに設定するプロファイル管理機能の提供
* Asakusa Frameworkを開発環境へインストールするタスクの提供

タスク
------

Framework Organizer Plugin は、以下のタスクを定義します。

..  list-table:: Framework Organizer Plugin - タスク
    :widths: 152 121 48 131
    :header-rows: 1

    * - タスク名
      - 依存先
      - 型
      - 説明
    * - :program:`assembleAsakusafw`
      - ``-``
      - ``Task``
      - 運用環境向けのデプロイメント構成を持つデプロイメントアーカイブを生成する
    * - :program:`installAsakusafw`
      - ``-``
      - ``Task``
      - 開発環境向けのデプロイメント構成をローカル環境にインストールする

..  note::
    Framework Organizer Pluginは上記のタスク一覧の他に、プラグイン内部で ``attach`` から始まるタスクを生成し利用します。

リポジトリ
----------

Framework Organizer Plugin は、 `Batch Application Plugin`_ のリポジトリ定義と共通の設定を使用します。

..  tip::
    `Batch Application Plugin`_ と同様に、プロジェクトに固有のリポジトリを追加する場合、ビルドスクリプトのプラグイン定義 ( ``apply plugin: 'xxx'`` ) 位置の前にリポジトリ定義を追加すると、プラグインが標準で設定するリポジトリよりも優先して使用されます。

規約プロパティ
--------------

Framework Organizer Plugin の規約プロパティはビルドスクリプトから 参照名  ``asakusafwOrganizer`` でアクセスできます [#]_ 。
この規約オブジェクトは以下のプロパティを持ちます。

..  list-table:: Framework Organizer Plugin - 規約プロパティ
    :widths: 135 102 101 113
    :header-rows: 1

    * - プロパティ名
      - 型
      - デフォルト値
      - 説明
    * - ``assembleDir``
      - String
      - ``${project.buildDir}/asakusafw-assembly``
      - デプロイメント構成の構築時に利用するワーキングディレクトリのプレフィックス

..  [#] これらのプロパティは規約オブジェクト :gradledoc:`com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention` が提供します。

バッチアプリケーションプロパティ
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

バッチアプリケーションの構成に関する規約プロパティは、 ``asakusafwOrganizer`` ブロック内の参照名 ``batchapps`` でアクセスできます [#]_ 。
この規約オブジェクトは以下のプロパティを持ちます。

..  list-table:: Framework Organizer Plugin - バッチアプリケーションプロパティ ( ``batchapps`` ブロック )
    :widths: 2 1 2 5
    :header-rows: 1

    * - プロパティ名
      - 型
      - デフォルト値
      - 説明
    * - ``enabled``
      - boolean
      - true
      - この値をtrueにするとデプロイメントアーカイブにプロジェクトのバッチアプリケーションを含める

..  [#] これらのプロパティは規約オブジェクト :gradledoc:`com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.BatchappsConfiguration` が提供します。

MapReduceプロパティ
~~~~~~~~~~~~~~~~~~~

MapReduce DSLコンパイラが生成するバッチアプリケーションの構成に関する規約プロパティは、 ``asakusafwOrganizer`` ブロック内の参照名 ``mapreduce`` でアクセスできます [#]_ 。
この規約オブジェクトは以下のプロパティを持ちます。

..  list-table:: Batch Application Plugin - MapReduceコンパイラプロパティ ( ``mapreduce`` ブロック )
    :widths: 2 1 2 5
    :header-rows: 1

    * - プロパティ名
      - 型
      - デフォルト値
      - 説明
    * - ``enabled``
      - boolean
      - true
      - この値をtrueにするとデプロイメントアーカイブにMapReduce DSLコンパイラが生成するプロジェクトのバッチアプリケーションを含める

..  [#] これらのプロパティは規約オブジェクト :gradledoc:`com.asakusafw.mapreduce.gradle.plugins.AsakusafwOrganizerMapReduceExtension` が提供します。

Sparkプロパティ
~~~~~~~~~~~~~~~

Spark DSLコンパイラが生成するバッチアプリケーションの構成に関する規約プロパティは、 ``asakusafwOrganizer`` ブロック内の参照名 ``spark`` でアクセスできます。

この規約オブジェクトについては、Asakusa on Sparkの以下のドキュメントを参照してください。

* :asakusa-on-spark:`Asakusa on Spark リファレンス - Framework Organizer Plugin ( asakusafwOrganizer ) への拡張 <reference.html#framework-organizer-plugin-asakusafworganizer>`

M3BPプロパティ
~~~~~~~~~~~~~~

M\ :sup:`3`\ BP DSLコンパイラが生成するバッチアプリケーションの構成に関する規約プロパティは、 ``asakusafwOrganizer`` ブロック内の参照名 ``m3bp`` でアクセスできます。

この規約オブジェクトについては、 |ASAKUSA_ON_M3BP| の以下のドキュメントを参照してください。

* :asakusa-on-m3bp:`Asakusa on M3BP リファレンス - Framework Organizer Plugin ( asakusafwOrganizer ) への拡張 <reference.html#framework-organizer-plugin-asakusafworganizer>`

Direct I/Oプロパティ
~~~~~~~~~~~~~~~~~~~~

Direct I/Oの構成に関する規約プロパティは、 ``asakusafwOrganizer`` ブロック内の参照名 ``directio`` でアクセスできます [#]_ 。
この規約オブジェクトは以下のプロパティを持ちます。

..  list-table:: Framework Organizer Plugin - Direct I/Oプロパティ ( ``directio`` ブロック )
    :widths: 2 1 2 5
    :header-rows: 1

    * - プロパティ名
      - 型
      - デフォルト値
      - 説明
    * - ``enabled``
      - boolean
      - true
      - この値をtrueにするとDirect I/O用の構成を行う

..  [#] これらのプロパティは規約オブジェクト :gradledoc:`com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.DirectIoConfiguration` が提供します。

.. _gradle-plugin-oraganizer-hive:

Hiveプロパティ
~~~~~~~~~~~~~~

Direct I/O Hiveの構成に関する規約プロパティは、 ``asakusafwOrganizer`` ブロック内の参照名 ``hive`` でアクセスできます [#]_ 。
この規約オブジェクトは以下のプロパティを持ちます。

..  list-table:: Framework Organizer Plugin - Hiveプロパティ ( ``hive`` ブロック )
    :widths: 2 1 2 5
    :header-rows: 1

    * - プロパティ名
      - 型
      - デフォルト値
      - 説明
    * - ``enabled``
      - boolean
      - false
      - この値をtrueにすると Direct I/O Hive連携モジュール用の構成を行う
    * - ``libraries``
      - java.util.List
      - ``org.apache.hive:hive-exec:1.1.1``
      - Directi I/O Hiveが実行時に使用するHiveライブラリ

..  [#] これらのプロパティは規約オブジェクト :gradledoc:`com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.HiveConfiguration` が提供します。

テストドライバープロパティ
~~~~~~~~~~~~~~~~~~~~~~~~~~

テストモジュール用の構成に関する規約プロパティは、 ``asakusafwOrganizer`` ブロック内の参照名 ``testing`` でアクセスできます [#]_ 。
この規約オブジェクトは以下のプロパティを持ちます。

..  list-table:: Framework Organizer Plugin - テストモジュールプロパティ ( ``testing`` ブロック )
    :widths: 2 1 2 5
    :header-rows: 1

    * - プロパティ名
      - 型
      - デフォルト値
      - 説明
    * - ``enabled``
      - boolean
      - false
      - この値をtrueにするとテストモジュール用の構成を行う

..  [#] これらのプロパティは規約オブジェクト :gradledoc:`com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.TestingConfiguration` が提供します。

WindGateプロパティ
~~~~~~~~~~~~~~~~~~

WindGateの構成に関する規約プロパティは、 ``asakusafwOrganizer`` ブロック内の参照名 ``windgate`` でアクセスできます [#]_ 。
この規約オブジェクトは以下のプロパティを持ちます。

..  list-table:: Framework Organizer Plugin - WindGateプロパティ ( ``windgate`` ブロック )
    :widths: 2 1 2 5
    :header-rows: 1

    * - プロパティ名
      - 型
      - デフォルト値
      - 説明
    * - ``enabled``
      - boolean
      - true
      - この値をtrueにするとWindGate用の構成を行う
    * - ``retryableEnabled``
      - boolean
      - false
      - この値をtrueにするとWindGateプラグイン ``asakusa-windgate-retryable`` を追加する [#]_
    * - ``sshEnabled``
      - boolean
      - true
      - この値をtrueにするとHadoopブリッジ ( ``windgate-ssh`` ) を追加する [#]_

..  [#] これらのプロパティは規約オブジェクト :gradledoc:`com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.WindGateConfiguration` が提供します。
..  [#] 詳しくは :doc:`../windgate/user-guide` - :ref:`windgate-userguide-retryable-plugin` を参照してください。
..  [#] 詳しくは :doc:`../windgate/user-guide` - :ref:`windgate-userguide-ssh-hadoop` を参照してください。

YAESSプロパティ
~~~~~~~~~~~~~~~

YAESSの構成に関する規約プロパティは、 ``asakusafwOrganizer`` ブロック内の参照名 ``yaess`` でアクセスできます [#]_ 。
この規約オブジェクトは以下のプロパティを持ちます。

..  list-table:: Framework Organizer Plugin - YAESSプロパティ ( ``yaess`` ブロック )
    :widths: 2 1 2 5
    :header-rows: 1

    * - プロパティ名
      - 型
      - デフォルト値
      - 説明
    * - ``enabled``
      - boolean
      - true
      - この値をtrueにするとYAESS用の構成を行う
    * - ``hadoopEnabled``
      - boolean
      - true
      - この値をtrueにするとHadoopブリッジ ( ``yaess-hadoop`` ) を追加する [#]_
    * - ``jobqueueEnabled``
      - boolean
      - false
      - この値をtrueにするとYAESSプラグイン ``asakusa-yaess-jobqueue`` を追加する [#]_
    * - ``toolsEnabled``
      - boolean
      - true
      - この値をtrueにするとYAESS拡張ツールを追加する
    * - ``iterativeEnabled``
      - boolean
      - true
      - この値をtrueにするとYAESSのIterative Extensions向け拡張モジュールを追加する [#]_

..  [#] これらのプロパティは規約オブジェクト :gradledoc:`com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.YaessConfiguration` が提供します。
..  [#] 詳しくは :doc:`../yaess/user-guide` - :ref:`yaess-profile-hadoop-section-ssh` を参照してください。
..  [#] 詳しくは :doc:`../yaess/jobqueue` - :ref:`yaess-plugin-jobqueue-client` を参照してください。
..  [#] Iterative ExtensionsについてはAsakusa on Sparkのドキュメント :asakusa-on-spark:`Asakusa on Spark Iterative Extensions <iterative-extension.html>` を参照してください。

フレームワーク拡張プロパティ
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Asakusa Frameworkの拡張構成に関する規約プロパティは、 ``asakusafwOrganizer`` ブロック内の参照名 ``extension`` でアクセスできます [#]_ 。
この規約オブジェクトは以下のプロパティを持ちます。

..  list-table:: Framework Organizer Plugin - フレームワーク拡張プロパティ ( ``extension`` ブロック )
    :widths: 2 1 2 5
    :header-rows: 1

    * - プロパティ名
      - 型
      - デフォルト値
      - 説明
    * - ``libraries``
      - java.util.List
      - ``[]``
      - ``$ASAKUSA_HOME/ext/lib`` 配下に配置するライブラリ [#]_

..  [#] これらのプロパティは規約オブジェクト :gradledoc:`com.asakusafw.gradle.plugins.AsakusafwOrganizerPluginConvention.ExtensionConfiguration` が提供します。
..  [#] 明示的に指定されたライブラリのみを配置し、明示的でない依存ライブラリ等は自動的に配置しません。

デプロイメントアーカイブの編集
------------------------------

デプロイメントアーカイブの構成方法として、コンポーネントの規約プロパティによってデプロイ構成を編集する機能の他に、デプロイメントアーカイブに任意のファイルを追加する機能を利用できます。

この機能は、 ``asakusafwOrganizer`` ブロック内の参照名 ``assembly`` でアクセスできます [#]_ 。

以下は、 ``assembly`` の利用例です [#]_ 。

..  code-block:: groovy
    :caption: build.gradle
    :name: build.gradle-gradle-plugin-reference-3

    asakusafwOrganizer {
        profiles.prod {
            assembly.into('.') {
                put 'src/dist/prod'
                replace 'asakusa-resources.xml', inputCombineMax: '24'
            }
        }
    }

``assembly.into`` は引数に指定したパス上に、ブロック配下の定義で対象とするファイルを追加します。

コンポーネントの規約プロパティによる構成で追加されるファイルと同名のファイルが含まれる場合は、ここで追加するファイルで上書きされるため、特定環境向けに構成した設定ファイルなどを含めることができます。

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
