package test;

import static org.junit.Assert.*;

import org.junit.Test;

import exception.GBBadArgumentError;
import exception.GBNilPointerError;
import ui.GBApplication;

public class GBApplication_Test {

	@Test
	public void testGBApplication() throws GBNilPointerError, GBBadArgumentError {
		new GBApplication().run();
	}



}
