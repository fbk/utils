/*
 * Copyright (2011) Fondazione Bruno Kessler (FBK)
 * 
 * FBK reserves all rights in the Program as delivered.
 * The Program or any portion thereof may not be reproduced
 * in any form whatsoever except as provided by license
 * without the written consent of FBK.  A license under FBK's
 * rights in the Program may be available directly from FBK.
 */

package eu.fbk.utils.mylibsvm;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.*;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * This class extends svm to provide methods to save and load
 * the svn model customized to the TWM.
 * Could this class be move to the jwikify?
 *
 * @author Claudio Giuliano
 * @version %I%, %G%
 * @since 1.0
 * @see svm
 */
public class MySVM extends svm {

    /**
     * Define a static logger variable so that it references the
     * Logger instance named <code>MySVM</code>.
     */
    static Logger logger = Logger.getLogger(MySVM.class.getName());

    //
    static Pattern spacePattern = Pattern.compile(" ");

    //
    static Pattern colonPattern = Pattern.compile(":");

    //
    private static double atof(String s) {
        return Double.valueOf(s).doubleValue();
    }

    //
    private static int atoi(String s) {
        return Integer.parseInt(s);
    }

    //
    public static void my_svm_save_model(OutputStream output, svm_model model) throws IOException {
        logger.debug("MySVM.my_svm_save_model");

        DataOutputStream fp = new DataOutputStream(new BufferedOutputStream(output));

        svm_parameter param = model.param;

        //fp.writeBytes("svm_type "+svm_type_table[param.svm_type]+"\n");
        fp.writeInt(param.svm_type);
        // I use linear kernels only
    /*
		//fp.writeBytes("kernel_type "+kernel_type_table[param.kernel_type]+"\n");
		fp.writeInt(param.kernel_type);
		
		if(param.kernel_type == svm_parameter.POLY)
		{
			//fp.writeBytes("degree "+param.degree+"\n");
			fp.writeInt(param.degree);
		}
			
		
		if(param.kernel_type == svm_parameter.POLY ||
		   param.kernel_type == svm_parameter.RBF ||
		   param.kernel_type == svm_parameter.SIGMOID)
		{
			//fp.writeBytes("gamma "+param.gamma+"\n");
			fp.writeDouble(param.gamma);
		}
			
		
		if(param.kernel_type == svm_parameter.POLY ||
		   param.kernel_type == svm_parameter.SIGMOID)
		{
			//fp.writeBytes("coef0 "+param.coef0+"\n");
			fp.writeDouble(param.coef0);
		}
	*/

        int nr_class = model.nr_class;
        int l = model.l;
        //fp.writeBytes("nr_class "+nr_class+"\n");
        fp.writeInt(nr_class);
        //fp.writeBytes("total_sv "+l+"\n");
        fp.writeInt(l);

        logger.debug("MySVM.svm_save_model.nr_class " + nr_class);
        logger.debug("MySVM.svm_save_model.total_sv " + l);

        {
            //fp.writeBytes("rho");
            for (int i = 0; i < nr_class * (nr_class - 1) / 2; i++) {
                //fp.writeBytes(" "+model.rho[i]);
                fp.writeDouble(model.rho[i]);
            }

            //fp.writeBytes("\n");
        }

        if (model.label != null) {
            //fp.writeBytes("label");
            for (int i = 0; i < nr_class; i++) {
                //fp.writeBytes(" "+model.label[i]);
                fp.writeInt(model.label[i]);
                logger.debug("MySVM.svm_save_model.label[" + i + "] = " + model.label[i]);
            }

            //fp.writeBytes("\n");
        }
        // I use linear kernels only
	/*	
		if(model.probA != null) // regression has probA only
		{
			fp.writeBytes("probA");
			for(int i=0;i<nr_class*(nr_class-1)/2;i++)
				fp.writeBytes(" "+model.probA[i]);
			fp.writeBytes("\n");
		}
		if(model.probB != null) 
		{
			fp.writeBytes("probB");
			for(int i=0;i<nr_class*(nr_class-1)/2;i++)
				fp.writeBytes(" "+model.probB[i]);
			fp.writeBytes("\n");
		}
	*/
        if (model.nSV != null) {
            //fp.writeBytes("nr_sv");
            for (int i = 0; i < nr_class; i++) {
                //fp.writeBytes(" "+model.nSV[i]);
                fp.writeInt(model.nSV[i]);
                logger.debug("MySVM.svm_save_model.label[" + i + "] = " + model.nSV[i]);
            }

            //fp.writeBytes("\n");
        }

        //fp.writeBytes("SV\n");
        double[][] sv_coef = model.sv_coef;
        svm_node[][] SV = model.SV;

        for (int i = 0; i < l; i++) {
            for (int j = 0; j < nr_class - 1; j++) {
                //fp.writeBytes(sv_coef[j][i]+" ");
                fp.writeDouble(sv_coef[j][i]);
                //logger.info("MySVM.svm_save_model.sv_coef[" + j + "][" + i + "] = " + sv_coef[j][i]);
            }

            svm_node[] p = SV[i];
            fp.writeInt(p.length);
            //logger.info("MySVM.svm_save_model.SV[" + i + "] = " + p.length);

            for (int j = 0; j < p.length; j++) {
                //fp.writeBytes(p[j].index+":"+p[j].value+" ");
                fp.writeInt(p[j].index);
                fp.writeDouble(p[j].value);
                //logger.info("MySVM.svm_save_model.p[" + j + " ] = " + p[j].index + ":" + p[j].value);
            }

            //fp.writeBytes("\n");
        } // end for

        fp.close();
    } // end my_svm_save_model

