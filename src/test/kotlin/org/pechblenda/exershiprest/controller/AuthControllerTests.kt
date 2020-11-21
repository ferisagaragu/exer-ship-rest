package org.pechblenda.exershiprest.controller

import com.nhaarman.mockito_kotlin.any

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.runner.RunWith

import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito

import org.pechblenda.exershiprest.dao.IUserDAO
import org.pechblenda.exershiprest.entity.User
import org.pechblenda.exershiprest.mail.MailTemplate
import org.pechblenda.rest.Request
import org.pechblenda.storage.FirebaseStorage

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

import java.util.UUID
import org.pechblenda.exershiprest.dao.IStorageDAO
import org.pechblenda.exershiprest.entity.Storage

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner::class)
@TestPropertySource(locations = ["classpath:application-test.properties"])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthControllerTests {

	@Autowired
	private lateinit var mockMvc: MockMvc

	@Autowired
	private lateinit var userDAO: IUserDAO

	@Autowired
	private lateinit var storageDAO: IStorageDAO

	@Autowired
	private lateinit var encoder: PasswordEncoder

	@MockBean //se usa para que no se envíe el correo de verificación
	private lateinit var mailTemplate: MailTemplate

	@MockBean
	private lateinit var firebaseStorage: FirebaseStorage

	private var userMount: User? = null

	@BeforeAll
	fun beforeAll() {
		restoreUser()
	}

	@BeforeEach
	fun beforeEach() {
		restoreUser()
	}

	fun restoreUser() {
		if (userMount != null) {
			userMount = userDAO.findById(userMount!!.uid).orElse(null)
			userMount!!.enabled = false
			userMount!!.active = false
			userMount!!.activatePassword = UUID.randomUUID()
			userMount = userDAO.save(userMount!!)
		} else {
			val storage = storageDAO.save(
				Storage(
					uid = UUID.randomUUID(),
					directory = "",
					contentType = "",
					name = "",
					extension = "",
					url = ""
				)
			)

			userMount = userDAO.save(
				User(
					uid = UUID.randomUUID(),
					name = "fakeName",
					lastName = "",
					userName = "fakeUser",
					email = "no-real@fake.com",
					password = encoder.encode("fake"),
					enabled = false,
					active = false,
					activatePassword = UUID.randomUUID(),
			 		photo = storage,
					refreshToken = null,
					roles = mutableListOf()
				)
			)
		}
	}

	@Test
	fun `validate-token works`() {
		userMount!!.active = true
		userMount!!.enabled = true
		userDAO.save(userMount!!)

		val requestBody = Request()
		requestBody["userName"] = "fakeUser"
		requestBody["password"] = "fake"

		val responseSignIn = mockMvc.perform(
			post("/auth/sign-in")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody.toJSON())
		).andDo(print())
			.andExpect(status().isOk)
			.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().response

		val token = (Request()
			.toRequest(responseSignIn.contentAsString)
			.toMap("data")["session"] as Map<String, String>)["token"]

		val response = mockMvc.perform(
			get("/auth/validate-token")
			.header(HttpHeaders.AUTHORIZATION, "Bearer $token")
		).andDo(print())
			.andExpect(status().isOk)
			.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().response

		val request = Request().toRequest(response.contentAsString)

		assertEquals(request.toMap("data")["validateToken"], true)
	}

	@Test
	fun `validate-token bad token`() {
		val token = "eyJhbGciOiJIUzUxMiJ9." +
			"eyJzdWIiOiJmYWtlVXNlciIsImlhdCI6MTYwMjEzNjQ5MSwiZXhwIjoxNjAyMTU0NDkxfQ." +
			"JwOUiaY74DKwpmp3mpVFtEwD9zmn1yvKRLbXb0tPkL9FgmV82BwkCBcqYNeZgvwy6KLkigft0zDpxemf2rvcxf"

		val response = mockMvc.perform(
			get("/auth/validate-token")
			.header(HttpHeaders.AUTHORIZATION, "Bearer $token")
		).andDo(print())
			.andExpect(status().isUnauthorized)
	}

	@Test
	fun `validate-token no is enabled`() {
		userMount!!.active = true
		userMount!!.enabled = true
		userDAO.save(userMount!!)

		val requestBody = Request()
		requestBody["userName"] = "fakeUser"
		requestBody["password"] = "fake"

		val responseSignIn = mockMvc.perform(
			post("/auth/sign-in")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody.toJSON())
		).andDo(print())
			.andExpect(status().isOk)
			.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().response

		val token = (Request()
			.toRequest(responseSignIn.contentAsString)
			.toMap("data")["session"] as Map<String, String>)["token"]

		userMount!!.enabled = false
		userDAO.save(userMount!!)

		val response = mockMvc.perform(
			get("/auth/validate-token")
			.header(HttpHeaders.AUTHORIZATION, "Bearer $token")
		).andDo(print())
			.andExpect(status().isBadRequest)
			.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().response

		val request = Request().toRequest(response.contentAsString)

		assertEquals(
			request["message"],
			"Upps tu cuenta se encuentra bloqueada, " +
				"te enviamos a tu correo electrÃ³nico las razones"
		)
	}

	@Test
	fun `can-activate-account works`() {
		val response = mockMvc.perform(
			get("/auth/can-activate-account/${userMount!!.uid}")
		).andDo(print())
			.andExpect(status().isOk)
			.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().response

		val request = Request().toRequest(response.contentAsString)

		assertEquals(request.toMap("data")["canActivate"], true)
	}

	@Test
	fun `can-activate-account account is activate`() {
		userMount!!.active = true
		userDAO.save(userMount!!)

		val response = mockMvc.perform(
			get("/auth/can-activate-account/${userMount!!.uid}")
		).andDo(print())
			.andExpect(status().isBadRequest)
			.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().response

		val request = Request().toRequest(response.contentAsString)

		assertEquals(
			request["message"],
			"Upps tu cuenta ya esta activada, intenta iniciando sesiÃ³n de forma habitual"
		)
	}

	@Test
	fun `can-activate-account incorrect param`() {
		val response = mockMvc.perform(
			get("/auth/can-activate-account/8455017f-18dc-4c69-973a-5e4f53678f78")
		).andDo(print())
			.andExpect(status().isBadRequest)
			.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().response

			val request = Request().toRequest(response.contentAsString)

			assertEquals(
				request["message"],
				"Upps no se encuentra el usuario que quieres activar"
			)
	}

	@Test
	fun `can-change-password works`() {
		val response = mockMvc.perform(
			get("/auth/can-change-password/${userMount!!.activatePassword}")
		).andDo(print())
			.andExpect(status().isOk)
			.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().response

		val request = Request().toRequest(response.contentAsString)

		assertEquals(request.toMap("data").toMap()["canChangePassword"], true)
	}

	@Test
	fun `can-change-password param is not uid`() {
		mockMvc.perform(
			get("/auth/can-change-password/a")
		).andDo(print())
			.andExpect(status().isBadRequest)
	}

	@Test
	fun `can-change-password incorrect param`() {
		val response = mockMvc.perform(
			get("/auth/can-change-password/8455017f-18dc-4c69-973a-5e4f53678f78")
		).andDo(print())
			.andExpect(status().isBadRequest)
			.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().response

			val request = Request().toRequest(response.contentAsString)

			assertEquals(
				request["message"],
				"Upps el cÃ³digo de recuperaciÃ³n no es valido"
			)
	}

	@Test
	fun `activate-account works`() {
		val requestBody = Request()
		requestBody["uid"] = userMount!!.uid
		requestBody["password"] = "Fernny27"

		val response = mockMvc.perform(
			post("/auth/activate-account")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody.toJSON())
		).andDo(print())
			.andExpect(status().isOk)
			.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().response

		val request = Request().toRequest(response.contentAsString)

		assertEquals(request.toString("message"), "Tu cuenta a sido activada con Ã©xito")
	}

	@Test
	fun `activate-account null params`() {
		val requestBody = Request()
		requestBody["uid"] = null
		requestBody["password"] = ""

		val response = mockMvc.perform(
			post("/auth/activate-account")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody.toJSON())
		).andDo(print())
			.andExpect(status().isBadRequest)
			.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().response

		val request = Request().toRequest(response.contentAsString)

		assertEquals(request.toString("message"), "Upps la contraseÃ±a es requerida")
	}

	@Test
	fun `activate-account incorrect params`() {
		val requestBody = Request()
		requestBody["uid"] = UUID.randomUUID()
		requestBody["password"] = "fer"

		val response = mockMvc.perform(
			post("/auth/activate-account")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody.toJSON())
		).andDo(print())
			.andExpect(status().isBadRequest)
			.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().response

		val request = Request().toRequest(response.contentAsString)

		assertEquals(request.toString("message"), "Upps no se encuentra el usuario")
	}

	@Test
	fun `change-password works`() {
		val requestBody = Request()
		requestBody["activatePassword"] = userMount!!.activatePassword
		requestBody["password"] = "Fernny27"

		val response = mockMvc.perform(
			post("/auth/change-password")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody.toJSON())
		).andDo(print())
			.andExpect(status().isOk)
			.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().response

		val request = Request().toRequest(response.contentAsString)

		assertEquals(request.toString("message"), "Has cambiado tu contraseÃ±a con Ã©xito")
	}

	@Test
	fun `change-password null params`() {
		val requestBody = Request()
		requestBody["activatePassword"] = null
		requestBody["password"] = ""

		val response = mockMvc.perform(
			post("/auth/change-password")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody.toJSON())
		).andDo(print())
			.andExpect(status().isBadRequest)
			.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().response

		val request = Request().toRequest(response.contentAsString)

		assertEquals(request.toString("message"), "Upps la contraseÃ±a es requerida")
	}

	@Test
	fun `change-password incorrect params`() {
		val requestBody = Request()
		requestBody["activatePassword"] = UUID.randomUUID()
		requestBody["password"] = "fernnypay95"

		val response = mockMvc.perform(
			post("/auth/change-password")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody.toJSON())
		).andDo(print())
			.andExpect(status().isBadRequest)
			.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().response

		val request = Request().toRequest(response.contentAsString)

		assertEquals(
			request.toString("message"),
			"Upps no se encuentra ningÃºn registro que coincida con el correo electrÃ³nico"
		)
	}

	@Test
	fun `recover-password works`() {
		val requestBody = Request()
		requestBody["email"] = userMount!!.email

		val response = mockMvc.perform(
			post("/auth/recover-password")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody.toJSON())
		).andDo(print())
			.andExpect(status().isOk)
			.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().response

		val request = Request().toRequest(response.contentAsString)

		assertEquals(
			request["message"],
			"Hemos enviado envido un correo electrÃ³nico a no-real@fake.com con " +
				"las instrucciones para recuperar tu contraseÃ±a"
		)
	}

	@Test
	fun `recover-password null params`() {
		val requestBody = Request()
		requestBody["email"] = ""

		val response = mockMvc.perform(
			post("/auth/recover-password")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody.toJSON())
		).andDo(print())
			.andExpect(status().isBadRequest)
			.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().response

		val request = Request().toRequest(response.contentAsString)

		assertEquals(
			request["message"],
			"Upps no se encuentra ningÃºn registro que coincida con el correo electrÃ³nico"
		)
	}

	@Test
	fun `recover-password incorrect params`() {
		val requestBody = Request()
		requestBody["email"] = "realnoFake@fake.com"

		val response = mockMvc.perform(
			post("/auth/recover-password")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody.toJSON())
		).andDo(print())
			.andExpect(status().isBadRequest)
			.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().response

		val request = Request().toRequest(response.contentAsString)

		assertEquals(
			request["message"],
			"Upps no se encuentra ningÃºn registro que coincida con el correo electrÃ³nico"
		)
	}

	@Test
	fun `sign-up works`() {
		Mockito.doReturn("url-image")
			.`when`(firebaseStorage).put(
				anyString(),
				anyString(),
				anyString(),
				anyString(),
				any<ByteArray>()
			)

		val requestBody = Request()
		requestBody["name"] = "Take2"
		requestBody["userName"] = "userfakeName"
		requestBody["email"] = "ultraFake@false.com"

		val response = mockMvc.perform(
			post("/auth/sign-up")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody.toJSON())
		).andDo(print())
			.andExpect(status().isCreated)
			.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().response

		val request = Request().toRequest(response.contentAsString)

		assertEquals(
			request["message"],
			"Tu cuenta a sido creada con Ã©xito, te enviamos un correo " +
			"electrÃ³nico con instrucciones de como activarla"
		)
	}

	@Test
	fun `sign-up repeat userName`() {
		val requestBody = Request()
		requestBody["name"] = "Take2"
		requestBody["userName"] = "fakeUser"
		requestBody["email"] = "no-real@fakes.com"

		val response = mockMvc.perform(
			post("/auth/sign-up")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody.toJSON())
		).andDo(print())
			.andExpect(status().isBadRequest)
			.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().response

		val request = Request().toRequest(response.contentAsString)

		assertEquals(
			request["message"],
			"El nombre de usuario ya esta registrado"
		)
	}

	@Test
	fun `sign-up repeat email`() {
		val requestBody = Request()
		requestBody["name"] = "Take2"
		requestBody["userName"] = "fakeUser1"
		requestBody["email"] = "no-real@fake.com"

		val response = mockMvc.perform(
			post("/auth/sign-up")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody.toJSON())
		).andDo(print())
			.andExpect(status().isBadRequest)
			.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().response

		val request = Request().toRequest(response.contentAsString)

		assertEquals(
			request["message"],
			"El correo electrÃ³nico ya esta registrado registrado"
		)
	}

	@Test
	fun `sign-in works`() {
		userMount!!.active = true
		userMount!!.enabled = true
		userDAO.save(userMount!!)

		val requestBody = Request()
		requestBody["userName"] = "fakeUser"
		requestBody["password"] = "fake"

		val response = mockMvc.perform(
			post("/auth/sign-in")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody.toJSON())
		).andDo(print())
			.andExpect(status().isOk)
			.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().response

		val request = Request().toRequest(response.contentAsString)

		assertEquals(
			request.toMap("data")["name"],
			"fakeName"
		)
	}

	@Test
	fun `sign-in bad user`() {
		userMount!!.active = true
		userMount!!.enabled = true
		userDAO.save(userMount!!)

		val requestBody = Request()
		requestBody["userName"] = "fakeUser334"
		requestBody["password"] = "fake"

		val response = mockMvc.perform(
			post("/auth/sign-in")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody.toJSON())
		).andDo(print())
			.andExpect(status().isBadRequest)
			.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().response

		val request = Request().toRequest(response.contentAsString)

		assertEquals(
			request["message"],
			"Upps no se encuentra el usuario"
		)
	}

	@Test
	fun `sign-in bad password`() {
		userMount!!.active = true
		userMount!!.enabled = true
		userDAO.save(userMount!!)

		val requestBody = Request()
		requestBody["userName"] = "fakeUser"
		requestBody["password"] = "fake123"

		val response = mockMvc.perform(
			post("/auth/sign-in")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody.toJSON())
		).andDo(print())
			.andExpect(status().isUnauthorized)
			.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().response

		val request = Request().toRequest(response.contentAsString)

		assertEquals(
			request["message"],
			"Upps la contraseÃ±a es incorrecta"
		)
	}

	@Test
	fun `refresh-token works`() {
		userMount!!.active = true
		userMount!!.enabled = true
		userMount!!.password = encoder.encode("fake")
		userDAO.save(userMount!!)

		val requestBody = Request()
		requestBody["userName"] = "fakeUser"
		requestBody["password"] = "fake"

		val response = mockMvc.perform(
			post("/auth/sign-in")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody.toJSON())
		).andDo(print())
			.andExpect(status().isOk)
			.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().response

		val refreshToken = (Request().toRequest(response.contentAsString)
			.toMap("data")["session"] as Map<String, Any>)["refreshToken"]

		userMount!!.refreshToken = refreshToken as String
		userDAO.save(userMount!!)

		val requestBodyRefresh = Request()
		requestBodyRefresh["refreshToken"] = userMount!!.refreshToken

		mockMvc.perform(
			post("/auth/refresh-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBodyRefresh.toJSON())
		).andDo(print())
			.andExpect(status().isOk)
			.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().response
	}

	@Test
	fun `refresh-token bad token`() {
		userMount!!.active = true
		userMount!!.enabled = true
		userMount!!.password = encoder.encode("fake")
		userDAO.save(userMount!!)

		val requestBody = Request()
		requestBody["refreshToken"] =
			"eyJhbGciOiJIUzUxMiJ9." +
			"eyJzdWIiOiJmYWtlVXNlciIsImlhdCI6MTYwMTk5OTg1OCwiZXhwIjoxNjAyMDE3ODU4fQ." +
			"mqRnjroGbSt0kItfQs-uHxtapPWsXyraWq_kgNIIZDyuXr6jdf2pPxSfd29O8o8gjROTdL" +
			"cfOSTLYcrNuZ70Nt"

		val response = mockMvc.perform(
			post("/auth/refresh-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody.toJSON())
		).andDo(print())
			.andExpect(status().isUnauthorized)
			.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().response

		val request = Request().toRequest(response.contentAsString)

		assertEquals(
			request["message"],
			"refresh token has expired"
		)
	}

}
