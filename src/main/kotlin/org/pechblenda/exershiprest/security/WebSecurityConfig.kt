package org.pechblenda.exershiprest.security

import org.pechblenda.exershiprest.service.AuthServiceImpl
import org.pechblenda.security.JwtAuthEntryPoint
import org.pechblenda.security.JwtAuthTokenFilter
import org.pechblenda.security.JwtProvider
import org.pechblenda.security.JwtProviderSocket

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class WebSecurityConfig(
	@Value("\${app.auth.jwt-secret}")
	private val jwtSecret: String,
	@Value("\${app.auth.jwt-expiration}")
	private val jwtExpiration: Int
): WebSecurityConfigurerAdapter() {

	@Autowired
	private lateinit var userDetailsService: AuthServiceImpl

	@Autowired
	private lateinit var jwtAuthEntryPoint: JwtAuthEntryPoint

	@Autowired
	private lateinit var jwtProvider: JwtProvider


	@Throws(Exception::class)
	override fun configure(authenticationManagerBuilder: AuthenticationManagerBuilder) {
		authenticationManagerBuilder
			.userDetailsService<UserDetailsService?>(userDetailsService)
			.passwordEncoder(passwordEncoder())
	}

	@Throws(Exception::class)
	override fun configure(http: HttpSecurity) {
		http
			.cors()
			.and()
			.csrf()
			.disable()
			.authorizeRequests()
			.antMatchers(
				"/auth/can-activate-account/**",
				"/auth/can-change-password/**",
				"/auth/activate-account",
				"/auth/change-password",
				"/auth/recover-password",
				"/auth/sign-up",
				"/auth/sign-in",
				"/auth/refresh-token",
				"/ws/**"
			).permitAll()
			.anyRequest()
			.authenticated()
			.and()
			.exceptionHandling()
			.authenticationEntryPoint(jwtAuthEntryPoint)
			.and()
			.sessionManagement()
			.sessionCreationPolicy(SessionCreationPolicy.STATELESS)

		http.addFilterBefore(
			authenticationJwtTokenFilter(),
			UsernamePasswordAuthenticationFilter::class.java
		)
	}

	@Bean
	fun jwtAuthEntryPoint(): JwtAuthEntryPoint {
		return JwtAuthEntryPoint()
	}

  @Bean
  fun jwtProvider(): JwtProvider {
  	return JwtProvider(jwtSecret, jwtExpiration)
  }

	@Bean
	fun passwordEncoder(): PasswordEncoder {
		return BCryptPasswordEncoder()
	}

	@Bean
	fun authenticationJwtTokenFilter(): JwtAuthTokenFilter {
		return JwtAuthTokenFilter(jwtProvider, userDetailsService)
	}

	@Bean
  fun jwtProviderSocket(): JwtProviderSocket {
  	return JwtProviderSocket(jwtSecret, jwtExpiration)
  }

	@Bean
	@Throws(Exception::class)
	override fun authenticationManagerBean(): AuthenticationManager {
		return super.authenticationManagerBean()
	}

}