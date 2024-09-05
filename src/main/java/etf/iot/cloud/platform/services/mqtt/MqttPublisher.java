package etf.iot.cloud.platform.services.mqtt;

import jakarta.annotation.PostConstruct;
import org.eclipse.paho.mqttv5.client.*;
import org.eclipse.paho.mqttv5.client.persist.MqttDefaultFilePersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MqttPublisher {

    private MqttConnectionOptions connOpts = new MqttConnectionOptions();
    private long MAX_SESSION_EXPIRATION_INTERVAL = 4294967295L;

    /**
     * Protocol topic name.
     */
    @Value("${mqtt.PROTOCOL_TOPIC}")
    private String PROTOCOL_TOPIC;

    /**
     * Quality of service level for publisher packets.
     */
    @Value("${mqtt.qos}")
    private int qos;
    /**
     * Mqtt broker url to which client connects.
     */
    @Value("${mqtt.broker}")
    private String broker;
    /**
     * Client id for this subscriber.
     */
    @Value("${mqtt.protocolClientId}")
    private String clientId;
    /**
     * Username for mqtt broker authentication.
     */
    @Value("${mqtt.username}")
    private String username;
    /**
     * Password for mqtt broker authentication.
     */
    @Value("${mqtt.password}")
    private String password;

    private MqttClient mqttClient;

    @PostConstruct
    public void init() throws MqttException {
        MqttConnectionOptionsBuilder options = new MqttConnectionOptionsBuilder();
        options.username(username);
        options.password(password.getBytes());
        options.serverURI(broker);
        options.automaticReconnect(true);
        options.keepAliveInterval(8*60*60);
        options.cleanStart(false);
        options.sessionExpiryInterval(MAX_SESSION_EXPIRATION_INTERVAL);

        mqttClient = new MqttClient(broker, clientId, new MqttDefaultFilePersistence());
        mqttClient.connect(options.build());
    }

    /*public MqttPublisher() throws MqttException {
        MqttConnectionOptionsBuilder options = new MqttConnectionOptionsBuilder();
        options.username(username);
        options.password(password.getBytes());
        options.serverURI(broker);
        options.automaticReconnect(true);
        options.keepAliveInterval(8*60*60);
        options.cleanStart(false);
        options.sessionExpiryInterval(MAX_SESSION_EXPIRATION_INTERVAL);

        this.mqttClient = new MqttClient(broker, clientId, new MqttDefaultFilePersistence());
        mqttClient.connect(options.build());
    }*/

    public void publish(String payload) throws MqttException {
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(qos);
        mqttClient.publish(PROTOCOL_TOPIC, message);

    }

    public void disconnect() throws MqttException {
        mqttClient.disconnect();
    }

}
