====================
対応プラットフォーム
====================

Hadoopディストリビューション
============================
Asakusa Framework は、以下のHadoopディストリビューションと組み合わせた運用環境で動作を検証しています。

..  list-table:: 動作検証プラットフォーム(Hadoopディストリビューション)
    :header-rows: 1

    * - Distribution
      - Version
      - OS
      - JDK
      - 検証バージョン [1]_
    * - Apache Hadoop
      - 1.2.1
      - CentOS 6 (6.2)
      - JDK 6 (1.6.0_31)
      - |version|
    * - Apache Hadoop
      - 2.2.0
      - CentOS 6 (6.2)
      - JDK 7 (1.7.0_45)
      - |version|-hadoop2 [2]_
    * - CDH3
      - Update 5 (u5)
      - CentOS 6 (6.2)
      - JDK 6 (1.6.0_31)
      - |version|
    * - CDH4
      - 4.5.0
      - CentOS 6 (6.2)
      - JDK 7 (1.7.0_45)
      - |version|-hadoop2 [2]_
    * - MapR
      - 3.0.2 (M3/M5/M7)
      - Ubuntu 12.04
      - JDK 6 (1.6.0_32)
      - |version|
    * - Amazon EMR [3]_
      - Hadoop 1.0.3
      - AMI 2.4.2
      - JDK 7 (1.7.0_40)
      - |version|
    * - Amazon EMR
      - Hadoop 2.2.0
      - AMI 3.0.2
      - JDK 7 (1.7.0_45)
      - |version|-hadoop2 [2]_
    * - Amazon EMR
      - MapR 3.0.2 (M3/M5/M7)
      - AMI 2.4.2
      - JDK 7 (1.7.0_40)
      - |version|

..  [1] 検証バージョンは、Hadoopディストリビューションの動作検証に使用したAsakusa Frameworkのバージョンです。
..  [2] 検証バージョンに ``-hadoop2`` と記載されている行に対応するHadoopディストリビューションでは、Hadoop2系向けのAsakusa Frameworkバージョンが必要です。詳しくは :doc:`../administration/deployment-hadoop2` を参照してください。

..  attention::
    Asakusa Frameworkの現在のバージョン |version| では、Hadoop2系の対応は試験的機能として提供されています。

..  [3] Amazon EMR上で利用するための情報を :sandbox:`Amazon EMR上でAsakusa Frameworkを利用する <administration/asakusa-on-emr.html>` (Asakusa Framework Sandbox) に公開しています。

アプリケーション開発環境
========================
Asakusa Frameworkを利用したバッチアプリケーションの開発環境は、 以下のプラットフォームで動作を検証しています。

..  list-table:: 動作検証プラットフォーム(開発環境)
    :widths:  4 6
    :header-rows: 1

    * - 種類
      - Product/Version
    * - OS
      - Ubuntu Desktop 12.04
    * - OS
      - CentOS 6.2
    * - Java
      - JDK 6 (1.6.0_45)
    * - Java
      - JDK 7 (1.7.0_45) [#]_
    * - ビルドツール
      - Gradle 1.10 [#]_
    * - ビルドツール
      - Apache Maven 3.0.5 [#]_
    * - IDE
      - Eclipse IDE for Java Developers 3.7.2
    * - IDE
      - Eclipse IDE for Java Developers 4.3.1
    * - Hadoop
      - Apache Hadoop 1.2.1

..  [#] JDK 7の利用については、 :doc:`../application/develop-with-jdk7` を参照してください。
..  [#] Gradleの利用については、 :doc:`../application/gradle-plugin` を参照してください。
..  [#] Mavenの利用については、 :doc:`../application/maven-archetype` を参照してください。

WindGate
========
:doc:`WindGate <../windgate/index>` は以下のプラットフォームで動作を検証しています。

..  list-table:: 動作検証プラットフォーム(WindGate/JDBC [#]_ )
    :widths: 4 6
    :header-rows: 1

    * - 種類
      - Product/Version
    * - DBMS
      - PostgreSQL 9.1.4
    * - JDBC Driver
      - PostgreSQL JDBC Driver 9.1 Build 901

..  [#] データベースを利用しない場合(例えば WindGate/CSV のみを使う場合)には不要です

ThunderGate
===========
:doc:`ThunderGate <../thundergate/index>` は以下のプラットフォームで動作を検証しています。

..  list-table:: 動作検証プラットフォーム(ThunderGate)
    :widths: 4 6
    :header-rows: 1

    * - 種類
      - Product/Version
    * - DBMS
      - MySQL Server 5.5.25
    * - JDBC Driver
      - MySQL Connector/J 5.1.25

リンク
======
対応プラットフォームのリンク集です。

..  list-table::
    :widths: 2 8
    :header-rows: 1

    * - Product
      - Link
    * - Apache Hadoop
      - http://hadoop.apache.org/
    * - Cloudera CDH
      - http://www.cloudera.com/content/cloudera/en/products-and-services/cdh.html
    * - MapR
      - http://www.mapr.com/
    * - Amazon EMR
      - http://aws.amazon.com/elasticmapreduce/
    * - CentOS
      - http://www.centos.org/
    * - Ubuntu
      - http://www.ubuntu.com/
    * - JDK (Java SE)
      - http://www.oracle.com/technetwork/java/javase/index.html
    * - Gradle
      - http://www.gradle.org/
    * - Apache Maven
      - http://maven.apache.org/
    * - Eclipse
      - http://www.eclipse.org/
    * - PostgreSQL
      - http://www.postgresql.org/
    * - MySQL
      - http://www.mysql.com/
