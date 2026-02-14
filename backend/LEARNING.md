# Java 9+ 新特性与 Agent 开发学习指南

本文件汇总项目中使用的 JDK 9+ 新特性及 Agent/LLM 相关概念，便于边写边学。

---

## 一、JDK 9+ 新特性

### 1. var（局部变量类型推断，JDK 10）

```java
// 旧写法
List<Map<String, Object>> messages = List.of(...);

// 新写法：var 让编译器根据右侧推断类型
var messages = List.of(...);
var body = Map.of("model", "gpt-3.5-turbo", "messages", messages);
```

- 仅用于局部变量，不能用于方法参数、返回值、字段
- 右侧必须有明确的类型信息，否则编译器无法推断

### 2. 文本块 Text Blocks（JDK 15，预览；JDK 21 正式）

```java
// 旧写法：多行字符串用 + 拼接
String content = "你是面向幼儿园教师的智能助手，\n" +
                 "专门帮助老师完成教育教学、班级管理。\n" +
                 "回答时请专业、实用。";

// 新写法：""" """ 文本块
String content = """
    你是面向幼儿园教师的智能助手，专门帮助老师完成教育教学、班级管理。
    回答时请专业、实用，符合幼教场景。
    """;
```

- 以 `"""` 开始，以 `"""` 结束
- 自动处理换行和缩进，末尾换行可省略
- 适合写长文本、JSON、HTML 等

### 3. Record（不可变数据类，JDK 16）

```java
// 旧写法：POJO
public final class ChatRequest {
    private final String message;
    public ChatRequest(String message) { this.message = message; }
    public String getMessage() { return message; }
    @Override public boolean equals(Object o) { ... }
    @Override public int hashCode() { ... }
    @Override public String toString() { ... }
}

// 新写法：Record
public record ChatRequest(String message, String docTypeId) {
    // 紧凑构造器：在赋值后执行，用于校验
    public ChatRequest {
        if (message == null || message.isBlank())
            throw new IllegalArgumentException("message 不能为空");
    }
}
```

- Record 自动生成：全参构造器、getter（如 `message()`）、equals、hashCode、toString
- 紧凑构造器 `public ChatRequest { ... }` 不写参数列表，在自动赋值后执行
- 适合 DTO、配置类、不可变数据载体

### 4. 模式匹配 for instanceof（JDK 16）

```java
// 旧写法
if (obj instanceof String) {
    String s = (String) obj;
    System.out.println(s.length());
}

// 新写法：变量 s 自动完成类型转换，限定在 if 块内
if (obj instanceof String s) {
    System.out.println(s.length());
}
```

- 在 instanceof 后直接声明变量，类型转换自动完成
- 变量作用域限定在 if 块内

### 5. switch 表达式（JDK 14，预览；JDK 21 正式）

```java
// 旧写法
String result;
switch (x) {
    case 1: result = "one"; break;
    case 2: result = "two"; break;
    default: result = "other";
}

// 新写法：switch 有返回值
String result = switch (x) {
    case 1 -> "one";
    case 2 -> "two";
    default -> "other";
};
```

- 使用 `->` 替代 `:` + `break`
- 整个 switch 是一个表达式，可赋值给变量
- 也可用 `yield` 返回值：`case 1: yield "one";`

### 6. List.of / Map.of（JDK 9）

```java
// 旧写法
List<String> list = new ArrayList<>();
list.add("a"); list.add("b");

Map<String, Object> map = new HashMap<>();
map.put("key", "value");

// 新写法：不可变集合
List<String> list = List.of("a", "b");
Map<String, Object> map = Map.of("key", "value", "model", "gpt-3.5-turbo");
```

- `List.of`、`Map.of`、`Set.of` 创建不可变集合
- 不能增删改，适合配置、常量、简单数据

### 7. Mono / Flux（Reactor，与 Spring WebFlux 一同使用）

```java
// Mono：0 或 1 个元素的异步流，类似 Optional 的异步版
Mono<String> result = llmService.chat("你好");

// Flux：0 到 N 个元素的异步流，用于流式响应
Flux<String> stream = llmService.chatStream("你好");
stream.subscribe(chunk -> System.out.print(chunk));
```

- 响应式编程：数据以流的形式异步处理，不阻塞线程
- `Mono` 用于单次结果，`Flux` 用于多次/流式结果

---

## 二、Agent / LLM 相关概念

### 1. LLM（Large Language Model）

大语言模型，如 GPT、通义千问、文心一言等。输入文本，输出文本。

### 2. Prompt

发给模型的「指令 + 上下文 + 用户输入」。模型据此生成回复。

- **System Prompt**：定义 AI 的角色、行为、约束
- **User Message**：用户本轮输入

### 3. Token

模型处理文本的基本单位。中文约 1–2 字/token，英文约 4 字符/token。API 通常按 token 计费。

### 4. 流式（Streaming）

逐 token 返回，用户能边看边等，体验更好。OpenAI 等 API 支持 `stream: true`，返回 SSE（Server-Sent Events）格式。

### 5. Agent

在本项目中指：接收用户输入 → 转发给 LLM → 返回生成结果的智能体。后续可扩展为：工具调用、RAG 检索、多轮对话等。

---

## 三、本项目中的用法示例

| 文件 | 使用的特性 |
|------|-----------|
| `ChatRequest.java` | Record、紧凑构造器 |
| `LlmService.java` | var、文本块、Map.of、Mono/Flux |
| `LlmProperties.java` | Record、@ConfigurationProperties |
| `ChatController.java` | Mono、链式调用 |
| `application.yml` | Spring Boot 配置绑定 |
