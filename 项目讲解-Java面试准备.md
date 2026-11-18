# 会议室预约系统 · Java 项目保姆级讲解（面试准备版）

> 面向对象：没接触过 Java 后端、只有一点前端基础的你
> 目标：把这个项目讲透，面试官问任何一个点都能答上来
> 建议：先通读 1 遍，再用"高频面试 Q&A"那节自测

---

## 一、先认识：这个项目到底是做什么的？

一句话：**它是一个"网页版会议室预约系统"**，就像订酒店一样，同事在网页上选会议室、选时间，提交预约，别人就约不走了。

它有 3 个功能点：
1. **选会议室** —— 星辰厅(3F)、创新厅(5F)、灵感厅(2F)
2. **选时间段预约** —— 选日期，点时间格子，填表单提交
3. **冲突检测** —— 同一个会议室、同一个时间段，不能被两个人同时约

**为什么这是一个好的面试项目？** 因为它虽然小，但**五脏俱全**：
- 有真正的"前端 + 后端"完整分层（你会前端，能讲前端部分）
- 有**数据库**（虽然用的是 JSON 文件，但能讲出"数据持久化"概念）
- 有**业务逻辑**（冲突检测 = 核心算法点）
- 有**并发考虑**（`synchronized` 关键字）
- 有**配置、打包、启动**全流程（Maven 打包 → 打 jar → 运行）

---

## 二、技术栈总览（先记住这几个名字，下面逐个解释）

| 名称 | 干什么的（一句话） | 比喻 |
|------|-------------------|------|
| **Java 11** | 编程语言版本，写后端代码 | 写菜谱的语言 |
| **Spring Boot 2.7** | 快速搭 Java 后端的框架 | 一个"水电气装修全包"的厨房 |
| **Maven 3.5** | 项目管理/打包工具 | 装修包工头 |
| **REST API** | 前后端约定的"接口规范" | 餐厅的点菜单 |
| **JSON** | 数据交换格式 | 服务员的"点菜单"写法 |
| **内嵌 Tomcat** | 内置的 web 服务器 | 厨房里自带的服务窗口 |
| **JDK 11** | Java 运行环境 | 你电脑里的"烹饪系统" |
| **Jackson** | 把 Java 对象转成 JSON 的工具 | 翻译官 |
| **synchronized** | 并发锁，防同时改数据出错 | 收银台的"排队" |

---

## 三、逐个名词解释（面试常问，必须会）

### 1. Java 是什么？
一种**编译型**编程语言。写的是 `.java` 文件，需要先**编译**成 `.class`（字节码），再由 Java 虚拟机(JVM)运行。跨平台：Windows/Mac/Linux 都能跑。

> 和前端 JS 的区别：JS 是**解释执行**（浏览器直接跑），Java 是**先编译再执行**。所以 Java 项目需要"打包"步骤。

### 2. Spring Boot 是什么？（最重要，必问）
**Spring** 是一个超大的 Java 框架，功能多但配置繁琐。**Spring Boot** 是在 Spring 之上的"封装"，让你**几乎不用配置**就能跑起来。

它帮你做了 4 件大事：
- **内嵌服务器**：把 Tomcat 打包进项目，`java -jar` 一跑就有网站，不用单独装服务器
- **自动配置**：你告诉它"我要做 web 应用"，它自动配好一堆东西
- **起步依赖**：`spring-boot-starter-web` 一句话就引入整组库
- **约定优于配置**：默认规则，少写配置

> 比喻：Spring 像一台"能装你所有要求"的复杂机器，Spring Boot 是"按一下电源就自己装好并开机"的傻瓜机。它俩关系 = 儿子(Spring Boot) 继承了 爸爸(Spring) 的全部能力，还帮你省事。

### 3. Maven 是什么？
**项目管理工具**，负责两件事：
- **依赖管理**：你想要的第三方库（比如 Spring Boot），在 `pom.xml` 里写一行，Maven 自动去网上下载并引入。
- **构建打包**：一条 `mvn package` 命令，把源码编译成 `.jar` 可执行文件。

