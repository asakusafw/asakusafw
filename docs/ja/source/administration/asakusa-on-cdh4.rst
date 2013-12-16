===================================
CDH4上でAsakusa Frameworkを利用する
===================================

..  warning::
    Asakusa Framework バージョン ``0.5.3`` 以降、
    CDH4を含むHadoop2系の環境でAsakusa Frameworkを利用するためのドキュメントは、
    Asakusa Frameworkのドキュメント
    `Hadoop2系の運用環境でAsakusa Frameworkを利用する`_
    に統合されました。
     
    このページの内容は古くなっている可能性があるため、上記のドキュメントを参照してください。
    またこのページは将来削除される予定です。

..  _`Hadoop2系の運用環境でAsakusa Frameworkを利用する`: http://asakusafw.s3.amazonaws.com/documents/latest/release/ja/html/administration/deployment-hadoop2.html

* 対象バージョン: Asakusa Framework ``0.5.0`` 以降

この文書は、 `Cloudera`_ が提供する
Hadoopディストリビューション `CDH4`_ 上で
Asakusa Frameworkを利用する方法について説明します。

..  _`Cloudera`: http://www.cloudera.com/
..  _`CDH4`: http://www.cloudera.com/content/support/en/documentation/cdh4-documentation/cdh4-documentation-v4-latest.html

はじめに
========
`CDH4`_ は CDH3 からいくつかの非互換性を含む変更 [#]_ が行われています。
この影響により、通常のAsakusa Frameworkのバージョンでは
CDH4上では動作しません。

このため、Asakusa Framework バージョン ``0.5.0`` 以降では試験的に、
通常のAsakusa Frameworkのバージョンのほかに、
CDH4上で動作するAsakusa Frameworkのバージョンを
個別に提供しています。

CDH4上でAsakusa Frameworkを利用する場合、
CDH4向けバージョンを使って
Asakusa Frameworkのデプロイメントアーカイブを生成し、
これを用いて運用環境を構築する必要があります。

なお、開発環境で生成するバッチアプリケーションについては
Asakusa Framework ``0.5.0`` 以降では
開発環境と運用環境で異なるHadoopバージョンを
利用しても動作するようになっています [#]_ 。

..  [#] CDH4の非互換性を含む変更については、 `CDH4 Release Notes`_ に含まれる Incompatible Changes などを参照してください。

..  [#] これは上述の非互換性の影響を受けないようバッチアプリケーションを構成する、ということであり、
    すべてのHadoopディストリビューション、及びHadoopバージョン間の互換性を保証するものではありません。

..  _`CDH4 Release Notes`: http://www.cloudera.com/content/cloudera-content/cloudera-docs/CDH4/latest/CDH4-Release-Notes/CDH4-Release-Notes.html

CDH4向けAsakusa Frameworkのデプロイメントアーカイブ生成
=======================================================
CDH4向けのAsakusa Frameworkのデプロイメントアーカイブを生成するには、
通常のデプロイメントアーカイブの生成方法と同様に
`Framework Organizer`_ を利用します。

`Framework Organizer`_ の ``pom.xml`` に対して、
Asakusa Frameworkのバージョンを示すプロパティ ``asakusafw.version`` を
``[version]-hadoop2`` のように、通常のバージョン番号の後ろに ``-hadoop2`` を加えたものを指定します。
例えば、Asakusa Frameworkのバージョン ``0.5.0`` のCDH4向けバージョンは、
``0.5.0-hadoop2`` となります。

以下は、Asakusa Framework のバージョン ``0.5.0`` のCDH4向けデプロイメントアーカイブを生成する例です [#]_ 。

..  code-block:: sh
     
    cd asakusafw-organizer
    mvn package -Dasakusafw.version=0.5.0-hadoop2

..  warning::
    Framework Organizerでデプロイメントアーカイブを生成すると、
    デフォルトで開発環境向けのデプロイメントアーカイブ
    (例えば ``asakusafw-0.5.0-hadoop2-dev.tar.gz`` )も生成されますが、
    CDH4向けバージョンは開発環境で使用するための動作検証は行っていないため、
    CDH4向けバージョンは開発環境にインストール ( ``antrun:run`` ) しないことを推奨します

これにより、Framework Organizerの ``target`` ディレクトリに
CDH4向けデプロイメントアーカイブ(例えば ``asakusafw-0.5.0-hadoop2-prod-windgate.tar.gz`` )
が作成されます。

CDH4の運用環境上にAsakusa Frameworkをデプロイする方法は
通常のAsakusa Frameworkのデプロイ方法と同様です。
詳しくはAsakusa Frameworkのドキュメント `運用環境の整備`_ などを参照してください。

..  [#] Framework Organizer のインストールについては、 `Framework Organizer`_ を参照してください。

..  _`Framework Organizer`: http://asakusafw.s3.amazonaws.com/documents/latest/release/ja/html/administration/framework-organizer.html
..  _`運用環境の整備`: http://asakusafw.s3.amazonaws.com/documents/latest/release/ja/html/administration/index.html

    
