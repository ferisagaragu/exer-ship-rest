package org.pechblenda.exershiprest.service

import com.nhaarman.mockito_kotlin.any

import org.mockito.ArgumentMatchers
import org.mockito.Mockito

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.runner.RunWith

import org.pechblenda.exershiprest.dao.INotificationDAO
import org.pechblenda.exershiprest.dao.IStorageDAO
import org.pechblenda.exershiprest.dao.IUserDAO
import org.pechblenda.exershiprest.entity.Notification
import org.pechblenda.exershiprest.entity.Storage
import org.pechblenda.exershiprest.entity.User
import org.pechblenda.exershiprest.service.`interface`.INotificationService
import org.pechblenda.database.FirebaseDatabase

import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.annotation.Transactional

import java.util.UUID
import java.util.Date
import org.junit.jupiter.api.Assertions
import org.pechblenda.exception.BadRequestException
import org.pechblenda.exershiprest.enum.NotificationType

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner::class)
@TestPropertySource(locations = ["classpath:application-test.properties"])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@WithMockUser(username = "notificationUser", authorities = ["ADMIN", "USER"])
class NotificationServiceImplTest {

	@Autowired
	private lateinit var notificationService: INotificationService

	@Autowired
	private lateinit var notificationsDAO: INotificationDAO

	@Autowired
	private lateinit var userDAO: IUserDAO

	@Autowired
	private lateinit var storageDAO: IStorageDAO

	@Autowired
	private lateinit var encoder: PasswordEncoder

	@MockBean
	private lateinit var firebaseDatabase: FirebaseDatabase

	private var userMount: User? = null

	@BeforeAll
	fun beforeAll() {
		restoreUser()
	}

	fun restoreUser() {
		if (userMount == null) {
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
					name = "fakeNameNotify",
					lastName = "",
					userName = "notificationUser",
					email = "no-realNotify@fake.com",
					password = encoder.encode("fake"),
					enabled = true,
					active = true,
					activatePassword = UUID.randomUUID(),
					photo = storage,
					refreshToken = null,
					roles = mutableListOf()
				)
			)

			notificationsDAO.save(
				Notification(
					uid = UUID.randomUUID(),
					title = "Fake title",
					message = "Fake name",
					type = "info",
					see = false,
					createDate = Date(),
					go = null,
					user = userMount!!
				)
			)
		}
	}

	@Test
	fun `test find all notifications`() {
		val resp = notificationService.findAllNotifications()
		assertEquals(resp.statusCodeValue, 200)
	}

	@Test
	fun `test find all notifications contains data`() {
		val resp = (
			notificationService.findAllNotifications().body
				as Map<String, Any>
		)["data"] as List<Any>

		assertEquals(resp.size, 1)
	}

	@Test
	fun `test set notification see`() {
		val notification = notificationsDAO.save(
			Notification(
				uid = UUID.randomUUID(),
				title = "Fake title see",
				message = "Fake name",
				type = "info",
				see = false,
				createDate = Date(),
				go = null,
				user = userMount!!
			)
		)

		Mockito.doNothing()
			.`when`(firebaseDatabase).delete(
				ArgumentMatchers.anyString()
			)

		val resp = (
			(
				notificationService.setNotificationSee(notification.uid).body
					as Map<String, Any>
			)["data"] as Map<String, Any>
		)["see"]

		assertEquals(resp, true)
	}

	@Test
	fun `test set notification see is mark to see`() {
		val notification = notificationsDAO.save(
			Notification(
				uid = UUID.randomUUID(),
				title = "Fake title see",
				message = "Fake name",
				type = "info",
				see = true,
				createDate = Date(),
				go = null,
				user = userMount!!
			)
		)

		Mockito.doNothing()
			.`when`(firebaseDatabase).delete(
				ArgumentMatchers.anyString()
			)

		val message = Assertions.assertThrows(BadRequestException::class.java) {
			notificationService.setNotificationSee(notification.uid)
		}.message

		assertEquals(message, "400 BAD_REQUEST \"Upps la notificación ya fue marcada como vista\"")
	}

	@Test
	fun `test set notification see is not found`() {
		Mockito.doNothing()
			.`when`(firebaseDatabase).delete(
				ArgumentMatchers.anyString()
			)

		val message = Assertions.assertThrows(BadRequestException::class.java) {
			notificationService.setNotificationSee(UUID.randomUUID())
		}.message

		assertEquals(message, "400 BAD_REQUEST \"Upps no se encuentra la notificación\"")
	}

	@Test
	fun `notify is send`() {
		Mockito.doNothing()
			.`when`(firebaseDatabase).put(
				ArgumentMatchers.anyString(),
				any()
			)

		notificationService.notify(
			userMount!!.uid,
			"title1234#",
			"message",
			"/fake",
			NotificationType.INFO
		)

		val resp = notificationsDAO.findAll().find {
			item -> item.title == "title1234#"
		}

		assertEquals(resp?.title, "title1234#")
	}

	@Test
	fun `notify user not fount`() {
		Mockito.doNothing()
			.`when`(firebaseDatabase).put(
				ArgumentMatchers.anyString(),
				any()
			)

		notificationService.notify(
			UUID.randomUUID(),
			"title1234#",
			"message",
			"/fake",
			NotificationType.INFO
		)

		val resp = notificationsDAO.findAll().find {
			item -> item.title == "title1234#"
		}

		assertEquals(resp?.title, null)
	}

}