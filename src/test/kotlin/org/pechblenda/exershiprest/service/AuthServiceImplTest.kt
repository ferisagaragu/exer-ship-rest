package org.pechblenda.exershiprest.service

import org.junit.Rule
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith

import org.pechblenda.exception.BadRequestException
import org.pechblenda.exershiprest.dao.IUserDAO
import org.pechblenda.exershiprest.entity.User
import org.pechblenda.exershiprest.service.`interface`.IAuthService
import org.pechblenda.rest.Request

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
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
	private lateinit var encoder: PasswordEncoder

	@Rule
	private var exceptionRule = ExpectedException.none()

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
					photo = "",
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

}