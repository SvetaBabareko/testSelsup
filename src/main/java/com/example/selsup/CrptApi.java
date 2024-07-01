package com.example.selsup;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@SpringBootApplication
public class CrptApi {

	private final TimeUnit timeUnit;
	private final int requestLimit;

	public CrptApi(TimeUnit timeUnit, int requestLimit) {
		this.timeUnit = timeUnit;
		this.requestLimit = requestLimit;
	}

	public static void main(String[] args) throws IOException {
		CrptApi crptApi = new CrptApi(TimeUnit.SECONDS, 2);
		Document document = crptApi.getDocument();
		String signature = crptApi.getSignature();

		crptApi.createDocument(document, signature);
	}


	private Document getDocument() {
		Description description = new Description("INN");

		var product1 = new Product("certificate1", new Date(), "1", "ownerInn1", "producerInn1", new Date(), "tnvedCode1", "uitCode1", "uituCode1");
		var product2 = new Product("certificate2", new Date(), "2", "ownerInn2", "producerInn2", new Date(), "tnvedCode2", "uitCode2", "uituCode2");

		List<Product> products = new ArrayList<>();
		products.add(product1);
		products.add(product2);

		return new Document(description, "docId", "approved", "type", Boolean.TRUE, "ownerInn", "participantInn", "producerInn", new Date(), "type2", products, new Date(), "regNumber");
	}

	private String getSignature() {
		return "signature";
	}

	private void createDocument(Document document, String signature) throws IOException {
		var rateLimiter = RateLimiter.create(requestLimit / (timeUnit.toSeconds(1) / 1000.0));
		rateLimiter.acquire();
		ReentrantLock locker = new ReentrantLock();
		locker.lock();
		send(document, signature);
		locker.unlock();
	}

	private void send(Document document, String signature) throws IOException {
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			HttpPost httpPost = new HttpPost("https://ismp.crpt.ru/api/v3/lk/documents/create");

			ObjectMapper objectMapper = new ObjectMapper();
			String json = objectMapper.writeValueAsString(document);

			StringEntity entity = new StringEntity(json);
			httpPost.setEntity(entity);
			httpPost.setHeader("Content-Type", ContentType.APPLICATION_JSON.toString());
			httpPost.setHeader("Signature", signature);

			httpClient.execute(httpPost);
		}
	}
}

class Document {

	private Description description;

	@JsonProperty("doc_id")
	private String docId;

	@JsonProperty("docStatus")
	private String docStatus;

	@JsonProperty("docType")
	private String docType;

	private boolean importRequest;

	@JsonProperty("ownerInn")
	private String ownerInn;

	@JsonProperty("participant_inn")
	private String participantInn;

	@JsonProperty("producer_inn")
	private String producerInn;

	@JsonProperty("production_date")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date productionDate;

	@JsonProperty("production_type")
	private String productionType;

	private List<Product> products;

	@JsonProperty("reg_date")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date regDate;

	@JsonProperty("reg_number")
	private String regNumber;

	public Document(Description description, String docId, String docStatus, String docType, boolean importRequest, String ownerInn, String participantInn, String producerInn, Date productionDate, String productionType, List<Product> products, Date regDate, String regNumber) {
		this.description = description;
		this.docId = docId;
		this.docStatus = docStatus;
		this.docType = docType;
		this.importRequest = importRequest;
		this.ownerInn = ownerInn;
		this.participantInn = participantInn;
		this.producerInn = producerInn;
		this.productionDate = productionDate;
		this.productionType = productionType;
		this.products = products;
		this.regDate = regDate;
		this.regNumber = regNumber;
	}

	public Description getDescription() {
		return description;
	}

	public void setDescription(Description description) {
		this.description = description;
	}

	public String getDocId() {
		return docId;
	}

	public void setDocId(String docId) {
		this.docId = docId;
	}

	public String getDocStatus() {
		return docStatus;
	}

	public void setDocStatus(String docStatus) {
		this.docStatus = docStatus;
	}

	public String getDocType() {
		return docType;
	}

	public void setDocType(String docType) {
		this.docType = docType;
	}

	public boolean isImportRequest() {
		return importRequest;
	}

	public void setImportRequest(boolean importRequest) {
		this.importRequest = importRequest;
	}

