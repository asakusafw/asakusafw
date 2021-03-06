/**
 * Copyright 2011-2021 Asakusa Framework Team.
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

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Keeps cell styles.
 * @since 0.2.3
 */
class WorkbookInfo {

    final Workbook workbook;

    private final CellStyle commonStyle;

    final CellStyle titleStyle;

    final CellStyle dataStyle;

    final CellStyle dateDataStyle;

    final CellStyle timeDataStyle;

    final CellStyle datetimeDataStyle;

    /**
     * Creates a new instance.
     * @param workbook target workbook
     * @throws IllegalArgumentException if some parameters were {@code null}
     */
    WorkbookInfo(Workbook workbook) {
        if (workbook == null) {
            throw new IllegalArgumentException("workbook must not be null"); //$NON-NLS-1$
        }
        this.workbook = workbook;

        Font font = workbook.createFont();

        commonStyle = workbook.createCellStyle();
        commonStyle.setFont(font);
        commonStyle.setBorderTop(BorderStyle.THIN);
        commonStyle.setBorderBottom(BorderStyle.THIN);
        commonStyle.setBorderLeft(BorderStyle.THIN);
        commonStyle.setBorderRight(BorderStyle.THIN);

        titleStyle = workbook.createCellStyle();
        titleStyle.cloneStyleFrom(commonStyle);
        titleStyle.setLocked(true);
        titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        titleStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        titleStyle.setAlignment(HorizontalAlignment.CENTER);

        CreationHelper helper = workbook.getCreationHelper();
        DataFormat df = helper.createDataFormat();

        dataStyle = workbook.createCellStyle();
        dataStyle.cloneStyleFrom(commonStyle);

        dateDataStyle = workbook.createCellStyle();
        dateDataStyle.cloneStyleFrom(commonStyle);
        dateDataStyle.setDataFormat(df.getFormat("yyyy-mm-dd")); //$NON-NLS-1$

        timeDataStyle = workbook.createCellStyle();
        timeDataStyle.cloneStyleFrom(commonStyle);
        timeDataStyle.setDataFormat(df.getFormat("hh:mm:ss")); //$NON-NLS-1$

        datetimeDataStyle = workbook.createCellStyle();
        datetimeDataStyle.cloneStyleFrom(commonStyle);
        datetimeDataStyle.setDataFormat(df.getFormat("yyyy-mm-dd hh:mm:ss")); //$NON-NLS-1$
    }
}
