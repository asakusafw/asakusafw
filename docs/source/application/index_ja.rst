============================
アプリケーションの開発と管理
============================

Asakusa Frameworkによる開発を行うための開発環境の構築、
およびHadoopクラスタ上にAsakusa Frameworkとバッチアプリケーションを
デプロイ、実行するための方法を記述します。

開発環境の構築
==============

Asakusa Framework上で開発環境を準備するために、
以下に示す方法が提供されています。

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

管理者ガイド
------------
Hadoopクラスタ上にAsakusa Framework、
およびバッチアプリケーションをデプロイし、動作させるための
手順について解説します。

Maven Archetype
---------------
Asakusa Frameworkが提供する
バッチアプリケーション開発用のMaven Archetypeを使うことで、
Asakusa Frameworkが提供するモデル生成ツールやコンパイラが
Mavenのゴール実行と統合されます。

関連するドキュメント
====================

..  toctree::
    :maxdepth: 1

    start-guide_ja
    user-guide_ja
    administration-guide_ja
    maven-archetype_ja
