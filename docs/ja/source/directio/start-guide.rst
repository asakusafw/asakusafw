=========================
Direct I/O スタートガイド
=========================

この文書では、Direct I/Oの簡単な利用方法について紹介します。

..  seealso::
    Direct I/Oの詳しい利用方法については :doc:`user-guide` を参照してください。

アプリケーションの開発準備
==========================

アプリケーションの開発環境の構築手順や利用方法については、 :doc:`../introduction/start-guide` を参照してください。
この文書では開発環境の構築が完了しているものとして説明を行います。

データソースの設定
==================

Direct I/Oの機構を利用するには、入出力の仲介を行う「データソース」の設定が必要です。
主に以下のような設定を行います。

* データソースの実装
* データソースを配置する論理パス
* データソースが実際に利用するファイルシステム上のパス

これらの設定は、 ``$ASAKUSA_HOME`` で指定したディレクトリ以下の :file:`core/conf/asakusa-resources.xml` (以下「設定ファイル」)内に、Hadoopの設定ファイルと同様の形式でそれぞれ記述していきます。

データソースのマッピング
------------------------

``$ASAKUSA_HOME/core/conf/asakusa-resources.xml`` にはデフォルトの設定ファイルが作成されています。これは以下のような内容になっています。

..  code-block:: xml
    :caption: asakusa-resources.xml
    :name: asakusa-resources.xml-directio-start-guide-1

    <?xml version="1.0" encoding="UTF-8"?>
    <?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
    <configuration>
    <!--
    Default Implementations (for Development)
    -->
        <property>
            <name>com.asakusafw.runtime.core.Report.Delegate</name>
            <value>com.asakusafw.runtime.core.Report$Default</value>
        </property>

        <property>
            <name>com.asakusafw.directio.root</name>
            <value>com.asakusafw.runtime.directio.hadoop.HadoopDataSource</value>
        </property>
        <property>
            <name>com.asakusafw.directio.root.path</name>
            <value>/</value>
        </property>
        <property>
            <name>com.asakusafw.directio.root.fs.path</name>
            <value>target/testing/directio</value>
        </property>
    </configuration>

