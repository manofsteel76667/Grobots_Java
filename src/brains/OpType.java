package brains;

import java.util.*;

public enum OpType {
	otPrimitive,
	otConstantRead,
	otVariableRead,
	otVariableWrite,
	otVectorRead,
	otVectorWrite,
	otLabelRead,
	otLabelCall,
	otHardwareRead,
	otHardwareWrite,
	otHardwareVectorRead,
	otHardwareVectorWrite;

	OpType(){
		ID = this.ordinal();
	}
	static Map<Integer, OpType> idLookup = new HashMap<Integer, OpType>();
	static{
		for(OpType typ : OpType.values())
			idLookup.put(typ.ID, typ);
	}
	public static final int kNumOpCodes() {
		return OpType.values().length;
	}
	
	public final int ID;
	public static OpType byID(int _ID){
		if (idLookup.containsKey(_ID))
			return idLookup.get(_ID);
		else
			return null;
	}
}