### 4. 什么是 jar 文件？
Java 的**可执行压缩包**，里面装着你编译好的代码 + 资源。命令行 `java -jar xxx.jar` 就能直接运行整个应用。你可以把 jar 理解成"一个完整的、能独立运行的软件程序"。

### 5. REST API（接口）是什么？
**前后端通信的约定规则**。前端通过 **HTTP 请求**（`fetch`）访问后端的网址，后端返回数据。核心是 **4 种操作方法**：

| HTTP 方法 | 含义 | 本项目用法 |
|-----------|------|-----------|
| GET | 查（读） | 查会议室列表、查预约列表 |
| POST | 增（写） | 创建预约 |
| DELETE | 删 | 取消预约 |

**URL 风格**：`/api/bookings` 这种，`/api/` 表示是接口，后面是资源名。

### 6. JSON 是什么？
一种**键值对**的文本格式，前后端用它传数据。例：
```json
{"roomId":"room-001","startTime":"10:00","endTime":"11:00","title":"周例会"}
```
前端发 JSON 给后端，后端返回 JSON 给前端。**前后端不认识对方，靠 JSON 沟通**。

### 7. 什么是"数据持久化"？
内存里的数据，程序一关就没了。**持久化** = 把数据**保存到磁盘**（文件/数据库），程序重启还在。本项目把预约数据写进 `data/bookings.json` 文件 —— 这就是持久化。面试问"你的数据存在哪"，答"用 JSON 文件持久化，重启不丢"。

### 8. 什么是 MVC / 分层架构？
一种**代码分层**思想，把功能拆成几层，各管各的：

| 层 | 角色 | 本项目对应 |
|----|------|----------|
| **Model（模型）** | 数据长什么样 | `Room`、`Booking` 实体类 |
| **Controller（控制器）** | 接收请求、返回响应 | `MeetingController` |
| **Service（服务层）** | 业务逻辑 | `MeetingService` |
| **View（视图）** | 页面 | `index.html` + CSS/JS |

**面试标准答案**："我的项目采用分层架构，Controller 只负责接收和返回，业务逻辑放 Service 层，实体类负责数据建模，职责单一、便于维护。"

### 9. 什么是"依赖注入"（Spring 核心）？
**Spring 自动帮你创建对象，并"送"到你需要的类里**，你不需要手动 `new`。
例：`MeetingController` 需要一个 `MeetingService`，Spring 看到构造器参数，**自动创建一个 MeetingService 实例注入进来**。这就是 **IOC（控制反转）/ DI（依赖注入）**。

### 10. 什么是 Bean？
Spring 管理的对象叫 **Bean**。凡是被 `@Component`、`@Service` 等标注的类，Spring 都会创建成 Bean 并管理它的生命周期。

### 11. 什么是 REST？和 API 什么关系？
**REST** 是一种接口**设计风格**（用 HTTP 方法表达增删改查、URL 表达资源）。
**API** = 接口。所以 "REST API" = "遵循 REST 风格的接口"。本项目就是 REST API。

### 12. 什么是 Tomcat？
一个 **Web 服务器程序**，负责**接收浏览器发来的 HTTP 请求，转给我们的代码处理，再把结果返回**。Spring Boot 把它**内嵌**在项目里了，所以你不装它也能跑。

### 13. 什么是"状态码"？
HTTP 响应的数字，表示结果：
- **200** = 成功
- **400** = 请求有问题（参数错误）
- **404** = 找不到资源
本项目返回：成功 code=0，参数错误 400，删除不存在 404。

### 14. 什么是 synchronized（并发）？
多个人同时操作同一份数据会"打架"。**synchronized 方法 = 同时只能有一个线程执行**，其他人排队。本项目用它保证"两个请求同时预约时，冲突检测不会出错"。

---

## 四、项目目录结构 —— 每个文件逐个标注

下面把 `会议室预约系统-java/` 目录**每个文件**标出来，说明它是干什么的。

