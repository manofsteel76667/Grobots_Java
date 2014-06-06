// GBSideReader.cpp
// parser for side files and hardware
// Grobots (c) 2002-2007 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#include "GBSideReader.h"
#include "GBStringUtilities.h"
#include "GBRobotType.h"
#include "GBSide.h"
#include "GBStackBrainSpec.h"
#include <ctype.h>

#if USE_MAC_IO
	#if CARBON
		#include <Carbon/Carbon.h>
	#else
		#include <Devices.h>
		#include <Errors.h>
	#endif
#else
	using std::ifstream;
#endif

const string tagNames[kNumElementTypes] = {
	"none-illegal",
	"side", "seed",
	"author", "date", "comment", "color",
	"type", "decoration", "hardware", "code",
	"start", "var", "vector", "const",
	"end"
};

const string decorationNames[kNumRobotDecorations] = {
	"none",
	"dot", "circle", "square", "triangle",
	"cross", "x", "hline", "vline", "slash", "backslash"
};

enum {
	hcNone = 0,
	hcProcessor, hcRadio,
	hcEngine,
	hcConstructor,
	hcEnergy, hcSolarCells, hcEater,
	hcArmor, hcRepairRate, hcShield,
	hcRobotSensor, hcFoodSensor, hcShotSensor,
	hcBlaster, hcGrenades, hcForceField, hcBomb,
	hcSyphon, hcEnemySyphon,
	kNumHardwareComponents
};

const string hardwareNames[kNumHardwareComponents] = {
	"none_illegal",
	"processor", "radio",
	"engine",
	"constructor",
	"energy", "solar-cells", "eater",
	"armor", "repair-rate", "shield",
	"robot-sensor", "food-sensor", "shot-sensor",
	"blaster", "grenades", "force-field", "bomb",
	"syphon", "enemy-syphon"
};

// error classes //

class GBReaderError : public GBError {
public:
	GBReaderError();
	~GBReaderError();
	string ToString() const;
};

class GBNoSuchElementError : public GBReaderError {
public:
	string ToString() const;
};

class GBMisplacedElementError : public GBReaderError {
public:
	string ToString() const;
};

class GBMissingElementError : public GBReaderError {
public:
	string ToString() const;
};

class GBElementArgumentError : public GBReaderError {
public:
	string ToString() const;
};

class GBMissingElementArgumentError : public GBReaderError {
public:
	string ToString() const;
};

class GBForbiddenContentError : public GBReaderError {
public:
	string ToString() const;
};

class GBLineTooLongError : public GBReaderError {
public:
	string ToString() const;
};

class GBUnknownHardwareError : public GBReaderError {
public:
	string ToString() const;
};

class GBHardwareArgumentError : public GBReaderError {
public:
	string ToString() const;
};

class GBMissingHardwareArgumentError : public GBReaderError {
public:
	string ToString() const;
};

class GBFileError : public GBError {
public:
	GBFileError();
	~GBFileError();
	string ToString() const;
};


GBReaderError::GBReaderError() {}
GBReaderError::~GBReaderError() {}

string GBReaderError::ToString() const {
	return "unspecified reader error";
}

string GBNoSuchElementError::ToString() const {
	return "invalid element type";
}

string GBMisplacedElementError::ToString() const {
	return "an element appeared in an invalid place";
}

string GBMissingElementError::ToString() const {
	return "a required element is missing";
}

string GBElementArgumentError::ToString() const {
	return "invalid or forbidden element argument";
}

string GBMissingElementArgumentError::ToString() const {
	return "missing element argument";
}

string GBForbiddenContentError::ToString() const {
	return "content is not allowed here";
}

string GBLineTooLongError::ToString() const {
	return "input line too long";
}

string GBUnknownHardwareError::ToString() const {
	return "unknown hardware component";
}

string GBHardwareArgumentError::ToString() const {
	return "bad argument to a hardware component";
}

string GBMissingHardwareArgumentError::ToString() const {
	return "missing argument to a hardware component";
}

GBFileError::GBFileError() {}
GBFileError::~GBFileError() {}

string GBFileError::ToString() const {
	return "file I/O error";
}

// GBSideReader //

void GBSideReader::ProcessLine() {
	if ( SkipWhitespace() )
		return;
	if ( line[pos] == '#' ) {
		string token;
		++ pos;
		if ( ! ExtractToken(token, line, pos) )
			throw GBGenericError("# but no token - huh?");
	// identify tag
		for ( int i = 1; i < kNumElementTypes; i ++ )
			if ( NamesEquivalent(token, tagNames[i]) ) {
				ProcessTag((GBElementType)i);
				return;
			}
		throw GBNoSuchElementError();
	} else // content reached
		switch ( state ) {
			case etNone:
			case etEnd:
				throw GBForbiddenContentError();
				return;
			case etHardware:
				ProcessHardwareLine();
				return;
			case etCode:
				ProcessCodeLine();
				return;
			case etSide: case etType: // side and type act like description,
			case etDescription:	// so #description is now optional
				return; // do nothing
			case etAuthor: case etDate:
			case etStart: case etVariable: case etConstant:
			case etDecoration:
			default:
				throw GBReaderError(); // should never be in these states
		}
}

