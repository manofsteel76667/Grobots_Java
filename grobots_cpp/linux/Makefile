#Grobots Makefile for Linux, headless mode 
#Created by Warren Schudy November 12, 2005 
 
#List of file names by folder 
SRC_DIR = ..#location of sources relative to makefile 
OBJ_DIR = .#location to put object files relative to makefile 
BRAINOBJS = GBBrain GBBrainSpec GBStackBrain GBStackBrainOpcodes GBStackBrainPrimitives GBStackBrainSpec 
SIDESOBJS = GBHardwareSpec GBRobotType GBScores GBSide GBSideReader 
SIMULATIONOBJS = GBDecorations GBFood GBHardwareState GBMessages GBObject GBObjectWorld GBRobot GBSensorShot GBShot GBWorld 
SUPPORTOBJS = GBColor GBDeletionReporter GBErrors GBFinePoint GBGraphics GBLongNumber GBMilliseconds GBModel GBNumber GBRandomState GBSound GBStringUtilities 
 
#WJS 20051112 The .SUFFIXES line was in the makefile I copied - don't know if we need it or what it does 
.SUFFIXES: .H .cpp 
 
OBJECTS = GBHeadless $(BRAINOBJS:%=brains/%) $(SIDESOBJS:%=sides/%) $(SIMULATIONOBJS:%=simulation/%) $(SUPPORTOBJS:%=support/%) 
#OFILES is the list of object files brains/GBBrain.o brains/GBBrainSpec.o etc. 
OFILES = $(OBJECTS:%=$(OBJ_DIR)/%.o) 
 
#include directories 
INCLUDES = -I ../brains -I ../sides -I ../simulation -I ../support -I ../views 
 
CCC = g++ 
CCOPT = -O  
 
EXEC    = grobots #Executable name 
 
# --------------------------------------------------------------------- 
# Link options and libraries 
# --------------------------------------------------------------------- 
 
CCLNFLAGS =  -lm  
 
CCFLAGS = $(CCOPT) $(INCLUDES) -DHEADLESS 
 
# ------------------------------------------------------------ 
default:
	echo "To use, \"make prepare\" and then \"make all\""

prepare:
	mkdir brains
	mkdir sides
	mkdir support
	mkdir simulation

all     : $(EXEC) 
 
clean : 
	/bin/rm -f *.o *~ $(OFILES) 
	/bin/rm -f $(EXEC) 
 
$(EXEC): $(OFILES) 
	@echo 
	$(CCC) $(CCFLAGS) $(OFILES) -o $(EXEC) $(CCLNFLAGS) 
 
$(OBJ_DIR)/%.o: $(SRC_DIR)/%.cpp 
	$(CCC) $(CCFLAGS)  -c $< -o $@ 
 
#For some reason it doesn't work without individual rules for the folders 
$(OBJ_DIR)/brains/%.o:  $(SRC_DIR)/brains/%.cpp 
	$(CCC) $(CCFLAGS)  -c $< -o $@ 
 
$(OBJ_DIR)/sides/%.o:   $(SRC_DIR)/sides/%.cpp 
	$(CCC) $(CCFLAGS)  -c $< -o $@ 
 
$(OBJ_DIR)/simulation/%.o:      $(SRC_DIR)/simulation/%.cpp 
	$(CCC) $(CCFLAGS)  -c $< -o $@ 
 
$(OBJ_DIR)/support/%.o: $(SRC_DIR)/support/%.cpp 
	$(CCC) $(CCFLAGS)  -c $< -o $@ 