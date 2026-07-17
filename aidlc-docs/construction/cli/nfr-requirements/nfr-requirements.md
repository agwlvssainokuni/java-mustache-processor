# NFR Requirements - cli (Unit 2: CLI Tool)

`cli-nfr-requirements-plan.md` の回答結果（Q1〜Q5すべてA）に基づく確定事項を示す。

## Scalability（拡張性）
**N/A**。requirements.md NFR-1で確定済み。CLIは単発実行のプロセスであり、水平スケーリング・負荷分散の概念を持たない。

## Performance（性能）
**N/A**。requirements.md NFR-1で確定済み: 特別な性能要件は設けない。

## Availability（可用性）
**N/A**。requirements.md NFR-5でResiliency Baseline拡張が無効と確定済み。CLIは単発実行のプロセスであり可用性の概念が無い。

## Security（セキュリティ、Security Baseline拡張: 有効）

### NFR-SEC-1: cliモジュールの依存関係脆弱性スキャン
`cli/build.gradle.kts`にも`core`と同様にOWASP Dependency-Check Gradleプラグインを導入し、Jackson等`cli`固有の依存関係を含めてスキャン対象とする。サブプロジェクトごとに設定する既存の構成方針（`core`で確立済み）と一貫させる。（SECURITY-10対応）

### NFR-SEC-2: JSON/YAMLデシリアライズの安全性
`DataLoader`が使用する`ObjectMapper`は既定設定のまま使用し、多相型ハンドリング（`enableDefaultTyping`等）は一切有効化しない。読込先は常に`Map<String, Object>`型に固定し、任意のJavaクラスへのバインディングは行わない。追加のハードニング設定（最大ネスト深さ制限等）は導入しない（既定で安全側と判断）。（SECURITY-13対応）

### NFR-SEC-3: コマンドライン引数のファイルパス検証タイミング
`ArgumentParser`では値の構文検証（空文字列でない等）のみを行い、テンプレート/データ/出力/パーシャルディレクトリの各パスに対する実際のファイルアクセス可否は、各処理の実行時（ファイルオープン時）に発生する`IOException`として自然に検出し`ExitCode.IO_ERROR`にマッピングする。事前の存在確認は二重コスト・TOCTOU（Time-of-check to time-of-use）問題を避けるため行わない。coreの`FilePartialResolver`と同じ「実行時に検出」の設計方針に統一する。（SECURITY-05対応）

### 適用対象外と判断したSECURITY項目
Security Compliance節（本ファイル末尾）を参照。

## Reliability（信頼性）

### NFR-REL-1: CLI固有ロジックのテスト方針
`CliRunner.run(args, in, out, err)`のシグネチャ（標準入出力を引数として受け取る設計）を活かし、JUnit 5単体テストで標準入出力を`ByteArrayInputStream`/`ByteArrayOutputStream`に差し替え、代表的な引数パターン・データ形式（JSON/YAML）・エラーケース（引数エラー、データ構文エラー、テンプレート構文エラー、レンダリングエラー、I/Oエラー）ごとに`ExitCode`と出力内容（stdout/stderr）を検証する。実際に`java -jar`でfat jarをプロセス起動するE2Eテストは行わない（規模に対して過剰と判断）。

## Maintainability（保守性）
**N/A**。`cli`は他モジュールから利用されるライブラリとしての公開APIを持たない（`Main`が唯一のエントリポイント）。内部クラス（`CliRunner`, `ArgumentParser`等）へのJavadocは任意とし、必須要件にはしない。

## Usability（使いやすさ）
**質問なし**（既決定）。引数の長短形式は`functional-design/business-rules.md` BR-2/BR-7で`--help`/`-h`以外は長形式オプションのみとする方針が確定済み。

## Testability（テスト、NFR-3関連）
- JUnit 5による単体テストを実施する（requirements.md NFR-3）
- テスト対象: `ArgumentParser`（引数解析・検証）、`DataLoader`（JSON/YAML読込・形式判定）、`CliRunner`（オーケストレーション全体、終了コードマッピング）、`OutputWriter`
- 公式Mustache specテストスイートおよびProperty-Based Testingは`core`の責務であり（`unit-of-work-story-map.md`参照）、`cli`では対象外

---

## Security Compliance（Security Baseline拡張）

| Rule | 判定 | 根拠 |
|---|---|---|
| SECURITY-01（保存時/転送時暗号化） | N/A | cliはデータストア・ネットワーク通信を持たない |
| SECURITY-02（アクセスログ） | N/A | cliはネットワーク仲介コンポーネントを持たない |
| SECURITY-03（アプリケーションログ） | Compliant | requirements.md NFR-4で見込み済み。`System.out`/`System.err`は使用せず、SLF4J（`slf4j-api`）をログAPIとして採用する（coreと同じ方針。ユーザー向けCLI出力は`business-rules.md` BR-9の通り注入された`PrintStream`経由で行い、SLF4Jログとは区別する） |
| SECURITY-04（HTTPセキュリティヘッダー） | N/A | HTMLをサーブするエンドポイントを持たない |
| SECURITY-05（入力検証） | Compliant | NFR-SEC-3でファイルパス検証タイミングを規定。データ形式判定（BR-3）・引数構文検証（BR-2）は構文的に不正な入力を`ArgumentException`として安全に拒否する |
| SECURITY-06（最小権限アクセスポリシー） | N/A | IAM/権限ポリシーを持たない |
| SECURITY-07（ネットワーク構成） | N/A | ネットワーク構成を持たない |
| SECURITY-08（アプリケーションレベルアクセス制御） | N/A | 認証・認可の対象となるエンドポイントを持たない |
| SECURITY-09（堅牢化・エラーハンドリング） | Compliant | `business-rules.md` BR-9で内部詳細を含まないエラーメッセージ方針を規定済み（本ステージで変更なし） |
| SECURITY-10（ソフトウェアサプライチェーン） | Compliant | NFR-SEC-1でOWASP Dependency-Check導入を規定（Jackson・Shadow plugin依存を含む） |
| SECURITY-11（セキュアな設計原則） | N/A | 認証・決済等のセキュリティクリティカルな機能を持たない |
| SECURITY-12（認証・認証情報管理） | N/A | 認証機能を持たない |
| SECURITY-13（ソフトウェア・データ整合性） | Compliant | NFR-SEC-2でJacksonによるJSON/YAMLデシリアライズの安全性方針（多相型ハンドリング不使用、`Map<String,Object>`固定）を規定。coreのNFR RequirementsでUnit 2の責務と明記済み |
| SECURITY-14（アラート・監視） | N/A | 監視対象となる稼働中サービスを持たない |
| SECURITY-15（例外処理・フェイルセーフ） | Compliant | `business-logic-model.md` 4節で例外種別→ExitCodeの対応表を規定済み。分類不能な例外も`RENDER_ERROR`にフォールバックし、CLIプロセスが未処理例外でクラッシュしない設計とする |

## PBT Compliance（Property-Based Testing拡張）

| Rule | 判定 | 根拠 |
|---|---|---|
| 全PBTルール（01〜10） | N/A | Property-Based Testingは`unit-of-work-story-map.md`でUnit 1（core）の責務と明記済み。`cli`固有ロジック（引数解析・データ読込・終了コード等）は例示ベースのJUnit 5単体テストで検証する（NFR-REL-1参照） |
