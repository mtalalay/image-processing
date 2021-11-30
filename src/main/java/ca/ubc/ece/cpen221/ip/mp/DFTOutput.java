package ca.ubc.ece.cpen221.ip.mp;

import ca.ubc.ece.cpen221.ip.core.DoubleMatrix;
import ca.ubc.ece.cpen221.ip.core.Image;
import ca.ubc.ece.cpen221.ip.mp.ImageTransformer;
import ca.ubc.ece.cpen221.ip.core.ImageProcessingException;
import ca.ubc.ece.cpen221.ip.core.Rectangle;

/**
 * This datatype represents the output of a spatial Discrete Fourier Transform,
 * and holds the amplitude and phase matrix obtained from the DFT.
 */
public class DFTOutput {
    private DoubleMatrix amplitude;
    private DoubleMatrix phase;

    /*
        Abstraction Function:
            Represents the output of the (spatial) DFT applied to an image.
            amplitude is a matrix that represents the amplitude portion of the DFT output.
            phase is a matrix that represents the phase portion of the DFT output.

        Note:
            A DFT can usually be represented by a matrix of complex numbers but
            we use the amplitude & phase representation using two matrices.

        Representation Invariant:
            amplitude.width == phase.width
            amplitude.height == phase.width
     */

    /**
     * Create a new DFTOutput instance.
     *
     * @param _amplitude is not null
     * @param _phase     is not null, and is equal in dimensions to _amplitude
     */
    public DFTOutput(double[][] _amplitude, double[][] _phase) {
        amplitude = new DoubleMatrix(_amplitude);
        phase = new DoubleMatrix(_phase);
        if (amplitude.columns != phase.columns || amplitude.rows != phase.rows) {
            throw new IllegalArgumentException(
                "amplitude and phase matrices should have the same dimensions"
            );
        }
    }

    public Image amplitudeToImage() {
        Image amplitudeImg = new Image(amplitude.columns, amplitude.rows);
        int[] color = new int[4];
        color[0] = 255;
        int rgb;
        double c;
        double max = 0.0;

        for (int col = 0; col < amplitude.columns; col++) {
            for (int row = 0; row < amplitude.rows; row++) {
                 if(max < amplitude.get(row,col)) {
                     max = amplitude.get(row,col);
                 }
            }
        }

        c=255/(Math.log(1+max));

        for (int col = 0; col < amplitude.columns; col++) {
            for (int row = 0; row < amplitude.rows; row++) {
                for(int i = 1; i <color.length; i++) {
                    color[i] = (int)Math.round(c*Math.log(1 + amplitude.get(row,col)));
                }
                rgb = ImageTransformer.mergedColor(color);
                amplitudeImg.setRGB(col,row,rgb);
            }
        }
        return amplitudeImg;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DFTOutput)) {
            return false;
        }
        DFTOutput other = (DFTOutput) o;
        return amplitude.equals(other.amplitude) && phase.equals(other.phase);
    }

    @Override
    public int hashCode() {
        return amplitude.hashCode();
    }
}