```
会议室预约系统-java/
├── pom.xml                     ← Maven 的"项目说明书"，声明用哪些依赖、Java版本、怎么打包
├── start.bat                   ← 双击启动脚本，自动 打包 + 启动服务（Windows 专用）
├── data/                       ← 运行时自动生成的数据目录
│   └── bookings.json           ← ★ 所有预约记录的"数据库"（JSON 文件持久化）
│
├── target/                     ← 构建产物目录（mvn package 后自动生成，不用管）
│   └── meeting-room-reservation-1.0.0.jar   ← ★ 最终可执行文件（java -jar 运行它）
│
└── src/                        ← 源代码目录
    └── main/
        ├── java/               ← Java 源码
        │   └── com/example/meeting/
        │       ├── MeetingRoomApplication.java   ← 启动入口（main 方法，run 一跑项目就启动）
        │       ├── controller/                   ← 控制层：接收 HTTP 请求，调 Service
        │       │   ├── MeetingController.java    ← ★ 定义所有 REST 接口
        │       │   └── ApiResponse.java          ← 统一返回格式：{code, message, data}
        │       ├── service/                      ← 业务层：核心逻辑在这
        │       │   ├── MeetingService.java       ← ★ 预约/取消/冲突检测/JSON持久化
        │       │   └── AppProperties.java        ← 读取配置文件里 app.* 的值
        │       └── model/                        ← 实体类：描述"数据长什么样"
        │           ├── Room.java                 ← ★ 会议室实体（id/名称/楼层/位置/容量）
        │           └── Booking.java              ← ★ 预约记录实体（id/日期/起止时间/主题/预约人...）
        │
        └── resources/          ← 资源文件（配置文件 + 静态页面）
            ├── application.yml ← ★ 配置文件（端口、开放时间、数据目录）
            └── static/         ← ★ 前端页面（浏览器直接访问这些文件）
                ├── index.html  ← 前端主页面（框架：标题/会议室tab/时间格/弹窗）
                ├── css/style.css ← 前端样式（美化 + 修复 hidden 属性的那行）
                └── js/app.js   ← ★ 前端逻辑（发请求、渲染时间格、弹窗开关）
```

### 逐文件的"人话"解释

| 文件 | 用一句话讲清楚它是干什么的 |
|------|---------------------------|
| `pom.xml` | 告诉 Maven"我用了 Spring Boot、Java 11，帮我下载这些库并打包" |
| `MeetingRoomApplication.java` | **程序的入口**。Spring Boot 的启动器，运行它整个项目就活了（它有个 `@SpringBootApplication` 注解 + main 方法） |
| `MeetingRoomController.java` | **门口的服务员**。接收前端发来的请求（GET/POST/DELETE），叫 Service 干活的 |
| `ApiResponse.java` | **统一信封**。所有接口返回都装成 `{code, message, data}` 这个信封，前端好判断成功失败 |
| `MeetingService.java` | **核心大脑**。真正的业务：校验参数 → 冲突检测 → 保存到 JSON |
| `AppProperties.java` | **配置小管家**。把 `application.yml` 里的 `app.开放时间` 等值读进 Java 代码里用 |
| `Room.java` | 会议室"数据模板"。定义会议室有哪些字段（名称、楼层...） |
| `Booking.java` | 预约"数据模板"。定义一条预约有哪些字段（日期、起止、主题...） |
| `application.yml` | 配置文件。改端口、改开放时间都在这里 |
| `index.html` | 网页的骨架（房间结构） |
| `style.css` | 网页的样式（装修） |
| `app.js` | 网页的大脑（动态逻辑：请求数据、画格子、开弹窗） |
| `bookings.json` | **预约记录的"存储仓库"**，所有已约的数据存在这 |

---

## 五、项目整体运行逻辑（画一遍流程图）

### ① 用户打开页面（前端）
```
浏览器 输入 http://localhost:8080
  → 后端返回 index.html + style.css + app.js
  → 页面出现：标题栏、3个会议室Tab、时间格子、日期选择
```

