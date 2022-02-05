package crypto;

import java.security.*;
import java.io.*;
import java.util.Scanner;

/*
 * KeyGen.java
 * 
 * This class creates two keys, a public key and a private key.
 * We ask our user to output these two keys into separate files to be used
 * for our encryption and decryption classes.
 */


public class KeyGen {

	public static Scanner input;
	//protected static KeyPair publicPrivateKey;
	protected static final String ALGORITHM = "RSA";

	//Creates a pair of keys.  A private key, and a public key.
	public static KeyPair generateKey() throws NoSuchAlgorithmException
	{
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
		keyGen.initialize(1024);
		KeyPair key = keyGen.generateKeyPair();
		return key;
	}

	public static void main(String[] args) {
		input = new Scanner(System.in);
		System.out.println("RSA Key Generator");
		try{
			System.out.println("Please wait while the keys are being made.");
			KeyPair publicPrivateKey = generateKey();
			//Need to get the separate keys for output into a file
			PrivateKey privateKey = publicPrivateKey.getPrivate();
			PublicKey publicKey = publicPrivateKey.getPublic();
			//Outputs our keys
			System.out.println("Input the name for your public key file(without .txt)");
			String pubkey = input.nextLine();
			FileOutputStream FOS = new FileOutputStream(new File(pubkey + ".txt"));
			FOS.write(publicKey.getEncoded());
			FOS.close();
			//Close the first one to make sure it does not affect the next output.
			System.out.println("Input the name for your private key file(without .txt)");
			String privkey = input.nextLine();
			FOS = new FileOutputStream(new File(privkey + ".txt"));
			FOS.write(privateKey.getEncoded());
			FOS.close();
			
			System.out.println("Keys successfully created and outputted into " + pubkey + ".txt and " + privkey + ".txt respectively.");
			
		}
		catch(Exception e){
			System.out.println("Error, algorithm does not exist.");
		}
	}


	//The simpleKeyGen is the part from part A of the homework.  I decided to separate it from the main class for part B.
	/*
	 * It asks for two prime numbers.  If one(or both) isn't or there is a char instead then it will end.
	 * After checking, it will create the keys.
	 */
	private static void simpleKeyGen(){
		// choose two distinct primes with p < q
		System.out.println("Please put in two prime numbers with a space between them(Example: '5 17').");
		input = new Scanner(System.in);
		//try-catch to make sure what is inputted are numbers and not characters.
		try{
			long p = input.nextLong();
			long q = input.nextLong();
			if(!isPrime(p) || !isPrime(q)){
				System.out.println("One or both numbers are not prime.  Program will exit.");
				System.exit(0);
			}

			System.out.println("p = " + p + " q = " + q );

			// choose n as the product of p and q
			// no known algorithm can recompute p and q from n within a reasonable period
			// of time for large n.

			long n = p * q;
			System.out.println("The value of n = " + n);

			// Compute phi = (p-1)*(q-1).

			long phi = (p - 1) * ( q - 1);
			System.out.println("The value of PHI = " + phi);

			// choose a random prime e between 1 and phi, exclusive, so that e
			// has no common factors with phi.

			long e = findfirstnocommon(phi);

			System.out.println("The public exponent = " + e);

			// Compute d as the multiplicative inverse of e
			// modulo phi(n).

			long d = findinverse(e,phi);

			System.out.println("The private key is " + d);

			System.out.println( " (d) (e) mod phi = " + (d * e) % phi);
		}
		catch(Exception e){
			System.out.println("Inputted item is not a number.");
		}

	}


	/*
	 * This function is used to confirm the number inputed in the given fields is a prime.
	 * Returns true if it is.  False if it is divisible by a number.
	 */
	private static boolean isPrime(long p){
		long h = (p/2)+1;
		for(long i = 2; i <= h; i++){
			//			System.out.println(i + " " + p%i);
			if(p%i == 0){
				System.out.println(p + " Is not a prime number.");
				return false;
			}
		}
		System.out.println(p + " Is a prime number.");
		return true;
	}

	private static long findfirstnocommon(long n) {
		long j;
		for(j = 2; j < n; j++)
			if(euclid(n,j) == 1) return j;
		return 0;
	}


	private static long findinverse(long n, long phi) {
		long i = 2;
		while( ((i * n) % phi) != 1) i++;
		return i;
	}


	// m and n are two positive integers (not both 0)
	// returns the largest integer that divides both m and n exactly
	private static long euclid(long m, long n) {
		while(m > 0) {
			long t = m;
			m = n % m;
			n = t;
		}
		return n;
	}

}
