===========================
Direct I/OのTSVファイル連携
===========================

この文書では、Direct I/Oを使ってTSVフォーマットのファイルをインポート/エクスポートするための拡張機能の使い方について説明します。

Mavenアーティファクト
=====================
Direct I/OのTSVファイル連携モジュールはAsakusa FrameworkのMavenリポジトリに
グループID ``com.asakusafw.sandbox`` を持つMavenアーティファクトとして登録されています。

..  list-table:: Direct I/OのTSVファイル連携で使用するMavenアーティファクト
    :widths: 5 5 
    :header-rows: 1

    * - グループID
      - アーティファクトID
    * - ``com.asakusafw.sandbox``
      - ``asakusa-directio-dmdl-ext``

TSVファイル連携モジュールの利用方法
===================================

Gradleプロジェクト
------------------
GradleプロジェクトでTSVファイル連携モジュールを使用する場合は
``build.gradle`` の ``dependencies`` ブロック内に ``compile`` 依存関係(コンフィグレーション)に対して
依存定義を追加します。

..  code-block:: groovy

    dependencies {
        ...
        compile group: 'com.asakusafw.sandbox', name: 'asakusa-directio-dmdl-ext', version: asakusafw.asakusafwVersion

Mavenプロジェクト
=================
MavenプロジェクトでTSVファイル連携モジュールを使用する場合は
Direct I/O用アーキタイプ ``asakusa-archetype-directio`` から作成したプロジェクトの ``pom.xml`` に以下の依存性定義を追加します。

..  code-block:: xml

	<dependencies>
        ...
		<dependency>
			<groupId>com.asakusafw.sandbox</groupId>
			<artifactId>asakusa-directio-dmdl-ext</artifactId>
			<version>${asakusafw.version}</version>
			<scope>compile</scope>
		</dependency>

モデルクラスの生成
==================

DMDLに対するTSVファイル用拡張属性の追加
---------------------------------------
TSVファイル連携を行うために、DMDLに対してTSVファイルを扱うことを示す拡張属性を追加します。

各モデルのDMDLスクリプトに対して、モデル名の記述行の前行に拡張属性 ``@directio.tsv`` を付与します。

以下にTSVファイルを扱う場合のDMDLスクリプトの例を示します。

..  code-block:: sh

    "テーブルEX1"
    ...
    @directio.tsv
    ex1 = {
        "SID"
        sid : LONG;
        "VALUE"
        value : INT;
        "STRING"
        string : TEXT;
    };

TSV形式の設定
~~~~~~~~~~~~~
``@directio.tsv`` 属性には、次のような要素を指定できます。

..  list-table:: TSV形式の設定
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
    * - ``has_header``
      - 論理値
      - ``FALSE``
      - ``TRUE`` に設定すると、読み込み時に先頭行をスキップし、書き込み時にモデルの定義内容に応じたヘッダ行を生成する。
    * - ``compression``
      - 文字列
      - なし
      - ファイルの圧縮コーデック

``compression`` には、 ``"gzip"`` または ``org.apache.hadoop.io.compress.CompressionCodec`` のサブタイプのクラス名を指定します [#]_ 。
ここで指定した圧縮形式で対象のファイルが読み書きされるようになりますが、代わりにファイルの分割読み出しが行われなくなります。

以下はDMDLスクリプトの記述例です。

..  code-block:: none

    @directio.tsv(
        charset = "ISO-2022-JP",
        has_header = TRUE,
        compression = "gzip",
    )
    model = {
        ...
    };

..  [#] ``org.apache.hadoop.io.compress.DefaultCodec`` などが標準で用意されています

ヘッダの設定
~~~~~~~~~~~~
`TSV形式の設定`_ でヘッダを有効にしている場合、出力の一行目にプロパティ名が表示されます。
ここで表示される内容を変更するには、それぞれのプロパティに ``@directio.tsv.field`` 属性を指定し、さらに ``name`` 要素でフィールド名を指定します。

以下はヘッダの内容の付加したDMDLスクリプトの記述例です。

..  code-block:: none

    @directio.tsv
    document = {
        "the name of this document"
        @directio.tsv.field(name = "題名")
        name : TEXT;

        "the content of this document"
        @directio.tsv.field(name = "内容")
        content : TEXT;
    };

..  attention::
    ヘッダの内容に対する検証は行いません。 Asakusa Frameworkが標準で提供している
    CSV形式 [#]_ のフォーマットではヘッダ行に対する検証を行ない、
    モデル定義に対してヘッダの内容が一文字でも異なる場合ヘッダ行として扱われませんが、
    TSV形式ではヘッダの設定を有効にした場合、常に先頭行に対してデータの読み込みをスキップします。

..  [#] CSV形式の詳細については、:asakusafw:`CSV形式のDataFormatの作成 <directio/user-guide.html#csvdataformat>` を参照してください。

ファイル情報の取得
~~~~~~~~~~~~~~~~~~
解析中のTSVファイルに関する属性を取得する場合、以下の属性をプロパティに指定します。

..  list-table:: ファイル情報の取得に関する属性
    :widths: 4 2 4
    :header-rows: 1

    * - 属性
      - 型
      - 内容
    * - ``@directio.tsv.file_name``
      - ``TEXT``
      - ファイル名

上記の属性が指定されたプロパティは、TSVのフィールドから除外されます。

..  attention::
    Direct I/O のCSV連携で提供している、行番号・レコード番号の取得機能 ( ``@directio.csv.line_number`` , ``@directio.csv.record_number`` )はTSV連携では提供していません。

..  attention::
    これらの属性はTSVの解析時のみ有効です。
    TSVを書き出す際には無視されます。

TSVから除外するプロパティ
~~~~~~~~~~~~~~~~~~~~~~~~~
特定のプロパティをCSVのフィールドとして取り扱いたくない場合、プロパティに ``@directio.tsv.ignore`` を指定します。

データモデルクラスの生成
~~~~~~~~~~~~~~~~~~~~~~~~
データモデルクラスの生成は通常のDMDLで提供する方法と同様に、Mavenの ``generate-sources`` フェーズを実行して生成します。

..  code-block:: sh

    mvn generate-sources

Asakusa DSLの記述
=================
Direct I/OのTSVファイル連携を使った場合のAsakusa DSLの記述については、基本的な流れはCSVファイル連携を使った場合と同様です。
Direct I/OのCSVファイルによる連携の基本的な流れについては、:asakusafw:`Direct I/O スタートガイド - Asakusa DSLの記述 <directio/start-guide.html#asakusa-dsl>` などを参照してください。
また、より詳細な情報は :asakusafw:`Direct I/O ユーザガイド <directio/user-guide.html>` を参照してください。

以下ではAsakusa DSLの記述に関して、Direct I/OのCSVファイル連携とTSVファイル連携で異なる部分についてのみ説明します。

ファイルシステム上のTSVファイルを入力に利用する
-----------------------------------------------
TSVファイルをインポートしてHadoopの処理を行う場合、 `データモデルクラスの生成`_ で生成した ``<パッケージ名>.tsv.Abstract<データモデル名>TsvInputDescription`` クラスのサブクラスを作成して必要な情報を記述します。

ファイルシステム上にTSVファイルを出力する
-----------------------------------------
ジョブフローの処理結果をTSVファイルにエクスポートする場合、 `データモデルクラスの生成`_ で生成した ``<パッケージ名>.tsv.Abstract<データモデル名>TsvOutputDescription`` クラスのサブクラスを作成して必要な情報を記述します。

TSVファイルフォーマット仕様
===========================
Direct I/OのTSV連携機能で扱うTSVファイルのフォーマット仕様について説明します。

..  warning::
    現時点では、本項のTSVファイルフォーマットは暫定仕様です。

TSVフォーマット概要
-------------------
* TSVファイルは、MySQLの ``SELECT ... INTO OUTFILE`` で、次の指定をした場合に生成されるファイルフォーマットと同一です（MySQL 5.1のデフォルト)
    * ``FIELDS TERMINATED BY '\t' ENCLOSED BY '' ESCAPED BY '\\'``
    * ``LINES TERMINATED BY '\n' STARTING BY ''``

TSVフォーマット詳細
-------------------
* 各フィールドをDMDLスクリプトの順番に記述します。
* フィールドの区切り文字にはタブ文字を使用します。
* レコードの区切り文字は改行(LF)を使用します。
    * CR+LF は使用できません。
* エスケープ文字には「\\」を使用します。
    * エスケープ文字そのもの、改行(LF)、タブ文字をデータとして扱う場合は「\\」を前に付加してエスケープします。
* 引用文字は使用しません。
* 最終レコードにも(LF)が必要です。
* NULL値は「\\N」で表します。
* 空文字はフィールド区切り文字間に何も文字を入れないことで表現します。
* 指数表記は使用しません。
* Booleanは0/1で表します。
    * 0:false , 1:true
* Date, Datetimeは以下の書式で表します。
    * Date: YYYY-MM-DD
    * Datetime: YYYY-MM-DD HH:MM:SS

TSVファイルのサンプル
---------------------
DMDLスクリプトに対応するTSVファイルの例を以下に示します。

サンプル:DMDLスクリプト
~~~~~~~~~~~~~~~~~~~~~~~
..  code-block:: java

    "テーブルEX1"
    ... 
    @directio.tsv
    ex1 = { 
        "SID"
        sid : LONG;
        "VALUE"
        value : INT;
        "STRING"
        string : TEXT;
    };  

サンプル:TSVファイル
~~~~~~~~~~~~~~~~~~~~
..  note::
    以下サンプルのドキュメント上の区切り文字はスペースになっていますが、実際のファイルはタブ文字を使用してください。

..  code-block:: java

    1	111	hoge1
    2	222	fuga2
    3	333	bar3
    4	111	hoge4
    5	222	fuga5
    6	333	bar6
    7	111	hoge7
    8	222	fuga8
    9	444	bar9


