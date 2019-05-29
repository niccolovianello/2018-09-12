package it.polito.tdp.poweroutages.model;

import java.util.HashMap;

public class NercIdMap extends HashMap<Integer, Nerc> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Nerc get(Nerc n) {
		Nerc old = super.get(n.getId());
		if (old == null) {
			super.put(n.getId(), n);
			return n;
		}
		return old;
	}
}