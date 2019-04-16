package com.apmats.rentflix.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestRecencyType {

    @Test
    public void chargeWhenImmediatelyReturningIsInitialCharge() {
        for (RecencyType recencyType: RecencyType.values()) {
            assertTrue("Expected surcharge to be 0 when film is returned the same day it was rented", recencyType.calculateSurcharge(0) == 0L);
        }
    }

    @Test
    public void surchargeForNegativeNumberOfTotalDaysThrows() {
        Integer expectedExceptions =  RecencyType.values().length;
        Integer raisedExceptions = 0;
        for (RecencyType recencyType : RecencyType.values()) {
            try {
                recencyType.calculateSurcharge(-1);
            } catch (Exception ex) {
                raisedExceptions++;
            }
        }
        assertEquals("Expected each recency type to raise an exception when asked to calculate" +
        "a surcharge for a negative number of days", expectedExceptions, raisedExceptions);
    }
}