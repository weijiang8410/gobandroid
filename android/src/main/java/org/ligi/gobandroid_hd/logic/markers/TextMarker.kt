/**
 * gobandroid
 * by Marcus -Ligi- Bueschleb
 * http://ligi.de

 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation;

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see //www.gnu.org/licenses/>.

 */

package org.ligi.gobandroid_hd.logic.markers

import android.graphics.Canvas
import android.graphics.Paint

import org.ligi.gobandroid_hd.logic.Cell

/**
 * class to mark a pos on the board useful for go problems - e.g. from SGF
 */
class TextMarker(cell: Cell, val text: String) : GoMarker(cell) {

    override fun equals(other: Any?): Boolean {
        return super.equals(other) && other is TextMarker && other.text.equals(text)
    }

    override fun draw(c: Canvas, size: Float, x: Float, y: Float, paint: Paint) {
        val fm = paint.fontMetrics
        c.drawText(text, x, y + size + (fm.ascent + fm.descent), paint)
    }

}
