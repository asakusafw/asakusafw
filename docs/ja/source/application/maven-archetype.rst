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

    wget http://raw.github.com/asakusafw/asakusafw-contrib/master/development-utilities/scripts/setup_batchapp_project.sh
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
      
..  [#] 指定可能なVersionは次のアーキタイプカタログを参照:http://asakusafw.s3.amazonaws.com/maven/archetype-catalog.xml
    
例えばAsakusa Framework ver.0.2.0を使ったアプリケーションプロジェクトを作成する場合は以下のように実行します。この例では ``$HOME/workspace`` 配下にプロジェクト ``batchapp-sample`` ディレクトリが作成されます。

..  code-block:: sh

    ./setup_batchapp_project.sh com.example batchapp-sample 0.2.0

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
    2: 0.2-SNAPSHOT
    3: 0.2.0
    4: 0.2.1-RC1
    5: 0.3-SNAPSHOT
    
    Choose a number: 5: ※3を入力
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

..  warning::
    ``antrun:run`` を実行すると、Asakusa ThunderGateが使用するテンポラリディレクトリが作成されます。このディレクトリはデフォルトの設定では /tmp/asakusa となっていますが、一部のLinuxディストリビューションではシャットダウンしたタイミングで /tmp ディレクトリがクリアされるため、再起動後にこのディレクトリを再度作成する必要があります。
    
    テンポラリディレクトリを変更する場合、$ASAKUSA_HOME/bulkloader/conf/bulkloader-conf-db.properties の以下の設定値を変更した上で、設定値に対応したテンポラリディレクトリを作成し、このディレクトリのパーミッションを777に変更します。
    
    例えばテンポラリディレクトリを /var/tmp/asakusa に変更する場合は以下のようにします。

    * $ASAKUSA_HOME/bulkloader/conf/bulkloader-conf-db.propertiesの変更
    
        * import.tsv-create-dir=/var/tmp/asakusa/importer
        * export.tsv-create-dir=/var/tmp/asakusa/exporter
    
    * テンポラリディレクトリの作成
    
        * mkdir -p -m 777 /var/tmp/asakusa/importer
        * mkdir -p -m 777 /var/tmp/asakusa/exporter

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
       |-- ${artifactid}-${version}.jar         : Mavenにより生成されるjarファイルですが、Asakusa Frameworkでは使用しません。
       |-- ${artifactid}-${version}-sources.jar : Mavenにより生成されるjarファイルですが、Asakusa Frameworkでは使用しません。
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

..  [#] :doc:`../dmdl/start-guide` 
..  [#] :doc:`../dmdl/with-thundergate` 

モデル定義情報の記述方法については上述のドキュメントを参照してください。

以下はモデルの定義情報を記述したスクリプトファイルの配置について説明します。

モデルの定義情報をDMDLとして記述する場合
----------------------------------------
モデルの定義情報をDMDLとして記述する場合、DMDLスクリプトはプロジェクトの ``src/main/dmdl`` ディレクトリ以下に配置してください。また、スクリプトのファイル名には ``.dmdl`` の拡張子を付けて保存してください。

DMDLファイルは複数配置することが出来ます。上記ディレクトリ配下にサブディレクトリを作成し、そこにSQLファイルを配置することも可能です。

モデルの定義情報をSQLのDDLとして記述する場合
--------------------------------------------
モデルクラスをSQLのDDLとして記述する場合、SQLファイルはプロジェクトの ``src/main/sql/modelgen`` ディレクトリ以下に配置してください。また、スクリプトのファイル名には ``.sql`` の拡張子を付けて保存してください。

SQLファイルは複数配置することが出来ます。上記ディレクトリ配下にサブディレクトリを作成し、そこにSQLファイルを配置することも可能です。SQLファイルを複数配置した場合、ディレクトリ名・ファイル名の昇順にSQLが実行されます。

なお、Asakusa Framework 0.2からは、SQLファイルは一旦DMDLに変換され、このDMDLからモデルクラスが生成されるようになりました。この時SQLファイルから生成されるDMDLファイルは ``target/dmdl`` ディレクトリに生成されます。

Maven:モデルの生成とテストデータ定義シートの生成
------------------------------------------------

``generate-sources`` モデルクラスの生成とテストデータ定義シートの生成
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
アーキタイプから作成したプロジェクトのpom.xmlに対して ``generate-sources`` フェーズを実行するとモデルジェネレータによるモデル生成処理が実行され  ``target/generated-sources/modelgen`` ディレクトリにモデルクラス用のJavaソースファイルが生成されます。

..  code-block:: sh

    mvn generate-sources

モデルクラスに使われるJavaパッケージ名は、デフォルトではアーキタイプ生成時に指定したパッケージ名の末尾に ``.modelgen`` を付加したパッケージになります (例えばアーキタイプ生成時に指定したパッケージが ``com.example`` の場合、モデルクラスのパッケージ名は ``com.example.mogelgen`` になります）。このパッケージ名は、後述するビルド定義ファイルにて変更することが出来ます。

また、generate-sources フェーズを実行すると、以下のファイルも合わせて生成されます。

* テストドライバを使ったテストで使用するテストデータ定義シートが ``target/excel`` 配下に生成されます。テストデータ定義シートについては、 :doc:`../testing/using-excel` を参照して下さい。
* ThunderGateが使用する管理テーブル用DDLスクリプトが ``target/sql`` 配下に生成され、開発環境用のデータベースに対してこのSQLが実行されます。ThunderGateが要求するテーブルが自動的に作成されるため、テストドライバを使ったテストがすぐに行える状態になります。

.. _maven-archetype-batch-compile:

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

``${artifactid}-batchapps-${version}.jar`` はHadoopクラスタ上でjarファイルを展開してデプロイします。Hadoopクラスタへのアプリケーションのデプロイについては  :doc:`administrator-guide` を参照して下さい。

..  note::
    バッチコンパイルを実行すると、 ``target`` ディレクトリ配下には ``${artifactid}-batchapps-${version}.jar`` の他に ``${artifactid}-${version}.jar`` , ``${artifactid}-${version}-sources.jar`` という名前のjarファイルも同時に作成されます。これらのファイルはMavenの標準の ``package`` フェーズの処理により作成されるjarファイルですが、Asakusa Frameworkではこれらのファイルは使用しません。これらのファイルをHadoopクラスタにデプロイしてもバッチアプリケーションとしては動作しないので注意してください。

バッチコンパイルオプションの指定
--------------------------------

バッチのビルドオプションを指定するには、pom.xmlのプロファイルに定義されているプロパティ ``asakusa.compiler.options`` に値を設定します。設定できる値は「+<有効にするオプション名>」や「-<無効にするオプション名>」のように、オプション名の先頭に「+」や「-」を指定します。また、複数のオプションを指定するには「,」(カンマ)でそれぞれを区切ります。

指定出来るバッチコンパイルのオプションについては、  :doc:`../dsl/user-guide` の :ref:`batch-compile-options` を参照してください。

Eclipseを使ったアプリケーションの開発
=====================================
Eclipseを使ってアプリケーションを開発する場合、アーキタイプから作成したプロジェクトのpom.xmlに対して ``eclipse:eclipse`` ゴールを実行します。また、Eclipseに対してMavenリポジトリのロケーションを指定するために ``eclipse:add-maven-repo`` ゴールを実行します。

詳しくは、 :doc:`user-guide` の :ref:`user-guide-eclipse` を参照して下さい。

アプリケーション用依存ライブラリの追加
======================================
バッチアプリケーションの演算子から共通ライブラリ（Hadoopによって提供されているライブラリ以外のもの、例えばApache Commons Lang等）を使用する場合は、まず通常のMavenを使ったアプリケーションと同様pom.xmlに依存定義(<dependency>)を追加します。これに加えて依存するjarファイルを $ASAKUSA_HOME/ext/lib ディレクトリに配置する必要があります。以下はApache Commons Langを配置する例です。

pom.xmlの編集
-------------

pom.xmlの<dependencies>配下に依存定義を追加します。

..  code-block:: sh

    <dependency>
        <groupId>commons-lang</groupId>
        <artifactId>commons-lang</artifactId>
        <version>${commons.lang.version}</version>
    </dependency>

Mavenリポジトリからjarファイルを取得
------------------------------------

Mavenでコンパイルを実行します。依存するjarファイルがローカルリポジトリに配置されます。

..  code-block:: sh

    mvn compile

Eclipseを使って開発している場合は、Eclipse用クラスパス定義ファイル(.classpath)を更新します。

..  code-block:: sh

    mvn eclipse:eclipse

Asausaの拡張ライブラリディレクトリへjarファイルを配置
-----------------------------------------------------

ローカルリポジトリに配置されたjarファイルを $ASAKUSA_HOME/ext/lib ディレクトリに配置します。

..  code-block:: sh

    cp $HOME/.m2/repository/commons-lang/commons-lang/2.6/commons-lang-2.6.jar $ASAKUSA_HOME/ext/lib

Asakusa Frameworkのバージョンアップ
===================================
開発環境のAsakusa Frameworkをバージョンする手順を示します。

なお、バージョンアップ内容によっては以下の他に追加の手順が必要となります。バージョン毎の固有の手順についてはRelease Note等を参照してください。

pom.xml上のバージョンを更新
---------------------------
pom.xmlの10行目にある「<asakusafw.version」の値を
更新したいバージョンに書き換えます。

..  code-block:: sh

    <asakusafw.version>0.2.1-RC1</asakusafw.version>

Asakusa Frameworkの再セットアップ
---------------------------------
Asakusa Frameworkの再セットアップを行うため、Mavenの以下のフェーズ（ゴール）を実行します。

..  code-block:: sh

    mvn assembly:single antrun:run compile

Eclipseを使って開発している場合は、Eclipse用クラスパス定義ファイル(.classpath)を更新します。

..  code-block:: sh

    mvn eclipse:eclipse

``build.properties`` ビルド定義ファイル
=======================================
アーキタイプから作成したプロジェクトの ``build.properties`` はプロジェクトのビルドや各種ツールの動作を設定します。設定項目について以下に説明します。

項目値が択一式の項目については、デフォルト値を **太字** で示しています。

---------------------

General Settings

  asakusa.database.enabled 
    ( **true** or false ) このプロパティをfalseにすると、Asakusa Frameworkの開発環境へのインストール( ``antrun:run`` )、及びモデル生成処理 ( ``generate-sources`` ) でデータベースに対する処理を行わなくなります。
    
    ThunderGateを使用せず、モデルの定義をDMDLのみで行う場合は、このオプションをfalseにするとデータベースを使用しない構成で開発を行うことが可能になります。

  asakusa.database.target
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

  asakusa.modelgen.output
    モデルジェネレータが生成するモデルクラス用Javaソースの出力ディレクトリを指定します。アーキタイプが提供するEclipseの設定情報と対応しているため、特に理由が無い限りはデフォルト値を変更しないようにしてください。この値を変更する場合、合わせてpom.xmlの修正も必要となります。

  asakusa.dmdl.encoding
    DMDLスクリプトが使用する文字エンコーディングを指定します。

  asakusa.dmdl.dir
    DMDLスクリプトを配置するディレクトリを指定します。

---------------------

ThunderGate Settings

  asakusa.bulkloader.tables
    ``generate-sources`` フェーズ実行時に生成されるThunderGate管理テーブル用DDLスクリプト（後述の「asakusa.bulkloader.genddl」で指定したファイル）に含める対象テーブルを指定します。このプロパティにインポート、及びエクスポート対象テーブルのみを指定することで、余分な管理テーブルの生成を抑止することが出来ます。開発時にはデフォルト（コメントアウト）の状態で特に問題ありません。

  asakusa.bulkloader.genddl
    ``generate-sources`` フェーズ実行時に生成されるThunderGate管理テーブル用DDLスクリプトのファイルパスを指定します。

  asakusa.dmdl.fromddl.output
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
    ( true or **false** ) Asakusa Framework 0.1 仕様のテストデータ定義シートを出力するかを設定します（デフォルトは出力しない）。 このプロパティをtrueにすると、 ``generate-sources`` フェーズ実行時にテストデータ定義シートが ``target/excel_v01`` ディレクトリ配下に出力されるようになります。

  asakusa.testdriver.testdata.dir
    テストドライバの実行時に、テストドライバが参照するテストデータ定義シートの配置ディレクトリを指定します。
    
    このプロパティは、テストドライバAPIのうち、Asakusa Framework 0.1 から存在する ``*TestDriver`` というクラスの実行時のみ使用されます。Asakusa Framework 0.2 から追加された ``*Tester`` 系のテストドライバAPIは、この値を使用せず、テストドライバ実行時のクラスパスからテストデータ定義シートを参照するようになっています。

  asakusa.excelgen.tables
    Asakusa Framework 0.1 仕様のテストデータ定義シート生成ツールをMavenコマンドから実行 ( ``mvn exec:java -Dexec.mainClass=com.asakusafw.testtools.templategen.Main`` )した場合に、テストデータシート生成ツールが生成の対象とするテーブルをスペース区切りで指定します。
    
