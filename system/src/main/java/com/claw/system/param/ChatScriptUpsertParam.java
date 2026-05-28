package com.claw.system.param;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/** 话术新建 / 更新共用 */
@Data
public class ChatScriptUpsertParam implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "问题不能为空")
    @Size(max = 2048)
    private String question;

    @NotBlank(message = "话术不能为空")
    @Size(max = 65535)
    private String scriptText;

    @Size(max = 1024)
    private String supplement;
}
