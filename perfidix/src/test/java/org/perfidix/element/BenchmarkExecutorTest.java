/*
 * Copyright 2008 Distributed Systems Group, University of Konstanz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Revision$
 * $Author$
 * $Date$
 *
 */
package org.perfidix.element;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.perfidix.annotation.AfterEachRun;
import org.perfidix.annotation.AfterLastRun;
import org.perfidix.annotation.BeforeEachRun;
import org.perfidix.annotation.BeforeFirstRun;
import org.perfidix.annotation.Bench;
import org.perfidix.annotation.SkipBench;
import org.perfidix.exceptions.PerfidixMethodCheckException;
import org.perfidix.exceptions.PerfidixMethodInvocationException;
import org.perfidix.meter.AbstractMeter;
import org.perfidix.meter.CountingMeter;
import org.perfidix.meter.Time;
import org.perfidix.meter.TimeMeter;
import org.perfidix.result.BenchmarkResult;
import org.perfidix.result.ClassResult;
import org.perfidix.result.MethodResult;

/**
 * Test case for the BenchmarkExecutor. Note that all classes used in this
 * testcase are not allowed to be internal classes because of the reflective
 * invocation. This is not working with encapsulated classes.
 * 
 * @author Sebastian Graf, University of Konstanz
 */
public class BenchmarkExecutorTest {

    private LinkedHashSet<AbstractMeter> meter;
    /** static int to check the beforefirstcounter */
    public static int once;
    /** static int to check the beforeeachcounter */
    public static int each;

    private BenchmarkResult res;

    /**
     * Simple SetUp.
     */
    @Before
    public void setUp() {
        res = new BenchmarkResult();
        meter = new LinkedHashSet<AbstractMeter>();
        meter.add(new TimeMeter(Time.MilliSeconds));
        meter.add(new CountingMeter());
        once = 0;
        each = 0;
        BenchmarkExecutor.initialize(meter, res);
    }

    /**
     * Simple tearDown.
     */
    @After
    public void tearDown() {
        meter.clear();
        meter = null;
        res = null;
    }

    /**
     * Test method for
     * {@link org.perfidix.element.BenchmarkExecutor#getExecutor(org.perfidix.element.BenchmarkElement)}
     * 
     * @throws Exception
     *             of any kind because of reflection
     */
    @Test
    public void testGetExecutor() throws Exception {
        final NormalTestClass getInstanceClass = new NormalTestClass();
        final Method meth = getInstanceClass.getClass().getMethod("bench");

        final BenchmarkMethod elem1 = new BenchmarkMethod(meth);
        final BenchmarkMethod elem2 = new BenchmarkMethod(meth);

        final BenchmarkExecutor exec1 =
                BenchmarkExecutor.getExecutor(new BenchmarkElement(elem1));
        final BenchmarkExecutor exec2 =
                BenchmarkExecutor.getExecutor(new BenchmarkElement(elem2));

        assertTrue(exec1 == exec2);
    }

    /**
     * Test method for
     * {@link org.perfidix.element.BenchmarkExecutor#executeBeforeMethods(java.lang.Object)}
     * 
     * @throws Exception
     *             of any kind because of reflection
     */
    @Test
    public void testExecuteBeforeMethods() throws Exception {

        final BeforeTestClass getClass = new BeforeTestClass();
        final Method meth = getClass.getClass().getMethod("bench");
        final Object objToExecute = getClass.getClass().newInstance();

        final BenchmarkMethod elem = new BenchmarkMethod(meth);

        final BenchmarkExecutor exec =
                BenchmarkExecutor.getExecutor(new BenchmarkElement(elem));

        exec.executeBeforeMethods(objToExecute);
        exec.executeBeforeMethods(objToExecute);

        assertEquals(1, once);
        assertEquals(2, each);

    }

