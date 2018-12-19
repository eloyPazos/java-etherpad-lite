package net.gjerull.etherpad.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;

import etm.core.configuration.BasicEtmConfigurator;
import etm.core.configuration.EtmManager;
import etm.core.monitor.EtmMonitor;
import etm.core.monitor.EtmPoint;
import etm.core.renderer.SimpleTextRenderer;

public class EPLiteConnectionTest {
	private static final String RESPONSE_TEMPLATE = "{\n" + "  \"code\": %d,\n" + "  \"message\": \"%s\",\n"
			+ "  \"data\": %s\n" + "}";
	private static final String API_VERSION = "1.2.12";
	private static final String ENCODING = "UTF-8";
	private static final EtmMonitor etmMonitor = EtmManager.getEtmMonitor();

	@Before
	public void prepare() {
		BasicEtmConfigurator.configure();
	}

	public void display() {
		etmMonitor.render(new SimpleTextRenderer());
		etmMonitor.stop();
	}

	@Test
	public void domain_with_trailing_slash_when_construction_an_api_path() throws Exception {
		prepare();
		EPLiteConnection connection = new EPLiteConnection("http://example.com/", "apikey", API_VERSION, ENCODING);
		etmMonitor.start();
		EtmPoint point = etmMonitor.createPoint("apiPath");
		String apiMethodPath = null;
		try {

			apiMethodPath = connection.apiPath("exampleMethod");
		} finally {
			point.collect();
		}
		display();
		assertEquals("/api/1.2.12/exampleMethod", apiMethodPath);
	}

	@Test
	public void domain_without_trailing_slash_when_construction_an_api_path() throws Exception {
		EPLiteConnection connection = new EPLiteConnection("http://example.com", "apikey", API_VERSION, ENCODING);
		String apiMethodPath = connection.apiPath("exampleMethod");
		assertEquals("/api/1.2.12/exampleMethod", apiMethodPath);
	}

	@Test
	public void query_string_from_map() throws Exception {
		EPLiteConnection connection = new EPLiteConnection("http://example.com/", "apikey", API_VERSION, ENCODING);
		Map<String, Object> apiArgs = new TreeMap<>(); // Ensure ordering for testing
		apiArgs.put("padID", "g.oln5fzaE8qfv4gdE$test-1");
		apiArgs.put("rev", 27);

		String queryString = connection.queryString(apiArgs, false);

		assertEquals("apikey=apikey&padID=g.oln5fzaE8qfv4gdE$test-1&rev=27", queryString);
	}
	// faise o test do caso en que se envie o mapa baleiro

	@Test
	public void query_string_from_map_empty() throws Exception {
		EPLiteConnection connection = new EPLiteConnection("http://example.com/", "apikey", API_VERSION, ENCODING);
		Map<String, Object> apiArgs = new TreeMap<>(); // Ensure ordering for testing

		String queryString = connection.queryString(apiArgs, false);

		assertEquals("apikey=apikey", queryString);
	}

	@Test
	public void url_encoded_query_string_from_map() throws Exception {
		EPLiteConnection connection = new EPLiteConnection("http://example.com/", "apikey", API_VERSION, ENCODING);
		Map<String, Object> apiArgs = new TreeMap<>(); // Ensure ordering for testing
		apiArgs.put("padID", "g.oln5fzaE8qfv4gdE$test-1");
		apiArgs.put("text", "æøå");

		String queryString = connection.queryString(apiArgs, true);

		assertEquals("apikey=apikey&padID=g.oln5fzaE8qfv4gdE%24test-1&text=%C3%A6%C3%B8%C3%A5", queryString);
	}

	@Test(expected = EPLiteException.class)
	public void api_url_need_to_be_absolute() throws Exception {
		EPLiteConnection connection = new EPLiteConnection("http://example.com/", "apikey", API_VERSION, ENCODING);
		connection.apiUrl("relative-path", null);
	}

	@Test
	public void handle_valid_response_from_server() throws Exception {
		EPLiteConnection connection = new EPLiteConnection("http://example.com/", "apikey", API_VERSION, ENCODING);
		String listAllPads = "{\"padIDs\": [\n" + "  \"test\",\n" + "  \"test2\",\n" + "  \"test3\"\n" + "]}";
		String serverResponse = String.format(RESPONSE_TEMPLATE, 0, "no or wrong API Key", listAllPads);

		Map response = (Map) connection.handleResponse(serverResponse);

		assertEquals("test", ((List) response.get("padIDs")).get(0));
	}

