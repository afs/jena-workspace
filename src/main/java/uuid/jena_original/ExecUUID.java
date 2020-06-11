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

package uuid.jena_original;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import org.apache.jena.atlas.io.IO;

// Rewrite : extract V1 generator as standalone, makign a java.util.UUID.

public class ExecUUID {

 public static void main(String ...a) {
        String x = "51c8015c-7f63-11ea-afc6-874a623ce2f5" ;

        JenaUUID.setFactory(new UUID_V4_Gen());
        JenaUUID ju = JenaUUID.generate();

        examine("JenaUUID", ju.toString());
        if ( ju instanceof UUID_V1 ) {
            examine1((UUID_V1)ju);
            System.out.println();
        }
        JenaUUID j = JenaUUID.parse(x);
        examine("JenaUUID from base", j.toString());
        System.out.println();
    }

    static void examine(String label, String x) {
        System.out.println("Examine: "+label);
        exec("uuid", "-d", x);
        System.out.println(x);
        UUID u = UUID.fromString(x);
        System.out.printf("M:%016x\n", u.getMostSignificantBits());
        System.out.printf("L:%016x\n", u.getLeastSignificantBits());
        if ( u.version() == 1 ) {
            int time14 = u.clockSequence();
            System.out.printf("%08x\n", time14);
        }
    }

    //Most Significant Byte first (known as network byte order).
    /*
    Version 1:
    60 bits of time
      32 bits time low
      16 bits time mid
      4 bits version
      14 bits time high

    2  bits variant
    12 bits of clock sequence
    48 bits of nodeId

    Bit numbering is high-end/low-end which is the opposite to Java access.

       0                   1                   2                   3
       0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |                          time_low                             |   8 hex digits
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |       time_mid                |         time_hi_and_version   |   4-4
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |clk_seq_hi_res |  clk_seq_low  |         node (0-1)            |   4-
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
      |                         node (2-5)                            |   12
      +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
    */
    private static void examine1(UUID_V1 uuid) {
        String s =
//            p("ver:%01x",uuid.getVersion())+"-"+
//            p("var:%01x",uuid.getVariant())+"-"+
            p("timeLow(%08x)", uuid.getTimeLow()) + "-"+
            p("timeMid(%04x)",uuid.getTimeMid()) + "-"+
            p("ver(%01x)",uuid.getVersion())+"-"+
            p("timeHigh(%03x)",uuid.getTimeHigh()) + "\n"+
            p("var:%01x",uuid.getVariant())+"-"+
            p("seq(%04x)", uuid.getClockSequence())+"-"+ // 14 bits, hex
            p("seq(%d)", uuid.getClockSequence())+"-"+   // 14 bits, decimal
            p("node(%012x)", uuid.getNode());
        System.out.println(s);
    }



    private static String p(String fmt, long value) {
        return String.format(fmt, value);
    }

    static void exec(String... args) {
        try {
            Process process = Runtime.getRuntime().exec(args);
            String s = IO.readWholeFileAsUTF8(process.getInputStream());
            System.out.println(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static Object lock = new Object();
    private static void setTime() {
        long lastTime = 0;          // Microseconds
        long DELAY_ms = 10;         // Milliseconds
        long gregorianTime = 0;     // 100ns units since the gergorian epoch.

        long time = 0 ;

        // Wait for a clock tick.
        synchronized (lock) {
            if ( lastTime == 0 )
                lastTime = getCurrentMicros();

            boolean done = false ;
            while (!done) {
                time = getCurrentMicros();
                // Time gap converted to milliseconds.
                if ( (time - lastTime)/1000 < DELAY_ms ) {
                    // pause for a while to wait for time to change
                    // XXX better way Java9+?
                    try {
                        Thread.sleep(DELAY_ms) ;
                    } catch (java.lang.InterruptedException e) {} // ignore exception
                    continue ;
                } else {
                    done = true ;
                }
            }
        }

        // Java8
        // Milli to micro conversion.
        time = time*1000;
        lastTime = time ;

//        uuids_this_tick = 0 ;
//        uuid_time = ((unsigned64)tp.tv_sec * 10000000)
//            + ((unsigned64)tp.tv_usec * 10)
//            + I64(0x01B21DD213814000);

        // Convert to the UUID base time (00:00:00.00, 15 October 1582)
        // That's the date of the Gregorian calendar reforms
        // See the text quoted for the number.
        // Java base time is is January 1, 1970.

        gregorianTime = time * 10 + 0x01B21DD213814000L ;

        System.out.printf("%016X\n", gregorianTime);
        System.out.printf("%016X\n", 0x01B21DD213814000L);
    }

    static long getCurrentMicros() {
        // Java 8
        //return System.currentTimeMillis()*1000;
        // Java 9
        Instant microNow = Instant.now();
        long seconds = microNow.getEpochSecond();
        int nano = microNow.getNano(); // Low part 000
        int micro = nano/1000;
        return 1_000_000 * seconds + micro;

        //Java9 - tidy.
//        Instant microNow = Instant.now().truncatedTo(ChronoUnit.MICROS);
//      long seconds = microNow.getEpochSecond();
//      int nano = microNow.getNano(); // Low part 000
//      //System.out.printf("MicroNano: %d\n",nano);
//      int micro = nano/1000;
//

    }
}
