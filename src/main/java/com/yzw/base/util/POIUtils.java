package com.yzw.base.util;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.Region;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.Iterator;
/**
 * Created by liuyj on 2015/4/08.
 *
 * POI工具类
 * liuyingjun
 * 2015-04-23
 *
 */
public class POIUtils {
    /**
     * 获取区域 Region
     * @param sheet
     * @param row
     * @param column
     * @return
     */
    public static int getMergedRegionIndex(Sheet sheet, int row, int column) {

        int sheetMergeCount = sheet.getNumMergedRegions();
        for (int i = 0; i < sheetMergeCount; i++) {
            CellRangeAddress ca = sheet.getMergedRegion(i);
            int firstColumn = ca.getFirstColumn();
            int lastColumn = ca.getLastColumn();
            int firstRow = ca.getFirstRow();
            int lastRow = ca.getLastRow();
            if (row >= firstRow && row <= lastRow) {
                if (column >= firstColumn && column <= lastColumn) {
                    return i;
                }
            }
        }
        return 0;
    }

    /**
	 * 复制一个单元格样式到目的单元格样式
	 * @param fromStyle
	 * @param toStyle
	 */
	public static void copyCellStyle(HSSFCellStyle fromStyle,
			HSSFCellStyle toStyle,HSSFWorkbook workbook) {
		toStyle.setAlignment(fromStyle.getAlignment());
		//边框和边框颜色
		toStyle.setBorderBottom(fromStyle.getBorderBottom());
		toStyle.setBorderLeft(fromStyle.getBorderLeft());
		toStyle.setBorderRight(fromStyle.getBorderRight());
		toStyle.setBorderTop(fromStyle.getBorderTop());
		toStyle.setTopBorderColor(fromStyle.getTopBorderColor());
		toStyle.setBottomBorderColor(fromStyle.getBottomBorderColor());
		toStyle.setRightBorderColor(fromStyle.getRightBorderColor());
		toStyle.setLeftBorderColor(fromStyle.getLeftBorderColor());
		
		//背景和前景
		toStyle.setFillBackgroundColor(fromStyle.getFillBackgroundColor());
		toStyle.setFillForegroundColor(fromStyle.getFillForegroundColor());
		
		toStyle.setDataFormat(fromStyle.getDataFormat());
		toStyle.setFillPattern(fromStyle.getFillPattern());
		toStyle.setFont(fromStyle.getFont(workbook));
		toStyle.setHidden(fromStyle.getHidden());
		toStyle.setIndention(fromStyle.getIndention());//首行缩进
		toStyle.setLocked(fromStyle.getLocked());
		toStyle.setRotation(fromStyle.getRotation());//旋转
		toStyle.setVerticalAlignment(fromStyle.getVerticalAlignment());
		toStyle.setWrapText(fromStyle.getWrapText());
		
	}
	/**
	 * Sheet复制
	 * @param fromSheet
	 * @param toSheet
	 * @param copyValueFlag
	 */
	public static void copySheet(HSSFWorkbook wb,HSSFSheet fromSheet, HSSFSheet toSheet,
			boolean copyValueFlag) {
		//合并区域处理
		mergerRegion(fromSheet, toSheet);
		for (Iterator rowIt = fromSheet.rowIterator(); rowIt.hasNext();) {
			HSSFRow tmpRow = (HSSFRow) rowIt.next();
			HSSFRow newRow = toSheet.createRow(tmpRow.getRowNum());
			//行复制
			copyRow(wb,tmpRow,newRow,copyValueFlag);
		}
	}
	/**
	 * 行复制功能
	 * @param fromRow
	 * @param toRow
	 */
	public static void copyRow(HSSFWorkbook wb,HSSFRow fromRow,HSSFRow toRow,boolean copyValueFlag){
		for (Iterator cellIt = fromRow.cellIterator(); cellIt.hasNext();) {
			HSSFCell tmpCell = (HSSFCell) cellIt.next();
			HSSFCell newCell = toRow.createCell(tmpCell.getCellNum());
			copyCell(wb,tmpCell, newCell, copyValueFlag);
		}
        //行高
        toRow.setHeightInPoints(fromRow.getHeightInPoints());
        //行样式
        toRow.setRowStyle(fromRow.getRowStyle());
	}
	/**
	* 复制原有sheet的合并单元格到新创建的sheet
	* 
	* @param fromSheet 原有的sheet
	* @param toSheet      新创建sheet
	*/
	public static void mergerRegion(HSSFSheet fromSheet, HSSFSheet toSheet) {
	   int sheetMergerCount = fromSheet.getNumMergedRegions();
	   for (int i = 0; i < sheetMergerCount; i++) {
	    Region mergedRegionAt = fromSheet.getMergedRegionAt(i);
	    toSheet.addMergedRegion(mergedRegionAt);
	   }
	}
	/**
	 * 复制单元格
	 * 
	 * @param srcCell
	 * @param distCell
	 * @param copyValueFlag
	 *            true则连同cell的内容一起复制
	 */
	public static void copyCell(HSSFWorkbook wb,HSSFCell srcCell, HSSFCell distCell,
			boolean copyValueFlag) {
		HSSFCellStyle newstyle=wb.createCellStyle();
		copyCellStyle(srcCell.getCellStyle(), newstyle,wb);
		//distCell.setEncoding(srcCell.getEncoding());
		//样式
		distCell.setCellStyle(newstyle);
		//评论
		if (srcCell.getCellComment() != null) {
			distCell.setCellComment(srcCell.getCellComment());
		}
		// 不同数据类型处理
		int srcCellType = srcCell.getCellType();
		distCell.setCellType(srcCellType);
		if (copyValueFlag) {
			if (srcCellType == HSSFCell.CELL_TYPE_NUMERIC) {
				if (HSSFDateUtil.isCellDateFormatted(srcCell)) {
					distCell.setCellValue(srcCell.getDateCellValue());
				} else {
					distCell.setCellValue(srcCell.getNumericCellValue());
				}
			} else if (srcCellType == HSSFCell.CELL_TYPE_STRING) {
				distCell.setCellValue(srcCell.getRichStringCellValue());
			} else if (srcCellType == HSSFCell.CELL_TYPE_BLANK) {
				// nothing21
			} else if (srcCellType == HSSFCell.CELL_TYPE_BOOLEAN) {
				distCell.setCellValue(srcCell.getBooleanCellValue());
			} else if (srcCellType == HSSFCell.CELL_TYPE_ERROR) {
				distCell.setCellErrorValue(srcCell.getErrorCellValue());
			} else if (srcCellType == HSSFCell.CELL_TYPE_FORMULA) {
				distCell.setCellFormula(srcCell.getCellFormula());
			} else { // nothing29
			}
		}
	}
}