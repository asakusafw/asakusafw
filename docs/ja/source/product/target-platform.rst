====================
対応プラットフォーム
====================

Asakusa Framework本体
=====================
Asakusa Frameworkは以下のプラットフォームで動作を検証しています。

..  list-table:: 動作プラットフォーム(本体)
    :widths: 2 5 3 
    :header-rows: 1

    * - 種類
      - バージョン
      - 備考
    * - オペレーティングシステム
      - CentOS 6.2 [#]_
      - 
    * - Java SDK
      - Java SE6 Update 33 [#]_
      - 
    * - Hadoop
      - CDH3 Update 4 [#]_
      - 
    * - Hadoop
      - Apache Hadoop 0.20.203.0 [#]_
      - 
    * - Hadoop
      - Apache Hadoop 1.0.3 [#]_
      - 

..  [#] http://www.centos.org/
..  [#] http://www.oracle.com/technetwork/java/javase/
..  [#] https://ccp.cloudera.com/display/CDHDOC/CDH3+Documentation
..  [#] http://hadoop.apache.org/common/docs/r0.20.203.0/
..  [#] http://hadoop.apache.org/common/docs/r1.0.3/

アプリケーション開発環境
========================
Asakusa Frameworkを利用したバッチアプリケーションの開発では、 `Asakusa Framework本体`_ に加えて以下のプラットフォームで動作を検証しています。

..  list-table:: 動作プラットフォーム(開発環境)
    :widths: 3 7 
    :header-rows: 1

    * - 種類
      - バージョン
    * - オペレーティングシステム
      - Ubuntu Desktop 12.04 [#]_
    * - オペレーティングシステム
      - CentOS 6.2 [#]_
    * - ビルドツール
      - Apache Maven 3.0.4 [#]_
    * - 統合開発環境
      - Eclipse IDE for Java Developers 3.7.2 

..  [#] http://www.ubuntu.com/
..  [#] http://www.centos.org/
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
