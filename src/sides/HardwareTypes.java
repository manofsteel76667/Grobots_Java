package sides;

import java.util.HashMap;
import java.util.Map;

public enum HardwareTypes {
	hcNone("none_illegal"), hcProcessor("processor"), hcRadio("radio"), hcEngine(
			"engine"), hcConstructor("constructor"), hcEnergy("energy"), hcSolarCells(
			"solar-cells"), hcEater("eater"), hcArmor("armor"), hcRepairRate(
			"repair-rate"), hcShield("shield"), hcRobotSensor("robot-sensor"), hcFoodSensor(
			"food-sensor"), hcShotSensor("shot-sensor"), hcBlaster("blaster"), hcGrenades(
			"grenades"), hcForceField("force-field"), hcBomb("bomb"), hcSyphon(
			"syphon"), hcEnemySyphon("enemy-syphon");
	public final String tagName;

	public static HardwareTypes byTag(String _tagName)
			throws GBElementArgumentError {
		try {
			return tagLookup.get(_tagName.toLowerCase());
		} catch (Exception e) {
			throw new GBElementArgumentError();
		}
	}

	public static final int kHardwareComponentTypes = HardwareTypes.values().length;
	static final Map<String, HardwareTypes> tagLookup = new HashMap<String, HardwareTypes>();
	static {
		for (HardwareTypes typ : HardwareTypes.values())
			tagLookup.put(typ.tagName, typ);
	}

	HardwareTypes(String _tagName) {
		tagName = _tagName;
	}
}