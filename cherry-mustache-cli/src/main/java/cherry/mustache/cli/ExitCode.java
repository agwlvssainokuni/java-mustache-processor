/*
 * Copyright 2026 agwlvssainokuni
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cherry.mustache.cli;

/**
 * CLIプロセスの終了コード。
 */
public enum ExitCode {
    SUCCESS(0),
    ARGUMENT_ERROR(1),
    PARSE_ERROR(2),
    RENDER_ERROR(3),
    IO_ERROR(4);

    private final int codeValue;

    ExitCode(int codeValue) {
        this.codeValue = codeValue;
    }

    public int code() {
        return codeValue;
    }
}
