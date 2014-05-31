package exception;

public class GBNilPointerError extends GBError {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8971345894512217180L;

	@Override
	public String ToString() {
		return "nil pointer passed";
	}
};