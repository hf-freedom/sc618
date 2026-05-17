<template>
  <div class="payment">
    <el-card shadow="hover">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center">
          <span>打款记录</span>
          <el-row :gutter="10">
            <el-tag type="warning">待打款: {{ pendingCount }}</el-tag>
            <el-tag type="success">已完成: {{ completedCount }}</el-tag>
          </el-row>
        </div>
      </template>
      <el-table :data="paymentList" border stripe v-loading="loading">
        <el-table-column prop="id" label="打款单号" width="250" />
        <el-table-column prop="reimbursementId" label="报销单号" width="250" />
        <el-table-column prop="employeeName" label="收款人" width="100" />
        <el-table-column prop="amount" label="金额" width="120" align="right">
          <template #default="scope">
            <span style="font-weight: bold; color: #f56c6c">¥{{ formatAmount(scope.row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="paymentTime" label="打款时间" width="180" />
        <el-table-column prop="paymentStatus" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.paymentStatus === 'COMPLETED' ? 'success' : 'warning'" size="small">
              {{ scope.row.paymentStatus === 'COMPLETED' ? '已打款' : '待打款' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.paymentStatus !== 'COMPLETED'"
              size="small"
              type="primary"
              @click="completePayment(scope.row)"
            >
              确认打款
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="paymentList.length === 0" description="暂无打款记录" :image-size="100" style="margin-top: 20px" />
    </el-card>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  name: 'Payment',
  data() {
    return {
      paymentList: [],
      loading: false
    }
  },
  computed: {
    pendingCount() {
      return this.paymentList.filter(p => p.paymentStatus !== 'COMPLETED').length
    },
    completedCount() {
      return this.paymentList.filter(p => p.paymentStatus === 'COMPLETED').length
    }
  },
  mounted() {
    this.loadPaymentRecords()
  },
  activated() {
    this.loadPaymentRecords()
  },
  watch: {
    $route(to) {
      if (to.path === '/payment') {
        this.loadPaymentRecords()
      }
    }
  },
  methods: {
    async loadPaymentRecords() {
      this.loading = true
      try {
        const response = await axios.get('http://localhost:8003/api/reimbursement/payments')
        this.paymentList = response.data
      } catch (error) {
        console.error('加载打款记录失败', error)
        this.$message.error('加载打款记录失败')
      } finally {
        this.loading = false
      }
    },
    async completePayment(row) {
      try {
        await axios.post(`http://localhost:8003/api/reimbursement/complete-payment/${row.reimbursementId}`)
        this.$message.success('打款完成')
        this.loadPaymentRecords()
      } catch (error) {
        this.$message.error('打款失败')
      }
    },
    formatAmount(amount) {
      if (amount === null || amount === undefined || amount === '') return '0.00'
      return parseFloat(amount).toFixed(2)
    }
  }
}
</script>

<style scoped>
.payment {
  padding: 0;
}
</style>
