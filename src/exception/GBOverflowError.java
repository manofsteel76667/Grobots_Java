package exception;

public class GBOverflowError extends GBArithmeticError {
/**
	 * 
	 */
	private static final long serialVersionUID = 8883754526835981365L;

	//public:
	@Override
	public String toString() {
		return "arithmetic overflow";
	}


}