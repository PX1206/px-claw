package com.claw.system.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("mail_imap_checkpoint")
public class MailImapCheckpoint implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId
    private Integer id;

    private String folder;

    private Long lastUid;
}
