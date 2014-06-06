// GBApplication.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#include "GBPlatform.h"
#include "GBApplication.h"
#include "GBWorld.h"
#include "GBSide.h"
#include "GBPortal.h"
#include "GBMiniMap.h"
#include "GBRosterView.h"
#include "GBAboutBox.h"
#include "GBScoresView.h"
#include "GBRobotTypeView.h"
#include "GBDebugger.h"
#include "GBSideDebugger.h"
#include "GBTournamentView.h"
#include "GBSideReader.h"
#include "GBSound.h"
#include "GBStringUtilities.h"
#include "GBWindow.h"

#if MAC && ! MAC_OS_X
#include <Sound.h>
#include <NumberFormatting.h>
#include <Dialogs.h>
#include <Controls.h>
#if CARBON
	#include <Navigation.h>
#else
	#include <StandardFile.h>
#endif
#endif


//Menu item IDs: these are 100*menuid + itemposition
enum {
	kAppleMenu = 128,
		miAbout = 12801,
	kFileMenu = 129,
		miLoadSide = 12901, miDuplicateSide,
		miReloadSide = 12903,
		miRemoveSide = 12905, miRemoveAllSides,
		miClose = 12908,
		miQuit = 12910,
	kWindowMenu = 130,
		miRosterView = 13001, miMainView, miMinimapView,
		miScoresView, miTypesView,
		miDebuggerView,
		miTournamentView, miSideDebuggerView,
	kViewMenu = 131,
		miSound = 13101,
		miShowSensors = 13103, miShowDecorations, miShowMeters,
		miMinimapRobots = 13107, miMinimapFood, miMinimapSensors, miMinimapDecorations,
		miMinimapTrails,
		miReportErrors = 13113, miReportPrints,
		miRefollow = 13116, miFollowRandom, miRandomNear, miAutofollow,
		miGraphAllRounds = 13121,
	kSimulationMenu = 132,
		miRun = 13201, miSingleFrame, miStep, miPause,
		miSlowerSpeed = 13206, miSlowSpeed, miNormalSpeed, miFastSpeed, miFasterSpeed, miUnlimitedSpeed,
		miNewRound = 13213, miRestart,
		miSeed = 13215, miReseed,
		miRules = 13218,
		miTournament = 13219, miSaveScores, miResetScores,
		miStartStopBrain = 13223,
	kToolsMenu = 133,
		miScroll = 13301,
		miAddManna = 13303, miAddRobot, miAddSeed,
		miMove = 13307, miPull, miSmite, miBlasts, miErase, miEraseBig
};

const short kRulesDialogID = 129;

const GBMilliseconds kSlowerSpeedLimit = 500;
const GBMilliseconds kSlowSpeedLimit = 100;
const GBMilliseconds kNormalSpeedLimit = 33;
const GBMilliseconds kFastSpeedLimit = 17;
const GBMilliseconds kFasterSpeedLimit = 10;
const GBMilliseconds kNoSpeedLimit = 0;

const int kMaxFasterSteps = 3;
const GBMilliseconds kMaxEventInterval = 50;

void GBApplication::SetupMenus() {
#if MAC
	MenuHandle currentMenu;
	currentMenu = GetMenu(kAppleMenu);
#if ! CARBON
	AppendResMenu(currentMenu, 'DRVR');
#endif
	InsertMenu(currentMenu, 0);
	currentMenu = GetMenu(kFileMenu);
	InsertMenu(currentMenu, 0);
	currentMenu = GetMenu(kWindowMenu);
	InsertMenu(currentMenu, 0);
	currentMenu = GetMenu(kSimulationMenu);
	InsertMenu(currentMenu, 0);
	currentMenu = GetMenu(kViewMenu);
	InsertMenu(currentMenu, 0);
	currentMenu = GetMenu(kToolsMenu);
	InsertMenu(currentMenu, 0);
	// help items...?
	DrawMenuBar();
#elif WINDOWS
	HMENU mbar = LoadMenu(hInstance, MAKEINTRESOURCE(101));
	if (!mbar)
		FatalError("Couldn't load menu bar.");
	SetMenu(mainWindow->win, mbar);
#endif
}

