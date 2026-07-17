package cherry.mustache;

import cherry.mustache.ast.Node;
import cherry.mustache.render.Context;
import cherry.mustache.render.Renderer;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * コンパイル済みのMustacheテンプレート。内部にコンパイル済みAST（{@link Node}ツリー）を保持し、
 * 複数回の{@code render}呼び出しで再パースを行わず再利用できる（NFR-1）。
 * イミュータブルな{@link Context}に基づくため、同一インスタンスへの並行{@code render()}呼び出しに対して安全（NFR-REL-1）。
 */
public final class Template {

    private final Node root;
    private final PartialResolver defaultPartialResolver;
    private final Renderer renderer = new Renderer();

    Template(Node root, PartialResolver defaultPartialResolver) {
        this.root = root;
        this.defaultPartialResolver = defaultPartialResolver;
    }

    /**
     * @param data レンダリング対象データ（{@code Map}またはPOJO）
     * @return レンダリング結果の文字列
     */
    public String render(Object data) {
        return render(data, defaultPartialResolver);
    }

    /**
     * @param data レンダリング対象データ（{@code Map}またはPOJO）
     * @param out  レンダリング結果の書き込み先
     */
    public void render(Object data, Writer out) {
        renderTo(data, defaultPartialResolver, out);
    }

    /**
     * @param data            レンダリング対象データ（{@code Map}またはPOJO）
     * @param partialResolver コンパイル時のものを上書きするパーシャルリゾルバー
     * @return レンダリング結果の文字列
     */
    public String render(Object data, PartialResolver partialResolver) {
        StringWriter writer = new StringWriter();
        renderTo(data, partialResolver, writer);
        return writer.toString();
    }

    private void renderTo(Object data, PartialResolver partialResolver, Writer out) {
        try {
            renderer.render(root, new Context(data, null), partialResolver, out);
        } catch (IOException e) {
            throw new MustacheRenderException("Failed to write render output", null, e);
        }
    }
}
