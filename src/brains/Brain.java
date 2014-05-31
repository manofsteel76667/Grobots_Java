// GBBrain.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.
package brains;

import simulation.GBRobot;
import simulation.GBWorld;
import exception.GBAbort;
import exception.GBBadArgumentError;
import exception.GBGenericError;
import exception.GBOutOfMemoryError;

public class Brain {
	public BrainStatus status;

	// public:

	public Brain() {
		status = BrainStatus.bsOK;
	}

	/**
	 * Think one step
	 * 
	 * @param robot
	 * @param world
	 * @throws GBAbort
	 * @throws GBGenericError
	 * @throws GBOutOfMemoryError
	 * @throws GBBadArgumentError
	 */
	public void Step(GBRobot robot, GBWorld world) throws GBAbort,
			GBBadArgumentError, GBOutOfMemoryError, GBGenericError {
		think(robot, world);
	}

	/**
	 * Think one frame
	 * 
	 * @param robot
	 * @param world
	 * @throws GBAbort
	 * @throws GBGenericError
	 * @throws GBOutOfMemoryError
	 * @throws GBBadArgumentError
	 */
	public void think(GBRobot robot, GBWorld world) throws GBBadArgumentError,
			GBOutOfMemoryError, GBGenericError, GBAbort {

	}

	/**
	 * Can we think now?
	 */
	public boolean ready;

	protected class GBStackOverflowError extends GBBrainError {

		/**
	 * 
	 */
		private static final long serialVersionUID = 1L;

		@Override
		public String toString() {
			return "stack overflow";
		}
	}

	protected class GBStackUnderflowError extends GBBrainError {

		/**
	 * 
	 */
		private static final long serialVersionUID = 1L;

		@Override
		public String toString() {
			return "stack underflow";
		}
	}
}
