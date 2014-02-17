=============================
SDKアーティファクト利用ガイド
=============================
この文書では、アプリケーション開発プロジェクト用の依存性定義を行うためのSDKアーティファクトについて説明します。

SDKアーティファクト概要
=======================
Asakusa Frameworkではアプリケーションプロジェクトで使用する
Asakusa Frameworkのライブラリをグループ化した
「SDKアーティファクト」を提供しています。

SDkアーティファクトを利用することで、
アプリケーションプロジェクトに対して
ライブラリの依存性定義をシンプルに過不足なく行うことを支援します。
またAsakusa Frameworkのバージョンアップなどに伴う
マイグレーション作業を容易にします。

アプリケーションプロジェクトのビルド定義
( Gradleプロジェクトの場合は ``build.gradle`` , Mavenプロジェクトの場合は ``pom.xml`` )では
SDKアーティファクトを依存性定義に使用することを推奨します。

SDKアーティファクト一覧
=======================
SDKアーティファクトはAsakusa FrameworkのMavenリポジトリに
グループID ``com.asakusafw.sdk`` を持つMavenアーティファクトとして
登録されています。

Asakusa Frameworkが提供するSDKアーティファクトは以下のものがあります。

..  list-table:: Asakusa Framework SDKアーティファクト一覧
    :widths: 30 20 50
    :header-rows: 1
    
    * - アーティファクトID
      - 導入バージョン
      - 説明
    * - ``asakusa-sdk-core``
      - 0.5.0
      - すべてのアプリケーション開発プロジェクトで共通的に必要となるライブラリをまとめたアーティファクト。
    * - ``asakusa-sdk-windgate``
      - 0.5.0
      - Windgateを使用するアプリケーション開発プロジェクトで必要となるライブラリをまとめたアーティファクト。
    * - ``asakusa-sdk-thundergate``
      - 0.5.0
      - Thundergateを使用するアプリケーション開発プロジェクトで必要となるライブラリをまとめたアーティファクト。
    * - ``asakusa-sdk-directio``
      - 0.5.0
      - Direct I/Oを使用するアプリケーション開発プロジェクトで必要となるライブラリをまとめたアーティファクト。

SDKアーティファクトの利用方法
=============================

Gradleプロジェクト
------------------
:doc:`gradle-plugin` などの手順で構築した
GradleプロジェクトでSDKアーティファクトを使用する場合は
build.gradleの ``dependencies`` ブロック内に ``compile`` 依存関係(コンフィグレーション)に対して
依存定義を追加します。

例えば、Direct I/O と WindGate を使った
アプリケーション開発プロジェクト向けに
依存関係を設定する場合は、以下のようになります。

..  code-block:: groovy

    dependencies {
        compile group: 'com.asakusafw.sdk', name: 'asakusa-sdk-core', version: asakusafw.asakusafwVersion
        compile group: 'com.asakusafw.sdk', name: 'asakusa-sdk-directio', version: asakusafw.asakusafwVersion
        compile group: 'com.asakusafw.sdk', name: 'asakusa-sdk-windgate', version: asakusafw.asakusafwVersion

Mavenプロジェクト
-----------------
:doc:`maven-archetype` などの手順で構築した
MavenプロジェクトでSDKアーティファクトを使用する場合は
pom.xmlの ``<dependencies>`` 内に依存定義( ``<dependency>`` )を追加します。

例えば、Direct I/O と WindGate を使った
アプリケーション開発プロジェクト向けに
依存性定義を行う場合は、以下のようになります。

..  code-block:: xml
        
		<dependency>
			<groupId>com.asakusafw.sdk</groupId>
			<artifactId>asakusa-sdk-core</artifactId>
			<version>${asakusafw.version}</version>
		</dependency>
		<dependency>
			<groupId>com.asakusafw.sdk</groupId>
			<artifactId>asakusa-sdk-directio</artifactId>
			<version>${asakusafw.version}</version>
		</dependency>
		<dependency>
			<groupId>com.asakusafw.sdk</groupId>
			<artifactId>asakusa-sdk-windgate</artifactId>
			<version>${asakusafw.version}</version>
		</dependency>

