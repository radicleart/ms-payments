package com.radicle.payments.payments.service;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.radicle.payments.payments.service.domain.TransactionData;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@TypeAlias(value = "PaymentHelper")
public class PaymentHelper {

	@Value("${radicle.stax.stacksmate}") String stacksmatePath;
	@Autowired private RestOperations restTemplate;
    private static final Logger logger = LogManager.getLogger(PaymentHelper.class);
	@Autowired private Environment environment;

	public String sendPackage(TransactionData transactionData) throws JsonProcessingException {
		String url = stacksmatePath + "/stacksmate/transfer-nft";
		if (transactionData.getType() != null) {
			url = stacksmatePath + "/stacksmate/" + transactionData.getType();
		}
		HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);

		ResponseEntity<String> response = null;
		// String jsonInString = mapper.writeValueAsString(transactionData);
		HttpEntity<TransactionData> entity = new HttpEntity<TransactionData>(transactionData, headers);
		response = restTemplate.postForEntity(url, entity, String.class);
		logger.info("StacksMate Response: " + response.getBody());
		return response.getBody();
	}

	public void checkHash(String hashed_order, String paymentId) throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException, UnsupportedEncodingException {
		String hexVer = createHash(paymentId);
	    if (!hexVer.equals(hashed_order)) {
	    	throw new RuntimeException("Unknown hashed code sender.");
	    }
	}

	public String createHash(String paymentId) throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException, UnsupportedEncodingException {
		Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
		SecretKeySpec secret_key = new SecretKeySpec(environment.getProperty("OPENNODE_API_KEY_SM").getBytes("UTF-8"), "HmacSHA256");
		sha256_HMAC.init(secret_key);
		String hexVer = Hex.encodeHexString(sha256_HMAC.doFinal(paymentId.getBytes("UTF-8")));
	    return hexVer;
	}


}
