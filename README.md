# AI代码生成平台

项目体验地址http://acgp.mingzang.online/

## 项目介绍

这是一套以 **AI 开发实战 + 后端架构设计** 为核心的项目，基于 Spring Boot 3 + LangChain4j + Vue 3 的 **AI 代码生成平台**

### 预期实现的 4 大核心能力

1）智能代码生成：用户输入需求描述，AI 自动分析并选择合适的生成策略，通过工具调用生成代码文件，采用流式输出让用户实时看到 AI 的执行过程。
![img.png](src/main/resources/static/pic/img.png)
2）可视化编辑：生成的应用将实时展示，可以进入编辑模式，自由选择网页元素并且和 AI 对话来快速修改页面，直到满意为止。
![img_1.png](src/main/resources/static/pic/img_1.png)
3）一键部署分享：可以将生成的应用一键部署到云端并自动截取封面图，获得可访问的地址进行分享，同时支持完整项目源码下载。
![img_2-1.png](src/main/resources/static/pic/img_2-1.png)
4）企业级管理：提供用户管理、应用管理、系统监控、业务指标监控等后台功能，管理员可以设置精选应用、监控 AI 调用情况和系统性能。
![img_2.png](src/main/resources/static/pic/img_2.png)
![img_3.png](src/main/resources/static/pic/img_3.png)
## 第一次提交 init - 基础代码和依赖整理

## 第二次提交 用户模块

    1、MybatisFlex生成数据模型
    2、用户注册接口、用户登录接口
    3、用户管理以及用户权限控制（AOP + 自定义注解）

## 第三次提交 AI生成应用模块

    1、实现 AI 应用生成（原生模式）
    2、门面模式
    3、SSE 流式输出

## 第三次提交 AI生成应用模块 2
代码优化 - 使用设计模式抽象代码
    ![img.png](src/main/resources/pic/img1.png)

    1、首先使用执行器模式，通过选择不同执行器来实现不同功能，再选择不同参数决定选择策略
    2、然后是策略模式，通过传过来的不同参数，决定使用不同方法策略完成任务
    3、模板方法模式是因为相似功能，但是方法参数不同造成的大量重复代码，因此通过模板方法模式，重用共同方法，重写不同方法

## 第四次提交 应用模块 - 建立完整的应用生命周期管理体系

    1、MybatisFlex生成数据库表对应的基础数据实体
    2、app相关的基础功能，包括不同角色的应用的增删改查等

## 第五次提交 应用模块2 - 建立完整的应用生命周期管理体系
应用生成 - 客户端、数据库和app应用的链接

    1、SSE 流式接口开发及优化 - 传入appId、message调用接口
    2、应用部署 - 接口生成的文件通过nginx自动部署并显示出来

## 第六次提交 对话历史模块1

    1、chat_history表及相关实体的生成
    2、通过游标查询查询对话历史

## 第七次提交 对话历史模块2

    1、通过给容器id分配AI Service实例来隔离会话，实现对话记忆
    2、Caffeine实现缓存AI Service实例

## 第八次提交 工程项目生成1

    1、配置推理流式模型，开发写文件工具
    2、支持 Vue 项目生成
    3、工具调用流式输出、统一消息格式、TokenStream流处理

## 第九次提交 工程项目生成2

    1、工程项目构建和浏览
    2、工程项目部署

## 第十次提交 功能扩展1 - 应用封面图生成

    1、selenium实现网页自动截图、COS云端存储
    2、ThreadLocal优化并发截图功能、定时任务清理本地临时截图文件

## 第十一次提交 功能扩展2 - 下载代码功能 + AI智能选择方案

    1、使用 Hutool 工具库的 ZipUtil 实现 ZIP 包压缩
    2、AI智能选择输出何种类型的文件类型

## 第十二次提交 可视化修改 - 工程项目增量修改

    1、通过策略模式 + 工厂模式 + Spring依赖注入优化具体工具类实现

## 第十三次提交 AI工作流1 - 核心工作流开发

    1、定义工作节点 + 工作流图应用工作节点
    2、图片收集节点开发 & 图片收集AI服务 & 工作节点开发
| 工作步骤 | 输入状态 | 输出状态 |
|------|--|------|
| 图片收集 | originalPrompt 原始提示词 | images 图片资源列表每一个图片都应该是对象结构（图片类别、描述、地址）图片类别：content 内容图片URLsillustration 插画图片URLsarchitecture 架构图URLlogo Logo图片URL |
| 提示词增强 | originalPrompt 原始提示؜词 images 图片资源 | enhancedPrompt 增强后的提示词，包含图片描述和引用 |
| 智能路由 | originalPromptenhancedPrompt 增强后的提示词 | generationType 生成类型 |
| 代码生成 | enhancedPromptgenerationType 生成类型images | generatedCodeDir 生成的代码目录 |
| 项目构建 | generatedCodeDir 生成的代码目录 | buildResultDir 构建成功的目录 |

## 第十四次提交 AI工作流2 - 真实节点构建

    1、图片收集节点、提示词增强节点等五个节点的业务开发 & 工作流使用工作节点
    2、利用LangGraph4j 工作流特性 - 条件边

## 第十四次提交 AI工作流3 - LangGraph4j 工作流特性实战

    1、用循环边实现质量检查、 图片收集优化（CompletableFuture并发）
    2、Flux实现流式输出、appservice中添加agent参数整合工作流

## 第十五次提交 系统优化1 - 性能优化 & 实时性优化

    1、AI并发调用问题 - 通过chatModel多例模式解决：@Scope("prototype")
    2、Redis缓存优化，通过Redis缓存注解 & Redis缓存管理配置类实现
    3、在代码生成完成后，同步构建VUE项目，最后再返回完成结果

## 第十六次提交 系统优化2 - 安全性优化 & 稳定性优化

    1、流量保护 - 通过Redisson分布式的基于令牌桶算法的 RRateLimiter实现限流
    2、Prompt 安全审查 - 通过longchain4j的护轨机制实现输入内容护轨
    3、通过输出护轨与重试机制保证系统的稳定性
    4、通过再AiService中设置参数限制调用工具次数上限

## 第十七次提交 上线运行-Release0.0.1
在服务器中部署前端、后端、Redis、MySQL以及Node环境等运行环境，项目上线后可以正常运行