    /**
     * Test method for
     * {@link org.perfidix.element.BenchmarkExecutor#executeBench(Object)} .
     * 
     * @throws Exception
     *             of any kind because reflection
     */
    @Test
    public void testExecuteBench() throws Exception {
        final NormalTestClass normalClass = new NormalTestClass();
        final Method meth = normalClass.getClass().getMethod("bench");

        final Object objToExecute = normalClass.getClass().newInstance();

        final BenchmarkMethod elem = new BenchmarkMethod(meth);

        final BenchmarkExecutor exec =
                BenchmarkExecutor.getExecutor(new BenchmarkElement(elem));
        exec.executeBench(objToExecute);

        assertEquals(1, each);
        assertEquals(meter, res.getRegisteredMeters());
        final Iterator<ClassResult> classResIter =
                res.getIncludedResults().iterator();
        final ClassResult classRes = classResIter.next();
        assertFalse(classResIter.hasNext());
        assertEquals(meter, classRes.getRegisteredMeters());
        assertEquals(objToExecute.getClass(), classRes.getRelatedElement());
        assertEquals(normalClass.getClass(), classRes.getRelatedElement());

        final Iterator<MethodResult> methResIter =
                classRes.getIncludedResults().iterator();
        final MethodResult methRes = methResIter.next();
        assertFalse(methResIter.hasNext());
        assertEquals(meter, methRes.getRegisteredMeters());
        assertEquals(meth, methRes.getRelatedElement());

    }

    /**
     * Test method for
     * {@link org.perfidix.element.BenchmarkExecutor#executeAfterMethods(java.lang.Object)}
     * 
     * @throws Exception
     *             of any kind because of reflection.
     */
    @Test
    public void testExecuteAfterMethods() throws Exception {
        final AfterTestClass getClass = new AfterTestClass();
        final Method meth = getClass.getClass().getMethod("bench");
        final Object objToExecute = getClass.getClass().newInstance();

        final BenchmarkMethod elem = new BenchmarkMethod(meth);

        final BenchmarkExecutor exec =
                BenchmarkExecutor.getExecutor(new BenchmarkElement(elem));

        exec.executeAfterMethods(objToExecute);
        exec.executeAfterMethods(objToExecute);

        assertEquals(1, once);
        assertEquals(2, each);
    }

    /**
     * Test method for
     * {@link org.perfidix.element.BenchmarkExecutor#checkMethod(Object, Method, Class)}
     * and
     * {@link org.perfidix.element.BenchmarkExecutor#invokeMethod(Object, Method, Class)}
     * 
     * @throws Exception
     *             of any kind because of reflection
     */
    @Test
    public void testCheckAndExecute() throws Exception {
        final Object falseObj = new Object();
        final Object correctObj = new CheckAndExecuteTestClass();

        assertEquals(2, correctObj.getClass().getDeclaredMethods().length);

        final Method correctMethod =
                correctObj.getClass().getMethod("correctMethod");
        final Method falseMethod =
                correctObj.getClass().getMethod("incorrectMethod");

        final PerfidixMethodCheckException e1 =
                BenchmarkExecutor.checkMethod(
                        falseObj, correctMethod, SkipBench.class);
        assertTrue(e1 != null);

        final PerfidixMethodCheckException e2 =
                BenchmarkExecutor.checkMethod(
                        correctObj, falseMethod, SkipBench.class);
        assertTrue(e2 != null);

        final PerfidixMethodCheckException e3 =
                BenchmarkExecutor.checkMethod(
                        correctObj, correctMethod, SkipBench.class);
        assertTrue(e3 == null);

        final PerfidixMethodInvocationException e4 =
                BenchmarkExecutor.invokeMethod(
                        correctObj, correctMethod, SkipBench.class);
        assertTrue(e4 == null);

        assertEquals(1, once);

    }
}

class CheckAndExecuteTestClass {

    public void correctMethod() {
        BenchmarkExecutorTest.once++;
    }

    public Object incorrectMethod() {
        return null;
    }

}

class NormalTestClass {

    @Bench
    public void bench() {
        BenchmarkExecutorTest.each++;
    }

}

class AfterTestClass {

    @Bench
    public void bench() {
    }

    @AfterLastRun
    public void afterLast() {
        BenchmarkExecutorTest.once++;
    }

    @AfterEachRun
    public void afterEach() {
        BenchmarkExecutorTest.each++;
    }

}

class BeforeTestClass {

    @Bench
    public void bench() {
    }

    @BeforeFirstRun
    public void beforeFirst() {
        BenchmarkExecutorTest.once++;
    }

    @BeforeEachRun
    public void beforeEach() {
        BenchmarkExecutorTest.each++;
    }

}
