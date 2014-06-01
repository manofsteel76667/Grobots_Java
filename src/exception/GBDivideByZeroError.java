package exception;

public class GBDivideByZeroError extends GBArithmeticError {
/**
	 * 
	 */
	private static final long serialVersionUID = -5147074034724560453L;

	//public:
	@Override
	public String toString() {
		return "division by zero";
	}
}