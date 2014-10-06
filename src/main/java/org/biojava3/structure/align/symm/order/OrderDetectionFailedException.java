package org.biojava3.structure.align.symm.order;

/**
 * Order-detection failed.
 * @author dmyersturnbull
 */
public class OrderDetectionFailedException extends Exception {
	private static final long serialVersionUID = -7040421412578699838L;

	public OrderDetectionFailedException() {
		super();
	}

	public OrderDetectionFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	public OrderDetectionFailedException(String message) {
		super(message);
	}

	public OrderDetectionFailedException(Throwable cause) {
		super(cause);
	}

}