void GBApplication::DoLoadSide() {
#if CARBON
	NavReplyRecord reply;
	NavDialogOptions options;
	NavGetDefaultDialogOptions(&options);
	options.dialogOptionFlags |= kNavSelectAllReadableItem | kNavNoTypePopup;
	options.dialogOptionFlags &= ~ kNavAllowPreviews;
	ToPascalString(string("Load side"), options.windowTitle);
	#if MAC_OS_X
	if ( NavGetFile(nil, &reply, &options, nil, nil, nil, nil, nil) || ! reply.validRecord )
	#else
	NavTypeList types = { 'GBot', 0, 1, { 'TEXT' } };
	NavTypeListPtr typesp = &types;
	if ( NavGetFile(nil, &reply, &options, nil, nil, nil, &typesp, nil) || ! reply.validRecord )
	#endif
		return;
//open the selected items
	long items, dummySize;
	FSSpec spec;
	AEKeyword dummyKey;
	DescType dummyType;
	if ( AECountItems(&reply.selection, &items) )
		return;
	for ( long i = 1; i <= items; i ++ ) {
		if ( ! AEGetNthPtr(&reply.selection, i, typeFSS, &dummyKey, &dummyType,
				(Ptr)&spec, sizeof(FSSpec), &dummySize) )
			OpenFile(spec);
	}
#elif MAC
	SFTypeList types;
	types[0] = 'TEXT';
	StandardFileReply reply;
	StandardGetFile(nil, 1, types, &reply);
	if ( ! reply.sfGood ) return;
	OpenFile(reply.sfFile);
#elif WINDOWS
	OPENFILENAME ofn;
	char buff[2048] = "";
	ZeroMemory(&ofn, sizeof(ofn));
	ofn.lStructSize = sizeof(ofn);
	ofn.hwndOwner = mainWindow->win;
	ofn.lpstrFilter = "Grobots sides (*.gb)\0*.gb\0All Files (*.*)\0*.*\0";
	ofn.lpstrFile = buff;
	ofn.nMaxFile = 2048;
	ofn.Flags = OFN_EXPLORER | OFN_FILEMUSTEXIST | OFN_HIDEREADONLY | OFN_ALLOWMULTISELECT;
	ofn.lpstrDefExt = "gb";

	if (GetOpenFileName(&ofn)) {
		if (buff[ofn.nFileOffset - 1] != '\0') { //Only one file selected
			GBSide * side = GBSideReader::Load(buff);
			if (side) world.AddSide(side);
		} else { //There are multiple files selected.
			//Buffer is formatted as "path\0file1\0file2\0...filelast\0\0"
			string path = buff;
			path += '\\';
			for ( const char * filename = buff + path.size(); *filename;
					filename += strlen(filename) + 1 ) {
				GBSide * side = GBSideReader::Load(strchr(filename, '\\') ? filename : path + filename);
				if(side) world.AddSide(side);
			 }
		}
	}
#endif
}

void GBApplication::DoReloadSide() {
	GBSide * oldSide = world.SelectedSide();
	if ( ! oldSide ) return;
	GBSide * newSide = GBSideReader::Load(oldSide->filename);
	if ( newSide ) {
		if ( oldSide->Scores().Seeded() ) {
			world.Reset();
			world.running = false;
		}
		world.ReplaceSide(oldSide, newSide);
	}
}

