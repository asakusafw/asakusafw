=====================
Cache for ThunderGate
=====================

この文書では、ThunderGateのキャッシュの使い方を概説します。

キャッシュ利用の準備
====================

キャッシュを利用するには、ThunderGateが利用するデータベース上に、次のテーブルが追加で必要になります。

..  code-block:: sql

   CREATE  TABLE __TG_CACHE_INFO (
        CACHE_ID VARCHAR(128) NOT NULL,
        CACHE_TIMESTAMP DATETIME NOT NULL,
        BUILT_TIMESTAMP DATETIME NOT NULL,
        TABLE_NAME VARCHAR(64) NOT NULL,
        REMOTE_PATH VARCHAR(255) NOT NULL,
        PRIMARY KEY (CACHE_ID),
        INDEX I_CACHE_INFO_TABLE_NAME (TABLE_NAME)
    ) ENGINE=InnoDB;
    
    CREATE  TABLE __TG_CACHE_LOCK (
        CACHE_ID VARCHAR(128) NOT NULL,
        EXECUTION_ID VARCHAR(128) NOT NULL,
        ACQUIRED DATETIME NOT NULL,
        PRIMARY KEY (CACHE_ID),
        INDEX I_CACHE_LOCK_EXECUTION_ID (EXECUTION_ID)
    ) ENGINE=InnoDB;

前者の ``__TG_CACHE_INFO`` は作成したキャッシュの情報を管理するテーブルです。
また、後者の ``__TG_CACHE_LOCK`` はキャッシュに対するロックを管理するテーブルです。
いずれも初期データは不要で、何らかの理由でキャッシュが破損した場合には、上記テーブルの内容を全て削除することで初期化できます。

キャッシュを利用しない場合には上記は不要ですが、キャッシュを利用する場合には必ず上記の形で作成してください。

..  attention::
    補助インポーター ( doc:`user-guide` を参照) でキャッシュを利用する場合、 そこにも上記のテーブルを用意してください。


キャッシュの仕組み
==================

ThunderGateのキャッシュ機能を有効にした場合、テーブルのインポート処理が行われた後にもそのインポートしたデータをHadoopクラスター上に保存しておきます。

次回に同じテーブルをインポートする際に、ThunderGateは前回インポートしたデータと今回インポートするデータの差分を検出し、変更がない部分については前回保存したデータを最利用します。
このため、変更頻度が低い巨大なテーブルでキャッシュを利用すると、ThunderGateのインポート時間を大幅に削減できます。


..  attention::
    現在、ThunderGateのキャッシュが有効なのはインポート時のみです。
    エクスポートによってデータの大半が書き換えられてしまうようなケースでは、あまり効果がありません。


差分更新
--------

ThunderGateでは、差分データを「最終更新時刻」を元に計算します。
これにはThunderGateのシステムカラム ``UPDT_DATETIME`` を利用しており、最後にキャッシュを作成した時刻以降に変更のあったデータのみを差分データとして抽出します。

抽出した差分データをHadoopクラスター上に転送したのち、前回インポートしたデータと作成した差分データを MapReduce でマージ処理します。
この処理によって、前回のインポートからの変更を反映したデータを高速に作成します。

また、ここで作成したデータは、次回の差分更新時に「前回のインポート結果」として再利用します。
つまり、キャッシュを利用すると、最初のインポート時にすべてのデータを転送した後は、毎回のインポートに前回からの差分データのみを転送することになります。

..  attention::
    キャッシュを正しく利用するため、ThunderGateの外部からデータベースの内容を変更する際には、
    必ず  ``UPDT_DATETIME`` カラムにデータベース上の現在時刻 ( ``NOW()`` ) を指定しなければなりません。


削除フラグ
----------

インポート対象したレコードに「削除フラグ」が設定されている場合、そのレコードはキャッシュ上で自動的に削除されます。
現在のキャッシュ機構では、レコードの削除を行う前に必ず削除フラグを設定してキャッシュに反映させる必要があります。

削除フラグの指定方法については、 `論理削除をサポートするデータモデル`_ を参照してください。

