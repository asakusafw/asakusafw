================================
Asakusa Framework スタートガイド
================================
この文書では、Asakusa Frameworkをはじめて利用するユーザ向けに、Asakusa Frameworkの開発環境を作成し、サンプルアプリケーションを動かすまでの手順を説明します。

開発環境の構築
==============
Asakusa FrameworkはLinux OS上に開発環境を構築して利用します。WindowsPC上でAsakusa Frameworkを使った開発を行う場合、Windows上でLinuxの仮想マシンを実行し、ここで開発を行うと便利です。

このスタートガイドでは仮想マシンの実行ソフトウェアとして VMWare Player 、仮想マシンに使用するOSとして Ubuntu 11.10 Desktop (日本語 Remix CD x86用) を使用し、この環境に必要なソフトウェアをセットアップする手順を説明します。

..  note::
    2011年12月時点では、公式にWindows対応されているHadoopディストリビューションは存在しないため、Asakusa FrameworkもWindows上での開発をサポートしていません。将来HadoopがWindowsをサポートした場合、Asakusa FrameworkもWindows上での開発に対応する可能性があります。

VMWare Playerのインストール
---------------------------
VMWare Playerをダウンロードし、インストールを行います。

VMWare Playerのダウンロードサイト [#]_ からVMWare Player (Windows用) をダウンロードします。

ダウンロードしたインストーラを実行し、インストール画面の指示に従ってVMWare Playerをインストールします。

..  [#] http://www.vmware.com/go/get-player-jp

Ubuntu Desktop のインストール
-----------------------------
Ubuntu Desktopをダウンロードし、インストールを行います。

Ubuntu Desktop 日本語 Remix CDのダウンロードサイト [#]_ からisoファイル(CDイメージ)をダウンロードします。

..  [#] http://www.ubuntulinux.jp/products/JA-Localized/download 

ダウンロードが完了したらVMWare Playerを起動し、以下の手順に従ってUbuntu Desktopをインストールします。

1. メニューから「新規仮想マシンの作成」を選択します、
2. インストール元の選択画面では「後でOSをインストール」を選択し [#]_ 、次へ進みます。
3. ゲストOSの選択で「Linux」を選択し、バージョンに「Ubuntu」を選択して次へ進みます。
4. 仮想マシン名の入力では、任意の仮想マシン名と保存場所を指定して、次へ進みます。
5. ディスク容量の指定は任意です。デフォルトの「20GB」はAsakusa Frameworkの開発を試すには十分な容量です。お使いの環境に合わせて設定し、次へ進みます。
6. 仮想マシン作成準備画面で「ハードウェアをカスタマイズ」を選択します。デバイス一覧から「新規 CD/DVD(IDE)」を選択後、画面右側の「ISOイメージファイルを使用する」を選択し、参照ボタンを押下してダウンロードしたUbuntu Desktopのisoファイルを選択します。その他の設定は環境に合わせて設定してください。設定が完了したら画面下の閉じるボタンを押します。
7. 完了ボタンを押して仮想マシンを作成後、仮想マシンを起動すると、Ubuntu Desktopのインストールが開始します。インストール画面の指示に従ってUbuntu Desktopをインストールします。

..  [#] ここで「インストーラ ディスク イメージ ファイル」を選択し、isoファイルを選択するとOSの「簡易インストール」が行われますが、簡易インストールでは日本語環境がインストールされないほか、いくつかの設定が適切に行われないため、簡易インストールの使用は推奨しません。

Ubuntu Desktopが起動したら、同梱のブラウザなどを使用してUbuntuからインターネットにできることを確認してください。以後の手順ではインターネットに接続できることを前提とします。

また、以降の手順で使用するホームフォルダ直下のダウンロードディレクトリを日本語名から英語に変更するため、ターミナルを開いて以下のコマンドを実行します。

..  code-block:: sh

    LANG=C xdg-user-dirs-gtk-update

ダイアログが開いたら「次回からチェックしない」にチェックを入れ、「Update Names」を選択します。

そのほか、必須の手順ではないですがここでVMWare Toolsをインストールしておくとよいでしょう。

Java(JDK)のインストール
-----------------------
Hadoop、及びAsakusa Frameworkの実行に使用するJavaをインストールします。

Javaのダウンロードサイト [#]_ から、Java SE 6 の JDK をダウンロードします [#]_ 。

ダウンロードが完了したら、以下の例を参考にしてJavaをインストールします。

..  code-block:: sh

    cd ~/Downloads
    chmod +x jdk*
    ./jdk*
    
    sudo mkdir /usr/lib/jvm
    sudo mv jdk1.6.0_* /usr/lib/jvm

    cd /usr/lib/jvm
    sudo ln -s jdk1.6.0_* jdk-6

..  [#] http://www.oracle.com/technetwork/java/javase/downloads/index.html
..  [#] ダウンロードするファイルは「jdk-6uXX-linux-i586.bin」(XXはUpdate番号) です。本スタートガイドの環境に従う場合は、x64版(xx-ia64.bin)や、RPM版のファイル(xx-rpm.bin)をダウンロードしないよう注意してください。

このほかに環境変数の設定が必要ですが、本手順では後ほどまとめて設定するため、このまま次に進みます。

Mavenのインストール
-------------------
Asakusa Frameworkの開発環境に必要なビルドツールであるMavenをインストールします。

Mavenのダウンロードサイト [#]_ から Maven3 のtarball [#]_ をダウンロードします。

ダウンロードが完了したら、以下の例を参考にしてMavenをインストールします。

..  code-block:: sh

    cd ~/Downloads
    tar xf apache-maven-*-bin.tar.gz
    sudo mv apache-maven-* /usr/local/lib
    ln -s /usr/local/lib/apache-maven-*/bin/mvn /usr/local/bin/mvn

..  [#] http://maven.apache.org/download.html
..  [#] apache-maven-3.X.X-bin.tar.gz

..  todo:: プロキシ環境の設定について

Hadoopのインストール
--------------------
Clouderaから提供されているHadoopのディストリビューションである Cloudera Hadoop Distribution of Hadoop version 3(CDH3)をインストールします。

CDH3のインストール方法はOS毎に提供されているインストールパッケージを使う方法と、tarballを展開する方法がありますが、ここではtarballを展開する方法でインストールします。

CDH3のダウンロードサイト [#]_ から CDH3 のtarball [#]_ をダウンロードします。コンポーネントはHadoopのみをダウンロードします。

..  [#] https://ccp.cloudera.com/display/SUPPORT/CDH3+Downloadable+Tarballs
..  [#] hadoop-0.20.2-cdh3uX.tar.gz

ダウンロードが完了したら、以下の例を参考にしてCDH3をインストールします。

..  code-block:: sh

    cd ~/Downloads
    tar xf hadoop-0.20.2-*.tar.gz
    mv hadoop-0.20.2-*.tar.gz /tmp
    sudo mv hadoop-0.20.2-* /usr/lib
    sudo ln -s /usr/lib/hadoop-0.20.2-* /usr/lib/hadoop

環境変数の設定
--------------
Asakusa Frameworkの利用に必要となる環境変数を設定します。

$HOME/.profile の最下行に以下の定義を追加します。

..  code-block:: sh

    export JAVA_HOME=/usr/lib/jvm/jdk-6
    export HADOOP_HOME=/usr/lib/hadoop
    export ASAKUSA_HOME=$HOME/asakusa
    export PATH=$JAVA_HOME/bin:$HADOOP_HOME/bin:$PATH

環境変数をデスクトップ環境に反映させるため、一度デスクトップ環境からログアウトし、再ログインします。

Eclipseのインストール
---------------------
アプリケーションの実装・テストに使用する統合開発環境(IDE)として、Eclipseをインストールします。

Eclipseのダウンロードサイト [#]_ から Eclipse IDE for Java Developers (Linux 32 Bit) [#]_ をダウンロードします。

ダウンロードが完了したら、以下の例を参考にしてEclipseをインストールします。

..  code-block:: sh

    cd ~/Downloads
    tar xf eclipse-java-*-linux-gtk.tar.gz
    sudo mv eclipse /usr/local/lib

次に、Eclipseのワークスペースに対してクラスパス変数M2_REPOを設定します。ここでは、ワークスペースディレクトリに$HOME/workspace を指定します。

..  code-block:: sh

    mvn -Declipse.workspace=$HOME/workspace eclipse:add-maven-repo

Eclipseを起動するには、ファイラーから /usr/local/lib/eclipse/eclipse を実行します。ワークスペースはデフォルトの$HOME/workspace をそのまま指定します。

..  [#] http://www.eclipse.org/downloads/
..  [#] eclipse-java-XX-linux-gtk.tar.gz

Asakusa Frameworkのインストールとサンプルアプリケーションの実行
===============================================================



