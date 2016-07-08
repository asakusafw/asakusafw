===========================
Direct I/OのCSVファイル連携
===========================

この文書では、Direct I/OからCSVフォーマットのファイルをインポート/エクスポートする方法について説明します。

CSV形式のDataFormatの作成
=========================

CSV形式 [#]_ に対応した ``DataFormat`` の実装クラスを自動的に生成するには、対象のデータモデルに対応するDMDLスクリプトに ``@directio.csv`` を指定します。

..  code-block:: dmdl

    @directio.csv
    document = {
        "the name of this document"
        name : TEXT;

        "the content of this document"
        content : TEXT;
    };

上記のように記述してデータモデルクラスを生成すると、 ``<出力先パッケージ>.csv.<データモデル名>CsvFormat`` というクラスが自動生成されます。
このクラスは ``DataFormat`` を実装し、データモデル内のプロパティが順番に並んでいるCSVを取り扱えます。

また、 単純な :ref:`directio-dsl-input-description` と :ref:`directio-dsl-output-description` の骨格も自動生成します。
前者は ``<出力先パッケージ>.csv.Abstract<データモデル名>CsvInputDescription`` 、後者は ``<出力先パッケージ>.csv.Abstract<データモデル名>CsvOutputDescription`` というクラス名で生成します。必要に応じて継承して利用してください。

この機能を利用するには、DMDLコンパイラのプラグインに ``asakusa-directio-dmdl`` を追加する必要があります。
DMDLコンパイラについては :doc:`../dmdl/user-guide` を参照してください。

..  hint::
    :doc:`../application/gradle-plugin` の手順に従ってプロジェクトテンプレートから作成したプロジェクトは、これらのライブラリやプラグインがSDKアーティファクトという依存性定義によってデフォルトで利用可能になっています。詳しくは :doc:`../application/sdk-artifact` を参照してください。

..  note::
    この機構は :doc:`WindGate <../windgate/user-guide>` のものと将来統合されるかもしれません。

..  [#] ここでのCSV形式は、 :rfc:`4180` で提唱されている形式を一部変更したものです。
    文字セットをASCIIの範囲外にも拡張したり、CRLF以外にもLFのみも改行と見なしたり、ダブルクウォート文字の取り扱いを緩くしたりなどの拡張を加えています。
    `CSV形式の注意点`_ も参照してください。

CSV形式の設定
-------------

``@directio.csv`` 属性には、次のような要素を指定できます。

..  list-table:: CSV形式の設定
    :widths: 10 10 20 60
    :header-rows: 1

    * - 要素
      - 型
      - 既定値
      - 内容
    * - ``charset``
      - 文字列
      - ``"UTF-8"``
      - ファイルの文字エンコーディング
    * - ``allow_linefeed``
      - 論理値
      - ``FALSE``
      - ``TRUE`` で値内にLFを含められる。 ``FALSE`` で不許可
    * - ``has_header``
      - 論理値
      - ``FALSE``
      - ``TRUE`` でヘッダの利用を許可。 ``FALSE`` で不許可
    * - ``force_header``
      - 論理値
      - ``FALSE``
      - ``TRUE`` でヘッダの利用を許可し、ヘッダの形式チェックを行わない。 ``FALSE`` で不許可
    * - ``true``
      - 文字列
      - ``"true"``
      - ``BOOLEAN`` 型の ``TRUE`` 値の表現形式 [#]_
    * - ``false``
      - 文字列
      - ``"false"``
      - ``BOOLEAN`` 型の ``FALSE`` 値の表現形式 [#]_
    * - ``date``
      - 文字列
      - ``"yyyy-MM-dd"``
      - ``DATE`` 型の表現形式
    * - ``datetime``
      - 文字列
      - ``"yyyy-MM-dd HH:mm:ss"``
      - ``DATETIME`` 型の表現形式
    * - ``compression``
      - 文字列
      - なし（非圧縮）
      - ファイルの圧縮形式

..  [#] 設定した文字列と完全一致する場合のみ ``TRUE`` 値となり、それ以外は ``FALSE`` 値になります。
        ``BOOLEAN`` 型はデータ読み込み時に `自動トリム`_ が行われないため、入力データにスペースなどが入っている場合でも ``FALSE`` となる点に注意してください。

..  [#] データ出力時の文字列表現としてのみ使用されます。
        データ入力時の ``BOOLEAN`` 値の判定には使用されません。

なお、 ``date`` および ``datetime`` には ``SimpleDateFormat`` [#]_ の形式で日付や時刻を指定します。

また、 ``compression`` には、 ``"gzip"`` または ``CompressionCodec`` [#]_ のサブタイプのクラス名を指定します [#]_ 。
ここで指定した圧縮形式で対象のファイルが読み書きされるようになりますが、代わりに :ref:`directio-input-split` が行われなくなります。

..  attention::
    デフォルトでは ``allow_linefeed`` には ``FALSE`` が設定されていて、文字列の内部などに改行文字 LF を含められないようになっています。
    この設定を ``TRUE`` にすることでLFを含められるようになりますが、代わりに :ref:`directio-input-split` が行われなくなります。
    詳しくは `CSV形式の注意点`_ を参照してください。

以下はDMDLスクリプトの記述例です。

..  code-block:: dmdl

    @directio.csv(
        charset = "ISO-2022-JP",
        allow_linefeed = TRUE,
        has_header = TRUE,
        true = "1",
        false = "0",
        date = "yyyy/MM/dd",
        datetime = "yyyy/MM/dd HH:mm:ss",
        compression = "gzip",
    )
    model = {
        ...
    };

..  [#] ``java.text.SimpleDateFormat``
..  [#] ``org.apache.hadoop.io.compress.CompressionCodec``
..  [#] ``org.apache.hadoop.io.compress.DefaultCodec`` などが標準で用意されています

ヘッダの設定
------------

`CSV形式の設定`_ でヘッダを有効にしている場合、出力の一行目にプロパティ名が表示されます。
ここで表示される内容を変更するには、それぞれのプロパティに ``@directio.csv.field`` 属性を指定し、さらに ``name`` 要素でフィールド名を指定します。

以下はヘッダの内容の付加したDMDLスクリプトの記述例です。

..  code-block:: dmdl

    @directio.csv
    document = {
        "the name of this document"
        @directio.csv.field(name = "題名")
        name : TEXT;

        "the content of this document"
        @directio.csv.field(name = "内容")
        content : TEXT;
    };

ファイル情報の取得
------------------

解析中のCSVファイルに関する属性を取得する場合、それぞれ以下の属性をプロパティに指定します。

..  list-table:: ファイル情報の取得に関する属性
    :widths: 4 2 4
    :header-rows: 1

    * - 属性
      - 型
      - 内容
    * - ``@directio.csv.file_name``
      - ``TEXT``
      - ファイル名
    * - ``@directio.csv.line_number``
      - ``INT`` , ``LONG``
      - テキスト行番号 (1起算)
    * - ``@directio.csv.record_number``
      - ``INT`` , ``LONG``
      - レコード番号 (1起算)

上記の属性が指定されたプロパティは、CSVのフィールドから除外されます。

..  attention::
    ``@directio.csv.line_number`` または ``@directio.csv.record_number`` が指定された場合、 :ref:`directio-input-split` が行われなくなります。
    詳しくは `CSV形式の注意点`_ を参照してください。

..  attention::
    これらの属性はCSVの解析時のみ有効です。
    CSVを書き出す際には無視されます。

CSVから除外するプロパティ
-------------------------

特定のプロパティをCSVのフィールドとして取り扱いたくない場合、プロパティに ``@directio.csv.ignore`` を指定します。

自動トリム
----------

入力データの読み込み時に、プロパティ型が ``TEXT`` および ``BOOLEAN`` 以外のプロパティについては、入力データに対してトリムが行われます。

CSV形式の注意点
---------------
自動生成でサポートするCSV形式を利用するうえで、いくつかの注意点があります。

* 改行文字は CRLF または LF のみ、CRのみです

  * ただしCRのみを利用している場合、入力データの分割が正しく行われません

* CSVに空の文字列を書き出しても、読み出し時に ``null`` として取り扱われます
* 論理値は復元時に、値が ``true`` で指定した文字列の場合には ``true`` , 空の場合には ``null`` , それ以外の場合には ``false`` となります
* ヘッダが一文字でも異なる場合、解析時にヘッダとして取り扱われません
* 1レコードが10MBを超える場合、正しく解析できません
* 以下のいずれかが指定された場合、 :ref:`directio-input-split` は行われなくなります

  * ``@directio.csv( compression = ... )``
  * ``@directio.csv( allow_linefeed = TRUE )``
  * ``@directio.csv.line_number``
  * ``@directio.csv.record_number``

