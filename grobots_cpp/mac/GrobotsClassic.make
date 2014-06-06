#   File:       Grobots.make
#   Target:     Grobots
#   Created:    Monday, January 3, 2000 11:45:22 PM


MAKEFILE        = GrobotsClassic.make
¥MondoBuild¥    =  {MAKEFILE} # Make blank to avoid rebuilds when makefile is modified

ObjDir          = :ObjectFiles:Classic:
Includes        = -i :src:support: ¶
				  -i :src:simulation: ¶
				  -i :src:sides: ¶
				  -i :src:brains: ¶
				  -i :src:views: ¶
				  -i :src:mac: ¶
				  -i "{STL}"

Sym-PPC         = -sym off

PPCCPlusOptions = {Includes} {Sym-PPC} ¶
				  -bool on -exceptions on ¶
				  -ansi on -ansifor -w 23 -enum int -u nil ¶
				  -includes unix

### Source Files ###

SrcFiles        =  ¶
				  :src:support:GBErrors.cpp ¶
				  :src:support:GBRandomState.cpp ¶
				  :src:support:GBColor.cpp ¶
				  :src:support:GBNumber.cpp ¶
				  :src:support:GBLongNumber.cpp ¶
				  :src:support:GBFinePoint.cpp ¶
				  :src:support:GBModel.cpp ¶
				  :src:support:GBStringUtilities.cpp ¶
				  :src:support:GBSound.cpp ¶
				  :src:support:GBDeletionReporter.cpp ¶
				  :src:support:GBGraphics.cpp ¶
				  :src:support:GBMilliseconds.cpp ¶
				  :src:sides:GBScores.cpp ¶
				  :src:sides:GBSide.cpp ¶
				  :src:sides:GBRobotType.cpp ¶
				  :src:sides:GBHardwareSpec.cpp ¶
				  :src:sides:GBSideReader.cpp ¶
				  :src:simulation:GBObject.cpp ¶
				  :src:simulation:GBObjectWorld.cpp ¶
				  :src:simulation:GBFood.cpp ¶
				  :src:simulation:GBShot.cpp ¶
				  :src:simulation:GBRobot.cpp ¶
				  :src:simulation:GBHardwareState.cpp ¶
				  :src:simulation:GBSensorShot.cpp ¶
				  :src:simulation:GBMessages.cpp ¶
				  :src:simulation:GBDecorations.cpp ¶
				  :src:simulation:GBWorld.cpp ¶
				  :src:brains:GBBrain.cpp ¶
				  :src:brains:GBBrainSpec.cpp ¶
				  :src:brains:GBStackBrain.cpp ¶
				  :src:brains:GBStackBrainPrimitives.cpp ¶
				  :src:brains:GBStackBrainSpec.cpp ¶
				  :src:brains:GBStackBrainOpcodes.cpp ¶
				  :src:views:GBView.cpp ¶
				  :src:views:GBListView.cpp ¶
				  :src:views:GBAboutBox.cpp ¶
				  :src:views:GBPortal.cpp ¶
				  :src:views:GBMiniMap.cpp ¶
				  :src:views:GBRosterView.cpp ¶
				  :src:views:GBTournamentView.cpp ¶
				  :src:views:GBScoresView.cpp ¶
				  :src:views:GBRobotTypeView.cpp ¶
				  :src:views:GBDebugger.cpp ¶
				  :src:views:GBSideDebugger.cpp ¶
				  :src:ui:GBWindow.cpp ¶
				  :src:ui:GBViewsApplication.cpp ¶
				  :src:ui:GBApplication.cpp ¶
				  :src:ui:GBMain.cpp ¶

### Object Files ###

ObjFiles-PPC    =  ¶
				  "{ObjDir}GBErrors.cpp.x" ¶
				  "{ObjDir}GBRandomState.cpp.x" ¶
				  "{ObjDir}GBColor.cpp.x" ¶
				  "{ObjDir}GBNumber.cpp.x" ¶
				  "{ObjDir}GBLongNumber.cpp.x" ¶
				  "{ObjDir}GBFinePoint.cpp.x" ¶
				  "{ObjDir}GBSound.cpp.x" ¶
				  "{ObjDir}GBModel.cpp.x" ¶
				  "{ObjDir}GBStringUtilities.cpp.x" ¶
				  "{ObjDir}GBDeletionReporter.cpp.x" ¶
				  "{ObjDir}GBGraphics.cpp.x" ¶
				  "{ObjDir}GBMilliseconds.cpp.x" ¶
				  "{ObjDir}GBScores.cpp.x" ¶
				  "{ObjDir}GBSide.cpp.x" ¶
				  "{ObjDir}GBRobotType.cpp.x" ¶
				  "{ObjDir}GBHardwareSpec.cpp.x" ¶
				  "{ObjDir}GBSideReader.cpp.x" ¶
				  "{ObjDir}GBObject.cpp.x" ¶
				  "{ObjDir}GBObjectWorld.cpp.x" ¶
				  "{ObjDir}GBFood.cpp.x" ¶
				  "{ObjDir}GBDecorations.cpp.x" ¶
				  "{ObjDir}GBShot.cpp.x" ¶
				  "{ObjDir}GBRobot.cpp.x" ¶
				  "{ObjDir}GBHardwareState.cpp.x" ¶
				  "{ObjDir}GBSensorShot.cpp.x" ¶
				  "{ObjDir}GBMessages.cpp.x" ¶
				  "{ObjDir}GBWorld.cpp.x" ¶
				  "{ObjDir}GBBrain.cpp.x" ¶
				  "{ObjDir}GBBrainSpec.cpp.x" ¶
				  "{ObjDir}GBStackBrainPrimitives.cpp.x" ¶
				  "{ObjDir}GBStackBrain.cpp.x" ¶
				  "{ObjDir}GBStackBrainSpec.cpp.x" ¶
				  "{ObjDir}GBStackBrainOpcodes.cpp.x" ¶
				  "{ObjDir}GBView.cpp.x" ¶
				  "{ObjDir}GBListView.cpp.x" ¶
				  "{ObjDir}GBRosterView.cpp.x" ¶
				  "{ObjDir}GBTournamentView.cpp.x" ¶
				  "{ObjDir}GBScoresView.cpp.x" ¶
				  "{ObjDir}GBMiniMap.cpp.x" ¶
				  "{ObjDir}GBRobotTypeView.cpp.x" ¶
				  "{ObjDir}GBDebugger.cpp.x" ¶
				  "{ObjDir}GBSideDebugger.cpp.x" ¶
				  "{ObjDir}GBPortal.cpp.x" ¶
				  "{ObjDir}GBWindow.cpp.x" ¶
				  "{ObjDir}GBViewsApplication.cpp.x" ¶
				  "{ObjDir}GBApplication.cpp.x" ¶
				  "{ObjDir}GBMain.cpp.x" ¶
				  "{ObjDir}GBAboutBox.cpp.x" ¶

