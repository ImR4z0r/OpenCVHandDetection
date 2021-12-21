package Camera;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractor;
import org.opencv.video.BackgroundSubtractorKNN;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.opencv.video.Video;

import javax.swing.*;
import javax.swing.JFrame;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.*;

public class Webcam {

    static double camResWidth;
    static double camResHeight;

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Camera();
    }

    public static void Camera (){

        Mat frame = new Mat();


        //Zugriff auf Camera (0 für standard)
        VideoCapture camera = new VideoCapture(0);

        //Zugriff auf Camera Auflösung
        camResWidth = camera.get(Videoio.CAP_PROP_FRAME_WIDTH);
        camResHeight = camera.get(Videoio.CAP_PROP_FRAME_HEIGHT);

        //JLabel für JFrame
        JLabel vidpanel = new JLabel();

        //JFrame + set contentPane
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setContentPane(vidpanel);
        window.setSize(1920,1080);
        window.setVisible(true);

        //Schleife für jeden Frame der Webcam
        int j = 0;
        while (true) {
            if (camera.read(frame)) {

                ImageIcon image3 = new ImageIcon(contourZeichnen(frame));
                boolean zaehlen;

                vidpanel.setIcon(image3);
                vidpanel.repaint();
            }
        }
    }

    public static BufferedImage convertMatToBufferedImage(final Mat mat) {
        // Create buffered image
        BufferedImage bufferedImage = new BufferedImage(mat.width(), mat.height(), BufferedImage.TYPE_3BYTE_BGR);

        // Write data to image
        WritableRaster raster = bufferedImage.getRaster();
        DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
        mat.get(0, 0, dataBuffer.getData());

        //Draw Rectangles on Buffered Image
        drawRectangles(bufferedImage);

        return bufferedImage;
    }

    public static BufferedImage drawRectangles(BufferedImage rectangleImage){

        Graphics2D g2d = rectangleImage.createGraphics();

        g2d.setColor(Color.red);

        int thickness = 2;
        Stroke oldStroke = g2d.getStroke();
        g2d.setStroke(new BasicStroke(thickness));


        //Rect oben mitte
        g2d.drawRect((int)Math.floor(camResWidth/2)-100,0,200,125);
        //Rect unten mitte
        g2d.drawRect((int)Math.floor(camResWidth/2)-100,(int)Math.abs(camResHeight)-126,200,125);
        //Rect links mitte
        g2d.drawRect(0,(int)Math.floor(camResHeight/2)-125,150,250);
        //Rect rechts mitte
        g2d.drawRect((int)Math.floor(camResWidth)-151,(int)Math.abs(camResHeight/2)-125,150,250);

        g2d.setStroke(oldStroke);
        g2d.dispose();

        return rectangleImage;
    }

    public static BufferedImage contourZeichnen(Mat mat){
        List<MatOfPoint> contourPoints = new ArrayList<>();
        Mat processed = mat.clone();
        Mat hierarchy = new Mat();

        // BRG zu HSV
        Imgproc.cvtColor(processed,processed, Imgproc.COLOR_BGR2HSV);

        // inRange to get the black Glove
        Core.inRange(processed, new Scalar (85,0,0), new Scalar (168,255,51), processed);

        // Find Contours
        Imgproc.findContours(processed, contourPoints, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);

        //Find biggest Contour
        double maxValue = 0;
        int maxValueIdx = 0;
        Scalar color2 = new Scalar(255,255,255);

        for(int i = 0; i < contourPoints.size(); i++){
            double contours = Imgproc.contourArea(contourPoints.get(i));
            if(maxValue < contours){
                maxValue = contours;
                maxValueIdx = i;
            }
        }

        //Convex Hull around biggest Contour
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

        // draw bounding rect
        Rect rect = null;
        ArrayList<Rect> rectArr = new ArrayList<Rect>();

        for(int i= 0; i < hullList.size(); i++){
            rect = Imgproc.boundingRect(hullList.get(i));
            rectArr.add(rect);
            if(rect.width* rect.height > 16000){
                Imgproc.rectangle(mat, rect.br(), rect.tl(), new Scalar (255,255,255),2);

                //Punkt oben Links
                Point topLeftboundingRect = rect.tl();
                // Punkt unten rechts
                Point bottomRightboundingRect = rect.br();

                //Punkt oben Links
                Point topLeftRectTopCenter = new Point ((int)Math.floor(camResWidth/2)-100 , 0);
                //Punkt unten rechts
                Point bottomRightRectTopCenter = new Point ((int)Math.floor(camResWidth/2)+100 , 125);

                //Punkt oben Links
                Point topLeftRectbottomCenter = new Point ((int)Math.floor(camResWidth/2)-100, (int)Math.abs(camResHeight)-126);
                //Punkt unten rechts
                Point bottemRightRectbottomCenter = new Point ((int)Math.floor(camResWidth/2)+100, (int)Math.abs(camResHeight));

                //Punkt oben Links
                Point topLeftRectLeftCenter = new Point (0,(int)Math.floor(camResHeight/2)-125);
                //Punkt unten rechts
                Point bottomRightRectLeftCenter = new Point (150,(int)Math.floor(camResHeight/2)+125);

                //Punkt oben Links
                Point topLeftRectRightCenter = new Point ((int)Math.floor(camResWidth)-151,(int)Math.abs(camResHeight/2)-125);
                //Punkt unten rechts
                Point bottomRightRightCenter = new Point ((int)Math.floor(camResWidth),(int)Math.abs(camResHeight/2)+125);


                if(topLeftboundingRect.x >= topLeftRectTopCenter.x && topLeftboundingRect.x <= bottomRightRectTopCenter.x && topLeftboundingRect.y >= topLeftRectTopCenter.y && topLeftboundingRect.y <= bottomRightRectTopCenter.y){

                    int j = 0;
                    j++;
                    if(j == 30){
                        System.out.println("Das funktioniert");
                    }
                }

                if(topLeftboundingRect.x >= topLeftRectbottomCenter.x && topLeftboundingRect.x <= bottemRightRectbottomCenter.x && topLeftboundingRect.y >= topLeftRectbottomCenter.y && topLeftboundingRect.y <= bottemRightRectbottomCenter.y){

                    j++;
                    if(j == 30){
                        System.out.println("Das funktioniert");
                    }
                }

                if(topLeftboundingRect.x >= topLeftRectLeftCenter.x && topLeftboundingRect.x <= bottomRightRectLeftCenter.x && topLeftboundingRect.y >= topLeftRectLeftCenter.y && topLeftboundingRect.y <= bottomRightRectLeftCenter.y){
                    j++;
                    System.out.println(j);
                    if(j == 30){
                        System.out.println("Das funktioniert");
                    }
                }
                if(topLeftboundingRect.x >= topLeftRectRightCenter.x && topLeftboundingRect.x <= bottomRightRightCenter.x && topLeftboundingRect.y >= topLeftRectRightCenter.y && topLeftboundingRect.y <= bottomRightRightCenter.y){

                    j++;
                    if(j == 30){
                        System.out.println("Das funktioniert");
                    }
                }
            }
        }



        // Draw Contours to mat
        Imgproc.drawContours(mat, hullList, maxValueIdx , color2,2);

        // Create Buffered Image
        BufferedImage processedesImage = convertMatToBufferedImage(mat);

        return processedesImage;
    }


}
