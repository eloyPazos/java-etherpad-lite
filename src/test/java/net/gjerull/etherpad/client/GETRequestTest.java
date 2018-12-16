package net.gjerull.etherpad.client;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ GETRequest.class })
public class GETRequestTest {

	@Mock
	URL url;

	@Mock
	InputStreamReader input;

	@Mock
	InputStream urlReturn;

	@Mock
	BufferedReader buffer;

	@Mock
	URLConnection conexion;

	@Test
	public void testGETRequest() throws Exception {

		PowerMockito.whenNew(InputStreamReader.class).withAnyArguments().thenReturn(null);
		PowerMockito.whenNew(BufferedReader.class).withAnyArguments().thenReturn(buffer);
		Mockito.when(buffer.readLine()).thenReturn("readline", null);
		Request getRequest = new GETRequest(url);
		assertEquals("readline", getRequest.send());

		Mockito.when(buffer.readLine()).thenReturn(null, null);

		assertEquals("", getRequest.send());

	}
}
