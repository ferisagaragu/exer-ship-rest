package org.pechblenda.exershiprest.service

import com.nhaarman.mockito_kotlin.any

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.runner.RunWith

import org.mockito.ArgumentMatchers
import org.mockito.Mockito

import org.pechblenda.database.FirebaseDatabase
import org.pechblenda.exception.BadRequestException
import org.pechblenda.exershiprest.dao.IUserDAO
import org.pechblenda.exershiprest.entity.User
import org.pechblenda.exershiprest.mail.MailTemplate
import org.pechblenda.exershiprest.service.`interface`.IAuthService
import org.pechblenda.rest.Request
import org.pechblenda.storage.FirebaseStorage
import org.pechblenda.exershiprest.dao.IStorageDAO
import org.pechblenda.exershiprest.entity.Storage
import org.pechblenda.exception.UnauthenticatedException

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Transactional

import java.util.UUID

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner::class)
@TestPropertySource(locations = ["classpath:application-test.properties"])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@WithMockUser(username = "fakeUserImpl", authorities = ["ADMIN", "USER"])
class AuthServiceImplTest {

	@Autowired
	lateinit var authService: IAuthService

	@Autowired
	private lateinit var userDAO: IUserDAO

	@Autowired
	private lateinit var storageDAO: IStorageDAO

	@Autowired
	private lateinit var encoder: PasswordEncoder

	@MockBean
	private lateinit var mailTemplate: MailTemplate

	@MockBean
	private lateinit var firebaseStorage: FirebaseStorage

