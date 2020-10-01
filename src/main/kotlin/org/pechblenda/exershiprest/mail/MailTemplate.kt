package org.pechblenda.exershiprest.mail

import org.pechblenda.mail.GoogleMail
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.stream.Collectors

@Component
class MailTemplate(
	private val googleMail: GoogleMail,

	@Value("\${front.base-url}")
	private val frontBaseUrl: String
) {

	fun sendActivateAccount(
		userName: String,
		activateCode: String,
		email: String
	) {
		val resource = ClassPathResource("templates/mail/activate-account.html")
		val text: String = BufferedReader(InputStreamReader(resource.inputStream, StandardCharsets.UTF_8))
			.lines()
			.collect(Collectors.joining("\n"))
			.replace("\${userName}", userName)
			.replace("\${urlActivate}", "${frontBaseUrl}/authentication/activate/${activateCode}")

		googleMail.send("Verificación de cuenta Exership", text, email)
	}

	fun sendRecoverPassword(
		userName: String,
		activateCode: String,
		email: String
	) {
		val resource = ClassPathResource("templates/mail/recover-password.html")
		val text: String = BufferedReader(InputStreamReader(resource.inputStream, StandardCharsets.UTF_8))
			.lines()
			.collect(Collectors.joining("\n"))
			.replace("\${userName}", userName)
			.replace("\${urlActivate}", "${frontBaseUrl}/authentication/new-password/${activateCode}")

		googleMail.send("Recuperación de cuenta Exership", text, email)
	}

}