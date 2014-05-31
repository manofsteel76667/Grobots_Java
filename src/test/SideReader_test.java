package test;

import static org.junit.Assert.fail;
import headless.Headless;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import sides.RobotType;
import sides.Side;
import sides.SideReader;
import simulation.GBRobot;
import simulation.GBWorld;
import support.FinePoint;
import exception.GBAbort;
import exception.GBBadArgumentError;
import exception.GBError;
import exception.GBGenericError;
import exception.GBIndexOutOfRangeError;
import exception.GBNilPointerError;

public class SideReader_test {
	List<Side> allsides = null;
	Side side = null;
	RobotType type = null;

	/**
	 * Load each side file in the sides directory. For each side, instantiate
	 * one robot of each type in the side.
	 * 
	 * @throws IOException
	 * @throws GBError
	 */
	@Test
	public void testAllSides() {
		String msg = "";
		try {
			allsides = TestBase.loadAllSides();
		} catch (Exception e) {
			e.printStackTrace();
			fail("Could not load all sides: " + e.getMessage());
		}
		try {
			Iterator<Side> it = allsides.iterator();
			while (it.hasNext()) {
				side = it.next();
				for (RobotType typ : side.types) {
					type = typ;
					msg = String.format("%s's %s", side.Name(), type.name);
					GBRobot newone = new GBRobot(type, new FinePoint(0, 0));
					TestBase.log("Created " + msg);
				}
			}
		} catch (GBError e) {
			e.printStackTrace();
			fail("Could not create " + msg + ": " + e.ToString());
		}
	}

	@Test
	public void WorldTest() throws GBNilPointerError, GBBadArgumentError,
			GBAbort {
		try {
			TestBase.log("Creating world");
			GBWorld world = new GBWorld();
			TestBase.log("Loading Algae");
			Side side = TestBase.loadSide("algae.gb");
			TestBase.log("Adding to world");
			world.Sides().add(side);
			TestBase.log("Loading MicroAlgae");
			Side side2 = TestBase.loadSide("microalgae.gb");
			TestBase.log("Adding to world");
			world.Sides().add(side2);
			TestBase.log("Seeding world");
			world.AddSeeds();
		} catch (GBError e) {
			e.printStackTrace();
			fail("Failed:" + e.ToString());
		}
	}

	@Test
	public void testHeadless() {
		try {
			String[] sides = new String[] {
					TestBase.sidesFilePath() + "cyclops.gb",
					TestBase.sidesFilePath() + "tomatoes.gb",
					TestBase.sidesFilePath() + "microalgae.gb" };
			Headless.main(sides);
		} catch (Exception e) {
			e.printStackTrace();
			TestBase.log(e.getStackTrace());
		}
	}
}
