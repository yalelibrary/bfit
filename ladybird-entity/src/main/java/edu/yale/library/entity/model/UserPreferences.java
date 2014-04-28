package edu.yale.library.entity.model;

public class UserPreferences implements java.io.Serializable {


    private Integer userId;
    private Integer projectId;

    public UserPreferences() {
    }

    public UserPreferences(Integer projectId) {
        this.projectId = projectId;
    }

    public Integer getUserId() {
        return this.userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getProjectId() {
        return this.projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    @Override
    public String toString() {
        return "UserPreferences{" +
                "userId=" + userId +
                ", projectId=" + projectId +
                '}';
    }
}


