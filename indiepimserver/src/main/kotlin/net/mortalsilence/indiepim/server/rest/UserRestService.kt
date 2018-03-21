package net.mortalsilence.indiepim.server.rest

import net.mortalsilence.indiepim.server.service.UserService
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user")
class UserRestService(private val userService: UserService) {

    @RequestMapping(value = ["updatePassword"], method = [RequestMethod.POST])
    fun updateOwnPassword(@RequestBody password: String) {
        userService.updateOwnPassword(password)
    }

}