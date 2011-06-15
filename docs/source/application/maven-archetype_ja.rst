=======================
Asakusa Maven Archetype
=======================

この文書では、Asakusa Frameworkが提供しているMavenアーキタイプを使ってバッチアプリケーション開発用プロジェクトを作成する手順を説明します。また作成したプロジェクト上でAsakusa Frameworkが提供するモデル生成ツールやバッチコンパイラを使用する方法、およびこれらのツールの設定方法について解説します。

アプリケーション開発プロジェクトの作成
======================================
Asakusa Frameworkが提供すMavenアーキタイプ ``asakusa-archetype-batchapp`` を使ってアプリケーション開発用プロジェクトを作成します。Asakusa Frameworkでは、アプリケーションを開発するために２つの方法を提供しています。

1. Asakusa Frameworkが提供するバッチアプリケーション作成用スクリプトを使用する方法
2. Mavenコマンドを使用して、段階的にプロジェクトを構築する方法

バッチアプリケーション作成用スクリプト
--------------------------------------
バッチアプリケーション作成用スクリプトはアプリケーション開発環境構築用のMavenコマンドのラップスクリプトで、本スクリプトを使用すると１回のコマンド実行でアプリケーション開発用プロジェクトの作成とAsakusa Frameworkのインストールが行われるため便利です。このコマンドは ``$HOME/worspace`` 配下にプロジェクトディレクトリを作成します。

バッチアプリケーション作成用スクリプト ``setup_batchapp_project.sh`` はGitHub上のasakusa-contribリポジトリに置かれています。以下の手順でスクリプトを取得します。

..  code-block:: sh

    wget https://raw.github.com/asakusafw/asakusafw-contrib/master/development-utilities/scripts/setup_batchapp_project.sh
    chmod +x setup_batchapp_project.sh

setup_batchapp_project.shは以下の引数を指定して実行します。

