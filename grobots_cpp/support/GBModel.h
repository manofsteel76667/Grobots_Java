// GBModel.h
// mixin so dependents can tell when an object has changed.
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.


#ifndef GBModel_h
#define GBModel_h

typedef long GBChangeCount;

class GBModel {
private:
	GBChangeCount count;
public:
	GBModel();
	
	void Changed();
	GBChangeCount ChangeCount() const;
};

#endif
