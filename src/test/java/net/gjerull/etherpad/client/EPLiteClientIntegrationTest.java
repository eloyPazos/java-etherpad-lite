package net.gjerull.etherpad.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.StringBody;

/**
 * Integration test for simple App.
 */
public class EPLiteClientIntegrationTest {
	private EPLiteClient client;
	private ClientAndServer mockServer;

	@Before
	public void setUp() throws Exception {
		this.client = new EPLiteClient("http://localhost:9001",
				"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58");
		mockServer = startClientAndServer(9001);
	}

	@After
	public void tearDown() {
		mockServer.stop();
	}

	// funciona
	@Test
	public void validate_token() throws Exception {
		mockServer
				.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/checkToken")
						.withBody("{\"apikey\":\"a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58\"}"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
	}

	// funciona
	@Test
	public void create_and_delete_group() throws Exception {
		// send creategroup

		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createGroup").withBody(
						new StringBody("apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\":\"g.n1DM0GtdONYcTPss\"}}"));

		// deleteGroup
		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deleteGroup").withBody(
				"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&groupID=g.n1DM0GtdONYcTPss"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		// lanza createGroup
		Map response = client.createGroup();

		assertTrue(response.containsKey("groupID"));
		String groupId = (String) response.get("groupID");
		assertTrue("Unexpected groupID " + groupId, groupId != null && groupId.startsWith("g."));
		// deletegroup
		client.deleteGroup(groupId);
	}

	// funciona
	@Test
	public void create_group_if_not_exists_for_and_list_all_groups() throws Exception {
		// Crea grupo se non existe
		mockServer.when(
				HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createGroupIfNotExistsFor").withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&groupMapper=groupname"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody(" {\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\":\"g.VCwoAS1g23aftsD3\"}}"));

		// lista todos os grupos
		mockServer
				.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/listAllGroups")
						.withBody("apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"groupIDs\":[\"g.VCwoAS1g23aftsD3\"]}}"));

		// deleteGroup
		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deleteGroup").withBody(
				"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&groupID=g.VCwoAS1g23aftsD3"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
		String groupMapper = "groupname";

		// Crea grupo se non existe

		Map response = client.createGroupIfNotExistsFor(groupMapper);

		assertTrue(response.containsKey("groupID"));
		String groupId = (String) response.get("groupID");
		try {
			Map listResponse = this.client.listAllGroups();
			assertTrue(listResponse.containsKey("groupIDs"));
			int firstNumGroups = ((List) listResponse.get("groupIDs")).size();

			this.client.createGroupIfNotExistsFor(groupMapper);

			listResponse = this.client.listAllGroups();
			int secondNumGroups = ((List) listResponse.get("groupIDs")).size();

			assertEquals(firstNumGroups, secondNumGroups);
		} finally {
			this.client.deleteGroup(groupId);
		}
	}

//funciona
	@Test
	public void create_author() throws Exception {

		// crea autor
		mockServer
				.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/createAuthor")
						.withBody("apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.qbuJi3mFPYCq4hVE\"}}"));

		// crea autor nome
		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthor").withBody(
				"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&name=integration-author"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.qbuJi3mFPYCq4hVE\"}}"));

		// recupera o nome do autor
		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getAuthorName").withBody(
				"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&authorID=a.qbuJi3mFPYCq4hVE"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":\"integration-author\"}"));

		// crea autor
		Map authorResponse = client.createAuthor();
		String authorId = (String) authorResponse.get("authorID");
		assertTrue(authorId != null && !authorId.isEmpty());
		// crea autor nome
		authorResponse = client.createAuthor("integration-author");
		authorId = (String) authorResponse.get("authorID");
		// recupera o nome do autor
//
		String authorName = client.getAuthorName(authorId);
		assertEquals("integration-author", authorName);
	}

//// funciona
	@Test
	public void create_author_with_author_mapper() throws Exception {
		// crea autor
		mockServer.when(
				HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthorIfNotExistsFor").withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&name=integration-author-1&authorMapper=username"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.6XbPIhBeJ2aCk7eG\"}}"));

//		// recupera nome autor
		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getAuthorName").withBody(
				"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&authorID=a.6XbPIhBeJ2aCk7eG"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":\"integration-author-1\"}"));

