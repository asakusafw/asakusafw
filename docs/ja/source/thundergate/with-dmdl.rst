=======================
DMDLとThunderGateの連携
=======================

Asakusa Framework 0.2では、ThunderGateが利用している
データベースのテーブル定義情報や、ビューの定義情報などから
対応するDMDLスクリプトを生成できるようになっています。
このため、0.1で提供していたモデルジェネレータは非推奨となりました。

0.2以降では、ThunderGateが利用しているMySQLの
メタデータを分析し、対応するデータモデルをDMDLの形式で
生成する「DMDLジェネレータ」を提供しています。

MySQLのメタデータからDMDLを生成する
===================================

DMDLジェネレータはMySQLに登録されたテーブル定義やビュー定義から、
DMDLのレコードモデル、結合モデル、集計モデルをそれぞれ生成します。

* `テーブル定義 (レコードモデル)`_
* `ビュー定義 (結合モデル)`_
* `ビュー定義 (集計モデル)`_

なお、DMDLジェネレータはインフォメーションスキーマ [#]_ を利用してメタデータを分析しています。

..  [#] http://dev.mysql.com/doc/refman/5.1/ja/information-schema.html

テーブル定義 (レコードモデル)
-----------------------------

MySQLに定義されたテーブルごとに、DMDLのレコードモデルを生成します。
また、テーブルのカラムごとにプロパティが定義されます。

それぞれの名前は、次のような規則でDMDLでの名前に変換されます。

* 名前の先頭と末尾にくる全てのアンダースコアを取り除く
* 2つ以上連続するアンダースコアを単一のアンダースコアに読み替える
* 全ての文字を小文字にする

たとえば、テーブル ``_TX__HELLO_WORLD`` からは、レコードモデル ``tx_hello_world`` を生成します。

..  caution::
    上記の変換によってDMDLでの名前が重複してしまう場合、エラーとなります。

カラムに指定できる型と、DMDLでの表現は以下の通りです。

..  list-table:: カラムの型とDMDLでの型
    :widths: 1 1
    :header-rows: 1

    * - MySQLでのデータ型
      - 対応するDMDLの型
    * - TINYINT
      - BYTE
    * - SMALLINT
      - SHORT
    * - INT
      - INT
    * - BIGINT
      - LONG
    * - DECIMAL
      - DECIMAL
    * - FLOAT
      - FLOAT
    * - DOUBLE
      - DOUBLE
    * - DATE
      - DATE
    * - DATETIME
      - DATETIME
    * - TIMESTAMP
      - DATETIME
    * - CHAR
      - TEXT
    * - VARCHAR
      - TEXT
    * - TINYTEXT
      - TEXT
    * - TEXT
      - TEXT
    * - MEDIUMTEXT
      - TEXT
    * - LONGTEXT
      - TEXT

たとえば、以下のようなテーブルについて考えます。

..  code-block:: sql

    CREATE TABLE TABLE_EXAMPLE (
        SID              BIGINT        PRIMARY KEY AUTO_INCREMENT,
        VERSION_NO       BIGINT,
        RGST_DATETIME    DATETIME,
        UPDT_DATETIME    DATETIME,
        NAME             VARCHAR(255),
        VALUE            INT
    ) engine=InnoDB;

上記のテーブルから生成されるレコードモデルの構造は次のような形です。

..  code-block:: none

    table_example = {
        sid : LONG;
        version_no : LONG;
        rgst_datetime : DATETIME;
        updt_datetime : DATETIME;
        name : TEXT;
        value : INT;
    };


ビュー定義 (結合モデル)
-----------------------

2つのテーブルを結合した「結合モデル」を生成するには、MySQLにテーブルやビューの結合を行うようなビューを登録します。

このビューは、次のような文で生成されたものである必要があります。

* 2つのテーブルやビューとで、等価内部結合を行っている
* SELECT句で結合元のカラムを、演算を行わずにそのまま [#]_ 指定しているおり、それぞれにはテーブル名またはエイリアスが付与されている [#]_ (t1.SIDなど)
* 等価結合条件以外のWHERE句が存在しない

..  [#] ここでは演算を行えません。たとえば、「t1.SID + 1」などのようには書けません
..  [#] 2つの結合元で同じカラム名が存在しない場合は省略できます

たとえば、以下のような文で作成されたビューから、結合モデルを作成できます。

..  code-block:: none

    CREATE VIEW JOIN_EXAMPLE AS
    SELECT
        t1.SID AS SID1,
        t2.SID AS SID2,
        t1.VALUE AS VALUE
    FROM TABLE_EXAMPLE1 t1, TABLE_EXAMPLE2 t2
    WHERE
        t1.VALUE = t2.VALUE;

上記のビューから生成される結合モデルの構造は次のような形です。

..  code-block:: none

    joined join_example = table_example1 -> {
        sid -> sid1;
        value -> value;
    } % value + table_example2 -> {
        sid -> sid2;
        value -> value;
    } % value;

ビュー定義 (集計モデル)
-----------------------
テーブルをグループ化して集計した結果のデータモデル(集計モデル)を生成するには、MySQLにテーブルやビューの集計を行うようなビューを登録します。

このビューは、次のような文で生成されたものである必要があります。

* あらゆる結合を行っていない
* GROUP BY句でグループ化カラムを指定している
* GROUP BY句で指定した全てのカラムは、演算を行わずにSELECT句に指定している
* SELECT句に指定する式は、演算を行わないカラムか、集計のみを行ったカラムのみ 
* WHERE句が存在しない

たとえば、以下のような文で作成されたビューから、集計モデルクラスを作成できます。

..  code-block:: sql

    CREATE VIEW SUMMARIZE_EXAMPLE AS
    SELECT
        NAME AS NAME,
        SUM(VALUE) AS VALUE,
        COUNT(SID) AS SIZE
    FROM TABLE_EXAMPLE
    GROUP BY NAME;

上記のビューから生成される集計モデルの構造は次のような形です。

..  code-block:: none

    summarized summarize_example = table_example => {
        any name -> name;
        sum value -> value;
        count sid -> size;
    } % name;

DMDLジェネレータの実行
======================

MySQLのメタデータからDMDLスクリプトを生成するには、
DMDLジェネレータを利用します。

DMDLの生成
----------
DMDLスクリプトからJavaデータモデルクラスを生成する場合、
Asakusa Frameworkの ``asakusa-thundergate-dmdl-*.jar`` の
:javadoc:`com.asakusafw.dmdl.thundergate.Main` クラスを次の引数で起動します。

..  code-block:: none

    -jdbc      JDBCの設定情報を記載したプロパティファイル
    -output    DMDLスクリプトを出力するディレクトリ
    -encoding  出力するDMDLスクリプトのエンコーディング (default: UTF-8)
    -includes  対象とするテーブル/ビュー名の正規表現パターン (default: 全て)
    -excludes  除外とするテーブル/ビュー名の正規表現パターン (default: なし)

また、 ``-jdbc`` の引数には、下記のような情報を含むファイルのパスを指定します。
このファイルはJavaの ``*.properties`` ファイル形式で記述します。

.. code-block:: none

    jdbc.driver = <JDBCドライバーのクラス名>
    jdbc.url = <接続先のJDBC URL>
    jdbc.user = <接続ユーザー名>
    jdbc.password = <接続パスワード>
    database.name = <接続先データベース名>

キャッシュのサポート
~~~~~~~~~~~~~~~~~~~~
ThunderGateのキャッシュ機能をサポートするデータモデルを生成するには、コマンドライン引数に次の内容を追加します。

..  code-block:: none

    -sid_column        System IDのカラム名
    -timestamp_column  最終更新時刻のカラム名

初期設定では、ThunderGateはSystem IDのカラム名に ``SID`` 、最終更新時刻のカラム名に ``UPDT_DATETIME`` を利用しています。
そのため、ここでの引数は ``-sid_column SID -timestamp_column UPDT_DATETIME`` となります。

削除フラグのサポート
~~~~~~~~~~~~~~~~~~~~
テーブルに定義された削除フラグカラムをキャッシュに利用する場合、コマンドラインの引数に次の内容を追加します。

..  code-block:: none

    -delete_flag_column  論理削除フラグのカラム名
    -delete_flag_value   論理削除フラグが真(TRUE)となる値

削除フラグのカラムに利用できる型は以下に限られています。
それぞれの値は、整数、ダブルクウォートした文字列、または大文字の論理値で指定します。

..  list-table:: 利用できる型と値
    :widths: 4 4
    :header-rows: 1

    * - 型
      - 値の例
    * - CHAR, VARCHAR
      - ``"1"``, ``"T"``, ``"D"``, など
    * - TINYINT
      - ``1``, ``0``, など
    * - BOOLEAN
      - ``TRUE``, ``FALSE``

上記の情報は、データベースに対して1組のみ指定できます。
テーブルに削除フラグのカラムが定義されていない場合には、それに対応するデータモデルが削除をサポートしません。

..  attention::
    文字列型の値には、かならず文字列をダブルクウォートで括ってやる必要があります。
    コマンドラインシェルから文字列型の値を指定する際には ``'"1"'`` のようにさらにシングルクウォートで括るなどしてください。

生成されるデータモデルの属性
----------------------------

DMDLジェネレータが生成するDMDLスクリプトには、
ThunderGateが利用する様々な属性が付けられています。

以下は、DMDLジェネレータが単純なテーブルに対して生成した
DMDLスクリプトの例です。

.. code-block:: none

    "テーブルTGCACHE_SOURCE"
    @auto_projection
    @namespace(value = table)
    @thundergate.name(value = "TGCACHE_SOURCE")
    @thundergate.primary_key(value = { sid })
    @thundergate.cache_support(
        sid = sid,
        timestamp = updt_datetime,
        delete_flag = delete_flag,
        delete_flag_value = "1"
    )
    tgcache_source = {
        "SID"
        @thundergate.name(value = "SID")
        sid : LONG;
        "VERSION_NO"
        @thundergate.name(value = "VERSION_NO")
        version_no : LONG;
        "RGST_DATETIME"
        @thundergate.name(value = "RGST_DATETIME")
        rgst_datetime : DATETIME;
        "UPDT_DATETIME"
        @thundergate.name(value = "UPDT_DATETIME")
        updt_datetime : DATETIME;
        "CATEGORY"
        @thundergate.name(value = "CATEGORY")
        category : INT;
        "DELETE_FLAG"
        @thundergate.name(value = "DELETE_FLAG")
        delete_flag : TEXT;
    };

``@thundergate.`` から始まる属性は、DMDLジェネレータが独自に拡張している属性です。
そのため、DMDLジェネレータが生成するDMDLスクリプトからプログラムを生成する際には、
DMDLコンパイラのプラグインの指定に ``asakusa-thundergate-dmdl-*.jar`` の指定が必要です。

オリジナル名の属性
~~~~~~~~~~~~~~~~~~

データモデルの定義に ``@thundergate.name(value = "<名前>")`` を指定すると、
データモデルの元になったテーブル名やビュー名を保持させられます。

これらの情報は、Asakusa DSLからThunderGateを利用する際にも利用されます [#]_ 。

..  [#] ``DbImporterDescription`` や ``DbExporterDescription`` を利用する際に、
    テーブル名やカラム名などを省略していますが、かわりにここで指定した名前を利用しています。

主キー属性
~~~~~~~~~~

``@thundergate.primary_key(value = {<主キーの一覧>})`` を指定すると、
主キーとして取り扱われるプロパティの情報を保持させられます。

この情報は、ThunderGateがエクスポート処理を高速化する際になどに利用しています。

射影モデルの登録
~~~~~~~~~~~~~~~~

DMDLジェネレータが生成するデータモデルには、
自動射影の属性 ``@auto_projection`` が付けられています。

このため、独自に射影モデルを定義して、DMDLジェネレータが生成した
データモデルと併せてDMDLコンパイラに渡すと、
射影モデルを自動的に登録させられます。

自動射影や射影モデルについては、 :doc:`../dmdl/user-guide` も参考にしてください。

キャッシュサポート
~~~~~~~~~~~~~~~~~~

``@thundergate.cache_support(...)`` を指定すると、対象のデータモデルはThunderGateのキャッシュ機能をサポートします。
これには以下のような項目を指定できます。

..  list-table:: キャッシュサポートの項目
    :widths: 6 2 10
    :header-rows: 1

    * - 項目
      - 必須
      - 内容
    * - ``sid``
      - ○
      - System IDに対応するプロパティ名
    * - ``timestamp``
      - ○
      - 最終更新時刻に対応するプロパティ名
    * - ``delete_flag``
      - ×
      - 削除フラグに対応するプロパティ名
    * - ``delete_flag_value``
      - ×
      - 削除フラグが成立する値


