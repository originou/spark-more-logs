package io.dma.client.payload;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
public class StreamingPayload<T> {
  private String appName;
  private String jobId;
  private EventType eventType;
  private List<T> data;
}
