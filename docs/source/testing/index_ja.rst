========================
アプリケーションのテスト
========================

Asakusa Frameworkでは、 :doc:`../dsl/index_ja` で記述した
アプリケーションに対しての自動テストをサポートしています。

テストドライバ
==============
Asakusa Frameworkにはテストドライバという開発用のJavaライブラリが含まれています。
これには次のような特徴があります。

* Hadoopや外部入出力と自動的に連携したテストが可能
* 入出力と検査ルールを定義してバッチやデータフローを検証
* JUnitなどの様々なテストハーネスから利用可能

また、 :doc:`../dmdl/index_ja` の機能と連携して
テスト入出力や検査ルールのテンプレートを生成するツールも提供しています。

関連するドキュメント
====================

..  toctree::
    :maxdepth: 1

    start-guide_ja
    user-guide_ja
    with-thundergate_ja
    using-excel_ja
    using-json_ja
    developer-guide_ja
