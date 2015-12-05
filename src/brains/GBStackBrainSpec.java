/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package brains;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import support.FinePoint;
import exception.GBBrainError;

//typedef GBNumber GBStackDatum; doublr
//typedef unsigned short GBStackInstruction; int
//typedef long GBStackAddress; int
//typedef long GBSymbolIndex; int

class GBSymbol {
	// public:
	String name;

	public GBSymbol() {
	}

	public GBSymbol(String n) {
		name = n;
	}

	public GBSymbol(GBSymbol arg) {
		name = arg.name;
	}

	/**
	 * Hashcode of the symbol. For the purposes of the brain, the value of any
	 * descendant classes do not contribute to the level of uniqueness of the
	 * symbol; only its name matters.
	 */
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (!(other instanceof GBSymbol))
			return false;
		return name.toLowerCase().equals(((GBSymbol) other).name.toLowerCase());
	}
};

class GBConstant extends GBSymbol {
	// public:
	double value;

	public GBConstant() {
	}

	public GBConstant(String _name, double _value) {
		name = _name;
		value = _value;
	}

	public GBConstant(GBConstant arg) {
		name = arg.name;
		value = arg.value;
	}
};

class GBVectorSymbol extends GBSymbol {
	// public:
	FinePoint value;

	public GBVectorSymbol() {
	}

	public GBVectorSymbol(String n, FinePoint v) {
		name = n;
		value = v;
	}

	public GBVectorSymbol(GBVectorSymbol arg) {
		name = arg.name;
		value = arg.value;
	}
};

class GBLabel extends GBSymbol {
	// public:
	int address;
	boolean gensym;
	boolean referenced;

	public GBLabel()

	{
		address = -1;
	}

	public GBLabel(String n, int a, boolean g) {
		name = n;
		address = a;
		gensym = g;
	}

	public GBLabel(GBLabel arg) {
		name = arg.name;
		address = arg.address;
		gensym = arg.gensym;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof GBLabel))
			return false;
		boolean ret = super.equals(other);
		ret = ret && !gensym && !((GBLabel) other).gensym;
		return ret;
	}
};

public class GBStackBrainSpec extends BrainSpec {
	public static final double kCostPerInstruction = 0.02;
	public static final double kBrainMassPerCost = 0.01;
	public static final double kMassPerInstruction = kCostPerInstruction
			* kBrainMassPerCost;
	public static final double kCostPerConstant = 0.03;
	public static final double kMassPerConstant = kCostPerConstant
			* kBrainMassPerCost;
	public static final double kCostPerVariable = 0.1;
	public static final double kMassPerVariable = kCostPerVariable
			* kBrainMassPerCost;
	public static final double kCostPerLabel = 0.02;
	public static final double kMassPerLabel = kCostPerLabel
			* kBrainMassPerCost;
	List<Integer> code;
	List<Integer> lineNumbers;
	List<GBConstant> constants;
	List<GBConstant> variables;
	List<GBVectorSymbol> vectorVariables;
	List<GBLabel> labels;
	int startingLabel;
	// compile-time stack
	List<Integer> cStack;
	int gensymCounter;

	public GBStackBrainSpec() {
		code = new ArrayList<Integer>();
		lineNumbers = new ArrayList<Integer>();
		constants = new ArrayList<GBConstant>();
		variables = new ArrayList<GBConstant>();
		vectorVariables = new ArrayList<GBVectorSymbol>();
		labels = new ArrayList<GBLabel>();
		startingLabel = -1;
		cStack = new ArrayList<Integer>();
	}

