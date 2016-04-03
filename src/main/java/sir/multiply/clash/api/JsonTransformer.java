package sir.multiply.clash.api;

import spark.ResponseTransformer;

import sir.barchable.util.Json;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Map;

public class JsonTransformer implements ResponseTransformer {
	public String render(Object result) {
		try {
			return Json.toString(result);
		} catch (JsonProcessingException e) {
			return "Could not convert to JSON";
		}
	}
}
