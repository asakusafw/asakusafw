====================
対応プラットフォーム
====================

Hadoopディストリビューション
============================

Asakusa Framework は、以下のHadoopディストリビューションと組み合わせた運用環境で動作を検証しています。

..  list-table:: 動作検証プラットフォーム(Hadoopディストリビューション)
    :header-rows: 1
    :widths: 25 20 25 15 15

    * - Distribution
      - Version
      - OS
      - JDK
      - 検証バージョン [#]_
    * - Apache Hadoop
      - 1.2.1
      - CentOS 6 (6.2)
      - JDK 7 (1.7.0_76)
      - |version|-hadoop1
    * - MapR
      - 5.0.0 (MRv2) [#]_
      - Red Hat Enterprise Linux 7.1
      - JDK 8 (1.8.0_51)
      - |version|-hadoop2
    * - Hortonworks Data Platform
      - 2.3
      - CentOS 6 (6.6)
      - JDK 8 (1.8.0_51)
      - |version|-hadoop2
    * - CDH
      - 5.2.0 (MRv2) [#]_
      - CentOS 6 (6.2)
      - JDK 7 (1.7.0_45)
      - |version|-hadoop2
    * - Amazon EMR
      - Hadoop 2.4.0
      - AMI 3.8.0
      - JDK 7 (1.7.0_71)
      - |version|-hadoop2

..  [#] 検証バージョンとは、Hadoopディストリビューションの動作検証に使用したAsakusa Frameworkのバージョンです。
        詳しくは :doc:`../administration/deployment-guide` を参照してください。
..  [#] MapReduce Version 1 (MRv1) では動作検証を行なっていません。
..  [#] MapReduce Version 1 (MRv1) では動作検証を行なっていません。

アプリケーション開発環境
========================

Asakusa Frameworkを利用したバッチアプリケーションの開発環境は、 以下のプラットフォームで動作を検証しています。

..  list-table:: 動作検証プラットフォーム(開発環境)
    :widths: 2 4 4
    :header-rows: 1

    * - 種類
      - Product
      - Version
    * - OS
      - Ubuntu Desktop
      - 12.04 / 14.04
    * - OS
      - MacOSX [#]_
      - 10.7 / 10.9
    * - OS
      - Windows [#]_
      - 7(SP1) / 8.1
    * - Java
      - JDK [#]_
      - 1.7.0_76 [#]_ [#]_
    * - ビルドツール
      - Gradle [#]_
      - 2.2.1
    * - ビルドツール
      - Apache Maven
      - 3.0.5 [#]_
    * - IDE
      - Eclipse IDE for Java Developers
      - 4.4.2 / 4.5.0
    * - IDE
      - IntelliJ IDEA Community Edition [#]_
      - 14.0.3
    * - Hadoop
      - Apache Hadoop
      - 1.2.1 / 2.6.0 [#]_

..  [#] MacOSX上では基本的な動作のみ検証しています。
..  [#] Windows上ではテストドライバを利用したテストは :doc:`エミュレーションモード <../testing/emulation-mode>` のみ使用できます。Windowsの利用については、 :doc:`../introduction/start-guide-windows` を参照してください。
..  [#] JREでは一部の機能が動作しません。必ずJDKを使用してください。
..  [#] JDK6の利用は非推奨です。JDK6の利用については、 :doc:`../application/using-jdk` を参照してください。
..  [#] JDK8の利用は基本的な動作のみ検証しています。
..  [#] Gradleの利用については、 :doc:`../application/gradle-plugin` を参照してください。
..  [#] Mavenの利用は非推奨です。Mavenの利用については、 :doc:`../application/maven-archetype` を参照してください。
..  [#] IntelliJ IDEAの利用は試験的機能として提供しています。IntelliJ IDEAの利用については :doc:`../application/gradle-plugin` - :ref:`gradle-plugin-using-idea` を参照してください。
..  [#] 開発環境ではHadoop1系の利用を推奨しています。詳しくは :doc:`../application/using-hadoop` を参照してください。

WindGate
========

:doc:`WindGate <../windgate/index>` は以下のプラットフォームで動作を検証しています。

..  list-table:: 動作検証プラットフォーム(WindGate/JDBC [#]_ )
    :widths: 2 4 4
    :header-rows: 1

    * - 種類
      - Product
      - Version
    * - DBMS
      - PostgreSQL
      - 9.3
    * - JDBC Driver
      - PostgreSQL JDBC Driver
      - 9.1 Build 901

..  [#] データベースを利用しない場合(例えば WindGate/CSV のみを使う場合)には不要です

ThunderGate
===========

:doc:`ThunderGate <../thundergate/index>` は以下のプラットフォームで動作を検証しています。

..  list-table:: 動作検証プラットフォーム(ThunderGate)
    :widths: 2 4 4
    :header-rows: 1

    * - 種類
      - Product
      - Version
    * - DBMS
      - MySQL Server
      - 5.5.25
    * - JDBC Driver
      - MySQL Connector/J
      - 5.1.25

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
    * - MapR
      - http://www.mapr.com/
    * - Hortonworks Data Platform
      - http://hortonworks.com/hdp/
    * - Cloudera CDH
      - http://www.cloudera.com/content/cloudera/en/products-and-services/cdh.html
    * - Amazon EMR
      - http://aws.amazon.com/elasticmapreduce/
    * - CentOS
      - http://www.centos.org/
    * - Ubuntu
      - http://www.ubuntu.com/
    * - MacOSX
      - http://www.apple.com/osx/
    * - Windows
      - http://windows.microsoft.com/
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
