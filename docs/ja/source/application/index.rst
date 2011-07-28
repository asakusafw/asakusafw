============================
アプリケーションの開発と管理
============================

Asakusa Frameworkによる開発を行うための開発環境の構築、
およびHadoopクラスタ上にAsakusa Frameworkとバッチアプリケーションを
デプロイ、実行するための方法を記述します。

開発環境の構築
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

フローDSLのコンパイル結果のグラフ化
-----------------------------------
Graphvizを使ってフローDSLのコンパイル結果をグラフ化する手順を説明します。

運用環境の構築
==============

管理者ガイド
------------
Hadoopクラスタ上にAsakusa Framework、
およびバッチアプリケーションをデプロイし、動作させるための
手順について解説します。


関連するドキュメント
====================

..  toctree::
    :maxdepth: 1

    start-guide
    user-guide
    maven-archetype
    graphviz
    administrator-guide
