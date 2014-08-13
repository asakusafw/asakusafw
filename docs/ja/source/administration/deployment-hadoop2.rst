================================================
Hadoop2系の運用環境でAsakusa Frameworkを利用する
================================================

この文書は、Apache Hadoopのバージョン2系(以下「Hadoop2系」と表記)、
またはこれをベースとするHadoopディストリビューション上で
Asakusa Frameworkを利用する方法について説明します。

..  attention::
    Asakusa Frameworkの現在のバージョン |version| では、Hadoop2系の対応は試験的機能として提供されています。

..  note::
    Asakusa Frameworkが動作検証を行なっているHadoopディストリビューションについては、 :doc:`../product/target-platform` を参照してください。

はじめに
========
Hadoop2系はHadoop1系からいくつかの非互換性を含む変更 [#]_ が行われています。
この影響により、通常のAsakusa FrameworkのバージョンはHadoop2系の環境では動作しません。

このため、Asakusa Framework バージョン ``0.5.0`` 以降では、
通常のAsakusa Frameworkのバージョンのほかに
Hadoop2系で動作するAsakusa Frameworkのバージョンを試験的に提供しています。

Hadoop2系向けのAsakusa Frameworkのバージョン名は、
``[asakusafw.version]-hadoop2`` のように、通常のバージョン番号の後ろに ``-hadoop2`` を加えたものになります。
例えば、Asakusa Frameworkのバージョン ``0.7.0`` のHadoop2系向けバージョンは、
``0.7.0-hadoop2`` となります。

Hadoop2系でAsakusa Frameworkを利用する場合、
Hadoop2系向けのAsakusa Frameworkに対するデプロイメントアーカイブを生成し、
これを用いて運用環境を構築する必要があります。

なお、開発環境で生成するバッチアプリケーションについては
Asakusa Framework ``0.5.0`` 以降で開発およびビルドしたものに限り、
Hadoop1系とHadoop2系のいずれのAsakusa Frameworkでも動作するようになっています [#]_ 。

..  [#] Apache Hadoopの非互換性を含む変更については、 `Apache Hadoop Documentation`_ に含まれる Release Notes や Change Log などを参照してください。

..  [#] これはAsakusa Frameworkが上述の非互換性の影響を受けないようにバッチアプリケーションを構成する、ということであり、
    すべてのHadoopディストリビューション、及びHadoopバージョン間の互換性を保証するものではありません。

..  _`Apache Hadoop Documentation`: http://hadoop.apache.org/docs/current/

Hadoop2系向けAsakusa Frameworkのデプロイメントアーカイブ生成
============================================================
Hadoop2系向けAsakusa Frameworkのデプロイメントアーカイブ生成は、
使用している開発環境に応じて以下の説明を参照してください。

Asakusa Gradle Pluginを利用する
-------------------------------
ビルドシステムに :doc:`Gradle <../application/gradle-plugin>` を利用している場合は、
``build.gradle`` の ``asakusafwOrganizer/asakusafwVersion`` に
Hadoop2系向けのAsakusa Frameworkバージョンを指定してデプロイメントアーカイブを指定します。

詳しくは、 :doc:`../application/gradle-plugin` の :ref:`include-hadoop-gradle-plugin` や
:ref:`standalone-organizer-gradle-plugin` を参照してください。

Framework Organizerを利用する
-----------------------------
ビルドシステムに :doc:`Maven <../application/maven-archetype>` を利用している場合は、
Hadoop2系向けのAsakusa Frameworkのデプロイメントアーカイブを生成するには、
通常のデプロイメントアーカイブの生成方法と同様に
`Framework Organizer`_ を利用します。

`Framework Organizer`_ の ``pom.xml`` で定義されている
Asakusa Frameworkのバージョンを示すプロパティ ``asakusafw.version`` の値に
Hadoop2系向けのAsakusa Frameworkバージョン( ``[asakusafw.version]-hadoop2`` )を指定します。

以下は、Asakusa Framework のバージョン ``0.7.0`` のHadoop2系向け
デプロイメントアーカイブを生成する例です [#]_ 。

..  code-block:: sh
     
    cd asakusafw-organizer
    mvn package -Dasakusafw.version=0.7.0-hadoop2

これにより、Framework Organizerの ``target`` ディレクトリに
Hadoop2系向けデプロイメントアーカイブ(例えば ``asakusafw-0.7.0-hadoop2-prod-windgate.tar.gz`` )
が作成されます。

Hadoop2系の運用環境上にAsakusa Frameworkをデプロイする方法は
通常のAsakusa Frameworkのデプロイ方法と同様です。
詳しくはAsakusa Frameworkのドキュメント `運用環境の整備`_ などを参照してください。

..  warning::
    Hadoop2系向けバージョンは開発環境で使用するための動作検証は十分に行っていないため、
    Hadoop2系向けバージョンは開発環境にインストール ( ``antrun:run`` ) しないことを推奨します。
    
    通常は開発環境にはHadoop1系向けバージョンを利用してください。
    また、開発環境の整備方法は :doc:`../application/index` を参照してください。

..  [#] Framework Organizer のインストールについては、 `Framework Organizer`_ を参照してください。

..  _`Framework Organizer`: http://asakusafw.s3.amazonaws.com/documents/latest/release/ja/html/administration/framework-organizer.html
..  _`運用環境の整備`: http://asakusafw.s3.amazonaws.com/documents/latest/release/ja/html/administration/index.html

    
