=========================
Asakusa DSLスタートガイド
=========================

この文書では :doc:`../introduction/start-guide` の構成で、Asakusa DSLを使ってバッチアプリケーションを記述する方法について簡単に紹介します。

..  seealso::
    Asakusa DSLのより詳しい情報は :doc:`user-guide` を参照して下さい。

データモデルクラスを作成する
============================

Asakusa DSLで取り扱う処理対象のデータモデルは、特定の形式で定義されたJavaのクラスで表されます。

これらのクラスを作成する方法は、 :doc:`../dmdl/start-guide` を参照して下さい。

演算子を記述する
================

Asakusa Frameworkでは、HadoopやSparkなどのフレームワークを直接利用する代わりに、「演算子」と呼ばれる処理の単位をデータフローの形式で組み合わせて処理を記述します。
ここでは、その演算子を作成する方法を紹介します。

演算子の種類
------------
演算子を作成するには「演算子の種類」を一つ選んで、その種類の制約の中でプログラムを作成することになります。
以下は、Asakusa Frameworkで利用可能な演算子の種類の抜粋です。

..  list-table:: 演算子の種類 (抜粋)
    :widths: 2 2 5
    :header-rows: 1

    * - 名前
      - 注釈型
      - 概要
    * - 分岐演算子
      - ``Branch``
      - レコードを内容に応じた出力に振り分ける
    * - 更新演算子
      - ``Update``
      - レコードの内容を更新して出力する
    * - 変換演算子
      - ``Convert``
      - レコードを別の種類のレコードに変換して出力する
    * - マスタ結合演算子
      - ``MasterJoin``
      - レコードにマスタデータを結合して出力する
    * - マスタ分岐演算子
      - ``MasterBranch``
      - レコードとマスタデータの内容に応じた出力に振り分ける
    * - マスタつき更新演算子
      - ``MasterJoinUpdate``
      - レコードの内容をマスタデータの情報を元に更新して出力する
    * - 単純集計演算子
      - ``Summarize``
      - グループ化したレコードを集計して出力する
    * - グループ結合演算子
      - ``CoGroup``
      - 複数種類のレコードをグループ化して任意の処理を行う

