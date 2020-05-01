package ru.r2cloud.weather;

import static org.junit.Assert.assertEquals;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class R2WeatherClientTest {

	private HttpServer server;
	private R2WeatherClient client;
	private HttpResponse bulkHandler;
	private long receptionTime = 1587056820000L;

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void testSuccess() throws Exception {
		assertSuccess();
	}

	@Test
	public void testSuccessFromFile() throws Exception {
		File file = new File(tempFolder.getRoot(), UUID.randomUUID().toString());
		try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
			os.write(new byte[] { (byte) 0xca, (byte) 0xfe });
		}
		client.upload(file, receptionTime);
		assertEquals("cafe", bulkHandler.getRequestBody());
	}

	@Test(expected = AuthenticationException.class)
	public void testAuthFailure() throws Exception {
		bulkHandler = new HttpResponse(401, "{\"status\":\"Unthentication failure\"]}");
		setupContext("/api/v1/lrpt/" + receptionTime, bulkHandler);
		assertSuccess();
	}

	@Test(expected = IOException.class)
	public void testInternalServerError() throws Exception {
		bulkHandler = new HttpResponse(503, "{\"status\":\"Internal server error\"]}");
		setupContext("/api/v1/lrpt/" + receptionTime, bulkHandler);
		assertSuccess();
	}

	@Test(expected = IOException.class)
	public void testUnknownFile() throws Exception {
		client.upload(new File(UUID.randomUUID().toString()), receptionTime);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testParameters() throws Exception {
		client.upload((File) null, receptionTime);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testParameters2() throws Exception {
		client.upload((InputStream) null, receptionTime);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testParameters3() throws Exception {
		client.upload(new ByteArrayInputStream(new byte[] { (byte) 0xca, (byte) 0xfe }), 1404777500000L);
	}

	@Before
	public void start() throws Exception {
		String host = "localhost";
		int port = 8000;
		server = HttpServer.create(new InetSocketAddress(host, port), 0);
		server.start();
		bulkHandler = new HttpResponse(200, "{\"status\":\"SUCCESS\"]}");
		server.createContext("/api/v1/lrpt/" + receptionTime, bulkHandler);
		client = new R2WeatherClient("http://" + host + ":" + port, UUID.randomUUID().toString(), 10000);
	}

	private void assertSuccess() throws IOException, AuthenticationException {
		ByteArrayInputStream bais = new ByteArrayInputStream(new byte[] { (byte) 0xca, (byte) 0xfe });
		client.upload(bais, receptionTime);
		assertEquals("cafe", bulkHandler.getRequestBody());
	}

	private void setupContext(String name, HttpHandler handler) {
		server.removeContext(name);
		server.createContext(name, handler);
	}

	@After
	public void stop() {
		if (server != null) {
			server.stop(0);
		}
	}

}
