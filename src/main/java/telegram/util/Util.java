package telegram.util;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.net.InetAddress;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class Util {
    public static Date getDate(){
        Date date = new Date();
        try{
            String timeServer = "0.pool.ntp.org";

            NTPUDPClient timeClient = new NTPUDPClient();
            InetAddress inetAddress = InetAddress.getByName(timeServer);
            TimeInfo timeInfo = timeClient.getTime(inetAddress);
            long time = timeInfo.getMessage().getReceiveTimeStamp().getTime();

            Calendar cal = new GregorianCalendar();
            cal.setTimeInMillis(time);
            date = cal.getTime();
        } catch(Exception e){
            e.printStackTrace();
        }
        return date;
    }
}
