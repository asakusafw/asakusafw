==================
DMDLスタートガイド
==================

この文書では :doc:`../introduction/start-guide` の構成で、DMDLを使ってJavaのデータモデルクラスを生成する方法について簡単に紹介します。

..  seealso::
    DMDLの詳しい記述方法を知りたい場合や、コマンドライン等からDMDLを利用したい場合には、 :doc:`user-guide` を参照してください。

DMDLを記述する
==============

Data Model Definition Language (DMDL)はAsakusa Frameworkで利用可能なデータモデルを定義するためのDSLです。
DMDLスクリプトというファイルにデータモデルの名前や構造を定義し、DMDLコンパイラを実行することで、定義したデータモデルに対応するJavaのプログラムを自動的に生成します。

DMDLスクリプトを作成する
------------------------

:doc:`../introduction/start-guide` の流れに従ってプロジェクトを作成した場合、DMDLスクリプトはプロジェクトの :file:`src/main/dmdl` ディレクトリ以下に配置してください。
また、スクリプトのファイル名には :file:`.dmdl` の拡張子を付けて保存してください。

データモデルを定義する
----------------------

データモデルを新たに定義するには、作成したDMDLスクリプト内に ``<データモデル名> = <プロパティ定義>;`` のように記述します。

..  code-block:: dmdl

    model_name = {
        property_name : INT;
    };

ここでは、以下の点に注意してください。

* データモデル名やプロパティ名はすべて小文字で指定し、単語区切りにアンダースコアを利用する
* プロパティの型は、プロパティ名の右側にコロンをはさんで大文字で指定する
* データモデルやプロパティの末尾にはセミコロンを指定する

データモデル内に複数のプロパティを定義するには、 ``{`` と ``}`` の間にプロパティを続けて指定します。

..  code-block:: dmdl

    model_name = {
        property_name : INT;
        second_property : TEXT;
        third : DOUBLE;
    };

複数のデータモデルを定義するには、DMDLスクリプト内にデータモデルを続けて記述します。

..  code-block:: dmdl

    model_name = {
        property_name : INT;
    };
    second_model = {
        prop : LONG;
    };
    third = {
        other : DATE;
    };

..  tip::
    DMDLスクリプトファイルを複数用意し、それぞれのファイルにデータモデルクラスを定義することも可能です。

利用可能なプロパティの種類
--------------------------

それぞれのプロパティには下記のいずれかの型を指定できます。

..  list-table:: DMDLとJavaのデータ型
    :widths: 3 5
    :header-rows: 1

    * - 型の名前
      - 対応するJavaの型
    * - ``INT``
      - ``IntOption``
    * - ``LONG``
      - ``LongOption``
    * - ``FLOAT``
      - ``FloatOption``
    * - ``DOUBLE``
      - ``DoubleOption``
    * - ``TEXT``
      - ``StringOption``
    * - ``DECIMAL``
      - ``DecimalOption``
    * - ``DATE``
      - ``DateOption``
    * - ``DATETIME``
      - ``DateTimeOption``
    * - ``BOOLEAN``
      - ``BooleanOption``
    * - ``BYTE``
      - ``ByteOption``
    * - ``SHORT``
      - ``ShortOption``

複数のデータモデルを合成する
----------------------------

DMDLでは、定義した複数のデータモデルを組み合わせて新しいデータモデルを定義できます。

..  code-block:: dmdl

    both = left + right;
    left = {
        left_value : INT;
    };
    right = {
        right_value : TEXT;
    };

上記のようにデータモデル定義の右辺で「モデル名 + モデル名」と記述した場合、それぞれのデータモデルで定義したプロパティをすべて持つような新しいデータモデルを定義します。
この例では、以下のようなデータモデルを定義したことになります。

..  code-block:: dmdl

    both = {
        left_value : INT;
        right_value : TEXT;
    };

なお、3つ以上のデータモデルを組み合わせることも可能です。

データモデルを拡張する
----------------------

以下のように他のデータモデルと新しいプロパティを合成して、新しいデータモデルを定義できます。

..  code-block:: dmdl

    origin = {
        value : INT;
    };
    extended = origin + {
        extra : TEXT;
    };

上記の ``extended`` では、 ``origin`` で定義したプロパティ ``value`` に加えて、新たに ``extra`` というプロパティを定義しています。
この ``extended`` は以下のような構造になります。

..  code-block:: dmdl

    extended = {
        value : INT;
        extra : TEXT;
    };

このようにDMDLでは、他のデータモデルの定義や新たなプロパティの定義を組み合わせて、複雑なデータモデルを定義できます。


データモデルクラスを生成する
============================

DMDLコンパイラの起動
--------------------

DMDLスクリプトに記述したデータモデルからJavaのデータモデルクラスを生成するには、Gradleを利用してDMDLコンパイラを実行します。

これはGradleの :program:`compileDMDL` タスクで起動するので、プロジェクト内で以下のようにコマンドを実行します。

..  code-block:: sh

    ./gradlew compileDMDL

その他、 :program:`compileJava` タスクや :program:`build` タスクなどでも自動的にDMDLコンパイラが起動します。

Direct I/Oとの連携
------------------

:doc:`../introduction/start-guide` の構成では、Direct I/Oと連携したバッチアプリケーションを作成できます。

Direct I/Oを利用するプロジェクト構成の場合、Direct I/Oに関するデータ変換を行うプログラムをDMDLから自動生成できます。
詳しい情報は :doc:`../directio/index` を参照してください。

WindGateとの連携
----------------

WindGateとの連携について、詳しい情報は :doc:`../windgate/index` を参照してください。
