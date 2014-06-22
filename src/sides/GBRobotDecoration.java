package sides;

import java.util.HashMap;
import java.util.Map;

public enum GBRobotDecoration {
	none("none"), dot("dot"), circle("circle"), square("square"), triangle(
			"triangle"), cross("cross"), x("x"), hline("hline"), vline("vline"), slash(
			"slash"), backslash("backslash");
	public final String tagName;

	public static GBRobotDecoration byTag(String _tagName)
			throws GBElementArgumentError {
		try {
			return tagLookup.get(_tagName.toLowerCase());
		} catch (Exception e) {
			throw new GBElementArgumentError();
		}
	}

	public static final int kNumDecorationTypes = GBRobotDecoration.values().length;
	static final Map<String, GBRobotDecoration> tagLookup = new HashMap<String, GBRobotDecoration>();
	static {
		for (GBRobotDecoration typ : GBRobotDecoration.values())
			tagLookup.put(typ.tagName, typ);
	}

	GBRobotDecoration(String _tagName) {
		tagName = _tagName;
	}
}