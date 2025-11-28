package com.hacom.orderprocessing.infrastructure.smpp;

import com.cloudhopper.smpp.SmppBindType;
import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.impl.DefaultSmppClient;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.type.Address;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SmppSmsSender implements SmsSender {

    @Value("${smpp.host}")
    private String host;

    @Value("${smpp.port}")
    private int port;

    @Value("${smpp.systemId}")
    private String systemId;

    @Value("${smpp.password}")
    private String password;

    @Value("${smpp.source}")
    private String source;
    
    @Value("${smpp.enabled:true}")
    private boolean enabled;

    private DefaultSmppClient smppClient;
    private SmppSession session;

    private SmppSession getSession() {
        if (session != null && session.isBound()) {
            return session;
        }

        try {
            smppClient = new DefaultSmppClient();

            SmppSessionConfiguration config = new SmppSessionConfiguration();
            config.setName("client");
            config.setHost(host);
            config.setPort(port);
            config.setSystemId(systemId);
            config.setPassword(password);
            config.setType(SmppBindType.TRANSCEIVER);

            session = smppClient.bind(config);
            log.info("SMPP session established");
        } catch (Exception e) {
            log.error("Failed to connect SMPP", e);
        }

        return session;
    }

    @Override
    public void sendSms(String phoneNumber, String message) {
    	if (!enabled) {
            log.info("(MOCK SMS - disabled) -> {} : {}", phoneNumber, message);
            return;
        }
    	
        try {
            SmppSession session = getSession();
            if (session == null) {
                log.warn("SMPP session not available — SMS NOT sent. (mock fallback)");
                log.info("(MOCK SMS - no session) -> {} : {}", phoneNumber, message);
                return;
            }

            SubmitSm sm = new SubmitSm();
            sm.setSourceAddress(new Address((byte) 0x03, (byte) 0x00, source));
            sm.setDestAddress(new Address((byte) 0x01, (byte) 0x01, phoneNumber));
            sm.setShortMessage(message.getBytes());

            session.submit(sm, 10000);
            log.info("SMS sent to {}", phoneNumber);

        } catch (Exception e) {
        	log.warn("SMPP send failed — switching to mock mode: {}", e.getMessage());
            log.info("(MOCK SMS - exception) -> {} : {}", phoneNumber, message);
        }
    }
}