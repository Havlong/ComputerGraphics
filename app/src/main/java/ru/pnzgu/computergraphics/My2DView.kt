package ru.pnzgu.computergraphics

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


class My2DView(context: Context, attributes: AttributeSet) : View(context, attributes) {
    private val paint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
    }

    private val pointMatrix = listOf(
        listOf(28F, 0F, 1F),
        listOf(28F, -8F, 1F),
        listOf(20F, -8F, 1F),
        listOf(16F, 0F, 1F),
        listOf(6F, 0F, 1F),
        listOf(2F, -8F, 1F),
        listOf(-2F, -8F, 1F),
        listOf(-6F, 0F, 1F),
        listOf(-14F, 0F, 1F),
        listOf(-18F, -8F, 1F),
        listOf(-24F, -8F, 1F),
        listOf(-24F, 0F, 1F),
        listOf(-20F, 0F, 1F),
        listOf(-16F, 14F, 1F),
        listOf(10F, 14F, 1F),
        listOf(20F, 4F, 1F),
        listOf(14F, 4F, 1F),
        listOf(10F, 12F, 1F),
        listOf(12F, 12F, 1F),
        listOf(24F, 0F, 1F),
        listOf(-10F, 14F, 1F),
        listOf(6F, 14F, 1F),
        listOf(12F, 4F, 1F),
        listOf(12F, 0F, 1F),
        listOf(-10F, 0F, 1F),
        listOf(-14F, 12F, 1F),
        listOf(8F, 12F, 1F),
        listOf(-18F, 4F, 1F),
        listOf(-10F, -8F, 1F),
        listOf(-6F, -8F, 1F),
        listOf(-4F, -8F, 1F),
        listOf(10F, -8F, 1F),
        listOf(14F, -8F, 1F),
        listOf(16F, -8F, 1F),
        listOf(-9F, 1F, 1F),
        listOf(-9F, 3F, 1F),
        listOf(-3F, 3F, 1F),
        listOf(-3F, 2F, 1F)
    )

    private val car = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 19, 0)
    private val frontWindow = listOf(15, 16, 17, 18)
    private val door = listOf(20, 21, 26, 22, 23, 4, 5, 6, 7, 24, 20)
    private val windows = listOf(25, 26, 22, 27, 25)
    private val wheelL = listOf(28, 29)
    private val wheelLB = listOf(28, 30)
    private val wheelR = listOf(31, 32)
    private val wheelRB = listOf(31, 33)
    private val handle = listOf(34, 35, 36, 37, 34)

    private var sizeX = 0
    private var sizeY = 0

    private var previousX = 0F
    private var previousY = 0F
    private var totalX = 0F
    private var totalY = 0F

    var rotationAngle = 0F
        set(value) {
            field = value
            invalidate()
        }
    var scaling = 1F
        set(value) {
            field = value
            invalidate()
        }

    private fun transformToComputerAndScale(matrix: List<List<Float>>): List<List<Float>> {
        return matrix dot listOf(
            listOf(scaling, 0F, 0F),
            listOf(0F, -scaling, 0F),
            listOf(width / 2F, height / 2F, 1F)
        )
    }

    private fun rotateAndMove(matrix: List<List<Float>>): List<List<Float>> {
        return matrix dot listOf(
            listOf(cos(rotationAngle), sin(rotationAngle), 0F),
            listOf(-sin(rotationAngle), cos(rotationAngle), 0F),
            listOf(totalX / scaling, totalY / scaling, 1F)
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        sizeX = widthSize
        sizeY = heightSize

        setMeasuredDimension(sizeX, sizeY)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val points = transformToComputerAndScale(rotateAndMove(pointMatrix))
        canvas.apply {
            val carPoints = points.slice(car)
            val frontWindowPoints = points.slice(frontWindow)
            val doorPoints = points.slice(door)
            val windowsPoints = points.slice(windows)
            val wheelLPoints = points.slice(wheelL)
            val wheelLBPoints = points.slice(wheelLB)
            val wheelRPoints = points.slice(wheelR)
            val wheelRBPoints = points.slice(wheelRB)
            val handlePoints = points.slice(handle)
            drawPolygon(carPoints)
            drawPolygon(frontWindowPoints)
            drawPolygon(doorPoints)
            drawPolygon(windowsPoints)
            drawPolygon(handlePoints)
            drawCircle(wheelLPoints)
            drawCircle(wheelLBPoints)
            drawCircle(wheelRPoints)
            drawCircle(wheelRBPoints)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                val dx = x - previousX
                val dy = y - previousY
                totalX += dx
                totalY -= dy

                invalidate()
            }
        }

        previousX = x
        previousY = y
        performClick()
        return true
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun Canvas.drawPolygon(points: List<List<Float>>) {
        for (i in (1 until points.size)) {
            val (ax, ay, af) = points[i - 1]
            val (bx, by, bf) = points[i]
            drawLine(ax, ay, bx, by, paint)
        }
    }

    private fun Canvas.drawCircle(points: List<List<Float>>) {
        val (cx, cy, cf) = points[0]
        val (bx, by, bf) = points[1]
        drawCircle(cx, cy, sqrt((bx - cx) * (bx - cx) + (by - cy) * (by - cy)), paint)
    }
}