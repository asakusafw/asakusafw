====================================
アプリケーションテストスタートガイド
====================================

この文書では、asakusa-archetype-batchappを利用したプロジェクト構成で、
Asakusa Frameworkを使ったバッチアプリケーションをテストする方法について簡単に紹介します。

asakusa-archetype-batchappの利用方法については :doc:`../application/maven-archetype` を参照してください。
また、テストのより詳しい情報は :doc:`user-guide` を参照して下さい。

演算子のテスト
==============
演算子のテストは、演算子メソッドに対する通常の単体テストとして記述します。
ただし、演算子クラスは抽象クラスとして宣言しているため、
クラス名の末尾に ``Impl`` を付与したクラス [#]_ を代わりにインスタンス化します。

..  code-block:: java

    @Test
    public void testCheckShipment_shipped() {
        StockOpImpl operator = new StockOpImpl();
        Shipment shipment = new Shipment();
        shipment.setShippedDate(new DateTime());
        shipment.setCost(100);

        ShipmentStatus actual = operator.checkShipment(shipment);

        assertThat("COSTが指定されていたらCOMPLETED",
                actual, is(ShipmentStatus.COMPLETED));
    }

..  warning::
    上記の手順だけではフレームワークAPIを利用した演算子メソッドをテストできません。
    フレームワークAPIについては :doc:`../dsl/user-guide` を、
    そのテスト方法については :doc:`user-guide` をそれぞれ参照してください。

..  [#] このクラスは「演算子実装クラス」と呼ばれ、Opeerator DSLコンパイラが自動的に生成します。
    詳しくは :doc:`../dsl/start-guide` を参照してください。

結果型を利用する演算子のテスト
------------------------------
いくつかの演算子メソッドでは、出力を表す ``Result`` [#]_ 型のオブジェクトを引数に取ります。
これを利用するメソッドのテストには、モック実装の ``MockResult`` [#]_ が便利です。

..  code-block:: java

    @Test
    public void testCutoff_shortage() {
        StockOpImpl operator = new StockOpImpl();

        List<Stock> stocks = Arrays.asList(StockFactory.create(new DateTime(), 0, 100, 10));
        List<Shipment> shipments = Arrays.asList();
        MockResult<Stock> newStocks = new MockResult<Stock>();
        MockResult<Shipment> newShipments = new MockResult<Shipment>();

        operator.cutoff(stocks, shipments, newStocks, newShipments);

        assertThat(newStocks.getResults().size(), is(1));
        assertThat(newShipments.getResults().size(), is(0));
    }

``MockResult`` はメソッド ``add`` で追加されたオブジェクトをメモリ上に保持します。
同じオブジェクトを使いまわして結果を出力するメソッドでは、
次のように ``MockResult`` のメソッド ``bless`` をオーバーライドして、
独自にコピーを作成します。

..  code-block:: java

    MockResult<Stock> newStocks = new MockResult<Stock>() {
        @Override protected Stock bless(Stock obj) {
            Stock copy = new Stock();
            copy.copyFrom(obj);
            return copy;
        }
    };

なお、結果型を引数に指定する演算子については :doc:`../dsl/operators` を参照してください。

..  [#] ``com.asakusafw.runtime.core.Result``
..  [#] ``com.asakusafw.runtime.testing.MockResult``


データフローのテスト
====================
データフローやバッチのテストは、DSLのコンパイラや
実行環境であるHadoopと連携して行います。
Asakusa Frameworkはこの一連の処理を自動的に行う
テストドライバというモジュールを含んでいます。

テストドライバはテスト対象の要素に対して、次の一連の処理を行います。

#. 入力データを初期化する
#. 入力データを流し込む
#. 対象のプログラムをテスト実行する
#. 出力結果を取り込む
#. 出力結果と期待データを検証する


テストデータの準備
------------------
テストドライバでのテストを行うには、次の3種類の情報を用意します。

入力データ
    それぞれのデータフローの入力に指定するデータセット。
    データモデルオブジェクトのリストと同じ構造。
期待データ
    それぞれのデータフローからの出力に期待するデータセット。
    入力データと同じ構造。
出力の検証方法
    それぞれの出力と期待データを比較して間違いを見つける方法。

ここでは、これらをまとめて「テストデータ」と呼ぶことにします。

テストドライバはテストデータをさまざまな形式で記述できますが、
ここでは初めて利用する際に理解のしやすいExcel形式での準備方法を紹介します。

テストデータテンプレートの生成
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
テストデータをExcelで記述する場合、そのテンプレートを自動生成して利用します。
このテンプレートはデータモデルごとに生成され、それぞれ次のようなシートが含まれます。

入力データシート
    入力データを記述するシート。
    データモデルをシートの1行で表し、カラムごとにプロパティの値を記載できる。
    テンプレートではプロパティ名のヘッダのみが記載されている。

    ..  figure:: shipment-input.png

        入力データシートの例
期待データシート
    期待する出力データを記述するシート。
    入力データシートと同じ構造。
比較条件シート
    出力結果データと期待データの比較条件を記述するシート。
    それぞれのプロパティをどのように比較するかをドロップダウン形式で選択できる。

    ..  figure:: shipment-rule.png

        比較条件シートの例

テストデータのテンプレートを生成するには、mvnコマンドを利用して生成ツールを実行します。
これはMavenの ``generate-sources`` フェーズで自動的に起動しますので、プロジェクト内で以下のようにコマンドを実行します。

..  code-block:: sh

    mvn generate-sources

このコマンドを実行すると、プロジェクトの ``target/excel`` 以下に
データモデルごとにExcelのファイルが生成されます。
このファイルには、上記の3種類のシートが含められます。

なお、このテンプレートはDMDLで記述されたデータモデルを元に作成しています。
DMDLに利用方法は `../dmdl/start-guide` を参照してください。
また、ThunderGate向けのデータモデルを生成する方法は、
`../dmdl/with-thundergate` を参照してください。

入力、期待データの作成
~~~~~~~~~~~~~~~~~~~~~~
入力データを作成するには、生成したExcelファイルの ``input`` という名前のシートを編集します。
このシートの1行目には、データモデルに定義したプロパティの名前が記載されているはずです。
それぞれの行にオブジェクトごとのプロパティを入力してください。

期待データを作成するには、同様に ``output`` という名前のシートを編集して下さい。

..  note::
    セルを空にした場合、その値は ``null`` として取り扱われます。

..  attention::
    文字列型のプロパティを編集する際には注意が必要です。
    数値、日付、論理値などの値を指定したセルや、空のセルは文字列として取り扱われません。
    これらの値を利用したい場合には、セルを ``'`` から始めて文字列を指定してください。

テスト条件の記述
~~~~~~~~~~~~~~~~
Excelファイルのテストデータテンプレートを利用する場合、
出力データと期待データは次のように比較されます。

#. 各レコードのキーとなるプロパティをもとに、出力データと期待データのペアを作る
#. 出力と期待データのペアの中で、プロパティを条件に従って比較する
#. ペアを作れなかった出力データまたは期待データは、条件に従って比較する

上記のキープロパティ(1)、プロパティの比較(2)、全体の比較(3)はそれぞれ
生成したExcelファイルの ``rule`` という名前のシートで指定できます。

レコードのキーを指定する場合には、対象プロパティの「値の比較」という項目に ``検査キー[Key]`` を選択します。
キーとならないプロパティは、「値の比較」や「NULLの比較」をそれぞれ選択してください。
プロパティを比較しない場合には、「値の比較」に ``検査対象外[-]`` を、「NULLの比較」に ``通常比較[-]`` を
それぞれ選択します。

出力と期待データのペアを作れなかった場合の動作は、シート上部の「全体の比較」で選択します。

上記についての詳しい情報は、 :doc:`user-guide` を参照してください。


テストデータの配置
~~~~~~~~~~~~~~~~~~
作成したテストデータは、単体テストと同じパッケージ上に配置します。
asakusa-archetype-batchappを利用したプロジェクトでは、
``src/test/resources/<パッケージ>`` 以下に配置してください。


テストの実行
------------
`テストデータの準備`_ を完了したら、それぞれのデータフローをテストします。
ここでは、テストハーネスに `JUnit`_ を利用した場合のテスト方法を紹介します。
いずれの場合も、テスト対象のクラスに対応するテストクラスを作成してください。　

..  _`JUnit`: http://www.junit.org/

フロー部品のテスト
~~~~~~~~~~~~~~~~~~
フロー部品をテストするには、 ``FlowPartTester`` [#]_ を利用します。

..  code-block:: java

    @Test
    public void testExampleAsFlowPart() {
        FlowPartTester tester = new FlowPartTester(getClass());
        In<Shipment> shipmentIn = tester.input("shipment", Shipment.class)
            .prepare("shipment.xls#input");
        In<Stock> stockIn = tester.input("stock", Stock.class)
            .prepare("stock.xls#input");
        Out<Shipment> shipmentOut = tester.output("shipment", Shipment.class)
            .verify("shipment.xls#output", "shipment.xls#rule");
        Out<Stock> stockOut = tester.output("stock", Stock.class)
            .verify("stock.xls#output", "stock.xls#rule");

        FlowDescription flowPart = new StockJob(shipmentIn, stockIn, shipmentOut, stockOut);
        tester.runTest(flowPart);
    }

``FlowPartTester`` をインスタンス化する際には、
引数に ``getClass()`` を指定してテストケース自身のクラスを引き渡します。
これは、先ほど配置したテストデータを検索するなどに利用しています。

..  code-block:: java

    FlowPartTester tester = new FlowPartTester(getClass());

入力を定義するには、 ``input`` メソッドを利用します。
この引数には入力の名前 [#]_ と、入力のデータモデル型を指定します。

``input`` に続けて、 ``prepare`` で入力データを指定します。
引数には先ほど配置したテストデータを
``<Excelのファイル名>#<シート名>`` 
という形式で指定します。

上記の一連の結果を、 ``In<データモデル型>`` [#]_ の変数に保持します。

..  code-block:: java

    In<Shipment> shipmentIn = tester.input("shipment", Shipment.class)
        .prepare("shipment.xls#input");
    In<Stock> stockIn = tester.input("stock", Stock.class)
        .prepare("stock.xls#input");

出力を定義するには、 ``output`` メソッドを利用します。
この引数は入力と同様に名前とデータモデル型を指定します。

``output`` に続けて、 ``verify`` で期待データとテスト条件をそれぞれ指定します。
指定方法は入力データと同様です。

出力の定義結果は、 ``Out<データモデル型>`` [#]_ の変数に保存します。

..  code-block:: java

    Out<Shipment> shipmentOut = tester.output("shipment", Shipment.class)
        .verify("shipment.xls#output", "shipment.xls#rule");
    Out<Stock> stockOut = tester.output("stock", Stock.class)
        .verify("stock.xls#output", "stock.xls#rule");

入出力の定義が終わったら、フロー部品クラスを直接インスタンス化します。
このときの引数には、先ほど作成した入出力のオブジェクトを利用して下さい。
このインスタンスを ``runTest`` メソッドに渡すと、
テストデータに応じたテストを自動的に実行します。

..  code-block:: java

    In<Shipment> shipmentIn = ...;
    In<Stock> stockIn = ...;
    Out<Shipment> shipmentOut = ...;
    Out<Stock> stockOut = ...;
    FlowDescription flowPart = new StockJob(shipmentIn, stockIn, shipmentOut, stockOut);
    tester.runTest(flowPart);

..  [#] ``com.asakusafw.testdriver.FlowPartTester``
..  [#] ここの名前は他の名前と重複せず、アルファベットや数字のみで構成して下さい
..  [#] ``com.asakusafw.vocabulary.flow.In``
..  [#] ``com.asakusafw.vocabulary.flow.Out``

ジョブフローのテスト
~~~~~~~~~~~~~~~~~~~~
ジョブフローをテストするには、 ``JobFlowTester`` [#]_ を利用します。

..  code-block:: java

    @Test
    public void testExample() {
        JobFlowTester tester = new JobFlowTester(getClass());
        tester.input("shipment", Shipment.class)
            .prepare("shipment.xls#input");
        tester.input("stock", Stock.class)
            .prepare("stock.xls#input");
        tester.output("shipment", Shipment.class)
            .verify("shipment.xls#output", "shipment.xls#rule");
        tester.output("stock", Stock.class)
            .verify("stock.xls#output", "stock.xls#rule");
        tester.runTest(StockJob.class);
    }

利用方法は `フロー部品のテスト`_ とほぼ同様ですが、以下の点が異なります。

* 入出力の名前には、ジョブフローの注釈 ``Import`` や ``Export`` の ``name`` に指定した値を利用する
* 入出力を ``In`` や ``Out`` に保持しない
* ``runTest`` メソッドにはジョブフロークラス( ``.class`` )を指定する

..  [#] ``com.asakusafw.testdriver.JobFlowTester``

バッチのテスト
~~~~~~~~~~~~~~
バッチをテストするには、 ``BatchTester`` [#]_ を利用します。

..  code-block:: java

    @Test
    public void testExample() {
        BatchTester tester = new BatchTester(getClass());
        tester.jobflow("stock").input("shipment", Shipment.class)
            .prepare("shipment.xls#input");
        tester.jobflow("stock").input("stock", Stock.class)
            .prepare("stock.xls#input");
        tester.jobflow("stock").output("shipment", Shipment.class)
            .verify("shipment.xls#output", "shipment.xls#rule");
        tester.jobflow("stock").output("stock", Stock.class)
            .verify("stock.xls#output", "stock.xls#rule");
        tester.runTest(StockBatch.class);
    }

利用方法は `ジョブフローのテスト`_ とほぼ同様ですが、以下の点が異なります。

* 入出力を指定する前に、それらを定義したジョブフローのID [#]_ を指定する
* ``runTest`` メソッドにはバッチクラス( ``.class`` )を指定する

..  [#] ``com.asakusafw.testdriver.BatchTester``
..  [#] 注釈 ``@JobFlow`` の ``name`` に指定した文字列を利用して下さい
