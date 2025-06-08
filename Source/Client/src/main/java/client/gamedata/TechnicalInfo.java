package client.gamedata;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class TechnicalInfo {
    private final PropertyChangeSupport changes = new PropertyChangeSupport(this);
    private String lastMessage;

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changes.addPropertyChangeListener(listener);
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String msg) {
        String oldMsg = this.lastMessage;
        this.lastMessage = msg;
        changes.firePropertyChange("technicalInfo", oldMsg, msg);
    }
    
    public void addError(String msg) {
        setLastMessage(msg);
    }
}
// This class is used to track technical information