	public GBStackBrainSpec(GBStackBrainSpec original) {
		this();
		for (int x : original.code)
			code.add(x);
		for (int x : original.lineNumbers)
			lineNumbers.add(x);
		Iterator<GBConstant> constIt = original.constants.iterator();
		while (constIt.hasNext()) {
			GBConstant x = constIt.next();
			constants.add(new GBConstant(x));
		}
		constIt = original.variables.iterator();
		while (constIt.hasNext()) {
			GBConstant x = constIt.next();
			variables.add(new GBConstant(x));
		}
		Iterator<GBVectorSymbol> vectorIt = original.vectorVariables.iterator();
		while (vectorIt.hasNext()) {
			GBVectorSymbol x = vectorIt.next();
			vectorVariables.add(new GBVectorSymbol(x));
		}
		Iterator<GBLabel> labelIt = original.labels.iterator();
		while (labelIt.hasNext()) {
			GBLabel x = labelIt.next();
			labels.add(new GBLabel(x));
		}
		startingLabel = original.startingLabel;
		for (int x : original.cStack)
			cStack.add(x);
		gensymCounter = original.gensymCounter;
	}

	@Override
	public Brain makeBrain() {
		return new GBStackBrain(this);
	}

	@Override
	public double getCost() {
		return kCostPerInstruction * code.size() + kCostPerConstant
				* constants.size() + kCostPerVariable * variables.size() + 2
				* kCostPerVariable * vectorVariables.size() + kCostPerLabel
				* labels.size();
	}

	@Override
	public double getMass() {
		return kMassPerInstruction * code.size() + kMassPerConstant
				* constants.size() + kMassPerVariable * variables.size() + 2
				* kMassPerVariable * vectorVariables.size() + kMassPerLabel
				* labels.size();
	}

	void addInstruction(int ins, int line) {
		code.add(ins);
		lineNumbers.add(line);
	}

	void addInstruction(int type, int index, int line) {
		code.add((type << StackBrainOpcode.kOpcodeTypeShift) + index);
		lineNumbers.add(line);
	}

	void cPush(int value) {
		cStack.add(value);
	}

	int cPeek() {
		if (cStack.isEmpty())
			throw new GBCStackError();
		return cStack.get(cStack.size() - 1);
	}

	int cPop() {
		if (cStack.isEmpty())
			throw new GBCStackError();
		int i = cStack.get(cStack.size() - 1);
		cStack.remove(cStack.size() - 1);
		return i;
	}

	void executeCWord(StackBrainOpcode _code, int line) {
		int temp, temp2;
		switch (_code) {
		case cwNop:
			break;
		case cwIf:
			temp = addGensym("if-skip-g");
			addLabelRead(temp, line);
			addInstruction(OpType.otPrimitive.ID,
					StackBrainOpcode.opNotIfGo.ID, line);
			// AddInstruction(OpType.otPrimitive.ID,
			// StackBrainOpcode.opNotIfGo.ID, line);
			cPush(temp);
			break;
		case cwNotIf:
			temp = addGensym("nif-skip-g");
			addLabelRead(temp, line);
			addInstruction(OpType.otPrimitive.ID, StackBrainOpcode.opIfGo.ID,
					line);
			cPush(temp);
			break;
		case cwElse:
			temp = addGensym("else-skip-g");
			addLabelRead(temp, line);
			addInstruction(OpType.otPrimitive.ID, StackBrainOpcode.opJump.ID,
					line);
			// and the address
			temp2 = cPop();
			resolveGensym(temp2);
			cPush(temp);
			break;
		case cwThen:
			temp = cPop();
			resolveGensym(temp);
			break;
		case cwAndIf:
			temp = cPeek();
			addLabelRead(temp, line);
			addInstruction(OpType.otPrimitive.ID,
					StackBrainOpcode.opNotIfGo.ID, line);
			break;
		case cwNotAndIf:
			temp = cPeek();
			addLabelRead(temp, line);
			addInstruction(OpType.otPrimitive.ID, StackBrainOpcode.opIfGo.ID,
					line);
			break;
		case cwCElse: // end skip -- end
			temp = cPop();
			temp2 = cPeek();
			addLabelRead(temp2, line);
			addInstruction(OpType.otPrimitive.ID, StackBrainOpcode.opJump.ID,
					line);
			resolveGensym(temp);
			break;
		case cwDo: // -- loop-start
			temp = addGensym("loop-start-g");
			resolveGensym(temp);
			cPush(temp);
			break;
		case cwLoop: // loop-start loop-end --
			temp = cPop();
			addLabelRead(cPop(), line);
			addInstruction(OpType.otPrimitive.ID, StackBrainOpcode.opJump.ID,
					line);
			resolveGensym(temp);
			break;
		case cwForever: // loop-start --
			addLabelRead(cPop(), line);
			addInstruction(OpType.otPrimitive.ID, StackBrainOpcode.opJump.ID,
					line);
			break;
		case cwWhile: // -- loop-end
			temp = addGensym("loop-end-g");
			cPush(temp);
			addLabelRead(temp, line);
			addInstruction(OpType.otPrimitive.ID,
					StackBrainOpcode.opNotIfGo.ID, line);
			break;
		case cwUntil: // -- loop-end
			temp = addGensym("loop-end-g");
			cPush(temp);
			addLabelRead(temp, line);
			addInstruction(OpType.otPrimitive.ID, StackBrainOpcode.opIfGo.ID,
					line);
			break;
		case cwWhileLoop: // loop-start --
			addLabelRead(cPop(), line);
			addInstruction(OpType.otPrimitive.ID, StackBrainOpcode.opIfGo.ID,
					line);
			break;
		case cwUntilLoop: // loop-start --
			addLabelRead(cPop(), line);
			addInstruction(OpType.otPrimitive.ID,
					StackBrainOpcode.opNotIfGo.ID, line);
			break;
		default:
			throw new GBUnknownInstructionError();
		}
	}

