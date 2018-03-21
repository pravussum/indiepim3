package net.mortalsilence.indiepim.server.command.handler;

import net.mortalsilence.indiepim.server.pushmessage.PushMessageService;
import net.mortalsilence.indiepim.server.command.Command;
import net.mortalsilence.indiepim.server.command.actions.GetUsers;
import net.mortalsilence.indiepim.server.command.results.UserDTOListResult;
import net.mortalsilence.indiepim.server.dao.UserDAO;
import net.mortalsilence.indiepim.server.domain.UserPO;
import net.mortalsilence.indiepim.server.utils.UserUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Set;

@Service
public class GetUsersHandler implements Command<GetUsers, UserDTOListResult> {

    private final UserDAO userDAO;
    private final PushMessageService pushMessageService;

    @Inject
    public GetUsersHandler(UserDAO userDAO,
                           PushMessageService pushMessageService) {
        this.userDAO = userDAO;
        this.pushMessageService = pushMessageService;
    }

    @Transactional(readOnly = true)
	@Override
    public UserDTOListResult execute(GetUsers action) {

        final Long userId = ActionUtils.getUserId();
        final Collection<UserPO> userPOs;
        if(action.getOnlineOnly()) {
            final Set<Long> onlineUserIds = pushMessageService.getOnlineUsers(userId);
            userPOs = userDAO.getUsers(onlineUserIds);
        } else {
            userPOs = userDAO.getUsers();

        }
        return new UserDTOListResult(UserUtils.mapUserPOs2UserDTOs(userPOs));
	}

	@Override
	public void rollback(GetUsers arg0, UserDTOListResult arg1) {
		// no use rolling back a getter
	}

}
