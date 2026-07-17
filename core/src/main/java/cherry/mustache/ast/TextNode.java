package cherry.mustache.ast;

import cherry.mustache.render.Context;
import cherry.mustache.render.RenderSession;

import java.io.IOException;
import java.io.Writer;

/**
 * 静的テキストをそのまま出力するノード。
 */
public final class TextNode extends Node {

    private final String text;

    public TextNode(String text) {
        this.text = text;
    }

    @Override
    public void render(Context context, RenderSession session, Writer out) throws IOException {
        out.write(text);
    }
}
