=========================
Asakusa DSLスタートガイド
=========================

この文書では、asakusa-archetype-batchappを利用したプロジェクト構成で、Asakusa DSLを使ってバッチアプリケーションを記述する方法について簡単に紹介します。

asakusa-archetype-batchappの利用方法については :doc:`../application/maven-archetype` を参照してください。また、Asakusa DSLのより詳しい情報は :doc:`user-guide` を参照して下さい。

データモデルクラスを作成する
============================
Asakusa DSLで取り扱う処理対象のデータモデルは、特定の形式で定義されたJavaのクラスで表されます。

これらのクラスを作成する方法は、 :doc:`../dmdl/start-guide` を参照して下さい。


演算子を記述する
================
Asakusa Frameworkでは、Hadoop Map Reduceフレームワークを直接利用する代わりに、「演算子」と呼ばれる処理の単位をデータフローの形式で組み合わせて処理を記述します。ここでは、その演算子を作成する方法を紹介します。

演算子の種類
------------
演算子を作成するには「演算子の種類」を一つ選んで、その種類の制約の中でプログラムを作成することになります。
以下は、Asakusa Frameworkで利用可能な演算子の種類の抜粋です。

..  list-table:: 演算子の種類 (抜粋)
    :widths: 4 4 12
    :header-rows: 1

    * - 名前
      - 注釈型
      - 概要
    * - 分岐演算子
      - Branch
      - レコードを内容に応じた出力に振り分ける
    * - 更新演算子
      - Update
      - レコードの内容を更新して出力する
    * - 変換演算子
      - Convert
      - レコードを別の種類のレコードに変換して出力する
    * - マスタ結合演算子
      - MasterJoin
      - レコードにマスタデータを結合して出力する
    * - マスタ分岐演算子
      - MasterBranch
      - レコードとマスタデータの内容に応じた出力に振り分ける
    * - マスタつき更新演算子
      - MasterJoinUpdate
      - レコードの内容をマスタデータの情報を元に更新して出力する
    * - 単純集計演算子
      - Summarize
      - グループ化したレコードを集計して出力する
    * - グループ結合演算子
      - CoGroup
      - 複数種類のレコードをグループ化して任意の処理を行う

なお、上記の「注釈型」はOperator DSLで演算子のプログラムを記述する際に、
プログラムに対して付与しなければならない演算子注釈の型を表しています。
これらの注釈は、いずれも ``com.asakusafw.vocabulary.operator`` パッケージに宣言されています。

利用可能な全ての演算子については、 :doc:`operators` を参照して下さい。


