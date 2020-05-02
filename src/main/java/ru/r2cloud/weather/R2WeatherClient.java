package ru.r2cloud.weather;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class R2WeatherClient {

	private static final Logger LOG = LoggerFactory.getLogger(R2WeatherClient.class);
	private static final String USER_AGENT = "r2weatherClient/1.1 (dernasherbrezon)";
	private static final int DEFAULT_TIMEOUT = 30_000;
	private static final long LAUNCH_TIME = 1404777600000L;

	private final String host;
	private final String apiKey;
	private final int timeoutMillis;
	private HttpClient httpclient;

	public R2WeatherClient(String host, String apiKey) {
		this(host, apiKey, DEFAULT_TIMEOUT);
	}

	public R2WeatherClient(String host, String apiKey, int timeoutMillis) {
		this.host = host;
		this.apiKey = apiKey;
		this.timeoutMillis = timeoutMillis;
		this.httpclient = HttpClient.newBuilder().version(Version.HTTP_2).followRedirects(Redirect.NORMAL).connectTimeout(Duration.ofMillis(timeoutMillis)).build();
	}

	public void upload(File lrptFile, long receptionTimeMillis) throws IOException, AuthenticationException {
		if (lrptFile == null) {
			throw new IllegalArgumentException("file is null");
		}
		if (LOG.isTraceEnabled()) {
			LOG.trace("uploading: {} from: {}", lrptFile.getAbsolutePath(), new Date(receptionTimeMillis));
		}
		try (InputStream is = new BufferedInputStream(new FileInputStream(lrptFile))) {
			upload(is, receptionTimeMillis);
		}
	}

	public void upload(InputStream is, long receptionTimeMillis) throws IOException, AuthenticationException {
		if (is == null) {
			throw new IllegalArgumentException("stream is null");
		}
		if (receptionTimeMillis < LAUNCH_TIME) {
			throw new IllegalArgumentException("invalid reception time: " + receptionTimeMillis);
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(baos);
		is.transferTo(gzip);
		gzip.close();

		Builder result = HttpRequest.newBuilder().uri(URI.create(host + "/api/v1/lrpt/" + receptionTimeMillis));
		result.timeout(Duration.ofMillis(timeoutMillis));
		result.header("User-Agent", USER_AGENT);
		result.header("Content-Type", "application/octet-stream");
		result.header("Authorization", apiKey);
		result.header("Content-Encoding", "gzip");
		result.PUT(BodyPublishers.ofByteArray(baos.toByteArray()));

		HttpRequest request = result.build();
		HttpResponse<String> response;
		try {
			response = httpclient.send(request, BodyHandlers.ofString());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return;
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("response: {}", response.body());
		}
		if (response.statusCode() == 401) {
			throw new AuthenticationException("invalid apikey");
		}

		if (response.statusCode() != 200) {
			throw new IOException("invalid response code: " + response.statusCode());
		}
	}

}
