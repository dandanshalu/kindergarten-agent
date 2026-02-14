package com.kindergarten;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 幼儿园老师 Agent 应用入口。
 * 
 * Agent 小知识：Agent 在此处指「能根据用户输入调用大模型并返回结果的智能体」，
 * 本应用就是一个最简单的 Agent 形态——用户发消息 -> 转发给 LLM -> 返回生成结果。
 */
@SpringBootApplication
public class KindergartenAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(KindergartenAgentApplication.class, args);
    }
}
