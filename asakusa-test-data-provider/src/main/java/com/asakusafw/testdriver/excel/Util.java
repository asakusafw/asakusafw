/**
 * Copyright 2011 Asakusa Framework Team.
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
package com.asakusafw.testdriver.excel;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Common utilities for this package.
 * @since 0.2.0
 */
final class Util {

    private static final Pattern FRAGMENT = Pattern.compile(":(\\d+)|([^:].*)");

    private static final String EXTENSION = ".xls";

    static Sheet extract(URI source) throws IOException {
        assert source != null;
        String path = source.getPath();
        if (path == null || path.endsWith(EXTENSION) == false) {
            return null;
        }
        String fragment = source.getFragment();
        if (fragment == null) {
            return null;
        }
        Matcher matcher = FRAGMENT.matcher(fragment);
        if (matcher.matches() == false) {
            return null;
        }

        URL url = source.toURL();
        InputStream in = url.openStream();
        Workbook book;
        try {
            InputStream bin = new BufferedInputStream(in);
            book = new HSSFWorkbook(bin);
        } catch (IOException e) {
            throw new IOException(MessageFormat.format(
                    "Excelファイルの展開に失敗しました: {0}",
                    source));
        } finally {
            in.close();
        }

        if (matcher.group(1) != null) {
            int sheetNumber = Integer.parseInt(matcher.group(1));
            try {
                Sheet sheet = book.getSheetAt(sheetNumber);
                assert sheet != null;
                return sheet;
            } catch (RuntimeException e) {
                throw new IOException(MessageFormat.format(
                        "Excelシートの展開に失敗しました: {0} (シート{1}番がありません)",
                        source,
                        sheetNumber), e);
            }
        } else {
            String name = matcher.group(2);
            String sheetName = name;
            assert sheetName != null;
            Sheet sheet = book.getSheet(sheetName);
            if (sheet == null) {
                throw new IOException(MessageFormat.format(
                        "Excelシートの展開に失敗しました: {0} (シート\"{1}\"がありません)",
                        source,
                        sheetName));
            }
            return sheet;
        }
    }

    static String buildText(String symbol, String title) {
        assert symbol != null;
        assert title != null;
        if (symbol.equalsIgnoreCase(title)) {
            return symbol;
        } else {
            return MessageFormat.format(
                    "{0} ({1})", //$NON-NLS-1$
                    title,
                    symbol);
        }
    }

    private static final Pattern TEXT = Pattern.compile(".*\\(([0-9A-Za-z_\\- ]+)\\)");
    static String extractSymbol(String text) {
        assert text != null;
        Matcher matcher = TEXT.matcher(text);
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            return text;
        }
    }

    private Util() {
        return;
    }
}
