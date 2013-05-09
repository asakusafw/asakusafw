=============================
SDKアーティファクト利用ガイド
=============================
この文書では、アプリケーション開発プロジェクト用の依存性定義を行うためのSDKアーティファクトについて説明します。

SDKアーティファクト概要
=======================
Asakusa Frameworkではアプリケーション開発プロジェクトで使用する
Mavenプロジェクト構成 ( ``pom.xml`` で表現されます ) で使用する
依存性定義をグループ化した「SDKアーティファクト」を提供しています。

SDkアーティファクトを利用することで、
アプリケーション開発プロジェクトの構成に対して、
用途に応じてAsakusa Frameworkが提供するライブラリの
依存性定義をシンプルに過不足なく行うことを支援します。
またAsakusa Frameworkのバージョンアップなどに伴う
マイグレーション作業を容易にします。

アプリケーション開発プロジェクトの ``pom.xml`` では
SDKアーティファクトを依存性定義に使用することを推奨します。

:doc:`maven-archetype` の手順に従って作成したアプリケーション開発プロジェクトでは、
用途に応じた適切なSDKアーティファクトを使用した ``pom.xml`` が標準で作成されます。

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
SDKアーティファクトを使用する場合は、通常のMavenを使ったアプリケーションと同様、
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

