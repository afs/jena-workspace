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

package xsd;

import java.math.BigDecimal;
import java.util.regex.Pattern;

import javax.xml.datatype.DatatypeConstants;

import org.apache.jena.sparql.expr.NodeValue;

public class Duration2 {
    // Canonical
    // Year-Month
    // Day-Time
//    long year;
//    int months;
//    long days;

    /*
    8.2 Comparison operators on durations
        8.2.1 op:yearMonthDuration-less-than
        8.2.2 op:yearMonthDuration-greater-than
        8.2.3 op:dayTimeDuration-less-than
        8.2.4 op:dayTimeDuration-greater-than
        8.2.5 op:duration-equal
    8.3 Component extraction functions on durations
        8.3.1 fn:years-from-duration
        8.3.2 fn:months-from-duration
        8.3.3 fn:days-from-duration
        8.3.4 fn:hours-from-duration
        8.3.5 fn:minutes-from-duration
        8.3.6 fn:seconds-from-duration
    8.4 Arithmetic operators on durations
        8.4.1 op:add-yearMonthDurations
        8.4.2 op:subtract-yearMonthDurations
        8.4.3 op:multiply-yearMonthDuration
        8.4.4 op:divide-yearMonthDuration
        8.4.5 op:divide-yearMonthDuration-by-yearMonthDuration
        8.4.6 op:add-dayTimeDurations
        8.4.7 op:subtract-dayTimeDurations
        8.4.8 op:multiply-dayTimeDuration
        8.4.9 op:divide-dayTimeDuration
        8.4.10 op:divide-dayTimeDuration-by-dayTimeDuration
    */

    // The (month-seconds) tuples for the value.
    boolean monthsSet;
    boolean secondsSet;
    long months;
    long seconds;
    // Non-null and including the integer part if there are fractional seconds.
    BigDecimal fracSeconds;
    private String duration;

    private static BigDecimal secondsMod = BigDecimal.valueOf(60);

    private static final Pattern durationPattern;
    private static final Pattern yearMonthDurationPattern;
    private static final Pattern dayTimeDurationPattern;
    private static final Pattern fractSecPattern;

    static {

        String durationPatternStr =
                "-?P( ( ( [0-9]+Y([0-9]+M)?([0-9]+D)?"
                        + "       | ([0-9]+M)([0-9]+D)?"
                        + "       | ([0-9]+D)"
                        + "       )"
                        + "       (T ( ([0-9]+H)([0-9]+M)?([0-9]+(\\.[0-9]+)?S)?"
                        + "          | ([0-9]+M)([0-9]+(\\.[0-9]+)?S)?"
                        + "          | ([0-9]+(\\.[0-9]+)?S)"
                        + "          )"
                        + "       )?"
                        + "    )"
                        + "  | (T ( ([0-9]+H)([0-9]+M)?([0-9]+(\\.[0-9]+)?S)?"
                        + "       | ([0-9]+M)([0-9]+(\\.[0-9]+)?S)?"
                        + "       | ([0-9]+(\\.[0-9]+)?S)"
                        + "       )"
                        + "    )"
                        + "  )";
        durationPattern = Pattern.compile(durationPatternStr.replace(" ", ""));
        yearMonthDurationPattern = Pattern.compile("-?P((([0-9]+Y)([0-9]+M)?)|([0-9]+M))");

        // duDayTimeFrag ::= (duDayFrag duTimeFrag?) | duTimeFrag
        String duTimeFrag =
                "(T (  ([0-9]+H)([0-9]+M)?([0-9]+(\\.[0-9]+)?S)?"
                        +"  | ([0-9]+M)([0-9]+(\\.[0-9]+)?S)?"
                        +"  | ([0-9]+(\\.[0-9]+)?S)"
                        +"  )"
                        +")";
        duTimeFrag = duTimeFrag.replace(" ", "");
        String dayTimePatternStr = "-?P((([0-9]+D)("+duTimeFrag+")?)|("+duTimeFrag+"))";
        dayTimeDurationPattern = Pattern.compile(dayTimePatternStr);

        // test whether there are fractional seconds.
        fractSecPattern = Pattern.compile(".*(\\.[0-9]+)?S$");
    }

    public Duration2(String lexicalRepresentation) {
        javax.xml.datatype.Duration dur = NodeValue.xmlDatatypeFactory.newDuration(lexicalRepresentation);
        this.duration = lexicalRepresentation;
        boolean hasFactional = fractSecPattern.matcher(lexicalRepresentation).matches();

        boolean monthsSet = false;
        boolean secondsSet = false;

        long m = -1;
        if ( dur.isSet(DatatypeConstants.YEARS) ) {
            monthsSet = true;
            m += 12*dur.getYears();
        }

        if ( dur.isSet(DatatypeConstants.MONTHS) ) {
            monthsSet = true;
            m += dur.getMonths();
        }


        long s = 0;
        if ( dur.isSet(DatatypeConstants.DAYS) ) {
            secondsSet = true;
            s += (24*3600)*dur.getDays();
        }

        if ( dur.isSet(DatatypeConstants.HOURS) ) {
            secondsSet = true;
            s += 3600*dur.getHours();
        }

        if ( dur.isSet(DatatypeConstants.MINUTES) ) {
            secondsSet = true;
            s += 60*dur.getMinutes();
        }

        if ( dur.isSet(DatatypeConstants.SECONDS) ) {
            secondsSet = true;
            s += dur.getSeconds();
        }

        this.monthsSet = monthsSet;
        this.months = monthsSet ? m : -1;
        this.secondsSet = secondsSet;
        this.seconds = secondsSet ? m : -1;
        this.fracSeconds = null;

        // Fractional.
    }

    // fn:*-from-duration

    public long yearsFromDuration() {
        if ( ! monthsSet )
            return 0;
        return (months / 12);
    }

    public int monthsFromDuration() {
        if ( ! monthsSet )
            return 0;
        return (int)(months % 12) ;
    }

    public int daysFromDuration() {
        if ( ! secondsSet )
            return 0;
        return (int)(seconds / 86400);
    }

    public int hoursFromDuration() {
        if ( ! secondsSet )
            return 0;
        return (int)(seconds % 86400) / 3600 ;
    }

    public int minutesFromDuration() {
        if ( ! secondsSet )
            return 0;
        return (int)(seconds % 3600) / 60;
    }

    public int intSecondsFromDuration() {
        return (int)(seconds % 60);
    }

    // Decimal
    public BigDecimal secondsFromDuration() {
        if ( ! secondsSet )
            return BigDecimal.ZERO;
        if ( fracSeconds == null )
            return BigDecimal.valueOf(seconds % 60);
        return fracSeconds.remainder(secondsMod);
    }
}
