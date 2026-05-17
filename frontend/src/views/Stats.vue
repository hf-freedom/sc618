<template>
  <div class="stats">
    <el-card shadow="hover">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>部门统计</span>
          <el-select v-model="selectedDepartment" placeholder="选择部门" @change="loadStats" style="width: 150px">
            <el-option
              v-for="dept in departments"
              :key="dept"
              :label="dept"
              :value="dept"
            />
          </el-select>
        </div>
      </template>
      <el-row :gutter="20">
        <el-col :span="6">
          <el-statistic title="报销总金额" :value="formatAmount(deptStats.totalAmount)" prefix="¥" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="报销总数量" :value="deptStats.totalCount || 0" />
        </el-col>
        <el-col :span="6">
          <el-statistic title="已批准" :value="deptStats.approvedCount || 0">
            <template #suffix> 笔</template>
          </el-statistic>
        </el-col>
        <el-col :span="6">
          <el-statistic title="已驳回" :value="deptStats.rejectedCount || 0">
            <template #suffix> 笔</template>
          </el-statistic>
        </el-col>
      </el-row>
      <el-divider />
      <el-descriptions title="详细信息" border :column="2">
        <el-descriptions-item label="平均审批时长">
          <span style="font-size: 16px; font-weight: bold">{{ deptStats.averageApprovalDays || 0 }}</span> 天
        </el-descriptions-item>
        <el-descriptions-item label="平均单笔金额">
          ¥{{ deptStats.totalCount > 0 ? formatAmount(deptStats.totalAmount / deptStats.totalCount) : '0.00' }}
        </el-descriptions-item>
      </el-descriptions>
      <el-divider />
      <h4 style="margin-bottom: 10px">驳回原因分布</h4>
      <div v-if="Object.keys(deptStats.rejectReasons || {}).length > 0">
        <el-tag v-for="(count, reason) in deptStats.rejectReasons" :key="reason" type="danger" style="margin: 5px">
          {{ reason }}: {{ count }} 次
        </el-tag>
      </div>
      <el-empty v-else description="暂无驳回记录" :image-size="80" />
    </el-card>

    <el-card shadow="hover" style="margin-top: 20px">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>所有报销单</span>
          <el-tag type="info">共 {{ allReimbursements.length }} 笔</el-tag>
        </div>
      </template>
      <el-table :data="allReimbursements" border stripe max-height="400" v-loading="allReimbursements.length === 0">
        <el-table-column prop="employeeName" label="员工姓名" width="100" />
        <el-table-column prop="department" label="部门" width="100" />
        <el-table-column prop="expenseType" label="费用类型" width="120">
          <template #default="scope">
            {{ getExpenseTypeName(scope.row.expenseType) }}
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="120" align="right">
          <template #default="scope">
            <span style="font-weight: bold; color: #f56c6c">¥{{ formatAmount(scope.row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="invoiceNumber" label="发票编号" width="150" />
        <el-table-column prop="status" label="状态" width="130">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">
              {{ getStatusName(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submitTime" label="提交时间" width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Stats',
  data() {
    return {
      selectedDepartment: '',
      departments: [],
      deptStats: {
        totalAmount: 0,
        totalCount: 0,
        approvedCount: 0,
        rejectedCount: 0,
        averageApprovalDays: 0,
        rejectReasons: {}
      },
      allReimbursements: []
    }
  },
  mounted() {
    this.loadDepartments()
    this.loadAllReimbursements()
  },
  activated() {
    this.loadAllReimbursements()
    if (this.selectedDepartment) {
      this.loadStats()
    }
  },
  watch: {
    $route(to) {
      if (to.path === '/stats') {
        this.loadAllReimbursements()
        if (this.selectedDepartment) {
          this.loadStats()
        }
      }
    }
  },
  methods: {
    async loadDepartments() {
      try {
        const response = await axios.get('http://localhost:8003/api/reimbursement/departments')
        this.departments = response.data
        if (this.departments.length > 0 && !this.selectedDepartment) {
          this.selectedDepartment = this.departments[0]
          this.loadStats()
        }
      } catch (error) {
        console.error('加载部门列表失败', error)
        this.departments = ['技术部', '市场部', '人事部']
        this.selectedDepartment = '技术部'
        this.loadStats()
      }
    },
    async loadStats() {
      if (!this.selectedDepartment) return
      try {
        const response = await axios.get(
          `http://localhost:8003/api/reimbursement/stats/department/${this.selectedDepartment}`
        )
        this.deptStats = response.data
      } catch (error) {
        console.error('加载统计数据失败', error)
      }
    },
    async loadAllReimbursements() {
      try {
        const response = await axios.get('http://localhost:8003/api/reimbursement/all')
        this.allReimbursements = response.data
      } catch (error) {
        console.error('加载报销单失败', error)
      }
    },
    formatAmount(amount) {
      if (amount === null || amount === undefined || amount === '') return '0.00'
      return parseFloat(amount).toFixed(2)
    },
    getExpenseTypeName(type) {
      const names = {
        TRANSPORTATION: '交通费',
        ACCOMMODATION: '住宿费',
        MEAL: '餐饮费',
        OFFICE_SUPPLY: '办公用品',
        TRAVEL: '差旅费',
        OTHER: '其他'
      }
      return names[type] || type
    },
    getStatusName(status) {
      const names = {
        DRAFT: '草稿',
        PENDING_APPROVAL: '待审批',
        MANAGER_APPROVED: '经理已批准',
        FINANCE_REVIEW: '财务复核',
        APPROVED: '已批准',
        REJECTED: '已驳回',
        PAYMENT_PENDING: '待打款',
        COMPLETED: '已完成'
      }
      return names[status] || status
    },
    getStatusType(status) {
      const types = {
        DRAFT: 'info',
        PENDING_APPROVAL: 'warning',
        MANAGER_APPROVED: 'primary',
        FINANCE_REVIEW: 'primary',
        APPROVED: 'success',
        REJECTED: 'danger',
        PAYMENT_PENDING: 'warning',
        COMPLETED: 'success'
      }
      return types[status] || 'info'
    }
  }
}
</script>

<style scoped>
.stats {
  padding: 0;
}
</style>
