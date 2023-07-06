package Cells.Classification;

import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import static Functions.K_means.*;
import static pl.umk.mat.canny.Canny.*;
import static Functions.Utils.*;
import static org.opencv.imgproc.Imgproc.*;

public class Main
{
    public static void main( String[] args ) throws IOException {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("C:\\\\Users\\\\micha\\\\IdeaProjects\\\\Cells-OpenCV\\\\Cells\\\\src\\\\main\\\\java\\\\Images\\\\Matis.png"));
        } catch (IOException e) { }

        int height = img.getHeight();
        int width = img.getWidth();
        int[] imgColorsBinary = img.getRGB(0, 0, width, height, null, 0, width);
        int[][] imgColorsNonBinary = convertFromBinary(imgColorsBinary);
        int[][] clusteredImg = k_means(imgColorsNonBinary, 10);

        int[][] layerdImage = layerConvertedImageOnOriginalImage(imgColorsNonBinary, clusteredImg);

        int[][][] layerdImageTwoDim = new int[height][width][3];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                layerdImageTwoDim[i][j] = layerdImage[i * width + j];
            }
        }
        debugJPG(layerdImageTwoDim, "layerdImage");

        int[] grayscaleImage = convertToGrayscale(layerdImage);
        int[][] grayscaleImageTwoDim = convertToTwoDimArray(grayscaleImage, height, width);

        double[][] gaussMatrix = gaussMatrix(1, 1);
        int[][] bluredImage = convolution(grayscaleImageTwoDim, height, width, gaussMatrix);

        int[][] derivateX = sobelDerivativeX(bluredImage, height, width);
        int[][] derivateY = sobelDerivativeY(bluredImage, height, width);
        double[][] gradientMagnitude = magnitudeComputation(derivateX, derivateY, height, width, false);
        debugJPG(min_max(gradientMagnitude, height, width), "gradientMagnitude");

        double[][] angles = angleComputation(derivateX, derivateY);
        double[][] nonMaximumSuppresionImage = nonMaximumSuppression(gradientMagnitude, height, width, angles);
        debugJPG(min_max(nonMaximumSuppresionImage, height, width), "nonMaximumSuppresion");
        double[] meanAndSTD = meanAndSTD(gradientMagnitude);
        double mean = meanAndSTD[0];
        double STD = meanAndSTD[1];
        double k = 1.2;
        double thresholdMax = mean + k * STD;
        double thresholdMin = thresholdMax / 2;
        int[][] tresholdedImage = thresholding(nonMaximumSuppresionImage, thresholdMax, thresholdMin);
        int[][] tresholdedImageInt = new int[height][width];
        for (int i = 0; i < height; i++){
            for (int j = 0; j < width; j++) {
                tresholdedImageInt[i][j] = tresholdedImage[i][j];
            }
        }
        debugJPG(tresholdedImageInt, "thresholding");

        int[][] cannyOutput = hysteresis(tresholdedImage, height, width);

        int[][] hysteresisImage = new int[height][width];
        for(int i = 0; i < height; i++){
            for(int j = 0; j < width; j++){
                hysteresisImage[i][j] = 255 * cannyOutput[i][j];
            }
        }
        debugJPG(hysteresisImage, "hysteresis");

        OpenCV.loadLocally();
        Imgcodecs imageCodecs = new Imgcodecs();
        
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchey = new Mat();
        Mat cannyOutputMat = intToMat(cannyOutput, height, width);
        findContours(cannyOutputMat, contours, hierarchey, RETR_EXTERNAL, CHAIN_APPROX_NONE);

        List<MatOfPoint> cleandUpContours = contoursCleanUp(contours, 3);
        Mat image = Mat.zeros(height, width, CvType.CV_32SC1);

        for (int i = 0; i < cleandUpContours.size(); i++) {
            drawContour(image, cleandUpContours.get(i));
        }
        int[][] debugImage = matToInt(image);
        debugJPG(debugImage, "Clean contours");
        List<List<Point>> endPoints = endPoints(cannyOutput, cleandUpContours, height, width);
        image = connectingEndPoints(endPoints, image, 10, 5);
        debugJPG(matToInt(image), "Conected contours");

        File centersFile = new File("C:\\Users\\micha\\IdeaProjects\\Cells-OpenCV\\Cells\\src\\main\\java\\centers.txt");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(centersFile));

        String line;
        List<Point> centers = new ArrayList<>();

        while ((line = bufferedReader.readLine()) != null) {
                String[] lineSplited = line.split(",");
                int X = Integer.parseInt(lineSplited[0]);
                int Y = Integer.parseInt(lineSplited[1]);
                centers.add(new Point(X, Y));
        }

        List<double[]> colors = new ArrayList<>();
        colors.add(new double[]{0, 0, 0}); //Black means empty space
        colors.add(new double[]{255, 255, 255}); //White means eadge
        Random random = new Random();
        for (int i = 0; i < centers.size(); i++) { //random color for each center
            int r = random.nextInt(255);
            int g = random.nextInt(255);
            int b = random.nextInt(255);
            colors.add(new double[]{r, g, b});
        }


        Mat dilatedCenters = pixelDilatation(image, centers);
        Mat dilatedCentersColored = coloringCenters(dilatedCenters, colors);

        imageCodecs.imwrite("Centers_color.jpg", dilatedCentersColored);

        Mat dilatedCentersWithNewEdges = Mat.zeros(height, width, CvType.CV_32SC1);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int center = (int) dilatedCenters.get(i, j)[0];
                if (center >= 2) {
                    dilatedCentersWithNewEdges.put(i, j, new double[]{center});
                }
            }
        }

        HashMap cellsEadges = findEadges(dilatedCenters, centers.size());

        for (int i = 2; i < centers.size() + 2; i++) {
            List<Point> eadge = (List<Point>) cellsEadges.get(i);
            for (Point point : eadge) {
                dilatedCentersWithNewEdges.put((int) point.y, (int) point.x, new double[]{1});
            }
        }

        Mat temp_color = coloringCenters(dilatedCentersWithNewEdges, colors);
        imageCodecs.imwrite("Centers_eadges.jpg", temp_color);
    }
}
