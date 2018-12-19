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

import etm.core.configuration.BasicEtmConfigurator;
import etm.core.configuration.EtmManager;
import etm.core.monitor.EtmMonitor;
import etm.core.monitor.EtmPoint;
import etm.core.renderer.SimpleTextRenderer;

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

	private static final EtmMonitor etmMonitor = EtmManager.getEtmMonitor();

	@Test
	public void testGETRequest() throws Exception {
		BasicEtmConfigurator.configure();

		PowerMockito.whenNew(InputStreamReader.class).withAnyArguments().thenReturn(null);
		PowerMockito.whenNew(BufferedReader.class).withAnyArguments().thenReturn(buffer);
		Mockito.when(buffer.readLine()).thenReturn("readline", null);
		etmMonitor.start();

		Request getRequest = null;
		EtmPoint point = etmMonitor.createPoint("GETRequest");

		try {
			getRequest = new GETRequest(url);
			assertEquals("readline", getRequest.send());

		} finally {
			point.collect();
		}

		Mockito.when(buffer.readLine()).thenReturn(null, null);

		point = etmMonitor.createPoint("GETRequest");

		try {
			getRequest = new GETRequest(url);
			assertEquals("", getRequest.send());

		} finally {
			point.collect();
		}

		etmMonitor.render(new SimpleTextRenderer());
		etmMonitor.stop();

	}
}