void GBApplication::DoRulesDialog() {
#if MAC
// item IDs
	const short kOKButton = 1;
	const short kCancelButton = 2;
	const short kWorldWidthBox = 3;
	const short kWorldHeightBox = 4;
	const short kMannaSizeBox = 5;
	const short kMannaDensityBox = 6;
	const short kMannaRateBox = 7;
	const short kSeedLimitBox = 8;
	const short kAutoReseedCheckbox = 9;
	const short kSeedValueBox = 10;
	const short kSeedTypePenaltyBox = 11;
	const short kTimeLimitCheckbox = 12;
	const short kTimeLimitBox = 13;
	const short kStopOnEliminationCheckbox = 14;
// put up dialog
	bool done = false;
	short itemHit, itemType;
	Handle item;
	Rect bounds;
	Str255 text;
	DialogPtr dlog = GetNewDialog(kRulesDialogID, nil, (WindowPtr)(-1));
	SetDialogDefaultItem(dlog, kOKButton);
	SetDialogCancelItem(dlog, kCancelButton);
	SetDialogTracksCursor(dlog, true);
// fill in current values
	bool autoReseedCheck = world.autoReseed;
	bool timeLimitCheck = world.timeLimit > 0;
	bool stopOnEliminationCheck = world.stopOnElimination;
	GetDialogItem(dlog, kWorldWidthBox, &itemType, &item, &bounds);
	NumToString(world.BackgroundTilesX(), text);
	SetDialogItemText(item, text);
	GetDialogItem(dlog, kWorldHeightBox, &itemType, &item, &bounds);
	NumToString(world.BackgroundTilesY(), text);
	SetDialogItemText(item, text);
	GetDialogItem(dlog, kMannaSizeBox, &itemType, &item, &bounds);
	ToPascalString(ToString(world.mannaSize), text);
	SetDialogItemText(item, text);
	GetDialogItem(dlog, kMannaDensityBox, &itemType, &item, &bounds);
	ToPascalString(ToString(world.mannaDensity),text);
	SetDialogItemText(item, text);
	GetDialogItem(dlog, kMannaRateBox, &itemType, &item, &bounds);
	ToPascalString(ToString(world.mannaRate),text);
	SetDialogItemText(item, text);
	GetDialogItem(dlog, kSeedLimitBox, &itemType, &item, &bounds);
	NumToString(world.seedLimit, text);
	SetDialogItemText(item, text);
	GetDialogItem(dlog, kAutoReseedCheckbox, &itemType, &item, &bounds);
	SetControlValue((ControlHandle)item, autoReseedCheck);
	GetDialogItem(dlog, kSeedValueBox, &itemType, &item, &bounds);
	ToPascalString(ToString(world.seedValue),text);
	SetDialogItemText(item, text);
	GetDialogItem(dlog, kSeedTypePenaltyBox, &itemType, &item, &bounds);
	ToPascalString(ToString(world.seedTypePenalty),text);
	SetDialogItemText(item, text);
	GetDialogItem(dlog, kTimeLimitCheckbox, &itemType, &item, &bounds);
#if CARBON
	SetControl32BitValue((ControlHandle)item, timeLimitCheck);
#else
	SetControlValue((ControlHandle)item, stopOnEliminationCheck);
#endif
	GetDialogItem(dlog, kTimeLimitBox, &itemType, &item, &bounds);
	NumToString(world.timeLimit > 0 ? world.timeLimit : 18000, text);
	SetDialogItemText(item, text);
	GetDialogItem(dlog, kStopOnEliminationCheckbox, &itemType, &item, &bounds);
#if CARBON
	SetControl32BitValue((ControlHandle)item, stopOnEliminationCheck);
#else
	SetControlValue((ControlHandle)item, stopOnEliminationCheck);
#endif
// do dialog
	SelectDialogItemText(dlog, kWorldWidthBox, 0, -1);
	do {
		ModalDialog(nil, &itemHit);
		switch ( itemHit ) {
			case kOKButton: {
			// get and validate values:
			// world size
				long worldWidth;
				GetDialogItem(dlog, kWorldWidthBox, &itemType, &item, &bounds);
				GetDialogItemText(item, text);
				StringToNum(text, &worldWidth);
				if ( worldWidth < 2 || worldWidth > 50 ) {
					SelectDialogItemText(dlog, kWorldWidthBox, 0, -1);
					SysBeep(1);
					break;
				}
				long worldHeight;
				GetDialogItem(dlog, kWorldHeightBox, &itemType, &item, &bounds);
				GetDialogItemText(item, text);
				StringToNum(text, &worldHeight);
				if ( worldHeight < 2 || worldHeight > 50 ) {
					SelectDialogItemText(dlog, kWorldHeightBox, 0, -1);
					SysBeep(1);
					break;
				}
			// manna size
				GBNumber mannaSize;
				GetDialogItem(dlog, kMannaSizeBox, &itemType, &item, &bounds);
				GetDialogItemText(item, text);
				if ( ! ParseNumber(FromPascalString(text), mannaSize) || mannaSize < 10 ) {
					SelectDialogItemText(dlog, kMannaSizeBox, 0, -1);
					SysBeep(1);
					break;
				}
			// manna density
				GBNumber mannaDensity;
				GetDialogItem(dlog, kMannaDensityBox, &itemType, &item, &bounds);
				GetDialogItemText(item, text);
				if ( ! ParseNumber(FromPascalString(text), mannaDensity) || mannaDensity < 0 ) {
					SelectDialogItemText(dlog, kMannaDensityBox, 0, -1);
					SysBeep(1);
					break;
				}
			// manna rate
				GBNumber mannaRate;
				GetDialogItem(dlog, kMannaRateBox, &itemType, &item, &bounds);
				GetDialogItemText(item, text);
				if ( ! ParseNumber(FromPascalString(text), mannaRate) || mannaRate < 0 ) {
					SelectDialogItemText(dlog, kMannaRateBox, 0, -1);
					SysBeep(1);
					break;
				}
			// seed limit
				long seedLimit;
				GetDialogItem(dlog, kSeedLimitBox, &itemType, &item, &bounds);
				GetDialogItemText(item, text);
				StringToNum(text, &seedLimit);
				if ( seedLimit < 0 || seedLimit > 100 ) {
					SelectDialogItemText(dlog, kSeedLimitBox, 0, -1);
					SysBeep(1);
					break;
				}
			// seed value
				GBNumber seedValue;
				GetDialogItem(dlog, kSeedValueBox, &itemType, &item, &bounds);
				GetDialogItemText(item, text);
				if ( ! ParseNumber(FromPascalString(text), seedValue) || seedValue <= 0 ) {
					SelectDialogItemText(dlog, kSeedValueBox, 0, -1);
					SysBeep(1);
					break;
				}
			// seed type penalty
				GBNumber seedTypePenalty;
				GetDialogItem(dlog, kSeedTypePenaltyBox, &itemType, &item, &bounds);
				GetDialogItemText(item, text);
				if ( ! ParseNumber(FromPascalString(text), seedTypePenalty) || seedTypePenalty > seedValue ) {
					SelectDialogItemText(dlog, kSeedTypePenaltyBox, 0, -1);
					SysBeep(1);
					break;
				}
			// time limit
				long timeLimit;
				GetDialogItem(dlog, kTimeLimitBox, &itemType, &item, &bounds);
				GetDialogItemText(item, text);
				StringToNum(text, &timeLimit);
				if ( timeLimit < 0 || timeLimit > 1000000 ) {
					SelectDialogItemText(dlog, kTimeLimitBox, 0, -1);
					SysBeep(1);
					break;
				}
			// store values
				world.mannaSize = mannaSize;
				world.mannaDensity = mannaDensity;
				world.mannaRate = mannaRate;
				world.seedLimit = seedLimit;
				world.seedValue = seedValue;
				world.seedTypePenalty = seedTypePenalty;
				world.autoReseed = autoReseedCheck;
				world.timeLimit = timeLimitCheck ? timeLimit : 0;
				world.stopOnElimination = stopOnEliminationCheck;
			// resize is last so manna changes can take effect
				if ( worldWidth != world.BackgroundTilesX()
					|| worldHeight != world.BackgroundTilesY() ) {
					world.Resize(GBFinePoint(kBackgroundTileSize * worldWidth,
						kBackgroundTileSize * worldHeight));
					world.running = false;
					minimapWindow->ZoomOut();
				}
				done = true;
			} break;
			case kCancelButton:
				done = true; break;
			case kAutoReseedCheckbox:
				autoReseedCheck = ! autoReseedCheck;
				GetDialogItem(dlog, kAutoReseedCheckbox, &itemType, &item, &bounds);
				SetControlValue((ControlHandle)item, autoReseedCheck);
				break;
			case kTimeLimitCheckbox:
				timeLimitCheck = ! timeLimitCheck;
				GetDialogItem(dlog, kTimeLimitCheckbox, &itemType, &item, &bounds);
				SetControlValue((ControlHandle)item, timeLimitCheck);
				break;
			case kStopOnEliminationCheckbox:
				stopOnEliminationCheck = ! stopOnEliminationCheck;
				GetDialogItem(dlog, kStopOnEliminationCheckbox, &itemType, &item, &bounds);
				SetControlValue((ControlHandle)item, stopOnEliminationCheck);
				break;
			default: break;
		}
	} while ( ! done );
	DisposeDialog(dlog);
#endif
	//no non-Mac versions yet
}

