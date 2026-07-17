# NFR Design Patterns - core (Unit 1: Core Template Engine)

`core-nfr-design-plan.md` の回答結果（Q1〜Q3すべてA）に基づき、NFR要件を組み込む設計パターンを確定する。

## Resilience Patterns（耐障害性）

### パターン: フェイルクローズ＋例外ラップ（BR-8/SECURITY-15と一貫）
`FilePartialResolver.resolve(partialName)`のファイル読み取り処理は以下のパターンに従う:
1. パス検証（Security Patterns節参照）に失敗した場合、または対象ファイルが存在しない場合 → `null`を返す（「未解決」として正常系扱い、レンダリングは継続）
2. パス検証を通過し、ファイルが存在するにもかかわらず読み取り自体が失敗した場合（`IOException`: 権限エラー、I/O障害等）→ `MustacheRenderException`でラップして送出し、レンダリングを中断する（フェイルクローズ。Q1=A）
3. ファイル読み取りは`try-with-resources`（`Files.newBufferedReader`等）を用い、例外発生時も確実にファイルハンドルを解放する（SECURITY-15の「リソースクリーンアップ」対応）

この方針は、既にFunctional Designで確定しているBR-8（POJOアクセス例外のラップ）と対称的な設計であり、「未解決（missing）」と「異常系（exception）」を一貫した基準で区別する。

## Performance Patterns（性能）

### パターン: コンパイル済み表現の再利用（グローバルキャッシュなし、Q2=A）
- `Mustache.compile(templateString)`（または同等のファクトリメソッド）が返す`Template`インスタンスは、内部にコンパイル済みAST（`Node`ツリー）を保持する
- 呼び出し側が同一`Template`インスタンスを保持して繰り返し`render()`を呼び出すことで、テンプレート文字列の再パースを回避する（NFR-1準拠）
- ライブラリ内部にテンプレート文字列をキーとしたグローバル自動キャッシュは持たない。理由:
  - キャッシュエントリの無効化・サイズ上限管理という、要件にない複雑さを持ち込まない
  - `Template`インスタンス自体が「再利用可能なコンパイル済み表現」という要件を満たす最小の設計である
  - グローバル可変状態を持たないことは、BR-10（Contextのイミュータブル設計）と同じ「共有状態を避ける」思想と一貫する

## Security Patterns（セキュリティ実装方式）

### パターン: ガード節によるパス検証（Q3=A）
`FilePartialResolver.resolve(partialName)`内で以下のガード節を実装する（専用ユーティリティクラスへの切り出しは行わない。単一使用箇所のため）:

```
1. candidate = baseDir.resolve(partialName + ".mustache").normalize()
2. normalizedBaseDir = baseDir.toAbsolutePath().normalize()
3. if (!candidate.toAbsolutePath().startsWith(normalizedBaseDir)) return null  // baseDir外 → 未解決扱い
4. if (!Files.isRegularFile(candidate)) return null  // 存在しない → 未解決扱い
5. ファイル読み取りを試行し、失敗時はResilience Patterns節の方針に従う
```

この検証は`FilePartialResolver`のコンストラクタ引数`baseDir`を信頼の起点とし、パーシャル名（テンプレート内・CLI引数由来の可変値）を信頼しないという原則（SECURITY-05）に基づく。

## Logical Components
本ユニットにはキュー・キャッシュ・サーキットブレーカー等の外部インフラ的コンポーネントは存在しない（プロセス内ライブラリのため）。詳細は`logical-components.md`を参照。