### Libraries ###

LibFiles-PPC    =  ¶
				  "{SharedLibraries}InterfaceLib" ¶
				  "{SharedLibraries}StdCLib" ¶
				  "{SharedLibraries}MathLib" ¶
				  "{PPCLibraries}StdCRuntime.o" ¶
#				  "{PPCLibraries}PPCStdCLib.o" ¶
				  "{PPCLibraries}PPCCRuntime.o" ¶
#				  "{PPCLibraries}PPCToolLibs.o" ¶
				  "{PPCLibraries}MrCPlusLib.o" ¶
#				  "{PPCLibraries}MrCIOStreams.o" ¶
				  "{SharedLibraries}MrCExceptionsLib" ¶
				  "{STL}:lib:STLportLib.PPC.o"

### Default Rules ###

.cpp.x  Ä  .cpp  {¥MondoBuild¥}
	{PPCCPlus} {depDir}{default}.cpp -o {targDir}{default}.cpp.x {PPCCPlusOptions}

# Rebuild About box when anything changes, to update date.
"{ObjDir}GBAboutBox.cpp.x" Ä {SrcFiles}

### Build Rules ###

GrobotsClassic  ÄÄ  {ObjFiles-PPC} {LibFiles-PPC} {¥MondoBuild¥}
	PPCLink ¶
		-o {Targ} ¶
		{ObjFiles-PPC} ¶
		{LibFiles-PPC} ¶
		{Sym-PPC} ¶
		-linkfaster off ¶
		-mf -d ¶
		-t 'APPL' -c 'GBot'

GrobotsClassic  ÄÄ  ":src:mac:Mac_Interface.rsrc" {¥MondoBuild¥}
	Echo "Include ¶":src:mac:Mac_Interface.rsrc¶";" | Rez -o {Targ} -append

GrobotsClassic  ÄÄ  ":src:mac:Sounds.rsrc" {¥MondoBuild¥}
	Echo "Include ¶":src:mac:Sounds.rsrc¶";" | Rez -o {Targ} -append

### Required Dependencies ###
# snipped #

### Optional Dependencies ###
### Build this target to generate "include file" dependencies. ###

Dependencies  Ä  $OutOfDate
	MakeDepend ¶
		-append {MAKEFILE} ¶
		-ignore "{CIncludes}" -ignore "{STL}" ¶
		-objdir {ObjDir} ¶
		-objext .x ¶
		{Includes} ¶
		{SrcFiles}

#*** Dependencies: Cut here ***
# These dependencies were produced at 9:40:41 pm on 2006 Feb 23 Thu by MakeDepend

:ObjectFiles:Classic:GBErrors.cpp.x	Ä  ¶
	:src:support:GBErrors.cpp ¶
	:src:support:GBErrors.h ¶
	:src:support:GBStringUtilities.h ¶
	:src:support:GBPlatform.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h ¶
	:src:support:GBColor.h

:ObjectFiles:Classic:GBRandomState.cpp.x	Ä  ¶
	:src:support:GBRandomState.cpp ¶
	:src:support:GBRandomState.h ¶
	:src:support:GBErrors.h ¶
	:src:support:GBStringUtilities.h ¶
	:src:support:GBColor.h ¶
	:src:support:GBTypes.h ¶
	:src:support:GBPlatform.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h

:ObjectFiles:Classic:GBColor.cpp.x	Ä  ¶
	:src:support:GBColor.cpp ¶
	:src:support:GBColor.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBPlatform.h ¶
	:src:support:GBErrors.h