	void addLabelRead(int index, int line) {
		if (index >= labels.size() || index < 0)
			throw new GBBadSymbolIndexError();
		addInstruction(OpType.otLabelRead.ID, index, line);
		labels.get(index).referenced = true;
	}

	void addLabelCall(int index, int line) {
		if (index >= labels.size() || index < 0)
			throw new GBBadSymbolIndexError();
		addInstruction(OpType.otLabelCall.ID, index, line);
		labels.get(index).referenced = true;
	}

	void addImmediate(String name, double value, int line) {
		// avoid proliferation of immediates
		// (but should we require name equivalence?)
		for (int i = 0; i < constants.size(); i++)
			if (constants.get(i).value == value
					&& constants.get(i).name.equals(name)) {
				addInstruction(OpType.otConstantRead.ID, i, line);
				return;
			}
		addConstant(name, value);
		addInstruction(OpType.otConstantRead.ID, constants.size() - 1, line);
	}

	public void addConstant(String name, double value) {
		constants.add(new GBConstant(name, value));
	}

	public void addVariable(String name, double value) {
		variables.add(new GBConstant(name, value));
	}

	public void addVectorVariable(String name, FinePoint value) {
		vectorVariables.add(new GBVectorSymbol(name, value));
	}

	void addLabel(String name) {
		Iterator<GBLabel> it = labels.iterator();
		while (it.hasNext()) {
			// Check for forward declaration that hasn't been used yet
			GBLabel lbl = it.next();
			if (lbl.name.toLowerCase().equals(name.toLowerCase())) {
				if (lbl.address < 0) {
					lbl.address = code.size();
					return;
				} else
					throw new GBDuplicateSymbolError(name);
			}
		}
		labels.add(new GBLabel(name, code.size(), false));
	}

	void addForwardLabel(String name) {
		GBLabel lbl = new GBLabel(name, -1, false);
		lbl.referenced = true;
		labels.add(lbl);
	}

	int getConstantIndex(String name) {
		for (int i = 0; i < constants.size(); i++)
			if (constants.get(i).name.toLowerCase().equals(name.toLowerCase()))
				return i;
		return -1;
	}

	int getVariableIndex(String name) {
		for (int i = 0; i < variables.size(); i++)
			if (variables.get(i).name.toLowerCase().equals(name.toLowerCase()))
				return i;
		return -1;
	}

