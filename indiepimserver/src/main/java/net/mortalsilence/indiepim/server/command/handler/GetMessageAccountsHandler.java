package net.mortalsilence.indiepim.server.command.handler;

import net.mortalsilence.indiepim.server.command.Command;
import net.mortalsilence.indiepim.server.command.actions.GetMessageAccounts;
import net.mortalsilence.indiepim.server.command.results.GetMessageAccountsResult;
import net.mortalsilence.indiepim.server.dao.MessageDAO;
import net.mortalsilence.indiepim.server.domain.MessageAccountPO;
import net.mortalsilence.indiepim.server.dto.MessageAccountDTO;
import net.mortalsilence.indiepim.server.utils.MessageUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

@Named
public class GetMessageAccountsHandler implements Command<GetMessageAccounts, GetMessageAccountsResult> {

    private final MessageDAO messageDAO;
    private final MessageUtils messageUtils;
    private final ActionUtils actionUtils;

	@Inject
	public GetMessageAccountsHandler(MessageDAO messageDAO, MessageUtils messageUtils, ActionUtils actionUtils) {
		this.messageDAO = messageDAO;
		this.messageUtils = messageUtils;
		this.actionUtils = actionUtils;
	}

	@Transactional(readOnly = true)
	@Override
    public GetMessageAccountsResult execute(GetMessageAccounts action) {
		
		final List<MessageAccountPO> accounts = messageDAO.getMessageAccounts(actionUtils.getUserId());
		
		final List<MessageAccountDTO> result = new ArrayList<>();
		
		for(MessageAccountPO account : accounts) {
			MessageAccountDTO resultAccount = messageUtils.mapMessageAccountPO2MessageAccountDTO(account);
			result.add(resultAccount);
		}
		return new GetMessageAccountsResult(result);
	}

	@Override
	public void rollback(GetMessageAccounts arg0, GetMessageAccountsResult arg1) {
		// get method, no use to roll back
	}
	

}
