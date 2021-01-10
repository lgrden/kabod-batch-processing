package io.wegetit.kabod.processor.config;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DataProcessorProperties {
    @NotBlank
    private String prefix;
    @NotBlank
    private String source;
    @NotBlank
    private String destination;
    @Min(0)
    @NotNull
    private Integer initialDelay;
    @Min(100)
    @NotNull
    private Integer fixedDelay;
    @Min(100)
    @NotNull
    private Integer batchSize;
}
