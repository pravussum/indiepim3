package net.mortalsilence.indiepim.server.command.handler;

import net.mortalsilence.indiepim.server.command.Command;
import net.mortalsilence.indiepim.server.command.actions.GetTags;
import net.mortalsilence.indiepim.server.command.results.TagDTOListResult;
import net.mortalsilence.indiepim.server.dao.TagDAO;
import net.mortalsilence.indiepim.server.domain.TagPO;
import net.mortalsilence.indiepim.server.utils.TagUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Collection;

@Service
public class GetTagsHandler implements Command<GetTags, TagDTOListResult> {

    private final TagDAO tagDAO;
    private final TagUtils tagUtils;

    @Inject
    public GetTagsHandler(TagDAO tagDAO, TagUtils tagUtils) {
        this.tagDAO = tagDAO;
        this.tagUtils = tagUtils;
    }

    @Transactional(readOnly = true)
	@Override
    public TagDTOListResult execute(GetTags action) {
        final Collection<TagPO> tags;
        if(action.getTagLineageId() != null) {
			 tags = tagDAO.getAllTagsForTagLineage(ActionUtils.getUserIdDeprecated(), action.getTagLineageId());
        } else if(action.getQuery() != null) {
            tags = tagDAO.searchForTags(ActionUtils.getUserIdDeprecated(), action.getQuery());
        } else {
            tags = tagDAO.getAllTags(ActionUtils.getUserIdDeprecated());
        }
        return new TagDTOListResult(tagUtils.mapTagPO2TagDTOList(tags));
	}

	@Override
	public void rollback(GetTags arg0, TagDTOListResult arg1) {
		// no use rolling back a getter
	}

}