void GBSideReader::ProcessTag(GBElementType element) {
// clean up type
	if ( type && (element == etType || element == etEnd) ) {
		if ( brain ) {
			brain->Check();
			type->SetBrain(brain);
			brain = nil;
		}
		side->AddType(type);
		type = nil;
		if ( ! side->GetFirstType() )
			throw GBGenericError("type not showing up in side");
	}
// legality check
	if ( state == etEnd )
		throw GBForbiddenContentError();
	else if ( state == etNone && element != etSide )
		throw GBMisplacedElementError();
// process it
	switch ( element ) {
		case etSide:
			if ( state == etNone ) {
				string name;
				if ( ! ExtractRest(name, line, pos) )
					throw GBMissingElementArgumentError();
				if ( side )
					throw GBGenericError("SideReader already had side?!");
				side = new GBSide();
				if ( ! side )
					throw GBOutOfMemoryError();
				side->SetName(name);
				state = etSide;
			} else
				throw GBMisplacedElementError();
			break;
		case etSeed:
			if ( type ) throw GBMisplacedElementError();
			{
				string token;
				if ( ! ExtractToken(token, line, pos) )
					throw GBMissingElementArgumentError();
				do {
					long id;
					if ( ! ParseInteger(token, id) )
						throw GBElementArgumentError();
					side->AddSeedID(id);
				} while ( ExtractToken(token, line, pos) );
			} break;
		case etAuthor:
			if ( ! type ) {
				string author;
				if ( ! ExtractRest(author, line, pos) )
					throw GBMissingElementArgumentError();
				if ( ! side )
					throw GBGenericError("SideReader missing side");
				side->SetAuthor(author);
			} break;
		case etDate:
			break; // ignore
		case etDescription:
			state = etDescription;
			break;
		case etColor:
			{
				string token;
				if ( ! ExtractToken(token, line, pos) )
					throw GBMissingElementArgumentError();
				GBColor color;
				if ( ! ParseColor(token, color) )
					throw GBElementArgumentError();
				if ( type )
					type->SetColor(color);
				else
					side->SetColor(color);
			}
			break;
		case etType:
			if ( !type && brain ) {
				commonBrain = brain;
				brain = nil;
			}
			{
				string name;
				if ( ! ExtractRest(name, line, pos) )
					throw GBMissingElementArgumentError();
				if ( ! side )
					throw GBGenericError("SideReader missing side");
				type = new GBRobotType(side);
				type->SetName(name);
				state = etType;
			}
			break;
		case etDecoration:
			if ( ! type ) throw GBMisplacedElementError();
			{
				string token;
				if ( ! ExtractToken(token, line, pos) )
					throw GBMissingElementArgumentError();
				GBColor color;
				if ( ! ParseColor(token, color) )
					throw GBElementArgumentError();
				if ( ! ExtractToken(token, line, pos) )
					throw GBMissingElementArgumentError();
				GBRobotDecoration dec = kNumRobotDecorations;
				for ( int i = 0; i < kNumRobotDecorations; i ++ )
					if ( NamesEquivalent(token, decorationNames[i]) ) {
						dec = (GBRobotDecoration)i;
						break;
					}
				if ( dec == kNumRobotDecorations ) throw GBElementArgumentError();
				type->SetDecoration(dec, color);
			}
			break;
		case etHardware:
			if ( type )
				state = etHardware;
			else
				throw GBMisplacedElementError();
			break;
		case etCode:
			if ( ! brain ) {
				if (type && commonBrain) {
					brain = new GBStackBrainSpec(*commonBrain);
				//default to beginning of type-specific code
					GBSymbolIndex label = brain->AddGensym("start");
					brain->ResolveGensym(label);
					brain->SetStartingLabel(label);
				} else
					brain = new GBStackBrainSpec();
			}
			state = etCode;
			break;
		case etStart:
			if ( state == etCode ) {
				string name;
				if ( ExtractToken(name, line, pos) )
					brain->SetStartingLabel(brain->LabelReferenced(name));
				else {
					GBSymbolIndex label = brain->AddGensym("start");
					brain->ResolveGensym(label);
					brain->SetStartingLabel(label);
				}
			} else
				throw GBMisplacedElementError();
			break;
		case etVariable:
		case etConstant:
			if ( state == etCode ) {
				string name;
				if ( ! ExtractToken(name, line, pos) )
					throw GBMissingElementArgumentError();
				string val;
				GBNumber num;
				if ( ExtractToken(val, line, pos) ) {
					if ( ! ParseNumber(val, num) )
						throw GBElementArgumentError();
				} else if ( element == etConstant )
					throw GBMissingElementArgumentError();
				if ( element == etVariable )
					brain->AddVariable(name, num);
				else
					brain->AddConstant(name, num);
			} else
				throw GBMisplacedElementError();
			break;
		case etVectorVariable:
			if ( state == etCode ) {
				string name;
				if ( ! ExtractToken(name, line, pos) )
					throw GBMissingElementArgumentError();
				string val;
				GBVector v;
				if ( ExtractToken(val, line, pos) ) {
					if ( ! ParseNumber(val, v.x) )
						throw GBElementArgumentError();
					if ( ExtractToken(val, line, pos) ) {
						if ( ! ParseNumber(val, v.y) )
							throw GBElementArgumentError();
					} else
						throw GBMissingElementArgumentError();
				}
				brain->AddVectorVariable(name, v);
			} else
				throw GBMisplacedElementError();
			break;
		case etEnd:
			state = etEnd;
			break;
		default:
			throw GBNoSuchElementError();
	}
}

