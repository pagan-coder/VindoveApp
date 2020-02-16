package cz.httpvindove.vindove.Functions;

import android.util.Log;

import java.util.Calendar;
import java.util.Date;

/**
 * Moon, sun calculations.
 */
public class MoonSun {
    /**
     * Get moon age.
     * @return double
     */
    public static double moonAge() {
        Calendar cal = Calendar.getInstance();
        // calculate
        return MoonAge(cal.get(Calendar.DAY_OF_MONTH),
                cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR));
    }

    public static int numberOfMoon() {
        int result = 0;
        // prepare
        Calendar cal = Calendar.getInstance();
        Equinox equinox = new Equinox(cal.get(Calendar.YEAR));
        Equinox.DateTime winterSolsticeNow = equinox.getDecemberSolstice();
        // calculate
        if ((cal.get(Calendar.DAY_OF_MONTH) < winterSolsticeNow.getDay()) ||
                ((new Date().getMonth() + 1) != winterSolsticeNow.getMonth())) {
            // check the transition between years
            double second = MoonAge(winterSolsticeNow.getDay(),
                    winterSolsticeNow.getMonth(), winterSolsticeNow.getYear());
            if ((second + (31 - winterSolsticeNow.getDay())) < (29.53059))
                result++;
            // compute
            equinox = new Equinox(cal.get(Calendar.YEAR) - 1);
            Equinox.DateTime winterSolstice = equinox.getDecemberSolstice();
            double first = MoonAge(winterSolstice.getDay(),
                    winterSolstice.getMonth(), winterSolstice.getYear());
            double nowDays = cal.get(Calendar.DAY_OF_YEAR) + (31 - winterSolstice.getDay()) + (int)first;
            //Log.e("AAAA", String.valueOf(nowDays));
            nowDays -= 29.53059;
            while (nowDays >= 0) {
                result++;
                nowDays -= 29.53059;
            }
            // last correction
            // nowDays += 29.53059;
            //if (nowDays < (30 - 29.53059)) // the very next "day"
            //    result--;
            //Log.e("AAAA", String.valueOf(nowDays));
        } else {
            double first = MoonAge(winterSolsticeNow.getDay(),
                    winterSolsticeNow.getMonth(), winterSolsticeNow.getYear());
            if ((first + (cal.get(Calendar.DAY_OF_MONTH) - winterSolsticeNow.getDay())) >= (29.53059))
                result = 13;
            else
                result = 1;
        }
        return result;
    }

    /**
     * Get equinox/solstice.
     * @return Equinox
     */
    public static Equinox equinoxSolstice() {
        Calendar cal = Calendar.getInstance();
        // calculate
        return new Equinox(cal.get(Calendar.YEAR));
    }

    private static int JulianDate(int d, int m, int y)
    {
        int mm, yy;
        int k1, k2, k3;
        int j;

        yy = y - (int)((12 - m) / 10);
        mm = m + 9;
        if (mm >= 12)
        {
            mm = mm - 12;
        }
        k1 = (int)(365.25 * (yy + 4712));
        k2 = (int)(30.6001 * mm + 0.5);
        k3 = (int)((int)((yy / 100) + 49) * 0.75) - 38;
        // 'j' for dates in Julian calendar:
        j = k1 + k2 + d + 59;
        if (j > 2299160)
        {
            // For Gregorian calendar:
            j = j - k3; // 'j' is the Julian date at 12h UT (Universal Time)
        }
        return j;
    }

    private static double MoonAge(int d, int m, int y)
    {
        int j = JulianDate(d, m, y);
        // Calculate the approximate phase of the moon
        double ip = (j + 4.867) / 29.53059;
        ip = ip - Math.floor(ip);
        double ag = 0.0;
        if( ip < 0.5)
            ag = ip * 29.53059 + 29.53059 / 2;
        else
            ag = ip * 29.53059 - 29.53059 / 2;
        // Moon's age in days
        ag = Math.floor(ag) + 1;
        return ag;
    }
}
