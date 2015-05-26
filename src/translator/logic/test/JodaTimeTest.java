package translator.logic.test;


import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class JodaTimeTest {

  public static void main(String[] args) {
    // TODO Auto-generated method stub
    DateTime dt = new DateTime(2015, 5, 25, 4, 17, 00);
    System.out.println("Now:" + dt);
    DateTime plusPeriod = dt.plus(Period.seconds(60));
    System.out.println("After:" + plusPeriod);

    DateTimeFormatter fmt = DateTimeFormat.forPattern("YYYY-MM-dd'T'HH:mm:ss");
    String str = fmt.print(dt);
    System.out.println(str);
    
    String date = "20.04.15";
    DateTimeFormatter fmt3 = DateTimeFormat.forPattern("dd.MM.yy");
    DateTime dateTime = fmt3.parseDateTime(date);
    System.out.println("Date Time: " + dateTime);
    
    String time = "11:23:59";
    DateTimeFormatter fmt4 = DateTimeFormat.forPattern("dd.MM.yy HH:mm:ss");
    DateTime dateTime2 = fmt4.parseDateTime(date + " " + time);
    System.out.println(dateTime2);
  }

}
