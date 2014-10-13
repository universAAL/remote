package org.universAAL.ri.gateway;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.universAAL.ri.gateway.utils.ArraySet;

public class ArraySetTest {

    @Test
    public void testEqual1() {

	final Integer[] a = null;
	final Integer[] b = null;

	assertTrue(new ArraySet.Equal<Integer>().equal(a, b));

    }

    @Test
    public void testEqual2() {

	final Integer[] a = null;
	final Integer[] b = { 1 };

	assertTrue(!new ArraySet.Equal<Integer>().equal(a, b));

    }

    @Test
    public void testEqual3() {

	final Integer[] a = { 1, 2, 3 };
	final Integer[] b = { 1, 2, 3 };

	assertTrue(new ArraySet.Equal<Integer>().equal(a, b));

    }

    @Test
    public void testEqual4() {

	final Integer[] a = { 1, 2, 3 };
	final Integer[] b = { 3, 1, 2 };

	assertTrue(new ArraySet.Equal<Integer>().equal(a, b));

    }

    @Test
    public void testUnion1() {

	final Integer[] a = null;
	final Integer[] b = null;

	final Integer[] res = new ArraySet.Union<Integer>().combine(a, b,
		new Integer[] {});

	assertEquals(0, res.length);

    }

    @Test
    public void testUnion2() {

	final Integer[] a = { 1, 2 };
	final Integer[] b = { 2, 3 };

	final Integer[] res = new ArraySet.Union<Integer>().combine(a, b,
		new Integer[] {});

	assertEquals(3, res.length);
	assertArrayEquals(new Integer[] { 1, 2, 3 }, res);

    }

    @Test
    public void testDifference1() {

	final Integer[] a = null;
	final Integer[] b = null;

	final Integer[] res = new ArraySet.Difference<Integer>().combine(a, b,
		new Integer[] {});

	assertEquals(0, res.length);
    }

    @Test
    public void testDifference2() {

	final Integer[] a = new Integer[] { 1, 2, 3 };
	final Integer[] b = null;

	final Integer[] res = new ArraySet.Difference<Integer>().combine(a, b,
		new Integer[] {});

	assertEquals(3, res.length);
	assertArrayEquals(new Integer[] { 1, 2, 3 }, res);
    }

    @Test
    public void testDifference3() {

	final Integer[] a = new Integer[] { 1, 2, 3 };
	final Integer[] b = new Integer[] { 1, 2, 3 };

	final Integer[] res = new ArraySet.Difference<Integer>().combine(a, b,
		new Integer[] {});

	assertEquals(0, res.length);
    }

    @Test
    public void testDifference4() {

	final Integer[] a = new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
	final Integer[] b = new Integer[] { 7, 8, 9 };

	final Integer[] res = new ArraySet.Difference<Integer>().combine(a, b,
		new Integer[] {});

	assertEquals(6, res.length);
	assertArrayEquals(new Integer[] { 1, 2, 3, 4, 5, 6 }, res);
    }
}
