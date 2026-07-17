package cherry.mustache.ast;

import cherry.mustache.Lambda;
import cherry.mustache.render.Context;
import cherry.mustache.render.RenderSession;

import java.io.IOException;
import java.io.Writer;

/**
 * エスケープ無し変数展開（{@code {{{key}}}} / {@code {{&key}}}）を行うノード。
 */
public final class UnescapedVariableNode extends Node {

    private final String key;
    private final String openDelimiter;
    private final String closeDelimiter;

    public UnescapedVariableNode(String key, String openDelimiter, String closeDelimiter) {
        this.key = key;
        this.openDelimiter = openDelimiter;
        this.closeDelimiter = closeDelimiter;
    }

    @Override
    public void render(Context context, RenderSession session, Writer out) throws IOException {
        Object value = context.resolve(key);
        String text;
        if (value instanceof Lambda lambda) {
            text = LambdaSupport.renderLambdaOutput(lambda.execute(""), context, session, openDelimiter, closeDelimiter);
        } else {
            text = value == null ? "" : String.valueOf(value);
        }
        out.write(text);
    }
}
