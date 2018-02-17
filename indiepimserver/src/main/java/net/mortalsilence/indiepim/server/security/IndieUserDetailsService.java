package net.mortalsilence.indiepim.server.security;

import net.mortalsilence.indiepim.server.dao.UserDAO;
import net.mortalsilence.indiepim.server.domain.UserPO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Collection;
import java.util.LinkedList;

@Service
public class IndieUserDetailsService implements UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndieUserDetailsService.class);

    @Inject
    private UserDAO userDAO;

    @Override
    @Transactional(readOnly = true)
    public IndieUser loadUserByUsername(String userName) throws UsernameNotFoundException {
        final UserPO user = userDAO.getByUsername(userName);
        if (user == null) {
            String msg = String.format("User '%s' not found!", userName);
            LOGGER.warn(msg);
            throw new UsernameNotFoundException(msg);
        }

        final Collection<GrantedAuthority> grants = new LinkedList<>();
        grants.add(new SimpleGrantedAuthority("ROLE_USER"));
        if(user.isAdmin()) {
            grants.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        final IndieUser indieUser = new IndieUser(user.getUserName(), user.getPasswordHash(), grants);
        indieUser.setId(user.getId());
        return indieUser;
    }
}