	@Test
	public void handle_invalid_parameter_error_from_server() throws Exception {
		EPLiteConnection connection = new EPLiteConnection("http://example.com/", "apikey", API_VERSION, ENCODING);
		String serverResponse = String.format(RESPONSE_TEMPLATE, 1, "groupID does not exist", null);

		try {
			connection.handleResponse(serverResponse);
			fail("Expected '" + EPLiteException.class.getName() + "' to be thrown");
		} catch (EPLiteException e) {
			assertEquals("groupID does not exist", e.getMessage());
		}

	}

	@Test
	public void handle_internal_error_from_server() throws Exception {
		EPLiteConnection connection = new EPLiteConnection("http://example.com/", "apikey", API_VERSION, ENCODING);
		String serverResponse = String.format(RESPONSE_TEMPLATE, 2, "an internal error has occurred", null);

		try {
			connection.handleResponse(serverResponse);
			fail("Expected '" + EPLiteException.class.getName() + "' to be thrown");
		} catch (EPLiteException e) {
			assertEquals("an internal error has occurred", e.getMessage());
		}
	}

	@Test
	public void handle_no_such_function_error_from_server() throws Exception {
		EPLiteConnection connection = new EPLiteConnection("http://example.com/", "apikey", API_VERSION, ENCODING);
		String serverResponse = String.format(RESPONSE_TEMPLATE, 3, "no such function", null);

		try {
			connection.handleResponse(serverResponse);
			fail("Expected '" + EPLiteException.class.getName() + "' to be thrown");
		} catch (EPLiteException e) {
			assertEquals("no such function", e.getMessage());
		}
	}

	@Test
	public void handle_invalid_key_error_from_server() throws Exception {
		EPLiteConnection connection = new EPLiteConnection("http://example.com/", "invalid", API_VERSION, ENCODING);
		String serverResponse = String.format(RESPONSE_TEMPLATE, 4, "no or wrong API Key", null);

		try {
			connection.handleResponse(serverResponse);
			fail("Expected '" + EPLiteException.class.getName() + "' to be thrown");
		} catch (EPLiteException e) {
			assertEquals("no or wrong API Key", e.getMessage());
		}
	}

	@Test
	public void handle_unknown_error_from_server() throws Exception {
		EPLiteConnection connection = new EPLiteConnection("http://example.com/", "invalid", API_VERSION, ENCODING);
		String serverResponse = String.format(RESPONSE_TEMPLATE, 12, "no or wrong API Key", null);

		try {
			connection.handleResponse(serverResponse);
			fail("Expected '" + EPLiteException.class.getName() + "' to be thrown");
		} catch (EPLiteException e) {
			System.out.println(e.getMessage());
			assertTrue(e.getMessage().contains("An unknown error has occurred while handling the response:"));
		}
	}

	@Test
	public void unparsable_response_from_the_server() throws Exception {
		EPLiteConnection connection = new EPLiteConnection("http://example.com/", "apikey", API_VERSION, ENCODING);
		String serverResponse = "<html>\n" + "<head><title>Some HTML</title></head><body><h1>Some HTML</h1></body>"
				+ "</html>\n";

		try {
			connection.handleResponse(serverResponse);
			fail("Expected '" + EPLiteException.class.getName() + "' to be thrown");
		} catch (EPLiteException e) {
			assertTrue("Unexpected Message: " + e.getMessage(),
					e.getMessage().startsWith("Unable to parse JSON response ("));
		}
	}

	@Test
	public void unexpected_response_from_the_server() throws Exception {
		EPLiteConnection connection = new EPLiteConnection("http://example.com/", "apikey", API_VERSION, ENCODING);
		String serverResponse = "{}";

		try {
			connection.handleResponse(serverResponse);
			fail("Expected '" + EPLiteException.class.getName() + "' to be thrown");
		} catch (EPLiteException e) {
			assertTrue("Unexpected Message: " + e.getMessage(),
					e.getMessage().startsWith("An unexpected response from the server:"));
		}
	}

	@Test
	public void valid_response_with_null_data() throws Exception {
		EPLiteConnection connection = new EPLiteConnection("http://example.com/", "apikey", API_VERSION, ENCODING);
		String serverResponse = String.format(RESPONSE_TEMPLATE, 0, "everything ok", null);

		Object response = connection.handleResponse(serverResponse);
		assertNull(response);
	}
}
