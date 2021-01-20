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


class My3DView(context: Context, attributes: AttributeSet) : View(context, attributes) {
    private val paint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
    }

    private val pointMatrix = listOf(
        listOf(28F, 0F, 0F, 1F),
        listOf(28F, -8F, 0F, 1F),
        listOf(18F, -8F, 0F, 1F),
        listOf(14F, 0F, 0F, 1F),
        listOf(6F, 0F, 0F, 1F),
        listOf(2F, -8F, 0F, 1F),
        listOf(-2F, -8F, 0F, 1F),
        listOf(-6F, 0F, 0F, 1F),
        listOf(-14F, 0F, 0F, 1F),
        listOf(-18F, -8F, 0F, 1F),
        listOf(-24F, -8F, 0F, 1F),
        listOf(-24F, 0F, 0F, 1F),
        listOf(-20F, 0F, 0F, 1F),
        listOf(-16F, 14F, 0F, 1F),
        listOf(10F, 14F, 0F, 1F),
        listOf(24F, 0F, 0F, 1F),
        listOf(18F, -10F, 0F, 1F),
        listOf(14F, -2F, 0F, 1F),
        listOf(6F, -2F, 0F, 1F),
        listOf(2F, -10F, 0F, 1F),
        listOf(6F, -18F, 0F, 1F),
        listOf(14F, -18F, 0F, 1F),
        listOf(-2F, -10F, 0F, 1F),
        listOf(-6F, -2F, 0F, 1F),
        listOf(-14F, -2F, 0F, 1F),
        listOf(-18F, -10F, 0F, 1F),
        listOf(-14F, -18F, 0F, 1F),
        listOf(-6F, -18F, 0F, 1F)
    )

    private val car = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 0)
    private val lWheel = listOf(16, 17, 18, 19, 20, 21, 16)
    private val rWheel = listOf(22, 23, 24, 25, 26, 27, 22)

    private var sizeX = 0
    private var sizeY = 0

    private var previousX = 0F
    private var previousY = 0F
    private var totalX = 0F
    private var totalY = 0F

    var rotationAngleX = 0F
        set(value) {
            field = value
            invalidate()
        }
    var rotationAngleY = 0F
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
            listOf(scaling, 0F, 0F, 0F),
            listOf(0F, -scaling, 0F, 0F),
            listOf(0F, 0F, 0F, 0F),
            listOf(width / 2F, height / 2F, 0F, 1F)
        )
    }

    private fun rotate(matrix: List<List<Float>>): List<List<Float>> {
        return matrix dot listOf(
            listOf(cos(rotationAngleY), -sin(rotationAngleY), 0F, 0F),
            listOf(0F, 1F, 0F, 0F),
            listOf(sin(rotationAngleY), 0F, cos(rotationAngleY), 0F),
            listOf(0F, 0F, 0F, 1F)
        ) dot listOf(
            listOf(1F, 0F, 0F, 0F),
            listOf(0F, cos(rotationAngleX), sin(rotationAngleX), 0F),
            listOf(0F, -sin(rotationAngleX), cos(rotationAngleX), 0F),
            listOf(0F, 0F, 0F, 1F)
        )
    }

    private fun move(matrix: List<List<Float>>): List<List<Float>> {
        return matrix dot listOf(
            listOf(1F, 0F, 0F, 0F),
            listOf(0F, 1F, 0F, 0F),
            listOf(0F, 0F, 1F, 0F),
            listOf(totalX, totalY, 0F, 1F)
        )
    }

    private fun executeOrder66(matrix: List<List<Float>>): List<List<Float>> {
        return move(transformToComputerAndScale(rotate(matrix)))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        sizeX = widthSize
        sizeY = heightSize

        setMeasuredDimension(sizeX, sizeY)
    }

    private fun Canvas.push(matrix: List<List<Float>>, amount: Float) {
        val deepMatrix = matrix.map { (x, y, z, f) -> listOf(x, y, z + amount, f) }
        drawPolygon(executeOrder66(matrix))
        drawPolygon(executeOrder66(deepMatrix))
        for (i in matrix.indices) {
            drawPolygon(executeOrder66(listOf(matrix[i], deepMatrix[i])))
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.apply {
            val carMatrix = pointMatrix.slice(car)
            push(carMatrix, -40F)
            val lWheelMatrix = pointMatrix.slice(lWheel)
            val rWheelMatrix = pointMatrix.slice(rWheel)
            val deepLWheel = lWheelMatrix.map { (x, y, z, f) -> listOf(x, y, z - 40F, f) }
            val deepRWheel = rWheelMatrix.map { (x, y, z, f) -> listOf(x, y, z - 40F, f) }
            push(lWheelMatrix, -5F)
            push(rWheelMatrix, -5F)
            push(deepLWheel, 5F)
            push(deepRWheel, 5F)

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
                totalY += dy

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
            val (ax, ay) = points[i - 1]
            val (bx, by) = points[i]
            drawLine(ax, ay, bx, by, paint)
        }
    }
}