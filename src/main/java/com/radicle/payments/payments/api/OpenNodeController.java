package com.radicle.payments.payments.api;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.radicle.payments.payments.api.model.OpenNodeChargesBean;
import com.radicle.payments.payments.api.model.OpenNodeChargesResponse;
import com.radicle.payments.payments.service.OpenNodePaymentRepository;
import com.radicle.payments.payments.service.PaymentHelper;
import com.radicle.payments.payments.service.domain.OpenNodePayment;
import com.radicle.payments.payments.service.domain.TransactionData;

@RestController
public class OpenNodeController {

    private static final Logger logger = LogManager.getLogger(OpenNodeController.class);
	@Autowired private RestOperations restTemplate;
	@Value("${opennode.api.apiEndpoint}") String apiEndpoint;
	@Value("${opennode.api.callbackUrl}") String callbackUrl;
	@Autowired private PaymentHelper paymentHelper;
	@Autowired private OpenNodePaymentRepository openNodePaymentRepository;
	@Autowired private ObjectMapper mapper;
	@Autowired private SimpMessagingTemplate simpMessagingTemplate;
    private static final String HMAC_SHA512 = "HmacSHA512";
	@Autowired private Environment environment;

	@PostMapping(value = "/v2/fetchPayment")
	public OpenNodePayment fetchPayment(HttpServletRequest request, @RequestBody OpenNodePayment openNodePayment) throws JsonMappingException, JsonProcessingException {
		String url = apiEndpoint +  "/v1/charges";
		OpenNodeChargesBean bean = new OpenNodeChargesBean();
		bean.setCallback_url(callbackUrl);
		bean.setAmount(openNodePayment.getAmount());
		bean.setDescription(openNodePayment.getDescription());
		bean.setRouteHints(openNodePayment.getRouteHints());
		HttpEntity<String> entity = new HttpEntity<String>(convertMessage(bean), getHeaders());
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
		
		OpenNodeChargesResponse rates = (OpenNodeChargesResponse)mapper.readValue(response.getBody(), new TypeReference<OpenNodeChargesResponse>() {});
		OpenNodePayment payment = rates.getData();
		openNodePayment.getTransactionData().setPaymentId(payment.getId());
		payment.setTransactionData(openNodePayment.getTransactionData());
		Optional<OpenNodePayment> paymentDb = openNodePaymentRepository.findById(payment.getId());
		if (paymentDb.isPresent()) {
			openNodePaymentRepository.deleteById(payment.getId());
		}
		openNodePaymentRepository.save(payment);
		return payment;
	}

