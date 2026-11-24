# السؤال الثاني (Spring Boot): الأجوبة النظرية

> القيم والأمثلة أدناه مأخوذة من Spring Boot 4.2.0، وهو الإصدار المستخدم في مشروعي order-service و user-service، وقد شُغّلت فعلياً.

## 1. أهم ميزات إطار عمل Spring Boot

| الميزة | الشرح | مثال من مشروعنا |
|---|---|---|
| **الإعداد التلقائي (Auto-configuration)** | يفحص Spring Boot المكتبات الموجودة في المشروع (classpath) ويُنشئ الـBeans اللازمة تلقائياً بإعدادات افتراضية معقولة، فلا نكتب إعدادات يدوية أو ملفات XML. | وجود `h2` و `data-jpa` كافٍ ليُنشئ Spring Boot الـ`DataSource` و `EntityManager` بنفسه، ووجود `grpc-server` كافٍ ليشغّل مخدم gRPC. |
| **حزم البداية (Starters)** | كل حزمة تجمع المكتبات المتوافقة لغرض معين بإصدارات مضبوطة مسبقاً، فلا نبحث عن الإصدار المناسب لكل مكتبة. | `spring-boot-starter-webmvc` و `spring-boot-starter-data-jpa` و `spring-boot-starter-grpc-client`. |
| **مخدم مدمج (Embedded Server)** | التطبيق يحوي مخدم الويب داخله (Tomcat افتراضياً، أو Jetty)، فلا نحتاج تثبيت مخدم ونشر ملف WAR عليه. | order-service يشغّل Tomcat على المنفذ 8080 من تلقاء نفسه. |
| **تطبيق مستقل قابل للتشغيل** | يُبنى التطبيق كملف JAR واحد (fat jar) يحوي كل شيء، ويُشغّل بأمر `java -jar`. | `java -jar order-service-0.0.1-SNAPSHOT.jar` |
| **الإعداد الخارجي (Externalized Configuration)** | الإعدادات في `application.properties` أو `application.yml`، أو متغيرات البيئة، أو وسطاء سطر الأوامر، دون تعديل الكود. وتدعم الـProfiles (مثل dev و prod) لاختلاف الإعدادات بين البيئات. | `server.port=8080` و `spring.grpc.client.channel.user-service.target=static://localhost:9090` |
| **«الاصطلاح بدل الإعداد» (Opinionated Defaults)** | لكل شيء قيمة افتراضية جاهزة، ونغيّر فقط ما نحتاجه. | المنفذ الافتراضي 8080، وتطبيق قاعدة H2 في الذاكرة. |
| **إدارة الإصدارات (Dependency Management)** | الأب `spring-boot-starter-parent` يحدد إصدارات مئات المكتبات المتوافقة معاً، فنكتب الحزمة بلا رقم إصدار. | لم نكتب رقم إصدار لـ gRPC أو Protobuf أو H2 في pom.xml. |
| **Spring Boot Actuator** | نقاط جاهزة لمراقبة التطبيق في بيئة الإنتاج: حالته (`/actuator/health`)، وقياساته (metrics)، وسجلاته. | غير مستخدم في التمرين، ويُضاف بحزمة واحدة. |
| **دعم الاختبار** | حزم اختبار جاهزة مثل `@SpringBootTest` واختبار طبقة واحدة كـ`@WebMvcTest` و `@DataJpaTest`. | `OrderServiceApplicationTests` |
| **سرعة البدء (Spring Initializr)** | يولّد موقع start.spring.io هيكل المشروع كاملاً مع الحزم المختارة. | منه أنشأنا المشروعين. |

**الخلاصة:** يسمح Spring Boot ببناء تطبيقات وخدمات مصغّرة (Microservices) جاهزة للإنتاج بسرعة. المطوّر يركّز على منطق العمل، ويتكفّل الإطار بالإعدادات والبنية التحتية.

## 2. مجمّع النياسب (Thread Pool) الافتراضي في Spring Boot

### ما هو؟

عند تفعيل التنفيذ غير المتزامن بـ `@EnableAsync`، يُنشئ Spring Boot تلقائياً Bean اسمه `applicationTaskExecutor` من النوع **`ThreadPoolTaskExecutor`**. هذا الصف غلاف لـ`ThreadPoolExecutor` في Java، وهو الذي ينفّذ المناهج المعلَّمة بـ`@Async`. يُستخدم أيضاً في المعالجة غير المتزامنة لطلبات Spring MVC.

**قيمه الافتراضية (الخصائص تحت `spring.task.execution.*`):**

| الخاصية | القيمة الافتراضية | المعنى |
|---|---|---|
| `pool.core-size` | **8** | عدد النياسب الأساسية |
| `pool.max-size` | غير محدود (`Integer.MAX_VALUE`) | الحد الأعظمي للنياسب |
| `pool.queue-capacity` | غير محدودة | سعة طابور المهام المنتظرة |
| `pool.keep-alive` | 60s | مدة بقاء النيسب الزائد خاملاً قبل إنهائه |
| `pool.allow-core-thread-timeout` | true | يسمح بإنهاء النياسب الأساسية أيضاً عند الخمول |
| `thread-name-prefix` | `task-` | بادئة أسماء النياسب: task-1، task-2، ... |

