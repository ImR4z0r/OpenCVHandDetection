package Camera;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractor;
import org.opencv.video.Video;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;

public class Webcam2 {
    public static void main (String[] args){
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Mat img = Imgcodecs.imread("C:/Users/titus/Desktop/Download4.jpg");

        List<MatOfPoint> contourPoints = new ArrayList<>();
        Mat verarbeitet = new Mat();
        Mat hierarchy = new Mat();
        Mat fgMask = new Mat();
        int threshold = 255/3;
        int threshold2 = 255;


        // Background wegmachen
        int useM0G2 = verarbeitet.channels();
        System.out.println(useM0G2);
        BackgroundSubtractor backgroundSub;

        if(useM0G2 == 1) {
            backgroundSub = Video.createBackgroundSubtractorMOG2();
        }
        else{
            backgroundSub = Video.createBackgroundSubtractorKNN();
        }
        backgroundSub.apply(img, fgMask);


        Imgproc.cvtColor(img,verarbeitet, Imgproc.COLOR_BGR2GRAY);
        verarbeitet.convertTo(verarbeitet,-1,1,0);
        Imgproc.Canny(verarbeitet, verarbeitet,threshold, threshold2);

        //Imgproc.blur(verarbeitet,verarbeitet, new Size(3,3));
        //src -> original Image; dst -> outputImage

        Imgproc.findContours(verarbeitet, contourPoints, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);

        List<MatOfPoint> hullList = new ArrayList<>();
        for(MatOfPoint contour: contourPoints){
            MatOfInt hull = new MatOfInt();
            Imgproc.convexHull(contour, hull);

            Point[] contourArray = contour.toArray();
            Point[] hullPoints = new Point[hull.rows()];
            List<Integer> hullContourIdxList = hull.toList();
            for (int i = 0; i < hullContourIdxList.size(); i++) {
                hullPoints[i] = contourArray[hullContourIdxList.get(i)];
            }
            hullList.add(new MatOfPoint(hullPoints));
        }


        Mat drawing = Mat.zeros(verarbeitet.size(), CvType.CV_8UC3);
        double maxValue = 0;
        int maxValueIdx = 0;
        Scalar color = new Scalar(0,0,255);
        Scalar color2 = new Scalar(255,255,255);

        for(int i = 0; i < contourPoints.size(); i++){
            double contours = Imgproc.contourArea(hullList.get(i));
            //System.out.println(contours);
            if(maxValue < contours){
                maxValue = contours;
                maxValueIdx = i;
            }
            Imgproc.drawContours(drawing, contourPoints, i, color,1);

        }
        Imgproc.drawContours(drawing, hullList, maxValueIdx , color2);


        BufferedImage noBackground = convertMatToBufferedImage(fgMask);
        BufferedImage verarbeitetesBild = convertMatToBufferedImage(drawing);

        ImageIcon noBackPanel = new ImageIcon(noBackground);
        JLabel noBackLabel = new JLabel();
        noBackLabel.setIcon(noBackPanel);

        ImageIcon picPanel = new ImageIcon(verarbeitetesBild);
        JLabel picLabel = new JLabel();
        picLabel.setIcon(picPanel);

        JLabel picLabelOriginal = new JLabel();
        ImageIcon originalImage = new ImageIcon(convertMatToBufferedImage(img));
        picLabelOriginal.setIcon(originalImage);

        JLabel picLabelBearbeitet = new JLabel();
        ImageIcon bearbeitetesImage = new ImageIcon(convertMatToBufferedImage(verarbeitet));
        picLabelBearbeitet.setIcon(bearbeitetesImage);

        JFrame window = new JFrame();
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setLayout(new GridLayout(1,4));
        window.add(picLabel);
        window.add(picLabelOriginal);
        window.add(picLabelBearbeitet);
        window.add(noBackLabel);
        window.setSize(1920,1080);
        window.setVisible(true);

    }
    public static BufferedImage convertMatToBufferedImage(final Mat mat) {
        // Create buffered image
        BufferedImage bufferedImage = new BufferedImage(
                mat.width(),
                mat.height(),
                mat.channels() == 1 ? BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_3BYTE_BGR
        );

        // Write data to image
        WritableRaster raster = bufferedImage.getRaster();
        DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
        mat.get(0, 0, dataBuffer.getData());

        return bufferedImage;
    }
}

