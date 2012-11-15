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
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.bidimap.TreeBidiMap;
import org.apache.commons.collections.keyvalue.DefaultKeyValue;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellReference;

public class MetricsTable {

	private final int MAX_SIZE;

	MultiValueMap map = new MultiValueMap();

	/*
	 * Maps int -> String, where int is the column position and the String is the column name
	 */
	private TreeBidiMap columnMapping = new TreeBidiMap();
	private int columnCounter = 1;

	/*
	 * Created on the File passed as argument to the ctor.
	 */
	private Workbook workBook;

	/*
	 * Sheet created on the WorkBook.
	 */
	private Sheet sheet;

	/*
	 * Flag to keep track of where the columns headers cells were created.
	 */
	private boolean headersWerePrint = false;

	private int rowCount = 0;

	private final File output;

	public MetricsTable(int max, File output) throws FileNotFoundException, IOException, InvalidFormatException {
		MAX_SIZE = max;
		this.output = output;
		workBook = WorkbookFactory.create(new FileInputStream(output));
		this.sheet = workBook.createSheet();
	}

	public MetricsTable(File output) throws FileNotFoundException, IOException, InvalidFormatException {
		MAX_SIZE = Integer.MAX_VALUE;
		this.output = output;
		if (output.exists()) {
			workBook = WorkbookFactory.create(new FileInputStream(output));
		} else {
			workBook = new HSSFWorkbook();
		}
		this.sheet = workBook.createSheet();
	}

	public void setProperty(String method, String property, String value) {
		map.put(method, new DefaultKeyValue(property, value));
		if (map.size(method) == MAX_SIZE) {
			dumpEntry(method, map.getCollection(method));
			map.remove(method);
		}
		if (!columnMapping.containsValue(property)) {
			columnMapping.put(columnCounter, property);
			columnCounter++;
		}
	}

	public void setProperty(String method, String property, Double value) {
		map.put(method, new DefaultKeyValue(property, value));
		if (map.size(method) == MAX_SIZE) {
			dumpEntry(method, map.getCollection(method));
			map.remove(method);
		}
		if (!columnMapping.containsValue(property)) {
			columnMapping.put(columnCounter, property);
			columnCounter++;
		}
	}

	private void dumpEntry(String method, Collection<DefaultKeyValue> properties) {
		if (!headersWerePrint) {
			printHeaders();
			headersWerePrint = true;
		}
		Row entryRow = sheet.createRow(rowCount++);

		Cell methodSignatureCell = entryRow.createCell(0);
		methodSignatureCell.setCellValue(method);

		Iterator<DefaultKeyValue> iterator = properties.iterator();
		while (iterator.hasNext()) {
			DefaultKeyValue nextKeyVal = iterator.next();

			String property = (String) nextKeyVal.getKey();
			Object value = nextKeyVal.getValue();
			Integer columnIndex = (Integer) columnMapping.getKey(property);

			if (value instanceof Double) {
				Cell cell = entryRow.createCell(columnIndex);
				cell.setCellValue((Double) value);
				cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);

			} else {
				Cell cell = entryRow.createCell(columnIndex);
				cell.setCellValue((String) value);
				cell.setCellType(HSSFCell.CELL_TYPE_STRING);
			}

		}
	}

	private void printHeaders() {
		MapIterator columnMapIterator = columnMapping.mapIterator();
		Row headerRow = sheet.createRow(0);
		rowCount++;

		while (columnMapIterator.hasNext()) {
			Cell headerCell = headerRow.createCell((Integer) columnMapIterator.next());
			headerCell.setCellValue((String) columnMapIterator.getValue());
		}
	}

	private void printFooters() {
		int columns = columnMapping.size();
		Row firstRow = sheet.getRow(1);
		Row lastRow = sheet.getRow(rowCount - 1);

		Row sumFooterRow = sheet.createRow(rowCount++);
		Cell sumFooterLabelCell = sumFooterRow.createCell(0);
		sumFooterLabelCell.setCellValue("SUM");

		Row averageFooterRow = sheet.createRow(rowCount++);
		Cell averageFooterLabelCell = averageFooterRow.createCell(0);
		averageFooterLabelCell.setCellValue("AVERAGE");

		for (int index = 0; index <= columns; index++) {
			Cell cell = firstRow.getCell(index);
			if (cell == null) {
				cell = firstRow.createCell(index);
			}
			Cell sumFooterCell = sumFooterRow.createCell(index);
			Cell averageFooterCell = averageFooterRow.createCell(index);

			CellReference firstCell = new CellReference(firstRow.getCell(index));
			Cell lastRowCell = lastRow.getCell(index);
			if (lastRowCell == null) {
				lastRowCell = lastRow.createCell(index);
			}
			CellReference lastCell = new CellReference(lastRowCell);

			sumFooterCell.setCellFormula("SUM(" + firstCell.formatAsString() + ":" + lastCell.formatAsString() + ")");
			averageFooterCell.setCellFormula("AVERAGE(" + firstCell.formatAsString() + ":" + lastCell.formatAsString() + ")");
		}
	}

	public void dumpEntriesAndClose() throws IOException {
		dumpAllEntries();
		printFooters();
		int noOfColumns = columnMapping.keySet().size();
		for (int index = 0; index < noOfColumns; index++) {
			sheet.autoSizeColumn(index);
		}
		FileOutputStream outStream = new FileOutputStream(output);
		workBook.write(outStream);
		outStream.close();
	}

	private void dumpAllEntries() {
		TreeSet sortedKeySet = new TreeSet(map.keySet());
		for (Object object : sortedKeySet) {
			dumpEntry((String) object, map.getCollection(object));
		}
	}

}