/*
 * Copyright (2010) Fondazione Bruno Kessler (FBK)
 * 
 * FBK reserves all rights in the Program as delivered.
 * The Program or any portion thereof may not be reproduced
 * in any form whatsoever except as provided by license
 * without the written consent of FBK.  A license under FBK's
 * rights in the Program may be available directly from FBK.
 */

package eu.fbk.utils.lsa;

/**
 * TO DO
 *
 * @author Claudio Giuliano
 * @version %I%, %G%
 * @since 1.0
 */
public class License {

    /**
     * Returns a license message.
     * <p>
     * return a license message.
     */
    public static String get() {
        StringBuffer sb = new StringBuffer();

        // jLSI
        sb.append("\njLSI: V1.00\t 21.03.09\n");
        sb.append("developed by Claudio Giuliano (giuliano@fbk.eu)\n\n");

        // License
        sb.append("Copyright 2009 FBK (http://www.fbk.eu/)\n");
        sb.append("\n");
        sb.append("Licensed under the Apache License, Version 2.0 (the \"License\");\n");
        sb.append("you may not use this file except in compliance with the License.\n");
        sb.append("You may obtain a copy of the License at\n");
        sb.append("\n");
        sb.append("    http://www.apache.org/licenses/LICENSE-2.0\n");
        sb.append("\n");
        sb.append("Unless required by applicable law or agreed to in writing, software\n");
        sb.append("distributed under the License is distributed on an \"AS IS\" BASIS,\n");
        sb.append("WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n");
        sb.append("See the License for the specific language governing permissions and\n");
        sb.append("limitations under the License.\n\n");

        return sb.toString();
    } // end get

} // end class License
