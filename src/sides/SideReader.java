/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package sides;

//Note that #author, #date, #description, and #color can appear in multiple places.
//These are conveniently distinguished by whether type is non-null.
import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import simulation.GBObjectClass;
import simulation.GBRobot;
import support.FinePoint;
import support.StringUtilities;
import brains.GBStackBrainSpec;
import exception.GBError;

enum GBElementType {
	etNone("none-illegal"), // Top of file. content: #side
	etSide("side"), // arg: name; content: #author #date #description #color
					// #type
	etSeed("seed"), // args: type ids
	etAuthor("author"), // arg: name; empty
	etDate("date"), // arg: date; empty
	etDescription("comment"), // arg and content are optional and ignored.
	etColor("color"), // arg: color; empty
	etType("type"), // arg: name; content: #hardware #code #author? #date?
					// #description #color #decoration
	etDecoration("decoration"), // args: color, shape
	etHardware("hardware"), // no args; content: parse
	etCode("code"), // no args for now; content: parse
	etStart("start"), // arg: label-name; empty
	etVariable("var"), // args: name initial-value; empty
	etVectorVariable("vector"), // args: name initial-x initial-y; empty
	etConstant("const"), // args: name value; empty
	etEnd("end"); // no args; content forbidden
	public final String tagName;

	public static GBElementType byTag(String _tag) {
		return tagLookup.get(_tag.toLowerCase());
	}

	public static final int kNumElementTypes = GBElementType.values().length;
	static final HashMap<String, GBElementType> tagLookup = new HashMap<String, GBElementType>();
	static {
		for (GBElementType typ : GBElementType.values())
			tagLookup.put(typ.tagName, typ);
	}

	GBElementType(String _tagName) {
		tagName = _tagName;
	}

}

public class SideReader {

	public String fileName;
	Side side; // the side being read
	RobotType type; // current type
	GBStackBrainSpec brain;
	GBStackBrainSpec commonBrain; // global code; may move to side
	GBElementType state; // what we're in
	String line; // current line
	LinkedList<String> tokens;
	int lineno; // current line number

	/**
	 * Process one line from the source file. Assumes comments (text after ;)
	 * have already been removed
	 * 
	 * @throws GBReaderError
	 * @throws GBCStackError
	 * @throws GBUnresolvedSymbolError
	 * @throws java.lang.Exception
	 */
	void processLine() throws GBReaderError {
		lineno++;
		tokens = new LinkedList<String>(
				Arrays.asList(line.trim().split("\\s+")));
		while (tokens.size() > 0) {
			if (tokens.getFirst().charAt(0) == '#') {
				// Start of new section detected
				String token = tokens.removeFirst();
				token = token.substring(1, token.length());
				if (token.length() == 0)
					throw new RuntimeException("# but no token - huh?");
				GBElementType typ = GBElementType.byTag(token);
				if (typ != null) {
					processTag(typ);
					return;
				}
				throw new GBNoSuchElementError();
			} else
				// content reached
				switch (state) {
				case etNone:
				case etEnd:
					throw new GBForbiddenContentError();
				case etHardware:
					processHardwareLine();
					return;
				case etCode:
					processCodeLine();
					return;
				case etSide:
				case etType: // side and type act like description,
				case etDescription: // so #description is now optional
					return; // do nothing
				case etAuthor:
				case etDate:
				case etStart:
				case etVariable:
				case etConstant:
				case etDecoration:
				default:
					throw new GBReaderError();
					// // should
					// never be
					// in these
					// states
				}
		}
	}

	Color parseColor(String token) throws GBElementArgumentError {
		// could do named colors, but not urgent
		// check length
		float r, g, b;
		Color color = Color.white;
		try {
			if (token.length() == 3) {
				r = Integer.parseInt(token.substring(0, 1), 16) / 16.0f;
				g = Integer.parseInt(token.substring(1, 2), 16) / 16.0f;
				b = Integer.parseInt(token.substring(2, 3), 16) / 16.0f;
				color = new Color(r, g, b);
			} else if (token.length() == 6) {
				r = Integer.parseInt(token.substring(0, 2), 16) / 255.0f;
				g = Integer.parseInt(token.substring(2, 4), 16) / 255.0f;
				b = Integer.parseInt(token.substring(4, 6), 16) / 255.0f;
				color = new Color(r, g, b);
			}
			return color;
		} catch (NumberFormatException e) {
			throw new GBElementArgumentError();
		}
	}

