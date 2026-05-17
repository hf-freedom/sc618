<template>
  <div class="risk">
    <el-card shadow="hover">
      <template #header>
        <span>风险预警</span>
      </template>
      <el-table :data="riskList" border stripe>
        <el-table-column prop="id" label="预警编号" width="250" />
        <el-table-column prop="employeeName" label="员工姓名" width="100" />
        <el-table-column prop="alertType" label="预警类型" width="150">
          <template #default="scope">
            <el-tag type="warning">{{ scope.row.alertType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="reimbursementCount" label="报销数量" width="100" />
        <el-table-column prop="alertTime" label="预警时间" width="180" />
        <el-table-column prop="resolved" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.resolved ? 'success' : 'danger'">
              {{ scope.row.resolved ? '已处理' : '未处理' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Risk',
  data() {
    return {
      riskList: []
    }
  },
  mounted() {
    this.loadRisks()
  },
  methods: {
    async loadRisks() {
      try {
        const response = await axios.get('http://localhost:8003/api/reimbursement/risk-alerts')
        this.riskList = response.data
      } catch (error) {
        console.error('加载风险预警失败', error)
      }
    }
  }
}
</script>

<style scoped>
.risk {
  padding: 0;
}
</style>
