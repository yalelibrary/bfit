package edu.yale.library.ladybird.entity;


import java.util.Date;

/**
 * @author Osman Din {@literal <osman.din.yale@gmail.com>}
 */
public class ObjectAcid implements java.io.Serializable {


    private Integer dataId;
    private Date date;
    private int userId;
    private int objectId;
    private int value;
    private int fdid;

    public ObjectAcid() {
    }

    public ObjectAcid(Date date, int userId, int objectId, int value, int fdid) {
        this.date = date;
        this.userId = userId;
        this.objectId = objectId;
        this.value = value;
        this.fdid = fdid;
    }

    public ObjectAcid(ObjectAcid o) {
        this.date = o.getDate();
        this.userId = o.getUserId();
        this.objectId = o.getObjectId();
        this.value = o.getValue();
        this.fdid = o.getFdid();
    }

    public Integer getDataId() {
        return this.dataId;
    }

    public void setDataId(Integer dataId) {
        this.dataId = dataId;
    }

    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getUserId() {
        return this.userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getObjectId() {
        return this.objectId;
    }

    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }

    public int getValue() {
        return this.value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getFdid() {
        return this.fdid;
    }

    public void setFdid(int fdid) {
        this.fdid = fdid;
    }

    @Override
    public String toString() {
        return "ObjectAcid{"
                + "dataId=" + dataId
                + ", date=" + date
                + ", userId=" + userId
                + ", objectId=" + objectId
                + ", value=" + value
                + ", fdid=" + fdid
                + '}';
    }
}


