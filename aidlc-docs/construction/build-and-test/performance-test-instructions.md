# Performance Test Instructions

## 適用判定: N/A

本プロジェクトは以下の理由によりパフォーマンステストの対象外と判断する。

- `requirements.md` NFR-1にて「特別な性能要件は設けない（高頻度・大量データを想定した高速化は今回のスコープ外）」と明記済み
- `core`のNFR Requirements（`nfr-requirements.md`）でScalability/Performanceは既にN/Aと判定済み。パース結果の再利用（コンパイル済み`Template`インスタンス）という一般的に妥当な設計方針のみが要件であり、具体的な応答時間・スループット目標は存在しない
- `cli`のNFR Requirements（`nfr-requirements.md`）でもScalability/PerformanceはN/Aと判定済み。CLIは単発実行のプロセスであり、負荷テスト・スループット測定の対象となる継続稼働サービスではない

## 代替として実施した確認
`unit-test-instructions.md`の`ConcurrentRenderTest`（16スレッド×200回の並行`render()`呼び出し）により、同一`Template`インスタンスへの並行アクセス下での正当性（NFR-REL-1）を確認済み。これはスループット等のパフォーマンス特性の測定ではなく、あくまで並行アクセス時の正しさ（correctness）の検証である。

## 将来、性能要件が追加された場合の対応方針（参考）
- `core`: JMH（Java Microbenchmark Harness）等によるマイクロベンチマークで、テンプレートサイズ・データサイズに対するレンダリング時間を計測する
- `cli`: 大量データ・大きなテンプレートに対する起動時間・処理時間を計測する

いずれも現時点のスコープには含まれない。