bool GBSideReader::GetNextBuffer() {
#if USE_MAC_IO
	IOParam params;
// set up params
	params.ioCompletion = nil;
	params.ioRefNum = refNum;
	params.ioBuffer = buffer;
	params.ioReqCount = kBufferSize;
	params.ioPosMode = fsAtMark | 0x80 | ('\n' << 8); //TODO fix
	params.ioPosOffset = 0;
// read
	PBReadSync((ParmBlkPtr)(&params));
	buflen = params.ioActCount;
	if ( params.ioResult == eofErr ) {
		if ( ! buflen )
			return false;
	} else if ( params.ioResult )
		 throw GBFileError();
#else
	fin.read(buffer, kBufferSize);
	buflen = fin.gcount();
	if ( ! buflen && fin.eof() )
		return false;
	if ( fin.fail() && ! buflen )
		throw GBFileError();
#endif
	bufpos = 0;
	return buflen > 0;
}

bool GBSideReader::GetNextLine() {
	line = "";
	pos = 0;
	while ( bufpos < buflen || GetNextBuffer() ) {
		char c = buffer[bufpos++];
		if ( '\n' == c || '\r' == c ) {
			lineno++;
			return true;
		} else
			line += c;
	}
	if ( line.length() > 0 ) {
		lineno++;
		return true;
	}
	return false;
}

void GBSideReader::ProcessCodeLine() {
	if ( ! brain )
		throw GBGenericError("can't compile code without a brain");
	brain->ParseLine(line, lineno);
}

void GBSideReader::ProcessHardwareLine() {
	string name;
	if ( ! ExtractToken(name, line, pos) )
		return;
	for ( int i = 1; i < kNumHardwareComponents; i ++ )
		if ( NamesEquivalent(name, hardwareNames[i]) )
			switch ( i ) {
				case hcProcessor: {
					GBInstructionCount speed = GetHardwareInteger();
					long mem = GetHardwareInteger(0);
					type->Hardware().SetProcessor(speed, mem);
					} return;
				case hcRadio:
					return; //obsolete but remains for compatibility
				case hcEngine:
					type->Hardware().SetEngine(GetHardwareNumber());
					return;
				case hcConstructor:
					type->Hardware().constructor.Set(GetHardwareNumber());
					return;
				case hcEnergy:
					{
						GBEnergy max = GetHardwareNumber();
						GBEnergy initial = GetHardwareNumber();
						type->Hardware().SetEnergy(max, initial);
					}
					return;
				case hcSolarCells:
					type->Hardware().SetSolarCells(GetHardwareNumber());
					return;
				case hcEater:
					type->Hardware().SetEater(GetHardwareNumber());
					return;
				case hcArmor:
					type->Hardware().SetArmor(GetHardwareNumber());
					return;
				case hcRepairRate:
					type->Hardware().SetRepairRate(GetHardwareNumber());
					return;
				case hcShield:
					type->Hardware().SetShield(GetHardwareNumber());
					return;
				case hcRobotSensor: {
						GBDistance range = GetHardwareNumber();
						long maxResults = GetHardwareInteger(1);
						type->Hardware().sensor1.Set(range, maxResults, ocRobot);
					} return;
				case hcFoodSensor: {
						GBDistance range = GetHardwareNumber();
						long maxResults = GetHardwareInteger(1);
						type->Hardware().sensor2.Set(range, maxResults, ocFood);
					} return;
				case hcShotSensor: {
						GBDistance range = GetHardwareNumber();
						long maxResults = GetHardwareInteger(1);
						type->Hardware().sensor3.Set(range, maxResults, ocShot);
					} return;
				case hcBlaster: {
						GBDamage damage = GetHardwareNumber();
						GBDistance range = GetHardwareNumber();
						long reload = GetHardwareInteger();
						type->Hardware().blaster.Set(damage, range, reload);
					} return;
				case hcGrenades: {
						GBDamage damage = GetHardwareNumber();
						GBDistance range = GetHardwareNumber();
						long reload = GetHardwareInteger();
						type->Hardware().grenades.Set(damage, range, reload);
					} return;
				case hcForceField: {
						GBPower pwr = GetHardwareNumber();
						GBDistance range = GetHardwareNumber();
						type->Hardware().forceField.Set(pwr, range);
					} return;
				case hcBomb:
					type->Hardware().SetBomb(GetHardwareNumber());
					return;
				case hcSyphon: {
						GBDistance power = GetHardwareNumber();
						type->Hardware().syphon.Set(power, GetHardwareNumber(1), false);
					} return;
				case hcEnemySyphon: {
						GBDistance power = GetHardwareNumber();
						type->Hardware().enemySyphon.Set(power, GetHardwareNumber(1), true);
					} return;
			}
	throw GBUnknownHardwareError();
}

