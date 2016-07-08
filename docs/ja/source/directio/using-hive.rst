====================
Direct I/OのHive連携
====================

この文書では、Direct I/Oを使って `Apache Hive <https://hive.apache.org/>`_ （以下「Hive」）が対応するカラムナフォーマットのファイルを読み書きする方法について説明します。

..  attention::
    Asakusa Framework バージョン |version| では、Direct I/O のHive連携機能は試験的機能として提供しています。

..  seealso::
    Hiveの操作も含めたアプリケーション開発の全体の流れについては、 :doc:`../sandbox/asakusa-with-hive` も参照にしてください。

Hive連携モジュールの利用方法
============================

Direct I/OのHive連携モジュールを利用するには、利用するアプリケーションプロジェクトに対して以下のSDKアーティファクト（以下「Hive連携モジュールSDK」）を追加します。

..  list-table:: Direct I/OのHive連携で使用するアーティファクト
    :widths: 226 226
    :header-rows: 1

    * - グループID
      - アーティファクトID
    * - ``com.asakusafw.sdk``
      - ``asakusa-sdk-hive``

また、Hive連携モジュールの実行環境用のライブラリをAsakusa Frameworkのインストールディレクトリに追加します。

Gradleプロジェクトの設定
------------------------

GradleプロジェクトでHive 連携モジュールを使用する場合は :file:`build.gradle` に対して以下の定義を追加します。

* Framework Organizerに対してHiveの構成を有効化する

  * ``asakusafwOrganizer`` ブロックに ``hive.enabled true`` を追加
* Hive連携モジュールSDKを依存関係に追加する

  * ``dependencies`` ブロックの ``compile`` に対して ``asakusa-sdk-hive`` を 追加

以下、 ``build.gradle`` の設定例です。

..  code-block:: groovy

    asakusafwOrganizer {
        ...
        hive.enabled true
    }

    dependencies {
        ...
        compile group: 'com.asakusafw.sdk', name: 'asakusa-sdk-hive', version: asakusafw.asakusafwVersion
    }

上記の設定後、 :program:`installAsakusafw` タスクを実行して開発環境のAsakusa Frameworkを再インストールします。

Eclipseを利用している場合は、 :program:`eclipse` タスクを実行してEclipseのプロジェクト情報を再構成します。

..  attention::
    MavenプロジェクトはHiveとの連携機能をサポートしていません。Hiveとの連携機能を利用する場合はGradleプロジェクトを利用してください。

カラムナフォーマットファイルの入出力
====================================

Direct I/OのHive連携モジュールを利用することで、Direct I/OからHiveが対応するカラムナフォーマットファイルの入出力を行うことができます。

Asakusa Frameworkのバージョン |version| では、Hiveが対応するカラムナフォーマットのうち、以下のフォーマットに対するファイルの入出力に対応しています。

* `ORC File <https://cwiki.apache.org/confluence/display/Hive/LanguageManual+ORC>`_
* `Parquet <https://cwiki.apache.org/confluence/display/Hive/Parquet>`_

データモデルクラスの生成
========================

:doc:`csv-format` と同様に、データモデルクラス、及びファイル形式をマッピングする ``DataFormat`` の実装クラスは、DMDLコンパイラの拡張を利用して自動的に生成できます。

これらの生成機能は、DMDLコンパイラのプラグイン ``asakusa-hive-dmdl`` によって提供されます。
このコンパイラプラグインは上述の `Hive連携モジュールの利用方法`_ の設定を行うことで利用することができます。

ORC File形式のDataFormatの作成
------------------------------

ORC File形式に対応した ``DataFormat`` の実装クラスを自動的に生成するには、対象のデータモデルに対応するDMDLスクリプトに ``@directio.hive.orc`` を指定します。

..  code-block:: dmdl

    @directio.hive.orc
    document = {
        "the name of this document"
        name : TEXT;

        "the content of this document"
        content : TEXT;
    };

上記のように記述してデータモデルクラスを生成すると、 ``<出力先パッケージ>.hive.orc.<データモデル名>OrcFileFormat`` というクラスが自動生成されます。
このクラスは ``DataFormat`` を実装し、データモデルに対応するORC Fileを取り扱えます。

