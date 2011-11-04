======================
マイグレーションガイド
======================

この文書では、Asakusa Framework のバージョンアップに伴うバージョン固有の移行手順について解説します。

なお、Asakusa Framework の開発環境における標準的なバージョンアップ手順については、 :ref:`vup-development-environment` を参照してください。

ver0.2.3へのマイグレーション
============================
ver0.2.3ではThunderGateのキャッシュ機能、及びYAESSが追加されたため、必要に応じて DSLの仕様変更、及び開発環境の構成変更に対応する必要があります。

ジョブフローDSLの仕様変更
-------------------------
*(ThunderGate用アーキタイプ asakusa-archetype-batchapp から生成したアプリケーションプロジェクトについては、以下の変更を行なってください。)*

ジョブフローDSLのThunderGate用インポータ記述用親クラス (DbImporterDescription [#]_ ) において、キャッシュ有効/無効を指定するメソッド isCacheEnabled() がデフォルト実装され、戻り値 ``false`` を返すようになりました。

また、 ThunderGate用アーキタイプ ``asakusa-archetype-batchapp`` から生成されるサンプルアプリケーションのインポータ記述用親クラス (DefaultDbImporterDescription) のisCacheEnabled() メソッドが削除されました。

これらの変更の目的は、ver0.2.3で追加されたThunderGateキャッシュ機能について、デフォルトではキャッシュOFF（過去バージョンと同じ動作）とするためですが、ver0.2.2までの DefaultDbImporterDescription をそのまま実装しているアプリケーションについては、isCacheEnabled() が ``true`` を返すよう実装されているため意図せずキャッシュがONに設定される可能性があるため、アプリケーションの実装を確認の上、必要であれば ソースを修正してください。

..  [#] com.asakusafw.vocabulary.bulkloader.DbImporterDescription

開発環境の構成変更
------------------

build.propertiesの項目追加/変更
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
*(ThunderGate用アーキタイプ asakusa-archetype-batchapp から生成したアプリケーションプロジェクトについては、以下の変更を行なってください。)*

* ThunderGateキャッシュ機能用のプロパティ追加
   * asakusa.modelgen.sid.column
   * asakusa.modelgen.timestamp.column
   * asakusa.modelgen.delete.column
   * asakusa.modelgen.delete.value
* asakusa.modelgen.excludes のデフォルトが変更
   * ThunderGateが使用するテーブルについてはデフォルトでモデル生成対象から除外されるようになったため、このプロパティで除外指定を行う必要がなくなりました。

ver0.2.3の変更箇所を以下に示します。以下の定義をアプリケーションプロジェクトの build.properties に追加した上で、必要に応じてアプリケーション毎に適切な値に変更して下さい。

..  code-block:: properties

    # A regular expression string which excludes model name with model generation.
    asakusa.modelgen.excludes=.*_RL
    # The system ID column name (optional).
    asakusa.modelgen.sid.column=SID
    # The last modified timestamp column name (optional).
    asakusa.modelgen.timestamp.column=UPDT_DATETIME
    # The logical delete flag column name (optional).
    asakusa.modelgen.delete.column=DELETE_FLAG
    # Logical delete flag value (optional).
    asakusa.modelgen.delete.value="1"

ビルドスクリプトの更新
~~~~~~~~~~~~~~~~~~~~~~
*(この変更はすべてのアプリケーションプロジェクトに対して実施してください)*

アプリケーションプロジェクトの以下のファイルを、ver0.2.3のThunderGate用アーキタイプ ``asakusa-archetype-batchapp`` から生成したプロジェクトに含まれるファイルで上書き更新してください。

* src/main/assembly/asakusa-install-dev.xml
* src/main/scripts/asakusa-build.xml

YAESS用依存定義の追加
~~~~~~~~~~~~~~~~~~~~~
*(この変更はYAESSを使用する場合に実施して下さい)*

YAESSを使用する場合、アプリケーションプロジェクトのpom.xmlについて、以下のdependencyを追加してください。

..  code-block:: xml

        <dependency>
            <groupId>com.asakusafw</groupId>
            <artifactId>asakusa-yaess-plugin</artifactId>
            <version>${asakusafw.version}</version>
        </dependency>

CDHバージョンの変更
~~~~~~~~~~~~~~~~~~~
ver0.2.3ではCloudera CDH3 Update2をデフォルトの依存バージョンとしており、動作検証もこのバージョンで実施しているため、アプリケーションプロジェクトの依存バージョンもこれに合わせることを推奨します。

アプリケーションプロジェクトのpom.xmlについて、以下の変更を行ってください。

..  code-block:: xml

    <cloudera.cdh.version>0.20.2-cdh3u2</cloudera.cdh.version>

