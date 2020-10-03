package org.pechblenda.exershiprest.controller

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.runner.RunWith
import org.pechblenda.exershiprest.dao.IUserDAO
import org.pechblenda.exershiprest.entity.User
import org.pechblenda.exershiprest.mail.MailTemplate

import org.pechblenda.rest.Request

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc

import kotlin.collections.LinkedHashMap
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner::class)
@TestPropertySource(locations = [ "classpath:application-test.properties" ])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthControllerTests {

	@Autowired
	private lateinit var mockMvc: MockMvc

	@Autowired
	private lateinit var userDAO: IUserDAO

	@Autowired
	private lateinit var encoder: PasswordEncoder

	@MockBean
	private lateinit var mailTemplate: MailTemplate //se usa para que no se envíe el correo de verificación

	private lateinit var userMount: User

	@BeforeAll
	fun beforeAll() {
		userMount = userDAO.save(
			User(
				uid = UUID.randomUUID(),
				name = "Fernando",
				lastName = "",
				userName = "fernnypay95",
	      email = "ferisagaragu@gmail.com",
		    password = encoder.encode("Fernny27"),
		    enabled = false,
        active = false,
				activatePassword = UUID.randomUUID(),
        photo = "",
        roles = mutableListOf()
		))
	}

	@Test
	fun `can-activate-account works`() {
		val response = mockMvc.perform(
			get("/auth/can-activate-account/${userMount.uid}")
		).andDo(print())
			.andExpect(status().isOk)
			.andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn().response

		val request = Request().toRequest(response.contentAsString)

		assertEquals(request.toMap("data")["canActivate"], true)
	}

	@Test
	fun `can-activate-account param null is 400`() {
		mockMvc.perform(
			get("/auth/can-activate-account/a")
		).andDo(print())
			.andExpect(status().isBadRequest)
	}

}