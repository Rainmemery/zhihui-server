package com.zhihui.enums;

import com.zhihui.common.exception.BusinessException;
import com.zhihui.common.exception.ErrorCode;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * 会议状态枚举 — 内置状态转换规则
 *
 * 状态流转规则：
 *   DRAFT      → SCHEDULED / CANCELLED
 *   SCHEDULED  → IN_PROGRESS / CANCELLED
 *   IN_PROGRESS → COMPLETED
 *   COMPLETED  → (终态，不可转换)
 *   CANCELLED  → (终态，不可转换)
 */
public enum MeetingStatus {

    DRAFT("DRAFT", "草稿") {
        @Override
        public Set<MeetingStatus> allowedTransitions() {
            return EnumSet.of(SCHEDULED, CANCELLED);
        }
    },
    SCHEDULED("SCHEDULED", "已排期") {
        @Override
        public Set<MeetingStatus> allowedTransitions() {
            return EnumSet.of(IN_PROGRESS, CANCELLED);
        }
    },
    IN_PROGRESS("IN_PROGRESS", "进行中") {
        @Override
        public Set<MeetingStatus> allowedTransitions() {
            return EnumSet.of(COMPLETED);
        }
    },
    COMPLETED("COMPLETED", "已完成") {
        @Override
        public Set<MeetingStatus> allowedTransitions() {
            return Collections.emptySet();
        }
    },
    CANCELLED("CANCELLED", "已取消") {
        @Override
        public Set<MeetingStatus> allowedTransitions() {
            return Collections.emptySet();
        }
    };

    private final String code;
    private final String desc;

    MeetingStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() { return code; }
    public String getDesc() { return desc; }

    /** 每个状态允许转换到的目标状态集合 */
    public abstract Set<MeetingStatus> allowedTransitions();

    /** 判断是否可以转换到目标状态 */
    public boolean canTransitionTo(MeetingStatus target) {
        return allowedTransitions().contains(target);
    }

    /** 是否终态 */
    public boolean isTerminal() {
        return allowedTransitions().isEmpty();
    }

    /** 校验状态转换，非法则抛出异常 */
    public void validateTransition(MeetingStatus target) {
        if (!canTransitionTo(target)) {
            throw new BusinessException(400,
                    String.format("不允许从「%s」转换到「%s」", this.desc, target.desc));
        }
    }

    /** 根据 code 反查枚举 */
    public static MeetingStatus fromCode(String code) {
        for (MeetingStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new BusinessException(400,
                "无效的会议状态: " + code);
    }

    /** 静态枚举缓存，避免每次遍历 values() */
    private static final MeetingStatus[] CACHE = values();
}