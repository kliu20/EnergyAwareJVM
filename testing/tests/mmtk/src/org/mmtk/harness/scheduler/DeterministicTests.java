/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Eclipse Public License (EPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/eclipse-1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
 */

package org.mmtk.harness.scheduler;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mmtk.harness.Harness;

/**
 * Deterministic scheduler test cases, with maximum interleaving
 */
public class DeterministicTests {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    Harness.initOnce();
    Scheduler.setThreadModel(Scheduler.Model.DETERMINISTIC);
    Harness.policy.setValue("FIXED");
    Harness.yieldInterval.setValue(1);
    /* Must call this after switching scheduler */
    org.mmtk.harness.scheduler.Scheduler.initCollectors();
  }
  
  @AfterClass
  public static void tearDownAfterClass() {
    Scheduler.setThreadModel(Scheduler.Model.JAVA);
  }

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testOneThreadOneItem() {
    assertEquals(Arrays.asList("a"),
        new SchedulerTestCases().testOneThreadOneItem("a"));
  }

  @Test
  public void testOneThreadTwoItems() {
    assertEquals(Arrays.asList("a","b"),
        new SchedulerTestCases().testOneThreadTwoItems("a","b"));
  }

  @Test
  public void testTwoThreadsOneItem() {
    assertEquals(Arrays.asList("a","b"),
        new SchedulerTestCases().testTwoThreadsOneItem("a","b"));
  }

  @Test
  public void testTwoThreadsTwoItems() {
    assertEquals(Arrays.asList("a","c","b","d"),
        new SchedulerTestCases().testTwoThreadsTwoItems("a","b","c","d"));
  }
}
