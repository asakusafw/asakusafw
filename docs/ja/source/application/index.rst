==============
開発環境の整備
==============
Asakusa Frameworkによる開発を行うための開発環境の構築について説明します。

環境環境の構築
==============

スタートガイド
--------------
Clouderaから提供されているCloudera's Hadoop Demo VM上に
Asakusa Frameworkを導入します。

Cloudera's Hadoop Demo VMはHadoopがインストール済であり、
またAsakusa FrameworkがこのVM用にインストーラを提供しているため、
この手順はAsakusa Frameworkをすぐに試すには最適です。

ユーザガイド
------------
Linux OS上にAsakusa Frameworkの開発環境を構築する手順を解説します。

Maven Archetype
---------------
Asakusa Frameworkが提供するバッチアプリケーション開発用のMaven Archetypeを使うことで、
Asakusa Frameworkが提供するモデル生成ツールやコンパイラがMavenのゴール実行と統合されます。

開発環境 マイグレーションガイド
-------------------------------
Asakusa Framework のバージョンアップ時必要となる開発環境に対してのバージョン固有の移行手順について解説します。

開発ツールの利用
================

フローDSLのコンパイル結果のグラフ化
-----------------------------------
Graphvizを使ってフローDSLのコンパイル結果をグラフ化する手順を説明します。

関連するドキュメント
====================

..  toctree::
    :maxdepth: 1

    start-guide
    user-guide
    maven-archetype
    migration-guide
    graphviz
