===========================
Mavenアーキタイプ利用ガイド
===========================
この文書では、Asakusa Frameworkが提供しているMavenアーキタイプを使って、アプリケーション開発用のテンプレートからプロジェクトを作成し、アプリケーション開発環境を準備する手順を説明します。

また、作成したプロジェクト上でAsakusa Frameworkが提供するモデル生成ツールやAsakusa DSLコンパイラを使用する方法、およびこれらのツールに関する設定方法を説明します。

アプリケーション開発プロジェクトの作成
======================================
Asakusa Frameworkが提供するMavenアーキタイプ [#]_ を使ってアプリケーション開発用プロジェクトを作成します。

ここで作成するプロジェクトは、次のような特徴があります。

* Mavenを利用してバッチアプリケーションをビルドするための必要な設定がなされている
* ThunderGateやWindGateなど、利用したいコンポーネントに応じてすぐに利用可能な設定がなされている
* 選択したコンポーネントに応じたAsakusa Frameworkのインストーラーやサンプルプログラムが同梱されている

..  [#] http://maven.apache.org/guides/introduction/introduction-to-archetypes.html

Asakusa Frameworkが提供するMavenアーキタイプ
--------------------------------------------
現時点でAsakusa Frameworkが提供するアーキタイプの一覧を以下に示します。

..  list-table:: Asakusa Framework アーキタイプ一覧
    :widths: 35 20 45
    :header-rows: 1
    
    * - アーキタイプ
      - 導入バージョン
      - 説明
    * - ``asakusa-archetype-windgate``
      - 0.2.2
      - 外部システム連携にWindGateを使用するアプリケーション用のアーキタイプ。
    * - ``asakusa-archetype-thundergate``
      - 0.2.4
      - 外部システム連携にThunderGateを使用するアプリケーション用のアーキタイプ。
    * - ``asakusa-archetype-directio``
      - 0.2.5
      - 外部システム連携を利用せず、Direct I/Oを使用するアプリケーション用のアーキタイプ。

..  attention::
    旧バージョンで存在していたアーキタイプ ``asakusa-archetype-batchapp`` はバージョン 0.2.4で ``asakusa-archetype-thundergate`` に変更されました。バージョン0.2.4以降はこのアーキタイプは使用できません。

.. _archetype-catalog:

アーキタイプカタログによるアーキタイプとバージョンの選択
--------------------------------------------------------
Asakusa Frameworkが公開しているMavenアーキタイプカタログを指定してアプリケーション開発用プロジェクトを作成します。

Asakusa Frameworkは利用出来るアーキタイプとそのバージョンを定義したアーキタイプカタログを以下のURLで公開しています。

* http://asakusafw.s3.amazonaws.com/maven/archetype-catalog-0.5.xml

..  Attention::
    バージョン0.4 から、アーキタイプカタログファイルはバージョン毎(マイナーバージョン毎)に個別のファイルを提供するようになりました。過去バージョンのアーキタイプカタログを使用したい場合、以下のアーキタイプカタログURLを指定してください。 

    * http://asakusafw.s3.amazonaws.com/maven/archetype-catalog.xml

アーキタイプカタログからプロジェクトを作成するには上記のアーキタイプカタログを指定してMavenアーキタイププラグインを実行します。

..  code-block:: sh

    mvn archetype:generate -DarchetypeCatalog=http://asakusafw.s3.amazonaws.com/maven/archetype-catalog-0.5.xml

コマンド実行後、作成するプロジェクトに関するパラメータを対話式に入力していきます [#]_ 。以下はWindGate用のアーキタイプ ``asakusa-archetype-windgate`` を指定し、 Asakusa Framework バージョン ``0.5.3`` を利用したバッチアプリケーション用のプロジェクトを作成する手順例です。

..  code-block:: sh

    ...
    Choose archetype:
    1: http://asakusafw.s3.amazonaws.com/maven/archetype-catalog-0.5.xml -> com.asakusafw:asakusa-archetype-windgate (-)
    2: http://asakusafw.s3.amazonaws.com/maven/archetype-catalog-0.5.xml -> com.asakusafw:asakusa-archetype-thundergate (-)
    3: http://asakusafw.s3.amazonaws.com/maven/archetype-catalog-0.5.xml -> com.asakusafw:asakusa-archetype-directio (-)
    Choose a number or apply filter (format: [groupId:]artifactId, case sensitive contains): : 1 (<-1を入力)

    ...
    Choose com.asakusafw:asakusa-archetype-windgate version: 
    1: 0.5-SNAPSHOT
    2: 0.5.0
    3: 0.5.1
    4: 0.5.2
    5: 0.5.3
    Choose a number: 5: 5 (<-5を入力)

..  attention::
    ``-SNAPSHOT`` という名称が付いているバージョンは開発中のバージョンを表します。このバージョンはリリースバージョンと比べて不安定である可能性が高いため、使用する場合は注意が必要です。またこのバージョンはAsakusa FrameworkのMavenリポジトリが更新された場合、開発環境から自動的にライブラリの更新が行われる可能性があり、これが原因で予期しない問題が発生する可能性があります。

..  [#] Mavenアーキタイププラグインはアーキタイプカタログを利用して対話式にプロジェクトを作成するほかに、必要なパラメータを指定して非対話式にプロジェクトを作成することも出来ます。詳しくは、Mavenアーキタイププラグインのドキュメントなどを参照してください。

    * http://maven.apache.org/archetype/maven-archetype-plugin/generate-mojo.html

プロジェクト情報の入力
----------------------
アーキタイプの種類とバージョンを選択したら、続けてプロジェクト情報を入力していきます。

..  code-block:: sh

    Define value for property 'groupId': :    [<-アプリケーションのグループ名を入力] 
    Define value for property 'artifactId': : [<-アプリケーションのプロジェクト名を入力] 
    Define value for property 'version':      [<-アプリケーションの初期バージョンを入力]
    Define value for property 'package':      [<-アプリケーションの基底パッケージ名を入力]

プロジェクト情報を確認後、 ``Y`` を入力してプロジェクトを作成します。

..  code-block:: sh

    Confirm properties configuration:
    groupId: ...
    artifactId: ...
    version: ...
    package: ...
    Y: : Y

プロジェクトの作成が成功すると、 ``BUILD SUCCESS`` が表示され、
カレントディレクトリ配下にプロジェクトディレクトリが作成されます。


プロジェクトのディレクトリ構成
==============================
アーキタイプから作成したプロジェクトのディレクトリ構成について説明します。

プロジェクト全体構成
--------------------
アーキタイプから作成したプロジェクトディレクトリの直下には、以下のディレクトリ/ファイルが含まれます。

..  list-table::  プロジェクト全体構成
    :widths: 3 7
    :header-rows: 1
    
    * - ファイル/ディレクトリ
      - 説明
    * - ``src``
      - プロジェクトのソースディレクトリ
    * - ``target``
      - プロジェクトのビルドディレクトリ
    * - ``pom.xml``
      - プロジェクトの様々な構成や設定を定義するファイル
    * - ``build.properties``
      - プロジェクトのビルド設定を定義するファイル [#]_ 

このうち、アプリケーション開発者は ``src`` ディレクトリ配下を編集することでアプリケーションを開発します。
``target`` ディレクトリは ``src`` ディレクトリ配下のファイルをビルドすることで生成される成果物が配置されます。

``target`` ディレクトリ配下のファイルはビルドの度に初期化、再作成されるため
``taget`` ディレクトリ配下のファイルは直接編集しないようにしてください。

..  [#] 詳しくは後述の `ビルド定義ファイル`_ を参照してください。


ソースディレクトリ構成
----------------------
プロジェクトのソースディレクトリ (``src``) は大きくアプリケーション本体のコードを配置する ``src/main`` ディレクトリと、アプリケーションのテスト用のコードを配置する ``src/test`` ディレクトリに分かれます。

それぞれのディレクトリ/ファイルの構成を以下に示します。なお、表中の ``${package}`` 部分はプロジェクト作成時に指定した基底パッケージ名に対応したディレクトリが作成されます。

..  list-table:: ソースディレクトリ構成: ``src/main``
    :widths: 5 5
    :header-rows: 1
    
    * - ファイル/ディレクトリ
      - 説明
    * - ``src/main/dmdl``
      - DMDLスクリプトディレクトリ
    * - ``src/main/java/${package}/batch``
      - バッチDSLのソースディレクトリ
    * - ``src/main/java/${package}/flowpart``
      - フローDSL(フロー部品)のソースディレクトリ
    * - ``src/main/java/${package}/jobflow``
      - フローDSL(ジョブフロー)のソースディレクトリ
    * - ``src/main/java/${package}/operator``
      - 演算子DSLのソースディレクトリ
    * - ``src/main/resources``
      - プロジェクトのリソースディレクトリ [#]_
    * - ``src/main/sql/modelgen``
      - DDLスクリプトディレクトリ [#]_
    * - ``src/main/libs``
      - プロジェクトの依存ライブラリディレクトリ [#]_

..  [#] アーキタイプの標準構成では空になっています。
..  [#] ThunderGate用アーキタイプから生成したプロジェクトのみ存在します。
..  [#] このディレクトリ内に *直接* 配置したライブラリファイル ( ``*.jar`` ) のみ、バッチアプリケーション内でも利用可能です（サブディレクトリに配置したライブラリファイルは無視されます）。
        また、Eclipse内からライブラリを参照する場合には、Mavenの ``<dependencies>`` にも同様のライブラリをあらかじめ追加しておく必要があります。
        詳しくは、後述の `アプリケーション用依存ライブラリの追加`_ を参照してください。

..  list-table:: ソースディレクトリ構成: ``src/test``
    :widths: 5 5
    :header-rows: 1
    
    * - ファイル/ディレクトリ
      - 説明
    * - ``src/test/java/${package}/batch``
      - バッチDSLのテスト用ソースディレクトリ
    * - ``src/test/java/${package}/flowpart``
      - フローDSL(フロー部品)のテスト用ソースディレクトリ
    * - ``src/test/java/${package}/jobflow``
      - フローDSL(ジョブフロー)のテスト用ソースディレクトリ
    * - ``src/test/java/${package}/operator``
      - 演算子DSLのテスト用ソースディレクトリ
    * - ``src/test/resources/logback-test.xml``
      - ビルド/テスト実行時に使用されるログ定義ファイル
    * - ``src/test/resources/asakusa-resources.xml``
      - テスト実行時に使用される実行時プラグイン設定ファイル
    * - ``src/test/resources/${package}/batch``
      - バッチDSLのテストデータ用ディレクトリ
    * - ``src/test/resources/${package}/flowpart``
      - フローDSL(フロー部品)のテストデータ用ディレクトリ
    * - ``src/test/resources/${package}/jobflow``
      - フローDSL(ジョブフロー)のテストデータ用ディレクトリ
    * - ``src/test/example-dataset``
      - サンプルアプリケーション実行用のサンプルデータディレクトリ [#]_
    * - ``src/test/example-scripts``
      - サンプルアプリケーション実行用のサンプルスクリプトディレクトリ [#]_

..  note::
     上記ディレクトリはMavenの設定により変更可能です。詳しくはMavenのドキュメントを参照してください。また、一部のディレクトリやファイルは 後述する `ビルド定義ファイル`_ の設定により変更可能です。

..  [#] サンプルアプリケーションをYAESSから実行する際に利用するサンプルデータが含まれます。詳しくは :doc:`../introduction/start-guide` や 各外部連携モジュールのスタートガイドを参照してください。
..  [#] サンプルアプリケーションのデプロイ/実行例を示したスクリプトが含まれます。一部のアーキタイプでは環境依存の実装や環境の初期化処理が含まれるため、実行前に必ずスクリプトの内容を確認してください。

ビルドディレクトリ構成
----------------------
プロジェクトのビルドディレクトリ (``target``) はMavenの各フェーズの実行に対応したビルド成果物が作成されます。

ビルドディレクトリのディレクトリ/ファイルの構成を以下に示します [#]_ 。なお、表中の ``${artifactid}``, ``${version}`` 部分はプロジェクト作成時に指定したプロジェクト名, バージョンに対応した文字列が使用されます。

..  list-table:: ビルドディレクトリ構成
    :widths: 4 2 4
    :header-rows: 1
    
    * - ファイル/ディレクトリ
      - 生成フェーズ [#]_
      - 説明
    * - ``${artifactid}-batchapps-${version}.jar``
      - ``package``
      - Asakusa Frameworkバッチアプリケーション用アーカイブ [#]_
    * - ``${artifactid}-${version}.jar``
      - ``package``
      - Mavenにより生成される標準のjarアーカイブ [#]_
    * - ``${artifactid}-${version}-sources.jar``
      - ``package``
      - Mavenにより生成される標準のソースアーカイブ
    * - ``batchc``
      - ``package``
      - Batch DSLコンパイラが生成するバッチコンパイル結果の出力ディレクトリ
    * - ``batchcwork``
      - ``package``
      - Batch DSLコンパイラが使用するワークディレクトリ
    * - ``dmdl``
      - ``generate-sources``
      - DMDLジェネレータが生成するDMDLスクリプトディレクトリ [#]_
    * - ``excel``
      - ``generate-sources``
      - テストデータジェネレータが生成するテストデータテンプレート用ディレクトリ [#]_
    * - ``sql``
      - ``generate-sources``
      - 管理テーブル用DDL用のディレクトリ [#]_
    * - ``testdriver``
      - ``test``
      - テストドライバが使用するワークディレクトリ
    * - ``generated-sources/annotations/${package}/flowpart``
      - ``compile``
      - Operator DSLコンパイラが生成するフロー演算子
    * - ``generated-sources/annotations/${package}/operator``
      - ``compile``
      - Opretor DSLコンパイラが生成する演算子ファクトリと演算子実装クラス
    * - ``generated-sources/modelgen/${package}/modelgen``
      - ``generate-sources``
      - DMDLコンパイラによって生成されるデータモデルクラス用ディレクトリ

..  note::
    各種コンパイラやジェネレータについて詳しくは、 :doc:`../dmdl/index` や :doc:`../dsl/index`, :doc:`../testing/index` などのドキュメントを参照してください。
..  note::
     上記ディレクトリはMavenの設定により変更可能です。詳しくはMavenのドキュメントを参照してください。また、一部のディレクトリやファイルは 後述する `ビルド定義ファイル`_ の設定により変更可能です。

..  [#] ここで示すディレクトリ以外にも、実行するMavenのプラグインによって様々なディレクトリが生成されます。これらの詳細についてはMavenプラグインのドキュメントなどを参照してください。
..  [#] ファイル/ディレクトリを生成するMavenのフェーズ
..  [#] バッチコンパイルやバッチアプリケーションアーカイブについては、後述の `バッチコンパイルとバッチアプリケーションアーカイブの生成`_ を参照してください。
..  [#] Asakusa Frameworkで作成したアプリケーション実行では利用しません。詳しくは後述の `バッチコンパイルとバッチアプリケーションアーカイブの生成`_ を参照してください。
..  [#] ThunderGate用アーキタイプから生成したプロジェクトのみ生成されます。詳しくは ThunderGate の各ドキュメントを参照してください。
..  [#] テストデータテンプレートについては 後述の `テストデータテンプレートの生成`_ を参照してください。
..  [#] ThunderGate用アーキタイプから生成したプロジェクトのみ生成されます。詳しくは ThunderGate の各ドキュメントを参照してください。


データモデルクラスの生成
========================
Asakusa Frameworkでは、モデルの定義情報の記述するための言語としてDMDL(Data Model Definition Language) が提供されています。
モデル定義情報の記述方法については :doc:`../dmdl/index` を参照してください。

以下はモデルの定義情報を記述したスクリプトファイルの配置について説明します。

DMDLスクリプトの配置
--------------------
DMDLスクリプトはプロジェクトの ``src/main/dmdl`` [#]_ ディレクトリ以下に配置してください。
また、スクリプトのファイル名には ``.dmdl`` の拡張子を付けて、UTF-8エンコーディングで保存してください。

DMDLファイルは複数配置することが出来ます。上記ディレクトリ配下にサブディレクトリを作成し、そこにDMDLファイルを配置することも可能です。

..  [#] このディレクトリはプロジェクトの設定ファイル ``build.properties`` によって変更することが出来ます。詳しくは後述の `ビルド定義ファイル`_ を参照してください。

データモデルクラスの生成
------------------------
アーキタイプから作成したプロジェクトのpom.xmlに対して ``generate-sources`` フェーズを実行するとDMDLコンパイラが起動し、
``target/generated-sources/modelgen`` ディレクトリ以下にデータモデルに関するJavaソースファイルが生成されます。

..  code-block:: sh

    mvn clean generate-sources

データモデルクラスに使われるJavaパッケージ名は、デフォルトではアーキタイプ生成時に指定したパッケージ名の末尾に ``.modelgen`` を付加したパッケージになります。例えばアーキタイプ生成時に指定したパッケージが ``com.example`` の場合、データモデルクラスのパッケージ名は ``com.example.mogelgen`` になります [#]_ 。

..  attention::
    Mavenの実行時に ``clean`` フェーズを常に実行することで、DMDLスクリプトでモデルの名称を変えたとき時などに使わなくなったデータモデルクラスが削除されます。特に理由が無い限りは ``clean`` フェーズを常に実行するとよいでしょう。

..  [#] パッケージ名は、後述する `ビルド定義ファイル`_ の設定により変更することが出来ます。

テストデータテンプレートの生成
------------------------------
``generate-sources`` フェーズを実行すると、データモデルクラスの生成のほか、テストドライバを利用するテストで使用する テストデータテンプレート が ``target/excel`` 配下に生成されます。テストデータテンプレートについては、 :doc:`../testing/using-excel` を参照して下さい。

.. _maven-archetype-batch-compile:


バッチコンパイルとバッチアプリケーションアーカイブの生成
========================================================
Asakusa DSLで記述したバッチアプリケーションをHadoopクラスタにデプロイするためには、Asakusa DSLコンパイラを実行してバッチアプリケーション用のアーカイブファイルを作成します。

DSLコンパイラについての詳しい情報は :doc:`../dsl/user-guide` を参照してください。


バッチコンパイルの実行
----------------------
アーキタイプから作成したプロジェクトのpom.xmlに対して ``package`` フェーズを実行するとバッチコンパイルが実行されます。

..  code-block:: sh

    mvn clean package

Mavenの標準出力に ``BUILD SUCCESS`` が出力されればバッチコンパイルは成功です。バッチコンパイルが完了すると、 ``target`` ディレクトリにバッチコンパイル結果のアーカイブファイルが ``${artifactid}-batchapps-${version}.jar`` というファイル名で生成されます。

``${artifactid}-batchapps-${version}.jar`` はHadoopクラスタ上でjarファイルを展開してデプロイします。Hadoopクラスタへのアプリケーションのデプロイについては以下を参照してください。

* :doc:`../administration/deployment-with-windgate`
* :doc:`../administration/deployment-with-thundergate`
* :doc:`../administration/deployment-with-directio`

..  warning::
    バッチコンパイルを実行すると、 ``target`` ディレクトリ配下には ``${artifactid}-batchapps-${version}.jar`` の他に ``${artifactid}-${version}.jar`` , ``${artifactid}-${version}-sources.jar`` という名前のjarファイルも同時に作成されます。
    これらのファイルはMavenの標準の ``package`` フェーズの処理により作成されるjarファイルですが、Asakusa Frameworkではこれらのファイルは使用しません。
    これらのファイルをHadoopクラスタにデプロイしてもバッチアプリケーションとしては動作しないので注意してください。

..  attention::
    バッチコンパイルの最中 ( ``compileフェーズ`` ) にJavaのソースファイルのコンパイル時に以下の警告が表示されることがあります。
     
    ..  code-block:: sh
    
         [WARNING] ... src/main/java/example/flowpart/ExFlowPart.java:[20,23] シンボルを見つけられません。
         シンボル: クラス ExOperatorFactory
    
    これは、DSLコンパイラが「スパイラルコンパイル」という方式でコンパイルを段階的に実行している過程の警告であり、
    最終的にコンパイルが成功していれば問題ありません。

    より詳しくは、 :doc:`../dsl/user-guide` の :ref:`dsl-userguide-operator-dsl-compiler` を参照してください。

.. _batch-compile-option-with-pom:

バッチコンパイルオプションの指定
--------------------------------
バッチのビルドオプションを指定するには、pom.xmlのプロファイルに定義されているプロパティ ``asakusa.compiler.options`` に値を設定します。
設定できる値は「 ``+<有効にするオプション名>`` 」や「 ``-<無効にするオプション名>`` 」のように、オプション名の先頭に「 ``+`` 」や「 ``-`` 」を指定します。
また、複数のオプションを指定するには「 ``,`` 」(カンマ)でそれぞれを区切ります。

指定できるバッチコンパイルのオプションについては、 :doc:`../dsl/user-guide` の :ref:`batch-compile-options` を参照してください。

モジュールの取り込み
--------------------
バッチコンパイルの実行時に、 :doc:`../dsl/user-guide` の :ref:`include-fragment-module` に説明されているマーカーファイルを使用する方法を使って、バッチアプリケーションを構成する外部のライブラリを取り込むことが出来ます。

マーカファイルの指定によりバッチアプリケーションに取り込まれたライブラリ（フラグメントライブラリ）は、バッチコンパイル実行時に ( ``package`` フェーズ実行時に) 以下のようなログが出力されます。

..  code-block:: sh

     [java] 11:02:42 [main] INFO  c.a.c.testing.DirectFlowCompiler - フラグメントクラスライブラリを取り込みます: /home/asakusa/.m2/repository/example/example-model/1.0-SNAPSHOT/example-model-1.0-SNAPSHOT.jar
     [java] 11:02:42 [main] INFO  c.a.c.testing.DirectFlowCompiler - フラグメントクラスライブラリを取り込みます: /home/asakusa/.m2/repository/example/example-utils/1.0-SNAPSHOT/example-utils-1.0-SNAPSHOT.jar

.. _eclipse-configuration:

Eclipseを使ったアプリケーションの開発
=====================================
統合開発環境(IDE)にEclipseを使用する場合、開発環境にEclipseをインストールした上で、以下の設定を行います。

プロジェクトにEclipse用定義ファイルを追加する
---------------------------------------------
アプリケーション用プロジェクトにEclipseプロジェクト用の定義ファイルを追加します。このコマンドを実行することによってEclipseからプロジェクトをインポートすることが可能になります。

例えば、バッチアプリケーション用プロジェクト「example-app」のEclipse定義ファイルを作成するには、プロジェクトのディレクトリに移動し、以下のコマンドを実行します。

..  code-block:: sh

    cd example-app
    mvn eclipse:eclipse

EclipseからプロジェクトをImportするには、Eclipseのメニューから ``[File]`` -> ``[Import]`` -> ``[General]`` -> ``[Existing Projects into Workspace]`` を選択し、プロジェクトディレクトリを指定します。

Mavenプロジェクトへの変換(m2eプラグインの利用)
----------------------------------------------
m2eプラグインを使ってアプリケーション用プロジェクトをMavenプロジェクトに変換すると、Eclipse上からMavenを実行することが可能になるなど、いくつか便利な機能を使用できます。

Mavenプロジェクトへの変換は任意です。変換を行う場合は以下の手順に従ってください。

m2e buildhelper connector のインストール
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
m2eの拡張機能である「m2e buildhelper connector」をインストールします。

1. Eclipseのメニューから ``[Window]`` -> ``[Preferences]`` -> ``[Maven]`` -> ``[Discovery]`` を選択し、ダイアログに表示される ``[Open Dialog]`` ボタンを押下します。
2. 「install m2e connectors」ダイアログが表示されるので、そのリストから「buildhelper」の項目のチェックをONにして ``[Finish]`` ボタンを押下します。
3. ウィザードに従ってm2e buildhelper connectorをインストールします。
    1. 「Install」ダイアログでは そのまま ``[Next>]`` ボタンを押下します。
    2. 「Install Details」ダイアログでは そのまま ``[Next>]`` ボタンを押下します。
    3. 「Review Licenses」ダイアログでは ``[I accept...]`` を選択して ``[Finish]`` ボタンを押下します。
    4. 「Security Warinig」ダイアログが表示された場合、そのまま ``[OK]`` ボタンを押下します。
    5. 「Software Updates」ダイアログではEclipseの再起動を促されるので、 ``[Yes]`` ボタンを押下してEclipseを再起動します。

Mavenプロジェクトへの変換
~~~~~~~~~~~~~~~~~~~~~~~~~
Eclipseのパッケージエクスプローラーからアプリケーション用プロジェクトを右クリックして ``[Configure]`` -> ``[Convert to Maven Project]`` を選択します。

これでMavenプロジェクトへの変換が行われました。アプリケーション用プロジェクトに対してMavenを実行する場合は、アプリケーション用プロジェクトを右クリックして ``[Run As]`` を選択するとサブメニューに ``[Maven build...]`` など、いくつかのMaven実行用メニューが表示されるのでこれを選択してください。

.. _application-dependency-library:

アプリケーション用依存ライブラリの追加
======================================
バッチアプリケーションの演算子から共通ライブラリ（Hadoopによって提供されているライブラリ以外のもの）を使用する場合は、まず通常のMavenを使ったアプリケーションと同様pom.xmlに依存定義( ``<dependency>`` )を追加します。
これに加えて、依存するjarファイルを以下に示す規定のディレクトリに配置する必要があります。

以下はJavaの日付ライブラリである `Joda Time`_ 2.1 を配置する例です。

..  _`Joda Time`: http://joda-time.sourceforge.net/

pom.xmlの編集
-------------

pom.xmlの ``<dependencies>`` 内に依存定義を追加します。

..  code-block:: xml

    <dependency>
        <groupId>joda-time</groupId>
        <artifactId>joda-time</artifactId>
        <version>2.1</version>
    </dependency>

依存ライブラリのコピー
----------------------

MavenのDependencyプラグイン [#]_ を利用して依存ライブラリを取得します。

..  code-block:: sh

    mvn dependency:copy-dependencies

上記のコマンドを実行すると、依存ライブラリがプロジェクト下の ``target/dependency`` 以下にコピーされます。

..  [#] http://maven.apache.org/plugins/maven-dependency-plugin/

プロジェクトの依存ライブラリディレクトリへjarファイルを配置
-----------------------------------------------------------
アプリケーション開発プロジェクトの「依存ライブラリディレクトリ」配下に配置すると、バッチアプリケーションアーカイブに自動的に含まれるようになります。

``target/dependency`` にコピーしたjarファイルから必要なものを選んで ``src/main/libs`` ディレクトリに配置します。

..  code-block:: sh

    mkdir src/main/libs
    cp target/dependency/joda-time-2.1.jar src/main/libs

Asakusaの拡張ライブラリディレクトリへjarファイルを配置
------------------------------------------------------
バッチアプリケーションの実行時に依存ライブラリを利用するもう一つの方法は、Asakusa Framework全体の「拡張ライブラリディレクトリ」に対象のjarファイルを直接配置してしまうことです。
拡張ライブラリディレクトリに追加したjarファイルは、実行時に全てのバッチアプリケーションから参照できます。

``target/dependency`` にコピーしたjarファイルから必要なものを選んで ``$ASAKUSA_HOME/ext/lib`` ディレクトリに配置します。

..  code-block:: sh

    cp target/dependency/joda-time-2.1.jar $ASAKUSA_HOME/ext/lib

ビルド定義ファイル
==================
アーキタイプから作成したプロジェクトの ``build.properties`` はプロジェクトのビルドや各種ツールの動作を設定します。設定項目について以下に説明します。

General Settings
----------------

  ``asakusa.database.enabled``
    *(asakusa-archetype-thundergateのみ)*

    ( ``true`` or ``false`` ) このプロパティをfalseにすると、モデル生成処理 ( ``generate-sources`` ) でデータベースに対する処理を行わなくなります。
    
    モデルの定義をDMDLのみで行う場合は、このオプションをfalseにするとデータベースを使用せずにモデル生成を行うことが可能になります。

  ``asakusa.database.target``
    *(asakusa-archetype-thundergateのみ)*

    モデル生成処理 ( ``generate-sources`` ) でデータベースを使用する場合に、データベース定義ファイルを特定するためのターゲット名を指定します。
    
    開発環境で使用するデータベース定義ファイルは、ローカルにインストールしたAsakusa FrameworkのThunderGate用データベース定義ファイル ( ``$ASAKUSA_HOME/bulkloader/conf/${asakusa.database.target}-jdbc.properties`` )を使用します。開発環境へのインストール時に本プロパティの設定値を使って左記ディレクトリにデータベース定義ファイルを生成します。
    
    通常はこの値を変更する必要はありませんが、ThnderGateのインポータ/エクスポータ記述でターゲット名を変更している場合にはターゲット名に合わせて変更します。また、１つの開発環境で複数のアプリケーションプロジェクトに対して作業している場合に、それぞれのプロジェクトでデータベースを分けておきたい場合に個別の値を指定すると便利です。
    
    なお、インポータ/エクスポータ記述で複数のデータソースを指定している場合は、本ターゲット名は使用しているデータソース名のうちいずれか１つのデータソースを使用し、データベース定義ファイルはターゲット分の定義ファイルを ``$ASAKUSA_HOME/bulkloader/conf`` 配下に配置します。その上で、定義ファイル内に記述するすべてのデータベース設定をすべて同じ内容にしてください（バージョン |version| ではAsakusa Frameworkのテストツールが複数データソースに対応していないため）。

Batch Compile Settings
----------------------

  ``asakusa.package.default``
    バッチコンパイル時に生成されるHadoopのジョブ、及びMapReduce関連クラスのJavaパッケージを指定します。デフォルト値はアーキタイプ生成時に指定した ``package`` の値に ``.batchapp`` を付与した値になります。

  ``asakusa.batchc.dir``
    バッチコンパイル時に生成されるHadoopのジョブ、及びMapReduce関連クラスの出力ディレクトリを指定します。 ``package`` フェーズを実行した時に生成されるjarファイルは、このディレクトリ配下のソースをアーカイブしたものになります。

  ``asakusa.compilerwork.dir``
    バッチコンパイル時にコンパイラが使用するワークディレクトリを指定します。

  ``asakusa.hadoopwork.dir``
    Asakusa Frameworkがジョブフローの実行毎にデータを配置するHadoopファイルシステム上のディレクトリを、ユーザのホームディレクトリからの相対パスで指定します。
    
    パスに文字列 ``${execution_id}`` が含まれる場合、ワークフローエンジンから指定されたexecution_idによって置換されます。デフォルト値はexecution_idが指定されているため、ジョブフローの実行毎にファイルシステム上は異なるディレクトリが使用されることになります。

Model Generator Settings
------------------------

  ``asakusa.modelgen.package``
    モデルジェネレータによるモデル生成時にデータモデルクラスに付与されるJavaパッケージを指定します。デフォルト値は、アーキタイプ生成時に指定した ``package`` の値に ``.modelgen`` を付与した値になります。

  ``asakusa.modelgen.includes``
    ``generate-sources`` フェーズ実行時にモデルジェネレータ、およびテストデータテンプレート生成ツールが生成対象とするモデル名を正規表現の書式で指定します。
    
  ``asakusa.modelgen.excludes``
    ``generate-sources`` フェーズ実行時にモデルジェネレータ、およびテストデータテンプレート生成ツールが生成対象外とするモデル名を正規表現の書式で指定します。デフォルト値はThunderGateが使用する管理テーブルを生成対象外とするよう指定されています。特に理由が無い限り、デフォルト値で指定されている値は削除しないようにして下さい。

  ``asakusa.modelgen.sid.column``
    *(asakusa-archetype-thundergateのみ)*

    ThunderGateが入出力を行う業務テーブルのシステムIDカラム名を指定します。この値はThunderGate用のデータベースノード用プロパティファイル( ``bulkloader-conf-db.properties`` )のプロパティ ``table.sys-column-sid`` と同じ値を指定してください。この項目はThunderGateキャッシュを使用する場合にのみ必要です。

  ``asakusa.modelgen.timestamp.column``
    *(asakusa-archetype-thundergateのみ)*

    ThunderGateが入出力を行う業務テーブルの更新日時カラム名を指定します。この値はThunderGate用のデータベースノード用プロパティファイル( ``bulkloader-conf-db.properties`` )のプロパティ ``table.sys-column-updt-date`` と同じ値を指定してください。この項目はThunderGateキャッシュを使用する場合にのみ必要です。

  ``asakusa.modelgen.delete.column``
    *(asakusa-archetype-thundergateのみ)*

    ThunderGateが入出力を行う業務テーブルの論理削除フラグカラム名を指定します。この項目はThunderGateキャッシュを使用する場合にのみ必要です。

  ``asakusa.modelgen.delete.value``
    *(asakusa-archetype-thundergateのみ)*

    ThunderGateが入出力を行う業務テーブルの論理削除フラグが削除されたことを示す値を指定します。この項目はThunderGateキャッシュを使用する場合にのみ必要です。

  ``asakusa.modelgen.output``
    モデルジェネレータが生成するデータモデルクラス用Javaソースの出力ディレクトリを指定します。アーキタイプが提供するEclipseの設定情報と対応しているため、特に理由が無い限りはデフォルト値を変更しないようにしてください。この値を変更する場合、合わせてpom.xmlの修正も必要となります。

  ``asakusa.dmdl.encoding``
    DMDLスクリプトが使用する文字エンコーディングを指定します。

  ``asakusa.dmdl.dir``
    DMDLスクリプトを配置するディレクトリを指定します。

ThunderGate Settings
--------------------

  ``asakusa.bulkloader.tables``
    *(asakusa-archetype-thundergateのみ)*

    ``generate-sources`` フェーズ実行時に生成されるThunderGate管理テーブル用DDLスクリプト（後述の ``asakusa.bulkloader.genddl`` で指定したファイル）に含める対象テーブルを指定します。このプロパティにインポート、及びエクスポート対象テーブルのみを指定することで、余分な管理テーブルの生成を抑止することが出来ます。開発時にはデフォルト（コメントアウト）の状態で特に問題ありません。

  ``asakusa.bulkloader.genddl``
    *(asakusa-archetype-thundergateのみ)*

    ``generate-sources`` フェーズ実行時に生成されるThunderGate管理テーブル用DDLスクリプトのファイルパスを指定します。

  ``asakusa.dmdl.fromddl.output``
    *(asakusa-archetype-thundergateのみ)*

    ``generate-sources`` フェーズ実行時にモデル定義情報となるDDLスクリプトから生成するDMDLスクリプトの出力先を指定します。

TestDriver Settings
-------------------

  ``asakusa.testdatasheet.generate``
    ( ``true`` or ``false`` ) このプロパティをfalseにすると、 ``generate-sources`` フェーズ実行時にテストデータテンプレートの作成を行わないようになります。テストドライバを使ったテストにおいて、テストデータの定義をExcelシート以外で管理する場合はfalseに設定してください。

  ``asakusa.testdatasheet.format``
    ``generate-sources`` フェーズ実行時に生成されるテストデータテンプレートのフォーマットを指定します。以下の値を指定することが出来ます。
      * ``DATA``: テストデータテンプレートにテストデータの入力データ用シートのみを含めます。
      * ``RULE``: テストデータテンプレートにテストデータの検証ルール用シートのみを含めます。
      * ``INOUT``: テストデータテンプレートにテストデータの入力データ用シートと出力（期待値）用シートを含めます。
      * ``INSPECT``: テストデータテンプレートにテストデータの出力（期待値）用シートと検証ルール用シートのみを含めます。
      * ``ALL``: テストデータテンプレートに入力データ用シート、出力（期待値）用シート、検証ルール用シートを含めます。

  ``asakusa.testdatasheet.output``
    ``generate-sources`` フェーズ実行時に生成されるテストデータテンプレートの出力ディレクトリを指定します。

  ``asakusa.testdriver.compilerwork.dir``
    テストドライバの実行時にテストドライバの内部で実行されるバッチコンパイルに対してコンパイラが使用するワークディレクトリを指定します。 
    
    ``asakusa.compilerwork.dir`` と同じ働きですが、この項目はテストドライバの実行時にのみ使われます。

  ``asakusa.testdriver.hadoopwork.dir``
    テストドライバの実行時にテストドライバの内部で使用される、ジョブフローの実行毎にデータを配置するHadoopファイルシステム上のディレクトリを、ユーザのホームディレクトリからの相対パスで指定します。Hadoopのスタンドアロンモード使用時には、OS上のユーザのホームディレクトリが使用されます。

    ``asakusa.hadoopwork.dir`` と同じ働きですが、この項目はテストドライバの実行時にのみ使われます。

TestDriver Settings (for Asakusa 0.1 asakusa-test-tools)
--------------------------------------------------------

  ``asakusa.testdatasheet.v01.generate``
    *(asakusa-archetype-thundergateのみ)*

    ( ``true`` or ``false`` ) Asakusa Framework 0.1 仕様のテストデータテンプレートを出力するかを設定します（デフォルトは出力しない）。 このプロパティをtrueにすると、 ``generate-sources`` フェーズ実行時にテストデータテンプレートが ``target/excel_v01`` ディレクトリ配下に出力されるようになります。

  ``asakusa.testdriver.testdata.dir``
    *(asakusa-archetype-thundergateのみ)*

    テストドライバの実行時に、テストドライバが参照するテストデータテンプレートの配置ディレクトリを指定します。
    
    このプロパティは、テストドライバAPIのうち、Asakusa Framework 0.1 から存在する ``*TestDriver`` というクラスの実行時のみ使用されます。Asakusa Framework 0.2 から追加された ``*Tester`` 系のテストドライバAPIは、この値を使用せず、テストドライバ実行時のクラスパスからテストデータテンプレートを参照するようになっています。

  ``asakusa.excelgen.tables``
    *(asakusa-archetype-thundergateのみ)*

    Asakusa Framework 0.1 仕様のテストデータテンプレート生成ツールをMavenコマンドから実行 ( ``mvn exec:java -Dexec.mainClass=com.asakusafw.testtools.templategen.Main`` )した場合に、テストデータシート生成ツールが生成の対象とするテーブルをスペース区切りで指定します。
    
