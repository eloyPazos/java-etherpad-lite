package net.gjerull.etherpad.client;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
@PrepareForTest({ POSTRequest.class })
public class POSTRequestTest {

	@Mock
	URL url;

	@Mock
	OutputStreamWriter output;

	@Mock
	InputStreamReader input;

	@Mock
	StringBuilder stringBuild;

	@Mock
	BufferedReader buffer;

	@Mock
	StringBuilder builder;

	@Mock
	URLConnection conexion;

	private static final EtmMonitor etmMonitor = EtmManager.getEtmMonitor();

	@Test
	public void testPOSTTRequest() throws Exception {
		BasicEtmConfigurator.configure();

		PowerMockito.whenNew(OutputStreamWriter.class).withAnyArguments().thenReturn(output);
		PowerMockito.whenNew(InputStreamReader.class).withAnyArguments().thenReturn(input);
		PowerMockito.whenNew(BufferedReader.class).withAnyArguments().thenReturn(buffer);
		PowerMockito.whenNew(BufferedReader.class).withAnyArguments().thenReturn(buffer);
		Mockito.when(buffer.readLine()).thenReturn("readline", null);
		Mockito.when(url.openConnection()).thenReturn(conexion);
		etmMonitor.start();

		EtmPoint point = etmMonitor.createPoint("POSTRequest");
		Request postRequest = null;
		try {

			postRequest = new POSTRequest(url, "readline");

			assertEquals("readline", postRequest.send());

		} finally {
			point.collect();
		}

		Mockito.when(buffer.readLine()).thenReturn("", null);
		point = etmMonitor.createPoint("POSTRequest");
		try {
			assertEquals("", postRequest.send());
		} finally {
			point.collect();
		}

		Mockito.when(buffer.readLine()).thenReturn(null, null);
		point = etmMonitor.createPoint("POSTRequest");

		try {

			assertEquals("", postRequest.send());
		} finally {
			point.collect();
		}

		etmMonitor.render(new SimpleTextRenderer());
		etmMonitor.stop();
	}

}