⚠️ **ملاحظة مهمة:** الطابور غير محدود، لذلك لا يتجاوز عدد النياسب فعلياً 8 أبداً. النياسب الإضافية لا تُنشأ إلا عندما يمتلئ الطابور، وهو لا يمتلئ. فالحد الأعظمي هنا لا أثر له، ولهذا يُنصح عادة بتحديد سعة الطابور.

**مجمّعات أخرى يُنشئها Spring Boot:**
- **`ThreadPoolTaskScheduler`** للمهام المجدولة بـ`@Scheduled`: نيسب **واحد** افتراضياً (`spring.task.scheduling.pool.size=1`) وبادئة أسمائه `scheduling-`.
- **مجمّع نياسب Tomcat** لطلبات HTTP، وهو منفصل عما سبق: الحد الأعظمي **200** نيسب (`server.tomcat.threads.max`)، والحد الأدنى **10** نياسب احتياطية (`server.tomcat.threads.min-spare`).
- **النياسب الافتراضية (Virtual Threads):** عند تفعيل `spring.threads.virtual.enabled=true` على Java 21، يستعمل Spring Boot `SimpleAsyncTaskExecutor` ينشئ نيسباً افتراضياً خفيفاً لكل مهمة بدل المجمّع التقليدي.

### الطريقة الأولى: تغيير سلوك المجمّع الافتراضي من application.properties

```properties
spring.task.execution.pool.core-size=4
spring.task.execution.pool.max-size=16
spring.task.execution.pool.queue-capacity=100
spring.task.execution.pool.keep-alive=30s
spring.task.execution.thread-name-prefix=myapp-
spring.task.execution.shutdown.await-termination=true
spring.task.execution.shutdown.await-termination-period=10s
```

### الطريقة الثانية: إنشاء مجمّع نياسب مخصص

```java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "reportExecutor")
    public Executor reportExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);          // النياسب الأساسية
        executor.setMaxPoolSize(4);           // الحد الأعظمي
        executor.setQueueCapacity(50);        // سعة الطابور
        executor.setKeepAliveSeconds(30);
        executor.setThreadNamePrefix("report-");
        // عند امتلاء الطابور والنياسب: تُنفَّذ المهمة في نيسب المستدعي بدل رفضها
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }
}
```

ثم نحدد المجمّع باسمه عند الاستخدام:

```java
@Service
public class Jobs {
    @Async                    // المجمّع الافتراضي
    public void defaultPool(int i) { ... }

    @Async("reportExecutor")  // المجمّع المخصص
    public void customPool(int i) { ... }
}
```

**ترتيب عمل `ThreadPoolTaskExecutor` عند وصول مهمة:**
1. إذا كان عدد النياسب أقل من `corePoolSize`، يُنشئ نيسباً جديداً.
2. وإلا يضع المهمة في الطابور ما دام فيه مكان.
3. إذا امتلأ الطابور، يُنشئ نياسب إضافية حتى `maxPoolSize`.
4. إذا امتلأ كل شيء، يطبّق سياسة الرفض (`RejectedExecutionHandler`): رمي استثناء، أو التنفيذ في نيسب المستدعي، أو إهمال المهمة.

### نتيجة التجربة، وتنبيه مهم

شغّلتُ المثال السابق فعلياً، وطبعت كل مهمة اسم النيسب الذي نفّذها:

| الحالة | `@Async` (الافتراضي) | `@Async("reportExecutor")` |
|---|---|---|
| بلا مجمّع مخصص، مع `core-size=3` و `thread-name-prefix=myapp-` | `myapp-1`، `myapp-2`، `myapp-3` | - |
| مع المجمّع المخصص | `SimpleAsyncTaskExecutor-1`، `-2`، `-3` ⚠️ | `report-1`، `report-2` |
| مع المجمّع المخصص + `spring.task.execution.mode=force` | `myapp-1`، `myapp-2`، `myapp-3` | `report-1`، `report-2` |

**التفسير:** عندما نعرّف أي Bean من النوع `Executor`، يتراجع Spring Boot عن إنشاء مجمّعه الافتراضي `applicationTaskExecutor`. هذا سلوك الإعداد التلقائي: «إن عرّفتَ Bean بنفسك فلن أُنشئ مثله». عندها تذهب مناهج `@Async` التي لا تحدد اسم مجمّع إلى `SimpleAsyncTaskExecutor`، وهو يُنشئ نيسباً جديداً لكل مهمة دون أي حد.

**الحلول:**
- **الخاصية `spring.task.execution.mode=force`:** تُبقي المجمّع الافتراضي موجوداً بجانب المخصص (الصف الأخير في الجدول).
- **تسمية الـBean باسم `taskExecutor`:** يصبح عندها هو المجمّع الافتراضي لكل `@Async`.
- **تحقيق الواجهة `AsyncConfigurer`:** وفيها المنهج `getAsyncExecutor()` يحدد المجمّع الافتراضي صراحة.
