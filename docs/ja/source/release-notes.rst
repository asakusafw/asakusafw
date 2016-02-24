==============
リリースノート
==============

Release 0.8.0
=============

XXX XX, 2016

新機能と主な変更点
------------------

互換性に関して
--------------

変更点
~~~~~~

本リリースでは、対応プラットフォームに関する重要な変更と非互換性があります。

..  warning::
    バージョン 0.8.0 は以前のバージョンからいくつかの重要な変更が行われました。
    過去のバージョンからのマイグレーションを検討する際には必ず以下の内容を確認してください。
    
Java (JDK)
  Java6、およびJDK 6は非対応になりました。
  
  Java6、およびJDK 6を利用している場合、Java 7(JDK 7)、またはJava 8 (JDK 8)に移行する必要があります。
  
Hadoop
  Hadoop1系は非対応となりました。

  開発環境にHadoop1系をインストールしている場合、Hadoop2系をインストールしてAsakusa FrameworkからはHadoop2系を利用するよう設定してください。
  
  運用環境でHadoop1系を利用している場合、Hadoop2系に移行する必要があります。

Gradle
  Gradleのバージョン1系は非対応になりました。
  
  また、Asakusa Gradle Pluginにいくつか仕様変更が行われ、一部のタスクの動作やビルドスクリプトの設定方法が変更されています。
  
Maven
  Mavenの利用は非対応になりました。
  
  Mavenを利用しているアプリケーションプロジェクトは、Gradleを利用するよう移行する必要があります。

Asakusa Framework
  Hadoop1系が非対応となったことにより、Asakusa Framwork バージョン 0.7.0 から導入された「Hadoopバージョン」が廃止になりました。
  
  Asakusa Framework 0.7系では、Asakusa Framworkのバージョンは ``<version>-hadoop1``, ``<version>-hadoop2`` のように、利用するHadoopのバージョンを持つバージョン体系を導入していました。
  
  本リリース以降は、Asakusa Frameworkのバージョンは単一のバージョン体系 ( 例えば本リリースのバージョンは ``0.8.0`` ) を使用します。

変更内容の詳細やマイグレーション手順については、以下のドキュメント説明しています。

* :doc:`application/migration-guide`
* :doc:`administration/migration-guide`

..  attention::
    過去のバージョンからのマイグレーション作業を行う場合、必ず :doc:`application/migration-guide` と :doc:`administration/migration-guide` を確認してください。

リンク
======

* :doc:`previous-release-notes`
* :doc:`changelogs`

