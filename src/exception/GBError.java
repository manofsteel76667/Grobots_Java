/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package exception;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

// Grobots (c) 2002-2006 Devon and Warren Schudy
// Distributed under the GNU General Public License.

public final class GBError {
	/**
	 * 
	 */
	// private static final long serialVersionUID = -6819324881379371856L;

	public static void FatalError(String message) {
		javax.swing.JOptionPane.showMessageDialog(null, message);
		System.exit(1);
	}

	/*
	 * Error handling strategy: All exceptions are unchecked custom exceptions
	 * derived from RuntimeException (SimulationException and its descendants)
	 * Exceptions thrown at their source NonfatalError is called at the
	 * appropriate level; Brain level for Brain exceptions, World level for
	 * simulation problems and brain errors that have been aborted, and UI level
	 * for system-level errors and simulation errors that have been aborted.
	 */
	public static void NonfatalError(final String message) throws GBAbort {
		/*
		 * Invoke an option pane to inform the user of the error andask him what
		 * to do. Since Swing is touchy about screen updates, we need to make
		 * sure we do it on the EDT thread
		 */
		int response = 0;
		if (SwingUtilities.isEventDispatchThread()) {
			String[] buttons = { "Quit", "Abort", "Continue" };
			response = JOptionPane.showOptionDialog(null, message,
					"Nonfatal error", javax.swing.JOptionPane.WARNING_MESSAGE,
					0, null, buttons, buttons[2]);
		} else {
			FutureTask<Integer> dialogTask = new FutureTask<Integer>(
					new Callable<Integer>() {
						@Override
						public Integer call() {
							String[] buttons = { "Quit", "Abort", "Continue" };
							return JOptionPane.showOptionDialog(null, message,
									"Nonfatal error",
									javax.swing.JOptionPane.WARNING_MESSAGE, 0,
									null, buttons, buttons[2]);
						}
					});
			SwingUtilities.invokeLater(dialogTask);
			try {
				response = dialogTask.get();
			} catch (Exception e) {
				e.getMessage();
			}
		}
		switch (response) {
		case 1: // abort, clicked retry
			throw new GBAbort();
		case 2: // continue, clicked ignore
			break;
		case 0: // quit, clicked abort
			System.exit(1);
		default:
			break;
		}
	}

	public static boolean Confirm(String message, String operation) {
		// TODO this should actually ask
		return true;
	}
};
