package org.pechblenda.exershiprest.service.message

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class AuthMessage(
	@Value("\${message.auth.account-not-active}")
	val accountNotActive: String,
	@Value("\${message.auth.activate-user-not-fount}")
	val activateUserNotFount: String,
	@Value("\${message.auth.activate-user-invalid}")
	val activateUserInvalid: String,
	@Value("\${message.auth.recover-code-invalid}")
	val recoverCodeInvalid: String,
	@Value("\${message.auth.password-required}")
	val passwordRequired: String,
	@Value("\${message.auth.uid-required}")
	val uidRequired: String,
	@Value("\${message.auth.user-not-fount}")
	val userNotFount: String,
	@Value("\${message.auth.account-be-activated}")
	val accountBeActivated: String,
	@Value("\${message.auth.account-activated}")
	val accountActivated: String,
	@Value("\${message.auth.account-not-match}")
	val accountNotMatch: String,
	@Value("\${message.auth.password-changed}")
	val passwordChanged: String,
	@Value("\${message.auth.recover-instruction}")
	val recoverInstruction: String,
	@Value("\${message.auth.name-required}")
	val nameRequired: String,
	@Value("\${message.auth.user-name-required}")
	val userNameRequired: String,
	@Value("\${message.auth.email-required}")
	val emailRequired: String,
	@Value("\${message.auth.user-name-registered}")
	val userNameRegistered: String,
	@Value("\${message.auth.email-registered}")
	val emailRegistered: String,
	@Value("\${message.auth.account-created}")
	val accountCreated: String,
	@Value("\${message.auth.account-not-activate}")
	val accountNotActivate: String,
	@Value("\${message.auth.account-blocked}")
	val accountBlocked: String,
	@Value("\${message.auth.password-incorrect}")
	val passwordIncorrect: String,
	@Value("\${message.auth.refresh-token-required}")
	val refreshTokenRequest: String
)