//		// crea autor 2
		mockServer.when(
				HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthorIfNotExistsFor").withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&name=integration-author-2&authorMapper=username"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.6XbPIhBeJ2aCk7eG\"}}"));

		// recupera nome autor 2

		// recupera nome autor 3
		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getAuthorName").withBody(
				"{\"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&authorID=a.6XbPIhBeJ2aCk7eG\"}"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":\"integration-author-2\"}"));

		String authorMapper = "username";
		// crea autor
		Map authorResponse = client.createAuthorIfNotExistsFor(authorMapper, "integration-author-1");
		String firstAuthorId = (String) authorResponse.get("authorID");
		assertTrue(firstAuthorId != null && !firstAuthorId.isEmpty());
		// recupera nome autor
		String firstAuthorName = client.getAuthorName(firstAuthorId);
//		// crea autor 2
		authorResponse = client.createAuthorIfNotExistsFor(authorMapper, "integration-author-2");
		String secondAuthorId = (String) authorResponse.get("authorID");
		assertEquals(firstAuthorId, secondAuthorId);
//		// recupera nome autor2

		mockServer.clear(
				HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getAuthorName").withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&authorID=a.6XbPIhBeJ2aCk7eG")));

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getAuthorName").withBody(
				"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&authorID=a.6XbPIhBeJ2aCk7eG"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":\"integration-author-2\"}"));

		String secondAuthorName = client.getAuthorName(secondAuthorId);
		assertNotEquals(firstAuthorName, secondAuthorName);

		// crea autor 3
		mockServer.when(
				HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthorIfNotExistsFor").withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&authorMapper=username"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.6XbPIhBeJ2aCk7eG\"}}"));

//		// crea autor 3
		authorResponse = client.createAuthorIfNotExistsFor(authorMapper);
		String thirdAuthorId = (String) authorResponse.get("authorID");
		assertEquals(secondAuthorId, thirdAuthorId);
//
		mockServer.clear(
				HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getAuthorName").withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&authorID=a.6XbPIhBeJ2aCk7eG")));

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getAuthorName").withBody(
				"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&authorID=a.6XbPIhBeJ2aCk7eG"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":\"integration-author-2\"}"));

		// recupera o nome autor
		String thirdAuthorName = client.getAuthorName(thirdAuthorId);
		// recupera nome autor 3

		assertEquals(secondAuthorName, thirdAuthorName);
	}

