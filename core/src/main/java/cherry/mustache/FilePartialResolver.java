package cherry.mustache;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 指定した基準ディレクトリ配下から、パーシャル名+{@code .mustache}拡張子でファイルを探索する{@link PartialResolver}実装。
 * 基準ディレクトリ外を指すパーシャル名は未解決（{@code null}）として扱う（NFR-SEC-1、パストラバーサル対策）。
 */
public final class FilePartialResolver implements PartialResolver {

    private final Path baseDir;

    /**
     * @param baseDir パーシャルファイルを探索する基準ディレクトリ
     */
    public FilePartialResolver(Path baseDir) {
        this.baseDir = baseDir.toAbsolutePath().normalize();
    }

    @Override
    public String resolve(String partialName) {
        Path candidate = baseDir.resolve(partialName + ".mustache").normalize();
        if (!candidate.startsWith(baseDir)) {
            return null;
        }
        if (!Files.isRegularFile(candidate)) {
            return null;
        }
        try {
            return Files.readString(candidate, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new MustacheRenderException("Failed to read partial file: " + partialName, partialName, e);
        }
    }
}
