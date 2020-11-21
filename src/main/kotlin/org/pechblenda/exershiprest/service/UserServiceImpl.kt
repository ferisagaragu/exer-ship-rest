package org.pechblenda.exershiprest.service

import org.pechblenda.exception.BadRequestException
import org.pechblenda.exception.UnauthenticatedException
import org.pechblenda.exershiprest.dao.IUserDAO
import org.pechblenda.exershiprest.entity.User
import org.pechblenda.exershiprest.service.`interface`.IUserService
import org.pechblenda.rest.Request
import org.pechblenda.rest.Response
import org.pechblenda.storage.FirebaseStorage

import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder

@Service
class UserServiceImpl(
	private val userDAO: IUserDAO,
	private val response: Response,
	private val firebaseStorage: FirebaseStorage,
	private val authenticationManager: AuthenticationManager,
	private val passwordEncoder: PasswordEncoder
): IUserService {

	@Transactional(readOnly = true)
	override fun findUser(): ResponseEntity<Any> {
		val user = userDAO.findByUserName(
			SecurityContextHolder.getContext().authentication.name
		).orElseThrow {
			BadRequestException("Upps el usuario no existe")
		}

		return response
			.toMap(
				user,
				"convertPhotoName"
			)
			.exclude(
				"password",
				"activatePassword",
				"active",
				"refreshToken"
			)
			.ok()
	}

	@Transactional
	override fun putUser(request: Request): ResponseEntity<Any> {
		val user = request.to<User>(User::class)
		val userFind = userDAO.findByUserName(
			SecurityContextHolder.getContext().authentication.name
		).orElseThrow {
			BadRequestException("Upps el usuario no existe")
		}

		userFind.name = user.name
		userFind.lastName = user.lastName
		userFind.enabled = user.enabled

		if (
			"${userFind.photo.name}${userFind.photo.extension}" !=
			request["photoFile"]
		) {
			firebaseStorage.replace(
				"${userFind.photo.directory}/${userFind.photo.name}",
				userFind.photo.contentType,
				userFind.photo.extension,
				request.toString("base64").split(",")[1]
			)
		}

		if (request.containsKey("newPassword")) {
			try {
				authenticationManager.authenticate(
					UsernamePasswordAuthenticationToken(
						userFind.userName,
						user.password
					)
				)

				userFind.password = passwordEncoder.encode(
					request.toString("newPassword")
				)
			} catch (e: Exception) {
				throw UnauthenticatedException("Upps la contraseña es incorrecta")
			}
		}

		return response.ok()
	}

}