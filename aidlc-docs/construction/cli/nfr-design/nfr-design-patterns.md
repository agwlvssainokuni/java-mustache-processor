# NFR Design Patterns - cli (Unit 2: CLI Tool)

`cli-nfr-design-plan.md` の回答結果（Q1〜Q4すべてA）に基づき、NFR要件を組み込む設計パターンを確定する。

## Resilience Patterns（耐障害性）

### パターン1: 例外→ExitCodeの一元的マッピング（Q1=A）
`CliRunner.run(args, in, out, err)`内に単一のtry-catchチェーンを設け、以下の順序でcatch節を並べる（**上から順に評価されるため、より具体的な例外型を先に配置する**）:

```
try {
    ... Step1(引数解析) 〜 Step6(出力) ...
    return ExitCode.SUCCESS;
} catch (ArgumentException e) {
    return ExitCode.ARGUMENT_ERROR;
} catch (com.fasterxml.jackson.core.JsonProcessingException e) {
    // JsonProcessingExceptionはIOExceptionのサブタイプのため、
    // 後続の汎用IOExceptionのcatch節より必ず先に配置する（パターン2参照）
    return ExitCode.PARSE_ERROR;
} catch (cherry.mustache.MustacheParseException e) {
    return ExitCode.PARSE_ERROR;
} catch (cherry.mustache.MustacheRenderException e) {
    return ExitCode.RENDER_ERROR;
} catch (java.io.IOException e) {
    return ExitCode.IO_ERROR;
} catch (RuntimeException e) {
    // 分類できない予期しない例外（business-logic-model.md 4.1節の既定カテゴリ）
    return ExitCode.RENDER_ERROR;
}
```

`ArgumentParser`/`DataLoader`/`Mustache`/`Template`/`OutputWriter`等の各コンポーネントは、自身の責務における例外を素直に送出するだけでよく、`ExitCode`を意識しない（`ExitCode`の決定は`CliRunner`に一元化する）。

### パターン2: Jackson例外の扱い（Q2=A）
`DataLoader`はJacksonの例外（`JsonProcessingException`とそのサブクラス、例: `JsonParseException`, `MismatchedInputException`）を追加の独自例外クラスにラップせずそのまま伝播させる。cli固有の新しい例外クラス（例: `DataFormatException`）は追加しない（Application Designで確定済みの例外構成（`ArgumentException`のみ）を変更しない）。

**注意点**: `JsonProcessingException`は`java.io.IOException`のサブタイプである。パターン1のcatchチェーンで`JsonProcessingException`を汎用`IOException`より後に配置すると、データの構文エラーが誤って`IO_ERROR`に分類されてしまう。この誤分類を防ぐため、catch節の順序を厳密に守る。

### パターン3: 出力ファイル書き込みエラーの扱い（Q4=A）
`OutputWriter`は`IOException`（ディスク容量不足、権限エラー等）を捕捉せずそのまま`CliRunner`へ伝播させ、パターン1のcatchチェーンにより`IO_ERROR`としてマッピングする。一時ファイル経由のアトミックな書き込み（一時ファイルへ書き込み後、成功時のみ最終ファイルへリネーム）は行わない。部分的に書き込まれた不完全な出力ファイルが残る可能性はあるが、単一開発者・低頻度実行のCLIツールとして許容できるリスクと判断する（シンプルさを優先）。

## Performance Patterns（性能）
**N/A**。CLIは1回の実行で各テンプレートを1回ずつコンパイル・レンダリングするのみであり、繰り返し実行や再利用の概念が無い。パース結果再利用のキャッシュパターン（NFR-1）は`core`（`Template`インスタンス）側で既に対応済み（`core/nfr-design/nfr-design-patterns.md`参照）。

## Security Patterns（セキュリティ実装方式）

### パターン: パストラバーサル対策のcore一任（Q3=A）
`cli`は`--partial-dir`指定の有無に応じた基準ディレクトリの決定（`business-rules.md` BR-5）のみを担当し、`core`の`FilePartialResolver`インスタンスにその基準ディレクトリを渡す。パストラバーサル対策自体（正規化・`baseDir`外の検出）は`core`の`FilePartialResolver`が既に実装済み（`core/nfr-design/nfr-design-patterns.md`「Security Patterns」参照）であるため、`cli`側では重複した検証ロジックを一切実装しない。

`--partial-dir`の値自体（テンプレート/データ/出力パスも同様）の存在確認は、`nfr-requirements.md` NFR-SEC-3で確定済みの「実行時に検出」方針に従い、事前検証は行わない。

## Logical Components
本ユニットにはキュー・キャッシュ・サーキットブレーカー等の外部インフラ的コンポーネントは存在しない（単発実行のCLIプロセスのため）。詳細は`logical-components.md`を参照。
