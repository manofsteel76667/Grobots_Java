/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/


#include "GBModel.h"


GBModel::GBModel()
	: count(0)
{}

void GBModel::Changed() {
	++ count;
}

GBChangeCount GBModel::ChangeCount() const {
	return count;
}

