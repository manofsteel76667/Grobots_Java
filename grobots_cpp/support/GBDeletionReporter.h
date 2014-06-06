// GBDeletionReporter.h
// pair of mixins for notifying dependents when an object dies.
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef GBDeletionReporter_h
#define GBDeletionReporter_h

#include <list>

class GBDeletionListener;

class GBDeletionReporter {
	std::list<GBDeletionListener *> * listeners;
public:
	GBDeletionReporter();
	~GBDeletionReporter();
	void AddDeletionListener(GBDeletionListener * newDL);
	void RemoveDeletionListener(GBDeletionListener * oldDL);
};

class GBDeletionListener {
public:
	GBDeletionListener();
	~GBDeletionListener();
	virtual void ReportDeletion(const GBDeletionReporter * deleted);
};

#endif
