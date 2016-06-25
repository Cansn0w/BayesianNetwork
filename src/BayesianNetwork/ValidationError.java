package BayesianNetwork;

/**
 * Runtime Exception used when checking the validity of the network
 */
public class ValidationError extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ValidationError() {
		super();
	}
	
	public ValidationError(String message) {
		super(message);
	}

    public ValidationError(Throwable cause) {
        super(cause);
    }

    public ValidationError(String message, Throwable cause) {
        super(message, cause);
    }

}
