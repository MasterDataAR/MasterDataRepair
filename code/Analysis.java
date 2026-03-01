import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Analysis {
    private int columnCnt;
    private ArrayList<ArrayList<Double>> td = new ArrayList<>();
    private ArrayList<ArrayList<Double>> md = new ArrayList<>();
    private ArrayList<ArrayList<Double>> td_cleaned = new ArrayList<>();
    private ArrayList<ArrayList<Double>> test_td = new ArrayList<>();
    private double MAE;
    private double RMSE;
    private double[] tolerance;
    private double tolerance_rate;
    private double[] std;

    public Analysis(int columnCnt, ArrayList<ArrayList<Double>> td, ArrayList<ArrayList<Double>> md,
            ArrayList<ArrayList<Double>> test_td, ArrayList<ArrayList<Double>> td_cleaned, double tolerance_rate) {
        this.columnCnt = columnCnt;
        this.td = td;
        this.test_td = test_td;
        this.md = md;
        this.td_cleaned = td_cleaned;
        this.tolerance = new double[columnCnt];
        this.tolerance_rate = tolerance_rate;
        this.MAE = 0d;
        this.RMSE = 0d;
        this.calStd();
        this.calTolerance();
        this.analysis();
    }

    public String getMAE() {
        String s = String.format("%.3f", MAE);
        return s;
    }

    public String getRMSE() {
        String s = String.format("%.3f", RMSE);
        return s;
    }

    public void analysis() {
        ArrayList<ArrayList<Double>> arrayLists = this.td;
        int consistentCnt = 0;
        for (int i = 0; i < arrayLists.size(); i++) {
            ArrayList<Double> o_tuple = arrayLists.get(i);
            if (!checkIfDirty(o_tuple)) {
                consistentCnt++;
                ArrayList<Double> r_tuple = this.td_cleaned.get(i);
                for (int j = 0; j < columnCnt; j++) {
                    this.MAE += Math.abs(o_tuple.get(j) - r_tuple.get(j)) / this.std[j];
                    this.RMSE += Math.pow((o_tuple.get(j) - r_tuple.get(j)) / this.std[j], 2);
                }
            }
        }
        int columnCnt = consistentCnt * this.columnCnt;
        this.MAE = this.MAE / columnCnt;
        this.RMSE = Math.sqrt(this.RMSE / columnCnt);
    }

    public void calStd() {
        std = new double[this.columnCnt];
        for (int i = 0; i < this.columnCnt; i++) {
            std[i] = Math.sqrt(this.varianceImperative(getColumn(i)));
        }
    }

    public void calTolerance() {
        this.tolerance = new double[columnCnt];
        for (int i = 0; i < this.columnCnt; i++) {
            this.tolerance[i] = this.std[i] * tolerance_rate;
        }
    }

    public boolean checkIfComplete(ArrayList<Double> t_tuple) {
        for (Double value : t_tuple) {
            if (Double.isNaN(value)) {
                return false;
            }
        }
        return true;
    }

    public boolean checkIfConsistent(ArrayList<Double> t_tuple) {
        for (ArrayList<Double> m_tuple : this.md) {
            boolean isConsistent = true;
            for (int k = 0; k < t_tuple.size(); k++) {
                if (Math.abs(t_tuple.get(k) - m_tuple.get(k)) > this.tolerance[k]) {
                    isConsistent = false;
                    break;
                }
            }
            if (isConsistent) {
                return true;
            }
        }
        return false;
    }

    public boolean checkIfDirty(ArrayList<Double> t_tuple) {
        return !checkIfComplete(t_tuple) || !checkIfConsistent(t_tuple);
    }

    private double varianceImperative(double[] value) {
        double average = 0.0;
        int cnt = 0;
        for (double p : value) {
            if (!Double.isNaN(p)) {
                cnt += 1;
                average += p;
            }
        }
        if (cnt == 0) {
            return 0d;
        }
        average /= cnt;

        double variance = 0.0;
        for (double p : value) {
            if (!Double.isNaN(p)) {
                variance += (p - average) * (p - average);
            }
        }
        return variance / cnt;
    }

    private double[] getColumn(int pos) {
        double[] column = new double[this.td.size()];
        for (int i = 0; i < this.td.size(); i++) {
            column[i] = this.td.get(i).get(pos);
        }
        return column;
    }

    public void writeRepairResultToFile(String targetFileName) {
        File writeFile = new File(targetFileName);
        try {
            BufferedWriter writeText = new BufferedWriter(new FileWriter(writeFile));
            for (int j = 0; j < this.td_cleaned.size(); j++) {
                writeText.newLine(); // 换行
                ArrayList<Double> tuple = this.td_cleaned.get(j);
                writeText.write(j + ",");
                for (int i = 0; i < columnCnt - 1; i++) {
                    if (!Double.isNaN(tuple.get(i))) {
                        writeText.write(String.valueOf(tuple.get(i)));
                    }
                    writeText.write(",");
                }
                if (!Double.isNaN(tuple.get(columnCnt - 1))) {
                    writeText.write(String.valueOf(tuple.get(columnCnt - 1)));
                }
            }
            writeText.flush();
            writeText.close();
        } catch (IOException e) {
            System.out.println("Error");
        }
    }
}
