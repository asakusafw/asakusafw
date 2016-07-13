==============================
テストドライバーユーザーガイド
==============================

この文書では、Asakusa Frameworkを使ったバッチアプリケーションをテストする方法について紹介します。

演算子のテスト
==============

演算子のテストは、演算子メソッドに対する通常の単体テストとして記述します。
ただし、演算子クラスは抽象クラスとして宣言しているため、クラス名の末尾に ``Impl`` を付与したクラス [#]_ を代わりにインスタンス化します。

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

..  [#] このクラスは「演算子実装クラス」と呼ばれ、Operator DSLコンパイラが自動的に生成します。
    詳しくは :doc:`../dsl/user-guide` を参照してください。

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
同じオブジェクトを使いまわして結果を出力するメソッドでは、次のように ``MockResult`` のメソッド ``bless`` をオーバーライドして、独自にコピーを作成します。

..  code-block:: java

    MockResult<Stock> newStocks = new MockResult<Stock>() {
        @Override protected Stock bless(Stock obj) {
            Stock copy = new Stock();
            copy.copyFrom(obj);
            return copy;
        }
    };

なお、結果型を引数に指定する演算子については :doc:`../dsl/operators` を参照してください。

..  [#] :javadoc:`com.asakusafw.runtime.core.Result`
..  [#] :javadoc:`com.asakusafw.runtime.testing.MockResult`

演算子テストの補助
------------------

いくつかの演算子のテストには、 ``OperatorTestEnvironment`` [#]_ クラスを利用します。
このクラスはAsakusaのフレームワークAPIをテスト時にエミュレーションするためのもので、フレームワークAPIを利用する演算子をテストする場合には必須です。

``OperatorTestEnvironment`` クラスは、テストクラスの ``public`` フィールドに ``@Rule`` [#]_ という注釈を付けてインスタンス化します。

..  code-block:: java

    // 必ずpublicで宣言し、インスタンスを代入する
    @Rule
    public OperatorTestEnvironment resource = new OperatorTestEnvironment();

..  [#] :javadoc:`com.asakusafw.testdriver.OperatorTestEnvironment`
..  [#] ``org.junit.Rule``

コンテキストAPIを利用する演算子のテスト
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

テスト対象の演算子がコンテキストAPI [#]_ を利用する場合、コンテキストAPIが参照するバッチの起動引数をテスト側で指定します。

バッチ起動引数の指定は、 ``OperatorTestEnvironment`` クラスの ``setBatchArg`` メソッドで行います。
``setBatchArg`` メソッドは第一引数に変数名、第二引数に変数の値を指定します。
すべてのバッチ起動引数を指定したら、同クラスの ``reload`` メソッドで設定を有効化します。

..  code-block:: java

    @Rule
    public OperatorTestEnvironment resource = new OperatorTestEnvironment();

    @Test
    public void sometest() {
        resource.setBatchArg("key1", "value1");
        resource.setBatchArg("key2", "value2");
        ...
        resource.reload();

        // ここにテストを書く
    }

..  attention::
    演算子メソッドに対する操作は必ず ``reload`` メソッドの呼出し後に記述してください。

..  [#] コンテキストAPIについては、 :doc:`../dsl/user-guide` - :ref:`dsl-context-api` を参照してください。

実行時プラグインの設定
~~~~~~~~~~~~~~~~~~~~~~

テスト対象の演算子で実行時プラグイン [#]_ を利用する場合、「実行時プラグイン設定ファイル」が必要になります。
これは利用する実行時プラグインや、それぞれのプラグインの設定を記述したもので、 ``OperatorTestEnvironment`` クラスをインスタンス化する際に位置を指定できます。

..  code-block:: java

    @Rule
    public OperatorTestEnvironment resource =
        new OperatorTestEnvironment("conf/asakusa-test-resources.xml");

ここに指定する位置は、クラスパス上の位置です。

引数を指定せずに ``OperatorTestEnvironment`` クラスをインスタンス化した場合には、クラスパスルートの ``asakusa-resources.xml`` というファイルを利用します。
このファイルがない場合、最低限の設定のみを自動的に行います。

その他、 ``OperatorTestEnvironment`` クラスの ``configure`` メソッドを利用して個々のプラグインの設定を行うことも可能です。
``configure`` メソッドは第一引数にプロパティ名、第二引数にプロパティの値を指定します。

..  code-block:: java

    @Rule
    public OperatorTestEnvironment resource = new OperatorTestEnvironment(...);

    @Test
    public void sometest() {
        resource.configure(
            "com.asakusafw.runtime.core.Report.Delegate",
            "com.asakusafw.runtime.core.Report$Default");
        ...
        resource.reload();

        // ここにテストを書く
    }


..  attention::
    演算子メソッドに対する操作は必ず ``reload`` メソッドの呼出し後に記述してください。

..  [#] 実行時プラグインについては、 :doc:`../administration/deployment-runtime-plugins` を参照してください。

.. _testing-userguide-dataflow-test:

データフローのテスト
====================

データフローやバッチのテストは、DSLのコンパイラや実行環境と連携してテストを実行します。
Asakusa Frameworkはこの一連の処理を自動的に行うテストドライバーというモジュールを含んでいます。

テストドライバーはテスト対象の要素に対して、次の一連の処理を行います。

#. 入力データを初期化する
#. 入力データを流し込む
#. 対象のプログラムをテスト実行する
#. 出力結果を取り込む
#. 出力結果と期待データを検証する

テストドライバーの動作モード
----------------------------

テストドライバーを利用するテストでは、以下の2種類の動作モードを利用することができます。

1. エミュレーションモードを利用したテスト
2. Hadoop環境上でのテスト

エミュレーションモードを利用したテスト
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

エミュレーションモードを利用したテストでは、Asakusa Frameworkが提供する独自の実行エンジン「スモールジョブ実行エンジン」上でテストを実行します。
通常はこのモードでテストを実行することを推奨します。

Asakusa Framework バージョン 0.8.0 以降では、:doc:`../introduction/start-guide` や :doc:`../application/gradle-plugin` などで説明しているプロジェクトテンプレートを利用する場合、
標準でエミュレーションモードが利用されます。

少量のデータを扱う際には、エミュレーションモードを利用したテストはHadoop環境上でのテストに比べて、多くの場合高速に実行することができます。
また開発環境に対するHadoopの設定は不要で、WindowsなどのHadoopが対応していないプラットフォーム上でテストを実行することも可能です。

エミュレーションモードについて詳しくは、以下のドキュメントを参照してください。

* :doc:`emulation-mode`

Hadoop環境上でのテスト
~~~~~~~~~~~~~~~~~~~~~~

Hadoop環境上によるテストでは、テストドライバーを実行する環境にインストールされたHadoopを利用してアプリケーションを実行します。

開発環境では、Hadoopを「スタンドアロンモード」と呼ばれる分散処理を行わず単一JVM上で実行するモードで利用することを想定しています。
この動作モードを利用する場合にはテストドライバーを実行する環境にHadoopのインストールと設定が必要です。

開発環境に対するHadoop環境のセットアップについては、以下のドキュメントを参照してください。

* :doc:`../application/using-hadoop`

テストデータの作成
------------------

テストドライバーでのテストを行うには、次の3種類の情報を用意します。

入力データ
  それぞれのデータフローの入力に指定するデータセット。
  データモデルオブジェクトのリストと同じ構造。

期待データ
  それぞれのデータフローからの出力に期待するデータセット。
  入力データと同じ構造。

出力の検証方法
  それぞれの出力と期待データを比較して間違いを見つける方法。

ここでは、これらをまとめて「テストデータ」と呼ぶことにします。

テストドライバーはテストデータをさまざまな形式で記述できます。
Asakusa Frameworkが標準でサポートしているのは以下の3種類です。

* :doc:`using-excel`
* :doc:`using-json`
* `Javaオブジェクトによるテストデータ定義`_

それぞれの形式におけるテストデータの作成方法は上記のドキュメントを参照してください。

テストデータの配置
~~~~~~~~~~~~~~~~~~

:doc:`using-excel` や :doc:`using-json` で作成したテストデータは、それを利用するテストと同じパッケージか、そのサブパッケージ上に配置します。

複数のテストから利用されるテストデータを、任意のパッケージに配置することもできます。
この場合、テストデータの指定時にクラスパスからの絶対パスを指定する必要があります。

テストの実行
------------

`テストデータの作成`_ を完了したら、それぞれのデータフローをテストします。

ここでは、テストハーネスに `JUnit`_ を利用し、テストデータに :doc:`using-excel` を利用した場合のテスト方法を紹介します。
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

``FlowPartTester`` をインスタンス化する際には、引数に ``getClass()`` を指定してテストケース自身のクラスを引き渡します。
これは、先ほど配置したテストデータを検索するなどに利用しています。

..  code-block:: java

    FlowPartTester tester = new FlowPartTester(getClass());

入力を定義するには、 ``input`` メソッドを利用します。
この引数には入力の名前 [#]_ と、入力のデータモデル型を指定します。

``input`` に続けて、 ``prepare`` で入力データを指定します。
引数には先ほど配置したテストデータを、以下のいずれかで指定します。

* パッケージからの相対パス
* クラスパスからの絶対パス ( ``/`` から始める )

サブパッケージ ``a.b`` などに配置している場合には、 ``a/b/file.xls#hoge`` のように ``/`` で区切って指定します。

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

テスト条件を詳細に定義したい場合、テスト条件をJavaで指定することもできます。
テスト条件をJavaで直接記述する場合の方法は、 `テスト条件をJavaで記述する`_ や `テスト条件をJavaで拡張する`_ を参照してください。

出力の定義結果は、 ``Out<データモデル型>`` [#]_ の変数に保存します。

..  code-block:: java

    Out<Shipment> shipmentOut = tester.output("shipment", Shipment.class)
        .verify("shipment.xls#output", "shipment.xls#rule");
    Out<Stock> stockOut = tester.output("stock", Stock.class)
        .verify("stock.xls#output", "stock.xls#rule");

なお、 ``input`` と同様に ``output`` でも初期データの指定を行えます。
利用方法は ``input`` の ``prepare`` と同様です。

..  hint::
    「出力に初期データがある場合」のテストでは、出力に対して ``prepare`` を実行します。

入出力の定義が終わったら、フロー部品クラスを直接インスタンス化します。
このときの引数には、先ほど作成した入出力のオブジェクトを利用して下さい。
このインスタンスを ``runTest`` メソッドに渡すと、テストデータに応じたテストを自動的に実行します。

..  code-block:: java

    In<Shipment> shipmentIn = ...;
    In<Stock> stockIn = ...;
    Out<Shipment> shipmentOut = ...;
    Out<Stock> stockOut = ...;
    FlowDescription flowPart = new StockJob(shipmentIn, stockIn, shipmentOut, stockOut);
    tester.runTest(flowPart);

..  [#] :javadoc:`com.asakusafw.testdriver.FlowPartTester`
..  [#] ここの名前は他の名前と重複せず、アルファベットや数字のみで構成して下さい
..  [#] :javadoc:`com.asakusafw.vocabulary.flow.In`
..  [#] :javadoc:`com.asakusafw.vocabulary.flow.Out`

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

..  [#] :javadoc:`com.asakusafw.testdriver.JobFlowTester`

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

* 入出力を指定する前に、 ``jobflow`` メソッドを経由して入出力を利用するジョブフローのID [#]_ を指定する
* ``runTest`` メソッドにはバッチクラス( ``.class`` )を指定する

..  [#] :javadoc:`com.asakusafw.testdriver.BatchTester`
..  [#] 注釈 ``@JobFlow`` の ``name`` に指定した文字列を利用して下さい

出力結果を保存する
------------------

テスト時の出力結果を保存するには、対象の出力に対して ``dumpActual("<出力先>")`` を指定します。

..  code-block:: java

    Out<Shipment> shipmentOut = tester.output("shipment", Shipment.class)
        .dumpActual("build/dump/actual.xls")
        .verify("shipment.xls#output", "shipment.xls#rule");

出力先には、ファイルパスや ``File`` [#]_ オブジェクトを指定できます。
ファイルパスで相対パスを指定した場合、テストを実行したワーキングディレクトリからの相対パス上に結果が出力されます。

..  attention::
    EclipseなどのIDEを利用している場合、ファイルが出力された後にワークスペースの表示更新やリフレッシュなどを行うまで、出力されたファイルが見えない場合があります。

また、出力先に指定したファイル名の拡張子に応じた形式で出力が行われます。
標準ではExcelシートを出力する ``.xls`` または ``.xlsx`` を指定できます。

この操作は、 ``verify()`` と組み合わせて利用することもできます。

..  code-block:: java

    Out<Shipment> shipmentOut = tester.output("shipment", Shipment.class)
        .dumpActual("build/dump/actual.xls")
        .verify("shipment.xls#output", "shipment.xls#rule");

..  [#] ``java.io.File``

比較結果を保存する
------------------

出力されたデータの比較結果を保存するには、対象の出力に対して ``dumpDifference(<出力先>)`` を指定します。

..  code-block:: java

    Out<Shipment> shipmentOut = tester.output("shipment", Shipment.class)
        .verify("shipment.xls#output", "shipment.xls#rule")
        .dumpDifference("build/dump/difference.html");

`出力結果を保存する`_ と同様に、出力先にはファイルパスや ``File`` オブジェクトを指定できます。
ファイルパスで相対パスを指定した場合、テストを実行したワーキングディレクトリからの相対パス上に結果が出力されます。

また、出力先に指定したファイル名の拡張子に応じた形式で出力が行われます。
標準ではHTMLファイルを出力する ``.html`` を指定できます。

..  attention::
    この操作は、 ``verify()`` と組み合わせて指定してください。
    ``verify()`` の指定がない場合、比較結果の保存は行われません。
    また、比較結果に差異がない場合には比較結果は保存されません。

Javaオブジェクトによるテストデータ定義
--------------------------------------

ここではテストデータをJavaで記述する方法について紹介します。

入力データと期待データをJavaで記述する
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

入力データや期待データをJavaで定義するには、 `テストの実行`_ で紹介したテストドライバーAPIの ``input.prepare()`` メソッドや ``output.verify()`` メソッドでテスト対象となるデータモデル型のデータモデルオブジェクトを保持するコレクションを指定します。

..  code-block:: java

    List<Shipment> shipments = new ArrayList<Shipment>();

    Shipment ship1 = new Shipment();
    ship1.setItemCode(1001);
    ship1.setShippedDate(DateTime.valueOf("20110102000000", Format.SIMPLE));
    shipments.add(ship1)

    Shipment ship2 = new Shipment();
    ship2.setItemCode(1002);
    ship2.setShippedDate(DateTime.valueOf("20110103000000", Format.SIMPLE));
    shipments.add(ship2)

    In<Shipment> shipmentIn = tester.input("shipment", Shipment.class)
        .prepare(shipments);

テスト条件をJavaで記述する
~~~~~~~~~~~~~~~~~~~~~~~~~~

テスト条件は期待データと実際の結果を突き合わせるためのルールを示したもので、Javaで直接記述することも可能です。

テスト条件をJavaで記述するには、 ``ModelVerifier`` [#]_ インターフェースを実装したクラスを作成します。
このインターフェースには、2つのインターフェースメソッドが定義されています。

``Object getKey(T target)``
    指定のオブジェクトから突き合わせるためのキーを作成して返す。
    キーは ``Object.equals()`` を利用して突き合わせるため、返すオブジェクトは同メソッドを正しく実装している必要がある。

``Object verify(T expected, T actual)``
    突き合わせた2つのオブジェクトを比較し、比較に失敗した場合にはその旨のメッセージを返す。成功した場合には ``null`` を返す。

``ModelVerifier`` インターフェースを利用したテストでは、次のように期待データと結果の比較を行います。

#. それぞれの期待データから ``getKey(期待データ)`` でキーの一覧を取得する
#. それぞれの結果データから ``getKey(結果データ)`` でキーの一覧を取得する
#. 期待データと結果データから同じキーになるものを探す

   #. 見つかれば ``veriry(期待データ, 結果データ)`` を実行する
   #. 期待データに対する結果データが見つからなければ、 ``verify(期待データ, null)`` を実行する
   #. 結果データに対する期待データが見つからなければ、 ``verify(null, 結果データ)`` を実行する

#. いずれかの ``verify()`` が ``null`` 以外を返したらテストは失敗となる
#. 全ての ``verify()`` が ``null`` を返したら、次の出力に対する期待データと結果データを比較する

以下は ``ModelVerifier`` インターフェースの実装例です。
`category`, `number` という2つのプロパティから複合キーを作成して、突き合わせた結果の `value` を比較しています。
また、期待データと結果データの個数が違う場合はエラーにしています。

..  code-block:: java

    class ExampleVerifier implements ModelVerifier<Hoge> {
        @Override
        public Object getKey(Hoge target) {
            return Arrays.asList(target.getCategory(), target.getNumber());
        }

        @Override
        public Object verify(Hoge expected, Hoge actual) {
            if (expected == null || actual == null) {
                return "invalid record";
            }
            if (expected.getValue() != actual.getValue()) {
                return "invalid value";
            }
            return null;
        }
    }

``ModelVerifier`` を実装したクラスを作成したら、各 ``Tester`` クラスの ``verify`` メソッドの第二引数に指定します。

..  code-block:: java

    @Test
    public void testExample() {
        JobFlowTester tester = new JobFlowTester(getClass());
        tester.input("shipment", Shipment.class)
            .prepare("shipment.xls#input");
        tester.output("hoge", Hoge.class)
            .verify("hoge.json", new ExampleVerifier());
        ...
    }

..  [#] :javadoc:`com.asakusafw.testdriver.core.ModelVerifier`

テスト条件をJavaで拡張する
~~~~~~~~~~~~~~~~~~~~~~~~~~

`テスト条件をJavaで記述する`_ で説明した方法ではテスト条件をすべてJavaで記述しますが、Excelなどで記述したテスト条件をJavaで拡張することもできます。

テスト条件をJavaで拡張するには、 ``ModelTester`` [#]_ インターフェースを実装したクラスを作成します。
このインターフェースは先述の ``ModelVerifier`` の親インターフェースとして宣言されており、以下のインターフェースメソッドが定義されています。

``Object verify(T expected, T actual)``
    突き合わせた2つのオブジェクトを比較し、比較に失敗した場合にはその旨のメッセージを返す。成功した場合には ``null`` を返す。

``ModelTester`` インターフェースを利用したテストでは、次のように期待データと結果の比較を行います。

#. Excel等で記述したテスト条件で期待データと結果データの突き合わせと比較を行う
#. 上記で突き合わせに成功したら、 ``ModelTester.verify(<期待データ>, <結果データ>)`` で比較を行う
#. 両者の比較のうちいずれかに失敗したらテストは失敗となる

以下は ``ModelTester`` インターフェースの実装例です。

..  code-block:: java

    class ExampleTester implements ModelTester<Hoge> {

        @Override
        public Object verify(Hoge expected, Hoge actual) {
            if (expected == null || actual == null) {
                return "invalid record";
            }
            if (expected.getValue() != actual.getValue()) {
                return "invalid value";
            }
            return null;
        }
    }

``ModelTester`` を実装したクラスを作成したら、各 ``Tester`` クラスの ``verify`` メソッドの第三引数にインスタンスを指定します [#]_ 。

..  code-block:: java

    @Test
    public void testExample() {
        JobFlowTester tester = new JobFlowTester(getClass());
        tester.input("shipment", Shipment.class)
            .prepare("shipment.xls#input");
        tester.output("hoge", Hoge.class)
            .verify("hoge.json", "hoge.xls#rule", new ExampleTester());
        ...
    }

テスト条件の拡張は、主にExcelなどで表現しきれない比較を行いたい場合に利用できます。
比較方法をすべてJavaで記述する場合には `テスト条件をJavaで記述する`_ の方法を参照してください。

..  [#] :javadoc:`com.asakusafw.testdriver.core.ModelTester`

..  [#] 第三引数を指定できるのは、テスト条件をパスで指定した場合のみです。
        ``ModelVerifier`` を利用する場合には指定できません。

演算子のトレースログを出力する
------------------------------

テスト対象のデータフローに含まれる演算子について、入力されたデータと出力されたデータを調べるには、テストドライバーのトレース機能を利用すると便利です。
トレース機能を利用すると、指定した演算子に入力されたデータや出力されたデータを :ref:`dsl-report-api` 経由で表示できます。

..  attention::
    トレース機能はユーザー演算子に指定することができます。コア演算子にはトレースを指定することはできません。

入力データのトレース
~~~~~~~~~~~~~~~~~~~~

演算子に入力されたデータを調べる場合、各 ``Tester`` クラスの ``addInputTrace`` メソッドを利用して対象の演算子と入力ポートを指定します。
下記の例は、演算子クラス ``YourOperator`` に作成した演算子メソッド ``operatorName`` の入力ポート [#]_ ``inputName`` に入力される全てのデータについてトレースの設定を行います。

..  code-block:: java

    @Test
    public void testExample() {
        JobFlowTester tester = new JobFlowTester(getClass());
        tester.addInputTrace(YourOperator.class, "operatorName", "inputName");
        ...
    }

..  [#] 演算子ファクトリクラスに含まれる演算子ファクトリメソッドの引数名が入力ポート名に該当します。
        詳しくは :doc:`../dsl/user-guide` - :ref:`dsl-userguide-operator-factory` を参照してください。

出力データのトレース
~~~~~~~~~~~~~~~~~~~~

演算子から出力されたデータを調べる場合、各 ``Tester`` クラスの ``addOutputTrace`` メソッドを利用して対象の演算子と出力ポートを指定します。
下記の例は、演算子クラス ``YourOperator`` に作成した演算子メソッド ``operatorName`` の出力ポート [#]_ ``outputName`` から出力する全てのデータについてトレースの設定を行います。

..  code-block:: java

    @Test
    public void testExample() {
        JobFlowTester tester = new JobFlowTester(getClass());
        tester.addOutputTrace(YourOperator.class, "operatorName", "outputName");
        ...
    }

..  [#] 演算子ファクトリクラスに含まれる演算子オブジェクトクラスのフィールド名が出力ポート名に該当します。
        詳しくは :doc:`../dsl/user-guide` - :ref:`dsl-userguide-operator-factory` を参照してください。

トレース情報の出力
~~~~~~~~~~~~~~~~~~

上記の設定を行った状態でテストを実行すると、指定した演算子の入力や出力が行われるたびに、文字列 ``TRACE-`` を含むメッセージを :ref:`dsl-report-api` 経由で出力します [#]_ 。
ここには、トレースを設定した対象の情報や、実際に入出力が行われたデータの内容が含まれています。

..  attention::
    トレースの出力方式は将来変更される可能性があります。

..  attention::
    トレース機能を有効にすると、テストの実行に非常に時間がかかるようになる場合があります。

..  [#] このとき、 ``Report.info()`` を利用してメッセージを出力しています。
        メッセージが正しく表示されない場合には、Report APIの設定を確認してください。

テストドライバーの各実行ステップをスキップする
----------------------------------------------

テストドライバーは、各ステップをスキップするためのメソッドが提供されています。
これらのメソッドを使用することで、以下のようなことが可能になります。

* 入力データ設定前にクリーニング、および入力データの投入をスキップして既存データに対するテストを行う
* 出力データの検証をスキップしてテストドライバーAPIの外側で独自のロジックによる検証を行う。

スキップを行う場合、 ``Tester`` クラスが提供する以下のメソッドを利用します。

``void skipValidateCondition(boolean skip)``
    テスト条件の検証をスキップするかを設定する。

``void skipCleanInput(boolean skip)``
    入力データのクリーニング(truncate)をスキップするかを設定する。

``void skipCleanOutput(boolean skip)``
    出力データのクリーニング(truncate)をスキップするかを設定する。

``void skipPrepareInput(boolean skip)``
    入力データのセットアップ(prepare)をスキップするかを設定する。

``void skipPrepareOutput(boolean skip)``
    出力データのセットアップ(prepare)をスキップするかを設定する。

``void skipRunJobFlow(boolean skip)``
    ジョブフローの実行をスキップするかを設定する。

``void skipVerify(boolean skip)``
    テスト結果の検証をスキップするかを設定する。

コンテキストAPIを利用する演算子のテスト
---------------------------------------

テスト対象のデータフローでコンテキストAPIを利用している場合、コンテキストAPIが参照するバッチの起動引数をテスト側で指定します。
この設定には、 各 ``Tester`` クラスの ``setBatchArg`` というメソッドから設定します。

..  code-block:: java

    @Test
    public void testExample() {
        BatchTester tester = new BatchTester(getClass());
        tester.setBatchArg("message", "Hello, world!");
        ...
    }

上記のように、第一引数には変数名、第二引数には変数の値を指定します。

..  hint::
    データフローのテストでは、演算子の際のような
    ``reload`` は不要です。

.. _testing-runtime-plugin-configuration:

実行時プラグインの設定
----------------------

テスト対象の演算子で実行時プラグイン [#]_ を利用する場合、「実行時プラグイン設定ファイル」が必要になります。
データフローのテストの際には、利用している開発環境にインストールされた設定ファイル [#]_ を利用して処理を実行します。

その他、各 ``Tester`` クラスの ``configure`` メソッドを利用して個々のプラグインの設定を行うことも可能です。

..  code-block:: java

    @Test
    public void testExample() {
        BatchTester tester = new BatchTester(getClass());
        tester.configure("com.asakusafw.message", "Hello, world!");
        ...
    }

上記のように、第一引数にはプロパティ名、第二引数にはプロパティの値を指定します。

..  attention::
    実行時プラグインはの設定は、Hadoop起動時の "-D" オプションで指定するプロパティをそのまま利用しています。
    そのため、 ``configure`` メソッドでHadoopのプロパティを利用することも可能ですが、通常の場合は利用しないでください。

..  hint::
    データフローのテストでは、演算子の際のような ``reload`` は不要です。

..  [#] :doc:`../administration/deployment-runtime-plugins` を参照
..  [#] :doc:`../application/gradle-plugin` の手順に従って作成したプロジェクトでは :file:`$ASAKUSA_HOME/core/conf/asakusa-resources.xml` が配置されるため、デフォルトの状態ではこのファイルが利用されます。
        デフォルトの状態では演算子のテストで使用される実行時プラグイン設定ファイルと異なるファイルが利用されることに注意してください。

.. _testing-userguide-integration-test:

インテグレーションテスト
========================

バッチアプリケーションのインテグレーションテストを行うには、以下のような方法があります。

#. :doc:`YAESS <../yaess/index>` を利用してアプリケーションを実行する
#. `バッチテストランナー`_ を利用してアプリケーションを実行する
#. `テストツールタスク`_ を利用してアプリケーションを実行する

YAESSを利用する方法では、運用環境と同様の手順でバッチアプリケーションを実行するため、運用環境に近い確実なテストが行えます。
その反面、YAESSのコマンドラインインターフェースを経由しなければならないため、ちょっとした動作確認を行うにはやや手順が煩雑です。

バッチテストランナーを利用する方法では、テストドライバーの内部機構を利用して簡易的にバッチアプリケーションを実行します。
プログラミングインターフェースやJavaのコマンドラインインターフェースを提供しており、開発環境から容易に実行できます。
ただし、YAESSのような豊富な機能は提供しておらず、テストドライバーと同様にローカルコンピューター上のAsakusa Framework と Hadoop を利用してバッチを実行します。

..  attention::
    テスト実行以外の用途では、YAESSを利用してバッチアプリケーションを実行することを推奨します。
    バッチテストランナーは、主に開発時のさまざまな動作確認用に利用することを想定しています。

テストツールタスクを利用する方法は、 YAESSやバッチテストランナーを使ってアプリケーションを実行しつつ、データ配置やデータの検証はテストドライバーの機構を利用する、という場合に利用することができます。

..  hint::
    テストツールタスクはインテグレーションテストの自動化を行う場合や、自動テストと手動テストを組み合わせるような場合などで利用することを想定しています。

以下はツールごとにおける自動化部分の比較です。

..  list-table:: ツールごとのインテグレーションテスト自動化部分の比較
    :widths: 1 1 1 1
    :header-rows: 1

    * - 項目
      - テストドライバー
      - バッチテストランナー
      - テストツールタスク
    * - アプリケーションのビルド
      - ○
      - ×
      - ×
    * - アプリケーションのデプロイ
      - ○
      - ×
      - ×
    * - 入力データの配置
      - ○
      - ×
      - ○
    * - アプリケーションの実行
      - ○
      - ○
      - ○
    * - 実行結果の確認
      - ○
      - ×
      - ○

.. _testing-userguide-batch-test-runner:

バッチテストランナー
--------------------

バッチテストランナーはテストドライバーが持つ機能のうち、アプリケーションの実行のみを単独で行えるようにしたものです。
テストドライバーが自動的に行っていたいくつかの部分について、手動で細やかな設定を行えるようになります。

バッチテストランナーを利用してアプリケーションを実行するには、バッチテストランナーのプログラミングインターフェースや、コマンドラインインターフェースを利用します。
詳しくは以降を参照してください。

..  hint::
    バッチテストランナーが自動的に行わない部分の手順については、 :ref:`startguide-running-example` などを参照してください。

..  hint::
    バッチテストランナーは内部的にテストドライバーの機構を利用しているため、テストドライバーと同様の方法で :doc:`エミュレーションモード <emulation-mode>` を利用できます。

プログラミングインターフェース
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Javaのプログラムからバッチテストランナーを実行するには、 ``com.asakusafw.testdriver.tools.runner.BatchTestRunner`` [#]_ クラスを利用します。
詳しい利用方法は、Javadocを参照してください。

以下は :ref:`Asakusa Framework スタートガイド <startguide-running-example>` で紹介しているサンプルアプリケーションを実行する例です。

..  code-block:: java

    int result = new BatchTestRunner("example.summarizeSales")
        .withArgument("date", "2011-04-01")
        .execute();

    if (result != 0) {
        // エラー処理 ...
    }

..  [#] :javadoc:`com.asakusafw.testdriver.tools.runner.BatchTestRunner`

コマンドラインインターフェース
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

コマンドラインからバッチテストランナーを実行するには、テストドライバーのクラスライブラリ群をクラスパスに登録した状態で ``com.asakusafw.testdriver.tools.runner.BatchTestRunner`` クラスを実行します。

指定できるオプションは次の通りです。

..  program:: com.asakusafw.testdriver.tools.runner.BatchTestRunner

..  option:: -b,--batch <batch_id>

    実行するバッチのバッチIDを指定します。

..  option:: -A,--argument <name=value>

    実行するバッチのバッチ引数を指定します。

..  option:: -D,--property <name=value>

    :ref:`testing-runtime-plugin-configuration` を行います。

例えば :ref:`Asakusa Framework スタートガイド <startguide-running-example>` で紹介しているサンプルアプリケーションを実行する場合のオプション指定は以下のようになります。

..  code-block:: sh

    -b example.summarizeSales -A date=2011-04-01

コマンドラインインターフェースは、バッチアプリケーションが正常終了した際に終了コード ``0`` を返し、正常終了しなかった場合に非 ``0`` を返します。

.. _testing-userguide-testtool-task:

テストツールタスク
------------------

テストツールタスクはテストドライバーやバッチテストランナーが持つ機能を組み合わせてGradleのタスクとして実行できるようにするものです。
バッチの実行にはYAESSとバッチテストランナーのどちらかを選択します。

以下にテストツールタスクを使って作成したGradleタスクの例を示します。

..  code-block:: groovy

    task batchTestSummarize(type: com.asakusafw.gradle.tasks.TestToolTask) {
        clean description: 'com.example.batch.SummarizeBatch'
        prepare importer: 'com.example.jobflow.StoreInfoFromCsv',
            data: '/com/example/jobflow/masters.xls#store_info'
        prepare importer: 'com.example.jobflow.ItemInfoFromCsv',
            data: '/com/example/jobflow/masters.xls#item_info'
        prepare importer: 'com.example.jobflow.SalesDetailFromCsv',
            data: '/com/example/jobflow/summarize.xls#sales_detail'
        run batch: 'example.summarizeSales'
        verify exporter: 'com.example.jobflow.CategorySummaryToCsv',
            data: '/com/example/jobflow/summarize.xls#result',
            rule: '/com/example/jobflow/summarize.xls#result_rule'
    }

..  seealso::
    ``TestToolTask`` や Gradleの利用方法については :doc:`../application/gradle-plugin` を参照してください。

