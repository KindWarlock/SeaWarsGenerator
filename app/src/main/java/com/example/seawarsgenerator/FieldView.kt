package com.example.seawarsgenerator

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlin.random.Random

class FieldView(context: Context, attrs: AttributeSet): View(context, attrs) {
    lateinit var viewCanvas: Canvas
    private val paint = Paint()
    private val paintShip = Paint()

    var squareSize: Int = 0

    val filledPos = ArrayList<Pos>(0)
    val ships = arrayListOf(4, 3, 3, 2, 2, 2, 1, 1, 1, 1)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        paint.style = Paint.Style.STROKE
        paint.color = Color.GRAY
        paint.isAntiAlias = true
        paint.strokeWidth = 5F

        paintShip.style = Paint.Style.FILL
        paintShip.color = Color.BLACK
        paintShip.isAntiAlias = true
        paintShip.strokeWidth = 5F
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        viewCanvas = canvas!!
        Log.d(TAG, "W: ${this.width}, H: ${this.height}")

        for (x in 0 .. FIELD_SIZE) {
            val posX = squareSize * x.toFloat()
            val posY = FIELD_SIZE * squareSize.toFloat()
            canvas.drawLine(posX, 0F, posX, posY, paint)
        }
        for (y in 0 .. FIELD_SIZE) {
            val posY = squareSize * y.toFloat()
            val posX = FIELD_SIZE * squareSize.toFloat()
            canvas.drawLine(0F, posY, posX, posY, paint)
        }

        for (pos in filledPos) {
            drawShip(pos)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = 100

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        val width: Int = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> desiredWidth.coerceAtMost(widthSize)
            else -> desiredWidth
        }

        squareSize = width / 10
        setMeasuredDimension(width, width)
    }

    fun generate() {
        val randomGenerator = Random(System.currentTimeMillis())
        val shipsLeft = ships.toMutableList()
        filledPos.clear()
        outer@ while (shipsLeft.isNotEmpty()) {
            var x = -1
            var y = -1
            var startingPos = Pos(x, y)
            while (!checkField(startingPos)) {
                x = randomGenerator.nextInt(0, FIELD_SIZE)
                y = randomGenerator.nextInt(0, FIELD_SIZE)
                startingPos = Pos(x, y)
            }
            var dir = Direction.values().random()
//            Log.d(TAG, "starting pos: ${startingPos.x}, ${startingPos.y}; dir: $dir")

            val shipPos = arrayListOf<Pos>()
            shipPos.add(startingPos)

            val shipSize = shipsLeft[0]
            val dirChanged = false
            for (i in (1 until shipSize)) {
                var nextPos = Pos(x + dir.x, y + dir.y)
                if (!checkField(nextPos, dir) && !dirChanged) {
                    Log.d(TAG, "$i, False")
                    dir = Direction.from(dir.x * -1, dir.y * -1)
                    x = startingPos.x
                    y = startingPos.y
//                    nextPos = Pos(x + i * dir.x, y + i * dir.y)
                    nextPos = Pos(x + dir.x, y + dir.y)
                }

                if (!checkField(nextPos, dir)) {
                    if (shipsLeft.contains(i)) {
                        break
                    }
                    Log.d(TAG, "RETURNING")
                    continue@outer
                }

                shipPos.add(nextPos)
                x = nextPos.x
                y = nextPos.y
            }

            filledPos += shipPos
            Log.d(TAG, "${startingPos.x};${startingPos.y} , $dir, " +
                    "expected: $shipSize, true: ${shipPos.size}")
            Log.d(TAG, "$shipPos")
            shipsLeft.remove(shipPos.size)
        }
        invalidate()
    }

    private fun checkField(pos: Pos, dir: Direction? = null): Boolean {
        val isGreater = pos.x > FIELD_SIZE - 1 || pos.y > FIELD_SIZE - 1
        val isLess = pos.x < 0 || pos.y < 0
        val isPresent = filledPos.contains(pos)

        // Field itself
        if (isGreater || isLess || isPresent) {
            return false
        }

        // Neighbours
        val neighbours = arrayListOf(
            Pos(pos.x - 1, pos.y - 1),
            Pos(pos.x, pos.y - 1),
            Pos(pos.x + 1, pos.y - 1),
            Pos(pos.x - 1, pos.y),
            Pos(pos.x + 1, pos.y),
            Pos(pos.x - 1, pos.y + 1),
            Pos(pos.x, pos.y + 1),
            Pos(pos.x +1, pos.y + 1)
        )
        when (dir) {
            Direction.UP -> neighbours.removeAt(6)
            Direction.DOWN -> neighbours.removeAt(1)
            Direction.LEFT -> neighbours.removeAt(4)
            Direction.RIGHT -> neighbours.removeAt(3)
            else -> {}
        }
        for (n in neighbours) {
            if (filledPos.contains(n)) {
                return false
            }
        }
        return true
    }

    fun drawShip(pos: Pos) {
        val left = pos.x * squareSize + paint.strokeWidth / 2
        val top = pos.y * squareSize + paint.strokeWidth / 2
        val right = (pos.x + 1) * squareSize - paint.strokeWidth / 2
        val bottom = (pos.y + 1) * squareSize - paint.strokeWidth / 2
        viewCanvas.drawRect(left, top, right, bottom, paintShip)
    }

    companion object {
        const val TAG = "TAG"
        const val FIELD_SIZE = 10
    }

    enum class Direction (val x: Int, val y: Int) {
        UP(0, -1),
        DOWN(0, 1),
        LEFT(-1, 0),
        RIGHT(1, 0);

        companion object {
            fun from(findX: Int, findY: Int): Direction = values().first {
                it.x == findX && it.y == findY
            }
        }
    }
}