# Unit of Work Requirement Map

**注**: User Storiesはスキップ済み（開発者向けライブラリ/CLIで明確なユーザーペルソナがないため）。本ドキュメントは「ストーリー→Unit」マッピングの代替として、`requirements.md`の機能要件（FR）・非機能要件（NFR）とUnitの対応を示す。

## Unit 1: Core Template Engine（`core`）

| 要件ID | 概要 | 対応 |
|---|---|---|
| FR-1 | Mustacheテンプレートエンジン（ライブラリ本体、公式仕様フルサポート、Map/POJO、パーシャル解決、ラムダ） | 全面対応 |
| NFR-1 | パフォーマンス（コンパイル済み内部表現の再利用） | 全面対応 |
| NFR-3 | JUnit 5単体テスト、公式specテストスイート準拠テスト | 全面対応（`core`の`src/test`に配置） |
| NFR-4 | Security Baseline（入力検証、デシリアライズ安全性、堅牢化・フェイルセーフ、例外処理） | ライブラリ内で発生しうる項目に対応（SECURITY-05, 09, 13, 15等） |
| NFR-6 | Property-Based Testing（Round-trip, Invariant, Oracleの各性質） | 全面対応 |

## Unit 2: CLI Tool（`cli`）

| 要件ID | 概要 | 対応 |
|---|---|---|
| FR-2 | CLIツール（複数テンプレート連結出力、`--data`オプション、標準入出力、JSON/YAML自動判定/`--format`、パーシャル解決、出力先、終了コード、ラムダ非対応の明記） | 全面対応 |
| NFR-2 | ビルド環境のうちCLI固有分（fat jar配布、GraalVMは将来検討） | 全面対応 |
| NFR-3 | JUnit 5単体テスト（CLI固有ロジック: 引数解析、データ読込、終了コード） | 全面対応 |
| NFR-4 | Security Baselineのうち CLI固有分（コマンドライン引数・ファイルパスの検証、SECURITY-10の依存脆弱性スキャン対象としてのcli依存関係） | 該当項目に対応 |

## 両Unit共通・プロジェクト全体に関わる要件

| 要件ID | 概要 | 対応 |
|---|---|---|
| NFR-2（Gradle/Java 25/UTF-8/配布方法/言語） | ビルド環境全般 | ルートプロジェクト（`settings.gradle.kts`, ルート`build.gradle.kts`）で設定し、両Unitに適用 |
| NFR-5 | Resiliency Baseline（無効） | 対象Unitなし（適用しない） |

## 未マッピング要件の確認
`requirements.md`の全FR/NFR項目を確認した結果、上記2Unit + プロジェクト全体設定でカバーされていない要件は無い。
