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
* :doc:`gradle-plugin-reference`
* :doc:`gradle-plugin-migration-guide`
* :doc:`gradle-plugin-v08-changes`
* :doc:`sdk-artifact`

マイグレーション
================

* :doc:`migration-guide`
* :doc:`previous-migration-guide`

開発ツールの利用
================

Asakusa Frameworkが提供する開発ツールに関するドキュメントです。

* :doc:`dsl-visualization`

プラットフォーム関連
====================

特定のプラットフォームでAsakusa Frameworkを利用することに関するドキュメントです。

* :doc:`using-hadoop`
* :doc:`using-jdk`

非推奨機能
==========

Asakusa Framework バージョン |version| において、以下のドキュメントで説明する機能は非推奨となっています。

* :doc:`gradle-plugin-deprecated`
* :doc:`legacy-module-guide`
* :doc:`yaess-log-visualization`

..  attention::
    非推奨となった機能のうち、その多くは、現在のバージョンで代替となる推奨機能が提供されています。
    詳しくは各ドキュメント内の説明を参照してください。

関連するドキュメント
====================

..  toctree::
    :maxdepth: 1

    gradle-plugin
    gradle-plugin-reference
    gradle-plugin-migration-guide
    gradle-plugin-v08-changes
    sdk-artifact
    migration-guide
    previous-migration-guide
    dsl-visualization
    yaess-log-visualization
    using-hadoop
    using-jdk
    gradle-plugin-deprecated
    legacy-module-guide
