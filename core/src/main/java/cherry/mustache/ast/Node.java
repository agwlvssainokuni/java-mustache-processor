package cherry.mustache.ast;

import cherry.mustache.render.Context;
import cherry.mustache.render.RenderSession;

import java.io.IOException;
import java.io.Writer;

/**
 * ASTノードの抽象基底クラス。各サブクラスが自身のレンダリング方法を実装する
 * （ポリモーフィズムによるノード自己レンダリング、Application Design Q7）。
 */
public abstract class Node {

    /**
     * 自身をレンダリングし{@code out}に書き込む。
     *
     * @param context 現在のデータコンテキスト
     * @param session パーシャル解決・ラムダ再パース・循環参照検出を提供するレンダリングセッション
     * @param out     出力先
     */
    public abstract void render(Context context, RenderSession session, Writer out) throws IOException;
}
