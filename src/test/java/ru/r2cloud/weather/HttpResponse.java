package ru.r2cloud.weather;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class HttpResponse implements HttpHandler {

	private static final int HEX_0X0F = 0x0F;

	private final int statusCode;
	private final String message;
	
	private String userAgent;
	private String requestBody;
	private int executedTimes;

	public HttpResponse(int statusCode, String message) {
		this.statusCode = statusCode;
		this.message = message;
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		userAgent = exchange.getRequestHeaders().getFirst("User-Agent");
		requestBody = convertToString(exchange.getRequestBody());
		executedTimes++;
		byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
		exchange.sendResponseHeaders(statusCode, bytes.length);
		OutputStream os = exchange.getResponseBody();
		os.write(bytes);
		os.close();
	}

	private static String convertToString(InputStream is) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			new GZIPInputStream(is).transferTo(baos);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return convertToHex(baos.toByteArray());
	}

	private static String convertToHex(final byte[] data) {
		final StringBuilder buf = new StringBuilder();
		for (final byte element : data) {
			int halfbyte = (element >>> 4) & HEX_0X0F;
			int twoHalfs = 0;
			do {
				if (0 <= halfbyte && halfbyte <= 9) {
					buf.append((char) ('0' + halfbyte));
				} else {
					buf.append((char) ('a' + (halfbyte - 10)));
				}
				halfbyte = element & HEX_0X0F;
			} while (twoHalfs++ < 1);
		}
		return buf.toString();
	}

	public int getExecutedTimes() {
		return executedTimes;
	}

	public String getRequestBody() {
		return requestBody;
	}

	public String getMessage() {
		return message;
	}

	public int getStatusCode() {
		return statusCode;
	}
	
	public String getUserAgent() {
		return userAgent;
	}
}
