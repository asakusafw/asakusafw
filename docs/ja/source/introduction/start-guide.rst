================================
Asakusa Framework スタートガイド
================================
この文書では、Asakusa Frameworkをはじめて利用するユーザ向けに、Asakusa Frameworkの開発環境を作成し、その環境でサンプルアプリケーションを動かすまでの手順を説明します。

なお、この文書では開発環境の構築に必要となる各種ソフトウェアのバージョンは明記していません。Asakusa Frameworkが動作検証を行っている各種ソフトウェアのバージョンについては、 :doc:`../product/target-platform` の :ref:`target-platform-development-environment` を確認してください。

.. _startguide-development-environment:

開発環境の構築
==============
Asakusa FrameworkはLinux OS上に開発環境を構築して利用します。WindowsPC上で開発を行う場合、Windows上でLinuxの仮想マシンを実行し、ここで開発を行うと便利です。

このスタートガイドでは仮想マシンの実行ソフトウェアとして `VMWare Player`_ 、仮想マシンに使用するOSとして `Ubuntu 12.04 Desktop (日本語 Remix CD x86用)`_ を使用し、この環境に必要なソフトウェアをセットアップする手順を説明します。

..  _`VMWare Player`: http://www.vmware.com/jp/products/desktop_virtualization/player/overview 
..  _`Ubuntu 12.04 Desktop (日本語 Remix CD x86用)`: http://www.ubuntulinux.jp/download/ja-remix 

