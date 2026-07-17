package cherry.mustache.ast;

import cherry.mustache.render.Context;
import cherry.mustache.render.RenderSession;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * テンプレート全体のASTルート。子ノード群を順にレンダリングする。
 */
public final class RootNode extends Node {

    private final List<Node> children;

    public RootNode(List<Node> children) {
        this.children = children;
    }

    @Override
    public void render(Context context, RenderSession session, Writer out) throws IOException {
        for (Node child : children) {
            child.render(context, session, out);
        }
    }
}
