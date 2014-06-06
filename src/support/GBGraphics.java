package support;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.Map;

// GBGraphics.cpp
// Grobots (c) 2002-2004 Devon and Warren Schudy
// Distributed under the GNU General Public License.
public class GBGraphics extends Graphics2D{
	// GBGraphics //

	public GBGraphics() {
	}

	public void DrawLine(int x1, int y1, int x2, int y2,
			GBColor color, int thickness) {
		HPEN pen = CreatePen(PS_SOLID, thickness, ColorRef(color));
		HGDIOBJ old = SelectObject(hdc, pen);
		MoveToEx(hdc, x1, y1, 0);
		LineTo(hdc, x2, y2);
		SelectObject(hdc, old);
		DeleteObject(pen);
	}

	public void DrawSolidRect(Rectangle r, GBColor color) {
		HBRUSH brush = CreateSolidBrush(ColorRef(color));
		RECT rect;
		r.ToRect(rect);
		FillRect(hdc, rect, brush);
		DeleteObject(brush);
	}

	public void DrawOpenRect(Rectangle r, GBColor color, int thickness) {
		HBRUSH brush = CreateSolidBrush(ColorRef(color));
		RECT rect;
		r.ToRect(rect);
		FrameRect(hdc, rect, brush);
		DeleteObject(brush);
	}

	public void DrawSolidOval(Rectangle r, GBColor color) {
		HBRUSH brush = CreateSolidBrush(ColorRef(color));
		HPEN pen = CreatePen(PS_SOLID, 0, ColorRef(color));
		HGDIOBJ oldbrush = SelectObject(hdc, brush);
		HGDIOBJ oldpen = SelectObject(hdc, pen);
		Ellipse(hdc, r.left, r.top, r.right, r.bottom);
		SelectObject(hdc, oldpen);
		SelectObject(hdc, oldbrush);
		DeleteObject(brush);
		DeleteObject(pen);
	}

	public void DrawOpenOval(Rectangle r, GBColor color, int thickness) {
		DrawArc(r, 180, 360, color, thickness);
	}

	public void DrawArc(Rectangle r, int startAngle, int length,
							 GBColor color, int thickness) {
		int start = 90 - startAngle - length; //different direction
		float kPiOver180 = 3.14159265f / 180;
		HPEN pen = CreatePen(PS_SOLID, thickness, ColorRef(color));
		HGDIOBJ old = SelectObject(hdc, pen);
		MoveToEx(hdc, r.CenterX() + r.Width() * cos(start * kPiOver180) / 2,
			r.CenterY() + r.Height() * sin(start * kPiOver180) / -2, 0);
		AngleArc(hdc, r.CenterX(), r.CenterY(), r.Height() / 2, start, length);
		SelectObject(hdc, old);
		DeleteObject(pen);
	}

	public void DrawString(String str, int x, int y,
			int size, GBColor color, boolean useBold) {
		HFONT f = CreateFont(- size, 0, 0, 0, useBold ? FW_BOLD : FW_NORMAL,
			0, 0, 0, ANSI_CHARSET, OUT_DEFAULT_PRECIS, CLIP_DEFAULT_PRECIS,
			DEFAULT_QUALITY, DEFAULT_PITCH | FF_SWISS, "Arial");
		HGDIOBJ old = SelectObject(hdc, f);
		SetTextColor(hdc, ColorRef(color));
		TextOut(hdc, x, y, str.c_str(), str.length());
		SelectObject(hdc, old);
		DeleteObject(f);
	}

	public void DrawStringLeft(String str, int x, int y,
			int size, GBColor color, boolean useBold) {
		SetTextAlign(hdc, TA_LEFT | TA_BASELINE);
		DrawString(str, x, y, size, color, useBold);
	}

	public void DrawStringRight(String str, int x, int y,
			int size, GBColor color, boolean useBold) {
		SetTextAlign(hdc, TA_RIGHT | TA_BASELINE);
		DrawString(str, x, y, size, color, useBold);
	}

