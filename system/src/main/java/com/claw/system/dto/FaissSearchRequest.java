package com.claw.system.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class FaissSearchRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String query;

    @JsonProperty("top_k")
    private Integer topK;

    /** 话术归属用户：与 ~/.px-claw/faq_{owner_user_id}.txt 对应；不传则走旧版单文件索引 */
    @JsonProperty("owner_user_id")
    private Long ownerUserId;
}
