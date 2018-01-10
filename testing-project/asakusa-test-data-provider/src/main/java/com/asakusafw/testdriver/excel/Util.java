/**
 * Copyright 2011-2018 Asakusa Framework Team.
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
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common utilities for this package.
 * @since 0.2.0
 * @version 0.5.3
 */
final class Util {

    static final Logger LOG = LoggerFactory.getLogger(Util.class);

    private static final Pattern FRAGMENT = Pattern.compile(":(\\d+)|([^:].*)"); //$NON-NLS-1$

    private static final String HSSF_EXTENSION = ".xls"; //$NON-NLS-1$

    private static final String XSSF_EXTENSION = ".xlsx"; //$NON-NLS-1$

    private static final String FRAGMENT_FIRST_SHEET = ":0"; //$NON-NLS-1$

    static Sheet extract(URI source) throws IOException {
        assert source != null;
        String path = source.getSchemeSpecificPart();
        if (isHssf(path) == false && isXssf(path) == false) {
            LOG.debug("Not an Excel workbook: {}", source); //$NON-NLS-1$
            return null;
        }
        String fragment = source.getFragment();
        if (fragment == null) {
            // the first sheet
            fragment = FRAGMENT_FIRST_SHEET;
            LOG.debug("Fragment is not set, using first sheet: {}", source); //$NON-NLS-1$
        }
        Matcher matcher = FRAGMENT.matcher(fragment);
        if (matcher.matches() == false) {
            LOG.info(MessageFormat.format(
                    Messages.getString("Util.infoUnsupportedUriFragment"), //$NON-NLS-1$
                    source));
            return null;
        }

        LOG.debug("Processing Excel workbook: {}", source); //$NON-NLS-1$
        URL url = source.toURL();
        Workbook book;
        try (InputStream in = new BufferedInputStream(url.openStream())) {
            book = openWorkbookFor(path, in);
        } catch (IOException e) {
            throw new IOException(MessageFormat.format(
                    Messages.getString("Util.errorFailedToOpenWorkbook"), //$NON-NLS-1$
                    source));
        }

        if (matcher.group(1) != null) {
            int sheetNumber = Integer.parseInt(matcher.group(1));
            LOG.debug("Opening sheet by index : {}", sheetNumber); //$NON-NLS-1$
            try {
                Sheet sheet = book.getSheetAt(sheetNumber);
                assert sheet != null;
                return sheet;
            } catch (RuntimeException e) {
                throw new IOException(MessageFormat.format(
                        Messages.getString("Util.errorMissingSheetByIndex"), //$NON-NLS-1$
                        source,
                        sheetNumber), e);
            }
        } else {
            String sheetName = matcher.group(2);
            LOG.debug("Opening sheet by name : {}", sheetName); //$NON-NLS-1$
            assert sheetName != null;
            Sheet sheet = book.getSheet(sheetName);
            if (sheet == null) {
                throw new IOException(MessageFormat.format(
                        Messages.getString("Util.errorMissingSheetByName"), //$NON-NLS-1$
                        source,
                        sheetName));
            }
            return sheet;
        }
    }

    static Workbook openWorkbookFor(String path, InputStream input) throws IOException {
        if (isHssf(path)) {
            return new HSSFWorkbook(input);
        } else if (isXssf(path)) {
            return new XSSFWorkbook(input);
        } else {
            return new HSSFWorkbook(input);
        }
    }

    static Workbook createEmptyWorkbookFor(String path) {
        if (isHssf(path)) {
            return new HSSFWorkbook();
        } else if (isXssf(path)) {
            return new XSSFWorkbook();
        } else {
            return new HSSFWorkbook();
        }
    }

    static SpreadsheetVersion getSpreadsheetVersionFor(String path) {
        if (isHssf(path)) {
            return SpreadsheetVersion.EXCEL97;
        } else if (isXssf(path)) {
            return SpreadsheetVersion.EXCEL2007;
        } else {
            return SpreadsheetVersion.EXCEL97;
        }
    }

    static boolean isXssf(String path) {
        if (path == null) {
            return false;
        }
        return path.endsWith(XSSF_EXTENSION);
    }

    static boolean isHssf(String path) {
        if (path == null) {
            return false;
        }
        return path.endsWith(HSSF_EXTENSION);
    }

    static String buildText(String symbol, String title) {
        assert symbol != null;
        assert title != null;
        if (symbol.equalsIgnoreCase(title)) {
            return symbol;
        } else {
            return MessageFormat.format(
                    "{0} [{1}]", //$NON-NLS-1$
                    title,
                    symbol);
        }
    }

    private static final Pattern TEXT = Pattern.compile(".*\\[(.*?)\\]"); //$NON-NLS-1$
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