..  warning::
    削除フラグを設定せずにレコードを削除した場合、そのレコードはキャッシュ上に残り続けてしまいます。
    その場合、 `キャッシュ管理情報の削除`_ などの方法で、作成されたキャッシュを一度無効化する必要があります。


キャッシュデータの格納先
------------------------

キャッシュのデータはファイルシステム上の次の位置に保存されます。

* HDFS

  * /user/<ユーザ名>/thundergate/cache/<ターゲット名>/<テーブル名>/<キャッシュID>

* ローカルファイルシステム (スタンドアロンモード時)

  * ~/thundergate/cache/<ターゲット名>/<テーブル名>/<キャッシュID>

キャッシュはさらに、上記ディレクトリの以下に配置されます。

..  list-table:: キャッシュディレクトリ内の内容
    :widths: 4 4
    :header-rows: 1

    * - パス
      - 内容
    * - HEAD/cache.properties
      - キャッシュの管理情報
    * - HEAD/part-*
      - キャッシュされたテーブルデータ


キャッシュの利用
================

ThunderGateのキャッシュを利用する方法は簡単です。

まず、データベースを解析してデータモデルを作成する際に、 `キャッシュをサポートするデータモデル`_ として作成します。
つぎに、キャッシュを利用したいインポート処理に対して、 `キャッシュ利用の宣言`_ を行います。

この2つで、ThunderGateは自動的にキャッシュを利用したインポートを行います。


キャッシュをサポートするデータモデル
------------------------------------

