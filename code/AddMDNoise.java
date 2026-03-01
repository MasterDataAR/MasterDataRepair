import java.util.ArrayList;
import java.util.Random;

public class AddMDNoise {
    public static ArrayList<ArrayList<Double>> addNoise(
            ArrayList<ArrayList<Double>> md,
            double noiseRate,
            double[] lowerBound,
            double[] upperBound) {
        Random rand = new Random();
        ArrayList<ArrayList<Double>> noisyMd = new ArrayList<>();

        for (ArrayList<Double> tuple : md) {
            ArrayList<Double> newTuple = new ArrayList<>();
            for (int d = 0; d < tuple.size(); d++) {
                double val = tuple.get(d);

                if (rand.nextDouble() < noiseRate) {
                    // 加扰动（均匀扰动 or 高斯都可以）
                    double range = upperBound[d] - lowerBound[d];
                    double noise = (rand.nextDouble() - 0.5) * 0.5 * range;
                    val = Math.min(upperBound[d], Math.max(lowerBound[d], val + noise));
                }

                newTuple.add(val);
            }
            noisyMd.add(newTuple);
        }
        return noisyMd;
    }

}
