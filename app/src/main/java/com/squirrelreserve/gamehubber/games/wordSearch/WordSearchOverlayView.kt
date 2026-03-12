package com.squirrelreserve.gamehubber.games.wordSearch

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.ColorUtils
import kotlin.math.min

class WordSearchOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
): View(context,attrs){
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private var cellSizePx: Float = 0f
    private var dragLine: Line? = null
    private val foundLines = mutableListOf<Line>()
    data class Line(
        val x1: Float, val y1: Float,
        val x2: Float, val y2: Float,
        val color: Int
    )
    fun setCellSize(cellSize: Float){
        cellSizePx = cellSize
        paint.strokeWidth = cellSizePx
        invalidate()
    }
    fun setDragLine(x1: Float, y1: Float, x2: Float, y2: Float, color: Int){
        val translucent = ColorUtils.setAlphaComponent(color, 0x88)
        dragLine = Line(x1,y1,x2,y2,translucent)
        invalidate()
    }
    fun clearDragLine(){
        dragLine = null
        invalidate()
    }
    fun setFoundLines(lines: List<Line>){
        foundLines.clear()
        foundLines.addAll(lines)
        invalidate()
    }
    fun addFoundLine(x1: Float, y1: Float, x2: Float, y2: Float, color: Int){
        val translucent = ColorUtils.setAlphaComponent(color, 0x88)
        foundLines.add(Line(x1,y1,x2,y2,translucent))
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (line in foundLines){
            paint.color = line.color
            canvas.drawLine(line.x1, line.y1, line.x2, line.y2, paint)
        }
        dragLine?.let {
            paint.color = it.color
            canvas.drawLine(it.x1, it.y1, it.x2, it.y2, paint)
        }
    }
}