package jmegner;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.imageio.ImageIO;

public class IshiharaAssist {

    public static void main(String[] args)
        throws IOException
    {
        System.out.println("pwd: " + System.getProperty("user.dir"));
        //keyboardRgbLoop();
        cvdSimulateImages(args);
    }


    public static void cvdSimulateImages(String[] inFileNames)
        throws IOException
    {
        CVDSimulator cvdSim = new CVDSimulator(CVDSimulator.CVDType.DEUTAN, 1.0);

        for(String inFileName : inFileNames)
        {
            System.out.println("processing " + inFileName);

            File inFile = new File(inFileName);
            File outFile = new File(inFileName + ".cvd.png");
            BufferedImage inImage = ImageIO.read(inFile);
            BufferedImage outImage = cvdSim.GetCvdImage(inImage);

            ImageIO.write(outImage, "png", outFile);
        }

        System.out.println("done processing images");
    }


    public static void keyboardRgbLoop()
    {
        Scanner scan = new Scanner(System.in);
        CVDSimulator cvdSim = new CVDSimulator(CVDSimulator.CVDType.DEUTAN, 1.0);

        while(true)
        {
            System.out.print("Enter RGB triple: ");

            if(!scan.hasNextInt())
            {
                break;
            }

            int inR = IU.ClampU8(scan.nextInt());
            int inG = IU.ClampU8(scan.nextInt());
            int inB = IU.ClampU8(scan.nextInt());
            int outRgb = cvdSim.GetCvdArgb(inR, inG, inB);
            int outR = IU.GetR(outRgb);
            int outG = IU.GetG(outRgb);
            int outB = IU.GetB(outRgb);

            System.out.println(Integer.toString(outR)
                + " " + Integer.toString(outG)
                + " " + Integer.toString(outB));
        }
    }

}
