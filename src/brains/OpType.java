/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package brains;

import java.util.HashMap;
import java.util.Map;

public enum OpType {
	otPrimitive, otConstantRead, otVariableRead, otVariableWrite, otVectorRead, otVectorWrite, otLabelRead, otLabelCall, otHardwareRead, otHardwareWrite, otHardwareVectorRead, otHardwareVectorWrite;

	OpType() {
		ID = this.ordinal();
	}

	static Map<Integer, OpType> idLookup = new HashMap<Integer, OpType>();
	static {
		for (OpType typ : OpType.values())
			idLookup.put(typ.ID, typ);
	}

	public static final int kNumOpCodes() {
		return OpType.values().length;
	}

	public final int ID;

	public static OpType byID(int _ID) {
		if (idLookup.containsKey(_ID))
			return idLookup.get(_ID);
		else
			return null;
	}
}