..  list-table:: バッチアプリケーション作成用スクリプトの引数
    :widths: 1 9
    :header-rows: 1
    
    * - no
      - 説明
    * - 1
      - グループID (パッケージ名)
    * - 2
      - アーティファクトID (プロジェクト名)
    * - 3
      - Asakusa Frameworkのpom.xml上のVersion [#]_ 
      
..  [#] 指定可能なVersionは次のアーキタイプカタログを参照:https://asakusafw.s3.amazonaws.com/maven/archetype-catalog.xml
    
例えばAsakusa Framework 0.2.0(SNAPSHOT)を使ったアプリケーションプロジェクトを作成する場合は以下のように実行します。この例では ``$HOME/workspace`` 配下にプロジェクト ``batchapp-sample`` ディレクトリが作成されます。

..  code-block:: sh

    ./setup_batchapp_project.sh com.example batchapp-sample 0.2.0-SNAPSHOT

Maven:プロジェクトの作成とAsakusa Frameworkのインストール
---------------------------------------------------------
バッチアプリケーション作成用スクリプトを使わず、Mavenコマンドを使用して段階的にプロジェクトを構築する場合は以下の手順で実行します。

``archetype:generate`` アーキタイプからプロジェクトを生成
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Asakusa Frameworkが公開しているMavenアーキタイプカタログを指定してアプリケーション開発用プロジェクトを作成します。

..  code-block:: sh

    mvn archetype:generate -DarchetypeCatalog=http://asakusafw.s3.amazonaws.com/maven/archetype-catalog.xml
    ...
    Choose archetype:
    1: http://asakusafw.s3.amazonaws.com/maven/archetype-catalog.xml -> asakusa-archetype-batchapp (-)
    Choose a number: : ※1を入力
    ...
    Choose version: 
    1: 0.1.0
    2: 0.1.1-SNAPSHOT
    3: 0.2.0-SNAPSHOT

    Choose a number: 3: ※3を入力
    ...
    Define value for property 'groupId': : com.example ※任意の値を入力
    Define value for property 'artifactId': : batchapp-sample ※任意の値を入力
    Define value for property 'version':  1.0-SNAPSHOT ※任意の値を入力
    Define value for property 'package':  com.example ※任意の値を入力
    ...
    Y: : Yを入力

``assembly:single`` Asakusa Frameworkインストールアーカイブ(開発環境用)を生成
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
アーキタイプから作成したプロジェクトのpom.xmlに対して ``assembly:single`` ゴールを実行すると、Asakusa Framework本体のインストール用アーカイブが ``target`` 配下に ``${artifactid}-${version}-asakusa-install-dev.tar.gz`` というファイル名（ ``${artifactid}`` と ``${version}`` は上記 ``archetype:generate`` の実行時に指定したアーティファクトID、バージョンがそれぞれ使われる）で作成されます。 

このアーカイブファイルは、後述する ``antrun:run`` ゴールと組み合わせてAsakusa Frameworkをローカル環境にインストールするために使用します。

``antrun:run`` Asakusa Frameworkの開発環境用インストール(開発環境用)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
``assembly:single`` ゴールで作成したAsakusa Framework本体のインストール用アーカイブを使用して、 ``$ASAKUSA_HOME`` 配下にAsakusa Frameworkをインストールします。

``assembly:single`` と ``antrun:run`` を組み合わせて、以下のようにAsakusa Frameworkをローカル環境にインストールします。

..  code-block:: sh

    cd batchapp-sample
    mvn assembly:single antrun:run

プロジェクトのディレクトリ構成
------------------------------
アーキタイプ ``asakusa-archetype-batchapp`` から生成されたAsakusaのプロジェクト構成は以下の通りです。

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
    |   |   `-- assembly           : ローカル環境へAsakusa Frameworkをインストールするためのスクリプト。
    |   |   `-- dmdl               : モデルクラス生成用のDMDLスクリプト。
    |   |   `-- scripts            : Asakusa Frameworkが提供する自動生成ツールやコンパイラの制御スクリプト
    |   |   `-- sql                : モデルクラス生成用のDDL記述SQLファイル。
    |   |   
    |   `-- test
    |       `-- java
    |       |   `-- ${package}
    |       |       `-- batch      : バッチDSLテストクラス
    |       |       `-- flowpart   : フローDSL(フロー部品)テストクラス
    |       |       `-- jobflow    : フローDSL(ジョブフロー)テストクラス
    |       |       `-- operator   : 演算子テストクラス (プロジェクト生成時は存在しません)
    |       |
    |       `-- resources
    |           `-- asakusa-jdbc.properties    : Asakusa FrameworkのDB設定ファイル
    |           `-- asakusa-resources.xml      : Asakusa Framework Core Runtime用の定義ファイル
    |           `-- logback-test.xml           : 開発環境上のテスト時に使用されるログ設定ファイル
    |           |
    |           `-- ${package}
    |               `-- batch      : バッチDSL用テストデータ
    |               `-- flowpart   : フローDSL(フロー部品)テストデータ
    |               `-- jobflow    : フローDSL(ジョブフロー)テストデータ
    |
    `-- target ※Mavenが標準でtarget配下に出力するファイルの説明は省略
       |-- ${artifactid}-batchapps-${version}.jar 
       |      : Ashigel Compilerによりバッチコンパイルされたバッチアプリケーションのアーカイブ。
       |        Mavenのpacageフェーズの実行により生成される。
       |
       |-- ${artifactid}-XX.jar         : Mavenにより生成されるjarファイルですが、Asakusa Frameworkでは使用しません。
       |-- ${artifactid}-XX-sources.jar : Mavenにより生成されるjarファイルですが、Asakusa Frameworkでは使用しません。
       |
       |-- batchc       : Ashigel Compilerによるバッチコンパイル結果の出力ディレクトリ。Mavenのpacageフェーズの実行により生成される。
       |-- batchcwork   : Ashigel Compilerによるバッチコンパイルのワークディレクトリ。
       |-- dmdl         : モデルクラス生成用のDDL記述SQLファイルから生成されるDMDLスクリプト。
       |-- excel        : テストデータ定義シート生成用のディレクトリ。Mavenのgenerate-sourcesフェーズの実行により生成される。
       |-- excel_v01    : Asakusa0.1形式のテストデータ定義シート生成用のディレクトリ。デフォルトの設定では出力されない。
       |-- sql          : Thndergate用のDDL作成用ディレクトリ。Mavenのgenerate-sourcesフェーズの実行により生成される。
       |-- testdriver   : Asakusa Frameworkのテストドライバが使用するワークディレクトリ。
       |
       |-- generated-sources
           `-- annotations
           |    `-- ${package}
           |        `-- flowpart   : 注釈プロセッサによって生成される演算子ファクトリクラス
           |        `-- operator   : 注釈プロセッサによって生成される演算子ファクトリと実装クラス
           `-- modelgen
                `-- ${package}
                    `-- modelgen
                       `-- table
                       |  `-- model   : テーブル構造を元に作成したデータモデルクラス
                       |  `-- io      : テーブル構造を元に作成したデータモデルの入出力ドライバクラス
                       `-- view
                          `-- model   : ビュー情報を元に作成したデータモデルクラス
                          `-- io      : ビュー情報を元に作成したデータモデルの入出力ドライバクラス

モデルクラスの生成
==================
モデルクラスを作成するには、モデルの定義情報を記述後にMavenの ``generate-sources`` フェーズを実行します。

Asakusa Frameworkでは、モデルの定義情報の記述するために、以下２つの方法が提供されています。

1. モデルの定義情報をDMDL(Data Model Definition Language)として記述する [#]_ 
2. モデルの定義情報をSQLのDDLとして記述する [#]_ 

..  [#] :doc:`../dmdl/start-guide_ja` 
..  [#] :doc:`../dmdl/with-thundergate_ja` 

モデル定義情報の記述方法については上述のドキュメントを参照してください。

以下はモデルの定義情報を記述したスクリプトファイルの配置について説明します。

モデルの定義情報をDMDLとして記述する場合
----------------------------------------
モデルの定義情報をDMDLとして記述する場合、DMDLスクリプトはプロジェクトの ``src/main/dmdl`` ディレクトリ以下に配置してください。また、スクリプトのファイル名には ``.dmdl`` の拡張子を付けて保存してください。

DMDLファイルは複数配置することが出来ます。上記ディレクトリ配下にサブディレクトリを作成し、そこにSQLファイルを配置することも可能です。

モデルの定義情報をSQLのDDLとして記述する場合
--------------------------------------------
モデルクラスをSQLのDDLとして記述する場合、SQLファイルはプロジェクトの ``src/main/sql`` ディレクトリ以下に配置してください。また、スクリプトのファイル名には ``.sql`` の拡張子を付けて保存してください。

SQLファイルは複数配置することが出来ます。上記ディレクトリ配下にサブディレクトリを作成し、そこにSQLファイルを配置することも可能です。SQLファイルを複数配置した場合、ディレクトリ名・ファイル名の昇順にSQLが実行されます。

なお、Asakusa Framework 0.2からは、SQLファイルは一旦DMDLに変換され、このDMDLからモデルクラスが生成されるようになりました。この時SQLファイルから生成されるDMDLファイルは ``target/dmdl`` ディレクトリに生成されます。

Maven:モデルの生成とテストデータ定義シートの生成
------------------------------------------------

``generate-sources`` モデルクラスの生成とテストデータ定義シートの生成
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
アーキタイプから作成したプロジェクトのpom.xmlに対して ``generate-sources`` フェーズを実行するとモデルジェネレータによるモデル生成処理が実行され  ``target/generated-sources/modelgen`` ディレクトリにモデルクラス用のJavaソースファイルが生成されます。

..  code-block:: sh

    mvn generate-sources

モデルクラスに使われるJavaパッケージ名は、デフォルトではアーキタイプ生成時に指定したパッケージ名の末尾に ``.modelgen`` を付加したパッケージになります (例えばアーキタイプ生成時に指定したパッケージが ``com.example`` の場合、モデルクラスのパッケージ名は ``com.example.mogelgen`` になります）。このパッケージ名は、後述する TODO ビルド定義ファイルのプロパティXXX にて変更することが出来ます。

.. todo:: ビルド定義ファイルへのリンク

また、generate-sources フェーズを実行すると、テストドライバを使ったテストで使用するテストデータ定義シートが ``target/excel`` 配下に生成されます。テストデータ定義シートについては、TODO テストドライバ を参照して下さい。

.. todo:: テストドライバへのリンク

Asakusa DSLのバッチコンパイルとアプリケーションアーカイブの生成
===============================================================
Asakusa DSLで記述したバッチアプリケーションをHadoopクラスタにデプロイするためには、Ashigelコンパイラのバッチコンパイルを実行し、バッチアプリケーション用のアーカイブファイルを作成します。

Maven:バッチコンパイル
----------------------

``package`` バッチコンパイルの実行
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
アーキタイプから作成したプロジェクトのpom.xmlに対して ``package`` フェーズを実行するとバッチコンパイルが実行されます。

..  code-block:: sh

    mvn package

なお、Asakusa DSLのコンパイル時に以下例のように演算子ファクトリクラスのシンボルが見つからない旨のワーニングメッセージが出力されることがありますが、このメッセージが出力されても正常にコンパイルが行われているため、この警告メッセージは無視してください。

..  code-block:: sh

    [WARNING] ... src/main/java/example/flowpart/ExFlowPart.java:[20,23] シンボルを見つけられません。
    シンボル: クラス ExOperatorFactory

Mavenの標準出力に ``BUILD SUCCESS`` が出力されればバッチコンパイルは成功です。バッチコンパイルが完了すると、 ``target`` ディレクトリにバッチコンパイル結果のアーカイブファイルが ``${artifactid}-batchapps-${version}.jar`` というファイル名で生成されます。

``${artifactid}-batchapps-${version}.jar`` はHadoopクラスタ上でjarファイルを展開してデプロイします。Hadoopクラスタへのアプリケーションのデプロイについては TODO [[Deployment Guide]]」を参照して下さい。

.. todo:: AdministrationGuideへのリンク

バッチコンパイルオプションの指定
--------------------------------

バッチのビルドオプションを指定するには、pom.xmlのプロファイルに定義されているプロパティ ``asakusa.compiler.options`` に値を設定します。設定できる値は「+<有効にするオプション名>」や「-<無効にするオプション名>」のように、オプション名の先頭に「+」や「-」を指定します。また、複数のオプションを指定するには「,」(カンマ)でそれぞれを区切ります。

指定出来るバッチコンパイルのオプションについては、 TODO DSLユーザガイド を参照してください。

.. todo:: コンパイルオプションへのリンク

Eclipse
==================
TODO スタートガイドへのリンク

ビルド定義ファイル
==================

TODO build.properties について

