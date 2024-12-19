package com.framework.main;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.api.framework.ApiSaveResult;
import com.api.framework.RunApiAutomation;
import com.mobile.framework.RunMobAutomation;
import com.recordandplay.framework.Run_Recorder_Automation;
import com.recordandplay.framework.sideFileExecution;
import com.web.framework.Run_Automation;

public class RunFramework {

	private static String automationType;
	private static String exit;
	private static String playbackType;

	public static void main(String[] args) throws IOException {

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				if (exit.equalsIgnoreCase("false")) {

					if (automationType.equalsIgnoreCase("Automation")) {
						if (Run_Automation.getLogCount() != Run_Automation.getAppCount()) {
							try {
								SaveResult.getWorkbook().write(SaveResult.getFos());
								SaveResult.getExtentrpt().flush();
							} catch (IOException e) {
								e.printStackTrace();
							}

							if (Run_Automation.getdB_Connection().equalsIgnoreCase("Y")) {

								String Query = "SELECT * FROM Run_history where RUN_ID="
										+ DatabaseOperations.getRun_Id() + "";
								ResultSet rs = null;
								Connection con = null;
								Statement stmt = null;
								String status = null;
								try {
									Class.forName(Run_Automation.getDbDriver());
									con = DriverManager.getConnection(Run_Automation.getDbUrl(),
											Run_Automation.getdBUserName(), Run_Automation.getdBPassword());
									stmt = con.createStatement();
									rs = stmt.executeQuery(Query);
									if (rs.next()) {
										status = rs.getString("Status");
									}
								} catch (Exception e2) {
									e2.printStackTrace();
								}
								System.out.println("Status================" + status);
								if (status.equalsIgnoreCase("RUNNING")) {
									System.out.println("=================Terminated===============");
									try {
										String query = "SELECT count(*) FROM result_logs where RUN_ID='"
												+ DatabaseOperations.getRun_Id() + "' and Application_Name='"
												+ Run_Automation.getApplication_Name() + "'";
										ResultSet RS = stmt.executeQuery(query);
										if (RS != null) {
											while (RS.next()) {
												int count = RS.getInt("count(*)");
												System.out.println(count);
											}
										}
										String statuss = "Terminated Abnormally";
										SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
										Date formatter2 = new Date();
										String stmt2 = "update Run_history set End_Time='"
												+ formatter.format(formatter2) + "',Status='" + statuss
												+ "' where RUN_ID=" + DatabaseOperations.getRun_Id()
												+ " and Application_Name='" + Run_Automation.getApplication_Name()
												+ "' ";
										System.out.println(stmt2);
										stmt.executeUpdate(stmt2);
										String Query2 = "SELECT * FROM Run_history where RUN_ID="
												+ DatabaseOperations.getRun_Id() + "";
										ResultSet rs2 = stmt.executeQuery(Query2);
										System.out.println(Query2);
										if (rs2 != null && rs2.next()) {
											String START_TIME = rs2.getString("Start_Time");
											String END_TIME = rs2.getString("End_Time");
											String time1 = START_TIME;
											String time2 = END_TIME;
											SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
											Date date1 = format.parse(time1);
											Date date2 = format.parse(time2);
											long difference = date2.getTime() - date1.getTime();
											long secondsInMilli = 1000L;
											long minutesInMilli = secondsInMilli * 60L;
											long hoursInMilli = minutesInMilli * 60L;
											long elapsedHours = difference / hoursInMilli;
											difference %= hoursInMilli;
											final long elapsedMinutes = difference / minutesInMilli;
											difference %= minutesInMilli;
											long elapsedSeconds = difference / secondsInMilli;
											String Total_Time = elapsedHours + "hr:" + elapsedMinutes + "min:"
													+ elapsedSeconds + "Sec";
											String datequery = "update Run_history set Execution_Time='" + Total_Time
													+ "' where RUN_ID=" + DatabaseOperations.getRun_Id()
													+ " and Application_Name='" + Run_Automation.getApplication_Name()
													+ "' ";
											System.out.println(datequery);
											stmt.executeUpdate(datequery);
											System.out.println(
													"=============================Completed====================================");
										}
									} catch (Exception e3) {
										e3.printStackTrace();
									}
								}
								try {
									if (con != null) {
										stmt.close();
										con.close();
									}
								} catch (Exception e3) {
									e3.printStackTrace();
								}
							}

						}
						// api save result
						if (ReadDataFromConfigFile.getAutomationType().equalsIgnoreCase("API")) {
							if (RunApiAutomation.isSaveResult() == false) {
								try {
									ApiSaveResult.getWorkbook().write(ApiSaveResult.getFos());
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}

					}

				}
			}
		});
		Run_Automation.setHomedir(System.getProperty("user.dir"));
		Date dt = new Date();
		System.out.println(dt);
		SimpleDateFormat smdt = new SimpleDateFormat("dd/MM/yyyy");
		String sDate1 = "30/12/2024";
		Date date1 = null;
		try {
			date1 = smdt.parse(sDate1);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if (dt.before(date1)) {
			System.out.println("Your License Validity is till " + sDate1);
			exit = "false";
		} else {
			exit = "true";
			JFrame parent = new JFrame();
			JOptionPane.showMessageDialog(parent, "Your License is expired, please contact Automation team");
			System.exit(1);
		}

		// Read config file
		ReadDataFromConfigFile.readFromConfigFile();
		boolean argsPresent = false;
		try {
			if (args[0] != null && args[1] != null) {
				argsPresent = true;
			}
		} catch (Exception e) {
			System.out.println("No Argument Found");
		}
		if (argsPresent) {
			System.out.println(args[0]);
			automationType = args[0];
		} else {
			JDialog.setDefaultLookAndFeelDecorated(true);
			Object[] options2 = { "Automation", "Monitoring", "Playback" };
			int result2 = JOptionPane.showOptionDialog(null, "Please choose automation type", "Choose",
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options2, null);

			if (result2 == JOptionPane.YES_OPTION) {
				automationType = "Automation";
			} else if (result2 == JOptionPane.NO_OPTION) {
				automationType = "Monitoring";
			} else {
				automationType = "Playback";
			}
		}
		int result;
		if (argsPresent) {
			System.out.println(args[1]);
			ReadDataFromConfigFile.setAutomationType(args[1]);
		} else {
			if (automationType.equalsIgnoreCase("Playback")) {
				JDialog.setDefaultLookAndFeelDecorated(true);
				Object[] options1 = { "Web", "Mobile" };
				result = JOptionPane.showOptionDialog(null, "Please choose automation type", "Choose",
						JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options1, null);
				if (result == JOptionPane.YES_OPTION) {
					ReadDataFromConfigFile.setAutomationType("Web");
				} else if (result == JOptionPane.NO_OPTION) {
					ReadDataFromConfigFile.setAutomationType("Mobile");
				} else {
					Run_Automation.setSaveResult(true);
				}
			} else {
				JDialog.setDefaultLookAndFeelDecorated(true);
				Object[] options1 = { "Web", "Mobile", "API" };
				result = JOptionPane.showOptionDialog(null, "Please choose automation type", "Choose",
						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options1, null);
				if (result == JOptionPane.YES_OPTION) {
					ReadDataFromConfigFile.setAutomationType("Web");
				} else if (result == JOptionPane.NO_OPTION) {
					ReadDataFromConfigFile.setAutomationType("Mobile");
				} else if (result == JOptionPane.CANCEL_OPTION) {
					ReadDataFromConfigFile.setAutomationType("API");
				} else {
					Run_Automation.setSaveResult(true);
				}
			}
		}
		if (ReadDataFromConfigFile.getAutomationType().equalsIgnoreCase("Web")) {

			if (automationType.equalsIgnoreCase("Playback")) {
				JDialog.setDefaultLookAndFeelDecorated(true);
				Object[] options1 = { "Process File", "Run Recorded File", "Run Modified" };
				result = JOptionPane.showOptionDialog(null, "Please choose automation type", "Choose",
						JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options1, null);
				if (result == JOptionPane.YES_OPTION) {
					playbackType = "Process File"; // Process json data and save to an excel file for further
													// modifications
				} else if (result == JOptionPane.NO_OPTION) {
					playbackType = "Run Recorded File"; // Run side file directly
				} else if (result == JOptionPane.CANCEL_OPTION) {
					playbackType = "Run Modified"; // Process json data and save to an excel file for further
													// modifications and run side file
				} else {
					Run_Automation.setSaveResult(true);
					playbackType = "Please select Automation type";
				}
				if (playbackType.equalsIgnoreCase("Process File")) {
					sideFileExecution.sideFileChooser();
					if (sideFileExecution.isFlagFileSelected() != false) {
						sideFileExecution.writeDataToExcelSheet();
					}
				} else if (playbackType.equalsIgnoreCase("Run Recorded File")) {
					sideFileExecution.sideFileChooser();
					if (sideFileExecution.getSelectedFile().toString() != null) {
						sideFileExecution.runSideFile();
					}
				} else if (playbackType.equalsIgnoreCase("Run Modified")) {
//					sideFileExecution.writeDataToExcelSheet();
//					sideFileExecution.runSideFile();
					Run_Recorder_Automation.getDataFromController();

				} else {
					Run_Automation.setSaveResult(true);
				}
			} else {
				if (automationType.equalsIgnoreCase("Monitoring")) {
					int i = 0;
					while (i <= 3) {
						Run_Automation.getDataFromController();
						i++;
					}
				} else {
					Run_Automation.getDataFromController();
				}
			}

		} else if (ReadDataFromConfigFile.getAutomationType().equalsIgnoreCase("Mobile")) {
			if (automationType.equalsIgnoreCase("Playback")) {
				JDialog.setDefaultLookAndFeelDecorated(true);
				Object[] options1 = { "Process File",  "Run Modified" };
				result = JOptionPane.showOptionDialog(null, "Please choose automation type", "Choose",
						JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, options1, null);
				if (result == JOptionPane.YES_OPTION) {
					playbackType = "Process File"; // Process json data and save to an excel file for further
													// modifications
				}else if (result == JOptionPane.NO_OPTION) {
					playbackType = "Run Modified"; // Process excel data and save to an excel file for further
													// modifications and run excel file using framework
				} else {
					Run_Automation.setSaveResult(true);
					playbackType = "Please select Automation type";
				}
				if (playbackType.equalsIgnoreCase("Process File")) {
					sideFileExecution.sideFileChooser();
					if (sideFileExecution.isFlagFileSelected() != false) {
						sideFileExecution.writeDataToExcelSheetMobile();
					}
				}  else if (playbackType.equalsIgnoreCase("Run Modified")) {
					System.out.println(playbackType);
					Run_Recorder_Automation.getDataFromMobController();

				} else {
					Run_Automation.setSaveResult(true);
				}
			} else {

				if (automationType.equalsIgnoreCase("Monitoring")) {
					int i = 0;
					while (i <= 3) {

						RunMobAutomation.getDataFromMobController();
						i++;
					}
				} else {
					RunMobAutomation.getDataFromMobController();
				}
			}

		} else if (ReadDataFromConfigFile.getAutomationType().equalsIgnoreCase("API")) {
			System.out.println("Running Api autmation");

			if (automationType.equalsIgnoreCase("Monitoring")) {
				int i = 0;
				while (i <= 3) {
					RunApiAutomation.getDataFromApiDatasheet();
					i++;
				}
			} else {
				RunApiAutomation.getDataFromApiDatasheet();
			}

		} else {
			Run_Automation.setSaveResult(true);
			System.out.println("Please choose Automation type");
		}

	}

	public static String getAutomationType() {
		return automationType;
	}

	public static void setAutomationType(String automationType) {
		RunFramework.automationType = automationType;
	}

}
