package pkit.cert;

/**
 *  Utility methods for various cert tasks
 *
 *@author     justinw
 *@created    13 February 2003
 */
public class SignUtils {

	/**
	 *  Formats the paramters into a DN string
	 *
	 *@param  commonName 
	 *@param  orgUnit  
	 *@param  org  
	 *@param  locality 
	 *@param  region
	 *@param  countryCode 
	 *@return              returns a DN string
	 */
	public static String createDN(String commonName, String orgUnit, String org, String locality, String region, String countryCode) {
		StringBuffer sb = new StringBuffer();
		sb.append("CN=").append(commonName).append(", OU=").append(orgUnit).append(", O=").append(org).append(", L=").append(locality).append(", ST=").append(region).append(", C=").append(countryCode);
		return sb.toString();
	}
}

