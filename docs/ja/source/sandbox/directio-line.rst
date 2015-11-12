=================================
Direct I/O のテキストファイル連携
=================================

Direct I/Oで任意のテキストファイルを行ごとに読み書きするための機能として、「Direct I/O line」を利用することができます。

Direct I/O lineはファイル内の行文字列とデータモデル内の1つの文字列型プロパティをマッピングする機能のみを提供します。
行文字列の解析、生成などの処理はバッチアプリケーションの演算子として記述します。

Direct I/O lineは、以下のような用途に利用することを想定しています。

* Direct I/Oが対応していないファイルフォーマットの入出力

  * 例えば `JSON <http://json.org>`_ や `LTSV <http://ltsv.org>`_ といったフォーマットを扱う場合に、行文字列をパースする処理と組み合わせて利用します。
* 入力ファイルの整形や形式変換、バリデーションチェックなどの事前処理

  * 例えばCSVファイルの一部にDirect I/Oでは直接扱えないような形式が含まれる場合に、事前に形式の変換を行うといった用途で利用します。

Mavenアーティファクト
=====================

Direct I/O lineはAsakusa FrameworkのMavenリポジトリにグループID ``com.asakusafw.sandbox`` を持つMavenアーティファクトとして登録されています。

..  list-table:: Direct I/O lineで使用するMavenアーティファクト
    :widths: 5 5
    :header-rows: 1

    * - グループID
      - アーティファクトID
    * - ``com.asakusafw.sandbox``
      - ``asakusa-directio-dmdl-ext``
      
..  note::
    Sandoxモジュールとして提供するDirect I/Oの各フォーマット拡張機能は同一のMavenアーティファクトで提供しています。

Direct I/O lineの利用方法
=========================

Direct I/O lineを使用する場合はアプリケーションプロジェクトの :file:`build.gradle` の ``dependencies`` ブロック内に依存定義を追加します。

..  code-block:: groovy

    dependencies {
        ...
        compile group: 'com.asakusafw.sandbox', name: 'asakusa-directio-dmdl-ext', version: asakusafw.asakusafwVersion

データモデルクラスの生成
========================

Direct I/O lineを利用するには、DMDLに対してDirect I/O lineを利用することを示す拡張属性 ``@directio.line``  を追加します。

シンプルな例
------------

以下はDirect I/O lineと連携するデータモデル定義の最もシンプルな例です。

..  code-block:: none

    @directio.line
    model = {
        line : TEXT;
    };

データモデルに拡張属性 ``@directio.line`` を指定して、 ``TEXT`` 型のプロパティを1つだけ定義すると、
このプロパティが行文字列の入出力対象として使用されます。

ファイル形式の設定
------------------

``@directio.line`` 属性には、次のような要素を指定できます。

..  list-table:: Direct I/O lineのファイル形式の設定
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
    * - ``compression``
      - 文字列
      - なし（非圧縮）
      - ファイルの圧縮コーデック

``compression`` には、 ``"gzip"`` または ``org.apache.hadoop.io.compress.CompressionCodec`` のサブタイプのクラス名を指定します [#]_ 。
ここで指定した圧縮形式で対象のファイルが読み書きされるようになりますが、代わりにファイルの分割読み出しが行われなくなります。

以下はDMDLスクリプトの記述例です。

..  code-block:: none

    @directio.line(
        charset="ISO-2022-jp",
        compression="gzip",
    )
    model = {
        ...
    };

..  [#] ``org.apache.hadoop.io.compress.DefaultCodec`` などが標準で用意されています

改行文字について
----------------

Direct I/O lineでは、ファイル内の改行文字を以下のように扱います。

* ファイルの入力時には CRLF または LF を改行文字として扱います。
* ファイルの出力時には、 LF を改行文字として出力します。

モデルプロパティの設定
----------------------

Direct I/O lineで利用できるモデルプロパティの設定について説明します。

以下は、モデルプロパティの設定を指定したDMDLスクリプトの記述例です。

..  code-block:: none

    @directio.line
    model = {
        // 行文字列の格納(TEXT型)
        @directio.line.body
        body : TEXT;

        // ファイルパスの格納(TEXT型)
        @directio.line.file_name
        path : TEXT;

        // 行番号の格納(INTまたはLONG型)
        @directio.line.line_number
        num : INT;

        // その他のプロパティは無視
        other : TEXT;
    };

行文字列用プロパティの指定
~~~~~~~~~~~~~~~~~~~~~~~~~~

データモデル内で行文字列を格納するプロパティを指定するには、以下の属性をプロパティに指定します。

..  list-table:: ファイル情報の取得に関する属性
    :widths: 4 2 4
    :header-rows: 1

    * - 属性
      - 型
      - 内容
    * - ``@directio.line.body``
      - ``TEXT``
      - 行文字列

データモデル内にプロパティが複数ある場合、この属性を付与したプロパティに行文字列が格納され、その他のプロパティは無視されます。
``@directio.line.body`` を指定できるプロパティは1つのみです。

なお、データモデル内に ``TEXT`` 型を持つプロパティが1つのみ存在するようにデータモデルを定義した場合、
``@directio.line.body`` を付与しなくても自動的にそのプロパティが行文字列を格納するプロパティとして自動認識されます。
先述の `シンプルな例`_ で示すデータモデルの例はこの自動認識の機能が利用されています。

..  attention::
    行文字列のプロパティ値が ``null`` のデータモデルオブジェクトをファイルに出力した場合、行は追加されません。

ファイル情報の取得
~~~~~~~~~~~~~~~~~~

解析中のファイルに関する属性を取得する場合、以下の属性をプロパティに指定します。

..  list-table:: ファイル情報の取得に関する属性
    :widths: 4 2 4
    :header-rows: 1

    * - 属性
      - 型
      - 内容
    * - ``@directio.line.file_name``
      - ``TEXT``
      - ファイル名
    * - ``@directio.line.line_number``
      - ``INT`` , ``LONG``
      - テキスト行番号 (1起算)

..  attention::
    ``@directio.line.line_number`` が指定された場合、 :ref:`directio-input-split` が行われなくなります。
    
..  attention::
    これらの属性はファイルを読み込みの解析時のみ有効です。
    ファイルを書き出す際には無視されます。

データモデルクラス生成コマンド
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

データモデルクラスの生成は通常のDMDLで提供する方法と同様に、Gradleの :program:`compileDMDL` タスクを実行して生成します。

..  code-block:: sh

    ./gradlew compileDMDL

Asakusa DSLの記述
=================

Direct I/O lineを使った場合のAsakusa DSLの記述については、基本的な流れは他のデータフォーマットを使った場合と同様です。
詳しくは、 :doc:`../directio/start-guide` などを参照してください。

以下ではAsakusa DSLの記述に関して、Direct I/O lineと他のデータフォーマットで異なる部分についてのみ説明します。

ファイルシステム上のファイルを入力に利用する
--------------------------------------------

処理対象のファイルをインポートしてHadoopの処理を行う場合、 `データモデルクラスの生成`_ で生成した ``<パッケージ名>.line.Abstract<データモデル名>LineInputDescription`` クラスのサブクラスを作成して必要な情報を記述します。

ファイルシステム上にファイルを出力する
--------------------------------------

ジョブフローの処理結果をファイルにエクスポートする場合、 `データモデルクラスの生成`_ で生成した ``<パッケージ名>.line.Abstract<データモデル名>LineOutputDescription`` クラスのサブクラスを作成して必要な情報を記述します。