また、 :ref:`directio-dsl-input-description` と :ref:`directio-dsl-output-description` の骨格も自動生成します。
前者は ``<出力先パッケージ>.hive.orc.Abstract<データモデル名>OrcFileInputDescription`` 、後者は ``<出力先パッケージ>.hive.orc.Abstract<データモデル名>OrcFileOutputDescription`` というクラス名で生成します。
必要に応じて継承して利用してください。

ORC File形式の設定
~~~~~~~~~~~~~~~~~~

``@directio.hive.orc`` 属性には、次のような要素を指定できます。

..  list-table:: ORC File形式の設定
    :widths: 130 47 100 175
    :header-rows: 1

    * - 要素
      - 型
      - 既定値
      - 内容
    * - ``table_name``
      - 文字列
      - ``モデル名``
      - Hiveメタストア上のテーブル名
    * - ``field_mapping``
      - 文字列
      - ``position``
      - ファイル入力時の `カラム名のマッピング`_ 方式。 ``name`` : 名前マッピング, ``position`` : 位置マッピング
    * - ``on_missing_source``
      - 文字列
      - ``logging``
      - ファイル入力時に入力ファイル内にカラムがない場合の動作。 ``ignore`` : 無視, ``logging`` : 警告ログの出力, ``fail`` : エラー
    * - ``on_missing_target``
      - 文字列
      - ``logging``
      - ファイル入力時にデータモデル内にカラムがない場合の動作。 ``ignore`` : 無視, ``logging`` : 警告ログの出力, ``fail`` : エラー
    * - ``on_incompatible_type``
      - 文字列
      - ``fail``
      - ファイル入力時に入力ファイルとデータモデルでカラム型に互換性がない場合の動作。 ``ignore`` : 無視, ``logging`` : 警告ログの出力, ``fail`` : エラー
    * - ``format_version``
      - 文字列
      - ``ライブラリが持つ規定値``
      - ファイル出力時に使用するORC Fileのバージョン (後方互換性向け)。 ``0.11`` | ``0.12``
    * - ``compression``
      - 文字列
      - ``snappy``
      - ファイル出力時に使用する圧縮コーデック。 ``none`` | ``zlib`` | ``snappy`` | ``lzo``
    * - ``stripe_size``
      - 数値
      - ``67108864``
      - ファイル出力時に使用するORC Fileのストライプサイズ(バイト数)

``table_name`` には、Hive上のテーブル名を指定します。指定しない場合はデータモデル上のモデル名をテーブル名として使用します。

``field_mapping`` 、 ``on_missing_source`` 、 ``on_missing_target`` は、Direct I/Oがファイルを読み込む際に使用するデータモデルとのマッピング方式と、マッピングできないカラムが存在した場合の動作をそれぞれ指定します。
詳しくは後述の `カラム名のマッピング`_ を参照してください。

``on_incompatible_type`` には、Direct I/Oがファイルを読み込む際にORC File上のカラムデータ型とデータモデルのプロパティの型が対応していない場合の動作を指定します。
データモデルとHive、及び各ファイルフォーマットとのデータ型の対応については、 `データ型のマッピング`_ を参照してください。

``format_version`` はDirect I/Oで作成するORC Fileのバージョンを、ファイルを読み込むHiveのバージョンに合わせて指定します。
例えば、作成したファイルを Hive ``0.11`` で読む場合は、フォーマットバージョンに ``0.11`` と指定します。
Hiveのバージョンについては 後述の `Hiveのバージョンに関して`_ も合わせて参照してください。

以下はDMDLスクリプトの記述例です。

..  code-block:: dmdl

    @directio.hive.orc(
        table_name = "tb_lineitem",
        field_mapping = "name",
        on_missing_source = "fail",
        on_missing_target = "fail",
        on_incompatible_type = "fail",
        format_version = "0.11",
        compression = "none",
        stripe_size = 67108864,
    )
    document = {
        ...
    };

Parquet形式のDataFormatの作成
-----------------------------

Parquet形式に対応した ``DataFormat`` の実装クラスを自動的に生成するには、対象のデータモデルに対応するDMDLスクリプトに ``@directio.hive.parquet`` を指定します。

..  code-block:: dmdl

    @directio.hive.parquet
    document = {
        "the name of this document"
        name : TEXT;

        "the content of this document"
        content : TEXT;
    };

