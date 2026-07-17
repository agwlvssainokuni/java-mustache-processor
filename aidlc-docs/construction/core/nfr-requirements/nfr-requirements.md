# NFR Requirements - core (Unit 1: Core Template Engine)

`core-nfr-requirements-plan.md` の回答結果（Q1〜Q6すべてA）に基づく確定事項を示す。

## Scalability（拡張性）
**N/A**。coreはインメモリのプロセス内ライブラリであり、水平スケーリング・負荷分散の概念を持たない。呼び出し側アプリケーションのスケーリング戦略に依存する。

## Performance（性能）
requirements.md NFR-1で確定済み: 特別な性能要件は設けないが、`Template`はパース結果を再利用可能なコンパイル済み内部表現（ASTツリー）として保持し、同一テンプレートの繰り返しレンダリングで再パースを不要とする設計とする。

## Availability（可用性）
**N/A**。requirements.md NFR-5でResiliency Baseline拡張が無効と確定済み。coreはホスティングされるサービスではない。

## Security（セキュリティ、Security Baseline拡張: 有効）

### NFR-SEC-1: FilePartialResolverのパストラバーサル対策
`FilePartialResolver.resolve(partialName)`は、`baseDir.resolve(partialName + ".mustache")`で得たパスを正規化（`Path.normalize()`等）した上で、正規化後のパスが`baseDir`（同じく正規化済み）の配下に収まっていることを検証する。範囲外と判定された場合は、例外を送出せず「未解決（`null`）」として扱う。これにより、他の未解決パーシャル（単純に存在しないファイル）と外部から見て区別できないようにし、パス構造に関する情報漏えいを防ぐ。（SECURITY-05, SECURITY-09対応）

### NFR-SEC-2: 依存関係の脆弱性スキャン
ビルドにOWASP Dependency-Check Gradleプラグインを導入し、既知の脆弱性を持つ依存関係を検出する。CIまたはローカルビルド手順にスキャン実行ステップを含める（具体的なタスク組み込み方法はBuild and Testステージで確定）。（SECURITY-10対応）

### NFR-SEC-3: 例外メッセージのハードニング
`MustacheParseException`（行番号・列番号・構文エラー内容）、`MustacheRenderException`（関連するキー/パーシャル名）には、テンプレート開発者のデバッグに必要な仕様上の情報のみを含める。JVM内部のスタックトレース文字列やファイルシステムの絶対パス等、内部実装詳細を独自にメッセージへ埋め込むことはしない。根本原因は標準の`cause`チェーン（`Throwable#getCause()`）で保持し、ログ出力自体は呼び出し側アプリケーションに委ねる。（SECURITY-09対応）

### 適用対象外と判断したSECURITY項目
Security Compliance節（本ファイル末尾）を参照。

## Reliability（信頼性）

### NFR-REL-1: スレッドセーフ性の検証
`Context`のイミュータブル設計（BR-10）により、同一`Template`インスタンスへの並行`render()`呼び出しが安全であることを保証する。この性質を検証するため、複数スレッドから同一`Template`インスタンスに対して同時に`render()`を呼び出し、各スレッドの出力がそれぞれの期待値と一致することを確認する専用のマルチスレッドテストをUnit 1のテストスイートに含める。

## Maintainability（保守性）

### NFR-MNT-1: 公開APIのJavadoc
`core`パッケージの公開クラス・メソッド（`Mustache`, `Template`, `PartialResolver`, `MapPartialResolver`, `FilePartialResolver`, `Lambda`, `MustacheException`, `MustacheParseException`, `MustacheRenderException`）にはJavadocを必須とする。内部実装クラス（AST Node群、`Parser`、`Renderer`、`Context`等でpublicでないもの）は対象外。

## Usability（使いやすさ）
**N/A**。coreはCLIやUIを持たないライブラリAPIのみを提供する。

