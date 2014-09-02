==============
運用環境の整備
==============
Asakusa FrameworkとバッチアプリケーションをHadoopクラスタなどの各種サーバにデプロイし、実行するための手順などについて説明します。

運用環境のデプロイ
==================
* :doc:`deployment-guide`
* :doc:`deployment-architecture`
* :doc:`configure-hadoop-parameters`
* :doc:`configure-library-cache`
* :doc:`migration-guide`

追加モジュールのデプロイ
========================

Asakusa Frameworkが提供する追加モジュールを運用環境にデプロイする方法を示したドキュメントです。

* :doc:`deployment-extension-module`
* :doc:`deployment-runtime-plugins`

運用ツールの利用
================

Asakusa Frameworkが提供する運用ツールに関するドキュメントです。

* :doc:`utility-tool-user-guide`

プラットフォーム関連
====================

特定のプラットフォームでAsakusa Frameworkを利用することに関するドキュメントです。

クラウドプラットフォーム
------------------------

* :doc:`../sandbox/asakusa-on-emr` (Asakusa Framework Sandbox Document)

その他
======

* :doc:`generate-deployment-archive-from-source`

非推奨機能
==========

Asakusa Framework バージョン |version| において、以下のドキュメントで説明する機能は非推奨となっています。

* :doc:`framework-organizer`
* :doc:`deployment-cleaner`

..  attention::
    非推奨となった機能のうち、その多くは、現在のバージョンで代替となる推奨機能が提供されています。

関連するドキュメント
====================

..  toctree::
    :maxdepth: 1

    deployment-guide
    deployment-architecture
    configure-hadoop-parameters
    configure-library-cache
    migration-guide
    deployment-extension-module
    deployment-runtime-plugins
    utility-tool-user-guide
    generate-deployment-archive-from-source
    framework-organizer
    deployment-cleaner