:ObjectFiles:Classic:GBNumber.cpp.x	Ä  ¶
	:src:support:GBNumber.cpp ¶
	:src:support:GBNumber.h ¶
	:src:support:GBStringUtilities.h ¶
	:src:support:GBErrors.h ¶
	:src:support:GBPlatform.h ¶
	:src:support:GBFinePoint.h ¶
	:src:support:GBColor.h

:ObjectFiles:Classic:GBLongNumber.cpp.x	Ä  ¶
	:src:support:GBLongNumber.cpp ¶
	:src:support:GBLongNumber.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBErrors.h

:ObjectFiles:Classic:GBFinePoint.cpp.x	Ä  ¶
	:src:support:GBFinePoint.cpp ¶
	:src:support:GBFinePoint.h ¶
	:src:support:GBErrors.h ¶
	:src:support:GBStringUtilities.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBPlatform.h ¶
	:src:support:GBColor.h

:ObjectFiles:Classic:GBModel.cpp.x	Ä  ¶
	:src:support:GBModel.cpp ¶
	:src:support:GBModel.h

:ObjectFiles:Classic:GBStringUtilities.cpp.x	Ä  ¶
	:src:support:GBStringUtilities.cpp ¶
	:src:support:GBStringUtilities.h ¶
	:src:support:GBPlatform.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h ¶
	:src:support:GBErrors.h ¶
	:src:support:GBColor.h

:ObjectFiles:Classic:GBSound.cpp.x	Ä  ¶
	:src:support:GBSound.cpp ¶
	:src:support:GBSound.h ¶
	:src:support:GBErrors.h ¶
	:src:support:GBPlatform.h

:ObjectFiles:Classic:GBDeletionReporter.cpp.x	Ä  ¶
	:src:support:GBDeletionReporter.cpp ¶
	:src:support:GBDeletionReporter.h ¶
	:src:support:GBErrors.h ¶
	:src:support:GBPlatform.h

:ObjectFiles:Classic:GBGraphics.cpp.x	Ä  ¶
	:src:support:GBGraphics.cpp ¶
	:src:support:GBGraphics.h ¶
	:src:support:GBStringUtilities.h ¶
	:src:support:GBPlatform.h ¶
	:src:support:GBColor.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h ¶
	:src:support:GBErrors.h

:ObjectFiles:Classic:GBMilliseconds.cpp.x	Ä  ¶
	:src:support:GBMilliseconds.cpp ¶
	:src:support:GBPlatform.h ¶
	:src:support:GBMilliseconds.h

:ObjectFiles:Classic:GBScores.cpp.x	Ä  ¶
	:src:sides:GBScores.cpp ¶
	:src:sides:GBScores.h ¶
	:src:support:GBTypes.h ¶
	:src:support:GBLongNumber.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h ¶
	:src:support:GBErrors.h

:ObjectFiles:Classic:GBSide.cpp.x	Ä  ¶
	:src:sides:GBSide.cpp ¶
	:src:sides:GBSide.h ¶
	:src:sides:GBRobotType.h ¶
	:src:support:GBErrors.h ¶
	:src:support:GBRandomState.h ¶
	:src:support:GBColor.h ¶
	:src:support:GBModel.h ¶
	:src:support:GBTypes.h ¶
	:src:simulation:GBMessages.h ¶
	:src:sides:GBScores.h ¶
	:src:sides:GBHardwareSpec.h ¶
	:src:support:GBLongNumber.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h

:ObjectFiles:Classic:GBRobotType.cpp.x	Ä  ¶
	:src:sides:GBRobotType.cpp ¶
	:src:sides:GBRobotType.h ¶
	:src:support:GBErrors.h ¶
	:src:brains:GBBrainSpec.h ¶
	:src:sides:GBHardwareSpec.h ¶
	:src:support:GBColor.h ¶
	:src:support:GBModel.h ¶
	:src:support:GBLongNumber.h ¶
	:src:support:GBTypes.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h

:ObjectFiles:Classic:GBHardwareSpec.cpp.x	Ä  ¶
	:src:sides:GBHardwareSpec.cpp ¶
	:src:sides:GBHardwareSpec.h ¶
	:src:support:GBTypes.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h ¶
	:src:support:GBErrors.h

:ObjectFiles:Classic:GBSideReader.cpp.x	Ä  ¶
	:src:sides:GBSideReader.cpp ¶
	:src:sides:GBSideReader.h ¶
	:src:support:GBStringUtilities.h ¶
	:src:sides:GBRobotType.h ¶
	:src:sides:GBSide.h ¶
	:src:brains:GBStackBrainSpec.h ¶
	:src:support:GBPlatform.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h ¶
	:src:support:GBErrors.h ¶
	:src:support:GBColor.h ¶
	:src:sides:GBHardwareSpec.h ¶
	:src:support:GBModel.h ¶
	:src:support:GBLongNumber.h ¶
	:src:support:GBTypes.h ¶
	:src:simulation:GBMessages.h ¶
	:src:sides:GBScores.h ¶
	:src:brains:GBBrainSpec.h

:ObjectFiles:Classic:GBObject.cpp.x	Ä  ¶
	:src:simulation:GBObject.cpp ¶
	:src:simulation:GBObject.h ¶
	:src:support:GBErrors.h ¶
	:src:support:GBColor.h ¶
	:src:support:GBDeletionReporter.h ¶
	:src:support:GBTypes.h ¶
	:src:support:GBGraphics.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h ¶
	:src:support:GBPlatform.h

