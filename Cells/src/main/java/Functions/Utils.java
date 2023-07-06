package Functions;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.core.CvType.CV_8UC3;

public class Utils {
    public static int[][] convertFromBinary(int[] image) {
        int[][] output = new int[image.length][3];
        int r;
        int g;
        int b;

        for(int i = 0; i < image.length; i++) {
            int pixel = image[i];
            r = ((pixel >>> 16) & 0xFF);
            g = ((pixel >>> 8) & 0xFF);
            b = (pixel & 0xFF);
            output[i] = new int[]{r, g, b};
        }
        return  output;
    }

    public static Mat intToMat(int[][] image, int height, int width) {
        OpenCV.loadLocally();
        Mat output = new Mat(height, width, CV_8UC1);

        for(int i = 0; i < height; i++) {
            for(int j = 0; j < width; j++){
                output.put(i, j, image[i][j]);
            }
        }
        return output;
    }


    public static void debugJPG(int[][] image, String picName) {
        int width = image[0].length;
        int height = image.length;
        BufferedImage BImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] Output = new int[height * width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int piks = image[i][j];
                Output[i * width + j] = piks << 16 | piks << 8 | piks | 0xFF000000;
            }
        }
        BImage.setRGB(0, 0, width, height, Output, 0, width);

        try {
            File outputfile = new File(picName + ".png");
            ImageIO.write(BImage, "png", outputfile);
        } catch (IOException e) {
            System.out.println("Error");
        }
    }

    public static void debugJPG(int[][][] image, String picName) {
        int width = image[0].length;
        int height = image.length;
        BufferedImage BImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] Output = new int[height * width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int[] piks = image[i][j];
                Output[i * width + j] = piks[0] << 16 | piks[1] << 8 | piks[2] | 0xFF000000;
            }
        }
        BImage.setRGB(0, 0, width, height, Output, 0, width);

        try {
            File outputfile = new File(picName + ".png");
            ImageIO.write(BImage, "png", outputfile);
        } catch (IOException e) {
            System.out.println("Error");
        }
    }

    public static int[][] matToInt(Mat picture) {
        OpenCV.loadLocally();
        int[][] output = new int[picture.height()][picture.width()];

        for(int i = 0; i < picture.height(); i++){
            for(int j = 0; j < picture.width(); j++){
                output[i][j] = (int) picture.get(i, j)[0] * 255;
            }
        }
        return output;
    }

    @Deprecated
    public static List<MatOfPoint> Sorting_contours(List<MatOfPoint> contours) {

        List<MatOfPoint> Output = new ArrayList<>();
        int debug1 = 0;
        for(int i = 0; i < contours.size(); i++) {

            MatOfPoint debug = contours.get(i);
            List<Point> contour_list = contours.get(i).toList();

            if (contour_list.size() > 15 || Imgproc.contourArea(debug) > 10.0) {


                List<Point> contour_list_clean = new ArrayList<>();

                for (Point point : contour_list) {
                    if (!contour_list_clean.contains(point)) {
                        contour_list_clean.add(point);
                    }
                }

                Point start = contour_list_clean.get(0);

                int X = 0;
                int Y = 0;
                int contour_length = contour_list_clean.size();

                for (Point point : contour_list_clean) {
                    X += point.x;
                    Y += point.y;
                }

                Point center = new Point(X / contour_length, Y / contour_length);

                try {
                    contour_list_clean.sort((point1, point2) -> comparator(point1, point2, start, center));

                    MatOfPoint contour_sorted = new MatOfPoint();
                    contour_sorted.fromList(contour_list_clean);
                    Output.add(contour_sorted);
                } catch (IllegalArgumentException e) {
                    debug1 += 1;
                }

            }
        }
        return Output;
    }
    @Deprecated
    private static int sign(double x){

        if(x < 0){
            return -1;
        }
        if(x > 0){
            return 1;
        }
        return 0;

    }

    @Deprecated
    public static double angle(Point o1, double[] center){

        double[] vec = new double[]{o1.x - center[0], o1.y - center[1]};
        int sign_x = sign(vec[0]);
        int sign_y = sign(vec[1]);

        if(sign_y == 0){
            sign_x = 4 * sign_x;
        }
        int sign = sign_x + 2 * sign_y;
        double temp = Math.abs(vec[0]) + Math.abs(vec[1]);

        switch (sign) {
            case 4:
            case 3:
            case 2:
                return vec[1] / temp;
            case 1:
            case -4:
                return 2 - vec[1] / temp;
            case -3:
                return 2 + Math.abs(vec[1]) / temp;
            case -1:
            case -2:
                return 4 - Math.abs(vec[1]) / temp;
            default:
                return 0;

        }
    }
    @Deprecated
    public static int comparator(Point o1, Point o2, Point start, Point center){

        double angle1 = Math.toDegrees(angle_between3(start, o1, center));
        double angle2 = Math.toDegrees(angle_between3(start, o2, center));
        double dist_square = distance_square(o1,o2);

        if(angle1 > angle2 & dist_square <= 9){ return 1;}
        if(angle1 > angle2 & dist_square > 9){ return -1;}
        if(angle1 < angle2){ return -1;}
        return 0;
    }
    @Deprecated
    private static double angle_between3(Point start, Point point, Point center) {
        double[] vector1 = new double[]{start.x - center.x, start.y - center.y};
        double[] vector2 = new double[]{point.x - center.x, point.y - center.y};

        double angle = Math.atan2(vector2[1], vector2[0]) - Math.atan2(vector1[1], vector1[0]);

        if(angle < 0){
            angle += 2 * Math.PI;
        }

        return angle;
    }
    @Deprecated
    private static double distance_square(Point o1, Point o2){
        return Math.pow((o1.x - o2.x),2) + Math.pow((o1.y - o2.y),2);
    }
    @Deprecated
    public static List<MatOfPoint> Connecting_contours(List<MatOfPoint> Sorted_contours, double threshold){
        double threshold_squared = threshold * threshold;
        int Sorted_contours_length = Sorted_contours.size();

        List<MatOfPoint> Output = new ArrayList<>();
        List<int[]> pairs = new ArrayList<>();
        int[] singles = new int[Sorted_contours_length];
        for(int i = 0; i < Sorted_contours_length; i++){
            singles[i] = 0;
        }
        int Next = 2;


        for(int i = 0; i < Sorted_contours_length - Next; i++){
            for(int j = i + 1; j < i + 1 + Next; j++){

                boolean found = false;

                for(Point m : Sorted_contours.get(j).toList()) {
                    for (Point n : Sorted_contours.get(i).toList()) {

                        double distance_sqr = distance_square(n,m);

                        if(distance_sqr <= threshold_squared){
                            singles[i] = 1;
                            singles[j] = 1;

                            pairs.add(new int[]{j,i});
                            found = true;
                            break;
                        }

                    }
                    if(found) break;
                }
            }
        }

        for(int i = 0; i < singles.length; i++) {
            if(singles[i] == 0){
                pairs.add(new int[]{i});
            }
        }

        for(int i = 0; i < pairs.size(); i++){

            List<Point> connected_contour = new ArrayList<>();

            for(int j : pairs.get(i)){
                List<Point> contour = Sorted_contours.get(j).toList();
                connected_contour.addAll(contour);
            }

            MatOfPoint temp = new MatOfPoint();
            temp.fromList(connected_contour);
            Output.add(temp);
        }

        return Output;
    }

    @Deprecated
    public static double Angle_between_2(Point a1, Point a2, Point b1, Point b2){
        double[] a1a2 = new double[]{a2.x - a1.x, a2.y - a1.y};
        double[] b1b2 = new double[]{b2.x - b1.x, b2.y - b1.y};

        double len1 = Math.sqrt(a1a2[0] * a1a2[0] + a1a2[1] * a1a2[1]);
        double len2 = Math.sqrt(b1b2[0] * b1b2[0] + b1b2[1] * b1b2[1]);

        double Dot_prod = a1a2[0] * b1b2[0] + a1a2[1] * b1b2[1];

        if(Dot_prod == 0){
            return 0;
        }
        else{
            double cos = Dot_prod / (len1 * len2);
            if(cos >= 1){return Math.acos(1);}
            if(cos <= -1){return Math.acos(-1);}
            else{
                return Math.acos(cos);
            }
        }
    }
    @Deprecated
    public static double distance(int[] point1, Point point2){
        return Math.sqrt( Math.pow((point1[0] - point2.x),2) + Math.pow((point1[1] - point2.y),2));
    }

    public static Mat drawContour(Mat image, MatOfPoint contour) {

        for (int i = 0; i < contour.height(); i++) {
            double[] point = contour.get(i,0);
            image.put((int) point[1], (int) point[0], 1);
        }
        return image;
    }

    public static Mat line(Mat image, Point point1, Point point2) {

        if (Math.abs(point2.y - point1.y) < Math.abs(point2.x - point1.x)) {
            if (point1.x > point2.x) {
                image = plotLineLow(point2, point1, image);
            } else {
                image = plotLineLow(point1, point2, image);
            }
        } else {
            if (point1.y > point2.y) {
                image = plotLineHigh(point2, point1, image);
            } else {
                image = plotLineHigh(point1, point2, image);
            }
        }
        return image;
    }

    private static Mat plotLineHigh(Point point1, Point point2, Mat image) {
        int dx = (int) (point2.x - point1.x);
        int dy = (int) (point2.y - point1.y);
        int xi = 1;

        if (dx < 0) {
            xi -= 1;
            dx = -dx;
        }
        int d = (2 * dx) -dy;
        int x = (int) point1.x;

        for (int y = (int) point1.y; y < point2.y; y++) {
            image.put(y, x, new double[]{1,1,1});
            if (d > 0) {
                x += xi;
                d += (2 * (dx - dy));
            }
            else {
                d += 2 * dx;
            }
        }
        return image;
    }
    private static Mat plotLineLow(Point point1, Point point2, Mat image) {
        int dx = (int) (point2.x - point1.x);
        int dy = (int) (point2.y - point1.y);
        int yi = 1;

        if (dy > 0) {
            yi = -1;
            dy = - dy;
        }
        int d = (2 * dy) - dx;
        int y = (int) point1.y;

        for (int x = (int) point1.x; x < point2.x; x++) {
            image.put(y, x, new double[]{1,1,1});
            if (d > 0) {
                y += yi;
                d += (2 * (dy - dx));
            } else {
                d += 2 * dy;
            }
        }
        return image;
    }

    public static List<List<Point>> endPoints(int[][] image, List<MatOfPoint> contours, int height, int width){
        List<List<Point>> output = new ArrayList<>();
        int[][] neighbors = new int[][]{{1,0}, {1,1}, {0,1}, {-1,1}, {-1,0}, {-1,-1}, {0,-1}, {1,-1}, {1,0}};

        for (MatOfPoint contour : contours) {
            List<Point> contoursEnds = new ArrayList<>();
            for (Point point : contour.toList()) {
                int neighbours_number = 0;
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {

                        if (point.x + i >= 0 & point.x + i < width & point.y + j >= 0 & point.y + j < height & (i != 0 || j != 0)) {
                            if (image[(int) (point.y + j)][(int) point.x + i] != 0) {
                                neighbours_number += 1;
                            }
                        }
                    }
                }
                if (neighbours_number == 1) {
                    contoursEnds.add(point);
                }
                if (neighbours_number == 2) {
                    for (int i = 0; i < neighbors.length - 1; i++) {
                        int[] neighbour1 = neighbors[i];
                        int[] neighbour2 = neighbors[i + 1];
                        try{
                            if (image[(int) (neighbour1[0] + point.y)][(int) (neighbour1[1] + point.x)] != 0 & image[(int) (neighbour2[0] + point.y)][(int) (neighbour2[1] + point.x)] != 0) {
                                contoursEnds.add(point);
                            }
                        } catch (IndexOutOfBoundsException e) {

                        }
                    }
                }
            }
            output.add(contoursEnds);
        }
        return output;
    }

    public static List<MatOfPoint> contoursCleanUp(List<MatOfPoint> contours, int minimalSize){
        List<MatOfPoint> output = new ArrayList<>();

        for (MatOfPoint contour : contours) {
            List<Point> contour_list = contour.toList();
            if (contour_list.size() > minimalSize) {
                output.add(contour);
            }
        }
        return output;
    }

    public static Mat connectingEndPoints(List<List<Point>> endPoints, Mat image, double treshold, int boxesSize) {
        int height = image.height();
        int width = image.width();
        List<Point> temp = new ArrayList<>();
        double thresholdSquare = treshold * treshold;
        for (List<Point> contourEnds : endPoints) {
            if (contourEnds.size() == 2) {
                if ((contourEnds.get(0).x - contourEnds.get(1).x) * (contourEnds.get(0).x - contourEnds.get(1).x) + (contourEnds.get(0).y
                        - contourEnds.get(1).y) * (contourEnds.get(0).y - contourEnds.get(1).y) <= thresholdSquare) {
                    image = line(image, contourEnds.get(0), contourEnds.get(1));
                } else {
                    temp.addAll(contourEnds);
                }
            } else {
                temp.addAll(contourEnds);
            }
        }
        Table<Integer, Integer, List<Point>> boxes = HashBasedTable.create();

        for (int i = 0; i < height / boxesSize + 1; i++) {
            for (int j = 0; j < width / boxesSize + 1; j++) {
                boxes.put(j, i, new ArrayList<>());
            }
        }

        for (Point point : temp) {
            int x = (int) (point.x / boxesSize);
            int y = (int) (point.y / boxesSize);
            List<Point> box = boxes.get(x, y);
            box.add(point);
            boxes.put(x, y, box);
        }

        int boxesWidth = width / boxesSize + 1;
        int boxesHeight = height / boxesSize + 1;
        for (Point point : temp) {
            List<Point> candidates = new ArrayList<>();
            int x = (int) (point.x / boxesSize);
            int y = (int) (point.y / boxesSize);

            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    if (x + j >= 0 & x + j < boxesWidth & y + i >= 0 & y + i < boxesHeight) {
                        List<Point> box = boxes.get(x + j, y + i);
                        candidates.addAll(box);
                    }
                }
            }

            if (candidates != null) {
                candidates.remove(point);
            }
            if (candidates.size() != 0) {
                Point nearestPoint = candidates.get(0);
                double min = distance_square(point, nearestPoint);

                for (int i = 1; i < candidates.size(); i++) {
                    Point candidate = candidates.get(i);
                    double dist = distance_square(point, candidate);

                    if (distance_square(point, candidates.get(i)) < min) {
                        nearestPoint = candidates.get(i);
                        min = dist;
                    }
                }
                image = line(image, point, nearestPoint);
            }
        }
        return image;
    }

    public static Mat pixelDilatation(Mat image, List<Point> centers) {
        int height = image.height();
        int width = image.width();
        List<Point> queue = new ArrayList<>();
        List<Point> nextQueue = new ArrayList<>();
        int iter = 0;

        for (int i = 0; i < centers.size(); i++) {
            Point center = centers.get(i);
            image.put((int) center.y, (int) center.x, i + 2);
            queue.add(center);
        }

        while (queue.size() != 0 & iter < 30) {
            for (Point point : queue) {

                if (point.x + 1 < width) {
                    if (image.get((int) point.y, (int) (point.x + 1))[0] == 0 & isValid(image, (int) point.x + 1, (int) point.y, height, width) == true) {
                        image.put((int) point.y, (int) (point.x + 1), image.get((int) point.y, (int) point.x)[0]);
                        nextQueue.add(new Point((int) (point.x + 1), (int) point.y));
                    }
                }

                if (point.x - 1 >= 0) {
                    if(image.get((int) point.y, (int) (point.x - 1))[0] == 0 & isValid(image, (int) point.x - 1, (int) point.y, height, width) == true) {
                        image.put((int) point.y, (int) (point.x - 1), image.get((int) point.y, (int) point.x)[0]);
                        nextQueue.add(new Point((int) (point.x - 1), (int) point.y));
                    }
                }

                if (point.y + 1 < height) {
                    if(image.get((int) point.y + 1, (int) (point.x))[0] == 0 & isValid(image, (int) point.x, (int) point.y + 1, height, width) == true) {
                        image.put((int) point.y + 1, (int) (point.x), image.get((int) point.y, (int) point.x)[0]);
                        nextQueue.add(new Point((int) (point.x), (int) point.y + 1));
                    }
                }

                if (point.y - 1 >= 0) {
                    if(image.get((int) point.y - 1, (int) (point.x))[0] == 0 & isValid(image, (int) point.x, (int) point.y - 1, height, width) == true) {
                        image.put((int) point.y - 1, (int) (point.x), image.get((int) point.y, (int) point.x)[0]);
                        nextQueue.add(new Point((int) (point.x), (int) point.y - 1));
                    }
                }
            }
            queue = new ArrayList<>();
            queue.addAll(nextQueue);
            nextQueue = new ArrayList<>();
            iter += 1;
        }
        return image;
    }

    private static boolean isValid(Mat image, int x, int y, int height, int width){
        boolean answer = true;

        if (x + 1 < width) {
            if (image.get(y, x + 1)[0] == 1) {
                answer = false;
            }
        }
        if (x - 1 >= 0) {
            if (image.get(y, x - 1)[0] == 1) {
                answer = false;
            }
        }
        if (y + 1 < height) {
            if (image.get(y + 1, x)[0] == 1) {
                answer = false;
            }
        }
        if (y - 1 >= 0) {
            if (image.get(y - 1, x)[0] == 1) {
                answer = false;
            }
        }
        return answer;
    }

    public static Mat coloringCenters(Mat image, List<double[]> colors){
        int height = image.height();
        int width = image.width();
        Mat output = new Mat(height, width, CV_8UC3);

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                double[] color = colors.get((int) image.get(i,j)[0]);
                output.put(i,j, color);
            }
        }
        return output;
    }

    public static HashMap<Integer, List<Point>> findEadges(Mat image, int centersNumber){
        int height = image.height();
        int width = image.width();

        HashMap<Integer, List<Point>> output = new HashMap<>();
        for (int i = 0; i < centersNumber + 2; i++) {
            output.put(i, new ArrayList<>());
        }
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int centerPixel = (int) image.get(i,j)[0];
                boolean answer = true;
                if (j + 1 < width) {
                    if (image.get(i,j + 1)[0] != centerPixel) {
                        answer = false;
                    }
                }
                if (j - 1 >= 0) {
                    if (image.get(i,j - 1)[0] != centerPixel) {
                        answer = false;
                    }
                }
                if (i + 1 < height) {
                    if (image.get(i + 1,j)[0] != centerPixel) {
                        answer = false;
                    }
                }
                if (i - 1 >= 0) {
                    if (image.get(i - 1,j)[0] != centerPixel) {
                        answer = false;
                    }
                }
                if (answer == false) {
                    List<Point> temp = output.get(centerPixel);
                    temp.add(new Point(j, i));
                    output.put(centerPixel, temp);
                }
            }
        }
        return output;
    }
}
