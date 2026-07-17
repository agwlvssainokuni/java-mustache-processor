# Services

本プロジェクトはネットワークサービスを持たないライブラリ／CLIであるため、「サービス」は業務プロセスのオーケストレーションを担うコンポーネントを指す。

## Service 1: コンパイルオーケストレーション（`Mustache`）
- **責務**: テンプレート文字列を受け取り、`Parser`にパースを委譲してASTを取得し、`Template`インスタンスとして返す
- **オーケストレーション対象**: `Parser`
- **境界**: パース処理そのもの（構文解析ロジック）は`Parser`の責務であり、`Mustache`は入出力の取りまとめのみを行う

## Service 2: レンダリングオーケストレーション（`Template.render` → `Renderer`）
- **責務**: コンパイル済みAST・利用者データ・パーシャルリゾルバーを受け取り、`Renderer`にレンダリングを委譲して最終的な出力文字列を得る
- **オーケストレーション対象**: `Renderer`, `Context`, `PartialResolver`
- **境界**: ノードごとのレンダリングロジックは各ASTノード自身の責務（ポリモーフィズム）であり、`Renderer`はASTの走査開始点として機能する

## Service 3: CLIオーケストレーション（`CliRunner`）
- **責務**: CLI実行の一連の流れ（引数解析 → データ読込 → パーシャルリゾルバー構築 → テンプレートのコンパイル・連結レンダリング → 出力書き込み → 終了コード決定）を制御する
- **オーケストレーション対象**: `ArgumentParser`, `DataLoader`, `FilePartialResolver`, `Mustache`/`Template`（coreライブラリ）, `OutputWriter`
- **境界**: 各処理コンポーネントの内部ロジックには関与せず、呼び出し順序とエラー時の`ExitCode`決定のみを担う
- **エラーハンドリング方針**: `CliRunner`が`ArgumentException`（CLI固有） / `MustacheParseException` / `MustacheRenderException`（coreライブラリ由来） / `IOException`（ファイルI/O由来）を捕捉し、`ExitCode`にマッピングして返す。`Main`はこの`ExitCode`をもとに`System.exit()`するのみ

## オーケストレーションフロー概要（CLI実行時）

```
Main
  → CliRunner.run(args, in, out, err)
      → ArgumentParser.parse(args) → CliArguments
      → DataLoader.load(args, stdin) → Map<String, Object>
      → FilePartialResolver構築（args.partialDir または テンプレートと同一ディレクトリ）
      → 各テンプレートパスについて:
          → Mustache.compile(templateText, partialResolver) → Template
          → Template.render(data) → String
          → （複数テンプレートの場合は結果を連結）
      → OutputWriter.write(結果, args, out)
      → ExitCode.SUCCESS を返す
      （途中で例外発生時は捕捉し対応するExitCodeを返す）
```