	@MockBean
	private lateinit var firebaseDatabase: FirebaseDatabase

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
			userMount!!.enabled = true
			userMount!!.active = true
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
					name = "fakeUserNameImpl",
					lastName = "",
					userName = "fakeUserImpl",
					email = "no-realImpl@fake.com",
					password = encoder.encode("fake"),
					enabled = true,
					active = true,
					activatePassword = UUID.randomUUID(),
					photo = storage,
					refreshToken = null,
					roles = mutableListOf()
				)
			)
		}
	}

	@Test
	fun `test validate if token is valid`() {
		assertEquals(
			((authService.validateToken().body as Map<String, Any>)["data"]
				as Map<String, Any>)["validateToken"],
			true
		)
	}

	@Test
	fun `test validate user not fount`() {
		userMount!!.userName = "${userMount!!.userName}123"

		val message = Assertions.assertThrows(BadRequestException::class.java) {
			((authService.validateToken().body as Map<String, Any>)["data"]
				as Map<String, Any>)["validateToken"]
		}.message

		assertEquals(message, "400 BAD_REQUEST \"Upps no se encuentra el usuario\"")
	}

	@Test
	fun `test validate account not activate`() {
		userMount!!.active = false

		val message = Assertions.assertThrows(BadRequestException::class.java) {
			((authService.validateToken().body as Map<String, Any>)["data"]
				as Map<String, Any>)["validateToken"]
		}.message

		assertEquals(
			message,
			"400 BAD_REQUEST \"Upps tu cuenta aun no esta activada, " +
			"revisa tu correo electrónico para saber como activarla\""
		)
	}

	@Test
	fun `test validate account blocked`() {
		userMount!!.enabled = false

		val message = Assertions.assertThrows(BadRequestException::class.java) {
			((authService.validateToken().body as Map<String, Any>)["data"]
				as Map<String, Any>)["validateToken"]
		}.message

		assertEquals(
			message,
			"400 BAD_REQUEST \"Upps tu cuenta se encuentra bloqueada, " +
			"te enviamos a tu correo electrónico las razones\""
		)
	}

	@Test
	fun `test validate can activate`() {
		userMount!!.active = false
		var body = authService.canActivate(userMount!!.uid).body as Map<String, Any>
		body = (body["data"] as Map<String, Any>)
		assertEquals(body["canActivate"], true)
	}

	@Test
	fun `test validate account is active`() {
		val message = Assertions.assertThrows(BadRequestException::class.java) {
			authService.canActivate(userMount!!.uid)
		}.message

		assertEquals(
			message,
			"400 BAD_REQUEST \"Upps tu cuenta ya esta activada, " +
			"intenta iniciando sesión de forma habitual\""
		)
	}

	@Test
	fun `test validate can activate not fount user`() {
		val message = Assertions.assertThrows(BadRequestException::class.java) {
			authService.canActivate(UUID.randomUUID())
		}.message

		assertEquals(
			message,
			"400 BAD_REQUEST \"Upps no se encuentra el usuario que quieres activar\""
		)
	}

	@Test
	fun `test validate can change password`() {
		userMount!!.activatePassword = UUID.randomUUID()
		var body = authService.canChangePassword(
			userMount!!.activatePassword!!
		).body as Map<String, Any>
		body = (body["data"] as Map<String, Any>)

		assertEquals(body["canChangePassword"], true)
	}

	@Test
	fun `test validate can change password code not fount`() {
		val message = Assertions.assertThrows(BadRequestException::class.java) {
			authService.canChangePassword(UUID.randomUUID())
		}.message

		assertEquals(
			message,
			"400 BAD_REQUEST \"Upps el código de recuperación no es valido\""
		)
	}

	@Test
	fun `test activate account`() {
		userMount!!.active = false

		val request = Request()
		request["uid"] = userMount!!.uid
		request["password"] = "fakeUserPassword"

		var message = (
			authService.activateAccount(
				request
			).body as Map<String, Any>
		)["message"]

		assertEquals(message, "Tu cuenta a sido activada con éxito")
	}

	@Test
	fun `test activate account password not fount`() {
		val message = Assertions.assertThrows(BadRequestException::class.java) {
			val request = Request()
			request["password"] = ""
			authService.activateAccount(request)
		}.message

		assertEquals(
			message,
			"400 BAD_REQUEST \"Upps la contraseña es requerida\""
		)
	}

	@Test
	fun `test activate account password user not fount`() {
		val message = Assertions.assertThrows(BadRequestException::class.java) {
			val request = Request()
			request["password"] = "asd"
			request["uid"] = UUID.randomUUID()
			authService.activateAccount(request)
		}.message

		assertEquals(
			message,
			"400 BAD_REQUEST \"Upps no se encuentra el usuario\""
		)
	}

	@Test
	fun `test activate account is activated`() {
		userMount!!.active = true

		val message = Assertions.assertThrows(BadRequestException::class.java) {
			val request = Request()
			request["password"] = "asd"
			request["uid"] = userMount!!.uid
			authService.activateAccount(request)
		}.message

		assertEquals(
			message,
			"400 BAD_REQUEST \"Upps tu cuenta ya esta activada\""
		)
	}

	@Test
	fun `test change password`() {
		userMount!!.activatePassword = UUID.randomUUID()

		val request = Request()
		request["activatePassword"] = userMount!!.activatePassword
		request["password"] = "fakeUserPassword"

		var message = (
			authService.changePassword(
				request
			).body as Map<String, Any>
		)["message"]

		assertEquals(message, "Has cambiado tu contraseña con éxito")
	}

	@Test
	fun `test change password is void password`() {
		userMount!!.activatePassword = UUID.randomUUID()

		val message = Assertions.assertThrows(BadRequestException::class.java) {
			val request = Request()
			request["activatePassword"] = userMount!!.activatePassword
			request["password"] = ""
			authService.changePassword(request)
		}.message

		assertEquals(message, "400 BAD_REQUEST \"Upps la contraseña es requerida\"")
	}

	@Test
	fun `test change password not activate password`() {
		userMount!!.activatePassword = UUID.randomUUID()

		val message = Assertions.assertThrows(BadRequestException::class.java) {
			val request = Request()
			request["activatePassword"] = UUID.randomUUID()
			request["password"] = "fake"
			authService.changePassword(request)
		}.message

		assertEquals(
			message,
			"400 BAD_REQUEST \"Upps no se encuentra ningún " +
			"registro que coincida con el correo electrónico\""
		)
	}

	@Test
	fun `test recover password`() {
		val request = Request()
		request["email"] = "no-realImpl@fake.com"

		var message = (
			authService.recoverPassword(
				request
			).body as Map<String, Any>
		)["message"]

		assertEquals(
			message,
			"Hemos enviado envido un correo " +
			"electrónico a no-realImpl@fake.com con " +
			"las instrucciones para recuperar tu contraseña"
		)
	}

	@Test
	fun `test recover password email not fount`() {
		val message = Assertions.assertThrows(BadRequestException::class.java) {
			val request = Request()
			request["email"] = "ferisagaragu@gmail.com"
			authService.recoverPassword(request)
		}.message

		assertEquals(
			message,
			"400 BAD_REQUEST \"Upps no se encuentra ningún " +
			"registro que coincida con el correo electrónico\""
		)
	}

	@Test
	fun `test sign up`() {
		Mockito.doReturn("url-image")
			.`when`(firebaseStorage).put(
				ArgumentMatchers.anyString(),
				ArgumentMatchers.anyString(),
				ArgumentMatchers.anyString(),
				ArgumentMatchers.anyString(),
				any<ByteArray>()
			)

		val request = Request()
		request["name"] = "userFake123"
		request["userName"] = "userFake123"
		request["email"] = "userFake123@fake.com"

		var message = (
			authService.signUp(
				request
			).body as Map<String, Any>
		)["message"]

		assertEquals(
			message,
			"Tu cuenta a sido creada con éxito, " +
			"te enviamos un correo electrónico con " +
			"instrucciones de como activarla"
		)
	}

	@Test
	fun `test sign up name not empty`() {
		Mockito.doReturn("url-image")
			.`when`(firebaseStorage).put(
				ArgumentMatchers.anyString(),
				ArgumentMatchers.anyString(),
				ArgumentMatchers.anyString(),
				any<ByteArray>()
			)

		val message = Assertions.assertThrows(BadRequestException::class.java) {
			val request = Request()
			request["name"] = ""
			request["userName"] = "userFake123"
			request["email"] = "userFake123@fake.com"
			authService.signUp(request)
		}.message

		assertEquals(
			message,
			"400 BAD_REQUEST \"Upps el nombre es requerido\""
		)
	}

	@Test
	fun `test sign up user name not empty`() {
		Mockito.doReturn("url-image")
			.`when`(firebaseStorage).put(
				ArgumentMatchers.anyString(),
				ArgumentMatchers.anyString(),
				ArgumentMatchers.anyString(),
				any<ByteArray>()
			)

		val message = Assertions.assertThrows(BadRequestException::class.java) {
			val request = Request()
			request["name"] = "userFake123Name"
			request["userName"] = ""
			request["email"] = "userFake123@fake.com"
			authService.signUp(request)
		}.message

		assertEquals(
			message,
			"400 BAD_REQUEST \"Upps el nombre de usuario es requerido\""
		)
	}

	@Test
	fun `test sign up email not empty`() {
		Mockito.doReturn("url-image")
			.`when`(firebaseStorage).put(
				ArgumentMatchers.anyString(),
				ArgumentMatchers.anyString(),
				ArgumentMatchers.anyString(),
				any<ByteArray>()
			)

		val message = Assertions.assertThrows(BadRequestException::class.java) {
			val request = Request()
			request["name"] = "userFake123Name"
			request["userName"] = "userFake123"
			request["email"] = ""
			authService.signUp(request)
		}.message

		assertEquals(
			message,
			"400 BAD_REQUEST \"Upps el correo electrónico es requerido\""
		)
	}

	@Test
	fun `test sign in`() {
		val request = Request()
		request["userName"] = "no-realImpl@fake.com"
		request["password"] = "fake"

		var email = ((
			authService.signIn(request).body as Map<String, Any>
		)["data"] as Map<String, Any>)["email"]

		assertEquals(email, "no-realImpl@fake.com")
	}

	@Test
	fun `test sign in user not found`() {
		val message = Assertions.assertThrows(BadRequestException::class.java) {
			val request = Request()
			request["userName"] = "no-realImpleee@fake.com"
			request["password"] = "fake"

			authService.signIn(request)
		}.message

		assertEquals(message, "400 BAD_REQUEST \"Upps no se encuentra el usuario\"")
	}

	@Test
	fun `test sign in incorrect password`() {
		val message = Assertions.assertThrows(UnauthenticatedException::class.java) {
			val request = Request()
			request["userName"] = "no-realImpl@fake.com"
			request["password"] = "fake123"

			authService.signIn(request)
		}.message

		assertEquals(message, "401 UNAUTHORIZED \"Upps la contraseña es incorrecta\"")
	}

	@Test
	fun `test sign in account not activate`() {
		userMount!!.active = false

		val message = Assertions.assertThrows(BadRequestException::class.java) {
			val request = Request()
			request["userName"] = "no-realImpl@fake.com"
			request["password"] = "fake"
			authService.signIn(request)
		}.message

		assertEquals(
			message,
			"400 BAD_REQUEST \"Upps tu cuenta aun no esta activada, " +
			"revisa tu correo electrónico para saber como activarla\""
		)
	}

	@Test
	fun `test sign in account blocked`() {
		userMount!!.enabled = false

		val message = Assertions.assertThrows(BadRequestException::class.java) {
			val request = Request()
			request["userName"] = "no-realImpl@fake.com"
			request["password"] = "fake"
			authService.signIn(request)
		}.message

		assertEquals(
			message,
			"400 BAD_REQUEST \"Upps tu cuenta se encuentra bloqueada, " +
			"te enviamos a tu correo electrónico las razones\""
		)
	}

	@Test
	fun `test refresh token`() {
		val requestSignIn = Request()
		val requestRefreshToken = Request()
		requestSignIn["userName"] = "no-realImpl@fake.com"
		requestSignIn["password"] = "fake"

		var refreshToken = (
			(
				(
					authService.signIn(requestSignIn).body as Map<String, Any>
				)["data"] as Map<String, Any>
			)["session"] as Map<String, Any>
		)["refreshToken"]

		requestRefreshToken["refreshToken"] = refreshToken

		val refreshStatus = authService.refreshToken(requestRefreshToken).statusCodeValue

		assertEquals(refreshStatus, 200)
	}

	@Test
	fun `test refresh token not params emply`() {
		val response = Assertions.assertThrows(BadRequestException::class.java) {
			val requestRefreshToken = Request()
			requestRefreshToken["refreshToken"] = ""
			authService.refreshToken(requestRefreshToken)
		}.message

		assertEquals(response, "400 BAD_REQUEST \"Upps refreshToken es requerido\"")
	}

	@Test
	fun `test refresh token not params in request`() {
		val response = Assertions.assertThrows(BadRequestException::class.java) {
			val requestRefreshToken = Request()
			authService.refreshToken(requestRefreshToken)
		}.message

		assertEquals(response, "400 BAD_REQUEST \"Upps refreshToken es requerido\"")
	}

}