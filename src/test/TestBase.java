package test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import sides.Side;
import sides.SideReader;
import exception.GBAbort;
import exception.GBGenericError;
import exception.GBIndexOutOfRangeError;

public class TestBase {
	static String[] slotNames = { "chassis", "processor", "engine",
			"constructor", "energy", "solar-cells", "eater", "syphon",
			"robot-sensor", "food-sensor", "shot-sensor", "armor",
			"repair-rate", "shield", "blaster", "grenades", "force-field",
			"enemy-syphon", "bomb", "cooling charge", "code" };

	public static final String sidesFilePath() {
		String sep = System.getProperty("file.separator");
		return "src" + sep + "test" + sep + "sides" + sep;
	}

	public static final String outputFilePath() {
		String sep = System.getProperty("file.separator");
		return "src" + sep + "test" + sep;
	}

	public static Side loadSide(String filename) throws GBIndexOutOfRangeError,
			GBAbort, GBGenericError {
		Side ret = SideReader.Load(TestBase.sidesFilePath() + filename);
		return ret;
	}

	public static void log(Object msg) {
		System.out.println(msg);
	}

	public static List<Side> loadAllSides() throws FileNotFoundException,
			GBIndexOutOfRangeError, GBAbort, GBGenericError {
		String path = TestBase.sidesFilePath();
		List<String> filenames = new LinkedList<String>();
		List<Side> ret = new ArrayList<Side>();
		File f = new File(path);// the path required
		for (String s : f.list()) {
			if (s.length() > 3)
				if (s.substring(s.length() - 3).equals(".gb"))
					filenames.add(path + s);
		}
		PrintWriter out = new PrintWriter(outputFilePath()
				+ "SideLoader test output.txt");
		for (String file : filenames) {
			ret.add(loadSideWithLogging(file, out));
		}
		out.close();
		return ret;
	}

	public static Side loadSideWithLogging(String filename, PrintWriter out)
			throws GBIndexOutOfRangeError, GBAbort, GBGenericError {
		String file = filename;
		if (!filename.contains(sidesFilePath()))
			file = sidesFilePath() + filename;
		TestBase.log("Loading " + filename);
		Side test = SideReader.Load(file);
		TestBase.log("Complete.");
		out.println(file + " read successfully.  Side " + test.Name()
				+ " compiles without error.");
		Iterator<sides.RobotType> it = test.types.iterator();
		while (it.hasNext()) {
			sides.RobotType typ = it.next();
			out.println("Type " + typ.name + " layout: ");
			for (int i = 0; i < typ.hardware.hardwareList.length - 1; i++)
				if (typ.hardware.hardwareList[i].Cost() > 0)
					out.println(String.format("%s: Cost: %.0f,  mass %.2f",
							slotNames[i], typ.hardware.hardwareList[i].Cost(),
							typ.hardware.hardwareList[i].Mass()));
			if (typ.Brain() != null)
				out.println(String.format("%s: \tCost: \t%f,  \tmass \t%f",
						slotNames[20], typ.brain.Cost(), typ.brain.Mass()));
			else
				TestBase.log(typ.name + " has no brain??");
			out.println(String.format(
					"%s's %s Total Cost: %.1f \t Total Mass: %.2f",
					test.Name(), typ.name, typ.Cost(), typ.Mass()));
			out.println("");
		}
		return test;
	}
}
