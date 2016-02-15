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
    * - MapR
      - 5.0.0 (MRv2) [#]_
      - Red Hat Enterprise Linux 7.1
      - JDK 8 (1.8.0_51)
    * - Hortonworks Data Platform
      - 2.3.2
      - CentOS 7 (7.1)
      - JDK 8 (1.8.0_40)
    * - CDH
      - 5.2.0 (MRv2) [#]_
      - CentOS 6 (6.2)
      - JDK 7 (1.7.0_45)
    * - Amazon EMR
      - Hadoop 2.6.0
      - AMI 4.1.0
      - JDK 7 (1.7.0_85)

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
      - 10.9 / 10.11
    * - OS
      - Windows [#]_
      - 7(SP1) / 10
    * - Java
      - JDK [#]_ [#]_
      - 1.7.0_76 / 1.8.0.66
    * - ビルドツール
      - Gradle [#]_
      - 2.11
    * - IDE
      - Eclipse IDE for Java Developers
      - 4.4.2 / 4.5.1
    * - IDE
      - IntelliJ IDEA Community Edition [#]_
      - 14.0.3
    * - Hadoop
      - Apache Hadoop [#]_
      - 2.7.2

..  [#] MacOSX上では基本的な動作のみ検証しています。
..  [#] Windows上ではテストドライバを利用したテストは :doc:`エミュレーションモード <../testing/emulation-mode>` のみ使用できます。Windowsの利用については、 :doc:`../introduction/start-guide-windows` を参照してください。
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
    * - Eclipse
      - http://www.eclipse.org/
    * - PostgreSQL
      - http://www.postgresql.org/
