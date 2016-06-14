==================================
Direct I/Oのシーケンスファイル連携
==================================

この文書では、Direct I/OからHadoopのシーケンスファイルをインポート/エクスポートする方法について説明します。

シーケンスファイル形式のDataFormatの作成
========================================

Hadoopのシーケンスファイル [#]_ を直接読み書きするには、 ``SequenceFileFormat`` [#]_ のサブクラスを作成します。

..  hint::
    以降の記述は、Asakusa Frameworkの外部で作成されたシーケンスファイルを利用する際の方法です。
    シーケンスファイルにAsakusa Frameworkのデータモデル形式を直接利用する場合 `内部データ形式を利用したシーケンスファイル形式のDataFormatの作成`_ なども利用可能です。

``SequenceFileFormat`` は ``HadoopFileFormat`` のサブクラスで、シーケンスファイルを読み書きするための骨格実装が提供されています。

このクラスを継承する際には、以下の型引数を ``SequenceFileFormat<K, V, T>`` にそれぞれ指定してください。

``K``
  対象シーケンスファイルのキーオブジェクトの型

``V``
  対象シーケンスファイルの値オブジェクトの型

``T``
  アプリケーションで利用するデータモデルオブジェクトの型

このクラスでは、下記のメソッドをオーバーライドします。

``Class<T> getSupportedType()``
  対象となるデータモデルのクラスを戻り値に指定します。

``K createKeyObject()``
  対象のシーケンスファイルのキーと同じクラスのオブジェクトを戻り値に指定します。

``V createValueObject()``
  対象のシーケンスファイルの値と同じクラスのオブジェクトを戻り値に指定します。

``void copyToModel(K key, V value, T model)``
  シーケンスファイルから読み出したキー ( ``key`` ) と 値 ( ``value`` ) の内容を、
  対象のデータモデルオブジェクト ( ``model`` ) に設定します。

  このメソッドは、シーケンスファイルからデータ読み出す際に、レコードごとに起動されます。
  このメソッドによって変更されたデータモデルオブジェクトは、以降の処理の入力として利用されます。

``void copyFromModel(T model, K key, V value)``
  結果を表すデータモデルオブジェクトの内容を、シーケンスファイルのキー ( ``key`` ) と値 ( ``value`` ) に設定します。

  このメソッドは、シーケンスファイルにデータを書き込む際に、レコードごとに起動されます。
  このメソッドによって変更されたキーと値がそのままシーケンスファイルに書き出されます。

``CompressionCodec getCompressionCodec(Path path)``
  シーケンスファイルの作成時に利用する圧縮コーデックを指定します。

  オーバーライドしない場合、全体の設定情報をもとに圧縮コーデックを決定します。
  詳しくは `シーケンスファイルの圧縮`_ を参照してください。

以下はシーケンスファイル形式のDataFormatの実装例です。

..  code-block:: java

    public class ExampleSequenceFormat extends SequenceFileFormat<LongWritable, Text, MyData> {

        @Override
        public Class<MyData> getSupportedType() {
            return MyData.class;
        }

        @Override
        protected LongWritable createKeyObject() {
            return new LongWritable();
        }

        @Override
        protected Text createValueObject() {
            return new Text();
        }

        @Override
        protected void copyToModel(LongWritable key, Text value, MyData model) {
            model.setPosition(key.get());
            model.setText(value);
        }

        @Override
        protected void copyFromModel(MyData model, LongWritable key, Text value) {
            key.set(model.getPositionOption().or(0L));
            value.set(model.getTextOption().or("(null)"));
        }
    }

..  hint::
    この機能は、 `Apache Sqoop`_ 等のツールと連携することを想定して提供されています。

..  [#] ``org.apache.hadoop.io.SequenceFile``
..  [#] :javadoc:`com.asakusafw.runtime.directio.hadoop.SequenceFileFormat`

..  _`Apache Sqoop` : http://sqoop.apache.org/

シーケンスファイルの圧縮
------------------------

``SequenceFileFormat`` を利用してシーケンスファイルを作成する場合、以下のいくつかの方法で圧縮形式を指定できます。
以下、上から順に該当する項目があれば、そこで設定された圧縮形式を利用します。

``SequenceFileFormat.getCompressionCodec(Path path)`` をオーバーライド
  オーバーライドしたメソッドが返す圧縮コーデックを利用します。

  ``null`` を指定した場合、圧縮は行われません。

設定ファイルで ``com.asakusafw.output.sequencefile.compression.codec`` を指定
  上記の設定値に ``CompressionCodec`` [#]_ を実装したクラス名を指定すると、その圧縮コーデックを利用します。

  なお、利用する圧縮コーデックはあらかじめHadoopクラスターの全台に導入されている必要があります。

上記いずれの指定もない場合、シーケンスファイルの圧縮を行いません。

..  note::
    上記の設定はシーケンスファイル作成時のみ有効です。
    シーケンスファイルを読み出す際には、シーケンスファイルの圧縮形式を自動的に判別します。

..  [#] ``org.apache.hadoop.io.compress.CompressionCodec``

内部データ形式を利用したシーケンスファイル形式のDataFormatの作成
================================================================

シーケンスファイル対し、Asakusa Frameworkで利用するデータモデル形式を直接保存したり復元したりするような ``DataFormat`` の実装クラスを自動的に生成するには、対象のデータモデルに ``@directio.sequence_file`` を指定します。

..  code-block:: dmdl

    @directio.sequence_file
    document = {
        "the name of this document"
        name : TEXT;

        "the content of this document"
        content : TEXT;
    };

上記のように記述してデータモデルクラスを生成すると、 ``<出力先パッケージ>.sequencefile.<データモデル名>SequenceFileFormat`` というクラスが自動生成されます。
このクラスは ``DataFormat`` を実装し、対象のデータモデルオブジェクトをHadoopの直列化機構を直接利用したシーケンスファイルを取り扱えます。

また、 単純な :ref:`directio-dsl-input-description` と :ref:`directio-dsl-output-description` の骨格も自動生成します。
前者は ``<出力先パッケージ>.sequencefile.Abstract<データモデル名>SequenceFileInputDescription`` 、後者は ``<出力先パッケージ>.sequencefile.Abstract<データモデル名>SequenceFileOutputDescription`` というクラス名で生成します。
必要に応じて継承して利用してください。

この機能を利用するには、DMDLコンパイラのプラグインに ``asakusa-directio-dmdl`` を追加する必要があります。
DMDLコンパイラについては :doc:`../dmdl/user-guide` を参照してください。

..  attention::
    シーケンスファイルの形式や、内部データのバイナリ表現はHadoopやAsakusa Frameworkのメジャーバージョンアップの際に変更になる場合があります。
    データを長期にわたって保管する場合、CSVなどのポータブルな形式を利用することを推奨します。

..  hint::
    :doc:`../application/gradle-plugin` の手順に従ってプロジェクトテンプレートから作成したプロジェクトは、これらのライブラリやプラグインがSDKアーティファクトという依存性定義によってデフォルトで利用可能になっています。
    詳しくは :doc:`../application/sdk-artifact` を参照してください。

..  hint::
    DMDLのデータモデル定義で、同一のデータモデルに ``@directio.csv`` と ``@directio.sequence_file`` の両方を指定することもできます。

..  hint::
    シーケンスファイルの中身をテキスト形式で確認する場合、以下のコマンドを利用すると便利です。

    ..  code-block:: sh

        hadoop fs -libjars "$ASAKUSA_HOME/core/lib/asakusa-runtime-all.jar,$ASAKUSA_HOME/batchapps/<バッチID>/lib/jobflow-<フローID>.jar" -text "<path/to/sequence-file>"

