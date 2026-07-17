package cherry.mustache.render;

import cherry.mustache.PartialResolver;
import cherry.mustache.ast.Node;
import cherry.mustache.parser.Parser;

import java.io.IOException;
import java.io.Writer;

/**
 * ASTルートノードからレンダリングを駆動するエントリポイント。
 * 内部的には{@code root.render(...)}を呼び出す（component-methods.md）。
 */
public final class Renderer {

    private final Parser parser = new Parser();

    /**
     * @param root            ASTルートノード
     * @param context         初期データコンテキスト
     * @param partialResolver パーシャル解決に使うリゾルバー
     * @param out             出力先
     */
    public void render(Node root, Context context, PartialResolver partialResolver, Writer out) throws IOException {
        RenderSession session = new RenderSession(partialResolver,
                (template, openDelimiter, closeDelimiter) -> parser.parse(template, openDelimiter, closeDelimiter));
        root.render(context, session, out);
    }
}