### ② 页面加载时，前端自动请求数据
```
app.js 里 init() 启动
  → fetch /api/config    拿到开放时间(9:00-21:00)
  → fetch /api/rooms     拿到3个会议室
  → 默认选中星辰厅，fetch /api/bookings?roomId=room-001&date=今天
  → 拿到已有预约，渲染成时间格子里的蓝色卡片
```

### ③ 用户点空白格子 → 弹窗填表单 → 提交
```
用户点击空白格
  → app.js 弹开"新建预约"弹窗，自动填好起止时间
  → 用户填：会议主题、预约人、电话、备注
  → 点"提交预约"
  → app.js 发 POST /api/bookings，带 JSON 数据
```

### ④ 后端收到请求 → 处理 → 返回
```
Spring Boot(内嵌Tomcat) 收到 POST /api/bookings
  → 路由到 MeetingController.createBooking()
  → Controller 调用 MeetingService.createBooking()
  → Service 里：
        ① 校验参数（必填项、时间格式、结束>开始）
        ② 冲突检测（同会议室同日，时间是否重叠）★核心
        ③ 没有冲突 → 生成id → 加入内存List → 写进 bookings.json
  → 返回 {code:0, data:{预约数据}}
  → 前端收到，刷新格子，蓝色卡片出现
```

### ⑤ 删除预约（类似）
```
用户点已约卡片 → 弹"预约详情" → 点"取消预约"
  → DELETE /api/bookings/{id}
  → Controller → Service 删除 → 更新 JSON
  → 前端刷新，卡片消失
```

---

## 六、核心代码逻辑逐层精讲（面试深度）

### 6.1 启动类 `MeetingRoomApplication.java`
```java
@SpringBootApplication   // 告诉 Spring Boot：这里启动应用，自动配置+自动扫描
public class MeetingRoomApplication {
    public static void main(String[] args) {
        SpringApplication.run(MeetingRoomApplication.class, args);
        // 一句 run() 就把整个应用拉起来了
    }
}
```
**面试点**：`@SpringBootApplication` 是一个**组合注解** = `@SpringBootConfiguration` + `@EnableAutoConfiguration`（开启自动配置）+ `@ComponentScan`（扫描包下的组件）。

### 6.2 控制层 `MeetingController.java`
```java
@RestController                      // = Controller + ResponseBody，返回JSON
@RequestMapping("/api")              // 所有接口以 /api 开头
public class MeetingController {

    @GetMapping("/rooms")            // 前端 GET /api/rooms
    public ApiResponse<List<Room>> listRooms() { ... }

    @PostMapping("/bookings")        // 前端 POST /api/bookings
    public ApiResponse<Booking> createBooking(@RequestBody Booking request) { ... }

    @DeleteMapping("/bookings/{id}") // 前端 DELETE /api/bookings/xxx
    public ApiResponse<Boolean> deleteBooking(@PathVariable String id) { ... }
}
```
**面试点**：
- `@RestController` = `@Controller` + `@ResponseBody`，返回结果自动转 JSON
- `@RequestBody`：把前端发的 JSON **反序列化**成 Java 的 Booking 对象
- `@PathVariable`：把 URL 里的 `{id}` 取出来

### 6.3 业务层 `MeetingService.java`（最重点）
核心是**冲突检测**这一段，面试一定会问：
```java
// 判断"新预约"和"已存在的预约"时间段是否重叠
for (Booking b : bookings) {
    boolean sameRoom = b.getRoomId().equals(roomId);   // 同会议室
    boolean sameDate = b.getDate().equals(date);       // 同日期
    boolean overlap = startTime.compareTo(b.getEndTime()) < 0
                    && endTime.compareTo(b.getStartTime()) > 0;  // ★ 时间段重叠
    if (sameRoom && sameDate && overlap) {
        throw new IllegalArgumentException("该时间段已被预约...");
    }
}
```
**重叠判断原理**：两个时间段 `[start1,end1]` 和 `[start2,end2]` 重叠的条件是：
```
start1 < end2  且  end1 > start2
```
（各自开始时间早于对方结束时间，就一定有交集）

