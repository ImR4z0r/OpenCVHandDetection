package HandDetection;

import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import javax.swing.JFrame;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;

public class CameraHandDetection {
    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        JPanel cameraFeed = new JPanel();
        JPanel processedFeed = new JPanel();

        VideoCapture camera = new VideoCapture(0);
        Mat frame = new Mat();


        JFrame window = new JFrame("Hand Detection");
        window.setSize(1920, 1080);
        window.setResizable(true);
        window.add(cameraFeed);
        window.setVisible(true);
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setLayout(new GridLayout(1,2));



        while(true){
            camera.read(frame);

            Mat processed = Contours.processImage(frame);

            Contours.markOuterContour(processed, frame);

            CameraHandDetection.drawImage(frame, cameraFeed);

            drawImage(processed, processedFeed);
        }
    }


    public static void drawImage(final Mat mat, final JPanel panel) {
        // Get buffered image from mat frame
        final BufferedImage image = CameraHandDetection.convertMatToBufferedImage(mat);

        // Draw image to panel
        final Graphics graphics = panel.getGraphics();
        graphics.drawImage(image, 0, 0, panel);
    }


    private static BufferedImage convertMatToBufferedImage(final Mat mat) {
        // Create buffered image
        final BufferedImage bufferedImage = new BufferedImage(
                mat.width(),
                mat.height(),
                mat.channels() == 1 ? BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_3BYTE_BGR
        );

        // Write data to image
        final WritableRaster raster = bufferedImage.getRaster();
        final DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
        mat.get(0, 0, dataBuffer.getData());

        return bufferedImage;
    }

}

