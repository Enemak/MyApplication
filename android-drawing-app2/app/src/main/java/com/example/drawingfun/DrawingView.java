package com.example.drawingfun;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.RemoteViews;

import static android.view.MotionEvent.INVALID_POINTER_ID;

@RemoteViews.RemoteView
public class DrawingView extends ViewGroup implements ScaleGestureDetector.OnScaleGestureListener {

	private static final int INVALID_POINTER_ID = 1;
	private int mActivePointerId = INVALID_POINTER_ID;
	private Matrix mScaleMatrix = new Matrix();
	private Matrix mScaleMatrixInverse = new Matrix();
	private Matrix mTranslateMatrix = new Matrix();
	private Matrix mTranslateMatrixInverse = new Matrix();
	private float mLastTouchX;
	private float mLastTouchY;
	private float mFocusY;
	private float mFocusX;
	private float[] mInvalidateWorkingArray = new float[6];
	private float[] mDispatchTouchEventWorkingArray = new float[2];
	private float[] mOnTouchEventWorkingArray = new float[2];
	private int mLeftWidth;
	private int mRightWidth;
	private final Rect mTmpContainerRect = new Rect();
	private final Rect mTmpChildRect = new Rect();
	private Path drawPath;
	private Paint drawPaint, canvasPaint;
	private int paintColor = 0xFFFF0000, paintAlpha = 255;
	private Canvas drawCanvas;
	private Bitmap canvasBitmap;
	private float brushSize, lastBrushSize;
	private boolean erase=false;
	private ViewGroup RootLayout;
	private int Position_X;
	private int Position_Y;
	private boolean longClickActive = true;
	private boolean rotation= true;
	private boolean move= true;
	private DrawingView circlesView;
	private boolean ShemaZoom=true;
	private ScaleGestureDetector mScaleDetector;
	private float mScaleFactor = 1.f;
	private static final String TAG = "ZoomLayout";
	private static final float MIN_ZOOM = 1.0f;
	private static final float MAX_ZOOM = 4.0f;
	private Mode mode = Mode.NONE;
	private float scale = 1.0f;
	private float lastScaleFactor = 0f;
	private float startX = 0f;
	private float startY = 0f;
	private float prevDx = 0f;
	private float prevDy = 0f;
	private float mX, mY;
	DrawingView	drawView = (DrawingView)findViewById(R.id.root);
	final ImageView iv = new ImageView(getContext());
	float mPosX=0;
	float mPosY=0;
	float dx = 0f;
	float dy = 0f;
	int clickCount;
	float touchX ;
	float touchY;
	long startTime= 0;
	float distx;
	float disty;
	private enum Mode {
		NONE,
		DRAG,
		ZOOM
	}