	void processTag(GBElementType element) throws GBReaderError {
		// clean up type
		if (type != null
				&& (element == GBElementType.etType || element == GBElementType.etEnd)) {
			if (brain != null) {
				brain.check();
				type.setBrain(brain);
				brain = null;
			}
			side.addType(type);
			type = null;
		}
		// legality check
		if (state == GBElementType.etEnd)
			throw new GBForbiddenContentError();
		else if (state == GBElementType.etNone
				&& element != GBElementType.etSide)
			throw new GBMisplacedElementError();
		// process it
		switch (element) {
		case etSide:
			if (state == GBElementType.etNone) {
				if (tokens.size() == 0)
					throw new GBMissingElementArgumentError();
				if (side != null)
					throw new RuntimeException("SideReader already had side?!");
				side = new Side();
				side.debug = true;
				java.lang.StringBuilder sb = new java.lang.StringBuilder(
						tokens.removeFirst());
				while (tokens.size() > 0) {
					sb.append(" ");
					sb.append(tokens.removeFirst());
				}
				side.setName(sb.toString());
				state = GBElementType.etSide;
			} else
				throw new GBMisplacedElementError();
			break;
		case etSeed:
			if (type != null)
				throw new GBMisplacedElementError();
			{
				if (tokens.size() == 0)
					throw new GBMissingElementArgumentError();
				while (tokens.size() > 0) {
					String token = tokens.removeFirst();
					try {
						side.addSeedID(StringUtilities.parseInt(token));
					} catch (NumberFormatException e) {
						throw new GBElementArgumentError();
					}
				}
			}
			break;
		case etAuthor:
			if (type == null) {
				if (tokens.size() == 0)
					throw new GBMissingElementArgumentError();
				if (side == null)
					throw new RuntimeException("SideReader missing side");
				java.lang.StringBuilder sb = new java.lang.StringBuilder(
						tokens.removeFirst());
				while (tokens.size() > 0) {
					sb.append(" ");
					sb.append(tokens.removeFirst());
				}
				side.getAuthor(sb.toString());
			}
			break;
		case etDate:
			break; // ignore
		case etDescription:
			state = GBElementType.etDescription;
			break;
		case etColor: {
			String token = tokens.removeFirst();
			if (token == null)
				throw new GBMissingElementArgumentError();
			Color color = parseColor(token);
			if (type != null)
				type.setColor(color);
			else
				side.setColor(color);
		}
			break;
		case etType:
			if (type == null && brain != null) {
				commonBrain = brain;
				brain = null;
			}
			if (tokens.size() == 0)
				throw new GBMissingElementArgumentError();
			if (side == null)
				throw new RuntimeException("SideReader missing side");
			type = new RobotType(side);
			java.lang.StringBuilder sb = new java.lang.StringBuilder(
					tokens.removeFirst());
			while (tokens.size() > 0) {
				sb.append(" ");
				sb.append(tokens.removeFirst());
			}
			type.setName(sb.toString());
			state = GBElementType.etType;
			break;
		case etDecoration:
			if (type == null)
				throw new GBMisplacedElementError();
			{
				if (tokens.size() == 0)
					throw new GBMissingElementArgumentError();
				String token = tokens.removeFirst();

				Color color = parseColor(token);
				if (tokens.size() == 0)
					throw new GBMissingElementArgumentError();
				token = tokens.removeFirst();

				GBRobotDecoration dec = GBRobotDecoration.byTag(token);
				type.setDecoration(dec, color);

			}
			break;
		case etHardware:
			if (type != null)
				state = GBElementType.etHardware;
			else
				throw new GBMisplacedElementError();
			break;
		case etCode:
			if (brain == null) {
				if (type != null && commonBrain != null) {
					brain = new GBStackBrainSpec(commonBrain); // default to
					// beginning
					// of
					// type-specific
					// code
					int label = brain.addGensym("start");
					brain.resolveGensym(label);
					brain.setStartingLabel(label);
				} else
					brain = new GBStackBrainSpec();
			}

			state = GBElementType.etCode;
			break;
		case etStart:
			if (state == GBElementType.etCode) {
				if (tokens.size() > 0)
					brain.setStartingLabel(brain.getLabelReferencedIndex(tokens
							.removeFirst()));
				else {
					int label = brain.addGensym("start");
					brain.resolveGensym(label);
					brain.setStartingLabel(label);
				}
			} else
				throw new GBMisplacedElementError();
			break;
		case etVariable:
		case etConstant:
			if (state == GBElementType.etCode) {
				if (tokens.size() == 0)
					throw new GBMissingElementArgumentError();
				String name = tokens.removeFirst();
				Double num = 0.0;
				// check if a value also exists
				if (tokens.size() > 0) {
					// TODO try looking it up as a constant
					// TODO allow forward label references?
					String val = tokens.removeFirst();
					num = StringUtilities.parseDouble(val);
					if (num == null)
						throw new GBElementArgumentError();
				} else if (element == GBElementType.etConstant)
					throw new GBMissingElementArgumentError();
				if (element == GBElementType.etVariable)
					brain.addVariable(name, num);
				else
					brain.addConstant(name, num);

			} else
				throw new GBMisplacedElementError();
			break;
		case etVectorVariable:
			if (state == GBElementType.etCode) {
				if (tokens.size() == 0)
					throw new GBMissingElementArgumentError();
				String name = tokens.removeFirst();
				Double x, y;
				FinePoint f = new FinePoint();
				if (tokens.size() != 0) {
					x = StringUtilities.parseDouble(tokens.removeFirst());
					if (tokens.size() == 0)
						throw new GBMissingElementArgumentError();
					else
						y = StringUtilities.parseDouble(tokens.removeFirst());
					if (x == null || y == null)
						throw new GBElementArgumentError();
					else {
						f.x = x;
						f.y = y;
					}
				}
				brain.addVectorVariable(name, f);
			} else
				throw new GBMisplacedElementError();
			break;
		case etEnd:
			state = GBElementType.etEnd;
			break;
		default:
			throw new GBNoSuchElementError();
		}
		/*
		 * Only 1 section per line is allowed. This was implied in the
		 * ExtractToken and ExtractRest procedures but not stated explicitly
		 */
		tokens.clear();
	}

