==============
リリースノート
==============
Asakusa Frameworkのリリースノートです。

すべての変更点一覧は :doc:`changelogs` も参照してください。

Release 0.5.1
=============
Jul 26, 2013

`Asakusa Framework 0.5.1 documentation`_

..  _`Asakusa Framework 0.5.1 documentation`: http://asakusafw.s3.amazonaws.com/documents/0.5.1/release/ja/html/index.html

本リリースはAsakusa Frameworkの開発版リリースです。
主な変更内容は以下の通りです。

* テストドライバに演算子のトレースログを出力する機構を追加。
* アプリケーション依存ライブラリの管理方法を改善。
* DMDLコンパイラの日本語メッセージリソースを追加。
* その他、多くの細かな機能改善、およびバグフィックス。

Release 0.5.0
=============
May 9, 2013

`Asakusa Framework 0.5.0 documentation`_

..  _`Asakusa Framework 0.5.0 documentation`: http://asakusafw.s3.amazonaws.com/documents/0.5.0/release/ja/html/index.html

本リリースはAsakusa Frameworkの開発版リリースです。
主な変更内容は以下の通りです。

* 試験的にCDH4に対応 [#]_ 。またいくつかの動作検証プラットフォームの追加。
* フレームワーク本体とバッチアプリケーションの構成情報を分離し、バッチアプリケーションの構成定義をシンプル化。
* 今後のAsakusa Frameworkの拡張のベースとなるFramework本体に対する多くのリファインメント。
* その他、多くの細かな機能改善、およびバグフィックス。

..  [#] CDH4上でAsakusa Frameworkを利用するためのドキュメントを、 Sandboxプロジェクトに公開しています。

* `Asakusa Framework Sandbox - CDH4上でAsakusa Frameworkを利用する`_

..  _`Asakusa Framework Sandbox - CDH4上でAsakusa Frameworkを利用する`: http://asakusafw.s3.amazonaws.com/documents/sandbox/ja/html/administration/asakusa-on-cdh4.html

Release 0.4.0
=============
Aug 30, 2012

`Asakusa Framework 0.4.0 documentation`_

..  _`Asakusa Framework 0.4.0 documentation`: http://asakusafw.s3.amazonaws.com/documents/0.4.0/release/ja/html/index.html

本リリースはAsakusa Frameworkの安定版リリースです。
主な変更内容は以下の通りです。

* [Direct I/O] ワイルドカード指定の出力機能などを追加し、試験的機能から正式機能として昇格。
* [Asakusa DSL] コンパイラ最適化のチューニングおよびバグフィックス。
* [YAESS] シミュレーションモードの実行やデプロイモジュールのバージョン検証機能などを追加。
* 動作検証プラットフォームの追加。
* 広範囲にわたるドキュメントの拡充と改善、および多くのドキュメントバグのフィックス。
* その他、多くの細かな機能改善、およびバグフィックス。

Release 0.2.6
=============
May 31, 2012

`Asakusa Framework 0.2.6 documentation`_

..  _`Asakusa Framework 0.2.6 documentation`: http://asakusafw.s3.amazonaws.com/documents/0.2/release/ja/html/index.html

本リリースではYAESS マルチディスパッチ機能が追加されました。

これによりバッチやジョブフローなどを異なる複数のHadoopクラスタに振り分けて実行したり、それぞれ異なる設定で起動したりできるようになります。

* :doc:`yaess/multi-dispatch`

その他、多数の機能改善やバグフィックスが行われています。 

Release 0.2.5
=============
Jan 31, 2012

本リリースでは試験的な機能として「Direct I/O」が追加されました。
これは、Hadoopクラスターから直接バッチの入出力データを
読み書きするための機構です。

* :doc:`directio/index`

また、本バージョンでは対応プラットフォームの拡張として、
従来のバージョンで対応していた
HadoopディストリビューションであるCDH3に加えて、
Apache Hadoop 0.20.203.0での動作検証が行われ、
この環境で動作するための変更が行われています。

その他、細かな機能改善やバグフィックスが行われています。

Release 0.2.4
=============
Dec 19, 2011

本リリースからWindGateがGA (Generally Available) となりました。
WindGateにはローカルのCSVに対するデータ入出力を行う機能が追加となっています。

また、本リリースではドキュメントの構成を見直し、
内容を大幅に拡充しました。
特に「Asakusa Framework入門」の追加、および
WindGateやYAESSに関する記述が多く追加されています。

* :doc:`introduction/index`

旧バージョンを使っている開発環境を0.2.4に移行するには
マイグレーション作業が必要となります。
詳しくは以下のマイグレーションガイドを参照してください。

* :doc:`application/migration-guide`

その他、細かな機能改善やバグフィックスが行われています。

Release 0.2.3
=============
Nov 16, 2011

本リリースでは、様々な環境に合わせて実行方法をカスタマイズ
することが可能なバッチ実行ツール「YAESS」と
ThunderGateの差分インポート機能を実現する
「ThunderGateキャッシュ」機能が追加されました。

* :doc:`yaess/index`
* :doc:`thundergate/cache`

今回のリリースでは、旧バージョンを使っている開発環境を
0.2.3に移行するためにマイグレーション作業が必要となります。
詳しくは以下のマイグレーションガイドを参照してください。

* :doc:`application/migration-guide`

その他、細かな機能改善やバグフィックスが行われています。

Release 0.2.2
=============
Sep 29, 2011

本リリースではExperimental Featureとして「WindGate」が追加されました。

WindGateはThunderGateと同様にバッチに対するデータの外部入出力を行うモジュールですが、
様々なプラットフォームに対応するよう設計され、ThunderGateに対してポータビリティが高いことが特徴です。

* :doc:`windgate/index`

その他、バグフィックスや細かい機能改善が行われています。

Release 0.2.1
=============
Jul 27, 2011

* Extract演算子の追加
* Restructure演算子の追加
* ThunderGateのCLOBサポート
* その他バグフィックス

Release 0.2.0
=============
Jun 29, 2011

* DMDLの導入
* テストドライバの大幅な改善
* その他多くのバグフィックス


Release 0.1.0
=============
Mar 30, 2011

* 初版リリース

