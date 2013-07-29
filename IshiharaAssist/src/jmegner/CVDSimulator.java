package jmegner;

import java.awt.image.BufferedImage;


public class CVDSimulator
{

    public enum CVDType { PROTAN, DEUTAN, }


    private CVDType m_simType;
    private double m_strength;
    private int m_k1;
    private int m_k2;
    private int m_k3;
    private double m_p1;
    private double m_p2;
    private double m_p3;


    public CVDSimulator()
    {
        this(CVDType.DEUTAN, 1.0);
    }


    public CVDSimulator(CVDType simType, double strength)
    {
        m_simType = simType;
        m_strength = strength;

        if(m_strength < 0 || m_strength > 1)
        {
            throw new IllegalArgumentException();
        }

        if(m_simType == CVDType.PROTAN)
        {
            m_k1 = 3683;
            m_k2 = 29084;
            m_k3 = 131;
        }
        else if(m_simType == CVDType.DEUTAN)
        {
            m_k1 = 9591;
            m_k2 = 23173;
            m_k3 = -730;
        }
        else
        {
            throw new IllegalArgumentException();
        }

        m_p1 = ((double) m_k1) / (m_k1 + m_k2);
        m_p2 = 1 - m_p1;
        m_p3 = m_k3 / 32768.0;
    }


    public BufferedImage GetCvdImage(BufferedImage inImage)
    {
        int width = inImage.getWidth();
        int height = inImage.getHeight();

        BufferedImage outImage = new BufferedImage(
            width, height, BufferedImage.TYPE_INT_ARGB);

        //DataBufferInt inBuffer = (DataBufferInt) inImage.getRaster().getDataBuffer();
        //DataBufferInt outBuffer = (DataBufferInt) outImage.getRaster().getDataBuffer();
        //int[] inDataX = inBuffer.getData();
        //int[] outDataX = outBuffer.getData();

        //int[] inData = new int[inImage.getWidth() * inImage.getHeight()];
        int[] outData = new int[width * height];

        int[] inData = inImage.getRGB(
            0, 0,
            width, height,
            null,
            0, width);

        int prevIn = -2;
        int prevOut = -2;

        for(int dataIdx = 0; dataIdx < inData.length; dataIdx++)
        {
            int argbIn = inData[dataIdx];
            int argbOut = -1;

            if(argbIn == prevIn)
            {
                argbOut = prevOut;
            }
            else
            {
                argbOut = GetCvdArgb(argbIn);
            }

            outData[dataIdx] = argbOut;
            prevIn = argbIn;
            prevOut = argbOut;
        }

        outImage.setRGB(0, 0, width, height, outData, 0, width);
        return outImage;
    }


    public int GetCvdArgb(int normalArgb)
    {
        final int normalA = IU.GetA(normalArgb);
        final int normalR = IU.GetR(normalArgb);
        final int normalG = IU.GetG(normalArgb);
        final int normalB = IU.GetB(normalArgb);

        return GetCvdArgb(normalA, normalR, normalG, normalB);
    }


    public int GetCvdArgb(int normalR, int normalG, int normalB)
    {
        return GetCvdArgb(0, normalR, normalG, normalB);
    }


    public int GetCvdArgb(int normalA, int normalR, int normalG, int normalB)
    {
        final int linearR = IU.GammaCorrectedToLinear(normalR);
        final int linearG = IU.GammaCorrectedToLinear(normalG);
        final int linearB = IU.GammaCorrectedToLinear(normalB);

        final int cvdLinearRG = IU.ClampU8((m_k1 * linearR
            + m_k2 * linearG) >> 22);
        final int cvdLinearB = IU.ClampU8((m_k3 * linearR - m_k3 * linearG
            + 32768 * linearB) >> 22);

        final int blendedLinearR = (int) Math.round(m_strength * cvdLinearRG
            + (1 - m_strength) * linearR);
        final int blendedLinearG = (int) Math.round(m_strength * cvdLinearRG
            + (1 - m_strength) * linearG);
        final int blendedLinearB = (int) Math.round(m_strength * cvdLinearB
            + (1 - m_strength) * linearB);

        final int blendedGammaCorrectedR
            = IU.LinearToGammaCorrected(blendedLinearR);
        final int blendedGammaCorrectedG
            = IU.LinearToGammaCorrected(blendedLinearG);
        final int blendedGammaCorrectedB
            = IU.LinearToGammaCorrected(blendedLinearB);

        final int cvdArgb = IU.GetArgb(
            normalA,
            blendedGammaCorrectedR,
            blendedGammaCorrectedG,
            blendedGammaCorrectedB);

        return cvdArgb;
    }


    public int[] getFamilyForBlindYB(int blindY, int blindB)
    {
        return null;
    }


    private int getMinGForBlindY(int blindY)
    {
        return (int) Math.ceil(Math.pow(
            (Math.pow(blindY, IU.s_Gamma)- m_p1) / m_p2,
            IU.s_GammaInverse));
    }


    private int getMaxGForBlindY(int blindY)
    {
        return (int) Math.floor(Math.pow(m_p2, -IU.s_GammaInverse) * blindY);
    }


    private int getRForBlindYAndNormalG(int blindY, int normalG)
    {
        final double y = blindY / 255.0;
        final double g = normalG / 255.0;

        return IU.ClampU8((int) Math.round(Math.pow(
            (Math.pow(y, IU.s_Gamma) - m_p2 * Math.pow(g, IU.s_Gamma)) / m_p1,
            IU.s_GammaInverse)));
    }


    private int getNormalBForNormalRGAndBlindB(int normalR, int normalG, int blindB)
    {
        final double nr = normalR / 255.0;
        final double ng = normalG / 255.0;
        final double bb = blindB / 255.0;

        final double nb = IU.EGI(IU.ClampF1(
            IU.EG(bb) - m_p3 * (IU.EG(nr) - IU.EG(ng))));

        final int normalB = (int) Math.round(255.0 * nb);

        return normalB;
    }


}

