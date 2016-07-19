====================
対応プラットフォーム
====================

Hadoopディストリビューション
============================

Asakusa Framework は、以下のHadoopディストリビューションと組み合わせた運用環境で動作を検証しています。

..  list-table:: 動作検証プラットフォーム(Hadoopディストリビューション)
    :header-rows: 1
    :widths: 3 2 3 2

    * - Distribution
      - Version
      - OS
      - JDK
    * - Hortonworks Data Platform
      - 2.4.0
      - CentOS 7 (7.2)
      - JDK 8 (1.8.0_60)
    * - MapR
      - 5.1.0 (MRv2) [#]_
      - Red Hat Enterprise Linux 7.2
      - JDK 8 (1.8.0_51)
    * - CDH
      - 5.7.0 (MRv2) [#]_
      - CentOS 6 (6.7)
      - JDK 7 (1.7.0_67)
    * - Amazon EMR
      - 4.7.1
      - Amazon Linux 2016.03 based
      - JDK 8 (1.8.0_71)
    * - Microsoft Azure HDInsight
      - 3.4
      - Ubuntu 14.04.4
      - JDK 7 (1.7.0_95)

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
      - 16.04
    * - OS
      - Windows [#]_
      - 7(SP1) / 10
    * - OS
      - MacOSX [#]_
      - 10.11
    * - Java
      - JDK [#]_ [#]_
      - 1.7.0_79 / 1.8.0_91
    * - ビルドツール
      - Gradle [#]_
      - 2.14.1
    * - IDE
      - Eclipse IDE for Java Developers
      - 4.5.2 / 4.6.0
    * - IDE
      - IntelliJ IDEA Community Edition [#]_
      - 2016.1
    * - Hadoop
      - Apache Hadoop [#]_
      - 2.7.2

..  [#] Windows上ではテストドライバーを利用したテストは :doc:`エミュレーションモード <../testing/emulation-mode>` のみ使用できます。Windowsの利用については、 :doc:`../introduction/start-guide-windows` を参照してください。
..  [#] MacOSX上では基本的な動作のみ検証しています。
..  [#] JREでは一部の機能が動作しません。必ずJDKを使用してください。
..  [#] 開発環境に対するJavaのセットアップについては、 :doc:`../application/using-jdk` を参照してください。
..  [#] Gradleの利用については、 :doc:`../application/gradle-plugin` を参照してください。
..  [#] IntelliJ IDEAの利用は試験的機能として提供しています。IntelliJ IDEAの利用については :doc:`../application/gradle-plugin` - :ref:`gradle-plugin-using-idea` を参照してください。
..  [#] 開発環境に対するHadoopのセットアップについては、 :doc:`../application/using-hadoop` を参照してください。

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
    * - Hortonworks Data Platform
      - http://hortonworks.com/hdp/
    * - MapR
      - http://www.mapr.com/
    * - Cloudera CDH
      - http://www.cloudera.com/content/cloudera/en/products-and-services/cdh.html
    * - Amazon EMR
      - http://aws.amazon.com/elasticmapreduce/
    * - Microsoft Azure HDInsight
      - https://azure.microsoft.com/services/hdinsight/
    * - CentOS
      - http://www.centos.org/
    * - Ubuntu
      - http://www.ubuntu.com/
    * - Windows
      - http://windows.microsoft.com/
    * - MacOSX
      - http://www.apple.com/osx/
    * - JDK (Java SE)
      - http://www.oracle.com/technetwork/java/javase/index.html
    * - Gradle
      - http://www.gradle.org/
    * - Eclipse
      - http://www.eclipse.org/
    * - IntelliJ IDEA
      - https://www.jetbrains.com/idea/
    * - PostgreSQL
      - http://www.postgresql.org/
