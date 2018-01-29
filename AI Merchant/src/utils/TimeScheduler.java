package utils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

public class TimeScheduler {

    public static long getNextOSBUpdateTime() {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date(System.currentTimeMillis()));

        if(calendar.get(Calendar.MINUTE) >= 30) {
            calendar.add(Calendar.HOUR, 1);
            calendar.set(Calendar.MINUTE, 1);
        } else {
            calendar.set(Calendar.MINUTE, 31);
        }

        calendar.set(Calendar.SECOND, new Random().nextInt(60));

        return calendar.getTimeInMillis();
    }
}