上記のように記述してデータモデルクラスを生成すると、 ``<出力先パッケージ>.hive.parquet.<データモデル名>ParquetFileFormat`` というクラスが自動生成されます。
このクラスは ``DataFormat`` を実装し、データモデルに対応するParquetを取り扱えます。

また、 :ref:`directio-dsl-input-description` と :ref:`directio-dsl-output-description` の骨格も自動生成します。
前者は ``<出力先パッケージ>.hive.parquet.Abstract<データモデル名>ParquetFileInputDescription`` 、後者は ``<出力先パッケージ>.hive.parquet.Abstract<データモデル名>ParquetFileOutputDescription`` というクラス名で生成します。
必要に応じて継承して利用してください。

Parquet形式の設定
~~~~~~~~~~~~~~~~~

``@directio.hive.parquet`` 属性には、次のような要素を指定できます。

..  list-table:: Parquet形式の設定
    :widths: 130 47 100 175
    :header-rows: 1

    * - 要素
      - 型
      - 既定値
      - 内容
    * - ``table_name``
      - 文字列
      - ``モデル名``
      - Hiveメタストア上のテーブル名
    * - ``field_mapping``
      - 文字列
      - ``position``
      - ファイル入力時の `カラム名のマッピング`_ 方式。 ``name`` : 名前マッピング, ``position`` : 位置マッピング
    * - ``on_missing_source``
      - 文字列
      - ``logging``
      - ファイル入力時に入力ファイル内にカラムがない場合の動作。 ``ignore`` : 無視, ``logging`` : 警告ログの出力, ``fail`` : エラー
    * - ``on_missing_target``
      - 文字列
      - ``logging``
      - ファイル入力時にデータモデル内にカラムがない場合の動作。 ``ignore`` : 無視, ``logging`` : 警告ログの出力, ``fail`` : エラー
    * - ``on_incompatible_type``
      - 文字列
      - ``fail``
      - ファイル入力時に入力ファイルとデータモデルでカラム型に互換性がない場合の動作。 ``ignore`` : 無視, ``logging`` : 警告ログの出力, ``fail`` : エラー
    * - ``format_version``
      - 文字列
      - ``v1``
      - ファイル出力時に使用するParquetのバージョン。 ``v1`` | ``v2``
    * - ``compression``
      - 文字列
      - ``snappy``
      - ファイル出力時に使用する圧縮コーデック。 ``uncompressed`` | ``gzip`` | ``snappy`` | ``lzo``
    * - ``block_size``
      - 数値
      - ``134217728``
      - ファイル出力時に使用するParquetのブロックサイズ(バイト数)
    * - ``data_page_size``
      - 数値
      - ``1048576``
      - ファイル出力時に使用するParquetのページサイズ(バイト数)
    * - ``dictionary_page_size``
      - 数値
      - ``1048576``
      - ファイル出力時に使用するParquetのディクショナリページサイズ(バイト数)
    * - ``enable_dictionary``
      - 論理値
      - ``TRUE``
      - ファイル出力時にParquetのディクショナリエンコーディングを使用するか。 ``TRUE`` :使用する, ``FALSE`` :使用しない
    * - ``enable_validation``
      - 論理値
      - ``FALSE``
      - ファイル出力時にParquetのデータスキーマの検査を行うか。 ``TRUE`` :検査する, ``FALSE`` :検査しない

``table_name`` には、Hive上のテーブル名を指定します。
指定しない場合はデータモデル上のモデル名をテーブル名として使用します。

``field_mapping`` 、 ``on_missing_source`` 、 ``on_missing_target`` は、Direct I/Oがファイルを読み込む際に使用するデータモデルとのマッピング方式と、マッピングできないカラムが存在した場合の動作をそれぞれ指定します。
詳しくは後述の `カラム名のマッピング`_ を参照してください。

``on_incompatible_type`` には、Direct I/Oがファイルを読み込む際にParquet上のカラムデータ型とデータモデルのプロパティの型が対応していない場合の動作を指定します。
データモデルとHive、及び各ファイルフォーマットとのデータ型の対応については、 `データ型のマッピング`_ を参照してください。

以下はDMDLスクリプトの記述例です。