	void processCodeLine() {
		if (brain == null)
			throw new RuntimeException("can't compile code without a brain");
		brain.parseLine(line, lineno);
	}

	void processHardwareLine() throws GBUnknownHardwareError,
			GBMissingHardwareArgumentError, GBHardwareArgumentError,
			GBElementArgumentError {
		if (tokens.size() == 0)
			return;
		String name = tokens.removeFirst();
		HardwareTypes hc = HardwareTypes.byTag(name);
		switch (hc) {
		case hcProcessor: {
			int speed = getHardwareInteger();
			int mem = getHardwareInteger(0);
			type.getHardware().SetProcessor(speed, mem);
		}
			return;
		case hcRadio:
			return; // obsolete but remains for compatibility
		case hcEngine:
			type.getHardware().SetEngine(getHardwareNumber());
			return;
		case hcConstructor:
			type.getHardware().constructor.set(getHardwareNumber());
			return;
		case hcEnergy: {
			double max = getHardwareNumber();
			double initial = getHardwareNumber();
			type.getHardware().SetEnergy(max, initial);
		}
			return;
		case hcSolarCells:
			type.getHardware().SetSolarCells(getHardwareNumber());
			return;
		case hcEater:
			type.getHardware().SetEater(getHardwareNumber());
			return;
		case hcArmor:
			type.getHardware().SetArmor(getHardwareNumber());
			return;
		case hcRepairRate:
			type.getHardware().SetRepairRate(getHardwareNumber());
			return;
		case hcShield:
			type.getHardware().SetShield(getHardwareNumber());
			return;
		case hcRobotSensor: {
			double range = getHardwareNumber();
			int maxResults = getHardwareInteger(1);
			type.getHardware().sensor1.set(range, maxResults,
					GBObjectClass.ocRobot.value);
		}
			return;
		case hcFoodSensor: {
			double range = getHardwareNumber();
			int maxResults = getHardwareInteger(1);
			type.getHardware().sensor2.set(range, maxResults,
					GBObjectClass.ocFood.value);
		}
			return;
		case hcShotSensor: {
			double range = getHardwareNumber();
			int maxResults = getHardwareInteger(1);
			type.getHardware().sensor3.set(range, maxResults,
					GBObjectClass.ocShot.value);
		}
			return;
		case hcBlaster: {
			double damage = getHardwareNumber();
			double range = getHardwareNumber();
			int reload = getHardwareInteger();
			type.getHardware().blaster.set(damage, range, reload);
		}
			return;
		case hcGrenades: {
			double damage = getHardwareNumber();
			double range = getHardwareNumber();
			int reload = getHardwareInteger();
			type.getHardware().grenades.set(damage, range, reload);
		}
			return;
		case hcForceField: {
			double pwr = getHardwareNumber();
			double range = getHardwareNumber();
			type.getHardware().forceField.set(pwr, range);
		}
			return;
		case hcBomb:
			type.getHardware().SetBomb(getHardwareNumber());
			return;
		case hcSyphon: {
			double power = getHardwareNumber();
			type.getHardware().syphon.set(power, getHardwareNumber(1), false);
		}
			return;
		case hcEnemySyphon: {
			double power = getHardwareNumber();
			type.getHardware().enemySyphon.set(power, getHardwareNumber(1), true);
		}
			return;
		default:
			// hcNone was not included... do we even need it?
			break;
		}
		throw new GBUnknownHardwareError();
	}