演算子クラスを作成する
----------------------
それぞれの演算子は、演算子注釈を指定したJavaのメソッドとして宣言します。
ここではまず、演算子を作成するためのクラスを宣言します。このクラスは、以下のようにJavaの抽象 (abstract) クラスとして宣言します [#]_ 。

..  code-block:: java

    package com.example.operator;

    public abstract class ExampleOperator {
        ...
    }

なお、それぞれの演算子クラスは、末尾の名前が"operator"であるようなパッケージに配置することを推奨しています。

..  [#] その他、publicなトップレベルクラスであり、型引数を宣言しない、明示的な親クラスや親インターフェースを指定しない、明示的なコンストラクタを宣言しない、などの制約があります。


演算子メソッドの作成
--------------------
演算子クラスには、演算子注釈を指定したJavaのメソッドを宣言します。
フレームワークが提供する全ての演算子注釈は、パッケージ ``com.asakusafw.vocabulary.operator`` 以下に配置されています。

演算子の種類によって演算子メソッドの構成は変わります。
たとえば、メソッドを抽象メソッドとして宣言して、コンパイラが実装コードを自動的に生成するものなどもあります。
全ての演算子メソッドで共通のルールは、以下の通りです。

* 全ての演算子メソッドはpublicで宣言する
* メソッド1つに付き、演算子注釈は1つまで
* 同じ名前の演算子メソッド [#]_ は同じクラスに宣言できない

それぞれの演算子について詳しくは、 :doc:`operators` か、演算子注釈のドキュメンテーションコメントを参照して下さい。

..  [#] 演算子クラス内では、メソッドのオーバーロードを禁止しています

演算子メソッドの制限
--------------------
ここで作成した演算子メソッドは、最終的にHadoopのMap Reduceプログラムの一部として利用されます。
そのため、以下のようなプログラムを演算子メソッドの本体に書いた場合、期待した通りに動作しない場合があります。

* フィールドの値を演算子間で共有する
* ローカルシステムのファイルなどのリソースを利用する
* スレッドを生成する

基本的には、演算子メソッドのフィールドに渡されたリソースや、その演算子メソッドのみから利用するフィールドを利用してプログラムを作成して下さい。

フレームワークAPI
-----------------
Asakusa Frameworkは、演算子メソッドを記述する際にいくつか便利なAPIを用意しています。
演算子の中では前項のようにできることに制限がありますが、フレームワークAPIを併用することでその制限のいくつかを緩和できる可能性があります。
フレームワークAPIにはレポートとバッチ設定情報の2種類がコアとして用意されており、利用方法については :doc:`user-guide` を参照して下さい。

演算子の実装例
--------------
いくつかの演算子について、実装例を示します。
ここでの実装例は1クラス1演算子メソッドとなっていますが、実際には1つのクラスに複数の演算子メソッドを宣言することも可能です。

更新演算子の実装例
~~~~~~~~~~~~~~~~~~
更新演算子は、 ``Update`` 注釈を付与したメソッドを宣言します。
以下は、Hogeクラスのモデルオブジェクトのプロパティvalueを100に変更するような、更新演算子の例です。

..  code-block:: java

    public abstract class ExampleOperator {

        /**
         * レコードの値に100を設定する。
         * @param hoge 更新するレコード
         */
        @Update
        public void edit(Hoge hoge) {
            hoge.setValue(100);
        }
        ...
    }

マスタ結合演算子の実装例
~~~~~~~~~~~~~~~~~~~~~~~~
マスタ結合演算子は、 ``MasterJoin`` 注釈を付与したメソッドを宣言します。
以下は、 ``HogeTrn`` のモデルオブジェクトに、マスタである ``HogeMst`` を結合するような、マスタ結合演算子の例です。

..  code-block:: java

    public abstract class ExampleOperator {

        /**
         * レコードHogeMstとHogeTrnを結合し、結合結果のHogeを返す。
         * @param master マスタデータ
         * @param tx トランザクションデータ
         * @return 結合結果
         */
        @MasterJoin
        public abstract Hoge join(HogeMst master, HogeTrn tx);

        ...
    }

マスタ結合演算子は、結合条件や結合方法をデータモデルクラスから自動的に推定して、適切なコードを自動生成します。
そのため、抽象(abstract)メソッドとして宣言し、戻り値は結合モデル [#]_ でなければなりません。

..  [#] :doc:`../dmdl/user-guide`

非等価結合を用いるマスタつき更新演算子の実装例
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
マスタつき更新演算子は、 ``MasterJoinUpdate`` 注釈を付与したメソッドを宣言します。
また、非等価結合を含む場合には、さらに補助演算子として ``MasterSelection`` 注釈を付与したメソッドを宣言し、
``MasterJoinUpdate`` 注釈からそのメソッドを指定して下さい。
以下は、 ``HogeTrn`` のモデルオブジェクトに、マスタである ``ItemMst`` の項目を一部追記するような、マスタつき更新演算子の例です。

..  code-block:: java

    public abstract class ExampleOperator {
        /**
         * 有効なマスタを選択する。
         * @param masters 選択対象のマスタデータ一覧
         * @param tx トランザクションデータ
         * @return 実際に利用するマスタデータ、利用可能なものがない場合はnull
         */
        @MasterSelection
        public ItemMst selectItemMst(List<ItemMst> masters, HogeTrn tx) {
            for (ItemMst mst : masters) {
                if (mst.getStart() <= tx.getDate() &&
                        tx.getDate() <= mst.getEnd()) {
                    return mst;
                }
            }
            return null;
        }

        /**
         * マスタの価格をトランザクションデータに設定する。
         * @param master マスタデータ
         * @param tx 変更するトランザクションデータ
         */
        @MasterJoinUpdate(selection = "selectItemMst")
        public void updateWithMaster(
                @Key(group = "id") ItemMst master,
                @Key(group = "itemId") HogeTrn tx) {
            tx.setPrice(master.getPrice());
        }
    }

マスタつき更新演算子は、結合条件をメソッドの引数に対する ``Key`` 注釈で記述します。
このとき、要素 ``group`` に指定する値は、等価結合に用いるプロパティの名前です。
同時に、非等価結合の部分を ``selectItemMst`` メソッドに記述して、 ``MasterJoinUpdate`` 注釈の要素 ``selection`` から指定しています。

単純集計演算子の実装例
~~~~~~~~~~~~~~~~~~~~~~
単純集計演算子は、 ``Summarize`` 注釈を付与した抽象メソッドを宣言します。
以下は、 ``Hoge`` クラスのモデルオブジェクトを集計し、 ``HogeTotal`` クラスのモデルオブジェクトに格納する例です。

..  code-block:: java

    public abstract class ExampleOperator {

        /**
         * レコードHogeをHogeTotalに集計する。
         * @param hoge 集計対象
         * @return 集計結果
         */
        @Summarize
        public abstract HogeTotal summarize(Hoge hoge);

        ...
    }

なお、この演算子は集計モデルである ``HogeTotal`` を作成した際の情報を元に、自動的に ``Hoge`` を集計するプログラムを生成します。
そのため、抽象(abstract)メソッドとして宣言し、戻り値は必ず集計モデル [#]_ でなければなりません。

..  [#] :doc:`../dmdl/user-guide`

グループ整列演算子の実装例
~~~~~~~~~~~~~~~~~~~~~~~~~~
グループ整列演算子は、 ``GroupSort`` 注釈を付与したメソッドを宣言します。
以下は、 ``Hoge`` クラスのモデルオブジェクトをプロパティ ``name`` でグループ化し、
さらにプロパティ ``age`` の昇順で並べたリストを引数に受け取ったのちに、
そのリストの先頭と末尾の要素をそれぞれ別の出力 ``first, last`` に渡すような例です。

..  code-block:: java

    public abstract class ExampleOperator {

        /**
         * レコードHogeを名前ごとに年齢の若い順に並べ、先頭と末尾だけをそれぞれ結果に流す。
         * @param joined グループごとのリスト
         * @param first グループごとの先頭要素
         * @param last グループごとの末尾要素
         */
        @GroupSort
        public void firstLast(
                @Key(group = "name", order = "age ASC") List<Hoge> hogeList,
                Result<Hoge> first,
                Result<Hoge> last) {
            first.add(hogeList.get(0));
            last.add(hogeList.get(hogeList.size() - 1));
        }
        ...
    }

メソッドの引数に指定している ``Result`` [#]_ は、この演算子の出力を表しています。
また、注釈 ``Key`` の要素 ``order`` は、要素の整列順序を表しています。

..  [#] ``com.asakusafw.runtime.core.Result``

演算子のテスト
--------------
演算子のテストは、通常のJavaメソッドをテストする方法でテストして下さい。

より詳しくは、 :doc:`../testing/start-guide` を参照して下さい。

なお、フレームワークAPIを利用したメソッドをテストする場合、フレームワークAPIをモックに差し替えてテストすることも可能です。

演算子のビルド
--------------
asakusa-archetype-batchappから生成したEclipseプロジェクト上では、通常のJavaを使った開発と同様、ソースを記述するとインクリメンタルビルドによって演算子のコンパイルが自動的に行われるほか、注釈プロセッサによって演算子用のJavaソースが以下のディレクトリに自動生成されます。

* ``<プロジェクトのルート>/target/generated-sources/annotations``

または、mvnコマンドを利用してJavaコンパイラを実行すると、注釈プロセッサを起動できます。これはMavenの ``compile`` フェーズで自動的に起動しますので、プロジェクト内で以下のようにコマンドを実行します。

..  code-block:: sh

    mvn compile

その他、 ``mvn package`` や ``mvn install`` などでも自動的に注釈プロセッサが起動します。

注釈プロセッサによって、演算子を組み合わせてフローを構築するためのファクトリークラス(演算子ファクトリクラス)と、
演算子クラスの実装を提供する実装クラスの2つが自動的に生成されます。
そのとき、演算子ファクトリクラスは、元の演算子クラスの末尾に ``Factory`` を付与した名前のクラスで、
実装クラスは同様に ``Impl`` を付与した名前のクラスとなります。

データフローを記述する
======================
データフローは、演算子を組み合わせて一連のデータ処理の流れを記述したものです。
Asakusa DSLでは、外部入力をソースにデータを処理して外部出力に結果を書き戻す「ジョブフロー」と、
演算子を組み合わせてより大きな演算子を構築する「フロー部品」を、それぞれ定義できます。

この章では、前者のジョブフローのみを紹介します。
フロー部品については :doc:`user-guide` を参照して下さい。

外部入出力を定義する
--------------------
ジョブフローが利用する外部入出力を定義するには、
それぞれ「インポーター」と「エクスポーター」の処理内容を記述します。

現在のところ、Asakusa Frameworkでは2種類の外部入出力を提供しています。

* Hadoopファイルシステム上のファイル入出力に利用する
* ThunderGateと連携してリレーショナルデータベースのテーブル情報を入出力に利用する

Hadoopファイルシステムからインポートする
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Hadoopファイルシステム上のファイルをインポートする場合、
``com.asakusafw.vocabulary.external.FileImporterDescription`` クラスのサブクラスを作成して必要な情報を記述します。
このクラスでは、下記のメソッドをオーバーライドします。

``Class<?> getModelType()``
    処理対象とするモデルオブジェクトの型を表すクラスを指定します。
    ここに指定した型がジョブフローの入力として利用されます。

``Set<String> getPaths()``
    処理対象とするファイルシステム上のパス一覧を指定します。

``Class<? extends FileInputFormat> getInputFormat()``
    処理対象とするファイルの形式を表すクラス [#]_ を指定します。
    このとき、キーは ``NullWritable`` で値は ``getModelType()`` に指定した型である必要があります。

``DataSize getDataSize()``
    このインポータが取り込むデータサイズの分類を指定します。

..  [#] ``org.apache.hadoop.mapreduce.FileInputFormat`` のサブクラス

Hadoopファイルシステムへエクスポートする
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
ジョブフローの処理結果をHadoopファイルシステムに書き出すには、
``com.asakusafw.vocabulary.external.FileExporterDescription`` クラスのサブクラスを作成して必要な情報を記述します。
このクラスでは、下記のメソッドをオーバーライドします。

``Class<?> getModelType()``
    処理対象とするモデルオブジェクトの型を表すクラスを指定します。
    ここに指定した型がジョブフローの出力として利用されます。

``String getPathPrefix()``
    エクスポート先のファイルシステム上のパスを指定します。
    このパスは ``<directory>/<prefix>-*`` の形式である必要があります。

``Class<? extends FileOutputFormat> getOutputFormat()``
    エクスポータの出力ファイルの形式を表すクラス [#]_ を指定します。
    このとき、キーは ``NullWritable`` で値は ``getModelType()`` に指定した型である必要があります。

..  [#] ``org.apache.hadoop.mapreduce.FileOutputFormat`` のサブクラス

ThunderGateと連携する
~~~~~~~~~~~~~~~~~~~~~
ThunderGateと連携してデータベースのテーブルを操作する方法は、
:doc:`with-thundergate` を参照して下さい。

ジョブフロークラスの作成
------------------------
それぞれのジョブフローは、 ``FlowDescription`` [#]_ を継承したJavaのクラス(ジョブフロークラス)として宣言します [#]_ 。ただしジョブフローであることを表すために、クラスの注釈として ``JobFlow`` [#]_ を指定し、要素 ``name`` にこのジョブフローの名前を指定します。

..  code-block:: java

    package com.example.jobflow;

    import com.asakusafw.vocabulary.flow.*;

    @JobFlow(name = "example")
    public class ExampleJobFlow extends FlowDescription {
        ...
    }

なお、それぞれのジョブフロークラスは、末尾の名前が ``jobflow`` であるようなパッケージに配置することを推奨しています。

..  [#] ``com.asakusafw.vocabulary.flow.FlowDescription``
..  [#] その他、publicなトップレベルクラスであり、具象クラスである(abstractを指定しない)、型引数を宣言しない、 ``FlowDescription`` 以外の親クラスや親インターフェースを指定しない、などの制約があります。
..  [#] ``com.asakusafw.vocabulary.flow.JobFlow``

コンストラクタの作成
--------------------
ジョブフローの入出力は、ジョブフロークラスのコンストラクタで宣言します。
このコンストラクタは ``public`` コンストラクタとして宣言し、次のような型の仮引数を宣言します。

* ジョブフローへの入力を表す ``In<T>`` [#]_

  * 型引数には入力されるデータモデルクラスの型を指定する
  * インポート処理記述を注釈 ``Import`` [#]_ で指定する

* ジョブフローからの出力を表す ``Out<T>`` [#]_

  * 型引数には出力するデータモデルクラスの型を指定する
  * エクスポート処理記述を注釈 ``Export`` [#]_ で指定する

なお、注釈 ``Import`` および ``Export`` には、それぞれ名前とインポータやエクスポータの処理内容を記述したクラスを指定します。
ここで指定した処理内容に応じて、ジョブフローの入力や出力の方法が決まります。

以下のように、コンストラクタの引数と同名のインスタンスフィールドを作成し、
コンストラクタの引数をフィールドに代入するとよいでしょう。

..  code-block:: java

    package com.example.jobflow;

    import com.asakusafw.vocabulary.flow.*;

    @JobFlow(name = "example")
    public class ExampleJobFlow extends FlowDescription {
        In<Hoge> in;
        Out<Hoge> out;
        public ExampleFlowPart(
                @Import(name = "hoge", description = HogeFromDb.class)
                In<Hoge> in,
                @Export(name = "hoge", description = HogeIntoDb.class)
                Out<Hoge> out) {
            this.in = in;
            this.out = out;
        }
        ...
    }

..  [#] ``com.asakusafw.vocabulary.flow.In``
..  [#] ``com.asakusafw.vocabulary.flow.Import``
..  [#] ``com.asakusafw.vocabulary.flow.Out``
..  [#] ``com.asakusafw.vocabulary.flow.Export``

ジョブフローメソッドの作成
--------------------------
ジョブフローの内容は、 ``FlowDescription`` クラスの ``describe`` メソッドをオーバーライドして記述します。
このメソッドの中には、コンストラクタに指定した入出力と作成した演算子を接続して、データフローを構築するようなプログラムを書きます。

..  code-block:: java

    ...
    @JobFlow(name = "example")
    public class ExampleJobFlow extends FlowDescription {
        ...
        @Override
        public void describe() {
            // ここにデータフローを記述する
        }
    }

演算子ファクトリを用意する
~~~~~~~~~~~~~~~~~~~~~~~~~~
データフローを構築するには、まず演算子のビルド結果として生成された演算子ファクトリをインスタンス化します。

演算子ファクトリには、元となった演算子メソッドと同じ名前のメソッドがそれぞれ定義されています。
これはデータフロー中の演算子を新たに作成するファクトリメソッドで、対応する演算子を組み立てるために利用します。

また、Asakusa Frameworkは ``CoreOperatorFactory`` [#]_ という組み込みの演算子ファクトリも提供しています。
以下はそれぞれの演算子ファクトリをインスタンス化する例です。

..  code-block:: java

    @Override
    public void describe() {
        CoreOperatorFactory core = new CoreOperatorFactory();
        ExampleOperatorFactory example = new ExampleOperatorFactory();
        ...
    }

..  [#] ``com.asakusafw.vocabulary.flow.util.CoreOperatorFactory``

入力と演算子を接続する
----------------------
コンストラクタに指定した ``In`` オブジェクトを、演算子ファクトリのメソッドの引数に渡すと、ジョブフローに入力されたデータを、その演算子で処理することができます。
このとき、入力されるデータの種類と、演算子に入力できるデータの種類は一致していなければなりません。

..  code-block:: java

    In<Hoge> in;

    @Override
    public void describe() {
        ExampleOperatorFactory example = new ExampleOperatorFactory();
        Update update = example.update(in);
    }

演算子と演算子を接続する
~~~~~~~~~~~~~~~~~~~~~~~~
演算子ファクトリの各メソッドが返すオブジェクトは、それぞれ対応する演算子を表しています。
このオブジェクトはそれぞれいくつかの公開フィールドを持っていて、演算子の出力を表しています。

演算子の出力を別の演算子の入力に接続することで、複雑なデータの流れを表現できます。

..  code-block:: java

    In<Hoge> in;

    @Override
    public void describe() {
        ExampleOperatorFactory example = new ExampleOperatorFactory();
        Update update = example.update(in);
        Branch branch = example.branch(update.out);
    }

演算子と出力を接続する
~~~~~~~~~~~~~~~~~~~~~~
ジョブフローの結果を出力する際には、コンストラクタに指定された ``Out`` オブジェクトの ``add()`` メソッドの引数に、それぞれの演算子の出力を渡します。
こうすることで、その演算子の出力結果がそのままフロー部品の出力結果となります。このとき、両者の出力は同じデータの種類でなければなりません。

なお、それぞれの演算子の出力は、いずれかの演算子への入力、またはフロー部品からの出力と接続されている必要があります。
不要な演算子の出力がある場合、 ``CoreOperatorFactory.stop()`` メソッド利用してその出力を利用しないことを明示的にコンパイラに指示する必要があります。

..  code-block:: java

    In<Hoge> in;
    In<Hoge> out;

    @Override
    public void describe() {
        CoreOperatorFactory core = new CoreOperatorFactory();
        ExampleOperatorFactory example = new ExampleOperatorFactory();
        Update update = example.update(in);
        Branch branch = example.branch(update.out);
        out.add(branch.ok);
        core.stop(branch.ng);
    }

ジョブフローの実装例
--------------------
ジョブフローの単純な例を示します。ここで紹介する例の完全なコードは、サンプルプロジェクト ``example-business`` [#]_ にあります。

まず、 ``STOCK`` テーブルに含まれる行のうち、 ``QUANTITY`` が1以上のもののみを読み出す例です。
また、読み出し時にテーブル全体をロックします。

..  code-block:: java

    package com.example.business.jobflow;

    import com.example.business.model.table.model.Stock;
    import com.asakusafw.vocabulary.bulkloader.DbImporterDescription;

    public class StockFromDb extends DbImporterDescription {
        @Override
        public Class<?> getModelType() {
            return Stock.class;
        }
        @Override
        public String getWhere() {
            // 在庫が1個以上ないと計算しても無駄
            return "QUANTITY > 0";
        }
        @Override
        public LockType getLockType() {
            // テーブル全体をロックしておく
            return LockType.TABLE;
        }
    }

次に、Hadoopでの処理内容を ``STOCK`` テーブルに書き戻す例です。
テーブルモデルクラスである ``Stock`` を指定すると、その他の情報はクラスの情報を元に自動的に計算します。

..  code-block:: java

    package com.example.business.jobflow;

    import com.example.business.model.table.model.Stock;
    import com.asakusafw.vocabulary.bulkloader.DbExporterDescription;

    public class StockToDb extends DbExporterDescription {
        @Override
        public Class<?> getModelType() {
            return Stock.class;
        }
    }

最後にジョブフローの例を示します。

..  code-block:: java

    package com.example.business.jobflow;

    import com.example.business.model.table.model.*;
    import com.example.business.operator.StockOpFactory;
    import com.example.business.operator.StockOpFactory.*;
    import com.asakusafw.vocabulary.flow.*;
    import com.asakusafw.vocabulary.flow.util.CoreOperatorFactory;

    @JobFlow(name = "stock")
    public class StockJob extends FlowDescription {
        private In<Shipment> shipmentIn;
        private In<Stock> stockIn;
        private Out<Shipment> shipmentOut;
        private Out<Stock> stockOut;
        /**
         * コンストラクタ。
         * @param shipmentIn 処理対象の注文情報
         * @param stockIn 処理対象の在庫情報
         * @param shipmentOut 処理結果の注文情報
         * @param stockOut 処理結果の在庫情報
         */
        public StockJob(
                @Import(name = "shipment", description = ShipmentFromDb.class)
                In<Shipment> shipmentIn,
                @Import(name = "stock", description = StockFromDb.class)
                In<Stock> stockIn,
                @Export(name = "shipment", description = ShipmentToDb.class)
                Out<Shipment> shipmentOut,
                @Export(name = "stock", description = StockToDb.class)
                Out<Stock> stockOut) {
            this.shipmentIn = shipmentIn;
            this.stockIn = stockIn;
            this.shipmentOut = shipmentOut;
            this.stockOut = stockOut;
        }

        @Override
        protected void describe() {
            CoreOperatorFactory core = new CoreOperatorFactory();
            StockOpFactory op = new StockOpFactory();
            
            // 処理できない注文をあらかじめフィルタリング
            CheckShipment check = op.checkShipment(shipmentIn);
            core.stop(check.notShipmentped);
            core.stop(check.completed);
            
            // 在庫引当を行う
            Cutoff cutoff = op.cutoff(stockIn, check.costUnknown);
            
            // 結果を書き出す
            shipmentOut.add(cutoff.newShipments);
            stockOut.add(cutoff.newStocks);
        }
    }

..  [#] https://github.com/asakusafw/asakusafw-examples


ジョブフローのテスト
--------------------
ジョブフローのテストは、Asakusa Frameworkが提供するテストドライバを利用して行います。

詳しくは、 :doc:`../testing/start-guide` を参照して下さい。


バッチを記述する
================
バッチはこれまでに紹介したジョブフローをワークフローの形式で組み合わせて、一連の処理を実現するための構造です。

バッチクラスの作成
------------------
それぞれのバッチは、 ``BatchDescription`` [#]_ を継承したJavaのクラス(バッチクラス)として宣言します [#]_ 。また、付加情報を表すために、クラスの注釈として ``Batch`` [#]_ を指定して要素 ``name`` にこのバッチの名前を指定します。
以下はバッチクラスを作成する例です。

..  code-block:: java

    package com.example.batch;

    import com.asakusafw.vocabulary.flow.*;

    @Batch(name = "example")
    public class ExampleBatch extends BatchDescription {
        ...
    }

なお、それぞれのバッチクラスは、末尾の名前が ``batch`` であるようなパッケージに配置することを推奨しています。

..  [#] ``com.asakusafw.vocabulary.batch.BatchDescription``
..  [#] その他、publicなトップレベルクラスであり、具象クラスである(abstractを指定しない)、型引数を宣言しない、明示的な親クラスや親インターフェースを指定しない、明示的なコンストラクタを宣言しない、などの制約があります。
..  [#] ``com.asakusafw.vocabulary.batch.Batch``

バッチメソッドの作成
--------------------
バッチの内容は、 ``BatchDescription`` クラスの ``describe`` メソッドをオーバーライドして記述します。
このメソッドの中には、ジョブフローの依存関係を記述して、バッチ全体を構築するようなプログラムを書きます。
以下はバッチメソッドを記述する例です。

..  code-block:: java

    @Batch(name = "example")
    public class ExampleBatch extends BatchDescription {
        @Override
        public void describe() {
            Work first = run(FirstFlow.class).soon();
            Work second = run(SecondFlow.class).after(first);
            Work para = run(ParallelFlow.class).after(first);
            Work join = run(JoinFlow.class).after(second, para);
            ...
        }
    }

バッチの内部で実行するジョブフローは、 ``BatchDescription`` クラスから継承した ``run()`` メソッドで指定します。
同メソッドには対象のジョブフロークラスのクラスリテラルを指定し、そのままメソッドチェインで ``soon()`` や ``after()`` メソッドを起動します。

``soon`` メソッドはバッチの内部で最初に実行されるジョブフローを表し、
``after`` メソッドは依存関係にある処理を引数に指定して、
それらの処理が全て完了後に実行されるジョブフローを表します。

バッチの実装例
--------------
バッチの単純な例を示します。
ここで紹介する例の完全なコードは、サンプルプロジェクト ``example-tutorial`` [#]_ にあります。
以下の例は非常に簡単なバッチで、 ``TutorialJob`` というジョブフローを実行するのみです。
また、バッチの名前には ``tutorial`` を指定しています。

..  code-block:: java

    package com.example.tutorial.batch;

    import com.asakusafw.vocabulary.batch.*;
    import com.example.tutorial.jobflow.*;

    @Batch(name = "tutorial")
    public class TutorialBatch extends BatchDescription {

        @Override
        protected void describe() {
            run(TutorialJob.class).soon();
        }
    }

..  [#] https://github.com/asakusafw/asakusafw

バッチアプリケーションを生成する
================================

Asakusa DSLからバッチアプリケーションを生成するには、mvnコマンドを利用してAsakusa DSLコンパイラを実行します [#]_ 。
これはMavenの ``package`` フェーズで自動的に起動しますので、プロジェクト内で以下のようにコマンドを実行します。

..  code-block:: sh

    mvn package

その他、 ``mvn install`` などでも自動的にコンパイラが起動します。


..  [#] クリーンビルドを行う際に、演算子の依存関係の問題で一時的にJavaのコンパイルエラーのメッセージが表示される場合があります。
        Javaコンパイルのフェーズを正常終了できた場合、これらのメッセージが出ても特に問題はありません。