//aleatorio o tempo ??	
	@Test
	public void create_and_delete_session() throws Exception {
		String authorMapper = "username";
		String groupMapper = "groupname";

		// crea grupo
		mockServer.when(
				HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createGroupIfNotExistsFor").withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&groupMapper=groupname"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody(" {\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\":\"g.sVnGyuWcKe4WX4rt\"}}"));

		// crea autor

		mockServer.when(
				HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthorIfNotExistsFor").withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&name=integration-author-1&authorMapper=username"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.6XbPIhBeJ2aCk7eG\"}}"));

		// crea grupo
		Map groupResponse = client.createGroupIfNotExistsFor(groupMapper);
		String groupId = (String) groupResponse.get("groupID");
		// crea autor
		Map authorResponse = client.createAuthorIfNotExistsFor(authorMapper, "integration-author-1");
		String authorId = (String) authorResponse.get("authorID");

		int sessionDuration = 8;
		// crea sesion
		Long validUntilResponse1 = ((new Date()).getTime() + (sessionDuration * 60L * 60L * 1000L)) / 1000L;

		// crea sesion
		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createSession").withBody(
				"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&groupID=g.sVnGyuWcKe4WX4rt&validUntil="
						+ validUntilResponse1 + "&authorID=a.6XbPIhBeJ2aCk7eG"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"sessionID\":\"s.f7179037cd78e2f99b492e4ad085628d\"}}"));

		Map sessionResponse = client.createSession(groupId, authorId, sessionDuration);
		String firstSessionId = (String) sessionResponse.get("sessionID");

		Calendar oneYearFromNow = Calendar.getInstance();
		oneYearFromNow.add(Calendar.YEAR, 1);
		Date sessionValidUntil = oneYearFromNow.getTime();
		// limpa sesion 2
		mockServer.clear(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createSession"));
		Long validUntilResponse2 = sessionValidUntil.getTime() / 1000L;
		// crea sesion 2
		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createSession").withBody(
				"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&groupID=g.sVnGyuWcKe4WX4rt&validUntil="
						+ validUntilResponse2 + "&authorID=a.6XbPIhBeJ2aCk7eG"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"sessionID\":\"s.5c16699d34b57893cd76cdfda97d5d4a\"}}"));

		sessionResponse = client.createSession(groupId, authorId, sessionValidUntil);
		String secondSessionId = (String) sessionResponse.get("sessionID");
		try {
			assertNotEquals(firstSessionId, secondSessionId);
//			// recupera info da sesion

//			// recupera info da sesion
			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getSessionInfo").withBody(
					"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&sessionID=s.5c16699d34b57893cd76cdfda97d5d4a"))
					.respond(HttpResponse.response().withStatusCode(200).withBody(
							"{\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\":\"g.sVnGyuWcKe4WX4rt\",\"authorID\":\"a.6XbPIhBeJ2aCk7eG\",\"validUntil\":"
									+ validUntilResponse2 + "}}"));
			System.out.println("EJECUCION");
			Map sessionInfo = client.getSessionInfo(secondSessionId);
			assertEquals(groupId, sessionInfo.get("groupID"));
			assertEquals(authorId, sessionInfo.get("authorID"));
			assertEquals(sessionValidUntil.getTime() / 1000L, (long) sessionInfo.get("validUntil"));
			// lista a sesions do grupo

			// lista as sesions do grupo
			mockServer
					.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/listSessionsOfGroup").withBody(
							"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&groupID=g.sVnGyuWcKe4WX4rt"))
					.respond(HttpResponse.response().withStatusCode(200).withBody(
							"{\"code\":0,\"message\":\"ok\",\"data\":{\"s.f7179037cd78e2f99b492e4ad085628d\":{\"groupID\":\"g.sVnGyuWcKe4WX4rt\",\"authorID\":\"a.6XbPIhBeJ2aCk7eG\",\"validUntil\":"
									+ validUntilResponse1
									+ "},\"s.5c16699d34b57893cd76cdfda97d5d4a\":{\"groupID\":\"g.sVnGyuWcKe4WX4rt\",\"authorID\":\"a.6XbPIhBeJ2aCk7eG\",\"validUntil\":"
									+ validUntilResponse2 + "}}}"));

			Map sessionsOfGroup = client.listSessionsOfGroup(groupId);
			sessionInfo = (Map) sessionsOfGroup.get(firstSessionId);
			assertEquals(groupId, sessionInfo.get("groupID"));
			sessionInfo = (Map) sessionsOfGroup.get(secondSessionId);
			assertEquals(groupId, sessionInfo.get("groupID"));
			// lista as sesions do autor

			// lista as sesions do autor
			mockServer
					.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/listSessionsOfAuthor").withBody(
							"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&authorID=a.6XbPIhBeJ2aCk7eG"))
					.respond(HttpResponse.response().withStatusCode(200).withBody(
							"{\"code\":0,\"message\":\"ok\",\"data\":{\"s.f7179037cd78e2f99b492e4ad085628d\":{\"groupID\":\"g.sVnGyuWcKe4WX4rt\",\"authorID\":\"a.6XbPIhBeJ2aCk7eG\",\"validUntil\":"
									+ validUntilResponse1
									+ "},\"s.5c16699d34b57893cd76cdfda97d5d4a\":{\"groupID\":\"g.sVnGyuWcKe4WX4rt\",\"authorID\":\"a.6XbPIhBeJ2aCk7eG\",\"validUntil\":"
									+ validUntilResponse1 + "}}}"));

			Map sessionsOfAuthor = client.listSessionsOfAuthor(authorId);
			sessionInfo = (Map) sessionsOfAuthor.get(firstSessionId);
			assertEquals(authorId, sessionInfo.get("authorID"));
			sessionInfo = (Map) sessionsOfAuthor.get(secondSessionId);
			assertEquals(authorId, sessionInfo.get("authorID"));
		} finally {
			// borra sesion
			// borra sesion2

			// borra sesion1
			mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deleteSession").withBody(
					"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&sessionID=s.f7179037cd78e2f99b492e4ad085628d"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

			// borra sesion2
			mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deleteSession").withBody(
					"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&sessionID=s.5c16699d34b57893cd76cdfda97d5d4a"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

			client.deleteSession(firstSessionId);
			client.deleteSession(secondSessionId);
		}
//
	}