:ObjectFiles:Classic:GBObjectWorld.cpp.x	Ä  ¶
	:src:simulation:GBObjectWorld.cpp ¶
	:src:simulation:GBObjectWorld.h ¶
	:src:simulation:GBRobot.h ¶
	:src:simulation:GBSensorShot.h ¶
	:src:simulation:GBObject.h ¶
	:src:support:GBTypes.h ¶
	:src:simulation:GBHardwareState.h ¶
	:src:support:GBRandomState.h ¶
	:src:support:GBColor.h ¶
	:src:support:GBGraphics.h ¶
	:src:support:GBDeletionReporter.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h ¶
	:src:sides:GBHardwareSpec.h ¶
	:src:simulation:GBMessages.h ¶
	:src:support:GBPlatform.h ¶
	:src:support:GBErrors.h

:ObjectFiles:Classic:GBFood.cpp.x	Ä  ¶
	:src:simulation:GBFood.cpp ¶
	:src:simulation:GBFood.h ¶
	:src:support:GBErrors.h ¶
	:src:simulation:GBWorld.h ¶
	:src:sides:GBSide.h ¶
	:src:support:GBStringUtilities.h ¶
	:src:simulation:GBObject.h ¶
	:src:support:GBRandomState.h ¶
	:src:support:GBModel.h ¶
	:src:simulation:GBObjectWorld.h ¶
	:src:sides:GBScores.h ¶
	:src:support:GBColor.h ¶
	:src:support:GBTypes.h ¶
	:src:simulation:GBMessages.h ¶
	:src:support:GBPlatform.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h ¶
	:src:support:GBGraphics.h ¶
	:src:support:GBDeletionReporter.h ¶
	:src:support:GBLongNumber.h

:ObjectFiles:Classic:GBShot.cpp.x	Ä  ¶
	:src:simulation:GBShot.cpp ¶
	:src:simulation:GBShot.h ¶
	:src:simulation:GBWorld.h ¶
	:src:support:GBColor.h ¶
	:src:support:GBErrors.h ¶
	:src:simulation:GBDecorations.h ¶
	:src:simulation:GBRobot.h ¶
	:src:sides:GBSide.h ¶
	:src:support:GBSound.h ¶
	:src:support:GBStringUtilities.h ¶
	:src:simulation:GBObject.h ¶
	:src:support:GBRandomState.h ¶
	:src:support:GBModel.h ¶
	:src:simulation:GBObjectWorld.h ¶
	:src:sides:GBScores.h ¶
	:src:support:GBTypes.h ¶
	:src:simulation:GBHardwareState.h ¶
	:src:simulation:GBMessages.h ¶
	:src:support:GBPlatform.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h ¶
	:src:support:GBGraphics.h ¶
	:src:support:GBDeletionReporter.h ¶
	:src:support:GBLongNumber.h ¶
	:src:sides:GBHardwareSpec.h

:ObjectFiles:Classic:GBRobot.cpp.x	Ä  ¶
	:src:simulation:GBRobot.cpp ¶
	:src:simulation:GBRobot.h ¶
	:src:support:GBColor.h ¶
	:src:simulation:GBFood.h ¶
	:src:simulation:GBWorld.h ¶
	:src:simulation:GBShot.h ¶
	:src:support:GBErrors.h ¶
	:src:sides:GBRobotType.h ¶
	:src:sides:GBSide.h ¶
	:src:simulation:GBHardwareState.h ¶
	:src:brains:GBBrain.h ¶
	:src:brains:GBBrainSpec.h ¶
	:src:support:GBStringUtilities.h ¶
	:src:simulation:GBObject.h ¶
	:src:support:GBTypes.h ¶
	:src:support:GBRandomState.h ¶
	:src:support:GBModel.h ¶
	:src:simulation:GBObjectWorld.h ¶
	:src:sides:GBScores.h ¶
	:src:sides:GBHardwareSpec.h ¶
	:src:support:GBLongNumber.h ¶
	:src:simulation:GBMessages.h ¶
	:src:support:GBPlatform.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h ¶
	:src:support:GBGraphics.h ¶
	:src:support:GBDeletionReporter.h

:ObjectFiles:Classic:GBHardwareState.cpp.x	Ä  ¶
	:src:simulation:GBHardwareState.cpp ¶
	:src:simulation:GBHardwareState.h ¶
	:src:sides:GBHardwareSpec.h ¶
	:src:support:GBErrors.h ¶
	:src:simulation:GBShot.h ¶
	:src:simulation:GBFood.h ¶
	:src:simulation:GBSensorShot.h ¶
	:src:simulation:GBRobot.h ¶
	:src:simulation:GBWorld.h ¶
	:src:sides:GBRobotType.h ¶
	:src:sides:GBSide.h ¶
	:src:simulation:GBDecorations.h ¶
	:src:support:GBSound.h ¶
	:src:support:GBTypes.h ¶
	:src:simulation:GBMessages.h ¶
	:src:simulation:GBObject.h ¶
	:src:support:GBRandomState.h ¶
	:src:support:GBModel.h ¶
	:src:simulation:GBObjectWorld.h ¶
	:src:sides:GBScores.h ¶
	:src:support:GBColor.h ¶
	:src:support:GBLongNumber.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h ¶
	:src:support:GBGraphics.h ¶
	:src:support:GBDeletionReporter.h ¶
	:src:support:GBPlatform.h