なお、上記の「注釈型」はOperator DSLで演算子のプログラムを記述する際に、プログラムに対して付与しなければならない演算子注釈の型を表しています。
これらの注釈は、いずれも ``com.asakusafw.vocabulary.operator`` パッケージ [#]_ に宣言されています。

..  seealso::
    利用可能な全ての演算子については、 :doc:`operators` を参照して下さい。

..  [#] :javadoc:`com.asakusafw.vocabulary.operator.package-summary`

演算子クラスを作成する
----------------------

それぞれの演算子は、演算子注釈を指定したJavaのメソッドとして宣言します。
ここではまず、演算子を作成するためのクラスを宣言します。
このクラスは、以下のようにJavaの抽象 ( ``abstract`` ) クラスとして宣言します [#]_ 。

..  code-block:: java

    package com.example.operator;

    public abstract class ExampleOperator {
        ...
    }

なお、それぞれの演算子クラスは、末尾の名前が ``operator`` であるようなパッケージに配置することを推奨しています。

..  [#] その他、publicなトップレベルクラスであり、型引数を宣言しない、明示的な親クラスや親インターフェースを指定しない、明示的なコンストラクタを宣言しない、などの制約があります。

演算子メソッドの作成
--------------------

演算子クラスには、演算子注釈を指定したJavaのメソッドを宣言します。
Asakusa Frameworkが提供する全ての演算子注釈は、パッケージ ``com.asakusafw.vocabulary.operator`` [1]_ 以下に配置されています。

演算子の種類によって演算子メソッドの構成は変わります。
たとえば、メソッドを抽象メソッドとして宣言して、コンパイラが実装コードを自動的に生成するものなどもあります。
全ての演算子メソッドで共通のルールは、以下の通りです。

* 全ての演算子メソッドは ``public`` で宣言する
* メソッド1つに付き、演算子注釈は1つまで
* 同じ名前の演算子メソッドは同じクラスに宣言できない [#]_

..  seealso::
    それぞれの演算子について詳しくは、 :doc:`operators` か、演算子注釈のドキュメンテーションコメントを参照して下さい。

..  [#] 演算子クラス内では、メソッドのオーバーロードを禁止しています

演算子メソッドの制限
--------------------

ここで作成した演算子メソッドは、最終的にHadoopやSparkなどのプラットフォーム上で動作するプログラムの一部として利用されます。
そのため、以下のようなプログラムを演算子メソッドの本体に書いた場合、期待した通りに動作しない場合があります。

* フィールドの値を演算子間で共有する
* ローカルシステムのファイルなどのリソースを利用する
* スレッドを生成する

基本的には、演算子メソッドのフィールドに渡されたリソースや、その演算子メソッドのみから利用するフィールドを利用してプログラムを作成して下さい。

フレームワークAPI
-----------------

Asakusa Frameworkは、演算子メソッドを記述する際にいくつか便利なAPIを用意しています。
演算子の中では前項のようにできることに制限がありますが、フレームワークAPIを併用することでその制限のいくつかを緩和できる可能性があります。

フレームワークAPIにはレポートとバッチ設定情報の2種類がコアAPIとして用意されています。

..  seealso::
    フレームワークAPIの利用方法については :doc:`user-guide` を参照して下さい。

演算子の実装例
--------------

いくつかの演算子について、実装例を示します。
ここでの実装例は1クラス1演算子メソッドとなっていますが、実際には1つのクラスに複数の演算子メソッドを宣言することも可能です。

更新演算子の実装例
~~~~~~~~~~~~~~~~~~

更新演算子は、 ``Update`` 注釈を付与したメソッドを宣言します。
以下は、 `Hoge` クラスのデータモデルオブジェクトのプロパティ `value` を `100` に変更するような、更新演算子の例です。

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
..  **

マスタ結合演算子の実装例
~~~~~~~~~~~~~~~~~~~~~~~~

マスタ結合演算子は、 ``MasterJoin`` 注釈を付与したメソッドを宣言します。
以下は、 `HogeTrn` のデータモデルオブジェクトに、マスタである `HogeMst` を結合するような、マスタ結合演算子の例です。

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
..  **

マスタ結合演算子は、結合条件や結合方法をデータモデルクラスから自動的に推定して、適切なコードを自動生成します。
そのため、抽象( ``abstract`` )メソッドとして宣言し、戻り値は結合モデルでなければなりません。

..  seealso::
    結合モデルについては :doc:`../dmdl/user-guide` を参照してください。

非等価結合を用いるマスタつき更新演算子の実装例
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

マスタつき更新演算子は、 ``MasterJoinUpdate`` 注釈を付与したメソッドを宣言します。
また、非等価結合を含む場合には、さらに補助演算子として ``MasterSelection`` 注釈を付与したメソッドを宣言し、 ``MasterJoinUpdate`` 注釈からそのメソッドを指定して下さい。
以下は、 `HogeTrn` のデータモデルオブジェクトに、マスタである `ItemMst` の項目を一部追記するような、マスタつき更新演算子の例です。

..  code-block:: java

    public abstract class ExampleOperator {

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
    }
..  **

マスタつき更新演算子は、結合条件をメソッドの引数に対する ``Key`` 注釈で記述します。
このとき、要素 ``group`` に指定する値は、等価結合に用いるプロパティの名前です。
同時に、非等価結合の部分を `selectItemMst` メソッドに記述して、 ``MasterJoinUpdate`` 注釈の要素 ``selection`` から指定しています。

単純集計演算子の実装例
~~~~~~~~~~~~~~~~~~~~~~

単純集計演算子は、 ``Summarize`` 注釈を付与した抽象メソッドを宣言します。
以下は、 `Hoge` クラスのデータモデルオブジェクトを集計し、 `HogeTotal` クラスのデータモデルオブジェクトに格納する例です。

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
..  **

なお、この演算子は集計モデルである `HogeTotal` を作成した際の情報を元に、自動的に `Hoge` を集計するプログラムを生成します。
そのため、抽象( ``abstract`` )メソッドとして宣言し、戻り値は必ず集計モデルでなければなりません。

..  seealso::
    集計モデルについては :doc:`../dmdl/user-guide` を参照してください。

グループ整列演算子の実装例
~~~~~~~~~~~~~~~~~~~~~~~~~~

グループ整列演算子は、 ``GroupSort`` 注釈を付与したメソッドを宣言します。
以下は、 `Hoge` クラスのデータモデルオブジェクトをプロパティ `name` でグループ化し、さらにプロパティ `age` の昇順で並べたリストを引数に受け取ったのちに、そのリストの先頭と末尾の要素をそれぞれ別の出力 `first, last` に渡すような例です。

..  code-block:: java

    public abstract class ExampleOperator {

        /**
         * レコードHogeを名前ごとに年齢の若い順に並べ、先頭と末尾だけをそれぞれ結果に流す。
         * @param hogeList グループごとのリスト
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
..  **

メソッドの引数に指定している ``Result`` は、この演算子の出力を表しています。
また、注釈 ``Key`` の要素 `order` は、要素の整列順序を表しています。

分岐演算子の実装例
~~~~~~~~~~~~~~~~~~

更新演算子は、 ``Branch`` 注釈を付与したメソッドを宣言します。
以下は、 `Hoge` クラスのデータモデルオブジェクトのプロパティ `value` の値に応じてそれぞれの出力にレコードを振り分けるような例です。

..  code-block:: java

    public abstract class ExampleOperator {

        /**
         * レコードの状態ごとに処理を分岐する。
         * @param hoge 対象のレコード
         * @return 分岐先を表すオブジェクト
         */
        @Branch
        public Status select(Hoge hoge) {
            int value = hoge.getPrice();
            if (value <= 100) {
                return Status.OK;
            }
            else {
                return Status.NG;
            }
        }

        /**
         * レコードの状態。
         */
        public enum Status {
            /**
             * 成功。
             */
            OK,

            /**
             * 失敗。
             */
            NG,
        }
        ....
    }
..  **

分岐演算子は出力先を示した列挙型と組み合わせて使用します。
個々のレコードに対して条件判定を行い、分岐先の出力先を示す列挙型を戻り値として返します。

演算子のテスト
--------------

演算子のテストは、通常のJavaメソッドをテストする方法でテストして下さい。

より詳しくは、 :doc:`../testing/start-guide` を参照して下さい。

なお、フレームワークAPIを利用したメソッドをテストする場合、フレームワークAPIをモックに差し替えてテストすることも可能です。

演算子のビルド
--------------

:doc:`../introduction/start-guide` の流れで作成したEclipseプロジェクト上では、通常のJavaを使った開発と同様、ソースを記述するとインクリメンタルビルドによって演算子のコンパイルが自動的に行われるほか、注釈プロセッサによって演算子用のJavaソースが以下のディレクトリに自動生成されます。

* :file:`<プロジェクトのルート>/build/generated-sources/annotations`

または、Gradleを利用してJavaコンパイラを実行すると、注釈プロセッサを起動できます。
これはGradleの :program:`compileJava` タスクで起動するので、プロジェクト内で以下のようにコマンドを実行します。

..  code-block:: sh

    ./gradlew compileJava

その他、 :program:`assemble` タスクや :program:`build` タスクなどでも自動的に注釈プロセッサが起動します。

注釈プロセッサによって、演算子を組み合わせてフローを構築するためのファクトリークラス(演算子ファクトリクラス)と、演算子クラスの実装を提供する実装クラスの2つが自動的に生成されます。
そのとき、演算子ファクトリクラスは、元の演算子クラスの末尾に ``Factory`` を付与した名前のクラスで、実装クラスは同様に ``Impl`` を付与した名前のクラスとなります。

データフローを記述する
======================

データフローは、演算子を組み合わせて一連のデータ処理の流れを記述したものです。
Asakusa DSLでは、外部入力をソースにデータを処理して外部出力に結果を書き戻す「ジョブフロー」と、演算子を組み合わせてより大きな演算子を構築する「フロー部品」を、それぞれ定義できます。

この章では、前者のジョブフローのみを紹介します。
フロー部品については :doc:`user-guide` を参照して下さい。

外部入出力を定義する
--------------------

ジョブフローが利用する外部入出力を定義するには、それぞれ「インポーター」と「エクスポーター」の処理内容を記述します。

Asakusa Frameworkでは以下の外部入出力を提供しています。

* :doc:`Direct I/O <../directio/index>` を利用してHadoopから参照可能なデータソースを直接入出力に利用する
* :doc:`WindGate <../windgate/index>` と連携してローカルファイルシステムやリレーショナルデータベースのテーブル情報を入出力に利用する

以降では、サンプルとしてWindGateを利用して、ローカルファイルシステム上のCSVファイルを外部入出力に利用します。

CSVフォーマットを定義する
~~~~~~~~~~~~~~~~~~~~~~~~~

WindGateがローカルファイルシステム上のCSVファイルを読み書きできるように、それぞれのデータモデルに対するCSVフォーマットを定義します。

`データモデルクラスを作成する`_ 作成したデータモデルの手前に、次のように ``@windgate.csv`` という属性をつけてください。
この作業により、対象のデータモデルと同じ形式のCSVファイルをWindGateが入出力に利用できるようになります。

..  code-block:: none

    @windgate.csv
    example_model = {
        ...
    };

この属性をつけるのは、CSVの入出力に利用するデータモデルのみで十分です。
この属性をつけた状態でデータモデルを再作成すると、元のデータモデルクラスのほかに以下の3つのクラスが作成されます。

#. ``<パッケージ名>.csv.<データモデル名>CsvSupport``
#. ``<パッケージ名>.csv.Abstract<データモデル名>ImporterDescription``
#. ``<パッケージ名>.csv.Abstract<データモデル名>ExporterDescription``

CSVフォーマットについては、 :doc:`../windgate/user-guide` も参考にしてください。

WindGateからインポートする
~~~~~~~~~~~~~~~~~~~~~~~~~~

WindGateからデータをインポートしてジョブフローで処理するには、 ``FsImporterDescription`` [#]_ や ``JdbcImporterDescription`` [#]_ など、
``WindGateImporterDescription`` [#]_ のサブクラスを継承したクラスを作成し、必要なメソッドを実装します。

`CSVフォーマットを定義する`_ で生成された ``Abstract<データモデル名>ImporterDescription`` はそれらの骨格実装を行ったクラスで、
このクラスを継承して以下のメソッドをオーバーライドするだけでインポート処理を記述できます。

``String getProfileName()``
    インポータが使用するプロファイル名を戻り値に指定します。

    インポータは実行時に :file:`$ASAKUSA_HOME/windgate/profile` 以下の :file:`<プロファイル名>.properties` に記述された設定を元に動作します。
    今回はデフォルトの ``"asakusa"`` という文字列を ``return`` 文に指定してください。

``String getPath()``
    インポートするCSVファイルのパスを指定します。

``DataSize getDataSize()``
    このインポータが取り込むデータサイズの分類を指定します。

以下は `Document` というデータモデルを宣言した場合の実装例です。

..  code-block:: java

    public class DocumentFromCsv extends AbstractDocumentCsvImporterDescription {

        @Override
        public String getProfileName() {
            return "asakusa";
        }

        @Override
        public String getPath() {
            return "input.csv";
        }
    }

..  [#] :javadoc:`com.asakusafw.vocabulary.windgate.FsImporterDescription`
..  [#] :javadoc:`com.asakusafw.vocabulary.windgate.JdbcImporterDescription`
..  [#] :javadoc:`com.asakusafw.vocabulary.windgate.WindGateImporterDescription`

WindGateにエクスポートする
~~~~~~~~~~~~~~~~~~~~~~~~~~

ジョブフローの処理結果をHadoopファイルシステムに書き出すには、 ``FsExporterDescription`` [#]_ や ``JdbcExporterDescription`` [#]_ など、
``WindGateExporterDescription`` [#]_ のサブクラスを継承したクラスを作成し、必要なメソッドを実装します。

「 `CSVフォーマットを定義する`_ 」で生成された ``Abstract<データモデル名>ExporterDescription`` はそれらの骨格実装を行ったクラスで、
このクラスを継承して以下のメソッドをオーバーライドするだけでインポート処理を記述できます。

``String getProfileName()``
    エクスポータが使用するプロファイル名を戻り値に指定します。

    インポータと同様に ``"asakusa"`` という文字列を ``return`` 文に指定してください。

``String getPath()``
    エクスポートするCSVファイルのパスを指定します。

以下は ``Document`` というデータモデルを宣言した場合の実装例です。

..  code-block:: java

    public class DocumentToCsv extends AbstractDocumentCsvExporterDescription {

        @Override
        public String getProfileName() {
            return "asakusa";
        }

        @Override
        public String getPath() {
            return "output.csv";
        }
    }

..  [#] :javadoc:`com.asakusafw.vocabulary.windgate.FsExporterDescription`
..  [#] :javadoc:`com.asakusafw.vocabulary.windgate.JdbcExporterDescription`
..  [#] :javadoc:`com.asakusafw.vocabulary.windgate.WindGateExporterDescription`

Direct I/Oを利用する
~~~~~~~~~~~~~~~~~~~~

Direct I/Oを利用してHadoopが管理するデータソースを入出力に利用する方法は、 :doc:`../directio/user-guide` を参照してください。

WindGateと連携する
~~~~~~~~~~~~~~~~~~

WindGateはCSVのほか、さまざまな形式のファイルやデータベースと連携できます。
詳しくは :doc:`../windgate/user-guide` を参照してください。

ジョブフロークラスの作成
------------------------

それぞれのジョブフローは、 ``FlowDescription`` [#]_ を継承したJavaのクラス(ジョブフロークラス)として宣言します [#]_ 。
さらにジョブフローであることを表すために、クラスの注釈として ``JobFlow`` [#]_ を指定し、要素 ``name`` にこのジョブフローの名前を指定します。

..  code-block:: java

    package com.example.jobflow;

    import com.asakusafw.vocabulary.flow.*;

    @JobFlow(name = "example")
    public class ExampleJobFlow extends FlowDescription {
        ...
    }

なお、それぞれのジョブフロークラスは、末尾の名前が ``jobflow`` であるようなパッケージに配置することを推奨しています。

..  [#] :javadoc:`com.asakusafw.vocabulary.flow.FlowDescription`
..  [#] その他、publicなトップレベルクラスであり、具象クラスである(  ``abstract`` を指定しない)、型引数を宣言しない、 ``FlowDescription`` 以外の親クラスや親インターフェースを指定しない、などの制約があります。
..  [#] :javadoc:`com.asakusafw.vocabulary.flow.JobFlow`

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

以下のように、コンストラクタの引数と同名のインスタンスフィールドを作成し、コンストラクタの引数をフィールドに代入するとよいでしょう。

..  code-block:: java

    package com.example.jobflow;

    import com.asakusafw.vocabulary.flow.*;

    @JobFlow(name = "example")
    public class ExampleJobFlow extends FlowDescription {

        In<Hoge> in;
        Out<Hoge> out;

        public ExampleJobFlow(
                @Import(name = "hoge", description = HogeFromCsv.class)
                In<Hoge> in,
                @Export(name = "hoge", description = HogeIntoCsv.class)
                Out<Hoge> out) {
            this.in = in;
            this.out = out;
        }
        ...
    }

..  [#] :javadoc:`com.asakusafw.vocabulary.flow.In`
..  [#] :javadoc:`com.asakusafw.vocabulary.flow.Import`
..  [#] :javadoc:`com.asakusafw.vocabulary.flow.Out`
..  [#] :javadoc:`com.asakusafw.vocabulary.flow.Export`

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

..  [#] :javadoc:`com.asakusafw.vocabulary.flow.util.CoreOperatorFactory`

入力と演算子を接続する
----------------------

コンストラクタに指定した ``In`` オブジェクトを、演算子ファクトリのメソッドの引数に渡すと、ジョブフローに入力されたデータを、その演算子で処理することができます。
このとき、入力されるデータの種類と、演算子に入力できるデータの種類は一致していなければなりません。

以下は、データモデル `Hoge` に対して更新演算子として定義した演算子メソッド `edit` を実行する例です。

..  code-block:: java

    In<Hoge> in;

    @Override
    public void describe() {
        ExampleOperatorFactory example = new ExampleOperatorFactory();
        Edit edit = example.edit(in);
    }

演算子と演算子を接続する
~~~~~~~~~~~~~~~~~~~~~~~~

演算子ファクトリの各メソッドが返すオブジェクトは、それぞれ対応する演算子を表しています。
このオブジェクトはそれぞれいくつかの公開フィールドを持っていて、演算子の出力を表しています。

演算子の出力を別の演算子の入力に接続することで、複雑なデータの流れを表現できます。

以下は、上記例で演算子メソッド `edit` を実行したデータモデル `Hoge` にして、分岐演算子として定義した演算子メソッド `select` を実行する例です。

..  code-block:: java

    In<Hoge> in;

    @Override
    public void describe() {
        ExampleOperatorFactory example = new ExampleOperatorFactory();
        Edit edit = example.edit(in);
        Select select = example.select(edit.out);
    }

演算子と出力を接続する
~~~~~~~~~~~~~~~~~~~~~~

ジョブフローの結果を出力する際には、コンストラクタに指定された ``Out`` オブジェクトの ``add()`` メソッドの引数に、それぞれの演算子の出力を渡します。
こうすることで、その演算子の出力結果がそのままジョブフローの出力結果となります。
このとき、両者の出力は同じデータの種類でなければなりません。

なお、それぞれの演算子の出力は、いずれかの演算子への入力、またはジョブフローからの出力と接続されている必要があります。
不要な演算子の出力がある場合、停止演算子（ ``CoreOperatorFactory.stop()`` メソッド）を利用してその出力を利用しないことを明示的にコンパイラに指示する必要があります。

以下の例では、上記例で演算子メソッド `select` を実行したデータモデル `Hoge` にして、分岐先の出力 `ok` をジョブフローの出力結果として出力しています。
また、分岐先の出力 `ng` は出力せず、ジョブフロー内でデータを破棄しています。

..  code-block:: java

    In<Hoge> in;
    In<Hoge> out;

    @Override
    public void describe() {
        CoreOperatorFactory core = new CoreOperatorFactory();
        ExampleOperatorFactory example = new ExampleOperatorFactory();
        Edit edit = example.edit(in);
        Select select = example.select(edit.out);
        out.add(select.ok);
        core.stop(select.ng);
    }

ジョブフローの実装例
--------------------
ジョブフローの実装例を示します。

この実装例では、これまでの説明と同様にWindGateを利用してCSVデータを読み書きします。
ここで紹介する例の完全なコードは、サンプルプログラム集 [#]_ に含まれるプロジェクト ``example-windgate-csv`` を参照してください。

..  [#] https://github.com/asakusafw/asakusafw-examples

インポート処理の実装例
~~~~~~~~~~~~~~~~~~~~~~

``example-windgate-csv`` のバッチ処理では、以下の3種類のデータをインポートしています。

* 店舗情報マスタ ( `StoreInfoFromCsv` )
* 商品情報マスタ ( `ItemInfoFromCsv` )
* 売上明細データ ( `SalesDetailFromCsv` )

まず、店舗情報のマスタデータである :file:`<ベースディレクトリ>/master/store_info.csv` にあるCSVファイルを読み出す例 ( `StoreInfoFromCsv` ) を以下に示します。
この ``<ベースディレクトリ>`` の部分はWindGateの設定で、既定では :file:`/tmp/windgate-<ログインユーザー名>` を利用します。

..  code-block:: java

    package com.asakusafw.example.csv.jobflow;

    import com.asakusafw.example.csv.modelgen.dmdl.csv.AbstractStoreInfoCsvImporterDescription;

    public class StoreInfoFromCsv extends AbstractStoreInfoCsvImporterDescription {

        @Override
        public String getProfileName() {
            return "asakusa";
        }

        @Override
        public String getPath() {
            return "master/store_info.csv";
        }

        @Override
        public DataSize getDataSize() {
            return DataSize.TINY;
        }
    }

`WindGateからインポートする`_ 際の手順に従い、自動生成されたクラスを継承して必要なメソッドを実装しています。

このとき、 ``getDataSize()`` メソッドは ``DataSize.TINY`` という値を返しています。
:file:`.../store_info.csv` は店舗情報のマスタデータを表すもので、それほど大きくないという前提です。

..  hint::
    データサイズに ``DataSize.TINY`` を指定することで、いくつかの最適化が有効になります。
    詳しくは :doc:`user-guide` を参照してください。

次に、商品情報のマスタデータとして :file:`<ベースディレクトリ>/master/item_info.csv` にあるCSVファイルを読み出す例 ( `ItemInfoFromCsv` ) です。

..  code-block:: java

    package com.asakusafw.example.csv.jobflow;

    import com.asakusafw.example.csv.modelgen.dmdl.csv.AbstractItemInfoCsvImporterDescription;

    public class ItemInfoFromCsv extends AbstractItemInfoCsvImporterDescription {

        @Override
        public String getProfileName() {
            return "asakusa";
        }

        @Override
        public String getPath() {
            return "master/item_info.csv";
        }

        @Override
        public DataSize getDataSize() {
            return DataSize.LARGE;
        }
    }

先ほどの例と異なり、 ``getDataSize()`` メソッドは ``DataSize.LARGE`` という値を返しています。

さらに、売上明細データとして :file:`<ベースディレクトリ>/sales/<日付>.csv` にあるCSVファイルを読み出す例  ( `SalesDetailFromCsv` ) です。
``<日付>`` の部分はバッチ処理を開始する際に `date` という名前の引数で指定できるようにしています。

..  code-block:: java

    package com.asakusafw.example.csv.jobflow;

    import com.asakusafw.example.csv.modelgen.dmdl.csv.AbstractSalesDetailCsvImporterDescription;

    public class SalesDetailFromCsv extends AbstractSalesDetailCsvImporterDescription {

        @Override
        public String getProfileName() {
            return "asakusa";
        }

        @Override
        public String getPath() {
            return "sales/${date}.csv";
        }

        @Override
        public DataSize getDataSize() {
            return DataSize.LARGE;
        }
    }

エクスポート処理の実装例
~~~~~~~~~~~~~~~~~~~~~~~~

`インポート処理の実装例`_ と同様に、エクスポート処理の部分の実装例を紹介します。

``example-windgate-csv`` のバッチ処理では、以下の2種類のデータをエクスポートしています。

* カテゴリ別売上集計 ( `CategorySummaryToCsv` )
* エラー情報 ( `ErrorRecordToCsv` )


カテゴリ別売上集計を :file:`<ベースディレクトリ>/result/category-<日付>.csv` にCSV形式で書き出す例 ( `CategorySummaryToCsv` ) です。
``<日付>`` の部分は売上明細データをインポートする際と同様に、バッチ処理を開始する際の `date` で指定された文字列を利用します。

..  code-block:: java

    package com.asakusafw.example.csv.jobflow;

    import com.asakusafw.example.csv.modelgen.dmdl.csv.AbstractCategorySummaryCsvExporterDescription;

    public class CategorySummaryToCsv extends AbstractCategorySummaryCsvExporterDescription {

        @Override
        public String getProfileName() {
            return "asakusa";
        }

        @Override
        public String getPath() {
            return "result/category-${date}.csv";
        }
    }

上記は、 `WindGateにエクスポートする`_ 際の手順に従い、自動生成されたクラスを継承して必要なメソッドを実装しています。

エラー情報もカテゴリ別売上集計と同様の形で :file:`<ベースディレクトリ>/result/error-<日付>.csv` にCSV形式で書き出します ( `ErrorRecordToCsv` )。

..  code-block:: java

    package com.asakusafw.example.csv.jobflow;

    import com.asakusafw.example.csv.modelgen.dmdl.csv.AbstractErrorRecordCsvExporterDescription;

    public class ErrorRecordToCsv extends AbstractErrorRecordCsvExporterDescription {

        @Override
        public String getProfileName() {
            return "asakusa";
        }

        @Override
        public String getPath() {
            return "result/error-${date}.csv";
        }
    }

ジョブフロー本体の実装例
~~~~~~~~~~~~~~~~~~~~~~~~

最後にジョブフローの例を示します。

..  code-block:: java

    package com.asakusafw.example.csv.jobflow;

    import com.asakusafw.example.csv.modelgen.dmdl.model.*;
    import com.asakusafw.example.csv.operator.CategorySummaryOperatorFactory;
    import com.asakusafw.example.csv.operator.CategorySummaryOperatorFactory.*;
    import com.asakusafw.vocabulary.flow.*;
    import com.asakusafw.vocabulary.flow.util.*;

    /**
     * カテゴリ別に売上の集計を計算する。
     */
    @JobFlow(name = "byCategory")
    public class CategorySummaryJob extends FlowDescription {

        final In<SalesDetail> salesDetail;

        final In<StoreInfo> storeInfo;

        final In<ItemInfo> itemInfo;

        final Out<CategorySummary> categorySummary;

        final Out<ErrorRecord> errorRecord;

        /**
         * ジョブフローインスタンスを生成する。
         * @param salesDetail 売上明細
         * @param storeInfo 店舗マスタ
         * @param itemInfo 商品マスタ
         * @param categorySummary カテゴリ別集計結果
         * @param errorRecord エラーレコード
         */
        public CategorySummaryJob(
                @Import(name = "salesDetail", description = SalesDetailFromCsv.class)
                In<SalesDetail> salesDetail,
                @Import(name = "storeInfo", description = StoreInfoFromCsv.class)
                In<StoreInfo> storeInfo,
                @Import(name = "itemInfo", description = ItemInfoFromCsv.class)
                In<ItemInfo> itemInfo,
                @Export(name = "categorySummary", description = CategorySummaryToCsv.class)
                Out<CategorySummary> categorySummary,
                @Export(name = "errorRecord", description = ErrorRecordToCsv.class)
                Out<ErrorRecord> errorRecord) {
            this.salesDetail = salesDetail;
            this.storeInfo = storeInfo;
            this.itemInfo = itemInfo;
            this.categorySummary = categorySummary;
            this.errorRecord = errorRecord;
        }

        @Override
        protected void describe() {
            CoreOperatorFactory core = new CoreOperatorFactory();
            CategorySummaryOperatorFactory operators = new CategorySummaryOperatorFactory();

            // 店舗コードが妥当かどうか調べる
            CheckStore checkStore = operators.checkStore(storeInfo, salesDetail);

            // 売上に商品情報を載せる
            JoinItemInfo joinItemInfo = operators.joinItemInfo(itemInfo, checkStore.found);

            // 売上をカテゴリ別に集計
            SummarizeByCategory summarize = operators.summarizeByCategory(joinItemInfo.joined);

            // 集計結果を出力
            categorySummary.add(summarize.out);

            // 存在しない店舗コードでの売上はエラー
            SetErrorMessage unknownStore = operators.setErrorMessage(
                    core.restructure(checkStore.missed, ErrorRecord.class),
                    "店舗不明");
            errorRecord.add(unknownStore.out);

            // 商品情報が存在しない売上はエラー
            SetErrorMessage unknownItem = operators.setErrorMessage(
                    core.restructure(joinItemInfo.missed, ErrorRecord.class),
                    "商品不明");
            errorRecord.add(unknownItem.out);
        }
    }
..  **

ジョブフローのテスト
--------------------

ジョブフローのテストは、Asakusa Frameworkが提供するテストドライバーを利用して行います。

詳しくは、 :doc:`../testing/start-guide` を参照して下さい。


バッチを記述する
================

バッチはこれまでに紹介したジョブフローをワークフローの形式で組み合わせて、一連の処理を実現するための構造です。

バッチクラスの作成
------------------

それぞれのバッチは、 ``BatchDescription`` [#]_ を継承したJavaのクラス(バッチクラス)として宣言します [#]_ 。
また、付加情報を表すために、クラスの注釈として ``Batch`` [#]_ を指定して要素 ``name`` にこのバッチの名前を指定します。

以下はバッチクラスを作成する例です。

..  code-block:: java

    package com.example.batch;

    import com.asakusafw.vocabulary.batch.*;

    @Batch(name = "example")
    public class ExampleBatch extends BatchDescription {
        ...
    }

なお、それぞれのバッチクラスは、末尾の名前が ``batch`` であるようなパッケージに配置することを推奨しています。

..  [#] :javadoc:`com.asakusafw.vocabulary.batch.BatchDescription`
..  [#] その他、publicなトップレベルクラスであり、具象クラスである( ``abstract`` を指定しない)、型引数を宣言しない、明示的な親クラスや親インターフェースを指定しない、明示的なコンストラクタを宣言しない、などの制約があります。
..  [#] :javadoc:`com.asakusafw.vocabulary.batch.Batch`

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

``soon`` メソッドはバッチの内部で最初に実行されるジョブフローを表し、 ``after`` メソッドは依存関係にある処理を引数に指定して、それらの処理が全て完了後に実行されるジョブフローを表します。

バッチの実装例
--------------

バッチの単純な例を示します。
ここで紹介する例の完全なコードも、サンプルプログラム集 [#]_ に含まれるプロジェクト ``example-windgate-csv`` にあります。

以下の例は非常に簡単なバッチで、 ``TutorialJob`` というジョブフローを実行するのみです。
また、バッチの名前には ``example.summarizeSales`` を指定しています。

..  code-block:: java

    package com.asakusafw.example.csv.batch;

    import com.asakusafw.example.csv.jobflow.CategorySummaryJob;
    import com.asakusafw.vocabulary.batch.Batch;
    import com.asakusafw.vocabulary.batch.BatchDescription;

    @Batch(name = "example.summarizeSales")
    public class SummarizeBatch extends BatchDescription {

        @Override
        protected void describe() {
            run(CategorySummaryJob.class).soon();
        }
    }

..  [#] https://github.com/asakusafw/asakusafw-examples

バッチアプリケーションを生成する
================================

Asakusa DSLからバッチアプリケーションを生成するには、 Gradle利用してAsakusa DSLコンパイラを実行します。

これはGradleの :program:`compileBatchapp` タスクで起動するので、プロジェクト内で以下のようにコマンドを実行します。

..  code-block:: sh

    ./gradlew compileBatchapp

その他、 :program:`jarBatchapp` タスクや :program:`assemble` タスク、 :program:`build` タスクなどでも自動的にコンパイラが起動します。

バッチアプリケーションの生成方法やGradleの利用方法については、 :doc:`../application/gradle-plugin` などを参照してください。

バッチアプリケーションを実行する
================================

作成したバッチアプリケーションの実行方法は、 :doc:`../yaess/start-guide` などを参照してください。
