
package com.baidu.rigel.biplatform.ac.util;

import org.junit.Test;

public class TimeRangeDetailTest {

    @Test
    public void testGetDays() {
        TimeRangeDetail detail = new TimeRangeDetail("20150207", "20150217");
        String[] days = detail.getDays();
        System.out.println(days.length);
        System.out.println(days[days.length - 1]);
    }

}

