/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package simulation;

import java.util.HashMap;
import java.util.Map;

public enum GBObjectClass {
	ocRobot(0, 1), // a robot or other large mobile object
	ocFood(1, 0), // small and immobile
	ocShot(2, 3), // small and mobile
	ocArea(3, 2), // large; collides with food and robots (forcefields and
					// explosions)
	ocSensorShot(4, 5), // collides with robots, shots, and food
	ocDecoration(5, 4), // noncolliding (shot explosions and smoke)
	ocDead(6, 6); // to delete (was 7)

	public final int value;
	public final int drawOrder;

	public static final GBObjectClass byValue(int _value) {
		return valueLookup.get(_value);
	}

	public static final int kNumObjectClasses = GBObjectClass.values().length;
	static Map<Integer, GBObjectClass> valueLookup = new HashMap<Integer, GBObjectClass>();

	GBObjectClass(int _value, int _drawOrder) {
		value = _value;
		drawOrder = _drawOrder;
	}

	static {
		for (GBObjectClass cl : GBObjectClass.values())
			valueLookup.put(cl.value, cl);
	}
}
