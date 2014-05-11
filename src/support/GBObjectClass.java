package support;

public enum GBObjectClass {
	/*TODO: find out ihow this is used and remove if possible*/
	kFirstObjectClass(0), // =0,
	ocRobot(0), // = 0, // a robot or other large mobile object
	ocFood(1), // small and immobile
	ocShot(2), // small and mobile
	ocArea(3), // large; collides with food and robots
	ocSensorShot(4), // collides with robots, shots, and food
	ocDecoration(5), // noncolliding
	kNumObjectClasses(6), ocDead(7); // to delete

	public final int value;

	GBObjectClass(int _value) {
		value = _value;
	}
}