long GBSideReader::GetHardwareInteger() {
	string token;
	if ( ! ExtractToken(token, line, pos) )
		throw GBMissingHardwareArgumentError();
	long n;
	if ( ! ParseInteger(token, n) )
		throw GBHardwareArgumentError();
	return n;
}

long GBSideReader::GetHardwareInteger(const long defaultNum) {
	string token;
	if ( ! ExtractToken(token, line, pos) )
		return defaultNum;
	long n;
	if ( ! ParseInteger(token, n) )
		throw GBHardwareArgumentError();
	return n;
}

GBNumber GBSideReader::GetHardwareNumber() {
	string token;
	if ( ! ExtractToken(token, line, pos) )
		throw GBMissingHardwareArgumentError();
	GBNumber num;
	if ( ! ParseNumber(token, num) )
		throw GBHardwareArgumentError();
	return num;
}

GBNumber GBSideReader::GetHardwareNumber(const GBNumber & defaultNum) {
	string token;
	if ( ! ExtractToken(token, line, pos) )
		return defaultNum;	
	GBNumber num;
	if ( ! ParseNumber(token, num) )
		throw GBHardwareArgumentError();
	return num;
}

// returns true if EOL or semicolon reached.
bool GBSideReader::SkipWhitespace() {
	while ( pos < line.length() && line[pos] != ';' ) {
		if ( ! isspace(line[pos]) )
			return false;
		++ pos;
	}
	return true;
}

GBSideReader::GBSideReader(const GBFilename & filename)
#if USE_MAC_IO
	: refNum(0),
#else
	: fin(filename.c_str()),
#endif
	bufpos(0), buflen(0),
	side(nil), type(nil), brain(nil), commonBrain(nil),
	state(etNone),
	lineno(0), pos(0)
{
#if USE_MAC_IO
	if ( HOpen(filename.vRefNum, filename.parID, filename.name, fsRdPerm, &refNum) )
		throw GBFileError();
#else
	//fin.open(filename.c_str(), ifstream::in);
	if ( fin.fail() || ! fin.is_open() || fin.eof() ) throw GBFileError();
#endif
}

GBSideReader::~GBSideReader() {
	if ( state != etEnd )
		delete side;
	delete type;
	if ( commonBrain != brain ) delete commonBrain;
	delete brain;
#if USE_MAC_IO
	if ( refNum && FSClose(refNum) )
		throw GBFileError();
#else
	fin.close();
#endif
}

void GBSideReader::LoadIt() {
	while ( GetNextLine() ) {
		try {
			ProcessLine();
		} catch ( GBError & err ) {
			NonfatalError(string("Error loading side: ") + err.ToString() + " at line " + ToString(lineno));
		}
	}
}

GBSide * GBSideReader::Side() {
	if ( state == etEnd && side ) {
		if ( side->NumSeedTypes() == 0 )  // auto-generate seeding
			for ( GBRobotType * type = side->GetFirstType(); type != nil; type = type->next )
				if ( ! type->Hardware().Bomb() )
					side->AddSeedID(type->ID());
		return side;
	} else
		throw GBGenericError("tried to get unfinished side from SideReader - was #end missing?");
	return nil; // not reached
}

GBSide * GBSideReader::Load(const GBFilename & filename){
	try {
		GBSideReader reader(filename);
		reader.LoadIt();
		GBSide * side = reader.Side();
		side->filename = filename;
		return side;
	} catch ( GBError & err ) {
		NonfatalError("Error loading side: " + err.ToString());
	} catch ( GBAbort & ) {}
	return nil;
}

