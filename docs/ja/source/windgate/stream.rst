=========================
WindGateのTSVファイル連携
=========================

この文書では、WindGateを使ってTSVフォーマットのローカルファイルをインポート/エクスポートするための拡張機能の使い方について説明します。

Mavenアーティファクト
=====================
WindGateのTSVファイル連携モジュールはAsakusa FrameworkのMavenリポジトリに
グループID ``com.asakusafw.sandbox`` を持つMavenアーティファクトとして登録されています。

..  list-table:: WindGateのTSVファイル連携で使用するMavenアーティファクト
    :widths: 5 5 
    :header-rows: 1

    * - グループID
      - アーティファクトID
    * - ``com.asakusafw.sandbox``
      - ``asakusa-windgate-dmdl-ext``

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
        compile group: 'com.asakusafw.sandbox', name: 'asakusa-windgate-dmdl-ext', version: asakusafw.asakusafwVersion

Mavenプロジェクト
=================
MavenプロジェクトでTSVファイル連携モジュールを使用する場合は
WindGate用アーキタイプ ``asakusa-archetype-windgate`` から作成したプロジェクトの ``pom.xml`` に以下の依存性定義を追加します。

..  code-block:: xml

	<dependencies>
        ...
		<dependency>
			<groupId>com.asakusafw.sandbox</groupId>
			<artifactId>asakusa-windgate-dmdl-ext</artifactId>
			<version>${asakusafw.version}</version>
			<scope>compile</scope>
		</dependency>

モデルクラスの生成
==================

DMDLに対するTSVファイル用拡張属性の追加
---------------------------------------
TSVファイル連携を行うために、DMDLに対してTSVファイルを扱うことを示す拡張属性を追加します。

各モデルのDMDLスクリプトに対して、モデル名の記述行の前行に拡張属性 ``@windgate.stream_format(type="tsv")`` を付与します。

以下にTSVファイルを扱う場合のDMDLスクリプトの例を示します。

..  code-block:: sh

    "テーブルEX1"
    ...
    @windgate.stream_format(type="tsv")
    ex1 = {
        "SID"
        sid : LONG;
        "VALUE"
        value : INT;
        "STRING"
        string : TEXT;
    };

Asakusa DSLの記述
=================
WindGateのTSVファイル連携を使った場合のAsakusa DSLの記述については、CSVファイル連携を使った場合と同じです。

TSVファイルフォーマット仕様
===========================
WindGateのTSV連携機能で扱うTSVファイルのフォーマット仕様について説明します。

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
    @windgate.stream_format(type="tsv")
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


