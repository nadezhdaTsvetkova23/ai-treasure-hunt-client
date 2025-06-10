package client.gamedata;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TechnicalInfo {
	private static final Logger log = LoggerFactory.getLogger(TechnicalInfo.class);

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
        log.warn("Technical issue tracked: {}", msg);
        changes.firePropertyChange("technicalInfo", oldMsg, msg);
    }
    
    public void addError(String msg) {
        setLastMessage(msg);
    }
}
// This class is used to track technical information