バージョン0.2.3以降のMavenアーキタイプを利用しているプロジェクトの場合、データベースのテーブル情報を元に生成されるデータモデルには自動的にキャッシュをサポートする情報が付加 [#]_ されます。
そのようなDMDLをコマンドから生成する場合には、 :doc:`../dmdl/with-thundergate` を参照してください。


..  todo::
    navigate to migration guide

..  [#] implements com.asakusafw.thundergate.runtime.cache.ThunderGateCacheSupport


論理削除をサポートするデータモデル
----------------------------------

キャッシュをサポートするデータモデルに、さらに削除フラグを利用した論理削除をサポートさせるには、
テーブルからデータモデルを生成する際のオプションを変更します。

Mavenアーキタイプを利用する場合、プロジェクト直下の ``build.properties`` ファイルに次の内容を設定します。

..  list-table:: 論理削除のサポート
    :widths: 4 4
    :header-rows: 1

    * - 項目
      - 内容
    * - asakusa.modelgen.delete.column
      - 削除フラグのカラム名
    * - asakusa.modelgen.delete.value
      - 削除フラグが成立する値

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
    データベース内で削除フラグの構造が異なる場合については現在サポートしていません。

DMDLを生成するコマンドで上記を指定する場合には、 :doc:`../dmdl/with-thundergate` を参照してください。


キャッシュ利用の宣言
--------------------

インポート時にキャッシュを利用するには、 ``DbImporterDescription`` [#]_ クラスの ``isCacheEnabled()`` メソッドをオーバーライドし、 ``true`` を返すようにします。

..  code-block:: java


    public class SomeImporter extends DbImporterDescription {
    
        @Override public Class<?> getModelType() {
            return SomeDataModel.class;
        }
    
        @Override public String getTargetName() {
            return "asakusa";
        }
    
        @Override public LockType getLockType() {
            return LockType.UNUSED;
        }
    
        @Override
        public DataSize getDataSize() {
            return DataSize.LARGE;
        }
    
        @Override public boolean isCacheEnabled() {
            return true;
        }
    }


ただし、キャッシュを利用する際には次の制約があります。

* ``getModelType()`` に指定できるのは `キャッシュをサポートするデータモデル`_ のみ
* ``getWhere()`` は指定できない ( ``null`` を返す必要がある)
* ``getLockType()`` に指定できるのは ``UNUSED``, ``TABLE``, ``CHECK`` のみ
* ``getDataSize()`` に指定できるのは ``UNKNOWN``, ``LARGE`` のみ

..  note::
    この制約は今後緩和される可能性があります。

..  [#] ``com.asakusafw.vocabulary.bulkloader.DbImporterDescription``


キャッシュ運用上の注意
----------------------

ThunderGateのキャッシュを運用するにあたって、以下の点に注意する必要があります。

* 同一のキャッシュIDを利用するジョブは、同時に2つ以上動作させられません 

  * 動作させようとした場合、ThunderGateがエラー終了します
  * ``DbImporterDescription.computeCacheId()`` をオーバーライドしてキャッシュIDを書き換えることで対処できます [#]_

* キャッシュを利用するテーブルのレコードを削除する前に、削除フラグをキャッシュに伝搬させる必要があります

  * 詳しくは `レコードの物理削除`_ を参照してください

* キャッシュが壊れている場合、差分転送ではなく全データの転送を行います

  * データベースやHadoopクラスターが障害から復旧した際などに破損している場合があります
  * 正しく動作しない場合には `キャッシュのメンテナンス`_ を参照してください

..  [#] ただし、キャッシュデータが2重に作られるようになるため、Hadoopクラスターのディスク容量を余計に必要とします


キャッシュのメンテナンス
========================

キャッシュ機能を利用する場合、ThunderGateは「状態」を持ってしまうことになります。
何らかの不整合が発生した場合の対処方法について紹介します。


キャッシュロックの解除
----------------------

ThunderGateのキャッシュ機構は、ThunderGate本体とは別の方法でロックの処理を行なっています。
このロックはインポート処理の手前で取得され、エクスポート処理後に解放されます。

何らかの理由でキャッシュのロックが解放されなかった場合、次のいずれかの方法で開放できます。

* ``$ASAKUSA_HOME/bulkloader/bin/release-cache-lock.sh`` コマンドを利用する
* ``$ASAKUSA_HOME/bulkloader/bin/dbcleaner.sh`` コマンドを利用する

前者はターゲット名と実行IDを指定して、そのジョブフローに関する最低限のロックを開放します。
また、実行IDを指定しなかった場合には、すべてのキャッシュロックを開放します。

後者はThunderGateのあらゆる管理情報を初期化します。
その処理の過程で、キャッシュのロックも全て開放します。


レコードの物理削除
------------------

キャッシュの対象となったテーブルのレコードを実際に削除するには、その前に「削除フラグ」を設定してインポートし、キャッシュに削除を反映させておく必要があります。
そのため、削除フラグを設定して、すべてのキャッシュにそのフラグを伝搬されるまで、レコードを削除してはいけません。

それぞれのテーブルに対して、キャッシュが反映されている時刻を調べるには、次のような問い合わせを行います。

..  code-block:: sql

    SELECT TABLE_NAME, MIN(BUILT_TIMESTAMP) FROM __TG_CACHE_INFO GROUP BY TABLE_NAME


キャッシュ管理情報の削除
------------------------

キャッシュが何らかの理由で破損してしまった場合、キャッシュの管理情報を削除することで初期化できます。
キャッシュの削除は、 ``$ASAKUSA_HOME/delete-cache-info.sh`` コマンドを利用します。

..  list-table:: キャッシュ管理情報削除ツールの引数
    :widths: 4 8 10
    :header-rows: 1

    * - サブコマンド
      - 残りの引数
      - 内容
    * - ``cache``
      - target-name cache-id
      - 指定したキャッシュIDのキャッシュのみを削除します
    * - ``table``
      - target-name table-name
      - 指定したテーブルに関するキャッシュをすべて削除します
    * - ``all``
      - target-name
      - すべてのキャッシュを削除します


キャッシュデータの削除
----------------------

キャッシュデータそのものを削除するには、以下のディレクトリ以下をファイルシステム上から削除します。

* HDFS

  * /user/<ユーザ名>/thundergate/cache/<ターゲット名>/<テーブル名>/<キャッシュID>

* ローカルファイルシステム (スタンドアロンモード時)

  * ~/thundergate/cache/<ターゲット名>/<テーブル名>/<キャッシュID>

キャッシュデータが削除されている場合、次回のインポート時に差分転送ではなく全データの転送を行います。

