# السؤال الأول: تطبيق محادثة شبيه بـ Skype باستخدام Java RMI

الكود في الحزمة `org.ds.skype` ضمن مشروع `rmi-lab`، وأسماء الواجهات والصفوف مطابقة لملف الحالة الدراسية:

```
org/ds/skype/
  shared/IServerServices.java     واجهة خدمات المخدم (نسخة عند المخدم والزبون)
  shared/IClientListener.java     واجهة يحققها الزبون ويستدعيها المخدم (Callback)
  server/Server_Services_Imp.java تحقيق خدمات المخدم: الغرف، الحسابات، الرسائل
  server/Server.java              يشغّل RMI Registry على المنفذ 6000 ويسجل الخدمة باسم rmiServer
  client/ClientListener.java      الغرض البعيد الخاص بالزبون (يستقبل الرسائل)
  client/Client.java              برنامج الزبون (واجهة سطر أوامر)
```

## التشغيل من IntelliJ

1. شغّلي `org.ds.skype.server.Server`، فيظهر: `Chat server ready: registry on port 6000 ...`
   (يُنشئ المخدم غرفتين جاهزتين: `General` بسعة 10 و `Study` بسعة 3).
2. شغّلي `org.ds.skype.client.Client` **مرتين أو ثلاثاً** (زبون لكل مستخدم).
   لتشغيل أكثر من نسخة: Run → Edit Configurations → Client → Modify options → **Allow multiple instances**.
3. في كل زبون اكتبي الأوامر، مثلاً:

| الزبون الأول | الزبون الثاني |
|---|---|
| `/signup Ahmad 123 Ahmad Ali` | `/signup Sara 456 Sara Omar` |
| `/login Ahmad 123 General` | `/login Sara 456 General` |
| `/msg Sara Hi Sara` (عبر المخدم) | `/p2p Ahmad Hello` (مباشرة، دون المخدم) |
| `Hello everyone` (لكل الغرفة) | `/users`  ثم  `/rooms` |
| `/logout`  ثم  `/quit` | `/quit` |

الأوامر الأخرى: `/addroom <name> [capacity]`، `/removeroom <name>`، `/help`.

## نتيجة تشغيل فعلية (مخدم + زبونان)

**الزبون Ahmad:**
```
> Signed in to General as Ahmad. In room: [Ahmad]
<< *** Sara joined the room
> (sent via server)
> (sent directly to Sara, not through the server)
<< [Sara -> you] Hello Ahmad (via server)
<< [P2P Sara -> you] Hello Ahmad (peer-to-peer)
<< [General] Sara: Hello everyone in General
```

**سجل المخدم:**
```
signin(Ahmad, General) -> 1
AddReceiveListener(Ahmad) - listeners: [Ahmad]
signin(Sara, General) -> 1
AddReceiveListener(Sara) - listeners: [Sara, Ahmad]
SendMessage(to=Sara, room=General) -> 1
SendMessage(to=Ahmad, room=General) -> 1
SendBroadCatMessage(from=Sara, room=General) -> 1
```
أُرسلت أربع رسائل خاصة، لكن المخدم سجّل اثنتين فقط (`SendMessage`). الرسالتان الأخريان أُرسلتا بـ `/p2p` من زبون إلى زبون مباشرة، فلم تمرّا على المخدم أصلاً.

## أثر مفهوم Callback في الاستدعاء العكسي من المخدم إلى الزبون

في RMI العادي (التمرينان EX1 و EX2) الزبون فقط يستدعي المخدم: يطلب فيأخذ الرد، والمخدم لا يعرف الزبون ولا يستطيع أن يبادر بالاتصال به. في تطبيق محادثة هذا لا يكفي، لأن الرسالة تصل إلى المستقبِل في وقت لا يعرفه. فلو لم يوجد Callback لاضطر كل زبون أن يسأل المخدم كل ثانية «هل لي رسائل جديدة؟» (Polling).

**كيف حللنا ذلك بالـ Callback:**

