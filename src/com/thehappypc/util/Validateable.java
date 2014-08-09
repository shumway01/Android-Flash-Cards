package com.thehappypc.util;

public interface Validateable {
	/**
	 * Validate the paramters entered in the UI.
	 * @param card
	 * @return true if validation passes, false otherwise
	 */
	public abstract boolean validate();
}
