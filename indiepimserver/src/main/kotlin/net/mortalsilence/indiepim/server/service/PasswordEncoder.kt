package net.mortalsilence.indiepim.server.service

import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.stereotype.Component

@Component
class PasswordEncoder {
    fun encodePassword(clearTextPassword: String): String {
        val passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()
        return passwordEncoder.encode(clearTextPassword)
    }
}