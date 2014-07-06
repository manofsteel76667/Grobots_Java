/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package support;

import java.util.HashMap;
import java.util.Map;

public enum GBObjectClass {
	ocRobot(0), // = 0, // a robot or other large mobile object
	ocFood(1), // small and immobile
	ocShot(2), // small and mobile
	ocArea(3), // large; collides with food and robots
	ocSensorShot(4), // collides with robots, shots, and food
	ocDecoration(5), // noncolliding
	ocDead(6); // to delete (was 7)

	public final int value;

	public static final GBObjectClass byValue(int _value) {
		return valueLookup.get(_value);
	}

	public static final int kNumObjectClasses = GBObjectClass.values().length;
	static Map<Integer, GBObjectClass> valueLookup = new HashMap<Integer, GBObjectClass>();

	GBObjectClass(int _value) {
		value = _value;
	}

	static {
		for (GBObjectClass cl : GBObjectClass.values())
			valueLookup.put(cl.value, cl);
	}
}
