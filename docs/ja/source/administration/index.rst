==============
運用環境の整備
==============
Asakusa FrameworkとバッチアプリケーションをHadoopクラスタなどの各種サーバにデプロイし、実行するための手順などについて説明します。

デプロイモジュールの管理
========================

Asakusa Frameworkのデプロイモジュールの作成や設定は、 :doc:`framework-organizer` を参照してください。

また、 :doc:`../application/gradle-plugin` で説明しているGradleプラグインを利用している場合、 Framework Organizer の機能はこのプラグインに統合されているのでプラグインのドキュメントを参照してください。

運用環境へのデプロイ
====================

Asakusa Frameworkが提供する外部システム連携の各モジュールごとに運用環境のデプロイ方法を示したドキュメントです。

* :doc:`deployment-with-windgate`
* :doc:`deployment-with-thundergate`
* :doc:`deployment-with-directio`
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

* `Amazon EMR上でAsakusa Frameworkを利用する`_ (Asakusa Framework Sandbox Document)

その他
======

* :doc:`generate-deployment-archive-from-source`

関連するドキュメント
====================

..  toctree::
    :maxdepth: 1

    framework-organizer
    deployment-with-windgate
    deployment-with-thundergate
    deployment-with-directio
    configure-hadoop-parameters
    migration-guide
    deployment-extension-module
    deployment-runtime-plugins
    deployment-cleaner
    utility-tool-user-guide
    deployment-hadoop2
    generate-deployment-archive-from-source

..  _`Amazon EMR上でAsakusa Frameworkを利用する`: http://asakusafw.s3.amazonaws.com/documents/sandbox/ja/html/administration/asakusa-on-emr.html
