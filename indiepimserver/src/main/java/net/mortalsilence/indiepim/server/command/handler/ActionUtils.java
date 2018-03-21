package net.mortalsilence.indiepim.server.command.handler;

import net.mortalsilence.indiepim.server.security.IndieUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class ActionUtils {

	@Deprecated
    public static Long getUserIdDeprecated() {
        final IndieUser indieUser = (IndieUser)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(indieUser == null || indieUser.getId() == null)
            throw new RuntimeException("No user id");
		return indieUser.getId();
	}

	public Long getUserId() {
        final IndieUser indieUser = (IndieUser)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(indieUser == null || indieUser.getId() == null)
            throw new RuntimeException("No user id");
        return indieUser.getId();
    }
}
