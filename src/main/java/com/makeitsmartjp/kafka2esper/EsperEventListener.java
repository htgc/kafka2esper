package com.makeitsmartjp.kafka2esper;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public class EsperEventListener implements UpdateListener {

	@Override
	public void update(EventBean[] newEvents, EventBean[] oldEvents) {
		EventBean event = newEvents[0];
		System.out.println("EventUpdate: " + event.getUnderlying());
		try {
			String avg_response = event.get("avg(upstream_response)").toString();
			String max_response = event.get("avg(upstream_response)").toString();
			System.out.println("Avg. UpstreamResponseTime: " + avg_response);
			System.out.println("Max. UpstreamResponseTime: " + max_response);
		} catch (Exception e) {
			System.out.println("EventListener Exception: " + e);
		}
	}

}
