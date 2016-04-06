=====================================
アプリケーションのテスト - TestDriver
=====================================

Asakusa Frameworkでは、 :doc:`../dsl/index` で記述したアプリケーションに対しての自動テストをサポートしています。

テストドライバー
================

Asakusa Frameworkにはテストドライバーという開発用のJavaライブラリが含まれています。
これには次のような特徴があります。

* Hadoopや外部入出力と自動的に連携したテストが可能
* 入出力と検査ルールを定義してバッチやデータフローを検証
* JUnitなどの様々なテストハーネスから利用可能

また、 :doc:`../dmdl/index` の機能と連携してテスト入出力や検査ルールのテンプレートを生成するツールも提供しています。

関連するドキュメント
====================

..  toctree::
    :maxdepth: 1

    start-guide
    user-guide
    using-excel
    using-json
    emulation-mode
    developer-guide
