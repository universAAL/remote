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
	public T[] combine(T[] a, T[] b);
    }

    public static class Union<T> implements Combiner<T> {

	public T[] combine(final T[] a, final T[] b) {
	    final Set<T> aSet = new HashSet<T>(Arrays.asList(a));
	    final Set<T> bSet = new HashSet<T>(Arrays.asList(b));
	    aSet.addAll(bSet);
	    return (T[]) aSet.toArray(new Object[aSet.size()]);
	}
    }

    public static class Difference<T> implements Combiner<T> {

	public T[] combine(final T[] a, final T[] b) {
	    final Set<T> aSet = new HashSet<T>(Arrays.asList(a));
	    final Set<T> bSet = new HashSet<T>(Arrays.asList(b));
	    aSet.removeAll(bSet);
	    return (T[]) aSet.toArray(new Object[aSet.size()]);
	}

    }

    public static class Equal<T> {
	public boolean equal(final T[] a, final T[] b) {
	    if (a.length != b.length) {
		return false;
	    }
	    boolean equal = true;
	    int i = 0;
	    while (equal && i < a.length) {
		boolean e = false;
		final int j = 0;
		while (!e && j < b.length) {
		    e = a[i].equals(b[j]);
		}
		equal = e;
		i++;
	    }
	    return equal;
	}
    }

}
