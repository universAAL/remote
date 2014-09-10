/*******************************************************************************
 * Copyright 2014 Universidad Politécnica de Madrid UPM
 * 
 * Licensed under the Apache License, Version 2.0 (the "License";
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.universAAL.ri.gateway.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author amedrano
 * 
 */
public class ArraySet {

    public interface Combiner<T> {
	public T[] combine(T[] a, T[] b, T[] resultArray);
    }

    public static class Union<T> implements Combiner<T> {

	public T[] combine(final T[] a, final T[] b, final T[] resultArray) {
	    final Set<T> aSet;
	    final Set<T> bSet;
	    if (a != null) {
		aSet = new HashSet<T>(Arrays.asList(a));
	    } else {
		aSet = new HashSet<T>();
	    }
	    if (b != null) {
		bSet = new HashSet<T>(Arrays.asList(b));
	    } else {
		bSet = new HashSet<T>();
	    }
	    aSet.addAll(bSet);
	    return aSet.toArray(resultArray);
	}
    }

    public static class Difference<T> implements Combiner<T> {

	public T[] combine(final T[] a, final T[] b, final T[] resultArray) {
	    final Set<T> aSet;
	    final Set<T> bSet;
	    if (a != null) {
		aSet = new HashSet<T>(Arrays.asList(a));
	    } else {
		aSet = new HashSet<T>();
	    }
	    if (b != null) {
		bSet = new HashSet<T>(Arrays.asList(b));
	    } else {
		bSet = new HashSet<T>();
	    }
	    aSet.removeAll(bSet);
	    return aSet.toArray(resultArray);
	}

    }

    public static class Equal<T> {
	public boolean equal(final T[] a, final T[] b) {
	    if (a == null && b == null) {
		return true;
	    }
	    if (a == null || b == null || a.length != b.length) {
		return false;
	    }
	    boolean equal = true;
	    int i = 0;
	    while (equal && i < a.length) {
		boolean e = false;
		int j = 0;
		while (!e && j < b.length) {
		    e = a[i].equals(b[j++]);
		}
		equal = e;
		i++;
	    }
	    return equal;
	}
    }

}
