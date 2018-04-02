package com.example.drawingfun;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGImageView;
import com.caverock.androidsvg.SVGParseException;
import java.util.Calendar;
import java.util.UUID;
import uk.co.senab.photoview.PhotoViewAttacher;


public class MainActivity extends Activity  implements View.OnTouchListener , View.OnClickListener {
	static MainActivity activityA;
	PhotoViewAttacher pAttacher;
    public String couleur="rouge";
	private ViewGroup RootLayout;
	boolean visibility=true;
	private int Position_X;
	private int Position_Y;
	private boolean longClickActive = true;
	private boolean rotation= true;
	private boolean move= true;
	private DrawingView drawView;
	private DrawingView circlesView;
	private boolean ShemaZoom=true;
	private ImageButton currPaint;
	private static final int MIN_CLICK_DURATION = 1000;
	private long startClickTime;
	public boolean schemaOption=true;
	int clickCount = 0;;
	long startTime = 0 ;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		drawView = (DrawingView)findViewById(R.id.root);
		activityA = this;
		LinearLayout paintLayout = (LinearLayout)findViewById(R.id.paint_colors);
		currPaint = (ImageButton)paintLayout.getChildAt(0);
		currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
		final DrawingView drawingView = new DrawingView(getApplicationContext());

		SVG svg = null;
		try {
			svg = SVG.getFromResource(getResources(), R.raw.exemple2);
		} catch (SVGParseException e) {
			e.printStackTrace();
		}
		final SVGImageView svgImageView = new SVGImageView(this);
		svgImageView.setSVG(svg);
		svgImageView.setLayoutParams(
				new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.MATCH_PARENT));

		drawView.addView(svgImageView);

		ImageButton alarmeBtn = (ImageButton) findViewById(R.id.alarme_btn);
		alarmeBtn.setOnClickListener(this);
		ImageButton hubBtn = (ImageButton)findViewById(R.id.hub_btn);
		hubBtn.setOnClickListener(this);
		ImageButton gatewayBtn = (ImageButton)findViewById(R.id.gateway_btn);
		gatewayBtn.setOnClickListener(this);
		ImageButton hideBtn = (ImageButton)findViewById(R.id.hide_btn);
		hideBtn.setOnClickListener(this);
		ImageButton saveBtn = (ImageButton)findViewById(R.id.save_btn);
		saveBtn.setOnClickListener(this);
	}

	public static MainActivity getInstance(){
		return   activityA;
	}

	public void onClick(View view){
		final ImageView iv = new ImageView(this);
		if(view.getId()==R.id.alarme_btn) {
			if (schemaOption)
			{Toast modeZoomToast = Toast.makeText(getApplicationContext(),
					"En mode zoom!", Toast.LENGTH_SHORT);
				modeZoomToast.show();}
				else{
			iv.setImageResource(R.drawable.alarme);
			DrawingView.LayoutParams layoutParams = new DrawingView.LayoutParams(50, 50);
			iv.setLayoutParams(layoutParams);
			drawView.addView(iv, layoutParams);
			iv.setOnTouchListener( this);}
		}
        else if(view.getId()==R.id.hub_btn) {
			if (schemaOption)
			{Toast modeZoomToast = Toast.makeText(getApplicationContext(),
					"En mode zoom!", Toast.LENGTH_SHORT);
				modeZoomToast.show();}
			else{
			iv.setImageResource(R.drawable.hub);
			DrawingView.LayoutParams layoutParams = new DrawingView.LayoutParams(70, 70);
			iv.setLayoutParams(layoutParams);
			drawView.addView(iv, layoutParams);
			iv.setOnTouchListener( this);}
		}
		else if(view.getId()==R.id.gateway_btn) {
			if (schemaOption)
			{Toast modeZoomToast = Toast.makeText(getApplicationContext(),
					"En mode zoom!", Toast.LENGTH_SHORT);
				modeZoomToast.show();}
			else{
			iv.setImageResource(R.drawable.gateway);
			DrawingView.LayoutParams layoutParams = new DrawingView.LayoutParams(52, 52);
			iv.setLayoutParams(layoutParams);
			drawView.addView(iv, layoutParams);
			iv.setOnTouchListener( this);}
		}
		else if(view.getId()==R.id.save_btn){
			AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
			saveDialog.setTitle("Enregistrer le fichier");
			saveDialog.setMessage("Voulez vous enregistrer le fichier ?");
			saveDialog.setPositiveButton("Oui", new DialogInterface.OnClickListener(){

				public void onClick(DialogInterface dialog, int which){
					drawView.setDrawingCacheEnabled(true);
					String imgSaved = MediaStore.Images.Media.insertImage(
							getContentResolver(), drawView.getDrawingCache(),
							UUID.randomUUID().toString()+".png", "drawing");
					if(imgSaved!=null){
						Toast savedToast = Toast.makeText(getApplicationContext(),
								"Fichier enregistré!", Toast.LENGTH_SHORT);
						savedToast.show();
					}
					else{
						Toast unsavedToast = Toast.makeText(getApplicationContext(),
								"Oops! Le fichier n'a pas pu etre enregistré.", Toast.LENGTH_SHORT);
						unsavedToast.show();
					}
					drawView.destroyDrawingCache();
				}
			});
			saveDialog.setNegativeButton("Annuler", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					dialog.cancel();
				}
			});
			saveDialog.show();
		}
		else if(view.getId()==R.id.hide_btn){
			if (visibility) {
				for (int i = 1; i < drawView.getChildCount(); i++) {
					View child = drawView.getChildAt(i);
					child.setVisibility(View.INVISIBLE);
				}
				visibility =false;
				Toast unsavedToast = Toast.makeText(getApplicationContext(),
						"Cachés", Toast.LENGTH_SHORT);
				unsavedToast.show();
			} else {
				for (int i = 0; i < drawView.getChildCount(); i++) {
					View child = drawView.getChildAt(i);
					child.setVisibility(View.VISIBLE);
				}
				Toast unsavedToast = Toast.makeText(getApplicationContext(),
						"Affichés", Toast.LENGTH_SHORT);
				unsavedToast.show();
				visibility =true;
			}
		}
	}

	public void zoomClick(View view){
		schemaOption=true;
	}
	public void drawClick(View view){
		schemaOption=false;
	}
	public void paintClickedRouge(View view){
		//set erase false
		drawView.setErase(false);
		drawView.setPaintAlpha(100);
		drawView.setBrushSize(drawView.getLastBrushSize());

		if(view!=currPaint){
			ImageButton imgView = (ImageButton)view;
			String color = view.getTag().toString();
			couleur="rouge";
			drawView.setColor(color);
			//update ui
			imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
			currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
			currPaint=(ImageButton)view;
		}
	}

	public void paintClickedVert(View view){
		drawView.setErase(false);
		drawView.setPaintAlpha(100);
		drawView.setBrushSize(drawView.getLastBrushSize());

		if(view!=currPaint){
			ImageButton imgView = (ImageButton)view;
			String color = view.getTag().toString();
			couleur="vert";
			drawView.setColor(color);
			//update ui
			imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
			currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
			currPaint=(ImageButton)view;
		}
	}

    public String getCouleur(){return 	couleur; }
	public Boolean getSchemaOption(){return schemaOption ; }

	public boolean onTouch(final View view, MotionEvent event) {
		final int X = (int) event.getRawX();
		final int Y = (int) event.getRawY();
		int pointerCount = event.getPointerCount();
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				if (longClickActive == true) {
					longClickActive = false;
					startClickTime = Calendar.getInstance().getTimeInMillis();
				}
				DrawingView.LayoutParams layoutParams = (DrawingView.LayoutParams) view.getLayoutParams();
				Position_X = X - layoutParams.leftMargin;
				Position_Y = Y - layoutParams.topMargin;
				break;
			case MotionEvent.ACTION_UP:
				longClickActive = true;
				rotation= true;
				move= true;
				if (startTime == 0){
					startTime = System.currentTimeMillis();
				}else {
					if (System.currentTimeMillis() - startTime < 200) {
						AlertDialog.Builder builder = new AlertDialog.Builder(this);
						builder.setMessage("Voulez vous supprimer cet élément ?");
						builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								drawView.removeView(view);
							}
						});
						builder.setNegativeButton("Non", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						});
						AlertDialog alertDialog = builder.create();
						alertDialog.show();
					}
					startTime = System.currentTimeMillis();
				}
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				break;
			case MotionEvent.ACTION_POINTER_UP:
				break;
			case MotionEvent.ACTION_MOVE:
				if (pointerCount == 1){
					if (move) {
						//Move the ImageView
						DrawingView.LayoutParams Params = (DrawingView.LayoutParams) view.getLayoutParams();
						Params.leftMargin = X - Position_X;
						Params.topMargin = Y - Position_Y;
						Params.rightMargin = -500;
						Params.bottomMargin = -500;
						view.setLayoutParams(Params);
					}

				}
				if (longClickActive == false) {
					long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
					if (clickDuration >= MIN_CLICK_DURATION) {
						if (rotation) {
							//Rotate the ImageView
							view.setRotation(view.getRotation() + 10.0f);
						}
					}
				}
				if (pointerCount == 2){
					rotation= false;
					move= false;
					//Zooming the ImageView
					DrawingView.LayoutParams layoutParams1 =  (DrawingView.LayoutParams) view.getLayoutParams();
					layoutParams1.width = Position_X +(int)event.getX();
					layoutParams1.height = Position_Y + (int)event.getY();
					view.setLayoutParams(layoutParams1);
				}
				break;
		}
		drawView.invalidate();
		return true;
	}
}