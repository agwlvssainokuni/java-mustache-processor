# Component Dependency

## 依存関係マトリクス

| コンポーネント | 依存先 |
|---|---|
| `Mustache` | `Parser`, `Template` |
| `Template` | `Renderer`, `Context`, `PartialResolver` |
| `Parser` | `ast.*`（AST Nodes）, `MustacheParseException` |
| `ast.*`（各Node） | `Context`, `PartialResolver`, `MustacheRenderException` |
| `Renderer` | `ast.*`（AST Nodes）, `Context`, `PartialResolver` |
| `Context` | （なし。純粋なデータ解決ロジック） |
| `MapPartialResolver` | `PartialResolver`（interface実装） |
| `Lambda` | （なし。呼び出し側が実装する契約のみ） |
| `CliRunner` | `ArgumentParser`, `DataLoader`, `FilePartialResolver`, `Mustache`, `Template`, `OutputWriter`, `ExitCode` |
| `Main` | `CliRunner`, `ExitCode` |
| `ArgumentParser` | `CliArguments`, `ArgumentException` |
| `DataLoader` | `CliArguments`（JSON/YAMLパーサーライブラリはNFR Requirementsで選定） |
| `FilePartialResolver` | `PartialResolver`（core、interface実装） |
| `OutputWriter` | `CliArguments` |

## モジュール（Gradleサブプロジェクト）間の依存

```
cli サブプロジェクト
  └── depends on → core サブプロジェクト
```

`core`は`cli`に依存しない（一方向）。`core`は外部ライブラリへの依存を最小限（標準ライブラリ中心）に保ち、`cli`のみがJSON/YAMLパーサーライブラリ等の追加依存を持つ。

## コンポーネント関連図

```mermaid
flowchart TB
    subgraph CORE["core サブプロジェクト (cherry.mustache)"]
        Mustache["Mustache<br/>(factory)"]
        Template["Template"]
        Parser["Parser"]
        AST["ast.* (Node群)"]
        Renderer["Renderer"]
        Context["Context"]
        PartialResolver["PartialResolver<br/>(interface)"]
        MapPartialResolver["MapPartialResolver"]
        Lambda["Lambda<br/>(interface)"]
        Exceptions["MustacheException 系"]
    end

    subgraph CLI["cli サブプロジェクト (cherry.mustache.cli)"]
        Main["Main"]
        CliRunner["CliRunner"]
        ArgumentParser["ArgumentParser"]
        DataLoader["DataLoader"]
        FilePartialResolver["FilePartialResolver"]
        OutputWriter["OutputWriter"]
        ExitCode["ExitCode"]
    end

    Mustache --> Parser
    Mustache --> Template
    Parser --> AST
    Template --> Renderer
    Template --> Context
    Template --> PartialResolver
    Renderer --> AST
    Renderer --> Context
    MapPartialResolver -.implements.-> PartialResolver
    FilePartialResolver -.implements.-> PartialResolver

    Main --> CliRunner
    CliRunner --> ArgumentParser
    CliRunner --> DataLoader
    CliRunner --> FilePartialResolver
    CliRunner --> Mustache
    CliRunner --> Template
    CliRunner --> OutputWriter
    CliRunner --> ExitCode
    CliRunner -.depends on.-> CORE

    style CORE fill:#C8E6C9,stroke:#2E7D32,stroke-width:3px,color:#000
    style CLI fill:#BBDEFB,stroke:#1565C0,stroke-width:3px,color:#000
```

### テキスト代替（コンポーネント関連図）

```
[core] Mustache --> Parser, Template
[core] Parser --> ast.*(Node群)
[core] Template --> Renderer, Context, PartialResolver
[core] Renderer --> ast.*(Node群), Context
[core] MapPartialResolver ..implements.. PartialResolver

[cli] Main --> CliRunner
[cli] CliRunner --> ArgumentParser, DataLoader, FilePartialResolver, OutputWriter, ExitCode
[cli] CliRunner --> (core) Mustache, Template
[cli] FilePartialResolver ..implements.. (core) PartialResolver

依存方向: cli サブプロジェクト → core サブプロジェクト（一方向、逆方向の依存なし）
```

## 通信パターン
- コンポーネント間はすべて**同期的な直接メソッド呼び出し**（インプロセス）。非同期処理・メッセージング・ネットワーク通信は存在しない
- `PartialResolver`はStrategyパターンとして機能し、`core`はCLI固有の実装（`FilePartialResolver`）を一切知らない（`cli`→`core`の一方向依存を維持するための設計）
