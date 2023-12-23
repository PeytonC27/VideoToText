import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ImageToText {

    private final double[] lumaValueRatio = new double[]{0, 0.0751, 0.0829, 0.0848, 0.1227, 0.1403, 0.1559, 0.185, 0.2183, 0.2417, 0.2571, 0.2852, 0.2902, 0.2919, 0.3099, 0.3192, 0.3232, 0.3294, 0.3384, 0.3609, 0.3619, 0.3667, 0.3737, 0.3747, 0.3838, 0.3921, 0.396, 0.3984, 0.3993, 0.4075, 0.4091, 0.4101, 0.42, 0.423, 0.4247, 0.4274, 0.4293, 0.4328, 0.4382, 0.4385, 0.442, 0.4473, 0.4477, 0.4503, 0.4562, 0.458, 0.461, 0.4638, 0.4667, 0.4686, 0.4693, 0.4703, 0.4833, 0.4881, 0.4944, 0.4953, 0.4992, 0.5509, 0.5567, 0.5569, 0.5591, 0.5602, 0.5602, 0.565, 0.5776, 0.5777, 0.5818, 0.587, 0.5972, 0.5999, 0.6043, 0.6049, 0.6093, 0.6099, 0.6465, 0.6561, 0.6595, 0.6631, 0.6714, 0.6759, 0.6809, 0.6816, 0.6925, 0.7039, 0.7086, 0.7235, 0.7302, 0.7332, 0.7602, 0.7834, 0.8037, 1};

    /**
     * Converts an image to ASCII characters, looping through the pixels of the
     * provided imageFilePath and writing the results to the textFilePath
     *
     * @param maxCharacterCount  the maximum characters the converter can write
     * @param invertASCIIDensity whether the darker pixels should be represented as
     *                           a dense ASCII character (true) or an empty/small
     *                           ASCII character (false)
     */
    public String convertImageToText(BufferedImage image, int maxCharacterCount, boolean invertASCIIDensity) throws Exception {

        int compressionRatio = maxCharacterCount == 0 ? 1 : calculateCompressionRatio(image, maxCharacterCount);
        //int charCount = 0;

        // This is where the image is converted into ASCII
        try {

            StringBuilder sb = new StringBuilder();

            for (int y = 0; y < image.getHeight(); y += compressionRatio) {
                for (int x = 0; x < image.getWidth(); x += compressionRatio) {

                    // grab the pixel RGB
                    double luma = calculateLumaAverage(image, x, y, compressionRatio);

                    sb.append(getASCIICharacter(luma, invertASCIIDensity));
                    //charCount++;
                }
                sb.append("\n");
            }

            sb.append(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n");
            return sb.toString();
        } catch (Exception e) {
            System.out.println("There was an issue parsing the provided image.");
            throw new Exception();
        }

    }

    /**
     * This grabs an area of pixels and calculates the average value at the target
     * pixel. This is for compressing the image into a smaller ASCII file. The
     * skipSize is the length of the square used for the area.
     *
     * @param image        the image to grab the pixels from
     * @param x            the x coordinate of the top left corner
     * @param y            the y coordinate of the top left corner
     * @param squareLength the side length of the square
     * @return the average luma value of the pixels in the specified area
     */
    private int calculateLumaAverage(BufferedImage image, int x, int y, int squareLength) {
        int sum = 0;
        int sumCount = 0;
        for (int i = x; i < x + squareLength; i++) {
            for (int j = y; j < y + squareLength; j++) {

                int color = image.getRGB(x, y);

                sum += (int) lumaValue(color);
                sumCount++;
            }
        }

        return sum / sumCount;
    }

    /**
     * Calculates the luma value of the passed in color
     *
     * @param color the rgb value of the color
     * @return the luma value
     */
    double lumaValue(int color) {
        int r = (color & 0x00ff0000) >> 16;
        int g = (color & 0x0000ff00) >> 8;
        int b = (color & 0x000000ff);

        return (0.2126 * r + 0.7152 * g + 0.0722 * b);
    }

    /**
     * Calculates how much the image should be compressed. A
     * value of 1 will result in a 1x1 square of pixels :
     * 1 ASCII character map, whereas a value of 6 will
     * result in a 6x6 square of pixels : 1 ASCII
     * character map
     *
     * @param maxCharacterCount the character count
     * @return the compression ratio (-1 if something went wrong)
     */
    private int calculateCompressionRatio(BufferedImage image, int maxCharacterCount) {

        int width = image.getWidth();
        int height = image.getHeight();
        int area = width * height;

        int ratio = 1; // the return value
        int count = 0; // loop count
        int loss; // pixels not accounted for in final result

        int maxLoops = Math.min(width, height);

        // if the loop fails after 500 times, then something went wrong
        while (count <= maxLoops) {
            int iterationY = (height + ratio) / ratio;
            int iterationX = (width + ratio) / ratio;
            int product = iterationX * iterationY;

            // the equation below is complicated, but essentially it measures how many
            // pixels will be lost when generating the ASCII output
            loss = area - ((width - width % ratio) * (height - height % ratio));

            if (product < maxCharacterCount) {
                return ratio;
            }

            ratio++;
            count++;
        }
        return -1;
    }

    /**
     * <p>Gets the necessary ASCII character based on the passed in luma value. The
     * * larger the luma value, the more "dense" the character. You can enable the
     * * inverse boolean to perform the opposite (larger luma gets the less dense
     * * character)</p>
     * <a href="https://stackoverflow.com/questions/30097953/ascii-art-sorting-an-array-of-ascii-characters-by-brightness-levels-c-c">Link to Reference</a>
     *
     * @param lumaValue the brightness of the pixel as a double
     * @param invert    whether to invert the density of the ASCII characters
     * @return an ASCII character representing the passed in luma value
     */
    private char getASCIICharacter(double lumaValue, boolean invert) {

        int valueSkip = 1;

        for (int i = lumaValueRatio.length - 1; i >= 0; i -= valueSkip) {
            if (lumaValue >= lumaValueRatio[i] * 255) {
                // the second character should be `, not '
                String asciiCharacters = " `.-':_,^=;><+!rc*/z?sLTv)J7(|Fi{C}fI31tlu[neoZ5Yxjya]2ESwqkP6h9d4VpOGbUAKXHm8RD#$Bg0MNWQ%&@";
                if (invert) return asciiCharacters.charAt(i);
                else return asciiCharacters.charAt(asciiCharacters.length() - i - 1);
            }
        }
        return ' ';
    }
}
