/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
// GBSound.h
// asynchronous sound output
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef GBSound_h
#define GBSound_h


typedef enum {
	siBirth = 0,
	siBlast, siSmallBlast,
	siGrenade,
	siTinyExplosion, siSmallExplosion,
	siMediumExplosion, siLargeExplosion,
	siEndRound,
	siExtinction,
	siBeep,
	kNumSounds
} GBSoundID;

// control
void SetupSound();
void CleanupSound();
void SetSoundActive(bool flag);
bool SoundActive();
void StartSound(GBSoundID which);

#endif

