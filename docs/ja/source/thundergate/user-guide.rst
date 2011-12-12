=========================
ThunderGateユーザーガイド
=========================

この文書では、ThunderGateの利用方法について紹介します。

ThunderGateのセットアップ
=========================
ThunderGateのセットアップ方法は :doc:`../administration/deployment-with-thundergate` を参照してください。

ThunderGateを利用したバッチの開発
=================================
ThunderGateを利用したアプリケーションでは、MySQLに登録されたテーブル情報から、
対応するデータモデル定義を自動的に生成できます。
詳しくは :doc:`../dmdl/with-thundergate` を参照してください。

また、MySQLのデータをThunderGateを利用してバッチ内で使うには、Asakusa DSLとの連携が必要です。
詳しくは :doc:`../dsl/with-thundergate` を参照してください。
なお、同バッチのテスト時に注意すべき点は :doc:`../testing/with-thundergate` を参照してください。

ThunderGateに関する詳しい情報
=============================
`ThunderGateユーザーガイド(PDF)`_ を参照してください。

.. _`ThunderGateユーザーガイド(PDF)` : https://asakusafw.s3.amazonaws.com/documents/AsakusaThundergate_UserGuide.pdf
