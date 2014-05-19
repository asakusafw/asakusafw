=============================
Framework Organizer利用ガイド
=============================
この文書では、Asakusa Frameworkの構成ツールであるFramework Organizerについて説明します。

Framework Organizerを使ってAsakusa Frameworkのデプロイメントメントアーカイブを生成する方法、及び開発環境にAsakusa Frameworkをインストールする方法などを説明します。

Framework Organizerの概要
=========================
Framework Organizerが提供する機能には、主に以下のようなものがあります。

Asakusa Frameworkのデプロイメントアーカイブを生成
-------------------------------------------------
* Asakusa Framework本体を開発環境、及び運用環境にデプロイするためのデプロイメントアーカイブを生成します。
* 構成情報を変更して、Asakusa Frameworkのバージョンを指定したり、Asakusa Frameworkの拡張モジュール [#]_ を生成することもできます。

..  [#] :doc:`../administration/deployment-extension-module`

Asakusa Frameworkを開発環境にインストール
-----------------------------------------
* 開発環境用に構成されたAsakusa Frameworkのデプロイメントアーカイブを開発環境にインストールします。

..  note::
    Asakusa Framework バージョン ``0.5.0`` より前のバージョンでは、これらの機能はMavenアーキタイプから生成したバッチアプリケーションプロジェクトのpom.xmlの定義によって提供されていました。
    
    バージョン ``0.5.0`` 以降ではこれらの構成情報が分離されたことにより、バッチアプリケーションプロジェクトの構成情報がシンプルになり、またアプリケーションの構成情報とは独立してAsakusa Framework本体の構成情報を定義することが出来るため、開発環境と運用環境で異なるAsakusa Frameworkのバージョンを利用することが容易となっています。

Framework Organizerのインストール
=================================
Framework Organizer は以下からダウンロードします。

* http://www.asakusafw.com/download/framework-organizer/asakusafw-organizer-0.6.2.tar.gz

ダウンロードが完了したら、任意のディレクトリでFramework Organizerを展開します。

..  code-block:: sh
     
    tar xf asakusafw-organizer-*.tar.gz

..  attention::
    開発環境で利用する場合、Framework Organizerを ``$ASAKUSA_HOME`` 配下に **展開しない** ことを強く推奨します。
    ``$ASAKUSA_HOME`` は Asakusa Framework本体を再インストールする都度初期化されるため、
    意図せず Framework Organizerの構成情報を失う可能性があります。

.. _deployment-archive-maven-archetype:

Asakusa Frameworkのデプロイメントアーカイブ生成
===============================================
Framework Organizerに含まれる ``pom.xml`` に対してMavenの ``package`` ゴールを実行すると、
Asakusa Framework本体のデプロイメントアーカイブがFramework Organizerの ``target`` ディレクトリ直下に作成されます。

Framework Organizerからデプロイメントアーカイブを生成する手順の例を以下に示します。

..  code-block:: sh
     
    cd asakusafw-organizer
    mvn package

Framework Organizerの標準構成では、以下のデプロイメントアーカイブを生成します。

..  list-table:: Framework Organizerが標準で生成するデプロイメントアーカイブ一覧
    :widths: 4 6
    :header-rows: 1
    
    * - ファイル名
      - 説明
    * - ``asakusafw-${asakusafw-version}-dev.tar.gz``
      - Asakusa Frameworkを開発環境に展開するためのアーカイブ。Framework Organizerに対して ``antrun:run`` ゴールを実行することによって、このアーカイブを開発環境にインストールする。
    * - ``asakusafw-${asakusafw-version}-windgate.tar.gz``
      - Asakusa FrameworkとWindGateを運用環境に展開するためのアーカイブ。詳しくは :doc:`../administration/deployment-with-windgate` を参照してください。

..  note::
    WindGate向けのデプロイメントアーカイブ ``asakusafw-${asakusafw-version}-windgate.tar.gz`` には Direct I/O も含まれているため、Direct I/Oを使う運用環境に対しても、このアーカイブを利用することができます。

Asakusa Frameworkのバージョンを指定する
---------------------------------------
Framework Organizerに対してAsakusa Frameworkのバージョンを指定することで、指定したバージョンのAsakusa Frameworkを含むデプロイメントアーカイブを生成することができます。

Asakusa Frameworkのバージョン指定はFramework Organizerの ``pom.xml`` が持つプロパティ ``asakusafw.version`` で指定します。
このプロパティを変更するには、 ``pom.xml`` を編集するか、 ``mvn`` コマンドのパラメータとして指定します。

pom.xml を変更する
~~~~~~~~~~~~~~~~~~
``pom.xml`` を編集する場合、 ``properties`` 要素のサブ要素として指定されている ``asakusafw.version`` の内容を変更します。
以下は、 ``asakusafw.version`` に Asakusa Frameworkのバージョン ``0.6.2`` を指定する例です。

..  code-block:: xml
   
    ...
     
	<properties>
		<asakusafw.version>0.6.2</asakusafw.version>
	
    ...

コマンドパラメータで指定する
~~~~~~~~~~~~~~~~~~~~~~~~~~~~
``mvn`` コマンドのパラメータで指定する場合、 ``-D`` オプションに続いてプロパティキーとその値を指定します。

以下は、Asakusa Framework のバージョン ``0.6.2`` で試験的に提供している、Hadoop2系で利用するためのAsakusa Frameworkバージョンを指定する例です [#]_ 。

..  code-block:: sh
     
    cd asakusafw-organizer
    mvn package -Dasakusafw.version=0.6.2-hadoop2

..  [#] Hadoop2系でAsakusa Frameworkを利用する方法について詳しくは :doc:`deployment-hadoop2` を参照してください。

生成するデプロイメントアーカイブを指定する
------------------------------------------
Framework Organizerに対してデプロイメントアーカイブの種類を指定することで、指定したモジュール構成を持つAsakusa Frameworkのデプロイメントアーカイブを生成することができます。

Framework Organizerで指定することが出来るデプロイメントアーカイブの一覧を以下に示します。

..  list-table:: デプロイメントアーカイブ一覧
    :widths: 2 3 5
    :header-rows: 1
    
    * - 記述指定子
      - ファイル名
      - 説明
    * - ``dev``
      - ``asakusafw-${asakusafw-version}-dev.tar.gz``
      - Asakusa Frameworkを開発環境に展開するためのアーカイブ。後述の ``antrun:run`` ゴールを実行することによって開発環境にインストールする。
    * - ``prod-windgate``
      - ``asakusafw-${asakusafw-version}-windgate.tar.gz``
      - Asakusa FrameworkとWindGateを運用環境に展開するためのアーカイブ。詳しくは :doc:`../administration/deployment-with-windgate` を参照してください。
    * - ``prod-thundergate``
      - ``asakusafw-${asakusafw-version}-prod-thundergate.tar.gz``
      - Asakusa FrameworkとThunderGateを運用環境に展開するためのアーカイブ。詳しくは :doc:`../administration/deployment-with-thundergate` を参照してください。
    * - ``prod-directio``
      - ``asakusafw-${asakusafw-version}-directio.tar.gz``
      - Asakusa Frameworkを運用環境に展開するためのアーカイブ。詳しくは :doc:`../administration/deployment-with-directio` を参照してください。

生成するデプロイメントアーカイブを指定するには、Framework Organizerの ``pom.xml`` に対して ``maven-assembly-plugin`` のプラグインの定義にデプロイメントアーカイブ生成用の設定を追加します。

上表「デプロイメントアーカイブ一覧」の ``記述指定子`` から利用するデプロイメントアーカイブの記述指定子を確認し、その値を ``maven-assembly-plugin`` の設定 ``plugin/executions/execution/configuration/descriptorRefs/descriptorRef`` 要素の値として設定します。

以下はDirect I/O用のデプロイメントアーカイブ を生成する ``pom.xml`` の設定例です。

..  code-block:: xml

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-assembly-plugin</artifactId>
                <version>${plugin.assembly.version}</version>
                <dependencies>
                    <dependency>
                        <groupId>com.asakusafw</groupId>
                        <artifactId>asakusa-distribution</artifactId>
                        <version>${asakusafw.version}</version>
                    </dependency>
                </dependencies>
                <executions>
                    <execution>
                        <id>default-cli</id>
                        <phase>package</phase>
                        <goals>
                            <goal>single</goal>
                        </goals>
                        <configuration>
                            <descriptorRefs>
                                <descriptorRef>dev</descriptorRef>
                                <descriptorRef>prod-windgate</descriptorRef>
                                <!-- Direct I/O用のデプロイメントアーカイブを追加 -->
                                <descriptorRef>prod-directio</descriptorRef>
                            </descriptorRefs>
                            <finalName>asakusafw-${asakusafw.version}</finalName>
                        </configuration>
                    </execution>
                </executions>
            </plugin>

上記の設定を追加後、Framework Organizerの ``pom.xml`` に対して ``mvn package`` を実行します。Framework Organizerの ``target`` ディレクトリ配下に指定したデプロイアーカイブが生成されます。

拡張モジュール用のデプロイメントアーカイブを生成する
----------------------------------------------------
Asakusa Frameworkでは標準的な構成を持つデプロイメントアーカイブのほかに、固有の用途で利用するための拡張モジュールを提供しています。

拡張モジュールも上記と同様の手順で Framework Organizerの ``pom.xml`` に記述指定子 (拡張モジュールID) を持つ定義を追加することで、拡張モジュール用のデプロイメントアーカイブを生成することが出来ます。

拡張モジュールの一覧やその利用方法については、 :doc:`../administration/deployment-extension-module` を参照してください。


Asakusa Frameworkのインストール
===============================
Framework Organizerの ``pom.xml`` に対してMavenの ``antrun:run`` ゴールを実行すると、先述の `Asakusa Frameworkのデプロイメントアーカイブ生成`_  で作成した開発環境用のAsakusa Frameworkのデプロイメントアーカイブを使用して、 ``$ASAKUSA_HOME`` 配下にAsakusa Frameworkがインストールされます。

..  code-block:: sh
    
    cd asakusafw-organizer
    mvn antrun:run

通常は開発環境用のデプロイメントアーカイブの生成と、Asakusa Frameworkのインストールを同時に行うことが多いでしょう。
この場合、以下のように記述することができます。

..  code-block:: sh
    
    cd asakusafw-organizer
    mvn package antrun:run

..  attention::

    ``antrun:run`` ゴールを実行した際に、 ``$ASAKUSA_HOME`` で指定したディレクトリが存在しない場合は、ディレクトリを作成した後、その配下にAsakusa Frameworkの各ファイルがインストールされます。
    
    既に ``$ASAKUSA_HOME`` にディレクトリが存在した場合は、既存のディレクトリをタイムスタンプ付のディレクトリ名 ( ``$ASAKUSA_HOME_yyyyMMddHHmmss`` ) でリネームした上で、 ``$ASAKUSA_HOME`` に新規にディレクトリを再作成した後、その配下にAsakusa Frameworkの各ファイルがインストールされます。


Asakusa Frameworkのバージョンを指定してインストールする
-------------------------------------------------------
`Asakusa Frameworkのデプロイメントアーカイブ生成`_ の `Asakusa Frameworkのバージョンを指定する`_ で、 デプロイメントアーカイブの生成時にAsakusa Frameworkのバージョンを指定することを説明しましたが、Asakusa Frameworkのインストールにおいても、プロパティ ``asakusafw.version`` で指定したAsakusa Frameworkのバージョンを使ってインストールが行われます。

注意点として、 `Asakusa Frameworkのバージョンを指定する`_ の `コマンドパラメータで指定する`_  で ``-D`` オプションでAsakusa Frameworkのバージョンを指定してデプロイメントアーカイブを生成した場合で、かつこのバージョンのAsakusa Frameworkを開発環境にインストールしたい場合、 ``antrun:run`` ゴールの実行時にも同じAsakusa Frameworkのバージョンを指定する必要があります。

..  code-block:: sh
    
    cd asakusafw-organizer
    mvn antrun:run -Dasakusafw.version=0.6.2-hadoop2

コマンドパラメータでバージョンを指定する場合も、
デプロイメントアーカイブの生成とAsakusa Frameworkのインストールを同時に行うことができます。

..  code-block:: sh
    
    cd asakusafw-organizer
    mvn package antrun:run -Dasakusafw.version=0.6.2-hadoop2


ThunderGateを利用する場合の追加設定
-----------------------------------
開発環境でThunderGateを使ったバッチアプリケーションの開発を行う場合、 :ref:`thundergate-jdbc-configuration-file` を指定するためのターゲット名をインストール時に指定する必要があります。

ThunderGateのターゲット名指定はFramework Organizerの ``pom.xml`` に対してプロパティ ``thundergate.target`` で指定します。
このプロパティを指定するには、 ``pom.xml`` を編集するか、 ``mvn`` コマンドのパラメータとして指定します。

..  hint::
    Framework Organizerに対してプロパティを指定する方法は、 `Asakusa Frameworkのバージョンを指定する`_ を参考にしてください。

..  warning::
    プロパティ ``thundergate.target`` を使用している場合、 ``antrun:run`` を実行すると、ThunderGate用のインストール処理が追加され、その中でThunderGateが使用するテンポラリディレクトリが作成されます。
    
    このディレクトリはデフォルトの設定では ``/tmp/thundergate-asakusa`` となっていますが、一部のLinuxディストリビューションではOSをシャットダウンしたタイミングで ``/tmp`` ディレクトリの内容が消去されるため、再起動後にこのディレクトリを再度作成する必要があります。
    
    テンポラリディレクトリを変更する場合、 ``$ASAKUSA_HOME/bulkloader/conf/bulkloader-conf-db.properties`` の設定値を変更した上で、設定値に対応したテンポラリディレクトリを作成し、このディレクトリのパーミッションを777に変更します。
    
    例えばテンポラリディレクトリを ``/var/tmp/asakusa`` に変更する場合は以下のようにします。

    * ``$ASAKUSA_HOME/bulkloader/conf/bulkloader-conf-db.properties`` の変更
    
        * ``import.tsv-create-dir=/var/tmp/asakusa/importer``
        * ``export.tsv-create-dir=/var/tmp/asakusa/exporter``
    
    * テンポラリディレクトリの作成

        ..  code-block:: sh
    
            mkdir -p -m 777 /var/tmp/asakusa/importer
            mkdir -p -m 777 /var/tmp/asakusa/exporter

