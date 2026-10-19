package vanguard.game.ui;

/**
 * A rectangle of the pixel screen in the UI kit's coordinates (from the top-left corner, y down): a
 * hit region of the mouse, the same rectangle the screen draws the item in.
 */
public record Box(float x, float y, float width, float height) {
    /** Whether the point lies in the box: its left and top edges count, its right and bottom ones not. */
    public boolean contains(float pointX, float pointY) {
        return pointX >= x && pointX < x + width && pointY >= y && pointY < y + height;
    }

    /**
     * A row of a list or menu: the band {@code step} px high centred on a line of text whose
     * capitals start at {@code textY} and are {@code capHeight} px tall, so the rows of a list touch
     * and every pixel between two items belongs to one of them.
     */
    public static Box row(float x, float width, float textY, float capHeight, float step) {
        return new Box(x, Math.round(textY + capHeight / 2 - step / 2), width, step);
    }
}
