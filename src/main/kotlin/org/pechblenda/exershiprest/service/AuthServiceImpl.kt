package org.pechblenda.exershiprest.service

import org.pechblenda.exception.BadRequestException
import org.pechblenda.exception.UnauthenticatedException
import org.pechblenda.exershiprest.dao.IUserDAO
import org.pechblenda.exershiprest.entity.User
import org.pechblenda.exershiprest.mail.MailTemplate
import org.pechblenda.exershiprest.security.UserPrinciple
import org.pechblenda.exershiprest.service.`interface`.IAuthService
import org.pechblenda.exershiprest.service.message.AuthMessage
import org.pechblenda.rest.Request
import org.pechblenda.rest.Response
import org.pechblenda.security.JwtProvider
import org.pechblenda.storage.FirebaseStorage
import org.pechblenda.util.Avatar
import org.pechblenda.util.Text

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import java.util.UUID

@Service
class AuthServiceImpl: IAuthService, UserDetailsService {

	@Autowired
	private lateinit var userDAO: IUserDAO

	@Autowired
	private lateinit var response: Response

	@Autowired
	private lateinit var passwordEncoder: PasswordEncoder

	@Autowired
	private lateinit var authenticationManager: AuthenticationManager

	@Autowired
	private lateinit var jwtProvider: JwtProvider

	@Autowired
	private lateinit var text: Text

	@Autowired
	private lateinit var mailTemplate: MailTemplate

	@Autowired
	private lateinit var firebaseStorage: FirebaseStorage

	@Autowired
	private lateinit var avatar: Avatar

	@Autowired
	private lateinit var message: AuthMessage

	@Transactional(readOnly = true)
	override fun canActivate(userUid: UUID): ResponseEntity<Any> {
		val user = userDAO.findById(userUid).orElseThrow {
			throw BadRequestException(message.activateUserNotFount)
		}

		if (user.active) {
			throw BadRequestException(message.activateUserInvalid)
		}

		val out = Request()
		out["canActivate"] = true

		return response.ok(out)
	}

	@Transactional(readOnly = true)
	override fun canChangePassword(activatePassword: UUID): ResponseEntity<Any> {
		if (!userDAO.existsByActivatePassword(activatePassword)) {
			throw BadRequestException(message.recoverCodeInvalid)
		}

		val out = Request()
		out["canChangePassword"] = true

		return response.ok(out)
	}

	@Transactional
	override fun activateAccount(request: Request): ResponseEntity<Any> {
		val user = request.to<User>(User::class)

		if (user.password.isEmpty()) {
			throw BadRequestException(message.passwordRequired)
		}

		val findUser = userDAO.findById(user.uid).orElseThrow {
			throw BadRequestException(message.userNotFount)
		}

		if (findUser.active) {
			throw BadRequestException(message.accountBeActivated)
		}

		findUser.password = passwordEncoder.encode(user.password)
		findUser.active = true
		findUser.enabled = true

		return response.ok(message.accountActivated)
	}

	@Transactional
	override fun changePassword(request: Request): ResponseEntity<Any> {
		val user = request.to<User>(User::class)

		if (user.password.isEmpty()) {
			throw BadRequestException(message.passwordRequired)
		}

		val userFind = userDAO.findByActivatePassword(user.activatePassword!!).orElseThrow {
			throw BadRequestException(message.accountNotMatch)
		}

		userFind.activatePassword = null
		userFind.password = passwordEncoder.encode(user.password)

		return response.ok(message.passwordChanged)
	}

	@Transactional
	override fun recoverPassword(request: Request): ResponseEntity<Any> {
		val user = request.to<User>(User::class)
		val userFind = userDAO.findByUserNameOrEmail(user.email).orElseThrow {
			BadRequestException(message.accountNotMatch)
		}

		userFind.activatePassword = UUID.randomUUID()
		mailTemplate.sendRecoverPassword(
			userFind.name,
			userFind.activatePassword.toString(),
			userFind.email
		)

		return response.ok(
			message.recoverInstruction.replace(
				"{email}",
				userFind.email
			)
		)
	}

	@Transactional
	override fun signUp(request: Request): ResponseEntity<Any> {
		val user = request.to<User>(User::class)
		val temporalPassword = text.unique()

		if (user.name.isEmpty()) {
			throw BadRequestException(message.nameRequired)
		}

		if (user.userName.isEmpty()) {
			throw BadRequestException(message.userNameRequired)
		}

		if (user.email.isEmpty()) {
			throw BadRequestException(message.emailRequired)
		}

		if (userDAO.existsByUserName(user.userName)) {
			throw BadRequestException(message.userNameRegistered)
		}

		if (userDAO.existsByEmail(user.email)) {
			throw BadRequestException(message.emailRegistered)
		}

		user.password = passwordEncoder.encode(temporalPassword)

		user.photo = firebaseStorage.put(
			"users",
			"image/png",
			".png",
			avatar.createDefaultAccountImage(
				"${user.name[0]}".toUpperCase()
			).readBytes()
		)

		val userOut = userDAO.save(user)
		mailTemplate.sendActivateAccount(user.name, userOut.uid.toString(), user.email)

		return response.created(message.accountCreated)
	}

	@Transactional
	override fun signIn(request: Request): ResponseEntity<Any> {
		val user = request.to<User>(User::class)
		val userOut = userDAO.findByUserNameOrEmail(
			user.userName
		).orElseThrow { throw BadRequestException(message.userNotFount) }

		if (!userOut.active) {
			throw BadRequestException(message.accountNotActivate)
		}

		if (!userOut.enabled) {
			throw BadRequestException(message.accountBlocked)
		}

		val session: Map<String, Any>

		try {
			val authentication: Authentication = authenticationManager.authenticate(
				UsernamePasswordAuthenticationToken(
					userOut.userName,
					user.password
				)
			)

			if (userOut.refreshToken.isEmpty() || jwtProvider.isJwtExpire(userOut.refreshToken)) {
				session = jwtProvider.generateJwtTokenRefresh(authentication)
				userOut.refreshToken = session["refreshToken"] as String
			} else {
				session = jwtProvider.generateJwtToken(authentication)
				session["refreshToken"]	= userOut.refreshToken
			}
		} catch (e: Exception) {
			throw UnauthenticatedException(message.passwordIncorrect)
		}

		val out = response.toMap(
			userOut
		)

		out["session"] = session

		return out
			.exclude(
				"password",
				"enabled",
				"active",
				"activatePassword",
				"refreshToken"
			)
			.firstId()
			.ok()
	}

	@Transactional
	override fun refreshToken(request: Request): ResponseEntity<Any> {
		if (!request.containsKey("refreshToken")) {
			throw BadRequestException(message.refreshTokenRequest)
		}

		if (request["refreshToken"].toString().isEmpty()) {
			throw BadRequestException(message.refreshTokenRequest)
		}

		return response.ok(
			jwtProvider.refreshToken(
				request["refreshToken"].toString()
			)
		)
	}

	@Transactional(readOnly = true)
	override fun loadUserByUsername(userName: String): UserDetails {
		val user: User = userDAO.findByUserName(userName).orElseThrow {
			UsernameNotFoundException(message.userNotFount)
		}
		return UserPrinciple.build(user)
	}

}