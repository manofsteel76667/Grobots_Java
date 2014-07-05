/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/


package support;

public class Model {
	public long count;

	public Model() {
		count = 0;
	}

	public void Changed() {
		++count;
	}

}
