<template>
  <div class="home">
    <el-card shadow="hover">
      <template #header>
        <span>欢迎使用企业报销审批系统</span>
      </template>
      <el-row :gutter="20">
        <el-col :span="6">
          <el-statistic title="报销总金额" :value="stats.totalAmount" prefix="¥" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="报销单数量" :value="stats.totalCount" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="待审批" :value="stats.pendingCount">
            <template #suffix> 笔</template>
          </el-statistic>
        </el-col>
        <el-col :span="6">
          <el-statistic title="已完成" :value="stats.completedCount">
            <template #suffix> 笔</template>
          </el-statistic>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="hover" style="margin-top: 20px">
      <template #header>
        <span>快速操作</span>
      </template>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-button type="primary" size="large" style="width: 100%; height: 80px" @click="$router.push('/create')">
            提交报销申请
          </el-button>
        </el-col>
        <el-col :span="8">
          <el-button type="success" size="large" style="width: 100%; height: 80px" @click="$router.push('/approval')">
            处理审批任务
          </el-button>
        </el-col>
        <el-col :span="8">
          <el-button type="info" size="large" style="width: 100%; height: 80px" @click="$router.push('/my')">
            查看我的报销
          </el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="hover" style="margin-top: 20px">
      <template #header>
        <span>审批流程说明</span>
      </template>
      <el-steps direction="vertical" :active="4" finish-status="success">
        <el-step title="员工提交报销单" description="员工填写报销信息，选择费用类型，系统自动校验金额上限" />
        <el-step title="部门负责人审批" description="部门负责人审核报销内容，可批准或驳回" />
        <el-step title="财务复核" description="财务人员复核发票信息，校验发票是否重复使用" />
        <el-step title="打款完成" description="审批通过后生成待打款记录，打款完成后更新状态为已完成" />
      </el-steps>
    </el-card>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'HomeView',
  data() {
    return {
      stats: {
        totalAmount: 0,
        totalCount: 0,
        pendingCount: 0,
        completedCount: 0
      }
    }
  },
  mounted() {
    this.loadStats()
  },
  activated() {
    this.loadStats()
  },
  watch: {
    $route(to) {
      if (to.path === '/') {
        this.loadStats()
      }
    }
  },
  methods: {
    async loadStats() {
      try {
        const response = await axios.get('http://localhost:8003/api/reimbursement/all')
        const list = response.data
        this.stats.totalCount = list.length
        this.stats.totalAmount = list.reduce((sum, item) => sum + Number(item.amount || 0), 0).toFixed(2)
        this.stats.pendingCount = list.filter(item => 
          ['PENDING_APPROVAL', 'MANAGER_APPROVED', 'FINANCE_REVIEW'].includes(item.status)
        ).length
        this.stats.completedCount = list.filter(item => item.status === 'COMPLETED').length
      } catch (error) {
        console.error('加载统计数据失败', error)
      }
    }
  }
}
</script>

<style scoped>
.home {
  padding: 0;
}
</style>
