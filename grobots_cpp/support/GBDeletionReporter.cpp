/*******************************************************************************
 * Copyright (c) 2002-2013 (c) Devon and Warren Schudy
 * Copyright (c) 2014  Devon and Warren Schudy, Mike Anderson
 *******************************************************************************/


#include "GBDeletionReporter.h"
#include "GBErrors.h"
#include "GBPlatform.h"
#include <algorithm>
using std::find;


GBDeletionReporter::GBDeletionReporter()
	: listeners(nil)
{}

GBDeletionReporter::~GBDeletionReporter() {
	if ( listeners ) {
		for ( std::list<GBDeletionListener *>::iterator iter = listeners->begin();
				iter != listeners->end(); ++ iter )
			(*iter)->ReportDeletion(this);
		delete listeners;
	}
}

void GBDeletionReporter::AddDeletionListener(GBDeletionListener * newDL) {
	int n = listeners ? listeners->size() : 0;
	if ( ! listeners )
		listeners = new std::list<GBDeletionListener *>();
	if ( find(listeners->begin(), listeners->end(), newDL) != listeners->end() )
		throw GBGenericError("Tried to add an existing deletion listener");
	listeners->push_back(newDL);
	if ( listeners->size() != n + 1 ) throw GBGenericError("inconsistent listener list size");
}

void GBDeletionReporter::RemoveDeletionListener(GBDeletionListener * oldDL) {
	if ( ! listeners )
		throw GBGenericError("Tried to remove a deletion listener when there aren't any.");
	if ( find(listeners->begin(), listeners->end(), oldDL) == listeners->end() )
		throw GBGenericError("Tried to remove a nonexistent deletion listener");
	listeners->remove(oldDL);
	if ( listeners->empty() ) {
		delete listeners;
		listeners = nil;
	}
}


GBDeletionListener::GBDeletionListener() {}
GBDeletionListener::~GBDeletionListener() {}

void GBDeletionListener::ReportDeletion(const GBDeletionReporter *) {}