	int getHardwareInteger() throws GBMissingHardwareArgumentError,
			GBHardwareArgumentError {
		if (tokens.size() == 0)
			throw new GBMissingHardwareArgumentError();
		String token = tokens.removeFirst();
		Integer n = StringUtilities.parseInt(token);
		if (n == null)
			throw new GBHardwareArgumentError();
		return n;
	}

	int getHardwareInteger(int defaultNum) throws GBHardwareArgumentError {
		if (tokens.size() == 0)
			return defaultNum;
		String token = tokens.removeFirst();
		Integer n = StringUtilities.parseInt(token);
		if (n == null)
			throw new GBHardwareArgumentError();
		return n;
	}

	double getHardwareNumber() throws GBMissingHardwareArgumentError,
			GBHardwareArgumentError {
		if (tokens.size() == 0)
			throw new GBMissingHardwareArgumentError();
		String token = tokens.removeFirst();
		Double n = StringUtilities.parseDouble(token);
		if (n == null)
			throw new GBHardwareArgumentError();
		return n;
	}

	double getHardwareNumber(double defaultNum) throws GBHardwareArgumentError {
		if (tokens.size() == 0)
			return defaultNum;
		String token = tokens.removeFirst();
		Double n = StringUtilities.parseDouble(token);
		if (n == null)
			throw new GBHardwareArgumentError();
		return n;
	}

	public SideReader(String _filename) {
		fileName = _filename;
	}

	void loadIt() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.contains(";")) // parse out explicit comments
					line = line.substring(0, line.indexOf(";"));
				if (line.length() > 0)
					try {
						processLine();
					} catch (Exception e) {
						GBError.NonfatalError("Error loading side from "
								+ fileName + ": " + e.getMessage()
								+ " at line " + lineno);
					}
			}
			br.close();
		} catch (IOException e) {
			GBError.NonfatalError("Error loading side from " + fileName + ": "
					+ e.getMessage());
		}
	}

	Side getSide() {
		if (state == GBElementType.etEnd && side != null) {
			if (side.getNumSeedTypes() == 0) // auto-generate seeding
				for (int i = 1; i <= side.getTypeCount(); ++i)
					if (side.getRobotType(i).getHardware().Bomb() == 0)
						side.addSeedID(side.getRobotType(i).getID());
			return side;
		} else
			throw new RuntimeException(
					"tried to get unfinished side from SideReader - was #end missing?");
	}

	public static Side loadFromFile(String filename) {
		try {
			SideReader reader = new SideReader(filename);
			reader.state = GBElementType.etNone;
			reader.loadIt();
			Side side = reader.getSide();
			side.filename = filename;
			for (RobotType botType : side.types) {
				botType.sample = new GBRobot(botType, new FinePoint(0, 0));
				botType.sample.setReloaded();
			}
			return side;
		} catch (Exception e) {
			GBError.NonfatalError("Error loading side: " + e.getMessage());
			return null;
		}
	}

};

// error classes //

class GBReaderError extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 868639067553966720L;

	@Override
	public String toString() {
		return "unspecified reader error";
	}

	GBReaderError() {
	}
};

class GBNoSuchElementError extends GBReaderError {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6461302010627743143L;

	@Override
	public String toString() {
		return "invalid element type";
	}

};

class GBMisplacedElementError extends GBReaderError {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6773763990635882381L;

	@Override
	public String toString() {
		return "an element appeared in an invalid place";
	}

};

class GBMissingElementError extends GBReaderError {
	/**
	 * 
	 */
	private static final long serialVersionUID = 133326259568207644L;

	@Override
	public String toString() {
		return "a required element is missing";
	}

};

class GBElementArgumentError extends GBReaderError {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1927401559638696895L;

	@Override
	public String toString() {
		return "invalid or forbidden element argument";
	}

};

class GBMissingElementArgumentError extends GBReaderError {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4160350834828591548L;

	@Override
	public String toString() {
		return "missing element argument";
	}

};

class GBForbiddenContentError extends GBReaderError {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1544294849800329702L;

	@Override
	public String toString() {
		return "content is not allowed here";
	}

};

class GBLineTooLongError extends GBReaderError {
	/**
	 * 
	 */
	private static final long serialVersionUID = 989008233723928310L;

	@Override
	public String toString() {
		return "input line too long";
	}

};

class GBUnknownHardwareError extends GBReaderError {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5518712721840211353L;

	@Override
	public String toString() {
		return "unknown hardware component";
	}

};

class GBHardwareArgumentError extends GBReaderError {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3101185710730165359L;

	@Override
	public String toString() {
		return "bad argument to a hardware component";
	}

};

class GBMissingHardwareArgumentError extends GBReaderError {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6411802234623163474L;

	@Override
	public String toString() {
		return "missing argument to a hardware component";
	}

};

class GBFileError extends GBReaderError {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2401737324445858461L;

	@Override
	public String toString() {
		return "file I/O error";
	}
};
