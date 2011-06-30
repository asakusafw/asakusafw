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

OSやHadoopがインストール済であり、
本VM専用のAsakusa Frameworkインストーラが提供されているため、
Asakusa Frameworkを試用するために最適な方法です。

ユーザガイド
------------
Linux OS上にAsakusa Frameworkの開発環境を
構築する手順を解説します。

Maven Archetype
---------------
Asakusa Frameworkが提供するバッチアプリケーション開発用のMaven Archetypeを使うことで、
Asakusa Frameworkが提供するモデル生成ツールやコンパイラがMavenのゴール実行と統合されます。

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
    administrator-guide
    maven-archetype
