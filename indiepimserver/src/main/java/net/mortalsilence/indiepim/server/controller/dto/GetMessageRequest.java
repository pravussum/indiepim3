package net.mortalsilence.indiepim.server.controller.dto;

public class GetMessageRequest {

    private Long accountId;
    private String tagName;
    private Long tagLineageId;
    private String searchTerm;
    private Boolean read;
    private Long offset;
    private Integer pageSize;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public Long getTagLineageId() {
        return tagLineageId;
    }

    public void setTagLineageId(Long tagLineageId) {
        this.tagLineageId = tagLineageId;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public Long getOffset() {
        return offset;
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
