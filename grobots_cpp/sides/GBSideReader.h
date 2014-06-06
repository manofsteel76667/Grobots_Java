// GBSideReader.h
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef GBSideReader_h
#define GBSideReader_h

#include "GBPlatform.h"
#include "GBNumber.h"
#include "GBSide.h"

#if ! USE_MAC_IO
	#include <fstream>
#endif

typedef enum {
	etNone = 0, // Top of file. content: #side
	etSide,		// arg: name; content: #author #date #description #color #type
	etSeed,		// args: type ids
	etAuthor,	// arg: name; empty
	etDate,		// arg: date; empty
	etDescription,	// arg and content are optional and ignored.
	etColor,	// arg: color; empty
	etType,		// arg: name; content: #hardware #code #author? #date? #description #color #decoration
	etDecoration, // args: color, shape
	etHardware,	// no args; content: parse
	etCode,		// no args for now; content: parse
	etStart,	// arg: label-name; empty
	etVariable,	// args: name initial-value; empty
	etVectorVariable, // args: name initial-x initial-y; empty
	etConstant,	// args: name value; empty
	etEnd,		// no args; content forbidden
	kNumElementTypes
} GBElementType;

// Note that #author, #date, #description, and #color can appear in multiple places.
// These are conveniently distinguished by whether type is non-nil.

class GBStackBrainSpec;

const size_t kBufferSize = 512;

class GBSideReader {
#if USE_MAC_IO
	short refNum; // file reference number
#else
	std::ifstream fin;
#endif
	char buffer[kBufferSize];
	int bufpos, buflen;
	GBSide * side; // the side being read
	GBRobotType * type; // current type
	GBStackBrainSpec * brain;
	GBStackBrainSpec * commonBrain; //global code; may move to side
	GBElementType state; // what we're in
	string line; // current line
	int lineno; // current line number
	int pos; // where in the line
// reading
	void ProcessLine();
	void ProcessTag(GBElementType element); // process a tag line
	bool GetNextBuffer(); // returns false if EOF is reached
	bool GetNextLine(); // returns false if EOF is reached
// specific line kinds
	void ProcessHardwareLine();
	void ProcessCodeLine();
// hardware arguments
	long GetHardwareInteger();
	long GetHardwareInteger(const long defaultNum);
	GBNumber GetHardwareNumber();
	GBNumber GetHardwareNumber(const GBNumber & defaultNum);
	bool SkipWhitespace();
// constructors
	GBSideReader(const GBFilename & filename);
	~GBSideReader();
// loading
	void LoadIt();
	GBSide * Side();
public:
	static GBSide * Load(const GBFilename & filename);
};

#endif
