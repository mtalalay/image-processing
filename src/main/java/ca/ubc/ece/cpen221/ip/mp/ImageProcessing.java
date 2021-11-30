package ca.ubc.ece.cpen221.ip.mp;
import ca.ubc.ece.cpen221.ip.core.Image;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * This class provides some simple operations involving
 * more than one image.
 */
public class ImageProcessing {

    /**
     * Compute the cosine similarity between two images. If both images are black it outputs 1,
     * if 1 is black it ouputs 0.
     *
     * @param img1: the first image, is not null.
     * @param img2: the second image, in not null and matches img1 in dimensions.
     * @return the cosine similarity between the Images
     * referenced by img1 and img2.
     */
    public static double cosineSimilarity(Image img1, Image img2) {
        ImageTransformer greyImg1 = new ImageTransformer(img1);
        img1 = greyImg1.grayscale();

        ImageTransformer greyImg2 = new ImageTransformer(img2);
        img2 = greyImg2.grayscale();


        int cols, rows;
        int[] pixel1;
        int[] pixel2;

        BigDecimal product = new BigDecimal(0);
        BigDecimal square1 = new BigDecimal(0);
        BigDecimal square2 = new BigDecimal(0);
        BigDecimal zero = new BigDecimal(0);
        MathContext mc = new MathContext(10);

        int smallerWidth;
        int smallerHeight;

        if (img1.width() < img2.width()) {
            smallerWidth = img1.width();
        } else {
            smallerWidth = img2.width();
        }

        if (img1.height() < img2.height()) {
            smallerHeight = img1.height();
        } else {
            smallerHeight = img2.height();
        }


        for (cols = 0; cols < smallerWidth; cols++) {
            for (rows = 0; rows < smallerHeight; rows++) {
                pixel1 = greyImg1.getBytes(cols, rows);
                pixel2 = greyImg2.getBytes(cols, rows);
                /*System.out.println(pixel1[1]);
                System.out.println(pixel2[1]);*/
                product =
                    product.add(BigDecimal.valueOf(pixel1[1])
                        .multiply(BigDecimal.valueOf(pixel2[1])));
                square1 =
                    square1.add(BigDecimal.valueOf(pixel1[1])
                        .multiply(BigDecimal.valueOf(pixel1[1])));
                square2 =
                    square2.add(BigDecimal.valueOf(pixel2[1])
                        .multiply(BigDecimal.valueOf(pixel2[1])));
            }
        }
        square1 = square1.sqrt(mc);
        square2 = square2.sqrt(mc);

        if (square1.equals(zero) && square2.equals(zero)) {
            return 1;
        }

        if (square1.equals(zero) ^ square2.equals(zero)) {
            return 0;
        }

        return (product.divide((square1.multiply(square2)), mc)).doubleValue();
    }
}
