package ca.ubc.ece.cpen221.ip.core;

/**
 * This datatype represents an <strong>immutable</strong> Rectangle
 * in the positive quadrant of the 2D plane. These rectangles match
 * standard image representations with the coordinate values for the
 * top-left corner being smaller than the coordinate values for the
 * bottom-right corner.
 *
 * After an instance is created, the coordinates of the top-left and bottom-right
 * corners can be accessed using the constant fields
 * <code>xTopLeft, yTopLeft, xBottomRight, yBottomRight</code>.
 */
public class Rectangle {

    public final int xTopLeft, yTopLeft;
    public final int xBottomRight, yBottomRight;

    /*
        Abstraction Function: Represents a rectangle using its top-left corner and bottom-right
        corner. xTopLeft is the x-coordinate of the top-left corner and yTopLeft is the y-coordinate
        of the top-left corner. xBottomRight and yBottom right are the x- and y- coordinates
        of the bottom-right corner.

        Representation Invariant:
            0 <= xTopLeft < xBottomRight
            0 <= yTopLeft < yBottomRight
    */



    /**
     * Create a new Rectangle.
     *
     * @param _xTopLeft: is the x coordinate of the top-left corner and should be >= 0
     * @param _yTopLeft: is the y coordinate of the top-left corner and should be >= 0
     * @param _xBottomRight: is the x coordinate of the bottom-right corner and should be > _xTopLeft
     * @param _yBottomRight: is the y coordinate of the bottom-right corner and should be > _yTopLeft
     */
    public Rectangle(int _xTopLeft, int _yTopLeft, int _xBottomRight, int _yBottomRight) {
        if (_xTopLeft < 0 || _yTopLeft < 0 ||
            _xBottomRight <= _xTopLeft || _yBottomRight <= _yTopLeft) {
            throw new IllegalArgumentException("Invalid rectangle specified.");
        }

        xTopLeft = _xTopLeft;
        yTopLeft = _yTopLeft;
        xBottomRight = _xBottomRight;
        yBottomRight = _yBottomRight;
    }

}
