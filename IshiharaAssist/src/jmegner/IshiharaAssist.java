package jmegner;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.Stack;

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

            BufferedImage inImage = ImageIO.read(inFile);
            File outFile = new File(inFileName + ".reverse_ishihara.png");
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

        int[] outData = new int[width * height];
        boolean[] touched = new boolean[width * height];

        int[] inData = inImage.getRGB(
            0, 0,
            width, height,
            null,
            0, width);

        for(int dataIdx = 0; dataIdx < inData.length; dataIdx++)
        {
            if(touched[dataIdx])
            {
                continue;
            }

            int rgbIn = IU.GetRgb(inData[dataIdx]);
            int rgbOut = 0;

            if(rgbIn == IU.s_White)
            {
                rgbOut = IU.s_White;
            }
            else if(rgbIn == IU.s_Black)
            {
                int randIdx = (int) (Math.random() * backFamily.length);
                rgbOut = backFamily[randIdx];
            }
            else
            {
                int randIdx = (int) (Math.random() * foreFamily.length);
                rgbOut = foreFamily[randIdx];
            }

            infect(inData, outData, touched, width,
                dataIdx, rgbIn, rgbOut);
        }

        BufferedImage outImage = new BufferedImage(
            width, height, BufferedImage.TYPE_INT_RGB);

        outImage.setRGB(0, 0, width, height, outData, 0, width);
        return outImage;
    }


    public static void infect(
        int[] inData, int[] outData, boolean[] touched, int width,
        int dataIdx, int rgbIn, int rgbOut)
    {
        int height = touched.length / width;
        Stack<Integer> idxStack = new Stack<Integer>();

        idxStack.push(dataIdx);
        touched[dataIdx] = true;

        while(!idxStack.isEmpty())
        {
            int i = idxStack.pop();
            int r = i / width;
            int c = i % width;

            outData[i] = rgbOut;

            int iLeft = i - 1;
            int iRight = i + 1;
            int iUp = i - width;
            int iDown = i + width;

            // left
            if(c > 0 && !touched[iLeft] && IU.GetRgb(inData[iLeft]) == rgbIn)
            {
                touched[iLeft] = true;
                idxStack.push(iLeft);
            }

            // right
            if(c < width - 1 && !touched[iRight] && IU.GetRgb(inData[iRight]) == rgbIn)
            {
                touched[iRight] = true;
                idxStack.push(iRight);
            }

            // up
            if(r > 0 && !touched[iUp] && IU.GetRgb(inData[iUp]) == rgbIn)
            {
                touched[iUp] = true;
                idxStack.push(iUp);
            }

            // down
            if(r < height - 1 && !touched[iDown] && IU.GetRgb(inData[iDown]) == rgbIn)
            {
                touched[iDown] = true;
                idxStack.push(iDown);
            }
        }
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
