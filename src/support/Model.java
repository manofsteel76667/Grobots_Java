// GBModel.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.

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
