/*
 *  Copyright(c) 2003-2004 by the authors indicated in the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.tigris.subversion.svnclientadapter.commandline;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * common methods for both SvnCommandLine and SvnAdminCommandLine 
 *  
 * @author Philip Schatz (schatz at tigris)
 * @author Cédric Chabanois (cchabanois at no-log.org)
 */
abstract class CommandLine {

	protected String CMD;
    protected CmdLineNotificationHandler notificationHandler;
    
	//Constructors
	CommandLine(String svnPath,CmdLineNotificationHandler notificationHandler) {
		CMD = svnPath;
        this.notificationHandler = notificationHandler;
	}

	//Methods
	String version() throws CmdLineException {
		ArrayList args = new ArrayList();
		args.add("--version");
		return execString(args,false);
	}


    /**
     * Executes the given svn command and returns the corresponding
     * <code>Process</code> object.
     *
     * @param svnArguments The command-line arguments to execute.
     */
	protected Process execProcess(ArrayList svnArguments)
        throws CmdLineException {
		// We add "svn" or "svnadmin" to the arguments (as
		// appropriate), and convert it to an array of strings.
        int svnArgsLen = svnArguments.size();
        String[] cmdline = new String[svnArgsLen + 1];
        cmdline[0] = CMD;

		StringBuffer svnCommand = new StringBuffer();
		boolean nextIsPassword = false;

		for (int i = 0; i < svnArgsLen; i++) {
			if (i != 0)
				svnCommand.append(' ');
			
			Object arg = svnArguments.get(i);
            if (arg != null)
                arg = arg.toString();
			
			if ("".equals(arg)) {
				arg = "\"\"";
			}
			
			if (nextIsPassword) {
				// Avoid showing the password on the console.
				svnCommand.append("*******");
				nextIsPassword = false;	
			} else {
				svnCommand.append(arg);
			}
			
			if ("--password".equals(arg)) {
				nextIsPassword = true;
			}

            // Regardless of the data type passed in via svnArguments,
            // at this point we expect to have a String object.
            cmdline[i + 1] = (String) arg;
		}
        notificationHandler.logCommandLine(svnCommand.toString());

		// Run the command, and return the associated Process object.
		try {
			return Runtime.getRuntime().exec(cmdline);
		} catch (IOException e) {
			throw new CmdLineException(e);
		}
	}

	/**
	 * Runs the process and returns the results.
     *
	 * @param svnArguments The command-line arguments to execute.
     * @param coalesceLines
	 * @return Any output returned from execution of the command-line.
	 */
	protected String execString(ArrayList svnArguments, boolean coalesceLines)
        throws CmdLineException {
		Process proc = execProcess(svnArguments);

        CmdLineStreamPumper outPumper = new CmdLineStreamPumper(proc.getInputStream(),coalesceLines);
        CmdLineStreamPumper errPumper = new CmdLineStreamPumper(proc.getErrorStream());

        Thread threadOutPumper = new Thread(outPumper);
        Thread threadErrPumper = new Thread(errPumper);
        threadOutPumper.start();         
        threadErrPumper.start();
        try {
            outPumper.waitFor();
            errPumper.waitFor();
        } catch (InterruptedException e) {
        }
        
		try {
            String errMessage = errPumper.toString();
            if (errMessage.length() > 0) {
                throw new CmdLineException(errMessage);        
            }
            String outputString = outPumper.toString(); 

            notifyFromSvnOutput(outputString);
			return outputString;
		} catch (CmdLineException e) {
            notificationHandler.logException(e);
			throw e;
		}
	}

	/**
	 * runs the process and returns the results.
	 * @param cmd
	 * @return String
	 */
	protected byte[] execBytes(ArrayList svnArguments, boolean assumeUTF8) throws CmdLineException {
		Process proc = execProcess(svnArguments);

        CmdLineByteStreamPumper outPumper = new CmdLineByteStreamPumper(proc.getInputStream());
        CmdLineStreamPumper errPumper = new CmdLineStreamPumper(proc.getErrorStream());

        Thread threadOutPumper = new Thread(outPumper);
        Thread threadErrPumper = new Thread(errPumper);
        threadOutPumper.start();         
        threadErrPumper.start();
        try {
            outPumper.waitFor();
            errPumper.waitFor();
        } catch (InterruptedException e) {
        }
        
		try {
            String errMessage = errPumper.toString();
            if (errMessage.length() > 0) {
                throw new CmdLineException(errMessage);        
            }
            byte[] bytes = outPumper.getBytes(); 

            String notifyMessage = "";
            if (assumeUTF8) {
            	try {
            		notifyMessage = new String(bytes, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// It is guaranteed to be there!
				}
            } else {
            	// This uses the default charset, which is likely
            	// wrong if we are trying to get the bytes, anyway...
            	notifyMessage = new String(bytes);
            }
			notifyFromSvnOutput(notifyMessage);
			
			return bytes;
		} catch (CmdLineException e) {
            notificationHandler.logException(e);
			throw e;
		}
	}

    /**
     * runs the command (returns nothing)
     * @param svnCommand
     * @throws CmdLineException
     */
	protected void execVoid(ArrayList svnArguments) throws CmdLineException {
		execString(svnArguments,false);
	}

	/**
	 * notify the listeners from the output. This is the default implementation
     *
	 * @param svnOutput
	 */
    protected void notifyFromSvnOutput(String svnOutput) {
		StringTokenizer st = new StringTokenizer(svnOutput, Helper.NEWLINE);
		int size = st.countTokens();
		//do everything but the last line
		for (int i = 1; i < size; i++) {
            notificationHandler.logMessage(st.nextToken());
		}

		//log the last line as the completed message.
		if (size > 0)
            notificationHandler.logCompleted(st.nextToken());
    }
    	
	
	
}

