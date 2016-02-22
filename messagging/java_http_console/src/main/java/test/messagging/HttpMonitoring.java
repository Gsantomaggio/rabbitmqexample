package test.messagging;


import com.rabbitmq.http.client.Client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by GaS on 21/02/16.
 */
public class HttpMonitoring {

    private String ip;
    private int port;
    private String user;
    private String password;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
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

    public String getUrlConnection() {

        return String.format("http://%s:%d/api/", ip, port);

    }


    // HTTP GET request
    private void checkStatus() throws Exception {

        Client c = new Client(getUrlConnection(), getUser(), getPassword());
        System.out.println(c.getOverview());

    }


    public static void main(String[] argv) throws Exception {
        HttpMonitoring httpMonitoring = new HttpMonitoring();
        httpMonitoring.setIp(argv[0]);
        httpMonitoring.setPort(Integer.parseInt(argv[1]));
        httpMonitoring.setUser(argv[2]);
        httpMonitoring.setPassword(argv[3]);
        httpMonitoring.checkStatus();
    }

}
