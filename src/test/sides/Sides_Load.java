package test.sides;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import sides.Side;
import sides.SideReader;
import exception.GBError;

public class Sides_Load {
	String[] slotNames = {
			"chassis",
			"processor",
			"engine",
			"constructor",
			"energy",
			"solar-cells",
			"eater",
			"syphon",
			"robot-sensor",
			"food-sensor",
			"shot-sensor",
			"armor",
			"repair-rate",
			"shield",
			"blaster",
			"grenades",
			"force-field",
			"enemy-syphon",
			"bomb",
			"cooling charge",
			"code"
	};
	public static final String testFilePath(){
		String sep = System.getProperty("file.separator");
		return "src" + sep + "test" + sep + "sides" + sep;
	}
	@Test
	public void testAllSides() throws IOException, GBError {
		String path = testFilePath();
		List<String> filenames = new LinkedList<String>();
		File f = new File(path);// the path required
		for (String s : f.list()) {
			if (s.length() > 3)
				if (s.substring(s.length() - 3).equals(".gb"))
					filenames.add(path + s);
		}
		for (String file : filenames) {
			testSideReader(file);
		}
	}
	@Test
	public void testOneSide(){
		testSideReader("the-lunacy.gb");
	}
	public void testSideReader(String filename){
		try {
			String file = filename;
			if (!filename.contains(testFilePath()))
				file = testFilePath() + filename;
			Side test = SideReader.Load(file);
			System.out.println(file + " read successfully.  Side "
					+ test.Name() + " compiles without error.");
			Iterator<sides.RobotType> it = test.types.iterator();			
			while(it.hasNext()){
				sides.RobotType typ = it.next();				
				System.out.println("Type " + typ.name + " layout: ");
				for(int i = 0;i< typ.hardware.hardwareList.length - 1;i++)
					System.out.println(String.format("%s: Cost: %f,  mass %f", 
							slotNames[i], typ.hardware.hardwareList[i].Cost(), typ.hardware.hardwareList[i].Mass()));
				System.out.println(String.format("%s: Cost: %f,  mass %f", 
						slotNames[20], typ.brain.Cost(), typ.brain.Mass()));
			}
		} catch (Exception e) {
			System.out.println("Error in " + filename + ": " + e.getMessage());
		}
	}
}
