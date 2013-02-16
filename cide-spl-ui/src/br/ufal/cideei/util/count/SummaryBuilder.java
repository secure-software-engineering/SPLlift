/*
 * This is a prototype implementation of the concept of Feature-Sen
 * sitive Dataflow Analysis. More details in the AOSD'12 paper:
 * Dataflow Analysis for Software Product Lines
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package br.ufal.cideei.util.count;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.format.CellFormatType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellReference;

public class SummaryBuilder {

	public static void main(String[] args) {
		try {
			buildSummary("bk");
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void buildSummary(String splShortName) throws InvalidFormatException, FileNotFoundException, IOException {

		// final String userHomeFolder = System.getProperty("user.home").substring(3);
		String userHomeFolder = "C:\\tst";
		final String output = userHomeFolder + File.separator + "summ.xls";
		File outputFile = new File(output);
		Workbook outputWorkbook;
		if (!outputFile.exists()) {
			outputFile.createNewFile();
			outputWorkbook = new HSSFWorkbook();
		} else {
			FileInputStream inputFileStream = new FileInputStream(outputFile);
			outputWorkbook = WorkbookFactory.create(inputFileStream);
		}

		{
			List<String> referencesForRDA3 = new ArrayList<String>();
			List<String> referencesForUVA3 = new ArrayList<String>();
			List<String> referencesForRDA2 = new ArrayList<String>();
			List<String> referencesForUVA2 = new ArrayList<String>();
			String fileName = "fs-" + splShortName + ".xls";
			String filePath = userHomeFolder + File.separator;
			String fullFileName = filePath + File.separator + "fs-" + splShortName + ".xls";
			Workbook workbook = WorkbookFactory.create(new FileInputStream(new File(fullFileName)));
			for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
				Sheet sheet = workbook.getSheetAt(i);
				Row headerRow = sheet.getRow(0);
				for (Cell cell : headerRow) {
					String stringCellValue = cell.getStringCellValue();
					if (stringCellValue.equals("rd")) {
						Row sumRow = sheet.getRow(sheet.getLastRowNum() - 1);
						Cell sumCell = sumRow.getCell(i);
						CellReference sumCellRef = new CellReference(sumCell);
						String cellRefForAnotherSheet = "\'" + filePath + "[" + fileName + "]Sheet" + i + "\'!" + sumCellRef.formatAsString();
						referencesForRDA2.add(cellRefForAnotherSheet);
					} else if (stringCellValue.equals("uv")) {
						Row sumRow = sheet.getRow(sheet.getLastRowNum() - 1);
						Cell sumCell = sumRow.getCell(i);
						CellReference sumCellRef = new CellReference(sumCell);
						String cellRefForAnotherSheet = "\'" + filePath + "[" + fileName + "]Sheet" + i + "\'!" + sumCellRef.formatAsString();
						referencesForUVA2.add(cellRefForAnotherSheet);
					} else if (stringCellValue.equals("rd (a3)")) {
						Row sumRow = sheet.getRow(sheet.getLastRowNum() - 1);
						Cell sumCell = sumRow.getCell(i);
						CellReference sumCellRef = new CellReference(sumCell);
						String cellRefForAnotherSheet = "\'" + filePath + "[" + fileName + "]Sheet" + i + "\'!" + sumCellRef.formatAsString();
						referencesForRDA3.add(cellRefForAnotherSheet);
					} else if (stringCellValue.equals("uv (a3)")) {
						Row sumRow = sheet.getRow(sheet.getLastRowNum() - 1);
						Cell sumCell = sumRow.getCell(i);
						CellReference sumCellRef = new CellReference(sumCell);
						String cellRefForAnotherSheet = "\'" + filePath + "[" + fileName + "]Sheet" + i + "\'!" + sumCellRef.formatAsString();
						referencesForUVA3.add(cellRefForAnotherSheet);
					}
				}
			}
			if (outputWorkbook.getSheet(splShortName) != null) {
				outputWorkbook.removeSheetAt(outputWorkbook.getSheetIndex(splShortName));
			}
			Sheet outputSheet = outputWorkbook.createSheet(splShortName);
			Row RDA2Row = outputSheet.createRow(0);
			RDA2Row.createCell(0).setCellValue("RD A2");
			for (int i = 0; i < referencesForRDA2.size(); i++) {
				Cell createdCell = RDA2Row.createCell(i + 1);
				System.out.println(referencesForRDA2.get(i));
				createdCell.setCellType(Cell.CELL_TYPE_FORMULA);
				createdCell.setCellValue(referencesForRDA2.get(i));
			}
			Row UVA2Row = outputSheet.createRow(1);
			UVA2Row.createCell(0).setCellValue("UV A2");
			for (int i = 0; i < referencesForUVA2.size(); i++) {
				Cell createdCell = UVA2Row.createCell(i + 1);
				createdCell.setCellFormula(referencesForUVA2.get(i));
			}
			Row RDA3Row = outputSheet.createRow(2);
			RDA3Row.createCell(0).setCellValue("RD A3");
			for (int i = 0; i < referencesForRDA3.size(); i++) {
				Cell createdCell = RDA3Row.createCell(i + 1);
				createdCell.setCellFormula(referencesForRDA3.get(i));
			}
			Row UVA3Row = outputSheet.createRow(3);
			UVA3Row.createCell(0).setCellValue("UV A3");
			for (int i = 0; i < referencesForUVA3.size(); i++) {
				Cell createdCell = UVA3Row.createCell(i + 1);
				createdCell.setCellFormula(referencesForUVA3.get(i));
			}
		}
		FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
		outputWorkbook.write(fileOutputStream);
		fileOutputStream.close();
	}
}
