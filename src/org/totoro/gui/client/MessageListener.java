package org.totoro.gui.client;

public abstract class MessageListener {
    public static final int ERROR = 1, MESSAGE = 0, INFO = 2;

    public void messageReceived(int type, String message) { }
}
