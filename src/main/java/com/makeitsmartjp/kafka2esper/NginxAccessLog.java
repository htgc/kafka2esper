package com.makeitsmartjp.kafka2esper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class NginxAccessLog {
	private String	remote_ip;
	private int		status;
	private double	upstream_response;
}
