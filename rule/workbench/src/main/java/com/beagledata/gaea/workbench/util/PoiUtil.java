package com.beagledata.gaea.workbench.util;

import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;

/**
 * 描述:
 * excel util
 * @author 周庚新
 * @date 2020-11-11
 */
public class PoiUtil {
	private static Logger logger = LoggerFactory.getLogger(PoiUtil.class);

	public static void downExcel(List<List<String>> list, boolean isMergedHead, HttpServletResponse response) {
		try {
			XSSFWorkbook xssfWorkbook = new XSSFWorkbook();
			XSSFSheet xssfSheet = xssfWorkbook.createSheet();
			XSSFCellStyle style = xssfWorkbook.createCellStyle();

			XSSFRow xssfRow;
			XSSFCell xssfCell;
			int num = 0;
			int maxNum = 0;
			for (int i = 0; i < list.size(); i++) {
				xssfRow = xssfSheet.createRow(i);
				List<String> data = list.get(i);
				num = data.size();
				if (num > maxNum) {
					maxNum = num;
				}
				for (int j = 0; j < num; j++) {
					xssfCell = xssfRow.createCell(j);
					xssfCell.setCellValue(data.get(j));
				}
			}
			if (isMergedHead) {
				CellRangeAddress cellRangeAddress = new CellRangeAddress(0, 0, 0, maxNum - 1);
				xssfSheet.addMergedRegion(cellRangeAddress);
			}
			for (int i = 0; i < maxNum; i++) {
				xssfSheet.setColumnWidth(i, 5000);
			}
			response.setContentType("application/x-xls");
			response.setHeader("Content-Disposition", "attachment; filename=batchmodel.xlsx");
			OutputStream outputStream = response.getOutputStream();
			xssfWorkbook.write(outputStream);
			outputStream.flush();
			outputStream.close();
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (Exception e) {
			logger.error("下载批量模板失败", e);
			throw new IllegalStateException("下载批量模板失败");
		}
	}
}