	@PostMapping(value = "/v2/checkPayment/{paymentId}")
	public OpenNodePayment checkPayment(HttpServletRequest request, @PathVariable String paymentId) throws JsonMappingException, JsonProcessingException {
		logger.info("PAYMENTS-OPENNODE: checkPayment=", paymentId);
		if (paymentId == null || paymentId.equals("undefined")) return null;
		String url = apiEndpoint +  "/v1/charge/" + paymentId;
		try {
			ResponseEntity<String> response1 = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<String>(getHeaders()), String.class);
			OpenNodeChargesResponse rates = (OpenNodeChargesResponse) mapper.readValue(response1.getBody(), new TypeReference<OpenNodeChargesResponse>() {});
			OpenNodePayment payment = rates.getData();
			// check database for this payment and update if required
			Optional<OpenNodePayment> paymentDbOp = openNodePaymentRepository.findById(payment.getId());
			if (paymentDbOp.isPresent()) {
				OpenNodePayment paymentDb = paymentDbOp.get();
				paymentDb.setStatus(payment.getStatus());
				openNodePaymentRepository.save(paymentDb);
				TransactionData td = paymentDb.getTransactionData();
				if (td.getBatchOption() == null) td.setBatchOption(1L);
				if (td.getPrice() == null) td.setPrice(100L);
				if (paymentDb.getStatus().equals("paid")) {
					if (td.getTxStatus() == null || td.getTxStatus().equals("started") || td.getTxStatus().equals("errored") || paymentDb.getTransactionData().getStacksmateResponse() == null) {
						paymentDb = sendStacksTx(payment);
					}
				}
				return paymentDb;
			} else {
				return payment;
			}
		} catch (RestClientException e) {
			logger.error("No payment for paymentId: " + url + " Error: " + e.getMessage());
			return null;
		}
	}

	@PostMapping(value = "/v2/charge/callback")
	public void chargeCallback(@RequestBody String charge) throws InvalidKeyException, NoSuchAlgorithmException, IllegalStateException, UnsupportedEncodingException {
		logger.info("Received callback: " + charge);
		// id=a2023596-80eb-4891-bf92-9007293b4e76&callback_url=https%3A%2F%2Ftapi.risidio.com%2Fmesh%2Fv2%2Fcharge%2Fcallback&success_url=https%3A%2F%2Fopennode.com&status=processing&order_id=N%2FA&description=Simulating+webhook+processing&price=100000000&fee=0&auto_settle=0&hashed_order=ed145226b96572625933b4ec52d35722534bb93c9c43eecfdd248cb7e80e9ffe
		// Received callback: id=b0f5a1da-4cf9-48ce-a0a0-e52966c9910f&callback_url=https%3A%2F%2Fapi.risidio.com%2Fmesh%2Fv2%2Fcharge%2Fcallback&success_url=null&status=paid&order_id=null&description=Donation+to+project&price=6006&fee=0&auto_settle=false&fiat_value=1.98&net_fiat_value=1.98&hashed_order=3d19cd4dce77fd035d98b10cac74f490e828c25361ef949a1c0551c048403e02
		String [] params = charge.split("&");
		Map<String, String> items = new HashMap<String, String>();
		for (String param : params) {
			String [] nv = param.split("=");
			if (nv != null && nv.length == 2) {
				items.put(nv[0], nv[1]);
			}
		}
		
		String hashed_order = items.get("hashed_order");
		String paymentId = items.get("id");
		paymentHelper.checkHash(hashed_order, paymentId);
		
		Optional<OpenNodePayment> paymentDbOp = openNodePaymentRepository.findById(paymentId);
		if (paymentDbOp.isPresent()) {
			OpenNodePayment payment = paymentDbOp.get();
			payment.setStatus(items.get("status"));
			payment.getTransactionData().setHashed_order(hashed_order);
			if (payment.getStatus().equals("paid")) {
				sendStacksTx(payment);
			}
		}
	}

	private OpenNodePayment sendStacksTx(OpenNodePayment payment) {
		String response = null;
		try {
			payment.getTransactionData().setHashed_order(payment.getHashed_order());
			payment.getTransactionData().setPaymentId(payment.getId());
			payment.getTransactionData().setTxStatus("started");
			// save it here in case we need to do a refund later
			openNodePaymentRepository.save(payment);
			response = paymentHelper.sendPackage(payment.getTransactionData());
			if (response == null || response.indexOf("error") > -1) {
				payment.getTransactionData().setTxStatus("errored");
			} else {
				payment.getTransactionData().setTxStatus("pending");
			}
			payment.getTransactionData().setStacksmateResponse(response);
			openNodePaymentRepository.save(payment);
		} catch (Exception e) {
			payment.getTransactionData().setTxStatus("errored");
			openNodePaymentRepository.save(payment);
		}
		simpMessagingTemplate.convertAndSend("/queue/payment-news-" + payment.getId(), response);
		return payment;
	}


	private String convertMessage(Object model) {
		try {
			return mapper.writeValueAsString(model);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	private HttpHeaders getHeaders() {
//		String val = " "; // environment.getProperty("BTC_ACCESS_KEY_ID");
//		String auth = "Authorization" + ":" + val;
//		String encodedAuth = new String(Base64.getEncoder().encode(auth.getBytes(Charset.forName("UTF8"))));
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", environment.getProperty("OPENNODE_API_KEY_SM"));
		// headers.setContentLength(jsonInString.length());
		return headers;
	}
	
	private String doSHA() {
        Mac sha512Hmac;
        String result = null;
        final String key = "Welcome1";

        try {
            final byte[] byteKey = key.getBytes(StandardCharsets.UTF_8);
            sha512Hmac = Mac.getInstance(HMAC_SHA512);
            SecretKeySpec keySpec = new SecretKeySpec(byteKey, HMAC_SHA512);
            sha512Hmac.init(keySpec);
            byte[] macData = sha512Hmac.doFinal("My message".getBytes(StandardCharsets.UTF_8));

            // Can either base64 encode or put it right into hex
            result = Base64.getEncoder().encodeToString(macData);
            // result = bytesToHex(macData);
            return result;
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return result;
        }
    }

}