    /**
     * @see SuperAllInOne.trainAll
     */
    public static void my_svm_save_prob_model(OutputStream output, svm_model model) throws IOException {
        logger.debug("MySVM.my_svm_save_prob_model");

        DataOutputStream fp = new DataOutputStream(new BufferedOutputStream(output));

        svm_parameter param = model.param;

        //fp.writeBytes("svm_type "+svm_type_table[param.svm_type]+"\n");
        fp.writeInt(param.svm_type);
        // I use linear kernels only
		/*
		 //fp.writeBytes("kernel_type "+kernel_type_table[param.kernel_type]+"\n");
		 fp.writeInt(param.kernel_type);
		 
		 if(param.kernel_type == svm_parameter.POLY)
		 {
		 //fp.writeBytes("degree "+param.degree+"\n");
		 fp.writeInt(param.degree);
		 }
		 
		 
		 if(param.kernel_type == svm_parameter.POLY ||
		 param.kernel_type == svm_parameter.RBF ||
		 param.kernel_type == svm_parameter.SIGMOID)
		 {
		 //fp.writeBytes("gamma "+param.gamma+"\n");
		 fp.writeDouble(param.gamma);
		 }
		 
		 
		 if(param.kernel_type == svm_parameter.POLY ||
		 param.kernel_type == svm_parameter.SIGMOID)
		 {
		 //fp.writeBytes("coef0 "+param.coef0+"\n");
		 fp.writeDouble(param.coef0);
		 }
		 */

        int nr_class = model.nr_class;
        int l = model.l;
        //fp.writeBytes("nr_class "+nr_class+"\n");
        fp.writeInt(nr_class);
        //fp.writeBytes("total_sv "+l+"\n");
        fp.writeInt(l);

        logger.debug("MySVM.my_svm_save_prob_model.nr_class " + nr_class);
        logger.debug("MySVM.my_svm_save_prob_model.total_sv " + l);

        {
            //fp.writeBytes("rho");
            for (int i = 0; i < nr_class * (nr_class - 1) / 2; i++) {
                //fp.writeBytes(" "+model.rho[i]);
                fp.writeDouble(model.rho[i]);
            }

            //fp.writeBytes("\n");
        }

        if (model.label != null) {
            //fp.writeBytes("label");
            for (int i = 0; i < nr_class; i++) {
                //fp.writeBytes(" "+model.label[i]);
                fp.writeInt(model.label[i]);
                logger.debug("MySVM.svm_save_model.label[" + i + "] = " + model.label[i]);
            }

            //fp.writeBytes("\n");
        }

        if (model.nSV != null) {
            //fp.writeBytes("nr_sv");
            for (int i = 0; i < nr_class; i++) {
                //fp.writeBytes(" "+model.nSV[i]);
                fp.writeInt(model.nSV[i]);
                logger.debug("MySVM.my_svm_save_prob_model.label[" + i + "] = " + model.nSV[i]);
            }

            //fp.writeBytes("\n");
        }

        //fp.writeBytes("SV\n");
        double[][] sv_coef = model.sv_coef;
        svm_node[][] SV = model.SV;

        for (int i = 0; i < l; i++) {
            for (int j = 0; j < nr_class - 1; j++) {
                //fp.writeBytes(sv_coef[j][i]+" ");
                fp.writeDouble(sv_coef[j][i]);
                //logger.info("MySVM.svm_save_model.sv_coef[" + j + "][" + i + "] = " + sv_coef[j][i]);
            }

            svm_node[] p = SV[i];
            fp.writeInt(p.length);
            //logger.info("MySVM.svm_save_model.SV[" + i + "] = " + p.length);

            for (int j = 0; j < p.length; j++) {
                //fp.writeBytes(p[j].index+":"+p[j].value+" ");
                fp.writeInt(p[j].index);
                fp.writeDouble(p[j].value);
                //logger.info("MySVM.svm_save_model.p[" + j + " ] = " + p[j].index + ":" + p[j].value);
            }

            //fp.writeBytes("\n");
        } // end for

        if (model.probA != null) // regression has probA only
        {
            //fp.writeBytes("probA");
            for (int i = 0; i < nr_class * (nr_class - 1) / 2; i++)
            //fp.writeBytes(" "+model.probA[i]);
            {
                fp.writeDouble(model.probA[i]);
            }
            //fp.writeBytes("\n");
        }
        if (model.probB != null) {
            //fp.writeBytes("probB");
            for (int i = 0; i < nr_class * (nr_class - 1) / 2; i++)
            //fp.writeBytes(" "+model.probB[i]);
            {
                fp.writeDouble(model.probB[i]);
            }
            //fp.writeBytes("\n");
        }

        fp.close();
    } // end my_svm_save_prob_model

