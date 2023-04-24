package fr.utc.sr03.chat.model;

import javax.persistence.*;

@Entity
@Table(name = "relation_users_chatrooms")
public class UserChatroomRelation {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY) // strategy=GenerationType.IDENTITY => obligatoire pour auto increment mysql
    private long EntryId;

    @Column(name = "user_id")
    private long userId;

    @Column(name = "chatroom_id")
    private long chatRoomId;

    @Column(name = "chatroom_ownedby_user")
    private boolean owned;

    public UserChatroomRelation(){}

    public long getUserId() {
        return this.userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getChatRoomId() {
        return this.chatRoomId;
    }

    public void setChatRoomId(long chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public boolean isOwned() {
        return this.owned;
    }

    public void setOwned(boolean owned) {
        this.owned = owned;
    }
}
