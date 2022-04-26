package com.glia.widgets.core.queue.model;

public interface GliaQueueingState {
    enum Type {
        NONE,
        CHAT,
        MEDIA
    }

    GliaQueueingState.Type getType();

    String getTicketId();

    class None implements GliaQueueingState {
        @Override
        public Type getType() {
            return Type.NONE;
        }

        @Override
        public String getTicketId() {
            return null;
        }
    }

    class Chat implements GliaQueueingState {
        public final String queueId;
        public final String queueTicket;

        public Chat(String queueId, String queueTicket) {
            this.queueId = queueId;
            this.queueTicket = queueTicket;
        }

        public Type getType() {
            return Type.CHAT;
        }

        @Override
        public String getTicketId() {
            return queueTicket;
        }
    }

    class Media implements GliaQueueingState {
        public final String queueId;
        public final String queueTicket;

        public Media(String queueId, String queueTicket) {
            this.queueId = queueId;
            this.queueTicket = queueTicket;
        }

        public Type getType() {
            return Type.MEDIA;
        }

        @Override
        public String getTicketId() {
            return queueTicket;
        }
    }
}
