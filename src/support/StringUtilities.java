package support;

public class StringUtilities {

	/*These might be necessary whenever we get around to adding the graphics back.  Or maybe not.
	 * 
	 * public static unsigned short HexDigitValue(char digit) {
		if ( isdigit((unsigned char)digit) )
			return digit - '0';
		return toupper(digit) - 'A' + 10;
	}

	public static float HexDigitIntensity(char digit) {
		return HexDigitValue(digit) / 15.0;
	}

	public static float HexDigitsIntensity(char d1, char d2) {
		return (HexDigitValue(d1) 16 + HexDigitValue(d2)) / 255.0;
	}


	public static boolean ParseColor(String token, GBColor color) {
	// could do named colors, but not urgent
	// check digits
		for ( int i = 0; i < token.length(); i ++ )
			if ( ! isxdigit((unsigned char)token[i]) )
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
	}*/
	
	public static Integer parseInt(String str){
		try{
			return Integer.parseInt(str);
		}
		catch(NumberFormatException nfe) {
			return null;
		}
	}
	
	public static Double parseDouble(String str){
		try{
			return Double.parseDouble(str);
		}
		catch(NumberFormatException nfe) {
			return null;
		}
	}
}
