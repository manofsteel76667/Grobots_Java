/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/

package brains;

import simulation.GBRobot;
import simulation.GBWorld;
import exception.GBBrainError;

public abstract class Brain {
	public BrainStatus status;

	public Brain() {
		status = BrainStatus.bsOK;
	}

	/**
	 * Think one step
	 * 
	 * @param robot
	 * @param world
	 */
	public void step(GBRobot robot, GBWorld world) {
		think(robot, world);
	}

	/**
	 * Think one frame
	 * 
	 * @param robot
	 * @param world
	 */
	public abstract void think(GBRobot robot, GBWorld world);

	/**
	 * Can we think now?
	 */
	public boolean ready() {
		return false;
	}

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
