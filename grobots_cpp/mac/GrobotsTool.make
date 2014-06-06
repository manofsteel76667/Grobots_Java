#   File:       Grobots.make
#   Target:     Grobots
#   Created:    Monday, January 3, 2000 11:45:22 PM


MAKEFILE        = GrobotsTool.make
¥MondoBuild¥    =   # Make blank to avoid rebuilds when makefile is modified

ObjDir          = :ObjectFiles:Tool:
Includes        = -i :src:support: ¶
				  -i :src:simulation: ¶
				  -i :src:sides: ¶
				  -i :src:brains: ¶
				  -i "{STL}"

Sym-PPC         = -sym off

PPCCPlusOptions = {Includes} {Sym-PPC} ¶
				  -bool on -exceptions on ¶
				  -ansi on -ansifor -w 23 -enum int -u nil ¶
				  -includes unix -d HEADLESS=1

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
				  :src:GBHeadless.cpp ¶

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
				  "{ObjDir}GBHeadless.cpp.x" ¶

### Libraries ###

LibFiles-PPC    =  ¶
				  "{SharedLibraries}InterfaceLib" ¶
				  "{SharedLibraries}StdCLib" ¶
				  "{SharedLibraries}MathLib" ¶
				  "{PPCLibraries}StdCRuntime.o" ¶
				  "{PPCLibraries}PPCStdCLib.o" ¶
				  "{PPCLibraries}PPCCRuntime.o" ¶
				  "{PPCLibraries}PPCToolLibs.o" ¶
				  "{PPCLibraries}MrCPlusLib.o" ¶
				  "{PPCLibraries}MrCIOStreams.o" ¶
				  "{SharedLibraries}MrCExceptionsLib" ¶
				  "{STL}:lib:STLportLib.PPC.o"

### Default Rules ###

.cpp.x  Ä  .cpp  {¥MondoBuild¥}
	{PPCCPlus} {depDir}{default}.cpp -o {targDir}{default}.cpp.x {PPCCPlusOptions}

# Rebuild About box when anything changes, to update date.
"{ObjDir}GBAboutBox.cpp.x" Ä {SrcFiles}

### Build Rules ###

GrobotsTool  ÄÄ  {ObjFiles-PPC} {LibFiles-PPC} {¥MondoBuild¥}
	PPCLink ¶
		-o {Targ} ¶
		{ObjFiles-PPC} ¶
		{LibFiles-PPC} ¶
		{Sym-PPC} ¶
		-linkfaster off ¶
		-mf -d ¶
		-t 'MPST' -c 'MPS '

GrobotsTool  ÄÄ  ":src:mac:Sounds.rsrc" {¥MondoBuild¥}
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
# These dependencies were produced at 8:25:37 pm on 2004 Dec 14 Tue by MakeDepend

:ObjectFiles:Tool:GBErrors.cpp.x	Ä  ¶
	:src:support:GBErrors.cpp ¶
	:src:support:GBErrors.h ¶
	:src:support:GBStringUtilities.h ¶
	:src:support:GBPlatform.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h ¶
	:src:support:GBColor.h

:ObjectFiles:Tool:GBRandomState.cpp.x	Ä  ¶
	:src:support:GBRandomState.cpp ¶
	:src:support:GBRandomState.h ¶
	:src:support:GBErrors.h ¶
	:src:support:GBStringUtilities.h ¶
	:src:support:GBColor.h ¶
	:src:support:GBMilliseconds.h ¶
	:src:support:GBTypes.h ¶
	:src:support:GBPlatform.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h

:ObjectFiles:Tool:GBColor.cpp.x	Ä  ¶
	:src:support:GBColor.cpp ¶
	:src:support:GBColor.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBPlatform.h ¶
	:src:support:GBErrors.h

:ObjectFiles:Tool:GBNumber.cpp.x	Ä  ¶
	:src:support:GBNumber.cpp ¶
	:src:support:GBNumber.h ¶
	:src:support:GBStringUtilities.h ¶
	:src:support:GBErrors.h ¶
	:src:support:GBPlatform.h ¶
	:src:support:GBFinePoint.h ¶
	:src:support:GBColor.h

:ObjectFiles:Tool:GBLongNumber.cpp.x	Ä  ¶
	:src:support:GBLongNumber.cpp ¶
	:src:support:GBLongNumber.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBErrors.h

:ObjectFiles:Tool:GBFinePoint.cpp.x	Ä  ¶
	:src:support:GBFinePoint.cpp ¶
	:src:support:GBFinePoint.h ¶
	:src:support:GBErrors.h ¶
	:src:support:GBStringUtilities.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBPlatform.h ¶
	:src:support:GBColor.h

:ObjectFiles:Tool:GBModel.cpp.x	Ä  ¶
	:src:support:GBModel.cpp ¶
	:src:support:GBModel.h

:ObjectFiles:Tool:GBStringUtilities.cpp.x	Ä  ¶
	:src:support:GBStringUtilities.cpp ¶
	:src:support:GBStringUtilities.h ¶
	:src:support:GBPlatform.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h ¶
	:src:support:GBErrors.h ¶
	:src:support:GBColor.h

