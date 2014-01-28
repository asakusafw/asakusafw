============================
テストドライバユーザーガイド
============================
この文書では、Asakusa Frameworkを使ったバッチアプリケーションをテストする方法について紹介します。

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

..  [#] このクラスは「演算子実装クラス」と呼ばれ、Operator DSLコンパイラが自動的に生成します。
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

..  [#] :javadoc:`com.asakusafw.runtime.core.Result`
..  [#] :javadoc:`com.asakusafw.runtime.testing.MockResult`


演算子テストの補助
------------------
いくつかの演算子のテストには、 ``OperatorTestEnvironment`` [#]_ クラスを利用します。
このクラスはAsakusaのフレームワークAPIをテスト時にエミュレーションするためのもので、
フレームワークAPIを利用する演算子をテストする場合には必須です。

``OperatorTestEnvironment`` クラスは、テストクラスの ``public`` フィールドに
``@Rule`` [#]_ という注釈を付けてインスタンス化します。

..  code-block:: java

    // 必ずpublicで宣言し、インスタンスを代入する
    @Rule
    public OperatorTestEnvironment resource = new OperatorTestEnvironment();

..  [#] :javadoc:`com.asakusafw.testdriver.OperatorTestEnvironment`
..  [#] ``org.junit.Rule``

コンテキストAPIを利用する演算子のテスト
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
テスト対象の演算子がコンテキストAPI [#]_ を利用する場合、
コンテキストAPIが参照するバッチの起動引数をテスト側で指定します。

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

..  warning::
    演算子メソッドに対する操作は必ず ``reload`` メソッドの呼出し後に記述してください。

..  [#] :doc:`../dsl/user-guide` の :ref:`dsl-context-api` を参照


実行時プラグインの設定
~~~~~~~~~~~~~~~~~~~~~~
テスト対象の演算子で実行時プラグイン [#]_ を利用する場合、
「実行時プラグイン設定ファイル」が必要になります。
これは利用する実行時プラグインや、それぞれのプラグインの設定を記述したもので、
``OperatorTestEnvironment`` クラスをインスタンス化する際に位置を指定できます。

..  code-block:: java

    @Rule
    public OperatorTestEnvironment resource =
        new OperatorTestEnvironment("conf/asakusa-test-resources.xml");

ここに指定する位置は、クラスパス上の位置です。
特に指定せずに ``OperatorTestEnvironment`` クラスをインスタンス化した場合には、
クラスパスルートの ``asakusa-resources.xml`` というファイルを利用します [#]_ 。

その他、 ``OperatorTestEnvironment`` クラスの ``configure`` メソッドを利用して
個々のプラグインの設定を行うことも可能です。
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


..  warning::
    演算子メソッドに対する操作は必ず ``reload`` メソッドの呼出し後に記述してください。

..  [#] 実行時プラグインについては、 :doc:`../administration/deployment-runtime-plugins` を参照してください。
..  [#] :doc:`../application/gradle-plugin` の手順に従って作成したプロジェクトでは ``src/test/resources/asakusa-resources.xml`` が配置されるため、デフォルトの状態ではこのファイルが利用されます。


データフローのテスト
====================
データフローやバッチのテストは、DSLのコンパイラや実行環境であるHadoopと連携して行います。
Asakusa Frameworkはこの一連の処理を自動的に行うテストドライバというモジュールを含んでいます。

テストドライバはテスト対象の要素に対して、次の一連の処理を行います。

#. 入力データを初期化する
#. 入力データを流し込む
#. 対象のプログラムをテスト実行する
#. 出力結果を取り込む
#. 出力結果と期待データを検証する

なお、データフローのテストにはHadoopのインストールが必要です。
設定については `Hadoopの設定`_ を参照してください。

テストデータの作成
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

テストドライバはテストデータをさまざまな形式で記述できます。
Asakusa Frameworkが標準でサポートしているのは以下の2種類です。

* :doc:`using-excel`
* :doc:`using-json`

テストデータの配置
~~~~~~~~~~~~~~~~~~
作成したテストデータは、それを利用するテストと同じパッケージか、
そのサブパッケージ上に配置します。

複数のテストから利用されるテストデータは、
任意のパッケージに配置して構いません。

テストの実行
------------
`テストデータの作成`_ を完了したら、それぞれのデータフローをテストします。
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
引数には先ほど配置したテストデータを、以下のいずれかで指定します。

* パッケージからの相対パス
* クラスパスからの絶対パス ( ``/`` から始める )

サブパッケージ ``a.b`` などに配置している場合には、
``a/b/file.xls#hoge`` のように ``/`` で区切って指定します。

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
テスト条件をJavaで直接記述する場合の方法は、
`テスト条件をJavaで記述する`_ や `テスト条件をJavaで拡張する`_ を参照してください。

出力の定義結果は、 ``Out<データモデル型>`` [#]_ の変数に保存します。

..  code-block:: java

    Out<Shipment> shipmentOut = tester.output("shipment", Shipment.class)
        .verify("shipment.xls#output", "shipment.xls#rule");
    Out<Stock> stockOut = tester.output("stock", Stock.class)
        .verify("stock.xls#output", "stock.xls#rule");

なお、 ``input`` と同様に ``output`` でも初期データの指定を行えます。
利用方法は ``input`` の ``prepare`` と同様です。

..  note::
    「出力に初期データがある場合」のテストでは、出力に対して ``prepare`` を実行します。
    たとえば、ThunderGateの重複チェック機能を利用する場合、対象のテーブルには
    データが既に格納されている必要があります。

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
テスト時の出力結果を保存するには、対象の出力に対して ``.dumpActual("<出力先>")`` を指定します。

..  code-block:: java

    Out<Shipment> shipmentOut = tester.output("shipment", Shipment.class)
        .dumpActual("build/dump/actual.xls")
        .verify("shipment.xls#output", "shipment.xls#rule");

出力先には、ファイルパスや ``File`` [#]_ オブジェクトを指定できます。
ファイルパスで相対パスを指定した場合、テストを実行したワーキングディレクトリからの相対パス上に結果が出力されます。

..  hint::
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
出力されたデータの比較結果を保存するには、対象の出力に対して ``.dumpDifference(<出力先>)`` を指定します。

..  code-block:: java

    Out<Shipment> shipmentOut = tester.output("shipment", Shipment.class)
        .verify("shipment.xls#output", "shipment.xls#rule")
        .dumpDifference("build/dump/difference.html");

「 `出力結果を保存する`_ 」と同様に、出力先にはファイルパスや ``File`` オブジェクトを指定できます。
ファイルパスで相対パスを指定した場合、テストを実行したワーキングディレクトリからの相対パス上に結果が出力されます。

また、出力先に指定したファイル名の拡張子に応じた形式で出力が行われます。
標準ではHTMLファイルを出力する ``.html`` を指定できます。

..  warning::
    この操作は、 ``verify()`` と組み合わせて指定してください。 ``verify()`` の指定がない場合、比較結果の保存は行われません。
    また、比較結果に差異がない場合には比較結果は保存されません。

テスト条件をJavaで記述する
--------------------------
テスト条件は期待データと実際の結果を突き合わせるための
ルールを示したもので、Javaで直接記述することも可能です。

テスト条件をJavaで記述するには、 ``ModelVerifier`` [#]_ インターフェースを
実装したクラスを作成します。
このインターフェースには、2つのインターフェースメソッドが定義されています。

``Object getKey(T target)``
    指定のオブジェクトから突き合わせるためのキーを作成して返す。
    キーは ``Object.equals()`` を利用して突き合わせるため、
    返すオブジェクトは同メソッドを正しく実装している必要がある。

``Object verify(T expected, T actual)``
    突き合わせた2つのオブジェクトを比較し、比較に失敗した場合には
    その旨のメッセージを返す。成功した場合には ``null`` を返す。

``ModelVerifier`` インターフェースを利用したテストでは、
次のように期待データと結果の比較を行います。

#. それぞれの期待データから ``getKey(期待データ)`` でキーの一覧を取得する
#. それぞれの結果データから ``getKey(結果データ)`` でキーの一覧を取得する
#. 期待データと結果データから同じキーになるものを探す

   #. 見つかれば ``veriry(期待データ, 結果データ)`` を実行する
   #. 期待データに対する結果データが見つからなければ、 ``verify(期待データ, null)`` を実行する
   #. 結果データに対する期待データが見つからなければ、 ``verify(null, 結果データ)`` を実行する

#. いずれかの ``verify()`` が ``null`` 以外を返したらテストは失敗となる
#. 全ての ``verify()`` が ``null`` を返したら、次の出力に対する期待データと結果データを比較する


以下は ``ModelVerifier`` インターフェースの実装例です。
``category``, ``number`` という2つのプロパティから複合キーを作成して、
突き合わせた結果の ``value`` を比較しています。
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

``ModelVerifier`` を実装したクラスを作成したら、
各 ``Tester`` クラスの ``verify`` メソッドの第二引数に指定します。

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
--------------------------
「 `テスト条件をJavaで記述する`_ 」という他に、Excelなどで記述したテスト条件をJavaで拡張することもできます。

テスト条件をJavaで拡張するには、 ``ModelTester`` [#]_ インターフェースを実装したクラスを作成します。
このインターフェースは先述の ``ModelVerifier`` の親インターフェースとして宣言されており、以下のインターフェースメソッドが定義されています。

``Object verify(T expected, T actual)``
    突き合わせた2つのオブジェクトを比較し、比較に失敗した場合には
    その旨のメッセージを返す。成功した場合には ``null`` を返す。


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

``ModelTester`` を実装したクラスを作成したら、
各 ``Tester`` クラスの ``verify`` メソッドの第三引数にインスタンスを指定します [#]_ 。

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
比較方法をすべてJavaで記述する場合には「 `テスト条件をJavaで記述する`_ 」の方法を参照してください。

..  [#] :javadoc:`com.asakusafw.testdriver.core.ModelTester`

..  [#] 第三引数を指定できるのは、テスト条件をパスで指定した場合のみです。
        ``ModelVerifier`` を利用する場合には指定できません。

演算子のトレースログを出力する
------------------------------
テスト対象のデータフローに含まれる演算子について、入力されたデータと出力されたデータを調べるには、テストドライバのトレース機能を利用すると便利です。
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

フロー部品の入力に対してトレースの設定を行う場合、引数を2つだけとる ``addInputTrace`` メソッドを利用して演算子の名前を省略できます。

..  code-block:: java

    @Test
    public void testExample() {
        JobFlowTester tester = new JobFlowTester(getClass());
        tester.addInputTrace(YourFlowpart.class, "inputName");
        ...
    }

..  [#] 演算子ファクトリクラスに含まれる演算子ファクトリメソッドの引数名が入力ポート名に該当します。
        詳しくは :doc:`../dsl/user-guide` の :ref:`dsl-userguide-operator-factory` を参照してください。

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

入力と同様に、フロー部品の出力に対してトレースの設定を行う場合、引数を2つだけとる ``addOutputTrace`` メソッドを利用して演算子の名前を省略できます。

..  code-block:: java

    @Test
    public void testExample() {
        JobFlowTester tester = new JobFlowTester(getClass());
        tester.addOutputTrace(YourFlowpart.class, "outputName");
        ...
    }

..  hint::
    フロー部品から作成されるフロー演算子について、3つの引数を取るメソッド ``addInputTrace`` や ``addOutputTrace`` を利用する場合、
    演算子の名前には ``"create"`` という名前を指定してください。

..  [#] 演算子ファクトリクラスに含まれる演算子オブジェクトクラスのメソッド名が出力ポート名に該当します。
        詳しくは :doc:`../dsl/user-guide` の :ref:`dsl-userguide-operator-factory` を参照してください。

トレース情報の出力
~~~~~~~~~~~~~~~~~~
上記の設定を行った状態でテストを実行すると、指定した演算子の入力や出力が行われるたびに ``[TRACE-xxxx]`` ( ``xxxx`` は識別番号) を含むメッセージを :ref:`dsl-report-api` 経由で出力します。
ここには、トレースを設定した対象の情報や、実際に入出力が行われたデータの内容が含まれています。

以下はHadoopのログに出力するレポートAPIを利用し、トレースの設定を行ってテストを実行した際の、コンソールログの一部を抜粋し、表示用に加工したものです。
コンソールに ``[TRACE-0001]`` という文字列を含む行がいくつか含まれています。

..  code-block:: sh

    stage.AbstractStageClient: Job Submitted: id=job_local_0001, name=bid.byCategory.stage0001
    mapred.JobClient: Running job: job_local_0001
    mapred.Task:  Using ResourceCalculatorPlugin : org.apache.hadoop.util.LinuxResourceCalculatorPlugin@1c5ddc9
    trace.TraceDriverLifecycleManager: The configuration key "com.asakusafw.runtime.trace.TraceReportActionFactory" is not set, we use "com.asakusafw.runtime.trace.TraceReportActionFactory"
    report.CommonsLoggingReport: [TRACE-0001] 1@..CategorySummaryOperator#.checkStore.INPUT.sales:..SalesDetail: {class=sales_detail, salesDateTime=2011-01-01 00:00:00, storeCode=..}
    report.CommonsLoggingReport: [TRACE-0001] 1@..CategorySummaryOperator#.checkStore.INPUT.sales:..SalesDetail: {class=sales_detail, salesDateTime=2011-01-15 00:00:00, storeCode=..}
    report.CommonsLoggingReport: [TRACE-0001] 1@..CategorySummaryOperator#.checkStore.INPUT.sales:..SalesDetail: {class=sales_detail, salesDateTime=2011-01-31 23:59:59, storeCode=..}
    report.CommonsLoggingReport: [TRACE-0001] 1@..CategorySummaryOperator#.checkStore.INPUT.sales:..SalesDetail: {class=sales_detail, salesDateTime=2011-02-01 00:00:00, storeCode=..}
    report.CommonsLoggingReport: [TRACE-0001] 1@..CategorySummaryOperator#.checkStore.INPUT.sales:..SalesDetail: {class=sales_detail, salesDateTime=2012-01-01 00:00:00, storeCode=..}
    mapred.Task: Task:attempt_local_0001_m_000000_0 is done. And is in the process of commiting
    mapred.LocalJobRunner:
    mapred.Task: Task attempt_local_0001_m_000000_0 is allowed to commit now

..  warning::
    上記のトレースの出力形式は、将来変更される可能性があります。

..  warning::
    トレース機能を有効にすると、テストの実行に非常に時間がかかるようになる場合があります。

テストドライバの各実行ステップをスキップする
--------------------------------------------
テストドライバは、各ステップをスキップするためのメソッドが提供されています。
これらのメソッドを使用することで、以下のようなことが可能になります。

* 入力データ設定前にクリーニング、および入力データの投入をスキップして既存データに対するテストを行う
* 出力データの検証をスキップしてテストドライバAPIの外側で独自のロジックによる検証を行う。

スキップを行う場合、 ``Tester`` クラスが提供する以下のメソッドを利用します。

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
テスト対象のデータフローでコンテキストAPIを利用している場合、
コンテキストAPIが参照するバッチの起動引数をテスト側で指定します。
この設定には、 各 ``Tester`` クラスの ``setBatchArg`` という
メソッドから設定します。

..  code-block:: java

    @Test
    public void testExample() {
        BatchTester tester = new BatchTester(getClass());
        tester.setBatchArg("message", "Hello, world!");
        ...
    }

上記のように、第一引数には変数名、第二引数には変数の値を指定します。

..  note::
    データフローのテストでは、演算子の際のような
    ``reload`` は不要です。

Hadoopの設定
------------
データフローのテストでは、Hadoopを利用してテストデータの事前配置や結果の取得を行っています。
また、テスト対象のプログラムを実行する際にもHadoopを利用しています。

ここで利用するHadoopの ``hadoop`` コマンドは次の順番で検出しています。

#. 環境変数 ``HADOOP_CMD`` に ``hadoop`` コマンドへのパスが設定されている場合、それを利用する
#. 環境変数 ``HADOOP_HOME`` にHadoopのインストール先が指定されている場合、 ``$HADOOP_HOME/bin/hadoop`` を利用する [#]_
#. 環境変数 ``PATH`` に ``hadoop`` コマンドを含むディレクトリが指定されている場合、それを利用する

上記のいずれによっても ``hadoop`` コマンドを見つけられなかった場合、テストの実行に失敗します。

また、上記で検出したHadoopの設定とは異なる設定でテストを実行することも可能です。
テストドライバが利用するHadoopの設定は次の順番で検出しています。

#. 環境変数 ``HADOOP_CONF`` が指定されている場合、その内容を設定ディレクトリへのパスとして利用する
#. ``hadoop`` コマンドを実行し、そこで利用されている設定ディレクトリを利用する

上記のうち、環境変数 ``HADOOP_CONF`` を指定する際には「 ``core-site.xml`` 」などのHadoopの設定情報が格納されたディレクトリへのパスを指定してください。

..  [#] Hadoop 0.20.205以降、環境変数 ``HADOOP_HOME`` の利用は推奨されていません

実行時プラグインの設定
----------------------
テスト対象の演算子で実行時プラグイン [#]_ を利用する場合、
「実行時プラグイン設定ファイル」が必要になります。
データフローのテストの際には、利用している開発環境にインストールされた
設定ファイル [#]_ を利用して処理を実行します。

その他、各 ``Tester`` クラスの ``configure`` メソッドを利用して
個々のプラグインの設定を行うことも可能です。

..  code-block:: java

    @Test
    public void testExample() {
        BatchTester tester = new BatchTester(getClass());
        tester.configure("com.asakusafw.message", "Hello, world!");
        ...
    }

上記のように、第一引数にはプロパティ名、第二引数にはプロパティの値を指定します。

..  warning::
    実行時プラグインはの設定は、Hadoop起動時の "-D" オプションで指定する
    プロパティをそのまま利用しています。
    そのため、 ``configure`` メソッドでHadoopのプロパティを利用することも可能ですが、
    通常の場合は利用しないでください。

..  note::
    データフローのテストでは、演算子の際のような
    ``reload`` は不要です。

..  [#] :doc:`../administration/deployment-runtime-plugins` を参照
..  [#] :doc:`../application/gradle-plugin` の手順に従って作成したプロジェクトでは ``$ASAKUSA_HOME/core/conf/asakusa-resources.xml`` が配置されるため、デフォルトの状態ではこのファイルが利用されます。デフォルトの状態では演算子のテストで使用される実行時プラグイン設定ファイルと異なるファイルが利用されることに注意してください。

