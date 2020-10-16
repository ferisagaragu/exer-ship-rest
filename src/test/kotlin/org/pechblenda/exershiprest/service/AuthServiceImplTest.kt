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

}