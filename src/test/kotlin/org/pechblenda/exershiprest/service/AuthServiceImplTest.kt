package org.pechblenda.exershiprest.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.runner.RunWith

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

	private var userMount: User? = null

	@BeforeAll
	fun beforeAll() {
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

}