================================
Asakusa Framework スタートガイド
================================

この文書では、Asakusa Frameworkをはじめて利用するユーザー向けに、Asakusa Frameworkを使ったバッチアプリケーションの開発、実行方法を簡単に説明します。

..  seealso::
    本書より詳しい入門向けドキュメントとして、 :basic-tutorial:`Asakusa Framework チュートリアル <index.html>` ではサンプルアプリケーションを作成しながらフレームワークの基本的な使い方や開発の流れを説明しています。

.. _startguide-development-environment:

開発環境の準備
==============

前提環境
--------

Asakusa Frameworkを開発環境で利用するためには、開発環境に以下がセットアップされている必要があります。

..  list-table::
    :widths: 3 3 4
    :header-rows: 1

    * - ソフトウェア
      - バージョン
      - 備考
    * - JDK
      - JDK 7 または JDK 8
      - JREは利用不可
    * - Eclipse
      - 4.4.2 以上を推奨
      - Shafuを利用する場合に使用

..  attention::
    Asakusa Frameworkのバッチアプリケーションのコンパイルやテストの実行にはJDKが必要です。
    JREのみがインストールされている環境では利用できません。

環境変数の設定
--------------

Asakusa Frameworkを開発環境で利用するには、以下の環境変数が必要です。

..  list-table::
    :widths: 3 7
    :header-rows: 1

    * - 変数名
      - 説明
    * - ``ASAKUSA_HOME``
      - Asakusa Frameworkのインストールディレクトリ

Asakusa Frameworkの開発支援ツール
---------------------------------

Asakusa Frameworkを使ったバッチアプリケーションの開発は、基本的にJavaを利用したアプリケーションの開発になりますが、いくつかの点でAsakusa Framework固有の環境設定やビルドに関する知識が必要になります。

そのため、Asakusa Frameworkではアプリケーションの開発をサポートするいくつかのツールを提供しています。

Shafu
~~~~~

Shafu (車夫) は、 Asakusa Framework のバッチアプリケーション開発をサポートするEclipseプラグインです。

* :jinrikisha:`Shafu - Asakusa Gradle Plug-in Helper for Eclipse - <shafu.html>`

Asakusa Frameworkではアプリケーションのビルドに `Gradle <http://www.gradle.org/>`_ というビルドシステムを利用しますが、
Shafuを使うことで、Gradleに関する詳細な知識がなくてもAsakusa Frameworkの基本的な開発作業が行えるようになります。
また、コマンドライン上でのGradleの操作が不要となり、Eclipse上でアプリケーション開発に必要なほとんどの作業を行うことができるようになります。

なお、このスタートガイドではコマンドライン上でGradleを利用する手順と、Shafuを利用する手順の両方を記載します。

Jinrikisha
~~~~~~~~~~

Jinrikisha (人力車) は、 Asakusa Framework の開発環境を手軽に構築するためのインストーラパッケージです。

* :jinrikisha:`Jinrikisha - Asakusa Framework Starter Package - <index.html>`

Jinrikishaは新規のLinux環境上にAsakusa Framework専用の開発環境を簡単にセットアップすることを主な目的としています。
JDKやEclipseといった基本的なツールのセットアップに加えて、ローカル環境でHadoopやSparkを利用したバッチアプリケーションを実行できるようにするための設定も行うため、
Hadoopクラスターなどの運用環境にデプロイする前にバッチアプリケーションの疎通確認や検証を行うといった用途に適しています。

このスタートガイドではJinrikishaの利用は前提ではありませんが、開発したアプリケーションをローカルで実行したい場合はJinrikishaを使って環境をセットアップすると便利です。

Shafuのセットアップ
===================

Shafuを利用する場合、以下の手順例を参考にしてEclipse上にインストールしてください。

..  seealso::
    Shafuについては、 :jinrikisha:`Shafuのドキュメント <shafu.html>` も参考にしてください。

Shafuのインストール
-------------------

ShafuはEclipseプラグインとして公開されており、一般的なEclipseプラグインと同様にインストールを行います。

以下、Eclipse上でのインストール手順例です。

