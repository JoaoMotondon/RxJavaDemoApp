package com.motondon.rxjavademoapp.view.generalexamples

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.util.AttributeSet
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import com.jakewharton.rxbinding.view.RxMenuItem
import com.jakewharton.rxbinding.view.RxView
import com.jakewharton.rxbinding.widget.RxSeekBar

import com.motondon.rxjavademoapp.R
import com.motondon.rxjavademoapp.view.base.BaseActivity

import rx.subjects.PublishSubject

import com.motondon.rxjavademoapp.view.generalexamples.DrawingExampleActivity.MouseDragView.Action.ERASER
import com.motondon.rxjavademoapp.view.generalexamples.DrawingExampleActivity.MouseDragView.Action.PAINT
import kotlinx.android.synthetic.main.activity_general_drawing_example.*


/**
 * Examples on this activity are based on the following article (with some enhancements, such as change brush color and size, etc):
 *
 * - http://choruscode.blogspot.com.br/2014/07/rxjava-for-ui-events-on-android-example.html
 *
 * Please, visit it in order to get more details about it.
 *
 * Icons were taken from https://code.tutsplus.com/tutorials/android-sdk-create-a-drawing-app-interface-creation--mobile-19021
 *
 */
class DrawingExampleActivity : BaseActivity() {

