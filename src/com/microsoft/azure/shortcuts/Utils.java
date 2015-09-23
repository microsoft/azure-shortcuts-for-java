package com.microsoft.azure.shortcuts;

import com.microsoft.azure.shortcuts.resources.AzureResources;
import com.microsoft.azure.utility.AuthHelper;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

public class Utils {
	
	// Create a new self-signed public/private key pair for an X.509 certificate packaged inside a PKCS#12 (PFX) file
	public static File createCertPkcs12(
			File targetPfxFile, 
			File jdkDirectory, 
			String alias, 
			String password, 
			String cnName, 
			int daysValid) throws Exception {
		
		final File keytool = getKeytool(jdkDirectory);
		final String[] commandArgs = { 
				keytool.toString(), 
				"-genkey", 
				"-alias", alias, 
				"-storetype", "pkcs12", 
				"-keystore", targetPfxFile.toString(),
				"-storepass", password,
				"-validity", String.valueOf(daysValid),
				"-keyalg", "RSA",
				"-keysize", "2048",
				"-storetype", "pkcs12",
				"-dname", "CN=" + cnName 
			};
		
		invokeCommand(commandArgs, false);
		
		if(!targetPfxFile.exists()) {
			throw new IOException("Failed to create PFX file");
		} else {
			return targetPfxFile;
		}
	}
	
	
	// Extract the public X.509 certificate from a PFX file and save as a CER file
	public static File createCertPublicFromPkcs12(
			File sourcePfxFile, 
			File targetCerFile, 
			File jdkDirectory,
			String alias,
			String password) throws Exception {
		
		if(sourcePfxFile == null || !sourcePfxFile.exists()) {
			throw new IOException("Incorrect source PFX file path");
		} 
		
		final File keyTool = getKeytool(jdkDirectory);
		final String[] commandArgs = {
				keyTool.toString(),
				"-export", 
				"-alias", alias,
				"-storetype", "pkcs12",
				"-keystore", sourcePfxFile.toString(),
				"-storepass", password,
				"-rfc",
				"-file", targetCerFile.toString()
		};
				
		invokeCommand(commandArgs, true);
		
		if(!targetCerFile.exists()) {
			throw new IOException("Failed to create CER file");
		} else {
			return targetCerFile;
		}
	}
	
	
	/**
	 * Invoke a shell command.
	 * 
	 * @param command :Command line to invoke, including arguments
	 * @param ignoreErrorStream :Set to true if exception is to be thrown when the error stream is not empty.
	 * @return result :The text contents of the output of the invoked command
	 * @throws Exception
	 * @throws IOException
	 */
	private static String invokeCommand(String[] command, boolean ignoreErrorStream) throws Exception, IOException {
		String result, error;
		InputStream inputStream = null, errorStream = null;
		BufferedReader inputReader = null, errorReader = null;
		try {
			Process process = new ProcessBuilder(command).start();
			inputStream = process.getInputStream();
			errorStream = process.getErrorStream();
			inputReader = new BufferedReader(new InputStreamReader(inputStream));
			result = inputReader.readLine();
			process.waitFor();
			errorReader = new BufferedReader(new InputStreamReader(errorStream));
			error = errorReader.readLine();
			if (error != null && !error.isEmpty() && !ignoreErrorStream) {
				throw new Exception(error, null);
			}
		} catch (Exception e) {
			throw new Exception("Exception occurred while invoking command", e);
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
			if (errorStream != null) {
				errorStream.close();
			}
			if (inputReader != null) {
				inputReader.close();
			}
			if (errorReader != null) {
				errorReader.close();
			}
		}
		
		return result;
	}
	
	
	// Returns the file path to the keytool
	private static File getKeytool(File jdkDirectory) throws IOException {
		File binDirectory = new File(jdkDirectory, "bin");
		if(jdkDirectory == null || !jdkDirectory.isDirectory()) {
			throw new IOException("Incorrect JDK directory path");
		} else if(!binDirectory.isDirectory()) {
			throw new IOException("JDK directory is missing teh bin subdirectory");
		} else {
			return new File(binDirectory, "keytool");
		}
	}

	public static Configuration createConfiguration(String subscriptionId, String tenantId, String clientId, String clientKey) throws Exception {
		String baseUri = AzureResources.ARM_URL;

		return ManagementConfiguration.configure(
				null,
				baseUri != null ? new URI(baseUri) : null,
				subscriptionId,
				AuthHelper.getAccessTokenFromServicePrincipalCredentials(AzureResources.MANAGEMENT_URI, AzureResources.ARM_AAD_URL,
						tenantId, clientId, clientKey)
						.getAccessToken());
	}
}