**面试点**：为什么能对？因为时间都存成 `HH:mm` 字符串，**格式统一，字符串比较就相当于时间比较**（10:00 < 11:00）。

### 6.4 持久化 `save()`
```java
objectMapper.writerWithDefaultPrettyPrinter().writeValue(dataFile, bookings);
```
**意思**：用 Jackson 把内存里的 List 转成 JSON 文本，写进 `bookings.json`。启动时 `init()` 再读回来。

---

## 七、高频面试题 + 标准答案（直接背）

**Q1: 这个项目用到了哪些技术？**
答：Spring Boot 2.7 构建 REST API 后端，Java 11 开发，Maven 打包，前端原生 HTML/CSS/JS 调用接口，数据用 JSON 文件持久化，用 synchronized 保证并发安全。

**Q2: Spring Boot 和 Spring 的区别？**
答：Spring 是基础框架，Spring Boot 在它之上做了**自动配置、内嵌服务器、起步依赖**，让开发更省事，开箱即用。

**Q3: 你的项目数据存在哪？为什么不用数据库？**
答：存在 JSON 文件里（`bookings.json`），属于**文件持久化**。选它是为了轻量、无需额外安装数据库、部署简单；但**生产环境一般会用 MySQL**，把 Service 里的读写换成 JDBC 即可（这是可扩展点，回答时点一下"如果数据量大我会用 MySQL"）。

**Q4: 冲突检测怎么实现的？**
答：遍历当天该会议室的已有预约，用"开始时间 < 对方结束时间 && 结束时间 > 对方开始时间"判断是否重叠，重叠就抛异常拒绝。时间统一存成 HH:mm 字符串，用 compareTo 比较。（说清楚上面那段代码即可）

**Q5: 为什么用 synchronized？**
答：因为多用户可能同时预约同一个时间，`synchronized` 保证同一时刻只有一个线程执行"校验+保存"，避免两个请求同时通过导致数据不一致。

**Q6: @RestController 和 @Controller 区别？**
答：@RestController  = @Controller + @ResponseBody，默认把方法返回值转成 JSON 响应；@Controller 返回的是视图名（页面）。

**Q7: 什么是依赖注入？你的项目哪里用到了？**
答：由 Spring 自动创建对象并注入到需要的类，而不是自己 new。本项目 `MeetingController` 构造器接收 `MeetingService`，Spring 自动把它注入进来。

**Q8: 启动流程？**
答：双击 start.bat → mvn 打包成 jar → java -jar 启动 → Spring Boot 内嵌 Tomcat 在 8080 端口接收请求 → 浏览器访问。

---

## 八、面试"能讲 / 需要谦虚"的边界（重要！）

✅ **可以自信讲的**（真实实现了）：
- Spring Boot 项目结构、启动
- REST 接口 增删查
- 冲突检测算法
- 数据持久化（JSON 文件）
- 前后端如何 fetch 交互（你前端的强项）

⚠️ **一定不要讲的**（没实现，讲了会被追问穿帮）：
- ❌ MyBatis / JPA / 数据库操作（本项目没用数据库）
- ❌ Redis 缓存
- ❌ 分布式、微服务
- ❌ JWT 登录鉴权

**面试防穿帮**：如果面试官问"你数据存哪"，就诚实说"为了简化用 JSON 文件"，**别吹用了数据库**。讲清真实实现，比吹牛更稳。

---

## 九、一分钟"项目介绍"速记版（面试开场）

> "这是一个**会议室预约系统**。后端用 **Spring Boot 2.7** + Java 11，前端是原生 HTML/CSS/JS。功能包括**选会议室、选时间段预约、冲突检测、取消预约**。我采用**分层架构**：Controller 层接收 HTTP 请求，Service 层处理业务和冲突检测，Model 层定义数据实体，数据用 JSON 文件持久化。核心的冲突检测我用了'时间段重叠判断'算法，并用 synchronized 保证并发安全。整个项目用 Maven 打包成 jar，一键启动。"

---

*祝你面试顺利！把"第六、七节"背熟，其余理解即可。*

> AI生成
