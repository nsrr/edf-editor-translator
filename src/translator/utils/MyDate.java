package translator.utils;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyDate {
	
	public static String currentDateTime(){
		Date date = new Date();
		Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String currentDateTime = formatter.format(date);
		return currentDateTime;
	}

	public static String currentDate(){
		Date date = new Date();
		Format formatter = new SimpleDateFormat("yyyy-MM-dd");
		String currentDateTime = formatter.format(date);
		return currentDateTime;
	}

	public static String currentTime(){
		Date date = new Date();
		Format formatter = new SimpleDateFormat("HH-mm-ss");
		String currentDateTime = formatter.format(date);
		return currentDateTime;
	}
}
