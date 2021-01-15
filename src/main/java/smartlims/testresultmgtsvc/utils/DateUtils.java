package smartlims.testresultmgtsvc.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    // 将时间转为String
    public static String convertDate2String(Date Date, String pattern) {
        if (Date == null) {
            return null;
        }
        try {
            DateFormat dateFormat = new SimpleDateFormat(pattern);
            return dateFormat.format(Date);
        } catch (Exception e) {
            return null;
        }       
    }

    // String转为时间
    public static Date convertString2Date(String date, String pattern) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        try {
            java.util.Date utilDate = simpleDateFormat.parse(date);
            Date result = new Date(utilDate.getTime());
            return result;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 检查fromDate是否合法，如果不合法返回当前时间
    public static String convertFromDate2ValidDate(String fromDate) {
        Date dfromDate = convertString2Date(fromDate, "yyyy-MM-dd");
        Date date = new Date(System.currentTimeMillis());

        if (dfromDate == null) {
            try {
                // fromdate为前一个月
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.add(Calendar.MONTH, -1);
                Date date1 = (Date) calendar.getTime();
                return DateUtils.convertDate2String(date1, "yyyy-MM-dd");
            } catch (Exception e) {
                return DateUtils.convertDate2String(date, "yyyy-MM-dd");
            }
        } else {
            return DateUtils.convertDate2String(dfromDate, "yyyy-MM-dd");
        }
    }

    // 检查toDate是否合法，如果不合法返回
    public static String convertToDate2ValidDate(String toDate) {
        Date dtoDate = convertString2Date(toDate, "yyyy-MM-dd");
        Date date = new Date(System.currentTimeMillis());
        if (dtoDate == null) {
            return DateUtils.convertDate2String(date, "yyyy-MM-dd");
        } 
        return DateUtils.convertDate2String(dtoDate, "yyyy-MM-dd");
    }

    public static String substractDate(Integer subsDay) {
        String result = "";
        Date date = new Date(System.currentTimeMillis());
        if (subsDay != null) {
            try {
                // fromdate为前一个月
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.add(Calendar.DATE, subsDay);
                Date date1 = (Date) calendar.getTime();
                result =  DateUtils.convertDate2String(date1, "yyyy-MM-dd");
            } catch (Exception e) {
                result =  DateUtils.convertDate2String(date, "yyyy-MM-dd");
            }
        }
        return result;
    }
}