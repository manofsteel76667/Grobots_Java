;Macros to replace the most common elements of C++ syntax with its Java equivalent.
;Assumes that any file found is the corresponding .h and .cpp files merged together
;Does not mess with naming (refactoring will be used for that later)

!F1::
;Standard variable types
	replace("bool "," boolean ")
	replace("string ","String ")
	replace("std::string","String")
	replace("long "," int ") ;design choice... we don't really need 64-bit ints anywhere
	replace("short "," int ")
	replace("std::vector","List")
;Typedef'd variable types
	replace("GBNumber","double")
	replace("GBEnergy","double")
	replace("GBPower","double")
	replace("GBDamage","double")
	replace("GBDistance","double")
	replace("GBMass","double")
	replace("GBSpeed","double")
	replace("GBForceScalar","double")
	replace("GBAccelerationScalar","double")
	replace("GBMomentumScalar","double")
	replace("GBRatio","double")
	replace("GBAngle","double")
	replace("GBFinePoint","FinePoint")
	replace("GBPosition","FinePoint")
	replace("GBVelocity","FinePoint")
	replace("GBMomentum","FinePoint")
	replace("GBAcceleration","FinePoint")
	replace("GBForce ","FinePoint ")
	replace("GBVector ","FinePoint ")	
	replace("GBFrames","int")
	replace("GBInstructionCount","int")
;The const keyword in various forms
;Careful not to break the 'constructor' hardware type
	replace(" const "," ")
	replace(" const`;"," `;")
	replace(" const("," (")
	replace("(const ", "( ")
;By this point there should hopefully be no more const keywords in parameters, leaving only declared constants
;This converts leftover constants to public static finals, but will also introduce some errors if it's not really a constant.  
;Those have to be removed manually but generally the benefit is greater than the downside.
	replace("const ","public static final ")
;Exceptions, nulls, and oddballs
	replace("throw ","throw new ")
	replace("nil","null")
	replace("->",".")
	replace("public:","//public:")
	replace("private:","//private:")
	replace("protected:","//protected:")
	replace(" : public "," extends ")
	replace("<int>","<Integer>")
;Math
	replace(" abs("," Math.abs(")
	replace(" min("," Math.min(")
	replace(" max("," Math.max(")
	replace(" ceil("," Math.ceil(")
	replace(" floor("," Math.floor(")
	replace(" round("," Math.round(")
	replace("(abs(","(Math.abs(")
	replace("(min(","(Math.min(")
	replace("(max(","(Math.max(")
	replace("(ceil(","(Math.ceil(")
	replace("(floor(","(Math.floor(")
	replace("(round(","(Math.round(")
;UI Stuff
	replace("GBRect","Rectangle")
	replace("GBMilliseconds()","System.currentTimeMillis()")
	replace("GBMilliseconds","long")
	replace("GBChangeCount","long")
	replace"GBBitMap","BufferedImage")
return

;use Eclipse's global find-and-replace.  All instances of x become y 
replace(_find,_replace)
{
	send ^f
	send %_find%
	send {tab}
	send %_replace%
	send !a
	send {esc}
	return
}

