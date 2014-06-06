// GBDeletionReporter.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

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

