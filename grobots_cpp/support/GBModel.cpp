// GBModel.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

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

