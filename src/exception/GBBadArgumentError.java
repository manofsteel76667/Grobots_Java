package exception;

public class GBBadArgumentError extends GBError {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3422480932674388703L;

	@Override
	public String ToString() {
		return "bad argument (no more detail available)";
	}
};