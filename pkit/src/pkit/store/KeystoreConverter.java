package pkit.store;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.UnrecoverableKeyException;

import java.util.Enumeration;

/**
 *  Converts a simple keystore with a private key and matching public key for a given alias
*  from one format to another provided that the keys are RSA.
 *
 *@author     Justin Wood <justin@braidquest.co.za>
 *@created    June 20, 2003
 *@todo       include all certs ie trusted cert entries and keys not only those associate with the given
 *      alias but with say an array of aliases and passwords
 */
public class KeystoreConverter {

	/**
	 *  The main program for the KeystoreConverter class
	 *
	 *@param  args  The command line arguments
	 */
	public static void main(String[] args) {
		if (args.length < 12) {
			System.out.println("Usage: KeystoreConverter fromStoreFileName fromStorePassword fromStoreProvider fromStoreType fromAlias fromStoreKeyPassword toStoreFileName toStorePassword toStoreProvider toStoreType toAlias toStoreKeyPassword");
			System.exit(1);
		}
		String fromStoreFileName = args[0];
		char[] fromStorePassword = args[1].toCharArray();
		String fromStoreProvider = args[2];
		String fromStoreType = args[3];
		String fromAlias = args[4];
		char[] fromStoreKeyPassword = args[5].toCharArray();
		String toStoreFileName = args[6];
		char[] toStorePassword = args[7].toCharArray();
		String toStoreProvider = args[8];
		String toStoreType = args[9];
		String toAlias = args[10];
		char[] toStoreKeyPassword = args[11].toCharArray();
		try {
			convert(fromStoreFileName,
					fromStorePassword,
					fromStoreProvider,
					fromStoreType,
					fromAlias,
					fromStoreKeyPassword,
					toStoreFileName,
					toStorePassword,
					toStoreProvider,
					toStoreType,
					toAlias,
					toStoreKeyPassword);
		} catch (KeystoreConversionException e) {
			e.printStackTrace();
		}
		System.out.println("New keystore" + toStoreFileName + "successfully created.");
	}


