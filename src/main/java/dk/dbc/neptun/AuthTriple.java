package dk.dbc.neptun;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AuthTriple {
    private String user;
    private String group;
    private String password;

    public String getUser() {
        return user;
    }

    @XmlElement
    public void setUser(String user) {
        this.user = user;
    }

    public String getGroup() {
        return group;
    }

    @XmlElement
    public void setGroup(String group) {
        this.group = group;
    }

    public String getPassword() {
        return password;
    }

    @XmlElement
    public void setPassword(String password) {
        this.password = password;
    }
}
