package org.universAAL.ri.gateway.eimanager.impl;

import java.util.Comparator;

import org.universAAL.ri.gateway.eimanager.ImportOperationInterceptor;

public class ImportInterceptorsComparator implements Comparator<ImportOperationInterceptor> {

	public int compare(ImportOperationInterceptor o1,
			ImportOperationInterceptor o2) {
		return Integer.valueOf(o1.getPriority()).compareTo(o2.getPriority());
	}

}
