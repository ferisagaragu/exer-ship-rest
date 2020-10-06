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
					photo = "",
					refreshToken = "",
					roles = mutableListOf()
				)
			)
		}
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

}
