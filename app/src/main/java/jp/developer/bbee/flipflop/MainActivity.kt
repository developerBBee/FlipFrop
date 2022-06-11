package jp.developer.bbee.flipflop

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import androidx.lifecycle.ViewModelProvider

class MainActivity : AppCompatActivity() {
    val TAG: String = "FlipFlop"
    private lateinit var mModel: MyViewModel
    private lateinit var parentLayout: LinearLayout
    private lateinit var setButton: Button
    private lateinit var resetButton: Button
    private lateinit var v: CustomView

    var xMin: Float = 10000f
    var yMin: Float = 10000f
    var xMax: Float = 0f
    var yMax: Float = 0f
    var w: Int = 0
    var h: Int = 0
    var mRetryCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d(TAG, "onCreate()")

        // Hide StatusBar and NavigationBar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars() or
                    WindowInsets.Type.navigationBars())
            window.insetsController?.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            window.decorView.apply {
                systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
            }
        }

        // lateinit
        // set ViewModel (s, r, q, qb)
        mModel = ViewModelProvider(this).get(MyViewModel::class.java)
        parentLayout = findViewById(R.id.parentLayout)
        setButton = findViewById(R.id.setButton)
        resetButton = findViewById(R.id.resetButton)
        v = CustomView(this) // View of Flip-Flop circuit
        v.layoutParams = ViewGroup.LayoutParams(1031, 908) // Initialize to approximate size

        // initialize
        if (mModel.q == mModel.qb) {
            if (Math.random() > 0.5f) mModel.q = true
            mModel.qb = !mModel.q
        }
        setButtonStyle(setButton, mModel.s)
        setButtonStyle(resetButton, mModel.r)

        mRetryCount = 0
        parentLayout.addView(v)

        // Button Click
        setButton.setOnClickListener {
            if (!mModel.r) {
                mModel.s = !mModel.s
                mModel.q = !(!mModel.s && mModel.qb)
                mModel.qb = !(!mModel.r && mModel.q)
                setButtonStyle(setButton, mModel.s)
                mRetryCount = 0
                v.requestLayout()
            }
        }
        resetButton.setOnClickListener {
            if (!mModel.s) {
                mModel.r = !mModel.r
                mModel.qb = !(!mModel.r && mModel.q)
                mModel.q = !(!mModel.s && mModel.qb)
                setButtonStyle (resetButton, mModel.r)
                mRetryCount = 0
                v.requestLayout()
            }
        }
    }
    
    private fun setButtonStyle(button: Button, isPositive: Boolean) {
        if (isPositive) {
            button.setBackgroundColor(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    getColor(R.color.amber_500)
                } else {
                    Color.RED
                }
            )
            button.setTextColor(Color.BLACK)
        } else {
            button.setBackgroundColor(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    getColor(R.color.purple_500)
                } else {
                    Color.BLUE
                }
            )
            button.setTextColor(Color.WHITE)
        }
    }

    fun layoutSet() {
        if (mRetryCount >= 3) return
        if (w == (xMax+xMin).toInt() && h == (yMax+yMin).toInt()) return
        Log.d(TAG, "layoutSet() update -> xMin=$xMin yMin=$yMin xMax=$xMax yMax=$yMax")
        mRetryCount++
        w = (xMax+xMin).toInt()
        h = (yMax+yMin).toInt()
        parentLayout.removeView(v)
        v.layoutParams = ViewGroup.LayoutParams(w, h)
        parentLayout.addView(v)
    }

    /**
     * CustomView is a flip-flop circuit drawing class
     */
    internal inner class CustomView(context: Context) : View(context) {
        private var mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var mPath: Path = Path()
        private var dp: Float = resources.displayMetrics.density

        // TODO Looks like it doesn't need to be initialized here
        init {
            mPaint.style = Paint.Style.STROKE
            mPaint.strokeWidth = 3*dp
        }

        override fun onConfigurationChanged(newConfig: Configuration?) {
            super.onConfigurationChanged(newConfig)
            // Get dp again just in case
            dp = resources.displayMetrics.density
            Log.d(TAG, "onConfigurationChanged() update -> dp=$dp")
        }

        override fun onDraw(canvas: Canvas?) {
            Log.d(TAG, "onDraw()")

            if (canvas != null) {
                changePaintText(mModel.s)
                canvas.drawText("S",50*dp, 42*dp, mPaint)
                changePaintText(mModel.r)
                canvas.drawText("R",50*dp, 272*dp, mPaint)
                changePaintText(mModel.q)
                canvas.drawText("Q",310*dp, 57*dp, mPaint)
                changePaintText(mModel.qb)
                canvas.drawText("Ǭ",310*dp, 257*dp, mPaint)

                drawBar(canvas, 50*dp, 50*dp, mModel.s) // Input line to Nor
                drawBar(canvas, 150*dp, 50*dp, !mModel.s) // Output line from Nor and 1st input line to Nand
                drawBar(canvas, 150*dp, 80*dp, mModel.qb) // 2nd input line to Nand
                drawVerticalBar(canvas, 150*dp, 80*dp, mModel.qb) // 2nd input line to Nand
                drawDiagonalBar(canvas, 150*dp, 130*dp, true, mModel.qb) // 2nd input line to Nand
                drawBar(canvas, 275*dp, 265*dp, mModel.qb) // Output line from Nand
                drawVerticalBar(canvas, 300*dp, 215*dp, mModel.qb) // Output line from Nand

                drawBar(canvas, 50*dp, 280*dp, mModel.r) // Input line to Nor
                drawBar(canvas, 150*dp, 280*dp, !mModel.r) // Output line from Nor and 1st input line to Nand
                drawBar(canvas, 150*dp, 250*dp, mModel.q) // 2nd input line to Nand
                drawVerticalBar(canvas, 150*dp, 200*dp, mModel.q) // 2nd input line to Nand
                drawDiagonalBar(canvas, 150*dp, 200*dp, false, mModel.q) // 2nd input line to Nand
                drawBar(canvas, 275*dp, 65*dp, mModel.q) // Output line from Nand
                drawVerticalBar(canvas, 300*dp, 65*dp, mModel.q) // Output line from Nand

                drawNorSymbol(canvas, 100*dp, 25*dp)
                drawNandSymbol(canvas, 200*dp, 40*dp)
                drawNorSymbol(canvas, 100*dp, 255*dp)
                drawNandSymbol(canvas, 200*dp, 240*dp)

                layoutSet()
            }
        }

        private fun drawBar(canvas: Canvas, xOffset: Float, yOffset: Float, isPositive: Boolean) {
            changePaintBlackStrokeStyle3dp(isPositive)
            mPath.rewind()
            mPath.moveTo(xOffset, yOffset)
            mPath.lineTo(xOffset+50*dp, yOffset)
            mPath.close()
            canvas.drawPath(mPath, mPaint)

            if (xMin > xOffset) xMin = xOffset
            if (yMin > yOffset) yMin = yOffset
            if (xMax < xOffset+50*dp) xMax = xOffset+50*dp
            if (yMax < yOffset) yMax = yOffset
        }

        private fun drawVerticalBar(canvas: Canvas, xOffset: Float, yOffset: Float, isPositive: Boolean) {
            changePaintBlackStrokeStyle3dp(isPositive)
            mPath.rewind()
            mPath.moveTo(xOffset, yOffset)
            mPath.lineTo(xOffset, yOffset+50*dp)
            mPath.close()
            canvas.drawPath(mPath, mPaint)

            if (xMin > xOffset) xMin = xOffset
            if (yMin > yOffset) yMin = yOffset
            if (xMax < xOffset) xMax = xOffset
            if (yMax < yOffset+50*dp) yMax = yOffset+50*dp
        }

        private fun drawDiagonalBar(canvas: Canvas, xOffset: Float, yOffset: Float,
                                   isBurnDown: Boolean, isPositive: Boolean) {
            var code: Int = -1
            if (isBurnDown) code = 1
            changePaintBlackStrokeStyle3dp(isPositive)
            mPath.rewind()
            mPath.moveTo(xOffset, yOffset)
            mPath.lineTo(xOffset+150*dp, yOffset+code*85*dp)
            mPath.close()
            canvas.drawPath(mPath, mPaint)
        }

        private fun drawNorSymbol(canvas: Canvas, xOffset: Float, yOffset: Float) {
            changePaintBlackStrokeStyle3dp(false)
            mPath.rewind()
            mPath.moveTo(xOffset, yOffset)
            mPath.lineTo(xOffset, yOffset+50*dp)
            mPath.lineTo(xOffset+45*dp, yOffset+25*dp)
            mPath.close()
            canvas.drawPath(mPath, mPaint)

            setCircle(canvas, xOffset+50*dp, yOffset+25*dp)

            if (xMin > xOffset) xMin = xOffset
            if (yMin > yOffset) yMin = yOffset
            if (xMax < xOffset+50*dp) xMax = xOffset+50*dp
            if (yMax < yOffset+50*dp) yMax = yOffset+50*dp
        }

        private fun drawNandSymbol(canvas: Canvas, xOffset: Float, yOffset: Float) {
            changePaintBlackStrokeStyle3dp(false)
            mPath.rewind()
            mPath.moveTo(xOffset+40*dp, yOffset)
            mPath.lineTo(xOffset, yOffset)
            mPath.lineTo(xOffset, yOffset+50*dp)
            mPath.lineTo(xOffset+40*dp, yOffset+50*dp)
            canvas.drawPath(mPath, mPaint)
            canvas.drawArc(xOffset,yOffset,xOffset+70*dp,yOffset+50*dp,90f,-180f,false, mPaint)

            setCircle(canvas, xOffset+75*dp, yOffset+25*dp)

            if (xMin > xOffset) xMin = xOffset
            if (yMin > yOffset) yMin = yOffset
            if (xMax < xOffset+75*dp) xMax = xOffset+75*dp
            if (yMax < yOffset+50*dp) yMax = yOffset+50*dp
        }

        private fun setCircle(canvas: Canvas, xOffset: Float, yOffset: Float) {
            changePaintWhiteFillStyle() // Fill white inner circle
            canvas.drawCircle(xOffset, yOffset, 5*dp, mPaint)
            changePaintBlackStrokeStyle2dp() // Draw circle with black outline
            canvas.drawCircle(xOffset, yOffset, 5*dp, mPaint)
        }

        private fun changePaintWhiteFillStyle() {
            mPaint.color = Color.WHITE
            mPaint.style = Paint.Style.FILL
            mPaint.strokeWidth = 2*dp
        }

        private fun changePaintBlackStrokeStyle2dp() {
            mPaint.color = Color.BLACK
            mPaint.style = Paint.Style.STROKE
            mPaint.strokeWidth = 2*dp
        }

        private fun changePaintBlackStrokeStyle3dp(isPositive: Boolean) {
            mPaint.color = Color.BLACK
            if (isPositive) if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mPaint.color = getColor(R.color.amber_500)
            } else {
                mPaint.color = Color.RED
            }
            mPaint.style = Paint.Style.STROKE
            mPaint.strokeWidth = 3*dp
        }

        private fun changePaintText(isPositive: Boolean) {
            mPaint.color = Color.BLACK
            if (isPositive) if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mPaint.color = getColor(R.color.amber_500)
            } else {
                mPaint.color = Color.RED
            }
            mPaint.style = Paint.Style.FILL
            mPaint.strokeWidth = 1*dp
            mPaint.textSize = 20*dp
        }
    }
}