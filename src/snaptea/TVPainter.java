package snaptea;
import org.teavm.jso.canvas.*;
import org.teavm.jso.core.*;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import snap.gfx.*;

/**
 * A snap Painter for rendering to a TeaVM HTMLCanvasElement.
 */
public class TVPainter extends PainterImpl {
    
    // The canvas
    HTMLCanvasElement _canvas;
    
    // The RenderContext2D
    CanvasRenderingContext2D _cntx;

/**
 * Creates a new painter for given canvas.
 */
public TVPainter(HTMLCanvasElement aCnvs)
{
    // Set canvas and context
    _canvas = aCnvs;
    _cntx = (CanvasRenderingContext2D)_canvas.getContext("2d");
    
    // Clip to canvas bounds
    clip(new Rect(0,0,_canvas.getWidth(),_canvas.getHeight()));
    
    // If hidpi, scale default transform
    if(TVWindow.scale>1) _cntx.transform(TVWindow.scale,0,0,TVWindow.scale,0,0);
}

/**
 * Sets the paint in painter.
 */
public void setPaint(Paint aPaint)
{
    super.setPaint(aPaint);
    if(aPaint instanceof Color) { String cstr = TV.get((Color)aPaint);
        _cntx.setFillStyle(cstr);
        _cntx.setStrokeStyle(cstr);
    }
}

/** Sets the current stroke. */
public void setStroke(Stroke aStroke)
{
    // Do normal version
    super.setStroke(aStroke);
    
    // Set LineWidth
    _cntx.setLineWidth(aStroke!=null? aStroke.getWidth() : 1);
    
    // Set DashArray, DashOffset
    if(aStroke.getDashArray()==null) _cntx.setLineDash(_empty);
    else {
        double da[] = aStroke.getDashArray();
        JSArray jsa = JSArray.create(); for(double d : da) jsa.push(JSNumber.valueOf(d));
        _cntx.setLineDash(jsa);
    }
    _cntx.setLineDashOffset((float)aStroke.getDashOffset());
    
    // Set cap
    switch(aStroke.getCap()) {
        case Round: _cntx.setLineCap("round"); break;
        case Butt: _cntx.setLineCap("butt"); break;
        case Square: _cntx.setLineCap("square"); break;
    }
    
    // Set join
    switch(aStroke.getJoin()) {
        case Miter: _cntx.setLineJoin("miter"); _cntx.setMiterLimit(aStroke.getMiterLimit()); break;
        case Round: _cntx.setLineJoin("round"); break;
        case Bevel: _cntx.setLineJoin("bevel"); break;
    }
}

// Empty Dash array
JSArray _empty = JSArray.create();

/**
 * Sets the opacity.
 */
public void setOpacity(double aValue)
{
    super.setOpacity(aValue);
    _cntx.setGlobalAlpha(aValue);
}

/**
 * Sets the font in painter.
 */
public void setFont(Font aFont)
{
    super.setFont(aFont);
    _cntx.setFont(TV.get(aFont));
}

/**
 * Sets the current transform.
 */
public void setTransform(Transform aTrans)
{
    super.setTransform(aTrans);
    double m[] = aTrans.getMatrix();
    
    // Set transform with CJWindow.scale (in case hidpi)
    _cntx.setTransform(m[0]*TVWindow.scale, m[1], m[2], m[3]*TVWindow.scale, m[4], m[5]);
}

/**
 * Transform painter.
 */
public void transform(Transform aTrans)
{
    super.transform(aTrans);
    double m[] = aTrans.getMatrix();
    _cntx.transform(m[0], m[1], m[2], m[3], m[4], m[5]);
}

/**
 * Draws a shape in painter.
 */
public void draw(Shape aShape)
{
    if(getPaint() instanceof GradientPaint) { GradientPaint gpnt = (GradientPaint)getPaint();
        GradientPaint gpnt2 = gpnt.copyFor(aShape.getBounds());
        CanvasGradient cg = TV.get(gpnt2, _cntx);
        _cntx.setStrokeStyle(cg);
    }
    
    setShape(aShape);
    _cntx.stroke();
}

/**
 * Draws a shape in painter.
 */
public void fill(Shape aShape)
{
    if(getPaint() instanceof GradientPaint) { GradientPaint gpnt = (GradientPaint)getPaint();
        GradientPaint gpnt2 = gpnt.copyFor(aShape.getBounds());
        CanvasGradient cg = TV.get(gpnt2, _cntx);
        _cntx.setFillStyle(cg);
    }
    
    setShape(aShape);
    _cntx.fill();
}

/**
 * Clips a shape in painter.
 */
public void clip(Shape aShape)
{
    setShape(aShape);
    _cntx.clip();
}

/**
 * Sets a shape.
 */
public void setShape(Shape aShape)
{
    double pnts[] = new double[6];
    PathIter piter = aShape.getPathIter(null);
    _cntx.beginPath();
    while(piter.hasNext()) {
        switch(piter.getNext(pnts)) {
            case MoveTo: _cntx.moveTo(pnts[0], pnts[1]); break;
            case LineTo: _cntx.lineTo(pnts[0], pnts[1]); break;
            case CubicTo: _cntx.bezierCurveTo(pnts[0], pnts[1], pnts[2], pnts[3], pnts[4], pnts[5]); break;
            case Close: _cntx.closePath(); break;
        }
    }
}

/**
 * Draw image with transform.
 */
public void drawImage(Image anImg, Transform xform)
{
    CanvasImageSource img = (CanvasImageSource)anImg.getNative();
    save();
    transform(xform);
    _cntx.drawImage(img, 0, 0);
    restore();
}

/**
 * Draw image in rect.
 */
public void drawImage(Image anImg, double sx, double sy, double sw, double sh, double dx,double dy,double dw,double dh)
{
    // Correct source width/height for image dpi
    double isw = anImg.getDPIX()/72, ish = anImg.getDPIY()/72;
    if(isw!=1) { sx *= isw; sw *= isw; }
    if(ish!=1) { sy *= ish; sh *= ish; }
    
    // Get points for corner as ints and draw image
    CanvasImageSource img = anImg instanceof TVImage? (CanvasImageSource)anImg.getNative() : null;
    _cntx.drawImage(img, sx, sy, sw, sh, dx, dy, dw, dh);
}

/**
 * Draw string at location.
 */
public void drawString(String aStr, double aX, double aY, double cs)
{
    // Handle no char spacing
    if(cs==0) _cntx.fillText(aStr, aX, aY);
        
    // Handle char spacing
    else {
        _cntx.fillText(aStr, aX, aY);
    }
}

/**
 * Clears a rect.
 */
public void clearRect(double aX, double aY, double aW, double aH)  { _cntx.clearRect(aX,aY,aW,aH); }

/**
 * Standard clone implementation.
 */
public void save()  { super.save(); _cntx.save(); }

/**
 * Disposes of the painter.
 */
public void restore()  { super.restore(); _cntx.restore(); }

/**
 * Sets image rendering quality.
 */
public void setImageQuality(double aValue)
{
    if(snap.util.MathUtils.equals(aValue,getImageQuality())) return;
    super.setImageQuality(aValue);
    //if(aValue>.67) _cntx.setImageSmoothingQuality("high");
    //else if(aValue>.33) _cntx.setImageSmoothingQuality("medium");
    //else _cntx.setImageSmoothingQuality("low");
    //_cntx.setImageSmoothingEnabled(aValue>.33);
}

/**
 * Sets the composite mode.
 */
public void setComposite(Composite aComp)
{
    super.setComposite(aComp);
    switch(aComp) {
        case SRC_OVER: _cntx.setGlobalCompositeOperation("source-over"); break;
        case SRC_IN: _cntx.setGlobalCompositeOperation("source-in"); break;
        case DST_IN: _cntx.setGlobalCompositeOperation("destination-in"); break;
    }
}

}