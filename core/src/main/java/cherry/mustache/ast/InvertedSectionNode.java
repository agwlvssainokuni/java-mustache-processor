package cherry.mustache.ast;

import cherry.mustache.Lambda;
import cherry.mustache.render.Context;
import cherry.mustache.render.RenderSession;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * 否定セクション（{@code {{^key}}...{{/key}}}）を表すノード。値がfalsyの場合のみ描画する。
 * Lambdaは常にtruthy扱いとし、否定セクションとしては何も出力しない（BR-2.4節）。
 */
public final class InvertedSectionNode extends Node {

    private final String key;
    private final List<Node> children;

    public InvertedSectionNode(String key, List<Node> children) {
        this.key = key;
        this.children = children;
    }

    @Override
    public void render(Context context, RenderSession session, Writer out) throws IOException {
        Object value = context.resolve(key);
        if (value instanceof Lambda) {
            return;
        }
        if (Truthiness.isTruthy(value)) {
            return;
        }
        for (Node child : children) {
            child.render(context, session, out);
        }
    }
}
