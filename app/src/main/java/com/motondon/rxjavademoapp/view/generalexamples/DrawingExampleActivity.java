package com.motondon.rxjavademoapp.view.generalexamples;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.jakewharton.rxbinding.view.RxMenuItem;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxSeekBar;
import com.motondon.rxjavademoapp.R;
import com.motondon.rxjavademoapp.view.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.subjects.PublishSubject;

import static com.motondon.rxjavademoapp.view.generalexamples.DrawingExampleActivity.MouseDragView.Action.ERASER;
import static com.motondon.rxjavademoapp.view.generalexamples.DrawingExampleActivity.MouseDragView.Action.PAINT;

/**
 * Examples on this activity are based on the following article (with some enhancements, such as change brush color and size, etc):
 *
 *  - http://choruscode.blogspot.com.br/2014/07/rxjava-for-ui-events-on-android-example.html
 *
 * Please, visit it in order to get more details about it.
 *
 * Icons were taken from https://code.tutsplus.com/tutorials/android-sdk-create-a-drawing-app-interface-creation--mobile-19021
 *
 */
public class DrawingExampleActivity extends BaseActivity  {

    private MouseDragView mouseDragView;
    @BindView(R.id.reactive_simple_paint) LinearLayout layout;
    @BindView(R.id.btn_brush) ImageButton btnBrush;
    @BindView(R.id.btn_eraser) ImageButton btnEraser;
    @BindView(R.id.btn_new) ImageButton btnNew;
    @BindView(R.id.brush_size) SeekBar brushSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_drawing_example);
        ButterKnife.bind(this);

        // This is our custom view which will listen and react for mouse events
        mouseDragView = new MouseDragView(this, null);
        layout.addView(mouseDragView, new ActionBar.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getIntent().getStringExtra("TITLE"));
        }

        // Using RxBindings we can listen for SeekBar the same wey we do for the other views. Just choose the
        // appropriate method (on this case we use 'changes' method)
        RxSeekBar.changes(brushSize).subscribe(mouseDragView::setBrushSize);

        // These lines demonstrate RxBindings in action for button clicks. Simple enough!
        RxView.clicks(btnBrush).subscribe(view -> mouseDragView.brush());
        RxView.clicks(btnEraser).subscribe(view -> mouseDragView.eraser());
        RxView.clicks(btnNew).subscribe(view -> mouseDragView.newFile());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reactive_ui_drawing_example, menu);

        MenuItem menuBlue = menu.findItem(R.id.blue);
        MenuItem menuYellow = menu.findItem(R.id.yellow);
        MenuItem menuRed = menu.findItem(R.id.red);
        MenuItem menuGreen = menu.findItem(R.id.green);
        MenuItem menuBlack = menu.findItem(R.id.black);

        // By using RxBindings we can listen for menu items events the same way we do for the other views.
        RxMenuItem
            .clicks(menuBlue)

            // We added filter() operator here only to demonstrate we can transform emitted data the same way we do when working with RxJava.
            // In this case, it will filter out if color is already set to that one user is trying to change to. We no this is actually not necessary,
            // but as said before, this is only for demonstration purpose.
            .filter(m -> mouseDragView.getCurrentBrushColor() != Color.BLUE)
            .subscribe(item -> {
                mouseDragView.changeColor(Color.BLUE);
                menuBlue.setChecked(true);
                menuYellow.setChecked(false);
                menuRed.setChecked(false);
                menuGreen.setChecked(false);
                menuBlack.setChecked(false);
            });

        RxMenuItem
            .clicks(menuYellow)
            .filter(m -> mouseDragView.getCurrentBrushColor() != Color.YELLOW)
            .subscribe(item ->  {
                mouseDragView.changeColor(Color.YELLOW);
                menuBlue.setChecked(false);
                menuYellow.setChecked(true);
                menuRed.setChecked(false);
                menuGreen.setChecked(false);
                menuBlack.setChecked(false);
            });

        RxMenuItem
            .clicks(menuRed)
            .filter(m -> mouseDragView.getCurrentBrushColor() != Color.RED)
            .subscribe(item ->  {
                mouseDragView.changeColor(Color.RED);
                menuBlue.setChecked(false);
                menuYellow.setChecked(false);
                menuRed.setChecked(true);
                menuGreen.setChecked(false);
                menuBlack.setChecked(false);
            });

        RxMenuItem
            .clicks(menuGreen)
            .filter(m -> mouseDragView.getCurrentBrushColor() != Color.GREEN)
            .subscribe(item ->  {
                mouseDragView.changeColor(Color.GREEN);
                menuBlue.setChecked(false);
                menuYellow.setChecked(false);
                menuRed.setChecked(false);
                menuGreen.setChecked(true);
                menuBlack.setChecked(false);
            });

        RxMenuItem
            .clicks(menuBlack)
            .filter(m -> mouseDragView.getCurrentBrushColor() != Color.BLACK)
            .subscribe(item ->  {
                mouseDragView.changeColor(Color.BLACK);
                menuBlue.setChecked(false);
                menuYellow.setChecked(false);
                menuRed.setChecked(false);
                menuGreen.setChecked(false);
                menuBlack.setChecked(true);
            });

        return true;
    }

    /**
     * How it works:
     *
     * First we create a PublishSubject in order to be able to, when subscribe to it, receive only those items that are emitted by the source
     * Observable(s) subsequent to the time of the subscription.
     *
     * Next, we create an observable from our PublishSubject which will be used to filter mouseMotion events according to our needs.
     *
     * Then we create three different observables but using filter() operator so that each one will receive only one specific MouseMotion event.
     *
     * After that we create a MotionEvent listener which will fire mouse event whenever they happen. Inside its action function we call our subject onNext()
     * method which will make every mouseEvent to be emitted by our subject.
     *
     * And finally we subscribe our observable which is interested in the ACTION_DOWN events and start collect all ACTION_MOVE events until ACTION_UP event
     * is received. When it happens, we call our draw method to paint the source to the screen.
     *
     * See comments below for a better understanding.
     *
     */
    public static class MouseDragView extends View {

        public enum Action {
            PAINT,
            ERASER,
        }

        // First create a PublishSubject which allow us to subscribe to it multiple times
        private final PublishSubject<MotionEvent> mTouchSubject = PublishSubject.create();

        // Now, create an observable based on the PublishSubject. It will be used to make some transformations in the emitted items
        private final Observable<MotionEvent> mTouches = mTouchSubject.asObservable();

        // Here we create three different observables which will be used to emit different types of mouse events. Note that, for each
        // one, we are filtering only an specific MotionEvent event type.
        private final Observable<MotionEvent> mDownObservable = mTouches.filter(ev -> ev.getActionMasked() == MotionEvent.ACTION_DOWN);
        private final Observable<MotionEvent> mUpObservable = mTouches.filter(ev -> ev.getActionMasked() == MotionEvent.ACTION_UP);
        private final Observable<MotionEvent> mMovesObservable = mTouches.filter(ev -> ev.getActionMasked() == MotionEvent.ACTION_MOVE);

        private Bitmap mBitmap;
        private Canvas mCanvas;
        private Paint mPaint;
        private MouseDragView.Action action;
        private Integer currentColor;

        public MouseDragView(Context context, AttributeSet attr) {
            super(context, attr);

            // This action will inform our draw what to do
            this.action = PAINT;

            this.setBackgroundColor(Color.WHITE);
            this.currentColor = Color.BLACK;

            // Create a listener for the motion event. This will make our subject to publish mouse events whenever they happen.
            setOnTouchListener((View v, MotionEvent event) -> {
                mTouchSubject.onNext(event);
                return true;
            });

            // Now it is time to subscribe our observables in order to do something when they receive events. This is where all the reactive magic happens.
            // Actually we will only subscribe the observable interesting in the ACTION_DOWN events. Whenever it happens, it will create a Path object and
            // store the coordinates to it while the ACTION_MOVE events are being received. Once ACTION_UP is received it will stop storing path (due the
            // takeUntil() operator) and will draw the path to the canvas.
            mDownObservable.subscribe(downEvent -> {
                final Path path = new Path();
                path.moveTo(downEvent.getX(), downEvent.getY());
                Log.i(downEvent.toString(), "Touch down");

                mMovesObservable
                    .takeUntil(mUpObservable
                        .doOnNext(upEvent -> {
                            draw(path);
                            path.close();
                            Log.i(upEvent.toString(), "Touch up");
                        }))
                    .subscribe(motionEvent -> {
                        path.lineTo(motionEvent.getX(), motionEvent.getY());
                        draw(path);
                        Log.i(motionEvent.toString(), "Touch move");
                    });

            });

            init();
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);

            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }

        public void init() {
            mPaint = new Paint();
            mPaint.setDither(true);
            mPaint.setColor(currentColor);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(3);
        }

        private void draw(Path path) {
            if (action == PAINT) {
                if (mBitmap == null) {
                    mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
                    mCanvas = new Canvas(mBitmap);
                }

                // When painting, use color chosen by the user in the menu options.
                mPaint.setColor(currentColor);
                mCanvas.drawPath(path, mPaint);
                invalidate();

            // When current action is ERASER, change brush color to white in order to give the eraser effect (since this is the same color as
            // the background
            } else if (action == ERASER) {
                if (mBitmap != null) {
                    mPaint.setColor(Color.WHITE);
                    mCanvas.drawPath(path, mPaint);
                    invalidate();
                }
            }
        }

        // This is an event called by the framework when we call invalidate().
        public void onDraw(Canvas canvas) {
            if (mBitmap != null) {
                canvas.drawBitmap(mBitmap, 0, 0, mPaint);
            }
        }

        // Used when user change brush side in the seekBar view.
        public void setBrushSize(Integer size) {
            mPaint.setStrokeWidth(size);
        }

        // Called when user change current color form the menu.
        public void changeColor(int newColor) {
            currentColor = newColor;
            brush();
        }

        // Used to avoid change current color the a color that is already the current one. This is not actually needed, but is only
        // to demonstrate filter() operator in action.
        public Integer getCurrentBrushColor() {
            return currentColor;
        }

        // This is called when user chooses to create a new draw
        public void newFile() {
            if (mBitmap != null) {

                mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
                mCanvas = new Canvas(mBitmap);

                final Path path = new Path();
                mCanvas.drawPath(path, mPaint);
                invalidate();

                brush();
            }
        }

        // Change the current action to ERASER.
        public void eraser() {
            action = ERASER;
        }

        // Change the current action to PAINT.
        public void brush() {
            action = PAINT;
        }
    }
}
