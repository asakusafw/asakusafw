==============================
運用環境マイグレーションガイド
==============================
この文書では、Asakusa Framework のバージョンアップに伴う、運用環境のマイグレーション手順について解説します。

Asakusa Frameworkのバージョンアップに伴う機能追加や変更については、各機能のドキュメントを参照してください。

各バージョン共通のマイグレーション手順
======================================
運用環境のAsakusa Frameworkをマイグレーションするための、各バージョン共通のマイグレーション手順を示します。

運用環境をマイグレーションする際には、ここで説明しているマイグレーション手順が基本となりますが、
これに加えて後述する `バージョン固有のマイグレーション手順`_ が必要となる場合があります。

マイグレーションを行う際には、必ず `バージョン固有のマイグレーション手順`_ も確認してください。

運用環境の再デプロイ
--------------------
運用環境のマイグレーションは、基本的にはマイグレーション対象バージョンで環境全体を再度デプロイします。運用環境のデプロイについては、以下のドキュメントを参照してください。

* :doc:`deployment-with-directio`
* :doc:`deployment-with-windgate`
* :doc:`deployment-with-thundergate`

..  attention::
    Asakusa Frameworkのマイグレーションを行う際には、基本的には合わせてマイグレーション対象バージョンでバッチコンパイルを行ったバッチアプリケーションをデプロイする必要があります。

設定の反映
----------
環境に合わせて編集した設定ファイルを反映します。その際、設定ファイルなどを過去バージョンのファイルで上書きして設定を反映させる場合は、必ず本マイグレーションガイドや各ドキュメントを参照して、バージョン間のモジュールの変更内容を考慮してください。


バージョン固有のマイグレーション手順
====================================
運用環境のAsakusa Frameworkをマイグレーションするための、バージョン固有のマイグレーション手順を示します。

なお、複数バージョンをまたいだマイグレーションを行う場合 (例えば バージョン 0.2.3 から 0.2.6 へバージョンアップを行う場合)、中間のバージョンの手順 (ここの例では 0.2.4, および 0.2.5) も確認し、パッチ適用手順などが提供されていた場合は、必ずそのパッチを順次適用するようにしてください。

0.4.0 へのマイグレーション
--------------------------

YAESSプロファイルセット
~~~~~~~~~~~~~~~~~~~~~~~
ジョブフロー中間ファイルのクリーンアップの動作の設定方法が見直され、関連する以下の項目について以下の変更が行われました。

* ``hadoop.workingDirectory`` が廃止
   * 0.4.0 ではこのプロパティの代わりに後述する ``hadoop.cleanup`` によりクリーンアップの設定を行うように変更されました。この設定が存在すると、YAESSの実行時に警告が出力されます。
* ``hadoop-cleanup.sh`` が廃止
   * このため、このスクリプトに介入するためのプロパティ ``hadoop.cleanup.<n>`` は使用できません。
* ``hadoop.cleanup`` が追加
   * このプロパティが存在しない場合、中間ファイルのクリーンアップが行われません。クリーンアップを行う場合は必ずこのプロパティを設定してください。

..  warning::
    上記の変更のため、バージョン 0.2.6 以前のYAESSプロファイルセットをそのまま使用すると、設定によってはジョブフロー中間ファイルのクリーンアップは行われなくなる可能性があります。必ず設定を確認し、必要に応じて設定のマイグレーションを行なってください。

YAESSのクリーンアップについては、 :doc:`../yaess/user-guide` を参照してください。

WindGateプロファイル
~~~~~~~~~~~~~~~~~~~~
WindGateプロファイルの設定項目 ``resource.hadoop.target`` はバージョン 0.4.0 より非推奨になりました。代わりに ``resource.hadoop.env.ASAKUSA_HOME`` にログイン先の Asakusa Framework のインストール先を指定してください。

WindGateプロファイルについては、 :doc:`../windgate/user-guide` を参照してください。


ThunderGateのデプロイ
~~~~~~~~~~~~~~~~~~~~~
ThunderGateは0.4.0でデプロイ手順や設定ファイルのフォーマットが全体的に見直されました。詳しくは :doc:`deployment-with-thundergate` や :doc:`../thundergate/user-guide` を参照してください。


0.2.4 へのマイグレーション
--------------------------

Asakusa Frameworkインストールアーカイブの名称変更と統廃合
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Asakusa Frameworkインストールアーカイブが以下の通り変更されました。

* asakusa-distribution-${version}-prod-hc.tar.gz -> asakusafw-${asakusafw.version}-prod-thundergate-hc.tar.gz
* asakusa-distribution-${version}-prod-db.tar.gz -> asakusafw-${asakusafw.version}-prod-thundergate-db.tar.gz
* asakusa-distribution-${version}-prod-cleaner.tar.gz -> asakusafw-${asakusafw.version}-prod-cleaner.tar.gz
* asakusa-distribution-${version}-prod-windgate.tar.gz -> asakusafw-${asakusafw.version}-prod-windgate.tar.gz
* asakusa-distribution-${version}-prod-windgate-ssh.tar.gz -> 廃止(prod-windgateを使用して下さい)

ver0.2.3へのマイグレーション
----------------------------

ThunderGate用設定ファイルの項目追加
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
ThunderGateキャッシュ機能の追加により、ThunderGateの設定ファイルに項目が追加されました。

bulkloader-conf-db.properties
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
$ASAKUSA_HOME/bulkloader/conf/bulkloader-conf-db.properties に対して、以下の項目を追加してください。

..  code-block:: properties

    # Importer setting
    # ...

    # Shell name of Get Cache Info (required)
    import.cache-info-shell-name=asakusa/bulkloader/bin/get-cache-info.sh
    # Shell name of Delete Cache Storage (required)
    import.delete-cache-shell-name=asakusa/bulkloader/bin/delete-cache-storage.sh

* import.cache-info-shell-name
   * Hadoopクライアントノードに配置する、キャッシュ情報を取得するためのスクリプト名です。Hadoopクライアントノード上のホームディレクトリ>からの相対パスを指定するため、ASAKUSA_HOME を $HOME/asakusa 以外に設定している場合は環境に合わせたパスに修正して下さい。
* import.cache-info-shel
   * Hadoopクライアントノードに配置する、キャッシュファイルを削除するためのスクリプト名です。Hadoopクライアントノード上のホームディレク>トリからの相対パスを指定するため、ASAKUSA_HOME を $HOME/asakusa 以外に設定している場合は環境に合わせたパスに修正してください。

bulkloader-conf-hc.properties
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
$ASAKUSA_HOME/bulkloader/conf/bulkloader-conf-hc.properties に対して、以下の項目を追加してください。

..  code-block:: properties

    # Extractor setting
    # ...

    # Shell name of Cache Builder (required)
    import.cache-build-shell-name=asakusa/thundergate-cache/bin/build-cache.sh
    # Maximim number of parallel cache builders (optional)
    import.cache-build-max-parallel=1

* import.cache-build-shell-name
   * Hadoopクライアントノードに配置する、キャッシュファイルを作成するるためのスクリプト名です。Hadoopクライアントノード上のホームディレ>クトリからの相対パスを指定するため、ASAKUSA_HOME を $HOME/asakusa 以外に設定している場合は環境に合わせたパスに修正して下さい。
* import.cache-build-max-parallel
   * キャッシュ作成処理の並列実行数を指定します。スタンドアロンモードで実行する環境では、この値は必ず1を指定してください。

