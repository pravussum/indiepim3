package net.mortalsilence.indiepim.server.message.synchronisation

import net.mortalsilence.indiepim.server.dao.GenericDAO
import net.mortalsilence.indiepim.server.domain.MessagePO
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@DataJpaTest
@SpringBootTest
class PersistenceHelperTest {

    @Test
    fun test() {
        val genericDao = GenericDAO()
        val messagePO = MessagePO()
        genericDao.persist(messagePO)
    }

}