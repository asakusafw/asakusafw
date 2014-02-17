==============
運用環境の整備
==============
Asakusa FrameworkとバッチアプリケーションをHadoopクラスタなどの各種サーバにデプロイし、実行するための手順などについて説明します。

運用環境のデプロイ
==================

Asakusa Frameworkが提供する外部システム連携の各モジュールごとに運用環境のデプロイ方法を示したドキュメントです。

* :doc:`deployment-with-directio`
* :doc:`deployment-with-windgate`
* :doc:`deployment-with-thundergate`
* :doc:`configure-hadoop-parameters`
* :doc:`migration-guide`

追加モジュールのデプロイ
========================

Asakusa Frameworkが提供する追加モジュールを運用環境にデプロイする方法を示したドキュメントです。

* :doc:`deployment-extension-module`
* :doc:`deployment-runtime-plugins`
* :doc:`deployment-cleaner`

運用ツールの利用
================

Asakusa Frameworkが提供する運用ツールに関するドキュメントです。

* :doc:`utility-tool-user-guide`

プラットフォーム関連
====================

特定のプラットフォームでAsakusa Frameworkを利用することに関するドキュメントです。

* :doc:`deployment-hadoop2`

クラウドプラットフォーム
------------------------

* :sandbox:`Amazon EMR上でAsakusa Frameworkを利用する <administration/asakusa-on-emr.html>` (Asakusa Framework Sandbox Document)

その他
======

* :doc:`generate-deployment-archive-from-source`

関連するドキュメント
====================

..  toctree::
    :maxdepth: 1

    deployment-with-directio
    deployment-with-windgate
    deployment-with-thundergate
    framework-organizer
    configure-hadoop-parameters
    migration-guide
    deployment-extension-module
    deployment-runtime-plugins
    deployment-cleaner
    utility-tool-user-guide
    deployment-hadoop2
    generate-deployment-archive-from-source

