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
      - 検証バージョン [#]_
    * - Apache Hadoop
      - 1.2.1
      - CentOS 6 (6.2)
      - JDK 6 (1.6.0_31)
      - |version|-hadoop1
    * - Apache Hadoop
      - 2.2.0
      - CentOS 6 (6.2)
      - JDK 7 (1.7.0_45)
      - |version|-hadoop2
    * - CDH3
      - Update 5 (u5)
      - CentOS 6 (6.2)
      - JDK 6 (1.6.0_31)
      - |version|-hadoop1
    * - CDH4
      - 4.6.0 (MRv1)
      - CentOS 6 (6.2)
      - JDK 7 (1.7.0_45)
      - |version|-hadoop2
    * - CDH5
      - 5.0.1 (MRv1/MRv2)
      - CentOS 6 (6.2)
      - JDK 7 (1.7.0_45)
      - |version|-hadoop2
    * - MapR
      - 3.0.2 (M3/M5/M7)
      - Ubuntu 12.04
      - JDK 6 (1.6.0_32)
      - |version|-hadoop1
    * - Amazon EMR [#]_
      - Hadoop 1.0.3
      - AMI 2.4.5
      - JDK 7 (1.7.0_60 EA)
      - |version|-hadoop1
    * - Amazon EMR
      - Hadoop 2.4.0
      - AMI 3.1.0
      - JDK 7 (1.7.0_60 EA)
      - |version|-hadoop2
    * - Amazon EMR
      - MapR 3.0.2 (M3/M5/M7)
      - AMI 2.4.2
      - JDK 7 (1.7.0_40)
      - |version|-hadoop1

..  [#] 検証バージョンは、Hadoopディストリビューションの動作検証に使用したAsakusa Frameworkのバージョンです。
        詳しくは :doc:`../administration/deployment-guide` を参照してください。

..  [#] Amazon EMR上で利用するための情報を :doc:`../sandbox/asakusa-on-emr` (Asakusa Framework Sandbox) に公開しています。

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
      - Ubuntu Desktop 14.04
    * - OS
      - CentOS 6.2
    * - Java
      - JDK 6 (1.6.0_45) [#]_
    * - Java
      - JDK 7 (1.7.0_45)
    * - ビルドツール
      - Gradle 1.12 [#]_
    * - ビルドツール
      - Apache Maven 3.0.5 [#]_
    * - IDE
      - Eclipse IDE for Java Developers 3.7.2
    * - IDE
      - Eclipse IDE for Java Developers 4.3.2
    * - Hadoop
      - Apache Hadoop 1.2.1

..  [#] JDK 6の利用については、 :doc:`../application/using-jdk` を参照してください。
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