..  code-block:: dmdl

    @directio.hive.parquet(
        table_name = "tb_lineitem",
        field_mapping = "name",
        on_missing_source = "fail",
        on_missing_target = "fail",
        on_incompatible_type = "fail",
        format_version = "v2",
        compression = "uncompressed",
        block_size = 134217728,
        data_page_size = 1048576,
        dictionary_page_size = 1048576,
        enable_dictionary = TRUE,
        enable_validation = FALSE
    )
    document = {
        ...
    };

モデルプロパティとカラムのマッピング
------------------------------------

データモデルのプロパティと各カラムナフォーマットのカラムとの対応付けについては、データモデルの要素やモデルプロパティの属性を指定することで様々な対応方法を設定することができます。

カラム名のマッピング
~~~~~~~~~~~~~~~~~~~~

データモデルのプロパティとカラムナフォーマットのカラムとのマッピングには `位置マッピング`_ と `名前マッピング`_ の2種類のマッピング方法があります。

位置マッピング
^^^^^^^^^^^^^^

位置マッピングはデータモデル内のプロパティ定義の順番でカラムナフォーマットのカラムとの対応を行います。
位置マッピングは :doc:`csv-format` のマッピング方法と同様の方法です。

位置マッピングを行うには、データモデルの要素 ``field_mapping`` の値に ``position`` を指定します。

名前マッピング
^^^^^^^^^^^^^^

名前マッピングはデータモデルのプロパティ名とカラムナフォーマットが保持するカラム名で対応を行います。

名前マッピングを行うには、データモデルの要素 ``field_mapping`` の値に ``name`` を 指定します。

データモデルのプロパティ名と異なる名前でカラムナフォーマットと名前マッピングを行いたい場合は、それぞれのモデルプロパティに ``@directio.hive.field`` 属性を指定し、さらに ``name`` 要素でフィールド名を指定します。

以下は名前マッピングの定義を付加したDMDLスクリプトの記述例です。

..  code-block:: dmdl

    @directio.hive.orc
    document = {
        "the name of this document"
        @directio.hive.field(name = "doc_name")
        name : TEXT;

        "the content of this document"
        @directio.hive.field(name = "doc_content")
        content : TEXT;
    };


マッピング失敗時の動作
^^^^^^^^^^^^^^^^^^^^^^

ファイル入力時にデータモデルのモデルプロパティとカラムナフォーマットファイルのカラム間の対応付けができなかった場合の動作は、データモデルの要素 ``on_missing_source`` と ``on_missing_target`` で指定します。

``on_missing_source`` はデータモデルのプロパティ名に対して、入力ファイル内にカラムがない場合の動作を指定します。
``on_missing_target`` は反対に、入力ファイル内のカラムに対して、データモデルのプロパティがない場合の動作を指定します。

各要素の値にはそれぞれ以下の値を設定することができます。

* ``ignore`` : マッピングの失敗を無視して処理を続行
* ``logging`` : マッピングが失敗したことを示す警告ログを出力して処理を続行
* ``fail`` : エラーとしてバッチ処理を異常終了

..  attention::
    ORC FileをHiveで生成する際に、利用するHiveのバージョンによってはファイルにカラム名の情報が出力されないようです。
    この場合、名前マッピングは利用できないため、位置マッピングの機能を利用する必要があります。

..  hint::
    ORC Fileにカラム情報が出力されているかどうかを確認する方法として、ORC File Dump Utility を利用することができます。
    このツールはHive CLIが利用できる環境で以下のコマンドを実行します。

    :program:`hive --orcfiledump <hdfs-location-of-orc-file>`

データ型のマッピング
~~~~~~~~~~~~~~~~~~~~

モデルプロパティのデータ型とカラムナフォーマットのデータ型との対応については、以下の2つのマッピングを考慮する必要があります。

a) モデルプロパティとHiveデータ型とのマッピング
b) Hiveデータ型とカラムナフォーマットのデータ型とのマッピング

たとえばあるデータ型について、a.のマッピングは対応しているが、b.のマッピングは対応していない、という場合にはそのままではそのプロパティを扱うことはできません。

そのような場合に、異なるデータ型としてそのプロパティを扱うための `マッピング型変換機能`_ を提供しています。
これは、a.のモデルプロパティとHiveデータ型とのマッピングにおいて、標準のデータ型のマッピングとは異なるデータ型へのマッピングを行う機能です。
これによりそのプロパティを取り扱うことを可能にしています。