	public String getOwnerInn() {
		return ownerInn;
	}

	public void setOwnerInn(String ownerInn) {
		this.ownerInn = ownerInn;
	}

	public String getParticipantInn() {
		return participantInn;
	}

	public void setParticipantInn(String participantInn) {
		this.participantInn = participantInn;
	}

	public String getProducerInn() {
		return producerInn;
	}

	public void setProducerInn(String producerInn) {
		this.producerInn = producerInn;
	}

	public Date getProductionDate() {
		return productionDate;
	}

	public void setProductionDate(Date productionDate) {
		this.productionDate = productionDate;
	}

	public String getProductionType() {
		return productionType;
	}

	public void setProductionType(String productionType) {
		this.productionType = productionType;
	}

	public List<Product> getProducts() {
		return products;
	}

	public void setProducts(List<Product> products) {
		this.products = products;
	}

	public Date getRegDate() {
		return regDate;
	}

	public void setRegDate(Date regDate) {
		this.regDate = regDate;
	}

	public String getRegNumber() {
		return regNumber;
	}

	public void setRegNumber(String regNumber) {
		this.regNumber = regNumber;
	}
}

class Description {

	private String participantInn;

	public Description(String participantInn) {
		this.participantInn = participantInn;
	}

	public String getParticipantInn() {
		return participantInn;
	}

	public void setParticipantInn(String participantInn) {
		this.participantInn = participantInn;
	}
}

class Product {

	@JsonProperty("certificate_document")
	private String certificateDocument;

	@JsonProperty("certificate_document_date")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date certificateDocumentDate;

	@JsonProperty("certificate_document_number")
	private String certificateDocumentNumber;

	@JsonProperty("owner_inn")
	private String ownerInn;

	@JsonProperty("producer_inn")
	private String producerInn;

	@JsonProperty("production_date")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date productionDate;

	@JsonProperty("tnved_code")
	private String tnvedCode;

	@JsonProperty("uit_code")
	private String uitCode;

	@JsonProperty("uitu_code")
	private String uituCode;

	public Product(String certificateDocument, Date certificateDocumentDate, String certificateDocumentNumber, String ownerInn, String producerInn, Date productionDate, String tnvedCode, String uitCode, String uituCode) {
		this.certificateDocument = certificateDocument;
		this.certificateDocumentDate = certificateDocumentDate;
		this.certificateDocumentNumber = certificateDocumentNumber;
		this.ownerInn = ownerInn;
		this.producerInn = producerInn;
		this.productionDate = productionDate;
		this.tnvedCode = tnvedCode;
		this.uitCode = uitCode;
		this.uituCode = uituCode;
	}

	public String getCertificateDocument() {
		return certificateDocument;
	}

	public void setCertificateDocument(String certificateDocument) {
		this.certificateDocument = certificateDocument;
	}

	public Date getCertificateDocumentDate() {
		return certificateDocumentDate;
	}

	public void setCertificateDocumentDate(Date certificateDocumentDate) {
		this.certificateDocumentDate = certificateDocumentDate;
	}

	public String getCertificateDocumentNumber() {
		return certificateDocumentNumber;
	}

	public void setCertificateDocumentNumber(String certificateDocumentNumber) {
		this.certificateDocumentNumber = certificateDocumentNumber;
	}

	public String getOwnerInn() {
		return ownerInn;
	}

	public void setOwnerInn(String ownerInn) {
		this.ownerInn = ownerInn;
	}

	public String getProducerInn() {
		return producerInn;
	}

	public void setProducerInn(String producerInn) {
		this.producerInn = producerInn;
	}

	public Date getProductionDate() {
		return productionDate;
	}

	public void setProductionDate(Date productionDate) {
		this.productionDate = productionDate;
	}

	public String getTnvedCode() {
		return tnvedCode;
	}

	public void setTnvedCode(String tnvedCode) {
		this.tnvedCode = tnvedCode;
	}

	public String getUitCode() {
		return uitCode;
	}

	public void setUitCode(String uitCode) {
		this.uitCode = uitCode;
	}

	public String getUituCode() {
		return uituCode;
	}

	public void setUituCode(String uituCode) {
		this.uituCode = uituCode;
	}
}