    //
    public static svm_model my_svm_load_model(InputStream input) throws IOException {
        //logger.debug("MySVM.my_svm_load_model");
        //long begin = System.currentTimeMillis();
        //System.out.println("MySVM.my_svm_load_model");
        // read parameters

        svm_model model = new svm_model();
        svm_parameter param = new svm_parameter();
        model.param = param;
        model.rho = null;
        model.probA = null;
        model.probB = null;
        model.label = null;
        model.nSV = null;

        DataInputStream dis = new DataInputStream(new BufferedInputStream(input));
        //DataInputStream dis = new DataInputStream(input);

        param.svm_type = dis.readInt();
        // I use linear kernels only
        param.kernel_type = svm_parameter.LINEAR;

        model.nr_class = dis.readInt();
        model.l = dis.readInt();
        model.param = param;
        //logger.debug("MySVM.my_svm_load_model.param\n" + param);

        {
            model.rho = new double[model.nr_class * (model.nr_class - 1) / 2];
            for (int i = 0; i < model.nr_class * (model.nr_class - 1) / 2; i++) {
                model.rho[i] = dis.readDouble();
                //logger.debug("rho[" + i + "] = " + model.rho[i]);
            }
        }

        {
            model.label = new int[model.nr_class];
            for (int i = 0; i < model.nr_class; i++) {
                model.label[i] = dis.readInt();
                //logger.debug("label[" + i + "] = " + model.label[i]);
            }
        }

        model.nSV = new int[model.nr_class];
        {
            for (int i = 0; i < model.nr_class; i++) {
                model.nSV[i] = dis.readInt();
                //logger.debug("nSV[" + i + "] = " + model.nSV[i]);
            }
        }

        model.sv_coef = new double[model.nr_class - 1][model.l];
        svm_node[][] SV = model.SV;
        model.SV = new svm_node[model.l][];
        for (int i = 0; i < model.l; i++) {
            for (int j = 0; j < model.nr_class - 1; j++) {
                model.sv_coef[j][i] = dis.readDouble();
                //logger.debug("sv_coef[" + j + "][" + i + "] = " + model.sv_coef[j][i]);
            } // end for j

            int s = dis.readInt();
            //logger.info("p.length = " + s);
            svm_node[] p = new svm_node[s];
            for (int j = 0; j < p.length; j++) {
                //fp.writeBytes(p[j].index+":"+p[j].value+" ");
                p[j] = new svm_node();
                p[j].index = dis.readInt();
                p[j].value = dis.readDouble();

            }
            model.SV[i] = p;
            //fp.writeBytes("\n");
        } // end for

        dis.close();

        //long end = System.currentTimeMillis();
        //System.out.println("done in " + (end - begin) + " ms");

        return model;
    } // end my_svm_load_model

