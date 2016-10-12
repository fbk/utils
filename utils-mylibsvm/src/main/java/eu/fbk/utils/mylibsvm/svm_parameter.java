package eu.fbk.utils.mylibsvm;

public class svm_parameter implements Cloneable, java.io.Serializable {

    //
    private static final long serialVersionUID = 5024396602591514749L;

    /* svm_type */
    public static final int C_SVC = 0;
    public static final int NU_SVC = 1;
    public static final int ONE_CLASS = 2;
    public static final int EPSILON_SVR = 3;
    public static final int NU_SVR = 4;

    /* kernel_type */
    public static final int LINEAR = 0;
    public static final int POLY = 1;
    public static final int RBF = 2;
    public static final int SIGMOID = 3;
    public static final int PRECOMPUTED = 4;

    public int svm_type;
    public int kernel_type;
    public int degree;    // for poly
    public double gamma;    // for poly/rbf/sigmoid
    public double coef0;    // for poly/sigmoid

    // these are for training only
    public double cache_size; // in MB
    public double eps;    // stopping criteria
    public double C;    // for C_SVC, EPSILON_SVR and NU_SVR
    public int nr_weight;        // for C_SVC
    public int[] weight_label;    // for C_SVC
    public double[] weight;        // for C_SVC
    public double nu;    // for NU_SVC, ONE_CLASS, and NU_SVR
    public double p;    // for EPSILON_SVR
    public int shrinking;    // use the shrinking heuristics
    public int probability; // do probability estimates

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    //
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("svm_type = ");
        sb.append(svm_type);
        sb.append("\nkernel_type = ");
        sb.append(kernel_type);
        sb.append("\ncache_size = ");
        sb.append(cache_size);
        sb.append("\neps = ");
        sb.append(eps);
        sb.append("\nC = ");
        sb.append(C);
        sb.append("\nnr_weight = ");
        sb.append(nr_weight);
        sb.append("\nnu = ");
        sb.append(nu);
        sb.append("\np = ");
        sb.append(p);
        sb.append("\nshrinking = ");
        sb.append(shrinking);
        return sb.toString();
    }
}
