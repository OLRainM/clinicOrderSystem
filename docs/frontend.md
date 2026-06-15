### 前端页面路由与跳转流转图 (JSP + AJAX 混合架构)

**【路由设计原则】**
1. **页面级跳转 (Page Load)：** 仅用于跨越大的业务模块（如从首页进入个人中心）。通过 Spring Boot `@Controller` 返回 JSP 视图。
2. **无感交互 (AJAX + Modal)：** 抢号、取消、改签、开处方等高频核心动作，**绝对禁止整页刷新**。统一通过 `@RestController` 交互 JSON，前端结合 SweetAlert2 弹窗和 DOM 局部更新。
3. **安全拦截重定向：** 所有 `/user/**`、`/doctor/**`、`/admin/**` 的路径必须经过过滤器（Filter/Interceptor），未登录直接 `302 Redirect` 至统一登录页。

---

#### 一、 患者端 (Patient Flow)

患者端采用“所见即所得”的扁平化跳转逻辑。

* **1. 门户首页 (`GET /`)**
    * **展示：** 诊所介绍、快速科室入口。
    * **操作：** 点击“某科室” $\rightarrow$ **跳转至** $\rightarrow$ **医生列表页** (`/department/{id}`)。
* **2. 医生排班列表页 (`GET /department/{id}`)**
    * **展示：** 该科室下所有医生的信息卡片。
    * **操作：** 点击“查看排班” $\rightarrow$ **【不跳转】** 触发 AJAX，拉取该医生近7天号源，弹出 Bootstrap Modal（模态框）。
    * **核心动作：** 在模态框内点击“预约” $\rightarrow$ 触发抢号请求 $\rightarrow$ 若成功，弹出 SweetAlert 提示“抢号成功” $\rightarrow$ 倒计时3秒后自动 **跳转至** $\rightarrow$ **个人中心-我的预约页** (`/user/dashboard`)。
* **3. 个人中心 - 我的预约 (`GET /user/dashboard`)**
    * **展示：** 包含 Tab 切换的个人控制台。默认显示订单列表。
    * **操作 [支付]：** 点击“去支付” $\rightarrow$ **跳转至** $\rightarrow$ 收银台页面 (`/payment/checkout`) $\rightarrow$ 支付成功后 **跳回** 个人中心。
    * **操作 [取消]：** 点击“取消预约” $\rightarrow$ **【不跳转】** AJAX 提交，成功后 `window.location.reload()` 刷新当前列表状态。
    * **操作 [改签]：** 点击“改签” $\rightarrow$ **【不跳转】** 弹出时段选择模态框，AJAX 提交置换请求，成功后刷新列表。
* **4. 个人中心 - 电子病历 Tab (`GET /user/dashboard?tab=emr`)**
    * **操作 [查看]：** 点击某次就诊记录 $\rightarrow$ **【不跳转】** 侧边划出抽屉（Offcanvas）展示病历详情。
    * **操作 [下载处方]：** 点击“下载PDF” $\rightarrow$ 调用 `GET /api/prescription/download` $\rightarrow$ 浏览器直接触发底层下载机制，页面无任何闪烁。

---

#### 二、 医生端 (Doctor Flow)

医生在出诊期间，页面切换的成本极高，因此医生端采用**“单页面工作台”**理念。

* **1. 医生登录页 (`GET /login/doctor`)**
    * **操作：** 验证账密 $\rightarrow$ 成功后 **重定向至** $\rightarrow$ **医生工作台** (`/doctor/workspace`)。
* **2. 医生工作台 (`GET /doctor/workspace`)**
    * *(这是医生整天唯一需要面对的页面)*
    * **左侧：** 患者排队列表。
    * **操作：** 点击队列中的患者姓名 $\rightarrow$ **【不跳转】** 右侧主内容区通过 AJAX 拉取并渲染该患者的历史病历、今日挂号信息。
    * **提交动作：** 填写完确诊主诉、开具处方后，点击“完结问诊” $\rightarrow$ **【不跳转】** AJAX 提交表单 $\rightarrow$ 弹出成功提示 $\rightarrow$ 左侧队列自动将该患者移入“已看诊”，同时焦点自动切换至下一位患者。

---

#### 三、 管理员端 (Admin Flow)

管理员端偏向后台管理系统风格，采用经典的**左右分栏路由**。

* **1. 管理后台登录 (`GET /admin/login`)**
    * **操作：** 验证通过 $\rightarrow$ **重定向至** $\rightarrow$ **全局仪表盘** (`/admin/dashboard`)。
* **2. 核心大屏与管理框架 (`GET /admin/dashboard`)**
    * **页面结构：** 顶部全局 Header，左侧为功能导航栏（Dashboard、排班管理、医生管理、财务流水），右侧为内容区。
    * **菜单跳转：** 左侧导航菜单的点击使用标准页面跳转（如点击“排班管理”跳转至 `/admin/schedule/manage`）。
    * **数据刷新：** 在 Dashboard 页面内切换“今日/近7天”时 $\rightarrow$ **【不跳转】** AJAX 请求后端统计接口，重新渲染 ECharts 图表。