	/**
	 *  Takes the private key and certs associated with a particular alias and writes them to a newly created file
	 *
	 *@param  fromStoreFileName                The name of the keystore you are converting from
	 *@param  fromStorePassword                The store password you are converting from
	 *@param  fromStoreProvider                The provider to use to read the keystore you are converting from
	 *@param  fromStoreType                    The type of the keystore you are converting from
	 *@param  fromAlias                   		The alias of the private 
	 *@param  fromStoreKeyPassword             The password for the key entry associated with the fromAlias
	 *@param  toStoreFileName                  The file name of the keystore you will be converting to
	 *@param  toStorePassword                  The password of the keystore you will be converting to 
	 *@param  toStoreProvider                  The provider to be used to create the keystore you will be converting to
	 *@param  toStoreType                      The type of the keystore you will be converting to
	 *@param  toAlias                     The alias of the private key entry in the keystore you will be converting to
	 *@param  toStoreKeyPassword               the password for the private key entry in the keystore you will be converting to
	 *@exception  KeystoreConversionException  if anything goes wrong
	 */
	public static void convert(
			String fromStoreFileName,
			char[] fromStorePassword,
			String fromStoreProvider,
			String fromStoreType,
			String fromAlias,
			char[] fromStoreKeyPassword,
			String toStoreFileName,
			char[] toStorePassword,
			String toStoreProvider,
			String toStoreType,
			String toAlias,
			char[] toStoreKeyPassword) throws KeystoreConversionException {

		//load the existing keystore and get the private key, certificate and chain.
		KeyStore fromKeyStore = null;
		FileInputStream fromStoreInputStream = null;
		try {
			fromStoreInputStream = new FileInputStream(fromStoreFileName);
			fromKeyStore = KeyStore.getInstance(fromStoreType, fromStoreProvider);
			fromKeyStore.load(fromStoreInputStream, fromStorePassword);
		} catch (FileNotFoundException e) {
			throw new KeystoreConversionException(fromStoreFileName + " keystore could not be found.", e);
		} catch (KeyStoreException e) {
			throw new KeystoreConversionException("There was a problems reading from the keystore " + fromStoreFileName, e);
		} catch (NoSuchProviderException e) {
			throw new KeystoreConversionException("The provider " + fromStoreProvider + "is not available", e);
		} catch (IOException e) {
			throw new KeystoreConversionException("There was an error reading the keystore", e);
		} catch (NoSuchAlgorithmException e) {
			throw new KeystoreConversionException("There was an error reading the keystore", e);
		} catch (CertificateException e) {
			throw new KeystoreConversionException("There was an error with one of the certificates", e);
		}

		//retreive the private key
		RSAPrivateCrtKey fromStorePrivateCrtKey = null;
		try {
			fromStorePrivateCrtKey = (RSAPrivateCrtKey) fromKeyStore.getKey(fromAlias, fromStoreKeyPassword);
		} catch (KeyStoreException e) {
			throw new KeystoreConversionException("There was an error retreiving the private key.", e);
		} catch (NoSuchAlgorithmException e) {
			throw new KeystoreConversionException("The converter does not support the algorithim for the key associated with alias " + fromAlias, e);
		} catch (UnrecoverableKeyException e) {
			throw new KeystoreConversionException("There was an error reading the private key from the keystore", e);
		}

		//Get the certificate and chain
		Certificate fromStoreCert = null;
		Certificate[] fromStoreCerts = null;
		try {
			fromStoreCert = fromKeyStore.getCertificate(fromAlias);
			fromStoreCerts = fromKeyStore.getCertificateChain(fromAlias);
		} catch (KeyStoreException e) {
			throw new KeystoreConversionException("There was an error retrieving the certificate or this certificate chain associated wtih the alias" + fromAlias, e);
		}

		if (fromStoreCert == null) {
			throw new KeystoreConversionException("There is no certificate associated with the alias: " + fromAlias);
		}

		//create a new keystore
		KeyStore toKeyStore = null;
		try {
			toKeyStore = KeyStore.getInstance(toStoreType, toStoreProvider);
			toKeyStore.load(null, toStorePassword);
		} catch (KeyStoreException e) {
			throw new KeystoreConversionException("Could not instantiate a new keystore ", e);
		} catch (NoSuchProviderException e) {
			throw new KeystoreConversionException("Could not find a provider for the new keystore " + toStoreFileName, e);
		} catch (IOException e) {
			throw new KeystoreConversionException("Could not load the new keystore " + toStoreFileName, e);
		} catch (NoSuchAlgorithmException e) {
			throw new KeystoreConversionException("Could not load the new keystore " + toStoreFileName, e);
		} catch (CertificateException e) {
			throw new KeystoreConversionException("Could not load the new keystore " + toStoreFileName, e);
		}

		//put the private key and cert chain into the store
		try {
			toKeyStore.setKeyEntry(toAlias, fromStorePrivateCrtKey, toStoreKeyPassword, fromStoreCerts);
		} catch (KeyStoreException e) {
			throw new KeystoreConversionException("aaaaaaaaaa", e);
		}

		OutputStream toStoreOutputStream = null;
		try {
			toStoreOutputStream = new FileOutputStream(toStoreFileName);
		} catch (FileNotFoundException e) {
			throw new KeystoreConversionException("Can't write keystore to " + toStoreFileName, e);
		}

		try {
			toKeyStore.store(toStoreOutputStream, toStorePassword);
			toStoreOutputStream.close();
		} catch (IOException e) {
			throw new KeystoreConversionException("There was an error writing the new keystore " + toStoreFileName + " to a file.", e);
		} catch (KeyStoreException e) {
			throw new KeystoreConversionException("There was an error writing to the new keystore " + toStoreFileName, e);
		} catch (NoSuchAlgorithmException e) {
			throw new KeystoreConversionException("There was an error writing to the new keystore " + toStoreFileName, e);
		} catch (CertificateException e) {
			throw new KeystoreConversionException("There was an error writing a certificate to the new store.", e);
		}

	}
}

