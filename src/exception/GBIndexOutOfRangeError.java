package exception;

public class GBIndexOutOfRangeError extends GBBadArgumentError {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7664977257690822562L;

	@Override
	public String ToString() {
		return "index out of range";
	}
};