====================
対応プラットフォーム
====================

Asakusa Framework本体
=====================
Asakusa Frameworkは以下のプラットフォームで動作を検証しています。

..  list-table:: 動作プラットフォーム(本体)
    :widths: 10 10
    :header-rows: 1

    * - 種類
      - バージョン
    * - オペレーティングシステム
      - CentOS 5.5 [#]_
    * - Java SDK
      - Java SE6 Update 22 [#]_
    * - Hadoop
      - CDH3 Update 2 [#]_

..  [#] http://www.centos.org/
..  [#] http://www.oracle.com/technetwork/java/javase/
..  [#] http://www.cloudera.com/hadoop/

アプリケーション開発環境
========================
Asakusa Frameworkを利用したバッチアプリケーションの開発では、 `Asakusa Framework本体`_ に加えて以下のプラットフォームで動作を検証しています。

..  list-table:: 動作プラットフォーム(開発環境)
    :widths: 10 20
    :header-rows: 1

    * - 種類
      - バージョン
    * - オペレーティングシステム
      - Ubuntu Desktop 11.10 [#]_
    * - ビルドツール
      - Apache Maven 3.0.3 [#]_
    * - 統合開発環境
      - Eclipse IDE for Java Developers 3.6.2 [#]_

..  [#] http://www.ubuntu.com/
..  [#] http://maven.apache.org/
..  [#] http://eclipse.org/

WindGate
========
:doc:`WindGate <../windgate/index>` は以下のプラットフォームで動作を検証しています。

..  list-table:: 動作プラットフォーム(WindGate/JDBC [#]_ )
    :widths: 10 30
    :header-rows: 1

    * - 種類
      - バージョン
    * - DBMS
      - PostgreSQL 8.4.9 [#]_
    * - JDBC Driver
      - PostgreSQL JDBC Driver 8.4 Build 703 [#]_

..  [#] データベースを利用しない場合には不要です
..  [#] http://www.postgresql.org/
..  [#] http://jdbc.postgresql.org/

ThunderGate
===========
:doc:`ThunderGate <../thundergate/index>` は以下のプラットフォームで動作を検証しています。

..  list-table:: 動作プラットフォーム(ThunderGate)
    :widths: 10 30
    :header-rows: 1

    * - 種類
      - バージョン
    * - DBMS
      - MySQL Server 5.1.53 [#]_
    * - JDBC Driver
      - MySQL Connector/J 5.1.13

..  [#] http://www-jp.mysql.com/