    private var mouseDragView: MouseDragView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_general_drawing_example)

        // This is our custom view which will listen and react for mouse events
        mouseDragView = MouseDragView(this, null)
        layout.addView(mouseDragView, ActionBar.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT))

        supportActionBar?.title = intent.getStringExtra("TITLE")

        // Using RxBindings we can listen for SeekBar the same wey we do for the other views. Just choose the
        // appropriate method (on this case we use 'changes' method)
        RxSeekBar.changes(brushSize).subscribe { mouseDragView?.setBrushSize(it) }

        // These lines demonstrate RxBindings in action for button clicks. Simple enough!
        RxView.clicks(btnBrush).subscribe { _ -> mouseDragView?.brush() }
        RxView.clicks(btnEraser).subscribe { _ -> mouseDragView?.eraser() }
        RxView.clicks(btnNew).subscribe { _ -> mouseDragView?.newFile() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_reactive_ui_drawing_example, menu)

        val menuBlue = menu.findItem(R.id.blue)
        val menuYellow = menu.findItem(R.id.yellow)
        val menuRed = menu.findItem(R.id.red)
        val menuGreen = menu.findItem(R.id.green)
        val menuBlack = menu.findItem(R.id.black)

        // By using RxBindings we can listen for menu items events the same way we do for the other views.
        RxMenuItem
            .clicks(menuBlue)

            // We added filter() operator here only to demonstrate we can transform emitted data the same way we do when working with RxJava.
            // In this case, it will filter out if color is already set to that one user is trying to change to. We no this is actually not necessary,
            // but as said before, this is only for demonstration purpose.
            .filter { _ -> mouseDragView?.currentBrushColor != Color.BLUE }
            .subscribe { _ ->
                mouseDragView?.changeColor(Color.BLUE)
                menuBlue.isChecked = true
                menuYellow.isChecked = false
                menuRed.isChecked = false
                menuGreen.isChecked = false
                menuBlack.isChecked = false
            }

        RxMenuItem
            .clicks(menuYellow)
            .filter { _ -> mouseDragView?.currentBrushColor != Color.YELLOW }
            .subscribe { _ ->
                mouseDragView?.changeColor(Color.YELLOW)
                menuBlue.isChecked = false
                menuYellow.isChecked = true
                menuRed.isChecked = false
                menuGreen.isChecked = false
                menuBlack.isChecked = false
            }

        RxMenuItem
            .clicks(menuRed)
            .filter { _ -> mouseDragView?.currentBrushColor != Color.RED }
            .subscribe { _ ->
                mouseDragView?.changeColor(Color.RED)
                menuBlue.isChecked = false
                menuYellow.isChecked = false
                menuRed.isChecked = true
                menuGreen.isChecked = false
                menuBlack.isChecked = false
            }

        RxMenuItem
            .clicks(menuGreen)
            .filter { _ -> mouseDragView?.currentBrushColor != Color.GREEN }
            .subscribe { _ ->
                mouseDragView?.changeColor(Color.GREEN)
                menuBlue.isChecked = false
                menuYellow.isChecked = false
                menuRed.isChecked = false
                menuGreen.isChecked = true
                menuBlack.isChecked = false
            }

        RxMenuItem
            .clicks(menuBlack)
            .filter { _ -> mouseDragView?.currentBrushColor != Color.BLACK }
            .subscribe { _ ->
                mouseDragView?.changeColor(Color.BLACK)
                menuBlue.isChecked = false
                menuYellow.isChecked = false
                menuRed.isChecked = false
                menuGreen.isChecked = false
                menuBlack.isChecked = true
            }

        return true
    }

    /**
     * How it works:
     *
     * First we create a PublishSubject in order to be able to, when subscribe to it, receive only those items that are emitted by the source
     * Observable(s) subsequent to the time of the mSubscription.
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
    class MouseDragView(context: Context, attr: AttributeSet?) : View(context, attr) {

        // First create a PublishSubject which allow us to subscribe to it multiple times
        private val mTouchSubject = PublishSubject.create<MotionEvent>()

        // Now, create an observable based on the PublishSubject. It will be used to make some transformations in the emitted items
        private val mTouches = mTouchSubject.asObservable()

        // Here we create three different observables which will be used to emit different types of mouse events. Note that, for each
        // one, we are filtering only an specific MotionEvent event type.
        private val mDownObservable = mTouches.filter { ev -> ev.actionMasked == MotionEvent.ACTION_DOWN }
        private val mUpObservable = mTouches.filter { ev -> ev.actionMasked == MotionEvent.ACTION_UP }
        private val mMovesObservable = mTouches.filter { ev -> ev.actionMasked == MotionEvent.ACTION_MOVE }

        private var mBitmap: Bitmap? = null
        private var mCanvas: Canvas? = null
        private var mPaint: Paint? = null
        private var action: MouseDragView.Action? = null
        // Used to avoid change current color the a color that is already the current one. This is not actually needed, but is only
        // to demonstrate filter() operator in action.
        var currentBrushColor: Int = Color.BLACK
            private set

        enum class Action {
            PAINT,
            ERASER
        }

        init {

            // This action will inform our draw what to do
            this.action = PAINT

            this.setBackgroundColor(Color.WHITE)
            this.currentBrushColor = Color.BLACK

            // Create a listener for the motion event. This will make our subject to publish mouse events whenever they happen.
            setOnTouchListener { _: View, event: MotionEvent ->
                mTouchSubject.onNext(event)
                true
            }

            // Now it is time to subscribe our observables in order to do something when they receive events. This is where all the reactive magic happens.
            // Actually we will only subscribe the observable interesting in the ACTION_DOWN events. Whenever it happens, it will create a Path object and
            // store the coordinates to it while the ACTION_MOVE events are being received. Once ACTION_UP is received it will stop storing path (due the
            // takeUntil() operator) and will draw the path to the canvas.
            mDownObservable.subscribe { downEvent ->
                val path = Path()
                path.moveTo(downEvent.x, downEvent.y)
                Log.i("$downEvent", "Touch down")

                mMovesObservable
                    .takeUntil(mUpObservable
                        .doOnNext { upEvent ->
                            draw(path)
                            path.close()
                            Log.i(upEvent.toString(), "Touch up")
                        })
                    .subscribe { motionEvent ->
                        path.lineTo(motionEvent.x, motionEvent.y)
                        draw(path)
                        Log.i("$motionEvent", "Touch move")
                    }

            }

            init()
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)

            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            mCanvas = Canvas(mBitmap)
        }

        fun init() {
            mPaint = Paint()
            mPaint?.isDither = true
            mPaint?.color = currentBrushColor
            mPaint?.style = Paint.Style.STROKE
            mPaint?.strokeJoin = Paint.Join.ROUND
            mPaint?.strokeCap = Paint.Cap.ROUND
            mPaint?.strokeWidth = 3f
        }

        private fun draw(path: Path) {
            if (action == PAINT) {
                if (mBitmap == null) {
                    mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    mCanvas = Canvas(mBitmap)
                }

                // When painting, use color chosen by the user in the menu options.
                mPaint?.color = currentBrushColor
                mCanvas?.drawPath(path, mPaint)
                invalidate()

                // When current action is ERASER, change brush color to white in order to give the eraser effect (since this is the same color as
                // the background
            } else if (action == ERASER) {
                if (mBitmap != null) {
                    mPaint?.color = Color.WHITE
                    mCanvas?.drawPath(path, mPaint)
                    invalidate()
                }
            }
        }

        // This is an event called by the framework when we call invalidate().
        public override fun onDraw(canvas: Canvas) {
            if (mBitmap != null) {
                canvas.drawBitmap(mBitmap, 0f, 0f, mPaint)
            }
        }

        // Used when user change brush side in the seekBar view.
        fun setBrushSize(size: Int) {
            mPaint?.strokeWidth = size.toFloat()
        }

        // Called when user change current color form the menu.
        fun changeColor(newColor: Int) {
            currentBrushColor = newColor
            brush()
        }

        // This is called when user chooses to create a new draw
        fun newFile() {
            if (mBitmap != null) {

                mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                mCanvas = Canvas(mBitmap)

                val path = Path()
                mCanvas?.drawPath(path, mPaint)
                invalidate()

                brush()
            }
        }

        // Change the current action to ERASER.
        fun eraser() {
            action = ERASER
        }

        // Change the current action to PAINT.
        fun brush() {
            action = PAINT
        }
    }
}
