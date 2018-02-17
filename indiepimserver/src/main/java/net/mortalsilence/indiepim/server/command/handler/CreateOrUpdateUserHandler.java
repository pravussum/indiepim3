package net.mortalsilence.indiepim.server.command.handler;

import net.mortalsilence.indiepim.server.command.Command;
import net.mortalsilence.indiepim.server.command.actions.CreateOrUpdateUser;
import net.mortalsilence.indiepim.server.command.results.IdResult;
import net.mortalsilence.indiepim.server.dao.GenericDAO;
import net.mortalsilence.indiepim.server.dao.UserDAO;
import net.mortalsilence.indiepim.server.domain.CalendarPO;
import net.mortalsilence.indiepim.server.domain.UserPO;
import net.mortalsilence.indiepim.server.dto.UserDTO;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;

@Named
public class CreateOrUpdateUserHandler implements Command<CreateOrUpdateUser, IdResult> {

    private final UserDAO userDAO;
    private final GenericDAO genericDAO;

    @Inject
    public CreateOrUpdateUserHandler(UserDAO userDAO, GenericDAO genericDAO) {
        this.userDAO = userDAO;
        this.genericDAO = genericDAO;
    }

    @Transactional
    public IdResult execute(CreateOrUpdateUser action) {

        final UserDTO userDTO = action.getUser();
        if(userDTO.getUserName() == null || "".equals(userDTO.getUserName()))
            throw new RuntimeException("Username must not be null!");

        // existing user?
        UserPO user = null;
        if(userDTO.getId() != null) {
            user = userDAO.getUser(userDTO.getId());
        }
        boolean newUser = user == null;
        if(newUser) {
            if(userDTO.getPassword() == null || "".equals(userDTO.getPassword()))
                throw new RuntimeException("User password must not be empty!");

            user = new UserPO();
        }

        user.setUserName(userDTO.getUserName());
        // only overwrite password when given. Must always be present for new users (check see above).
        if(userDTO.getPassword() != null) {
            PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
            user.setPasswordHash(passwordEncoder.encode(userDTO.getPassword()));
        }
        user.setAdmin(userDTO.getAdmin() != null ? userDTO.getAdmin() : false);

        user = genericDAO.merge(user);

        // create some initial data (default calendar, ...)

        if(newUser) {
            createInitialUserData(user);
        }

		return new IdResult(user.getId());
	}

    private void createInitialUserData(UserPO user) {
        // create an default calendar
        final CalendarPO defaultCalendarPO = new CalendarPO();
        defaultCalendarPO.setUser(user);
        defaultCalendarPO.setName("Default Calendar");
        defaultCalendarPO.setDefaultCalendar(true);
        defaultCalendarPO.setColor("000099");
        genericDAO.persist(defaultCalendarPO);
    }

	public void rollback(CreateOrUpdateUser arg0, IdResult arg1) {
		// TODO implement rollback
	}

}
