# Tech Stack Decisions - core (Unit 1: Core Template Engine)

requirements.md（NFR-2, NFR-3, NFR-6）および本ステージの回答結果に基づく技術選定を確定する。

## 言語・ビルド環境（requirements.md NFR-2で確定済み、再掲）
- **言語**: Java 25
- **ビルドツール**: Gradle（Gradle Wrapper使用）
- **文字エンコーディング**: UTF-8固定
- **配布**: Maven Central等への公開は当面行わない。ソースからのビルド利用のみ

## テストフレームワーク
- **単体テスト**: JUnit 5（`org.junit.jupiter:junit-jupiter`）
- **Property-Based Testing**: jqwik（`net.jqwik:jqwik`）最新安定版。JUnit 5 Platform統合を使用する（requirements.md NFR-6、Q4=A）
  - shrinking・seedベースの再現性はjqwikの標準機能をそのまま使用し、無効化しない（PBT-08準拠は Code Generation/Build and Testステージで最終確認）

## セキュリティ関連ツール
- **依存関係脆弱性スキャン**: OWASP Dependency-Check Gradleプラグイン（`org.owasp.dependencycheck`）を導入する（Q2=A）
  - 実行タイミング・失敗時のビルド停止条件（しきい値）はBuild and Testステージで確定する

## 公式Mustache specテストスイートの取り込み
- BR-2のスコープ（コア6ファイル + `~lambdas.yml`）に対応するYAMLファイルをテストリソースとして`core/src/test/resources`配下に配置する
- YAMLパース用ライブラリの選定（例: SnakeYAML）はCode Generationステージで確定する（coreの本体実装にはYAML依存を持ち込まず、テストスコープの依存として分離する）

## ドキュメント
- 公開APIクラス・メソッドにJavadocを必須とする（Q6=A、NFR-MNT-1）

## 未確定事項（Code Generationステージで確定）
- jqwik・JUnit 5・OWASP Dependency-Check・SnakeYAML（テストスコープ）の具体的なバージョン番号
- OWASP Dependency-Checkのビルド組み込み方法・失敗しきい値