#if WINDOWS
GBApplication::GBApplication(HINSTANCE hInstance, int showCmd)
	: GBViewsApplication(hInstance, showCmd),
#else
GBApplication::GBApplication()
	: GBViewsApplication(),
#endif
	world(),
	minimapWindow(nil), rosterWindow(nil),
	scoresWindow(nil), typeWindow(nil),
	aboutWindow(nil), tournamentWindow(nil),
	debuggerWindow(nil), sideDebuggerWindow(nil)
{
	SetupSound();
	portal = new GBPortal(world);
	mainWindow = MakeWindow(new GBDoubleBufferedView(portal), 291, 43);
	debugger = new GBDebuggerView(world);
	debuggerWindow = MakeWindow(debugger, 616, 43, false);
	sideDebuggerWindow = MakeWindow(new GBSideDebuggerView(world), 200, 400, false);
	minimap = new GBMiniMapView(world, *portal);
	minimapWindow = MakeWindow(new GBDoubleBufferedView(minimap), 7,
		#if MAC && ! CARBON
			qd.screenBits.bounds.bottom - 230,
		#else
			400,
		#endif
		true);
	rosterWindow = MakeWindow(new GBRosterView(world), 7, 43);
	aboutWindow = MakeWindow(new GBAboutBox(), 200, 150, false);
	scores = new GBScoresView(world);
	scoresWindow = MakeWindow(scores, 291, 384, false);
	typeWindow = MakeWindow(new GBRobotTypeView(world), 616, 270, false);
	tournamentWindow = MakeWindow(new GBTournamentView(world), 100, 100, false);
	SetupMenus();
	SetStepPeriod(kNormalSpeedLimit);
}

