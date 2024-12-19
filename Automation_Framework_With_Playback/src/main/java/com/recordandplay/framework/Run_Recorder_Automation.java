package com.recordandplay.framework;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openqa.selenium.remote.DesiredCapabilities;

import com.codoid.products.exception.FilloException;
import com.codoid.products.fillo.Fillo;
import com.framework.main.DatabaseOperations;
import com.framework.main.ReadFromDatasheet;
import com.framework.main.RunFramework;
import com.framework.main.SaveResult;
import com.mobile.framework.AppCapabilities;
import com.mobile.framework.RunMobAutomation;
import com.web.framework.BrowserConfiguration;
import com.web.framework.Run_Automation;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;

public class Run_Recorder_Automation extends AppCapabilities{
	public static void getDataFromController() {
		Run_Automation.setStartTimeForLogs(new SimpleDateFormat("yyyy-MM-dd HH mm ss").format(new Date()));
		System.out.println("Inside Controller Recorder Web");
		String queryForController = "select * from Controller where RunStatus='Y'";
		Run_Automation.setHomedir(System.getProperty("user.dir"));
		Fillo fillo = new Fillo();
		try {
			Run_Automation
			.setCon(fillo.getConnection(Run_Automation.getHomedir() + "/Resources/Recorder_Controller_Web.xlsx"));
			Run_Automation.setRsController(Run_Automation.getCon().executeQuery("SELECT count(*) FROM Controller where RunStatus='Y'"));
			
			if (Run_Automation.getRsController() != null) {
				Run_Automation.setAppCount(Run_Automation.getRsController().getCount());
			}
			System.out.println("Number of Application to execute : " + Run_Automation.getAppCount());
			Run_Automation.setRsController(Run_Automation.getCon().executeQuery(queryForController));

			while (Run_Automation.getRsController().next()) {
				Run_Automation.setSrNO_Controller(Run_Automation.getRsController().getField("SrNO")) ;
				Run_Automation.setRunStatusController(Run_Automation.getRsController().getField("RunStatus"));
				Run_Automation.setApplication_Name(Run_Automation.getRsController().getField("Application_Name"));
				Run_Automation.setDataSheet_Name(Run_Automation.getRsController().getField("DataSheet_Name"));
				Run_Automation.setBrowser(Run_Automation.getRsController().getField("Browser"));
				Run_Automation.setMaxExplicitWait_Time(Long.parseLong(Run_Automation.getRsController().getField("MaxExplicitWait_Time")));
				Run_Automation.setMaxImplicitWait_Time(Long.parseLong(Run_Automation.getRsController().getField("MaxImplicitWait_Time")));
				Run_Automation.setdB_Connection(Run_Automation.getRsController().getField("DB_Connection"));
				Run_Automation.setEmail(Run_Automation.getRsController().getField("Email"));
				Run_Automation.setsMS(Run_Automation.getRsController().getField("SMS"));
				Run_Automation.setMultipleTestData(Run_Automation.getRsController().getField("MultipleTestData"));
				System.out.println("Application Name ------------> " + Run_Automation.getApplication_Name());
				if (Run_Automation.getEmail().equals("Y")) {
					Run_Automation.getDataFromEmailConfig();

				}
				if (Run_Automation.getdB_Connection().equals("Y")) {
					Run_Automation.getDataFromDBProperties();
					DatabaseOperations.RunDuration("started");
				}
				//////////////////////////// BROWSER CONFIGURATION/////////////////////////////
				BrowserConfiguration.configureBrowser();
				////////////////////////// RESULT FILE CREATION////////////////////////////////
				SaveResult.generateResultFolderAndFiles(Run_Automation.getApplication_Name());
				//////////////////////////// READ DATASHEET/////////////////////////////////////
				ReadFromDatasheet.getDataFromDataSheet();

				//////////////// Save result for Application //////////////////
				if (RunFramework.getAutomationType().equalsIgnoreCase("Playback")) {
					try {
						if (Run_Automation.getdB_Connection().equals("Y")) {
							Run_Automation.getDataFromDBProperties();
							DatabaseOperations.RunDuration("completed");
							DatabaseOperations.RunDuration("total");
						}
						SaveResult.getWorkbook().write(SaveResult.getFos());
						SaveResult.getExtentrpt().flush();
//						setSaveResult(true);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				Run_Automation.setLogCount(Run_Automation.getLogCount()+1);
			}

		} catch (Exception e) {
			System.out.println("Unable to Connect to Controller..........");
			e.printStackTrace();
		} finally {
			if (RunFramework.getAutomationType().equalsIgnoreCase("Monitoring")) {
				Run_Automation.getDriver().quit();
			} else {
				System.exit(0);
			}
		}
	}
	public static void getDataFromMobController() {

		Run_Automation.setStartTimeForLogs(new SimpleDateFormat("yyyy-MM-dd HH mm ss").format(new Date()));
		System.out.println("Inside Mobile Controller Recorder");
		String queryForController = "select * from MobileController where RunStatus='Y'";
		Run_Automation.setHomedir(System.getProperty("user.dir"));
		Fillo fillo = new Fillo();
		try {

			Run_Automation
					.setCon(fillo.getConnection(Run_Automation.getHomedir() + "/Resources/Recorder_Controller_Mob.xlsx"));
			RunMobAutomation.setRsControllerMobile(Run_Automation.getCon()
					.executeQuery("SELECT count(*) FROM MobileController where RunStatus='Y'"));

			if (RunMobAutomation.getRsControllerMobile() != null) {
				Run_Automation.setAppCount(RunMobAutomation.getRsControllerMobile().getCount());
			}
			System.out.println("Number of Application to execute : " + Run_Automation.getAppCount());
			RunMobAutomation.setRsControllerMobile(Run_Automation.getCon().executeQuery(queryForController));

			while (RunMobAutomation.getRsControllerMobile().next()) {
				RunMobAutomation.setSrNO_Controller(RunMobAutomation.getRsControllerMobile().getField("SrNO"));
				RunMobAutomation.setRunStatusController(RunMobAutomation.getRsControllerMobile().getField("RunStatus"));
				RunMobAutomation.setApplication_Name(RunMobAutomation.getRsControllerMobile().getField("Application_Name"));
				RunMobAutomation.setDataSheet_Name(RunMobAutomation.getRsControllerMobile().getField("DataSheet_Name"));
				RunMobAutomation.setMaxExplicitWait_Time(Long.parseLong(RunMobAutomation.getRsControllerMobile().getField("MaxExplicitWait_Time")));
				RunMobAutomation.setMaxImplicitWait_Time(Long.parseLong(RunMobAutomation.getRsControllerMobile().getField("MaxImplicitWait_Time")));
				String dB_Connection = RunMobAutomation.getRsControllerMobile().getField("DB_Connection");
				Run_Automation.setdB_Connection(dB_Connection);
				RunMobAutomation.setEmail(RunMobAutomation.getRsControllerMobile().getField("Email"));
				RunMobAutomation.setsMS(RunMobAutomation.getRsControllerMobile().getField("SMS"));
				Run_Automation.setMultipleTestData(RunMobAutomation.getRsControllerMobile().getField("MultipleTestData"));

				if (RunMobAutomation.getEmail().equals("Y")) {
					RunMobAutomation.getDataFromEmailConfigMobile();

				}
				if (dB_Connection.equals("Y")) {
					RunMobAutomation.getDataFromDBPropertiesMobile();
					DatabaseOperations.RunDuration("started");
				}
				//////////////////////////// APPLICATION
				//////////////////////////// CONFIGURATION/////////////////////////////
				DesiredCapabilities capability = AppCapabilities.GetApplicationCapabilitiesFromSheet();
				////////////////////////// RESULT FILE CREATION////////////////////////////////
				SaveResult.generateResultFolderAndFiles(RunMobAutomation.getApplication_Name());
				//////////////////////////// READ DATASHEET/////////////////////////////////////
				if (AppCapabilities.getPlatformName().equalsIgnoreCase("Android")) {
					try {
						RunMobAutomation.setDriver(new AndroidDriver(new URL(AppCapabilities.getAppiumUrl()), capability));
						RunMobAutomation.setAppStarted(1);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (AppCapabilities.getPlatformName().equalsIgnoreCase("IOS")) {
					try {
						RunMobAutomation.setDriver(new IOSDriver(new URL(AppCapabilities.getAppiumUrl()), capability));
						RunMobAutomation.setAppStarted(1);
					} catch (MalformedURLException e) {
						e.printStackTrace();
					}
				}
				ReadFromDatasheet.getDataFromDataSheet();
				//////////////// Save result for Application //////////////////
				if (RunFramework.getAutomationType().equalsIgnoreCase("Automation")) {
					try {
						if (dB_Connection.equals("Y")) {
							getDataFromDBPropertiesMobile();
							DatabaseOperations.RunDuration("completed");
							DatabaseOperations.RunDuration("total");
						}
						SaveResult.getWorkbook().write(SaveResult.getFos());
						SaveResult.getExtentrpt().flush();
//						setSaveResult(true);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				Run_Automation.setLogCount(Run_Automation.getLogCount() + 1);

			}

		} catch (FilloException e) {
			System.out.println("Unable to Connect to Controller..........");
			e.printStackTrace();
		} finally {
//			try {
//				SaveResult.getWorkbook().write(SaveResult.getFos());
//				SaveResult.getExtentrpt().flush();
//				Run_Automation.setSaveResult(true);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
			if (RunFramework.getAutomationType().equalsIgnoreCase("Monitoring")) {
				RunMobAutomation.getDriver().quit();
			} else {

				System.exit(0);
			}
		}

	}
}
