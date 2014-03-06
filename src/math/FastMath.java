package math;

public class FastMath
{
    public static void main(String[] args)
    {
        double min = -100;
        double max = +100;
        double step = 0.12f;

        for (int i = 0; i < 8; i++)
        {
            long t0A = System.nanoTime() / 1000000L;
            double sumA = 0.0f;
            for (double y = min; y < max; y += step)
                for (double x = min; x < max; x += step)
                    sumA += atan2(y, x);
            long t1A = System.nanoTime() / 1000000L;

            long t0B = System.nanoTime() / 1000000L;
            double sumB = 0.0f;
            for (double y = min; y < max; y += step)
                for (double x = min; x < max; x += step)
                    sumB += Math.atan2(y, x);
            long t1B = System.nanoTime() / 1000000L;

            System.out.println();
            System.out.println("FastMath: " + (t1A - t0A) + "ms, sum=" + sumA);
            System.out.println("JavaMath: " + (t1B - t0B) + "ms, sum=" + sumB);
            System.out.println("factor: " + (double)(t1B - t0B) / (t1A - t0A));
        }
    }

    private static final int           SIZE                 = 1024;
    private static final double        STRETCH            = (double) Math.PI;
    // Output will swing from -STRETCH to STRETCH (default: Math.PI)
    // Useful to change to 1 if you would normally do "atan2(y, x) / Math.PI"

    // Inverse of SIZE
    private static final int        EZIS            = -SIZE;
    private static final double[]    ATAN2_TABLE_PPY    = new double[SIZE + 1];
    private static final double[]    ATAN2_TABLE_PPX    = new double[SIZE + 1];
    private static final double[]    ATAN2_TABLE_PNY    = new double[SIZE + 1];
    private static final double[]    ATAN2_TABLE_PNX    = new double[SIZE + 1];
    private static final double[]    ATAN2_TABLE_NPY    = new double[SIZE + 1];
    private static final double[]    ATAN2_TABLE_NPX    = new double[SIZE + 1];
    private static final double[]    ATAN2_TABLE_NNY    = new double[SIZE + 1];
    private static final double[]    ATAN2_TABLE_NNX    = new double[SIZE + 1];

    static
    {
        for (int i = 0; i <= SIZE; i++)
        {
            double f = (double)i / SIZE;
            ATAN2_TABLE_PPY[i] = (double)(StrictMath.atan(f) * STRETCH / StrictMath.PI);
            ATAN2_TABLE_PPX[i] = STRETCH * 0.5f - ATAN2_TABLE_PPY[i];
            ATAN2_TABLE_PNY[i] = -ATAN2_TABLE_PPY[i];
            ATAN2_TABLE_PNX[i] = ATAN2_TABLE_PPY[i] - STRETCH * 0.5f;
            ATAN2_TABLE_NPY[i] = STRETCH - ATAN2_TABLE_PPY[i];
            ATAN2_TABLE_NPX[i] = ATAN2_TABLE_PPY[i] + STRETCH * 0.5f;
            ATAN2_TABLE_NNY[i] = ATAN2_TABLE_PPY[i] - STRETCH;
            ATAN2_TABLE_NNX[i] = -STRETCH * 0.5f - ATAN2_TABLE_PPY[i];
        }
    }

    /**
     * ATAN2
     */

    public static final double atan2(double y, double x)
    {
        if (x >= 0)
        {
            if (y >= 0)
            {
                if (x >= y)
                    return ATAN2_TABLE_PPY[(int)(SIZE * y / x + 0.5)];
                else
                    return ATAN2_TABLE_PPX[(int)(SIZE * x / y + 0.5)];
            }
            else
            {
                if (x >= -y)
                    return ATAN2_TABLE_PNY[(int)(EZIS * y / x + 0.5)];
                else
                    return ATAN2_TABLE_PNX[(int)(EZIS * x / y + 0.5)];
            }
        }
        else
        {
            if (y >= 0)
            {
                if (-x >= y)
                    return ATAN2_TABLE_NPY[(int)(EZIS * y / x + 0.5)];
                else
                    return ATAN2_TABLE_NPX[(int)(EZIS * x / y + 0.5)];
            }
            else
            {
                if (x <= y) // (-x >= -y)
                    return ATAN2_TABLE_NNY[(int)(SIZE * y / x + 0.5)];
                else
                    return ATAN2_TABLE_NNX[(int)(SIZE * x / y + 0.5)];
            }
        }
    }
}
