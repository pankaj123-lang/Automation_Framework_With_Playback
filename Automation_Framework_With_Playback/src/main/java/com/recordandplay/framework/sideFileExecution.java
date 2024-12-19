package com.recordandplay.framework;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.codoid.products.fillo.Connection;
import com.codoid.products.fillo.Fillo;
import com.codoid.products.fillo.Recordset;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.web.framework.Run_Automation;

public class sideFileExecution {

	private static String url;
	private static String appName;
	private static String comment;
	private static String action;
	private static String target;
	private static String dataValue;
	private static FileOutputStream fosSideFile;
	private static XSSFWorkbook workbookSideFile;
	private static XSSFSheet sheetSideFile;
	private static String locatorType = "";
	private static String locatorValue = "";
	private static File selectedFileName;
	private static String excelPathToGenerate;
	private static File selectedFile;
	private static boolean flagFileSelected;
	private static String fileName;
	private static String platformName;
	private static String appPackage;
	private static String appActivity;
	private static String platformVersion;
	private static String udid;
	private static String deviceName;
	private static String appiumUrl;

	public static void main(String[] args) {
//		getDataFromSideFile();
//		runSideFile();
		sideFileChooser();

	}

	public static void sideFileChooser() {
		// Create a JFileChooser instance
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Select a side file");

		// Show the file chooser dialog within a JOptionPane
		int userSelection = JOptionPane.showConfirmDialog(null, fileChooser, "Please Select a File",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

		// Handle the user's Action
		if (userSelection == JOptionPane.OK_OPTION) {
			selectedFile = fileChooser.getSelectedFile();
			if (selectedFile != null) {
				flagFileSelected = true;
				selectedFileName = selectedFile.getAbsoluteFile();
				fileName = selectedFile.getName();
				System.out.println("Selected File: " + selectedFileName);
			} else {
				System.out.println("No File Selected ");
			}
		} else {
			flagFileSelected = false;
			System.out.println("File Selection was canceled");
		}
	}

	public static void getDataFromSideFile() throws IOException {
		JsonParser parser = new JsonParser();
		try {
			JsonObject o = (JsonObject) parser.parse(new FileReader(selectedFileName));
			System.out.println(o);
			url = o.get("url").toString().replace("\"", "").trim();
			appName = o.get("name").toString().replace("\"", "").trim();
			System.out.println("URL: " + url);
			System.out.println("Application Name: " + appName);
			/// Generate Excel file with application name
			excelPathToGenerate = Run_Automation.getHomedir() + "/Resources/DataSheet/" + appName + ".xlsx";
			System.out.println(excelPathToGenerate);
			File excelFileName = new File(excelPathToGenerate);
			if (excelFileName.exists()) {
				System.out.println("File " + excelFileName + " is exist");
				JDialog.setDefaultLookAndFeelDecorated(true);
				Object[] options1 = { "Yes Delete", "Merge" };
				int result = JOptionPane.showOptionDialog(null,
						"File " + excelFileName + "is exist, Do you want to delete the file and process again ?",
						"Choose", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options1, null);
				if (result == JOptionPane.YES_OPTION) {
					excelFileName.delete();
				} else {
					FileInputStream fis = new FileInputStream(excelFileName);
					workbookSideFile = new XSSFWorkbook(fis);
					fosSideFile = new FileOutputStream(excelFileName);

					System.out.println("Cancelled");
//					System.exit(0);
				}

			}
			if (!excelFileName.exists()) {

				try {
					workbookSideFile = new XSSFWorkbook();
					Font headerFont = workbookSideFile.createFont();
					headerFont.setBold(true);
					headerFont.setColor(IndexedColors.BLACK.index);

					XSSFCellStyle style = workbookSideFile.createCellStyle();
					style.setFont(headerFont);
//					style.setFillBackgroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
					style.setFillPattern(FillPatternType.DIAMONDS);
//					style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
					style.setFillPattern(FillPatternType.FINE_DOTS);
					fosSideFile = new FileOutputStream(excelFileName);
					sheetSideFile = workbookSideFile.createSheet("Sheet1");
					excelFileName.createNewFile();
					String[] columnHeadings = { "SrNo", "Application_Name", "Module", "PageName", "RunStatus",
							"Control", "ObjectType", "Object", "Perform", "DataValues", "Options", "Scenario_Id",
							"Scenario_Description", "TestCase_Id", "TestCase_Description", "TestCase_type",
							"Step_Description" };
					Row headerRow = sheetSideFile.createRow(0);
					for (int j = 0; j < columnHeadings.length; j++) {
						Cell cell = headerRow.createCell(j);
						cell.setCellValue(columnHeadings[j]);
						cell.setCellStyle(style);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
			JsonArray jarr = (JsonArray) o.getAsJsonArray("tests");
			JsonObject jobj1 = (JsonObject) jarr.get(0);
			JsonArray commands = jobj1.getAsJsonArray("commands");
			System.out.println(commands);
			int k = 1;
			for (int i = 0; i < commands.size(); i++) {
				JsonObject jobj = (JsonObject) commands.get(i);
				System.out.println(jobj);
				comment = jobj.get("comment").toString().replace("\"", "").trim();
				action = jobj.get("command").toString().replace("\"", "").trim();
				target = jobj.get("target").toString().replace("\"", "").trim();
				if (target.contains("xpath") || target.contains("linkText") || target.contains("css")
						|| target.contains("id") || target.contains("name")) {
					String[] test = target.split("=");
					locatorType = test[0];
					if (test[1].contains("@")) {
						locatorValue = test[1] + "=" + test[2];
					} else {
						locatorValue = test[1];
					}
				}

				System.out.println("locatorType: " + locatorType);
				System.out.println("locatorValue: " + locatorValue);
				dataValue = jobj.get("value").toString().replace("\"", "").trim();
				System.out.println("Comment: " + comment);
				System.out.println("Actions: " + action);
//				System.out.println("Locator: " + target);
				System.out.println("DataValue: " + dataValue);
				if (action.equalsIgnoreCase("open")) {
					url = url.concat(target);
					System.out.println("URL: " + url);
				}
				JsonArray targets = (JsonArray) jobj.getAsJsonArray("targets");
				System.out.println(targets);
				if (action.equalsIgnoreCase("open")) {
					int p = 0;
					while (p < 2) {
						try {
							sheetSideFile = workbookSideFile.getSheet("Sheet1");
							int lastRow = sheetSideFile.getLastRowNum();
							XSSFRow row = sheetSideFile.createRow(lastRow + 1);

//						count++;
//						System.out.println("Count==" + lastRow);
							XSSFCell cell = row.createCell(0);// sr no
							cell.setCellValue(k);
							cell = row.createCell(1);// Application_Name
							cell.setCellValue(appName);
							cell = row.createCell(2);// Module
							cell.setCellValue(appName);
							cell = row.createCell(3);// page name
							cell.setCellValue(appName);
							cell = row.createCell(4);// run status
							cell.setCellValue("Y");
							cell = row.createCell(5);// control
							cell.setCellValue("C");
							cell = row.createCell(6);// object type
							cell.setCellValue(locatorType);
							cell = row.createCell(7);// Object
							cell.setCellValue(locatorValue);
							cell = row.createCell(8);// Perform
							if (p == 0) {
								cell.setCellValue("StartBrowser");
								cell = row.createCell(9);// data value
								cell.setCellValue("");
							} else {
								cell.setCellValue("BROWSEURL");
								cell = row.createCell(9);// data value
								cell.setCellValue(url);
							}

							cell = row.createCell(10);// options
							cell.setCellValue("");
							cell = row.createCell(11);// scenario id
							cell.setCellValue("SC_001");
							cell = row.createCell(12);// sc desc
							cell.setCellValue("SC_001");
							cell = row.createCell(13);// tc id
							cell.setCellValue("TC_001");
							cell = row.createCell(14);// tc desc
							cell.setCellValue("TC_001");
							cell = row.createCell(15);// tc type
							cell.setCellValue("Positive");
							cell = row.createCell(16);// step desc
							cell.setCellValue(comment);
							k++;
						} catch (Exception e) {
							e.printStackTrace();
						}
						p++;
					}
				} else if (action.equalsIgnoreCase("select")) {

					try {
						sheetSideFile = workbookSideFile.getSheet("Sheet1");
						int lastRow = sheetSideFile.getLastRowNum();
						XSSFRow row = sheetSideFile.createRow(lastRow + 1);

						XSSFCell cell = row.createCell(0);// sr no
						cell.setCellValue(k);
						cell = row.createCell(1);// Application_Name
						cell.setCellValue(appName);
						cell = row.createCell(2);// Module
						cell.setCellValue(appName);
						cell = row.createCell(3);// page name
						cell.setCellValue(appName);
						cell = row.createCell(4);// run status
						cell.setCellValue("Y");
						cell = row.createCell(5);// control
						cell.setCellValue("C");
						cell = row.createCell(6);// object type
						cell.setCellValue(locatorType);
						cell = row.createCell(7);// Object
						cell.setCellValue(locatorValue);
						cell = row.createCell(8);// Perform

						if (dataValue.contains("label")) {
							cell.setCellValue("SELECTVALUE");
							cell = row.createCell(9);// data value
							cell.setCellValue(dataValue.split("=")[1]);
						} else {
							cell.setCellValue(action);
							cell = row.createCell(9);// data value
							cell.setCellValue(dataValue);
						}

						cell = row.createCell(10);// options
						cell.setCellValue("");
						cell = row.createCell(11);// scenario id
						cell.setCellValue("SC_001");
						cell = row.createCell(12);// sc desc
						cell.setCellValue("SC_001");
						cell = row.createCell(13);// tc id
						cell.setCellValue("TC_001");
						cell = row.createCell(14);// tc desc
						cell.setCellValue("TC_001");
						cell = row.createCell(15);// tc type
						cell.setCellValue("Positive");
						cell = row.createCell(16);// step desc
						cell.setCellValue(comment);
						k++;
					} catch (Exception e) {
						e.printStackTrace();
					}

				} else {
					try {
						sheetSideFile = workbookSideFile.getSheet("Sheet1");
						int lastRow = sheetSideFile.getLastRowNum();
						XSSFRow row = sheetSideFile.createRow(lastRow + 1);
						XSSFCell cell = row.createCell(0);// sr no
						cell.setCellValue(k);
						cell = row.createCell(1);// Application_Name
						cell.setCellValue(appName);
						cell = row.createCell(2);// Module
						cell.setCellValue(appName);
						cell = row.createCell(3);// page name
						cell.setCellValue(appName);
						cell = row.createCell(4);// run status
						cell.setCellValue("Y");
						cell = row.createCell(5);// control
						cell.setCellValue("C");
						cell = row.createCell(6);// object type
						cell.setCellValue(locatorType);
						cell = row.createCell(7);// Object
						cell.setCellValue(locatorValue);
						cell = row.createCell(8);// Perform
						if (action.equalsIgnoreCase("type")) {
							cell.setCellValue("Sendkeys");
						} else {
							cell.setCellValue(action);
						}
						cell = row.createCell(9);// data value
						cell.setCellValue(dataValue);
						cell = row.createCell(10);// options
						cell.setCellValue("");
						cell = row.createCell(11);// scenario id
						cell.setCellValue("SC_001");
						cell = row.createCell(12);// sc desc
						cell.setCellValue("SC_001");
						cell = row.createCell(13);// tc id
						cell.setCellValue("TC_001");
						cell = row.createCell(14);// tc desc
						cell.setCellValue("TC_001");
						cell = row.createCell(15);// tc type
						cell.setCellValue("Positive");
						cell = row.createCell(16);// step desc
						cell.setCellValue(comment);
						k++;
					} catch (Exception e) {
						e.printStackTrace();
					}

				}

			}
		} catch (JsonIOException e) {
			e.printStackTrace();
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		try {
			workbookSideFile.write(fosSideFile);
			fosSideFile.close();
			workbookSideFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void writeDataToExcelSheet() throws IOException {
		getDataFromSideFile();
		generateMainController();

	}

	public static void writeDataToExcelSheetMobile() {
		generateDatasheetMbile();
		generateMainControllerMobile();

	}

	private static void generateMainControllerMobile() {

		String filePath = Run_Automation.getHomedir() + "/Resources/" + "Recorder_Controller_Mob.xlsx";
		String sheetNameMain = "MobileController";
		String sheetNameCap = "MobileCapabilities";

		try {
			File file = new File(filePath);
			XSSFWorkbook workbookMain;
			XSSFSheet sheet;
			XSSFSheet sheet2;

			FileOutputStream fosMain = null;

			if (file.exists()) {

				// Update existing Excel file
				FileInputStream fis = new FileInputStream(file);
				workbookMain = new XSSFWorkbook(fis);
				sheet = workbookMain.getSheet(sheetNameMain);
				if (sheet == null) {
					sheet = workbookMain.createSheet(sheetNameMain);
				}

				boolean isUpdated = false;

				// Check if the searchValue exists in the sheet
				for (Row row : sheet) {
					for (Cell cell : row) {
						if (cell.getStringCellValue().equals(appName + ".xlsx")) {
//	                        cell.setCellValue(newValue); // Update the value
							isUpdated = true;
							System.out.println("MobileController File up to date");
							break;
						}
					}
					if (isUpdated) {
						break;
					}
				}
				fosMain = new FileOutputStream(file);
				if (!isUpdated) {

					int lastRow = sheet.getLastRowNum();
					XSSFRow row = sheet.createRow(lastRow + 1);

					XSSFCell cell = row.createCell(0);// sr no
					cell.setCellValue("");
					cell = row.createCell(1);
					cell.setCellValue("Y");
					cell = row.createCell(2);
					cell.setCellValue(appName);
					cell = row.createCell(3);
					cell.setCellValue(appName + ".xlsx");
					cell = row.createCell(4);
					cell.setCellValue("10");
					cell = row.createCell(5);
					cell.setCellValue("10");
					cell = row.createCell(6);
					cell.setCellValue("N");
					cell = row.createCell(7);
					cell.setCellValue("N");
					cell = row.createCell(8);
					cell.setCellValue("N");
					cell = row.createCell(9);
					cell.setCellValue("N");
					cell = row.createCell(10);
					cell.setCellValue("");
					
				}
			} else {
				// Create new Excel file
				workbookMain = new XSSFWorkbook();
				sheet = workbookMain.createSheet(sheetNameMain);

				Font headerFont = workbookMain.createFont();
				headerFont.setBold(true);
				headerFont.setColor(IndexedColors.BLACK.index);

				XSSFCellStyle style = workbookMain.createCellStyle();
				style.setFont(headerFont);
				style.setFillPattern(FillPatternType.DIAMONDS);
				style.setFillPattern(FillPatternType.FINE_DOTS);
				fosMain = new FileOutputStream(file);

				String[] columnHeadings = { "SrNO", "RunStatus", "Application_Name", "DataSheet_Name",
						"MaxExplicitWait_Time", "MaxImplicitWait_Time", "DB_Connection", "Email", "SMS",
						"MultipleTestData", "TestDataId" };
				Row headerRow = sheet.createRow(0);
				for (int j = 0; j < columnHeadings.length; j++) {
					Cell cell = headerRow.createCell(j);
					cell.setCellValue(columnHeadings[j]);
					cell.setCellStyle(style);
				}

				int lastRow = sheet.getLastRowNum();
				XSSFRow row = sheet.createRow(lastRow + 1);

				XSSFCell cell = row.createCell(0);// sr no
				cell.setCellValue("");
				cell = row.createCell(1);
				cell.setCellValue("Y");
				cell = row.createCell(2);
				cell.setCellValue(appName);
				cell = row.createCell(3);
				cell.setCellValue(appName + ".xlsx");
				cell = row.createCell(4);
				cell.setCellValue("10");
				cell = row.createCell(5);
				cell.setCellValue("10");
				cell = row.createCell(6);
				cell.setCellValue("N");
				cell = row.createCell(7);
				cell.setCellValue("N");
				cell = row.createCell(8);
				cell.setCellValue("N");
				cell = row.createCell(9);
				cell.setCellValue("N");
				cell = row.createCell(10);
				cell.setCellValue("");
				
				sheet2 = workbookMain.createSheet(sheetNameCap);
				String[] columnHeadings2 = { "SrNO", "RunStatus", "Application_Name", "AppPackage",
						"AppActivity", "AppReset", "PlatformName", "PlatformVersion", "DeviceName",
						"Udid", "AppiumUrl", "ApplicationType", "AppPath" };
				Row headerRow2 = sheet2.createRow(0);
				for (int j = 0; j < columnHeadings2.length; j++) {
					Cell cell2 = headerRow2.createCell(j);
					cell2.setCellValue(columnHeadings2[j]);
					cell2.setCellStyle(style);
				}

				int lastRow2 = sheet2.getLastRowNum();
				XSSFRow row2 = sheet2.createRow(lastRow2 + 1);

				XSSFCell cell2 = row2.createCell(0);// sr no
				cell2.setCellValue("");
				cell2 = row2.createCell(1);
				cell2.setCellValue("Y");
				cell2 = row2.createCell(2);
				cell2.setCellValue(appName);
				cell2 = row2.createCell(3);//AppPackage
				cell2.setCellValue(appPackage);
				cell2 = row2.createCell(4);//AppActivity
				cell2.setCellValue(appActivity);
				cell2 = row2.createCell(5);//AppReset
				cell2.setCellValue("No");
				cell2 = row2.createCell(6);//PlatformName
				cell2.setCellValue(platformName);
				cell2 = row2.createCell(7);//PlatformVersion
				cell2.setCellValue(platformVersion);
				cell2 = row2.createCell(8);//DeviceName
				cell2.setCellValue(udid);
				cell2 = row2.createCell(9);//Udid
				cell2.setCellValue(udid);
				cell2 = row2.createCell(10);//AppiumUrl
				cell2.setCellValue(appiumUrl);
				cell2 = row2.createCell(11);//ApplicationType
				cell2.setCellValue("Native");
				cell2 = row2.createCell(12);
				cell2.setCellValue("");
				

			}

			// Write the updated workbook back to the file
			workbookMain.write(fosMain);

			// Close the resources
			fosMain.close();
			workbookMain.close();

			System.out.println("Excel file created/updated successfully!");

		} catch (IOException e) {
			e.printStackTrace();
		}
	

	}

	private static void generateDatasheetMbile() {
		try {
			appName = fileName.substring(0, fileName.lastIndexOf('.'));
			excelPathToGenerate = Run_Automation.getHomedir() + "/Resources/DataSheet/" + appName + ".xlsx";
			System.out.println(excelPathToGenerate);
			File excelFileName = new File(excelPathToGenerate);
			if (excelFileName.exists()) {
				System.out.println("File " + excelFileName + " is exist");
				JDialog.setDefaultLookAndFeelDecorated(true);
				Object[] options1 = { "Replace", "Merge" };
				int result = JOptionPane.showOptionDialog(null,
						"File " + excelFileName + " is exist, Please Choose.",
						"Choose", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options1, null);
				if (result == JOptionPane.YES_OPTION) {
					excelFileName.delete();
				} else {
					FileInputStream fis = new FileInputStream(excelFileName);
					workbookSideFile = new XSSFWorkbook(fis);
					fosSideFile = new FileOutputStream(excelFileName);

					System.out.println("Merge");
//					System.exit(0);
				}

			}
			if (!excelFileName.exists()) {

				try {
					workbookSideFile = new XSSFWorkbook();
					Font headerFont = workbookSideFile.createFont();
					headerFont.setBold(true);
					headerFont.setColor(IndexedColors.BLACK.index);

					XSSFCellStyle style = workbookSideFile.createCellStyle();
					style.setFont(headerFont);
					style.setFillPattern(FillPatternType.DIAMONDS);
					style.setFillPattern(FillPatternType.FINE_DOTS);
					fosSideFile = new FileOutputStream(excelFileName);
					sheetSideFile = workbookSideFile.createSheet("Sheet1");
					excelFileName.createNewFile();
					String[] columnHeadings = { "SrNo", "Application_Name", "Module", "PageName", "RunStatus",
							"Control", "ObjectType", "Object", "Perform", "DataValues", "Options", "Scenario_Id",
							"Scenario_Description", "TestCase_Id", "TestCase_Description", "TestCase_type",
							"Step_Description" };
					Row headerRow = sheetSideFile.createRow(0);
					for (int j = 0; j < columnHeadings.length; j++) {
						Cell cell = headerRow.createCell(j);
						cell.setCellValue(columnHeadings[j]);
						cell.setCellStyle(style);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			

			}
			Fillo fillo = new Fillo();
			String queryForDatasheet = "select * from Sheet1 where RunStatus='Y'";
			Connection conn = fillo.getConnection(String.valueOf(selectedFileName));
			Recordset rsDatasheet = conn.executeQuery(queryForDatasheet);
			int k = 1;
			while (rsDatasheet.next()) {
				String objectTypeDS = rsDatasheet.getField("ObjectType");
				String objectDS = rsDatasheet.getField("Object");
				String performDS = rsDatasheet.getField("Perform");
				String valuesDS = rsDatasheet.getField("DataValues");

				

					try {
						sheetSideFile = workbookSideFile.getSheet("Sheet1");
						int lastRow = sheetSideFile.getLastRowNum();
						XSSFRow row = sheetSideFile.createRow(lastRow + 1);
						XSSFCell cell = row.createCell(0);// sr no
						cell.setCellValue(k);
						cell = row.createCell(1);// Application_Name
						cell.setCellValue(appName);
						cell = row.createCell(2);// Module
						cell.setCellValue(appName);
						cell = row.createCell(3);// page name
						cell.setCellValue(appName);
						cell = row.createCell(4);// run status
						cell.setCellValue("Y");
						cell = row.createCell(5);// control
						cell.setCellValue("C");
						cell = row.createCell(6);// object type
						cell.setCellValue(objectTypeDS);
						cell = row.createCell(7);// Object
						cell.setCellValue(objectDS);
						cell = row.createCell(8);// Perform
						cell.setCellValue(performDS);
						cell = row.createCell(9);// data value
						cell.setCellValue(valuesDS);
						cell = row.createCell(10);// options
						cell.setCellValue("");
						cell = row.createCell(11);// scenario id
						cell.setCellValue("SC_001");
						cell = row.createCell(12);// sc desc
						cell.setCellValue("SC_001");
						cell = row.createCell(13);// tc id
						cell.setCellValue("TC_001");
						cell = row.createCell(14);// tc desc
						cell.setCellValue("TC_001");
						cell = row.createCell(15);// tc type
						cell.setCellValue("Positive");
						cell = row.createCell(16);// step desc
						cell.setCellValue("");
						k++;
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			String queryForCap = "select * from Capabilities";
			Recordset rsCap = conn.executeQuery(queryForCap);
			while (rsCap.next()) {
				 platformName = rsCap.getField("platformName");
				 appPackage = rsCap.getField("appium:appPackage");
				 appActivity = rsCap.getField("appium:appActivity");
				 platformVersion = rsCap.getField("appium:platformVersion");
				 udid = rsCap.getField("appium:deviceName");
				 deviceName = rsCap.getField("appium:deviceName");
				 appiumUrl = rsCap.getField("AppiumURL");
				 System.out.println(platformName);
				 System.out.println(appPackage);
				 System.out.println(appActivity);
				 System.out.println(platformVersion);
				 System.out.println(udid);
				 System.out.println(appiumUrl);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			workbookSideFile.write(fosSideFile);
			fosSideFile.close();
			workbookSideFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void generateMainController() {
		String filePath = Run_Automation.getHomedir() + "/Resources/" + "Recorder_Controller_Web.xlsx";
		String sheetNameMain = "Controller";

		try {
			File file = new File(filePath);
			XSSFWorkbook workbookMain;
			XSSFSheet sheet;

			FileOutputStream fosMain = null;

			if (file.exists()) {

				// Update existing Excel file
				FileInputStream fis = new FileInputStream(file);
				workbookMain = new XSSFWorkbook(fis);
				sheet = workbookMain.getSheet(sheetNameMain);
				if (sheet == null) {
					sheet = workbookMain.createSheet(sheetNameMain);
				}

				boolean isUpdated = false;

				// Check if the searchValue exists in the sheet
				for (Row row : sheet) {
					for (Cell cell : row) {
						if (cell.getStringCellValue().equals(appName + ".xlsx")) {
//	                        cell.setCellValue(newValue); // Update the value
							isUpdated = true;
							System.out.println("Controller File up to date");
							break;
						}
					}
					if (isUpdated) {
						break;
					}
				}
				fosMain = new FileOutputStream(file);
				if (!isUpdated) {

					int lastRow = sheet.getLastRowNum();
					XSSFRow row = sheet.createRow(lastRow + 1);

					XSSFCell cell = row.createCell(0);// sr no
					cell.setCellValue("");
					cell = row.createCell(1);
					cell.setCellValue("Y");
					cell = row.createCell(2);
					cell.setCellValue(appName);
					cell = row.createCell(3);
					cell.setCellValue(appName + ".xlsx");
					cell = row.createCell(4);
					cell.setCellValue("Chrome");
					cell = row.createCell(5);
					cell.setCellValue("10");
					cell = row.createCell(6);
					cell.setCellValue("10");
					cell = row.createCell(7);
					cell.setCellValue("N");
					cell = row.createCell(8);
					cell.setCellValue("N");
					cell = row.createCell(9);
					cell.setCellValue("N");
					cell = row.createCell(10);
					cell.setCellValue("N");
					cell = row.createCell(11);
					cell.setCellValue("");
				}
			} else {
				// Create new Excel file
				workbookMain = new XSSFWorkbook();
				sheet = workbookMain.createSheet(sheetNameMain);

				Font headerFont = workbookMain.createFont();
				headerFont.setBold(true);
				headerFont.setColor(IndexedColors.BLACK.index);

				XSSFCellStyle style = workbookMain.createCellStyle();
				style.setFont(headerFont);
				style.setFillPattern(FillPatternType.DIAMONDS);
				style.setFillPattern(FillPatternType.FINE_DOTS);
				fosMain = new FileOutputStream(file);

				String[] columnHeadings = { "SrNO", "RunStatus", "Application_Name", "DataSheet_Name", "Browser",
						"MaxExplicitWait_Time", "MaxImplicitWait_Time", "DB_Connection", "Email", "SMS",
						"MultipleTestData", "TestDataId" };
				Row headerRow = sheet.createRow(0);
				for (int j = 0; j < columnHeadings.length; j++) {
					Cell cell = headerRow.createCell(j);
					cell.setCellValue(columnHeadings[j]);
					cell.setCellStyle(style);
				}

				int lastRow = sheet.getLastRowNum();
				XSSFRow row = sheet.createRow(lastRow + 1);

				XSSFCell cell = row.createCell(0);// sr no
				cell.setCellValue("");
				cell = row.createCell(1);
				cell.setCellValue("Y");
				cell = row.createCell(2);
				cell.setCellValue(appName);
				cell = row.createCell(3);
				cell.setCellValue(appName + ".xlsx");
				cell = row.createCell(4);
				cell.setCellValue("Chrome");
				cell = row.createCell(5);
				cell.setCellValue("10");
				cell = row.createCell(6);
				cell.setCellValue("10");
				cell = row.createCell(7);
				cell.setCellValue("N");
				cell = row.createCell(8);
				cell.setCellValue("N");
				cell = row.createCell(9);
				cell.setCellValue("N");
				cell = row.createCell(10);
				cell.setCellValue("N");
				cell = row.createCell(11);
				cell.setCellValue("");

			}

			// Write the updated workbook back to the file
			workbookMain.write(fosMain);

			// Close the resources
			fosMain.close();
			workbookMain.close();

			System.out.println("Excel file created/updated successfully!");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runSideFile() {
		ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "selenium-side-runner " + selectedFileName);
		builder.redirectErrorStream(true);
		Process p = null;
		try {
			p = builder.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line = null;
		while (true) {
			try {
				line = r.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (line == null) {
				break;
			}
			System.out.println(line);
		}

	}

	public static boolean isFlagFileSelected() {
		return flagFileSelected;
	}

	public static void setFlagFileSelected(boolean flagFileSelected) {
		sideFileExecution.flagFileSelected = flagFileSelected;
	}

	public static String getUrl() {
		return url;
	}

	public static void setUrl(String url) {
		sideFileExecution.url = url;
	}

	public static String getAppName() {
		return appName;
	}

	public static void setAppName(String appName) {
		sideFileExecution.appName = appName;
	}

	public static String getComment() {
		return comment;
	}

	public static void setComment(String comment) {
		sideFileExecution.comment = comment;
	}

	public static String getAction() {
		return action;
	}

	public static void setAction(String action) {
		sideFileExecution.action = action;
	}

	public static String getTarget() {
		return target;
	}

	public static void setTarget(String target) {
		sideFileExecution.target = target;
	}

	public static String getDataValue() {
		return dataValue;
	}

	public static void setDataValue(String dataValue) {
		sideFileExecution.dataValue = dataValue;
	}

	public static FileOutputStream getFosSideFile() {
		return fosSideFile;
	}

	public static void setFosSideFile(FileOutputStream fosSideFile) {
		sideFileExecution.fosSideFile = fosSideFile;
	}

	public static XSSFWorkbook getWorkbookSideFile() {
		return workbookSideFile;
	}

	public static void setWorkbookSideFile(XSSFWorkbook workbookSideFile) {
		sideFileExecution.workbookSideFile = workbookSideFile;
	}

	public static XSSFSheet getSheetSideFile() {
		return sheetSideFile;
	}

	public static void setSheetSideFile(XSSFSheet sheetSideFile) {
		sideFileExecution.sheetSideFile = sheetSideFile;
	}

	public static String getLocatorType() {
		return locatorType;
	}

	public static void setLocatorType(String locatorType) {
		sideFileExecution.locatorType = locatorType;
	}

	public static String getLocatorValue() {
		return locatorValue;
	}

	public static void setLocatorValue(String locatorValue) {
		sideFileExecution.locatorValue = locatorValue;
	}

	public static File getSelectedFileName() {
		return selectedFileName;
	}

	public static void setSelectedFileName(File selectedFileName) {
		sideFileExecution.selectedFileName = selectedFileName;
	}

	public static String getExcelPathToGenerate() {
		return excelPathToGenerate;
	}

	public static void setExcelPathToGenerate(String excelPathToGenerate) {
		sideFileExecution.excelPathToGenerate = excelPathToGenerate;
	}

	public static File getSelectedFile() {
		return selectedFile;
	}

	public static void setSelectedFile(File selectedFile) {
		sideFileExecution.selectedFile = selectedFile;
	}

}
