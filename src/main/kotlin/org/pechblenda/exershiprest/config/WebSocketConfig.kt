package org.pechblenda.exershiprest.config;

import org.pechblenda.exershiprest.service.AuthServiceImpl
import org.pechblenda.security.JwtProviderSocket

import org.slf4j.LoggerFactory

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
class WebSocketConfig: WebSocketMessageBrokerConfigurer {

	@Autowired
	private lateinit var jwtProviderSocket: JwtProviderSocket

	@Autowired
	private lateinit var authServiceImpl: AuthServiceImpl

	private val logger = LoggerFactory.getLogger(WebSocketConfig::class.java)

	override fun registerStompEndpoints(registry: StompEndpointRegistry) {
		registry
			.addEndpoint("/ws")
			.setAllowedOrigins("*")

		registry
			.addEndpoint("/ws")
			.setAllowedOrigins("*")
			.withSockJS()
	}

	override fun configureMessageBroker(registry: MessageBrokerRegistry) {
		registry
			.setApplicationDestinationPrefixes("/app")
			.enableSimpleBroker("/notify")
	}

	override fun configureClientInboundChannel(registration: ChannelRegistration) {
		registration.interceptors(object : ChannelInterceptor {
			override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
				val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)!!

				if (StompCommand.CONNECT == accessor.command) {
					val headers = accessor.getNativeHeader("Authorization")

					if (headers!!.size > 0) {
						val accessToken = headers[0].replace("Bearer ", "")

						try {
							val user = jwtProviderSocket.getUserNameFromJwtToken(accessToken)

							accessor.user = jwtProviderSocket.authenticateWithToken(
								accessToken,
								authServiceImpl.loadUserByUsername(user)
							)

							logger.info("Access to web socket for: $user")
						} catch (e: Exception) {
							logger.error("Unauthenticated token")
						}
					}
				}

				return message
			}
		})
	}

}