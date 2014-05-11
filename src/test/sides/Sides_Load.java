package test.sides;

import java.io.*;
import java.util.*;

import org.junit.Test;

import sides.Side;
import sides.SideReader;
import exception.*;

public class Sides_Load {
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
	public void testBalun(){
		testSideReader("balun.gb");
	}
	public void testSideReader(String filename){
		try {
			String file = filename;
			if (!filename.contains(testFilePath()))
				file = testFilePath() + filename;
			Side test = SideReader.Load(file);
			System.out.println(file + " read successfully.  Side "
					+ test.Name() + " compiles without error.");
		} catch (Exception e) {
			System.out.println("Error in " + filename + ": " + e.getMessage());
		}
	}
}
