package jmegner;

public class IU
{
    public static final double s_Gamma = 2.2;
    public static final double s_GammaInverse = 1 / s_Gamma;
    public static final int s_AMask   = 0xff000000;
    public static final int s_RMask   = 0x00ff0000;
    public static final int s_GMask   = 0x0000ff00;
    public static final int s_BMask   = 0x000000ff;
    public static final int s_RgbMask = 0x00ffffff;
    public static final int s_Black   = 0x00000000;
    public static final int s_White   = 0x00ffffff;


    public static int ClampU8(int val) { return Math.max(0, Math.min(255, val)); }
    public static double ClampF1(double val) { return Math.max(0.0, Math.min(1.0, val)); }

    public static int GetA(int argb) { return ((s_AMask & argb) >> 24) & 0xff; }
    public static int GetR(int argb) { return (s_RMask & argb) >> 16; }
    public static int GetG(int argb) { return (s_GMask & argb) >> 8; }
    public static int GetB(int argb) { return (s_BMask & argb); }
    public static int GetRgb(int argb) { return (s_RgbMask & argb); }
    public static boolean IsBlack(int argb) { return GetRgb(argb) == s_Black; }
    public static boolean IsWhite(int argb) { return GetRgb(argb) == s_White; }


    public static String RgbToString(int rgb)
    {
        return "{" + Integer.toString(GetR(rgb))
            + "," + Integer.toString(GetG(rgb))
            + "," + Integer.toString(GetB(rgb))
            + "}";
    }


    public static int GammaCorrectedToLinear(int val)
    {
        return s_gammaCorrectedToLinear[val];
    }


    public static int LinearToGammaCorrected(int val)
    {
        return s_linearToGammaCorrected[val];
    }


    public static double EG(double val)
    {
        return Math.pow(val, s_Gamma);
    }


    public static double EGI(double val)
    {
        return Math.pow(val, s_GammaInverse);
    }


    public static int GetRgb(int r, int g, int b)
    {
        return GetArgb(0, r, g, b);
    }


    public static int GetArgb(int a, int rgb)
    {
        return (ClampU8(a) << 24) | (rgb & s_RgbMask);
    }


    public static int GetArgb(int a, int r, int g, int b)
    {
        return (ClampU8(a) << 24)
            | (ClampU8(r) << 16)
            | (ClampU8(g) << 8)
            | (ClampU8(b));
    }


    private static final char[] s_gammaCorrectedToLinear;
    private static final char[] s_linearToGammaCorrected;

    static
    {
        final int tableLen = 256;
        s_gammaCorrectedToLinear = new char[tableLen];
        s_linearToGammaCorrected = new char[tableLen];

        for(int i = 0; i < tableLen; i++)
        {
            s_gammaCorrectedToLinear[i] = (char) (32767.0
                * (0.99052 * Math.pow(i / 255.0, s_Gamma) + 0.003974));

            s_linearToGammaCorrected[i] = (char) (255.0
                * Math.pow(i / 255.0, s_GammaInverse));
        }

    }


}
