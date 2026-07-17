# Logical Components - core (Unit 1: Core Template Engine)

NFR観点を組み込んだ論理コンポーネントの一覧。データモデルとしての詳細は`functional-design/domain-entities.md`を参照し、本ファイルはNFRとの対応関係のみを示す。

| コンポーネント | NFR対応 |
|---|---|
| `Template` | Performance: コンパイル済みASTを保持し再利用可能にする（グローバルキャッシュなし）。Reliability: イミュータブルな内部状態により、同一インスタンスへの並行`render()`呼び出しに対して安全 |
| `Context` | Reliability: イミュータブル連結構造（BR-10）によりスレッドセーフ性を保証 |
| `FilePartialResolver` | Security: パス検証ガード節（`baseDir`外は未解決扱い）。Resilience: I/Oエラーの`MustacheRenderException`ラップ、`try-with-resources`によるリソース解放 |
| `MapPartialResolver` | NFR対応なし（インメモリMapのみを参照する単純な実装のため、追加のNFRパターンは不要） |
| `MustacheException`系（例外階層） | Security: メッセージに内部実装詳細を含めないハードニング方針（SECURITY-09） |

## 外部インフラ的コンポーネント（N/A）
本ユニットはプロセス内ライブラリであり、以下の種類のコンポーネントは存在しない:
- キュー・メッセージブローカー
- 分散キャッシュ（Redis等）
- サーキットブレーカー・リトライフレームワーク（外部サービス呼び出しが無いため不要）
- ロードバランサー・APIゲートウェイ

## テスト関連コンポーネント（Tech Stack Decisionsとの対応）
| コンポーネント | 対応するNFR/PBTルール |
|---|---|
| マルチスレッドテスト（`Context`/`Template`の並行`render()`検証） | NFR-REL-1 |
| jqwikベースのPBTテスト群 | PBT-01〜10（Testable Propertiesに対応、Code Generationステージで実装） |
| 公式specテストスイート実行基盤 | NFR-3、BR-2 |
| OWASP Dependency-Checkタスク | NFR-SEC-2、SECURITY-10 |
