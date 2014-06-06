// GBSound.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.


#include "GBSound.h"
#include "GBErrors.h"
#include "GBPlatform.h"

#if MAC
	#if MAC_OS_X
		#include <Carbon/Carbon.h>
	#else
		#include <Sound.h>
		#include <Resources.h>
	#endif


const int kNumSoundChannels = 4;

class GBSoundChannel {
	SndChannelPtr channel;
public:
	int priority;
public:
	GBSoundChannel();
	~GBSoundChannel();
	bool Busy();
	void StartSound(GBSoundID id);
	void StopSound();
};

// globals

bool gSoundActive;
GBSoundChannel * gSoundChannels;
SndListHandle gSounds[kNumSounds];

const short kSoundResourceIDs[kNumSounds] = {
	300,
	400, 410,
	450,
	200, 210, 220, 230, // explosions
	500, 600,
	1000
};

const int kSoundPriorities[kNumSounds] = {
	10,
	30, 15,
	20,
	5, 25, 75, 100,
	500, 300,
	200
};


void SetupSound() {
	gSoundActive = false;
// make channels
	gSoundChannels = new GBSoundChannel[kNumSoundChannels];
// load sounds
	for ( int i = 0; i < kNumSounds; i ++ )
		gSounds[i] = (SndListHandle)GetResource('snd ', kSoundResourceIDs[i]);
}

void CleanupSound() {
	delete [] gSoundChannels;
}

void SetSoundActive(bool flag) {
// stop if needed
	if ( gSoundActive && ! flag )
		for ( int i = 0; i < kNumSoundChannels; i ++ )
			gSoundChannels[i].StopSound();
	gSoundActive = flag;
}

bool SoundActive() {
	return gSoundActive;
}

void StartSound(GBSoundID which) {
	if ( ! gSoundActive ) return;
// find lowest-priority channel
	int lowest = 0;
	for ( int i = 0; i < kNumSoundChannels && gSoundChannels[lowest].priority; i ++ )
		if ( ! gSoundChannels[i].Busy() ) {
			lowest = i;
			break;
		} else if ( gSoundChannels[i].priority < gSoundChannels[lowest].priority )
			lowest = i;
// play
	if ( gSoundChannels[lowest].priority <= kSoundPriorities[which] )
		gSoundChannels[lowest].StartSound(which);
}

GBSoundChannel::GBSoundChannel()
	: channel(nil), priority(0)
{
	if ( SndNewChannel(&channel, sampledSynth, initMono, nil) )
		throw GBGenericError("couldn't make sound channel");
}

GBSoundChannel::~GBSoundChannel() {
	if ( SndDisposeChannel(channel, true) )
		throw GBGenericError("couldn't dispose sound channel");
}

bool GBSoundChannel::Busy() {
	SCStatus status;
	if ( SndChannelStatus(channel, sizeof(SCStatus), &status) )
		throw GBGenericError("couldn't get sound channel status");
	if ( ! status.scChannelBusy )
		priority = 0;
	return status.scChannelBusy;
}

void GBSoundChannel::StartSound(GBSoundID id) {
	StopSound();
	if ( SndPlay(channel, gSounds[id], true) )
		throw GBGenericError("couldn't play sound");
	priority = kSoundPriorities[id];
}

void GBSoundChannel::StopSound() {
	SndCommand cmd;
	cmd.cmd = flushCmd;
	if ( SndDoImmediate(channel, &cmd) )
		throw GBGenericError("couldn't stop sound");
	cmd.cmd = quietCmd;
	if ( SndDoImmediate(channel, &cmd) )
		throw GBGenericError("couldn't stop sound");
	priority = 0;
}

#else
//sound stubs for platforms without sound yet.
void SetupSound() {}
void CleanupSound() {}
void SetSoundActive(bool) {}
bool SoundActive() { return false; }
void StartSound(GBSoundID) {}

#endif