GBApplication::~GBApplication() {
	delete typeWindow;
	delete scoresWindow;
	delete minimapWindow;
	delete rosterWindow;
	delete aboutWindow;
	delete tournamentWindow;
	delete debuggerWindow;
	delete sideDebuggerWindow;
	CleanupSound();
}

void GBApplication::AdjustMenus() {
//About item
#if MAC
	GBWindow * wind = GBWindow::GetFromWindow(FrontWindow());
	CheckOne(miAbout, wind == aboutWindow);
#endif
//file
	EnableOne(miDuplicateSide, world.SelectedSide() != 0);
	EnableOne(miReloadSide, world.SelectedSide() != 0);
	EnableOne(miRemoveSide, world.SelectedSide() != 0);
	EnableOne(miRemoveAllSides, world.Sides() != 0);
// check windows in Window menu
#if MAC
	CheckOne(miMainView, wind == mainWindow);
	CheckOne(miRosterView, wind == rosterWindow);
	CheckOne(miMinimapView, wind == minimapWindow);
	CheckOne(miScoresView, wind == scoresWindow);
	CheckOne(miTypesView, wind == typeWindow);
	CheckOne(miTournamentView, wind == tournamentWindow);
	CheckOne(miDebuggerView, wind == debuggerWindow);
	CheckOne(miSideDebuggerView, wind == sideDebuggerWindow);
#endif
// check view options
	CheckOne(miSound, SoundActive());
	CheckOne(miShowSensors, portal->showSensors);
	CheckOne(miShowDecorations, portal->showDecorations);
	CheckOne(miShowMeters, portal->showDetails);
	CheckOne(miMinimapRobots, minimap->showRobots);
	CheckOne(miMinimapFood, minimap->showFood);
	CheckOne(miMinimapSensors, minimap->showSensors);
	CheckOne(miMinimapDecorations, minimap->showDecorations);
	CheckOne(miMinimapTrails, minimap->showTrails);
	CheckOne(miReportErrors, world.reportErrors);
	CheckOne(miReportPrints, world.reportPrints);
	CheckOne(miAutofollow, portal->autofollow);
	CheckOne(miGraphAllRounds, scores->graphAllRounds);
//Simulation menu
	EnableOne(miRun, ! world.running);
	EnableOne(miSingleFrame, ! world.running);
	EnableOne(miStep, ! world.running && debugger->Active());
	EnableOne(miPause, world.running);

	CheckOne(miSlowerSpeed, stepPeriod == kSlowerSpeedLimit);
	CheckOne(miSlowSpeed, stepPeriod == kSlowSpeedLimit);
	CheckOne(miNormalSpeed, stepPeriod == kNormalSpeedLimit);
	CheckOne(miFastSpeed, stepPeriod == kFastSpeedLimit);
	CheckOne(miFasterSpeed, stepPeriod == kFasterSpeedLimit);
	CheckOne(miUnlimitedSpeed, stepPeriod == kNoSpeedLimit);

	CheckOne(miTournament, world.tournament);
	EnableOne(miStartStopBrain, debugger->Active());
//Tools menu
	GBSide * side = world.SelectedSide();
	EnableOne(miAddSeed, side != 0);
	EnableOne(miAddRobot, side != 0);
	if ( !side && (portal->tool == ptAddSeed || portal->tool == ptAddRobot) )
		portal->tool = ptScroll;
	CheckOne(miScroll, portal->tool == ptScroll);
	CheckOne(miAddManna, portal->tool == ptAddManna);
	CheckOne(miAddRobot, portal->tool == ptAddRobot);
	CheckOne(miAddSeed, portal->tool == ptAddSeed);
	CheckOne(miMove, portal->tool == ptMove);
	CheckOne(miPull, portal->tool == ptPull);
	CheckOne(miSmite, portal->tool == ptSmite);
	CheckOne(miBlasts, portal->tool == ptBlasts);
	CheckOne(miErase, portal->tool == ptErase);
	CheckOne(miEraseBig, portal->tool == ptEraseBig);
}

