package cherry.mustache.ast;

import cherry.mustache.render.Context;
import cherry.mustache.render.RenderSession;

import java.io.Writer;

/**
 * コメント（{@code {{! ... }}}）を表すノード。内容は保持するがレンダリング時には無視する。
 */
public final class CommentNode extends Node {

    private final String text;

    public CommentNode(String text) {
        this.text = text;
    }

    @Override
    public void render(Context context, RenderSession session, Writer out) {
        // コメントは出力しない
    }
}
