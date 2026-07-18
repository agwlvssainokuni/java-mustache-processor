# Security Test Instructions

`requirements.md` NFR-4（Security Baseline拡張: 有効）に基づき、以下のセキュリティテストを実施する。

## 1. 依存関係の脆弱性スキャン（SECURITY-10、OWASP Dependency-Check）

### 実行コマンド
```bash
./gradlew :cherry-mustache-core:dependencyCheckAnalyze
./gradlew :cherry-mustache-cli:dependencyCheckAnalyze
```
（または `./gradlew dependencyCheckAnalyze` で両モジュールを一括実行）

### 期待される結果
- CVSSスコア7.0以上の既知脆弱性が検出された場合、`failBuildOnCVSS = 7.0f`設定によりビルドが失敗する（`cherry-mustache-core/build.gradle.kts`, `cherry-mustache-cli/build.gradle.kts`で設定済み）
- 誤検知（false positive）がある場合は`cherry-mustache-core/dependency-check-suppressions.xml` / `cherry-mustache-cli/dependency-check-suppressions.xml`に抑制ルールを追記する
- レポート出力先: `cherry-mustache-core/build/reports/dependency-check-report.html`, `cherry-mustache-cli/build/reports/dependency-check-report.html`

### 本ステージでの実施状況: **未完了（環境制約により手動実行が必要）**
本ステージで`./gradlew :cherry-mustache-core:dependencyCheckAnalyze`の実行を試みたが、NVD（National Vulnerability Database）の脆弱性データベース初回同期にNVD APIキー無しでは非常に長い時間を要する旨の警告（`An NVD API Key was not provided - it is highly recommended to use an NVD API key as the update can take a VERY long time without an API Key`）が出力され、セッションの実行時間制約内での完了が見込めなかったため中断した。

**ユーザーによる手動実行が必要**:
1. [NVD API Key](https://nvd.nist.gov/developers/request-an-api-key)を取得する（無料、即時発行）
2. ローカル実行時は`~/.gradle/gradle.properties`等に以下を設定する:
   ```properties
   nvdApiKey=<取得したAPIキー>
   ```
   （両モジュールの`build.gradle.kts`は`dependencyCheck.nvd.apiKey`として、Gradleプロパティ`nvdApiKey`または環境変数`NVD_API_KEY`のいずれかから読み込む設定を組み込み済み）
3. `./gradlew dependencyCheckAnalyze`を実行する（APIキーがあれば数分程度で完了する）
4. レポートを確認し、CVSS 7.0以上の指摘があれば依存バージョンの更新または抑制ルールの追加を検討する

### CI（GitHub Actions）での実行
`.github/workflows/dependency-check.yml`は手動実行（`workflow_dispatch`）に対応済み。定期実行（毎週月曜03:00 UTCのcronスケジュール）はワークフロー内にコメントアウトした状態で用意しており、必要になった時点でコメントを解除すれば有効化できる。リポジトリのSecretsに`NVD_API_KEY`を登録することで、CI上でも同様にスキャンが実行される（Secrets未登録の場合はAPIキー無しの低速モードで実行される）。ビルド・テストを行う`build.yml`とは別ワークフローに分離した理由: 依存関係スキャンはNVD同期のため実行時間が長くなりうる／頻度もビルド・テストほど高くする必要がないため。

## 2. 入力検証（SECURITY-05）

### core
`FilePartialResolver`のパストラバーサル対策（`nfr-design-patterns.md`「Security Patterns」）は、公式Mustache specテストスイート実行時には直接カバーされないため、`FilePartialResolverTest`（`cherry-mustache-core/src/test/java/cherry/mustache/FilePartialResolverTest.java`）で個別に検証済み（`unit-test-instructions.md`参照）。

### cli
`DataLoader`のJSON/YAML構文検証、`ArgumentParser`の引数構文検証は`unit-test-instructions.md`のテストで検証済み。追加のペネトレーションテストは、本プロジェクトが外部公開されるネットワークサービスではない（`requirements.md` NFR-2「当面は非公開」）ため対象外と判断する。

## 3. デシリアライズ安全性（SECURITY-13）
`cli`の`DataLoader`はJacksonの`ObjectMapper`を常に`Map<String, Object>`型に固定して使用し、多相型ハンドリングを有効化しない設計（`nfr-requirements.md` NFR-SEC-2）。この設計自体がJacksonの既知のデシリアライズ脆弱性パターン（ポリモーフィック型による任意クラスインスタンス化）を回避する。追加のペネトレーションテストは行わず、設計レビュー（`nfr-requirements.md`）による対応とする。

## 実施結果サマリー
| 項目 | 状態 |
|---|---|
| OWASP Dependency-Check（core） | 未完了（NVD同期に長時間要するため、ユーザーによる手動実行が必要。上記手順参照） |
| OWASP Dependency-Check（cli） | 未完了（同上） |
| 入力検証（SECURITY-05） | 単体テストで検証済み |
| デシリアライズ安全性（SECURITY-13） | 設計レビューで対応済み（`cli/nfr-requirements/nfr-requirements.md`） |
