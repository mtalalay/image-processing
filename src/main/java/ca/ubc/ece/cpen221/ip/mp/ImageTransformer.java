package ca.ubc.ece.cpen221.ip.mp;

import ca.ubc.ece.cpen221.ip.core.Image;
import ca.ubc.ece.cpen221.ip.core.ImageProcessingException;
import ca.ubc.ece.cpen221.ip.core.Rectangle;

import java.awt.Point;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import static java.util.Arrays.sort;
import java.util.List;

/**
 * This datatype (or class) provides operations for transforming an image.
 *
 * <p>The operations supported are:
 * <ul>
 *     <li>The {@code ImageTransformer} constructor generates an instance of an image that
 *     we would like to transform;</li>
 *     <li></li>
 * </ul>
 * </p>
 */

public class ImageTransformer {

    private Image image;
    private int width;
    private int height;

    /**
     * Creates an ImageTransformer with an image. The provided image is
     * <strong>never</strong> changed by any of the operations.
     *
     * @param img is not null
     */
    public ImageTransformer(Image img) {
        this.image = img;
        width = img.width();
        height = img.height();
    }

    /**
     * Obtain the grayscale version of the image.
     *
     * @return the grayscale version of the instance.
     */
    public Image grayscale() {
        Image gsImage = new Image(width, height);
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                Color color = image.get(col, row);
                Color gray = Image.toGray(color);
                gsImage.set(col, row, gray);
            }
        }
        return gsImage;
    }

    /**
     * Obtain a version of the image with only the red colours.
     *
     * @return a reds-only version of the instance.
     */
    public Image red() {
        Image redImage = new Image(width, height);
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                int originalPixel = image.getRGB(col, row);
                int alpha = (originalPixel >> 24) & 0xFF;
                int red = (originalPixel >> 16) & 0xFF;
                int desiredColor = (alpha << 24) | (red << 16) | (0 << 8) | (0);
                redImage.setRGB(col, row, desiredColor);
            }
        }
        return redImage;
    }

    /**
     * Returns the mirror image of an instance.
     *
     * @return the mirror image of the instance.
     */
    public Image mirror() {
        Image mirrorImage = new Image(width, height);
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                mirrorImage.setRGB(col, row, image.getRGB(width - 1 - col, row));
            }
        }
        return mirrorImage;
    }

    /**
     * <p>Returns the negative version of an instance.<br />
     * If the colour of a pixel is (r, g, b) then the colours of the same pixel
     * in the negative of the image are (255-r, 255-g, 255-b).</p>
     *
     * @return the negative of the instance.
     */
    public Image negative() {
        Image negative = new Image(width, height);
        int col;
        int row;
        for (col = 0; col < width; col++) {
            for (row = 0; row < height; row++) {
                negative.setRGB(col, row, negativeColor(col, row));
            }
        }
        return negative;
    }

    /**
     * @return the colour of the pixel modified in the negative of an image
     */
    private int negativeColor(int col, int row) {
        int[] pixelColour = getBytes(col, row);
        int negativeConstant = 255;

        for (int i = 0; i < pixelColour.length; i++) {
            pixelColour[i] = negativeConstant - pixelColour[i];
        }

        return mergedColor(pixelColour);
    }

    /**
     * <p>Returns the posterized version of an instance.<br />
     * For each pixel, each colour is analyzed independently to produce a new image as follows:
     * <ul>
     * <li>if the value of the colour is between 0 and 64 (limits inclusive), set it to 32;</li>
     * <li>if the value of the colour is between 65 and 128, set it to 96;</li>
     * <li>if the value of the colour is between 129 and 255, set it to 222.</li>
     * </ul>
     * </p>
     *
     * @return the posterized version of the instance.
     */
    public Image posterize() {
        Image output = new Image(width, height);
        int col;
        int row;
        for (row = 0; row < height; row++) {
            for (col = 0; col < width; col++) {
                output.setRGB(col, row, posterizedColor(col, row));
            }
        }
        return output;
    }

    /**
     * @param col the column of the pixel in question
     * @param row the row of the pixel in question
     * @return the posterized version of the pixel
     */
    private int posterizedColor(int col, int row) {
        int[] pixel = getBytes(col, row);
        int length = 4;
        int i;
        final int lowerCutoff = 64;
        final int midCutoff = 128;
        final int lowerAvg = 32;
        final int midAvg = 96;
        final int upperAvg = 222;

        for (i = 1; i < length; i++) {
            if (0 <= pixel[i] && pixel[i] <= lowerCutoff) {
                pixel[i] = lowerAvg;
            } else if (lowerCutoff < pixel[i] && pixel[i] <= midCutoff) {
                pixel[i] = midAvg;
            } else {
                pixel[i] = upperAvg;
            }
        }
        return mergedColor(pixel);
    }

    /**
     * Splits the RGB integer into its 4 values: alpha, red, green, blue
     *
     * @param col the column index
     * @param row the row index
     * @return deciRGB [alpha,red,green,blue]
     */
    public int[] getBytes(int col, int row) {
        int numOfBytes = 4;
        byte[] alphaRGB = new byte[numOfBytes];
        int rgb = image.getRGB(col, row);
        int i;
        int discard = 0;
        final int byteSize = 8;

        for (i = numOfBytes - 1; i >= 0; i--, discard += byteSize) {
            alphaRGB[i] = (byte) ((rgb >> discard) & 0xFF);
        }

        int[] deciRGB = new int[numOfBytes];
        for (i = 0; i < alphaRGB.length; i++) {
            deciRGB[i] = alphaRGB[i];
            if (deciRGB[i] < 0) {
                deciRGB[i] += 256;
            }
        }
        return deciRGB;
    }

    /**
     * Merges the alphaRGB array back into a normal RGB value
     *
     * @param deciRGB [alpha,red,green,blue]
     * @return one RGB value
     */
    public static int mergedColor(int[] deciRGB) {
        int i;
        int rgb = 0;

        for (i = 0; i < deciRGB.length; i++) {
            rgb = rgb << 8;
            rgb += ((byte) deciRGB[i]);
            if ((byte) deciRGB[i] < 0) {
                rgb += 256;
            }
        }

        return rgb;
    }

    /**
     * Clip the image given a rectangle that represents the region to be retained.
     *
     * @param clippingBox is not null.
     * @return a clipped version of the instance.
     * @throws ImageProcessingException if the clippingBox does not fit completely
     *                                  within the image.
     */
    public Image clip(Rectangle clippingBox) throws ImageProcessingException {
        int rectangleWidth = clippingBox.xBottomRight - clippingBox.xTopLeft + 1;
        int rectangleHeight = clippingBox.yBottomRight - clippingBox.yTopLeft + 1;
        Image clippedImage = new Image(rectangleWidth, rectangleHeight);
        if (clippingBox.xBottomRight > width || clippingBox.yBottomRight > height) {
            throw new ImageProcessingException();
        }
        for (int row = 0; row < rectangleHeight; row++) {
            for (int col = 0; col < rectangleWidth; col++) {
                clippedImage.setRGB(col, row,
                    image.getRGB(clippingBox.xTopLeft + col, row + clippingBox.yTopLeft));
            }
        }
        return clippedImage;
    }

    /**
     * Denoise an image by replacing each pixel by the median value of that pixel and
     * all its neighbouring pixels. During this process, each colour channel is handled
     * separately.
     *
     * @return a denoised version of the instance.
     */
    public Image denoise() {
        Image denoisedImage = new Image(width, height);
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                denoisedImage.setRGB(col, row, denoisedColour(col, row));
            }
        }
        denoisedImage.show();
        return denoisedImage;
    }

    /**
     * @param col
     * @param row
     * @return denoised colour from a given pixel
     */
    private int denoisedColour(int col, int row) {
        int length = 4;
        int[] pixel = new int[length];
        int[] neighbors = getNeighbourPixels(col, row);
        for (int i = 0; i < length; i++) {
            pixel[i] = getMedian(neighbors, i);
        }
        return mergedColor(pixel);
    }

    /**
     * @param col
     * @param row
     * @return an array of integers that constitutes of all 4 RGB elements of each neighbor pixel
     * slots 0 - 3 describe first pixel, 4 - 7 second pixel... etc
     */
    private int[] getNeighbourPixels(int col, int row) {
        int i = 0;
        int length = 4;
        int count;
        // Corner cases will only have 4 neighbors
        if ((col - 1 < 0 && row - 1 < 0) || (col - 1 < 0 && row + 1 > height) ||
            (col + 1 > width && row - 1 < 0) || (col + 1 > width && row + 1 > height)) {
            count = 3 * length;
        }
        // Not corner but close to boundaries will have 5 neighbors
        else if (col - 1 < 0 || col + 1 > width || row - 1 < 0 || row + 1 > height) {
            count = 5 * length;
        } else {
            count = 8 * length;
        }
        int[] neighbourPixels = new int[count];
        for (int C = col - 1; C <= col + 1; C++) {
            for (int R = row - 1; R <= row + 1; R++) {
                //avoiding pixels that are out of limit
                if (C >= 0 && R >= 0 && C < width && R < height) {
                    // we dont want to compute the pixel itself, just its neighbors
                    if (C != col || R != row) {
                        int[] pixel = getBytes(C, R);
                        for (int j = 0; j < length; j++) {
                            neighbourPixels[i] = pixel[j];
                            i++;
                        }
                    }
                }
            }
        }
        return neighbourPixels;
    }

    /**
     * @param array
     * @param multiplier multiplier determines which component of color we get. Varies between 0 - 3, follows
     *                   predefined order alpha, red, green, blue.
     * @return The median of an array, for this purpose should use once for each colour in RGB
     */
    private int getMedian(int[] array, int multiplier) {
        int length = 4;
        int count = 0;
        // array.length over length because we want to compute once per RGB attribute
        int[] increasingOrderArray = new int[array.length / length];

        // Paste each neighbor attribute according to multiplier
        for (int i = multiplier; i < array.length; i += length) {
            increasingOrderArray[count] = array[i];
            count++;
        }
        // Gets them in increasing order
        sort(increasingOrderArray, 0, array.length / length);

        //Compute median
        if (increasingOrderArray.length % 2 == 0) {
            return (increasingOrderArray[(increasingOrderArray.length / 2) - 1] +
                increasingOrderArray[increasingOrderArray.length / 2]) / 2;
        } else {
            return increasingOrderArray[increasingOrderArray.length / 2];
        }
    }

    /**
     * Returns a weathered version of the image by replacing each pixel by the minimum value
     * of that pixel and all its neighbouring pixels. During this process, each colour channel
     * is handled separately.
     *
     * @return a weathered version of the image.
     */
    public Image weather() {
        Image output = new Image(width, height);
        int col;
        int row;
        for (row = 0; row < height; row++) {
            for (col = 0; col < width; col++) {
                output.setRGB(col, row, weatheredColor(col, row));
            }
        }
        return output;
    }

    /**
     * Decides how many neighboring pixels are present and returns the weathered version of the pixel
     *
     * @param col column of the pixel being checked
     * @param row row of the pixel being checked
     * @return the most weathered alpha,R,G, and B values fo the surrounding pixels
     */
    private int weatheredColor(int col, int row) {
        int[] pixel = getBytes(col, row);
        int length = 4;
        int[] check;

        for (int i = row - 1; i <= row + 1; i++) {
            for (int j = col - 1; j <= col + 1; j++) {
                if (i < 0 || i >= height || j < 0 || j >= width) {
                    continue;
                } else {
                    check = getBytes(j, i);
                    compareWeather(pixel, check);
                }
            }
        }
        return mergedColor(pixel);
    }

    /**
     * Compares two pixels and returns the most weatherd color values
     *
     * @param pixel the pixel being modified
     * @param check the pixel it is being compared to
     * @return the array of weathered color values
     */
    private int[] compareWeather(int[] pixel, int[] check) {
        int i;
        int length = pixel.length;
        for (i = 0; i < length; i++) {
            if (check[i] < pixel[i]) {
                pixel[i] = check[i];
            }
        }
        return pixel;
    }

    /**
     * Return a block paint version of the instance by treating the image as a
     * sequence of squares of a given size and replacing all pixels in a square
     * by the average value of all pixels in that square.
     * During this process, each colour channel is handled separately.
     *
     * @param blockSize the dimension of the square block, > 1.
     * @return the block paint version of the instance.
     * When the original image is not a perfect multiple of blockSize * blockSize,
     * the bottom rows and right columns are obtained by averaging the pixels that
     * fit the smaller rectangular regions. For example, if we have a 642 x 642 size
     * original image and the block size is 4 x 4 then the bottom two rows will use
     * 2 x 4 blocks, the rightmost two columns will use 4 x 2 blocks, and the
     * bottom-right corner will use a 2 x 2 block.
     */
    public Image blockPaint(int blockSize) {
        Image blockImage = new Image(width, height);

        for (int row = 0; row < height; row += blockSize) {
            for (int col = 0; col < width; col += blockSize) {
                averageOfNeighbor(blockSize, blockImage, col, row);
            }
        }
        return blockImage;
    }

    /**
     * @param m          the dimension of the square block, >1.
     * @param blockImage the image that we are converting into block paint.
     * @param col        the column we are at.
     * @param row        the row we are at.
     * @return blockImage with the block at position (row,col) converted into block paint of size m.
     */
    private Image averageOfNeighbor(int m, Image blockImage, int col, int row) {
        int arraySize = 4;
        int[] color;
        int[] averageArray = new int[arraySize];
        int averageInt = 0;
        int count = 0;
        for (int i = row; i < row + m; i++) {
            for (int j = col; j < col + m; j++) {
                if (i < 0 || i >= height || j < 0 || j >= width) {
                    continue;
                } else {
                    count++;
                    color = getBytes(j, i);
                    for (int k = 0; k < arraySize; k++) {
                        averageArray[k] += color[k];
                    }
                }
            }
        }
        for (int l = 0; l < arraySize; l++) {
            averageArray[l] /= count;
            averageInt = mergedColor(averageArray);
        }
        for (int i = row; i < row + m; i++) {
            for (int j = col; j < col + m; j++) {
                if (i < 0 || i >= height || j < 0 || j >= width) {
                    continue;
                } else {
                    blockImage.setRGB(j, i, averageInt);
                }
            }
        }
        return blockImage;
    }


    /**
     * Rotate an image by the given angle (degrees) about the centre of the image.
     * The centre of an image is the pixel at (width/2, height/2). The new regions
     * that may be created are given the colour white (<code>#ffffff</code>) with
     * maximum transparency (alpha = 255).
     *
     * @param degrees the angle to rotate the image by, 0 <= degrees <= 360.
     * @return a rotate version of the instance.
     */
    public Image rotate(double degrees) {
        double new_width = Math.abs(Math.sin(degrees * Math.PI / 180)) * height +
            Math.abs(Math.cos(degrees * Math.PI / 180)) * width;
        double new_height = Math.abs(Math.sin(degrees * Math.PI / 180)) * width +
            Math.abs(Math.cos(degrees * Math.PI / 180)) * height;
        Image outImage = new Image((int) Math.round(new_width), (int) Math.round(new_height));

        for (int col = 0; col < (int) Math.round(new_width); col++) {
            for (int row = 0; row < (int) Math.round(new_height); row++) {
                int original_x = (int) ((col - new_width / 2) * Math.cos(degrees * Math.PI / 180) +
                    (row - new_height / 2) * Math.sin(degrees * Math.PI / 180) + width / 2);
                int original_y = (int) (-(col - new_width / 2) * Math.sin(degrees * Math.PI / 180) +
                    (row - new_height / 2) * Math.cos(degrees * Math.PI / 180) + height / 2);
                if (original_x >= 0 && original_y >= 0 &&
                    original_x < width &&
                    original_y < height) {
                    outImage.set(col, row, image.get(original_x, original_y));
                } else {
                    outImage.set(col, row, Color.WHITE);
                }
            }
        }
        return outImage;
    }

    /**
     * Compute the discrete Fourier transform of the image and return the
     * amplitude and phase matrices as a DFTOutput instance.
     *
     * @return the amplitude and phase of the DFT of the instance.
     */
    public DFTOutput dft() {
        ImageTransformer grayImg = new ImageTransformer(this.grayscale());

        int x;
        int y;
        int u;
        int v;
        double theta;
        double realSum = 0;
        double iSum = 0;
        int intensity;
        double[][] amplitude = new double[height][width];
        double[][] phase = new double[height][width];

        for (u = 0; u < height; u++) {
            for (v = 0; v < width; v++) {
                for (x = 0; x < height; x++) {
                    for (y = 0; y < width; y++) {
                        theta = 2.0 * Math.PI * (u * x / (double) height + v * y / (double) width);
                        int[] pixel = grayImg.getBytes(y, x);
                        intensity = pixel[1];
                        realSum += intensity * (Math.cos(theta));
                        iSum += intensity * (Math.sin(theta));
                    }
                }
                amplitude[u][v] = Math.sqrt(Math.pow(realSum, 2) + Math.pow(iSum, 2));
                if(realSum == 0 || iSum == 0) {
                    phase[u][v] = 0;
                } else {
                    phase[u][v] = Math.atan(iSum / realSum);
                }
                realSum = 0;
                iSum = 0;
            }
        }
        System.out.println();
        return new DFTOutput(amplitude, phase);
    }

    /**
     * Filters a DFT image to make whitePercent of the pixels white
     * @return
     */
    private Image filter() {
        Image output = new Image(width, height);
        int area = width * height;
        double whitePercent = .0022222;
        int count = 0;
        int threshold = 190;

        int[] pixel;
        int col;
        int row;
        while (count < (int) Math.round(area * whitePercent)) {
            for (col = 0; col < width; col++) {
                for (row = 0; row < height; row++) {
                    pixel = getBytes(col, row);
                    if (pixel[1] > threshold) {
                        pixel[0] = 255;
                        pixel[1] = 255;
                        pixel[2] = 255;
                        pixel[3] = 255;
                        if (output.getRGB(col, row) != mergedColor(pixel)) {
                            count++;
                        }
                        output.setRGB(col, row, mergedColor(pixel));
                    } else {
                        pixel[0] = 255;
                        pixel[1] = 0;
                        pixel[2] = 0;
                        pixel[3] = 0;
                        output.setRGB(col, row, mergedColor(pixel));
                    }
                }
            }
            threshold--;
        }
        return output;
    }

    /**
     * Replaces a background screen with a provided image.
     * <p>
     * This operation identifies the largest connected region of the image that matches
     * <code>screenColour</code> exactly. This operation determines a rectangle that bounds
     * the "green screen" region and overlays the <code>backgroundImage</code> over that
     * rectangle by aligning the top-left corner of the image with the top-left corner of the
     * rectangle. After determining the screen region, all pixels in that region matching
     * <code>screenColour</code> are replaced with corresponding pixels from
     * <code>backgroundImage</code>.
     * <p>
     * If <code>backgroundImage</code> is smaller
     * than the screen then the image is tiled over the screen.
     *
     * @param screenColour    the colour of the background screen, is not null
     * @param backgroundImage the image to replace the screen with, is not null
     * @return an image with provided image replacing the background screen
     * of the specified colour, tiling the screen with the background image if the
     * background image is smaller than the screen size.
     */
    public Image greenScreen(Color screenColour, Image backgroundImage) {
        ArrayList<Set> regionSets = new ArrayList<>();
        boolean flag;
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                flag = true;
                if (image.get(col, row).equals(screenColour)) {
                    for (Set<Point> S : regionSets) {
                        for (Point P : S) {
                            if (P.x == col && P.y == row) {
                                flag = false;
                            }
                        }
                    }
                    if (flag) {
                        Set<Point> NeighbourPixelsSameColour = new HashSet<>();
                        NeighbourPixelsSameColour.add( new Point( col, row ));
                        checkNeighbours(col, row, screenColour, NeighbourPixelsSameColour);
                        regionSets.add(NeighbourPixelsSameColour);
                    }
                }
            }
        }

        Set<Point> smallSet = new HashSet<>();
        Set<Point> bigSet = new HashSet<>();
        for (int k = 0; k < regionSets.size(); k++) {
            for (int l = k + 1; l < regionSets.size(); l++) {
                if ((regionSets.get(k)).size() > (regionSets.get(l)).size()) {
                    smallSet = regionSets.get(l);
                    bigSet = regionSets.get(k);
                    regionSets.set(k, smallSet);
                    regionSets.set(l, bigSet);
                }
            }
        }
        Set<Point> biggestRegion = regionSets.get(regionSets.size() - 1);
        Rectangle bigRectangle = makeRectangle(biggestRegion);
        Image output = fitBackground( bigRectangle, backgroundImage, screenColour );
        return output;
    }

    /**
     * This method will go through all neighbour pixels of a given pixel to find every other one
     * that is connected and has the same colour of that first one.
     * @param col
     * @param row
     * @param screenColour
     * @return A Set of arrays of integers storing Column (pos 0) and Row (pos 1) data for each
     * pixel that has the same colour
     */
    private void checkNeighbours (int col, int row, Color screenColour, Set<Point> NeighbourPixelsSameColour ) {
        Point P = new Point( col, row );
        for ( int C = col - 1; C <= col + 1; C++ ) {
            for ( int R = row - 1; R <= row + 1; R++ ) {
                //avoiding pixels that are out of limit
                if ( C >= 0 && R >= 0 && C < width && R < height ) {
                    if( screenColour.equals( image.get( C, R ) )){
                        P.setLocation( C, R );
                        //it will eventually stop the recursion as we will store all
                        //neighbours data (C and R) into a set.
                        if( !( NeighbourPixelsSameColour.contains( P ) ) ) {
                            NeighbourPixelsSameColour.add( P );
                            checkNeighbours( C, R, screenColour, NeighbourPixelsSameColour );
                        }
                    }
                }
            }
        }
    }


    private Rectangle makeRectangle (Set<Point> biggestRegion ) {
        int minC = width;
        int minR = height;
        int maxC = 0;
        int maxR = 0;

        for ( Point P : biggestRegion) {
            if ( P.x < minC ) {
                minC = P.x;
            }
            if ( P.y < minR ) {
                minR = P.y;
            }
            if ( P.x > maxC ) {
                maxC = P.x;
            }
            if ( P.y > maxR ) {
                maxR = P.y;
            }
        }

        return new Rectangle(minC, minR, maxC, maxR);
    }

    private Image fitBackground ( Rectangle bigRectangle, Image backgroundImage, Color screenColor ) {
        int recWidth = bigRectangle.xBottomRight - bigRectangle.xTopLeft;
        int recHeight = bigRectangle.yBottomRight - bigRectangle.yTopLeft;
        int imageWidth = backgroundImage.width();
        int imageHeight = backgroundImage.height();
        Image output = image;
        int C = 0; //backgroundImage col counter
        int R = 0; //backgroundImage row counter

        for ( int col = bigRectangle.xTopLeft; col <= bigRectangle.xBottomRight; col++) {
            for ( int row = bigRectangle.yTopLeft; row <= bigRectangle.yBottomRight; row++) {
                if ( screenColor.equals( image.get( col, row ))) {
                    output.set(col, row, backgroundImage.get(C, R));
                    R++;
                    if (R == imageHeight) {
                        R = 0;
                    }
                }
            }
            R = 0;
            C++;
            if ( C == imageWidth ) {
                C = 0;
            }
        }

        return output;
    }

    /**
     * Align (appropriately rotate) an image of text that was improperly aligned.
     * This transformation can work properly only with text images.
     *
     * @return the aligned image.
     * @Precondition the input must have text from -89 to 89 degrees off of the norm. The image
     * should also contain text in the center of the image.
     */
    public Image alignTextImage() throws ImageProcessingException {
        int maximumSize = 150;
        int[] pixel;
        int row;
        int col;
        Image output;
        Image img = compressAndSquare(maximumSize);

        ImageTransformer forDFT = new ImageTransformer(img);
        ImageTransformer whiteCheck = new ImageTransformer((new ImageTransformer((forDFT.dft()).amplitudeToImage())).filter());

        int found = 0;
        int length = whiteCheck.width;
        int accuracy = 1000;
        ArrayList<Point> q1 = new ArrayList<>();
        ArrayList<Point> q2 = new ArrayList<>();
        ArrayList<Point> q3 = new ArrayList<>();
        ArrayList<Point> q4 = new ArrayList<>();

        whiteCheck.spiralSearch(accuracy, length, q1, q2, q3, q4);

        double q1Slope = slopeOfBestFit(q1);
        double q2Slope = slopeOfBestFit(q2);
        double q3Slope = slopeOfBestFit(q3);
        double q4Slope = slopeOfBestFit(q4);
        double finalSlope;
        boolean positive;

        if (q1.size() > q2.size()) {
            finalSlope = (q1Slope * q1.size() + q3Slope * q3.size()) / (q1.size() + q3.size());
            positive = true;
        } else {
            finalSlope = (q2Slope * q2.size() + q4Slope * q4.size()) / (q2.size() + q4.size());
            positive = false;
        }


        double angle = Math.atan(1 / finalSlope);
        if (Double.isNaN(angle)) {
            System.out.println("Text is not tilted or is tilted 90 degrees");
            throw new ImageProcessingException();
        } else if (positive) {
            output = this.rotate(180 * (2 * Math.PI - angle) / Math.PI);
        } else {
            output = this.rotate(180 * (-angle) / Math.PI);
        }
        return output;
    }
    /**
     * Returns the slope of the the best fit line of an ArrayList of points
     * @param points takes a list of points
     * @returns the slope of best fit as a double.
    */
    private double slopeOfBestFit(ArrayList<Point> points) {
        int size = points.size();
        double xAvg = 0;
        double yAvg = 0;
        double slopeNumerator = 0;
        double slopeDenominator = 0;

        for (Point cord : points) {
            xAvg += cord.getX() / size;
            yAvg += cord.getY() / size;
        }

        for (Point cord : points) {
            slopeNumerator += (cord.getX() - xAvg) * (cord.getY() - yAvg);
            slopeDenominator += (cord.getX() - xAvg) * (cord.getX() - xAvg);
        }
        return slopeNumerator / slopeDenominator;
    }
    /**
     * Determines which quadrant of the image a pixel lies in and adds it to that quadrant
     * @param col column of the pixel
     * @param row row of the pixel
     * @param length dimension of the square
     * @param q1 quadrant 1 ArrayList
     * @param q2 quadrant 2 ArrayList
     * @param q3 quadrant 3 ArrayList
     * @param q4 quadrant 4 ArrayList
     */
    private void quadrantFinder(int col, int row, int length, ArrayList<Point> q1,
                                ArrayList<Point> q2, ArrayList<Point> q3,
                                ArrayList<Point> q4) {
        Point point;
        if (col > length / 2 && row < length / 2) {
            point = new Point(col, -row);
            q1.add(point);
        }

        if (col < length / 2 && row < length / 2) {
            point = new Point(col, -row);
            q2.add(point);
        }

        if (col < length / 2 && row > length / 2) {
            point = new Point(col, -row);
            q3.add(point);
        }

        if (col > length / 2 && row > length / 2) {
            point = new Point(col, -row);
            q4.add(point);
        }
    }

    /**
     * Returns a smaller and square version of the image
     * @param maximumSize maximum length and height of the output
     * @returns the compressed and square version of the image
     */
    private Image compressAndSquare(int maximumSize) throws ImageProcessingException {
        Image img = new Image(maximumSize, maximumSize);
        int smallerDimen;
        int col;
        int row;
        int x;
        int y;
        if (width < height) {
            smallerDimen = width;
        } else {
            smallerDimen = height;
        }

        if (width > maximumSize && height > maximumSize) {
            Image output;
            Image blockPainted = this.blockPaint(smallerDimen / maximumSize);
            for (col = width / 2 - smallerDimen / 2, x = 0; x < maximumSize;
                 col += smallerDimen / maximumSize, x++) {
                for (row = height / 2 - smallerDimen / 2, y = 0; y < maximumSize;
                     row += smallerDimen / maximumSize, y++) {
                    img.setRGB(x, y, blockPainted.getRGB(col, row));
                }
            }
        } else if (width < height) {
            img = this.clip(new Rectangle(0, height / 2 - width / 2,
                width - 1, height / 2 - width / 2 + width - 1));
        } else {
            img = this.clip(
                new Rectangle(width / 2 - height / 2, 0,
                    width / 2 - height / 2 + height - 1, height - 1));
        }
        return img;
    }

    /**
     *
     * @param accuracy The number of white pixels that are desired
     * @param length The dimension of the square
     * @param q1 quadrant 1
     * @param q2 quadrant 2
     * @param q3 quadrant 3
     * @param q4 quadrant 4
     */
    private void spiralSearch(int accuracy, int length, ArrayList<Point> q1, ArrayList<Point> q2,
                              ArrayList<Point> q3,
                              ArrayList<Point> q4) {
        int found = 0;
        int col;
        int row;
        int[] pixel;
        for (int searchRadius = 1; searchRadius < length && found < accuracy; searchRadius += 2) {
            /*right*/
            for (col = length / 2 - searchRadius / 2, row = length / 2 - searchRadius / 2;
                 col < length / 2 + searchRadius / 2 && found < accuracy; col++) {
                pixel = this.getBytes(col, row);

                if (pixel[1] == 255) {
                    quadrantFinder(col, row, length, q1, q2, q3, q4);
                    found++;
                }
            }
            /*down*/
            for (row = length / 2 - searchRadius / 2, col = length / 2 + searchRadius / 2;
                 row < length / 2 + searchRadius / 2 && found < accuracy; row++) {
                pixel = this.getBytes(col, row);
                if (pixel[1] == 255) {
                    quadrantFinder(col, row, length, q1, q2, q3, q4);
                    found++;
                }
            }
            /*left*/
            for (col = length / 2 + searchRadius / 2, row = length / 2 + searchRadius / 2;
                 col > length / 2 - searchRadius / 2 && found < accuracy; col--) {
                pixel = this.getBytes(col, row);
                if (pixel[1] == 255) {
                    quadrantFinder(col, row, length, q1, q2, q3, q4);
                    found++;
                }
            }
            /*up*/
            for (row = length / 2 + searchRadius / 2, col = length / 2 - searchRadius / 2;
                 row > length / 2 - searchRadius / 2 && found < accuracy; row--) {
                pixel = this.getBytes(col, row);
                if (pixel[1] == 255) {
                    quadrantFinder(col, row, length, q1, q2, q3, q4);
                    found++;
                }
            }
        }
    }
}
