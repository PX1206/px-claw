package com.claw.system.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FaissReloadRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** null 表示沿用单文件旧行为（兼容）；已迁移多租户时传登录用户 id */
    @JsonProperty("owner_user_id")
    private Long ownerUserId;
}
