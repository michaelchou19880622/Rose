package com.bcs.core.enums;

import com.bcs.core.db.entity.SendGroup;

public enum DEFAULT_SEND_GROUP {

	ALL_USER(-1L, "ALL", "全部使用者", "全部使用者群組(一般, 已串聯), 不可刪除", true),
	BINDED_USER(-2L, "BINDED", "已串聯使用者", "已串聯使用者群組, 不可刪除", true),
	UNBIND_USER(-3L, "UNBIND", "一般使用者", "一般使用者群組, 不可刪除", true),
	BLOCK_USER(-4L, "BLOCK", "封鎖使用者", "封鎖使用者群組, 不可刪除", false),
	;

    private final Long groupId;
    private final String key;
    private final String title;
    private final String description;
    private final boolean isShow;
    
    DEFAULT_SEND_GROUP(Long groupId, String key, String title, String description, boolean isShow) {
        this.groupId = groupId;
        this.key = key;
        this.title = title;
        this.description = description;
        this.isShow = isShow;
    }
	/**
	 * @return the groupId
	 */
	public String toString() {
		return key;
	}
	public Long getGroupId() {
		return groupId;
	}
	public String getTitle() {
		return title;
	}
	public String getDescription() {
		return description;
	}
	public boolean isShow() {
		return isShow;
	}
	
	public SendGroup defaultGroup(){

		SendGroup defaultGroup = new SendGroup();
		defaultGroup.setGroupId(groupId);
		defaultGroup.setGroupTitle(title);
		defaultGroup.setGroupDescription(description);
		defaultGroup.setModifyUser("BcsAdmin");
		
		return defaultGroup;
	}
	
	public static DEFAULT_SEND_GROUP getGroupByGroupId(Long groupId){
		for(DEFAULT_SEND_GROUP group : values()){
			if(group.getGroupId().equals(groupId)){
				return group;
			}
		}
		return null;
	}
}
