package exception;

public class GBBadConstructorError extends GBError {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8694653977575593764L;

	@Override
	public String toString() {
		return "forbidden constructor called";
	}
};