:ObjectFiles:Classic:GBSensorShot.cpp.x	Ä  ¶
	:src:simulation:GBSensorShot.cpp ¶
	:src:simulation:GBSensorShot.h ¶
	:src:simulation:GBHardwareState.h ¶
	:src:simulation:GBRobot.h ¶
	:src:simulation:GBObject.h ¶
	:src:support:GBTypes.h ¶
	:src:sides:GBHardwareSpec.h ¶
	:src:simulation:GBMessages.h ¶
	:src:support:GBRandomState.h ¶
	:src:support:GBColor.h ¶
	:src:support:GBGraphics.h ¶
	:src:support:GBDeletionReporter.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h ¶
	:src:support:GBPlatform.h ¶
	:src:support:GBErrors.h

:ObjectFiles:Classic:GBMessages.cpp.x	Ä  ¶
	:src:simulation:GBMessages.cpp ¶
	:src:simulation:GBMessages.h ¶
	:src:support:GBTypes.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h ¶
	:src:support:GBErrors.h

:ObjectFiles:Classic:GBDecorations.cpp.x	Ä  ¶
	:src:simulation:GBDecorations.cpp ¶
	:src:simulation:GBDecorations.h ¶
	:src:simulation:GBObject.h ¶
	:src:support:GBTypes.h ¶
	:src:support:GBColor.h ¶
	:src:support:GBGraphics.h ¶
	:src:support:GBDeletionReporter.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h ¶
	:src:support:GBPlatform.h ¶
	:src:support:GBErrors.h

:ObjectFiles:Classic:GBWorld.cpp.x	Ä  ¶
	:src:simulation:GBWorld.cpp ¶
	:src:simulation:GBWorld.h ¶
	:src:simulation:GBFood.h ¶
	:src:simulation:GBShot.h ¶
	:src:simulation:GBRobot.h ¶
	:src:support:GBErrors.h ¶
	:src:sides:GBSide.h ¶
	:src:sides:GBRobotType.h ¶
	:src:support:GBSound.h ¶
	:src:support:GBStringUtilities.h ¶
	:src:support:GBRandomState.h ¶
	:src:support:GBModel.h ¶
	:src:simulation:GBObjectWorld.h ¶
	:src:sides:GBScores.h ¶
	:src:simulation:GBObject.h ¶
	:src:support:GBTypes.h ¶
	:src:simulation:GBHardwareState.h ¶
	:src:support:GBColor.h ¶
	:src:simulation:GBMessages.h ¶
	:src:sides:GBHardwareSpec.h ¶
	:src:support:GBLongNumber.h ¶
	:src:support:GBPlatform.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h ¶
	:src:support:GBGraphics.h ¶
	:src:support:GBDeletionReporter.h

:ObjectFiles:Classic:GBBrain.cpp.x	Ä  ¶
	:src:brains:GBBrain.cpp ¶
	:src:brains:GBBrain.h ¶
	:src:support:GBErrors.h ¶
	:src:support:GBTypes.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h

:ObjectFiles:Classic:GBBrainSpec.cpp.x	Ä  ¶
	:src:brains:GBBrainSpec.cpp ¶
	:src:brains:GBBrainSpec.h ¶
	:src:support:GBTypes.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h ¶
	:src:support:GBErrors.h

:ObjectFiles:Classic:GBStackBrain.cpp.x	Ä  ¶
	:src:brains:GBStackBrain.cpp ¶
	:src:brains:GBStackBrain.h ¶
	:src:simulation:GBRobot.h ¶
	:src:brains:GBStackBrainOpcodes.h ¶
	:src:support:GBErrors.h ¶
	:src:sides:GBSide.h ¶
	:src:sides:GBRobotType.h ¶
	:src:simulation:GBWorld.h ¶
	:src:brains:GBStackBrainSpec.h ¶
	:src:brains:GBBrain.h ¶
	:src:support:GBFinePoint.h ¶
	:src:simulation:GBMessages.h ¶
	:src:simulation:GBObject.h ¶
	:src:support:GBTypes.h ¶
	:src:simulation:GBHardwareState.h ¶
	:src:support:GBRandomState.h ¶
	:src:support:GBColor.h ¶
	:src:support:GBModel.h ¶
	:src:sides:GBScores.h ¶
	:src:sides:GBHardwareSpec.h ¶
	:src:support:GBLongNumber.h ¶
	:src:simulation:GBObjectWorld.h ¶
	:src:support:GBNumber.h ¶
	:src:brains:GBBrainSpec.h ¶
	:src:support:GBStringUtilities.h ¶
	:src:support:GBGraphics.h ¶
	:src:support:GBDeletionReporter.h ¶
	:src:support:GBPlatform.h

