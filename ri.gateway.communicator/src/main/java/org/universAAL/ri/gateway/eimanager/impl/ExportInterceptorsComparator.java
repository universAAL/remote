package org.universAAL.ri.gateway.eimanager.impl;

import java.util.Comparator;

import org.universAAL.ri.gateway.eimanager.ExportOperationInterceptor;

public class ExportInterceptorsComparator implements Comparator<ExportOperationInterceptor> {

	public int compare(ExportOperationInterceptor o1,
			ExportOperationInterceptor o2) {
		return Integer.valueOf(o1.getPriority()).compareTo(o2.getPriority());
	}

}
