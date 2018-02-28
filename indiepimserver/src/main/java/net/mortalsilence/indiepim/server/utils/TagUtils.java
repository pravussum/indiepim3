package net.mortalsilence.indiepim.server.utils;

import net.mortalsilence.indiepim.server.domain.TagHierarchyPO;
import net.mortalsilence.indiepim.server.domain.TagLineagePO;
import net.mortalsilence.indiepim.server.domain.TagPO;
import net.mortalsilence.indiepim.server.dto.TagDTO;
import net.mortalsilence.indiepim.server.tags.TagHierarchyNode;
import net.mortalsilence.indiepim.server.tags.TagHierarchyNodeHelper;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

@Service
public class TagUtils {

	private final TagHierarchyNodeHelper tagHierarchyNodeHelper;

	@Inject
	public TagUtils(TagHierarchyNodeHelper tagHierarchyNodeHelper) {
		this.tagHierarchyNodeHelper = tagHierarchyNodeHelper;
	}

	public TagHierarchyNode getTagHierarchyTree(final TagHierarchyPO tagHierarchy) {
		TagHierarchyNode tree = new TagHierarchyNode();
		
		List<TagLineagePO> tagLineages = new ArrayList<>(tagHierarchy.getTagLineages());
		Collections.sort(tagLineages);
		
		for(TagLineagePO tagLineage : tagLineages) {
			tagHierarchyNodeHelper.addPath(tree, tagLineage.getLineage(), tagLineage.getId());
		}
		return tree;
	}
	
	public Collection<TagDTO> mapTagPO2TagDTOList(final Collection<TagPO> tagPOs) {
		
		final Collection<TagDTO> result = new LinkedList<TagDTO>();
		final Iterator<TagPO> it = tagPOs.iterator();
		while(it.hasNext()) {
			final TagPO curTagPO = it.next();			
			final TagDTO tagDTO = new TagDTO();
			tagDTO.color = curTagPO.getColor();
			tagDTO.tag = curTagPO.getTag();
			result.add(tagDTO);
		}		
		
		return result;
	}

}
