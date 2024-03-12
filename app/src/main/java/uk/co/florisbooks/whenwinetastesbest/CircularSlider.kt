package uk.co.florisbooks.whenwinetastesbest

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import org.joda.time.DateTime
import org.joda.time.DateTimeConstants
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import kotlin.math.*


class CircularSlider : View {

    constructor(context: Context?) : super(context) {
        init(context)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private val dayFormat: DateTimeFormatter = DateTimeFormat.forPattern("EEE")

    private var mThumbX: Int = 0
    private var mThumbY: Int = 0

    private var mCircleCenterX: Int = 0
    private var mCircleCenterY: Int = 0
    private var mCircleRadius: Int = 0

    private var mThumbImage: Drawable = ContextCompat.getDrawable(this@CircularSlider.context, R.drawable.handle_pointer)!!
    private var mPadding: Int = 50.toPx
    private var mThumbSize: Int = 0
    private var mThumbColor: Int = 0
    private var mBorderThickness: Int = 100.toPx
    private var mStartAngle: Double = Math.PI / 2
    private var mAngle = mStartAngle
    private var mIsThumbSelected = false

    var isActive: Boolean = true

    private var is24HourTime: Boolean = true

    private val hourLabelFormat = DateTimeFormat.forPattern("H:mm")

    private val mPaintRed = Paint()
    private val mPaintGrey = Paint()
    private val mPaintUnavailable = Paint()
    private val mDashPaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        strokeWidth = 4f
        color = ContextCompat.getColor(this@CircularSlider.context, R.color.wineVanilla)
    }
    private val mTextPaint = TextPaint()
            .apply {
                isAntiAlias = true
                color = ContextCompat.getColor(this@CircularSlider.context, R.color.textBody)
                textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, context.resources.displayMetrics)
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            }

    var mListener: CircleScrollListener? = null

    private val rect = RectF()

    private var arcData: List<WinePeriodType?>? = null

    private var isDayMode: Boolean = true

    private fun init(context: Context?, attributeSet: AttributeSet? = null) {
        context
                ?.obtainStyledAttributes(attributeSet, R.styleable.CircularSlider)
                ?.apply {
                    mThumbSize = getDimensionPixelSize(R.styleable.CircularSlider_thumb_size, 50)
                    mThumbColor = getColor(R.styleable.CircularSlider_thumb_color, Color.GRAY)
                    mBorderThickness = getDimensionPixelSize(R.styleable.CircularSlider_border_thickness, 20)
                    isDayMode = getInt(R.styleable.CircularSlider_mode, 0) == 0
                }
                ?.recycle()

        mPaintRed.color = ContextCompat.getColor(getContext(), R.color.wineRed)
        mPaintRed.style = Paint.Style.STROKE
        mPaintRed.strokeWidth = mBorderThickness.toFloat()
        mPaintRed.isAntiAlias = true

        mPaintGrey.color = ContextCompat.getColor(getContext(), R.color.wineVanilla)
        mPaintGrey.style = Paint.Style.STROKE
        mPaintGrey.strokeWidth = mBorderThickness.toFloat()
        mPaintGrey.isAntiAlias = true

        mPaintUnavailable.color = ContextCompat.getColor(getContext(), R.color.textBody)
        mPaintUnavailable.style = Paint.Style.STROKE
        mPaintUnavailable.strokeWidth = 2.toPx.toFloat()
        mPaintUnavailable.isAntiAlias = true

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val smallerDim = minOf(w, h)

        val largestCenteredSquareLeft = (w - smallerDim) / 2
        val largestCenteredSquareTop = (h - smallerDim) / 2
        val largestCenteredSquareRight = largestCenteredSquareLeft + smallerDim
        val largestCenteredSquareBottom = largestCenteredSquareTop + smallerDim

        mCircleCenterX = largestCenteredSquareRight / 2 + (w - largestCenteredSquareRight) / 2
        mCircleCenterY = largestCenteredSquareBottom / 2 + (h - largestCenteredSquareBottom) / 2
        mCircleRadius = smallerDim / 2 - mBorderThickness / 2 - mPadding

        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas) {
        rect.set((mCircleCenterX - mCircleRadius).toFloat(), (mCircleCenterY - mCircleRadius).toFloat(), (mCircleCenterX + mCircleRadius).toFloat(), (mCircleCenterY + mCircleRadius).toFloat())

        if (isDayMode) {
            drawHourArcs(canvas)
            addLabels(stepSize = 360 / 8,
                    labels = generateHourLabels(),
                    canvas = canvas)
            drawDashes((-180..180 step (360 / 24)), longLineEvery = 3, canvas = canvas)
            addProgressIndicator(canvas)
        } else {
            drawDayArcs(canvas)
            addLabels(stepSize = 360 / 7,
                    labels = generateDayLabels(),
                    canvas = canvas)
            drawWeekDashes(canvas)
            addProgressIndicator(canvas)
        }

        super.onDraw(canvas)
    }

    private fun drawWeekDashes(canvas: Canvas) {

        val start = 90 + (360/ 13.9)
        (0..20)
                .map {
                    start + ((360 / 21.05) * it)
                }
                .map { Math.toRadians(it) }
                .forEachIndexed { index, d ->
                    val length = if (index % 3 == 0) 30.toPx else 20.toPx
                    canvas.drawLine(
                            xForRadius(mCircleRadius + 10.toPx, d),
                            yForRadius(mCircleRadius + 10.toPx, d),
                            xForRadius(mCircleRadius + length, d),
                            yForRadius(mCircleRadius + length, d),
                            mDashPaint)
                }


    }

    private fun generateHourLabels(): List<String> {
        return if (is24HourTime) {
            (0..8)
                    .map { LocalTime.MIDNIGHT.plusHours(3 * it) }
                    .map(hourLabelFormat::print)
        } else {
            resources.getStringArray(R.array.twelve_hour_labels).toList()
        }

    }

    private fun generateDayLabels(): List<String> {
        val startOfWeek = DateTime.now().withTimeAtStartOfDay().withDayOfWeek(DateTimeConstants.MONDAY)

        return (0 until 7)
                .map(startOfWeek::plusDays)
                .map(dayFormat::print)
    }

    private fun drawDayArcs(canvas: Canvas) {
        for (x in 0 until 7) {
            val gapSize = 0.5
            val dataSize = 7.5

            val s = -90 - (360 / (dataSize * 2)) + (x * (360 / (dataSize - gapSize)))
            val length = (360 / dataSize)

            val paint = arcData
                    ?.get(x)
                    ?.let(this::convertToColor)
                    ?: mPaintGrey
            canvas.drawArc(rect, s.toFloat(), length.toFloat(), false, paint)
        }
    }

    private fun drawHourArcs(canvas: Canvas) {
        for (i in 0..23) {
            var length = 15.1
            var start = i * 15

            if (i % 6 == 0) {
                length = 14.1
                start = i * 15 + 1
            }

            start = (start - 90) % 360

            val paint = arcData
                    ?.get(i)
                    ?.let(this::convertToColor)
                    ?: mPaintGrey
            canvas.drawArc(rect, start.toFloat(), length.toFloat(), false, paint)
        }
    }

    private fun drawDashes(dashProgression: IntProgression, longLineEvery: Int, canvas: Canvas) {
        dashProgression
                .map { it.toDouble() }
                .map(Math::toRadians)
                .forEachIndexed { index, d ->
                    val length = if (index % longLineEvery == 0) 30.toPx else 20.toPx
                    canvas.drawLine(
                            xForRadius(mCircleRadius + 10.toPx, d),
                            yForRadius(mCircleRadius + 10.toPx, d),
                            xForRadius(mCircleRadius + length, d),
                            yForRadius(mCircleRadius + length, d),
                            mDashPaint)
                }
    }

    private fun addProgressIndicator(canvas: Canvas) {
        // find thumb position
        mThumbX = (mCircleCenterX + mCircleRadius * cos(mAngle)).toInt()
        mThumbY = (mCircleCenterY - mCircleRadius * sin(mAngle)).toInt()


        canvas.save()
        mThumbImage.setBounds(mThumbX - mThumbSize / 2, mThumbY - mThumbSize / 2, mThumbX + mThumbSize / 2, mThumbY + mThumbSize / 2)
        canvas.rotate(360 - Math.toDegrees(mAngle).toFloat(), mThumbX.toFloat(), mThumbY.toFloat())
        mThumbImage.draw(canvas)
        canvas.restore()
    }

    private fun addLabels(stepSize: Int, labels: List<String>, canvas: Canvas) {
        (90 downTo -270 step stepSize)
                .zip(labels)
                .forEach {
                    canvas.save()
                    val rad = Math.toRadians(it.first.toDouble())
                    val xPoint = xForRadius(mCircleRadius + 40.toPx, rad)
                    val yPoint = yForRadius(mCircleRadius + 40.toPx, rad)
                    var angle = 90 - it.first

                    if (angle in 135..225) {
                        angle += 180
                    }

                    canvas.rotate(angle.toFloat(), xPoint, yPoint)
                    canvas.drawText(it.second,
                            xPoint,
                            yPoint,
                            mTextPaint)
                    canvas.restore()
                }
    }

    private fun xForRadius(radius: Int, angle: Double) = (mCircleCenterX + radius * cos(angle)).toFloat()

    private fun yForRadius(radius: Int, angle: Double) = (mCircleCenterY - radius * sin(angle)).toFloat()

    private fun convertToColor(i: WinePeriodType) = when (i) {
        WinePeriodType.LEAF -> mPaintGrey
        WinePeriodType.FRUIT -> mPaintRed
        WinePeriodType.FLOWER -> mPaintRed
        WinePeriodType.ROOT -> mPaintGrey
        WinePeriodType.SPECIAL_FRUIT -> mPaintGrey
        WinePeriodType.UNFAVOURABLE -> mPaintGrey
        WinePeriodType.NOT_PURCHASED -> mPaintUnavailable
    }

    private var realAngle: Double = 0.toDouble()

    private fun updateSliderState(touchX: Int, touchY: Int) {
        val distanceX = touchX - mCircleCenterX
        val distanceY = mCircleCenterY - touchY
        val c = sqrt(distanceX.toDouble().pow(2.0) + distanceY.toDouble().pow(2.0))
        mAngle = acos(distanceX / c)

        if (distanceY < 0) {
            mAngle = -mAngle
        }

        val previousAngle = realAngle

        realAngle = (mAngle - mStartAngle) / (2 * Math.PI)

        realAngle = when {
            realAngle < 0 -> abs(realAngle)
            else -> 1 - realAngle
        }

        val adjustedPrevious = adjustAngleForOffset(previousAngle)
        val adjustedCurrent = adjustAngleForOffset(realAngle)

        if (adjustedPrevious >= 0.9 && adjustedCurrent < 0.1 && adjustedCurrent > 0) {
            mListener?.onDayAdvanced()
        } else if (adjustedPrevious > 0 && adjustedPrevious < 0.1 && adjustedCurrent >= 0.9) {
            mListener?.onDayReversed()
        }

        mListener?.onAngleChanged(adjustedCurrent)
    }

    private fun adjustAngleForOffset(angle: Double) = if (isDayMode) angle else (angle + (1 / 16f)) % 1


    interface CircleScrollListener {
        fun onDayAdvanced()
        fun onDayReversed()
        fun onAngleChanged(angle: Double)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (isActive) {
            handleTouch(event)
            true
        } else {
            false
        }
    }

    private fun handleTouch(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // start moving the thumb (this is the first touch)
                val x = event.x.toInt()
                val y = event.y.toInt()
                if (x < mThumbX + mThumbSize && x > mThumbX - mThumbSize && y < mThumbY + mThumbSize && y > mThumbY - mThumbSize) {
                    parent.requestDisallowInterceptTouchEvent(true)
                    mIsThumbSelected = true
                    updateSliderState(x, y)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (mIsThumbSelected) {
                    val x = event.x.toInt()
                    val y = event.y.toInt()
                    updateSliderState(x, y)
                }
            }

            MotionEvent.ACTION_UP -> {
                // finished moving (this is the last touch)
                parent.requestDisallowInterceptTouchEvent(false)
                mIsThumbSelected = false
            }
        }

        // redraw the whole component
        invalidate()
    }

    fun bind(values: List<WinePeriodType?>, initialAngle: Double?, is24HourTime: Boolean) {
        this.arcData = values
        this.is24HourTime = is24HourTime
        initialAngle?.let {
            val offset = if (isDayMode) 0 else (360 / 14)
            mAngle = -Math.toRadians((it * 360) - (90 + offset))
        }
        invalidate()
        requestLayout()
    }

    private val Int.toPx: Int
        get() {
            return (this * Resources.getSystem().displayMetrics.density).toInt()
        }

}