	public void DrawStringCentered(String str, int x, int y,
			int size, GBColor color, boolean useBold) {
		SetTextAlign(hdc, TA_CENTER | TA_BASELINE);
		DrawString(str, x, y, size, color, useBold);
	}

	public void Blit(BufferedImage src, Rectangle srcRect, Rectangle destRect) {
		if (!BitBlt(hdc, destRect.left, destRect.top, destRect.Width(), destRect.Height(),
				src.Graphics().hdc, srcRect.left, srcRect.top, SRCCOPY))
			DrawSolidRect(destRect, GBColor.black);
	}

	public void DrawStringPair(String str1, String str2,
			int left, int right, int y, int size, GBColor color, boolean useBold) {
		DrawStringLeft(str1, left, y, size, color, useBold);
		DrawStringRight(str2, right, y, size, color, useBold);
	}

	@Override
	public void addRenderingHints(Map<?, ?> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clip(Shape arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void draw(Shape arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawGlyphVector(GlyphVector arg0, float arg1, float arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean drawImage(Image arg0, AffineTransform arg1,
			ImageObserver arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void drawImage(BufferedImage arg0, BufferedImageOp arg1, int arg2,
			int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawRenderableImage(RenderableImage arg0, AffineTransform arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawRenderedImage(RenderedImage arg0, AffineTransform arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawString(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawString(String arg0, float arg1, float arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawString(AttributedCharacterIterator arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawString(AttributedCharacterIterator arg0, float arg1,
			float arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fill(Shape arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Color getBackground() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Composite getComposite() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GraphicsConfiguration getDeviceConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FontRenderContext getFontRenderContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Paint getPaint() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getRenderingHint(Key arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RenderingHints getRenderingHints() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stroke getStroke() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AffineTransform getTransform() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hit(Rectangle arg0, Shape arg1, boolean arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void rotate(double arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void rotate(double arg0, double arg1, double arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void scale(double arg0, double arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBackground(Color arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setComposite(Composite arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPaint(Paint arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRenderingHint(Key arg0, Object arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRenderingHints(Map<?, ?> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setStroke(Stroke arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTransform(AffineTransform arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shear(double arg0, double arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void transform(AffineTransform arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void translate(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void translate(double arg0, double arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearRect(int arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clipRect(int arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void copyArea(int arg0, int arg1, int arg2, int arg3, int arg4,
			int arg5) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Graphics create() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawArc(int arg0, int arg1, int arg2, int arg3, int arg4,
			int arg5) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean drawImage(Image arg0, int arg1, int arg2, ImageObserver arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean drawImage(Image arg0, int arg1, int arg2, Color arg3,
			ImageObserver arg4) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean drawImage(Image arg0, int arg1, int arg2, int arg3,
			int arg4, ImageObserver arg5) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean drawImage(Image arg0, int arg1, int arg2, int arg3,
			int arg4, Color arg5, ImageObserver arg6) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean drawImage(Image arg0, int arg1, int arg2, int arg3,
			int arg4, int arg5, int arg6, int arg7, int arg8, ImageObserver arg9) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean drawImage(Image arg0, int arg1, int arg2, int arg3,
			int arg4, int arg5, int arg6, int arg7, int arg8, Color arg9,
			ImageObserver arg10) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void drawLine(int arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawOval(int arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawPolygon(int[] arg0, int[] arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawPolyline(int[] arg0, int[] arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawRoundRect(int arg0, int arg1, int arg2, int arg3, int arg4,
			int arg5) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fillArc(int arg0, int arg1, int arg2, int arg3, int arg4,
			int arg5) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fillOval(int arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fillPolygon(int[] arg0, int[] arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fillRect(int arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fillRoundRect(int arg0, int arg1, int arg2, int arg3, int arg4,
			int arg5) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Shape getClip() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Rectangle getClipBounds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Color getColor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Font getFont() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FontMetrics getFontMetrics(Font arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setClip(Shape arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setClip(int arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setColor(Color arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFont(Font arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPaintMode() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setXORMode(Color arg0) {
		// TODO Auto-generated method stub
		
	}



}
