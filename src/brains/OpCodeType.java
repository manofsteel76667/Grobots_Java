/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/
package brains;

import java.util.HashMap;
import java.util.Map;

public enum OpCodeType {
	ocPrimitive, ocHardwareVariable, ocHardwareVector, ocCompileWord;
	public final int ID;

	OpCodeType() {
		ID = this.ordinal();
	}

	static final Map<Integer, OpCodeType> idLookup = new HashMap<Integer, OpCodeType>();
	static {
		for (OpCodeType typ : OpCodeType.values())
			idLookup.put(typ.ID, typ);
	}

	public final static OpCodeType byID(int _ID) {
		return idLookup.get(_ID);
	}
}