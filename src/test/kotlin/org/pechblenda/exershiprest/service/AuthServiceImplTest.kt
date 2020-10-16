package org.pechblenda.exershiprest.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.runner.RunWith
import org.pechblenda.exershiprest.dao.IUserDAO
import org.pechblenda.exershiprest.entity.User

import org.pechblenda.exershiprest.service.`interface`.IAuthService

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Transactional

import java.util.UUID

@SpringBootTest
@Transactional
@AutoConfigureTestEntityManager
@RunWith(SpringRunner::class)
@TestPropertySource(locations = [ "classpath:application-test.properties" ])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@WithMockUser(username = "admin", authorities = ["ADMIN", "USER"])
class AuthServiceImplTest {

	@Autowired
	lateinit var authService: IAuthService

	@Autowired
	private lateinit var userDAO: IUserDAO

	private var userMount: User? = null

	@BeforeAll
	fun beforeAll() {
		userMount = userDAO.save(
			User(
				uid = UUID.randomUUID(),
				name = "admin",
				lastName = "",
				userName = "admin",
				email = "no-real@fake.com",
				password = "fake",
				enabled = true,
				active = true,
				activatePassword = UUID.randomUUID(),
				photo = "",
				refreshToken = null,
				roles = mutableListOf()
			)
		)
	}

	//sign-up
	@Test
	fun `test validate if token is valid`() {
		println(authService.validateToken().body)
		assertEquals(
			((authService.validateToken().body as Map<String, Any>)["data"]
				as Map<String, Any>)["validateToken"],
			true
		)
	}

}