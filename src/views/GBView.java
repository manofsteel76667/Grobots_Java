package views;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.*;

import brains.GBBadSymbolIndexError;
import exception.GBAbort;
import exception.GBBadArgumentError;
import exception.GBGenericError;
import exception.GBNilPointerError;
import exception.GBOutOfMemoryError;

public class GBView extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2380156825782030540L;
	// GBView.h
	// the abstract GBView class and a few others
	// Grobots (c) 2002-2004 Devon and Warren Schudy
	// Distributed under the GNU General Public License.

	public static final  int kEdgeSpace = 2;

	// A View is a visible interface component.
		Rectangle bounds;
		long lastDrawn;
	//protected:
		GBRect CalcExternalRect( GBRect src) ;
	// portable graphics: lines
		void DrawLine( int x1,  int y1,  int x2,  int y2,
			public static final GBColor & color,  int thickness = 1) ;
	// rectangles
		void DrawSolidRect( GBRect & where, GBColor & color) ;
		void DrawOpenRect( GBRect & where, GBColor & color,  int thickness = 1) ;
		void DrawBox( GBRect & box,  boolean selected = false) ;
		void DrawBackground( GBColor & color = GBColor::lightGray) ;
	// ovals
		void DrawSolidOval( GBRect & where, GBColor & color) ;
		void DrawOpenOval( GBRect & where, GBColor & color,  int thickness = 1) ;
		void DrawArc( GBRect & where,  int startAngle,  int length,
			public static final GBColor & color,  int thickness = 1) ;
	// strings
		void DrawStringLeft( String & str,  int x,  int y,
			 int size, GBColor & color = GBColor::black,  boolean bold = false) ;
		void DrawStringRight( String & str,  int x,  int y,
			 int size, GBColor & color = GBColor::black,  boolean bold = false) ;
		void DrawStringCentered( String & str,  int x,  int y,
			 int size, GBColor & color = GBColor::black,  boolean bold = false) ;
		void DrawStringPair( String & str1, String & str2,
			 int left,  int right,  int y,  int size, GBColor & color = GBColor::black,  boolean bold = false) ;
	// longs
		void DrawLongLeft( int n,  int x,  int y,
			 int size, GBColor & color = GBColor::black,  boolean bold = false) ;
		void DrawLongRight( int n,  int x,  int y,
			 int size, GBColor & color = GBColor::black,  boolean bold = false) ;
		void DrawStringLongPair( String & str1,  int n,
			 int left,  int right,  int y,  int size, GBColor & color = GBColor::black,  boolean bold = false) ;
	// blitter
		void Blit( GBBitmap & src, GBRect & srcRect, GBRect & destRect) ;
		void BlitAll( GBBitmap & src, GBRect & srcRect) ;
	//public:
		GBView();
		virtual ~GBView();
	// sizing
		virtual  boolean Resizable() ;
		virtual  int MinimumWidth() ;
		virtual  int MinimumHeight() ;
		virtual  int MaximumWidth() ;
		virtual  int MaximumHeight() ;
		virtual  int PreferredWidth() ;
		virtual  int PreferredHeight() ;
		 int Width() ;
		 int Height() ;
		 int CenterX() ;
		 int CenterY() ;
		virtual void SetSize( int width,  int height);
		virtual void SetBounds( GBRect & newbounds);
	// drawing
		GBGraphics & Graphics() ;
		void SetGraphics(GBGraphics * g);
		virtual void Draw();
		 boolean NeedsRedraw( boolean running) ;
		 boolean NeedsResize() ;
		virtual GBMilliseconds RedrawInterval() ;
		virtual  boolean InstantChanges() ;
		virtual  boolean DelayedChanges() ;
	// for owner to call
		void DoDraw();
		void DoClick( int x,  int y, int clicksBefore);
		void DoDrag( int x,  int y);
		void DoUnclick( int x,  int y, int clicksBefore);
	// to override
		virtual  boolean GetFrontClicks() ; // accept clicks that brought window to front?
		virtual void AcceptClick( int x,  int y, int clicksBefore) throws GBAbort, GBNilPointerError, GBBadArgumentError, GBBadSymbolIndexError, GBGenericError, GBOutOfMemoryError;
		virtual void AcceptDrag( int x,  int y) throws GBAbort, GBNilPointerError, GBBadArgumentError, GBBadSymbolIndexError, GBGenericError, GBOutOfMemoryError;
		virtual void AcceptUnclick( int x,  int y, int clicksBefore) throws GBAbort, GBNilPointerError, GBBadArgumentError, GBBadSymbolIndexError, GBGenericError, GBOutOfMemoryError;
		virtual void AcceptKeystroke( char what);
	// other
		virtual String Name() ;
		GBRect GBView::CalcExternalRect( GBRect & r) {
			return GBRect(r.left + bounds.left,
				r.top + bounds.top,
				r.right + bounds.left,
				r.bottom + bounds.top);
		}

		void GBView::DrawLine( int x1,  int y1,  int x2,  int y2,
				public static final GBColor & color,  int thickness) {
			graphics.DrawLine(x1, y1, x2, y2, color, thickness);
		}

		void GBView::DrawSolidRect( GBRect & where, GBColor & color) {
			graphics.DrawSolidRect(CalcExternalRect(where), color);
		}

		void GBView::DrawOpenRect( GBRect & where, GBColor & color,  int thickness) {
			graphics.DrawOpenRect(CalcExternalRect(where), color, thickness);
		}

		void GBView::DrawBox( GBRect & box,  boolean selected) {
			GBRect r = CalcExternalRect(box);
			if ( selected )
				graphics.DrawSolidRect(r, GBColor::black);
			else {
				graphics.DrawSolidRect(r, GBColor::white);
				graphics.DrawOpenRect(r, GBColor::black);
			}
		}

		void GBView::DrawBackground( GBColor & color) {
			graphics.DrawSolidRect(bounds, color);
		}

		void GBView::DrawSolidOval( GBRect & where, GBColor & color) {
			graphics.DrawSolidOval(CalcExternalRect(where), color);
		}

		void GBView::DrawOpenOval( GBRect & where, GBColor & color,  int thickness) {
			graphics.DrawOpenOval(CalcExternalRect(where), color, thickness);
		}

		void GBView::DrawArc( GBRect & where,  int startAngle,  int length,
				public static final GBColor & color,  int thickness) {
			graphics.DrawArc(CalcExternalRect(where), startAngle, length, color, thickness);
		}

		void GBView::DrawStringLeft( String & str,  int x,  int y,
				 int size, GBColor & color,  boolean useBold) {
			graphics.DrawStringLeft(str, x + bounds.left, y + bounds.top,
				size, color, useBold);
		}

		void GBView::DrawStringRight( String & str,  int x,  int y,
				 int size, GBColor & color,  boolean useBold) {
			graphics.DrawStringRight(str, x + bounds.left, y + bounds.top,
				size, color, useBold);
		}

		void GBView::DrawStringCentered( String & str,  int x,  int y,
				 int size, GBColor & color,  boolean useBold) {
			graphics.DrawStringCentered(str, x + bounds.left, y + bounds.top,
				size, color, useBold);
		}

		void GBView::DrawStringPair( String & str1, String & str2,
				 int left,  int right,  int y,  int size, GBColor & color,  boolean useBold) {
			DrawStringLeft(str1, left, y, size, color, useBold);
			DrawStringRight(str2, right, y, size, color, useBold);
		}

		void GBView::DrawLongLeft( int n,  int x,  int y,
				 int size, GBColor & color,  boolean useBold) {
			DrawStringLeft(ToString(n), x, y, size, color, useBold);
		}

		void GBView::DrawLongRight( int n,  int x,  int y,
				 int size, GBColor & color,  boolean useBold) {
			DrawStringRight(ToString(n), x, y, size, color, useBold);
		}

		void GBView::DrawStringLongPair( String & str,  int n,
				 int left,  int right,  int y,  int size, GBColor & color,  boolean useBold) {
			DrawStringLeft(str, left, y, size, color, useBold);
			DrawLongRight(n, right, y, size, color, useBold);
		}

		void GBView::Blit( GBBitmap & src, GBRect & srcRect, GBRect & destRect) {
			graphics.Blit(src, srcRect, CalcExternalRect(destRect));
		}

		void GBView::BlitAll( GBBitmap & src, GBRect & srcRect) {
			graphics.Blit(src, srcRect, bounds);
		}


		GBView::GBView()
			: lastDrawn(-1), graphics(null)
		{}

		GBView::~GBView() {}

		 boolean GBView::Resizable() {
			return false;
		}

		 int GBView::MinimumWidth() {
			return Resizable() ? 50 : PreferredWidth();
		}

		 int GBView::MinimumHeight() {
			return Resizable() ? 50 : PreferredHeight();
		}

		 int GBView::MaximumWidth() {
			return Resizable() ? 10000 : PreferredWidth();
		}

		 int GBView::MaximumHeight() {
			return Resizable() ? 10000 : PreferredHeight();
		}

		 int GBView::PreferredWidth() {
			return 300;
		}

		 int GBView::PreferredHeight() {
			return 300;
		}

		 int GBView::Width() {
			return bounds.Width();
		}

		 int GBView::Height() {
			return bounds.Height();
		}

		 int GBView::CenterX() {
			return bounds.CenterX();
		}

		 int GBView::CenterY() {
			return bounds.CenterY();
		}

		void GBView::SetSize( int width,  int height) {
			bounds.right = bounds.left + width;
			bounds.bottom = bounds.top + height;
		}

		void GBView::SetBounds( GBRect & newbounds) {
			bounds = newbounds;
		}

		GBGraphics & GBView::Graphics() {
			if ( ! graphics ) throw new GBnullPointerError();
			return *graphics;
		}

		void GBView::SetGraphics(GBGraphics * g) {
			if ( ! g ) throw new GBnullPointerError();
			graphics = g;
		}

		void GBView::Draw() {}

		 boolean GBView::NeedsRedraw( boolean running) {
			GBMilliseconds interval = RedrawInterval();
			if ( interval < 0 )
				return InstantChanges() || ! running && DelayedChanges();
			return InstantChanges() ||
				(! running || Milliseconds() >= lastDrawn + interval)
					&& DelayedChanges();
		}

		 boolean GBView::NeedsResize() {
			return Width() < MinimumWidth() || Height() < MinimumHeight()
				|| Width() > MaximumWidth() || Height() > MaximumHeight();
		}

		GBMilliseconds GBView::RedrawInterval() {
			return -1; // only draw delayed changes when paused
		}

		 boolean GBView::InstantChanges() {
			return false;
		}

		 boolean GBView::DelayedChanges() {
			return false;
		}

		void GBView::DoDraw() {
			if ( ! graphics ) throw new GBnullPointerError();
			try {
				Draw();
			} catch ( GBError & err ) {
				NonfatalError("Error drawing: " + err.ToString());
			} catch ( GBAbort & ) {}
			lastDrawn = Milliseconds();
		}

		void GBView::DoClick( int x,  int y, int clicksBefore) {
			AcceptClick(x - bounds.left, y - bounds.top, clicksBefore);
		}

		void GBView::DoDrag( int x,  int y) {
			AcceptDrag(x - bounds.left, y - bounds.top);
		}

		void GBView::DoUnclick( int x,  int y, int clicksBefore) {
			AcceptUnclick(x - bounds.left, y - bounds.top, clicksBefore);
		}


		 boolean GBView::GetFrontClicks() {
			return false;
		}

		void GBView::AcceptClick( int /*x*/,  int /*y*/, int /*clicksBefore*/) {}

		void GBView::AcceptDrag( int /*x*/,  int /*y*/) {}

		void GBView::AcceptUnclick( int /*x*/,  int /*y*/, int /*clicksBefore*/) {}

		void GBView::AcceptKeystroke( char) {}


		public static final String GBView::Name() {
			return "a view";
		}

		GBCursor GBView::Cursor() {
			return cuArrow;
		}
	};


	// WrapperView must forward all messages to content
	class GBWrapperView extends GBView {
	//protected:
		GBView * content;
	//public:
		explicit GBWrapperView(GBView * what);
		~GBWrapperView();
	// sizing
		 boolean Resizable() ;
		 int MinimumWidth() ;
		 int MinimumHeight() ;
		 int MaximumWidth() ;
		 int MaximumHeight() ;
		 int PreferredWidth() ;
		 int PreferredHeight() ;
		void SetSize( int width,  int height);
		void SetBounds( GBRect & newbounds);
	// drawing
		void Draw();
		GBMilliseconds RedrawInterval() ;
		 boolean InstantChanges() ;
		 boolean DelayedChanges() ;
	// event handling
		 boolean GetFrontClicks() ;
		void AcceptClick( int x,  int y, int clicksBefore);
		void AcceptDrag( int x,  int y);
		void AcceptUnclick( int x,  int y, int clicksBefore);
		void AcceptKeystroke( char what);
	// other
		public static final String Name() ;
		GBCursor Cursor() ;
		GBWrapperView::GBWrapperView(GBView * what)
		: content(what)
	{}

	GBWrapperView::~GBWrapperView() {
		delete content;
	}

	 boolean GBWrapperView::Resizable() {
		return content.Resizable();
	}

	 int GBWrapperView::MinimumWidth() {
		return content.MinimumWidth();
	}

	 int GBWrapperView::MinimumHeight() {
		return content.MinimumHeight();
	}

	 int GBWrapperView::MaximumWidth() {
		return content.MaximumWidth();
	}

	 int GBWrapperView::MaximumHeight() {
		return content.MaximumHeight();
	}

	 int GBWrapperView::PreferredWidth() {
		return content.PreferredWidth();
	}

	 int GBWrapperView::PreferredHeight() {
		return content.PreferredHeight();
	}

	void GBWrapperView::SetSize( int width,  int height) {
		content.SetSize(width, height);
		GBView::SetSize(content.Width(), content.Height());
	}

	void GBWrapperView::SetBounds( GBRect & newbounds) {
		content.SetBounds(newbounds);
		GBView::SetBounds(newbounds);
	}

	void GBWrapperView::Draw() {
		content.Draw();
	}

	GBMilliseconds GBWrapperView::RedrawInterval() {
		return content.RedrawInterval();
	}

	 boolean GBWrapperView::InstantChanges() {
		return content.InstantChanges();
	}

	 boolean GBWrapperView::DelayedChanges() {
		return content.DelayedChanges();
	}

	 boolean GBWrapperView::GetFrontClicks() {
		return content.GetFrontClicks();
	}

	void GBWrapperView::AcceptClick( int x,  int y, int clicksBefore) {
		content.AcceptClick(x, y, clicksBefore);
	}

	void GBWrapperView::AcceptDrag( int x,  int y) {
		content.AcceptDrag(x, y);
	}

	void GBWrapperView::AcceptUnclick( int x,  int y, int clicksBefore) {
		content.AcceptUnclick(x, y, clicksBefore);
	}

	void GBWrapperView::AcceptKeystroke( char what) {
		content.AcceptKeystroke(what);
	}

	public static final String GBWrapperView::Name() {
		return content.Name();
	}

	GBCursor GBWrapperView::Cursor() {
		return content.Cursor();
	}

	};


	class GBDoubleBufferedView extends GBWrapperView {
		GBBitmap * offscreen;
		 boolean draw, flip;
	//public:
		explicit GBDoubleBufferedView(GBView * what);
		~GBDoubleBufferedView();
	// sizing
		void SetSize( int width,  int height);
		void SetBounds( GBRect & newbounds);
	// event handling
		void AcceptKeystroke( char what);
	// drawing
		void Draw();
	};


	#endif
	// GBView.cpp
	// Grobots (c) 2002-2004 Devon and Warren Schudy
	// Distributed under the GNU General Public License.

	GBDoubleBufferedView::GBDoubleBufferedView(GBView * what)
		: GBWrapperView(what),
		offscreen(null),
		draw(true), flip(true)
	{}

	GBDoubleBufferedView::~GBDoubleBufferedView() {
		delete offscreen;
	}

	void GBDoubleBufferedView::SetSize( int width,  int height) {
		GBWrapperView::SetSize(width, height);
		if ( offscreen ) {
			delete offscreen;
			offscreen = null;
		}
	}

	void GBDoubleBufferedView::SetBounds( GBRect & newbounds) {
		GBWrapperView::SetBounds(newbounds);
		if ( offscreen ) {
			delete offscreen;
			offscreen = null;
		}
	}

	void GBDoubleBufferedView::AcceptKeystroke( char what) {
		if ( what == '!' ) draw = ! draw;
		else if ( what == '@' ) flip = ! flip;
		else GBWrapperView::AcceptKeystroke(what);
	}

	void GBDoubleBufferedView::Draw() {
		 boolean newWorld = false;
		if ( ! offscreen ) {
			offscreen = new GBBitmap(Width(), Height(), Graphics());
			content.SetGraphics(&(offscreen.Graphics()));
			newWorld = true;
		}
	// draw offscreen
		if ( draw && (NeedsRedraw(false) || newWorld) ) {
			offscreen.StartDrawing();
			content.Draw();
			offscreen.StopDrawing();
		}
	// draw onscreen
		if ( flip )
			BlitAll(*offscreen, offscreen.Bounds());
	}


}