## Testability（テスト、NFR-3/NFR-6関連）
- JUnit 5による単体テストを実施する（requirements.md NFR-3）
- Property-Based Testing拡張（PBT-01〜10、フル適用）に基づき、Testable Properties（`functional-design/business-logic-model.md` 4節）ごとにPBTを実施する
- 公式Mustache specテストスイート（BR-2のスコープ内）をOracleテストとして実行する

---

## Security Compliance（Security Baseline拡張）

| Rule | 判定 | 根拠 |
|---|---|---|
| SECURITY-01（保存時/転送時暗号化） | N/A | coreはデータストア・ネットワーク通信を持たない |
| SECURITY-02（アクセスログ） | N/A | coreはネットワーク仲介コンポーネントを持たない |
| SECURITY-03（アプリケーションログ） | Compliant | ユーザー指示により方針を修正: `System.out`/`System.err`は使用せず、SLF4J（`slf4j-api`）をログAPIとして採用する。coreは実行環境（バインディング/実装）を強制せず`slf4j-api`のみに依存し、呼び出し側が任意のロギング実装を選択できる（ライブラリのベストプラクティスは維持しつつ、標準的なログAPIの利用に統一） |
| SECURITY-04（HTTPセキュリティヘッダー） | N/A | HTMLをサーブするエンドポイントを持たない |
| SECURITY-05（入力検証） | Compliant | NFR-SEC-1でFilePartialResolverのパストラバーサル対策を規定。パーシャル名・テンプレート文字列は型（`String`）レベルで扱われ、構文的に不正な入力はパースエラーとして安全に失敗する（BR準拠） |
| SECURITY-06（最小権限アクセスポリシー） | N/A | IAM/権限ポリシーを持たない |
| SECURITY-07（ネットワーク構成） | N/A | ネットワーク構成を持たない |
| SECURITY-08（アプリケーションレベルアクセス制御） | N/A | 認証・認可の対象となるエンドポイントを持たない |
| SECURITY-09（堅牢化・エラーハンドリング） | Compliant | NFR-SEC-3で例外メッセージのハードニング方針を規定 |
| SECURITY-10（ソフトウェアサプライチェーン） | Compliant | NFR-SEC-2でOWASP Dependency-Check導入を規定。Gradleの依存関係管理によりバージョン固定・ロックが可能（詳細はtech-stack-decisions.md） |
| SECURITY-11（セキュアな設計原則） | N/A | 認証・決済等のセキュリティクリティカルな機能を持たない。レート制限が必要な公開エンドポイントも無い |
| SECURITY-12（認証・認証情報管理） | N/A | 認証機能を持たない |
| SECURITY-13（ソフトウェア・データ整合性） | N/A | coreはテンプレート/パーシャル文字列の構文解析のみを行い、シリアライズされたオブジェクトのデシリアライズは行わない（JSON/YAMLのデシリアライズはUnit 2 CLIの責務であり、Unit 2のNFR Requirementsで評価する） |
| SECURITY-14（アラート・監視） | N/A | 監視対象となる稼働中サービスを持たない |
| SECURITY-15（例外処理・フェイルセーフ） | Compliant | business-logic-model.md 3節でパースエラー/レンダリングエラーの区分を明確化。BR-8でPOJOアクセス例外を`MustacheRenderException`にラップし、フェイルクローズ（レンダリング中断）とする設計を規定済み |

## PBT Compliance（Property-Based Testing拡張）

| Rule | 判定 | 根拠 |
|---|---|---|
| PBT-01（性質の識別） | Compliant | functional-design/business-logic-model.md 4節「Testable Properties」で識別済み |
| PBT-09（フレームワーク選定） | Compliant | 本ドキュメントおよびtech-stack-decisions.mdでjqwik（最新安定版）を選定 |
| その他のPBTルール（02〜08, 10） | 適用時期はCode Generationステージ | Code Generationステージで生成するテストコードに対して適用・評価する（Enforcement Integration表の定義通り） |