#if 1
void GBApplication::HandleMenuSelection(int item) {
	try {
		switch ( item ) {
		//Apple or Help menu
			case miAbout: aboutWindow->Show(); break;
		//File menu
			case miLoadSide: DoLoadSide(); break;
			case miDuplicateSide:
				if ( world.SelectedSide() )
					world.AddSide(world.SelectedSide()->Copy());
				break;
			case miReloadSide: DoReloadSide(); break;
			case miRemoveSide:
				if ( world.SelectedSide() ) {
					if ( world.SelectedSide()->Scores().Seeded() ) {
						world.Reset();
						world.running = false;
					}
					world.RemoveSide(world.SelectedSide());
				} break;
			case miRemoveAllSides:
				world.Reset();
				world.RemoveAllSides();
				world.running = false;
				break;
#if MAC
			case miClose: {
					WindowPtr wind = FrontWindow();
					if ( GBWindow::IsGBWindow(wind) )
						GBWindow::GetFromWindow(wind)->Hide();
				} break;
#endif
			case miQuit:
				Quit();
				break;
		//Window menu
			case miMainView: mainWindow->Show(); break;
			case miRosterView: rosterWindow->Show(); break;
			case miMinimapView: minimapWindow->Show(); break;
			case miScoresView: scoresWindow->Show(); break;
			case miTypesView: typeWindow->Show(); break;
			case miTournamentView: tournamentWindow->Show(); break;
			case miDebuggerView: debuggerWindow->Show(); break;
			case miSideDebuggerView: sideDebuggerWindow->Show(); break;
		//View menu
			case miSound: SetSoundActive(! SoundActive()); break;
			case miShowSensors: portal->showSensors = ! portal->showSensors; break;
			case miShowDecorations: portal->showDecorations = ! portal->showDecorations; break;
			case miShowMeters: portal->showDetails = ! portal->showDetails; break;
			case miMinimapRobots: minimap->showRobots = ! minimap->showRobots; break;
			case miMinimapFood: minimap->showFood = ! minimap->showFood; break;
			case miMinimapSensors: minimap->showSensors = ! minimap->showSensors; break;
			case miMinimapDecorations: minimap->showDecorations = ! minimap->showDecorations; break;
			case miMinimapTrails: minimap->showTrails = ! minimap->showTrails; break;
			case miReportErrors: world.reportErrors = ! world.reportErrors; break;
			case miReportPrints: world.reportPrints = ! world.reportPrints; break;
			case miRefollow: portal->Refollow(); break;
			case miFollowRandom: portal->FollowRandom(); break;
			case miRandomNear: portal->FollowRandomNear(); break;
			case miAutofollow: portal->autofollow = ! portal->autofollow; break;
			case miGraphAllRounds: scores->graphAllRounds = ! scores->graphAllRounds; break;
		//Simulation menu:
			case miRun:
				world.running = true;
				lastStep = Milliseconds();
				break;
			case miSingleFrame:
				world.AdvanceFrame();
				world.running = false;
				break;
			case miStep:
				if ( debugger->Active() && debugger->Step() )
					world.AdvanceFrame();
				world.running = false;
				break;
			case miPause: world.running = false; break;
			case miSlowerSpeed: SetStepPeriod(kSlowerSpeedLimit); break;
			case miSlowSpeed: SetStepPeriod(kSlowSpeedLimit); break;
			case miNormalSpeed: SetStepPeriod(kNormalSpeedLimit); break;
			case miFastSpeed: SetStepPeriod(kFastSpeedLimit); break;
			case miFasterSpeed: SetStepPeriod(kFasterSpeedLimit); break;
			case miUnlimitedSpeed: SetStepPeriod(kNoSpeedLimit); break;
			case miNewRound:
				world.Reset();
				world.AddSeeds();
				world.running = true;
				break;
			case miRestart:
				world.Reset();
				world.running = false;
				break;
			case miSeed: world.AddSeeds(); break;
			case miReseed: world.ReseedDeadSides(); break;
			case miRules: DoRulesDialog(); break;
			case miTournament:
				if ( world.tournament ) world.tournament = false;
				else
#if MAC
					if ( DoNumberDialog("\pNumber of rounds:", world.tournamentLength, -1) )
#endif
					world.tournament = true;
				break;
			case miSaveScores: world.DumpTournamentScores(); break;
			case miResetScores: world.ResetTournamentScores(); break;
			case miStartStopBrain: debugger->StartStopBrain(); break;
		//Tools menu
			case miScroll: portal->tool = ptScroll; break;
			case miAddManna: portal->tool = ptAddManna; break;
			case miAddRobot: portal->tool = ptAddRobot; break;
			case miAddSeed: portal->tool = ptAddSeed; break;
			case miMove: portal->tool = ptMove; break;
			case miPull: portal->tool = ptPull; break;
			case miSmite: portal->tool = ptSmite; break;
			case miBlasts: portal->tool = ptBlasts; break;
			case miErase: portal->tool = ptErase; break;
			case miEraseBig: portal->tool = ptEraseBig; break;
		//other
			default:
#if MAC && ! CARBON
				if (item / 100 == kAppleMenu)
					OpenAppleMenuItem(item);
#endif
				break;
		}
	} catch ( GBAbort & ) {}
}
#endif

