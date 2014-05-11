// GBBrain.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.
package brains;

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
	 */
	public void Step(Robot robot, World world) {
		Think(robot, world);
	}

	/**
	 * Think one frame
	 * 
	 * @param robot
	 * @param world
	 */
	public void think(Robot robot, World world) {

	}

	/**
	 * Can we think now?
	 */
	public boolean ready; 
}
