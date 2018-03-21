package net.mortalsilence.indiepim.server.service

import net.mortalsilence.indiepim.server.command.handler.ActionUtils
import net.mortalsilence.indiepim.server.dao.UserDAO
import org.springframework.stereotype.Component
import javax.transaction.Transactional

@Component
class UserService (private val actionUtils: ActionUtils,
                   private val userDAO: UserDAO,
                   private val passwordEncoder: PasswordEncoder) {

    @Transactional
    fun updateOwnPassword(password: String) {
        val user = userDAO.getUser(this.actionUtils.userId)
        user.passwordHash = passwordEncoder.encodePassword(password)
    }
}