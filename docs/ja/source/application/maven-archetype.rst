===========================
Mavenアーキタイプ利用ガイド
===========================

この文書では、Asakusa Frameworkが提供しているMavenアーキタイプを使って、アプリケーション開発用のテンプレートからプロジェクトを作成し、アプリケーション開発環境を準備する手順を説明します。

また、作成したプロジェクト上でAsakusa Frameworkが提供するモデル生成ツールやバッチコンパイラを使用する方法、およびこれらのツールに関する設定方法を説明します。

アプリケーション開発プロジェクトの作成
======================================
Asakusa Frameworkが提供するMavenアーキタイプ [#]_ を使ってアプリケーション開発用プロジェクトを作成します。

ここで作成するプロジェクトは、次のような特徴があります。

* Mavenを利用してバッチアプリケーションをビルドするための必要な設定がなされている
* ThunderGateやWindGateなど、利用したいコンポーネントを選択できる
* 選択したコンポーネントに応じたAsakusa Frameworkのインストーラーが同梱されている

..  [#] http://maven.apache.org/guides/introduction/introduction-to-archetypes.html

``archetype:generate`` アプリケーション開発用プロジェクトを生成
---------------------------------------------------------------
Asakusa Frameworkが公開しているMavenアーキタイプカタログを指定してアプリケーション開発用プロジェクトを作成します。

現時点でAsakusa Frameworkが提供するアーキタイプには以下のものがあります。

..  list-table:: Asakusa Framework アーキタイプ一覧
    :widths: 3 1 6
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
      - 外部システム連携を利用せず、Direct I/Oを使用するアプリケーション用のアーキタイプ 。

``archetype:generate`` は引数にAsakusa Frameworkが提供するカタログのURL ``http://asakusafw.s3.amazonaws.com/maven/archetype-catalog-0.4.xml`` を指定して実行します 。

..  code-block:: sh

    mvn archetype:generate -DarchetypeCatalog=http://asakusafw.s3.amazonaws.com/maven/archetype-catalog-0.4.xml


コマンド実行後、作成するプロジェクトに関するパラメータを対話式に入力していきます。以下はWindGate用のアーキタイプ ``asakusa-archetype-windgate`` を指定し、 Asakusa Framework バージョン ``0.4.0`` を利用したバッチアプリケーション用のプロジェクトを作成する手順例です。

..  code-block:: sh

    $ mvn archetype:generate -DarchetypeCatalog=http://asakusafw.s3.amazonaws.com/maven/archetype-catalog-0.4.xml
    [INFO] Scanning for projects...
    [INFO]                                                                         
    [INFO] ------------------------------------------------------------------------
    [INFO] Building Maven Stub Project (No POM) 1
    [INFO] ------------------------------------------------------------------------
    ...
    Choose archetype:
    1: http://asakusafw.s3.amazonaws.com/maven/archetype-catalog-0.4.xml -> com.asakusafw:asakusa-archetype-windgate (-)
    2: http://asakusafw.s3.amazonaws.com/maven/archetype-catalog-0.4.xml -> com.asakusafw:asakusa-archetype-thundergate (-)
    3: http://asakusafw.s3.amazonaws.com/maven/archetype-catalog-0.4.xml -> com.asakusafw:asakusa-archetype-directio (-)
    Choose a number or apply filter (format: [groupId:]artifactId, case sensitive contains): : 1 (<-1を入力)

    ...
    Choose com.asakusafw:asakusa-archetype-windgate version: 
    1: 0.4-SNAPSHOT
    2: 0.4.0
    Choose a number: 2: 2 (<-2を入力)

    ...
    Define value for property 'groupId': :    [<-アプリケーションのグループ名を入力] 
    Define value for property 'artifactId': : [<-アプリケーションのプロジェクト名を入力] 
    Define value for property 'version':      [<-アプリケーションの初期バージョンを入力]
    Define value for property 'package':      [<-アプリケーションの基底パッケージ名を入力]
    ...
    Y: : Y

..  Attention::
    バージョン0.4 から、アーキタイプカタログファイルはバージョン毎(マイナーバージョン毎)に個別のファイルを提供するようになりました。旧バージョンのアーキタイプカタログを使用したい場合、以下のアーキタイプカタログURLを指定してください。 

    http://asakusafw.s3.amazonaws.com/maven/archetype-catalog.xml

..  attention::
    旧バージョンで存在していたアーキタイプ ``asakusa-archetype-batchapp`` はバージョン0.2.4で ``asakusa-archetype-thundergate`` に変更されました。バージョン0.2.4以降はこのアーキタイプは使用できません。

``assembly:single`` Asakusa Frameworkのインストールアーカイブを生成
-------------------------------------------------------------------
アーキタイプから作成したプロジェクトのpom.xmlに対して ``assembly:single`` ゴールを実行すると、
Asakusa Framework本体のインストール用アーカイブがプロジェクトの ``target`` ディレクトリ直下に作成されます。
これらのファイルを使用して開発環境、および運用環境にAsakusa Frameworkをインストールします。
以下は アプリケーションプロジェクト名を example-app と指定した場合の実行例を以下に示します。

..  code-block:: sh

    cd example-app
    mvn assembly:single

作成されるインストールアーカイブは、アーキタイプによって異なります。アーキタイプ毎に生成されるインストールアーカイブを以下に示します。

..  list-table:: アーキタイプ ``asakusa-atchetype-windgate`` が作成するインストールアーカイブ一覧
    :widths: 4 6
    :header-rows: 1
    
    * - ファイル名
      - 説明
    * - ``asakusafw-${asakusafw-version}-dev.tar.gz``
      - Asakusa Frameworkを開発環境に展開するためのアーカイブ。後述の ``antrun:run`` ゴールを実行することによって開発環境にインストールする。
    * - ``asakusafw-${asakusafw-version}-windgate.tar.gz``
      - Asakusa FrameworkとWindGateを運用環境に展開するためのアーカイブ。


..  list-table:: アーキタイプ ``asakusa-atchetype-thundergate`` が作成するインストールアーカイブ一覧
    :widths: 4 6
    :header-rows: 1
    
    * - ファイル名
      - 説明
    * - ``asakusafw-${asakusafw-version}-dev.tar.gz``
      - Asakusa Frameworkを開発環境に展開するためのアーカイブ。後述の ``antrun:run`` ゴールを実行することによって開発環境にインストールする。
    * - ``asakusafw-${asakusafw-version}-prod-thundergate.tar.gz``
      - Asakusa FrameworkをThunderGateを運用環境に展開するためのアーカイブ。


..  list-table:: アーキタイプ ``asakusa-atchetype-directio`` が作成するインストールアーカイブ一覧
    :widths: 4 6
    :header-rows: 1
    
    * - ファイル名
      - 説明
    * - ``asakusafw-${asakusafw-version}-dev.tar.gz``
      - Asakusa Frameworkを開発環境に展開するためのアーカイブ。後述の ``antrun:run`` ゴールを実行することによって開発環境にインストールする。
    * - ``asakusafw-${asakusafw-version}-directio.tar.gz``
      - Asakusa Frameworkを運用環境に展開するためのアーカイブ。


``antrun:run`` 開発環境用のAsakusa Frameworkをインストール
----------------------------------------------------------
``antrun:run`` ゴールは、 ``assembly:single`` ゴールで作成した開発環境用のAsakusa Frameworkのインストールアーカイブを使用して、 ``$ASAKUSA_HOME`` 配下にAsakusa Frameworkをインストールします。

..  code-block:: sh

    mvn antrun:run

..  warning::
    アーキタイプ ``asakusa-archetype-thundergate`` を使用している場合、 ``antrun:run`` を実行すると、Asakusa ThunderGateが使用するテンポラリディレクトリが作成されます。
    このディレクトリはデフォルトの設定では ``/tmp/thundergate-asakusa`` となっていますが、一部のLinuxディストリビューションではOSをシャットダウンしたタイミングで ``/tmp`` ディレクトリの内容が消去されるため、再起動後にこのディレクトリを再度作成する必要があります。
    
    テンポラリディレクトリを変更する場合、 ``$ASAKUSA_HOME/bulkloader/conf/bulkloader-conf-db.properties`` の設定値を変更した上で、設定値に対応したテンポラリディレクトリを作成し、このディレクトリのパーミッションを777に変更します。
    
    例えばテンポラリディレクトリを ``/var/tmp/asakusa`` に変更する場合は以下のようにします。

    * ``$ASAKUSA_HOME/bulkloader/conf/bulkloader-conf-db.properties`` の変更
    
        * ``import.tsv-create-dir=/var/tmp/asakusa/importer``
        * ``export.tsv-create-dir=/var/tmp/asakusa/exporter``
    
    * テンポラリディレクトリの作成
    
        * mkdir -p -m 777 /var/tmp/asakusa/importer
        * mkdir -p -m 777 /var/tmp/asakusa/exporter


プロジェクトのディレクトリ構成
==============================
アーキタイプから生成されたプロジェクト構成は以下の通りです [#]_ 。

..  code-block:: sh
     
    project
    |-- pom.xml
    |-- build.properties
    `-- src
    |   |-- main
    |   |   `-- java
    |   |   |   `-- ${package}
    |   |   |       `-- batch      : バッチDSLクラス
    |   |   |       `-- flowpart   : フローDSL(フロー部品)クラス
    |   |   |       `-- jobflow    : フローDSL(ジョブフロー)クラス
    |   |   |       `-- operator   : 演算子クラス
    |   |   |
    |   |   `-- dmdl               : モデルクラス生成用のDMDLスクリプト。
    |   |   `-- sql                 
    |   |       `-- modelgen       : モデルクラス生成用のDDL記述SQLファイル(thundergateのみ)。
    |   |   
    |   `-- test
    |       `-- java
    |       |   `-- ${package}
    |       |       `-- batch      : バッチDSLテストクラス
    |       |       `-- flowpart   : フローDSL(フロー部品)テストクラス
    |       |       `-- jobflow    : フローDSL(ジョブフロー)テストクラス
    |       |       `-- operator   : 演算子テストクラス
    |       |
    |       `-- resources
    |           `-- asakusa-resources.xml      : Asakusa Framework Core 用の定義ファイル
    |           `-- logback-test.xml           : 開発環境上のテスト時に使用されるログ設定ファイル
    |           |
    |           `-- ${package}
    |               `-- batch      : バッチDSL用テストデータ
    |               `-- flowpart   : フローDSL(フロー部品)テストデータ
    |               `-- jobflow    : フローDSL(ジョブフロー)テストデータ
    |
    `-- target (Mavenが標準でtarget配下に出力するファイルの説明は省略)
       |-- ${artifactid}-${version}.jar         : packageフェーズの実行によりjarファイル。Asakusa Frameworkでは使用しません。
       |-- ${artifactid}-${version}-sources.jar : packageフェーズの実行によりjarファイル。Asakusa Frameworkでは使用しません。
       |-- batchc       : DSLコンパイラによるバッチコンパイル結果の出力ディレクトリ。packageフェーズの実行により生成される。
       |-- batchcwork   : DSLコンパイラによるバッチコンパイルのワークディレクトリ。packageフェーズの実行により生成される。
       |-- dmdl         : モデルクラス生成用のDDL記述SQLファイルから生成されるDMDLスクリプト(thundergateのみ)。
       |-- excel        : テストデータ定義シート生成用のディレクトリ。generate-sourcesフェーズの実行により生成される。
       |-- excel_v01    : Asakusa0.1形式のテストデータ定義シート生成用のディレクトリ。デフォルトの設定では出力されない。
       |-- sql          : Thndergate用のDDL作成用ディレクトリ。generate-sourcesフェーズの実行により生成される(thundergateのみ)。
       |-- testdriver   : Asakusa Frameworkのテストドライバが使用するワークディレクトリ。
       |-- generated-sources
           `-- annotations
           |    `-- ${package}
           |        `-- flowpart   : 注釈プロセッサによって生成される演算子ファクトリクラス
           |        `-- operator   : 注釈プロセッサによって生成される演算子ファクトリと実装クラス
           `-- modelgen
                `-- ${package}
                    `-- modelgen
                       `-- dmdl
                       |  `-- csv   : CSV形式を使用する場合に生成されるジョブフロークラス (directio/windgateのみ)
                       |  `-- jdbc  : WindGate/JDBCを使用する場合に生成されるジョブフロークラス (windgateのみ)
                       |  `-- io    : DMDLを元に作成されるデータモデルの入出力ドライバクラス
                       |  `-- model : DMDLを元に作成されるデータモデルクラス
                       `-- table (thundergateのみ)
                       |  `-- model   : テーブル構造を元に作成されるデータモデルクラス
                       |  `-- io      : テーブル構造を元に作成されるデータモデルの入出力ドライバクラス
                       `-- view (thudergateのみ)
                          `-- model   : ビュー情報を元に作成されるデータモデルクラス
                          `-- io      : ビュー情報を元に作成されるデータモデルの入出力ドライバクラス

..  [#] 一部のパッケージやファイルは、アーキタイプの種類やバージョンによっては作成されません。

データモデルクラスの生成
========================
Asakusa Frameworkでは、モデルの定義情報の記述するために、以下２つの方法が提供されています。

1. モデルの定義情報をDMDL(Data Model Definition Language)として記述する [#]_ 
2. モデルの定義情報をSQLのDDLとして記述する( ``asakusa-archetype-thundergate`` のみ) [#]_ 

..  [#] :doc:`../dmdl/start-guide` 
..  [#] :doc:`../dmdl/with-thundergate` 

モデル定義情報の記述方法については上述のドキュメントを参照してください。

以下はモデルの定義情報を記述したスクリプトファイルの配置について説明します。

モデルの定義情報をDMDLとして記述する場合
----------------------------------------
モデルの定義情報をDMDLとして記述する場合、DMDLスクリプトはプロジェクトの ``src/main/dmdl`` ディレクトリ以下に配置してください。
また、スクリプトのファイル名には ``.dmdl`` の拡張子を付けて、UTF-8エンコーディングで保存してください。

DMDLファイルは複数配置することが出来ます。上記ディレクトリ配下にサブディレクトリを作成し、そこにDMDLファイルを配置することも可能です。

モデルの定義情報をSQLのDDLとして記述する場合
--------------------------------------------

..  attention::
    この機能は ``asakusa-archetype-thundergate`` のみで提供されています。
    ``asakusa-archetype-windgate`` や ``asakusa-archetype-directio`` では利用できません。

モデルクラスをSQLのDDLとして記述する場合、SQLファイルはプロジェクトの ``src/main/sql/modelgen`` ディレクトリ以下に配置してください。また、スクリプトのファイル名には ``.sql`` の拡張子を付けて保存してください。

SQLファイルは複数配置することが出来ます。上記ディレクトリ配下にサブディレクトリを作成し、そこにSQLファイルを配置することも可能です。SQLファイルを複数配置した場合、ディレクトリ名・ファイル名の昇順にSQLが実行されます。

なお、Asakusa Framework 0.2からは、SQLファイルは一旦DMDLに変換され、このDMDLからモデルクラスが生成されるようになりました。この時SQLファイルから生成されるDMDLファイルは ``target/dmdl`` ディレクトリに生成されます。

``generate-sources`` モデルクラスの生成とテストデータ定義シートの生成
---------------------------------------------------------------------
アーキタイプから作成したプロジェクトのpom.xmlに対して ``generate-sources`` フェーズを実行するとDMDLコンパイラが起動し、
``target/generated-sources/modelgen`` ディレクトリ以下にデータモデルに関するJavaソースファイルが生成されます。

..  code-block:: sh

    mvn clean generate-sources

モデルクラスに使われるJavaパッケージ名は、デフォルトではアーキタイプ生成時に指定したパッケージ名の末尾に ``.modelgen`` を付加したパッケージになります (例えばアーキタイプ生成時に指定したパッケージが ``com.example`` の場合、モデルクラスのパッケージ名は ``com.example.mogelgen`` になります）。このパッケージ名は、後述するビルド定義ファイルにて変更することが出来ます。

また、 ``generate-sources`` フェーズを実行すると、以下のファイルも合わせて生成されます。

* テストドライバを使ったテストで使用するテストデータ定義シートが ``target/excel`` 配下に生成されます。テストデータ定義シートについては、 :doc:`../testing/using-excel` を参照して下さい。
* (thundergateのみ)ThunderGateが使用する管理テーブル用DDLスクリプトが ``target/sql`` 配下に生成され、開発環境用のデータベースに対してこのSQLが実行されます。ThunderGateが要求するテーブルが自動的に作成されるため、テストドライバを使ったテストがすぐに行える状態になります。


.. _maven-archetype-batch-compile:


バッチコンパイルとバッチアプリケーションアーカイブの生成
========================================================
Asakusa DSLで記述したバッチアプリケーションをHadoopクラスタにデプロイするためには、Asakusa DSLコンパイラを実行してバッチアプリケーション用のアーカイブファイルを作成します。

DSLコンパイラについての詳しい情報は :doc:`../dsl/user-guide` を参照してください。


``package`` バッチコンパイルの実行
----------------------------------
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
    バッチコンパイルの最中にJavaのソースファイルのコンパイル時に以下の警告が表示されることがあります。
    これは、DSLコンパイラが「スパイラルコンパイル」という方式でコンパイルを段階的に実行している過程の警告であり、
    最終的にコンパイルが成功していれば問題ありません。

..  code-block:: sh

    [WARNING] ... src/main/java/example/flowpart/ExFlowPart.java:[20,23] シンボルを見つけられません。
    シンボル: クラス ExOperatorFactory

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

``eclipse:eclipse`` プロジェクトにEclipse用定義ファイルを追加
-------------------------------------------------------------
アプリケーション用プロジェクトにEclipseプロジェクト用の定義ファイルを追加します。このコマンドを実行することによってEclipseからプロジェクトをインポートすることが可能になります。

例えば、バッチアプリケーション用プロジェクト「example-app」のEclipse定義ファイルを作成するには、プロジェクトのディレクトリに移動し、以下のコマンドを実行します。

..  code-block:: sh

    cd example-app
    mvn eclipse:eclipse

EclipseからプロジェクトをImportするには、Eclipseのメニューから [File] -> [Import] -> [General] -> [Existing Projects into Workspace] を選択し、プロジェクトディレクトリを指定します。

..  code-block:: sh

    mvn clean eclipse:eclipse

Mavenプロジェクトへの変換(m2eプラグインの利用)
----------------------------------------------
m2eプラグインを使ってアプリケーション用プロジェクトをMavenプロジェクトに変換すると、Eclipse上からMavenを実行することが可能になるなど、いくつか便利な機能を使用できます。

Mavenプロジェクトへの変換は任意です。変換を行う場合は以下の手順に従ってください。

m2e buildhelper connector のインストール
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
m2eの拡張機能であるm2e buildhelper connectorをインストールします。

1. Eclipseのメニューから [Window] -> [Preferences] -> [Maven] -> [Discovery] を選択し、ダイアログに表示される [Open Dialog] ボタンを押下します。
2. install n2e connectors ダイアログが表示されるので、このなかから「buildhelper」のチェックをONにして [Finish] ボタンを押下します。
3. ウィザードに従ってconnectorをインストールします。
    1. Install ダイアログでは そのまま [Next>] ボタンを押下します。
    2. Install Details ダイアログでは そのまま [Next>] ボタンを押下します。
    3. Review Licenses ダイアログでは [I accept...] を選択して [Finish] ボタンを押下します。
    4. Security Warinig ダイアログが表示された場合、そのまま [OK] ボタンを押下します。
    5. Software Updates ダイアログではEclipseの再起動を促されるので、 [Yes] ボタンを押下してEclipseを再起動します。

Mavenプロジェクトへの変換
~~~~~~~~~~~~~~~~~~~~~~~~~
Eclipseのパッケージエクスプローラーからアプリケーション用プロジェクトを右クリックして [Configure] -> [Convert to Maven Project] を選択します。

これでMavenプロジェクトへの変換が行われました。アプリケーション用プロジェクトに対してMavenを実行する場合は、アプリケーション用プロジェクトを右クリックして [Run As] を選択するとサブメニューに [Maven build...] など、いくつかのMaven実行用メニューが表示されるのでこれを選択してください。

アプリケーション用依存ライブラリの追加
======================================
バッチアプリケーションの演算子から共通ライブラリ（Hadoopによって提供されているライブラリ以外のもの）を使用する場合は、まず通常のMavenを使ったアプリケーションと同様pom.xmlに依存定義( ``<dependency>`` )を追加します。
これに加えて、依存するjarファイルを ``$ASAKUSA_HOME/ext/lib`` ディレクトリに配置します。

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

Asakusaの拡張ライブラリディレクトリへjarファイルを配置
------------------------------------------------------

``target/dependency`` にコピーしたjarファイルから必要なものを選んで ``$ASAKUSA_HOME/ext/lib`` ディレクトリに配置します。

..  code-block:: sh

    cp target/dependency/joda-time-2.1.jar $ASAKUSA_HOME/ext/lib


``build.properties`` ビルド定義ファイル
=======================================
アーキタイプから作成したプロジェクトの ``build.properties`` はプロジェクトのビルドや各種ツールの動作を設定します。設定項目について以下に説明します。

項目値が択一式の項目については、デフォルト値を **太字** で示しています。

---------------------

General Settings

  asakusa.database.enabled
    *(asakusa-archetype-thundergateのみ)*

    ( **true** or false ) このプロパティをfalseにすると、Asakusa Frameworkの開発環境へのインストール( ``antrun:run`` )、及びモデル生成処理 ( ``generate-sources`` ) でデータベースに対する処理を行わなくなります。
    
    モデルの定義をDMDLのみで行う場合は、このオプションをfalseにするとデータベースを使用せずにモデル生成を行うことが可能になります。

  asakusa.database.target
    *(asakusa-archetype-thundergateのみ)*

    Asakusa Frameworkの開発環境へのインストール( ``antrun:run`` )、及びモデル生成処理 ( ``generate-sources`` ) でデータベースを使用する場合に、データベース定義ファイルを特定するためのターゲット名を指定します。
    
    開発環境で使用するデータベース定義ファイルは、ローカルにインストールしたAsakusa FrameworkのThunderGate用データベース定義ファイル ( $ASAKUSA_HOME/bulkloader/conf/${asakusa.database.target}-jdbc.properties )を使用します。開発環境へのインストール時に本プロパティの設定値を使って左記ディレクトリにデータベース定義ファイルを生成します。
    
    通常はこの値を変更する必要はありませんが、ThnderGateのインポータ/エクスポータ記述でターゲット名を変更している場合にはターゲット名に合わせて変更します。また、１つの開発環境で複数のアプリケーションプロジェクトに対して作業している場合に、それぞれのプロジェクトでデータベースを分けておきたい場合に個別の値を指定すると便利です。
    
    なお、インポータ/エクスポータ記述で複数のデータソースを指定している場合は、本ターゲット名は使用しているデータソース名のうちいずれか１つのデータソースを使用し、データベース定義ファイルはターゲット分の定義ファイルを$ASAKUSA_HOME/bulkloader/conf配下に配置します。その上で、定義ファイル内に記述するすべてのデータベース設定をすべて同じ内容にしてください（Asakusa Framework 0.2時点ではAsakusa Frameworkのテストツールが複数データソースに対応していないため）。

---------------------

Batch Compile Settings

  asakusa.package.default
    バッチコンパイル時に生成されるHadoopのジョブ、及びMapReduce関連クラスのJavaパッケージを指定します。デフォルト値はアーキタイプ生成時に指定した ``package`` の値に ``.batchapp`` を付与した値になります。

  asakusa.batchc.dir
    バッチコンパイル時に生成されるHadoopのジョブ、及びMapReduce関連クラスの出力ディレクトリを指定します。 ``package`` フェーズを実行した時に生成されるjarファイルは、このディレクトリ配下のソースをアーカイブしたものになります。

  asakusa.compilerwork.dir
    バッチコンパイル時にコンパイラが使用するワークディレクトリを指定します。

  asakusa.hadoopwork.dir
    Asakusa Frameworkがジョブフローの実行毎にデータを配置するHadoopファイルシステム上のディレクトリを、ユーザのホームディレクトリからの相対パスで指定します。
    
    パスに文字列 ``${execution_id}`` が含まれる場合、ワークフローエンジンから指定されたexecution_idによって置換されます。デフォルト値はexecution_idが指定されているため、ジョブフローの実行毎にファイルシステム上は異なるディレクトリが使用されることになります。

---------------------

Model Generator Settings

  asakusa.modelgen.package
    モデルジェネレータによるモデル生成時にモデルクラスに付与されるJavaパッケージを指定します。デフォルト値は、アーキタイプ生成時に指定した ``package`` の値に ``.modelgen`` を付与した値になります。

  asakusa.modelgen.includes
    ``generate-sources`` フェーズ実行時にモデルジェネレータ、およびテストデータ定義シート生成ツールが生成対象とするモデル名を正規表現の書式で指定します。
    
  asakusa.modelgen.excludes
    ``generate-sources`` フェーズ実行時にモデルジェネレータ、およびテストデータ定義シート生成ツールが生成対象外とするモデル名を正規表現の書式で指定します。デフォルト値はThunderGateが使用する管理テーブルを生成対象外とするよう指定されています。特に理由が無い限り、デフォルト値で指定されている値は削除しないようにして下さい。

  asakusa.modelgen.sid.column
    *(asakusa-archetype-thundergateのみ)*

    ThunderGateが入出力を行う業務テーブルのシステムIDカラム名を指定します。この値はThunderGate用のデータベースノード用プロパティファイル(bulkloader-conf-db.properties)のプロパティ ``table.sys-column-sid`` と同じ値を指定してください。この項目はThunderGateキャッシュを使用する場合にのみ必要です。

  asakusa.modelgen.timestamp.column
    *(asakusa-archetype-thundergateのみ)*

    ThunderGateが入出力を行う業務テーブルの更新日時カラム名を指定します。この値はThunderGate用のデータベースノード用プロパティファイル(bulkloader-conf-db.properties)のプロパティ ``table.sys-column-updt-date`` と同じ値を指定してください。この項目はThunderGateキャッシュを使用する場合にのみ必要です。

  asakusa.modelgen.delete.column
    *(asakusa-archetype-thundergateのみ)*

    ThunderGateが入出力を行う業務テーブルの論理削除フラグカラム名を指定します。この項目はThunderGateキャッシュを使用する場合にのみ必要です。

  asakusa.modelgen.delete.value
    *(asakusa-archetype-thundergateのみ)*

    ThunderGateが入出力を行う業務テーブルの論理削除フラグが削除されたことを示す値を指定します。この項目はThunderGateキャッシュを使用する場合にのみ必要です。

  asakusa.modelgen.output
    モデルジェネレータが生成するモデルクラス用Javaソースの出力ディレクトリを指定します。アーキタイプが提供するEclipseの設定情報と対応しているため、特に理由が無い限りはデフォルト値を変更しないようにしてください。この値を変更する場合、合わせてpom.xmlの修正も必要となります。

  asakusa.dmdl.encoding
    DMDLスクリプトが使用する文字エンコーディングを指定します。

  asakusa.dmdl.dir
    DMDLスクリプトを配置するディレクトリを指定します。

---------------------

ThunderGate Settings

  asakusa.bulkloader.tables
    *(asakusa-archetype-thundergateのみ)*

    ``generate-sources`` フェーズ実行時に生成されるThunderGate管理テーブル用DDLスクリプト（後述の「asakusa.bulkloader.genddl」で指定したファイル）に含める対象テーブルを指定します。このプロパティにインポート、及びエクスポート対象テーブルのみを指定することで、余分な管理テーブルの生成を抑止することが出来ます。開発時にはデフォルト（コメントアウト）の状態で特に問題ありません。

  asakusa.bulkloader.genddl
    *(asakusa-archetype-thundergateのみ)*

    ``generate-sources`` フェーズ実行時に生成されるThunderGate管理テーブル用DDLスクリプトのファイルパスを指定します。

  asakusa.dmdl.fromddl.output
    *(asakusa-archetype-thundergateのみ)*

    ``generate-sources`` フェーズ実行時にモデル定義情報となるDDLスクリプトから生成するDMDLスクリプトの出力先を指定します。

---------------------

TestDriver Settings

  asakusa.testdatasheet.generate
    ( **true** or false ) このプロパティをfalseにすると、 ``generate-sources`` フェーズ実行時にテストデータ定義シートの作成を行わないようになります。テストドライバを使ったテストにおいて、テストデータの定義をExcelシート以外で管理する場合はfalseに設定してください。

  asakusa.testdatasheet.format
    ``generate-sources`` フェーズ実行時に生成されるテストデータ定義シートのフォーマットを指定します。以下の値を指定することが出来ます。
      * DATA: テストデータ定義シートにテストデータの入力データ用シートのみを含めます。
      * RULE: テストデータ定義シートにテストデータの検証ルール用シートのみを含めます。
      * INOUT: テストデータ定義シートにテストデータの入力データ用シートと出力（期待値）用シートを含めます。
      * INSPECT: テストデータ定義シートにテストデータの出力（期待値）用シートと検証ルール用シートのみを含めます。
      * **ALL**: テストデータ定義シートに入力データ用シート、出力（期待値）用シート、検証ルール用シートを含めます。

  asakusa.testdatasheet.output
    ``generate-sources`` フェーズ実行時に生成されるテストデータ定義シートの出力ディレクトリを指定します。

  asakusa.testdriver.compilerwork.dir
    テストドライバの実行時にテストドライバの内部で実行されるバッチコンパイルに対してコンパイラが使用するワークディレクトリを指定します。 
    
    ``asakusa.compilerwork.dir`` と同じ働きですが、この項目はテストドライバの実行時にのみ使われます。

  asakusa.testdriver.hadoopwork.dir
    テストドライバの実行時にテストドライバの内部で使用される、ジョブフローの実行毎にデータを配置するHadoopファイルシステム上のディレクトリを、ユーザのホームディレクトリからの相対パスで指定します。Hadoopのスタンドアロンモード使用時には、OS上のユーザのホームディレクトリが使用されます。

    ``asakusa.hadoopwork.dir`` と同じ働きですが、この項目はテストドライバの実行時にのみ使われます。

---------------------

TestDriver Settings (for Asakusa 0.1 asakusa-test-tools)

  asakusa.testdatasheet.v01.generate
    *(asakusa-archetype-thundergateのみ)*

    ( true or **false** ) Asakusa Framework 0.1 仕様のテストデータ定義シートを出力するかを設定します（デフォルトは出力しない）。 このプロパティをtrueにすると、 ``generate-sources`` フェーズ実行時にテストデータ定義シートが ``target/excel_v01`` ディレクトリ配下に出力されるようになります。

  asakusa.testdriver.testdata.dir
    *(asakusa-archetype-thundergateのみ)*

    テストドライバの実行時に、テストドライバが参照するテストデータ定義シートの配置ディレクトリを指定します。
    
    このプロパティは、テストドライバAPIのうち、Asakusa Framework 0.1 から存在する ``*TestDriver`` というクラスの実行時のみ使用されます。Asakusa Framework 0.2 から追加された ``*Tester`` 系のテストドライバAPIは、この値を使用せず、テストドライバ実行時のクラスパスからテストデータ定義シートを参照するようになっています。

  asakusa.excelgen.tables
    *(asakusa-archetype-thundergateのみ)*

    Asakusa Framework 0.1 仕様のテストデータ定義シート生成ツールをMavenコマンドから実行 ( ``mvn exec:java -Dexec.mainClass=com.asakusafw.testtools.templategen.Main`` )した場合に、テストデータシート生成ツールが生成の対象とするテーブルをスペース区切りで指定します。
    