//	@Test
//	public void create_pad_set_and_get_content() {
//		String padID = "integration-test-pad";
//
//		// crea pad
//		client.createPad(padID);
//		try {
//			// setea texto
//			client.setText(padID, "gå å gjør et ærend");
//			String text = (String) client.getText(padID).get("text");
//			assertEquals("gå å gjør et ærend\n", text);
//
//			// setea HTML
//			client.setHTML(padID, "<!DOCTYPE HTML><html><body><p>gå og gjøre et ærend igjen</p></body></html>");
//
//			// recupera HTML
//			String html = (String) client.getHTML(padID).get("html");
//			assertTrue(html, html.contains("g&#229; og gj&#248;re et &#230;rend igjen<br><br>"));
//
//			// recupera HTML
//			html = (String) client.getHTML(padID, 2).get("html");
//			assertEquals("<!DOCTYPE HTML><html><body><br></body></html>", html);
//			// recupera texto
//			text = (String) client.getText(padID, 2).get("text");
//			assertEquals("\n", text);
//
//			// recupera revision
//			long revisionCount = (long) client.getRevisionsCount(padID).get("revisions");
//			assertEquals(3L, revisionCount);
//
//			// getRevisionChangeset
//			String revisionChangeset = client.getRevisionChangeset(padID);
//			assertTrue(revisionChangeset, revisionChangeset.contains("gå og gjøre et ærend igjen"));
//			// getRevisionChangeset
//
//			revisionChangeset = client.getRevisionChangeset(padID, 2);
//			assertTrue(revisionChangeset, revisionChangeset.contains("|1-j|1+1$\n"));
//			// getDiffHTML
//
//			String diffHTML = (String) client.createDiffHTML(padID, 1, 2).get("html");
//			assertTrue(diffHTML,
//					diffHTML.contains("<span class=\"removed\">g&#229; &#229; gj&#248;r et &#230;rend</span>"));
//			//Append
//
//			client.appendText(padID, "lagt til nå");
//			text = (String) client.getText(padID).get("text");
//			assertEquals("gå og gjøre et ærend igjen\nlagt til nå\n", text);
//			//getAttributePool
//
//			Map attributePool = (Map) client.getAttributePool(padID).get("pool");
//			assertTrue(attributePool.containsKey("attribToNum"));
//			assertTrue(attributePool.containsKey("nextNum"));
//			assertTrue(attributePool.containsKey("numToAttrib"));
//			//SaveRevision
//			//SaveRevision2
//
//			client.saveRevision(padID);
//			client.saveRevision(padID, 2);
//
//			long savedRevisionCount = (long) client.getSavedRevisionsCount(padID).get("savedRevisions");
//			assertEquals(2L, savedRevisionCount);
//
//			List savedRevisions = (List) client.listSavedRevisions(padID).get("savedRevisions");
//			assertEquals(2, savedRevisions.size());
//			assertEquals(2L, savedRevisions.get(0));
//			assertEquals(4L, savedRevisions.get(1));
//
//			long padUsersCount = (long) client.padUsersCount(padID).get("padUsersCount");
//			assertEquals(0, padUsersCount);
//
//			List padUsers = (List) client.padUsers(padID).get("padUsers");
//			assertEquals(0, padUsers.size());
//
//			String readOnlyId = (String) client.getReadOnlyID(padID).get("readOnlyID");
//			String padIdFromROId = (String) client.getPadID(readOnlyId).get("padID");
//			assertEquals(padID, padIdFromROId);
//
//			List authorsOfPad = (List) client.listAuthorsOfPad(padID).get("authorIDs");
//			assertEquals(0, authorsOfPad.size());
//
//			long lastEditedTimeStamp = (long) client.getLastEdited(padID).get("lastEdited");
//			Calendar lastEdited = Calendar.getInstance();
//			lastEdited.setTimeInMillis(lastEditedTimeStamp);
//			Calendar now = Calendar.getInstance();
//			assertTrue(lastEdited.before(now));
//
//			client.sendClientsMessage(padID, "test message");
//		} finally {
//			client.deletePad(padID);
//		}
//	}

	@Test
	public void create_pad_move_and_copy() throws Exception {
		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createPad")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad&text=should+be+kept")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		String padID = "integration-test-pad";
		String copyPadId = "integration-test-pad-copy";
		String movePadId = "integration-move-pad-move";
		String keep = "should be kept";
		String change = "should be changed";
		this.client.createPad(padID, keep);

		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/copyPad").withBody(new StringBody(
						"sourceID=integration-test-pad&apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&force=false&destinationID=integration-test-pad-copy")))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\":\"integration-test-pad-copy\"}}"));

		this.client.copyPad(padID, copyPadId);
		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText").withBody(new StringBody(
				"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-copy")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"should be kept\\n\"}}"));

		String copyPadText = (String) this.client.getText(copyPadId).get("text");

		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/copyPad").withBody(new StringBody(
						"sourceID=integration-test-pad&apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&force=false&destinationID=integration-move-pad-move")))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\":\"integration-move-pad-move\"}}"));

		this.client.movePad(padID, movePadId);

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText").withBody(new StringBody(
				"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-move-pad-move")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"should be kept\\n\"}}"));

		String movePadText = (String) this.client.getText(movePadId).get("text");

		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/setText").withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-move-pad-move&text=should+be+changed")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		this.client.setText(movePadId, change);

		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/copyPad").withBody(new StringBody(
						"sourceID=integration-move-pad-move&apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&force=true&destinationID=integration-test-pad-copy")))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\":\"integration-test-pad-copy\"}}"));

		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/copyPad").withBody(new StringBody(
						"sourceID=integration-test-pad&apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&force=false&destinationID=integration-move-pad-move")))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\":\"integration-move-pad-move\"}}"));

		this.client.copyPad(movePadId, copyPadId, true);

		mockServer.clear(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText"));
		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText").withBody(new StringBody(
				"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-copy")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"should be changed\\n\"}}"));

		String copyPadTextForce = (String) this.client.getText(copyPadId).get("text");

		mockServer
				.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/movePad").withBody(new StringBody(
						"sourceID=integration-move-pad-move&apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&force=true&destinationID=integration-test-pad-copy")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		this.client.movePad(movePadId, copyPadId, true);

		mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getText").withBody(new StringBody(
				"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-copy")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"text\":\"should be changed\\n\"}}"));
		String movePadTextForce = (String) this.client.getText(copyPadId).get("text");

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deletePad")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-copy")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		this.client.deletePad(copyPadId);

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deletePad")
				.withBody(new StringBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad")))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		this.client.deletePad(padID);

		assertEquals(keep + "\n", copyPadText);
		assertEquals(keep + "\n", movePadText);

		assertEquals(change + "\n", copyPadTextForce);
		assertEquals(change + "\n", movePadTextForce);
	}

	// funciona
	@Test
	public void create_pads_and_list_them() throws InterruptedException {
		String pad1 = "integration-test-pad-1";
		String pad2 = "integration-test-pad-2";

		// crea pad 1
		// crea pad 2
		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createPad").withBody(
				"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-1"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createPad").withBody(
				"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-2"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		mockServer
				.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/listAllPads")
						.withBody("apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58"))
				.respond(HttpResponse.response().withStatusCode(200).withBody(
						"{\"code\":0,\"message\":\"ok\",\"data\":{\"padIDs\":[\"12\",\"IHhrjEWaFP\",\"integration-test-pad-1\",\"integration-test-pad-2\",\"pad\",\"s25ZpWCu9g\"]}}"));

		client.createPad(pad1);
		client.createPad(pad2);
		Thread.sleep(100);
		// lista todos os pads
		List padIDs = (List) client.listAllPads().get("padIDs");

		// borra pad1
		// borra pad2

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deletePad").withBody(
				"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-1"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		assertTrue(padIDs.contains(pad1));
		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deletePad").withBody(
				"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-2"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));
		client.deletePad(pad1);
		client.deletePad(pad2);

		assertTrue(String.format("Size was %d", padIDs.size()), padIDs.size() >= 2);

		assertTrue(padIDs.contains(pad2));
	}

	// funciona
	@Test
	public void create_pad_and_chat_about_it() {
		String padID = "integration-test-pad-1";
		String user1 = "user1";
		String user2 = "user2";

		// crear autor
		System.out.println("*****************************************LISTADO");
		mockServer.when(
				HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthorIfNotExistsFor").withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&name=integration-author-1&authorMapper=user1"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.RMtRd8qqLZzHP9r2\"}}"));

		Map response = client.createAuthorIfNotExistsFor(user1, "integration-author-1");

		mockServer.when(
				HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthorIfNotExistsFor").withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&name=integration-author-2&authorMapper=user1"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.SFGAvOhlcqgWgLaW\"}}"));

		String author1Id = (String) response.get("authorID");
		mockServer.when(
				HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createAuthorIfNotExistsFor").withBody(
						"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&name=integration-author-2&authorMapper=user2"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"authorID\":\"a.RMtRd8qqLZzHP9r2\"}}"));

		// crear autor 2
		response = client.createAuthorIfNotExistsFor(user2, "integration-author-2");
		String author2Id = (String) response.get("authorID");
		// crear PAD

		mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/createPad").withBody(
				"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-1"))
				.respond(HttpResponse.response().withStatusCode(200)
						.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		client.createPad(padID);
		try {
//			// concatena
//
			mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/appendChatMessage").withBody(
					"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-1&text=hi+from+user1&authorID=a.RMtRd8qqLZzHP9r2"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

			client.appendChatMessage(padID, "hi from user1", author1Id);

			mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/appendChatMessage").withBody(
					"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-1&text=hi+from+user2&time="
							+ (System.currentTimeMillis() / 1000L) + "&authorID=a.RMtRd8qqLZzHP9r2"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

			// concatena
			client.appendChatMessage(padID, "hi from user2", author2Id, System.currentTimeMillis() / 1000L);

			mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/appendChatMessage").withBody(
					"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-1&text=g%C3%A5+%C3%A5+gj%C3%B8r+et+%C3%A6rend&time="
							+ (System.currentTimeMillis() / 1000L) + "&authorID=a.RMtRd8qqLZzHP9r2"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

			// concatena caracteres
			client.appendChatMessage(padID, "gå å gjør et ærend", author1Id, System.currentTimeMillis() / 1000L);

			String caracteres_envio = "";
			try {
				caracteres_envio = new String("gå å gjør et ærend".getBytes(), "ISO-8859-1");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			// devolve o head
//
			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getChatHead").withBody(
					"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-1"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"chatHead\":2}}"));

			response = client.getChatHead(padID);
			long chatHead = (long) response.get("chatHead");
			assertEquals(2, chatHead);

//			// devolve o historial
			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getChatHistory").withBody(
					"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-1"))
					.respond(HttpResponse.response().withStatusCode(200).withBody(
							"{\"code\":0,\"message\":\"ok\",\"data\":{\"messages\":[{\"text\":\"hi from user1\",\"userId\":\"a.RMtRd8qqLZzHP9r2\",\"time\":1542564554236,\"userName\":\"integration-author-1\"},{\"text\":\"hi from user2\",\"userId\":\"a.SFGAvOhlcqgWgLaW\",\"time\":1542564554,\"userName\":\"integration-author-2\"},{\"text\":\""
									+ caracteres_envio
									+ "\",\"userId\":\"a.RMtRd8qqLZzHP9r2\",\"time\":1542564554,\"userName\":\"integration-author-1\"}]}}"));

			response = client.getChatHistory(padID);
			List chatHistory = (List) response.get("messages");
			assertEquals(3, chatHistory.size());
			// caracteres
			assertEquals("gå å gjør et ærend", ((Map) chatHistory.get(2)).get("text"));
			// devolve o historial
			mockServer.clear(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getChatHistory"));

			mockServer.when(HttpRequest.request().withMethod("GET").withPath("/api/1.2.13/getChatHistory").withBody(
					"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-1&start=0&end=1"))
					.respond(HttpResponse.response().withStatusCode(200).withBody(
							" {\"code\":0,\"message\":\"ok\",\"data\":{\"messages\":[{\"text\":\"hi from user1\",\"userId\":\"a.RMtRd8qqLZzHP9r2\",\"time\":1542564554236,\"userName\":\"integration-author-1\"},{\"text\":\"hi from user2\",\"userId\":\"a.SFGAvOhlcqgWgLaW\",\"time\":1542564554,\"userName\":\"integration-author-2\"}]}}"));

			response = client.getChatHistory(padID, 0, 1);
			chatHistory = (List) response.get("messages");
			assertEquals(2, chatHistory.size());
			assertEquals("hi from user2", ((Map) chatHistory.get(1)).get("text"));

			mockServer.when(HttpRequest.request().withMethod("POST").withPath("/api/1.2.13/deletePad").withBody(
					"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-1"))
					.respond(HttpResponse.response().withStatusCode(200)
							.withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}"));

		} finally {
			client.deletePad(padID);
		}

	}
}
