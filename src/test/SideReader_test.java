package test;

import static org.junit.Assert.fail;
import headless.Headless;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import sides.RobotType;
import sides.Side;
import simulation.GBRobot;
import simulation.GBWorld;
import support.FinePoint;
import test.TestBase;
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

	@Test
	public void testOneSide() throws GBIndexOutOfRangeError, GBAbort,
			GBGenericError {
		TestBase.loadSide("active-4.gb");
	}

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
			fail("Could not create " + msg + ": " + e.toString());
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
			fail("Failed:" + e.toString());
		}
	}

	@Test
	public void testHeadless() {
		try {
			String[] sides = new String[] {
					TestBase.sidesFilePath() + "cyclops.gb",
					TestBase.sidesFilePath() + "tomatoes.gb",
					TestBase.sidesFilePath() + "move-zig-3.gb",
					// "-t100",//Tournament
					"-S",// Sound on
					"-l18000",// Time limit
					"-b500",// Stats period
					"-w10",// World width
					"-h10",// World height
					"-s5",// Seed limit
					"-H" };// HTML output
			Headless.main(sides);
		} catch (Exception e) {
			e.printStackTrace();
			TestBase.log(e.getStackTrace());
		}
	}
}