:ObjectFiles:Classic:GBStackBrainPrimitives.cpp.x	Ä  ¶
	:src:brains:GBStackBrainPrimitives.cpp ¶
	:src:brains:GBStackBrain.h ¶
	:src:simulation:GBRobot.h ¶
	:src:brains:GBStackBrainOpcodes.h ¶
	:src:support:GBErrors.h ¶
	:src:sides:GBSide.h ¶
	:src:sides:GBRobotType.h ¶
	:src:support:GBSound.h ¶
	:src:simulation:GBWorld.h ¶
	:src:brains:GBStackBrainSpec.h ¶
	:src:brains:GBBrain.h ¶
	:src:support:GBFinePoint.h ¶
	:src:simulation:GBMessages.h ¶
	:src:simulation:GBObject.h ¶
	:src:support:GBTypes.h ¶
	:src:simulation:GBHardwareState.h ¶
	:src:support:GBRandomState.h ¶
	:src:support:GBColor.h ¶
	:src:support:GBModel.h ¶
	:src:sides:GBScores.h ¶
	:src:sides:GBHardwareSpec.h ¶
	:src:support:GBLongNumber.h ¶
	:src:simulation:GBObjectWorld.h ¶
	:src:support:GBNumber.h ¶
	:src:brains:GBBrainSpec.h ¶
	:src:support:GBStringUtilities.h ¶
	:src:support:GBGraphics.h ¶
	:src:support:GBDeletionReporter.h ¶
	:src:support:GBPlatform.h

:ObjectFiles:Classic:GBStackBrainSpec.cpp.x	Ä  ¶
	:src:brains:GBStackBrainSpec.cpp ¶
	:src:brains:GBStackBrainSpec.h ¶
	:src:brains:GBStackBrainOpcodes.h ¶
	:src:support:GBErrors.h ¶
	:src:brains:GBStackBrain.h ¶
	:src:support:GBNumber.h ¶
	:src:brains:GBBrainSpec.h ¶
	:src:support:GBStringUtilities.h ¶
	:src:brains:GBBrain.h ¶
	:src:support:GBFinePoint.h ¶
	:src:simulation:GBMessages.h ¶
	:src:support:GBTypes.h ¶
	:src:support:GBPlatform.h ¶
	:src:support:GBColor.h

:ObjectFiles:Classic:GBStackBrainOpcodes.cpp.x	Ä  ¶
	:src:brains:GBStackBrainOpcodes.cpp ¶
	:src:brains:GBStackBrainOpcodes.h

:ObjectFiles:Classic:GBView.cpp.x	Ä  ¶
	:src:views:GBView.cpp ¶
	:src:views:GBView.h ¶
	:src:support:GBErrors.h ¶
	:src:support:GBColor.h ¶
	:src:support:GBStringUtilities.h ¶
	:src:support:GBTypes.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBGraphics.h ¶
	:src:support:GBMilliseconds.h ¶
	:src:support:GBPlatform.h ¶
	:src:support:GBFinePoint.h

:ObjectFiles:Classic:GBListView.cpp.x	Ä  ¶
	:src:views:GBListView.cpp ¶
	:src:views:GBListView.h ¶
	:src:views:GBView.h ¶
	:src:support:GBColor.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBGraphics.h ¶
	:src:support:GBMilliseconds.h ¶
	:src:support:GBErrors.h ¶
	:src:support:GBPlatform.h

:ObjectFiles:Classic:GBAboutBox.cpp.x	Ä  ¶
	:src:views:GBAboutBox.cpp ¶
	:src:views:GBAboutBox.h ¶
	:src:support:GBColor.h ¶
	:src:views:GBView.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBGraphics.h ¶
	:src:support:GBMilliseconds.h ¶
	:src:support:GBErrors.h ¶
	:src:support:GBPlatform.h

:ObjectFiles:Classic:GBPortal.cpp.x	Ä  ¶
	:src:views:GBPortal.cpp ¶
	:src:views:GBPortal.h ¶
	:src:simulation:GBFood.h ¶
	:src:simulation:GBShot.h ¶
	:src:sides:GBSide.h ¶
	:src:sides:GBRobotType.h ¶
	:src:support:GBRandomState.h ¶
	:src:simulation:GBRobot.h ¶
	:src:views:GBView.h ¶
	:src:simulation:GBWorld.h ¶
	:src:simulation:GBObject.h ¶
	:src:support:GBColor.h ¶
	:src:support:GBModel.h ¶
	:src:support:GBTypes.h ¶
	:src:simulation:GBMessages.h ¶
	:src:sides:GBScores.h ¶
	:src:sides:GBHardwareSpec.h ¶
	:src:support:GBLongNumber.h ¶
	:src:simulation:GBHardwareState.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBGraphics.h ¶
	:src:support:GBMilliseconds.h ¶
	:src:simulation:GBObjectWorld.h ¶
	:src:support:GBDeletionReporter.h ¶
	:src:support:GBFinePoint.h ¶
	:src:support:GBErrors.h ¶
	:src:support:GBPlatform.h

:ObjectFiles:Classic:GBMiniMap.cpp.x	Ä  ¶
	:src:views:GBMiniMap.cpp ¶
	:src:views:GBMiniMap.h ¶
	:src:views:GBView.h ¶
	:src:simulation:GBWorld.h ¶
	:src:views:GBPortal.h ¶
	:src:support:GBColor.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBGraphics.h ¶
	:src:support:GBMilliseconds.h ¶
	:src:support:GBRandomState.h ¶
	:src:support:GBModel.h ¶
	:src:simulation:GBObjectWorld.h ¶
	:src:sides:GBScores.h ¶
	:src:support:GBErrors.h ¶
	:src:support:GBPlatform.h ¶
	:src:support:GBTypes.h ¶
	:src:simulation:GBObject.h ¶
	:src:support:GBLongNumber.h ¶
	:src:support:GBFinePoint.h ¶
	:src:support:GBDeletionReporter.h

