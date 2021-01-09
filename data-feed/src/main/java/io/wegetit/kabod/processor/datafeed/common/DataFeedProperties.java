package io.wegetit.kabod.processor.datafeed.common;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DataFeedProperties {
    @Min(value = 0)
    private int minRows = 0;
    @Min(value = 1)
    private int maxRows = 1000;
    @Min(value = 0)
    private int fileCount = 0;
    @NotBlank
    private String prefix;
    @NotBlank
    private String destination;
    @Min(0)
    @NotNull
    private Integer initialDelay;
    @Min(100)
    @NotNull
    private Integer fixedDelay;
}