    /**
     * @see SuperAllInOne.svmPredict
     */
    public static svm_model my_svm_load_prob_model(InputStream input) throws IOException {
        logger.debug("MySVM.my_svm_load_prob_model");
        //long begin = System.currentTimeMillis();
        //System.out.println("MySVM.my_svm_load_model");
        // read parameters

        svm_model model = new svm_model();
        svm_parameter param = new svm_parameter();
        model.param = param;
        model.rho = null;
        model.probA = null;
        model.probB = null;
        model.label = null;
        model.nSV = null;

        DataInputStream dis = new DataInputStream(new BufferedInputStream(input));

        param.svm_type = dis.readInt();
        // I use linear kernels only
        param.kernel_type = svm_parameter.LINEAR;

        model.nr_class = dis.readInt();
        model.l = dis.readInt();
        model.param = param;
        //logger.debug("MySVM.my_svm_load_model.param\n" + param);
        int m = model.nr_class * (model.nr_class - 1) / 2;
        {
            //model.rho = new double[model.nr_class*(model.nr_class-1)/2];
            model.rho = new double[m];
            //for(int i=0;i<model.nr_class*(model.nr_class-1)/2;i++)
            for (int i = 0; i < m; i++) {
                model.rho[i] = dis.readDouble();
                //logger.debug("rho[" + i + "] = " + model.rho[i]);
            }
        }

        {
            model.label = new int[model.nr_class];
            for (int i = 0; i < model.nr_class; i++) {
                model.label[i] = dis.readInt();
                //logger.debug("label[" + i + "] = " + model.label[i]);
            }
        }

        model.nSV = new int[model.nr_class];
        {
            for (int i = 0; i < model.nr_class; i++) {
                model.nSV[i] = dis.readInt();
                //logger.debug("nSV[" + i + "] = " + model.nSV[i]);
            }
        }

        model.sv_coef = new double[model.nr_class - 1][model.l];
        svm_node[][] SV = model.SV;
        model.SV = new svm_node[model.l][];
        for (int i = 0; i < model.l; i++) {
            for (int j = 0; j < model.nr_class - 1; j++) {
                model.sv_coef[j][i] = dis.readDouble();
                //logger.debug("sv_coef[" + j + "][" + i + "] = " + model.sv_coef[j][i]);
            } // end for j

            int s = dis.readInt();
            //logger.info("p.length = " + s);
            svm_node[] p = new svm_node[s];
            for (int j = 0; j < p.length; j++) {
                //fp.writeBytes(p[j].index+":"+p[j].value+" ");
                p[j] = new svm_node();
                p[j].index = dis.readInt();
                p[j].value = dis.readDouble();

            }
            model.SV[i] = p;
            //fp.writeBytes("\n");
        } // end for

        // probA
        model.probA = new double[m];
        for (int i = 0; i < m; i++) {
            model.probA[i] = dis.readDouble();
            //logger.debug("probA[" + i + "] = " + model.probA[i]);
        }
        // probB
        model.probB = new double[m];
        for (int i = 0; i < m; i++) {
            model.probB[i] = dis.readDouble();
            //logger.debug("probA[" + i + "] = " + model.probA[i]);
        }

        dis.close();

        //long end = System.currentTimeMillis();
        //System.out.println("done in " + (end - begin) + " ms");

        return model;
    } // end my_svm_load_prob_model

