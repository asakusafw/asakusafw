====================
対応プラットフォーム
====================

Asakusa Framework本体
=====================
Asakusa Framework バージョン |version| は以下のプラットフォームで動作を検証しています。

..  list-table:: 動作検証プラットフォーム(本体)
    :widths: 3 7
    :header-rows: 1

    * - 種類
      - ディストリビューション/バージョン
    * - オペレーティングシステム
      - CentOS 6.2 [#]_
    * - Java SDK
      - Oracle Java SE6 Update 33 [#]_
    * - Hadoop
      - CDH3 Update 5 [#]_
    * - Hadoop
      - Apache Hadoop 0.20.205.0 [#]_
    * - Hadoop
      - Apache Hadoop 1.0.3 [#]_
    * - Hadoop
      - Greenplum MR 1.2 [#]_
    * - Hadoop
      - Amazon Elastic MapReduce (Hadoop Version: 1.0.3, AMI Version: 2.2) [#]_

..  [#] http://www.centos.org/
..  [#] http://www.oracle.com/technetwork/java/javase/
..  [#] https://ccp.cloudera.com/display/CDHDOC/CDH3+Documentation
..  [#] http://hadoop.apache.org/common/docs/r0.20.205.0/
..  [#] http://hadoop.apache.org/common/docs/r1.0.3/
..  [#] http://www.greenplum.com/products/greenplum-mr
..  [#] http://aws.amazon.com/jp/elasticmapreduce/ 

.. _target-platform-development-environment:

アプリケーション開発環境
========================
Asakusa Frameworkを利用したバッチアプリケーションの開発環境は、 以下のプラットフォームで動作を検証しています。

..  list-table:: 動作検証プラットフォーム(開発環境)
    :widths: 3 7
    :header-rows: 1

    * - 種類
      - ディストリビューション/バージョン
    * - オペレーティングシステム
      - Ubuntu Desktop 12.04 [#]_
    * - オペレーティングシステム
      - CentOS 6.2
    * - Java SDK
      - Oracle Java SE6 Update 33
    * - ビルドツール
      - Apache Maven 3.0.4 [#]_
    * - 統合開発環境
      - Eclipse IDE for Java Developers 3.7.2 [#]_
    * - Hadoop
      - CDH3 Update 5

..  attention::
    開発環境のHadoopにCDH3以外のHadoopディストリビューションを使う場合、いくつかの注意点や制約があります。
    詳しくは、 :doc:`../application/user-guide` の :ref:`development-environment-with-other-cdh` を参照してください。

..  [#] http://www.ubuntu.com/
..  [#] http://maven.apache.org/
..  [#] http://eclipse.org/

WindGate
========
:doc:`WindGate <../windgate/index>` は以下のプラットフォームで動作を検証しています。

..  list-table:: 動作プラットフォーム(WindGate/JDBC [#]_ )
    :widths: 3 7
    :header-rows: 1

    * - 種類
      - バージョン
    * - DBMS
      - PostgreSQL 9.1.4 [#]_
    * - JDBC Driver
      - PostgreSQL JDBC Driver 9.1 Build 901 [#]_

..  [#] データベースを利用しない場合には不要です
..  [#] http://www.postgresql.org/
..  [#] http://jdbc.postgresql.org/

ThunderGate
===========
:doc:`ThunderGate <../thundergate/index>` は以下のプラットフォームで動作を検証しています。

..  list-table:: 動作プラットフォーム(ThunderGate)
    :widths: 3 7
    :header-rows: 1

    * - 種類
      - バージョン
    * - DBMS
      - MySQL Server 5.5.25 [#]_
    * - JDBC Driver
      - MySQL Connector/J 5.1.20

..  [#] http://www-jp.mysql.com/