このうち、Direct I/Oに関係する項目 [#]_ は以下のとおりです。

``com.asakusafw.directio.root``
  データソースのJavaでの実装クラス名です。

  Direct I/Oでは、それぞれのデータソースを識別するための識別子を ``com.asakusafw.directio.<DSID>`` の形式で指定します。
  デフォルトの設定では ``root`` というIDのデータソースが設定されていることになります。

  Hadoopのファイルシステムを利用するには ``com.asakusafw.runtime.directio.hadoop.HadoopDataSource`` と指定します。

``com.asakusafw.directio.root.path``
  このデータソースを配置する「論理パス」を表します。

  論理パスは、 ``com.asakusafw.directio.<DSID>.path`` の形式で指定します。
  DSLからはこの論理パスでデータソースを指定します。

  論理パスはUnixのディレクトリのような構造を取り、 ``alpha/beta/gamma`` のように名前をスラッシュ ( ``/`` ) で区切って指定します。

  特別な論理パスとして、ルートパスは ``/`` 一文字で指定します。

  ..  tip::
    デフォルトの設定ではデータソース識別子 ``root`` にルートパスを表す論理パス ``/`` が割り当てられていますが、データソース識別子 ``root`` は特別な値ではなく、任意の値に変更することが出来ます。

``com.asakusafw.directio.root.fs.path``
  データソースが実際に利用するファイルシステム上のパス「ファイルシステムパス」を表します。

  ファイルシステムパスは、 ``com.asakusafw.directio.<DSID>.fs.path`` の形式で指定します。
  Direct I/Oを利用したアプリケーションは、ここに指定されたファイルシステムパス以下のファイルを利用します。

  ファイルシステムパスには :file:`target/testing/directio` のように相対パスを使用したり、 ``hdfs://localhost:8020/user/asakusa`` のように完全URIを使用することができます。

  相対パスを指定した場合、Hadoopのデフォルトファイルシステムのワーキングディレクトリからの相対パスが利用されます。

  ..  hint::
    Hadoopのデフォルトファイルシステムとは、Hadoopの設定ファイル :file:`core-site.xml` 内の ``fs.defaultFS`` (``fs.default.name``) に指定したファイルシステムです。また、デフォルトのワーキングディレクトリは、多くのHadoopディストリビューションではアプリケーション実行ユーザーのホームディレクトリです。

  ..  seealso::
    ファイルシステムパスの形式について、詳しくは :doc:`user-guide` - :ref:`directio-filesystem-path-format` を参照してください。

  ..  warning::
    ファイルシステムパス以下はテストドライバー実行時に削除されます。
    特にスタンドアロンモードのHadoopを利用時にデフォルトの設定のような相対パスを指定した場合、ホームディレクトリを起点としたパスと解釈されるため注意が必要です。

    例えばホームディレクトリが :file:`/home/asakusa` であった場合でデフォルト設定の相対パスを利用する場合、テスト実行の都度 :file:`/home/asakusa/target/testing/directio` ディレクトリ以下が削除されることになります。

..  [#] Direct I/Oに無関係の項目は、 :doc:`実行時プラグイン <../administration/deployment-runtime-plugins>` の設定です。

サンプルアプリケーションの実行
==============================

:doc:`../introduction/start-guide` と同様の手順でサンプルアプリケーションの実行を行いますが、ここでは Direct I/Oの機能説明を加えつつサンプルアプリケーションの実行手順を説明します。

データソースの設定
------------------

サンプルアプリケーションは、DSLから以下の論理パスを利用しています。

..  list-table:: サンプルアプリケーションが利用するパス
    :widths: 3 7
    :header-rows: 1

    * - 論理パス
      - 概要
    * - ``master``
      - マスタデータを配置するパス
    * - ``sales``
      - 売上データを配置するパス
    * - ``result``
      - 計算結果を出力するパス

上記の論理パスに対応するデータソースをそれぞれ配置するか、またはデフォルト設定のようにルート ( ``/`` ) に対してデータソースを配置してください。

以降の説明ではデータソースの設定には `データソースのマッピング`_ で説明したデフォルト設定をそのまま利用します。
この場合、データソースはルートパス ``/`` を使用し、この論理パスに対応するファイルシステムパスは :file:`target/testing/directio` が使用されます。

サンプルアプリケーションのビルド
--------------------------------

データソースの設定が完了したら、サンプルアプリケーションのビルドを行います。

..  warning::
    先述した通り、ビルド時に実行されるテストの実行中に、設定したデータソースのファイルシステムパスの内容がクリアされます。
    対応付けたファイルシステムパスをもう一度確認し、重要なデータがないようにしてください。

コマンドラインコンソールでアプリケーションプロジェクトのディレクトリに移動し、以下のコマンドを実行してください。

..  code-block:: none

    cd ~/workspace/example-app
    ./gradlew build

サンプルデータの配置
--------------------

サンプルアプリケーションプロジェクトには :file:`src/test/example-dataset` 以下にサンプルの入力ファイルが配置されています。
これは以下のような構成になっています。

..  list-table:: サンプルアプリケーションが利用するパス
    :widths: 3 2
    :header-rows: 1

    * - サンプルデータの位置
      - 対応する論理パス
    * - :file:`src/test/example-dataset/master`
      - ``master``
    * - :file:`src/test/example-dataset/sales`
      - ``sales``

:program:`hadoop fs -put` コマンドを利用して、設定したファイルシステムパス上にサンプルデータを配置してください。
以下はデフォルト設定を利用した場合のコマンド例です。

..  code-block:: sh

    # スタンドアロンモードに対応するため、ホームディレクトリに移動しておく
    cd ~
    # ファイルシステムパス上のデータをクリアしておく
    hadoop fs -rm -r target/testing/directio/*
    # サンプルデータを配置する
    hadoop fs -put <サンプルアプリケーションプロジェクトのパス>/src/test/example-dataset/master target/testing/directio/master
    hadoop fs -put <サンプルアプリケーションプロジェクトのパス>/src/test/example-dataset/sales target/testing/directio/sales

..  attention::
    直前に `サンプルアプリケーションのビルド`_ を実行している場合、ファイルシステムパス上にはテスト時に利用したデータが残っていることがあるため、データを配置する際には上の例のように、ファイルシステムパス上のデータをクリアしてから配置してください。

アプリケーションの実行
----------------------

アプリケーション実行の手順は :doc:`../introduction/start-guide` と同様です。

ここではローカルでアプリケーションを実行するためのコマンド例のみを示します。
詳しくは :doc:`../introduction/start-guide` の :ref:`introduction-start-guide-deploy-app` や :ref:`introduction-start-guide-run-app` を参考にしてください。

..  code-block:: sh

    cd <サンプルアプリケーションプロジェクトのパス>
    cp build/*batchapps*.jar $ASAKUSA_HOME/batchapps
    cd $ASAKUSA_HOME/batchapps
    jar xf *batchapps*.jar

    $ASAKUSA_HOME/yaess/bin/yaess-batch.sh example.summarizeSales -A date=2011-04-01

アプリケーション実行結果の確認
------------------------------

Direct I/Oでは論理パスに配置したデータソース内のファイルやディレクトリ一覧をリストアップするコマンド :program:`$ASAKUSA_HOME/directio/bin/list-file.sh` を提供しています。
このコマンドを利用して、サンプルアプリケーションの出力結果を確認します。

:program:`list-file.sh` は 第一引数にリストアップの対象とするベースパス、第二引数にベースパスからの相対パスや :ref:`directio-file-name-pattern` を指定します。
ここでは、論理パス ``result`` 配下のすべてのファイルをサブディレクトリ含めてリストするようコマンドを実行してみます。

..  code-block:: sh

    $ASAKUSA_HOME/directio/bin/list-file.sh result "**/*"
.. ***

上記のコマンドを実行した場合、サンプルデータでは以下のような結果が表示されます。

..  code-block:: sh

    Starting List Direct I/O Files:
    ...
    file:/home/asakusa/target/testing/directio/result/category
    file:/home/asakusa/target/testing/directio/result/error
    file:/home/asakusa/target/testing/directio/result/category/result.csv
    file:/home/asakusa/target/testing/directio/result/error/2011-04-01.csv

デフォルト設定の場合、論理パス ``result`` に対応するデータソースはルートパス ``/`` に対応するデータソース ``root`` が使用されます。
また、データソース ``root`` に対応するファイルシステムパスは相対パス :file:`target/testing/directio` が使用されます。

上記はスタンドアロンモード上のHadoop対して実行しているため、Hadoopのワーキングディレクトリであるユーザーのホームディレクトリ ( 上記の例では :file:`/home/asakusa` )配下の相対パスに結果が出力されています。

:program:`hadoop fs -text` コマンドを利用して :program:`list-file.sh` が出力したファイルシステムパスのファイル内容を確認します。

..  code-block:: sh

    hadoop fs -text file:/home/asakusa/target/testing/directio/result/category/result.csv

上記のコマンドを実行した場合、サンプルデータでは以下のような結果が表示されます。

..  code-block:: none
    :caption: category/result.csv
    :name: category/result.csv-directio-start-guide-1

    カテゴリコード,販売数量,売上合計
    1600,28,5400
    1300,12,1596
    1401,15,1470

このように、売上合計の降順で整列されたCSVになっています。

..  attention::
    Direct I/Oの出力は論理パス ``result`` 上に行われます。
    つまり、出力データの実体は 論理パス ``result`` に配置したデータソースが実際に利用するファイルシステム上に出力されます。

    Direct I/Oのデフォルト設定では、データソースが実際に利用するファイルシステムはHadoopファイルシステムです。
    Hadoopでは設定により、Hadoopファイルシステムの実体が変わることに注意してください。
    例えば、Hadoopがスタンドアロンモードの場合は、Hadoopファイルシステムはローカルファイルを使用し、Hadoopが分散モードの場合はHDFSを使用するといったケースが多いでしょう。

    つまり開発環境でHadoopをスタンドアロンモードで使用する場合は通常Direct I/Oの出力はローカルファイルシステム上に出力されますが、WindGate/CSVと異なり設定次第ではローカルファイルシステム以外にも出力されることがあることに注意してください。

アプリケーションの開発
======================

以降ではアプリケーションの開発における、Direct I/O特有の部分について紹介します。

データモデルクラスの生成
------------------------

Direct I/OではモデルをDMDLで記述します。
DMDLの記述方法については「 :doc:`../dmdl/start-guide` 」などを参考にしてください。

DMDLスクリプトはプロジェクトの :file:`src/main/dmdl` ディレクトリ以下に配置し、スクリプトのファイル名には :file:`.dmdl` の拡張子を付けて保存します。

データモデルクラスを作成するには、データモデルの定義情報を記述後にGradleの :program:`compileDMDL` タスクを実行します。

CSV入出力への対応
~~~~~~~~~~~~~~~~~

データモデルの定義情報を作成したら、CSVの入出力に対応させたいデータモデルに対して、 ``@directio.csv`` という属性を指定します。
この属性が指定されたデータモデルは、宣言されたプロパティと同じ順序のフィールドを持つCSVファイルの入出力に対応します。

この属性は、データモデルの宣言の直前に指定します。
以下は記述例です。

..  code-block:: none

    @directio.csv
    example_model = {
        // ... プロパティの定義
    };

この状態でデータモデルを作成すると、データモデルのほかに以下の3つのクラスが作成されます。

#. ``<パッケージ名>.csv.<データモデル名>CsvFormat``
#. ``<パッケージ名>.csv.Abstract<データモデル名>CsvInputDescription``
#. ``<パッケージ名>.csv.Abstract<データモデル名>CsvOutputDescription``

..  seealso::
    CSV入出力に関するより細かな設定については :doc:`csv-format` を参照してください。

Asakusa DSLの記述
-----------------

Direct I/Oを利用する場合でも、Asakusa DSLの基本的な記述方法は同様です。
Direct I/O特有の部分は、以下に示す `ファイルシステム上のCSVファイルを入力に利用する`_ と `ファイルシステム上にCSVファイルを出力する`_ 部分のみです。

それ以外の部分については、 :doc:`../dsl/start-guide` を参照してください。

ファイルシステム上のCSVファイルを入力に利用する
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

開発環境のCSVファイルをインポートしてHadoopの処理を行う場合、 `CSV入出力への対応`_ で生成した ``<パッケージ名>.csv.Abstract<データモデル名>CsvInputDescription`` クラスのサブクラスを作成して必要な情報を記述します。

このクラスでは、下記のメソッドをオーバーライドします。

``String getBasePath()``
  入力に利用する論理パスを戻り値に指定します。

  論理パスについては `データソースのマッピング`_ を参照してください。

``String getResourcePattern()``
  入力に利用するファイル名のパターンを戻り値に指定します。
  ``getBasePath()`` で指定したパスを起点に、このパターンの名前を持つファイルを検索します。

  パターンには ``*`` (ワイルドカード) や ``{alpha|beta|gamma}`` (選択)などを利用できます。
  パターンの完全なドキュメントについては :doc:`user-guide` を参照してください。

以下は ``Document`` というデータモデルを宣言した場合の実装例です。

..  code-block:: java

    public class DocumentFromCsv extends AbstractDocumentCsvInputDescription {

        @Override
        public String getBasePath() {
            return "input";
        }

        @Override
        public String getResourcePattern() {
            return "documents-*.csv";
        }
    }

ここで記述した内容は、ジョブフローの入力に対して、 ``@Import`` を利用して指定します。

ファイルシステム上にCSVファイルを出力する
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

ジョブフローの処理結果をCSVファイルにエクスポートする場合、 `CSV入出力への対応`_ で生成した ``<パッケージ名>.csv.Abstract<データモデル名>CsvOutputDescription`` クラスのサブクラスを作成して必要な情報を記述します。

このクラスでは、下記のメソッドをオーバーライドします。

``String getBasePath()``
  出力に利用する論理パスを戻り値に指定します。

  論理パスについては `データソースのマッピング`_ を参照してください。

``String getResourcePattern()``
  出力先のファイル名のパターンを戻り値に指定します。
  ``getBasePath()`` で指定したパスを起点に、このパターンの名前でファイルを作成します。

  パターンには ``{property_name}`` (プレースホルダ) や ``[0..100]`` (ランダムな値 [#]_ ) などを利用できます。
  ここに指定したプロパティの文字列表現がファイル名に埋め込まれます。
  プロパティ名はDMDLのプロパティ名と同様、すべて小文字で単語をアンダースコア ( ``_`` ) で区切ってください。

  ..  seealso::
    パターンの完全なドキュメントについては :doc:`user-guide` を参照してください。

``List<String> getOrder()``
  それぞれの出力ファイルの内容をソートするプロパティを指定します。

  それぞれのプロパティは ``+property_name`` で昇順、 ``-property_name`` で降順を表します。
  プロパティ名はDMDLのプロパティ名と同様、すべて小文字で単語をアンダースコア ( ``_`` ) で区切ってください。

以下は ``Document`` というデータモデルを宣言した場合の実装例です。
このデータモデルにはそれぞれ、カテゴリ名を表す ``category`` と、作成日付を表す ``date`` というプロパティがあるものとします。

..  code-block:: java

    public class DocumentToCsv extends AbstractDocumentCsvOutputDescription {

        @Override
        public String getBasePath() {
            return "output";
        }

        @Override
        public String getResourcePattern() {
            return "documents-{category}.csv";
        }

        @Override
        public List<String> getOrder() {
            return Arrays.asList("+date");
        }
    }

上記の例では、論理パス ``output`` 以下に :file:`documents-<カテゴリ名>.csv` というファイルをカテゴリごとに作成し、内容を作成日付の昇順でソートします。

ここで記述した内容は、ジョブフローの入力に対して、 ``@Export`` を利用して指定します。

..  attention::
    出力するデータが存在しない場合、ファイルは一つも作成されません。
    これは、ファイル名にプレースホルダを指定していない場合でも同様です。

..  [#] 「ランダムな値」を指定した場合、レコードごとにランダムな番号を生成して宛先のファイルを振り分けます。
        レコード数が少ない場合、ランダムな番号が偏ってしまって、範囲にあるすべてのファイルが生成されるとは限りません。