:ObjectFiles:Classic:GBRosterView.cpp.x	Ä  ¶
	:src:views:GBRosterView.cpp ¶
	:src:views:GBRosterView.h ¶
	:src:sides:GBSide.h ¶
	:src:simulation:GBWorld.h ¶
	:src:support:GBStringUtilities.h ¶
	:src:views:GBListView.h ¶
	:src:support:GBModel.h ¶
	:src:support:GBTypes.h ¶
	:src:support:GBColor.h ¶
	:src:simulation:GBMessages.h ¶
	:src:sides:GBScores.h ¶
	:src:support:GBRandomState.h ¶
	:src:simulation:GBObjectWorld.h ¶
	:src:support:GBPlatform.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h ¶
	:src:support:GBErrors.h ¶
	:src:views:GBView.h ¶
	:src:support:GBLongNumber.h ¶
	:src:simulation:GBObject.h ¶
	:src:support:GBGraphics.h ¶
	:src:support:GBMilliseconds.h ¶
	:src:support:GBDeletionReporter.h

:ObjectFiles:Classic:GBTournamentView.cpp.x	Ä  ¶
	:src:views:GBTournamentView.cpp ¶
	:src:views:GBTournamentView.h ¶
	:src:sides:GBSide.h ¶
	:src:simulation:GBWorld.h ¶
	:src:support:GBStringUtilities.h ¶
	:src:views:GBListView.h ¶
	:src:support:GBColor.h ¶
	:src:support:GBModel.h ¶
	:src:support:GBTypes.h ¶
	:src:simulation:GBMessages.h ¶
	:src:sides:GBScores.h ¶
	:src:support:GBRandomState.h ¶
	:src:simulation:GBObjectWorld.h ¶
	:src:support:GBPlatform.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h ¶
	:src:support:GBErrors.h ¶
	:src:views:GBView.h ¶
	:src:support:GBLongNumber.h ¶
	:src:simulation:GBObject.h ¶
	:src:support:GBGraphics.h ¶
	:src:support:GBMilliseconds.h ¶
	:src:support:GBDeletionReporter.h

:ObjectFiles:Classic:GBScoresView.cpp.x	Ä  ¶
	:src:views:GBScoresView.cpp ¶
	:src:views:GBScoresView.h ¶
	:src:sides:GBSide.h ¶
	:src:simulation:GBWorld.h ¶
	:src:sides:GBRobotType.h ¶
	:src:support:GBStringUtilities.h ¶
	:src:views:GBListView.h ¶
	:src:support:GBColor.h ¶
	:src:support:GBModel.h ¶
	:src:support:GBTypes.h ¶
	:src:simulation:GBMessages.h ¶
	:src:sides:GBScores.h ¶
	:src:support:GBRandomState.h ¶
	:src:simulation:GBObjectWorld.h ¶
	:src:sides:GBHardwareSpec.h ¶
	:src:support:GBLongNumber.h ¶
	:src:support:GBPlatform.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h ¶
	:src:support:GBErrors.h ¶
	:src:views:GBView.h ¶
	:src:simulation:GBObject.h ¶
	:src:support:GBGraphics.h ¶
	:src:support:GBMilliseconds.h ¶
	:src:support:GBDeletionReporter.h

:ObjectFiles:Classic:GBRobotTypeView.cpp.x	Ä  ¶
	:src:views:GBRobotTypeView.cpp ¶
	:src:sides:GBRobotType.h ¶
	:src:views:GBRobotTypeView.h ¶
	:src:sides:GBSide.h ¶
	:src:brains:GBBrainSpec.h ¶
	:src:support:GBStringUtilities.h ¶
	:src:simulation:GBWorld.h ¶
	:src:sides:GBHardwareSpec.h ¶
	:src:support:GBColor.h ¶
	:src:support:GBModel.h ¶
	:src:support:GBLongNumber.h ¶
	:src:views:GBListView.h ¶
	:src:support:GBTypes.h ¶
	:src:simulation:GBMessages.h ¶
	:src:sides:GBScores.h ¶
	:src:support:GBPlatform.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h ¶
	:src:support:GBErrors.h ¶
	:src:support:GBRandomState.h ¶
	:src:simulation:GBObjectWorld.h ¶
	:src:views:GBView.h ¶
	:src:simulation:GBObject.h ¶
	:src:support:GBGraphics.h ¶
	:src:support:GBMilliseconds.h ¶
	:src:support:GBDeletionReporter.h