	int getVectorVariableIndex(String name) {
		for (int i = 0; i < vectorVariables.size(); i++)
			if (vectorVariables.get(i).name.toLowerCase().equals(
					name.toLowerCase()))
				return i;
		return -1;
	}

	int getLabelIndex(String name) {
		for (int i = 0; i < labels.size(); i++)
			if (!labels.get(i).gensym
					&& labels.get(i).name.toLowerCase().equals(
							name.toLowerCase()))
				return i;
		return -1;
	}

	public int getLabelReferencedIndex(String name) {
		for (int i = 0; i < labels.size(); i++)
			if (labels.get(i).name.toLowerCase().equals(name.toLowerCase()))
				return i;
		addForwardLabel(name);
		return labels.size() - 1;
	}

	public int addGensym(String name) {
		labels.add(new GBLabel(name + Integer.toString(++gensymCounter), -1,
				true));
		return labels.size() - 1;
	}

	public void resolveGensym(int index) {
		if (index < 0 || index >= labels.size())
			throw new GBBadSymbolIndexError();
		labels.get(index).address = code.size();
	}

	public void setStartingLabel(int index) {
		if (index < 0 || index >= labels.size())
			throw new GBBadSymbolIndexError();
		startingLabel = index;
		labels.get(index).referenced = true;
	}

	public void parseLine(String line, int lineNum) {
		String[] tokens = line.trim().split("\\s+");
		for (String token : tokens) {
			int index;
			StackBrainOpcode code;
			String stem;
			// Always try user-defined symbols first, so users can shadow
			// anything safely.
			// look for adornments
			if (token.length() >= 2)
				switch (token.charAt(token.length() - 1)) {
				case '!':
					stem = token.substring(0, token.length() - 1);
					index = getVariableIndex(stem);
					if (index != -1) {
						addInstruction(OpType.otVariableWrite.ID, index,
								lineNum);
						continue;
					}
					index = getVectorVariableIndex(stem);
					if (index != -1) {
						addInstruction(OpType.otVectorWrite.ID, index, lineNum);
						continue;
					}
					// Must be a reserved word
					code = StackBrainOpcode.byName(stem);
					if (code != null) {
						switch (code.type) {
						case ocHardwareVariable:
							addInstruction(OpType.otHardwareWrite.ID, code.ID,
									lineNum);
							continue;
						case ocHardwareVector:
							addInstruction(OpType.otHardwareVectorWrite.ID,
									code.ID, lineNum);
							continue;
						default:
							break;
						}
					}
					// I have no idea what you just said
					throw new GBUnknownSymbolError(stem);
				case ':':
					addLabel(token.substring(0, token.length() - 1));
					continue;
				case '&':
					stem = token.substring(0, token.length() - 1);
					index = getLabelReferencedIndex(stem);
					if (index != -1) {
						addLabelRead(index, lineNum);
						continue;
					}
					throw new GBUnknownSymbolError(stem); // shouldn't
					// happen
				case '^':
					stem = token.substring(0, token.length() - 1);
					index = getLabelReferencedIndex(stem);
					if (index != -1) {
						addLabelCall(index, lineNum);
						continue;
					}
					throw new GBUnknownSymbolError(stem); // shouldn't
					// happen
				default: // unadorned
					break;
				}
			// unadorned - look up in various tables
			index = getConstantIndex(token);
			if (index != -1) {
				addInstruction(OpType.otConstantRead.ID, index, lineNum);
				continue;
			}
			index = getVariableIndex(token);
			if (index != -1) {
				addInstruction(OpType.otVariableRead.ID, index, lineNum);
				continue;
			}
			index = getVectorVariableIndex(token);
			if (index != -1) {
				addInstruction(OpType.otVectorRead.ID, index, lineNum);
				continue;
			}
			index = getLabelIndex(token);
			if (index != -1) {
				addLabelCall(index, lineNum);
				continue;
			}
			code = StackBrainOpcode.byName(token);
			if (code != null)
				switch (code.type) {
				case ocCompileWord:
					executeCWord(code, lineNum);
					continue;
				case ocHardwareVariable:
					addInstruction(OpType.otHardwareRead.ID, code.ID, lineNum);
					continue;
				case ocHardwareVector:
					addInstruction(OpType.otHardwareVectorRead.ID, code.ID,
							lineNum);
					continue;
				case ocPrimitive:
					addInstruction(OpType.otPrimitive.ID, code.ID, lineNum);
					continue;
				default:
					break;
				}
			// try as a number
			try {
				double num = Double.parseDouble(token);
				addImmediate(token, num, lineNum);
				continue;
			} catch (NumberFormatException e) {
			}
			// nothing's working
			throw new GBUnknownSymbolError(token);
		}
	}

