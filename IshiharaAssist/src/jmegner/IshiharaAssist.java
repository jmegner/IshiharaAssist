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
        //CvdSimulateImages(args);
        DoReverseIshiharaPlate(args);
    }


    public static void DoReverseIshiharaPlate(String[] inFileNames)
        throws IOException
    {
        if(inFileNames.length < 1)
        {
            return;
        }

        CVDSimulator cvdSim = new CVDSimulator(CVDSimulator.CVDType.DEUTAN, 1.0);
        Scanner scan = new Scanner(new File(inFileNames[0]));

        int foreR = IU.ClampU8(scan.nextInt());
        int foreG = IU.ClampU8(scan.nextInt());
        int foreB = IU.ClampU8(scan.nextInt());
        int backR = IU.ClampU8(scan.nextInt());
        int backG = IU.ClampU8(scan.nextInt());
        int backB = IU.ClampU8(scan.nextInt());

        int origForeRgb = IU.GetRgb(foreR, foreG, foreB);
        int origBackRgb = IU.GetRgb(backR, backG, backB);

        int blindForeRgb = cvdSim.GetCvdArgb(origForeRgb);
        int blindBackRgb = cvdSim.GetCvdArgb(origBackRgb);

        int[] foreFamily = cvdSim.GetFamilyForBlindYB(
            IU.GetR(blindForeRgb),
            IU.GetB(blindForeRgb));

        int[] backFamily = cvdSim.GetFamilyForBlindYB(
            IU.GetR(blindBackRgb),
            IU.GetB(blindBackRgb));

        printFamilyInfo("fore", origForeRgb, blindForeRgb, foreFamily);
        printFamilyInfo("back", origBackRgb, blindBackRgb, backFamily);

        for(int fileIdx = 1; fileIdx < inFileNames.length; fileIdx++)
        {
            String inFileName = inFileNames[fileIdx];
            System.out.println("processing " + inFileName);

            File inFile = new File(inFileName);

            if(!inFile.canRead())
            {
                throw new IllegalArgumentException("cannot read " + inFileName);
            }

            File outFile = new File(inFileName + ".reverse_ishihara.png");
            BufferedImage inImage = ImageIO.read(inFile);
            BufferedImage outImage = GetReverseIshiharaPlate(
                inImage, foreFamily, backFamily);

            ImageIO.write(outImage, "png", outFile);
        }

        System.out.println("done");
    }


    public static BufferedImage GetReverseIshiharaPlate(
        BufferedImage inImage, int[] foreFamily, int[] backFamily)
    {
        int width = inImage.getWidth();
        int height = inImage.getHeight();

        BufferedImage outImage = new BufferedImage(
            width, height, BufferedImage.TYPE_INT_ARGB);

        int[] outData = new int[width * height];

        int[] inData = inImage.getRGB(
            0, 0,
            width, height,
            null,
            0, width);

        for(int dataIdx = 0; dataIdx < inData.length; dataIdx++)
        {
            int argbIn = inData[dataIdx];
            int argbOut = -1;

            if(argbIn == -1)
            {
                throw new IllegalArgumentException("weird pixel value");
            }
            if(argbIn == IU.s_White)
            {
                argbOut = IU.s_White;
            }
            else if(argbIn == IU.s_Black)
            {
                argbOut = backFamily[0];
            }
            else
            {
                argbOut = foreFamily[0];
            }

            outData[dataIdx] = argbOut;
        }

        outImage.setRGB(0, 0, width, height, outData, 0, width);
        return outImage;
    }


    public static void printFamilyInfo(
        String familyName, int origRgb, int blindRgb, int[] family)
    {
        System.out.println(familyName + "...");
        System.out.println("    orig =" + IU.RgbToString(origRgb));
        System.out.println("    blind=" + IU.RgbToString(blindRgb));
        System.out.println("    family(" + Integer.toString(family.length) + ")...");

        for(int familyIdx = 0; familyIdx < family.length; familyIdx++)
        {
            System.out.println("        " + IU.RgbToString(family[familyIdx]));
        }
    }


    public static void CvdSimulateImages(String[] inFileNames)
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
