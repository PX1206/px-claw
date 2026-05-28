package com.claw.system.util;

import com.claw.common.tool.StringUtil;

/**
 * Excel 话术行与 faq.txt 行格式一致：问题 → 话术 → 补充（非空格连接）。
 */
public final class ChatKnowledgeMerger {

    private ChatKnowledgeMerger() {}

    public static String mergeCells(String question, String scriptText, String supplement) {
        StringBuilder sb = new StringBuilder();
        for (String p : new String[]{question, scriptText, supplement}) {
            if (StringUtil.isNotBlank(p)) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(p.trim());
            }
        }
        String s = sb.toString().replace('\r', ' ').replace('\n', ' ').trim();
        return StringUtil.isBlank(s) ? null : s;
    }
}