	@Override
	public void check() {
		// check compile-time stack
		if (!cStack.isEmpty())
			throw new GBCStackError();
		// check forwards
		for (int i = 0; i < labels.size(); i++)
			if (labels.get(i).address < 0)
				throw new GBUnresolvedSymbolError(labels.get(i).name);
		// add sentinel
		addInstruction(OpType.otPrimitive.ID, StackBrainOpcode.opEnd.ID, -1);
	}

	int getInstructionsCount() {
		return code.size();
	}

	int getConstantsCount() {
		return constants.size();
	}

	int getVariablesCount() {
		return variables.size();
	}

	int getVectorVariablesCount() {
		return vectorVariables.size();
	}

	int getLabelsCount() {
		return labels.size();
	}

	int readInstruction(int index) {
		return code.get(index);
	}

	double readConstant(int index) {
		if (index < 0 || index >= constants.size())
			throw new GBBadSymbolIndexError();
		return constants.get(index).value;
	}

	double readVariable(int index) {
		if (index < 0 || index >= variables.size())
			throw new GBBadSymbolIndexError();
		return variables.get(index).value;
	}

	FinePoint readVectorVariable(int index) {
		if (index < 0 || index >= vectorVariables.size())
			throw new GBBadSymbolIndexError();
		return vectorVariables.get(index).value;
	}

	int readLabel(int index) {
		if (index < 0 || index >= labels.size())
			throw new GBBadSymbolIndexError();
		return labels.get(index).address;
	}

	int getStartAddress() {
		if (startingLabel < 0)
			return 0; // default to first instruction
		return readLabel(startingLabel);
	}

	int getLineNumber(int index) {
		if (index < 0 || index >= code.size())
			return 0;
		return lineNumbers.get(index);
	}

	// For the debugger: label name if available, or else the raw address.
	String getAddressName(int addr) {
		for (int i = 0; i < labels.size(); i++)
			if (labels.get(i).address == addr)
				return labels.get(i).name;
		return Integer.toString(addr);
	}

	// Human-readable address, e.g. "main + 62"
	String getAddressDescription(int addr) {
		GBLabel label = getAddressLastLabel(addr);
		if (label != null)
			return addr == label.address ? label.name : label.name + " + "
					+ Integer.toString(addr - label.address);
		else
			return Integer.toString(addr);
	}

	String getAddressAndLine(int addr) {
		return getAddressDescription(addr) + " (line "
				+ Integer.toString(getLineNumber(addr)) + ")";
	}

	GBLabel getAddressLastLabel(int addr) {
		GBLabel closest = null;
		for (int i = 0; i < labels.size(); i++)
			if (!labels.get(i).gensym
					&& labels.get(i).address < addr
					&& (closest == null || labels.get(i).address > closest.address))
				closest = labels.get(i);
		return closest;
	}

	String disassembleAddress(int addr) {
		if (addr < 0 || addr >= code.size())
			return "";
		return disassembleInstruction(code.get(addr));
	}