..  tip::
    開発環境の構築については、ここで説明するセットアップ手順を実施するほか、Asakusa Frmameworkの開発環境を手軽に構築するインストーラパッケージである `Jinrikisha`_ (人力車) を利用する方法もあります。
    
    `Jinrikisha`_ を使ってインストールする場合、本書の :ref:`install-ubuntu` までの手順を実施し、その後は Jinrikisha のドキュメント (http://asakusafw.s3.amazonaws.com/documents/jinrikisha/ja/html/index.html) に従って開発環境を構築することができます。
    
    `Jinrikisha`_ ではインストール環境にJava(JDK)がインストールされていない場合、OpenJDKを簡易にインストールする機能が備わっていますが、試用目的以外でAsakusa Frameworkを使用する場合は 本書の :ref:`install-java` の手順を参考にしてOracleJDKをインストールした後に、Jinrikishaのドキュメントに従って開発環境を構築することを推奨します。

..  _`Jinrikisha`: http://asakusafw.s3.amazonaws.com/documents/jinrikisha/ja/html/index.html

VMWare Playerのインストール
---------------------------
VMWare Playerをダウンロードし、インストールを行います。

VMWare Playerのダウンロードサイト (http://www.vmware.com/go/get-player-jp) からVMWare Player (Windows用) をダウンロードします。

ダウンロードしたインストーラを実行し、インストール画面の指示に従ってVMWare Playerをインストールします。

.. _install-ubuntu:

Ubuntu Desktop のインストール
-----------------------------
Ubuntu Desktopをダウンロードし、インストールを行います。

Ubuntu Desktop 日本語 Remix CDのダウンロードサイト (http://www.ubuntulinux.jp/download/ja-remix) からisoファイル(CDイメージ)をダウンロードします。

ダウンロードが完了したらVMWare Playerを起動し、以下の手順に従ってUbuntu Desktopをインストールします。

1. メニューから「新規仮想マシンの作成」を選択します、
2. インストール元の選択画面では「後でOSをインストール」を選択し [#]_ 、次へ進みます。
3. ゲストOSの選択で「Linux」を選択し、バージョンに「Ubuntu」を選択して次へ進みます。
4. 仮想マシン名の入力では、任意の仮想マシン名と保存場所を指定して、次へ進みます。
5. ディスク容量の指定は任意です。デフォルトの「20GB」はAsakusa Frameworkの開発を試すには十分な容量です。お使いの環境に合わせて設定し、次へ進みます。
6. 仮想マシン作成準備画面で「ハードウェアをカスタマイズ」を選択します。デバイス一覧から「新規 CD/DVD(IDE)」を選択後、画面右側の「ISOイメージファイルを使用する」を選択し、参照ボタンを押下してダウンロードしたUbuntu Desktopのisoファイルを選択します。その他の設定は環境に合わせて設定してください。設定が完了したら画面下の閉じるボタンを押します。
7. 完了ボタンを押して仮想マシンを作成後、仮想マシンを起動すると、Ubuntu Desktopのインストールが開始します。インストール画面の指示に従ってUbuntu Desktopをインストールします。

Ubuntu Desktopが起動したら、同梱のブラウザなどを使用してUbuntuからインターネットにできることを確認してください。以後の手順ではインターネットに接続できることを前提とします。

また、以降の手順で使用するホームフォルダ直下のダウンロードディレクトリを日本語名から英語に変更するため、ターミナルを開いて [#]_ 以下のコマンドを実行します。

..  code-block:: sh

    LANG=C xdg-user-dirs-gtk-update

ダイアログが開いたら ``Don't ask me this again``  (次回からチェックしない) 」にチェックを入れ、 ``Update Names`` ボタンを押下します。

そのほか、必須の手順ではないですがここでVMWare Toolsをインストールしておくとよいでしょう [#]_ 。

..  [#] ここで「インストーラ ディスク イメージ ファイル」を選択し、isoファイルを選択するとOSの「簡易インストール」が行われますが、簡易インストールでは日本語環境がインストールされないほか、いくつかの設定が適切に行われないため、簡易インストールの使用は推奨しません。
..  [#] ターミナルを開くにはデスクトップ上で ``[ctrl]`` + ``[alt]`` + ``[t]`` キーを押すか、画面左上のDashメニューを選択し、検索ボックスに ``terminal`` と入力後、画面下に表示される ``端末`` を選択します。
..  [#] VMWare Toolsのインストールについては VMWare Playerのドキュメントなどを参照してください。

.. _install-java:

Java(JDK)のインストール
-----------------------
Hadoop、及びAsakusa Frameworkの実行に使用するJavaをインストールします。

ブラウザを開き、Javaのダウンロードサイト (http://www.oracle.com/technetwork/java/javase/downloads/index.html) から、Java SE 6 の インストールアーカイブ ``jdk-6uXX-linux-i586.bin`` ( ``XX`` はUpdate番号) をダウンロードします [#]_ 。本文書では、ブラウザ標準のダウンロードディレクトリ  ``~/Downloads`` にダウンロードしたものとして説明を進めます。

ダウンロードしたインストールアーカイブを実行します。

..  code-block:: sh

    cd ~/Downloads
    chmod +x jdk-*
    ./jdk-*

インストールに成功すると以下のような画面が表示されます。

..  code-block:: sh

    Unpacking...
    Checksumming...
    Extracting...
    ...
    ...

    Done.

作成されたJavaのディレクトリに対して適切なオーナーの指定やディレクトリ配置を行います。

..  code-block:: sh
    
    sudo mkdir /usr/lib/jvm
    sudo chown -R root:root jdk1.6.0_*/
    sudo mv jdk1.6.0_*/ /usr/lib/jvm
    
    cd /usr/lib/jvm
    sudo ln -s jdk1.6.0_* jdk-6

..  [#] 本スタートガイドの環境に従う場合は、x64版用のファイル( ``jdk-6uXX-linux-x64.bin`` )や、RPM版のファイル( ``jdk-6uXX-linux-i586-rpm.bin`` ) をダウンロードしないよう注意してください。


Mavenのインストール
-------------------
Asakusa Frameworkの開発環境に必要なビルドツールであるMavenをインストールします。

Mavenのダウンロードサイト (http://maven.apache.org/download.html) から Maven3 のtarball ``apache-maven-3.X.X-bin.tar.gz`` ( ``XX`` はバージョン番号 ) をダウンロードします。

ダウンロードが完了したら、以下の例を参考にしてMavenをインストールします。

..  code-block:: sh

    cd ~/Downloads
    tar xf apache-maven-*-bin.tar.gz
    sudo chown -R root:root apache-maven-*/
    sudo mv apache-maven-*/ /usr/local/lib
    sudo ln -s /usr/local/lib/apache-maven-*/bin/mvn /usr/local/bin/mvn

..  note:: 
    インターネットへの接続にプロキシサーバを経由する必要がある環境では、Mavenに対してプロキシの設定を行う必要があります。Mavenのプロキシ設定については、Mavenの次のサイト等を確認してください。

    http://maven.apache.org/guides/mini/guide-proxies.html

Hadoopのインストール
--------------------
`Apache Hadoop`_ をインストールします。

Apache Hadoopのインストール方法はOS毎に提供されているインストールパッケージを使う方法や、tarballを展開する方法などがありますが、ここではtarballを展開する方法でインストールします。

Apache Hadoopのダウンロードサイト (http://www.apache.org/dyn/closer.cgi/hadoop/common/) から Hadoop本体のコンポーネントのtarball ``hadoop-1.2.X.tar.gz`` ( ``X`` はバージョン番号 )  をダウンロードします。

ダウンロードが完了したら、以下の例を参考にしてApache Hadoopをインストールします。

..  code-block:: sh

    cd ~/Downloads
    tar xf hadoop-*.tar.gz
    sudo chown -R root:root hadoop-*/
    sudo mv hadoop-*/ /usr/lib
    sudo ln -s /usr/lib/hadoop-* /usr/lib/hadoop

..  _`Apache Hadoop`: http://hadoop.apache.org/

環境変数の設定
--------------
Asakusa Frameworkの利用に必要となる環境変数を設定します。

``~/.profile`` をエディタで開き、最下行に以下の定義を追加します。

..  code-block:: sh

    export JAVA_HOME=/usr/lib/jvm/jdk-6
    export ASAKUSA_HOME=$HOME/asakusa
    export PATH=$JAVA_HOME/bin:$PATH:/usr/lib/hadoop/bin

``~/.profile`` を保存した後、設定した環境変数をターミナル上のシェルに反映させるため、以下のコマンドを実行します。

..  code-block:: sh

    . ~/.profile

Asakusa Frameworkのインストール
-------------------------------
Asakusa Frameworkをインストールします。

Asakusa Frameworkを開発環境にインストールするには、Asakusa Frameworkの構成ツールである Framework Organizer を利用します。

このツールはMavenを使ってAsakusa Frameworkのインストールアーカイブを生成し、 ``$ASAKUSA_HOME`` 配下に Asakusa Frameworkを展開します。

Framework Organizer は以下からダウンロードします。

* http://www.asakusafw.com/download/framework-organizer/asakusafw-organizer-0.5.2.tar.gz

ダウンロードが完了したら、以下の例を参考にしてAsakusa Frameworkをインストールします。
インストールが成功すると、 ``$ASAKUSA_HOME`` 配下に Asakusa Frameworkがインストールされます。

..  code-block:: sh
     
    cd ~/Downloads
    tar xf asakusafw-organizer-*.tar.gz
    cd asakusafw-organizer
    mvn package antrun:run

インストールソフトウェアの動作確認
----------------------------------
これまでの手順でインストールしたソフトウェアの動作確認を行います。

以下の例を参考にして、ターミナルからコマンドを実行し、例の通りの出力が行われることを確認してください。
コマンドが見つからないと表示された場合には、それぞれのインストール手順や `環境変数の設定`_ を見直してください。

Javaの動作確認
~~~~~~~~~~~~~~

..  code-block:: sh

    java -version

    java version "1.6.0_45"
    ...

Java SDKの動作確認
~~~~~~~~~~~~~~~~~~

..  code-block:: sh

    javac -version

    javac 1.6.0_45

Mavenの動作確認
~~~~~~~~~~~~~~~

..  code-block:: sh

    mvn -version

    Apache Maven 3.0.5 (r01de14724cdef164cd33c7c8c2fe155faf9602da; 2013-02-19 22:51:28+0900)
    ...


Hadoopの動作確認
~~~~~~~~~~~~~~~~

..  code-block:: sh

    hadoop version

    Hadoop 1.2.1
    ...

..  attention::
    Hadoopのみバージョンを確認するためのコマンドが ``hadoop version`` となっていて、 ``version`` の前にハイフンが不要です。

Asakusa Frameworkのインストール確認
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

..  code-block:: sh
    
    cat $ASAKUSA_HOME/VERSION
    
    asakusafw.version=0.5.2
    
    asakusafw.build.timestamp=...
    asakusafw.build.java.version=1.6.0_...

Eclipseのインストール
---------------------
アプリケーションの実装・テストに使用する統合開発環境(IDE)として、Eclipseをインストールします。

..  note:: Asakusa Frameworkを使う上でEclipseの使用は必須ではありません。サンプルアプリケーションのソースを確認する場合などでEclipseがあると便利であると思われるため、ここでEclipseのインストールを説明していますが、スタートガイドの手順のみを実行するのであれば、Eclipseのインストールは不要です。

Eclipseのダウンロードサイト (http://www.eclipse.org/downloads/) から Eclipse IDE for Java Developers - Linux 32 Bit ``eclipse-java-XX-linux-gtk.tar.gz`` ( ``XX`` はバージョンを表すコード名 )  をダウンロードします。

ダウンロードが完了したら、以下の例を参考にしてEclipseをインストールします。

..  code-block:: sh

    cd ~/Downloads
    tar xf eclipse-java-*-linux-gtk.tar.gz
    mv eclipse ~/eclipse

Eclipseを起動するには、 ``$HOME/eclipse/eclipse`` を実行します。以下はターミナルから起動する例です。

..  code-block:: sh

    $HOME/eclipse/eclipse &

..  attention::
    GUIのファイラーなどからEclipseを起動する場合は、デスクトップ環境に対して ``~/.profile`` で定義した環境変数を反映させるため、Eclipseを起動する前に一度デスクトップ環境からログアウトし、再ログインする必要があります。

Eclipse起動時にワークスペースを指定するダイアログが表示されるので、デフォルトの ``$HOME/workspace`` をそのまま指定します。

サンプルアプリケーションの導入と実行
====================================
開発環境上で Asakusa Framework のサンプルアプリケーションを実行してみます。

アプリケーション開発プロジェクトの作成
--------------------------------------
まず、Asakusa Frameworkのバッチアプリケーションを開発、及び管理する単位となる「プロジェクト」を作成します。

Asakusa Frameworkでは、プロジェクトのテンプレートを提供しており、このテンプレートにサンプルアプリケーションも含まれています。

プロジェクトのテンプレートはMavenのアーキタイプという仕組みで提供されています。Mavenのアーキタイプからプロジェクトを作成するには、以下のコマンドを実行します（Mavenがライブラリをダウンロードするため、実行に時間がかかります)。

..  code-block:: sh

    mkdir -p ~/workspace
    cd ~/workspace
    mvn archetype:generate -DarchetypeCatalog=http://asakusafw.s3.amazonaws.com/maven/archetype-catalog-0.5.xml

コマンドを実行すると、Asakusa Frameworkが提供するプロジェクトテンプレートのうち、どれを使用するかを選択する画面が表示されます。ここでは、WindGateと連携するアプリケーション用のテンプレートである 1 ( ``asakusa-archetype-windgate`` ) を選択します。

..  code-block:: sh

    1: http://asakusafw.s3.amazonaws.com/maven/archetype-catalog-0.5.xml -> com.asakusafw:asakusa-archetype-windgate (-)
    2: http://asakusafw.s3.amazonaws.com/maven/archetype-catalog-0.5.xml -> com.asakusafw:asakusa-archetype-thundergate (-)
    3: http://asakusafw.s3.amazonaws.com/maven/archetype-catalog-0.5.xml -> com.asakusafw:asakusa-archetype-directio (-)
    Choose a number or apply filter (format: [groupId:]artifactId, case sensitive contains): : 1 (<-1を入力)

次に、Asakusa Frameworkのバージョンを選択します。ここでは 3 (バージョン 0.5.2) を選択します。

..  code-block:: sh

    Choose com.asakusafw:asakusa-archetype-windgate version: 
    1: 0.5-SNAPSHOT
    2: 0.5.0
    3: 0.5.1
    4: 0.5.2
    Choose a number: 4: 4 (<-4を入力)

..  attention::
    ``-SNAPSHOT`` という名称が付いているバージョンは開発中のバージョンを表します。このバージョンはリリースバージョンと比べて不安定である可能性が高いため、使用する場合は注意が必要です。またこのバージョンはAsakusa FrameworkのMavenリポジトリが更新された場合、開発環境から自動的にライブラリの更新が行われる可能性があり、これが原因で予期しない問題が発生する可能性があります。


この後、アプリケーションプロジェクトに関するいくつかの定義を入力します。いずれも任意の値を入力することが出来ます。

ここでは、グループIDに ``com.example`` 、アーティファクトID（アプリケーションプロジェクト名）に ``example-app`` を指定します。後の項目はそのままEnterキーを入力します。

最後に確認をうながされるので、そのままEnterキーを入力します。

..  code-block:: sh

    Define value for property 'groupId': : com.example    [<-アプリケーションのグループ名を入力。]
    Define value for property 'artifactId': : example-app [<-アプリケーションのプロジェクト名を入力。]
    Define value for property 'version':  1.0-SNAPSHOT: : [<-ここではそのままEnterキーを入力 (バージョン名)。]
    Define value for property 'package':  com.example: :  [<-ここではそのままEnterキーを入力 (パッケージ名)。]

    Confirm properties configuration:
    groupId: com.example
    artifactId: example-app
    version: 1.0-SNAPSHOT
    package: com.example
    Y: : [<-そのままEnterキーを入力]

入力が終わるとプロジェクトの作成が始まります。成功した場合、画面に以下のように ``BUILD SUCCESS`` と表示されます。

..  code-block:: sh

    ...
    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD SUCCESS
    [INFO] ------------------------------------------------------------------------
    [INFO] Total time: 20.245s
    ...

これでアプリケーションプロジェクトが作成されました。

..  note::
    以降の手順についても、Mavenのコマンド実行後に処理が成功したかを確認するには ``BUILD SUCCESS`` が表示されていることを確認してください。


サンプルアプリケーションのビルド
--------------------------------
アプリケーションのテンプレートには、あらかじめサンプルアプリケーション（カテゴリー別売上金額集計バッチ) のソースファイルが含まれています。このサンプルアプリケーションのソースファイルをAsakusa Framework上で実行可能な形式にビルドします。

アプリケーションのビルドを実行するには、Mavenの以下のコマンドを実行します（初回の実行時のみ、Mavenがライブラリをダウンロードするため、実行に時間がかかります）。

..  code-block:: sh

    cd ~/workspace/example-app
    mvn clean package

このコマンドの実行によって、アプリケーションのプロジェクトに対して以下の処理が実行されます。

1. データモデル定義DSL(DMDL)から、データモデルクラスを生成
2. Asakusa DSLとデータモデル定義DSLから、実行可能なプログラム群（HadoopのMapReduceジョブやWindGate用の実行定義ファイルなど)を生成
3. 実行可能なプログラム群に対するテストを実行
4. アプリケーションを実行環境に配置するためのデプロイメントアーカイブファイルを生成

ビルドが成功すると、プロジェクトの ``target`` ディレクトリ配下にいくつかのファイルが作成されますが、この中の ``example-app-batchapps-1.0-SNAPSHOT.jar`` というファイルがサンプルアプリケーションが含まれるデプロイメントアーカイブファイルです。

..  note::
    このアーカイブファイルの名前は、実際には ``${artifactId}-batchapp-${version}.jar`` という命名ルールに従って作成されます。プロジェクト作成時に本ドキュメントの例以外のプロジェクト名やバージョンを指定した場合は、それに合わせて読み替えてください。
    
..  warning::
    targetディレクトリの配下に似た名前のファイルとして ``${artifactId}-${version}.jar`` というファイル( ファイル名に ``batchapp`` が付いていないjarファイル)が同時に作成されますが、これはデプロイメントアーカイブファイルではないので注意してください。

.. _introduction-start-guide-deploy-app:

サンプルアプリケーションのデプロイ
----------------------------------
サンプルアプリケーションを実行するために、先ほどビルドしたサンプルアプリケーションを実行環境にデプロイします。

実行環境は、通常はHadoopクラスターが構築されている運用環境となりますが、ここでは開発環境（ローカル）上のHadoopとAsakusa Framework上でサンプルアプリケーションを実行するため、ローカルに対するデプロイを行います。

アプリケーションのデプロイは、Asakusa Frameworkがインストールされているマシン上の ``$ASAKUSA_HOME/batchapps`` ディレクトリに アプリケーションが含まれるjarファイルの中身を展開して配置します。以下はアプリケーションプロジェクトで生成したアーカイブファイルをローカルのAsakusa Frameworkにデプロイする例です。

..  code-block:: sh

    cd ~/workspace/example-app
    cp target/*batchapps*.jar $ASAKUSA_HOME/batchapps
    cd $ASAKUSA_HOME/batchapps
    jar xf *batchapps*.jar


サンプルデータの作成と配置
--------------------------
カテゴリー別売上金額集計バッチは、売上トランザクションデータと、商品マスタ、店舗マスタを入力として、エラーチェックを行った後、商品マスタのカテゴリ毎に集計するアプリケーションです。入力データの取得と出力データの生成はそれぞれCSVファイルに対して行うようになっています。

このバッチは入力データを ``/tmp/windgate-$USER`` ( ``$USER`` はOSユーザ名に置き換え ) ディレクトリから取得するようになっています。プロジェクトにはあらかじめ ``src/test/example-dataset`` ディレクトリ以下にテストデータが用意されているので、これらのファイルを  ``/tmp/windgate-$USER`` 配下にコピーします。

..  code-block:: sh

    mkdir -p /tmp/windgate-$USER
    rm /tmp/windgate-$USER/* -rf
    cd ~/workspace/example-app
    cp -a src/test/example-dataset/* /tmp/windgate-$USER/

.. _introduction-start-guide-run-app:

サンプルアプリケーションの実行
------------------------------
ローカルにデプロイしたサンプルアプリケーションを実行します。

Asakusa Frameworkでは、バッチアプリケーションを実行するためのコマンドプログラムとして「YAESS」というツールが提供されています。
バッチアプリケーションを実行するには、 ``$ASAKUSA_HOME/yaess/bin/yaess-batch.sh`` に実行するバッチのバッチIDを指定します。

サンプルアプリケーション「カテゴリー別売上金額集計バッチ」のバッチは「 ``example.summarizeSales`` 」というIDを持っています。
また、このバッチは引数に処理対象の売上日時( ``date`` )を指定し、この値に基づいて処理対象CSVファイルを特定します [#]_ 。

バッチIDとバッチ引数を指定して、以下のようにバッチアプリケーションを実行します。

..  code-block:: sh

    $ASAKUSA_HOME/yaess/bin/yaess-batch.sh example.summarizeSales -A date=2011-04-01

バッチの実行が成功すると、コマンドの標準出力の最終行に ``Finished: SUCCESS`` と出力されます。

..  code-block:: sh

    ...
    2013/04/22 13:50:35 INFO  [YS-CORE-I01999] Finishing batch "example.summarizeSales": batchId=example.summarizeSales, elapsed=12,712ms
    2013/04/22 13:50:35 INFO  [YS-BOOTSTRAP-I00999] Exiting YAESS: code=0, elapsed=12,798ms
    Finished: SUCCESS


..  [#] より詳しく言えば、このバッチでは ``/tmp/windgate-$USER/sales/<売上日時>.csv`` という名前のCSVファイルを読み出し、
    ``/tmp/windgate-$USER/result/category-<売上日時>.csv`` という名前のCSVファイルを作成します。
    なお、サンプルのデータセットには ``sales/2011-04-01.csv`` が含まれています。

サンプルアプリケーション実行結果の確認
--------------------------------------
カテゴリー別売上金額集計バッチはバッチの実行結果として、ディレクトリ ``/tmp/windgate-$USER/result`` にカテゴリー別売上金額の集計データとエラーチェックに該当したエラーレコードがCSVファイルとして出力されます。

下記は結果の例です (結果の順序は実行のたびに変わるかもしれません)。

..  code-block:: sh

    cat /tmp/windgate-$USER/result/category-2011-04-01.csv
    カテゴリコード,販売数量,売上合計
    1300,12,1596
    1401,15,1470
    1600,28,5400

    cat /tmp/windgate-$USER/result/error-2011-04-01.csv
    ファイル名,行番号,日時,店舗コード,商品コード,メッセージ
    /tmp/windgate-asakusa/sales/2011-04-01.csv,33,2011-04-01 19:00:00,9999,4922010001000,店舗不明
    /tmp/windgate-asakusa/sales/2011-04-01.csv,35,1990-01-01 10:40:00,0001,4922010001000,商品不明
    /tmp/windgate-asakusa/sales/2011-04-01.csv,34,2011-04-01 10:00:00,0001,9999999999999,商品不明

Eclipseへアプリケーションプロジェクトをインポート
-------------------------------------------------
アプリケーションプロジェクトをEclipseへインポートして、Eclipse上でアプリケーションの開発を行えるようにします。

インポートするプロジェクトのディレクトリに移動し、Mavenの以下のコマンドを実行してEclipse用の定義ファイルを作成します。

..  code-block:: sh

    cd ~/workspace/example-app
    mvn eclipse:eclipse

これでEclipseからプロジェクトをインポート出来る状態になりました。Eclipseのメニューから ``[File]`` -> ``[Import]`` -> ``[General]`` -> ``[Existing Projects into Workspace]`` を選択し、プロジェクトディレクトリを指定してEclipseにインポートします。

Next Step:アプリケーションの開発を行う
======================================
これまでの手順で、Asakusa Framework上でバッチアプリケーションの開発を行う準備が整いました。

次に、アプリケーションの開発を行うために、Asakusa Frameworkを使ったアプリケーション開発の流れを見てみましょう。 >> :doc:`next-step`

