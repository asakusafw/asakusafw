==============================
Asakusa DSLとThunderGateの連携
==============================

この文書では、ThunderGateと連携してデータベースを操作するバッチアプリケーションを
Asakusa DSLで記述する方法について紹介します。

ThunderGateについての情報は、 :doc:`user-guide` を参照して下さい。

基本的な連携の方法
==================
Asakusa DSLで定義するジョブフローは、
ThunderGateと連携することで入出力にデータベースのテーブルを利用できます。

..  note::
    単一のジョブフローの中で、インポートとエクスポートの対象にできるデータベースは1つまでです。
    ただし、 `補助インポータ`_ の機能を利用すると、制限つきで複数のデータベースからインポートできます。

データベースのテーブルからインポートする
----------------------------------------
ThunderGateと連携してデータベースのテーブルからデータをインポートする場合、
``DbImporterDescription`` [#]_ クラスのサブクラスを作成して必要な情報を記述します。
このクラスでは、下記のメソッドをオーバーライドします。

``String getTargetName()``
    インポータが使用するターゲット名（データソースを表す識別子）を戻り値に指定します。
    インポータは実行時に $ASAKUSA_HOME/bulkloader/conf 配下に配置した[ターゲット名]-jdbc.properties に記述されたデータベース接続情報定義ファイルを使用してデータベースに対するアクセスを行います。
    このファイルの内容については :doc:`user-guide` を参照して下さい。

``Class<?> getModelType()``
    インポータが処理対象とするデータモデルオブジェクトの型を表すクラスを戻り値に指定します。
    インポータは実行時にデータモデルクラスを作成する元となったテーブル名に対してインポート処理を行います [#]_ 。

``LockType getLockType()``
    インポータの処理時に行われるロックの種類を戻り値に指定します。
    指定可能なロック種別については ``BulkLoadImporterDescription.LockType`` のAPIリファレンスを参照して下さい [#]_ 。

``String getWhere()``
    インポータが利用する抽出条件をSQLの条件式で指定します。
    指定する文字列はMySQL形式の ``WHERE`` 以降の文字列である必要があります。

    ここには ``${変数名}`` の形式で、バッチ起動時の引数やあらかじめ宣言された変数を利用できます。
    利用可能な変数はコンテキストAPIで参照できるものと同様です。
    詳しくは :doc:`../dsl/user-guide` を参照してください。

``DataSize getDataSize()``
    インポートするデータのおおよそのサイズを指定します。
    コンパイラはこのデータサイズをヒントに実行計画を作成します。
    省略された場合にはデータサイズが不明であるという前提で実行計画を作成します。

以下の例では、テーブルから生成したデータモデルクラス ``Hoge`` に対応するテーブルの全データをロードして、その際にテーブルロックを取得します。

..  code-block:: java

    public class HogeFromDb extends DbImporterDescription {

        public String getTargetName() {
            return "asakusa";
        }

        public Class<?> getModelType() {
            return Hoge.class;
        }

        public LockType getLockType() {
            return LockType.TABLE;
        }
    }

以下の例では、バッチ引数で指定した ``parameter`` 変数を利用して条件式を指定しています。

..  code-block:: java

    public class HogeFromDb extends DbImporterDescription {

        public String getTargetName() {
            return "asakusa";
        }

        public Class<?> getModelType() {
            return Hoge.class;
        }

        public String getWhere() {
            return "NAME = '${parameter}'";
        }

        public LockType getLockType() {
            return LockType.ROW;
        }
    }

..  [#] :javadoc:`com.asakusafw.vocabulary.bulkloader.DbImporterDescription`
..  [#] DMDLを直接記述してデータモデルクラスを作成している場合、 ``DbImporterDescription`` の代わりに ``BulkLoadImporterDescription`` [#]_ を利用して下さい
..  [#] :javadoc:`com.asakusafw.vocabulary.bulkloader.BulkLoadImporterDescription.LockType`
..  [#] :javadoc:`com.asakusafw.vocabulary.bulkloader.BulkLoadImporterDescription`

データベースのテーブルにエクスポートする
----------------------------------------
ThunderGateと連携してジョブフローの処理結果をデータベースのテーブルに書き出すには、
``DbExporterDescription`` [#]_ クラスのサブクラスを作成して必要な情報を記述します。
このクラスでは、下記のメソッドをオーバーライドします。

``String getTargetName()``
    エクスポータが使用するターゲット名（データソースを表す識別子）を戻り値に指定します。
    利用方法はインポータの ``getTargetName()`` と同様です。

``Class<?> getModelType()``
    エクスポータが処理対象とするデータモデルオブジェクトの型を表すクラスを戻り値に指定します。
    エクスポータは実行時にデータモデルクラスを作成する元となったテーブル名に対してエクスポート処理を行います [#]_ 。

以下の例では、テーブルから生成したデータモデルクラス ``Hoge`` に対応するテーブルに対して、ジョブフローの処理結果を書き戻します。

..  code-block:: java

    public class HogeIntoDb extends DbExporterDescription {

        public Class<?> getModelType() {
            return Hoge.class;
        }
    }

..  [#] :javadoc:`com.asakusafw.vocabulary.bulkloader.DbExporterDescription`
..  [#] DMDLを直接記述してデータモデルクラスを作成している場合、 ``DbExporterDescription`` の代わりに ``BulkLoadExporterDescription`` [#]_ を利用して下さい
..  [#] :javadoc:`com.asakusafw.vocabulary.bulkloader.BulkLoadExporterDescription`

補助インポータ
==============
補助インポータは、１つのジョブフロー中に通常のインポートやエクスポート処理を行うデータベースとは別の、
データベースからデータをインポートする際に使用するインポータです。

通常のインポータはデータの更新を前提としてロック取得 (排他制御) の指定を行いますが、
補助インポータは指定したテーブルに対してデータを参照のみを行います。
つまり、補助インポータを利用すると、「他のデータベースからマスタデータなどの参照データを読み出せる」ということになります [#]_ 。

補助インポータを使用してインポート処理を行うには、
``SecondaryImporterDescription`` [#]_ を継承したクラス(インポート処理記述クラス)を作成し、必要なメソッドをオーバーライドします。
同クラスに指定するメソッドを以下に示します。

``String getTargetName()``
    補助インポータが使用するターゲット名（データソースを表す識別子）を戻り値に指定します。
    通常のインポータとは異なるターゲット名を指定します。補助インポータ実行時にはターゲット名に対応するデータベース接続情報定義ファイルを配置しておく必要があります。
    データベース接続情報定義ファイルの定義方法は通常のインポータと同様です。

``Class<?> getModelType()``
    補助インポータが処理対象とするデータモデルオブジェクトの型を表すクラスを戻り値に指定します。
    利用方法は通常のインポータと同様です。

``String getWhere()``
    補助インポータが利用する抽出条件をSQLの条件式で指定します。
    利用方法は通常のインポータと同様です。

``DataSize getDataSize()``
    このインポータが取り込むデータサイズの分類を指定します。
    利用方法は通常のインポータと同様です。

以下の例では、テーブルから生成したデータモデルクラス ``Foo`` に対応するテーブルの全データをロードします。
また、その時に利用するデータベースは ``other`` というターゲット名で指定しています。

以下の例では、テーブルから生成したデータモデルクラス ``Hoge`` に対応するテーブルに対して、ジョブフローの処理結果を書き戻します。

..  code-block:: java

    /**
     * 補助インポータの動作を定義する。
     */
    public class SecondaryImporterExample extends SecondaryImporterDescription {

        @Override
        public String getTargetName() {
            return "other";
        }

        @Override
        public Class<?> getModelType() {
            return Foo.class;
        }

        // 補助インポータはgetLockType()をオーバーライドできない。
    }
..  **


以下は補助インポータを利用する場合の注意点です。

* 補助インポータでないインポータのターゲットは、ジョブフロー中で1種類までです

  * ``DbImporterDescription`` を使う場合、 ``getTargetName()`` はジョブフロー中で全て同じものにしてください
  * ``SecondaryImporterDescription`` が、 ``DbImporterDescription`` と同じターゲット名を指定することは可能です

* エクスポータのターゲットは、通常のインポータと同じターゲットにしてください

  * 通常のインポータでターゲットAを指定し、エクスポータと補助インポータにターゲットBを指定、のようなことはできません
  * 通常のインポータを一つも利用しない場合、エクスポータのターゲット名は何を指定してもかまいません

..  [#] これとは逆の「補助エクスポータ」のような仕組みは現在提供していません
..  [#] :javadoc:`com.asakusafw.vocabulary.bulkloader.SecondaryImporterDescription`

.. _thundergate-dup-check:

重複チェック機能
================
エクスポータの拡張機能で、新しいレコードをテーブルに追加する際に、特定のカラムが同じデータが既にデータベース上にあるかどうかをチェックできます。
重複データがデータベース上に既に存在する場合には、そのデータを通常のテーブルには追加せずに、かわりにエラー情報のテーブルに追加します。
この機能は既存のレコードに対しては利用できず、 **新しいレコードを追加する際にだけ利用できます** 。

重複チェックを行う場合、まずは次のようなテーブルが必要です。

* 正常レコードを登録するテーブル

  * 重複チェック用のカラムがテーブルに存在すること

* 重複したレコードを登録するエラーテーブル

  * エラーコードを格納するカラム(CHAR/VARCHAR型)がテーブルに存在すること

この機能の利用方法を、2つのケースに分けて説明します。

正常テーブルよりもエラーテーブルの情報が少ない場合
--------------------------------------------------
正常テーブルよりもエラーテーブルの情報が少ない場合に、重複チェックを行う方法を紹介します。
このとき、正常テーブルとエラーテーブルは次のような関係であるとします。

* 正常テーブル

  * 必要な業務情報やシステムカラムを含んでいる
  * 重複チェック用のカラムを含んでいる

* エラーテーブル

  * 正常テーブルの一部または全部のカラムが、同じ名前で存在する
  * さらに、エラーコードを格納するカラムが存在する (正常テーブルに含まれていなくてよい)

つまり、正常テーブルにない情報をエラーテーブルに設定したい場合 [#]_ には、この方法は利用できません。
この場合には `正常テーブルとエラーテーブルの構造が大きく異なる場合`_ を参照して下さい。

重複チェックを行うには ``DbExporterDescription`` の代わりに ``DupCheckDbExporterDescription`` [#]_ を継承したエクスポータ記述を作成します。

..  code-block:: java

    /**
     * 重複チェックつきエクスポータの動作を定義する (正常テーブル中心)。
     */
    public class DupCheckExporterExample1 extends DupCheckDbExporterDescription {

        @Override
        public String getTargetName() {
            return "asakusa";
        }

        @Override
        public Class<?> getModelType() {
            return Hoge.class;
        }
        
        @Override
        protected Class<?> getNormalModelType() {
            return Hoge.class;
        }

        @Override
        protected Class<?> getErrorModelType() {
            return HogeError.class;
        }

        @Override
        protected List<String> getCheckColumnNames() {
            return Arrays.asList("VALUE");
        }

        @Override
        protected String getErrorCodeColumnName() {
            return "ERR_CODE";
        }

        @Override
        protected String getErrorCodeValue() {
            return "999";
        }
    }
..  **

それぞれのオーバーライドしたメソッドでは、以下のように設定します。

``getTargetName()``
    エクスポータが使用するターゲット名（データソースを表す識別子）を戻り値に指定します。
    利用方法は通常のエクスポータやインポータと同様です。

``getModelType()``
    正常テーブルのテーブルモデルクラスを返します。

``getNormalModelType()``
    正常テーブルのテーブルモデルクラスを返します。

``getErrorModelType()``
    エラーテーブルのテーブルモデルクラスを返します。

``getCheckColumnNames()``
    重複チェックを行うカラム名の一覧を返します。
    この値は、正常テーブルのテーブルモデルに存在するカラムを指定する必要があります。

``getErrorCodeColumnName()``
    エラーコードを格納するカラム名を返します。
    この値は、エラーテーブルに実際に存在するカラム名である必要があります。

``getErrorCodeValue()``
    重複チェックに失敗した場合に設定されるエラーコードです。
    この値は重複チェックに失敗したレコードがエラーテーブルに格納される際に、上記「エラーコードを格納するカラム」に自動的に設定されます。


..  [#] エラーコードを格納するカラムだけは、正常テーブルになくても大丈夫です
..  [#] :javadoc:`com.asakusafw.vocabulary.bulkloader.DupCheckDbExporterDescription`


正常テーブルとエラーテーブルの構造が大きく異なる場合
----------------------------------------------------
正常テーブルよりもエラーテーブルの情報が多い場合や、正常テーブルとエラーテーブルの構造が大きく異なる場合には、
それらの両方のプロパティを持つデータモデルを予め作成する必要があります。
ここでは、そのようなデータモデルを「ユニオンモデル」と仮に呼ぶことにします。

なお、ここで想定する正常テーブルとエラーテーブルは次のような制約があるものとします。

* 正常テーブル

  * 重複チェック用のカラムを含んでいる

* エラーテーブル

  * エラーコードを格納するカラムが存在する

ユニオンモデルは、上記の2つのテーブルの全てのカラムを持つようなデータ構造である必要があります [#]_ 。

正常テーブルの名前が ``NORMAL_TABLE``, エラーテーブルの名前が ``ERROR_TABLE`` である場合、
ユニオンモデルはDMDLで次のように記述できます [#]_ 。

..  code-block:: java

    union_model = normal_table + error_table;

上記の記述によって、 ``UnionModel`` という名前のユニオンモデルを作成できます。

また、ジョブフローやフロー部品では、ユニオンテーブルのテーブルモデルを使って処理を行います。
ユニオンテーブルのテーブルモデルをエクスポートする際に、先ほどと同様に ``DupCheckDbExporterDescription`` を指定して、次のように書きます。

..  code-block:: java

    /**
     * 重複チェックつきエクスポータの動作を定義する (ユニオンモデル)。
     */
    public class DupCheckExporterExample2 extends DupCheckDbExporterDescription {

        @Override
        public String getTargetName() {
            return "asakusa";
        }

        @Override
        public Class<?> getModelType() {
            return UnionModel.class;
        }
        
        @Override
        protected Class<?> getNormalModelType() {
            return NormalTable.class;
        }

        @Override
        protected Class<?> getErrorModelType() {
            return ErrorTable.class;
        }

        @Override
        protected List<String> getCheckColumnNames() {
            return Arrays.asList("VALUE");
        }

        @Override
        protected String getErrorCodeColumnName() {
            return "ERR_CODE";
        }

        @Override
        protected String getErrorCodeValue() {
            return "999";
        }
    }

この構造は `正常テーブルよりもエラーテーブルの情報が少ない場合`_ とほとんど同じですが、メソッド ``getModelType()`` の戻り値が異なっています。
ジョブフローの出力もここに指定する型(ユニオンモデル)でなくてはならないことに注意して下さい。

全体としては以下のように設定します。

``getTargetName()``
    エクスポータが使用するターゲット名（データソースを表す識別子）を戻り値に指定します。
    利用方法は通常のエクスポータやインポータと同様です。

``getModelType()``
    ユニオンモデルクラスを返します。

``getNormalModelType()``
    正常テーブルのテーブルモデルクラスを返します。

``getErrorModelType()``
    エラーテーブルのテーブルモデルクラスを返します。

``getCheckColumnNames()``
    重複チェックを行うカラム名の一覧を返します。
    この値は、正常テーブルのテーブルモデルに存在するカラムを指定する必要があります。

``getErrorCodeColumnName()``
    エラーコードを格納するカラム名を返します。
    この値は、エラーテーブルに実際に存在するカラム名である必要があります。

``getErrorCodeValue()``
    重複チェックに失敗した場合に設定されるエラーコードです。
    この値は重複チェックに失敗したレコードがエラーテーブルに格納される際に、上記「エラーコードを格納するカラム」に自動的に設定されます。

この機能で想定するユースケースは、「別システムからの取り込みとクレンジング処理のバッチ」です。

* 取込みデータの形式をユニオンモデルで表す
* 正常テーブルは、業務に必要なカラムだけを含める
* エラーテーブルは、エラートラッキングに必要なカラムだけを含める
* 取込みデータをクレンジングして、エラーがあればエラーカラムに情報をセットして、エラーテーブルに情報を書き出す
* クレンジングしたデータは、重複チェック機能を使って正常テーブルに情報を書き出す

  * 重複チェックに成功した場合には、必要なカラムだけを正常テーブルに書き出す
  * 重複チェックに失敗した場合には、エラーカラムに「重複エラー」の情報を設定して、エラーテーブルに情報を書き出す

..  [#] より厳密には、「エラーコードカラム」に対応するプロパティはユニオンモデルに不要です
..  [#] DMDLの利用方法は、 :doc:`../dmdl/user-guide` を参照して下さい


キャッシュ機能
==============
ThunderGateでは、インポート時に前回インポートからの差分のみを転送する「キャッシュ機能」が提供されています。
ただし、キャッシュを利用するには主に以下のような制限があります。

* 条件式 ( ``getWhere()`` )を利用できない
* ロック ( ``getLockType()`` )時に行単位のロックを指定できない
* 同一のキャッシュを複数のジョブフローで同時に利用できない

詳しい利用方法や、利用時の注意などは :doc:`../thundergate/cache` を参照してください。
