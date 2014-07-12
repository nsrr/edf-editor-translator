package translator.utils;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The class to format date and time
 */
public class MyDate {
	
	/**
	 * Get current date and time in format "yyyy-MM-dd HH:mm:ss"
	 * @return return current date and time in string
	 */
	public static String currentDateTime() {
		Date date = new Date();
		Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String currentDateTime = formatter.format(date);
		return currentDateTime;
	}

	/**
	 * Get current date in format "yyyy-MM-dd" 
	 * @return current date in string
	 */
	public static String currentDate() {
		Date date = new Date();
		Format formatter = new SimpleDateFormat("yyyy-MM-dd");
		String currentDateTime = formatter.format(date);
		return currentDateTime;
	}

	/**
	 * Get current time in format "HH-mm-ss"
	 * @return current time in string
	 */
	public static String currentTime() {
		Date date = new Date();
		Format formatter = new SimpleDateFormat("HH-mm-ss");
		String currentDateTime = formatter.format(date);
		return currentDateTime;
	}
}