void GBApplication::Process() {
	lastStep = Milliseconds();
	if ( !world.running ) {
		lastStep += 1000; //hack to prevent taking so much time when paused at Unlimited speed
		return;
	}
	try {
		int steps = 0;
		do {
			world.AdvanceFrame();
			++steps;
		} while ( world.running && (stepPeriod <= 0 || stepPeriod <= 10 && steps < kMaxFasterSteps)
			&& Milliseconds() <= lastStep + kMaxEventInterval );
	} catch ( GBError & err ) {
		NonfatalError("Error simulating: " + err.ToString());
	} catch ( GBAbort & ) {
		world.running = false;
	}
}

void GBApplication::Redraw() {
	try {
		mainWindow->DrawChanges(world.running);
		minimapWindow->DrawChanges(world.running);
		rosterWindow->DrawChanges(world.running || dragging);
		scoresWindow->DrawChanges(world.running || dragging);
		typeWindow->DrawChanges(world.running || dragging);
		tournamentWindow->DrawChanges(world.running || dragging);
		debuggerWindow->DrawChanges(world.running || dragging);
		sideDebuggerWindow->DrawChanges(world.running || dragging);
	} catch ( GBError & err ) {
		NonfatalError("Error drawing: " + err.ToString());
	}
}

#if MAC
//return sleep time for WNE, in ticks
long GBApplication::SleepTime() {
	if ( ! world.running ) return -1;
	return GBViewsApplication::SleepTime();
}

void GBApplication::OpenFile(FSSpec & file) {
#if !MAC_OS_X
	SetCursor(cuWait);
#endif
	GBSide * side = GBSideReader::Load(file);
	if ( side ) world.AddSide(side);
}
#endif


void GBApplication::Quit() {
	if ( ! world.tournament
		|| Confirm("Do you really want to quit during a tournament?", "Quit") )
		alive = false;
}