    /**
     * svm_type int \n
     * kernel_type int \n
     * degree int \n (optional kernel_type must be POLY)
     * gamma double \n (optional kernel_type must be POLY or RBF or SIGMOID)
     * coef0 double \n (optional kernel_type must be POLY or SIGMOID)
     * nr_class int \n
     * total_sv int \n
     * rho double[nr_class*(nr_class-1)/2] \n
     * label int[nr_class] \n
     * probA double[nr_class*(nr_class-1)/2] \n
     * probB double[nr_class*(nr_class-1)/2] \n
     * nr_sv int[nr_class] \n
     * SV \n
     * sv_coef[0][0] ... sv_coef[nr_class-1][0] svm_node[0][0] ... svm_node[0][m] \n
     * ...
     * sv_coef[0][l] ... sv_coef[nr_class-1][l] svm_node[l][0] ... svm_node[l][n] \n
     */
    public static void svm_save_model(OutputStream output, svm_model model) throws IOException {
        logger.info("MySVM.svm_save_model");

        DataOutputStream fp = new DataOutputStream(new BufferedOutputStream(output));

        svm_parameter param = model.param;

        fp.writeBytes("svm_type " + svm_type_table[param.svm_type] + "\n");
        fp.writeBytes("kernel_type " + kernel_type_table[param.kernel_type] + "\n");

        if (param.kernel_type == svm_parameter.POLY) {
            fp.writeBytes("degree " + param.degree + "\n");
        }

        if (param.kernel_type == svm_parameter.POLY ||
                param.kernel_type == svm_parameter.RBF ||
                param.kernel_type == svm_parameter.SIGMOID) {
            fp.writeBytes("gamma " + param.gamma + "\n");
        }

        if (param.kernel_type == svm_parameter.POLY ||
                param.kernel_type == svm_parameter.SIGMOID) {
            fp.writeBytes("coef0 " + param.coef0 + "\n");
        }

        int nr_class = model.nr_class;
        int l = model.l;
        fp.writeBytes("nr_class " + nr_class + "\n");
        fp.writeBytes("total_sv " + l + "\n");

        logger.info("MySVM.svm_save_model.nr_class" + nr_class);
        logger.info("MySVM.svm_save_model.total_sv" + l);

        {
            fp.writeBytes("rho");
            for (int i = 0; i < nr_class * (nr_class - 1) / 2; i++) {
                fp.writeBytes(" " + model.rho[i]);
            }
            fp.writeBytes("\n");
        }

        if (model.label != null) {
            fp.writeBytes("label");
            for (int i = 0; i < nr_class; i++) {
                fp.writeBytes(" " + model.label[i]);
            }
            fp.writeBytes("\n");
        }

        if (model.probA != null) // regression has probA only
        {
            fp.writeBytes("probA");
            for (int i = 0; i < nr_class * (nr_class - 1) / 2; i++) {
                fp.writeBytes(" " + model.probA[i]);
            }
            fp.writeBytes("\n");
        }
        if (model.probB != null) {
            fp.writeBytes("probB");
            for (int i = 0; i < nr_class * (nr_class - 1) / 2; i++) {
                fp.writeBytes(" " + model.probB[i]);
            }
            fp.writeBytes("\n");
        }

        if (model.nSV != null) {
            fp.writeBytes("nr_sv");
            for (int i = 0; i < nr_class; i++) {
                fp.writeBytes(" " + model.nSV[i]);
            }
            fp.writeBytes("\n");
        }

        fp.writeBytes("SV\n");
        double[][] sv_coef = model.sv_coef;
        svm_node[][] SV = model.SV;

        for (int i = 0; i < l; i++) {
            for (int j = 0; j < nr_class - 1; j++) {
                fp.writeBytes(sv_coef[j][i] + " ");
            }

            svm_node[] p = SV[i];
            if (param.kernel_type == svm_parameter.PRECOMPUTED) {
                fp.writeBytes("0:" + (int) (p[0].value));
            } else {
                for (int j = 0; j < p.length; j++) {
                    fp.writeBytes(p[j].index + ":" + p[j].value + " ");
                }
            }
            fp.writeBytes("\n");
        }

        fp.close();
    } // end save

