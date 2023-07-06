package Functions;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import static java.lang.Math.abs;
import static java.lang.Math.random;

public class K_means{

    private static int distance(int[] pix1, int[] pix2) {
        return abs(pix1[0] - pix2[0]) + abs(pix1[1] - pix2[1]) + abs(pix1[2] - pix2[2]);
    }

    public static int[][] k_means(int[][] image, int number_of_clusters) {
        int[][] centers = new int[number_of_clusters][3];
        int[][] new_centers = new int[number_of_clusters][4];

        for(int i = 0; i < number_of_clusters; i++) {
            centers[i] = new int[]{(int) random() * 255, (int) random() * 255, (int) random() * 255};
            new_centers[i] = new int[]{0,0,0,0};
        }

        int pixel_amount = image.length;
        int[] group = new int[pixel_amount];
        AtomicBoolean change = new AtomicBoolean(true);


        while (change.get() == true) {
            change.set(false);
            IntStream.range(0,pixel_amount).parallel().
                    forEach( x -> {

                        int index = 0;
                        int min;
                        int new_distance;
                        min = distance(image[x], centers[0]);

                        for(int i = 1; i < number_of_clusters; i++){
                            new_distance = distance(image[x], centers[i]);

                            if(new_distance < min){
                                min = new_distance;
                                index = i;
                            }
                        }

                        new_centers[index][0] += image[x][0];
                        new_centers[index][1] += image[x][1];
                        new_centers[index][2] += image[x][2];
                        new_centers[index][3] += 1;
                        if (group[x] != index) {
                            change.set(true);
                        }
                        group[x] = index;

                    });

            for(int i = 0; i < number_of_clusters; i++) {
                if(new_centers[i][3] != 0) {
                    centers[i][0] = new_centers[i][0] / new_centers[i][3];
                    centers[i][1] = new_centers[i][1] / new_centers[i][3];
                    centers[i][2] = new_centers[i][2] / new_centers[i][3];

                    new_centers[i][0] = 0;
                    new_centers[i][1] = 0;
                    new_centers[i][2] = 0;
                    new_centers[i][3] = 0;
                }
            }
            for (int i = 0; i < pixel_amount; i++) {

            }
        }

        int[][] output = new int[pixel_amount][3];

        for(int i = 0; i < pixel_amount; i++) {
            int cluster = group[i];
            output[i] = new int[] {centers[cluster][0], centers[cluster][1], centers[cluster][2]};
        }

        return output;
    }
}
