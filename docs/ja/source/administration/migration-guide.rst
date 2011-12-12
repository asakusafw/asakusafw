===============================
運用環境 マイグレーションガイド
===============================
この文書では、Asakusa Framework のバージョンアップに伴う、運用環境に対してのバージョン固有の移行手順について解説します。

運用環境のバージョンアップはAsakusa Frameworkを新バージョンで再インストールする必要があります。その際、設定ファイルなどを過去バージョンのファイルで上書きして設定を反映させる場合などは、必ず本マイグレーションガイドを参照して、バージョン間の変更を反映させてください。

ver0.2.3へのマイグレーション
============================

ThunderGate用設定ファイルの項目追加
===================================
ThunderGateキャッシュ機能の追加により、ThunderGateの設定ファイルに項目が追加されました。

bulkloader-conf-db.properties
-----------------------------
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
-----------------------------
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