    //
    public static svm_model svm_load_model(InputStream input) throws IOException {
        logger.info("MySVM.svm_load_model.their implementation");

        BufferedReader fp = new BufferedReader(new InputStreamReader(input));

        // read parameters

        svm_model model = new svm_model();
        svm_parameter param = new svm_parameter();
        model.param = param;
        model.rho = null;
        model.probA = null;
        model.probB = null;
        model.label = null;
        model.nSV = null;

        while (true) {
            String cmd = fp.readLine();
            String arg = cmd.substring(cmd.indexOf(' ') + 1);

            if (cmd.startsWith("svm_type")) {
                int i;
                for (i = 0; i < svm_type_table.length; i++) {
                    if (arg.indexOf(svm_type_table[i]) != -1) {
                        param.svm_type = i;
                        break;
                    }
                }
                if (i == svm_type_table.length) {
                    logger.error("unknown svm type.\n");
                    return null;
                }
            } else if (cmd.startsWith("kernel_type")) {
                int i;
                for (i = 0; i < kernel_type_table.length; i++) {
                    if (arg.indexOf(kernel_type_table[i]) != -1) {
                        param.kernel_type = i;
                        break;
                    }
                }
                if (i == kernel_type_table.length) {
                    logger.error("unknown kernel function.\n");
                    return null;
                }
            } else if (cmd.startsWith("degree")) {
                param.degree = atoi(arg);
            } else if (cmd.startsWith("gamma")) {
                param.gamma = atof(arg);
            } else if (cmd.startsWith("coef0")) {
                param.coef0 = atof(arg);
            } else if (cmd.startsWith("nr_class")) {
                model.nr_class = atoi(arg);
            } else if (cmd.startsWith("total_sv")) {
                model.l = atoi(arg);
            } else if (cmd.startsWith("rho")) {
                int n = model.nr_class * (model.nr_class - 1) / 2;
                model.rho = new double[n];
                StringTokenizer st = new StringTokenizer(arg);
                for (int i = 0; i < n; i++) {
                    model.rho[i] = atof(st.nextToken());
                }
            } else if (cmd.startsWith("label")) {
                int n = model.nr_class;
                model.label = new int[n];
                StringTokenizer st = new StringTokenizer(arg);
                for (int i = 0; i < n; i++) {
                    model.label[i] = atoi(st.nextToken());
                }
            } else if (cmd.startsWith("probA")) {
                int n = model.nr_class * (model.nr_class - 1) / 2;
                model.probA = new double[n];
                StringTokenizer st = new StringTokenizer(arg);
                for (int i = 0; i < n; i++) {
                    model.probA[i] = atof(st.nextToken());
                }
            } else if (cmd.startsWith("probB")) {
                int n = model.nr_class * (model.nr_class - 1) / 2;
                model.probB = new double[n];
                StringTokenizer st = new StringTokenizer(arg);
                for (int i = 0; i < n; i++) {
                    model.probB[i] = atof(st.nextToken());
                }
            } else if (cmd.startsWith("nr_sv")) {
                int n = model.nr_class;
                model.nSV = new int[n];
                StringTokenizer st = new StringTokenizer(arg);
                for (int i = 0; i < n; i++) {
                    model.nSV[i] = atoi(st.nextToken());
                }
            } else if (cmd.startsWith("SV")) {
                break;
            } else {
                logger.error("unknown text in model file: [" + cmd + "]\n");
                return null;
            }
        }

        // read sv_coef and SV
        long begin = System.currentTimeMillis();
        int m = model.nr_class - 1;
        int l = model.l;
        model.sv_coef = new double[m][l];
        model.SV = new svm_node[l][];

        for (int i = 0; i < l; i++) {
            String line = fp.readLine();
            StringTokenizer st = new StringTokenizer(line, " \t\n\r\f:");

            for (int k = 0; k < m; k++) {
                model.sv_coef[k][i] = atof(st.nextToken());
            }
            int n = st.countTokens() / 2;
            model.SV[i] = new svm_node[n];
            for (int j = 0; j < n; j++) {
                model.SV[i][j] = new svm_node();
                model.SV[i][j].index = atoi(st.nextToken());
                model.SV[i][j].value = atof(st.nextToken());
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("support vector parsed in " + (end - begin) + " ms");

        fp.close();
        return model;
    } // end load

    //
    public static svm_model svm_load_model(BufferedReader fp) throws IOException {
        logger.info("MySVM.svm_load_model.my implementation");

        long begin = System.currentTimeMillis();
        //BufferedReader fp = new BufferedReader(new InputStreamReader(input));

        // read parameters

        svm_model model = new svm_model();
        svm_parameter param = new svm_parameter();
        model.param = param;
        model.rho = null;
        model.probA = null;
        model.probB = null;
        model.label = null;
        model.nSV = null;

        while (true) {
            String cmd = fp.readLine();
            String arg = cmd.substring(cmd.indexOf(' ') + 1);

            if (cmd.startsWith("svm_type")) {
                int i;
                for (i = 0; i < svm_type_table.length; i++) {
                    if (arg.indexOf(svm_type_table[i]) != -1) {
                        param.svm_type = i;
                        break;
                    }
                }
                if (i == svm_type_table.length) {
                    logger.error("unknown svm type.\n");
                    return null;
                }
            } else if (cmd.startsWith("kernel_type")) {
                int i;
                for (i = 0; i < kernel_type_table.length; i++) {
                    if (arg.indexOf(kernel_type_table[i]) != -1) {
                        param.kernel_type = i;
                        break;
                    }
                }
                if (i == kernel_type_table.length) {
                    logger.error("unknown kernel function.\n");
                    return null;
                }
            } else if (cmd.startsWith("degree")) {
                param.degree = atoi(arg);
            } else if (cmd.startsWith("gamma")) {
                param.gamma = atof(arg);
            } else if (cmd.startsWith("coef0")) {
                param.coef0 = atof(arg);
            } else if (cmd.startsWith("nr_class")) {
                model.nr_class = atoi(arg);
            } else if (cmd.startsWith("total_sv")) {
                model.l = atoi(arg);
            } else if (cmd.startsWith("rho")) {
                int n = model.nr_class * (model.nr_class - 1) / 2;
                model.rho = new double[n];
                StringTokenizer st = new StringTokenizer(arg);
                for (int i = 0; i < n; i++) {
                    model.rho[i] = atof(st.nextToken());
                }
            } else if (cmd.startsWith("label")) {
                int n = model.nr_class;
                model.label = new int[n];
                StringTokenizer st = new StringTokenizer(arg);
                for (int i = 0; i < n; i++) {
                    model.label[i] = atoi(st.nextToken());
                }
            } else if (cmd.startsWith("probA")) {
                int n = model.nr_class * (model.nr_class - 1) / 2;
                model.probA = new double[n];
                StringTokenizer st = new StringTokenizer(arg);
                for (int i = 0; i < n; i++) {
                    model.probA[i] = atof(st.nextToken());
                }
            } else if (cmd.startsWith("probB")) {
                int n = model.nr_class * (model.nr_class - 1) / 2;
                model.probB = new double[n];
                StringTokenizer st = new StringTokenizer(arg);
                for (int i = 0; i < n; i++) {
                    model.probB[i] = atof(st.nextToken());
                }
            } else if (cmd.startsWith("nr_sv")) {
                int n = model.nr_class;
                model.nSV = new int[n];
                StringTokenizer st = new StringTokenizer(arg);
                for (int i = 0; i < n; i++) {
                    model.nSV[i] = atoi(st.nextToken());
                }
            } else if (cmd.startsWith("SV")) {
                break;
            } else {
                logger.error("unknown text in model file: [" + cmd + "]\n");
                return null;
            }
        } // end while

        long end = System.currentTimeMillis();
        logger.info("param parsed in " + (end - begin) + " ms");

        // read sv_coef and SV
        begin = System.currentTimeMillis();
        int m = model.nr_class - 1;
        int l = model.l;
        model.sv_coef = new double[m][l];
        model.SV = new svm_node[l][];

        String[] s1 = null, s2 = null;
        int n = 0;
        int z = 0;
        for (int i = 0; i < l; i++) {
            String line = fp.readLine();
            //StringTokenizer st = new StringTokenizer(line," \t\n\r\f:");
            s1 = spacePattern.split(line);
            for (int k = 0; k < m; k++) {
                model.sv_coef[k][i] = Double.parseDouble(s1[k]);

            }
            n = s1.length - m;
            //for(int k=0;k<m;k++)
            //	model.sv_coef[k][i] = atof(st.nextToken());
            //int n = st.countTokens()/2;
            model.SV[i] = new svm_node[n];
            for (int j = 0; j < n; j++) {
                model.SV[i][j] = new svm_node();
				/*s2 = colonPattern.split(s1[j + m]); 				
				//model.SV[i][j].index = atoi(st.nextToken());
				model.SV[i][j].index = Integer.parseInt(s2[0]);
				model.SV[i][j].value = Double.parseDouble(s2[1]);*/
                z = s1[j + m].indexOf(':');
                model.SV[i][j].index = Integer.parseInt(s1[j + m].substring(0, z));
                model.SV[i][j].value = Double.parseDouble(s1[j + m].substring(z + 1, s1[j + m].length()));

            }
        }
        end = System.currentTimeMillis();
        logger.info(model.l + " support vectors parsed in " + (end - begin) + " ms");

        fp.close();
        return model;
    } // end load

    //
    public static void main(String args[]) throws Exception {
        String logConfig = System.getProperty("log-config");
        if (logConfig == null) {
            logConfig = "log-config.txt";
        }

        PropertyConfigurator.configure(logConfig);

        if (args.length != 2) {
            logger.info("java mylibsvm.MySVM in out");
            System.exit(1);
        }

        //svm_model model = MySVM.svm_load_model(new FileInputStream(args[0]));
        //svm_model model = MySVM.svm_load_model(new BufferedReader(new FileReader(args[0])));
		
		/*svm_model model = MySVM.svm_load_model(new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(args[0])))));
		int m = model.nr_class - 1;
		System.out.println(model.l + "\t" + m + "\t" + model.sv_coef.length + "\t" + model.sv_coef[0].length);
		for (int i=0;i<model.l;i++)
		{
			for (int j=0;j<m;j++)
			{
				if (j > 0)
					System.out.print(" ");
				System.out.print(model.sv_coef[j][i]);
			}
			
			for (int j=0;j<model.SV[i].length;j++)
			{
				
				System.out.print(" " + model.SV[i][j].index + "=" + model.SV[i][j].value);
			}
			System.out.print("\n");
		}
		*/
        svm_model model = MySVM.svm_load_model(new FileInputStream(args[0]));
        MySVM.my_svm_save_model(new FileOutputStream(args[0] + ".mine"), model);

        svm_model model1 = MySVM.my_svm_load_model(new FileInputStream(args[0] + ".mine"));
        MySVM.svm_save_model(new FileOutputStream(args[1]), model1);
    } // end main
} // end class MySVM