#. Eclipseのメニューバーから :menuselection:`Help --> Install New Software...` を選択します。
#. :guilabel:`Install` ダイアログで :guilabel:`Work with:` の項目右の :guilabel:`Add` ボタンを押下します。
#. :guilabel:`Add Repository` ダイアログに以下の値を入力します。

   * :guilabel:`Name:` 任意の名前(例: ``Jinrikisha`` )
   * :guilabel:`Location:` ``http://www.asakusafw.com/eclipse/jinrikisha/updates/``
#. :guilabel:`Install` ダイアログに表示された :guilabel:`Jinrikisha (人力車)` カテゴリを展開して :guilabel:`Asakusa Gradle プラグインサポート` を選択し、 :guilabel:`Next >` ボタンを押下します。
#. 以降、画面の指示に従いインストールを進めます。Eclipseの再起動を促されたら :guilabel:`Yes` を選択します。
#. Eclipseの再起動が完了したら :guilabel:`Java` パースペクティブを選択し、 メニューバーから :menuselection:`Window --> Perspective --> Reset Perspective..` を選択して :guilabel:`Java` パースペクティブを初期化します。

..  attention::
    Shafuのインストール後にJavaパースペクティブの初期化を行わないと、Shafuのメニューが表示されないことがあります。

EclipseのJDK登録
----------------

Eclipse経由で実行するJavaにJREが設定されている場合、これをJDKに変更します。

#. Eclipseの設定画面から :menuselection:`Java --> Installed JREs` を選択します。
#. :guilabel:`Installed JREs` ダイアログにJDK以外のJava定義が表示されている場合 (例えば ``jre8`` のような項目が表示されている場合)、これら項目を削除します。 削除する項目を選択して、 :guilabel:`Remove` ボタンを押下します。
#. JDKを追加します。 :guilabel:`Installed JREs` ダイアログで :guilabel:`Add` ボタンを押下します。
#. :guilabel:`JRE Type` ダイアログで :guilabel:`Standard VM` を選択し、 :guilabel:`Next >` ボタンを押下します。
#. :guilabel:`JRE Definition` ダイアログで :guilabel:`JRE home:` の項目右の :guilabel:`Directory...` ボタンを押下し、JDKのフォルダを指定します。
#. :guilabel:`JRE Definition` ダイアログの各項目にインストールしたJDKの情報が設定されたことを確認して :guilabel:`Finish` ボタンを押下します。
#. :guilabel:`Installed JREs` ダイアログに追加したJDKの項目が表示されるので、その項目の :guilabel:`Name` 欄に表示されているチェックボックスを :guilabel:`ON` にします。JDKの項目が ``jdk1.8.0_XX (default)`` のような表示になれば設定完了です。

..  attention::
    Asakusa Frameworkのバッチアプリケーションのコンパイルやテストの実行にはJDKを使用する必要があります。
    JREを使用することはできないため、必ず上記の設定を確認してください。

環境変数の確認
--------------

Eclipse上で環境変数 ``ASAKUSA_HOME`` が有効になっていることを確認します。

#. Eclipseの設定画面から :menuselection:`Jinrikisha (人力車) --> Asakusa Framework` を選択します。
#. :guilabel:`フレームワークのインストール先 (ASAKUSA_HOME)` に環境変数 ``ASAKUSA_HOME`` で設定したフォルダが表示されていることを確認します。

正しく表示されていない場合、環境の設定を確認してください。

アプリケーション開発の準備
==========================

アプリケーションプロジェクトの作成
----------------------------------

バッチアプリケーションの開発をはじめるには、まずAsakusa Frameworkアプリケーション開発用のプロジェクトを作成します。

アプリケーションプロジェクトを作成するには、オンライン上に公開されているAsakusa Frameworkのプロジェクトテンプレートを利用すると便利です。
このプロジェクトテンプレートにはプロジェクトで利用するビルドツール(Gradle)の設定や実行環境、および開発環境で利用する設定ファイルなどが含まれます。

コマンドライン上から作成する場合、以下のURLに公開されているプロジェクトテンプレートのアーカイブを展開します。

* `asakusa-mapreduce-template-0.8.1.tar.gz <http://www.asakusafw.com/download/gradle-plugin/asakusa-mapreduce-template-0.8.1.tar.gz>`_

