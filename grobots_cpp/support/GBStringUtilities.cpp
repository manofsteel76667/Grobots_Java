// GBStringUtilities.cpp
// general-purpose parsing and string-formatting code
// Grobots (c) 2002-2006 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#include "GBStringUtilities.h"
#include <ctype.h>

#if MAC
void ToPascalString(const string & s, Str255 ps) {
	ps[0] = s.length() > 255 ? 255 : s.length();
	for ( int i = 0; i < ps[0]; ++ i )
		ps[i + 1] = s[i];
}

string FromPascalString(ConstStr255Param ps) {
	return string((const char *)&(ps[1]), ps[0]);
}
#endif

// slow but who cares?
string ToString(long n) {
	string s;
	if ( n < 0 ) { s = '-'; n = - n; }
	if ( n >= 10 ) s += ToString(n / 10);
	s += (char)('0' + n % 10);
	return s;
}

string ToString(GBNumber n, int digitsAfterDP, bool trailingZeroes) {
	long scale = 1;
	for ( int i = digitsAfterDP; i > 0; i -- ) scale *= 10;
	long actual = floor(abs(n)) * scale + round(fpart(abs(n)) * scale);
	string result;
	if ( n < 0 ) result += '-';
	result += ToString(actual / scale); // integer part
	// FIXME: still shows trailing zeros if any fractional part.
	if ( digitsAfterDP && (trailingZeroes || fpart(n)) ) {
		result += '.';
		string frac = ToString(actual % scale);
		if ( frac.length() < digitsAfterDP )
			result.append(digitsAfterDP - frac.length(), '0');
		result += frac;
	}
	return result;
}

string ToString(const GBFinePoint & v, int digitsAfterDP, bool trailingZeroes) {
	return string("<") + ToString(v.x, digitsAfterDP, trailingZeroes) + ", "
		+ ToString(v.y, digitsAfterDP, trailingZeroes) + '>';
}

string ToPercentString(float f, int digitsAfterDP, bool trailingZeroes) {
	return ToString(f * 100.0, digitsAfterDP, trailingZeroes) + '%';
}

string ToPercentString(GBNumber n, int digitsAfterDP, bool trailingZeroes) {
	return ToString(n * 100, digitsAfterDP, trailingZeroes) + '%';
}

string ToPercentString(long num, long denom, int digitsAfterDP, bool trailingZeroes) {
	return ToString((float)num / denom * 100, digitsAfterDP, trailingZeroes) + '%';
}

bool NamesEquivalent(const string & a, const string & b) {
	if ( a.length() != b.length() )
		return false;
	for ( int i = 0; i < a.length(); i ++ )
		if ( tolower(a[i]) != tolower(b[i]) )
			return false;
	return true;
}

unsigned short HexDigitValue(char digit) {
	if ( isdigit(digit) )
		return digit - '0';
	return toupper(digit) - 'A' + 10;
}

float HexDigitIntensity(char digit) {
	return HexDigitValue(digit) / 15.0;
}

float HexDigitsIntensity(char d1, char d2) {
	return (HexDigitValue(d1) * 16 + HexDigitValue(d2)) / 255.0;
}

// return whether this is an integer
bool ParseInteger(const string & token, long & number) {
	bool negated = false;
	short digits = 0; // how many digits have been processed
	number = 0;
	for ( short i = 0; i < token.length(); ++ i ) {
		switch ( token[i] ) {
			case '+':
				if ( i > 0 ) return false;
				break;
			case '-':
				if ( i > 0 ) return false;
				negated = true;
				break;
			case '0': case '1': case '2': case '3': case '4':
			case '5': case '6': case '7': case '8': case '9':
				++ digits;
				number = number * 10 + (token[i] - '0');
				break;
			default:
				return false;
		}
	}
	if ( digits <= 0 ) return false;
	if ( negated ) number = - number;
	return true;
}

// return whether this is a number
bool ParseNumber(const string & token, GBNumber & number) {
	bool negated = false;
	short decimal = 0; // how many digits past the radix point the next digit is
	short digits = 0; // how many digits have been processed
	number = 0;
	for ( short i = 0; i < token.length(); ++ i ) {
		switch ( token[i] ) {
			case '+':
				if ( i > 0 ) return false;
				break;
			case '-':
				if ( i > 0 ) return false;
				negated = true;
				break;
			case '.':
				if ( decimal )
					return false;
				else
					decimal = 1;
				break;
			case '0': case '1': case '2': case '3': case '4':
			case '5': case '6': case '7': case '8': case '9':
				++ digits;
				if ( decimal ) {
					GBNumber temp = token[i] - '0';
					for ( int x = 0; x < decimal; x ++ )
						temp /= 10;
					number += temp;
					++ decimal;
				} else
					try {
						number = number * 10 + (token[i] - '0');
					} catch ( GBOverflowError & ) { return false; }
				break;
			default:
				return false;
		}
	}
	if ( digits <= 0 ) return false;
	if ( negated ) number = - number;
	return true;
}

bool ParseColor(const string & token, GBColor & color) {
// could do named colors, but not urgent
// check digits
	for ( int i = 0; i < token.length(); i ++ )
		if ( ! isxdigit(token[i]) )
			return false;
// check length
	if ( token.length() == 3 ) {
		color.Set(HexDigitIntensity(token[0]),
				HexDigitIntensity(token[1]),
				HexDigitIntensity(token[2]));
		return true;
	} else if ( token.length() == 6 ) {
		color.Set(HexDigitsIntensity(token[0], token[1]),
				HexDigitsIntensity(token[2], token[3]),
				HexDigitsIntensity(token[4], token[5]));
		return true;
	}
	return false;
}

// copy the next token in line after cur into token. Advance cur.
// Recognise semicolon comments. Return whether a token was found.
bool ExtractToken(string & token, const string & line, int & cur) {
	bool intoken = false;
	while ( cur < line.length() && line[cur] != ';' ) {
		if ( isspace(line[cur]) ) {
			if ( intoken )
				return true;
		} else { // a real character
			if ( ! intoken ) {
				intoken = true;
				token = "";
			}
			token += line[cur];
		}
		++ cur;
	}
	return intoken;
}

// skip whitespace, then copy the rest of line after cur into token. Advance cur.
// Recognise semicolon comments. Return whether any non-whitespace was found.
// Minor bug: currently leaves trailing whitespace in.
bool ExtractRest(string & rest, const string & line, int & cur) {
	bool intoken = false;
	while ( cur < line.length() && line[cur] != ';' && line[cur] != '\r' && line[cur] != '\n' ) {
		if ( intoken || ! isspace(line[cur]) ) {
			if ( ! intoken ) {
				intoken = true;
				rest = "";
			}
			rest += line[cur];
		}
		++ cur;
	}
	return intoken;
}