	public DrawingView(Context context) {
		super(context);
		init(context);
		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
		mTranslateMatrix.setTranslate(0, 0);
		mScaleMatrix.setScale(1, 1);}
	public DrawingView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		setupDrawing();
		init(context);
		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
		mTranslateMatrix.setTranslate(0, 0);
		mScaleMatrix.setScale(1, 1);
	}
	public DrawingView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
		mTranslateMatrix.setTranslate(0, 0);
		mScaleMatrix.setScale(1, 1);}
	//setup drawing
	private void setupDrawing(){
		//prepare for drawing and setup paint stroke properties
		brushSize = getResources().getInteger(R.integer.medium_size);
		lastBrushSize = brushSize;
		drawPath = new Path();
		drawPaint = new Paint();
		drawPaint.setColor(paintColor);
		drawPaint.setAntiAlias(true);
		drawPaint.setStrokeWidth(brushSize);
		drawPaint.setStyle(Paint.Style.STROKE);
		drawPaint.setStrokeJoin(Paint.Join.ROUND);
		drawPaint.setStrokeCap(Paint.Cap.ROUND);
		canvasPaint = new Paint(Paint.DITHER_FLAG);
	}
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		drawCanvas = new Canvas(canvasBitmap);
	}
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
		canvas.drawPath(drawPath, drawPaint);
	}
	@Override
	protected void dispatchDraw(Canvas canvas) {
		canvas.save();
		canvas.translate(mPosX, mPosY);
			canvas.scale(mScaleFactor, mScaleFactor, mFocusX, mFocusY);
		super.dispatchDraw(canvas);
		canvas.restore();
	}
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		mDispatchTouchEventWorkingArray[0] = ev.getX();
		mDispatchTouchEventWorkingArray[1] = ev.getY();
		mDispatchTouchEventWorkingArray = screenPointsToScaledPoints(mDispatchTouchEventWorkingArray);
		ev.setLocation(mDispatchTouchEventWorkingArray[0],
				mDispatchTouchEventWorkingArray[1]);
		return super.dispatchTouchEvent(ev);
	}
	@Override
	public ViewParent invalidateChildInParent(int[] location, Rect dirty) {
		mInvalidateWorkingArray[0] = dirty.left;
		mInvalidateWorkingArray[1] = dirty.top;
		mInvalidateWorkingArray[2] = dirty.right;
		mInvalidateWorkingArray[3] = dirty.bottom;
		mInvalidateWorkingArray = scaledPointsToScreenPoints(mInvalidateWorkingArray);
		dirty.set(Math.round(mInvalidateWorkingArray[0]), Math.round(mInvalidateWorkingArray[1]),
				Math.round(mInvalidateWorkingArray[2]), Math.round(mInvalidateWorkingArray[3]));
		location[0] *= mScaleFactor;
		location[1] *= mScaleFactor;
		return super.invalidateChildInParent(location, dirty);
	}
	private float[] scaledPointsToScreenPoints(float[] a) {
		mScaleMatrix.mapPoints(a);
		mTranslateMatrix.mapPoints(a);
		return a;
	}
	private float[] screenPointsToScaledPoints(float[] a){
		mTranslateMatrixInverse.mapPoints(a);
		mScaleMatrixInverse.mapPoints(a);
		return a;
	}

	@Override
	public boolean performClick() {
		return true;
	}
	private void init(Context context) {
		final ScaleGestureDetector scaleDetector = new ScaleGestureDetector(context, this);
		this.setOnTouchListener(new View.OnTouchListener() {
	@Override
	public boolean onTouch(View view,MotionEvent event) {
		mOnTouchEventWorkingArray[0] = event.getX();
		mOnTouchEventWorkingArray[1] = event.getY();
		mOnTouchEventWorkingArray = scaledPointsToScreenPoints(mOnTouchEventWorkingArray);
		event.setLocation(mOnTouchEventWorkingArray[0], mOnTouchEventWorkingArray[1]);
		mScaleDetector.onTouchEvent(event);
		float touchX = event.getX();
		float touchY = event.getY();
		switch (event.getAction()& MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			float x = event.getX();
			float y = event.getY();
			mLastTouchX = x;
			mLastTouchY = y;
			// Save the ID of this pointer
			mActivePointerId = event.getPointerId(0);
			mX = x;
			mY = y;
			if (scale > MIN_ZOOM) {
				mode = Mode.DRAG;
				startX = event.getX() - prevDx;
				startY = event.getY() - prevDy;
			}
			drawPath.moveTo(touchX, touchY);
			break;
		case MotionEvent.ACTION_MOVE:
			if (mode == Mode.DRAG) {
				dx = event.getX() - startX;
				dy = event.getY() - startY;
			}
			 distx = Math.abs(touchX - mX);
			 disty = Math.abs(touchY - mY);
			int pointerIndex = event.findPointerIndex(mActivePointerId);
			 if (!MainActivity.getInstance().getSchemaOption())
			drawPath.lineTo(touchX, touchY);
             else{
			float x1 = event.getX(pointerIndex);
			float y1 = event.getY(pointerIndex);
			final float dx = x1 - mLastTouchX;
			final float dy = y1 - mLastTouchY;
			mPosX += dx;
			mPosY += dy;
			mTranslateMatrix.preTranslate(dx, dy);
			mTranslateMatrix.invert(mTranslateMatrixInverse);
			mLastTouchX = x1;
			mLastTouchY = y1;
			invalidate();}
			break;
			case MotionEvent.ACTION_POINTER_DOWN:
				mode = Mode.ZOOM;
				break;
			case MotionEvent.ACTION_CANCEL:
				mActivePointerId = INVALID_POINTER_ID;
				break;
			case MotionEvent.ACTION_POINTER_UP:
				mode = Mode.DRAG;
				pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
				final int pointerId = event.getPointerId(pointerIndex);
				if (pointerId == mActivePointerId ) {
					final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
					mLastTouchX = event.getX(newPointerIndex);
					mLastTouchY = event.getY(newPointerIndex);
					mActivePointerId = event.getPointerId(newPointerIndex);
				}
				break;
		case MotionEvent.ACTION_UP:
			mActivePointerId = INVALID_POINTER_ID;
			mode = Mode.NONE;
			drawPath.lineTo(touchX, touchY);
			//drawCanvas.drawPath(drawPath, drawPaint);
			if ((distx >= 200 || disty >= 200) && !MainActivity.getInstance().getSchemaOption()) {
				if( MainActivity.getInstance().getCouleur()=="rouge"){
				ImageView iv = new ImageView(getContext());
				iv.setImageResource(R.drawable.cerclerouge);
				//int radius = Math.min((int) touchX, (int) touchY) / 2;
				// (touchX+mX)/2, (int) (touchY+mY)/2
				DrawingView.LayoutParams layoutParams = new DrawingView.LayoutParams((int)distx, (int)disty);
				layoutParams.setMargins((int) touchX, (int) touchY, (int) touchX, (int) touchY);
				iv.setLayoutParams(layoutParams);
				drawView.addView(iv, layoutParams);
				iv.setOnTouchListener(MainActivity.getInstance());}
			    else if( MainActivity.getInstance().getCouleur()=="vert"){
					ImageView iv = new ImageView(getContext());
					iv.setImageResource(R.drawable.circlevert);
					DrawingView.LayoutParams layoutParams = new DrawingView.LayoutParams((int)distx, (int)disty);
					layoutParams.setMargins((int) touchX, (int) touchY, (int) touchX, (int) touchY);
					iv.setLayoutParams(layoutParams);
					drawView.addView(iv, layoutParams);
					iv.setOnTouchListener(MainActivity.getInstance());}
			}
			else if ((distx <= 200 || disty <= 200) && !MainActivity.getInstance().getSchemaOption()) {
				if( MainActivity.getInstance().getCouleur()=="rouge"){
				ImageView iv = new ImageView(getContext());
				iv.setImageResource(R.drawable.carrerouge);
					//ShapeDrawable rect = new ShapeDrawable(new RectShape());
					//rect.getPaint().setColor(paintColor);
					//if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
					//	iv.setBackground(rect);
					//}
					//Drawable drawable = getResources().getDrawable(R.drawable.line);
					//GradientDrawable gradientDrawable = (GradientDrawable) drawable;
					//gradientDrawable.setColor(paintColor);
					//gradientDrawable.setStroke(12, paintColor);
					DrawingView.LayoutParams layoutParams = new DrawingView.LayoutParams((int)distx, (int)disty);
				layoutParams.setMargins((int) touchX, (int) touchY, (int) touchX, (int) touchY);
				iv.setLayoutParams(layoutParams);
				drawView.addView(iv, layoutParams);
				iv.setOnTouchListener(MainActivity.getInstance());}
                else if( MainActivity.getInstance().getCouleur()=="vert"){
					ImageView iv = new ImageView(getContext());
					iv.setImageResource(R.drawable.carrevert);
					DrawingView.LayoutParams layoutParams = new DrawingView.LayoutParams((int)distx, (int)disty);
					layoutParams.setMargins((int) touchX, (int) touchY, (int) touchX, (int) touchY);
					iv.setLayoutParams(layoutParams);
					drawView.addView(iv, layoutParams);
					iv.setOnTouchListener(MainActivity.getInstance());}
			}
			drawPath.reset();
			break;
		default:
			return false;
		}
		scaleDetector.onTouchEvent(event);
		if ((mode == Mode.DRAG && scale >= MIN_ZOOM) || mode == Mode.ZOOM) {
			getParent().requestDisallowInterceptTouchEvent(true);
			float maxDx = (child().getWidth() - (child().getWidth() / scale)) / 2 * scale;
			float maxDy = (child().getHeight() - (child().getHeight() / scale))/ 2 * scale;
			dx = Math.min(Math.max(dx, -maxDx), maxDx);
			dy = Math.min(Math.max(dy, -maxDy), maxDy);
			Log.i(TAG, "Width: " + child().getWidth() + ", scale " + scale + ", dx " + dx
					+ ", max " + maxDx);
			//applyScaleAndTranslation();
		}
		invalidate();
		return true;
	}
		});
	}
	@Override
	public boolean onScaleBegin(ScaleGestureDetector scaleDetector) {
		Log.i(TAG, "onScaleBegin");
		return true;
	}
	@Override
	public boolean onScale(ScaleGestureDetector scaleDetector) {
		float scaleFactor = scaleDetector.getScaleFactor();
		Log.i(TAG, "onScale" + scaleFactor);
		if (lastScaleFactor == 0 || (Math.signum(scaleFactor) == Math.signum(lastScaleFactor))) {
			scale *= scaleFactor;
			scale = Math.max(MIN_ZOOM, Math.min(scale, MAX_ZOOM));
			lastScaleFactor = scaleFactor;
		} else {
			lastScaleFactor = 0;
		}
		return true;
	}
	@Override
	public void onScaleEnd(ScaleGestureDetector scaleDetector) {
		Log.i(TAG, "onScaleEnd");
	}
	private void applyScaleAndTranslation() {
		child().setScaleX(scale);
		child().setScaleY(scale);
		child().setTranslationX(dx);
		child().setTranslationY(dy);
	}
	private View child() {
		return getChildAt(0);
	}
	//update color
	public void setColor(String newColor){
		invalidate();
		//check whether color value or pattern name
		if(newColor.startsWith("#")){
			paintColor = Color.parseColor(newColor);
			drawPaint.setColor(paintColor);
			drawPaint.setShader(null);
		}
		else{
			int patternID = getResources().getIdentifier(
					newColor, "drawable", "com.example.drawingfun");
			Bitmap patternBMP = BitmapFactory.decodeResource(getResources(), patternID);
			BitmapShader patternBMPshader = new BitmapShader(patternBMP,
					Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);  
			drawPaint.setColor(0xFFFFFFFF);
			drawPaint.setShader(patternBMPshader);
		}
	}
	public void setBrushSize(float newSize){
		float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 
				newSize, getResources().getDisplayMetrics());
		brushSize=pixelAmount;
		drawPaint.setStrokeWidth(brushSize);
	}
	public void setLastBrushSize(float lastSize){
		lastBrushSize=lastSize;
	}
	public float getLastBrushSize(){
		return lastBrushSize;
	}
	public void setErase(boolean isErase){
		erase=isErase;
		if(erase) drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		else drawPaint.setXfermode(null);
	}
	public void startNew(){
		drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
		invalidate();
	}
	public int getPaintAlpha(){
		return Math.round((float)paintAlpha/255*100);
	}
	public void setPaintAlpha(int newAlpha){
		paintAlpha=Math.round((float)newAlpha/100*255);
		drawPaint.setColor(paintColor);
		drawPaint.setAlpha(paintAlpha);
	}
	@Override
	public boolean shouldDelayChildPressedState() {
		return false;
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int count = getChildCount();
		mLeftWidth = 0;
		mRightWidth = 0;
		int maxHeight = 0;
		int maxWidth = 0;
		int childState = 0;
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			if (child.getVisibility() != GONE) {
				measureChild(child, widthMeasureSpec, heightMeasureSpec);
			}
		}
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() != GONE) {
				measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
				final DrawingView.LayoutParams lp = (DrawingView.LayoutParams) child.getLayoutParams();
				if (lp.position == DrawingView.LayoutParams.POSITION_LEFT) {
					mLeftWidth += Math.max(maxWidth,
							child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
				} else if (lp.position == DrawingView.LayoutParams.POSITION_RIGHT) {
					mRightWidth += Math.max(maxWidth,
							child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
				} else {
					maxWidth = Math.max(maxWidth,
							child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
				}
				maxHeight = Math.max(maxHeight,
						child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
				childState = combineMeasuredStates(childState, child.getMeasuredState());
			}
		}
		maxWidth += mLeftWidth + mRightWidth;
		maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
		maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());
		setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
				resolveSizeAndState(maxHeight, heightMeasureSpec,
						childState << MEASURED_HEIGHT_STATE_SHIFT));
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		final int count = getChildCount();
		int leftPos = getPaddingLeft();
		int rightPos = right - left - getPaddingRight();
		final int middleLeft = leftPos + mLeftWidth;
		final int middleRight = rightPos - mRightWidth;
		final int parentTop = getPaddingTop();
		final int parentBottom = bottom - top - getPaddingBottom();
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			if (child.getVisibility() != GONE) {
				child.layout(left, top, left+child.getMeasuredWidth(), top + child.getMeasuredHeight());
			}
		}
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() != GONE) {
				final DrawingView.LayoutParams lp = (DrawingView.LayoutParams) child.getLayoutParams();
				final int width = child.getMeasuredWidth();
				final int height = child.getMeasuredHeight();
				if (lp.position == DrawingView.LayoutParams.POSITION_LEFT) {
					mTmpContainerRect.left = leftPos + lp.leftMargin;
					mTmpContainerRect.right = leftPos + width + lp.rightMargin;
					leftPos = mTmpContainerRect.right;
				} else if (lp.position == DrawingView.LayoutParams.POSITION_RIGHT) {
					mTmpContainerRect.right = rightPos - lp.rightMargin;
					mTmpContainerRect.left = rightPos - width - lp.leftMargin;
					rightPos = mTmpContainerRect.left;
				} else {
					mTmpContainerRect.left = middleLeft + lp.leftMargin;
					mTmpContainerRect.right = middleRight - lp.rightMargin;
				}
				mTmpContainerRect.top = parentTop + lp.topMargin;
				mTmpContainerRect.bottom = parentBottom - lp.bottomMargin;
				Gravity.apply(lp.gravity, width, height, mTmpContainerRect, mTmpChildRect);
				child.layout(mTmpChildRect.left, mTmpChildRect.top,
						mTmpChildRect.right, mTmpChildRect.bottom);
			}
		}
	}
	@Override
	public DrawingView.LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new DrawingView.LayoutParams(getContext(), attrs);
	}
	@Override
	protected DrawingView.LayoutParams generateDefaultLayoutParams() {
		return new DrawingView.LayoutParams(DrawingView.LayoutParams.MATCH_PARENT, DrawingView.LayoutParams.MATCH_PARENT);
	}
	@Override
	protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
		return new DrawingView.LayoutParams(p);
	}
	@Override
	protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
		return p instanceof DrawingView.LayoutParams;
	}
	public static class LayoutParams extends MarginLayoutParams {
		public int gravity = Gravity.TOP | Gravity.START;
		public static int POSITION_MIDDLE = 0;
		public static int POSITION_LEFT = 1;
		public static int POSITION_RIGHT = 2;
		public int position = POSITION_MIDDLE;
		public LayoutParams(Context c, AttributeSet attrs) {
			super(c, attrs);
		}
		public LayoutParams(int width, int height) {
			super(width, height);
		}
		public LayoutParams(ViewGroup.LayoutParams source) {
			super(source);
		}
	}
	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			mScaleFactor *= detector.getScaleFactor();
			if (detector.isInProgress()) {
				mFocusX = detector.getFocusX();
				mFocusY = detector.getFocusY();
			}
			mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));
			mScaleMatrix.setScale(mScaleFactor, mScaleFactor,
					mFocusX, mFocusY);
			mScaleMatrix.invert(mScaleMatrixInverse);
			invalidate();
			requestLayout();
			return true;
		}
	}
}