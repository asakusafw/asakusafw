==============
開発環境の整備
==============
Asakusa Frameworkによる開発を行うための開発環境の構築について説明します。

Asakusa Framework開発環境の新規構築
===================================

Asakusa Frameworkの開発環境(OSやAsakusa Frameworkが利用するソフトウェアも含めた開発環境)を新規に構築する場合は、 :doc:`../introduction/start-guide` や :jinrikisha:`Jinrikisha (人力車) - Asakusa Framework Starter Package - <index.html>` を参照してください。

アプリケーション開発プロジェクトの構築と管理
============================================

アプリケーション開発用のプロジェクトの作成やビルド、ライブラリの管理などに関するドキュメントです。

* :doc:`gradle-plugin`
* :doc:`sdk-artifact`
* :doc:`migration-guide`

開発ツールの利用
================

Asakusa Frameworkが提供する開発ツールに関するドキュメントです。

* :doc:`dsl-visualization`
* :doc:`yaess-log-visualization` (Experimental)

プラットフォーム関連
====================

特定のプラットフォームでAsakusa Frameworkを利用することに関するドキュメントです。

* :doc:`using-hadoop`
* :doc:`develop-with-jdk7`

非推奨機能
==========

Asakusa Framework バージョン |version| において、以下のドキュメントで説明する機能は非推奨となっています。

* :doc:`gradle-plugin-deprecated`
* :doc:`maven-archetype`
* :doc:`legacy-module-guide`

..  attention::
    非推奨となった機能のうち、その多くは、現在のバージョンで代替となる推奨機能が提供されています。
    詳しくはドキュメント内の説明を参照してください。

関連するドキュメント
====================

..  toctree::
    :maxdepth: 1

    gradle-plugin
    sdk-artifact
    migration-guide
    dsl-visualization
    yaess-log-visualization
    using-hadoop
    develop-with-jdk7
    gradle-plugin-deprecated
    maven-archetype
    legacy-module-guide

