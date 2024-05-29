package etf.iot.cloud.platform.services.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import etf.iot.cloud.platform.services.dto.Data;
import etf.iot.cloud.platform.services.dto.Stats;
import etf.iot.cloud.platform.services.dto.mqtt.MqttData;
import etf.iot.cloud.platform.services.dto.mqtt.MqttStats;
import etf.iot.cloud.platform.services.enums.DataType;
import etf.iot.cloud.platform.services.services.DataService;
import etf.iot.cloud.platform.services.services.StatsService;
import etf.iot.cloud.platform.services.util.LoggerBean;
import org.eclipse.paho.mqttv5.client.*;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.client.persist.MqttDefaultFilePersistence;
import org.eclipse.paho.mqttv5.common.*;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Mqtt class represents mqtt client for subscribing to specified topics on mqtt broker.
 * Its main functionality is started automatically during spring application startup.
 *
 * @author Stefan Kapetanovic
 * @version 1.0
 * @since   2024-01-01
 */
@Component
public class Mqtt implements ApplicationListener<ContextRefreshedEvent> {
    private MqttConnectionOptions connOpts = new MqttConnectionOptions();
    private long MAX_SESSION_EXPIRATION_INTERVAL = 4294967295L;

    /**
     * Temperature topic name.
     */
    @Value("${mqtt.TEMP_TOPIC}")
    private String TEMP_TOPIC;
    /**
     * Load topic name.
     */
    @Value("${mqtt.LOAD_TOPIC}")
    private String LOAD_TOPIC;
    /**
     * Fuel topic name.
     */
    @Value("${mqtt.FUEL_TOPIC}")
    private String FUEL_TOPIC;
    /**
     * Stats topic name.
     */
    @Value("${mqtt.STATS_TOPIC}")
    private String STATS_TOPIC;

    /**
     * Quality of service level for subscriber packets.
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
    @Value("${mqtt.clientId}")
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

    /**
     * Contains logic for managing received sensor data.
     */
    private DataService dataService;
    /**
     * Contains logic for managing received aggregated data stats.
     */
    private StatsService statsService;

    /**
     * Persistence for subscriber.
     */
    MemoryPersistence persistence = new MemoryPersistence();

    /**
     * Used for mapping json to specific objects.
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Array of listeners for every predefined topic.
     */
    private final IMqttMessageListener[] listeners = {
            (topic, mqttMessage) -> {
                System.out.println("CLOUD TEMP: " + new String(mqttMessage.getPayload()));
                MqttData mqttData = objectMapper.readValue(new String(mqttMessage.getPayload()), MqttData.class);
                mqttData.payload.setType(DataType.TEMPERATURE.name());
                dataService.receiveMqtt(mqttData.username, mqttData.payload);
            },
            (topic, mqttMessage) -> {
                System.out.println("CLOUD LOAD: " + new String(mqttMessage.getPayload()));
                MqttData mqttData = objectMapper.readValue(new String(mqttMessage.getPayload()), MqttData.class);
                mqttData.payload.setType(DataType.LOAD.name());
                dataService.receiveMqtt(mqttData.username, mqttData.payload);
            },
            (topic, mqttMessage) -> {
                System.out.println("CLOUD FUEL: " + new String(mqttMessage.getPayload()));
                MqttData mqttData = objectMapper.readValue(new String(mqttMessage.getPayload()), MqttData.class);
                mqttData.payload.setType(DataType.FUEL_LEVEL.name());
                dataService.receiveMqtt(mqttData.username, mqttData.payload);
            },
            (topic, mqttMessage) -> {
                System.out.println("CLOUD STATS: " + new String(mqttMessage.getPayload()));
                MqttStats mqttStats = objectMapper.readValue(new String(mqttMessage.getPayload()), MqttStats.class);
                statsService.receiveMqtt(mqttStats.username, mqttStats.payload);
            }
    };

    /**
     * Class constructor.
     *
     * @param dataService Object implementing DataService interface.
     * @param statsService Object implementing StatsService interface.
     */
    public Mqtt(DataService dataService, StatsService statsService) {
        this.dataService = dataService;
        this.statsService = statsService;
    }

    /**
     * This method calls the main logic of Mqtt class on spring startup.
     *
     * @param event Object representing event when application context changes.
     */
    @Override public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            this.subscribe();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Main logic of Mqtt class. Initializes all mqtt parameters, connects to mqtt broker and
     * subscribes to predefined topics.
     */
    private void subscribe() throws MqttException {
        MqttConnectionOptionsBuilder options = new MqttConnectionOptionsBuilder();
        options.username(username);
        options.password(password.getBytes());
        options.serverURI(broker);
        options.automaticReconnect(true);
        options.keepAliveInterval(8*60*60);
        // This is because MQTTv5 requires this property to be set, along with
        // 'cleanStart', in order to emulate session persistence from MQTTv3.
        options.cleanStart(false);
        options.sessionExpiryInterval(MAX_SESSION_EXPIRATION_INTERVAL);

        MqttAsyncClient client = new MqttAsyncClient(broker, clientId, new MqttDefaultFilePersistence());
        System.out.println("Connecting to broker: " + broker);
        IMqttToken token = client.connect(options.build());
        token.waitForCompletion();
        System.out.println("Connected");

        MqttSubscription[] subscriptions = {
            new MqttSubscription(TEMP_TOPIC, qos),
            new MqttSubscription(LOAD_TOPIC, qos),
            new MqttSubscription(FUEL_TOPIC, qos),
            new MqttSubscription(STATS_TOPIC, qos)
        };

        // Workaround for a bug that is still present (indexes zero length array at index 0)
        final MqttProperties props = new MqttProperties();
        props.setSubscriptionIdentifiers(Arrays.asList(new Integer[] { 0 }));
        client.subscribe(subscriptions, null, null, listeners, props);
    }
}