:ObjectFiles:Classic:GBDebugger.cpp.x	Ä  ¶
	:src:views:GBDebugger.cpp ¶
	:src:simulation:GBRobot.h ¶
	:src:simulation:GBWorld.h ¶
	:src:views:GBDebugger.h ¶
	:src:brains:GBStackBrain.h ¶
	:src:sides:GBRobotType.h ¶
	:src:simulation:GBObject.h ¶
	:src:support:GBTypes.h ¶
	:src:simulation:GBHardwareState.h ¶
	:src:support:GBRandomState.h ¶
	:src:support:GBModel.h ¶
	:src:simulation:GBObjectWorld.h ¶
	:src:sides:GBScores.h ¶
	:src:views:GBView.h ¶
	:src:brains:GBStackBrainSpec.h ¶
	:src:brains:GBBrain.h ¶
	:src:support:GBFinePoint.h ¶
	:src:simulation:GBMessages.h ¶
	:src:sides:GBHardwareSpec.h ¶
	:src:support:GBColor.h ¶
	:src:support:GBLongNumber.h ¶
	:src:support:GBGraphics.h ¶
	:src:support:GBDeletionReporter.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBMilliseconds.h ¶
	:src:brains:GBBrainSpec.h ¶
	:src:support:GBStringUtilities.h ¶
	:src:support:GBPlatform.h ¶
	:src:support:GBErrors.h

:ObjectFiles:Classic:GBSideDebugger.cpp.x	Ä  ¶
	:src:views:GBSideDebugger.cpp ¶
	:src:simulation:GBWorld.h ¶
	:src:views:GBSideDebugger.h ¶
	:src:sides:GBSide.h ¶
	:src:support:GBStringUtilities.h ¶
	:src:support:GBErrors.h ¶
	:src:support:GBRandomState.h ¶
	:src:support:GBModel.h ¶
	:src:simulation:GBObjectWorld.h ¶
	:src:sides:GBScores.h ¶
	:src:views:GBView.h ¶
	:src:support:GBColor.h ¶
	:src:support:GBTypes.h ¶
	:src:simulation:GBMessages.h ¶
	:src:support:GBPlatform.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h ¶
	:src:simulation:GBObject.h ¶
	:src:support:GBLongNumber.h ¶
	:src:support:GBGraphics.h ¶
	:src:support:GBMilliseconds.h ¶
	:src:support:GBDeletionReporter.h

:ObjectFiles:Classic:GBWindow.cpp.x	Ä  ¶
	:src:ui:GBWindow.cpp ¶
	:src:ui:GBWindow.h ¶
	:src:support:GBErrors.h ¶
	:src:support:GBStringUtilities.h ¶
	:src:views:GBView.h ¶
	:src:support:GBPlatform.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h ¶
	:src:support:GBColor.h ¶
	:src:support:GBGraphics.h ¶
	:src:support:GBMilliseconds.h

:ObjectFiles:Classic:GBViewsApplication.cpp.x	Ä  ¶
	:src:ui:GBViewsApplication.cpp ¶
	:src:support:GBPlatform.h ¶
	:src:ui:GBWindow.h ¶
	:src:support:GBErrors.h ¶
	:src:ui:GBViewsApplication.h ¶
	:src:views:GBView.h ¶
	:src:support:GBColor.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBGraphics.h ¶
	:src:support:GBMilliseconds.h

:ObjectFiles:Classic:GBApplication.cpp.x	Ä  ¶
	:src:ui:GBApplication.cpp ¶
	:src:support:GBPlatform.h ¶
	:src:ui:GBApplication.h ¶
	:src:simulation:GBWorld.h ¶
	:src:sides:GBSide.h ¶
	:src:views:GBPortal.h ¶
	:src:views:GBMiniMap.h ¶
	:src:views:GBRosterView.h ¶
	:src:views:GBAboutBox.h ¶
	:src:views:GBScoresView.h ¶
	:src:views:GBRobotTypeView.h ¶
	:src:views:GBDebugger.h ¶
	:src:views:GBSideDebugger.h ¶
	:src:views:GBTournamentView.h ¶
	:src:sides:GBSideReader.h ¶
	:src:support:GBSound.h ¶
	:src:support:GBStringUtilities.h ¶
	:src:ui:GBWindow.h ¶
	:src:ui:GBViewsApplication.h ¶
	:src:support:GBTypes.h ¶
	:src:support:GBMilliseconds.h ¶
	:src:support:GBRandomState.h ¶
	:src:support:GBModel.h ¶
	:src:simulation:GBObjectWorld.h ¶
	:src:sides:GBScores.h ¶
	:src:support:GBColor.h ¶
	:src:simulation:GBMessages.h ¶
	:src:views:GBView.h ¶
	:src:views:GBListView.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h ¶
	:src:support:GBErrors.h ¶
	:src:simulation:GBObject.h ¶
	:src:support:GBLongNumber.h ¶
	:src:support:GBGraphics.h ¶
	:src:support:GBDeletionReporter.h

:ObjectFiles:Classic:GBMain.cpp.x	Ä  ¶
	:src:ui:GBMain.cpp ¶
	:src:ui:GBApplication.h ¶
	:src:support:GBErrors.h ¶
	:src:ui:GBViewsApplication.h ¶
	:src:support:GBTypes.h ¶
	:src:support:GBMilliseconds.h ¶
	:src:simulation:GBWorld.h ¶
	:src:sides:GBSide.h ¶
	:src:views:GBView.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h ¶
	:src:support:GBRandomState.h ¶
	:src:support:GBModel.h ¶
	:src:simulation:GBObjectWorld.h ¶
	:src:sides:GBScores.h ¶
	:src:support:GBColor.h ¶
	:src:simulation:GBMessages.h ¶
	:src:support:GBGraphics.h ¶
	:src:simulation:GBObject.h ¶
	:src:support:GBLongNumber.h ¶
	:src:support:GBPlatform.h ¶
	:src:support:GBDeletionReporter.h