Asakusa on Sparkを利用する場合のプロジェクトテンプレートは、 :asakusa-on-spark:`Asakusa on Spark ユーザーガイド <user-guide.html>` に記載のリンクからダウンロードします。

..  code-block:: sh

    cd <work-dir>
    curl -OL http://www.asakusafw.com/download/gradle-plugin/asakusa-mapreduce-template-0.8.1.tar.gz
    tar xf asakusa-mapreduce-template-0.8.1.tar.gz
    mv asakusa-mapreduce-template my-batchapp
    cd my-batchapp

Shafuを導入した開発環境では、オンライン上に公開されているAsakusa Frameworkのプロジェクトテンプレートカタログを利用して、テンプレートプロジェクトをベースに新規プロジェクトを作成することができます。

#. Javaパースペクティブ上のメニューバーから :menuselection:`File --> New --> Gradleプロジェクトをテンプレートから生成` を選択します。
#. :guilabel:`新規プロジェクト情報` ダイアログで、プロジェクト名などを入力します。
#. :guilabel:`テンプレートからプロジェクトを作成` ダイアログで :guilabel:`URLを指定してプロジェクトテンプレートをダウンロードする` が選択状態になっていることを確認して、画面右の :guilabel:`選択` ボタンを押下します。
#. :guilabel:`プロジェクトテンプレート` ダイアログにオンラインに公開されている、利用可能なプロジェクトテンプレートの一覧が表示されます。

   *  MapReduce向けのテンプレートを利用する場合は、 :guilabel:`Asakusa Project Template <MapReduce> - 0.8.1` を選択します。
   *  Spark向けのテンプレートを利用する場合は、 :guilabel:`Asakusa Project Template <Spark> - <version>` を選択します [#]_ 。
#. :guilabel:`Finish` ボタンを押すと選択したプロジェクトテンプレートを読み込み、Eclipseプロジェクトとして新規プロジェクトが作成されます。

..  [#] Asakusa on Sparkで利用可能なバージョンは、 :asakusa-on-spark:`Asakusa on Spark <index.html>` のドキュメントを確認してください。

Asakusa Frameworkのインストール
-------------------------------

次に、開発環境用のAsakusa Frameworkをインストールします。これはアプリケーションのテスト時などに利用します。

コマンドライン上からインストールする場合、Gradleの :program:`installAsakusafw` タスクを実行します。
プロジェクト上でタスクを実行するには、以下のように :program:`gradlew` コマンドにタスク名を指定して実行します。

..  code-block:: sh

    ./gradlew installAsakusafw

Shafuを導入した開発環境では、EclipseのメニューからAsakusa Frameworkのインストールを実行します。

#. Javaパースペクティブ上のプロジェクトを選択してコンテキストメニュー(右クリックなどで表示されるメニュー)を表示します。
#. コンテキストメニューから :menuselection:`Jinrikisha (人力車) --> Asakusa開発環境の構成 --> Asakusa Frameworkのインストール` を選択します。

インストールが成功した場合、コンソールに以下のように表示され、環境変数 ``ASAKUSA_HOME`` で指定したフォルダ配下にAsakusa Frameworkがインストールされます。

..  code-block:: none

    ...
    :installAsakusafw
    Asakusa Framework is successfully installed: /home/asakusa/asakusa

    BUILD SUCCESSFUL

    Total time: 4.352 secs

Next Step
=========

ここまでの手順で、Asakusa Framework上でバッチアプリケーションの開発を行う準備が整いました。

次のステップとして、 :doc:`next-step` では実際にアプリケーションの開発を行うための、Asakusa Frameworkを使ったアプリケーション開発の流れを紹介しています。

また :basic-tutorial:`Asakusa Framework チュートリアル <index.html>` では、サンプルアプリケーションを作成しながらフレームワークの基本的な使い方や開発の流れを説明しています。

このスタートガイドの以降の説明では、公開されているサンプルアプリケーションを使ってバッチアプリケーションを実行する手順を紹介しています。

.. _startguide-running-example:

サンプルアプリケーションの実行
==============================

ここでは、Asakusa Frameworkのサンプルアプリケーションを使って、実行環境上でバッチアプリケーションを実行する手順を簡単に説明します。

サンプルアプリケーションの概要
------------------------------

Asakusa Frameworkの `サンプルプログラム集 (GitHub)`_ ではいくつかのサンプルアプリケーションが公開されています。
その中から、ここでは ``examle-directio-csv`` ディレクトリ配下に含まれるサンプルアプリケーション「カテゴリー別売上金額集計バッチ」を使います。

カテゴリー別売上金額集計バッチは、売上トランザクションデータと、商品マスタ、店舗マスタを入力として、エラーチェックを行った後、売上データを商品マスタのカテゴリ毎に集計するアプリケーションです。

バッチアプリケーションの入力データ取得と出力データ生成には、Asakusa Frameworkの「Direct I/O」と呼ばれるコンポーネントを利用しています。
Direct I/Oを利用して、Hadoopファイルシステム上のCSVファイルに対して入出力を行います。

..  _`サンプルプログラム集 (GitHub)`: http://github.com/asakusafw/asakusafw-examples

サンプルアプリケーションプロジェクトの作成
------------------------------------------

`サンプルプログラム集 (GitHub)`_ に公開されているプロジェクトを開発環境に取り込みます。

コマンドライン上でプロジェクトを作成する場合、GitHub上に公開されているサンプルアプリケーションのアーカイブを展開します。

..  code-block:: sh

    cd <work-dir>
    curl -OL https://github.com/asakusafw/asakusafw-examples/archive/0.8.1.tar.gz
    tar xf 0.8.1.tar.gz
    cd asakusafw-examples-0.8.1/example-basic-spark

Shafuを導入した開発環境では、オンライン上に公開されているAsakusa Frameworkのプロジェクトテンプレートカタログを利用して、サンプルアプリケーションのプロジェクトをEclipse上に取り込みます。

#. Javaパースペクティブ上のメニューバーから :menuselection:`File --> New --> Gradleプロジェクトをテンプレートから生成` を選択します。
#. :guilabel:`新規プロジェクト情報` ダイアログで、プロジェクト名などを入力します。
#. :guilabel:`テンプレートからプロジェクトを作成` ダイアログで :guilabel:`URLを指定してプロジェクトテンプレートをダウンロードする` が選択状態になっていることを確認して、画面右の :guilabel:`選択` ボタンを押下します。
#. :guilabel:`プロジェクトテンプレート` ダイアログにオンラインに公開されている、利用可能なプロジェクトテンプレートの一覧が表示されます。ここでは :guilabel:`Asakusa Example Projects - 0.8.1` を選択します。
#. :guilabel:`Finish` ボタンを押すと選択したプロジェクトテンプレートを読み込み、Eclipseプロジェクトとして新規プロジェクトが作成されます。
#. :guilabel:`テンプレートからプロジェクトを作成` ダイアログで ``example-basic-spark`` を選択して :guilabel:`OK` ボタンを押下します。

サンプルアプリケーションのビルド
--------------------------------

Asakusa Frameworkの開発環境で作成したバッチアプリケーションを運用環境（Hadoopクラスターなど）で実行するには、コンパイル済みのバッチアプリケーションとAsakusa Framework本体の実行モジュールをあわせて運用環境にデプロイします。
そのためにまず、開発環境上でデプロイに必要なモジュールを全て含めた「デプロイメントアーカイブ」と呼ばれるパッケージファイルを生成します。

コマンドライン上でデプロイメントアーカイブを生成するには、Gradleの :program:`assemble` タスクを実行します。

..  code-block:: sh

    ./gradlew assemble

Shafuを導入した開発環境では、コンテキストメニューから :menuselection:`Jinrikisha (人力車) --> Asakusaデプロイメントアーカイブを生成` を選択します。

このコマンドの実行によって、アプリケーションプロジェクトに対して以下の処理が実行されます。

* データモデル定義DSL(DMDL)から、データモデルクラスを生成
* Asakusa DSLとデータモデル定義DSLから、HadoopやSparkなどの各処理系で実行可能なプログラム群を生成
* アプリケーションを実行環境に配置するためのデプロイメントアーカイブファイルを生成

デプロイメントアーカイブファイルはプロジェクトの :file:`build` ディレクトリ配下に ``asakusafw-0.8.1.tar.gz`` というファイル名で生成されます。

.. _introduction-start-guide-deploy-app:

サンプルアプリケーションのデプロイ
----------------------------------

`サンプルアプリケーションのビルド`_ で作成したデプロイメントアーカイブファイルを運用環境にデプロイします。

通常、デプロイ対象となるノードはHadoopクライアントモジュールがインストールされているHadoopクラスターのノードを選択します。

以降の手順を行う前に、デプロイメントアーカイブファイル ``asakusafw-0.8.1.tar.gz`` をデプロイ対象となるノードに転送しておいてください。

環境変数の設定
~~~~~~~~~~~~~~

運用環境上でAsakusa Frameworkを配置しバッチアプリケーションを実行するためのOSユーザーに対して、以下の環境変数を設定します。

* ``JAVA_HOME``: Javaのインストールパス
* ``HADOOP_CMD``: :program:`hadoop` コマンドのパス
* ``SPARK_CMD``: :program:`spark-submit` コマンドのパス ( :asakusa-on-spark:`Asakusa on Spark <index.html>` を利用する場合 )
* ``ASAKUSA_HOME``: Asakusa Frameworkのインストールパス

以下は環境変数の設定例です。

..  code-block:: sh

    export JAVA_HOME=/usr/lib/jvm/java-8-oracle
    export HADOOP_CMD=/usr/lib/hadoop/bin/hadoop
    export SPARK_CMD=/opt/spark/bin/spark-submit
    export ASAKUSA_HOME=$HOME/asakusa

デプロイメントアーカイブの展開
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

`サンプルアプリケーションのビルド`_ で作成したデプロイメントアーカイブファイル ``asakusafw-0.8.1.tar.gz`` を配置し、 ``$ASAKUSA_HOME`` 配下にデプロイメントアーカイブを展開します。
展開後、 ``$ASAKUSA_HOME`` 配下の :file:`*.sh` に実行権限を追加します。

..  code-block:: sh

    mkdir -p "$ASAKUSA_HOME"
    cd "$ASAKUSA_HOME"
    tar -xzf /path/to/asakusafw-0.8.1.tar.gz
    find "$ASAKUSA_HOME" -name "*.sh" | xargs chmod u+x

..  hint::
    試用や疏通確認などの場合は、Hadoopクラスターの代わりにJinrikishaなどを使って構築した開発環境（ローカル）を利用することもできます。
    詳しくは :jinrikisha:`Jinrikishaのドキュメント <index.html>` を参照してください。

サンプルデータの配置
--------------------

サンプルアプリケーションの構成では、 :file:`$ASAKUSA_HOME/example-dataset` ディレクトリ以下にテスト用の入力データが用意されています。
これらのファイルをHadoopファイルシステム上のDirect I/Oの入出力ディレクトリ(デフォルトの設定では :file:`target/testing/directio` 配下) にコピーします。

..  warning::
    Direct I/Oの出力ディレクトリはバッチアプリケーション実行時に初期化されます。
    既存のディレクトリを利用する場合、このパスに重要なデータがないことを実行前に確認してください。

以下は、サンプルデータをHadoopファイルシステムに配置する手順の例です。

..  code-block:: sh

    hadoop fs -mkdir -p target/testing/directio
    hadoop fs -put $ASAKUSA_HOME/example-dataset/master target/testing/directio/master
    hadoop fs -put $ASAKUSA_HOME/example-dataset/sales target/testing/directio/sales

.. _introduction-start-guide-run-app:

サンプルアプリケーションの実行
------------------------------

サンプルアプリケーションを実行します。

Asakusa Frameworkでは、バッチアプリケーションを実行するためのコマンドプログラムとして「YAESS」というツールが提供されています。
バッチアプリケーションを実行するには、:program:`$ASAKUSA_HOME/yaess/bin/yaess-batch.sh` に実行するバッチのバッチIDとバッチ引数を指定します。

サンプルアプリケーション「カテゴリー別売上金額集計バッチ」は「 ``example.summarizeSales`` 」というバッチIDを持っています。
また、このバッチは引数に処理対象の売上日時( ``date`` )を指定し、この値に基づいて処理対象CSVファイルを特定します。

サンプルアプリケーションの標準の構成では、バッチアプリケーションはHadoop(MapReduce)を利用する構成と、Spark を利用する構成の2つの構成が配置されています。

MapReduceを利用するバッチアプリケーションは、そのままバッチIDとバッチ引数を指定して、以下のようにバッチアプリケーションを実行します。

..  code-block:: sh

    $ASAKUSA_HOME/yaess/bin/yaess-batch.sh example.summarizeSales -A date=2011-04-01

Sparkを利用するバッチアプリケーションは、Spark向けのバッチアプリケーションはバッチIDの接頭辞に spark. を付与して実行します。

..  code-block:: sh

    $ASAKUSA_HOME/yaess/bin/yaess-batch.sh spark.example.summarizeSales -A date=2011-04-01

バッチの実行が成功すると、コマンドの標準出力の最終行に ``Finished: SUCCESS`` と出力されます。

..  code-block:: none

    ...
    2016/03/17 03:56:24 INFO  [YS-CORE-I01999] Finishing batch "spark.example.summarizeSales": batchId=spark.example.summarizeSales, elapsed=51,738ms
    2016/03/17 03:56:24 INFO  [YS-BOOTSTRAP-I00999] Exiting YAESS: code=0, elapsed=51,790ms
    Finished: SUCCESS

サンプルアプリケーション実行結果の確認
--------------------------------------

Asakusa FrameworkはDirect I/Oの入出力ディレクトリやファイルの一覧をリストアップするコマンド :program:`$ASAKUSA_HOME/directio/bin/list-file.sh` を提供しています。
このコマンドを利用して、サンプルアプリケーションの出力結果を確認します。

ここでは、サンプルアプリケーションの出力結果ディレクトリ :file:`result` 以下のすべてのファイルを、サブディレクトリ含めてリストするようコマンドを実行してみます。

..  code-block:: sh

    $ASAKUSA_HOME/directio/bin/list-file.sh result "**/*"
.. ***

上記のコマンドを実行すると、以下のような結果が表示されます。

..  code-block:: sh

    Starting List Direct I/O Files:
    ...
    hdfs://<host:port>/user/asakusa/target/testing/directio/result/category
    hdfs://<host:port>/user/asakusa/target/testing/directio/result/error
    hdfs://<host:port>/user/asakusa/target/testing/directio/result/category/result.csv
    hdfs://<host:port>/user/asakusa/target/testing/directio/result/error/2011-04-01.csv
.. ***

出力ファイルの一覧に対して、 :program:`hadoop fs -text` コマンドを利用してファイル内容を確認します。

以下は 売上データの集計ファイル :file:`category/result.csv` を表示する例です。

..  code-block:: sh

    hadoop fs -text hdfs://<host:port>/user/asakusa/target/testing/directio/result/category/result.csv

指定したファイルの内容が表示されます。
売上データが商品マスタのカテゴリコード単位で集計され、売上合計の降順で整列されたCSVが出力されています。

..  code-block:: none
    :caption: category/result.csv
    :name: category/result.csv-introduction-start-guide-1

    カテゴリコード,販売数量,売上合計
    1600,28,5400
    1300,12,1596
    1401,15,1470

また、このバッチでは処理の中で不正なレコードをチェックして、該当したエラーレコードをまとめてファイル :file:`error/2011-04-01.csv` に出力します。

..  code-block:: sh

    hadoop fs -text hdfs://<host:port>/user/asakusa/target/testing/directio/result/error/2011-04-01.csv

エラーチェックに該当したレコードの一覧は以下のように出力されます。

..  code-block:: none
    :caption: error/2011-04-01.csv
    :name: error/2011-04-01.csv-introduction-start-guide-1

    ファイル名,日時,店舗コード,商品コード,メッセージ
    hdfs://<host:port>/user/asakusa/target/testing/directio/sales/2011-04-01.csv,2011-04-01 19:00:00,9999,4922010001000,店舗不明
    hdfs://<host:port>/user/asakusa/target/testing/directio/sales/2011-04-01.csv,2011-04-01 10:00:00,0001,9999999999999,商品不明
    hdfs://<host:port>/user/asakusa/target/testing/directio/sales/2011-04-01.csv,1990-01-01 10:40:00,0001,4922010001000,商品不明
