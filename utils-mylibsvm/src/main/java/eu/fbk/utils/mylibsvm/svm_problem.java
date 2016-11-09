package eu.fbk.utils.mylibsvm;

public class svm_problem implements java.io.Serializable {

    //
    private static final long serialVersionUID = 5024396602591514749L;

    public int l;
    public double[] y;
    public svm_node[][] x;
}
