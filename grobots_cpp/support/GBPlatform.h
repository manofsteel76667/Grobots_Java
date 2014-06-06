// GBPlatform.h
// Platform-specificity definitions, mostly conditional macros.
// Grobots (c) 2002-2006 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef GBPlatform_h
#define GBPlatform_h


#if (defined(__APPLE__) && defined(__GNUC__)) || defined(__MACOSX__)
	#define MAC 1
	#define MAC_OS_X 1
	#define CARBON 1
	
#elif defined __MRC__  // MrCpp
	#define MAC 1
	#if CARBON
		//#undef TARGET_API_MAC_CARBON
		//#define TARGET_API_MAC_CARBON 1
	#endif
#elif defined __unix__ || defined __MACH__
	#ifndef UNIX
		#define UNIX 1
	#endif
#elif defined _MSC_VER // MS Visual C++
	#define WINDOWS 1
	#if defined _MFC_VER // Microsoft Foundation Classes
		#define MFCWIN 1
	#endif
#else
	#warning "Can't tell what compiler is being used."
#endif

#ifndef nil
	#define nil 0 // maybe not needed here
#endif

#if _MSC_VER
	typedef __int64 GBLongLong;
	#define NOMINMAX
#else
	typedef long long GBLongLong;
#endif

#endif
