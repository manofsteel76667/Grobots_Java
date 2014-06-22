	// GBView.h
	// the abstract GBView class and a few others
	// Grobots (c) 2002-2004 Devon and Warren Schudy
	// Distributed under the GNU General Public License.
package views;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.*;

import brains.GBBadSymbolIndexError;
import exception.GBAbort;
import exception.GBBadArgumentError;
import exception.GBError;
import exception.GBGenericError;
import exception.GBIndexOutOfRangeError;
import exception.GBNilPointerError;
import exception.GBOutOfMemoryError;

public abstract class GBView extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2380156825782030540L;
	public static final  int kEdgeSpace = 2;
		long lastDrawn;

		protected Rectangle CalcExternalRect( Rectangle r) {
			return new Rectangle(r.left + bounds.left,
				r.top + bounds.top,
				r.right + bounds.left,
				r.bottom + bounds.top);
		}

		protected void DrawLine( int x1,  int y1,  int x2,  int y2,
				Color color,  int thickness) {
			graphics.DrawLine(x1, y1, x2, y2, color, thickness);
		}

		protected void DrawSolidRect( Rectangle where, Color color) {
			graphics.DrawSolidRect(CalcExternalRect(where), color);
		}

		protected void DrawOpenRect( Rectangle where, Color color,  int thickness) {
			graphics.DrawOpenRect(CalcExternalRect(where), color, thickness);
		}

		protected void DrawBox( Rectangle box,  boolean selected) {
			Rectangle r = CalcExternalRect(box);
			if ( selected )
				graphics.DrawSolidRect(r, Color.black);
			else {
				graphics.DrawSolidRect(r, Color.white);
				graphics.DrawOpenRect(r, Color.black);
			}
		}

		protected void DrawBackground( Color color) {
			graphics.DrawSolidRect(bounds, color);
		}

		protected void DrawSolidOval( Rectangle where, Color color) {
			graphics.DrawSolidOval(CalcExternalRect(where), color);
		}

		protected void DrawOpenOval( Rectangle where, Color color,  int thickness) {
			graphics.DrawOpenOval(CalcExternalRect(where), color, thickness);
		}

		protected void DrawArc( Rectangle where,  int startAngle,  int length,
				Color color,  int thickness) {
			graphics.DrawArc(CalcExternalRect(where), startAngle, length, color, thickness);
		}

		protected void DrawStringLeft( String str,  int x,  int y,
				 int size, Color color,  boolean useBold) {
			graphics.DrawStringLeft(str, x + bounds.left, y + bounds.top,
				size, color, useBold);
		}

		protected void DrawStringRight( String str,  int x,  int y,
				 int size, Color color,  boolean useBold) {
			graphics.DrawStringRight(str, x + bounds.left, y + bounds.top,
				size, color, useBold);
		}

		protected void DrawStringCentered( String str,  int x,  int y,
				 int size, Color color,  boolean useBold) {
			graphics.DrawStringCentered(str, x + bounds.left, y + bounds.top,
				size, color, useBold);
		}

		protected void DrawStringPair( String str1, String str2,
				 int left,  int right,  int y,  int size, Color color,  boolean useBold) {
			DrawStringLeft(str1, left, y, size, color, useBold);
			DrawStringRight(str2, right, y, size, color, useBold);
		}

		protected void DrawLongLeft( int n,  int x,  int y,
				 int size, Color color,  boolean useBold) {
			DrawStringLeft(n.toString(), x, y, size, color, useBold);
		}

		protected void DrawLongRight( int n,  int x,  int y,
				 int size, Color color,  boolean useBold) {
			DrawStringRight(n.toString(), x, y, size, color, useBold);
		}

		protected void DrawStringLongPair( String str,  int n,
				 int left,  int right,  int y,  int size, Color color,  boolean useBold) {
			DrawStringLeft(str, left, y, size, color, useBold);
			DrawLongRight(n, right, y, size, color, useBold);
		}

		public GBView(){
			lastDrawn=-1; 
		}

		public  boolean Resizable() {
			return false;
		}


		 public void Draw() {}

		public  boolean NeedsRedraw( boolean running) {
			long interval = RedrawInterval();
			if ( interval < 0 )
				return InstantChanges() || ! running && DelayedChanges();
			return InstantChanges() ||
				(! running || System.currentTimeMillis() >= lastDrawn + interval)
					&& DelayedChanges();
		}

		public long RedrawInterval() {
			return -1; // only draw delayed changes when paused
		}

		public  boolean InstantChanges() {
			return false;
		}

		 public  boolean DelayedChanges() {
			return false;
		}

		 public void DoDraw() {
			try {
				Draw();
			} catch ( GBError err ) {
				try {
				GBError.NonfatalError("Error drawing: " + err.toString());}
			 catch ( GBAbort ab) {System.exit(1);}}
			lastDrawn = System.currentTimeMillis();
		}

			public String Name() {
			return "a view";
		}

	};
