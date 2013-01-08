===========================
Direct I/OのTSVファイル連携
===========================

この文書では、Direct I/Oを使ってTSVフォーマットのファイルをインポート/エクスポートするための拡張機能の使い方について説明します。

TSVファイル連携用のPOM設定
==========================
Direct I/OのTSVファイル連携を使用するためには、 ``pom.xml`` に以下の依存性定義を追加します。

..  list-table:: TSVファイル連携で使用するMavenアーティファクト
    :widths: 5 5 
    :header-rows: 1

    * - グループID
      - アーティファクトID
    * - com.asakusafw.sandbox
      - asakusa-directio-dmdl-ext

..  code-block:: xml

	<dependencies>
    ...
		<dependency>
			<groupId>com.asakusafw.sandbox</groupId>
			<artifactId>asakusa-directio-dmdl-ext</artifactId>
			<version>0.5-SNAPSHOT</version>
			<scope>compile</scope>
		</dependency>

``pom.xml`` を編集したら、 このプロジェクトに対して（ ``pom.xml`` が存在するディレクトリ上で） ``mvn eclipse:eclipse`` を実行してEclipse用のクラスパス定義を更新します。

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

以下はDMDLスクリプトの記述例です。

..  code-block:: none

    @directio.tsv(
        charset = "ISO-2022-JP",
    )
    model = {
        ...
    };

Asakusa DSLの記述
=================
Direct I/OのTSVファイル連携を使った場合のAsakusa DSLの記述については、CSVファイル連携を使った場合と同じです。

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
* エンコーディングはUTF-8を使用します。
* NULL値は「\\n」で表します。
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


