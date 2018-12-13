package net.gjerull.etherpad.client;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.TreeMap;

import org.junit.runner.RunWith;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.Also;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;

@RunWith(JUnitQuickcheck.class)
public class EPLiteClientRandomTest {

	private static final String RESPONSE_TEMPLATE = "{\n" + "  \"code\": %d,\n" + "  \"message\": \"%s\",\n"
			+ "  \"data\": %s\n" + "}";
	private static final String API_VERSION = "1.2.12";
	private static final String ENCODING = "UTF-8";

	@Property(trials = 20)
	public void query_string_from_map(@Also({ "0", "-1" }) int envio) throws Exception {
		EPLiteConnection connection = new EPLiteConnection("http://example.com/", "apikey", API_VERSION, ENCODING);
		Map<String, Object> apiArgs = new TreeMap<>(); // Ensure ordering for testing
		apiArgs.put("padID", "g.oln5fzaE8qfv4gdE$test-1");
		apiArgs.put("rev", envio);
		String queryString = connection.queryString(apiArgs, false);
		assertEquals("apikey=apikey&padID=g.oln5fzaE8qfv4gdE$test-1&rev=" + envio, queryString);
	}

}