	String disassembleInstruction(int instruction) {
		int index = instruction & StackBrainOpcode.kOpcodeIndexMask;
		switch (OpType.byID(instruction >> StackBrainOpcode.kOpcodeTypeShift)) {
		case otPrimitive:
			if (StackBrainOpcode.byID(index).type != OpCodeType.ocPrimitive)
				return "bad-primitive";
			return StackBrainOpcode.byID(index).name;
		case otConstantRead:
			if (index >= constants.size())
				return "bad-constant-read";
			return constants.get(index).name;
		case otVariableRead:
			if (index >= variables.size())
				return "bad-variable-read";
			return variables.get(index).name;
		case otVariableWrite:
			if (index >= variables.size())
				return "bad-variable-write";
			return variables.get(index).name + "!";
		case otVectorRead:
			if (index >= vectorVariables.size())
				return "bad-vector-variable-read";
			return vectorVariables.get(index).name;
		case otVectorWrite:
			if (index >= vectorVariables.size())
				return "bad-vector-variable-write";
			return vectorVariables.get(index).name + "!";
		case otLabelRead:
			if (index >= labels.size())
				return "bad-label-read";
			return labels.get(index).name + "&";
		case otLabelCall:
			if (index >= labels.size())
				return "bad-label-call";
			return labels.get(index).name + "^";
		case otHardwareRead:
			if (StackBrainOpcode.byID(index).type != OpCodeType.ocHardwareVariable)
				// if (index >= StackBrainOpcode.kNumHardwareVariables())
				return "bad-hardware-variable-read";
			return StackBrainOpcode.byID(index).name;
		case otHardwareWrite:
			if (StackBrainOpcode.byID(index).type != OpCodeType.ocHardwareVariable)
				// if (index >= StackBrainOpcode.kNumHardwareVariables())
				return "bad-hardware-variable-write";
			return StackBrainOpcode.byID(index).name + "!";
		case otHardwareVectorRead:
			if (StackBrainOpcode.byID(index).type != OpCodeType.ocHardwareVector)
				// if (index >= StackBrainOpcode.kNumHardwareVectors())
				return "bad-hardware-vector-read";
			return StackBrainOpcode.byID(index).name;
		case otHardwareVectorWrite:
			if (StackBrainOpcode.byID(index).type != OpCodeType.ocHardwareVector)
				// if (index >= StackBrainOpcode.kNumHardwareVectors())
				return "bad-hardware-vector-write";
			return StackBrainOpcode.byID(index).name + "!";
		default:
			return "bad-opcode-type";
		}
	}

	String getVariableName(int index) {
		if (index >= variables.size())
			throw new GBBadSymbolIndexError();
		return variables.get(index).name;
	}

	String getVectorVariableName(int index) {
		if (index >= vectorVariables.size())
			throw new GBBadSymbolIndexError();
		return vectorVariables.get(index).name;
	}
}

// errors //

class GBSymbolError extends GBBrainError {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String sym;

	public GBSymbolError(String s) {
		sym = s;
	}
}

class GBCStackError extends GBBrainError {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public String toString() {
		return "item left on compile-time stack (unmatched compile-time word)";
	}
}

class GBBadAddressError extends GBBrainError {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	double address;

	public GBBadAddressError(double addr) {
		address = addr;
	}

	@Override
	public String toString() {
		return "invalid address: " + address;
	}
}

class GBUnknownSymbolError extends GBSymbolError {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GBUnknownSymbolError(String s) {
		super(s);
	}

	@Override
	public String toString() {
		return "undefined symbol: " + sym;
	}
}

class GBDuplicateSymbolError extends GBSymbolError {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GBDuplicateSymbolError(String s) {
		super(s);
	}

	@Override
	public String toString() {
		return "duplicate symbol: " + sym;
	}
}

class GBUnresolvedSymbolError extends GBSymbolError {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public GBUnresolvedSymbolError(String s) {
		super(s);
	}

	@Override
	public String toString() {
		return "unresolved forward symbol definition: " + sym;
	}
}
