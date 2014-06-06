// GBAboutBox.h
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

#ifndef GBAboutBox_h
#define GBAboutBox_h

#include "GBView.h"

class GBAboutBox : public GBView {
public:
	GBAboutBox();
	~GBAboutBox();

	void Draw();
	
	short PreferredWidth() const;
	short PreferredHeight() const;
	
	const string Name() const;
};

#endif
