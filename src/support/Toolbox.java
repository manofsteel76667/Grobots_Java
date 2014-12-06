package support;

import java.awt.Desktop;
import java.awt.Font;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class Toolbox {
	public static void openWebpage(URI uri) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop()
				: null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(uri);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void openWebpage(URL url) {
		try {
			openWebpage(url.toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Font used in game drawings
	 * 
	 * @param size
	 * @param bold
	 * @return
	 */
	public static Font gameFont(int size, boolean bold) {
		return new Font("Serif", bold ? Font.BOLD : Font.PLAIN, size);
	}
}
