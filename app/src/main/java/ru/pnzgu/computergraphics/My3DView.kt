package ru.pnzgu.computergraphics

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.*


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

    var rotationAngleX = (160.0 / 180.0 * PI - PI).toFloat()
        set(value) {
            field = value
            invalidate()
        }
    var rotationAngleY = (160.0 / 180.0 * PI - PI).toFloat()
        set(value) {
            field = value
            invalidate()
        }
    var scaling = 15F
        set(value) {
            field = value
            invalidate()
        }
    var invisible = false
        set(value) {
            field = value
            invalidate()
        }

    private fun transformToComputerAndScale(matrix: List<List<Float>>): List<List<Float>> {
        return matrix dot listOf(
            listOf(scaling, 0F, 0F, 0F),
            listOf(0F, -scaling, 0F, 0F),
            listOf(0F, 0F, scaling, 0F),
            listOf(width / 2F, height / 2F, 0F, 1F)
        )
    }

    private fun rotate(matrix: List<List<Float>>): List<List<Float>> {
        return matrix dot listOf(
            listOf(cos(rotationAngleY), sin(rotationAngleY), 0F, 0F),
            listOf(0F, 1F, 0F, 0F),
            listOf(-sin(rotationAngleY), 0F, cos(rotationAngleY), 0F),
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

    private fun push(matrix: List<List<Float>>, amount: Float) {
        val deepMatrix = matrix.map { (x, y, z, f) -> listOf(x, y, z + amount, f) }
        drawPolygon(executeOrder66(matrix))
        drawPolygon(executeOrder66(deepMatrix))
        for (i in matrix.indices) {
            drawPolygon(
                executeOrder66(
                    listOf(
                        matrix[i],
                        matrix[(i + 1) % matrix.size],
                        deepMatrix[(i + 1) % matrix.size],
                        deepMatrix[i]
                    )
                )
            )
        }
    }

    private lateinit var zBuffer: Array<FloatArray>
    private lateinit var colorBuffer: Array<BooleanArray>

    private fun getZ(point: List<Float>, surfaceParameters: List<Float>): Float {
        val (ax, ay) = point
        val (A, B, C, D) = surfaceParameters
        return (ax * A + ay * B + D) / -C
    }

    private infix fun List<Float>.dot(bVector: List<Float>): Double {
        val (ax, ay) = this
        val (bx, by) = bVector
        return (ax * by).toDouble() - (bx * ay).toDouble()
    }

    private infix fun List<Float>.inside(polygon: List<List<Float>>): Boolean {
        val signed: Double = (polygon[1] - this) dot (polygon[1] - polygon[0])
        for (i in 2 until polygon.size) {
            if (signed * ((polygon[i] - this) dot (polygon[i] - polygon[i - 1])) < 0)
                return false
        }
        return true
    }

    private fun putPixel(x: Int, y: Int, surfaceParameters: List<Float>, color: Boolean) {
        if (x !in 0 until sizeX || y !in 0 until sizeY)
            return
        val pixel = listOf(x.toFloat(), y.toFloat())
        val z = getZ(pixel, surfaceParameters)
        if (zBuffer[x][y] < z || zBuffer[x][y] == z && color) {
            zBuffer[x][y] = z
            colorBuffer[x][y] = color
        }
    }

    private operator fun List<Float>.minus(other: List<Float>): List<Float> {
        if (size == other.size) {
            return mapIndexed { index, it -> it - other[index] }
        }
        return listOf()
    }

    private fun drawLine(a: List<Float>, b: List<Float>, surfaceParameters: List<Float>) {
        val (ix, iy) = b
        val (jx, jy) = a

        val x2 = (if (ix < jx) floor(ix) else ceil(ix)).toInt()
        val y2 = (if (iy < jy) floor(iy) else ceil(iy)).toInt()
        val x1 = (if (jx < ix) floor(jx) else ceil(jx)).toInt()
        val y1 = (if (jy < iy) floor(jy) else ceil(jy)).toInt()
        val dx = abs(x2 - x1)
        val dy = abs(y2 - y1)
        val sx = if (x2 >= x1) 1 else -1
        val sy = if (y2 >= y1) 1 else -1

        if (dy <= dx) {
            var d = dy * 2 - dx
            val d1 = dy * 2
            val d2 = (dy - dx) * 2

            var x = x1 + sx
            var y = y1
            var i = 1

            putPixel(x1, y1, surfaceParameters, true)

            while (i <= dx) {
                if (d > 0) {
                    d += d2
                    y += sy
                } else {
                    d += d1
                }
                putPixel(x, y, surfaceParameters, true)
                i++
                x += sx
            }
        } else {
            var d = dx * 2 - dy
            val d1 = dx * 2
            val d2 = (dx - dy) * 2

            var x = x1
            var y = y1 + sy
            var i = 1

            putPixel(x1, y1, surfaceParameters, true)

            while (i <= dy) {
                if (d > 0) {
                    d += d2
                    x += sx
                } else {
                    d += d1
                }
                putPixel(x, y, surfaceParameters, true)
                i++
                y += sy
            }
        }
    }

    private fun drawPolygon(points: List<List<Float>>) {
        val (ax, ay, az) = points[2] - points[0]
        val (bx, by, bz) = points[2] - points[1]
        val (px, py, pz) = points[2]
        val nx = ay * bz - by * az
        val ny = bx * az - ax * bz
        val nz = ax * by - bx * ay
        val surfaceParameters = listOf(nx, ny, nz, -(nx * px + ny * py + nz * pz))

        if (invisible) {
            for (x in max(
                0,
                floor(points.minOf { it[0] }).toInt()
            )..min(
                sizeX - 1,
                ceil(points.maxOf { it[0] }).toInt()
            )) {
                for (y in max(
                    0,
                    floor(points.minOf { it[1] }).toInt()
                )..min(
                    sizeX - 1,
                    ceil(points.maxOf { it[1] }).toInt()
                )) {
                    val pixel = listOf(x.toFloat(), y.toFloat(), 0F, 1F)
                    if (pixel inside points) {
                        putPixel(x, y, surfaceParameters, false)
                    }
                }
            }
        }

        for (point in 1 until points.size) {
            drawLine(points[point - 1], points[point], surfaceParameters)
        }
    }

    private fun Canvas.zFlush() {
        colorBuffer.forEachIndexed { x, colorRow ->
            colorRow.forEachIndexed { y, color ->
                if (color) drawPoint(x.toFloat(), y.toFloat(), paint)
            }
        }
    }

    private fun cleanBuffer() {
        for (i in zBuffer.indices) {
            for (j in zBuffer[i].indices) {
                zBuffer[i][j] = Float.NEGATIVE_INFINITY
                colorBuffer[i][j] = false
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        zBuffer = Array(sizeX) { FloatArray(sizeY) { Float.NEGATIVE_INFINITY } }
        colorBuffer = Array(sizeX) { BooleanArray(sizeY) { false } }
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        cleanBuffer()

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

            zFlush()
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
}
