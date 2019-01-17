package snaptea;
import org.teavm.jso.dom.html.*;
import snap.gfx.*;
import snap.view.*;

/**
 * A class to manage RootView canvas.
 */
public class TVRootView {

    // The RootView
    RootView              _rview;
    
    // The HTMLCanvas
    HTMLCanvasElement     _canvas;
    
    // Painter
    Painter               _pntr;
    
/**
 * Sets the view.
 */
public void setView(RootView aView)
{
    // Set RootView
    _rview = aView;
    
    // Create canvas and configure to totally fill window element (minus padding insets)
    _canvas = (HTMLCanvasElement)HTMLDocument.current().createElement("canvas");
    _canvas.getStyle().setProperty("width", "100%");
    _canvas.getStyle().setProperty("height", "100%");
    _canvas.getStyle().setProperty("box-sizing", "border-box");

    // Add RootView listener to propagate size changes to canvas
    _rview.addPropChangeListener(pc -> rootViewSizeChange(), View.Width_Prop, View.Height_Prop);
    rootViewSizeChange();
    
    // Have to do this so TouchEvent.preventDefault doesn't complain and iOS doesn't scroll doc
    _canvas.getStyle().setProperty("touch-action", "none");
    _canvas.setAttribute("touch-action", "none");
    _canvas.addEventListener("touchstart", e -> e.preventDefault());
    _canvas.addEventListener("touchmove", e -> e.preventDefault());
    _canvas.addEventListener("touchend", e -> e.preventDefault());
    
    // Create painer
    _pntr = new TVPainter(_canvas);
    
    // Add listener to update cursor
    _rview.addPropChangeListener(pc -> rootViewCurrentCursorChanged(), RootView.CurrentCursor_Prop);
    
    // Register for drop events
    //_canvas.setAttribute("draggable", "true");
    //cjdom.EventListener dragLsnr = e -> handleDragEvent((DragEvent)e);
    //_canvas.addEventListener("dragenter", dragLsnr); _canvas.addEventListener("dragover", dragLsnr);
    //_canvas.addEventListener("drop", dragLsnr); _canvas.addEventListener("dragexit", dragLsnr);
    // Register for drag start event
    //_canvas.addEventListener("dragstart", e -> handleDragGesture((DragEvent)e));
    //_canvas.addEventListener("dragend", e -> handleDragEnd((DragEvent)e));
}

/**
 * Sets the cursor.
 */
void rootViewCurrentCursorChanged()
{
    Cursor aCursor = _rview.getCurrentCursor();
    String cstr = "default";
    if(aCursor==Cursor.DEFAULT) cstr = "default";
    if(aCursor==Cursor.CROSSHAIR) cstr = "crosshair";
    if(aCursor==Cursor.HAND) cstr = "pointer";
    if(aCursor==Cursor.MOVE) cstr = "move";
    if(aCursor==Cursor.TEXT) cstr = "text";
    if(aCursor==Cursor.NONE) cstr = "none";
    if(aCursor==Cursor.N_RESIZE) cstr = "n-resize";
    if(aCursor==Cursor.S_RESIZE) cstr = "s-resize";
    if(aCursor==Cursor.E_RESIZE) cstr = "e-resize";
    if(aCursor==Cursor.W_RESIZE) cstr = "w-resize";
    if(aCursor==Cursor.NE_RESIZE) cstr = "ne-resize";
    if(aCursor==Cursor.NW_RESIZE) cstr = "nw-resize";
    if(aCursor==Cursor.SE_RESIZE) cstr = "se-resize";
    if(aCursor==Cursor.SW_RESIZE) cstr = "sw-resize";
    _canvas.getStyle().setProperty("cursor",cstr);
}

/**
 * Called to register for repaint.
 */
public void repaint(Rect aRect)
{
    if(_rview.getFill()==null) _pntr.clearRect(0,0,_rview.getWidth(), _rview.getHeight());
    _pntr.setTransform(1,0,0,1,0,0); // I don't know why I need this!
    ViewUtils.paintAll(_rview, _pntr);
}

/**
 * Called when root view size changes.
 */
void rootViewSizeChange()
{
    int w = (int)Math.ceil(_rview.getWidth()), h = (int)Math.ceil(_rview.getHeight());
    _canvas.setWidth(w*TVWindow.scale); _canvas.setHeight(h*TVWindow.scale);
}

/** Called to handle a drag event. */
/*public void handleDragEvent(DragEvent anEvent) {
    anEvent.preventDefault(); ViewEvent event = CJViewEnv.get().createEvent(_rview, anEvent, null, null);
    _rview.dispatchEvent(event); */
/** Called to handle a drag event. */
/*public void handleDragGesture(DragEvent anEvent) {       
    ViewEvent event = CJViewEnv.get().createEvent(_rview, anEvent, null, null);
    _rview.dispatchEvent(event); if(!CJClipboard.isDragging) { anEvent.preventDefault(); anEvent.stopPropagation(); }}*/
/** Called to handle dragend event. */
/*public void handleDragEnd(DragEvent anEvent) {
    ViewEvent nevent = CJViewEnv.get().createEvent(_rview, anEvent, null, null); _rview.dispatchEvent(nevent); }*/

}