:ObjectFiles:Tool:GBSound.cpp.x	Ä  ¶
	:src:support:GBSound.cpp ¶
	:src:support:GBSound.h ¶
	:src:support:GBErrors.h ¶
	:src:support:GBPlatform.h

:ObjectFiles:Tool:GBDeletionReporter.cpp.x	Ä  ¶
	:src:support:GBDeletionReporter.cpp ¶
	:src:support:GBDeletionReporter.h ¶
	:src:support:GBErrors.h ¶
	:src:support:GBPlatform.h

:ObjectFiles:Tool:GBGraphics.cpp.x	Ä  ¶
	:src:support:GBGraphics.cpp ¶
	:src:support:GBGraphics.h ¶
	:src:support:GBStringUtilities.h ¶
	:src:support:GBPlatform.h ¶
	:src:support:GBColor.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h ¶
	:src:support:GBErrors.h

:ObjectFiles:Tool:GBMilliseconds.cpp.x	Ä  ¶
	:src:support:GBMilliseconds.cpp ¶
	:src:support:GBMilliseconds.h ¶
	:src:support:GBPlatform.h

:ObjectFiles:Tool:GBScores.cpp.x	Ä  ¶
	:src:sides:GBScores.cpp ¶
	:src:sides:GBScores.h ¶
	:src:support:GBTypes.h ¶
	:src:support:GBLongNumber.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h ¶
	:src:support:GBErrors.h

:ObjectFiles:Tool:GBSide.cpp.x	Ä  ¶
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

:ObjectFiles:Tool:GBRobotType.cpp.x	Ä  ¶
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

:ObjectFiles:Tool:GBHardwareSpec.cpp.x	Ä  ¶
	:src:sides:GBHardwareSpec.cpp ¶
	:src:sides:GBHardwareSpec.h ¶
	:src:support:GBTypes.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h ¶
	:src:support:GBErrors.h

:ObjectFiles:Tool:GBSideReader.cpp.x	Ä  ¶
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

:ObjectFiles:Tool:GBObject.cpp.x	Ä  ¶
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

:ObjectFiles:Tool:GBObjectWorld.cpp.x	Ä  ¶
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

:ObjectFiles:Tool:GBFood.cpp.x	Ä  ¶
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

:ObjectFiles:Tool:GBShot.cpp.x	Ä  ¶
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

:ObjectFiles:Tool:GBRobot.cpp.x	Ä  ¶
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

:ObjectFiles:Tool:GBHardwareState.cpp.x	Ä  ¶
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

:ObjectFiles:Tool:GBSensorShot.cpp.x	Ä  ¶
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

:ObjectFiles:Tool:GBMessages.cpp.x	Ä  ¶
	:src:simulation:GBMessages.cpp ¶
	:src:simulation:GBMessages.h ¶
	:src:support:GBTypes.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h ¶
	:src:support:GBErrors.h

:ObjectFiles:Tool:GBDecorations.cpp.x	Ä  ¶
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

:ObjectFiles:Tool:GBWorld.cpp.x	Ä  ¶
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

:ObjectFiles:Tool:GBBrain.cpp.x	Ä  ¶
	:src:brains:GBBrain.cpp ¶
	:src:brains:GBBrain.h ¶
	:src:support:GBErrors.h ¶
	:src:support:GBTypes.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h

:ObjectFiles:Tool:GBBrainSpec.cpp.x	Ä  ¶
	:src:brains:GBBrainSpec.cpp ¶
	:src:brains:GBBrainSpec.h ¶
	:src:support:GBTypes.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h ¶
	:src:support:GBErrors.h

:ObjectFiles:Tool:GBStackBrain.cpp.x	Ä  ¶
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

:ObjectFiles:Tool:GBStackBrainPrimitives.cpp.x	Ä  ¶
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

:ObjectFiles:Tool:GBStackBrainSpec.cpp.x	Ä  ¶
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

:ObjectFiles:Tool:GBStackBrainOpcodes.cpp.x	Ä  ¶
	:src:brains:GBStackBrainOpcodes.cpp ¶
	:src:brains:GBStackBrainOpcodes.h

:ObjectFiles:Tool:GBHeadless.cpp.x	Ä  ¶
	:src:GBHeadless.cpp ¶
	:src:simulation:GBWorld.h ¶
	:src:support:GBErrors.h ¶
	:src:support:GBRandomState.h ¶
	:src:support:GBModel.h ¶
	:src:simulation:GBObjectWorld.h ¶
	:src:sides:GBScores.h ¶
	:src:support:GBTypes.h ¶
	:src:support:GBColor.h ¶
	:src:simulation:GBObject.h ¶
	:src:support:GBLongNumber.h ¶
	:src:support:GBNumber.h ¶
	:src:support:GBFinePoint.h ¶
	:src:support:GBGraphics.h ¶
	:src:support:GBDeletionReporter.h ¶
	:src:support:GBPlatform.h

