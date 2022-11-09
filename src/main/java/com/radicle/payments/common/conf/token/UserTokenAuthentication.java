package com.radicle.payments.common.conf.token;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.text.ParseException;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Base58;
import org.bitcoinj.params.MainNetParams;
import org.bouncycastle.util.encoders.Hex;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.util.Base64URL;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.radicle.payments.common.ForbiddenException;

public class UserTokenAuthentication {

	private static final Logger logger = LogManager.getLogger(UserTokenAuthentication.class);
	private String token;
	private String stxAddress;
	private String profile;
	private SignedJWT jwt;
	private JWTClaimsSet claims;

	public UserTokenAuthentication() {
		super();
	}

	public UserTokenAuthentication(String token) {
		this.token = token;
	}
	
	private void decode() {
		try {
			this.jwt = SignedJWT.parse(this.token);
			this.claims = jwt.getJWTClaimsSet();
			Object p = this.claims.getClaim("profile");
			this.profile = p.toString();
		} catch (Exception e) {
			throw new ForbiddenException("Protected domain: Failed to decode authentication token: " + token);
		}
	}
	
	public static UserTokenAuthentication getInstance(String bearer) {
		try {
			if (bearer == null) {
				return null;
			}
			String v1Token = bearer.split(" ")[1]; // strip out Bearer string before passing along..
			if (!v1Token.startsWith("v1:")) {
				throw new ForbiddenException("Authorization header should start with v1:");
			}
			String token = v1Token.split(":")[1];
			return new UserTokenAuthentication(token);
		} catch (Exception e) {
			throw new ForbiddenException("Protected domain: Failed to unpack authentication token");
		}
	}

	public boolean authenticate(String address) {
		decode();
		verifyToken(address);
		checkExpiration();
		return true;
	}
	
	public String issuerAddressToB58(String iss) {
		org.bitcoinj.core.ECKey key = null;
		try {
			key = org.bitcoinj.core.ECKey.fromPublicOnly(Hex.decode(iss.getBytes()));
		} catch (Exception e) {
			key = org.bitcoinj.core.ECKey.fromPublicOnly(iss.getBytes());
		}
		Address issuerAddress = new Address(MainNetParams.get(), key.getPubKeyHash());
		return issuerAddress.toBase58();
	}

	public String getUsername() throws ParseException {
		if (this.claims == null) return this.token;
		return this.claims.getStringClaim("username");
	}

	public String bitcoinAddressToHex(String address) {
		logger.info("address: " + address);
		logger.info("address Base58 decoded and hex: " + new String(Hex.encode(Base58.decode(address))), "UTF-8");
		logger.info("address hexed: " + new String(Hex.encode(Base58.decode(address))), "UTF-8");
		String hexString = new String(Hex.encode(Base58.decode(address)));
		return hexString;
	}

	/**
	 * That the JWT is signed correctly by verifying with the pubkey hex provided.
	 * 
	 * @param address
	 * @throws NoSuchAlgorithmException
	 * @throws SignatureException 
	 */
	private void verifyToken(String address) {
		try {
			org.bitcoinj.core.ECKey key = null;
			try {
				key = org.bitcoinj.core.ECKey.fromPublicOnly(Hex.decode(address.getBytes()));
			} catch (Exception e) {
				key = org.bitcoinj.core.ECKey.fromPublicOnly(address.getBytes());
			}
			org.spongycastle.math.ec.ECPoint spoint = key.getPubKeyPoint();
			BigInteger xbg = spoint.getAffineXCoord().toBigInteger();
			BigInteger ybg = spoint.getAffineYCoord().toBigInteger();
			ECKey ecKey = new ECKey.Builder(Curve.P_256K, Base64URL.encode(xbg), Base64URL.encode(ybg))
			        .keyUse(KeyUse.SIGNATURE)
			        .keyID("1")
			        .build();
			ECDSAVerifier verifier = new ECDSAVerifier(ecKey);
			if (!jwt.verify(verifier)) {
				throw new ForbiddenException("Verification of token failed.");
			}
		} catch (JOSEException e) {
			throw new ForbiddenException("Unable to verify jwt.");
		}
	}

	private void checkExpiration()  {
		Date expirationTime = this.claims.getExpirationTime();
		if (expirationTime == null) {
			return;
			//throw new ForbiddenException("No expiry set on token");
		}
		long expiry = expirationTime.getTime();
		long nowish = new Date().getTime();
		if (nowish > expiry) {
			throw new ForbiddenException("TokenContract has expired.");
		}
	}

	public String getToken() {
		return this.token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public SignedJWT getJwt() {
		return jwt;
	}

	public void setJwt(SignedJWT jwt) {
		this.jwt = jwt;
	}

	public String getStxAddress() {
		return stxAddress;
	}

	public void checkStxAddress(String address) {
		if (this.profile.indexOf(address)== -1) {
			throw new ForbiddenException("Invalid stx address: " + stxAddress);
		}
	}

	public void setStxAddress(String stxAddress) {
		this.stxAddress = stxAddress;
	}
}
