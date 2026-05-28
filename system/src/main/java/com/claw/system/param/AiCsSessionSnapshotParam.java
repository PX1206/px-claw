package com.claw.system.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/** 桌面端同步「整会话」快照：按用户 + 客户端会话 id 幂等 upsert，并覆盖该会话下消息行 */
@Data
@ApiModel("AI客服会话快照")
public class AiCsSessionSnapshotParam implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank
    @Size(max = 80)
    @ApiModelProperty("桌面端会话 id，如 c_timestamp_random")
    private String clientSessionId;

    @Size(max = 512)
    private String title;

    @Size(max = 128)
    private String providerId;

    @ApiModelProperty("客户端 updatedAt 毫秒时间戳")
    private Long clientUpdatedAt;

    @NotNull
    @Valid
    private List<AiCsSnapshotMessageItem> messages;

    @Data
    @ApiModel("单条消息")
    public static class AiCsSnapshotMessageItem implements Serializable {
        private static final long serialVersionUID = 1L;

        /** user / assistant */
        @NotBlank
        @Size(max = 32)
        private String role;

        @NotBlank
        private String content;

        @ApiModelProperty("该轮用户问题对应的知识检索片段（assistant 行可为空）")
        private String ragContext;

        private Long createdAt;
    }
}