1. **الزبون يصبح غرضاً بعيداً:** الصف `ClientListener` يحقق الواجهة `IClientListener` ويرث `UnicastRemoteObject`. عند إنشائه يُصدَّر (export) فيصبح قادراً على استقبال استدعاءات عن بعد، تماماً كغرض المخدم.
2. **تسجيل الزبون عند المخدم:** بعد `signin` يستدعي الزبون `remote_ref.AddReceiveListener(listener)`. ما يصل إلى المخدم ليس نسخة من الغرض بل **Stub** يشير إلى جهاز الزبون، فيخزنه المخدم في `listeners` (اسم المستخدم ← Stub).
3. **المخدم يستدعي الزبون:** عندما يرسل Ahmad إلى Sara يستدعي `SendMessage`، فيبحث المخدم عن Stub الخاص بـ Sara ويستدعي `ReceiveMessage` عليه. يُنفَّذ هذا المنهج **على جهاز Sara** فتظهر الرسالة عندها فوراً. هنا انعكس الدور: صار المخدم «زبوناً» والزبون «مخدماً».
4. **إشعارات أخرى بنفس الآلية:** دخول مستخدم أو خروجه (`*** Sara joined the room`)، وحذف غرفة، والرسائل الجماعية (`SendBroadCatMessage`). كل هذه يرسلها المخدم من تلقاء نفسه دون أن يطلبها الزبون.
5. **مبدأ الند للند (Peer-to-Peer):** يستدعي المخدم المنهج `getListeners` على كل زبون في الغرفة، ويمرر له قائمة Stubs جميع الأعضاء. بعدها يجد الزبون المستقبِل في قائمته (`getClientListener("Sara")`) ويستدعي `ReceiveMessage` عليه مباشرة. صار المخدم يُستعمل فقط لتوزيع العناوين، والرسائل نفسها لا تمر عليه، فيقل الحمل على العقدة المركزية.

**ما الذي تغيّر بفضل الـ Callback:**
- **الفورية:** تصل الرسالة لحظة إرسالها، بلا Polling ولا استدعاءات فارغة على الشبكة.
- **التواصل في الاتجاهين:** كلا الطرفين يصدّر غرضاً ويستدعي غرض الآخر.
- **تبعات يجب معالجتها:**
  - **الزبون المنقطع:** إذا أُغلق زبون فجأة يرمي الاستدعاء العكسي `RemoteException`، فيحذفه المخدم من الغرفة (`deliver` في `Server_Services_Imp`).
  - **التزامن:** RMI ينفذ كل استدعاء في خيط منفصل، لذلك مناهج المخدم `synchronized`.
  - **المنفذ:** الزبون يحتاج منفذاً مفتوحاً يستقبل عليه، ولهذا قد يحتاج إعداد جدار حماية في شبكة حقيقية.

## ثلاث حالات استخدام نستفيد فيها من RMI

1. **أنظمة المحادثة والإشعارات الفورية:** مثل هذا التطبيق، أو لوحة مراقبة يرسل لها المخدم تنبيهاً عند تجاوز حساس لقيمة معينة. هنا يفيد الـ Callback لدفع البيانات إلى الزبائن فوراً.
2. **الحوسبة الموزعة وتوزيع المهام:** مخدم رئيسي يقسم عملاً حسابياً ثقيلاً (معالجة صور، محاكاة، حساب مصفوفات) إلى أجزاء. يرسل كل جزء إلى عدة أجهزة عاملة باستدعاء منهج بعيد مثل `compute(task)`، ثم يجمع النتائج. وبما أن RMI ينقل الأغراض كاملة عبر Serialization، يمكن إرسال المهمة نفسها كغرض.
3. **التطبيقات المؤسساتية متعددة الطبقات (زبون / مخدم منطق أعمال):** مثل نظام مصرفي أو نظام حجوزات أو نظام مستودعات داخل شركة. تطبيقات الموظفين المكتوبة بـ Java تستدعي مناهج مثل `transfer(from, to, amount)` أو `reserveSeat(flight, seat)` على مخدم مركزي يملك قاعدة البيانات. هكذا يبقى المنطق والبيانات في مكان واحد، ويتعامل المبرمج مع المخدم كأنه غرض محلي دون كتابة بروتوكول شبكي. وعلى هذه الفكرة بُنيت تقنية Java EE / EJB.
