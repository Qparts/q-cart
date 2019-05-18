package q.rest.cart.helper;

import java.text.SimpleDateFormat;
import java.util.*;

public class Helper {

    public static int getRandomInteger(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }



    public static Date getToDate(int month, int year) {
        Date to = new Date();
        Calendar cTo = new GregorianCalendar();
        if (month == 12) {
            cTo.set(year, 11, 31, 0, 0, 0);
        } else {
            cTo.set(year, month, 1, 0, 0, 0);
            cTo.set(Calendar.DAY_OF_MONTH, cTo.getActualMaximum(Calendar.DAY_OF_MONTH));
        }
        cTo.set(Calendar.HOUR_OF_DAY, 23);
        cTo.set(Calendar.MINUTE, 59);
        cTo.set(Calendar.SECOND, 59);
        cTo.set(Calendar.MILLISECOND, cTo.getActualMaximum(Calendar.MILLISECOND));
        to.setTime(cTo.getTimeInMillis());
        return to;
    }



    public static Date getFromDate(int month, int year) {
        Date from = new Date();
        if (month == 12) {
            Calendar cFrom = new GregorianCalendar();
            cFrom.set(year, 0, 1, 0, 0, 0);
            cFrom.set(Calendar.MILLISECOND, 0);
            from.setTime(cFrom.getTimeInMillis());
        } else {
            Calendar cFrom = new GregorianCalendar();
            cFrom.set(year, month, 1, 0, 0, 0);
            cFrom.set(Calendar.MILLISECOND, 0);
            from.setTime(cFrom.getTimeInMillis());
        }
        return from;
    }

    public static Integer paymentIntegerFormat(double am){
        return Double.valueOf(am * 100).intValue();
    }

    public static String getMoyaserSecurityHeader() {
        try {
            String input = AppConstants.MOYASAR_KEY;
            byte bytes[] ;
            bytes = input.getBytes("ISO-8859-1");
            String key= Base64.getEncoder().encodeToString(bytes);
            return "Basic " + key;
        } catch (Exception e) {
            return "";
        }
    }

    public static Date addSeconds(Date original, int seconds) {
        return new Date(original.getTime() + (1000L * seconds));
    }

    public static Date addMinutes(Date original, int minutes) {
        return new Date(original.getTime() + (1000L * 60 * minutes));
    }

    public String getDateFormat(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSX");
        return sdf.format(date);
    }

    public String getDateFormat(Date date, String pattern){
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }


}
