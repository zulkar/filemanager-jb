package net.zulkar.jb.core.ftp;

public class FtpParameters {
    private String host;
    private int port;
    private String user;
    private String password;
    private long timeoutMilliseconds;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getTimeoutMilliseconds() {
        return timeoutMilliseconds;
    }

    public void setTimeoutMilliseconds(long timeoutMilliseconds) {
        this.timeoutMilliseconds = timeoutMilliseconds;
    }
}
