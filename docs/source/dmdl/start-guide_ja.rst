==================
DMDLスタートガイド
==================

この文書では、asakusa-archetype-batchappを利用したプロジェクト構成で、DMDLを使ってJavaのデータモデルクラスを生成する方法について簡単に紹介します。

asakusa-archetype-batchappの利用方法については :doc:`../application/maven-archetype_ja` を参照してください。また、コマンドライン等からDMDLを利用する場合には、 :doc:`user-guide_ja` を参照してください。

DMDLを記述する
==============

Data Model Definition Language (DMDL)はAsakusa Frameworkで利用可能なデータモデルを定義するためのDSLです。DMDLスクリプトというファイルにデータモデルの名前や構造を定義し、DMDLコンパイラを実行することで、定義したデータモデルに対応するJavaのプログラムを自動的に生成します。

DMDLスクリプトを作成する
------------------------
asakusa-archetype-batchappを利用してプロジェクトを作成した場合、DMDLスクリプトはプロジェクトの ``src/main/dmdl`` フォルダ以下に配置してください。また、スクリプトのファイル名には ``.dmdl`` の拡張子を付けて保存してください。

データモデルを定義する
----------------------

データモデルを新たに定義するには、作成したDMDLスクリプト内に ``<データモデル名> = <プロパティ定義>;`` のように記述します。

..  code-block:: none

    model_name = {
        property_name : INT;
    };

ここでは、以下の点に注意してください。

* データモデル名やプロパティ名はすべて小文字で指定し、単語区切りにアンダースコアを利用する
* プロパティの型は、プロパティ名の右側にコロンをはさんで大文字で指定する
* データモデルやプロパティの末尾にはセミコロンを指定する

データモデル内に複数のプロパティを定義するには、 `{` と `}` の間に続けて指定します。

..  code-block:: none

    model_name = {
        property_name : INT;
        second_property : TEXT;
        third : DOUBLE;
    };

複数のデータモデルを定義するには、DMDLスクリプト内に続けて記述します。

..  code-block:: none

    model_name = {
        property_name : INT;
    };
    second_model = {
        prop : LONG;
    };
    third = {
        other : DATE;
    };

利用可能なプロパティの種類
--------------------------

それぞれのプロパティには下記のいずれかの型を指定できます。

..  list-table:: DMDLとJavaのデータ型
    :widths: 3 5
    :header-rows: 1

    * - 型の名前
      - 対応するJavaの型
    * - INT
      - IntOption
    * - LONG
      - LongOption
    * - FLOAT
      - FloatOption
    * - DOUBLE
      - DoubleOption
    * - TEXT
      - StringOption
    * - DECIMAL
      - DecimalOption
    * - DATE
      - DateOption
    * - DATETIME
      - DateTimeOption
    * - BOOLEAN
      - BooleanOption
    * - BYTE
      - ByteOption
    * - SHORT
      - ShortOption

複数のデータモデルを合成する
----------------------------

DMDLでは、定義した複数のデータモデルを組み合わせて新しいデータモデルを定義できます。

..  code-block:: none

    both = left + right;
    left = {
        left_value : INT;
    };
    right = {
        right_value : TEXT;
    };

上記のようにデータモデル定義の右辺で「モデル名 + モデル名」と記述した場合、それぞれのデータモデルで定義したプロパティをすべて持つような新しいデータモデルを定義します。この例では、以下のようなデータモデルを定義したことになります。

..  code-block:: none

    both = {
        left_value : INT;
        right_value : TEXT;
    };

なお、3つ以上のデータモデルを組み合わせることも可能です。

データモデルを拡張する
----------------------

以下のように他のデータモデルと新しいプロパティを合成して、新しいデータモデルを定義できます。

..  code-block:: none

    origin = {
        value : INT;
    };
    extended = origin + {
        extra : TEXT;
    };

上記のextendedでは、originで定義したプロパティvalueに加えて、新たにextraというプロパティを定義しています。このextendedは以下のような構造になります。

..  code-block:: none

    extended = {
        value : INT;
        extra : TEXT;
    };

このようにDMDLでは、他のデータモデルの定義や新たなプロパティの定義を組み合わせて、複雑なデータモデルを定義できます。

射影モデルを利用する
--------------------

Asakusa Framework 0.2で導入されたジェネリックデータフローを利用する場合、通常のデータモデルクラスのほかに、データモデルの一部を投影する「射影モデル」を利用します。DMDLを利用してこの射影モデルを記述するには、次のようにデータモデル定義の先頭に「projective」というキーワードを挿入します。

..  code-block:: none

    projective proj_model = {
        value : INT;
    };

上記のように記述した場合、proj_modelに対応するJavaのデータモデルクラスは生成されず、代わりに同様のプロパティを持つインターフェースが生成されます。このインターフェースを実装(implements)するデータモデルクラスを生成するには、次のようにデータモデル定義の右辺にこの射影モデルを利用します。

..  code-block:: none

    conc_model = proj_model + {
        other : INT;
    };

射影モデルをデータモデル定義の右辺に利用した場合、その射影モデルが定義するプロパティは、左辺のデータモデルにも自動的に追加されます。さらに、左辺のデータモデルは右辺に利用したすべての射影モデルをインターフェースとして実装します。
また、射影モデル自体を入れ子にすることも可能です。

..  code-block:: none

    projective super_proj = { a : INT; };
    projective sub_proj = super_proj + { b : INT; };

この場合、sub_projが生成するインターフェースは、super_projが生成するインターフェースのサブタイプになります。

Javaモデルクラスを生成する
==========================

DMDLコンパイラの起動
--------------------

DMDLスクリプトに記述したデータモデルからJavaのデータモデルクラスを生成するには、mvnコマンドを利用してDMDLコンパイラを実行します。これはMavenの ``generate-sources`` フェーズで自動的に起動しますので、プロジェクト内で以下のようにコマンドを実行します。

..  code-block:: sh

    mvn generate-sources

その他、 ``mvn package`` や ``mvn install`` などでも自動的にDMDLコンパイラが起動します。

ThunderGateとの連携
-------------------

asakusa-archetype-batchapを利用している場合、DMDLコンパイラの実行前にThunderGateが利用するデータベースの情報を分析して、データベース内に定義されたテーブルやビューの情報を元に、対応するデータモデルの定義を記述するDMDLを自動的に生成します。

ThunderGateとの連携について、詳しくは :doc:`with-thundergate_ja` を参照してください。

