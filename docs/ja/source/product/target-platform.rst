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
      - CDH4.2.1 (Experimental) [#]_
    * - Hadoop
      - Apache Hadoop 1.0.3 [#]_
    * - Hadoop
      - Apache Hadoop 1.1.2 [#]_
    * - Hadoop
      - Greenplum MR 1.2 [#]_
    * - Hadoop
      - Greenplum MR 2.1
    * - Hadoop
      - Amazon Elastic MapReduce (Hadoop Version: 1.0.3, AMI Version: 2.3) [#]_

..  [#] http://www.centos.org/
..  [#] http://www.oracle.com/technetwork/java/javase/
..  [#] http://www.cloudera.com/content/support/en/documentation.html
..  [#] Asakusa Framework バージョン |version| では、CDH4は試験的な対応です。
..  [#] http://hadoop.apache.org/common/docs/r1.0.3/
..  [#] http://hadoop.apache.org/common/docs/r1.1.2/
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
      - Apache Maven 3.0.5 [#]_
    * - 統合開発環境
      - Eclipse IDE for Java Developers 3.7.2 [#]_
    * - Hadoop
      - CDH3 Update 5
    * - Hadoop
      - Apache Hadoop 1.1.2

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
      - MySQL Connector/J 5.1.25

..  [#] http://www-jp.mysql.com/
