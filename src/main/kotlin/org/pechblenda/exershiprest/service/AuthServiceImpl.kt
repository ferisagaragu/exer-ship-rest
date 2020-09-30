package org.pechblenda.exershiprest.service

import org.pechblenda.exception.BadRequestException
import org.pechblenda.exception.UnauthenticatedException
import org.pechblenda.exershiprest.dao.IUserDAO
import org.pechblenda.exershiprest.entity.User
import org.pechblenda.exershiprest.mail.MailTemplate
import org.pechblenda.exershiprest.security.UserPrinciple
import org.pechblenda.exershiprest.service.`interface`.IAuthService
import org.pechblenda.rest.Request
import org.pechblenda.rest.Response
import org.pechblenda.security.JwtProvider
import org.pechblenda.storage.FirebaseStorage
import org.pechblenda.util.Avatar
import org.pechblenda.util.Text

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
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

	@Value("\${message.name-required}")
	private lateinit var messageNameRequired: String

	@Value("\${message.user-name-required}")
	private lateinit var messageUserNameRequired: String

	@Value("\${message.email-required}")
	private lateinit var messageEmailRequired: String

	@Value("\${message.user-name-registered}")
	private lateinit var messageUserNameRegistered: String

	@Value("\${message.email-registered}")
	private lateinit var messageEmailRegistered: String

	override fun canActivate(userUid: UUID): ResponseEntity<Any> {
		val user = userDAO.findById(userUid).orElseThrow {
			throw BadRequestException("Upps no se encuentra el usuario que quieres activar")
		}

		if (user.active) {
			throw BadRequestException(
				"Upps el usuario ya esta activado, intenta iniciando sesión de forma habitual ❤"
			)
		}

		val out = Request()
		out["canActivate"] = true;

		return response.ok(out)
	}

	@Transactional
	override fun signUp(request: Request): ResponseEntity<Any> {
		val user = request.to<User>(User::class)
		val temporalPassword = text.unique()

		if (user.name.isEmpty()) {
			throw BadRequestException(messageNameRequired)
		}

		if (user.userName.isEmpty()) {
			throw BadRequestException(messageUserNameRequired)
		}

		if (user.email.isEmpty()) {
			throw BadRequestException(messageEmailRequired)
		}

		if (userDAO.existsByUserName(user.userName)) {
			throw BadRequestException(messageUserNameRegistered)
		}

		if (userDAO.existsByEmail(user.email)) {
			throw BadRequestException(messageEmailRegistered)
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
		mailTemplate.sendTemporalActivateLink(user.userName, userOut.uid.toString(), user.email)

		return response
			.toMap(userOut)
			.exclude(
				"lastName",
				"password",
				"enabled",
				"active",
				"roles"
			)
			.firstId()
			.created()
	}

	@Transactional
	override fun signIn(request: Request): ResponseEntity<Any> {
		val user = request.to<User>(User::class)
		val userOut = userDAO.findByUserNameOrEmail(
			user.userName
		).orElseThrow { throw BadRequestException("Upps no se encontró el usuario") }
		var session: Map<String, Any>

		try {
			val authentication: Authentication = authenticationManager.authenticate(
				UsernamePasswordAuthenticationToken(
					userOut.userName,
					user.password
				)
			)

			session = jwtProvider.generateJwtToken(authentication)
		} catch (e: Exception) {
			throw UnauthenticatedException("Upps la contraseña es incorrecta");
		}

		var out = response.toMap(
			userOut
		)

		out["session"] = session

		return out
			.exclude("password")
			.firstId()
			.ok()
	}

	@Transactional
	override fun activateAccount(request: Request): ResponseEntity<Any> {
		val user = request.to<User>(User::class, false)

		if (user.password == null) {
			throw BadRequestException("Upps la contraseña es requerida")
		}

		if (user.password.isEmpty()) {
			throw BadRequestException("Upps la contraseña es requerida")
		}

		if (user.uid == null) {
			throw BadRequestException("Upps el uid del usuario es requerido")
		}

		val findUser = userDAO.findById(user.uid).orElseThrow {
			throw BadRequestException("Upps no se encuentra el usuario")
		}

		findUser.password = passwordEncoder.encode(user.password)
		findUser.active = true

		return response
			.toMap(findUser)
			.exclude(
				"password",
				"roles"
			)
			.firstId()
			.created()
	}

	override fun recoverPassword(request: Request): ResponseEntity<Any> {
		TODO("Not yet implemented")
	}

	@Transactional(readOnly = true)
	override fun loadUserByUsername(userName: String): UserDetails {
		val user: User = userDAO.findByUserName(userName).orElseThrow {
			UsernameNotFoundException("Upps no se encontró el usuario")
		}
		return UserPrinciple.build(user)
	}

}