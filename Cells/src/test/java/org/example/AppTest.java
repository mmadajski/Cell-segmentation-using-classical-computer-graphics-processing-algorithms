package org.example;

import static Functions.Utils.angle;
import static org.junit.Assert.assertEquals;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_NONE;
import static pl.umk.mat.canny.Canny.*;

import nu.pattern.OpenCV;
import org.junit.Test;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.*;


public class AppTest {
    @Test
    public void hysteresisTest() {
       int[][] test = new int[][]{
               {0,0,0,0,0},
               {0,127,127,0,0},
               {0,0,0,127,0},
               {0,0,0,255,0},
               {0,0,0,0,0}
       };

       int[][] test1 = hysteresis(test, 5, 5);
       for (int i = 0; i < 5; i++){
           List<Integer> asd = new ArrayList<>();
           for (int j = 0; j < 5; j++){
               asd.add(test1[i][j]);
           }
           System.out.println(asd);
       }
    }


    @Test
    public void testSplot() {
        int[][] image = {
                {7,8,9,4,5},
                {4,5,6,4,5},
                {1,2,3,4,5},
                {1,2,3,4,5},
                {1,2,3,4,5},
                {1,2,3,4,5}};

        int[][] result_X = convolution(image, 6,5, SOBEL_MATRIX_X); //SOBEL_MATRIX_X);
        assertEquals(8, result_X[1][1]);

        int[][] result_Y = convolution(image, 6,5, SOBEL_MATRIX_Y);
        assertEquals(24, result_Y[1][1]);

        int[][] result_X_dx = convolution(image, 6,5, dx);
        assertEquals(2, result_X_dx[1][1]);

        int[][] result_Y_dy = convolution(image, 6,5, dy);
        assertEquals(6, result_Y_dy[1][1]);

    }

    @Test
    public void angleTest() {

        double[] center = new double[]{0, 0};
        Point o1 = new Point(0,0);
        Point o2 = new Point(1,0);
        Point o3 = new Point(1,1);
        Point o4 = new Point(0,1);
        Point o5 = new Point(-1,1);
        Point o6 = new Point(-1,0);
        Point o7 = new Point(-1,-1);
        Point o8 = new Point(0,-1);
        Point o9 = new Point(1,-1);


        assertEquals(0, angle(o1, center), 0.1);
        assertEquals(0, angle(o2, center), 0.1);
        assertEquals(0.5, angle(o3, center), 0.1);
        assertEquals(1.0, angle(o4, center), 0.1);
        assertEquals(1.5, angle(o5, center), 0.1);
        assertEquals(2, angle(o6, center), 0.1);
        assertEquals(2.5, angle(o7, center), 0.1);
        assertEquals(3, angle(o8, center), 0.1);
        assertEquals(3.5, angle(o9, center), 0.1);

    }

    @Test
    public void queueTest(){
        OpenCV.loadLocally();
        Imgcodecs imageCodecs = new Imgcodecs();

        Mat My_canny_Output = Mat.zeros(new Size(10, 10), CvType.CV_8UC1);
        My_canny_Output.put(1,1,1);
        My_canny_Output.put(1,3,1);
        My_canny_Output.put(3,1,1);

        My_canny_Output.put(1,5,1);
        My_canny_Output.put(2,5,1);
        My_canny_Output.put(3,5,1);
        My_canny_Output.put(5,5,1);
        My_canny_Output.put(4,5,1);
        My_canny_Output.put(6,5,1);
        My_canny_Output.put(7,5,1);
        My_canny_Output.put(8,5,1);
        My_canny_Output.put(9,5,1);

        My_canny_Output.put(7,7,1);
        My_canny_Output.put(8,7,1);
        My_canny_Output.put(9,7,1);

        My_canny_Output.put(7,0,1);
        My_canny_Output.put(8,0,1);
        My_canny_Output.put(9,0,1);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchey = new Mat();
        Mat Image = Mat.zeros(new Size(10,10), CvType.CV_8UC3);

        Imgproc.findContours(My_canny_Output, contours, hierarchey, Imgproc.RETR_LIST, CHAIN_APPROX_NONE);

        System.out.println(contours.get(1).toList());
        System.out.println(contours.get(2).toList());
        System.out.println(contours.get(3).toList());
        System.out.println(contours.get(4).toList());
        System.out.println(contours.get(5).toList());

        for(int i = 0; i < contours.size(); i++){
            Imgproc.drawContours(Image, contours, -1, new Scalar(255,255,255));
        }
        imageCodecs.imwrite("Testing.jpg", Image);
    }
}
