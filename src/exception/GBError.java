package exception;

// GBErrors.cpp
// Grobots (c) 2002-2006 Devon and Warren Schudy
// Distributed under the GNU General Public License.

public class GBError extends java.lang.Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6819324881379371856L;

	public GBError() {
	}

	public String ToString() {
		return "undifferentiated GBError (shouldn't be used)";
	}

	// TODO: Check how these work in Headless Mode
	public static void FatalError(String message) {
		javax.swing.JOptionPane.showMessageDialog(null, message);
		System.exit(1);
	}

	public static void NonfatalError(String message) throws GBAbort  {
		String[] buttons = { "Abort", "Retry", "Ignore" };
		int rc = javax.swing.JOptionPane.showOptionDialog(null, message,
				"Nonfatal error", javax.swing.JOptionPane.WARNING_MESSAGE, 0,
				null, buttons, buttons[2]);

		switch (rc) {
		case 0: // quit, clicked abort
			System.exit(1);
		case 1: // abort, clicked retry
			throw new GBAbort();
		case 2: // continue, clicked ignore
		default:
			return;
		}
	}

	public static boolean Confirm(String message, String operation) {
		// TODO this should actually ask
		return true;
	}
};
