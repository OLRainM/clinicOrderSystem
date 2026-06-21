<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8" />
    <style>
        body { font-family: SimSun, "Microsoft YaHei", "Noto Sans CJK SC", serif; color: #111; line-height: 1.6; }
        .prescription { width: 680px; margin: 0 auto; }
        .title { text-align: center; font-size: 24px; font-weight: bold; margin-bottom: 16px; }
        .meta { margin-bottom: 12px; line-height: 1.8; }
        table { width: 100%; border-collapse: collapse; margin-top: 12px; }
        th, td { border: 1px solid #555; padding: 8px; }
        th { background: #f0f3f8; }
        .footer { margin-top: 28px; text-align: right; }
    </style>
</head>
<body>
<div class="prescription">
<div class="title">诊所处方笺</div>
<div class="meta">
    患者：${patientName!''}<br/>
    订单号：${orderNo!''}<br/>
    医生：${doctorName!''}<br/>
    主诉：${symptoms!''}<br/>
    诊断：${diagnosis!''}<br/>
    开具时间：${createdAt!''}
</div>
<table>
    <thead>
    <tr><th>药品名称</th><th>单次剂量</th><th>用法</th></tr>
    </thead>
    <tbody>
    <#list items as item>
        <tr>
            <td>${item.medicine_name!''}</td>
            <td>${item.dosage!''}</td>
            <td>${item.usage_instruction!''}</td>
        </tr>
    </#list>
    </tbody>
</table>
<div class="footer">医生签名：${doctorName!''}</div>
</div>
</body>
</html>
