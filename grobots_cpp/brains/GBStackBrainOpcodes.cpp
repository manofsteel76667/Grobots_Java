// GBStackBrainOpcodes.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#include "GBStackBrainOpcodes.h"

#if defined __MRC__
//something is broken about STL. The following definition is required for MrCpp
// to recognize that strings actually exist. This function is never called.
	static void stl_broken(string) {}
#endif

const string primitiveNames[kNumPrimitives] = {
	"nop",
// stack manipulation
	"drop", "2drop", "nip", "rdrop", "dropn",
	"swap", "subswap", "2swap", "rot", "rrot",
	"dup", "2dup", "tuck", "over", "2over",
	"stack", "stack-limit", "roll", "pick",
	">r", "r>",
// branches
	"jump", "call", "return",
	"ifg", "ifeg",
	"ifc", "ifec",
	"ifr",
	"nifg", "nifr", "nifc",
// arithmetic &c.
	"+", "-", "negate",
	"*", "/", "reciprocal", "mod", "rem",
	"square", "sqrt",
	"exponent",
	"is-integer",
	"floor", "ceiling", "round",
	"min", "max",
	"abs", "signum",
	"reorient",
	"sin", "cos", "tan",
	"arcsin", "arccos", "arctan",
	"random", "random-angle", "random-int", "random-bool",
// constants
	"pi", "2pi", "pi/2", "e",
	"epsilon", "infinity",
// vector operations
	"rect-to-polar", "polar-to-rect",
	"v+", "v-", "vnegate",
	"vs*", "vs/",
	"norm", "angle",
	"dot", "project", "cross", "unitize",
	"dist", "in-range",
	"v=", "v<>",
// comparisons and Boolean ops
	"=", "<>",
	"<", ">", "<=", ">=",
	"not", "and", "or", "xor", "nand", "nor",
	"ifev",
// misc external
	"print", "vprint", "beep", "pause", "stop",
	"sync",
// basic hardware
	"seek-location", "seek-moving-location", "restrict-position",
	"die",
	"store", "load", "vstore", "vload",
	"write", "read", "vwrite", "vread",
	"messages", "send", "receive", "clear-messages", "skip-messages",
	"type-population",
	"autoconstruct", "balance-type",
// sensors
	"fire-robot-sensor", "fire-food-sensor", "fire-shot-sensor",
	"next-robot", "next-food", "next-shot",
	"periodic-robot-sensor", "periodic-food-sensor", "periodic-shot-sensor",
// weapons
	"fire-blaster", "fire-grenade",
	"lead-blaster", "lead-grenade",
	"set-force-field",
};

const string hardwareVariableNames[kNumHardwareVariables] = {
// 
	"time", "time-limit",
	"world-width", "world-height",
	"radius", "mass", "speed",
	"processor", "remaining",
	"side", "type", "id", "parent-id",
	"population",
	"engine-power", "engine-max-power",
	"flag",
	"collision", "friendly-collision", "enemy-collision", "food-collision", "shot-collision", "wall-collision",
// energy
	"energy", "max-energy",
	"solar-cells",
	"eater", "eaten",
	"syphon-max-rate", "syphon-range", "syphon-distance", "syphon-direction", "syphon-rate", "syphoned",
	"enemy-syphon-max-rate", "enemy-syphon-range", "enemy-syphon-distance",
		"enemy-syphon-direction", "enemy-syphon-rate", "enemy-syphoned",
	"constructor-max-rate", "constructor-rate", "constructor-type",
		"constructor-progress", "constructor-remaining",
		"child-id",
// robot sensor
	"robot-sensor-range", "robot-sensor-firing-cost",
	"robot-sensor-focus-distance", "robot-sensor-focus-direction",
	"robot-sensor-sees-friends", "robot-sensor-sees-enemies",
	"robot-sensor-time", "robot-found", "robot-distance", "robot-direction",
	"robot-side", "robot-radius", "robot-mass", "robot-energy",
	"robot-type", "robot-ID", "robot-shield-fraction",
	"robot-bomb", "robot-reloading", "robot-flag",
	"robot-distance-overall", "robot-direction-overall",
	"current-robot-result", "num-robot-results", "max-robot-results",
// food sensor
	"food-sensor-range", "food-sensor-firing-cost",
	"food-sensor-focus-distance", "food-sensor-focus-direction",
	"food-sensor-time", "food-found", "food-distance", "food-direction",
	"food-side", "food-radius", "food-mass", "food-energy",
	"food-distance-overall", "food-direction-overall",
	"current-food-result", "num-food-results", "max-food-results",
// shot sensor
	"shot-sensor-range", "shot-sensor-firing-cost",
	"shot-sensor-focus-distance", "shot-sensor-focus-direction",
	"shot-sensor-sees-friendly", "shot-sensor-sees-enemy",
	"shot-sensor-time", "shot-found", "shot-distance", "shot-direction",
	"shot-side", "shot-radius", "shot-power", "shot-type",
	"shot-distance-overall", "shot-direction-overall",
	"current-shot-result", "num-shot-results", "max-shot-results",
 // defenses
	"armor", "max-armor",
	"repair-rate", "max-repair-rate",
	"shield", "max-shield", "shield-fraction",
	"last-hit",
 // weapons
	"blaster-damage", "blaster-range", "blaster-speed", "blaster-lifetime",
		"blaster-reload-time", "blaster-firing-cost", "blaster-cooldown",
	"grenades-damage", "grenades-range", "grenades-speed", "grenades-lifetime",
		"grenades-reload-time", "grenades-firing-cost", "grenades-cooldown", "grenades-radius",
	"force-field-max-power", "force-field-range", "force-field-distance", "force-field-direction",
		"force-field-power", "force-field-angle", "force-field-radius"
};

const string hardwareVectorNames[kNumHardwareVectors] = {
	"world-size",
	"position", "velocity",
	"engine-velocity",
// sensors
	"robot-position", "robot-velocity", "robot-position-overall",
	"food-position", "food-velocity", "food-position-overall",
	"shot-position", "shot-velocity", "shot-position-overall"
};

const string cWordNames[kNumCWords] = {
	"c-nop",
	"if", "nif", "else", "then", "and-if", "and-nif", "celse",
	"do", "loop", "forever", "while", "until", "while-loop", "until-loop",
};

