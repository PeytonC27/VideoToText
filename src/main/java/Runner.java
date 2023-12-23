import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

public class Runner {

    public static void main(String[] args) {
//        Scanner scanner = new Scanner(System.in);
//
//        boolean runProgram = true;
//
//        // discord limit (nitro) = 4000
//        while (runProgram) {
//            System.out.print("Enter the path to the image: ");
//            String imagePath = scanner.nextLine();
//            System.out.print("Enter the path to the text file to save to: ");
//            String filePath = scanner.nextLine();
//            System.out.print("Enter the max character count of your saved file: ");
//            String max = scanner.nextLine();
//            System.out.print("Invert the ASCII output (put \"yes\" if you want darker colors to be written as lighter ASCII characters, like ' and .): ");
//            String invert = scanner.nextLine();
//
//            int maxCharacterCount = 0;
//            try {
//                maxCharacterCount = Integer.parseInt(max);
//            } catch (Exception e) {
//                System.out.println("You need to provide a valid integer to print.");
//                return;
//            }
//
//            boolean invertChoice = invert.equals("yes");

            try {
                StringBuilder allData = new StringBuilder();

                FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("C:\\Users\\peyto\\Downloads\\ssstwitter.com_1702333150756.mp4");
                grabber.start();

                Frame frame;
                Java2DFrameConverter converter = new Java2DFrameConverter();
                ImageToText imageToText = new ImageToText();
                for (int i = 1; i <= grabber.getLengthInVideoFrames(); i++) {
                    frame = grabber.grabImage();

                    if (frame == null || frame.image == null) {
                        continue;
                    }

                    BufferedImage img = converter.getBufferedImage(frame);

                    String data = imageToText.convertImageToText(img, 4000, true);
                    allData.append(data);
                }
                grabber.stop();

                // write stuff in text file
                FileWriter writer = new FileWriter(new File("C:\\Users\\peyto\\Downloads\\beeg.txt"));
                writer.write(allData.toString());
                writer.close();
                System.out.println("Done");
            } catch (Exception e) {
                System.out.println("One or both of the provided file paths is invalid, make sure you have a valid .png or .jpg for the image path and a valid .txt file for the text file.");
            }
//
//            System.out.println("\nType \"again\" to run the app again");
//            String response = scanner.nextLine();
//            if (response.equals("again")) {
//                System.out.println("\n\n\n");
//            } else {
//                runProgram = false;
//            }
        }
    }