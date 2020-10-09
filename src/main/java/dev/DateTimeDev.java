/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.jena.atlas.lib.DateTimeUtils;
import org.apache.jena.atlas.logging.LogCtl;
import org.apache.jena.riot.RIOT;

public class DateTimeDev {
    static {
        LogCtl.setLog4j2();
        RIOT.getContext().set(RIOT.symTurtleDirectiveStyle, "sparql");
    }

//    /**
//     * Prefer
//     * <pre>
//     * DateTimeUtils.nowsAsString(DateTimeFormatter.ofPattern(string));
//     * </pre>
//     * @deprecated Uses {@link FastDateFormat} which has a slightly different pattern
//     *     language. Prefer {@link #nowAsString(DateTimeFormatter)}.
//     *
//     */
//    @Deprecated
//    public static String nowAsString(String formatString) {
//        FastDateFormat df = FastDateFormat.getInstance(formatString) ;
//        return df.format(new Date()) ;
//    }

    public static void main(String...a) {

        System.out.println("todayAsString: "+todayAsString());
        System.out.println("todayAsXSDDateString: "+todayAsXSDDateString());

        //System.out.println("nowAsString:            "+DateTimeUtils.nowAsString());
        System.out.println("nowAsXSDDateTimeString: "+DateTimeUtils.nowAsXSDDateTimeString());
        System.out.println("calendarToXSDDateTimeString: "+
            DateTimeUtils.calendarToXSDDateTimeString(new GregorianCalendar()));

        GregorianCalendar gCal = new GregorianCalendar();
        gCal.toZonedDateTime();
        gCal.toZonedDateTime().withZoneSameInstant(ZoneOffset.UTC);
        gCal.toInstant();

        System.out.println();
        ZonedDateTime zdt = ZonedDateTime.now(ZoneOffset.UTC);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSxxx");
        String x = fmt.format(zdt);
        System.out.println(x);
        System.exit(0);
    }

    // Use xxx to get +00:00 format with DateTimeFormatter
    private static final DateTimeFormatter dateTimeFmt_display  = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss zz");
    private static final DateTimeFormatter dateFmt_yyyymmdd     = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter dateFmt_XSD          = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx");
    private static final DateTimeFormatter dateTimeFmt_XSD_ms0  = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx");
    private static final DateTimeFormatter dateTimeFmt_XSD_ms   = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSxxx");
    private static final DateTimeFormatter timeFmt_XSD_ms0      = DateTimeFormatter.ofPattern("HH:mm:ssxxx");
    private static final DateTimeFormatter timeFmt_XSD_ms       = DateTimeFormatter.ofPattern("HH:mm:ss.SSSxxx");

    // UTC
    /** Return "now" as an XSD dateTime string in UTC. */
    public static String nowUTC() {
        ZonedDateTime zdt = ZonedDateTime.now(ZoneOffset.UTC);
        DateTimeFormatter fmt = (milliSeconds(zdt) == 0 ) ? dateTimeFmt_XSD_ms0 : dateTimeFmt_XSD_ms;
        return fmt.format(zdt);
    }


    /** Return "now" as an XSD string in the current timezone.*/
    public static String nowAsXSDDateTimeString() {
        ZonedDateTime zdt = ZonedDateTime.now();
        DateTimeFormatter fmt = (milliSeconds(zdt) == 0 ) ? dateTimeFmt_XSD_ms0 : dateTimeFmt_XSD_ms;
        return fmt.format(zdt);
    }

    private static int milliSeconds(ZonedDateTime zdt) { return zdt.getNano()/1_000_000; }

    /**

    /** Return "now" as readable string (date in yyyy/MM/dd format) */
    public static String nowAsString() {
        return nowAsString(dateTimeFmt_display) ;
    }

    /*
     * @deprecated Use {@link #todayAsString}
     */
    @Deprecated
    public static String todayAsXSDDateString() {
        return todayAsString();
    }

    /** Return "today" as readable string (date in yyyy/MM/dd format) */
    public static String todayAsString() {
        ZonedDateTime zdt = ZonedDateTime.now();
        DateTimeFormatter fmt = dateFmt_yyyymmdd;
        return fmt.format(zdt);
    }


    /**
     * Prefer
     * <pre>
     * DateTimeUtils.nowsAsString(DateTimeFormatter.ofPattern(string));
     * </pre>
     * @deprecated This method uses {@link FastDateFormat} which has a slightly
     *     different pattern language.
     *     Prefer {@link #nowAsString(DateTimeFormatter)}.
     */
    @Deprecated
    public static String nowAsString(String formatString) {
        FastDateFormat df = FastDateFormat.getInstance(formatString) ;
        return df.format(new Date()) ;
    }

    /**
     * Return "now" as readable string (date in yyyy/MM/dd HH:mm:ss zz format)
     */
    public static String nowAsString(DateTimeFormatter dateFormat) {
        ZonedDateTime now = ZonedDateTime.now();
        return dateFormat.format(now);
    }

    private static boolean hasZeroMilliSeconds(Calendar cal) {
        return ! cal.isSet(Calendar.MILLISECOND) || cal.get(Calendar.MILLISECOND) == 0 ;
    }

    /**
     * Convert a {@link Calendar} to an XSD dates string.
     * This is in canonical form:
     * <ul>
     * <li>if milliseconds == 0, the fractional seconds in the string.
     * <li>0 hours offset timezone is "+00:00", not "Z".
     * </ul>
     */
    // Canonical form : if ms == 0, don't include in the string.
    public static String calendarToXSDDateTimeString(Calendar cal) {
        DateTimeFormatter fmt = hasZeroMilliSeconds(cal)
            ? dateTimeFmt_XSD_ms0
            : dateTimeFmt_XSD_ms ;
        return calendarToXSDString(cal, fmt) ;
    }

    /**
     * Convert a {@link Calendar} to an XSD dates string.
     * This is in canonical form: 0 hours offset timezone is "+00:00", not "Z".
     */
    public static String calendarToXSDDateString(Calendar cal) {
        String x = calendarToXSDString(cal, dateFmt_XSD) ;
        if ( x.endsWith("Z") )
            x = x.substring(0, x.length()-1)+"+00:00";
        return x;
    }

    /**
     * Convert a {@link Calendar} to an XSD time string.
     * This is in canonical form: if milliseconds == 0, the fractional seconds in the string.
     */
    public static String calendarToXSDTimeString(Calendar cal) {
        DateTimeFormatter fmt = hasZeroMilliSeconds(cal)
            ? timeFmt_XSD_ms0
            : timeFmt_XSD_ms ;
        return calendarToXSDString(cal, fmt) ;
    }

    private static String calendarToXSDString(Calendar cal, DateTimeFormatter fmt) {
        ZonedDateTime zdt = ((GregorianCalendar)cal).toZonedDateTime();
        String lex = fmt.format(zdt) ;
        return lex ;
    }

}
