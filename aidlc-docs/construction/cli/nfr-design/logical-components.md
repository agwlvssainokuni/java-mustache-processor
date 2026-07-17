# Logical Components - cli (Unit 2: CLI Tool)

NFR観点を組み込んだ論理コンポーネントの一覧。データモデルとしての詳細は`functional-design/domain-entities.md`を参照し、本ファイルはNFRとの対応関係のみを示す。

| コンポーネント | NFR対応 |
|---|---|
| `CliRunner` | Resilience: 例外→`ExitCode`の一元的マッピング（catchチェーン、`nfr-design-patterns.md`パターン1）。Security: エラーメッセージのハードニング方針（SECURITY-09、`business-rules.md` BR-9）の実施箇所 |
| `ArgumentParser` | Security: 引数構文検証（`ArgumentException`送出）。ファイルパスの存在確認は行わない（実行時検出方針、NFR-SEC-3） |
| `DataLoader` | Resilience: Jackson例外をラップせず伝播（`nfr-design-patterns.md`パターン2）。Security: `ObjectMapper`を`Map<String,Object>`固定で使用し多相型ハンドリングを無効化（NFR-SEC-2） |
| `OutputWriter` | Resilience: `IOException`をラップせず伝播、アトミック書き込み保証なし（`nfr-design-patterns.md`パターン3） |
| `Main` | 配線（wiring）のみの責務、NFR対応なし |
| `ExitCode` | Reliability: 終了コード区分の一元的な定義（`business-rules.md` BR-8） |
| `ArgumentException` | Security: 引数エラーの型的な表現。`core`の`MustacheException`階層とは独立 |

## coreコンポーネントの再利用（重複実装なし）
| コンポーネント | 再利用元 | NFR対応の所在 |
|---|---|---|
| `FilePartialResolver` | `core` | パストラバーサル対策は`core`側の`nfr-design-patterns.md`「Security Patterns」に一元化。`cli`は基準ディレクトリの決定（BR-5）のみを担当 |
| `Mustache` / `Template` | `core` | コンパイル済み表現の再利用（Performance）は`core`側で対応済み |

## 外部インフラ的コンポーネント（N/A）
本ユニットは単発実行のCLIプロセスであり、以下の種類のコンポーネントは存在しない:
- キュー・メッセージブローカー
- 分散キャッシュ（Redis等）
- サーキットブレーカー・リトライフレームワーク（外部サービス呼び出しが無いため不要）
- ロードバランサー・APIゲートウェイ

## テスト関連コンポーネント（Tech Stack Decisionsとの対応）
| コンポーネント | 対応するNFR/BRルール |
|---|---|
| `CliRunner`の標準入出力モック化テスト（引数パターン・データ形式・エラーケース網羅） | NFR-REL-1（`nfr-requirements.md`） |
| OWASP Dependency-Checkタスク（`cli/build.gradle.kts`） | NFR-SEC-1、SECURITY-10 |