モデルプロパティとHiveデータ型とのマッピング
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

モデルプロパティとHiveデータ型のマッピング定義は以下の通りです。

..  list-table:: モデルプロパティとHiveデータ型のマッピング
    :widths: 83 110 120 140
    :header-rows: 1

    * - DMDL [#]_
      - Hive (標準マッピング) [#]_
      - Hive (マッピング型変換) [#]_
      - 備考
    * - ``INT``
      - ``INT``
      - ``-``
      -
    * - ``LONG``
      - ``BIGINT``
      - ``-``
      -
    * - ``FLOAT``
      - ``FLOAT``
      - ``-``
      -
    * - ``DOUBLE``
      - ``DOUBLE``
      - ``-``
      -
    * - ``TEXT``
      - ``STRING``
      - * ``VARCHAR``
        * ``CHAR``
      - ``VARCHAR`` はHive ``0.12`` 以降から利用可能、 ``CHAR`` はHive ``0.13`` 以降から利用可能
    * - ``DECIMAL``
      - ``DECIMAL``
      - * ``DECIMAL(精度とスケールの指定)``
        * ``STRING``
      - 精度とスケールの指定はHive ``0.13`` 以降から利用可能
    * - ``DATE``
      - ``DATE``
      - * ``TIMESTAMP``
        * ``STRING``
      - ``DATE`` はHive ``0.12`` 以降から利用可能
    * - ``DATETIME``
      - ``TIMESTAMP``
      - * ``STRING``
      - Hiveの ``TIMESTAMP`` 型が保持するミリ秒以下の情報はマッピング時に切り捨て
    * - ``BOOLEAN``
      - ``BOOLEAN``
      - ``-``
      -
    * - ``BYTE``
      - ``TINYINT``
      - ``-``
      -
    * - ``SHORT``
      - ``SMALLINT``
      - ``-``
      -

..  attention::
    上表で記載が無いHiveデータ型( ``BINARY`` 、及び ``ARRAY`` などの Complex Types）には対応していません。

..  attention::
    Hiveの ``TIMESTAMP`` 型はタイムゾーンを保持しません。
    複数の異なるタイムゾーンを持つ環境間で ``TIMESTAMP`` 型を持つデータを扱う場合、タイムゾーンの差異によって異なる値が扱われる可能性があることに注意してください。

..  [#] DMDLで指定するプロパティの型です。詳しくは :doc:`../dmdl/user-guide` を参照してください

..  [#] モデルプロパティの型に対して、標準で対応するHiveのデータ型です。
        Hiveのデータ型について詳しくはHiveのドキュメント `LanguageManual Types <https://cwiki.apache.org/confluence/display/Hive/LanguageManual+Types>`_ などを参照してください。

..  [#] モデルプロパティの型に対して、 `マッピング型変換機能`_ が対応するHiveのデータ型です。

Hiveのバージョンに関して
^^^^^^^^^^^^^^^^^^^^^^^^

Asakusa Framework バージョン |version| では、Direct I/O の Hive連携モジュールにはHiveのバージョン ``1.1.1`` を使用しています。
実行環境のHiveとAsakusa Frameworkが利用するHiveのバージョンが異なる場合、データの互換性に対する注意が必要です。

例えば実行環境のHiveバージョンが ``0.11`` の場合、Asakusa Frameworkが利用するHiveのバージョンではHiveの ``VARCHAR`` 型や ``CHAR`` 型を持つファイルを生成することができますが、生成したファイルを実行環境のHiveは取り扱うことができません。

マッピング型変換機能
^^^^^^^^^^^^^^^^^^^^

マッピング型変換機能は、Direct I/Oがカラムナフォーマットのファイルを入出力する際に、モデルプロパティの型に対して `モデルプロパティとHiveデータ型とのマッピング`_ 表で定義されている標準マッピング以外のHiveデータ型として取り扱う機能です。

モデルプロパティに対して、 `モデルプロパティとHiveデータ型とのマッピング`_ 表の「Hive (マッピング型変換)」に記載されているHiveデータ型に対するマッピングを行うことが可能です。

マッピング型変換を行うには、それぞれのモデルプロパティにマッピング型変換用の属性を指定します。
属性によっては、さらにその属性が持つ各要素でデータ型の詳細情報を指定します。

マッピング型変換で利用可能な属性は以下の通りです。

..  list-table:: マッピング型変換
    :widths: 90 120 70 163
    :header-rows: 1

    * - 属性
      - 要素
      - 型 [#]_
      - 内容
    * - ``@directio.hive.string``
      - ``-``
      - * ``DECIMAL``
        * ``DATE``
        * ``DATETIME``
      - モデルプロパティをHiveの ``STRING`` 型にマッピング
    * - ``@directio.hive.decimal``
      - * ``precision`` :精度(1 - 38)
        * ``scale`` :スケール(0 - 38)
      - * ``DECIMAL``
      - モデルプロパティを精度とスケールを持つHiveの ``DECIMAL`` 型にマッピング
    * - ``@directio.hive.timestamp``
      - ``-``
      - * ``DATE``
      - モデルプロパティをHiveの ``TIMESTAMP`` 型にマッピング ( ``DATE`` からのマッピングでは時刻は常に ``00:00:00`` )
    * - ``@directio.hive.char``
      - * ``length`` :最大文字列長(1 - 255)
      - * ``TEXT``
      - モデルプロパティをHiveの ``CHAR`` 型にマッピング
    * - ``@directio.hive.varchar``
      - * ``length`` :最大文字列長(1 - 65535)
      - * ``TEXT``
      - モデルプロパティをHiveの ``VARCHAR`` 型にマッピング

..  [#] この属性を指定することが可能なDMDLのプロパティの型です。

以下はマッピング変換機能の定義を付加したDMDLスクリプトの記述例です。

..  code-block:: dmdl

    item = {
        @directio.hive.char(length = 2)
        item_no : TEXT;

        @directio.hive.decimal(precision = 20, scale = 4)
        unit_selling_price : DECIMAL;

        @directio.hive.string
        extended_price : DECIMAL;

        @directio.hive.timestamp
        order_date : DATE;

        @directio.hive.varchar(length = 1024)
        memo : TEXT;
    };


Hiveデータ型とカラムナフォーマットのデータ型とのマッピング
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Hiveデータ型とカラムナフォーマットのデータ型とのマッピングにおける制約ついては、Hiveの以下のカラムナフォーマットのドキュメントを参照してください。

* `LanguageManual ORC <https://cwiki.apache.org/confluence/display/Hive/LanguageManual+ORC>`_
* `Parquet <https://cwiki.apache.org/confluence/display/Hive/Parquet>`_

..  attention::
    Asakusa Framework バージョン |version| では、Direct I/OはHiveのバージョン ``1.1.1`` のライブラリを使用しています。
    そのため、Parquetに関しては上記のHiveのドキュメントに記載がある通り、DATEデータ型がサポートされていないことに注意してください。

カラムナフォーマットファイルから除外するプロパティ
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

特定のプロパティをカラムナフォーマットファイルのカラムとして取り扱いたくない場合、プロパティに ``@directio.hive.ignore`` を指定します。

関連機能
========

Hive DDLの生成
--------------

アプリケーションの開発にGradleプロジェクトを利用している場合、Hive連携モジュールを利用するDMDLスクリプトからHiveのDDLを生成する :program:`generateHiveDDL` タスクを利用することができます。

..  code-block:: sh

    ./gradlew generateHiveDDL

:program:`generateHiveDDL` タスクを実行すると、プロジェクトの :file:`build/hive-ddl` ディレクトリ配下にHiveのDDL文を含むSQLファイルが生成されます。

詳しくは、 :doc:`../application/gradle-plugin` - :ref:`gradle-plugin-task-hiveddl` を参照してください。

ライブラリキャッシュの利用
--------------------------

Hive連携モジュールが使用する実行ライブラリはファイルサイズが大きいため、Hadoopジョブの実行のつどHadoopクラスターにライブラリを配布することでパフォーマンスに悪影響を与える可能性があります。
このため、ライブラリキャッシュの設定を行いHive連携モジュール用のライブラリをキャッシュすることを推奨します。

ライブラリキャッシュの利用方法については詳しくは、 :doc:`../administration/configure-library-cache` を参照してください。

