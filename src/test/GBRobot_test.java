package test;

import static org.junit.Assert.fail;

import java.io.FileNotFoundException;

import org.junit.Test;

import sides.Side;
import simulation.GBRobot;
import support.FinePoint;

public class GBRobot_test {

	@Test
	public void createOne() {
		try {
			Side side1 = TestBase.loadSide("algae.gb");
			GBRobot robot1 = new GBRobot(side1.types.get(0),
					new FinePoint(0, 0));
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void test_GBObject_next() throws FileNotFoundException {
		Side side1 = TestBase.loadSide("algae.gb");
		GBRobot robot1 = new GBRobot(side1.types.get(0), new FinePoint(0, 0));
		GBRobot robot2 = new GBRobot(side1.types.get(0), new FinePoint(0, 0));
		robot1.next = robot2;
		TestBase.log("Before next=null");
		TestBase.log(robot1.Details());
		TestBase.log(robot1.next.Details());
		TestBase.log(robot2.Details());
		robot1.next = null;
		TestBase.log("After next=null");
		TestBase.log(robot1.Details());
		TestBase.log(robot1.next);
		TestBase.log(robot2.Details());
	}

}
